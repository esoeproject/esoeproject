/* Copyright 2006, Queensland University of Technology
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
 * Creation Date: 06/03/2007
 * 
 * Purpose: Implementation of DelegatedAuthenticationProcessor to allow remote delegated authn handlers to process authentication specific to some known protocol and insert into the ESOE
 */
package com.qut.middleware.esoe.delegauthn.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3._2000._09.xmldsig_.Signature;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.crypto.KeyStoreResolver;
import com.qut.middleware.esoe.delegauthn.DelegatedAuthenticationProcessor;
import com.qut.middleware.esoe.delegauthn.bean.DelegatedAuthenticationData;
import com.qut.middleware.esoe.delegauthn.exception.InvalidResponseException;
import com.qut.middleware.esoe.log4j.InsaneLogLevel;
import com.qut.middleware.esoe.sessions.Create;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.esoe.sessions.exception.DataSourceException;
import com.qut.middleware.esoe.sessions.exception.DuplicateSessionException;
import com.qut.middleware.esoe.sso.impl.Messages;
import com.qut.middleware.esoe.util.CalendarUtils;
import com.qut.middleware.saml2.AuthenticationContextConstants;
import com.qut.middleware.saml2.ConsentIdentifierConstants;
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
import com.qut.middleware.saml2.schemas.assertion.AttributeType;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.esoe.delegated.RegisterPrincipalRequest;
import com.qut.middleware.saml2.schemas.esoe.delegated.RegisterPrincipalResponse;
import com.qut.middleware.saml2.schemas.protocol.Status;
import com.qut.middleware.saml2.schemas.protocol.StatusCode;
import com.qut.middleware.saml2.validator.SAMLValidator;

public class DelegatedAuthenticationProcessorImpl implements DelegatedAuthenticationProcessor
{
	private SAMLValidator samlValidator;
	private SessionsProcessor sessionsProcessor;
	private IdentifierGenerator identifierGenerator;
	private KeyStoreResolver keyStoreResolver;
	private List<String> deniedIdentifiers;
	private String delegatedAuthnIdentifier;

	private Unmarshaller<RegisterPrincipalRequest> unmarshaller;
	private Marshaller<RegisterPrincipalResponse> marshaller;

	private final String[] schemas = new String[]{ConfigurationConstants.delegatedAuthn};
	private final String UNMAR_PKGNAMES = RegisterPrincipalRequest.class.getPackage().getName();
	private final String MAR_PKGNAMES = RegisterPrincipalResponse.class.getPackage().getName();

	/* Local logging instance */
	private Logger logger = Logger.getLogger(DelegatedAuthenticationProcessorImpl.class.getName());

	public DelegatedAuthenticationProcessorImpl(SAMLValidator samlValidator, SessionsProcessor sessionsProcessor,
			IdentifierGenerator identifierGenerator, KeyStoreResolver keyStoreResolver, List<String> deniedIdentifiers, String delegatedAuthnIdentifier) throws UnmarshallerException,
			MarshallerException
	{
		if (samlValidator == null)
		{
			this.logger.fatal(Messages.getString("DelegatedAuthenticationProcessorImpl.0")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("DelegatedAuthenticationProcessorImpl.1")); //$NON-NLS-1$
		}
		if (sessionsProcessor == null)
		{
			this.logger.fatal(Messages.getString("DelegatedAuthenticationProcessorImpl.2")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("DelegatedAuthenticationProcessorImpl.3")); //$NON-NLS-1$
		}
		if (identifierGenerator == null)
		{
			this.logger.fatal(Messages.getString("DelegatedAuthenticationProcessorImpl.4")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("DelegatedAuthenticationProcessorImpl.5")); //$NON-NLS-1$
		}
		if (keyStoreResolver == null)
		{
			this.logger.fatal(Messages.getString("DelegatedAuthenticationProcessorImpl.6")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("DelegatedAuthenticationProcessorImpl.7")); //$NON-NLS-1$
		}
		if(deniedIdentifiers == null)
		{
			this.logger.fatal(Messages.getString("DelegatedAuthenticationProcessorImpl.8")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("DelegatedAuthenticationProcessorImpl.9")); //$NON-NLS-1$
		}
		if(delegatedAuthnIdentifier == null)
		{
			this.logger.fatal(Messages.getString("DelegatedAuthenticationProcessorImpl.10")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("DelegatedAuthenticationProcessorImpl.11")); //$NON-NLS-1$
		}

		this.samlValidator = samlValidator;
		this.sessionsProcessor = sessionsProcessor;
		this.identifierGenerator = identifierGenerator;
		this.keyStoreResolver = keyStoreResolver;
		this.deniedIdentifiers = deniedIdentifiers;
		this.delegatedAuthnIdentifier = delegatedAuthnIdentifier;

		this.unmarshaller = new UnmarshallerImpl<RegisterPrincipalRequest>(this.UNMAR_PKGNAMES, this.schemas, this.keyStoreResolver);
		this.marshaller = new MarshallerImpl<RegisterPrincipalResponse>(this.MAR_PKGNAMES, this.schemas, keyStoreResolver.getKeyAlias(), keyStoreResolver.getPrivateKey());
	}

	public result execute(DelegatedAuthenticationData processorData)
	{
		RegisterPrincipalRequest request;
		String sessionID;
		List<AttributeType> attributes = new ArrayList<AttributeType>();
		
		try
		{
			this.logger.debug(Messages.getString("DelegatedAuthenticationProcessorImpl.12")); //$NON-NLS-1$
			
			request = this.unmarshaller.unMarshallSigned(processorData.getRequestDocument());
		
			processorData.setRegisterPrincipalRequest(request);
			
			this.samlValidator.getRequestValidator().validate(request);
			
			sessionID = this.identifierGenerator.generateSessionID();
			
			/* Run through and grab all AttributeTypes from the request, when EncryptedAttribute support is
			 * enabled this will need to be extended
			 */
			for(Object obj : request.getAttributesAndEncryptedAttributes())
			{
				if(obj instanceof AttributeType)
				{
					AttributeType attrib = (AttributeType)obj;
					
					/* Only add this attribute if its not been marked as banned by ESOE admins,
					 * this would generally include values to do with security level or identifiers that
					 * should only be able to be configured at the campus level
					 */
					if(!this.deniedIdentifiers.contains(attrib.getName()))
					{
						this.logger.debug(MessageFormat.format(Messages.getString("DelegatedAuthenticationProcessorImpl.13") , attrib.getName(), sessionID) ); //$NON-NLS-1$
						attributes.add(attrib);
					}
					else
						this.logger.error(Messages.getString("DelegatedAuthenticationProcessorImpl.14") + attrib.getName() + Messages.getString("DelegatedAuthenticationProcessorImpl.15") + sessionID); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			
			Create.result res = this.sessionsProcessor.getCreate().createDelegatedSession(sessionID, request.getPrincipalAuthnIdentifier(), AuthenticationContextConstants.unspecified, attributes);
			
			if(res == Create.result.SessionCreated)
			{
				createSuccessfulRegisterPrincipalResponse(processorData, sessionID);
				return result.Successful;
			}
			
			createFailedRegisterPrincipalResponse(processorData);
			
			return result.Failure;
		}
		catch (SignatureValueException e)
		{
			createFailedRegisterPrincipalResponse(processorData);
			return result.Failure;
		}
		catch (ReferenceValueException e)
		{
			createFailedRegisterPrincipalResponse(processorData);
			return result.Failure;
		}
		catch (UnmarshallerException e)
		{
			createFailedRegisterPrincipalResponse(processorData);
			return result.Failure;
		}
		catch (InvalidSAMLRequestException e)
		{
			createFailedRegisterPrincipalResponse(processorData);
			return result.Failure;
		}
		catch (DataSourceException e)
		{
			createFailedRegisterPrincipalResponse(processorData);
			return result.Failure;
		}
		catch (DuplicateSessionException e)
		{
			createFailedRegisterPrincipalResponse(processorData);
			return result.Failure;
		}
		catch (InvalidResponseException e)
		{
			createFailedRegisterPrincipalResponse(processorData);
			return result.Failure;
		}
	}
	
	private void createSuccessfulRegisterPrincipalResponse(DelegatedAuthenticationData processorData, String sessionID) throws InvalidResponseException
	{
		RegisterPrincipalResponse response;
		NameIDType issuer;
		Signature signature;
		Status status;
		StatusCode statusCode;
		
		response = new RegisterPrincipalResponse();
		
		/* Advised the delegated handler what value to give the principal to identify their session */
		response.setSessionIdentifier(sessionID);

		/* Generate success status */
		statusCode = new StatusCode();
		statusCode.setValue(StatusCodeConstants.success);

		status = new Status();
		status.setStatusCode(statusCode);

		/* Generate Issuer to attach to response */
		issuer = new NameIDType();
		issuer.setValue(this.delegatedAuthnIdentifier);

		/* Generate placeholder <Signature/> block for SAML2lib-j in response */
		signature = new Signature();

		/* Generate our response */
		response.setID(this.identifierGenerator.generateSAMLID());
		response.setInResponseTo(processorData.getRegisterPrincipalRequest().getID());
		response.setVersion(VersionConstants.saml20);
		response.setIssueInstant(CalendarUtils.generateXMLCalendar(0));
		response.setConsent(ConsentIdentifierConstants.unspecified);

		response.setIssuer(issuer);
		response.setSignature(signature);
		response.setStatus(status);

		marshalResponse(processorData, response);
	}

	private void createFailedRegisterPrincipalResponse(DelegatedAuthenticationData processorData)
	{
		RegisterPrincipalResponse response;
		NameIDType issuer;
		Signature signature;
		Status status;
		StatusCode statusCode;
		
		response = new RegisterPrincipalResponse();

		/* Generate success status */
		statusCode = new StatusCode();
		statusCode.setValue(StatusCodeConstants.authnFailed);

		status = new Status();
		status.setStatusCode(statusCode);

		/* Generate Issuer to attach to response */
		issuer = new NameIDType();
		issuer.setValue(this.delegatedAuthnIdentifier);

		/* Generate placeholder <Signature/> block for SAML2lib-j in response */
		signature = new Signature();

		/* Generate our response */
		response.setID(this.identifierGenerator.generateSAMLID());
		
		// don't add this if there was a problem unmarshalling the request
		if(processorData.getRegisterPrincipalRequest() != null)
		{
			response.setInResponseTo(processorData.getRegisterPrincipalRequest().getID());
		}
		
		response.setVersion(VersionConstants.saml20);
		response.setIssueInstant(CalendarUtils.generateXMLCalendar(0));
		response.setConsent(ConsentIdentifierConstants.unspecified);

		response.setIssuer(issuer);
		response.setSignature(signature);
		response.setStatus(status);

		try
		{
			marshalResponse(processorData, response);
		}
		catch (InvalidResponseException e)
		{
			/* Can't generate a failure response for some reason so null it must be */
			processorData.setResponseDocument(null);
		}
	}
	
		
	/**
	 * Uses SAML2lib-j to marshall a response object to string and store in ResponseDocument of supplied data bean.
	 * 
	 * @param data
	 *            Populated SSOProcessor data bean
	 * @param response
	 *            A fully populated SAML response object for the response type to be returned to the SPEP
	 * @throws InvalidResponseException
	 */
	private void marshalResponse(DelegatedAuthenticationData processorData, RegisterPrincipalResponse response) throws InvalidResponseException
	{
		String responseDocument;
		try
		{
			responseDocument = this.marshaller.marshallSigned(response);
			processorData.setResponseDocument(responseDocument);
			this.logger.log(InsaneLogLevel.INSANE, Messages.getString("AuthenticationAuthorityProcessor.56") + processorData.getResponseDocument()); //$NON-NLS-1$
		}
		catch (MarshallerException me)
		{
			throw new InvalidResponseException(Messages.getString("AuthenticationAuthorityProcessor.14"), me); //$NON-NLS-1$
		}
	}
}
