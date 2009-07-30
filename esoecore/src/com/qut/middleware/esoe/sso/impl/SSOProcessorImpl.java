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
 * Author: Bradley Beddoes
 * Creation Date: 24/10/2006
 * 
 * Purpose: Implements logic to interact with SPEP using Web Browser SSO Post Profile of SAML 2.0 specification
 */
package com.qut.middleware.esoe.sso.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.w3._2000._09.xmldsig_.Signature;
import org.w3c.dom.Element;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.qut.middleware.crypto.KeystoreResolver;
import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.esoe.sessions.bean.IdentityAttribute;
import com.qut.middleware.esoe.sessions.exception.SessionCacheUpdateException;
import com.qut.middleware.esoe.spep.exception.InvalidRequestException;
import com.qut.middleware.esoe.sso.SSOProcessor;
import com.qut.middleware.esoe.sso.bean.SSOProcessorData;
import com.qut.middleware.esoe.sso.bean.SSOProcessorData.SSOAction;
import com.qut.middleware.esoe.sso.exception.SSOException;
import com.qut.middleware.esoe.sso.pipeline.Handler;
import com.qut.middleware.esoe.util.CalendarUtils;
import com.qut.middleware.metadata.bean.EntityData;
import com.qut.middleware.metadata.bean.saml.ServiceProviderRole;
import com.qut.middleware.metadata.exception.MetadataStateException;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.saml2.AuthenticationContextConstants;
import com.qut.middleware.saml2.BindingConstants;
import com.qut.middleware.saml2.ConfirmationMethodConstants;
import com.qut.middleware.saml2.ConsentIdentifierConstants;
import com.qut.middleware.saml2.ExternalKeyResolver;
import com.qut.middleware.saml2.NameIDFormatConstants;
import com.qut.middleware.saml2.SchemaConstants;
import com.qut.middleware.saml2.StatusCodeConstants;
import com.qut.middleware.saml2.VersionConstants;
import com.qut.middleware.saml2.exception.InvalidSAMLRequestException;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.exception.ReferenceValueException;
import com.qut.middleware.saml2.exception.SignatureValueException;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.Unmarshaller;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.handler.impl.UnmarshallerImpl;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.schemas.assertion.Assertion;
import com.qut.middleware.saml2.schemas.assertion.AudienceRestriction;
import com.qut.middleware.saml2.schemas.assertion.AuthnContext;
import com.qut.middleware.saml2.schemas.assertion.AuthnStatement;
import com.qut.middleware.saml2.schemas.assertion.ConditionAbstractType;
import com.qut.middleware.saml2.schemas.assertion.Conditions;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.assertion.Subject;
import com.qut.middleware.saml2.schemas.assertion.SubjectConfirmation;
import com.qut.middleware.saml2.schemas.assertion.SubjectConfirmationDataType;
import com.qut.middleware.saml2.schemas.assertion.SubjectLocality;
import com.qut.middleware.saml2.schemas.protocol.AuthnRequest;
import com.qut.middleware.saml2.schemas.protocol.Response;
import com.qut.middleware.saml2.schemas.protocol.Status;
import com.qut.middleware.saml2.schemas.protocol.StatusCode;
import com.qut.middleware.saml2.validator.SAMLValidator;

/** Implements logic to interact with SPEP using Web Browser SSO Post Profile of SAML 2.0 specification. */
public class SSOProcessorImpl implements SSOProcessor
{
	private List<Handler> handlers;

	private String esoeIdentifier;
	private IdentifierGenerator identifierGenerator;
	private MetadataProcessor metadata;

	private SessionsProcessor sessionsProcessor;
	private SAMLValidator samlValidator;

	private String uriEncodedCDC;

	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(SSOProcessorImpl.class.getName());
	private Logger authnLogger = LoggerFactory.getLogger(ConfigurationConstants.authnLogger);

	// --- "The line" ---
	private Unmarshaller<AuthnRequest> unmarshaller;
	private Marshaller<Response> marshaller;


	private int allowedTimeSkew;
	private int minimalTimeRemaining;

	private Map<String, String> identifierAttributeMapping;

	private final String UNMAR_PKGNAMES = AuthnRequest.class.getPackage().getName();
	private final String MAR_PKGNAMES = Response.class.getPackage().getName();

	private final String UTF8 = "UTF-8";
	private final String UTF16 = "UTF-16";
	private final String UTF16LE = "UTF-16LE";
	private final String UTF16BE = "UTF-16BE";
	
	private final String DEFAULT_CHARSET = UTF16;

	private String sessionTokenName;

	private String commonDomainTokenName;

	private String authnRedirectURL;

	private String authnDynamicURLParam;

	private String ssoURL;

	private String sessionDomain;

	private String commonDomain;

	private static String IMPLEMENTED_BINDING = BindingConstants.httpPost;


	/**
	 * Implementation of the SSOProcessor interface to service the SAML AuthnRequests.
	 * 
	 * @param samlValidator
	 *            SAML document validator instance
	 * @param sessionsProcessor
	 *            The system wide sessions processor instance
	 * @param metadata
	 *            The metadata used to obtain SPEP details
	 * @param identifierGenerator
	 *            A SAML2lib-j identifier generator instance
	 * @param extKeyResolver
	 *            The external key resolver instance
	 * @param keyStoreResolver
	 *            The keystore resolver used to obtain the ESOE private key and SPEP public keys
	 * @param allowedTimeSkew
	 *            Time skew in seconds we will accept our SPEP being out, this applied to both recieved messages and
	 *            responses
	 * @param minimalTimeRemaining
	 *            Time in seconds that a principal has to have remaining in their session in order to be granted a new
	 *            session on some remote SPEP
	 * @param identifierAttributeMapping
	 *            A mapping of keys from NameIDConstants to attributes exposed by the Attribute Authority for resolution
	 *            of subject identifier specifiers in responses.
	 * 
	 * @throws UnmarshallerException
	 *             if the unmarshaller cannot be created.
	 * @throws MarshallerException
	 *             if the marshaller cannot be created.
	 */
	public SSOProcessorImpl(SAMLValidator samlValidator, SessionsProcessor sessionsProcessor, MetadataProcessor metadata, IdentifierGenerator identifierGenerator, ExternalKeyResolver extKeyResolver, KeystoreResolver keyStoreResolver, Map<String, String> identifierAttributeMapping, List<Handler> handlers, Properties configuration) throws UnmarshallerException, MarshallerException
	{
		if (samlValidator == null)
		{
			throw new IllegalArgumentException("SAML validator must not be null");
		}

		if (sessionsProcessor == null)
		{
			throw new IllegalArgumentException("Sessions processor must not be null");
		}

		if (metadata == null)
		{
			throw new IllegalArgumentException("SPEP processor must not be null");
		}

		if (identifierGenerator == null)
		{
			throw new IllegalArgumentException("Identifier generator must not be null");
		}

		if (extKeyResolver == null)
		{
			throw new IllegalArgumentException("External key resolver must not be null");
		}

		if (keyStoreResolver == null)
		{
			throw new IllegalArgumentException("Keystore resolver must not be null");
		}

		if (identifierAttributeMapping == null)
		{
			throw new IllegalArgumentException("Identifier attribute mapping must not be null");
		}

		if (handlers == null || handlers.size() == 0)
		{
			this.logger.error("No SSO pipeline handlers were given.");
			throw new IllegalArgumentException("No SSO pipeline handlers were given.");
		}

		this.samlValidator = samlValidator;
		this.sessionsProcessor = sessionsProcessor;
		this.metadata = metadata;
		this.identifierGenerator = identifierGenerator;

		String[] schemas = new String[] { SchemaConstants.samlProtocol };
		this.unmarshaller = new UnmarshallerImpl<AuthnRequest>(this.UNMAR_PKGNAMES, schemas, extKeyResolver);
		this.marshaller = new MarshallerImpl<Response>(this.MAR_PKGNAMES, schemas, keyStoreResolver);

		this.allowedTimeSkew = allowedTimeSkew * 1000;
		this.minimalTimeRemaining = minimalTimeRemaining * 1000;
		this.identifierAttributeMapping = identifierAttributeMapping;
		this.handlers = handlers;

		this.sessionTokenName = configuration.getProperty(ConfigurationConstants.ESOE_SESSION_TOKEN_NAME);
		this.commonDomainTokenName = configuration.getProperty(ConfigurationConstants.COMMON_DOMAIN_TOKEN_NAME);
		this.authnRedirectURL = configuration.getProperty(ConfigurationConstants.AUTHN_REDIRECT_URL);
		this.authnDynamicURLParam = configuration.getProperty(ConfigurationConstants.AUTHN_DYNAMIC_URL_PARAM);
		this.ssoURL = configuration.getProperty(ConfigurationConstants.SSO_URL);
		this.sessionDomain = configuration.getProperty(ConfigurationConstants.ESOE_SESSION_DOMAIN);
		this.commonDomain = configuration.getProperty(ConfigurationConstants.COMMON_DOMAIN);
		this.esoeIdentifier = configuration.getProperty(ConfigurationConstants.ESOE_IDENTIFIER);

		if (this.allowedTimeSkew > Integer.MAX_VALUE / 1000)
		{
			throw new IllegalArgumentException("Allowed time skew is too large");
		}

		if (this.minimalTimeRemaining > Integer.MAX_VALUE / 1000)
		{
			throw new IllegalArgumentException("Session minimal time remaining is too large");
		}
		
		if (this.esoeIdentifier == null)
		{
			throw new IllegalArgumentException("ESOE identifier was null");
		}
		

		if (this.sessionTokenName == null)
		{
			throw new IllegalArgumentException("Couldn't find configuration option " + ConfigurationConstants.ESOE_SESSION_TOKEN_NAME + ". No session token name configured.");
		}

		if (this.authnRedirectURL == null)
		{
			throw new IllegalArgumentException("Couldn't find configuration option " + ConfigurationConstants.AUTHN_REDIRECT_URL + ". No authentication redirect URL configured.");
		}

		if (this.authnDynamicURLParam == null)
		{
			throw new IllegalArgumentException("Couldn't find configuration option " + ConfigurationConstants.AUTHN_DYNAMIC_URL_PARAM + ". No dynamic authentication redirect URL configured.");
		}

		if (this.ssoURL == null)
		{
			throw new IllegalArgumentException("Couldn't find configuration option " + ConfigurationConstants.SSO_URL + ". No SSO URL configured.");
		}

		if (this.sessionDomain == null)
		{
			throw new IllegalArgumentException("Couldn't find configuration option " + ConfigurationConstants.ESOE_SESSION_DOMAIN + ". No ESOE session domain configured.");
		}

		if (this.commonDomainTokenName == null)
		{
			throw new IllegalArgumentException("Couldn't find configuration option " + ConfigurationConstants.COMMON_DOMAIN_TOKEN_NAME + ". No common domain token name configured.");
		}

		if (this.commonDomain == null)
		{
			throw new IllegalArgumentException("Couldn't find configuration option " + ConfigurationConstants.COMMON_DOMAIN + ". No common domain configured.");
		}

		try
		{
			byte[] b64EncodedCDC = Base64.encodeBase64(this.esoeIdentifier.getBytes());
			uriEncodedCDC = URLEncoder.encode(new String(b64EncodedCDC), this.UTF8);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new IllegalArgumentException("Unable to create common domain cookie. UTF8 was reported as an unsupported encoding by the JVM.");
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sso.SSOProcessor#execute(com.qut.middleware.esoe.sso.bean.SSOProcessorData)
	 */
	public result execute(SSOProcessorData data) throws SSOException
	{
		if (data == null)
		{
			this.logger.debug("Supplied SSOProcessorData implementation can not be null");
			throw new IllegalArgumentException("Supplied SSOProcessorData implementation can not be null");
		}

		/* If this is a new SSO request then unmarshall / validate and set values accordingly */
		SSOAction currentAction = null;
		String currentHandlerName = null;
		String remoteAddress = data.getRemoteAddress();
		data.setSSOProcessor(this);

		if (data.getCurrentAction() != null)
		{
			currentAction = data.getCurrentAction();
			currentHandlerName = data.getCurrentHandler();

			this.logger.info("[SSO for {}] Resuming SSO processing with action {}. Current handler is {}.", new Object[] { remoteAddress, currentAction.toString(), currentHandlerName });
		}

		if (currentAction == null)
		{
			this.logger.info("[SSO for {}] Processing new SSO event.", new Object[] { remoteAddress });

			// Start at the beginning.
			currentAction = SSOAction.REQUEST_PROCESSING;
		}

		if (currentAction == SSOAction.REQUEST_PROCESSING)
		{
			boolean processedRequest = false;
			for (Handler handler : this.handlers)
			{
				if (currentHandlerName != null && !currentHandlerName.equals(handler.getHandlerName()))
				{
					this.logger.debug("[SSO for {}] Skipping already processed request handler. Current action {}, current handler {}, skipped handler {}", new Object[] { remoteAddress, currentAction.toString(), currentHandlerName, handler.getHandlerName() });
					// Skip the handlers that have already been processed.
					continue;
				}

				currentHandlerName = handler.getHandlerName();
				data.setCurrentAction(currentAction);
				data.setCurrentHandler(currentHandlerName);

				this.logger.debug("[SSO for {}] Starting request processing step. Current action {}, current handler {}.", new Object[] { remoteAddress, currentAction.toString(), currentHandlerName });

				Handler.result handlerResult = handler.executeRequest(data);
				switch (handlerResult)
				{
					case InvalidRequest:
					this.logger.error("[SSO for {}] Handler {} reported that the request was invalid. Unable to continue processing.", new Object[]{remoteAddress, currentHandlerName});
					data.setCurrentAction(null);
					data.setCurrentHandler(null);
					return SSOProcessor.result.SSOGenerationFailed;

					case NoAction:
					this.logger.debug("[SSO for {}] Handler {} reported while processing the request that no action was taken. Continuing.", new Object[]{remoteAddress, currentHandlerName});
					currentHandlerName = null;
					data.setCurrentHandler(null);
					continue;

					case Successful:
					this.logger.info("[SSO for {}] Handler {} reported that the request was processed successfully. Continuing.", new Object[]{remoteAddress, currentHandlerName});
					currentHandlerName = null;
					currentAction = SSOAction.SSO_PROCESSING;
					data.setCurrentHandler(currentHandlerName);
					data.setCurrentAction(currentAction);
					processedRequest = true;
					break;

					case Reset:
					this.logger.info("[SSO for {}] Handler {} reported a state change that requires restarting of the request processing. Restarting.", new Object[]{remoteAddress, currentHandlerName});
					data.setCurrentHandler(null);

					default:
					this.logger.error("[SSO for {}] Handler {} returned an invalid value. Unable to continue processing.", new Object[]{remoteAddress, currentHandlerName});
					data.setCurrentAction(null);
					data.setCurrentHandler(null);
					return SSOProcessor.result.SSOGenerationFailed;
				}
				
				// Don't loop again if we just processed the request.
				if (processedRequest) break;
			}

			if (!processedRequest)
			{
				this.logger.error("[SSO for {}] No handlers accepted the given request. Unable to continue processing.", new Object[] { remoteAddress });
				return SSOProcessor.result.SSOGenerationFailed;
			}
		}

		if (currentAction == SSOAction.SSO_PROCESSING)
		{
			this.logger.debug("[SSO for {}] Validating session information before responding.", new Object[] { remoteAddress });

			// Validate session from cookie contents / session cache.
			result validationResult = this.validateSession(data);
			// If it's null, we want to fall through to the following code
			if (validationResult != null) switch (validationResult)
			{
				case ForceAuthn:
				case ForcePassiveAuthn:
				this.generateForceAuthnResponse(data);
				// Either of these two would be very strange, but we'll just run with it..
				case SSOGenerationFailed:
				case SSOGenerationSuccessful:
				return validationResult;
				
				default:
				this.logger.warn("[SSO for {}] Unexpected result from validateSession: {} .. Continuing to terminate SSO process with this result.", new Object[]{ remoteAddress, validationResult.toString() });
				return validationResult;
			}

			data.setCommonCookieValue(this.uriEncodedCDC);

			currentHandlerName = null;
			currentAction = SSOAction.RESPONSE_PROCESSING;
			data.setCurrentHandler(currentHandlerName);
			data.setCurrentAction(currentAction);
		}

		if (currentAction == SSOAction.RESPONSE_PROCESSING)
		{
			for (Handler handler : this.handlers)
			{
				if (currentHandlerName != null && !currentHandlerName.equals(handler.getHandlerName()))
				{
					this.logger.debug("[SSO for {}] Skipping already processed response handler. Current action {}, current handler {}, skipped handler {}", new Object[] { remoteAddress, currentAction.toString(), currentHandlerName, handler.getHandlerName() });
					// Skip the handlers that have already been processed.
					continue;
				}

				currentHandlerName = handler.getHandlerName();
				data.setCurrentAction(currentAction);
				data.setCurrentHandler(currentHandlerName);

				this.logger.debug("[SSO for {}] Starting response processing step. Current action {}, current handler {}.", new Object[] { remoteAddress, currentAction.toString(), currentHandlerName });

				Handler.result handlerResult = handler.executeResponse(data);
				switch (handlerResult)
				{
					case InvalidRequest:
					this.logger.error("[SSO for {}] Handler {} reported while processing the response that the request was invalid. Unable to continue processing.", new Object[] { remoteAddress, currentHandlerName });
					data.setCurrentAction(null);
					data.setCurrentHandler(null);
					return SSOProcessor.result.SSOGenerationFailed;
					
					case UnwillingToRespond:
					this.logger.error("[SSO for {}] Handler {} reported while processing the response that it was unwilling to respond. Unable to continue processing.", new Object[] { remoteAddress, currentHandlerName });
					data.setCurrentAction(null);
					data.setCurrentHandler(null);
					return SSOProcessor.result.SSOGenerationFailed;

					case NoAction:
					this.logger.debug("[SSO for {}] Handler {} reported while processing the response that no action was taken. Continuing.", new Object[] { remoteAddress, currentHandlerName });
					currentHandlerName = null;
					data.setCurrentHandler(null);
					continue;

					case Successful:
					this.logger.info("[SSO for {}] Handler {} reported that the response was processed successfully. Continuing.", new Object[] { remoteAddress, currentHandlerName });
					data.setCurrentAction(null);
					data.setCurrentHandler(null);
					return SSOProcessor.result.SSOGenerationSuccessful;

					default:
					this.logger.error("[SSO for {}] Handler {} returned an invalid value. Unable to continue processing.", new Object[] { remoteAddress, currentHandlerName });
					data.setCurrentAction(null);
					data.setCurrentHandler(null);
					return SSOProcessor.result.SSOGenerationFailed;
				}
			}

			this.logger.error("[SSO for {}] No handlers responded. Unable to continue processing.", new Object[] { remoteAddress });
			return SSOProcessor.result.SSOGenerationFailed;
		}
		
		this.logger.warn("[SSO for {}] Fell off the end of the SSO processing logic. This is due to an invalid state in the HttpSession. The session will now be invalidated so that attempting to sign in again will correct the issue.", new Object[] { remoteAddress });
		data.getHttpRequest().getSession().invalidate();
		data.setCurrentAction(null);
		data.setCurrentHandler(null);
		return SSOProcessor.result.SSOGenerationFailed;
	}

	public void processAuthnRequest(SSOProcessorData data) throws SSOException
	{
		String remoteAddr = data.getRemoteAddress();
		try
		{
			this.logger.debug("[SSO for {}] Processing AuthnRequest for new SSO event.", new Object[]{remoteAddr}); //$NON-NLS-1$

			if (data.getAuthnRequest().getIssuer() == null) {
				this.logger.error("[SSO for {}] SSO processor could not resolve issuer ID from request document.", new Object[]{remoteAddr});
				throw new SSOException("SSO processor could not resolve issuer ID from request document.");
			}
			data.setIssuerID(data.getAuthnRequest().getIssuer().getValue());
			
			EntityData issuerEntity = this.metadata.getEntityData(data.getIssuerID());
			if (issuerEntity == null)
			{
				this.logger.error("[SSO for {}] SSO processor could not resolve issuer entity from metadata. Entity ID: {}", new Object[]{remoteAddr, data.getIssuerID()});
				throw new SSOException("SSO processor could not resolve issuer entity from metadata. Entity ID: " + data.getIssuerID());
			}
			
			ServiceProviderRole spRole = issuerEntity.getRoleData(ServiceProviderRole.class);
			if (spRole == null)
			{
				this.logger.error("[SSO for {}] SSO processor resolved issuer entity but it was not marked as an SPEP by the metadata processor. Entity ID: {}", new Object[]{remoteAddr, data.getIssuerID()});
				throw new SSOException("SSO processor resolved issuer entity but it was not marked as an SPEP by the metadata processor. Entity ID: " + data.getIssuerID());
			}

			// TODO Refactor this to make more sense
			// Check the spec to see if we can respond with a NameIDFormat that was specified on the SPSSODescriptor but not in the AuthnRequest
			// Also, what if both ACS index and ACS URL are specified?
			if (data.getAuthnRequest().getAssertionConsumerServiceIndex() != null)
			{
				int acsIndex = data.getAuthnRequest().getAssertionConsumerServiceIndex().intValue();

				data.setResponseEndpoint(spRole.getAssertionConsumerServiceEndpoint(IMPLEMENTED_BINDING, acsIndex));
				this.logger.debug("[SSO for {}] Retrieved endpoint from metadata", remoteAddr);

				data.setValidIdentifiers(spRole.getNameIDFormatList());
			}
			else
			{
				if (data.getAuthnRequest().getAssertionConsumerServiceURL() != null)
				{
					data.setResponseEndpoint(data.getAuthnRequest().getAssertionConsumerServiceURL());
					this.logger.debug("[SSO for {}] Retrieved endpoint directly from request", remoteAddr);

					List<String> validIdentifiers = new ArrayList<String>();
					validIdentifiers.add(data.getAuthnRequest().getNameIDPolicy().getFormat());
					data.setValidIdentifiers(validIdentifiers);
				}
				else
				{
					this.logger.error("[SSO for {}] Failed to retrieve response endpoint either from metadata or directly from request.", new Object[]{remoteAddr});
					throw new SSOException("Failed to retrieve response endpoint either from metadata or directly from request.");
				}
			}

			/* Important information has been retrieved to respond, now validate the entire document */
			this.logger.debug("[SSO for {}] Validating request via SAML validator", new Object[]{remoteAddr});
			this.samlValidator.getRequestValidator().validate(data.getAuthnRequest());

			this.logger.debug("[SSO for {}] SAML request was valid. Validating authentication request via SAML validator", new Object[]{remoteAddr});
			this.samlValidator.getAuthnRequestValidator().validate(data.getAuthnRequest());
			
			this.logger.debug("[SSO for {}] SAML authentication request was valid.", new Object[]{remoteAddr});
		}
		catch (MetadataStateException e)
		{
			this.logger.error("[SSO for {}] Unable to perform SSO as the metadata processor reported an invalid state. Error was: {}", new Object[]{remoteAddr, e.getMessage()});
			throw new SSOException("Unable to perform SSO as the metadata processor reported an invalid state.", e);
		}
		catch (InvalidSAMLRequestException e)
		{
			this.logger.error("[SSO for {}] Unable to perform SSO as the SAML validator found the request to be invalid. Error was: {}", new Object[]{remoteAddr, e.getMessage()});
			throw new SSOException("Unable to perform SSO as the SAML validator found the request to be invalid.", e);
		}
	}

	private String detectCharSet(byte[] document)
	{
		CharsetDetector charsetDetector;
		CharsetMatch charsetMatch;

		/* Make best effort determination of incoming document type */
		charsetDetector = new CharsetDetector();
		charsetDetector.setText(document);
		charsetMatch = charsetDetector.detect();

		/* FOR UTF-16 don't set endianness */
		if (charsetMatch.getName().equalsIgnoreCase(this.UTF16BE) || charsetMatch.getName().equalsIgnoreCase(this.UTF16LE))
		{
			this.logger.info("Detected incoming AuthnRequest encoding of " + this.UTF16 + " will respond in same encoding");
			return this.UTF16;
		}

		/*
		 * We deliberately setup UTF-8 here to ensure that misdiagnosed encodings of types such as ISO-8859 don't cause
		 * us grief
		 */
		this.logger.info("Detected incoming AuthnRequest encoding of " + charsetMatch.getName() + " will respond in UTF-8");
		return this.UTF8;
	}

	/**
	 * Creates a SAML response document for failed requests and stores it in the samlResponseDocument attribute of the
	 * principal
	 * 
	 * @param data
	 *            Populated SSOProcessor data bean
	 * @param authnRequest
	 *            The current AuthnRequest
	 * @param charset
	 *            The IANA registered name of the charset to encode the response in
	 * @throws InvalidRequestException
	 */
	public byte[] createStatusAuthnResponse(SSOProcessorData data, String statusValue, String detailedStatusValue, String statusMessage, boolean signed) throws SSOException
	{

		this.logger.info("[SSO for {}] Creating authentication response with status {}, detailed status {}, status message {}", new Object[]{data.getRemoteAddress(), statusValue, detailedStatusValue, statusMessage});

		NameIDType issuer;
		Signature signature;
		Status status;
		StatusCode statusCode, embededStatusCode;
		Response response;

		/* Generate failed status, two layers of code supplied */
		statusCode = new StatusCode();
		statusCode.setValue(statusValue);
		embededStatusCode = new StatusCode();
		embededStatusCode.setValue(detailedStatusValue);
		statusCode.setStatusCode(embededStatusCode);

		status = new Status();
		status.setStatusCode(statusCode);
		status.setStatusMessage(statusMessage);

		/* Generate Issuer to attach to response */
		issuer = new NameIDType();
		issuer.setValue(this.esoeIdentifier);

		/* Generate placeholder <Signature/> block for SAML2lib-j in response */
		signature = new Signature();

		/* Generate our response */
		response = new Response();
		response.setID(this.identifierGenerator.generateSAMLID());
		response.setInResponseTo(data.getAuthnRequest().getID());
		response.setVersion(VersionConstants.saml20);
		response.setIssueInstant(CalendarUtils.generateXMLCalendar(0));
		response.setDestination(data.getResponseEndpoint());
		response.setConsent(ConsentIdentifierConstants.unspecified);

		response.setIssuer(issuer);
		response.setSignature(signature);
		response.setStatus(status);

		try
		{
			return marshallResponse(response, signed, data.getRequestCharsetName());
		}
		catch (MarshallerException e)
		{
			this.logger.error("[SSO for {}] Failed to marshal status authentication response. Error was: {}", new Object[]{data.getRemoteAddress(), e.getMessage()});
			return null;
		}
	}

	public byte[] marshallResponse(Response responseObject, boolean signed, String charset) throws MarshallerException
	{
		byte[] responseDocument;
		
		if (charset == null)
		{
			charset = DEFAULT_CHARSET;
		}
		
		if (signed)
		{
			responseDocument = this.marshaller.marshallSigned(responseObject, charset);
		}
		else
		{
			responseDocument = this.marshaller.marshallUnSigned(responseObject, charset);
		}
		
		if (this.logger.isTraceEnabled())
		{
			CharsetDetector detector = new CharsetDetector();
			String responseString = detector.getString(responseDocument, null);
			this.logger.trace("Marshalled {}signed response document:\n{}", new Object[]{(signed?"":"un"), responseString});
		}

		return responseDocument;
	}

	public Element marshallResponseElement(Response responseObject, boolean signed, String charset) throws MarshallerException
	{
		Element responseDocument;
		
		if (charset == null)
		{
			charset = DEFAULT_CHARSET;
		}
		
		if (signed)
		{
			responseDocument = this.marshaller.marshallSignedElement(responseObject, charset);
		}
		else
		{
			responseDocument = this.marshaller.marshallUnSignedElement(responseObject, charset);
		}
		
		return responseDocument;
	}

	public AuthnRequest unmarshallRequest(byte[] requestDocument, boolean signed) throws SignatureValueException, ReferenceValueException, UnmarshallerException
	{
		if (this.logger.isTraceEnabled())
		{
			CharsetDetector detector = new CharsetDetector();
			String requestString = detector.getString(requestDocument, null);
			this.logger.trace("Unmarshalling {}signed request document:\n{}", new Object[]{(signed?"":"un"), requestString});
		}
		
		if (signed)
		{
			return this.unmarshaller.unMarshallSigned(requestDocument);
		}
		else
		{
			return this.unmarshaller.unMarshallUnSigned(requestDocument);
		}
	}

	public AuthnRequest unmarshallRequest(Element requestDocument, boolean signed) throws SignatureValueException, ReferenceValueException, UnmarshallerException
	{
		if (signed)
		{
			return this.unmarshaller.unMarshallSigned(requestDocument);
		}
		else
		{
			return this.unmarshaller.unMarshallUnSigned(requestDocument);
		}
	}

	private result validateSession(SSOProcessorData data)
	{
		String remoteAddr = data.getRemoteAddress();
		try
		{
			AuthnRequest authnRequest = data.getAuthnRequest();
			
			// Make sure the handler isn't dodgy.
			if (authnRequest == null)
			{
				this.logger.error("[SSO for {}] Calling SSO handler did not set AuthnRequest in the bean. Unable to continue processing.", remoteAddr);
				return SSOProcessor.result.SSOGenerationFailed;
			}
			
			// Check these so we can assume they're non-null
			if (authnRequest.getNameIDPolicy() == null
				|| authnRequest.getNameIDPolicy().isAllowCreate() == null
				|| authnRequest.getIssuer() == null
				|| authnRequest.getIssuer().getValue() == null)
			{
				this.logger.error("[SSO for {}] AuthnRequest was not complete - some required information was omitted. Unable to process.");
				return SSOProcessor.result.SSOGenerationFailed;
			}
			
			// Go to grab the session ID (and anything else we need) from the cookies.
			this.processCookies(data);
			String sessionID = data.getSessionID();
			
			data.setIssuerID(authnRequest.getIssuer().getValue());
			
			// If the user did not present a session cookie...
			if (sessionID == null || sessionID.length() <= 0)
			{
				if (authnRequest.getNameIDPolicy().isAllowCreate().booleanValue())
				{
					this.logger.info("[SSO for {}] Session not established, forcing authentication operation on principal", remoteAddr);
					return SSOProcessor.result.ForceAuthn;
				}

				this.logger.warn("[SSO for {}] Session is not established and SPEP has prevented establishment from being allowed non passively, creating failure response", remoteAddr);
				createStatusAuthnResponse(data, StatusCodeConstants.requester, StatusCodeConstants.requestDenied, "Session could not be resolved from previous session establishment data and SPEP will not allow session establishment", true);

				return SSOProcessor.result.ForcePassiveAuthn;
			}

			this.logger.debug("[SSO for {}] Querying sessions processor for session ID {}", new Object[] { remoteAddr, sessionID });
			Principal principal = this.sessionsProcessor.getQuery().queryAuthnSession(sessionID);
			
			// If the user's session cookie does not point to a valid session.
			if (principal == null)
			{
				// This is the same logic as when they do not have a session cookie, just that the log statements are different.
				if (authnRequest.getNameIDPolicy().isAllowCreate().booleanValue())
				{
					this.logger.warn("[SSO for {}] Could not locate a current session in the session cache. Forcing authentication operation on principal", remoteAddr);
					return SSOProcessor.result.ForceAuthn;
				}

				this.logger.warn("[SSO for {}] Could not locate a current session in the session cache, and SPEP has prevented establishment from being allowed non passively, creating failure response", remoteAddr);
				createStatusAuthnResponse(data, StatusCodeConstants.responder, StatusCodeConstants.authnFailed, "Could not locate principal in local session for supposedly active session identifier", true);

				return SSOProcessor.result.ForcePassiveAuthn;
			}

			// Store principal in the bean for future use.
			data.setPrincipal(principal);
			this.logger.debug("[SSO for {}] Retrieved principal for session ID {} - principal authn identifier is {}", new Object[] { remoteAddr, sessionID, principal.getPrincipalAuthnIdentifier() });

			if (authnRequest.isForceAuthn() != null && authnRequest.isForceAuthn().booleanValue())
			{
				TimeZone utc = new SimpleTimeZone(0, ConfigurationConstants.timeZone);
				GregorianCalendar cal = new GregorianCalendar(utc);
				long thisTime = cal.getTimeInMillis();

				/*
				 * The SP has indicated it wishes to force authn. If our principal authenticated more then allowed time skew
				 * in the past then we have to auth them again, the invisible else case is that the principal has
				 * authenticated within the allowed time skew window thus they can continue on their way
				 */
				if ((thisTime - principal.getAuthnTimestamp() > this.allowedTimeSkew))
				{
					/*
					 * The SP indicated it expects the interaction to be passive and it wishes to ensure the principal
					 * undertakes authn, we can't grant this so return error
					 */
					if (authnRequest.isIsPassive() != null && authnRequest.isIsPassive().booleanValue())
					{
						this.logger.debug("[SSO for {}] SPEP has requested forced authn as part of this SSO operation and has also requested only passive session establishment which is not supported", remoteAddr);
						createStatusAuthnResponse(data, StatusCodeConstants.responder, StatusCodeConstants.noPassive, "ESOE does not support passive only session establishment", true);
						return SSOProcessor.result.ForcePassiveAuthn;
					}

					this.logger.info("[SSO for {}] SPEP has requested forced authn as part of this SSO operation, forcing authentication", remoteAddr);
					return SSOProcessor.result.ForceAuthn;
				}
			}

			/* Determine if we have an identifier for this principal to use when communicating with remote SPEP's */
			if (principal.getSAMLAuthnIdentifier() == null || principal.getSAMLAuthnIdentifier().length() <= 0)
			{
				createStatusAuthnResponse(data, StatusCodeConstants.responder, StatusCodeConstants.authnFailed, "The SAMLID identifying this principal to SPEP's is corrupted or invalid", true);
				this.sessionsProcessor.getTerminate().terminateSession(sessionID);
				return SSOProcessor.result.SSOGenerationFailed;
			}

			/* Generate SAML session index */
			String sessionIndex = this.identifierGenerator.generateSAMLSessionID();
			this.sessionsProcessor.getUpdate().addEntitySessionIndex(principal, data.getIssuerID(), sessionIndex);
			this.authnLogger.info("[SSO for {}] Session established for SAML ID {} at SPEP {} and endpoint {}  Principal authn identifier is {}  New session index is {}", new Object[] { remoteAddr, principal.getSAMLAuthnIdentifier(), data.getIssuerID(), data.getResponseEndpoint(), principal.getPrincipalAuthnIdentifier(), sessionIndex });
			
			data.setSessionIndex(sessionIndex);

			// Return null to indicate that there is no user agent interaction required.
			return null;
		}
		catch (SessionCacheUpdateException e)
		{
			this.logger.error("[SSO for {}] Failed to update session cache. Error was: {}", new Object[]{remoteAddr, e.getMessage()});
			this.logger.debug(MessageFormatter.format("[SSO for {}] Failed to update session cache. Exception follows.", remoteAddr), e);
			return SSOProcessor.result.SSOGenerationFailed;
		}
		catch (SSOException e)
		{
			this.logger.error("[SSO for {}] Failed to perform SSO operation. Error was: {}", new Object[]{remoteAddr, e.getMessage()});
			this.logger.debug(MessageFormatter.format("[SSO for {}] Failed to perform SSO operation. Exception follows.", remoteAddr), e);
			return SSOProcessor.result.SSOGenerationFailed;
		}
	}
	
	private void processCookies(SSOProcessorData data)
	{
		String remoteAddr = data.getRemoteAddress();

		HttpServletRequest request = data.getHttpRequest();
		
		if (request == null)
		{
			this.logger.warn("[SSO for {}] No HTTP request object was passed in by the SSO handler. Unable to process cookies.", remoteAddr);
			return;
		}
		
		Cookie[] cookies = request.getCookies();
		if (cookies != null)
		{
			for (Cookie cookie : cookies)
			{
				this.logger.debug("[SSO for {}] Processing cookie {} = {}", new Object[]{remoteAddr, cookie.getName(), cookie.getValue()});
				if (cookie.getName().equals(this.sessionTokenName))
				{
					this.logger.debug("[SSO for {}] Identified ESOE cookie {} = {}", new Object[]{remoteAddr, cookie.getName(), cookie.getValue()});
					data.setSessionID(cookie.getValue());
					
					// We don't need any further cookies. Remove this if that changes.
					return;
				}
			}
		}
		else
		{
			this.logger.debug("[SSO for {}] No cookies in HTTP request.", remoteAddr);
		}
	}

	private Response createSuccessfulAuthnResponseObject(SSOProcessorData data, String sessionIndex, String charset, boolean signed) throws SSOException
	{
		Principal principal = data.getPrincipal();
		if (principal == null)
		{
			throw new SSOException("SSO response being generated for null principal. Unable to continue.");
		}
		
		String remoteAddress = data.getRemoteAddress();
		this.logger.debug("[SSO for {}] Creating successful Authn response.", remoteAddress);

		AuthnStatement authnStatement = new AuthnStatement();
		SubjectLocality subjectLocality = new SubjectLocality();
		AuthnContext authnContext = new AuthnContext();
		Subject subject = new Subject();
		NameIDType issuer = new NameIDType();
		NameIDType nameID = new NameIDType();
		Signature signature = new Signature();
		Conditions conditions = new Conditions();
		Assertion assertion = new Assertion();
		Status status = new Status();
		StatusCode statusCode = new StatusCode();
		Response response = new Response();

		/* Generate subject locality */
		subjectLocality.setDNSName(data.getHttpRequest().getServerName());

		/*
		 * Generate AuthnContext, SAML spec requires previous session to be set if user not directly authenticated
		 * during this transaction
		 */

		TimeZone utc = new SimpleTimeZone(0, ConfigurationConstants.timeZone);
		GregorianCalendar cal = new GregorianCalendar(utc);
		long thisTime = cal.getTimeInMillis();

		if (thisTime - principal.getAuthnTimestamp() > this.allowedTimeSkew)
			authnContext.setAuthnContextClassRef(AuthenticationContextConstants.previousSession);
		else
			authnContext.setAuthnContextClassRef(principal.getAuthenticationContextClass());

		/* Generate successful authentication response for consumption by SPEP */
		authnStatement.setAuthnInstant(CalendarUtils.generateXMLCalendar(0));
		authnStatement.setSessionIndex(sessionIndex);
		/* Add our allowed time skew to the current time */
		// TODO check that this is still accurate
		authnStatement.setSessionNotOnOrAfter(CalendarUtils.generateXMLCalendar(principal.getSessionNotOnOrAfter()) );

		authnStatement.setSubjectLocality(subjectLocality);
		authnStatement.setAuthnContext(authnContext);

		/* Generate Issuer to attach to assertion and response */
		issuer.setValue(this.esoeIdentifier);
		issuer.setFormat(NameIDFormatConstants.entity);

		/* populate subject to attach to assertion */
		boolean setNameID = false;

		/**
		 * If we're dealing with google insert mail identifier specially now and in special format until they correct
		 * their problems
		 */
		if (data.getAuthnRequest().getIssuer().getValue().contains("google"))
		{
			/* Figure out what value we should try and get from the principals attributes - google wants email */
			String attribName = this.identifierAttributeMapping.get(NameIDFormatConstants.emailAddress);

			if (attribName != null)
			{
				IdentityAttribute identityAttribute = principal.getAttributes().get(attribName);

				if (identityAttribute != null)
				{
					List<Object> attribValues = identityAttribute.getValues();
					if (attribValues != null && attribValues.size() > 0)
					{
						String completeEmailAddress = (String) attribValues.get(0);

						int indAt = completeEmailAddress.indexOf('@');
						nameID.setValue(completeEmailAddress.substring(0, indAt));
						nameID.setFormat(NameIDFormatConstants.emailAddress);
						setNameID = true;
					}
				}
			}
		}
		else
		{
			if (data.getValidIdentifiers() != null)
			{
				this.logger.debug("Got valid identifiers from Metadata or request, attempting to find appropriate value");
				for (String identifier : data.getValidIdentifiers())
				{
					if (NameIDFormatConstants.trans.equals(identifier))
					{
						this.logger.info("Sending AuthnStatement with transient identifier");
						nameID.setValue(principal.getSAMLAuthnIdentifier());
						nameID.setFormat(NameIDFormatConstants.trans);
						setNameID = true;
						break;
					}

					/* Figure out what value we should try and get from the principals attributes */
					String attribName = this.identifierAttributeMapping.get(identifier);
					if (attribName != null)
					{
						IdentityAttribute identityAttribute = principal.getAttributes().get(attribName);

						if (identityAttribute != null)
						{
							List<Object> attribValues = identityAttribute.getValues();
							if (attribValues != null && attribValues.size() > 0)
							{
								this.logger.info("Sending AuthnStatement with identifier of type " + identifier);

								/* Use the first value if there are multiples */
								nameID.setValue((String) attribValues.get(0));
								nameID.setFormat(identifier);
								setNameID = true;
								break;
							}
						}
					}
				}
			}
		}

		/* We couldn't figure out an identifier to response with, so send transient identifier */
		if (!setNameID)
		{
			nameID.setValue(principal.getSAMLAuthnIdentifier());
			nameID.setFormat(NameIDFormatConstants.trans);
		}

		subject.setNameID(nameID);

		/* subject MUST contain a SubjectConfirmation */
		SubjectConfirmation confirmation = new SubjectConfirmation();
		confirmation.setMethod(ConfirmationMethodConstants.bearer);
		SubjectConfirmationDataType confirmationData = new SubjectConfirmationDataType();
		confirmationData.setRecipient(data.getResponseEndpoint());
		confirmationData.setInResponseTo(data.getAuthnRequest().getID());
		confirmationData.setNotOnOrAfter(CalendarUtils.generateXMLCalendar(principal.getSessionNotOnOrAfter()) );
		confirmation.setSubjectConfirmationData(confirmationData);
		subject.getSubjectConfirmationNonID().add(confirmation);

		/* Set conditions on the response. Restrict audience to the SPEP recieving this Response. */
		conditions.setNotOnOrAfter(CalendarUtils.generateXMLCalendar(this.allowedTimeSkew));
		List<ConditionAbstractType> audienceRestrictions = conditions.getConditionsAndOneTimeUsesAndAudienceRestrictions();
		AudienceRestriction restrict = new AudienceRestriction();
		restrict.getAudiences().add(data.getIssuerID());
		audienceRestrictions.add(restrict);

		/* set assertion values */
		assertion.setConditions(conditions);
		assertion.setVersion(VersionConstants.saml20);
		assertion.setID(this.identifierGenerator.generateSAMLID());
		assertion.setIssueInstant(CalendarUtils.generateXMLCalendar(0));
		assertion.setIssuer(issuer);
		assertion.setSignature(signature);
		assertion.setSubject(subject);
		assertion.getAuthnStatementsAndAuthzDecisionStatementsAndAttributeStatements().add(authnStatement);

		/* Generate successful status, only top level code supplied */
		statusCode.setValue(StatusCodeConstants.success);
		status.setStatusCode(statusCode);

		/* Generate our response */
		response.setID(this.identifierGenerator.generateSAMLID());
		response.setInResponseTo(data.getAuthnRequest().getID());
		response.setVersion(VersionConstants.saml20);
		response.setIssueInstant(CalendarUtils.generateXMLCalendar(0));
		response.setDestination(data.getResponseEndpoint());
		response.setConsent(ConsentIdentifierConstants.prior);

		response.setIssuer(issuer);
		response.setSignature(new Signature());
		response.setStatus(status);
		response.getEncryptedAssertionsAndAssertions().add(assertion);
		
		return response;
	}
	

	public byte[] createSuccessfulAuthnResponse(SSOProcessorData data, String sessionIndex, String charset, boolean signed) throws SSOException
	{
		String remoteAddress = data.getRemoteAddress();
		
		Response response = this.createSuccessfulAuthnResponseObject(data, sessionIndex, charset, signed);
		try
		{
			this.logger.debug("[SSO for {}] Created Authn response object. Going to marshal to response document", remoteAddress);
			byte[] retval = marshallResponse(response, signed, charset);
			this.logger.debug("[SSO for {}] Marshalled Authn response document successfully.", remoteAddress);
			return retval;
		}
		catch (MarshallerException e)
		{
			throw new SSOException("Unable to marshal successful Authn response. Failing", e);
		}
	}
	
	public Element createSuccessfulAuthnResponseElement(SSOProcessorData data, String sessionIndex, String charset, boolean signed) throws SSOException
	{
		String remoteAddress = data.getRemoteAddress();
		
		Response response = this.createSuccessfulAuthnResponseObject(data, sessionIndex, charset, signed);
		try
		{
			this.logger.debug("[SSO for {}] Created Authn response object. Going to marshal to response document", remoteAddress);
			Element retval = marshallResponseElement(response, signed, charset);
			this.logger.debug("[SSO for {}] Marshalled Authn response document successfully.", remoteAddress);
			return retval;
		}
		catch (MarshallerException e)
		{
			throw new SSOException("Unable to marshal successful Authn response. Failing", e);
		}
	}
	
	private void generateForceAuthnResponse(SSOProcessorData data) throws SSOException
	{
		String remoteAddress = data.getRemoteAddress();
		HttpServletRequest request = data.getHttpRequest();
		HttpServletResponse response = data.getHttpResponse();
		try
		{
			byte[] encodedURL;
			String encodedURLString;

			this.logger.debug("[SSO for {}] Generating ForceAuthn response for unauthenticated user. SSO URL for redirect is {}", new Object[]{remoteAddress, this.ssoURL});

			/* Base64 encode dynamic redirect URL */
			encodedURL = Base64.encodeBase64(this.ssoURL.getBytes("UTF-16")); //$NON-NLS-1$
			encodedURLString = new String(encodedURL);

			String redirectURL = this.authnRedirectURL + "?" + this.authnDynamicURLParam + "=" + encodedURLString;

			this.logger.info("[SSO for {}] Redirecting unauthenticated user to {}", new Object[]{remoteAddress, redirectURL});
			response.sendRedirect(redirectURL);
		}
		catch (IOException e)
		{
			this.logger.warn("[SSO for {}] An I/O error occurred while trying to generate a ForceAuthn response. Error was: {}", new Object[]{remoteAddress, e.getMessage()});
			throw new SSOException("I/O error while trying to generate ForceAuthn response.", e);
		}
	}
}
