/*
 * Copyright 2006, Queensland University of Technology
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Author: Shaun Mangelsdorf
 * Creation Date: 09/11/2006
 *
 * Purpose: Responsible for initializing the SPEP.
 */
package com.qut.middleware.spep;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.crypto.KeystoreResolver;
import com.qut.middleware.crypto.exception.KeystoreResolverException;
import com.qut.middleware.crypto.impl.KeystoreResolverImpl;
import com.qut.middleware.metadata.cache.MetadataCache;
import com.qut.middleware.metadata.cache.impl.MetadataCacheImpl;
import com.qut.middleware.metadata.processor.DynamicMetadataUpdater;
import com.qut.middleware.metadata.processor.FormatHandler;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.metadata.processor.impl.DynamicMetadataUpdaterImpl;
import com.qut.middleware.metadata.processor.impl.MetadataProcessorImpl;
import com.qut.middleware.metadata.processor.impl.MetadataUpdateThread;
import com.qut.middleware.metadata.processor.saml.SAMLEntityDescriptorProcessor;
import com.qut.middleware.metadata.processor.saml.impl.SAMLIdentityProviderProcessor;
import com.qut.middleware.metadata.processor.saml.impl.SAMLMetadataFormatHandler;
import com.qut.middleware.metadata.processor.saml.impl.SAMLServiceProviderProcessor;
import com.qut.middleware.metadata.source.DynamicMetadataSource;
import com.qut.middleware.metadata.source.MetadataSource;
import com.qut.middleware.metadata.source.saml.impl.SAMLURLMetadataSource;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.handler.SOAPHandler;
import com.qut.middleware.saml2.handler.impl.SOAPv11Handler;
import com.qut.middleware.saml2.handler.impl.SOAPv12Handler;
import com.qut.middleware.saml2.identifier.IdentifierCache;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.identifier.impl.IdentifierCacheImpl;
import com.qut.middleware.saml2.identifier.impl.IdentifierGeneratorImpl;
import com.qut.middleware.saml2.validator.SAMLValidator;
import com.qut.middleware.saml2.validator.impl.SAMLValidatorImpl;
import com.qut.middleware.spep.attribute.impl.AttributeProcessorImpl;
import com.qut.middleware.spep.authn.impl.AuthnProcessorImpl;
import com.qut.middleware.spep.exception.SPEPInitializationException;
import com.qut.middleware.spep.impl.IdentifierCacheMonitor;
import com.qut.middleware.spep.impl.SPEPImpl;
import com.qut.middleware.spep.impl.SPEPProxyImpl;
import com.qut.middleware.spep.impl.StartupProcessorImpl;
import com.qut.middleware.spep.pep.PolicyEnforcementProcessor.decision;
import com.qut.middleware.spep.pep.impl.PolicyEnforcementProcessorImpl;
import com.qut.middleware.spep.pep.impl.SessionGroupCacheImpl;
import com.qut.middleware.spep.sessions.impl.SessionCacheImpl;
import com.qut.middleware.spep.ws.WSClient;
import com.qut.middleware.spep.ws.impl.WSClientImpl;
import com.qut.middleware.spep.ws.impl.WSProcessorImpl;

/** Responsible for initializing the SPEP. */
@SuppressWarnings("unused")
public class Initializer
{
	public interface ParameterValidator {
		boolean validate(String value);
	}
	/**
	 * Validates that a number is in fact numeric and is in the given range, if any.
	 */
	public class NumberValidator implements ParameterValidator {
		long min, max;
		public NumberValidator() { this(Long.MIN_VALUE, Long.MAX_VALUE); }
		public NumberValidator(long min) { this(min, Long.MAX_VALUE); }
		public NumberValidator(long min, long max) { this.min = min; this.max = max; }
		public boolean validate(String value) {
			try {
				Long num = Long.parseLong(value);
				return (num <= max) && (num >= min);
			} catch (NumberFormatException ex) {
				return false;
			}
		}
	}
	/**
	 * Validates that a string (sans whitespace) is non-empty.
	 */
	public class FreeFormStringValidator implements ParameterValidator {
		public boolean validate(String value) {
			return value.trim().length() > 0;
		}
	}
	/**
	 * Validates that a string matches the given regex.
	 */
	public class RegexStringValidator implements ParameterValidator {
		private String regex;
		public RegexStringValidator(String regex) {
			this.regex = regex;
		}
		public boolean validate(String value) {
			return Pattern.matches(this.regex, value);
		}
	}
	/**
	 * Validates that a URL can be parsed.
	 */
	public class URLValidator implements ParameterValidator {
		public boolean validate(String value) {
			try {
				new URL(value);
				return true;
			} catch (MalformedURLException e) {
				return false;
			}
		}
	}
	public class ExistingFileValidator implements ParameterValidator {
		public boolean validate(String value) {
			return new File(value).exists();
		}
	}

	private Initializer(){}

	/* Local logging instance */
	static private Logger logger = LoggerFactory.getLogger(Initializer.class.getName());

	private final static String DENY = "deny";
	private final static String PERMIT = "permit";

	private static String SPEP_CONFIGURATION_PATH = null;

	private static String resolveProperty(Properties properties, String propName, ParameterValidator validator) throws SPEPInitializationException
	{
		String tmpProp = properties.getProperty(propName).trim();

		if(tmpProp == null)
		{
			throw new SPEPInitializationException("Unable to find any value for requested property " + propName);
		}

		if(tmpProp.contains("${"+ ConfigurationConstants.SPEP_PATH_PROP + "}"))
		{
			tmpProp = tmpProp.replace("${"+ ConfigurationConstants.SPEP_PATH_PROP + "}", SPEP_CONFIGURATION_PATH);
		}

		if(! validator.validate(tmpProp)) {
			throw new SPEPInitializationException("Property was not in the expected format: " + propName);
		}

		return tmpProp;
	}

	/**
	 * @param context The servlet context in which to initialize a SPEP
	 * @return The SPEP for the given servlet context.
	 * @throws SPEPInitializationException
	 */
	public static synchronized SPEP init(ServletContext context) throws SPEPInitializationException
	{
		Initializer initializer = new Initializer();
		return initializer.doInit(context);
	}

	private SPEP doInit(ServletContext context) throws SPEPInitializationException {
		if(context == null)
		{
			throw new IllegalArgumentException(Messages.getString("Initializer.14"));  //$NON-NLS-1$
		}

		if(context.getAttribute(ConfigurationConstants.SERVLET_CONTEXT_NAME) == null)
		{
			SPEPImpl spep = new SPEPImpl();

			/* Determine the location of spep.data either from local spep config or from system property */
			InputStream varPropertyInputStream = context.getResourceAsStream(ConfigurationConstants.SPEP_CONFIG_LOCAL);
			if(varPropertyInputStream != null)
			{
				Properties varProperties = new Properties();
				try
				{
					varProperties.load(varPropertyInputStream);
				}
				catch (IOException e)
				{
					throw new SPEPInitializationException(Messages.getString("Initializer.0"), e); //$NON-NLS-1$
				}

				SPEP_CONFIGURATION_PATH = varProperties.getProperty(ConfigurationConstants.SPEP_PATH_PROP);
			}

			if(SPEP_CONFIGURATION_PATH != null && SPEP_CONFIGURATION_PATH.length() > 0)
				logger.info("Configured spep.data locally from spepvars.config, with a value of: " + SPEP_CONFIGURATION_PATH);
			else
			{
				SPEP_CONFIGURATION_PATH = System.getProperty(ConfigurationConstants.SPEP_PATH_PROP);
				if(SPEP_CONFIGURATION_PATH != null)
					logger.info("Configured spep.data from java property spep.data, with a value of: " + SPEP_CONFIGURATION_PATH);
				else
				{
					logger.error("Unable to resolve location of spep config and keystores from either local file of WEB-INF/spepvars.config (spep.data) or java property spep.data");
					throw new IllegalArgumentException("Unable to resolve location of spep config and keystores from either local file of WEB-INF/spepvars.config (spep.data) or java property spep.data");
				}
			}

			/* Get the core properties file */
			FileInputStream propertyInputStream;
			try
			{
				propertyInputStream = new FileInputStream( SPEP_CONFIGURATION_PATH + ConfigurationConstants.SPEP_CONFIG);
			}
			catch (FileNotFoundException e1)
			{
				throw new IllegalArgumentException(Messages.getString("Initializer.15") + SPEP_CONFIGURATION_PATH  +  ConfigurationConstants.SPEP_CONFIG); //$NON-NLS-1$
			}

			Properties properties = new Properties();
			try
			{
				properties.load(propertyInputStream);
			}
			catch (IOException e)
			{
				throw new SPEPInitializationException(Messages.getString("Initializer.0"), e); //$NON-NLS-1$
			}

			// Grab all the configuration data from the properties file.
			final String spepIdentifier = resolveProperty(properties, "spepIdentifier", new FreeFormStringValidator()); //$NON-NLS-1$
			String esoeIdentifier = resolveProperty(properties, "esoeIdentifier", new FreeFormStringValidator()); //$NON-NLS-1$
			String metadataUrl = resolveProperty(properties, "metadataUrl", new URLValidator()); //$NON-NLS-1$

			// Path, password and aliases for keystore
			String keystorePath = resolveProperty(properties, "keystorePath", new ExistingFileValidator()); //$NON-NLS-1$

			String keystorePassword = resolveProperty(properties, "keystorePassword", new FreeFormStringValidator()); //$NON-NLS-1$
			String spepKeyAlias = resolveProperty(properties, "spepKeyAlias", new FreeFormStringValidator()); //$NON-NLS-1$
			String spepKeyPassword = resolveProperty(properties, "spepKeyPassword", new FreeFormStringValidator());  //$NON-NLS-1$

			// Information line about the server
			String serverInfo = resolveProperty(properties, "serverInfo", new FreeFormStringValidator());  //$NON-NLS-1$

			// Node identifier configured in the metadata
			int nodeIndex = Integer.parseInt(resolveProperty(properties, "nodeIdentifier", new NumberValidator(0, 65536)));  //$NON-NLS-1$

			// Interval on which to refresh the metadata (seconds)
			int metadataInterval = Integer.parseInt(resolveProperty(properties, "metadataInterval", new NumberValidator(0))); //$NON-NLS-1$

			// Allowable time skew for SAML document expiry (seconds)
			int allowedTimeSkew = Integer.parseInt(resolveProperty(properties, "allowedTimeSkew", new NumberValidator(0))); //$NON-NLS-1$

			// Timeout and interval on which to check the session caches.
			long sessionCacheTimeout = Long.parseLong(resolveProperty(properties, "sessionCacheTimeout", new NumberValidator(0))); //$NON-NLS-1$
			long sessionCacheInterval = Long.parseLong(resolveProperty(properties, "sessionCacheInterval", new NumberValidator(0))); //$NON-NLS-1$

			// Timeout on data in the identifier cache. Should be longer than SAML document lifetime + time skew allowed
			long identifierCacheTimeout = Long.parseLong(resolveProperty(properties, "identifierCacheTimeout", new NumberValidator(0))); //$NON-NLS-1$

			// Time after which to retry SPEP startup if it failed.
			int spepStartupInterval = Integer.parseInt(resolveProperty(properties, "startupRetryInterval", new NumberValidator(0))); //$NON-NLS-1$

			// Default policy decision for LAXCML
			decision defaultPolicyDecision = decision.valueOf(resolveProperty(properties, "defaultPolicyDecision", new RegexStringValidator("(?i)(permit|deny)"))); //$NON-NLS-1$

			// Disabled SPEP components - default to false (don't disable)
			boolean disableAttributeQuery = Boolean.parseBoolean(properties.getProperty("disableAttributeQuery", "false"));
			boolean disablePolicyEnforcement = Boolean.parseBoolean(properties.getProperty("disablePolicyEnforcement", "false"));
			boolean disableSPEPStartup = Boolean.parseBoolean(properties.getProperty("disableSPEPStartup", "false"));

			// Run in compatibility mode to allow talking to non-ESOE identity providers - default to false
			boolean enableCompatibility = Boolean.parseBoolean(properties.getProperty("enableCompatibility", "false"));

			spep.setDisableAttributeQuery(disableAttributeQuery);
			spep.setDisablePolicyEnforcement(disablePolicyEnforcement);
			spep.setDisableSPEPStartup(disableSPEPStartup);
			spep.setEnableCompatibility(enableCompatibility);

			// IP Address list for this host
			String ipAddresses = resolveProperty(properties, "ipAddresses", new RegexStringValidator("((\\d+\\.){3}\\d+\\s+)*(\\d+\\.){3}\\d+")); //$NON-NLS-1$
			List<String> ipAddressList = new Vector<String>();
			StringTokenizer ipAddressTokenizer = new StringTokenizer(ipAddresses);
			while (ipAddressTokenizer.hasMoreTokens())
			{
				ipAddressList.add(ipAddressTokenizer.nextToken());
			}

			spep.setTrustedESOEIdentifier(esoeIdentifier);
			spep.setSPEPIdentifier(spepIdentifier);

			// Cookie and redirect information for authn
			spep.setTokenName(resolveProperty(properties, "spepTokenName", new RegexStringValidator("[^$\\s]\\S+"))); //$NON-NLS-1$
			spep.setEsoeGlobalTokenName(resolveProperty(properties, "commonDomainTokenName", new RegexStringValidator("[^$\\s]\\S+")));
			spep.setServiceHost(resolveProperty(properties, "serviceHost", new URLValidator())); //$NON-NLS-1$
			spep.setSsoRedirect(resolveProperty(properties, "ssoRedirect", new FreeFormStringValidator()));

			// Default url for users with no unauthenticated session
			spep.setDefaultURL(resolveProperty(properties, "defaultURL", new URLValidator())); //$NON-NLS-1$

			// List of cookies to clear when an invalid session is encountered.
			List<Cookie> logoutClearCookies = new Vector<Cookie>();
			String clearCookiePropertyValue = null;

			for( int i = 1; (clearCookiePropertyValue = properties.getProperty("logoutClearCookie." + i )) != null; ++i ) //$NON-NLS-1$
			{
				StringTokenizer cookieTokenizer = new StringTokenizer( clearCookiePropertyValue );

				// If there is a token, there will be a cookie name
				if ( cookieTokenizer.hasMoreTokens() )
				{
					// Construct the cookie from the name in the parameter.
					Cookie cookie = new Cookie( cookieTokenizer.nextToken().trim(), "" ); //$NON-NLS-1$

					// If there is another token it will be the cookie domain.
					if ( cookieTokenizer.hasMoreTokens() )
						cookie.setDomain( cookieTokenizer.nextToken().trim() );

					// If there is another token it will be the cookie path.
					if ( cookieTokenizer.hasMoreTokens() )
						cookie.setPath( cookieTokenizer.nextToken().trim() );

					logoutClearCookies.add( cookie );
				}
			}

			spep.setLogoutClearCookies(logoutClearCookies);

			// Determine if SPEP is operating is lazy mode
			spep.setLazyInit(new Boolean(properties.getProperty("lazyInit")).booleanValue());

			// If its in lazy mode then load up hardInit URL's there are the URLS that will force the SPEP to establish a session for the user
			if(spep.isLazyInit())
			{
				String lazyInitDefaultAction = properties.getProperty("lazyInitDefaultAction");
				if(lazyInitDefaultAction == null)
				{
					logger.error("Failed to retrieve lazyInitDefaultAction value");
					throw new SPEPInitializationException("Failed to retrieve lazyInitDefaultAction value");
				}

				if(lazyInitDefaultAction.equals(Initializer.DENY))
					spep.setLazyInitDefaultAction(SPEP.defaultAction.Deny);
				else
					if(lazyInitDefaultAction.equals(Initializer.PERMIT))
						spep.setLazyInitDefaultAction(SPEP.defaultAction.Permit);
					else
					{
						logger.error("Failed to retrieve lazyInitDefaultAction, invalid value must be deny or permit");
						throw new SPEPInitializationException("Failed to retrieve lazyInitDefaultAction, invalid value must be deny or permit");
					}

				List<String> lazyInitResources = new ArrayList<String>();
				String url;

				for(int i = 1; (url = properties.getProperty("lazyInit-resource-" + i)) != null; ++i)
				{
						lazyInitResources.add(url);
				}

				if(lazyInitResources.size() <= 0)
				{
					logger.error("Failed to retrieve any lazyinit-resource values, at least one URL MUST be specified");
					throw new SPEPInitializationException("Failed to retrieve any hardInit-URL-[] values, at least one URL MUST be specified");
				}

				spep.setLazyInitResources(lazyInitResources);
			}


			// Instantiate the file for other configuration.
			File keystoreFile = new File(keystorePath);

			// Initialize the keystore resolver from the file, and grab the metadata public key.
			KeystoreResolver keyStoreResolver;
			try
			{
				keyStoreResolver = new KeystoreResolverImpl(keystoreFile, keystorePassword, spepKeyAlias, spepKeyPassword);
			}
			catch (KeystoreResolverException e)
			{
				throw new SPEPInitializationException("Failed to load keystore. Exception was: " + e.getLocalizedMessage());
			}

			// Create metadata instance

			// Initialize dynamic updater with no dynamic sources.
			DynamicMetadataUpdater dynamicMetadataUpdater = new DynamicMetadataUpdaterImpl(new ArrayList<DynamicMetadataSource>());
			MetadataCache metadataCache = new MetadataCacheImpl(dynamicMetadataUpdater);

			// IdP processor
			SAMLIdentityProviderProcessor identityProviderProcessor;
			if (enableCompatibility)
			{
				// Don't require a trusted ESOE entity in compatibility mode.
				identityProviderProcessor = new SAMLIdentityProviderProcessor(null);
			}
			else
			{
				identityProviderProcessor = new SAMLIdentityProviderProcessor(esoeIdentifier);
			}
			// SP processor
			SAMLServiceProviderProcessor serviceProviderProcessor = new SAMLServiceProviderProcessor();

			List<SAMLEntityDescriptorProcessor> entityDescriptorProcessors = new ArrayList<SAMLEntityDescriptorProcessor>();
			entityDescriptorProcessors.add(identityProviderProcessor);
			entityDescriptorProcessors.add(serviceProviderProcessor);

			URL metadataUrlObject;
			try
			{
				metadataUrlObject = new URL(metadataUrl);
			}
			catch (MalformedURLException e)
			{
				throw new SPEPInitializationException("Failed to parse metadata URL. Exception was: " + e.getLocalizedMessage());
			}
			MetadataSource source = new SAMLURLMetadataSource(metadataUrlObject);

			List<MetadataSource> sources = new ArrayList<MetadataSource>();
			sources.add(source);

			List<FormatHandler> formatHandlers = new ArrayList<FormatHandler>();
			formatHandlers.add(new SAMLMetadataFormatHandler(keyStoreResolver, entityDescriptorProcessors));
			MetadataProcessor metadataProcessor = new MetadataProcessorImpl(metadataCache, formatHandlers, sources);
			spep.setMetadataProcessor(metadataProcessor);

			spep.setMetadataUpdateThread(new MetadataUpdateThread(metadataProcessor, metadataInterval));

			SOAPHandler soapv11Handler = new SOAPv11Handler();
			SOAPHandler soapv12Handler = new SOAPv12Handler();

			List<SOAPHandler> soapHandlers = new ArrayList<SOAPHandler>();
			soapHandlers.add(soapv11Handler);
			soapHandlers.add(soapv12Handler);

			// Web services client instance
			WSClient wsClient = new WSClientImpl(soapv12Handler);

			// Create the identifier cache and generator.
			IdentifierCache identifierCache = new IdentifierCacheImpl();
			IdentifierGenerator identifierGenerator = new IdentifierGeneratorImpl(identifierCache);

			// SAML validator instance
			SAMLValidator samlValidator = new SAMLValidatorImpl(identifierCache, allowedTimeSkew);

			// Session cache instance
			spep.setSessionCache( new SessionCacheImpl(sessionCacheTimeout, sessionCacheInterval) );

			// start the identifier cache monitor thread
			spep.setIdentifierCacheMonitor(new IdentifierCacheMonitor(identifierCache, sessionCacheInterval, identifierCacheTimeout));

			// Try to create the attribute processor instance
			try
			{
				spep.setAttributeProcessor(new AttributeProcessorImpl(spep.getMetadataProcessor(), wsClient, identifierGenerator, samlValidator, keyStoreResolver, esoeIdentifier, spepIdentifier, disableAttributeQuery, enableCompatibility));
			}
			catch (MarshallerException e)
			{
				logger.error(MessageFormat.format(Messages.getString("Initializer.7"), e.getMessage())); //$NON-NLS-1$
				throw new SPEPInitializationException(Messages.getString("Initializer.1"), e); //$NON-NLS-1$
			}
			catch (UnmarshallerException e)
			{
				logger.error(MessageFormat.format(Messages.getString("Initializer.7"), e.getMessage())); //$NON-NLS-1$
				throw new SPEPInitializationException(Messages.getString("Initializer.1"), e); //$NON-NLS-1$
			}
			catch (IOException e)
			{
				logger.error(MessageFormat.format(Messages.getString("Initializer.7"), e.getMessage())); //$NON-NLS-1$
				throw new SPEPInitializationException(Messages.getString("Initializer.1"), e); //$NON-NLS-1$
			}

			// Try to create the authn processor instance
			try
			{
				spep.setAuthnProcessor(new AuthnProcessorImpl(spep.getAttributeProcessor(), spep.getMetadataProcessor(), spep.getSessionCache(), samlValidator, identifierGenerator, keyStoreResolver, spep.getServiceHost(), spep.getSsoRedirect(), nodeIndex, nodeIndex, spepIdentifier));
			}
			catch (MarshallerException e)
			{
				logger.error(MessageFormat.format(Messages.getString("Initializer.8"), e.getMessage())); //$NON-NLS-1$
				throw new SPEPInitializationException(Messages.getString("Initializer.4"), e); //$NON-NLS-1$
			}
			catch (UnmarshallerException e)
			{
				logger.error(MessageFormat.format(Messages.getString("Initializer.8"), e.getMessage())); //$NON-NLS-1$
				throw new SPEPInitializationException(Messages.getString("Initializer.4"), e); //$NON-NLS-1$
			}
			catch (MalformedURLException e)
			{
				logger.error(MessageFormat.format(Messages.getString("Initializer.8"), e.getMessage()));
				throw new SPEPInitializationException(Messages.getString("Initializer.4"), e); //$NON-NLS-1$
			}

			// Create the session group cache, then attempt to create the policy enforcement processor
			spep.setSessionGroupCache(new SessionGroupCacheImpl(defaultPolicyDecision));
			try
			{
				spep.setPolicyEnforcementProcessor(new PolicyEnforcementProcessorImpl(spep.getSessionCache(), spep.getSessionGroupCache(), wsClient, identifierGenerator, spep.getMetadataProcessor(), keyStoreResolver, samlValidator, esoeIdentifier, spepIdentifier, disablePolicyEnforcement, enableCompatibility));
			}
			catch (MarshallerException e)
			{
				logger.error(MessageFormat.format(Messages.getString("Initializer.9"), e.getMessage())); //$NON-NLS-1$
				throw new SPEPInitializationException(Messages.getString("Initializer.6"), e); //$NON-NLS-1$
			}
			catch (UnmarshallerException e)
			{
				logger.error(MessageFormat.format(Messages.getString("Initializer.9"), e.getMessage())); //$NON-NLS-1$
				throw new SPEPInitializationException(Messages.getString("Initializer.6"), e); //$NON-NLS-1$
			}

			// Store the SPEP object in the servlet context.
			context.setAttribute(ConfigurationConstants.SERVLET_CONTEXT_NAME, spep);

			// Create a SPEPProxyImpl for use in external classloaders as a dynamic proxy and store in servlet context
			SPEPProxyImpl spepProxy = new SPEPProxyImpl(spep);
			context.setAttribute(ConfigurationConstants.SPEP_PROXY, spepProxy);

			// Create the SPEP startup processor
			try
			{
				spep.setStartupProcessor(new StartupProcessorImpl(spep.getMetadataProcessor(), spepIdentifier, esoeIdentifier, identifierGenerator, wsClient, samlValidator, keyStoreResolver, ipAddressList, serverInfo, nodeIndex, spepStartupInterval, disableSPEPStartup, enableCompatibility));
			}
			catch (MarshallerException e)
			{
				logger.error(MessageFormat.format(Messages.getString("Initializer.10"), e.getMessage())); //$NON-NLS-1$
				throw new SPEPInitializationException(Messages.getString("Initializer.11"), e);			 //$NON-NLS-1$
			}
			catch (UnmarshallerException e)
			{
				logger.error(MessageFormat.format(Messages.getString("Initializer.12"), e.getMessage())); //$NON-NLS-1$
				throw new SPEPInitializationException(Messages.getString("Initializer.13"), e);			 //$NON-NLS-1$
			}

			// Fire off a background thread to communicate to the ESOE about starting up.
			spep.getStartupProcessor().beginSPEPStartup();

			spep.setWSProcessor(new WSProcessorImpl(spep.getPolicyEnforcementProcessor(), spep.getAuthnProcessor(), soapHandlers));

			return spep;
		}

		return (SPEP)context.getAttribute(ConfigurationConstants.SERVLET_CONTEXT_NAME);
	}

	public static void cleanup( ServletContext context )
	{
		if(context == null)
		{
			throw new IllegalArgumentException(Messages.getString("Initializer.14"));  //$NON-NLS-1$
		}

		Object spepObject = context.getAttribute(ConfigurationConstants.SERVLET_CONTEXT_NAME);
		if( spepObject == null )
		{
			return;
		}

		if( !(spepObject instanceof SPEP) )
		{
			throw new IllegalArgumentException("SPEP instance was not an instance of the local SPEP class.");
		}

		SPEP spep = (SPEP)spepObject;
		spep.getMetadataUpdateThread().shutdown();
		spep.getSessionCache().cleanup();
		spep.getIdentifierCacheMonitor().stopRunning();
	}

}
