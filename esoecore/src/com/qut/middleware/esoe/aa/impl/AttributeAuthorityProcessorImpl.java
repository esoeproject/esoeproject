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
 * Creation Date: 19/10/2006
 * 
 * Purpose: Implements the AttributeAuthorityProcessor interface to handle attribute
 * 		queries.
 */
package com.qut.middleware.esoe.aa.impl;

import java.security.PrivateKey;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.w3._2000._09.xmldsig_.Signature;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.aa.AttributeAuthorityProcessor;
import com.qut.middleware.esoe.aa.Messages;
import com.qut.middleware.esoe.aa.bean.AAProcessorData;
import com.qut.middleware.esoe.aa.exception.InvalidPrincipalException;
import com.qut.middleware.esoe.aa.exception.InvalidRequestException;
import com.qut.middleware.esoe.crypto.KeyStoreResolver;
import com.qut.middleware.esoe.metadata.Metadata;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.esoe.sessions.bean.IdentityAttribute;
import com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException;
import com.qut.middleware.esoe.util.CalendarUtils;
import com.qut.middleware.saml2.AttributeFormatConstants;
import com.qut.middleware.saml2.ConfirmationMethodConstants;
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
import com.qut.middleware.saml2.schemas.assertion.AttributeStatement;
import com.qut.middleware.saml2.schemas.assertion.AttributeType;
import com.qut.middleware.saml2.schemas.assertion.Conditions;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.assertion.Subject;
import com.qut.middleware.saml2.schemas.assertion.SubjectConfirmation;
import com.qut.middleware.saml2.schemas.assertion.SubjectConfirmationDataType;
import com.qut.middleware.saml2.schemas.protocol.AttributeQuery;
import com.qut.middleware.saml2.schemas.protocol.Response;
import com.qut.middleware.saml2.schemas.protocol.Status;
import com.qut.middleware.saml2.schemas.protocol.StatusCode;
import com.qut.middleware.saml2.validator.SAMLValidator;

public class AttributeAuthorityProcessorImpl implements AttributeAuthorityProcessor
{
	private SessionsProcessor sessionsProcessor;
	private PrivateKey key;
	private String keyName;
	private Metadata metadata;
	private SAMLValidator samlValidator;
	private String requestError = StatusCodeConstants.requester;
	private String authnError = StatusCodeConstants.authnFailed;
	private String success = StatusCodeConstants.success;
	private IdentifierGenerator identifierGenerator;
	private Marshaller<Response> attributeStatementMarshaller;
	private Unmarshaller<AttributeQuery> attributeQueryUnmarshaller;
	private int allowedTimeSkew;
	
	private final String UNMAR_PKGNAMES = AttributeQuery.class.getPackage().getName();
	private final String MAR_PKGNAMES = Response.class.getPackage().getName();
	
	/* Local logging instance */
	private Logger logger = Logger.getLogger(AttributeAuthorityProcessorImpl.class.getName());
	
	/**
	 * Constructor.
	 * 
	 * @param metadata Metadata used to glean SPEP information.
	 * @param sessionsProcessor The sessions processor used to obtain principal information.
	 * @param samlValidator For validating SAML Requests/Responses.
	 * @param identifierGenerator Used to generate unique ID's. 
	 * @param keyStoreResolver used to resolve public keys.
	 * @param allowedTimeSkew Time skew in seconds we will accept our SPEP being out, this
	 *  applied to both recieved messages and responses. 
	 *  
	 * @throws MarshallerException if the marshaller cannot be created.
	 * @throws UnmarshallerException if the unmarshaller cannot be created.
	 */
	public AttributeAuthorityProcessorImpl(Metadata metadata, SessionsProcessor sessionsProcessor,
			SAMLValidator samlValidator, IdentifierGenerator identifierGenerator, KeyStoreResolver keyStoreResolver, int allowedTimeSkew) throws MarshallerException, UnmarshallerException
	{
		if(metadata == null)
		{
			throw new IllegalArgumentException(Messages.getString("AttributeAuthorityProcessorImpl.19"));  //$NON-NLS-1$
		}
		if(sessionsProcessor == null)
		{
			throw new IllegalArgumentException(Messages.getString("AttributeAuthorityProcessorImpl.20")); //$NON-NLS-1$
		}
		if(samlValidator == null)
		{
			throw new IllegalArgumentException(Messages.getString("AttributeAuthorityProcessorImpl.21"));  //$NON-NLS-1$
		}
		if(identifierGenerator == null)
		{
			throw new IllegalArgumentException(Messages.getString("AttributeAuthorityProcessorImpl.22")); //$NON-NLS-1$
		}
		if(keyStoreResolver == null)
		{
			throw new IllegalArgumentException(Messages.getString("AttributeAuthorityProcessorImpl.23")); //$NON-NLS-1$
		}
		if(allowedTimeSkew > Integer.MAX_VALUE / 1000)
		{
			throw new IllegalArgumentException(Messages.getString("AttributeAuthorityProcessorImpl.10")); //$NON-NLS-1$
		}
		
		this.metadata = metadata;
		this.sessionsProcessor = sessionsProcessor;
		this.samlValidator = samlValidator;
		this.identifierGenerator = identifierGenerator;
		this.key = keyStoreResolver.getPrivateKey();
		this.keyName = keyStoreResolver.getKeyAlias();
		this.allowedTimeSkew = allowedTimeSkew;
		
		String[] schemas = new String[] {ConfigurationConstants.samlProtocol};
		this.attributeStatementMarshaller = new MarshallerImpl<Response>(this.MAR_PKGNAMES, schemas, this.keyName, this.key);
		this.attributeQueryUnmarshaller = new UnmarshallerImpl<AttributeQuery>(this.UNMAR_PKGNAMES, schemas, metadata);
		
		this.logger.info(MessageFormat.format(Messages.getString("AttributeAuthorityProcessorImpl.12"), Integer.toString(allowedTimeSkew))); //$NON-NLS-1$
	}

	
	/* Builds a SAML Response containing attributes of the requested principal.
	 * 
	 * @param request The original attribute query SAML Request containing the attributes
	 * to retrieve.
	 * @param principal The principal to retrieve attributes for.
	 */
	private Response buildResponse(AttributeQuery request, Principal principal)
	{
		this.logger.debug(MessageFormat.format(Messages.getString("AttributeAuthorityProcessorImpl.13"), request.getID(), principal.getPrincipalAuthnIdentifier())); //$NON-NLS-1$
		AttributeStatement attributeStatement = new AttributeStatement();

		// Start building the statement..
		List<AttributeType> attributeList = request.getAttributes();
		for (AttributeType attribute : attributeList)
		{
			// For each attribute..

			String attributeName = attribute.getName();
			// .. find it in the principal object ..
			IdentityAttribute identityAttribute = principal.getAttributes().get(attributeName);

			if (identityAttribute != null)
			{
				AttributeType newAttribute = new AttributeType();
				newAttribute.setFriendlyName(attribute.getFriendlyName());
				newAttribute.setName(attribute.getName());
				newAttribute.setNameFormat(AttributeFormatConstants.basic);
				
				for (Object value : identityAttribute.getValues())
				{
					newAttribute.getAttributeValues().add(value);
				}

				// .. and add any values of that attribute to the statement.
				attributeStatement.getEncryptedAttributesAndAttributes().add(newAttribute);
			}
		}
		
		if (attributeList.size() <= 0)
		{
			this.logger.debug(Messages.getString("AttributeAuthorityProcessorImpl.14")); //$NON-NLS-1$
			for (Entry<String, IdentityAttribute> entry : principal.getAttributes().entrySet())
			{
				IdentityAttribute identityAttribute = entry.getValue();
				
				AttributeType newAttribute = new AttributeType();
				newAttribute.setName(entry.getKey());
				newAttribute.setNameFormat(AttributeFormatConstants.basic);
				
				for (Object value : identityAttribute.getValues())
				{
					newAttribute.getAttributeValues().add(value);
				}

				attributeStatement.getEncryptedAttributesAndAttributes().add(newAttribute);
			}
		}
		
		this.logger.debug(MessageFormat.format(Messages.getString("AttributeAuthorityProcessorImpl.15"), principal.getPrincipalAuthnIdentifier(), attributeStatement.getEncryptedAttributesAndAttributes().size())); //$NON-NLS-1$

		String assertionID = this.identifierGenerator.generateSAMLID();
		String responseID = this.identifierGenerator.generateSAMLID();
		
		NameIDType issuer = new NameIDType();
		issuer.setValue(this.metadata.getEsoeEntityID());

		// Create and populate the response with values
		Response response = new Response();
		Subject subject = new Subject();
		
		response.setID(responseID);
		response.setVersion(VersionConstants.saml20);
		response.setIssuer(issuer);
		
		// Timestamps MUST be set to UTC, no offset
		response.setIssueInstant(CalendarUtils.generateXMLCalendar(0));		
		response.setInResponseTo(request.getID());

		Signature signature = new Signature();
		response.setSignature(signature);
		
		// Conditions
		Conditions conditions = new Conditions();
		conditions.setNotOnOrAfter(CalendarUtils.generateXMLCalendar(this.allowedTimeSkew));
	
		/* subject MUST contain a SubjectConfirmation */
		SubjectConfirmation confirmation = new SubjectConfirmation();
		confirmation.setMethod(ConfirmationMethodConstants.bearer);
		SubjectConfirmationDataType confirmationData = new SubjectConfirmationDataType();
		confirmationData.setInResponseTo(request.getID());
		confirmationData.setNotOnOrAfter(CalendarUtils.generateXMLCalendar(this.allowedTimeSkew));
		confirmation.setSubjectConfirmationData(confirmationData);
		subject.getSubjectConfirmationNonID().add(confirmation);
	
		// Create an assertion
		Assertion assertion = new Assertion();
		assertion.setIssueInstant(CalendarUtils.generateXMLCalendar(0));
		assertion.setVersion(VersionConstants.saml20);
		assertion.setID(assertionID);
		assertion.setIssuer(issuer);
		assertion.setConditions(conditions);		
		assertion.getAuthnStatementsAndAuthzDecisionStatementsAndAttributeStatements().add(attributeStatement);
		assertion.setSubject(subject);
		
		Status status = new Status();
		StatusCode statusCode = new StatusCode();
		statusCode.setValue(this.success);
		status.setStatusCode(statusCode);

		response.getEncryptedAssertionsAndAssertions().add(assertion);
		response.setStatus(status);

		return response;
	}

	
	/* Creates a SAML error Response to return if there is an error retrieving
	 * attributes.
	 * 
	 */
	private Response getErrorResponse(String code)
	{
		// Build a response with the appropriate error code
		// Won't need any more information since it's an error response.	
		Response response = new Response();
		NameIDType issuer = new NameIDType();
		issuer.setValue(this.metadata.getEsoeEntityID());
		
		this.logger.debug(MessageFormat.format(Messages.getString("AttributeAuthorityProcessorImpl.16"), code)); //$NON-NLS-1$

		Signature signature = new Signature();
		response.setSignature(signature);

		Status status = new Status();
		StatusCode statusCode = new StatusCode();
		statusCode.setValue(code);
		status.setStatusCode(statusCode);
		response.setStatus(status);
		response.setID(this.identifierGenerator.generateSAMLID());
		response.setVersion(VersionConstants.saml20);
		response.setIssueInstant(CalendarUtils.generateXMLCalendar(0));
		response.setIssuer(issuer);

		return response;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.aa.AttributeAuthorityProcessor#processRequest(com.qut.middleware.esoe.aa.bean.AAProcessorData)
	 */
	public result execute(AAProcessorData processorData) throws InvalidPrincipalException,
			InvalidRequestException
	{
		Response response = null;
		
		boolean error = true;
		
		try
		{
			AttributeQuery attributeQuery;
			try
			{
				// Unmarshal the attribute query (request document).
				attributeQuery = this.attributeQueryUnmarshaller.unMarshallSigned(processorData.getRequestDocument());
			}
			catch (UnmarshallerException e)
			{
				response = getErrorResponse(this.requestError);
				this.logger.error(Messages.getString("AttributeAuthorityProcessorImpl.0")); //$NON-NLS-1$
				this.logger.debug(Messages.getString("AttributeAuthorityProcessorImpl.0"), e); //$NON-NLS-1$
				throw new InvalidRequestException(Messages.getString("AttributeAuthorityProcessorImpl.0"), e); //$NON-NLS-1$
			}
			catch (SignatureValueException e)
			{
				response = getErrorResponse(this.requestError);
				this.logger.error(Messages.getString("AttributeAuthorityProcessorImpl.1")); //$NON-NLS-1$
				this.logger.debug(Messages.getString("AttributeAuthorityProcessorImpl.1"), e); //$NON-NLS-1$
				throw new InvalidRequestException(Messages.getString("AttributeAuthorityProcessorImpl.1"), e); //$NON-NLS-1$
			}
			catch (ReferenceValueException e)
			{
				response = getErrorResponse(this.requestError);
				this.logger.error(Messages.getString("AttributeAuthorityProcessorImpl.2")); //$NON-NLS-1$
				this.logger.debug(Messages.getString("AttributeAuthorityProcessorImpl.2"), e); //$NON-NLS-1$
				throw new InvalidRequestException(Messages.getString("AttributeAuthorityProcessorImpl.2"), e); //$NON-NLS-1$
			}

			if (attributeQuery == null)
			{
				response = getErrorResponse(this.requestError);
				this.logger.error(Messages.getString("AttributeAuthorityProcessorImpl.2")); //$NON-NLS-1$
				throw new InvalidRequestException(Messages.getString("AttributeAuthorityProcessorImpl.3")); //$NON-NLS-1$
			}
			
			this.logger.debug(MessageFormat.format(Messages.getString("AttributeAuthorityProcessorImpl.17"), attributeQuery.getID())); //$NON-NLS-1$

			// Validate the request
			try
			{
				this.samlValidator.getRequestValidator().validate(attributeQuery);
			}
			catch (InvalidSAMLRequestException e)
			{
				response = getErrorResponse(this.requestError);
				this.logger.error(Messages.getString("AttributeAuthorityProcessorImpl.4")); //$NON-NLS-1$
				throw new InvalidRequestException(Messages.getString("AttributeAuthorityProcessorImpl.4"), e); //$NON-NLS-1$
			}

			// Get the subject ID from the XML
			Subject subject = attributeQuery.getSubject();
			if (subject == null)
			{
				response = getErrorResponse(this.requestError);
				String message = MessageFormat.format(Messages.getString("AttributeAuthorityProcessorImpl.5"), attributeQuery.getID()); //$NON-NLS-1$
				this.logger.error(message);
				throw new InvalidRequestException(message);
			}

			NameIDType nameID = subject.getNameID();
			if (nameID == null)
			{
				response = getErrorResponse(this.requestError);
				String message = MessageFormat.format(Messages.getString("AttributeAuthorityProcessorImpl.6"), attributeQuery.getID()); //$NON-NLS-1$
				this.logger.error(message);
				throw new InvalidRequestException(message);
			}

			String subjectID = nameID.getValue();

			processorData.setSubjectID(subjectID);

			// Query the sessions processor to get the principal in question
			Principal principal;
			try
			{
				principal = this.sessionsProcessor.getQuery().querySAMLSession(subjectID);
			}
			catch (InvalidSessionIdentifierException e)
			{
				response = getErrorResponse(this.authnError);
				String message = MessageFormat.format(Messages.getString("AttributeAuthorityProcessorImpl.7"), attributeQuery.getID()); //$NON-NLS-1$
				this.logger.error(message);
				throw new InvalidPrincipalException(message);
			}

			NameIDType issuer = attributeQuery.getIssuer();
			if (issuer == null)
			{
				response = getErrorResponse(this.requestError);
				this.logger.error(Messages.getString("AttributeAuthorityProcessorImpl.8")); //$NON-NLS-1$
				throw new InvalidRequestException(Messages.getString("AttributeAuthorityProcessorImpl.8")); //$NON-NLS-1$
			}

			// Set the issuer ID
			processorData.setIssuerID(issuer.getValue());

			response = buildResponse(attributeQuery, principal);
					
			error = false;
			return result.Successful;
		}
		finally
		{
			// Create a marshaller to marshal the statement back into XML
			String responseDocument;
			try
			{
				if (response == null)
				{
					response = getErrorResponse(this.requestError);
				}
				// Marshal and sign the response			
				processorData.setResponseDocument(this.attributeStatementMarshaller.marshallSigned(response));
				
				this.logger.debug(MessageFormat.format(Messages.getString("AttributeAuthorityProcessorImpl.18"), response.getID())); //$NON-NLS-1$
			}
			catch (MarshallerException e)
			{
				// Can't marshal the error document.
				// Still let the original exception through
				
				if (!error)
				{
					this.logger.error(Messages.getString("AttributeAuthorityProcessorImpl.9")); //$NON-NLS-1$
					this.logger.debug(Messages.getString("AttributeAuthorityProcessorImpl.9"), e); //$NON-NLS-1$
					throw new InvalidRequestException(Messages.getString("AttributeAuthorityProcessorImpl.9"), e); //$NON-NLS-1$
				}
			}
		}
	}
}
