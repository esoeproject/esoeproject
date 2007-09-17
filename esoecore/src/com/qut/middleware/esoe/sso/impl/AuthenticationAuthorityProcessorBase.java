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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.w3._2000._09.xmldsig_.Signature;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.crypto.KeyStoreResolver;
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
import com.qut.middleware.saml2.BindingConstants;
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
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.protocol.AuthnRequest;
import com.qut.middleware.saml2.schemas.protocol.Response;
import com.qut.middleware.saml2.schemas.protocol.Status;
import com.qut.middleware.saml2.schemas.protocol.StatusCode;
import com.qut.middleware.saml2.validator.SAMLValidator;

/** Implements logic to interact with SPEP using Web Browser SSO Post Profile of SAML 2.0 specification. */
public abstract class AuthenticationAuthorityProcessorBase implements SSOProcessor
{
	private Unmarshaller<AuthnRequest> unmarshaller;
	private Marshaller<Response> marshaller;

	private SAMLValidator samlValidator;
	private SessionsProcessor sessionsProcessor;
	protected Metadata metadata;
	protected IdentifierGenerator identifierGenerator;

	protected int allowedTimeSkew;
	private int minimalTimeRemaining;
	private boolean acceptUnsignedRequests = false;
	
	protected Map<String, String> identifierAttributeMapping;

	private final String UNMAR_PKGNAMES = AuthnRequest.class.getPackage().getName();
	private final String MAR_PKGNAMES = Response.class.getPackage().getName();
	
	private final String UTF8 = "UTF-8";
	private final String UTF16 = "UTF-16";
	private final String UTF16LE = "UTF-16LE";
	private final String UTF16BE = "UTF-16BE";

	private final int TMP_BUFFER_SIZE = 1024;

	/* Local logging instance */
	private Logger logger = Logger.getLogger(AuthenticationAuthorityProcessorBase.class.getName());
	private Logger authnLogger = Logger.getLogger(ConfigurationConstants.authnLogger);

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
	 * @param acceptUnsignedAuthnRequests
	 *            Give the ESOE the ability to accept non signed requests from SP's. May be required when allowing ESOE
	 *            to interact with external service providers such as Google Apps, generally not recommended.
	 * @param identifierAttributeMapping A mapping of keys from NameIDConstants to attributes exposed by the Attribute Authority for resolution of subject identifier specifiers in responses.
	 * 
	 * @throws UnmarshallerException
	 *             if the unmarshaller cannot be created.
	 * @throws MarshallerException
	 *             if the marshaller cannot be created.
	 */
	public AuthenticationAuthorityProcessorBase(SAMLValidator samlValidator, SessionsProcessor sessionsProcessor, Metadata metadata, IdentifierGenerator identifierGenerator, ExternalKeyResolver extKeyResolver, KeyStoreResolver keyStoreResolver, 
			int allowedTimeSkew, int minimalTimeRemaining, boolean acceptUnsignedAuthnRequests, Map<String, String> identifierAttributeMapping) throws UnmarshallerException, MarshallerException
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

		if (allowedTimeSkew > Integer.MAX_VALUE / 1000)
		{
			this.logger.fatal(Messages.getString("AuthenticationAuthorityProcessor.57")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("AuthenticationAuthorityProcessor.58")); //$NON-NLS-1$
		}

		if (minimalTimeRemaining > Integer.MAX_VALUE / 1000)
		{
			this.logger.fatal(Messages.getString("AuthenticationAuthorityProcessor.69")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("AuthenticationAuthorityProcessor.70")); //$NON-NLS-1$
		}
		
		if(identifierAttributeMapping == null)
		{
			this.logger.fatal("identifier attribute mapping was null");
			throw new IllegalArgumentException("identifier attribute mapping was null");
		}

		this.samlValidator = samlValidator;
		this.sessionsProcessor = sessionsProcessor;
		this.metadata = metadata;
		this.identifierGenerator = identifierGenerator;

		String[] schemas = new String[] { ConfigurationConstants.samlProtocol };
		this.unmarshaller = new UnmarshallerImpl<AuthnRequest>(this.UNMAR_PKGNAMES, schemas, extKeyResolver);
		this.marshaller = new MarshallerImpl<Response>(this.MAR_PKGNAMES, schemas, keyStoreResolver.getKeyAlias(), keyStoreResolver.getPrivateKey());

		this.allowedTimeSkew = allowedTimeSkew * 1000;
		this.minimalTimeRemaining = minimalTimeRemaining * 1000;
		this.acceptUnsignedRequests = acceptUnsignedAuthnRequests;
		this.identifierAttributeMapping = identifierAttributeMapping;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sso.SSOProcessor#execute(com.qut.middleware.esoe.sso.bean.SSOProcessorData)
	 */
	public result execute(SSOProcessorData data) throws InvalidSessionIdentifierException, InvalidRequestException
	{
		Principal principal;
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
					this.logger.trace(Messages.getString("AuthenticationAuthorityProcessor.30") + data.getRequestDocument()); //$NON-NLS-1$

					/*
					 * Processor currently supports HTTP Redirect and HTTP Post profiles of SAML2 profile specification,
					 * further extensions to support HTTP Artifact may be made here at a later time. Invoke logic to
					 * extract the AuthnRequest and verify the incoming state of each profile.
					 */
					if (data.getSamlBinding().equals(BindingConstants.httpPost))
					{
						data.setAuthnRequest(this.executePostBinding(data));
					}
					else
					{
						if (data.getSamlBinding().equals(BindingConstants.httpRedirect))
						{
							data.setAuthnRequest(this.executeRedirectBinding(data));
						}
						else
						{
							createFailedAuthnResponse(data, StatusCodeConstants.responder, StatusCodeConstants.authnFailed, "SAML Request profile undetermined", data.getRequestCharsetName());
							throw new InvalidRequestException("SAML Request profile undetermined");
						}
					}

					this.logger.debug(Messages.getString("AuthenticationAuthorityProcessor.31")); //$NON-NLS-1$
					data.setIssuerID(data.getAuthnRequest().getIssuer().getValue());
					if (data.getAuthnRequest().getAssertionConsumerServiceIndex() != null)
					{
						data.setResponseEndpointID(data.getAuthnRequest().getAssertionConsumerServiceIndex().intValue());

						data.setResponseEndpoint(this.metadata.resolveAssertionConsumerService(data.getIssuerID(), data.getResponseEndpointID()));
						this.logger.debug("Retrieved endpoint from metadata");
						
						data.setValidIdentifiers( this.metadata.resolveAssertionConsumerServiceIdentifierTypes(data.getIssuerID(), data.getResponseEndpointID()) );
					}
					else
					{
						if (data.getAuthnRequest().getAssertionConsumerServiceURL() != null)
						{
							data.setResponseEndpoint(data.getAuthnRequest().getAssertionConsumerServiceURL());
							this.logger.debug("Retrieved endpoint directly from request");
							
							List<String> validIdentifiers = new ArrayList<String>();
							validIdentifiers.add(data.getAuthnRequest().getNameIDPolicy().getFormat());
							data.setValidIdentifiers(validIdentifiers);
						}
						else
						{
							this.logger.debug("Failed to retrieve response endpoint either from metadata or directly from request");
							throw new InvalidRequestException("Failed to retrieve response endpoint either from metadata or directly from request");
						}
					}

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
					createFailedAuthnResponse(data, StatusCodeConstants.requester, StatusCodeConstants.requestDenied, Messages.getString("AuthenticationAuthorityProcessor.19"), data.getRequestCharsetName()); //$NON-NLS-1$
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
					createFailedAuthnResponse(data, StatusCodeConstants.responder, StatusCodeConstants.authnFailed, Messages.getString("AuthenticationAuthorityProcessor.38"), data.getRequestCharsetName()); //$NON-NLS-1$
					return SSOProcessor.result.ForcePassiveAuthn;
				}

				this.logger.debug(Messages.getString("AuthenticationAuthorityProcessor.39") + data.getSessionID() + Messages.getString("AuthenticationAuthorityProcessor.40") + principal.getPrincipalAuthnIdentifier()); //$NON-NLS-1$ //$NON-NLS-2$

				if (data.getAuthnRequest().isForceAuthn() != null && data.getAuthnRequest().isForceAuthn().booleanValue())
				{
					TimeZone utc = new SimpleTimeZone(0, ConfigurationConstants.timeZone);
					GregorianCalendar cal = new GregorianCalendar(utc);
					long thisTime = cal.getTimeInMillis();

					/*
					 * The SP indicated it expects the interaction to be passive and it wishes to ensure the principal
					 * undertakes authn, we can't grant this so return error
					 * 
					 */
					if (data.getAuthnRequest().isIsPassive().booleanValue() && (thisTime - principal.getAuthnTimestamp() > this.allowedTimeSkew))
					{
						this.logger.debug(Messages.getString("AuthenticationAuthorityProcessor.41")); //$NON-NLS-1$
						createFailedAuthnResponse(data, StatusCodeConstants.responder, StatusCodeConstants.noPassive, Messages.getString("AuthenticationAuthorityProcessor.20"), data.getRequestCharsetName()); //$NON-NLS-1$
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
					createFailedAuthnResponse(data, StatusCodeConstants.responder, StatusCodeConstants.authnFailed, Messages.getString("AuthenticationAuthorityProcessor.62"), data.getRequestCharsetName()); //$NON-NLS-1$
					this.sessionsProcessor.getTerminate().terminateSession(data.getSessionID());
					throw new InvalidSessionIdentifierException(Messages.getString("AuthenticationAuthorityProcessor.63")); //$NON-NLS-1$
				}

				/* Determine if the principal has previously had a record of communicating to this SPEP in this session */
				if (principal.getActiveDescriptors() != null && !principal.getActiveDescriptors().contains(data.getIssuerID()))
				{
					this.authnLogger.info( Messages.getString("AuthenticationAuthorityProcessor.45") + principal.getPrincipalAuthnIdentifier() + " at SPEP " + data.getIssuerID() + Messages.getString("AuthenticationAuthorityProcessor.46") + data.getResponseEndpoint()); //$NON-NLS-1$ //$NON-NLS-2$
					this.sessionsProcessor.getUpdate().updateDescriptorList(data.getSessionID(), data.getIssuerID());
				}

				/* Generate SAML session index */
				sessionIndex = this.identifierGenerator.generateSAMLSessionID();
				this.logger.debug("DescriptorID  " + data.getIssuerID() + " -- " + sessionIndex + " === " + principal);

				principal.addDescriptorSessionIdentifier(data.getIssuerID(), sessionIndex);

				this.authnLogger.info(Messages.getString("AuthenticationAuthorityProcessor.47") + sessionIndex + Messages.getString("AuthenticationAuthorityProcessor.48") + data.getIssuerID() + Messages.getString("AuthenticationAuthorityProcessor.49") + principal.getPrincipalAuthnIdentifier()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				this.sessionsProcessor.getUpdate().updateDescriptorSessionIdentifierList(data.getSessionID(), data.getIssuerID(), sessionIndex);

				/* Update our principal to get all the changes */
				principal = this.sessionsProcessor.getQuery().queryAuthnSession(data.getSessionID());

				createSucessfulAuthnResponse(data, principal, sessionIndex, data.getRequestCharsetName());
				
				/* Set the value of the common domain cookie */
				byte[] b64EncodedCDC = Base64.encodeBase64(this.metadata.getEsoeEntityID().getBytes());
				String uriEncodedCDC = URLEncoder.encode(new String(b64EncodedCDC), this.UTF8);
				data.setCommonCookieValue(uriEncodedCDC);

				return SSOProcessor.result.SSOGenerationSuccessful;
			}
			catch (InvalidSAMLRequestException isre)
			{
				this.logger.warn(Messages.getString("AuthenticationAuthorityProcessor.5") + VersionConstants.saml20 + Messages.getString(Messages.getString("AuthenticationAuthorityProcessor.50"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-				
				createFailedAuthnResponse(data, StatusCodeConstants.requester, StatusCodeConstants.authnFailed, Messages.getString("AuthenticationAuthorityProcessor.21"), data.getRequestCharsetName()); //$NON-NLS-1$
				throw new InvalidRequestException(Messages.getString("AuthenticationAuthorityProcessor.5") + VersionConstants.saml20 + Messages.getString("AuthenticationAuthorityProcessor.6"), isre); //$NON-NLS-1$ //$NON-NLS-2$
			}
			catch (com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException isie)
			{
				this.logger.warn(Messages.getString("AuthenticationAuthorityProcessor.7")); //$NON-NLS-1$
				if (data.getAuthnRequest().getNameIDPolicy().isAllowCreate().booleanValue())
				{
					this.logger.warn(Messages.getString("AuthenticationAuthorityProcessor.61")); //$NON-NLS-1$
					return SSOProcessor.result.ForceAuthn;
				}

				createFailedAuthnResponse(data, StatusCodeConstants.responder, StatusCodeConstants.unknownPrincipal, Messages.getString("AuthenticationAuthorityProcessor.22"), data.getRequestCharsetName()); //$NON-NLS-1$

				throw new InvalidSessionIdentifierException(Messages.getString("AuthenticationAuthorityProcessor.7"), isie); //$NON-NLS-1$
			}
			catch (InvalidDescriptorIdentifierException ieie)
			{
				this.logger.warn(Messages.getString("AuthenticationAuthorityProcessor.10")); //$NON-NLS-1$
				createFailedAuthnResponse(data, StatusCodeConstants.responder, StatusCodeConstants.unknownPrincipal, Messages.getString("AuthenticationAuthorityProcessor.24"), data.getRequestCharsetName()); //$NON-NLS-1$
				throw new InvalidRequestException(Messages.getString("AuthenticationAuthorityProcessor.10"), ieie); //$NON-NLS-1$
			}
			catch (UnsupportedEncodingException e)
			{
				this.logger.warn("Unable to process common domain cookie, encoding invalid");
				createFailedAuthnResponse(data, StatusCodeConstants.responder, StatusCodeConstants.authnFailed, "unable to generate common domain cookie", data.getRequestCharsetName()); //$NON-NLS-1$
				throw new InvalidRequestException("Unable to process common domain cookie, encoding invalid", e);
			}
		}
		catch (InvalidMetadataEndpointException imee)
		{
			this.logger.error(Messages.getString("AuthenticationAuthorityProcessor.13") + data.getIssuerID()); //$NON-NLS-1$
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

	private String detectCharSet(byte[] document)
	{
		CharsetDetector charsetDetector;
		CharsetMatch charsetMatch;

		/* Make best effort determination of incoming document type */
		charsetDetector = new CharsetDetector();
		charsetDetector.setText(document);
		charsetMatch = charsetDetector.detect();

		/* FOR UTF-16 don't set endianess */
		if (charsetMatch.getName().equalsIgnoreCase(this.UTF16BE) || charsetMatch.getName().equalsIgnoreCase(this.UTF16LE))
		{
			this.logger.info("Detected incoming AuthnRequest encoding of " + this.UTF16 + " will respond in same encoding");
			return this.UTF16;
		}

		/* We deliberately setup UTF-8 here to ensure that misdiagnosed encodings of types such as ISO-8859 don't cause us greif */
		this.logger.info("Detected incoming AuthnRequest encoding of " + charsetMatch.getName() + " will respond in UTF-8");
		return this.UTF8;
	}

	/**
	 * Implements specific logic for creating an AuthnRequest from the SAML HTTP Post binding. Requires that document be
	 * signed or crypto exceptions are thrown
	 * 
	 * @param data
	 *            The populated SSOProcessorData object
	 * @return A valid AuthnRequest sent by requestor
	 * @throws SignatureValueException
	 * @throws ReferenceValueException
	 * @throws UnmarshallerException
	 */
	private AuthnRequest executePostBinding(SSOProcessorData data) throws SignatureValueException, ReferenceValueException, UnmarshallerException
	{
		byte[] samlRequestBytes = Base64.decodeBase64(data.getRequestDocument()); //$NON-NLS-1$
		data.setRequestCharsetName(detectCharSet(samlRequestBytes));
		return this.unmarshaller.unMarshallSigned(samlRequestBytes);
	}

	/**
	 * Implements specific logic for creating AuthnRequest from SAML HTTP Redirect binding. Supports only the DEFLATE
	 * url encoding, other encodings cause error response. Does not currently support validation of signed requests, all
	 * requests containing a signature element result in error state due to inability to verify currently.
	 * 
	 * @param data
	 *            The populated SSOProcessorData object
	 * @return A valid AuthnRequest sent by requestor
	 * @throws UnmarshallerException
	 * @throws InvalidRequestException
	 */
	private AuthnRequest executeRedirectBinding(SSOProcessorData data) throws UnmarshallerException, InvalidRequestException
	{
		Inflater inflater = new Inflater(true);
		ByteArrayOutputStream inflatedByteStream = new ByteArrayOutputStream();
		byte[] tmpBuffer = new byte[this.TMP_BUFFER_SIZE];
		int writeCount = 0;

		/* Ensure the caller used Deflate encoding, we don't support other methods */
		if (data.getSamlEncoding() != null && !data.getSamlEncoding().equals(BindingConstants.deflateEncoding))
		{
			createFailedAuthnResponse(data, StatusCodeConstants.responder, StatusCodeConstants.authnFailed, "SAML Request encoding is unsupported", data.getRequestCharsetName());
			throw new InvalidRequestException("SAML Request encoding is unsupported");
		}

		/* Ensure there was no signature presented if there was create */
		if (data.getSignature() != null)
		{
			createFailedAuthnResponse(data, StatusCodeConstants.responder, StatusCodeConstants.authnFailed, "Cryptography in HTTP Request profile is not yet supported", data.getRequestCharsetName());
			throw new InvalidRequestException("Cryptography in HTTP Request profile is not yet supported");
		}

		/*
		 * Retrieves the AuthnRequest from the encoded and compressed String extracted from the request of SAML HTTP
		 * Redirect. The AuthnRequest XML is retrieved in the following order: 1. Base64 decode, 2. Inflate
		 */

		byte[] deflatedBytes = Base64.decodeBase64(data.getRequestDocument());
		inflater.setInput(deflatedBytes);

		try
		{
			writeCount = inflater.inflate(tmpBuffer);
			inflatedByteStream.write(tmpBuffer);

			/* Make sure we get the entire request content */
			while (!inflater.finished())
			{
				tmpBuffer = new byte[this.TMP_BUFFER_SIZE];
				writeCount = inflater.inflate(tmpBuffer);
				inflatedByteStream.write(tmpBuffer);
			}
		}
		catch (DataFormatException e)
		{
			createFailedAuthnResponse(data, StatusCodeConstants.responder, StatusCodeConstants.authnFailed, "Dataformat for Request is invalid", data.getRequestCharsetName());
			throw new InvalidRequestException("Dataformat for Request is invalid");
		}
		catch (IOException e)
		{
			createFailedAuthnResponse(data, StatusCodeConstants.responder, StatusCodeConstants.authnFailed, "Server error when inflating request", data.getRequestCharsetName());
			throw new InvalidRequestException("Server error when inflating request");
		}

		inflater.end();
		
		/* We need to run through and strip out padded nulls before passing to unmarshaller */
		byte[] paddedRequestDocument = inflatedByteStream.toByteArray();
		byte[] samlRequestDocument = new byte[writeCount];
		
		for(int i = 0; i < writeCount; i++)
			samlRequestDocument[i] = paddedRequestDocument[i];
		
		data.setRequestCharsetName(this.detectCharSet(samlRequestDocument));
		return this.unmarshaller.unMarshallUnSigned(samlRequestDocument);
	}

	/**
	 * Determines if a principal session has either expired or has enough time remaining to create new sessions on
	 * remote SPEP. Note the principal object even if timed out is not removed here, this is still the duty of the
	 * cleaning thread
	 * 
	 * @param principal
	 *            CUrrent principal object
	 * @return True if the principal session has expired, false if its still able to be used in session creation
	 */
	private boolean expiredPrincipal(Principal principal)
	{
		this.logger.debug(MessageFormat.format(Messages.getString("AuthenticationAuthorityProcessor.64"), this.minimalTimeRemaining)); //$NON-NLS-1$

		/* current time is current UTC time, not localized time */
		SimpleTimeZone utc = new SimpleTimeZone(0, ConfigurationConstants.timeZone);
		Calendar thisCalendar = new GregorianCalendar(utc);
		long currentTime = thisCalendar.getTimeInMillis();

		GregorianCalendar principalExpiry = principal.getSessionNotOnOrAfter().toGregorianCalendar();
		long expiryTime = principalExpiry.getTimeInMillis();

		this.logger.debug(Messages.getString("AuthenticationAuthorityProcessor.65") + principal.getSAMLAuthnIdentifier() + Messages.getString("AuthenticationAuthorityProcessor.66") + principal.getSessionNotOnOrAfter()); //$NON-NLS-1$ //$NON-NLS-2$

		/* If the principal session has already expired don't use it further */
		if (expiryTime < currentTime)
		{
			this.logger.debug(Messages.getString("AuthenticationAuthorityProcessor.67")); //$NON-NLS-1$
			return true;
		}

		if (expiryTime - currentTime < this.minimalTimeRemaining)
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
	 * @param charset
	 *            The IANA registered name of the charset to encode the response in
	 * @throws InvalidRequestException
	 */
	private void createFailedAuthnResponse(SSOProcessorData data, String failStatus, String detailedFailStatus, String failStatusMessage, String charset) throws InvalidRequestException
	{

		this.logger.info(Messages.getString("AuthenticationAuthorityProcessor.51") + failStatus + Messages.getString("AuthenticationAuthorityProcessor.52") + detailedFailStatus + Messages.getString("AuthenticationAuthorityProcessor.53") + failStatusMessage); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

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
		issuer.setValue(this.metadata.getEsoeEntityID());

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

		marshallResponse(data, response, charset);
	}

	/**
	 * Creates a SAML response document for successfully verified requests and stores it in the samlResponseDocument
	 * attribute of the response. Implements the SAML 2.0 HTTP profile for generated Response.
	 * 
	 * @param data
	 *            Populated SSOProcessor data bean. The marshalled Response string will be stored in this object.
	 * @param authnRequest
	 *            The current AuthnRequest.
	 * @param Principal
	 *            The current principal involved in the AuthnRequest.
	 * @param sessionIndex
	 *            The value for sessionIndex that was created for the successfully verified AuthnRequest
	 * @param charset
	 *            The IANA registered name of the charset to encode the response in
	 * @throws InvalidRequestException
	 */
	protected abstract void createSucessfulAuthnResponse(SSOProcessorData data, Principal principal, String sessionIndex, String charset) throws InvalidRequestException;

	/**
	 * Uses SAML2lib-j to marshall a response object to string and store in samlResponseDocument of supplied data bean.
	 * 
	 * @param data
	 *            Populated SSOProcessor data bean
	 * @param response
	 *            A fully populated SAML response object for the response type to be returned to the SPEP
	 * @param charset
	 *            The IANA registered name of the charset to encode the response in
	 * @throws InvalidRequestException
	 */
	protected void marshallResponse(SSOProcessorData data, Response response, String charset) throws InvalidRequestException
	{
		byte[] responseDocument;
		try
		{
			responseDocument = this.marshaller.marshallSigned(response, charset);
			data.setResponseDocument(responseDocument);
			this.logger.trace(Messages.getString("AuthenticationAuthorityProcessor.56") + data.getResponseDocument()); //$NON-NLS-1$
		}
		catch (MarshallerException me)
		{
			throw new InvalidRequestException(Messages.getString("AuthenticationAuthorityProcessor.14"), me); //$NON-NLS-1$
		}
	}
}
