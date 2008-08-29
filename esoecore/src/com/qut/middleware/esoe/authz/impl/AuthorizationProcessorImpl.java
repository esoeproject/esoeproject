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
 * Author: Andre Zitelli
 * Creation Date: 03/10/2006
 * 
 * Purpose: Implements the AuthorizationProcessor interface. Performs all logic for authorization requests
 * recieved from SPEP's. 
 */

package com.qut.middleware.esoe.authz.impl;

import java.io.UnsupportedEncodingException;
import java.security.PrivateKey;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3._2000._09.xmldsig_.Signature;

import com.qut.middleware.crypto.KeystoreResolver;
import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.authz.AuthorizationProcessor;
import com.qut.middleware.esoe.authz.bean.AuthorizationProcessorData;
import com.qut.middleware.esoe.authz.exception.InvalidRequestException;
import com.qut.middleware.esoe.pdp.processor.DecisionPoint;
import com.qut.middleware.esoe.pdp.processor.impl.DecisionData;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.esoe.sessions.bean.IdentityAttribute;
import com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException;
import com.qut.middleware.esoe.util.CalendarUtils;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.saml2.ConfirmationMethodConstants;
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
import com.qut.middleware.saml2.schemas.assertion.ConditionAbstractType;
import com.qut.middleware.saml2.schemas.assertion.Conditions;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.assertion.StatementAbstractType;
import com.qut.middleware.saml2.schemas.assertion.Subject;
import com.qut.middleware.saml2.schemas.assertion.SubjectConfirmation;
import com.qut.middleware.saml2.schemas.assertion.SubjectConfirmationDataType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.assertion.LXACMLAuthzDecisionStatement;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.DecisionType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Result;
import com.qut.middleware.saml2.schemas.esoe.lxacml.grouptarget.GroupTarget;
import com.qut.middleware.saml2.schemas.esoe.lxacml.protocol.LXACMLAuthzDecisionQuery;
import com.qut.middleware.saml2.schemas.protocol.Response;
import com.qut.middleware.saml2.schemas.protocol.Status;
import com.qut.middleware.saml2.schemas.protocol.StatusCode;
import com.qut.middleware.saml2.validator.SAMLValidator;

public class AuthorizationProcessorImpl implements AuthorizationProcessor
{
	/** The internal name of the string-equal function */
	public static final String FUNCTION_STRING_EQUAL = Messages.getString("AuthorizationProcessorImpl.27"); //$NON-NLS-1$
	/** The internal name of the string-regex function */
	public static final String FUNCTION_STRING_REGEX = Messages.getString("AuthorizationProcessorImpl.3"); //$NON-NLS-1$
	/** The internal name of the string-normalize-space function */
	public static final String FUNCTION_STRING_NORMALIZE_SPACE = Messages.getString("AuthorizationProcessorImpl.4"); //$NON-NLS-1$
	/** The internal name of the string-normalize-to-lower-case function */
	public static final String FUNCTION_STRING_NORMALIZE_TO_LOWER = Messages.getString("AuthorizationProcessorImpl.5"); //$NON-NLS-1$
	/** The internal name of the or function */
	public static final String FUNCTION_OR = Messages.getString("AuthorizationProcessorImpl.6"); //$NON-NLS-1$
	/** The internal name of the and function */
	public static final String FUNCTION_AND = Messages.getString("AuthorizationProcessorImpl.7"); //$NON-NLS-1$
	/** The internal name of the not function */
	public static final String FUNCTION_NOT = Messages.getString("AuthorizationProcessorImpl.8"); //$NON-NLS-1$

	/** Regular expression string for replacement functions */
	private final String REGEX_WHITESPACE_START = "^\\s*"; //$NON-NLS-1$
	private final String REGEX_WHITESPACE_END = "\\s*$"; //$NON-NLS-1$
	private final String REGEX_REPLACE_WITH = ""; //$NON-NLS-1$

	// the cache is a map of policy ID -> Policy objects
	private DecisionPoint pdp;
	private SessionsProcessor sessionProcessor;
	private MetadataProcessor metadata;
	private SAMLValidator samlValidator;
	private Unmarshaller<LXACMLAuthzDecisionQuery> unmarshaller;
	private Marshaller<Response> marshaller;
	private int allowedTimeSkew;

	private String[] schemas = new String[] { SchemaConstants.samlProtocol, SchemaConstants.lxacmlSAMLProtocol, SchemaConstants.lxacmlGroupTarget, SchemaConstants.lxacmlSAMLAssertion, SchemaConstants.samlAssertion };

	private final String UNMAR_PKGNAMES = LXACMLAuthzDecisionQuery.class.getPackage().getName();
	private final String MAR_PKGNAMES = LXACMLAuthzDecisionQuery.class.getPackage().getName() + ":" + //$NON-NLS-1$
	GroupTarget.class.getPackage().getName() + ":" + //$NON-NLS-1$
	StatementAbstractType.class.getPackage().getName() + ":" + //$NON-NLS-1$
	LXACMLAuthzDecisionStatement.class.getPackage().getName() + ":" + //$NON-NLS-1$
	Response.class.getPackage().getName();

	private IdentifierGenerator identifierGenerator;

	Logger logger = LoggerFactory.getLogger(AuthorizationProcessorImpl.class.getName());
	Logger authzLogger = LoggerFactory.getLogger(ConfigurationConstants.authzLogger);
	private String esoeIdentifier;

	/**
	 * @param cache
	 *            The authorization policy cache.
	 * @param sessionProcessor
	 *            The sessions processor used to retrieve user information.
	 * @param metadata
	 *            The metdata object used to retrieve ESOE and PK information.
	 * @param samlValidator
	 *            The SAML validator used to validate requests.
	 * @param identifierGenerator
	 * @param keyStoreResolver
	 *            Used to obtain private/public keys.
	 * @param metadata
	 *            The SPEP processor used to resolve SPEP attributes and external keys.
	 * @param defaultMode
	 *            Either PERMIT or DENY (case sensitive) Any other will default to DENY.
	 * @param allowedTimeSkew
	 *            The time difference to real time that a SAML document will remain active. This value will be added to
	 *            timestamps in Requests/Responses to determine the life of the document.
	 * @throws Exception
	 */
	public AuthorizationProcessorImpl(DecisionPoint pdp, SessionsProcessor sessionProcessor, MetadataProcessor metadata, SAMLValidator samlValidator, IdentifierGenerator identifierGenerator, KeystoreResolver keyStoreResolver,  int allowedTimeSkew, String esoeIdentifier) throws UnmarshallerException, MarshallerException
	{
		if (pdp == null)
			throw new IllegalArgumentException("DecisionPoint can NOT be null."); //$NON-NLS-1$

		if (sessionProcessor == null)
			throw new IllegalArgumentException(Messages.getString("AuthorizationProcessorImpl.1")); //$NON-NLS-1$

		if (samlValidator == null)
			throw new IllegalArgumentException(Messages.getString("AuthorizationProcessorImpl.11")); //$NON-NLS-1$

		if (identifierGenerator == null)
			throw new IllegalArgumentException(Messages.getString("AuthorizationProcessorImpl.63")); //$NON-NLS-1$

		if (keyStoreResolver == null)
			throw new IllegalArgumentException(Messages.getString("AuthorizationProcessorImpl.157")); //$NON-NLS-1$

		if (metadata == null)
			throw new IllegalArgumentException(Messages.getString("AuthorizationProcessorImpl.158")); //$NON-NLS-1$
		
		if (esoeIdentifier == null)
			throw new IllegalArgumentException("ESOE identifier cannot be null");

		if (allowedTimeSkew > Integer.MAX_VALUE / 1000)
		{
			this.allowedTimeSkew = allowedTimeSkew;
		}

		this.sessionProcessor = sessionProcessor;
		this.samlValidator = samlValidator;
		this.pdp = pdp;
		this.identifierGenerator = identifierGenerator;
		this.metadata = metadata;
		this.allowedTimeSkew = allowedTimeSkew;
		this.esoeIdentifier = esoeIdentifier;

		this.unmarshaller = new UnmarshallerImpl<LXACMLAuthzDecisionQuery>(this.UNMAR_PKGNAMES, this.schemas, this.metadata);
		this.marshaller = new MarshallerImpl<Response>(this.MAR_PKGNAMES, this.schemas, keyStoreResolver);

		this.logger.info(Messages.getString("AuthorizationProcessorImpl.64") + pdp.getDefaultMode()); //$NON-NLS-1$
	}

	public result execute(AuthorizationProcessorData authData) throws InvalidRequestException
	{
		if (authData == null)
			throw new IllegalArgumentException("AuthorizationProcessorData must not be null.");

		LXACMLAuthzDecisionQuery authzRequest = null;
		LXACMLAuthzDecisionStatement authzResponse = null;
		Principal principal = null;

		Result authResult = new Result();
		RequestEvaluator requestEval = new RequestEvaluator();
		Response response = null;

		try
		{
			if (authData.getRequestDocument() == null)
			{
				this.logger.warn("Recieved null Request Document . Invalid Request.");
				throw new InvalidSAMLRequestException("Null Request Document Recieved. Invalid Request.");
			}

			authzRequest = this.unmarshaller.unMarshallSigned(authData.getRequestDocument());

			this.samlValidator.getRequestValidator().validate(authzRequest);

			// we will restrict the audience of the response to the SPEP node that sent the request
			String restrictedAudience = authzRequest.getIssuer().getValue();

			// the SAML ID of the request will be used to respond to
			String inResponseTo = authzRequest.getID();

			authData.setIssuerID(requestEval.getEntityID(authzRequest));
			authData.setSubjectID(requestEval.getSubjectID(authzRequest));

			principal = this.sessionProcessor.getQuery().querySAMLSession(authData.getSubjectID());
			String requestedResource = requestEval.getResource(authzRequest);
			String specifiedAction = requestEval.getAction(authzRequest);

			this.authzLogger.info(MessageFormat.format(Messages.getString("AuthorizationProcessorImpl.80"), authData.getIssuerID(), requestedResource, specifiedAction, principal.getPrincipalAuthnIdentifier())); //$NON-NLS-1$ 

			DecisionData decisionData = new DecisionData();
			DecisionType decision = this.pdp.makeAuthzDecision(requestedResource, authData.getIssuerID(),  this.convertIdentityToStrings(principal.getAttributes()), specifiedAction, decisionData );
			
			authResult = this.createGenericResult(decision, decisionData, decisionData.getDecisionMessage());
		
			this.authzLogger.info(MessageFormat.format(Messages.getString("AuthorizationProcessorImpl.84"), authData.getIssuerID(), requestedResource, specifiedAction, principal.getPrincipalAuthnIdentifier(), authResult.getDecision())); //$NON-NLS-1$

			// call external helper to generate the LXACMLAuthzDecisionStatement
			authzResponse = ProtocolTools.generateAuthzDecisionStatement(authzRequest.getRequest(), authResult);

			// build success response with embedded authz return statement
			response = this.buildResponse(StatusCodeConstants.success, authzResponse, authData, restrictedAudience, inResponseTo);
		}
		catch (InvalidSessionIdentifierException e)
		{
			this.logger.warn(Messages.getString("AuthorizationProcessorImpl.85")); //$NON-NLS-1$
			authResult = this.createResult(null, null, Messages.getString("AuthorizationProcessorImpl.14")); //$NON-NLS-1$
			authzResponse = ProtocolTools.generateAuthzDecisionStatement(authzRequest.getRequest(), authResult);
			response = this.buildResponse(StatusCodeConstants.authnFailed, authzResponse, authData, null, null);

			throw new InvalidRequestException(Messages.getString("AuthorizationProcessorImpl.14")); //$NON-NLS-1$
		}
		catch (InvalidSAMLRequestException e)
		{
			this.logger.warn(Messages.getString("AuthorizationProcessorImpl.86")); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);

			authResult = this.createResult(null, null, Messages.getString("AuthorizationProcessorImpl.15")); //$NON-NLS-1$
			authzResponse = ProtocolTools.generateAuthzDecisionStatement(null, authResult);
			response = this.buildResponse(StatusCodeConstants.authnFailed, authzResponse, authData, null, null);

			throw new InvalidRequestException(Messages.getString("AuthorizationProcessorImpl.15")); //$NON-NLS-1$
		}
		catch (SignatureValueException e)
		{
			this.logger.warn(Messages.getString("AuthorizationProcessorImpl.87")); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);

			authResult = this.createResult(null, null, Messages.getString("AuthorizationProcessorImpl.15")); //$NON-NLS-1$
			authzResponse = ProtocolTools.generateAuthzDecisionStatement(null, authResult);
			response = this.buildResponse(StatusCodeConstants.authnFailed, authzResponse, authData, null, null);

			throw new InvalidRequestException(Messages.getString("AuthorizationProcessorImpl.15")); //$NON-NLS-1$

		}
		catch (ReferenceValueException e)
		{
			this.logger.warn(Messages.getString("AuthorizationProcessorImpl.88")); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);

			authResult = this.createResult(null, null, Messages.getString("AuthorizationProcessorImpl.15")); //$NON-NLS-1$
			authzResponse = ProtocolTools.generateAuthzDecisionStatement(null, authResult);
			response = this.buildResponse(StatusCodeConstants.authnFailed, authzResponse, authData, null, null);

			throw new InvalidRequestException(Messages.getString("AuthorizationProcessorImpl.15")); //$NON-NLS-1$
		}
		catch (UnmarshallerException e)
		{
			this.logger.warn(Messages.getString("AuthorizationProcessorImpl.89")); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);

			authResult = this.createResult(null, null, Messages.getString("AuthorizationProcessorImpl.15")); //$NON-NLS-1$
			authzResponse = ProtocolTools.generateAuthzDecisionStatement(null, authResult);
			response = this.buildResponse(StatusCodeConstants.authnFailed, authzResponse, authData, null, null);

			throw new InvalidRequestException(Messages.getString("AuthorizationProcessorImpl.15")); //$NON-NLS-1$

		}
		finally
		{
			// marshall all the response documents and set in auth data bean
			try
			{
				byte[] responseDocument = this.marshaller.marshallSigned(response);

				this.logger.debug(Messages.getString("AuthorizationProcessorImpl.90")); //$NON-NLS-1$

				try
				{
						String responseXml = new String(responseDocument, "UTF-16");
						this.logger.trace(responseXml);
				}
				catch(UnsupportedEncodingException e)
				{
					e.printStackTrace();
				}
				
				authData.setResponseDocument(responseDocument);
			}
			catch (MarshallerException e)
			{
				this.logger.error(Messages.getString("AuthorizationProcessorImpl.65") + e.getMessage()); //$NON-NLS-1$
				this.logger.debug(e.getLocalizedMessage(), e);
			}
		}

		return result.Successful;
	}

	
	private Map<String, List<String>> convertIdentityToStrings(Map<String, IdentityAttribute> principalIdentity)
	{
		Map<String, List<String>> principalAttributes = new HashMap<String, List<String>>();
		
		for (String attribute: principalIdentity.keySet())
		{
			List<Object> identities = principalIdentity.get(attribute).getValues();
			List<String> values = new Vector<String>();
			
			for(Object identity: identities)
			{
				if(identity instanceof String)
				{
					values.add((String)identity);
				}
			}
			
			principalAttributes.put(attribute, values);
		}
		
		return principalAttributes;
	}
	
	
	/*
	 * Create the appropriate result based on the decision supplied. The method will use the data contained in
	 * policyData to construct the result.
	 * 
	 * @param decision the decision to process. If null a default result type will be created. @param policyData The
	 * policy data obtained while processing the auth request. @param messageOverRide A message to override the default
	 * for default results.
	 */
	private Result createResult(DecisionType decision, DecisionData decisionData, String messageOverRide)
	{
		this.logger.debug("Creating new result for given decision " + decision);
		
		PolicyEvaluator eval = new PolicyEvaluator();

		Result result = new Result();
		String message;
		DecisionData localData = decisionData;

		// if a null param for this is passed in we create an empty object, as createDefaultResult requires it
		if (localData == null)
			localData = new DecisionData();

		// no match, fall through to default
		if (decision == null)
		{
			if (messageOverRide == null)
			{
				message = MessageFormat.format(Messages.getString("AuthorizationProcessorImpl.24"), this.pdp.getDefaultMode()); //$NON-NLS-1$
			}
			else
				message = messageOverRide;

			result = eval.createDefaultResult(message, this.pdp.getDefaultMode(), localData);
		}
		// create result on match
		else
			if (decision == DecisionType.PERMIT)
			{
				message = MessageFormat.format(Messages.getString("AuthorizationProcessorImpl.25"), localData.getProcessedPolicies()); //$NON-NLS-1$

				result = eval.createPermitResult(message, localData);
			}
			else
				if (decision == DecisionType.DENY)
				{
					if(localData.getCurrentPolicy() == null)
					{
						result = eval.createDenyResult(localData.getDecisionMessage(), null);
					}
					else
					{
						System.err.println("WOOOOOOT calling get processed pols ..");
						Object[] args = { localData.getCurrentPolicy(), localData.getCurrentRule(), localData.getProcessedPolicies() };
						message = MessageFormat.format(Messages.getString("AuthorizationProcessorImpl.26"), args); //$NON-NLS-1$

						result = eval.createDenyResult(message, localData);
					}
				}

		this.logger.debug("Returning  " + result);
		return result;
	}

	
	/*
	 * Create the appropriate result based on the decision supplied. The method will use the data contained in
	 * policyData to construct the result.
	 * 
	 * @param decision the decision to process. If null a default result type will be created. @param policyData The
	 * policy data obtained while processing the auth request. @param messageOverRide A message to override the default
	 * for default results.
	 */
	private Result createGenericResult(DecisionType decision, DecisionData decisionData, String message)
	{
		this.logger.debug("Creating new result for given decision " + decision);
		
		PolicyEvaluator eval = new PolicyEvaluator();
		DecisionData localData = decisionData;
		DecisionType localDecision = decision;
		
		// if the decision is null then a problem occurred processing the incoming request, return a default Result.
		if(localDecision == null)
			localDecision = ProtocolTools.createDecision(this.pdp.getDefaultMode());
				
		// if a null param for this is passed in we create an empty object, as createDefaultResult requires it
		if (localData == null)
			localData = new DecisionData();
		
		return eval.createResult(localDecision, message, decisionData.getGroupTargets());
	}
	
	
	
	/*
	 * Build a saml <code>Response</code> object to be marshalled and set in the <code>AuthorizationProcessorData</code>
	 * object. The <code>LXACMLAuthzDecisionStatement</code> provided will be embedded in the assertion generated by
	 * this method.
	 * 
	 * @param samlStatusCode The status code to set in the Response. @param authzResponse The authzResponse to embed in
	 * the Response. @param authData The data to use to extract the descriptorID of the requestor. @param
	 * audienceRestriction The nodeID of the SPEP for whom the response is intended. If there was a problem retrieving
	 * this value, set to null.
	 */
	private Response buildResponse(String samlStatusCode, LXACMLAuthzDecisionStatement authzResponse, AuthorizationProcessorData authData, String audienceRestriction, String inResponseTo)
	{
		Response response = new Response();
		response.setVersion(VersionConstants.saml20);

		// generate an ID for the response
		String responseID = this.identifierGenerator.generateSAMLID();

		// Timestamps MUST be set to UTC, no offset
		response.setIssueInstant(CalendarUtils.generateXMLCalendar());
		response.setSignature(new Signature());

		String assertionID = this.identifierGenerator.generateSAMLID();
		Assertion assertion = new Assertion();
		assertion.setIssueInstant(CalendarUtils.generateXMLCalendar());
		assertion.setID(assertionID);
		assertion.setVersion(VersionConstants.saml20);

		Conditions conditions = new Conditions();

		// add allowed time skew to the timestamp
		conditions.setNotOnOrAfter(CalendarUtils.generateXMLCalendar(this.allowedTimeSkew));

		// set audience restriction to SPEP receiving the response
		List<ConditionAbstractType> audienceRestrictions = conditions.getConditionsAndOneTimeUsesAndAudienceRestrictions();
		AudienceRestriction restrict = new AudienceRestriction();

		// we cant set this if the recieved request failed validation
		if (audienceRestriction != null)
		{
			restrict.getAudiences().add(audienceRestriction);
			audienceRestrictions.add(restrict);
		}

		assertion.setConditions(conditions);

		// set the subject content
		Subject subject = new Subject();
		NameIDType nameID = new NameIDType();
		nameID.setFormat(NameIDFormatConstants.trans);
		nameID.setValue(authData.getIssuerID());
		subject.setNameID(nameID);

		/* subject MUST contain a SubjectConfirmation */
		SubjectConfirmation confirmation = new SubjectConfirmation();
		confirmation.setMethod(ConfirmationMethodConstants.bearer);
		SubjectConfirmationDataType confirmationData = new SubjectConfirmationDataType();
		confirmationData.setInResponseTo(inResponseTo);
		confirmationData.setNotOnOrAfter(CalendarUtils.generateXMLCalendar(this.allowedTimeSkew));
		confirmation.setSubjectConfirmationData(confirmationData);
		subject.getSubjectConfirmationNonID().add(confirmation);

		assertion.setSubject(subject);

		NameIDType issuer = new NameIDType();
		issuer.setValue(this.esoeIdentifier);

		// set assertions and response issuer ID
		assertion.setIssuer(issuer);
		response.setIssuer(issuer);

		// add our authz decision statement
		assertion.getAuthnStatementsAndAuthzDecisionStatementsAndAttributeStatements().add(authzResponse);

		response.getEncryptedAssertionsAndAssertions().add(assertion);

		Status status = new Status();
		StatusCode statusCode = new StatusCode();
		statusCode.setValue(samlStatusCode);
		status.setStatusCode(statusCode);

		response.setStatus(status);
		response.setID(responseID);

		// we are responding to the initial request, so set the SAML ID
		if (inResponseTo != null)
		{
			response.setInResponseTo(inResponseTo);
		}

		return response;

	}


}
