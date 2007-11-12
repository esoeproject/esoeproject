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
 * Creation Date: 24/11/2006
 * 
 * Purpose: Implements logic to interact with SPEP using Web Browser SSO Post Profile of SAML 2.0 specification.
 */
package com.qut.middleware.esoe.sso.impl;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.w3._2000._09.xmldsig_.Signature;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.crypto.KeyStoreResolver;
import com.qut.middleware.esoe.metadata.Metadata;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.esoe.sessions.bean.IdentityAttribute;
import com.qut.middleware.esoe.sso.bean.SSOProcessorData;
import com.qut.middleware.esoe.sso.exception.InvalidRequestException;
import com.qut.middleware.esoe.util.CalendarUtils;
import com.qut.middleware.saml2.AuthenticationContextConstants;
import com.qut.middleware.saml2.ConfirmationMethodConstants;
import com.qut.middleware.saml2.ConsentIdentifierConstants;
import com.qut.middleware.saml2.ExternalKeyResolver;
import com.qut.middleware.saml2.NameIDFormatConstants;
import com.qut.middleware.saml2.StatusCodeConstants;
import com.qut.middleware.saml2.VersionConstants;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.exception.UnmarshallerException;
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
import com.qut.middleware.saml2.schemas.protocol.Response;
import com.qut.middleware.saml2.schemas.protocol.Status;
import com.qut.middleware.saml2.schemas.protocol.StatusCode;
import com.qut.middleware.saml2.validator.SAMLValidator;

public class AuthenticationAuthorityProcessor extends AuthenticationAuthorityProcessorBase
{

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
	public AuthenticationAuthorityProcessor(SAMLValidator samlValidator, SessionsProcessor sessionsProcessor, Metadata metadata, IdentifierGenerator identifierGenerator, ExternalKeyResolver extKeyResolver, KeyStoreResolver keyStoreResolver, int allowedTimeSkew, int minimalTimeRemaining, boolean acceptUnsignedAuthnRequests, Map<String, String> identifierAttributeMapping) throws UnmarshallerException, MarshallerException
	{
		super(samlValidator, sessionsProcessor, metadata, identifierGenerator, extKeyResolver, keyStoreResolver, allowedTimeSkew, minimalTimeRemaining, acceptUnsignedAuthnRequests, identifierAttributeMapping);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sso.impl.AuthenticationAuthorityProcessorBase#createSucessfulAuthnResponse(com.qut.middleware.esoe.sso.bean.SSOProcessorData,
	 *      com.qut.middleware.esoe.sessions.Principal, java.lang.String, java.lang.String)
	 */
	@Override
	protected void createSucessfulAuthnResponse(SSOProcessorData data, Principal principal, String sessionIndex, String charset) throws InvalidRequestException
	{
		this.logger.info(Messages.getString("AuthenticationAuthorityProcessor.54")); //$NON-NLS-1$

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
		authnStatement.setSessionNotOnOrAfter(principal.getSessionNotOnOrAfter());

		authnStatement.setSubjectLocality(subjectLocality);
		authnStatement.setAuthnContext(authnContext);

		/* Generate Issuer to attach to assertion and response */
		issuer.setValue(this.metadata.getEsoeEntityID());
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
					if (identifier.equals(NameIDFormatConstants.trans))
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

		marshallResponse(data, response, charset);
	}

}
