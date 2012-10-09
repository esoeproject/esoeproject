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

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.qut.middleware.crypto.KeystoreResolver;
import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.esoe.sessions.exception.SessionCacheUpdateException;
import com.qut.middleware.esoe.sso.SSOProcessor;
import com.qut.middleware.esoe.sso.bean.SSOProcessorData;
import com.qut.middleware.esoe.sso.exception.InvalidRequestException;
import com.qut.middleware.esoe.sso.exception.InvalidSessionIdentifierException;
import com.qut.middleware.esoe.util.CalendarUtils;
import com.qut.middleware.esoe.util.FingerPrint;
import com.qut.middleware.metadata.bean.EntityData;
import com.qut.middleware.metadata.bean.saml.ServiceProviderRole;
import com.qut.middleware.metadata.exception.MetadataStateException;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.saml2.*;
import com.qut.middleware.saml2.exception.*;
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
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3._2000._09.xmldsig_.Signature;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.*;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/** Implements logic to interact with SPEP using Web Browser SSO Post Profile of SAML 2.0 specification. */
public abstract class SSOProcessorBase implements SSOProcessor
{
	private Unmarshaller<AuthnRequest> unmarshaller;
	private Marshaller<Response> marshaller;

	private SAMLValidator samlValidator;
	private SessionsProcessor sessionsProcessor;
	protected MetadataProcessor metadata;
	protected IdentifierGenerator identifierGenerator;
	protected String esoeIdentifier;

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

	private final int TMP_BUFFER_SIZE = 4096;

	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(SSOProcessorBase.class.getName());
	private Logger authnLogger = LoggerFactory.getLogger(ConfigurationConstants.authnLogger);

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
	 * @param acceptUnsignedAuthnRequests
	 *            Give the ESOE the ability to accept non signed requests from SP's. May be required when allowing ESOE
	 *            to interact with external service providers such as Google Apps, generally not recommended.
	 * @param identifierAttributeMapping
	 *            A mapping of keys from NameIDConstants to attributes exposed by the Attribute Authority for resolution
	 *            of subject identifier specifiers in responses.
	 *
	 * @throws UnmarshallerException
	 *             if the unmarshaller cannot be created.
	 * @throws MarshallerException
	 *             if the marshaller cannot be created.
	 */
	public SSOProcessorBase(SAMLValidator samlValidator, SessionsProcessor sessionsProcessor, MetadataProcessor metadata, IdentifierGenerator identifierGenerator, ExternalKeyResolver extKeyResolver, KeystoreResolver keyStoreResolver, int allowedTimeSkew, int minimalTimeRemaining, boolean acceptUnsignedAuthnRequests, Map<String, String> identifierAttributeMapping, String esoeIdentifier) throws UnmarshallerException, MarshallerException
	{
		/* Ensure that a stable base is created when this Processor is setup */
		if (samlValidator == null)
		{
			this.logger.error(Messages.getString("SSOProcessor.0")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("SSOProcessor.0")); //$NON-NLS-1$
		}

		if (sessionsProcessor == null)
		{
			this.logger.error(Messages.getString("SSOProcessor.1")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("SSOProcessor.1")); //$NON-NLS-1$
		}

		if (metadata == null)
		{
			this.logger.error(Messages.getString("SSOProcessor.2")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("SSOProcessor.2")); //$NON-NLS-1$
		}

		if (identifierGenerator == null)
		{
			this.logger.error(Messages.getString("SSOProcessor.3")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("SSOProcessor.3")); //$NON-NLS-1$
		}

		if (extKeyResolver == null)
		{
			this.logger.error(Messages.getString("SSOProcessor.15")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("SSOProcessor.15")); //$NON-NLS-1$
		}

		if (keyStoreResolver == null)
		{
			this.logger.error(Messages.getString("SSOProcessor.25")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("SSOProcessor.25")); //$NON-NLS-1$
		}

		if (allowedTimeSkew > Integer.MAX_VALUE / 1000)
		{
			this.logger.error(Messages.getString("SSOProcessor.57")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("SSOProcessor.58")); //$NON-NLS-1$
		}

		if (minimalTimeRemaining > Integer.MAX_VALUE / 1000)
		{
			this.logger.error(Messages.getString("SSOProcessor.69")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("SSOProcessor.70")); //$NON-NLS-1$
		}

		if (identifierAttributeMapping == null)
		{
			this.logger.error("identifier attribute mapping was null");
			throw new IllegalArgumentException("identifier attribute mapping was null");
		}

		if (esoeIdentifier == null)
		{
			this.logger.error("ESOE identifier was null");
			throw new IllegalArgumentException("ESOE identifier was null");
		}

		this.samlValidator = samlValidator;
		this.sessionsProcessor = sessionsProcessor;
		this.metadata = metadata;
		this.identifierGenerator = identifierGenerator;
		this.esoeIdentifier = esoeIdentifier;

		String[] schemas = new String[] { SchemaConstants.samlProtocol };
		this.unmarshaller = new UnmarshallerImpl<AuthnRequest>(this.UNMAR_PKGNAMES, schemas, extKeyResolver);
		this.marshaller = new MarshallerImpl<Response>(this.MAR_PKGNAMES, schemas, keyStoreResolver);

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
			this.logger.debug(Messages.getString("SSOProcessor.17")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("SSOProcessor.17")); //$NON-NLS-1$
		}

		try
		{
			try
			{
				/* If this is a new SSO request then unmarshall / validate and set values accordingly */
				if (!data.isReturningRequest())
				{
					this.logger.trace(Messages.getString("SSOProcessor.30") + data.getRequestDocument()); //$NON-NLS-1$

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

					this.logger.debug(Messages.getString("SSOProcessor.31")); //$NON-NLS-1$

					data.setIssuerID(data.getAuthnRequest().getIssuer().getValue());

					EntityData issuerEntity = this.metadata.getEntityData(data.getIssuerID());
					if (issuerEntity == null)
					{
						this.logger.error("SSO processor could not resolve entity from metadata. Entity ID: " + data.getIssuerID());
						throw new InvalidRequestException("SSO processor could not resolve entity from metadata. Entity ID: " + data.getIssuerID());
					}

					ServiceProviderRole spRole = issuerEntity.getRoleData(ServiceProviderRole.class);
					if (spRole == null)
					{
						this.logger.error("SSO processor resolved entity but it was not marked as an SPEP by the metadata processor. Entity ID: " + data.getIssuerID());
						throw new InvalidRequestException("SSO processor resolved entity but it was not marked as an SPEP by the metadata processor. Entity ID: " + data.getIssuerID());
					}

					if (data.getAuthnRequest().getAssertionConsumerServiceIndex() != null)
					{
						data.setResponseEndpointID(data.getAuthnRequest().getAssertionConsumerServiceIndex().intValue());

						data.setResponseEndpoint(spRole.getAssertionConsumerServiceEndpoint(IMPLEMENTED_BINDING, data.getResponseEndpointID()));
						this.logger.debug("Retrieved endpoint from metadata");

						data.setValidIdentifiers(spRole.getNameIDFormatList());
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
					this.logger.debug(Messages.getString("SSOProcessor.32")); //$NON-NLS-1$
					this.samlValidator.getRequestValidator().validate(data.getAuthnRequest());

					this.logger.debug(Messages.getString("SSOProcessor.33")); //$NON-NLS-1$
					this.samlValidator.getAuthnRequestValidator().validate(data.getAuthnRequest());
				}

				if (data.getSessionID() == null || data.getSessionID().length() <= 0)
				{
					if (data.getAuthnRequest().getNameIDPolicy().isAllowCreate().booleanValue())
					{
						this.logger.info(Messages.getString("SSOProcessor.34")); //$NON-NLS-1$
						return SSOProcessor.result.ForceAuthn;
					}

					this.logger.debug(Messages.getString("SSOProcessor.35")); //$NON-NLS-1$
					createFailedAuthnResponse(data, StatusCodeConstants.requester, StatusCodeConstants.requestDenied, Messages.getString("SSOProcessor.19"), data.getRequestCharsetName()); //$NON-NLS-1$
					return SSOProcessor.result.ForcePassiveAuthn;
				}

                // Insert fingerprint check to eliminate the possibility of swapped user sessions
                HttpServletRequest userRequest = data.getHttpRequest();
                String userAgentData = userRequest.getRemoteAddr() + userRequest.getHeader("User-Agent") + userRequest.getHeader("Accept-Encoding");
                FingerPrint printChecker = new FingerPrint();

                try {
                    logger.info("{} - Checking fingerprint for session {}", userRequest.getRemoteAddr(), data.getSessionID());
                    if(!printChecker.assertFingerprintCheck(data.getSessionID(), userAgentData) ){

                        logger.warn("{} - Fingerprint check failed for session ID {}. Forcing Authn", userRequest.getRemoteAddr(), data.getSessionID());
                        return SSOProcessor.result.ForceAuthn;
                    } else {
                       logger.info("{} - Fingerprint check passed. Allowing request to proceed", userRequest.getRemoteAddr());
                    }
                } catch (SessionCacheUpdateException e) {
                    // allow user to login if the print can not be stored
                    logger.error("Failed to check fingerprint for session {} - {}", data.getSessionID(), e.getMessage());
                }

				principal = this.sessionsProcessor.getQuery().queryAuthnSession(data.getSessionID());

				if (principal == null || expiredPrincipal(principal))
				{
					if (data.getAuthnRequest().getNameIDPolicy().isAllowCreate().booleanValue())
					{
						this.logger.warn(Messages.getString("SSOProcessor.36")); //$NON-NLS-1$
						return SSOProcessor.result.ForceAuthn;
					}

					this.logger.warn(Messages.getString("SSOProcessor.37")); //$NON-NLS-1$
					createFailedAuthnResponse(data, StatusCodeConstants.responder, StatusCodeConstants.authnFailed, Messages.getString("SSOProcessor.38"), data.getRequestCharsetName()); //$NON-NLS-1$
					return SSOProcessor.result.ForcePassiveAuthn;
				}

				this.logger.debug(Messages.getString("SSOProcessor.39") + data.getSessionID() + Messages.getString("SSOProcessor.40") + principal.getPrincipalAuthnIdentifier()); //$NON-NLS-1$ //$NON-NLS-2$

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
						this.logger.debug(Messages.getString("SSOProcessor.41")); //$NON-NLS-1$
						createFailedAuthnResponse(data, StatusCodeConstants.responder, StatusCodeConstants.noPassive, Messages.getString("SSOProcessor.20"), data.getRequestCharsetName()); //$NON-NLS-1$
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
						this.logger.info(Messages.getString("SSOProcessor.42")); //$NON-NLS-1$
						return SSOProcessor.result.ForceAuthn;
					}
				}

				try 
				{			
					/* Determine if we have an identifier for this principal to use when communicating with remote SPEP's */
					if (principal.getSAMLAuthnIdentifier() == null || principal.getSAMLAuthnIdentifier().length() <= 0)
					{
						createFailedAuthnResponse(data, StatusCodeConstants.responder, StatusCodeConstants.authnFailed, Messages.getString("SSOProcessor.62"), data.getRequestCharsetName()); //$NON-NLS-1$
						this.sessionsProcessor.getTerminate().terminateSession(data.getSessionID());
						throw new InvalidSessionIdentifierException(Messages.getString("SSOProcessor.63")); //$NON-NLS-1$
					}
	
					/* Generate SAML session index */
					sessionIndex = this.identifierGenerator.generateSAMLSessionID();
				
					/* Determine if the principal has previously had a record of communicating to this SPEP in this session */
					if (principal.getActiveEntityList() != null && !principal.getActiveEntityList().contains(data.getIssuerID()))
					{					
						this.authnLogger.info(MessageFormat.format("Adding new entity session index {0} for entity {1} - Principal {2}.", sessionIndex, data.getIssuerID(), principal.getPrincipalAuthnIdentifier()) );
						this.logger.debug(MessageFormat.format("Adding new entity session index {0} for entity {1} - Principal {2}.", sessionIndex, data.getIssuerID(), principal.getPrincipalAuthnIdentifier()) );
						
						this.sessionsProcessor.getUpdate().addEntitySessionIndex(principal, data.getIssuerID(), sessionIndex);
					}
					
					/* Update our principal to get any changes */
					principal = this.sessionsProcessor.getQuery().queryAuthnSession(data.getSessionID());
	
					createSucessfulAuthnResponse(data, principal, sessionIndex, data.getRequestCharsetName());
	
					/* Set the value of the common domain cookie */
					byte[] b64EncodedCDC = Base64.encodeBase64(this.esoeIdentifier.getBytes());
					String uriEncodedCDC = URLEncoder.encode(new String(b64EncodedCDC), this.UTF8);
					data.setCommonCookieValue(uriEncodedCDC);
	
					return SSOProcessor.result.SSOGenerationSuccessful;
				
				}
				catch (SessionCacheUpdateException e)
				{
					this.logger.error("Failed to generate SSO session for {}. Session cache update failure.", principal.getPrincipalAuthnIdentifier());
					return SSOProcessor.result.SSOGenerationFailed;
				}
			
			}
			catch (InvalidSAMLRequestException isre)
			{
				this.logger.warn(Messages.getString("SSOProcessor.5") + VersionConstants.saml20 + Messages.getString(Messages.getString("SSOProcessor.50"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-				
				createFailedAuthnResponse(data, StatusCodeConstants.requester, StatusCodeConstants.authnFailed, Messages.getString("SSOProcessor.21"), data.getRequestCharsetName()); //$NON-NLS-1$
				throw new InvalidRequestException(Messages.getString("SSOProcessor.5") + VersionConstants.saml20 + Messages.getString("SSOProcessor.6"), isre); //$NON-NLS-1$ //$NON-NLS-2$
			}
			catch (UnsupportedEncodingException e)
			{
				this.logger.warn("Unable to process common domain cookie, encoding invalid");
				createFailedAuthnResponse(data, StatusCodeConstants.responder, StatusCodeConstants.authnFailed, "unable to generate common domain cookie", data.getRequestCharsetName()); //$NON-NLS-1$
				throw new InvalidRequestException("Unable to process common domain cookie, encoding invalid", e);
			}
		}
		catch (SignatureValueException sve)
		{
			this.logger.error(Messages.getString("SSOProcessor.8")); //$NON-NLS-1$
			throw new InvalidRequestException(Messages.getString("SSOProcessor.8"), sve); //$NON-NLS-1$
		}
		catch (ReferenceValueException rve)
		{
			this.logger.error(Messages.getString("SSOProcessor.11")); //$NON-NLS-1$
			throw new InvalidRequestException(Messages.getString("SSOProcessor.11"), rve); //$NON-NLS-1$
		}
		catch (UnmarshallerException ue)
		{
			this.logger.error(Messages.getString("SSOProcessor.12")); //$NON-NLS-1$
			throw new InvalidRequestException(Messages.getString("SSOProcessor.12"), ue); //$NON-NLS-1$
		}
		catch (MetadataStateException e)
		{
			this.logger.error("Invalid metadata state. Exception was: " + e.getMessage());
			this.logger.debug("Invalid metadata state.", e);
			throw new InvalidRequestException("Invalid metadata state.", e);
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

		/*
		 * We deliberately setup UTF-8 here to ensure that misdiagnosed encodings of types such as ISO-8859 don't cause
		 * us greif
		 */
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
		ByteArrayInputStream decodedByteStream = null;
		InflaterInputStream inflaterStream = null;
		ByteArrayOutputStream inflatedByteStream = null;
		byte[] chunk = new byte[this.TMP_BUFFER_SIZE];
		byte[] decodedBytes;
		int writeCount = 0;
		int count = 0;

		try
		{
			if (data.getRequestDocument() == null || data.getRequestDocument().length <= 0)
			{
				createFailedAuthnResponse(data, StatusCodeConstants.responder, StatusCodeConstants.authnFailed, "Request is invalid, no content supplied", this.UTF8);
				throw new InvalidRequestException("Dataformat for Request is invalid");
			}

			/* Ensure the caller used Deflate encoding, we don't support other methods */
			if (data.getSamlEncoding() != null && !data.getSamlEncoding().equals(BindingConstants.deflateEncoding))
			{
				createFailedAuthnResponse(data, StatusCodeConstants.responder, StatusCodeConstants.authnFailed, "SAML Request encoding is unsupported", this.UTF8);
				throw new InvalidRequestException("SAML Request encoding is unsupported");
			}

			/* Ensure there was no signature presented for now */
			if (data.getSignature() != null)
			{
				createFailedAuthnResponse(data, StatusCodeConstants.responder, StatusCodeConstants.authnFailed, "Cryptography in HTTP Request profile is not yet supported", this.UTF8);
				throw new InvalidRequestException("Cryptography in HTTP Request profile is not yet supported");
			}

			/*
			 * Retrieves the AuthnRequest from the encoded and compressed String extracted from the request of SAML HTTP
			 * Redirect. The AuthnRequest XML is retrieved in the following order: 1. Base64 decode, 2. Inflate
			 */
			decodedBytes = Base64.decodeBase64(data.getRequestDocument());
			decodedByteStream = new ByteArrayInputStream(decodedBytes);
			inflaterStream = new InflaterInputStream(decodedByteStream, new Inflater(true));
			inflatedByteStream = new ByteArrayOutputStream();

			while ((count = inflaterStream.read(chunk)) >= 0)
			{
				inflatedByteStream.write(chunk, 0, count);
				writeCount = writeCount + count;
			}

			/*
			 * inflater.setInput(deflatedBytes);
			 * 
			 * try { writeCount = inflater.inflate(tmpBuffer); inflatedByteStream.write(tmpBuffer); /* Make sure we get
			 * the entire request content * while (!inflater.finished()) { tmpBuffer = new byte[this.TMP_BUFFER_SIZE];
			 * writeCount = inflater.inflate(tmpBuffer); inflatedByteStream.write(tmpBuffer); } } catch
			 * (DataFormatException e) { createFailedAuthnResponse(data, StatusCodeConstants.responder,
			 * StatusCodeConstants.authnFailed, "Dataformat for Request is invalid", this.UTF8); throw new
			 * InvalidRequestException("Dataformat for Request is invalid"); } catch (IOException e) {
			 * createFailedAuthnResponse(data, StatusCodeConstants.responder, StatusCodeConstants.authnFailed, "Server
			 * error when inflating request", this.UTF8); throw new InvalidRequestException("Server error when inflating
			 * request"); } finally { inflater.reset(); inflater.end(); tmpBuffer = null; }
			 */

			/* We need to run through and strip out padded nulls before passing to unmarshaller */
			/*
			byte[] paddedRequestDocument = inflatedByteStream.toByteArray();
			byte[] samlRequestDocument = new byte[writeCount];

			for (int i = 0; i < writeCount; i++)
				samlRequestDocument[i] = paddedRequestDocument[i];
			*/
			
			byte[] samlRequestDocument = inflatedByteStream.toByteArray();
			
			data.setRequestCharsetName(this.detectCharSet(samlRequestDocument));
			return this.unmarshaller.unMarshallUnSigned(samlRequestDocument);
		}
		catch (IOException e)
		{
			this.logger.error("Unable to decode SAML get request - " + e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new InvalidRequestException("Unable to decode get request correctly");
		}
		finally
		{
			try
			{
				if (inflatedByteStream != null)
				{
					inflatedByteStream.reset();
					inflatedByteStream.close();
				}
				
				if(decodedByteStream != null)
					decodedByteStream.close();
				
				if(inflaterStream != null)
					inflaterStream.close();
			}
			catch (IOException e)
			{
				this.logger.error("Unable to close stream correctly - " + e.getLocalizedMessage());
				this.logger.debug(e.getLocalizedMessage(), e);
			}
		}
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
		this.logger.debug(MessageFormat.format(Messages.getString("SSOProcessor.64"), this.minimalTimeRemaining)); //$NON-NLS-1$

		/* current time is current UTC time, not localized time */
		SimpleTimeZone utc = new SimpleTimeZone(0, ConfigurationConstants.timeZone);
		Calendar thisCalendar = new GregorianCalendar(utc);
		long currentTime = thisCalendar.getTimeInMillis();

		long expiryTime = principal.getSessionNotOnOrAfter();
		
		this.logger.debug(Messages.getString("SSOProcessor.65") + principal.getPrincipalAuthnIdentifier()+ Messages.getString("SSOProcessor.66") + principal.getSessionNotOnOrAfter()); //$NON-NLS-1$ //$NON-NLS-2$

		/* If the principal session has already expired don't use it further */
		if (expiryTime < currentTime)
		{
			this.logger.debug(Messages.getString("SSOProcessor.67")); //$NON-NLS-1$
			return true;
		}

		if (expiryTime - currentTime < this.minimalTimeRemaining)
		{
			this.logger.debug(Messages.getString("SSOProcessor.68")); //$NON-NLS-1$
			return true;
		}

		this.logger.debug(Messages.getString("SSOProcessor.71") + expiryTime + Messages.getString("SSOProcessor.72") + currentTime + Messages.getString("SSOProcessor.73")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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

		this.logger.info(Messages.getString("SSOProcessor.51") + failStatus + Messages.getString("SSOProcessor.52") + detailedFailStatus + Messages.getString("SSOProcessor.53") + failStatusMessage); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

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

		marshallResponse(data, response, charset);
	}

	/**
	 * Creates a SAML response document for successfully verified requests and stores it in the samlResponseDocument
	 * attribute of the SSOProcessorData parameter.
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
			this.logger.trace(Messages.getString("SSOProcessor.56") + data.getResponseDocument()); //$NON-NLS-1$
		}
		catch (MarshallerException me)
		{
			throw new InvalidRequestException(Messages.getString("SSOProcessor.14"), me); //$NON-NLS-1$
		}
	}
}
