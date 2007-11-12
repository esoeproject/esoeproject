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
 * Creation Date: 13/11/2006
 * 
 * Purpose: Implements the AuthnProcessor interface.
 */
package com.qut.middleware.spep.authn.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.w3._2000._09.xmldsig_.Signature;

import com.qut.middleware.saml2.NameIDFormatConstants;
import com.qut.middleware.saml2.StatusCodeConstants;
import com.qut.middleware.saml2.VersionConstants;
import com.qut.middleware.saml2.exception.InvalidSAMLAssertionException;
import com.qut.middleware.saml2.exception.InvalidSAMLRequestException;
import com.qut.middleware.saml2.exception.InvalidSAMLResponseException;
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
import com.qut.middleware.saml2.schemas.assertion.AuthnStatement;
import com.qut.middleware.saml2.schemas.assertion.ConditionAbstractType;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.assertion.Subject;
import com.qut.middleware.saml2.schemas.assertion.SubjectConfirmation;
import com.qut.middleware.saml2.schemas.assertion.SubjectConfirmationDataType;
import com.qut.middleware.saml2.schemas.protocol.AuthnRequest;
import com.qut.middleware.saml2.schemas.protocol.LogoutRequest;
import com.qut.middleware.saml2.schemas.protocol.LogoutResponse;
import com.qut.middleware.saml2.schemas.protocol.NameIDPolicy;
import com.qut.middleware.saml2.schemas.protocol.Response;
import com.qut.middleware.saml2.schemas.protocol.Status;
import com.qut.middleware.saml2.schemas.protocol.StatusCode;
import com.qut.middleware.saml2.schemas.protocol.StatusResponseType;
import com.qut.middleware.saml2.validator.SAMLValidator;
import com.qut.middleware.spep.ConfigurationConstants;
import com.qut.middleware.spep.attribute.AttributeProcessor;
import com.qut.middleware.spep.authn.AuthnProcessor;
import com.qut.middleware.spep.authn.AuthnProcessorData;
import com.qut.middleware.spep.authn.Messages;
import com.qut.middleware.spep.exception.AttributeProcessingException;
import com.qut.middleware.spep.exception.AuthenticationException;
import com.qut.middleware.spep.exception.LogoutException;
import com.qut.middleware.spep.metadata.KeyStoreResolver;
import com.qut.middleware.spep.metadata.Metadata;
import com.qut.middleware.spep.sessions.PrincipalSession;
import com.qut.middleware.spep.sessions.SessionCache;
import com.qut.middleware.spep.sessions.UnauthenticatedSession;
import com.qut.middleware.spep.sessions.impl.PrincipalSessionImpl;
import com.qut.middleware.spep.sessions.impl.UnauthenticatedSessionImpl;
import com.qut.middleware.spep.util.CalendarUtils;

/** */
public class AuthnProcessorImpl implements AuthnProcessor
{
	private String[] authnSchemas = new String[] { ConfigurationConstants.samlProtocol, ConfigurationConstants.samlAssertion };
	private Marshaller<AuthnRequest> authnRequestMarshaller;
	private Unmarshaller<Response> responseUnmarshaller;
	private Metadata metadata;
	private SAMLValidator samlValidator;
	private IdentifierGenerator identifierGenerator;
	private SessionCache sessionCache;
	private AttributeProcessor attributeProcessor;
	private URL serviceURL;
	private String ssoRedirect;
	private Integer assertionConsumerServiceIndex;
	private Integer attributeConsumingServiceIndex;
	private String[] logoutSchemas;
	private Unmarshaller<LogoutRequest> logoutRequestUnmarshaller;
	private Marshaller<LogoutResponse> logoutResponseMarshaller;

	private final String UNMAR_PKGNAMES = LogoutRequest.class.getPackage().getName();
	private final String UNMAR_PKGNAMES2 = Response.class.getPackage().getName();
	private final String MAR_PKGNAMES = LogoutRequest.class.getPackage().getName();
	private final String MAR_PKGNAMES2 = AuthnRequest.class.getPackage().getName();
	
	private final String PROTOCOL_SPLIT = "://";
	private final String PORT_SPLIT = ":";

	/* Local logging instance */
	private Logger logger = Logger.getLogger(AuthnProcessorImpl.class.getName());
	private Logger authnLogger = Logger.getLogger(ConfigurationConstants.authnLogger);

	/**
	 * Constructor.
	 * 
	 * @param reportingProcessor
	 *            The reporting processor to use for SPEP logging.
	 * @param attributeProcessor
	 *            The sttribute processoe to use when resolving principal attributes.
	 * @param metadata
	 *            Used to retrieve SPEP service information and configuration details.
	 * @param sessionCache
	 *            Stores principal session details.
	 * @param samlValidator
	 *            Used to validate SAML Requests and Responses.
	 * @param identifierGenerator
	 *            For generating unique identifiers.
	 * @param keyStoreResolver
	 *            Used to resolve public/private keys to be used in crypto operations.
	 * @param serviceURL
	 * 		   URL used to access this service without application specific paths ie https://server.company.com
	 * @param ssoRedirect
	 * 		  Relative URL to this service SSO endpoint (if supplied with query string param this is stripped away)
	 * @param attributeConsumingServiceIndex
	 *            Index of this SPEP node used to resolve attribute consumer service URL.
	 * @param assertionConsumerServiceIndex
	 *            Index of this SPEP node used to resolve assertion consumer service URL
	 * @throws MarshallerException
	 *             if the marshallers cannot be created.
	 * @throws UnmarshallerException
	 *             if the unmarshallers cannot be created.
	 * @throws MalformedURLException 
	 */
	public AuthnProcessorImpl(AttributeProcessor attributeProcessor, Metadata metadata, SessionCache sessionCache, SAMLValidator samlValidator, IdentifierGenerator identifierGenerator, KeyStoreResolver keyStoreResolver, 
			String serviceURL, String ssoRedirect, int attributeConsumingServiceIndex, int assertionConsumerServiceIndex) throws MarshallerException, UnmarshallerException, MalformedURLException
	{
		if (attributeProcessor == null)
		{
			throw new IllegalArgumentException(Messages.getString("AuthnProcessorImpl.44")); //$NON-NLS-1$
		}
		if (metadata == null)
		{
			throw new IllegalArgumentException(Messages.getString("AuthnProcessorImpl.45")); //$NON-NLS-1$
		}
		if (sessionCache == null)
		{
			throw new IllegalArgumentException(Messages.getString("AuthnProcessorImpl.46")); //$NON-NLS-1$
		}
		if (samlValidator == null)
		{
			throw new IllegalArgumentException(Messages.getString("AuthnProcessorImpl.47")); //$NON-NLS-1$
		}
		if (identifierGenerator == null)
		{
			throw new IllegalArgumentException(Messages.getString("AuthnProcessorImpl.48")); //$NON-NLS-1$
		}
		if (keyStoreResolver == null)
		{
			throw new IllegalArgumentException(Messages.getString("AuthnProcessorImpl.49")); //$NON-NLS-1$
		}
		if (keyStoreResolver == null)
		{
			throw new IllegalArgumentException(Messages.getString("AuthnProcessorImpl.49")); //$NON-NLS-1$
		}
		if (serviceURL == null)
		{
			throw new IllegalArgumentException("Supplied serviceURL was null"); //$NON-NLS-1$
		}
		if (ssoRedirect == null)
		{
			throw new IllegalArgumentException("Supplied ssoRedirect was null"); //$NON-NLS-1$
		}
		if (attributeConsumingServiceIndex < 0 || attributeConsumingServiceIndex > Long.MAX_VALUE / 1000)
		{
			throw new IllegalArgumentException(Messages.getString("AuthnProcessorImpl.50")); //$NON-NLS-1$
		}
		if (assertionConsumerServiceIndex < 0 || assertionConsumerServiceIndex > Long.MAX_VALUE / 1000)
		{
			throw new IllegalArgumentException(Messages.getString("AuthnProcessorImpl.51")); //$NON-NLS-1$
		}

		this.attributeProcessor = attributeProcessor;
		this.metadata = metadata;
		this.sessionCache = sessionCache;
		this.samlValidator = samlValidator;
		this.serviceURL = new URL(serviceURL);
		this.attributeConsumingServiceIndex = Integer.valueOf(attributeConsumingServiceIndex);
		this.assertionConsumerServiceIndex = Integer.valueOf(assertionConsumerServiceIndex);
		this.authnRequestMarshaller = new MarshallerImpl<AuthnRequest>(this.MAR_PKGNAMES2, this.authnSchemas, keyStoreResolver.getKeyAlias(), keyStoreResolver.getPrivateKey());
		this.responseUnmarshaller = new UnmarshallerImpl<Response>(this.UNMAR_PKGNAMES2, this.authnSchemas, this.metadata);
		this.logoutSchemas = new String[] { ConfigurationConstants.samlProtocol };
		this.logoutRequestUnmarshaller = new UnmarshallerImpl<LogoutRequest>(this.UNMAR_PKGNAMES, this.logoutSchemas, this.metadata);
		this.logoutResponseMarshaller = new MarshallerImpl<LogoutResponse>(this.MAR_PKGNAMES, this.logoutSchemas, keyStoreResolver.getKeyAlias(), keyStoreResolver.getPrivateKey());
		this.identifierGenerator = identifierGenerator;
		
		int splitIndex = ssoRedirect.indexOf('?');
		if(splitIndex == -1)
			this.ssoRedirect = ssoRedirect;
		else
			this.ssoRedirect = ssoRedirect.substring(0, splitIndex);

		this.logger.debug(Messages.getString("AuthnProcessorImpl.16")); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.spep.authn.AuthnProcessor#generateAuthnRequest()
	 */
	public void generateAuthnRequest(AuthnProcessorData data) throws AuthenticationException
	{
		if (data == null)
		{
			this.logger.error( Messages.getString("AuthnProcessorImpl.0")); //$NON-NLS-1$
			throw new AuthenticationException(Messages.getString("AuthnProcessorImpl.1")); //$NON-NLS-1$
		}

		UnauthenticatedSession unauthenticatedSession = new UnauthenticatedSessionImpl();
		String authnRequestSAMLID = this.identifierGenerator.generateSAMLID();
		unauthenticatedSession.setAuthnRequestSAMLID(authnRequestSAMLID);
		unauthenticatedSession.setRequestURL(data.getRequestURL());

		this.logger.debug(Messages.getString("AuthnProcessorImpl.17")); //$NON-NLS-1$

		AuthnRequest authnRequest = new AuthnRequest();

		authnRequest.setIssueInstant(CalendarUtils.generateXMLCalendar());

		NameIDPolicy nameIDPolicy = new NameIDPolicy();

		nameIDPolicy.setFormat(NameIDFormatConstants.trans);
		nameIDPolicy.setAllowCreate(Boolean.TRUE);
		authnRequest.setNameIDPolicy(nameIDPolicy);
		authnRequest.setForceAuthn(Boolean.FALSE);
		authnRequest.setIsPassive(Boolean.FALSE);
		authnRequest.setVersion(VersionConstants.saml20);
		authnRequest.setSignature(new Signature());

		authnRequest.setID(authnRequestSAMLID);
		
		// Determine if the request was to the service URL or directly to the node, the former sets an
		// AssertionConsumerIndex the later an AssertionConsumerURL
		if(data.getRequest().getServerName().equals(serviceURL.getHost()))
		{
			authnRequest.setAssertionConsumerServiceIndex(this.assertionConsumerServiceIndex);
		}
		else
		{
			try
			{
				URL requestURL = new URL(data.getRequest().getRequestURL().toString());
				
				if(requestURL.getPort() == -1)
					authnRequest.setAssertionConsumerServiceURL(requestURL.getProtocol() + this.PROTOCOL_SPLIT + requestURL.getHost() + this.ssoRedirect);
				else
					authnRequest.setAssertionConsumerServiceURL(requestURL.getProtocol() + this.PROTOCOL_SPLIT + requestURL.getHost() + this.PORT_SPLIT + requestURL.getPort() + this.ssoRedirect);	
			}
			catch (MalformedURLException e)
			{
				this.logger.warn(
						Messages.getString("AuthnProcessorImpl.2") + e.getLocalizedMessage()); //$NON-NLS-1$
				throw new AuthenticationException(Messages.getString("AuthnProcessorImpl.3"), e); //$NON-NLS-1$
			}
		}
		
		authnRequest.setAttributeConsumingServiceIndex(this.attributeConsumingServiceIndex);

		NameIDType issuer = new NameIDType();
		issuer.setValue(this.metadata.getSPEPIdentifier());
		authnRequest.setIssuer(issuer);
		


		try
		{
			byte[] requestDocument = this.authnRequestMarshaller.marshallSigned(authnRequest);
			data.setRequestDocument(requestDocument);
		}
		catch (MarshallerException e)
		{
			this.logger.warn(
					Messages.getString("AuthnProcessorImpl.2") + e.getLocalizedMessage()); //$NON-NLS-1$
			throw new AuthenticationException(Messages.getString("AuthnProcessorImpl.3"), e); //$NON-NLS-1$
		}

		// Now that the request has been successfully created, we can add the unauthenticated session.
		this.sessionCache.putUnauthenticatedSession(authnRequestSAMLID, unauthenticatedSession);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.spep.authn.AuthnProcessor#processAuthnResponse(java.lang.String)
	 */
	public void processAuthnResponse(AuthnProcessorData data) throws AuthenticationException
	{
		if (data == null)
		{
			this.logger.error(Messages.getString("AuthnProcessorImpl.4")); //$NON-NLS-1$
			throw new AuthenticationException(Messages.getString("AuthnProcessorImpl.5")); //$NON-NLS-1$
		}

		this.logger.debug(Messages.getString("AuthnProcessorImpl.18")); //$NON-NLS-1$

		Response response = null;

		// Unmarshal the response
		try
		{
			response = this.responseUnmarshaller.unMarshallSigned(data.getResponseDocument());
			this.logger.debug(MessageFormat.format(Messages.getString("AuthnProcessorImpl.19"), response.getID())); //$NON-NLS-1$
		}
		catch (SignatureValueException e)
		{
			this.logger.warn(Messages.getString("AuthnProcessorImpl.6") + e.getLocalizedMessage()); //$NON-NLS-1$
			throw new AuthenticationException(Messages.getString("AuthnProcessorImpl.7"), e); //$NON-NLS-1$
		}
		catch (ReferenceValueException e)
		{
			this.logger.warn(Messages.getString("AuthnProcessorImpl.8") + e.getLocalizedMessage()); //$NON-NLS-1$
			throw new AuthenticationException(Messages.getString("AuthnProcessorImpl.9"), e); //$NON-NLS-1$
		}
		catch (UnmarshallerException e)
		{
			this.logger.warn(Messages.getString("AuthnProcessorImpl.10") + e.getLocalizedMessage()); //$NON-NLS-1$
			throw new AuthenticationException(Messages.getString("AuthnProcessorImpl.11"), e); //$NON-NLS-1$
		}

		try
		{
			this.samlValidator.getResponseValidator().validate(response);
		}
		catch (InvalidSAMLResponseException e)
		{
			this.logger.warn(MessageFormat.format(Messages.getString("AuthnProcessorImpl.52"), response.getID(), e.getMessage())); //$NON-NLS-1$
		}

		// Validate destination to ensure it is for this SPEP (either to service URL or local node).
		try
		{
			URL destination;
			destination = new URL(response.getDestination());
			if (!destination.getHost().equals(this.serviceURL.getHost()) && !destination.getHost().equals(data.getRequest().getServerName()))
			{
				this.logger.error(MessageFormat.format(Messages.getString("AuthnProcessorImpl.53"), this.serviceURL.getHost() + " or " + data.getRequest().getServerName(), destination.getHost())); //$NON-NLS-1$
				throw new AuthenticationException(Messages.getString("AuthnProcessorImpl.54")); //$NON-NLS-1$
			}
		}
		catch (MalformedURLException e)
		{
			this.logger.error("Unable to process destination from response " + e.getLocalizedMessage()); //$NON-NLS-1$
			throw new AuthenticationException(Messages.getString("AuthnProcessorImpl.54"), e); //$NON-NLS-1$
		}

		// obtain unauthenticated session that matches our original request
		String inResponseTo = response.getInResponseTo();

		UnauthenticatedSession unauthenticatedSession = this.sessionCache.getUnauthenticatedSession(inResponseTo);

		// Make sure that the request was actually made, and that we haven't just got some random response.
		if (unauthenticatedSession == null)
		{
			this.logger.debug(Messages.getString("AuthnProcessorImpl.12")); //$NON-NLS-1$
			throw new AuthenticationException(Messages.getString("AuthnProcessorImpl.13")); //$NON-NLS-1$
		}

		String requestURL = unauthenticatedSession.getRequestURL();
		data.setRequestURL(requestURL);

		data.setSessionID(null);

		for (Object assertionOrEncryptedAssertion : response.getEncryptedAssertionsAndAssertions())
		{
			if (assertionOrEncryptedAssertion instanceof Assertion)
			{
				Assertion assertion = (Assertion) assertionOrEncryptedAssertion;

				// ensure parameters are present. not the prettiest way to do it but it avoids
				// namespace clashes with Subject
				if (assertion.getSubject() == null)
				{
					this.logger.error(Messages.getString("AuthnProcessorImpl.58")); //$NON-NLS-1$
					throw new AuthenticationException(Messages.getString("AuthnProcessorImpl.59")); //$NON-NLS-1$
				}

				if (assertion.getSubject().getSubjectConfirmationNonID() == null)
				{
					this.logger.error(Messages.getString("AuthnProcessorImpl.60")); //$NON-NLS-1$
					throw new AuthenticationException(Messages.getString("AuthnProcessorImpl.61")); //$NON-NLS-1$
				}

				List<SubjectConfirmation> subjectConfirmations = assertion.getSubject().getSubjectConfirmationNonID();
				if (subjectConfirmations.size() == 0)
				{
					this.logger.error(Messages.getString("AuthnProcessorImpl.62")); //$NON-NLS-1$
					throw new AuthenticationException(Messages.getString("AuthnProcessorImpl.63")); //$NON-NLS-1$
				}

				for (SubjectConfirmation confirmation : subjectConfirmations)
				{
					SubjectConfirmationDataType confirmationData = confirmation.getSubjectConfirmationData();

					if (confirmationData == null)
					{
						this.logger.error(Messages.getString("AuthnProcessorImpl.64")); //$NON-NLS-1$
						throw new AuthenticationException(Messages.getString("AuthnProcessorImpl.65")); //$NON-NLS-1$
					}

					// validate recipient matches this SPEP assertion consumer location
					String recipient = confirmationData.getRecipient();

					if (recipient == null)
					{
						this.logger.error(Messages.getString("AuthnProcessorImpl.66")); //$NON-NLS-1$
						throw new AuthenticationException(Messages.getString("AuthnProcessorImpl.67")); //$NON-NLS-1$
					}
					
					try
					{
						URL recipientURL;
						recipientURL = new URL(recipient);
						if (!recipientURL.getHost().equals(this.serviceURL.getHost()) && !recipientURL.getHost().equals(data.getRequest().getServerName()))
						{
							this.logger.error(Messages.getString("AuthnProcessorImpl.68")); //$NON-NLS-1$
							this.logger.error(MessageFormat.format(Messages.getString("AuthnProcessorImpl.69"), this.serviceURL.getHost() + " or " + data.getRequest().getServerName(), recipientURL.getHost())); //$NON-NLS-1$
							throw new AuthenticationException(Messages.getString("AuthnProcessorImpl.70")); //$NON-NLS-1$
						}
					}
					catch (MalformedURLException e)
					{
						this.logger.error("Unable to process subject confirmation recipient from response " + e.getLocalizedMessage()); //$NON-NLS-1$
						throw new AuthenticationException("Unable to process subject confirmation recipient from response", e); //$NON-NLS-1$
					}

					// validate data has not expired
					XMLGregorianCalendar xmlCalendar = confirmationData.getNotOnOrAfter();
					GregorianCalendar notOnOrAfterCal = xmlCalendar.toGregorianCalendar();

					XMLGregorianCalendar thisXmlCal = CalendarUtils.generateXMLCalendar();
					GregorianCalendar thisCal = thisXmlCal.toGregorianCalendar();

					if (thisCal.after(notOnOrAfterCal))
					{
						// request is out of date
						this.logger.error(Messages.getString("AuthnProcessorImpl.71")); //$NON-NLS-1$
						throw new AuthenticationException(Messages.getString("AuthnProcessorImpl.72")); //$NON-NLS-1$
					}

				}

				// validate the Audience restrictions
				List<ConditionAbstractType> audienceRestrictions = assertion.getConditions().getConditionsAndOneTimeUsesAndAudienceRestrictions();
				if (audienceRestrictions.isEmpty())
				{
					this.logger.warn(Messages.getString("AuthnProcessorImpl.73")); //$NON-NLS-1$
					throw new AuthenticationException(Messages.getString("AuthnProcessorImpl.74")); //$NON-NLS-1$
				}

				for (ConditionAbstractType restriction : audienceRestrictions)
				{
					if (restriction instanceof AudienceRestriction)
					{
						AudienceRestriction ar = (AudienceRestriction) restriction;
						boolean validAudience = false;
						
						try
						{
							for(String audienceRestriction : ar.getAudiences())
							{
								URL audienceURL = new URL(audienceRestriction);
								
								if (audienceURL.getHost().equals(this.serviceURL.getHost()) || audienceURL.getHost().equals(data.getRequest().getServerName()))
								{
									validAudience = true;
									break;
								}
							}
						}
						catch (MalformedURLException e)
						{
							this.logger.error("Unable to process audience restriction from response " + e.getLocalizedMessage()); //$NON-NLS-1$
							throw new AuthenticationException("Unable to process audience restriction from response", e); //$NON-NLS-1$
						}
						
						if (!validAudience)
						{
							// this is not the intended audience
							this.logger.warn(Messages.getString("AuthnProcessorImpl.75")); //$NON-NLS-1$
							throw new AuthenticationException(Messages.getString("AuthnProcessorImpl.76")); //$NON-NLS-1$
						}
					}
				}

				try
				{
					this.samlValidator.getAssertionValidator().validate(assertion);
				}
				catch (InvalidSAMLAssertionException e)
				{
					e.printStackTrace();
					this.logger.warn(Messages.getString("AuthnProcessorImpl.55")); //$NON-NLS-1$
					throw new AuthenticationException(Messages.getString("AuthnProcessorImpl.56")); //$NON-NLS-1$
				}

				for (Object statement : assertion.getAuthnStatementsAndAuthzDecisionStatementsAndAttributeStatements())
				{
					if (statement instanceof AuthnStatement)
					{
						AuthnStatement authnStatement = (AuthnStatement) statement;

						String sessionID = processAuthnStatement(authnStatement, assertion);

						if (sessionID == null)
						{
							throw new AuthenticationException(Messages.getString("AuthnProcessorImpl.20")); //$NON-NLS-1$
						}

						data.setSessionID(sessionID);

						return;
					}
				}
			}
		}

		// TODO check that user unauthenticated session details are removed?
		// no assertions = an error occured on the ESOE
		this.logger.error(MessageFormat.format(Messages.getString("AuthnProcessorImpl.14"), response.getStatus().getStatusMessage())); //$NON-NLS-1$
		throw new AuthenticationException(MessageFormat.format(Messages.getString("AuthnProcessorImpl.14"), response.getStatus().getStatusMessage())); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.spep.authn.AuthnProcessor#verifySession(java.lang.String)
	 */
	public PrincipalSession verifySession(String sessionID)
	{
		PrincipalSession principalSession = this.sessionCache.getPrincipalSession(sessionID);

		if (principalSession == null)
		{
			this.logger.warn(MessageFormat.format(Messages.getString("AuthnProcessorImpl.21"), sessionID)); //$NON-NLS-1$
		}

		return principalSession;
	}

	private String processAuthnStatement(AuthnStatement authnStatement, Assertion assertion)
	{
		Subject subject = assertion.getSubject();
		String esoeSessionID = subject.getNameID().getValue();
		String esoeSessionIndex = authnStatement.getSessionIndex();

		// Validate the SessionNotOnOrAfter value to ensure this hasn't expired.
		// Timestamps MUST be set to UTC, no offset
		XMLGregorianCalendar xmlCalendar = authnStatement.getSessionNotOnOrAfter();
		GregorianCalendar notOnOrAfterCal = xmlCalendar.toGregorianCalendar();

		XMLGregorianCalendar thisXmlCal = CalendarUtils.generateXMLCalendar();
		GregorianCalendar thisCal = thisXmlCal.toGregorianCalendar();

		if (thisCal.after(notOnOrAfterCal))
		{
			// request is out of date
			this.logger.error(Messages.getString("AuthnProcessorImpl.77")); //$NON-NLS-1$
			return null;
		}
		
		PrincipalSession principalSession = this.sessionCache.getPrincipalSessionByEsoeSessionID(esoeSessionID);

		/* The above will provide an object is the principal already has an active session, otherwise create new session */
		if (principalSession == null)
			principalSession = new PrincipalSessionImpl();

		String sessionID = this.identifierGenerator.generateSessionID();

		principalSession.setSessionNotOnOrAfter(notOnOrAfterCal.getTime());
		principalSession.setEsoeSessionID(esoeSessionID);
		principalSession.addESOESessionIndexAndLocalSessionID(esoeSessionIndex, sessionID);
		
		this.authnLogger.info("Establishing session for principal identified by ESOE SessionID " + esoeSessionID + " local session identifier set to " + sessionID + " identified by session index of " + esoeSessionIndex);

		try
		{
			this.attributeProcessor.doAttributeProcessing(principalSession);
		}
		catch (AttributeProcessingException e)
		{
			this.authnLogger.info("Terminated session for principal identified by ESOE SessionID " + esoeSessionID + " local session identifier set to " + sessionID + " due to problems with attribute resolution");
			this.logger.error(Messages.getString("AuthnProcessorImpl.22")); //$NON-NLS-1$
			return null;
		}

		this.sessionCache.putPrincipalSession(sessionID, principalSession);

		return sessionID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.spep.authn.AuthnProcessor#logoutPrincipal(com.qut.middleware.spep.authn.AuthnProcessorData)
	 */
	public void logoutPrincipal(AuthnProcessorData data) throws LogoutException
	{
		this.logger.debug(Messages.getString("AuthnProcessorImpl.23")); //$NON-NLS-1$
		LogoutRequest logoutRequest;
		String samlID = null;
		String message;

		try
		{
			logoutRequest = this.logoutRequestUnmarshaller.unMarshallSigned(data.getRequestDocument());
			samlID = logoutRequest.getID();
		}
		catch (SignatureValueException e)
		{
			this.logger.error(Messages.getString("AuthnProcessorImpl.25") + e.getMessage()); //$NON-NLS-1$

			message = Messages.getString("AuthnProcessorImpl.24"); //$NON-NLS-1$
			try
			{
				data.setResponseDocument(generateLogoutResponse(StatusCodeConstants.requester, message, samlID));
			}
			catch (MarshallerException e1)
			{
				this.logger.error(Messages.getString("AuthnProcessorImpl.78") + e.getMessage()); //$NON-NLS-1$
			}

			throw new LogoutException(message, e);
		}
		catch (ReferenceValueException e)
		{
			this.logger.error(Messages.getString("AuthnProcessorImpl.27") + e.getMessage()); //$NON-NLS-1$

			message = Messages.getString("AuthnProcessorImpl.26"); //$NON-NLS-1$
			try
			{
				data.setResponseDocument(generateLogoutResponse(StatusCodeConstants.requester, message, samlID));
			}
			catch (MarshallerException e1)
			{
				this.logger.error(Messages.getString("AuthnProcessorImpl.79") + e.getMessage()); //$NON-NLS-1$
			}
			throw new LogoutException(message, e);
		}
		catch (UnmarshallerException e)

		{
			this.logger.error(Messages.getString("AuthnProcessorImpl.29") + e.getMessage()); //$NON-NLS-1$

			message = Messages.getString("AuthnProcessorImpl.28"); //$NON-NLS-1$
			try
			{
				data.setResponseDocument(generateLogoutResponse(StatusCodeConstants.requester, message, samlID));
			}
			catch (MarshallerException e1)
			{
				this.logger.error("Unable to marshall LogoutResponse - " + e.getMessage()); //$NON-NLS-1$
			}

			throw new LogoutException(message, e);
		}

		this.logger.debug(MessageFormat.format(Messages.getString("AuthnProcessorImpl.30"), samlID)); //$NON-NLS-1$

		// Validate the LogoutRequest received by the ESOE
		try
		{
			this.samlValidator.getRequestValidator().validate(logoutRequest);
		}
		catch (InvalidSAMLRequestException e)
		{
			this.logger.warn(Messages.getString("AuthnProcessorImpl.57")); //$NON-NLS-1$
			throw new LogoutException(e.getLocalizedMessage(), e);
		}

		this.logger.debug(Messages.getString("AuthnProcessorImpl.80") + logoutRequest.getNameID().getValue()); //$NON-NLS-1$
		PrincipalSession principalSession = this.sessionCache.getPrincipalSessionByEsoeSessionID(logoutRequest.getNameID().getValue());
		try
		{
			if (principalSession != null)
			{
				this.logger.debug(Messages.getString("AuthnProcessorImpl.81")); //$NON-NLS-1$
				/* Perform actual Logout operation */
				if (logoutRequest.getSessionIndices() != null && logoutRequest.getSessionIndices().size() > 0)
				{
					/* The ESOE has advised only specific sessions should be terminated, loop though and terminate each */
					synchronized (logoutRequest.getSessionIndices())
					{
						for (String sessionIndex : logoutRequest.getSessionIndices())
						{
							this.logger.debug(Messages.getString("AuthnProcessorImpl.82") + sessionIndex); //$NON-NLS-1$
							this.sessionCache.terminateIndividualPrincipalSession(principalSession, sessionIndex);
						}
					}
				}
				else
				{
					this.authnLogger.info("Logging out session for principal identified by ESOE SessionID " + principalSession.getEsoeSessionID() + " and local session index " + principalSession.getEsoeSessionIndex());
					this.logger.debug(MessageFormat.format(Messages.getString("AuthnProcessorImpl.31"), principalSession.getEsoeSessionID())); //$NON-NLS-1$
					this.sessionCache.terminatePrincipalSession(principalSession);
				}
				data.setResponseDocument(generateLogoutResponse(StatusCodeConstants.success, Messages.getString("AuthnProcessorImpl.83"), samlID)); //$NON-NLS-1$
				return;
			}

			this.logger.debug(Messages.getString("AuthnProcessorImpl.84")); //$NON-NLS-1$
			data.setResponseDocument(generateLogoutResponse(StatusCodeConstants.unknownPrincipal, Messages.getString("AuthnProcessorImpl.85"), samlID)); //$NON-NLS-1$
			throw new LogoutException(Messages.getString("AuthnProcessorImpl.86")); //$NON-NLS-1$
		}
		catch (MarshallerException e)
		{
			this.logger.error(Messages.getString("AuthnProcessorImpl.33") + e.getMessage()); //$NON-NLS-1$
			throw new LogoutException(Messages.getString("AuthnProcessorImpl.33") + e.getMessage()); //$NON-NLS-1$
		}
	}

	/*
	 * Generate the Logout Response to be sent back to the ESOE.
	 * 
	 */
	private byte[] generateLogoutResponse(String statusCodeValue, String statusMessage, String inResponseTo) throws MarshallerException
	{
		this.logger.debug(MessageFormat.format(Messages.getString("AuthnProcessorImpl.42"), statusCodeValue, statusMessage)); //$NON-NLS-1$

		byte[] responseDocument = null;

		NameIDType issuer = new NameIDType();
		issuer.setValue(this.metadata.getSPEPIdentifier());

		Status status = new Status();
		StatusCode statusCode = new StatusCode();
		statusCode.setValue(statusCodeValue);
		status.setStatusCode(statusCode);
		status.setStatusMessage(statusMessage);

		StatusResponseType response = new StatusResponseType();
		response.setID(this.identifierGenerator.generateSAMLID());
		response.setInResponseTo(inResponseTo);
		response.setIssueInstant(CalendarUtils.generateXMLCalendar());
		response.setIssuer(issuer);
		response.setSignature(new Signature());
		response.setStatus(status);
		response.setVersion(VersionConstants.saml20);

		responseDocument = this.logoutResponseMarshaller.marshallSigned(new LogoutResponse(response));

		return responseDocument;
	}
}
