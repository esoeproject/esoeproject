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

package com.qut.middleware.esoe.pdp.impl;

import java.security.PrivateKey;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.PatternSyntaxException;

import javax.xml.bind.JAXBElement;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3._2000._09.xmldsig_.Signature;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.crypto.KeyStoreResolver;
import com.qut.middleware.esoe.log4j.AuthzLogLevel;
import com.qut.middleware.esoe.metadata.Metadata;
import com.qut.middleware.esoe.pdp.AuthorizationProcessor;
import com.qut.middleware.esoe.pdp.bean.AuthorizationProcessorData;
import com.qut.middleware.esoe.pdp.cache.bean.AuthzPolicyCache;
import com.qut.middleware.esoe.pdp.exception.InvalidRequestException;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.esoe.sessions.bean.IdentityAttribute;
import com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException;
import com.qut.middleware.esoe.util.CalendarUtils;
import com.qut.middleware.saml2.ConfirmationMethodConstants;
import com.qut.middleware.saml2.NameIDFormatConstants;
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
import com.qut.middleware.saml2.schemas.esoe.lxacml.ApplyType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.AttributeValueType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.ConditionType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Policy;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Rule;
import com.qut.middleware.saml2.schemas.esoe.lxacml.SubjectAttributeDesignatorType;
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

	protected EvaluateStringEqualExpression strEqualEval;
	protected EvaluateStringRegexExpression strRegexEval;
	protected EvaluateOrExpression orEval;
	protected EvaluateAndExpression andEval;
	protected EvaluateNotExpression notEval;

	// the cache is a map of policy ID -> Policy objects
	private AuthzPolicyCache globalCache;
	private SessionsProcessor sessionProcessor;
	private Metadata metadata;
	private SAMLValidator samlValidator;
	private Unmarshaller<LXACMLAuthzDecisionQuery> unmarshaller;
	private Marshaller<Response> marshaller;
	private PrivateKey privKey;
	private String keyName;
	private int allowedTimeSkew;

	private String[] schemas = new String[] { ConfigurationConstants.samlProtocol,
			ConfigurationConstants.lxacmlSAMLProtocol, ConfigurationConstants.lxacmlGroupTarget,
			ConfigurationConstants.lxacmlSAMLAssertion, ConfigurationConstants.samlAssertion };

	private final String UNMAR_PKGNAMES = LXACMLAuthzDecisionQuery.class.getPackage().getName();
	private final String MAR_PKGNAMES = LXACMLAuthzDecisionQuery.class.getPackage().getName() + ":" + //$NON-NLS-1$
			GroupTarget.class.getPackage().getName() + ":" + //$NON-NLS-1$
			StatementAbstractType.class.getPackage().getName() + ":" + //$NON-NLS-1$
			LXACMLAuthzDecisionStatement.class.getPackage().getName() + ":" + //$NON-NLS-1$
			Response.class.getPackage().getName();

	private String defaultMode;
	private IdentifierGenerator identifierGenerator;

	Logger logger = Logger.getLogger(AuthorizationProcessorImpl.class.getName());

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
	public AuthorizationProcessorImpl(AuthzPolicyCache cache, SessionsProcessor sessionProcessor, Metadata metadata,
			SAMLValidator samlValidator, IdentifierGenerator identifierGenerator, KeyStoreResolver keyStoreResolver,
			String defaultMode, int allowedTimeSkew) throws UnmarshallerException, MarshallerException
	{
		if (cache == null)
			throw new IllegalArgumentException(Messages.getString("AuthorizationProcessorImpl.0")); //$NON-NLS-1$

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

		if (allowedTimeSkew > Integer.MAX_VALUE / 1000)
		{
			this.allowedTimeSkew = allowedTimeSkew;
		}

		// incorrect parameter defaults the policy to DENY (might want a log breakpoint here)
		if (defaultMode == null || !defaultMode.equals(ProtocolTools.PERMIT) || !defaultMode.equals(ProtocolTools.DENY))
			this.defaultMode = ProtocolTools.DENY;
		else
			this.defaultMode = defaultMode;

		this.sessionProcessor = sessionProcessor;
		this.samlValidator = samlValidator;
		this.globalCache = cache;
		this.privKey = keyStoreResolver.getPrivateKey();
		this.keyName = keyStoreResolver.getKeyAlias();
		this.identifierGenerator = identifierGenerator;
		this.metadata = metadata;
		this.allowedTimeSkew = allowedTimeSkew;

		this.unmarshaller = new UnmarshallerImpl<LXACMLAuthzDecisionQuery>(this.UNMAR_PKGNAMES, this.schemas,
				this.metadata);
		this.marshaller = new MarshallerImpl<Response>(this.MAR_PKGNAMES, this.schemas, this.keyName, this.privKey);

		this.strEqualEval = new EvaluateStringEqualExpression();
		this.strRegexEval = new EvaluateStringRegexExpression();
		this.orEval = new EvaluateOrExpression();
		this.andEval = new EvaluateAndExpression();
		this.notEval = new EvaluateNotExpression();

		this.logger.log(Level.INFO, Messages.getString("AuthorizationProcessorImpl.64") + this.defaultMode); //$NON-NLS-1$
	}

	public result execute(AuthorizationProcessorData authData) throws InvalidRequestException
	{
		// DEFAULT override. If there are no policies in the cache then we DENY all.
		if (this.globalCache.getSize() == 0)
		{
			this.logger.log(AuthzLogLevel.Authz, Messages.getString("AuthorizationProcessorImpl.79")); //$NON-NLS-1$
			this.defaultMode = ProtocolTools.DENY;
		}

		LXACMLAuthzDecisionQuery authzRequest = null;
		LXACMLAuthzDecisionStatement authzResponse = null;
		Principal principal = null;

		Result authResult = new Result();
		RequestEvaluator eval = new RequestEvaluator();
		Response response = null;

		try
		{
			authzRequest = this.unmarshaller.unMarshallSigned(authData.getRequestDocument());

			this.samlValidator.getRequestValidator().validate(authzRequest);

			// we will restrict the audience of the response to the SPEP node that sent the request
			String restrictedAudience = authzRequest.getIssuer().getValue();

			// the SAML ID of the request will be used to respond to
			String inResponseTo = authzRequest.getID();

			authData.setDescriptorID(eval.getDescriptorID(authzRequest));
			authData.setSubjectID(eval.getSubjectID(authzRequest));

			principal = this.sessionProcessor.getQuery().querySAMLSession(authData.getSubjectID());
			String requestedResource = new RequestEvaluator().getResource(authzRequest);

			this.logger.log(AuthzLogLevel.Authz, MessageFormat.format(Messages
					.getString("AuthorizationProcessorImpl.80"), authData.getDescriptorID(), requestedResource)); //$NON-NLS-1$ 

			// retrieve policy set associated with SPEP
			Vector<Policy> policies = this.queryCache(authData.getDescriptorID());

			if (policies == null)
			{
				this.logger.log(Level.DEBUG, Messages.getString("AuthorizationProcessorImpl.82")); //$NON-NLS-1$
				authResult = this.createResult(null, null,
						"No matching policy located. Falling through to default state of " + this.defaultMode); //$NON-NLS-1$
			}
			else
			// process auth request against policies
			{
				this.logger.log(Level.DEBUG, Messages.getString("AuthorizationProcessorImpl.83")); //$NON-NLS-1$
				authResult = this.evaluatePolicyRequest(policies, requestedResource, principal);
			}

			this.logger
					.log(Level.DEBUG, Messages.getString("AuthorizationProcessorImpl.84") + authResult.getDecision()); //$NON-NLS-1$

			// call external helper to generate the LXACMLAuthzDecisionStatement
			authzResponse = ProtocolTools.generateAuthzDecisionStatement(authzRequest.getRequest(), authResult);

			// build success response with embedded authz return statement
			response = this.buildResponse(StatusCodeConstants.success, authzResponse, authData, restrictedAudience,
					inResponseTo);
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
			this.logger.log(Level.DEBUG, e.getLocalizedMessage(), e);

			authResult = this.createResult(null, null, Messages.getString("AuthorizationProcessorImpl.15")); //$NON-NLS-1$
			authzResponse = ProtocolTools.generateAuthzDecisionStatement(null, authResult);
			response = this.buildResponse(StatusCodeConstants.authnFailed, authzResponse, authData, null, null);

			throw new InvalidRequestException(Messages.getString("AuthorizationProcessorImpl.15")); //$NON-NLS-1$
		}
		catch (SignatureValueException e)
		{
			this.logger.warn(Messages.getString("AuthorizationProcessorImpl.87")); //$NON-NLS-1$
			this.logger.log(Level.DEBUG, e.getLocalizedMessage(), e);

			authResult = this.createResult(null, null, Messages.getString("AuthorizationProcessorImpl.15")); //$NON-NLS-1$
			authzResponse = ProtocolTools.generateAuthzDecisionStatement(null, authResult);
			response = this.buildResponse(StatusCodeConstants.authnFailed, authzResponse, authData, null, null);

			throw new InvalidRequestException(Messages.getString("AuthorizationProcessorImpl.15")); //$NON-NLS-1$

		}
		catch (ReferenceValueException e)
		{
			this.logger.warn(Messages.getString("AuthorizationProcessorImpl.88")); //$NON-NLS-1$
			this.logger.log(Level.DEBUG, e.getLocalizedMessage(), e);

			authResult = this.createResult(null, null, Messages.getString("AuthorizationProcessorImpl.15")); //$NON-NLS-1$
			authzResponse = ProtocolTools.generateAuthzDecisionStatement(null, authResult);
			response = this.buildResponse(StatusCodeConstants.authnFailed, authzResponse, authData, null, null);

			throw new InvalidRequestException(Messages.getString("AuthorizationProcessorImpl.15")); //$NON-NLS-1$
		}
		catch (UnmarshallerException e)
		{
			this.logger.warn(Messages.getString("AuthorizationProcessorImpl.89")); //$NON-NLS-1$
			this.logger.log(Level.DEBUG, e.getLocalizedMessage(), e);

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
				String responseDocument = this.marshaller.marshallSigned(response);

				this.logger.log(Level.DEBUG, Messages.getString("AuthorizationProcessorImpl.90")); //$NON-NLS-1$

				authData.setResponseDocument(responseDocument);
			}
			catch (MarshallerException e)
			{
				this.logger.error(Messages.getString("AuthorizationProcessorImpl.65") + e.getMessage()); //$NON-NLS-1$
				this.logger.log(Level.DEBUG, e.getLocalizedMessage(), e);
			}
		}

		return result.Successful;
	}

	/*
	 * Retrieve a Policy object from the AuthzPolicyCache.
	 * 
	 */
	private Vector<Policy> queryCache(String descriptorID)
	{
		this.logger.log(Level.DEBUG, Messages.getString("AuthorizationProcessorImpl.91")); //$NON-NLS-1$

		return (Vector<Policy>) this.globalCache.getPolicies(descriptorID);
	}

	/*
	 * Evaluate the given resource request against the rules retrieved from the working policy and the current user
	 * session. NOTE: This function assumes that the policy object retrieved has been validated against the
	 * lxacmlSchema.xsd to contain only valid xml.
	 * 
	 * @param policy The authorization policy associated with the given SPEP. @param resource The target resource as
	 * requested by the SPEP. This is the resource given to the authorization processor in the <code>LXACMLAuthzDecisionQuery<code>.
	 * @param principal The principal associated with the auth request @return the Result representing the outcome of
	 * the request processing.
	 * 
	 */
	private Result evaluatePolicyRequest(Vector<Policy> policies, String resource, Principal principal)
	{
		Result result = null;
		DecisionType currentDecision = null;
		PolicyData policyData = new PolicyData();

		// for each policy, see if any targets match the resource request
		Iterator<Policy> policyIter = policies.iterator();

		// we'll want to break out of loops on deny
		boolean continueProcessing = true;

		while (policyIter.hasNext())
		{
			if (!continueProcessing)
				break;

			Policy policy = policyIter.next();

			// retrieve a list of all resources strings in the policy
			List<String> policyResources = PolicyEvaluator.getPolicyTargetResources(policy);

			// add current policy to list of processed policies
			policyData.addProcessedPolicy(policy.getPolicyId());

			Iterator<String> resIter = policyResources.iterator();

			while (resIter.hasNext())
			{
				if (!continueProcessing)
					break;

				String policyResource = resIter.next();

				this.logger.log(Level.DEBUG, Messages.getString("AuthorizationProcessorImpl.92") + policyResource); //$NON-NLS-1$

				// match the requested resource against policy derived resources
				if (resource.equals(policyResource) || resource.matches(policyResource))
				{
					this.logger.log(Level.DEBUG, Messages.getString("AuthorizationProcessorImpl.93")); //$NON-NLS-1$

					Iterator<Rule> rulesIter = policy.getRules().iterator();

					while (rulesIter.hasNext())
					{
						if (!continueProcessing)
							break;

						Rule currentRule = rulesIter.next();

						// get all resource targets of the rule and check for a match
						List<String> targetResources = PolicyEvaluator.getRuleTargetResources(currentRule);

						// if the rule has no specified targets, use the policy targets
						if (targetResources == null)
						{
							this.logger.debug(Messages.getString("AuthorizationProcessorImpl.159")); //$NON-NLS-1$
							targetResources = policyResources;
						}
						
						Iterator<String> ruleResources = targetResources.iterator();

						while (ruleResources.hasNext())
						{
							if (!continueProcessing)
								break;

							String ruleResource = ruleResources.next();

							if (resource.equals(ruleResource) || resource.matches(ruleResource))
							{
								policyData.addProcessedRule(currentRule.getRuleId().toString());

								DecisionType newDecision = this.processRule(currentRule, resource, principal);

								// null return indicates the a condition contained in the rule evaluated to false. We must ignore
								// this rule. ie: do not process this Rule any longer.
								if(newDecision == null)
									break;
								
								// end processing if we hit a deny
								if (newDecision == DecisionType.DENY)
								{
									this.logger.log(Level.DEBUG, Messages.getString("AuthorizationProcessorImpl.94")); //$NON-NLS-1$
									
									currentDecision = DecisionType.DENY;
									
									// we also only want to send the deny group target and authz target that matched the
									// requested resource, so we need to clear and reset these values in policy data bean
									policyData.clearTargets();
									continueProcessing = false;
								}
								else if(newDecision == DecisionType.PERMIT)
								{
									this.logger.log(Level.DEBUG, "Permit decision returned. Continuing processing .."); //$NON-NLS-1$
									
									currentDecision = DecisionType.PERMIT;
								}									
								
								// add the policy target match and authz match to data object
								policyData.addGroupTarget(policyResource);
								policyData.addMatch(ruleResource);

							}
						}
					}
				}
				else
					this.logger.log(Level.DEBUG, Messages.getString("AuthorizationProcessorImpl.95")); //$NON-NLS-1$

			}
		}

		result = this.createResult(currentDecision, policyData, null);

		return result;
	}

	/*
	 * Evaluate and process the given rule to determine the outcome of the resource request.
	 * 
	 * @param rule The Rule to evaluate.
	 * @param principal The Principal object that contains information to match against any conditions
	 * contained in the given Rule.
	 * @return A DecisionType representing the outcome of the Rule evaluation if one can be made. If a condition
	 * contained in the given rule evaluates to False, a decision can not be made based on the Effect of the Rule (because
	 * the condition does not match) and null is returned. 
	 */
	@SuppressWarnings("unchecked")  //$NON-NLS-1$
	private DecisionType processRule(Rule rule, String requestedResource, Principal principal)
	{
		boolean conditionMatches = true;
		String effect = new String();
		ConditionType cond = rule.getCondition();

		this.logger
				.log(
						Level.DEBUG,
						Messages.getString("AuthorizationProcessorImpl.97") + rule.getRuleId() + Messages.getString("AuthorizationProcessorImpl.98") + rule.getEffect()); //$NON-NLS-1$ //$NON-NLS-2$

		// if a condition exists, evaluate its expression
		if (cond != null)
		{
			JAXBElement<ApplyType> expression = (JAXBElement<ApplyType>) cond.getExpression();

			// condition can only hold an apply element so this shouldnt be necessary, but ...
			if (expression != null)
			{
				this.logger.log(Level.DEBUG, Messages.getString("AuthorizationProcessorImpl.99")); //$NON-NLS-1$

				// root level apply element (condition can only have 1 apply element)
				JAXBElement<ApplyType> apply = expression;

				// we don't want InvalidParameter runtime exceptions to halt the auth process, so we'll deal
				// with them here. Invalid parameters in a Rule = DENY that target.
				try
				{
					conditionMatches = this.processExpressions(apply, principal);
				}
				catch (IllegalArgumentException e)
				{
					this.logger.warn(Messages.getString("AuthorizationProcessorImpl.138") + e.getMessage()); //$NON-NLS-1$
					conditionMatches = false;
				}
			}
		}
		else
			this.logger.log(Level.DEBUG, Messages.getString("AuthorizationProcessorImpl.100")); //$NON-NLS-1$

		// The condition is the return value of any processed expression.
		// If the condition matches, we apply the effect of the rule, else we negate it
		if (!conditionMatches)
			this.logger.log(Level.DEBUG, Messages.getString("AuthorizationProcessorImpl.101")); //$NON-NLS-1$

		effect = rule.getEffect().toString();
		if (conditionMatches && effect.equalsIgnoreCase(ProtocolTools.PERMIT))
			return DecisionType.PERMIT;
		else if (conditionMatches && effect.equalsIgnoreCase(ProtocolTools.DENY))
			return DecisionType.DENY;
		else
			// in this case, the condition did not match so the Effect must be ignored.
			return null;
		}

	/*
	 * Evaluates all expresssions contained in the given apply type element and returns a boolean value based on the
	 * outcome of the processing. See specification for valid expressions.
	 * 
	 * @param rootNode The root apply type of the condition being processed. @param principal The principal associated
	 * with the request. @return boolean statement representing outcome of evaluated expression
	 */
	private boolean processExpressions(JAXBElement<ApplyType> rootNode, Principal principal)
	{
		String function = null;

		if (rootNode == null || (rootNode.getDeclaredType() != ApplyType.class))
			throw new IllegalArgumentException(Messages.getString("AuthorizationProcessorImpl.22")); //$NON-NLS-1$

		if (principal == null)
			throw new IllegalArgumentException(Messages.getString("AuthorizationProcessorImpl.22")); //$NON-NLS-1$

		JAXBElement<ApplyType> apply = rootNode;

		function = apply.getValue().getFunctionId();

		if (function == null)
			throw new IllegalArgumentException(Messages.getString("AuthorizationProcessorImpl.23")); //$NON-NLS-1$

		if (function.equals(FUNCTION_OR))
			return this.orEval.execute(rootNode, principal);
		else
			if (function.equals(FUNCTION_NOT))
				return this.notEval.execute(rootNode, principal);
			else
				if (function.equals(FUNCTION_AND))
					return this.andEval.execute(rootNode, principal);
				else
					if (function.equals(FUNCTION_STRING_REGEX))
						return this.strRegexEval.execute(rootNode, principal);
					else
						if (function.equals(FUNCTION_STRING_EQUAL))
							return this.strEqualEval.execute(rootNode, principal);

		return false;

	}

	/*
	 * Create the appropriate result based on the decision supplied. The method will use the data contained in
	 * policyData to construct the result.
	 * 
	 * @param decision the decision to process. If null a default result type will be created. @param policyData The
	 * policy data obtained while processing the auth request. @param messageOverRide A message to override the default
	 * for default results.
	 */
	private Result createResult(DecisionType decision, PolicyData policyData, String messageOverRide)
	{
		PolicyEvaluator eval = new PolicyEvaluator();

		Result result = new Result();
		String message;
		PolicyData localPolicyData = policyData;

		// if a null param for this is passed in we create an empty object, as createDefaultResult requires it
		if (localPolicyData == null)
			localPolicyData = new PolicyData();

		// no match, fall through to default
		if (decision == null)
		{
			if (messageOverRide == null)
			{
				message = MessageFormat.format(Messages.getString("AuthorizationProcessorImpl.24"), this.defaultMode); //$NON-NLS-1$
			}
			else
				message = messageOverRide;

			result = eval.createDefaultResult(message, this.defaultMode, localPolicyData);
		}
		// create result on match
		else
			if (decision == DecisionType.PERMIT)
			{
				message = MessageFormat.format(
						Messages.getString("AuthorizationProcessorImpl.25"), localPolicyData.getProcessedPolicies()); //$NON-NLS-1$

				result = eval.createPermitResult(message, localPolicyData);
			}
			else
				if (decision == DecisionType.DENY)
				{
					Object[] args = { localPolicyData.getCurrentPolicy(), localPolicyData.getCurrentRule(),
							localPolicyData.getProcessedRules(), localPolicyData.getProcessedPolicies() };
					message = MessageFormat.format(Messages.getString("AuthorizationProcessorImpl.26"), args); //$NON-NLS-1$

					result = eval.createDenyResult(message, localPolicyData);
				}

		return result;
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
	private Response buildResponse(String samlStatusCode, LXACMLAuthzDecisionStatement authzResponse,
			AuthorizationProcessorData authData, String audienceRestriction, String inResponseTo)
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
		List<ConditionAbstractType> audienceRestrictions = conditions
				.getConditionsAndOneTimeUsesAndAudienceRestrictions();
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
		nameID.setValue(authData.getDescriptorID());
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
		issuer.setValue(this.metadata.getESOEIdentifier());

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

	/*
	 * Inner classes for evaluating expressions based on function type.
	 * 
	 */

	protected class EvaluateOrExpression
	{
		/**
		 * Executes the "or" operation on the node.
		 * 
		 * @pre node != null && principal != null
		 * @param node
		 *            The node to apply this operation to.
		 * @param principal
		 *            The associated principal to use when evaluating.
		 * @return The result of this operation.
		 */
		@SuppressWarnings("unchecked")  //$NON-NLS-1$
		public boolean execute(JAXBElement<ApplyType> node, Principal principal)
		{
			String function = null;

			if (node.getDeclaredType() != ApplyType.class)
				throw new IllegalArgumentException(Messages.getString("AuthorizationProcessorImpl.31")); //$NON-NLS-1$

			JAXBElement<ApplyType> apply = node;

			function = apply.getValue().getFunctionId();

			if (function == null || !function.equals(FUNCTION_OR))
				throw new IllegalArgumentException(Messages.getString("AuthorizationProcessorImpl.32")); //$NON-NLS-1$

			// process children as arguments to this function
			Iterator<JAXBElement<?>> iter = apply.getValue().getExpressions().iterator();
			while (iter.hasNext())
			{
				JAXBElement child = iter.next();

				// or function can only hold other apply types
				if (child.getDeclaredType() != ApplyType.class)
				{
					return false;
				}
				JAXBElement<ApplyType> childFunction = child;
				String childFunctionID = childFunction.getValue().getFunctionId();

				boolean result = false;

				if (childFunctionID.equals(FUNCTION_OR))
					result = AuthorizationProcessorImpl.this.orEval.execute(child, principal);
				else
					if (childFunctionID.equals(FUNCTION_NOT))
						result = AuthorizationProcessorImpl.this.notEval.execute(child, principal);
					else
						if (childFunctionID.equals(FUNCTION_AND))
							result = AuthorizationProcessorImpl.this.andEval.execute(child, principal);
						else
							if (childFunctionID.equals(FUNCTION_STRING_REGEX))
								result = AuthorizationProcessorImpl.this.strRegexEval.execute(child, principal);
							else
								if (childFunctionID.equals(FUNCTION_STRING_EQUAL))
									result = AuthorizationProcessorImpl.this.strEqualEval.execute(child, principal);

				// if any returns evaluate to true, the OR is successfull
				if (result)
				{
					AuthorizationProcessorImpl.this.logger
							.log(
									Level.DEBUG,
									Messages.getString("AuthorizationProcessorImpl.104") + FUNCTION_OR + Messages.getString("AuthorizationProcessorImpl.105")); //$NON-NLS-1$ //$NON-NLS-2$
					return true;
				}
			}
			AuthorizationProcessorImpl.this.logger
					.debug(Messages.getString("AuthorizationProcessorImpl.106") + FUNCTION_OR + Messages.getString("AuthorizationProcessorImpl.107")); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
	}

	protected class EvaluateAndExpression
	{
		/**
		 * Executes the "and" operation on the node
		 * 
		 * @param node
		 *            The node to apply this operation to
		 * @param principal
		 *            The associated principal to use when evaluating
		 * @return The result of this operation
		 */
		@SuppressWarnings("unchecked")  //$NON-NLS-1$
		public boolean execute(JAXBElement<ApplyType> node, Principal principal)
		{
			String function = null;

			if (node.getDeclaredType() != ApplyType.class)
			{
				throw new IllegalArgumentException(Messages.getString("AuthorizationProcessorImpl.36")); //$NON-NLS-1$
			}

			JAXBElement<ApplyType> apply = node;

			function = apply.getValue().getFunctionId();

			if (function == null || !function.equals(FUNCTION_AND))
				throw new IllegalArgumentException(Messages.getString("AuthorizationProcessorImpl.37")); //$NON-NLS-1$

			// process children as arguments to this function
			Iterator<JAXBElement<?>> iter = apply.getValue().getExpressions().iterator();
			while (iter.hasNext())
			{
				JAXBElement child = iter.next();

				// or function can only hold other apply types
				if (child.getDeclaredType() != ApplyType.class)
				{
					return false;
				}

				JAXBElement<ApplyType> childFunction = child;
				String childFunctionID = childFunction.getValue().getFunctionId();

				boolean result = false;

				if (childFunctionID.equals(FUNCTION_OR))
					result = AuthorizationProcessorImpl.this.orEval.execute(child, principal);
				else
					if (childFunctionID.equals(FUNCTION_NOT))
						result = AuthorizationProcessorImpl.this.notEval.execute(child, principal);
					else
						if (childFunctionID.equals(FUNCTION_AND))
							result = AuthorizationProcessorImpl.this.andEval.execute(child, principal);
						else
							if (childFunctionID.equals(FUNCTION_STRING_REGEX))
								result = AuthorizationProcessorImpl.this.strRegexEval.execute(child, principal);
							else
								if (childFunctionID.equals(FUNCTION_STRING_EQUAL))
									result = AuthorizationProcessorImpl.this.strEqualEval.execute(child, principal);

				// if any returns evaluate to false, no dice
				if (!result)
				{
					AuthorizationProcessorImpl.this.logger
							.log(
									Level.DEBUG,
									Messages.getString("AuthorizationProcessorImpl.110") + FUNCTION_AND + Messages.getString("AuthorizationProcessorImpl.111")); //$NON-NLS-1$ //$NON-NLS-2$
					return false;
				}
			}

			AuthorizationProcessorImpl.this.logger
					.debug(Messages.getString("AuthorizationProcessorImpl.112") + FUNCTION_AND + Messages.getString("AuthorizationProcessorImpl.113")); //$NON-NLS-1$ //$NON-NLS-2$
			return true;
		}
	}

	protected class EvaluateNotExpression
	{
		/**
		 * Executes the "not" operation on the node
		 * 
		 * @param node
		 *            The node to apply this operation to
		 * @param principal
		 *            The associated principal to use when evaluating
		 * @return The result of this operation
		 */
		@SuppressWarnings("unchecked")  //$NON-NLS-1$
		public boolean execute(JAXBElement<ApplyType> node, Principal principal)
		{
			String function = null;

			if (node.getDeclaredType() != ApplyType.class)
			{
				throw new IllegalArgumentException(Messages.getString("AuthorizationProcessorImpl.41")); //$NON-NLS-1$
			}
			JAXBElement<ApplyType> apply = node;

			function = apply.getValue().getFunctionId();

			if (function == null || !function.equals(FUNCTION_NOT))
				throw new IllegalArgumentException(Messages.getString("AuthorizationProcessorImpl.42")); //$NON-NLS-1$

			// process children as arguments to this function
			Iterator<JAXBElement<?>> iter = apply.getValue().getExpressions().iterator();
			while (iter.hasNext())
			{
				JAXBElement child = iter.next();

				// or function can only hold other apply types
				if (child.getDeclaredType() != ApplyType.class)
				{
					return false;
				}
				JAXBElement<ApplyType> childFunction = child;
				String childFunctionID = childFunction.getValue().getFunctionId();
				boolean result = false;

				if (childFunctionID.equals(FUNCTION_OR))
					result = AuthorizationProcessorImpl.this.orEval.execute(child, principal);
				else
					if (childFunctionID.equals(FUNCTION_NOT))
						result = AuthorizationProcessorImpl.this.notEval.execute(child, principal);
					else
						if (childFunctionID.equals(FUNCTION_AND))
							result = AuthorizationProcessorImpl.this.andEval.execute(child, principal);
						else
							if (childFunctionID.equals(FUNCTION_STRING_REGEX))
								result = AuthorizationProcessorImpl.this.strRegexEval.execute(child, principal);
							else
								if (childFunctionID.equals(FUNCTION_STRING_EQUAL))
									result = AuthorizationProcessorImpl.this.strEqualEval.execute(child, principal);

				// if any returns evaluate to true, no dice
				if (result)
				{
					AuthorizationProcessorImpl.this.logger
							.log(
									Level.DEBUG,
									Messages.getString("AuthorizationProcessorImpl.116") + FUNCTION_NOT + Messages.getString("AuthorizationProcessorImpl.117")); //$NON-NLS-1$ //$NON-NLS-2$
					return false;
				}
			}

			AuthorizationProcessorImpl.this.logger
					.debug(Messages.getString("AuthorizationProcessorImpl.118") + FUNCTION_NOT + Messages.getString("AuthorizationProcessorImpl.119")); //$NON-NLS-1$ //$NON-NLS-2$
			return true;
		}
	}

	protected class EvaluateStringEqualExpression
	{
		/**
		 * Executes the String equal operation on the node
		 * 
		 * @param node
		 *            The node to apply this operation to
		 * @param principal
		 *            The associated principal to use when evaluating
		 * @return The result of this operation
		 */
		@SuppressWarnings("unchecked")  //$NON-NLS-1$
		public boolean execute(JAXBElement<ApplyType> node, Principal principal)
		{
			String logMessage = Messages.getString("AuthorizationProcessorImpl.122"); //$NON-NLS-1$
			String function = null;
			boolean toLower = false;
			boolean normalizeSpaces = false;

			// content of AttributeValue elements from the policy
			List<String> attributeValues = new Vector<String>();
			// list of subject designators from the policy, used to match identity attributes of principal
			List<String> subjectDesignatorAttributes = new Vector<String>();

			if (node == null || node.getDeclaredType() != ApplyType.class)
				throw new IllegalArgumentException(Messages.getString("AuthorizationProcessorImpl.46")); //$NON-NLS-1$

			JAXBElement<ApplyType> apply = node;
			function = apply.getValue().getFunctionId();

			if (function == null || !function.equals(FUNCTION_STRING_EQUAL))
				throw new IllegalArgumentException(Messages.getString("AuthorizationProcessorImpl.47")); //$NON-NLS-1$

			// process children as arguments to this function
			Iterator<JAXBElement<?>> iter = apply.getValue().getExpressions().iterator();
			while (iter.hasNext())
			{
				JAXBElement child = iter.next();

				// add subject attribute designators
				if (child.getDeclaredType() == SubjectAttributeDesignatorType.class)
				{
					// set the attributes of the subject that we will use to match in the principal
					JAXBElement<SubjectAttributeDesignatorType> subj = child;
					subjectDesignatorAttributes.add(subj.getValue().getAttributeId());
				}
				else
					if (child.getDeclaredType() == AttributeValueType.class)
					{
						// Contains the actual parameters of the function
						JAXBElement<AttributeValueType> attr = child;

						List content = attr.getValue().getContent();
						Iterator contentIter = content.iterator();
						while (contentIter.hasNext())
						{
							String value = contentIter.next().toString();
							attributeValues.add(value);
						}
					}
					// we can have an apply type, ONLY if its a normalize function
					else
						if (child.getDeclaredType() == ApplyType.class)
						{
							JAXBElement<ApplyType> normalizeFunction = child;
							String childFunction = normalizeFunction.getValue().getFunctionId();

							if (childFunction.equals(FUNCTION_STRING_NORMALIZE_TO_LOWER))
								toLower = true;
							else
								if (childFunction.equals(FUNCTION_STRING_NORMALIZE_SPACE))
									normalizeSpaces = true;
								else
									throw new IllegalArgumentException(Messages
											.getString("AuthorizationProcessorImpl.156") + childFunction); //$NON-NLS-1$
						}

			}

			// for each policy specified subject designator, retrieve any matching identity
			// attributes from the principal.
			List<IdentityAttribute> matchingIdentityAttributes = new Vector<IdentityAttribute>();
			Iterator subjDesignatorIter = subjectDesignatorAttributes.iterator();
			while (subjDesignatorIter.hasNext())
			{
				// make sure the attribute exists, if so add
				Map<String, IdentityAttribute> identityAttributes = principal.getAttributes();
				if (identityAttributes != null)
				{
					IdentityAttribute attrib = identityAttributes.get(subjDesignatorIter.next());
					if (attrib != null)
						matchingIdentityAttributes.add(attrib);
				}
			}

			// if the Expression doesn't contain a subject attribute designator, or there are no AttributeValue
			// elements supplied, we can't match it against principal attributes, so don't bother processing it
			if (subjectDesignatorAttributes.isEmpty() || attributeValues.isEmpty())
			{
				throw new IllegalArgumentException(Messages.getString("AuthorizationProcessorImpl.149")); //$NON-NLS-1$
			}

			Iterator subjectAttributeIterator = attributeValues.iterator();
			while (subjectAttributeIterator.hasNext())
			{
				String matcher = (String) subjectAttributeIterator.next();

				// if there were matching attributes, apply the function against then
				Iterator<IdentityAttribute> identityAttributeIterator = matchingIdentityAttributes.iterator();

				// if the principal's identity attributes did not match any requested attributes as 
				// specified by the policy, format our message accordingly
				if(! identityAttributeIterator.hasNext())
				{
					logMessage = "{null.equals("+ matcher + ")}";  //$NON-NLS-1$//$NON-NLS-2$
				}
				while (identityAttributeIterator.hasNext())
				{
					// iterate through values of attributes 
					IdentityAttribute attr = identityAttributeIterator.next();

					Iterator attributeValueIterator = attr.getValues().iterator();
										
					while (attributeValueIterator.hasNext())
					{
						String attrValue = (String) attributeValueIterator.next();
						{
							if (toLower)
								attrValue = attrValue.toLowerCase();

							if (normalizeSpaces)
							{
								attrValue = attrValue.replaceAll(
										AuthorizationProcessorImpl.this.REGEX_WHITESPACE_START,
										AuthorizationProcessorImpl.this.REGEX_REPLACE_WITH);
								attrValue = attrValue.replaceAll(AuthorizationProcessorImpl.this.REGEX_WHITESPACE_END,
										AuthorizationProcessorImpl.this.REGEX_REPLACE_WITH);
							}

							logMessage += Messages.getString("AuthorizationProcessorImpl.123") + attrValue + Messages.getString("AuthorizationProcessorImpl.124") + matcher + Messages.getString("AuthorizationProcessorImpl.125"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

							if (attrValue.equals(matcher))
							{
								AuthorizationProcessorImpl.this.logger.log(Level.DEBUG, logMessage
										+ Messages.getString("AuthorizationProcessorImpl.126")); //$NON-NLS-1$
								return true;
							}
						}
					}
				}
			}

			AuthorizationProcessorImpl.this.logger.log(Level.DEBUG, logMessage
					+ Messages.getString("AuthorizationProcessorImpl.127")); //$NON-NLS-1$
			return false;
		}

	}

	protected class EvaluateStringRegexExpression
	{
		/**
		 * Executes the String regex operation on the node
		 * 
		 * @param node
		 *            The node to apply this operation to
		 * @param principal
		 *            The associated principal to use when evaluating
		 * @return The result of this operation
		 */
		@SuppressWarnings("unchecked")  //$NON-NLS-1$
		public boolean execute(JAXBElement<ApplyType> node, Principal principal)
		{
			String logMessage = Messages.getString("AuthorizationProcessorImpl.131"); //$NON-NLS-1$
			String function = null;
			boolean toLower = false;
			boolean normalizeSpaces = false;

			// content of AttributeValue elements
			List<String> attributeValues = new Vector<String>();
			List<String> subjectDesignatorAttributes = new Vector<String>();

			if (node.getDeclaredType() != ApplyType.class)
			{
				throw new IllegalArgumentException(Messages.getString("AuthorizationProcessorImpl.51")); //$NON-NLS-1$
			}

			JAXBElement<ApplyType> apply = node;
			function = apply.getValue().getFunctionId();

			if (function == null || !function.equals(FUNCTION_STRING_REGEX))
				throw new IllegalArgumentException(Messages.getString("AuthorizationProcessorImpl.52")); //$NON-NLS-1$

			// process children as arguments to this function
			Iterator<JAXBElement<?>> iter = apply.getValue().getExpressions().iterator();
			while (iter.hasNext())
			{
				JAXBElement child = iter.next();

				// add subject attribute designators
				if (child.getDeclaredType() == SubjectAttributeDesignatorType.class)
				{
					// set the attributes of the subject that we will use to match in the principal
					JAXBElement<SubjectAttributeDesignatorType> subj = child;
					if (subj != null && subj.getValue() != null)
						subjectDesignatorAttributes.add(subj.getValue().getAttributeId());
				}
				else
					if (child.getDeclaredType() == AttributeValueType.class)
					{
						// Contains the actual parameters of the function
						JAXBElement<AttributeValueType> attr = child;

						List content = attr.getValue().getContent();
						Iterator contentIter = content.iterator();
						while (contentIter.hasNext())
						{
							String value = contentIter.next().toString();
							attributeValues.add(value);

						}
					}
					// we can have an apply type, ONLY if its a normalize function
					else
						if (child.getDeclaredType() == ApplyType.class)
						{
							JAXBElement<ApplyType> normalizeFunction = child;
							String childFunction = normalizeFunction.getValue().getFunctionId();

							if (childFunction.equals(FUNCTION_STRING_NORMALIZE_TO_LOWER))
								toLower = true;
							else
								if (childFunction.equals(FUNCTION_STRING_NORMALIZE_SPACE))
									normalizeSpaces = true;
								else
									throw new IllegalArgumentException(MessageFormat.format(Messages.getString("AuthorizationProcessorImpl.152"), childFunction) ); //$NON-NLS-1$
						}
			}

			// for each policy specified subject attribute designator, retrieve any matching identity
			// attributes from the principal.
			List<IdentityAttribute> identityAttributes = new Vector<IdentityAttribute>();
			Iterator subjDesignatorIter = subjectDesignatorAttributes.iterator();
			while (subjDesignatorIter.hasNext())
			{
				// make sure the attribute exists, if so add
				Map<String, IdentityAttribute> attributes = principal.getAttributes();
				if (attributes != null)
				{
					IdentityAttribute attrib = attributes.get(subjDesignatorIter.next());
					if (attrib != null)
						identityAttributes.add(attrib);
				}
			}

			Iterator subjectAttributeIterator = subjectDesignatorAttributes.iterator();
			// if the Expression doesn't contain a subject attribute designator we can't match it against
			// principal attributes, so don't bother processing it
			if (!subjectAttributeIterator.hasNext() || attributeValues.isEmpty())
			{
				throw new IllegalArgumentException(Messages.getString("AuthorizationProcessorImpl.153")); //$NON-NLS-1$
			}

			// attempt to match against policy defined values
			Iterator policyAttributeIterator = attributeValues.iterator();
			while (policyAttributeIterator.hasNext())
			{
				String matcher = (String) policyAttributeIterator.next();

				// if there were matching attributes, apply the function against them
				Iterator<IdentityAttribute> identityAttributeIterator = identityAttributes.iterator();
				
				// if the principal's identity attributes did not match any requested attributes as 
				// specified by the policy, format our message accordingly
				if(! identityAttributeIterator.hasNext())
				{
					logMessage = "{null.equals("+ matcher + ")}"; //$NON-NLS-1$ //$NON-NLS-2$
				}
				while (identityAttributeIterator.hasNext())
				{
					// iterate through values of attributes
					IdentityAttribute attr = identityAttributeIterator.next();

					Iterator attributeValueIterator = attr.getValues().iterator();
					
					// if no attribute values for the slected attribute, format our message accordingly
					if(!attributeValueIterator.hasNext())
					{
						logMessage = "{null.equals("+ matcher + ")}"; //$NON-NLS-1$ //$NON-NLS-2$
					}
					while (attributeValueIterator.hasNext())
					{
						String attrValue =  attributeValueIterator.next().toString();

						try
						{
							if (toLower)
								attrValue = attrValue.toLowerCase();

							if (normalizeSpaces)
							{
								attrValue = attrValue.replaceAll(
										AuthorizationProcessorImpl.this.REGEX_WHITESPACE_START,
										AuthorizationProcessorImpl.this.REGEX_REPLACE_WITH);
								attrValue = attrValue.replaceAll(AuthorizationProcessorImpl.this.REGEX_WHITESPACE_END,
										AuthorizationProcessorImpl.this.REGEX_REPLACE_WITH);
							}

							logMessage += Messages.getString("AuthorizationProcessorImpl.132") + attrValue + Messages.getString("AuthorizationProcessorImpl.133") + matcher + Messages.getString("AuthorizationProcessorImpl.134"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

							if (attrValue.matches(matcher))
							{
								AuthorizationProcessorImpl.this.logger.log(Level.DEBUG, logMessage
										+ Messages.getString("AuthorizationProcessorImpl.135")); //$NON-NLS-1$
								return true;
							}
						}
						catch (PatternSyntaxException e)
						{
							AuthorizationProcessorImpl.this.logger.log(Level.WARN, Messages
									.getString("AuthorizationProcessorImpl.78") + function + "."); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				}
			}

			AuthorizationProcessorImpl.this.logger.log(Level.DEBUG, logMessage
					+ Messages.getString("AuthorizationProcessorImpl.136")); //$NON-NLS-1$
			return false;
		}
	}

}
