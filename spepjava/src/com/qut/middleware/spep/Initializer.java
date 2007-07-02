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

import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;

import org.apache.log4j.Logger;

import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.exception.UnmarshallerException;
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
import com.qut.middleware.spep.impl.StartupProcessorImpl;
import com.qut.middleware.spep.metadata.KeyStoreResolver;
import com.qut.middleware.spep.metadata.impl.KeyStoreResolverImpl;
import com.qut.middleware.spep.metadata.impl.MetadataImpl;
import com.qut.middleware.spep.pep.PolicyEnforcementProcessor.decision;
import com.qut.middleware.spep.pep.impl.PolicyEnforcementProcessorImpl;
import com.qut.middleware.spep.pep.impl.SessionGroupCacheImpl;
import com.qut.middleware.spep.sessions.SessionCache;
import com.qut.middleware.spep.sessions.impl.SessionCacheImpl;
import com.qut.middleware.spep.ws.WSClient;
import com.qut.middleware.spep.ws.impl.WSClientImpl;

/** Responsible for initializing the SPEP. */
public class Initializer
{
	/* Local logging instance */
	static private Logger logger = Logger.getLogger(Initializer.class.getName());
	
	/**
	 * @param context The servlet context in which to initialize a SPEP
	 * @return The SPEP for the given servlet context.
	 * @throws SPEPInitializationException 
	 */
	public static synchronized SPEP init(ServletContext context) throws SPEPInitializationException
	{
		if(context == null)
		{
			throw new IllegalArgumentException(Messages.getString("Initializer.14"));  //$NON-NLS-1$
		}
		
		if(context.getAttribute(ConfigurationConstants.SERVLET_CONTEXT_NAME) == null)
		{
			SPEPImpl spep = new SPEPImpl();
			
			// Get the properties file from the servlet context and read it in.
			InputStream propertyInputStream = context.getResourceAsStream(ConfigurationConstants.SPEP_CONFIG);
			
			if(propertyInputStream == null)
				throw new IllegalArgumentException(Messages.getString("Initializer.15") + ConfigurationConstants.SPEP_CONFIG); //$NON-NLS-1$
			
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
			
			String spepIdentifier = properties.getProperty("spepIdentifier"); //$NON-NLS-1$
			String esoeIdentifier = properties.getProperty("esoeIdentifier"); //$NON-NLS-1$
			String metadataUrl = properties.getProperty("metadataUrl"); //$NON-NLS-1$
			
			// Path to attribute configuration data
			String attributeConfigPath = properties.getProperty("attributeConfigPath"); //$NON-NLS-1$
			
			// Path, password and aliases for keystore
			String keystorePath = properties.getProperty("keystorePath"); //$NON-NLS-1$
			String keystorePassword = properties.getProperty("keystorePassword"); //$NON-NLS-1$
			String metadataKeyAlias = properties.getProperty("metadataKeyAlias"); //$NON-NLS-1$
			String spepKeyAlias = properties.getProperty("spepKeyAlias"); //$NON-NLS-1$
			String spepKeyPassword = properties.getProperty("spepKeyPassword");  //$NON-NLS-1$
			
			// Information line about the server
			String serverInfo = properties.getProperty("serverInfo");  //$NON-NLS-1$
			
			// Node identifier configured in the metadata
			int nodeID = Integer.parseInt(properties.getProperty("nodeIdentifier"));  //$NON-NLS-1$
			
			// Interval on which to refresh the metadata (seconds)
			int metadataInterval = Integer.parseInt(properties.getProperty("metadataInterval")); //$NON-NLS-1$
			
			// Allowable time skew for SAML document expiry (seconds)
			int allowedTimeSkew = Integer.parseInt(properties.getProperty("allowedTimeSkew")); //$NON-NLS-1$
			
			// Timeout and interval on which to check the session caches.
			long sessionCacheTimeout = Long.parseLong(properties.getProperty("sessionCacheTimeout")); //$NON-NLS-1$
			long sessionCacheInterval = Long.parseLong(properties.getProperty("sessionCacheInterval")); //$NON-NLS-1$
			
			// Timeout on data in the identifier cache. Should be longer than SAML document lifetime + time skew allowed
			long identifierCacheTimeout = Long.parseLong(properties.getProperty("identifierCacheTimeout")); //$NON-NLS-1$
			
			// Default policy decision for LAXCML
			decision defaultPolicyDecision = decision.valueOf(properties.getProperty("defaultPolicyDecision")); //$NON-NLS-1$
			
			// IP Address list for this host
			String ipAddresses = properties.getProperty("ipAddresses"); //$NON-NLS-1$
			List<String> ipAddressList = new Vector<String>();
			StringTokenizer ipAddressTokenizer = new StringTokenizer(ipAddresses);
			while (ipAddressTokenizer.hasMoreTokens())
			{
				ipAddressList.add(ipAddressTokenizer.nextToken());
			}
			
			// Cookie and redirect information for authn
			spep.setTokenName(properties.getProperty("tokenName")); //$NON-NLS-1$
			spep.setTokenDomain(properties.getProperty("tokenDomain")); //$NON-NLS-1$
			spep.setLoginRedirect(properties.getProperty("loginRedirect")); //$NON-NLS-1$
			
			// Default url for users with no unauthenticated session
			spep.setDefaultUrl(properties.getProperty("defaultUrl")); //$NON-NLS-1$
			
			// List of cookies to clear when an invalid session is encountered.
			List<Cookie> logoutClearCookies = new Vector<Cookie>();
			String clearCookiePropertyValue = null;
			for( int i = 1; (clearCookiePropertyValue = properties.getProperty("logoutClearCookie." + i)) != null; ++i ) //$NON-NLS-1$
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
			
			
			// Instantiate the input streams for other configuration.
			InputStream keyStoreInputStream = context.getResourceAsStream(keystorePath);
			InputStream attributeConfigInputStream = context.getResourceAsStream(attributeConfigPath);
			
			// Initialize the keystore resolver from the stream, and grab the metadata public key.
			KeyStoreResolver keyStoreResolver = new KeyStoreResolverImpl(keyStoreInputStream, keystorePassword, spepKeyAlias, spepKeyPassword);
			PublicKey metadataPublicKey = keyStoreResolver.resolveKey(metadataKeyAlias);

			// Create metadata instance
			spep.setMetadata(new MetadataImpl(spepIdentifier, esoeIdentifier, metadataUrl, metadataPublicKey, nodeID, metadataInterval));

			// Web services client instance
			WSClient wsClient = new WSClientImpl();
			
			// Create the identifier cache and generator.
			IdentifierCache identifierCache = new IdentifierCacheImpl();
			IdentifierGenerator identifierGenerator = new IdentifierGeneratorImpl(identifierCache);
			
			// SAML validator instance
			SAMLValidator samlValidator = new SAMLValidatorImpl(identifierCache, allowedTimeSkew);
			
			// Session cache instance
			SessionCache sessionCache = new SessionCacheImpl(sessionCacheTimeout, sessionCacheInterval);

			// start the identifier cache monitor thread
			new IdentifierCacheMonitor(identifierCache, sessionCacheInterval, identifierCacheTimeout);
			
			// Try to create the attribute processor instance
			try
			{
				spep.setAttributeProcessor(new AttributeProcessorImpl(spep.getMetadata(), wsClient, identifierGenerator, samlValidator, attributeConfigInputStream, keyStoreResolver));
			}
			catch (MarshallerException e)
			{
				logger.fatal(MessageFormat.format(Messages.getString("Initializer.7"), e.getMessage())); //$NON-NLS-1$
				throw new SPEPInitializationException(Messages.getString("Initializer.1"), e); //$NON-NLS-1$
			}
			catch (UnmarshallerException e)
			{
				logger.fatal(MessageFormat.format(Messages.getString("Initializer.7"), e.getMessage())); //$NON-NLS-1$
				throw new SPEPInitializationException(Messages.getString("Initializer.1"), e); //$NON-NLS-1$
			}
			catch (IOException e)
			{
				logger.fatal(MessageFormat.format(Messages.getString("Initializer.7"), e.getMessage())); //$NON-NLS-1$
				throw new SPEPInitializationException(Messages.getString("Initializer.1"), e); //$NON-NLS-1$
			}
			
			// Try to create the authn processor instance
			try
			{
				spep.setAuthnProcessor(new AuthnProcessorImpl(spep.getAttributeProcessor(), spep.getMetadata(), sessionCache, samlValidator, identifierGenerator, keyStoreResolver, nodeID, nodeID));
			}
			catch (MarshallerException e)
			{
				logger.fatal(MessageFormat.format(Messages.getString("Initializer.8"), e.getMessage())); //$NON-NLS-1$
				throw new SPEPInitializationException(Messages.getString("Initializer.4"), e); //$NON-NLS-1$
			}
			catch (UnmarshallerException e)
			{
				logger.fatal(MessageFormat.format(Messages.getString("Initializer.8"), e.getMessage())); //$NON-NLS-1$
				throw new SPEPInitializationException(Messages.getString("Initializer.4"), e); //$NON-NLS-1$
			}
			
			// Create the session group cache, then attempt to create the policy enforcement processor
			spep.setSessionGroupCache(new SessionGroupCacheImpl(defaultPolicyDecision));
			try
			{
				spep.setPolicyEnforcementProcessor(new PolicyEnforcementProcessorImpl(sessionCache, spep.getSessionGroupCache(), wsClient, identifierGenerator, spep.getMetadata(), keyStoreResolver, samlValidator));
			}
			catch (MarshallerException e)
			{
				logger.fatal(MessageFormat.format(Messages.getString("Initializer.9"), e.getMessage())); //$NON-NLS-1$
				throw new SPEPInitializationException(Messages.getString("Initializer.6"), e); //$NON-NLS-1$
			}
			catch (UnmarshallerException e)
			{
				logger.fatal(MessageFormat.format(Messages.getString("Initializer.9"), e.getMessage())); //$NON-NLS-1$
				throw new SPEPInitializationException(Messages.getString("Initializer.6"), e); //$NON-NLS-1$
			}

			// Store the SPEP object in the servlet context.
			context.setAttribute(ConfigurationConstants.SERVLET_CONTEXT_NAME, spep);
			
			// Create the SPEP startup processor
			try
			{
				spep.setStartupProcessor(new StartupProcessorImpl(spep.getMetadata(), spepIdentifier, identifierGenerator, wsClient, samlValidator, keyStoreResolver, ipAddressList, serverInfo, nodeID));
			}
			catch (MarshallerException e)
			{
				logger.fatal(MessageFormat.format(Messages.getString("Initializer.10"), e.getMessage())); //$NON-NLS-1$
				throw new SPEPInitializationException(Messages.getString("Initializer.11"), e);			 //$NON-NLS-1$
			}
			catch (UnmarshallerException e)
			{
				logger.fatal(MessageFormat.format(Messages.getString("Initializer.12"), e.getMessage())); //$NON-NLS-1$
				throw new SPEPInitializationException(Messages.getString("Initializer.13"), e);			 //$NON-NLS-1$
			}
			
			// Fire off a background thread to communicate to the ESOE about starting up.
			spep.getStartupProcessor().beginSPEPStartup();
			
			return spep;
		}
		
		return (SPEP)context.getAttribute(ConfigurationConstants.SERVLET_CONTEXT_NAME);
	}
	
}
