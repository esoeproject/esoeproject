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
 * Creation Date: 24/11/2006
 * 
 * Purpose: Implements the AttributeProcessor interface.
 */
package com.qut.middleware.spep.attribute.impl;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;

import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3._2000._09.xmldsig_.Signature;
import org.w3c.dom.Element;

import com.qut.middleware.crypto.KeystoreResolver;
import com.qut.middleware.metadata.bean.saml.AttributeAuthorityRole;
import com.qut.middleware.metadata.bean.saml.TrustedESOERole;
import com.qut.middleware.metadata.exception.MetadataStateException;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.saml2.BindingConstants;
import com.qut.middleware.saml2.SchemaConstants;
import com.qut.middleware.saml2.StatusCodeConstants;
import com.qut.middleware.saml2.VersionConstants;
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
import com.qut.middleware.saml2.schemas.assertion.AttributeStatement;
import com.qut.middleware.saml2.schemas.assertion.AttributeType;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.assertion.StatementAbstractType;
import com.qut.middleware.saml2.schemas.assertion.Subject;
import com.qut.middleware.saml2.schemas.assertion.SubjectConfirmation;
import com.qut.middleware.saml2.schemas.assertion.SubjectConfirmationDataType;
import com.qut.middleware.saml2.schemas.protocol.AttributeQuery;
import com.qut.middleware.saml2.schemas.protocol.Response;
import com.qut.middleware.saml2.validator.SAMLValidator;
import com.qut.middleware.spep.attribute.AttributeProcessor;
import com.qut.middleware.spep.attribute.Messages;
import com.qut.middleware.spep.exception.AttributeProcessingException;
import com.qut.middleware.spep.sessions.PrincipalSession;
import com.qut.middleware.spep.util.CalendarUtils;
import com.qut.middleware.spep.ws.WSClient;
import com.qut.middleware.spep.ws.exception.WSClientException;

/** Implements the AttributeProcessor interface. */
public class AttributeProcessorImpl implements AttributeProcessor
{
	//private static int BUFFER_LEN = 4096;
	protected MetadataProcessor metadata;
	protected WSClient wsClient;
	protected IdentifierGenerator identifierGenerator;
	private String[] schemas = new String[] { SchemaConstants.samlAssertion, SchemaConstants.samlProtocol };
	private Marshaller<AttributeQuery> attributeQueryMarshaller;
	private Unmarshaller<Response> responseUnmarshaller;
	private SAMLValidator samlValidator;
	private String trustedESOEIdentifier;

	private final String UNMAR_PKGNAMES = Response.class.getPackage().getName();
	//private final String UNMAR_PKGNAMES2 = AttributeConfig.class.getPackage().getName();
	private final String MAR_PKGNAMES = AttributeQuery.class.getPackage().getName();
	private final String IMPLEMENTED_BINDING = BindingConstants.soap;

	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(AttributeProcessorImpl.class.getName());
	private String spepIdentifier;
	private boolean disableAttributeQuery;
	private boolean enableCompatibility;

	/**
	 * Constructor
	 * 
	 * @param metadata
	 *            The metadata used to otain SPEP service details.
	 * @param wsClient
	 *            The web service client to use for web service calls.
	 * @param identifierGenerator
	 *            For generating unique SAML identifiers.
	 * @param samlValidator
	 *            The SAML validator
	 * @param keyStoreResolver
	 *            The keystore resolver.
	 * @throws MarshallerException
	 *             if the marshaller cannot be created.
	 * @throws UnmarshallerException
	 *             if the unmarshaller cannot be created.
	 * @throws IOException
	 *             if there is an error reading the xml FileStream.
	 */
	public AttributeProcessorImpl(MetadataProcessor metadata, WSClient wsClient, IdentifierGenerator identifierGenerator, SAMLValidator samlValidator, KeystoreResolver keyStoreResolver, String trustedESOEIdentifier, String spepIdentifier, boolean disableAttributeQuery, boolean enableCompatibility) throws MarshallerException, UnmarshallerException, IOException
	{
		if (metadata == null)
		{
			throw new IllegalArgumentException(Messages.getString("AttributeProcessorImpl.26")); //$NON-NLS-1$
		}
		if (wsClient == null)
		{
			throw new IllegalArgumentException(Messages.getString("AttributeProcessorImpl.33")); //$NON-NLS-1$
		}
		if (identifierGenerator == null)
		{
			throw new IllegalArgumentException(Messages.getString("AttributeProcessorImpl.34")); //$NON-NLS-1$
		}
		if (samlValidator == null)
		{
			throw new IllegalArgumentException(Messages.getString("AttributeProcessorImpl.35")); //$NON-NLS-1$
		}
		if (keyStoreResolver == null)
		{
			throw new IllegalArgumentException(Messages.getString("AttributeProcessorImpl.37")); //$NON-NLS-1$
		}
		if (trustedESOEIdentifier == null)
		{
			throw new IllegalArgumentException("trustedESOEIdentifier cannot be null");
		}

		this.metadata = metadata;
		this.wsClient = wsClient;
		this.identifierGenerator = identifierGenerator;
		
		// We don't need a marshaller/unmarshaller if we're not going to do any attribute queries
		if (!disableAttributeQuery)
		{
			this.attributeQueryMarshaller = new MarshallerImpl<AttributeQuery>(this.MAR_PKGNAMES, this.schemas, keyStoreResolver);
			this.responseUnmarshaller = new UnmarshallerImpl<Response>(this.UNMAR_PKGNAMES, this.schemas, this.metadata);
		}
		
		this.samlValidator = samlValidator;
		this.trustedESOEIdentifier = trustedESOEIdentifier;
		this.spepIdentifier = spepIdentifier;
		this.disableAttributeQuery = disableAttributeQuery;
		this.enableCompatibility = enableCompatibility;

		this.logger.info(Messages.getString("AttributeProcessorImpl.27")); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.spep.attribute.AttributeProcessor#beginAttributeProcessing(com.qut.middleware.spep.sessions.PrincipalSession)
	 */
	public void doAttributeProcessing(PrincipalSession principalSession) throws AttributeProcessingException
	{
		if (principalSession == null)
		{
			this.logger.error(Messages.getString("AttributeProcessorImpl.3")); //$NON-NLS-1$
			throw new AttributeProcessingException(Messages.getString("AttributeProcessorImpl.4")); //$NON-NLS-1$
		}
		if (principalSession.getEsoeSessionID() == null || principalSession.getEsoeSessionID().length() == 0)
		{
			this.logger.error(Messages.getString("AttributeProcessorImpl.5")); //$NON-NLS-1$
			throw new AttributeProcessingException(Messages.getString("AttributeProcessorImpl.6")); //$NON-NLS-1$
		}
		if (principalSession.getAttributes() == null)
		{
			this.logger.error(Messages.getString("AttributeProcessorImpl.9")); //$NON-NLS-1$
			throw new AttributeProcessingException(Messages.getString("AttributeProcessorImpl.10")); //$NON-NLS-1$
		}
		
		// Now that we've sanity checked everything, return if attribute processing is disabled.
		if (this.disableAttributeQuery) return;
		
		this.logger.info("Going to do attribute processing for new session with ESOE session ID: " + principalSession.getEsoeSessionID());

		// Build the attribute query
		String samlID = this.identifierGenerator.generateSAMLID();
		this.logger.debug(MessageFormat.format(Messages.getString("AttributeProcessorImpl.28"), samlID)); //$NON-NLS-1$
		Element requestDocument = this.buildAttributeQuery(principalSession, samlID);

		if (requestDocument == null)
		{
			this.logger.error(Messages.getString("AttributeProcessorImpl.11")); //$NON-NLS-1$
			throw new AttributeProcessingException(Messages.getString("AttributeProcessorImpl.12")); //$NON-NLS-1$
		}

		// Web service call. May take a long time to return.
		Element responseDocument = null;
		try
		{
			this.logger.debug(MessageFormat.format(Messages.getString("AttributeProcessorImpl.29"), samlID)); //$NON-NLS-1$

			String endpoint;
			AttributeAuthorityRole attributeAuthorityRole;
			
			// If we're running in compatibility mode, don't enforce that the target identity provider be an ESOE.
			if (this.enableCompatibility)
			{
				attributeAuthorityRole = this.metadata.getEntityRoleData(this.trustedESOEIdentifier, AttributeAuthorityRole.class);
			}
			else
			{
				attributeAuthorityRole = this.metadata.getEntityRoleData(this.trustedESOEIdentifier, TrustedESOERole.class);
			}
			
			if (attributeAuthorityRole == null)
			{
				throw new AttributeProcessingException("Attribute authority value was null. ESOE entity (" + this.trustedESOEIdentifier + ") did not exist or did not have the expected role. Unable to continue processing for session " + principalSession.getEsoeSessionID());
			}

			endpoint = attributeAuthorityRole.getAttributeServiceEndpoint(IMPLEMENTED_BINDING);
			
			if (endpoint == null)
			{
				throw new AttributeProcessingException("Attribute authority endpoint was null for binding " + IMPLEMENTED_BINDING + " - unable to continue processing for session " + principalSession.getEsoeSessionID());
			}
			responseDocument = this.wsClient.attributeAuthority(requestDocument, endpoint);

			this.logger.debug(MessageFormat.format(Messages.getString("AttributeProcessorImpl.30"), samlID)); //$NON-NLS-1$
		}
		catch (WSClientException e)
		{
			this.logger.error(Messages.getString("AttributeProcessorImpl.13") + e.getLocalizedMessage()); //$NON-NLS-1$
			throw new AttributeProcessingException(Messages.getString("AttributeProcessorImpl.14"), e); //$NON-NLS-1$
		}
		catch (MetadataStateException e)
		{
			this.logger.error("Failed to resolve trusted ESOE from metadata - state was invalid. Exception was: " + e.getMessage());
			throw new AttributeProcessingException("Failed to resolve trusted ESOE from metadata - state was invalid.", e);
		}

		// Unmarshal the response
		List<AttributeStatement> attributeStatements = this.getAttributeStatements(responseDocument, samlID);

		if (attributeStatements == null || attributeStatements.size() == 0)
		{
			this.logger.info(Messages.getString("AttributeProcessorImpl.19") + principalSession.getEsoeSessionID()); //$NON-NLS-1$
			return;
		}

		this.logger.debug(MessageFormat.format(Messages.getString("AttributeProcessorImpl.31"), samlID, Integer.toString(attributeStatements.size()))); //$NON-NLS-1$

		// add retrieved attributes to client session
		for (AttributeStatement attributeStatement : attributeStatements)
		{
			for (Object encryptedAttributeOrAttribute : attributeStatement.getEncryptedAttributesAndAttributes())
			{
				if (encryptedAttributeOrAttribute instanceof AttributeType)
				{
					AttributeType attribute = (AttributeType) encryptedAttributeOrAttribute;

					String attributeName = attribute.getName();
					List<Object> attributeValues = principalSession.getAttributes().get(attributeName);
					if (attributeValues == null)
					{
						attributeValues = new Vector<Object>(0, 1);
						principalSession.getAttributes().put(attributeName, attributeValues);
					}

					attributeValues.addAll(attribute.getAttributeValues());
				}
			}
		}

		this.logger.debug(MessageFormat.format(Messages.getString("AttributeProcessorImpl.32"), principalSession.getEsoeSessionID(), principalSession.getEsoeSessionID())); //$NON-NLS-1$
	}

	/*
	 * Builds the string representation of an AttributeQuery using client details contained in the PrincipalSession and
	 * the given SAML ID.
	 * 
	 */
	private Element buildAttributeQuery(PrincipalSession principalSession, String samlID)
	{
		Subject subject = new Subject();
		NameIDType subjectNameID = new NameIDType();
		subjectNameID.setValue(principalSession.getEsoeSessionID());
		subject.setNameID(subjectNameID);

		NameIDType issuer = new NameIDType();
		issuer.setValue(this.spepIdentifier);

		// Build the attribute query.
		AttributeQuery attributeQuery = new AttributeQuery();
		attributeQuery.setID(samlID);
		attributeQuery.setVersion(VersionConstants.saml20);
		attributeQuery.setIssueInstant(CalendarUtils.generateXMLCalendar());
		attributeQuery.setSignature(new Signature());
		attributeQuery.setSubject(subject);
		attributeQuery.setIssuer(issuer);

		Element requestDocument = null;
		try
		{
			requestDocument = this.attributeQueryMarshaller.marshallSignedElement(attributeQuery);
		}
		catch (MarshallerException e)
		{
			this.logger.error(Messages.getString("AttributeProcessorImpl.21") + e.getLocalizedMessage()); //$NON-NLS-1$
		}

		return requestDocument;
	}

	/*
	 * Extract a list of AttributeStatements from the given string representaion of a SAML Response.
	 * 
	 */
	private List<AttributeStatement> getAttributeStatements(Element responseDocument, String expectedSAMLID)
	{
		// Unmarshal the response.
		Response response;
		try
		{
			response = this.responseUnmarshaller.unMarshallSigned(responseDocument);
		}
		catch (SignatureValueException e)
		{
			this.logger.error(Messages.getString("AttributeProcessorImpl.22") + e.getLocalizedMessage()); //$NON-NLS-1$
			return null;
		}
		catch (ReferenceValueException e)
		{
			this.logger.error(Messages.getString("AttributeProcessorImpl.23") + e.getLocalizedMessage()); //$NON-NLS-1$
			return null;
		}
		catch (UnmarshallerException e)
		{
			this.logger.error(Messages.getString("AttributeProcessorImpl.24") + e.getLocalizedMessage()); //$NON-NLS-1$
			return null;
		}

		try
		{
			this.samlValidator.getResponseValidator().validate(response);
			
			String statusCodeValue = response.getStatus().getStatusCode().getValue();
			
			if(!response.getStatus().getStatusCode().getValue().equals(StatusCodeConstants.success))
			{
				this.logger.error("Status code in response to attribute query " + response.getInResponseTo() + " did not indicate success. Status code returned was: " + statusCodeValue);
				return null;
			}
		}
		catch (InvalidSAMLResponseException e)
		{
			this.logger.error(MessageFormat.format(Messages.getString("AttributeProcessorImpl.38"), response.getID(), e.getMessage(), expectedSAMLID)); //$NON-NLS-1$
			return null;
		}

		// verify the issuer was the ESOE
		if (!this.trustedESOEIdentifier.equals(response.getIssuer().getValue()))
		{
			this.logger.error(Messages.getString("AttributeProcessorImpl.39")); //$NON-NLS-1$
			this.logger.debug(MessageFormat.format(Messages.getString("AttributeProcessorImpl.40"), this.trustedESOEIdentifier, response.getIssuer().getValue())); //$NON-NLS-1$
			return null;
		}

		// verify assertions
		for (Object assertionOrEncryptedAssertion : response.getEncryptedAssertionsAndAssertions())
		{
			if (assertionOrEncryptedAssertion instanceof Assertion)
			{
				Assertion assertion = (Assertion) assertionOrEncryptedAssertion;

				// validate SubjectConfirmationData recipient and notOnOrAfter fields
				if (assertion.getSubject() == null)
				{
					this.logger.error(Messages.getString("AttributeProcessorImpl.44")); //$NON-NLS-1$
					return null;
				}

				// validate SubjectConfirmationData recipient and notOnOrAfter fields
				List<SubjectConfirmation> subjectConfirmations = assertion.getSubject().getSubjectConfirmationNonID();

				if (subjectConfirmations.size() == 0)
				{
					this.logger.error(Messages.getString("AttributeProcessorImpl.41")); //$NON-NLS-1$
					return null;
				}

				for (SubjectConfirmation confirmation : subjectConfirmations)
				{
					SubjectConfirmationDataType confirmationData = confirmation.getSubjectConfirmationData();

					if (confirmationData == null)
					{
						this.logger.warn(Messages.getString("AttributeProcessorImpl.42")); //$NON-NLS-1$
						return null;
					}

					// validate data has not expired
					XMLGregorianCalendar xmlCalendar = confirmationData.getNotOnOrAfter();
					GregorianCalendar notOnOrAfterCal = xmlCalendar.toGregorianCalendar();

					XMLGregorianCalendar thisXmlCal = CalendarUtils.generateXMLCalendar();
					GregorianCalendar thisCal = thisXmlCal.toGregorianCalendar();

					if (thisCal.after(notOnOrAfterCal))
					{
						// request is out of date
						this.logger.error(Messages.getString("AttributeProcessorImpl.43")); //$NON-NLS-1$
						return null;
					}
				}
			}
		}

		// Enumerate the AttributeStatements and return them.
		List<AttributeStatement> attributeStatements = new Vector<AttributeStatement>(0, 1);
		for (Object encryptedAssertionOrAssertion : response.getEncryptedAssertionsAndAssertions())
		{
			if (encryptedAssertionOrAssertion instanceof Assertion)
			{
				Assertion assertion = (Assertion) encryptedAssertionOrAssertion;
				for (StatementAbstractType statement : assertion.getAuthnStatementsAndAuthzDecisionStatementsAndAttributeStatements())
				{
					if (statement instanceof AttributeStatement)
					{
						attributeStatements.add((AttributeStatement) statement);
					}
				}
			}
		}

		return attributeStatements;
	}
}
