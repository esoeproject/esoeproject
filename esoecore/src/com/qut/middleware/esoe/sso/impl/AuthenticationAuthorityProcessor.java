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

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.w3._2000._09.xmldsig_.Signature;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.crypto.KeyStoreResolver;
import com.qut.middleware.esoe.log4j.AuthnLogLevel;
import com.qut.middleware.esoe.log4j.InsaneLogLevel;
import com.qut.middleware.esoe.metadata.Metadata;
import com.qut.middleware.esoe.metadata.exception.InvalidMetadataEndpointException;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.esoe.sessions.exception.InvalidDescriptorIdentifierException;
import com.qut.middleware.esoe.sso.SSOProcessor;
import com.qut.middleware.esoe.sso.bean.SSOProcessorData;
import com.qut.middleware.esoe.sso.exception.InvalidRequestException;
import com.qut.middleware.esoe.sso.exception.InvalidSessionIdentifierException;
import com.qut.middleware.esoe.util.CalendarUtils;
import com.qut.middleware.saml2.AuthenticationContextConstants;
import com.qut.middleware.saml2.ConfirmationMethodConstants;
import com.qut.middleware.saml2.ConsentIdentifierConstants;
import com.qut.middleware.saml2.ExternalKeyResolver;
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
public class AuthenticationAuthorityProcessor implements SSOProcessor
{
	private Unmarshaller<AuthnRequest> unmarshaller;
	private Marshaller<Response> marshaller;

	private SAMLValidator samlValidator;
	private SessionsProcessor sessionsProcessor;
	private Metadata metadata;
	private IdentifierGenerator identifierGenerator;

	private int allowedTimeSkew;
	private int minimalTimeRemaining;
	
	private final String UNMAR_PKGNAMES = AuthnRequest.class.getPackage().getName();
	private final String MAR_PKGNAMES = Response.class.getPackage().getName();

	/* Local logging instance */
	private Logger logger = Logger.getLogger(AuthenticationAuthorityProcessor.class.getName());

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
	 * @param schema
	 *            String reprsentation of the schemas required to validate AuthnRequests
	 * @param allowedTimeSkew
	 *            Time skew in seconds we will accept our SPEP being out, this applied to both recieved messages and
	 *            responses
	 * @param minimalTimeRemaining
	 * 		      Time in seconds that a principal has to have remaining in their session in order to be granted a new session on some remote SPEP
	 *            
	 * @throws UnmarshallerException if the unmarshaller cannot be created.
	 * @throws MarshallerException if the marshaller cannot be created.
	 */
	public AuthenticationAuthorityProcessor(SAMLValidator samlValidator, SessionsProcessor sessionsProcessor,
			Metadata metadata, IdentifierGenerator identifierGenerator, ExternalKeyResolver extKeyResolver,
			KeyStoreResolver keyStoreResolver, String schema, int allowedTimeSkew, int minimalTimeRemaining) throws UnmarshallerException,
			MarshallerException
	{
		/* Ensure that a stable base is created when this Processor is setup */
		if (samlValidator == null)
		{
			this.logger.fatal(Messages.getString("AuthenticationAuthorityProcessor.0")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("AuthenticationAuthorityProcessor.0")); //$NON-NLS-1$
		}

		if (sessionsProcessor == null)
		{
			this.logger.fatal(Messages.getString("AuthenticationAuthorityProcessor.1")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("AuthenticationAuthorityProcessor.1")); //$NON-NLS-1$
		}

		if (metadata == null)
		{
			this.logger.fatal(Messages.getString("AuthenticationAuthorityProcessor.2")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("AuthenticationAuthorityProcessor.2")); //$NON-NLS-1$
		}

		if (identifierGenerator == null)
		{
			this.logger.fatal(Messages.getString("AuthenticationAuthorityProcessor.3")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("AuthenticationAuthorityProcessor.3")); //$NON-NLS-1$
		}

		if (extKeyResolver == null)
		{
			this.logger.fatal(Messages.getString("AuthenticationAuthorityProcessor.15")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("AuthenticationAuthorityProcessor.15")); //$NON-NLS-1$
		}

		if (keyStoreResolver == null)
		{
			this.logger.fatal(Messages.getString("AuthenticationAuthorityProcessor.25")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("AuthenticationAuthorityProcessor.25")); //$NON-NLS-1$
		}

		if (schema == null)
		{
			this.logger.fatal(Messages.getString("AuthenticationAuthorityProcessor.4")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("AuthenticationAuthorityProcessor.4")); //$NON-NLS-1$
		}
		
		if(allowedTimeSkew > Integer.MAX_VALUE / 1000)
		{
			this.logger.fatal(Messages.getString("AuthenticationAuthorityProcessor.57")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("AuthenticationAuthorityProcessor.58")); //$NON-NLS-1$
		}
		
		if(minimalTimeRemaining > Integer.MAX_VALUE / 1000)
		{
			this.logger.fatal(Messages.getString("AuthenticationAuthorityProcessor.69")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("AuthenticationAuthorityProcessor.70")); //$NON-NLS-1$
		}

		this.samlValidator = samlValidator;
		this.sessionsProcessor = sessionsProcessor;
		this.metadata = metadata;
		this.identifierGenerator = identifierGenerator;

		String[] schemas = new String[] { schema };
		this.unmarshaller = new UnmarshallerImpl<AuthnRequest>(this.UNMAR_PKGNAMES, schemas, extKeyResolver);
		this.marshaller = new MarshallerImpl<Response>(this.MAR_PKGNAMES, schemas, keyStoreResolver.getKeyAlias(), keyStoreResolver.getPrivateKey());

		this.allowedTimeSkew = allowedTimeSkew * 1000;
		this.minimalTimeRemaining = minimalTimeRemaining * 1000;

		this.logger
				.info(Messages.getString("AuthenticationAuthorityProcessor.27") + schema //$NON-NLS-1$
						+ Messages.getString("AuthenticationAuthorityProcessor.28") + this.allowedTimeSkew + Messages.getString("AuthenticationAuthorityProcessor.29")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sso.SSOProcessor#execute(com.qut.middleware.esoe.sso.bean.SSOProcessorData)
	 */
	public result execute(SSOProcessorData data) throws InvalidSessionIdentifierException, InvalidRequestException
	{
		Principal principal;
		AuthnRequest authnRequest;
		String sessionIndex;

		if (data == null)
		{
			this.logger.debug(Messages.getString("AuthenticationAuthorityProcessor.17")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("AuthenticationAuthorityProcessor.17")); //$NON-NLS-1$
		}

		try
		{
			try
			{
				/* If this is a new SSO request then unmarshall / validate and set values accordingly */
				if (!data.isReturningRequest())
				{
					this.logger.log(InsaneLogLevel.INSANE, Messages.getString("AuthenticationAuthorityProcessor.30") + data.getRequestDocument()); //$NON-NLS-1$
					authnRequest = this.unmarshaller.unMarshallSigned(data.getRequestDocument());

					data.setAuthnRequest(authnRequest);

					this.logger.debug(Messages.getString("AuthenticationAuthorityProcessor.31")); //$NON-NLS-1$
					data.setDescriptorID(data.getAuthnRequest().getIssuer().getValue());
					data.setResponseEndpointID(data.getAuthnRequest().getAssertionConsumerServiceIndex().intValue());
					data.setResponseEndpoint(this.metadata.resolveAssertionConsumerService(
							data.getDescriptorID(),
							data.getResponseEndpointID()));

					/* Important information has been retrieved to respond, now validate the entire document */
					this.logger.debug(Messages.getString("AuthenticationAuthorityProcessor.32")); //$NON-NLS-1$
					this.samlValidator.getRequestValidator().validate(data.getAuthnRequest());

					this.logger.debug(Messages.getString("AuthenticationAuthorityProcessor.33")); //$NON-NLS-1$
					this.samlValidator.getAuthnRequestValidator().validate(data.getAuthnRequest());
				}

				if (data.getSessionID() == null || data.getSessionID().length() <= 0)
				{
					if (data.getAuthnRequest().getNameIDPolicy().isAllowCreate().booleanValue())
					{
						this.logger.info(Messages.getString("AuthenticationAuthorityProcessor.34")); //$NON-NLS-1$
						return SSOProcessor.result.ForceAuthn;
					}

					this.logger.debug(Messages.getString("AuthenticationAuthorityProcessor.35")); //$NON-NLS-1$
					createFailedAuthnResponse(data, StatusCodeConstants.requester, StatusCodeConstants.requestDenied,
							Messages.getString("AuthenticationAuthorityProcessor.19")); //$NON-NLS-1$
					return SSOProcessor.result.ForcePassiveAuthn;
				}

				principal = this.sessionsProcessor.getQuery().queryAuthnSession(data.getSessionID());

				if (principal == null || expiredPrincipal(principal))
				{
					if (data.getAuthnRequest().getNameIDPolicy().isAllowCreate().booleanValue())
					{
						this.logger.warn(Messages.getString("AuthenticationAuthorityProcessor.36")); //$NON-NLS-1$
						return SSOProcessor.result.ForceAuthn;
					}

					this.logger.warn(Messages.getString("AuthenticationAuthorityProcessor.37")); //$NON-NLS-1$
					createFailedAuthnResponse(data, StatusCodeConstants.responder, StatusCodeConstants.authnFailed,
							Messages.getString("AuthenticationAuthorityProcessor.38")); //$NON-NLS-1$
					return SSOProcessor.result.ForcePassiveAuthn;
				}

				this.logger
						.debug(Messages.getString("AuthenticationAuthorityProcessor.39") + data.getSessionID() + Messages.getString("AuthenticationAuthorityProcessor.40") + principal.getPrincipalAuthnIdentifier()); //$NON-NLS-1$ //$NON-NLS-2$

				if (data.getAuthnRequest().isForceAuthn().booleanValue())
				{
					TimeZone utc = new SimpleTimeZone(0, ConfigurationConstants.timeZone);
					GregorianCalendar cal = new GregorianCalendar(utc);
					long thisTime = cal.getTimeInMillis();
					
					/*
					 * The SP indicated it expects the interaction to be passive and it wishes to ensure the principal
					 * undertakes authn, we can't grant this so return error
					 * 
					 */
					if (data.getAuthnRequest().isIsPassive().booleanValue()
							&& (thisTime - principal.getAuthnTimestamp() > this.allowedTimeSkew))
					{
						this.logger.debug(Messages.getString("AuthenticationAuthorityProcessor.41")); //$NON-NLS-1$
						createFailedAuthnResponse(data, StatusCodeConstants.responder, StatusCodeConstants.noPassive,
								Messages.getString("AuthenticationAuthorityProcessor.20")); //$NON-NLS-1$
						return SSOProcessor.result.ForcePassiveAuthn;
					}

					/*
					 * The SP has indicated it wishes to force authn. If our principal authenticated more then allowed
					 * time skew in the past then we have to auth them again, the invisible else case is that the
					 * principal has authenticated within the allowed time skew window thus they can continue on their
					 * way
					 */
					if ((thisTime - principal.getAuthnTimestamp() > this.allowedTimeSkew))
					{
						this.logger.info(Messages.getString("AuthenticationAuthorityProcessor.42")); //$NON-NLS-1$
						return SSOProcessor.result.ForceAuthn;
					}
				}

				/* Determine if we have an identifier for this principal to use when communicating with remote SPEP's */
				if (principal.getSAMLAuthnIdentifier() == null || principal.getSAMLAuthnIdentifier().length() <= 0)
				{
					createFailedAuthnResponse(data, StatusCodeConstants.responder, StatusCodeConstants.authnFailed, Messages.getString("AuthenticationAuthorityProcessor.62")); //$NON-NLS-1$
					this.sessionsProcessor.getTerminate().terminateSession(data.getSessionID());
					throw new InvalidSessionIdentifierException(Messages.getString("AuthenticationAuthorityProcessor.63")); //$NON-NLS-1$
				}

				/* Determine if the principal has previously had a record of communicating to this SPEP in this session */
				if (principal.getActiveDescriptors() != null
						&& !principal.getActiveDescriptors().contains(data.getDescriptorID()))
				{
					this.logger
							.log(
									AuthnLogLevel.AUTHN,
									Messages.getString("AuthenticationAuthorityProcessor.45") + data.getDescriptorID() + Messages.getString("AuthenticationAuthorityProcessor.46") + data.getResponseEndpoint()); //$NON-NLS-1$ //$NON-NLS-2$
					this.sessionsProcessor.getUpdate().updateDescriptorList(data.getSessionID(), data.getDescriptorID());
				}

				/* Generate SAML session index */
				sessionIndex = this.identifierGenerator.generateSAMLSessionID();
				principal.addDescriptorSessionIdentifier(data.getDescriptorID(), sessionIndex);
				
				this.logger
						.log(
								AuthnLogLevel.AUTHN,
								Messages.getString("AuthenticationAuthorityProcessor.47") + sessionIndex + Messages.getString("AuthenticationAuthorityProcessor.48") + data.getDescriptorID() + Messages.getString("AuthenticationAuthorityProcessor.49") + principal.getPrincipalAuthnIdentifier()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				this.sessionsProcessor.getUpdate().updateDescriptorSessionIdentifierList(data.getSessionID(),
						data.getDescriptorID(), sessionIndex);

				/* Update our principal to get all the changes */
				principal = this.sessionsProcessor.getQuery().queryAuthnSession(data.getSessionID());
				
				createSucessfulAuthnResponse(data, principal, sessionIndex);

				return SSOProcessor.result.SSOGenerationSuccessful;
			}
			catch (InvalidSAMLRequestException isre)
			{
				this.logger
						.warn(Messages.getString("AuthenticationAuthorityProcessor.5") + VersionConstants.saml20 + Messages.getString(Messages.getString("AuthenticationAuthorityProcessor.50"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-				
				createFailedAuthnResponse(data, StatusCodeConstants.requester, StatusCodeConstants.authnFailed,
						Messages.getString("AuthenticationAuthorityProcessor.21")); //$NON-NLS-1$
				throw new InvalidRequestException(
						Messages.getString("AuthenticationAuthorityProcessor.5") + VersionConstants.saml20 + Messages.getString("AuthenticationAuthorityProcessor.6"), isre); //$NON-NLS-1$ //$NON-NLS-2$
			}
			catch (com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException isie)
			{
				this.logger.warn(Messages.getString("AuthenticationAuthorityProcessor.7")); //$NON-NLS-1$
				if (data.getAuthnRequest().getNameIDPolicy().isAllowCreate().booleanValue())
				{
					this.logger.warn(Messages.getString("AuthenticationAuthorityProcessor.61") ); //$NON-NLS-1$
					return SSOProcessor.result.ForceAuthn;
				}
				
				createFailedAuthnResponse(data, StatusCodeConstants.responder, StatusCodeConstants.unknownPrincipal,
						Messages.getString("AuthenticationAuthorityProcessor.22")); //$NON-NLS-1$
				
				throw new InvalidSessionIdentifierException(
						Messages.getString("AuthenticationAuthorityProcessor.7"), isie); //$NON-NLS-1$
			}
			catch (InvalidDescriptorIdentifierException ieie)
			{
				this.logger.warn(Messages.getString("AuthenticationAuthorityProcessor.10")); //$NON-NLS-1$
				createFailedAuthnResponse(data, StatusCodeConstants.responder, StatusCodeConstants.unknownPrincipal,
						Messages.getString("AuthenticationAuthorityProcessor.24")); //$NON-NLS-1$
				throw new InvalidRequestException(Messages.getString("AuthenticationAuthorityProcessor.10"), ieie); //$NON-NLS-1$
			}
		}
		catch (InvalidMetadataEndpointException imee)
		{
			this.logger.error(Messages.getString("AuthenticationAuthorityProcessor.13") + data.getDescriptorID()); //$NON-NLS-1$
			throw new InvalidRequestException(Messages.getString("AuthenticationAuthorityProcessor.13"), imee); //$NON-NLS-1$
		}
		catch (SignatureValueException sve)
		{
			this.logger.error(Messages.getString("AuthenticationAuthorityProcessor.8")); //$NON-NLS-1$
			throw new InvalidRequestException(Messages.getString("AuthenticationAuthorityProcessor.8"), sve); //$NON-NLS-1$
		}
		catch (ReferenceValueException rve)
		{
			this.logger.error(Messages.getString("AuthenticationAuthorityProcessor.11")); //$NON-NLS-1$
			throw new InvalidRequestException(Messages.getString("AuthenticationAuthorityProcessor.11"), rve); //$NON-NLS-1$
		}
		catch (UnmarshallerException ue)
		{
			this.logger.error(Messages.getString("AuthenticationAuthorityProcessor.12")); //$NON-NLS-1$
			throw new InvalidRequestException(Messages.getString("AuthenticationAuthorityProcessor.12"), ue); //$NON-NLS-1$
		}
	}
	
	/**
	 * Determines if a principal session has either expired or has enough time remaining to create new sessions on remote SPEP.
	 * Note the principal object even if timed out is not removed here, this is still the duty of the cleaning thread
	 * 
	 * @param principal CUrrent principal object
	 * @return True if the principal session has expired, false if its still able to be used in session creation
	 */
	private boolean expiredPrincipal(Principal principal)
	{
		this.logger.debug(MessageFormat.format(Messages.getString("AuthenticationAuthorityProcessor.64"), this.minimalTimeRemaining) ); //$NON-NLS-1$
		
		/* current time is current UTC time, not localized time */
		SimpleTimeZone utc = new SimpleTimeZone(0, ConfigurationConstants.timeZone);
		Calendar thisCalendar = new GregorianCalendar(utc);
		long currentTime = thisCalendar.getTimeInMillis();
		
		GregorianCalendar principalExpiry = principal.getSessionNotOnOrAfter().toGregorianCalendar();
		long expiryTime = principalExpiry.getTimeInMillis();
		
		this.logger.debug(Messages.getString("AuthenticationAuthorityProcessor.65") + principal.getSAMLAuthnIdentifier() + Messages.getString("AuthenticationAuthorityProcessor.66") + principal.getSessionNotOnOrAfter()); //$NON-NLS-1$ //$NON-NLS-2$
		
		/* If the principal session has already expired don't use it further */
		if(expiryTime < currentTime)
		{
			this.logger.debug(Messages.getString("AuthenticationAuthorityProcessor.67")); //$NON-NLS-1$
			return true;
		}
		
		if(expiryTime - currentTime < this.minimalTimeRemaining)
		{
			this.logger.debug(Messages.getString("AuthenticationAuthorityProcessor.68")); //$NON-NLS-1$
			return true;
		}
		
		this.logger.debug(Messages.getString("AuthenticationAuthorityProcessor.71") + expiryTime + Messages.getString("AuthenticationAuthorityProcessor.72") + currentTime + Messages.getString("AuthenticationAuthorityProcessor.73")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return false;
	}

	/**
	 * Creates a SAML response document for failed requests and stores it in the samlResponseDocument attribute of the
	 * principal
	 * 
	 * @param data
	 *            Populated SSOProcessor data bean
	 * @param authnRequest
	 *            The current AuthnRequest
	 * @throws InvalidRequestException
	 */
	private void createFailedAuthnResponse(SSOProcessorData data, String failStatus, String detailedFailStatus,
			String failStatusMessage) throws InvalidRequestException
	{

		this.logger
				.info(Messages.getString("AuthenticationAuthorityProcessor.51") + failStatus + Messages.getString("AuthenticationAuthorityProcessor.52") + detailedFailStatus + Messages.getString("AuthenticationAuthorityProcessor.53") + failStatusMessage); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		NameIDType issuer;
		Signature signature;
		Status status;
		StatusCode statusCode, embededStatusCode;
		Response response;

		/* Generate failed status, two layers of code supplied */
		statusCode = new StatusCode();
		statusCode.setValue(failStatus);
		embededStatusCode = new StatusCode();
		embededStatusCode.setValue(detailedFailStatus);
		statusCode.setStatusCode(embededStatusCode);

		status = new Status();
		status.setStatusCode(statusCode);
		status.setStatusMessage(failStatusMessage);

		/* Generate Issuer to attach to response */
		issuer = new NameIDType();
		issuer.setValue(this.metadata.getESOEIdentifier());

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

		marshalResponse(data, response);
	}

	/**
	 * Creates a SAML response document for successfully verified requests and stores it in the samlResponseDocument
	 * attribute of the principal. Implements the SAML 2.0 HTTP profile for generated Response.
	 * 
	 * @param data
	 *            Populated SSOProcessor data bean. The marshalled Respons estring will be stored in this
	 *            object.
	 * @param authnRequest
	 *            The current AuthnRequest.
	 * @param Principal
	 *            The current principal involved in the AuthnRequest.
	 * @param sessionIndex
	 *            The value for sessionIndex that was created for the successfully verified AuthnRequest
	 * @throws InvalidRequestException
	 */
	private void createSucessfulAuthnResponse(SSOProcessorData data, Principal principal, String sessionIndex)
			throws InvalidRequestException
	{
		this.logger.info(Messages.getString("AuthenticationAuthorityProcessor.54")); //$NON-NLS-1$

		AuthnStatement authnStatement = new AuthnStatement();
		SubjectLocality subjectLocality = new SubjectLocality();
		AuthnContext authnContext = new AuthnContext();
		Subject subject = new Subject();
		NameIDType issuer  = new NameIDType();
		NameIDType nameID  = new NameIDType();
		Signature signature = new Signature();
		Conditions conditions = new Conditions();
		Assertion assertion = new Assertion();
		Status status = new Status();
		StatusCode statusCode  = new StatusCode();
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
		authnStatement.setSessionNotOnOrAfter(principal.getSessionNotOnOrAfter());

		authnStatement.setSubjectLocality(subjectLocality);
		authnStatement.setAuthnContext(authnContext);

		/* Generate Issuer to attach to assertion and response */	
		issuer.setValue(this.metadata.getESOEIdentifier());
		
		/* populate subject to attach to assertion */
		nameID.setValue(principal.getSAMLAuthnIdentifier());
		subject.setNameID(nameID);
		
		/* subject MUST contain a SubjectConfirmation */
		SubjectConfirmation confirmation = new SubjectConfirmation();
		confirmation.setMethod(ConfirmationMethodConstants.bearer);
		SubjectConfirmationDataType confirmationData = new SubjectConfirmationDataType();
		confirmationData.setRecipient(data.getResponseEndpoint());
		confirmationData.setInResponseTo(data.getAuthnRequest().getID());
		confirmationData.setNotOnOrAfter(principal.getSessionNotOnOrAfter());
		confirmation.setSubjectConfirmationData(confirmationData);
		subject.getSubjectConfirmationNonID().add(confirmation);
		
		/* Set conditions on the response. Restrict audience to the SPEP recieving this Response. */
		conditions.setNotOnOrAfter(CalendarUtils.generateXMLCalendar(this.allowedTimeSkew));
		List<ConditionAbstractType> audienceRestrictions = conditions.getConditionsAndOneTimeUsesAndAudienceRestrictions();
		AudienceRestriction restrict = new AudienceRestriction();
		restrict.getAudiences().add(data.getResponseEndpoint());
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

		marshalResponse(data, response);
	}

	
	/**
	 * Uses SAML2lib-j to marshall a response object to string and store in samlResponseDocument of supplied data bean.
	 * 
	 * @param data
	 *            Populated SSOProcessor data bean
	 * @param response
	 *            A fully populated SAML response object for the response type to be returned to the SPEP
	 * @throws InvalidRequestException
	 */
	private void marshalResponse(SSOProcessorData data, Response response) throws InvalidRequestException
	{
		String responseDocument;
		try
		{
			responseDocument = this.marshaller.marshallSigned(response);
			data.setResponseDocument(responseDocument);
			this.logger.log(InsaneLogLevel.INSANE, Messages.getString("AuthenticationAuthorityProcessor.56") + data.getResponseDocument()); //$NON-NLS-1$
		}
		catch (MarshallerException me)
		{
			throw new InvalidRequestException(Messages.getString("AuthenticationAuthorityProcessor.14"), me); //$NON-NLS-1$
		}
	}
}
