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
 * Author:
 * Creation Date:
 * 
 * Purpose:
 */
package com.qut.middleware.esoe.spep.impl;

import java.text.MessageFormat;

import org.apache.log4j.Logger;
import org.w3._2000._09.xmldsig_.Signature;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.crypto.KeyStoreResolver;
import com.qut.middleware.esoe.metadata.Metadata;
import com.qut.middleware.esoe.pdp.cache.PolicyCacheProcessor;
import com.qut.middleware.esoe.pdp.cache.PolicyCacheProcessor.result;
import com.qut.middleware.esoe.spep.Messages;
import com.qut.middleware.esoe.spep.SPEPRegistrationCache;
import com.qut.middleware.esoe.spep.Startup;
import com.qut.middleware.esoe.spep.bean.SPEPProcessorData;
import com.qut.middleware.esoe.spep.exception.DatabaseFailureException;
import com.qut.middleware.esoe.spep.exception.DatabaseFailureNoSuchSPEPException;
import com.qut.middleware.esoe.spep.exception.InvalidRequestException;
import com.qut.middleware.esoe.spep.exception.SPEPCacheUpdateException;
import com.qut.middleware.esoe.util.CalendarUtils;
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
import com.qut.middleware.saml2.schemas.esoe.protocol.ValidateInitializationRequest;
import com.qut.middleware.saml2.schemas.esoe.protocol.ValidateInitializationResponse;
import com.qut.middleware.saml2.schemas.protocol.Status;
import com.qut.middleware.saml2.schemas.protocol.StatusCode;
import com.qut.middleware.saml2.validator.SAMLValidator;

/** */
public class StartupImpl implements Startup
{
	private SAMLValidator samlValidator;
	private SPEPRegistrationCache spepRegistrationCache;
	private Metadata metadata;
	private IdentifierGenerator identifierGenerator;
	private Unmarshaller<ValidateInitializationRequest> validateInitializationRequestUnmarshaller;
	private Marshaller<ValidateInitializationResponse> statusResponseTypeMarshaller;
	private PolicyCacheProcessor policyCacheProcessor;
	
	private final String UNMAR_PKGNAMES = ValidateInitializationRequest.class.getPackage().getName();
	private final String MAR_PKGNAMES = ValidateInitializationRequest.class.getPackage().getName();
	
	/* Local logging instance */
	private Logger logger = Logger.getLogger(StartupImpl.class.getName());
	
	/**
	 * Constructor
	 * @param samlValidator The SAML validator to be used for validating Startup requests.
	 * @param identifierGenerator Used to generate unique id's.
	 * @param spepRegistrationCache The SPEP registration cache.
	 * @param metadata The metadata to use for resolving public keys.
	 * @param keyStoreResolver to use to obtain ESOE private key.
	 * @param policyCacheProcessor used to send cache intialization requests.
	 * @throws UnmarshallerException If the Unmarshaller fails to initialize.
	 * @throws MarshallerException If the Unmarshaller fails to initialize.
	 */
	public StartupImpl(SAMLValidator samlValidator, IdentifierGenerator identifierGenerator, SPEPRegistrationCache spepRegistrationCache, Metadata metadata, KeyStoreResolver keyStoreResolver, PolicyCacheProcessor policyCacheProcessor) throws UnmarshallerException, MarshallerException
	{
		if(samlValidator == null)
		{
			throw new IllegalArgumentException(Messages.getString("StartupImpl.11"));   //$NON-NLS-1$
		}
		if(identifierGenerator == null)
		{
			throw new IllegalArgumentException(Messages.getString("StartupImpl.12")); //$NON-NLS-1$
		}
		if(spepRegistrationCache == null)
		{
			throw new IllegalArgumentException(Messages.getString("StartupImpl.13")); //$NON-NLS-1$
		}
		if(metadata == null)
		{
			throw new IllegalArgumentException(Messages.getString("StartupImpl.14"));   //$NON-NLS-1$
		}
		if(keyStoreResolver == null)
		{
			throw new IllegalArgumentException(Messages.getString("StartupImpl.15"));  //$NON-NLS-1$
		}
		if(policyCacheProcessor == null)
		{
			throw new IllegalArgumentException(Messages.getString("StartupImpl.16"));  //$NON-NLS-1$
		}
		
		String[] schemas = new String[]{ConfigurationConstants.esoeProtocol, ConfigurationConstants.samlProtocol};

		this.samlValidator = samlValidator;
		this.identifierGenerator = identifierGenerator;
		this.spepRegistrationCache = spepRegistrationCache;
		this.metadata = metadata;
		this.policyCacheProcessor = policyCacheProcessor;
		
		this.validateInitializationRequestUnmarshaller = new UnmarshallerImpl<ValidateInitializationRequest>(this.UNMAR_PKGNAMES, schemas, this.metadata);
		this.statusResponseTypeMarshaller = new MarshallerImpl<ValidateInitializationResponse>(this.MAR_PKGNAMES, schemas, keyStoreResolver.getKeyAlias(), keyStoreResolver.getPrivateKey());
		
		this.logger.info(Messages.getString("StartupImpl.5")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.spep.Startup#registerSPEPStartup(com.qut.middleware.esoe.spep.bean.SPEPProcessorData)
	 */
	public void registerSPEPStartup(SPEPProcessorData data) throws InvalidRequestException,
			DatabaseFailureNoSuchSPEPException, SPEPCacheUpdateException, DatabaseFailureException
	{
		ValidateInitializationResponse validateInitializationResponse = null;
		
		this.logger.info(MessageFormat.format(Messages.getString("StartupImpl.6") ,data.getIssuerID()) ); //$NON-NLS-1$
		
		String requestSAMLID = null;
		
		try
		{
			byte[] requestDocument = data.getRequestDocument();
			this.logger.trace( requestDocument);
			
			ValidateInitializationRequest request = null;

			try
			{
				request = this.validateInitializationRequestUnmarshaller.unMarshallSigned(requestDocument);
			}
			catch (UnmarshallerException e)
			{
				validateInitializationResponse = generateResponse(StatusCodeConstants.requester, null);
				this.logger.error(MessageFormat.format(Messages.getString("StartupImpl.0"), data.getIssuerID())); //$NON-NLS-1$
				this.logger.debug(e.getLocalizedMessage(), e);
				throw new InvalidRequestException(MessageFormat.format(Messages.getString("StartupImpl.0"), data.getIssuerID()) ); //$NON-NLS-1$
			}
			catch (SignatureValueException e)
			{
				validateInitializationResponse = generateResponse(StatusCodeConstants.requester, null);
				this.logger.error(Messages.getString("StartupImpl.3"), e); //$NON-NLS-1$
				this.logger.debug(e.getLocalizedMessage(), e);
				throw new InvalidRequestException(Messages.getString("StartupImpl.3")); //$NON-NLS-1$
			}
			catch (ReferenceValueException e)
			{
				validateInitializationResponse = generateResponse(StatusCodeConstants.requester, null);
				this.logger.error(Messages.getString("StartupImpl.4"), e); //$NON-NLS-1$
				this.logger.debug(e.getLocalizedMessage(), e);
				throw new InvalidRequestException(Messages.getString("StartupImpl.4")); //$NON-NLS-1$
			}
			
			if(request == null)
			{
				validateInitializationResponse = generateResponse(StatusCodeConstants.requester, null);
				this.logger.error(Messages.getString("StartupImpl.1")); //$NON-NLS-1$
				throw new InvalidRequestException(Messages.getString("StartupImpl.1")); //$NON-NLS-1$
			}
			
			requestSAMLID = request.getID();
			this.logger.debug(MessageFormat.format(Messages.getString("StartupImpl.7"), requestSAMLID)); //$NON-NLS-1$

			try
			{
				this.samlValidator.getRequestValidator().validate(request);
			}
			catch (InvalidSAMLRequestException e)
			{
				validateInitializationResponse = generateResponse(StatusCodeConstants.requester, requestSAMLID);
				this.logger.debug(MessageFormat.format(Messages.getString("StartupImpl.8"), requestSAMLID), e); //$NON-NLS-1$
				throw new InvalidRequestException(e);
			}
			
			String requestEntityID = request.getIssuer().getValue();
			int authzCacheIndex = request.getAuthzCacheIndex();			
			
			data.setIssuerID(requestEntityID);
			data.setAuthzCacheIndex(authzCacheIndex);

			this.logger.debug(MessageFormat.format(Messages.getString("StartupImpl.9"), requestEntityID, requestSAMLID)); //$NON-NLS-1$
			
			// iBatis query..
			this.spepRegistrationCache.registerSPEP(request);			

			this.logger.info(MessageFormat.format(Messages.getString("StartupImpl.10"), requestEntityID)); //$NON-NLS-1$

			result spepStartingResult = this.policyCacheProcessor.spepStartingNotification(requestEntityID, authzCacheIndex);
			
			// if startup request fails, set appropriate response
			if (!spepStartingResult.equals(result.Success))
			{
				this.logger.error("PolicyCacheProcessor returned failure result for SPEP Startup Request. Setting Response status to " + StatusCodeConstants.requester);
				validateInitializationResponse = generateResponse(StatusCodeConstants.requester, requestSAMLID);
			}
			else
			{
				this.logger.debug("PolicyCacheProcessor returned success result for SPEP Startup Request. Setting Response status to " + StatusCodeConstants.success);
				validateInitializationResponse = generateResponse(StatusCodeConstants.success, requestSAMLID);
			}
		}
		finally
		{
			if(validateInitializationResponse == null)
			{
				validateInitializationResponse = generateResponse(StatusCodeConstants.requester, null);
			}
			
			try
			{
				data.setResponseDocument(this.statusResponseTypeMarshaller.marshallSigned(validateInitializationResponse));
				
				this.logger.trace( data.getResponseDocument());
			}
			catch (MarshallerException e)
			{
				this.logger.error(Messages.getString("StartupImpl.2"), e); //$NON-NLS-1$
				throw new InvalidRequestException(Messages.getString("StartupImpl.2"), e); //$NON-NLS-1$
			}			
		}
	}
	
	/* Generate the response to be sent back to the SPEP requesting startup.
	 * 
	 * @param statusCodeValue The status to set the request to.
	 * @param inResponseTo The SAML ID of the initial request.
	 */
	private ValidateInitializationResponse generateResponse(String statusCodeValue, String inResponseTo)
	{
		ValidateInitializationResponse validateInitializationResponse = new ValidateInitializationResponse();
		
		validateInitializationResponse.setID(this.identifierGenerator.generateSAMLID());
		validateInitializationResponse.setInResponseTo(inResponseTo);
		validateInitializationResponse.setVersion(VersionConstants.saml20);
		
		// Timestamps MUST be set to UTC, no offset
		validateInitializationResponse.setIssueInstant(CalendarUtils.generateXMLCalendar());
		
		Status status = new Status();
		StatusCode statusCode = new StatusCode();
		statusCode.setValue(statusCodeValue);
		status.setStatusCode(statusCode);
		
		NameIDType issuer = new NameIDType();
		issuer.setValue(this.metadata.getEsoeEntityID());

		validateInitializationResponse.setStatus(status);
		validateInitializationResponse.setIssuer(issuer);
		
		Signature signature = new Signature();
		validateInitializationResponse.setSignature(signature);
		
		return validateInitializationResponse;
	}
}
