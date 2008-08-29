package com.qut.middleware.esoe.pdp.processor.impl;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.esoe.pdp.cache.AuthzPolicyCache;
import com.qut.middleware.esoe.pdp.processor.DecisionPoint;
import com.qut.middleware.esoe.pdp.processor.applyfunctions.And;
import com.qut.middleware.esoe.pdp.processor.applyfunctions.Not;
import com.qut.middleware.esoe.pdp.processor.applyfunctions.Or;
import com.qut.middleware.esoe.pdp.processor.applyfunctions.StringEqual;
import com.qut.middleware.esoe.pdp.processor.applyfunctions.StringRegex;
import com.qut.middleware.saml2.schemas.esoe.lxacml.ApplyType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.ConditionType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Policy;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Rule;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.DecisionType;

public class DecisionPointImpl implements DecisionPoint 
{

	private AuthzPolicyCache globalCache;
	private String defaultMode;
	
	Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	public DecisionPointImpl(AuthzPolicyCache cache, String defaultMode)
	{
		if (cache == null)
			throw new IllegalArgumentException("AuthzPolicyCache can NOT be null.");

		// incorrect parameter defaults the policy to DENY (might want a log breakpoint here)
		if (defaultMode == null || !defaultMode.equals(ProtocolTools.PERMIT) || !defaultMode.equals(ProtocolTools.DENY))
			this.defaultMode = ProtocolTools.DENY;
		else
			this.defaultMode = defaultMode;
				
		this.globalCache = cache;
	
		this.logger.info(MessageFormat.format("Successfully created DecisionPointImpl using default Mode of {0}.", this.defaultMode));
	}
	
	public DecisionType makeAuthzDecision(String resource, String issuer, Map<String, List<String>>  identityAttributes)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public DecisionType makeAuthzDecision(String resource, String issuer,  Map<String, List<String>>  identityAttributes, String action) 
	{
		// DEFAULT override. If there are no policies in the cache then we DENY all.
		if (this.globalCache.getSize() == 0)
		{
			this.logger.error("Policy cache is empty. Overriding default mode. All requests will be denied."); //$NON-NLS-1$
			this.defaultMode = ProtocolTools.DENY;
		}
		
		// retrieve policy set associated with SPEP
		List<Policy> policies = this.globalCache.getPolicies(issuer);

		if (policies == null)
		{
			this.logger.debug( MessageFormat.format("No matching policy located for {0}. Falling through to default state of {1}. ", issuer, this.defaultMode) ); 
			return ProtocolTools.createDecision(this.defaultMode);
		}
		else
		// process auth request against policies
		{
			this.logger.debug("Policies located.");
			return this.evaluatePolicyRequest(policies, resource, action, identityAttributes, null);
		}
	}
	
	public DecisionType makeAuthzDecision(String resource, String issuer, Map<String, List<String>>  identityAttributes, String action, DecisionData decisionData) 
	{

		// retrieve policy set associated with SPEP
		List<Policy> policies = this.globalCache.getPolicies(issuer);

		if (policies == null)
		{
			this.logger.debug( MessageFormat.format("No matching policy located for {0}. Falling through to default state of {1}. ", issuer, this.defaultMode) ); 
			return ProtocolTools.createDecision(this.defaultMode);
		}
		else
		// process auth request against policies
		{
			this.logger.debug(MessageFormat.format("Located {0} policies located for Issuer {1}.", policies.size(), issuer) );
			return this.evaluatePolicyRequest(policies, resource, action, identityAttributes, decisionData);
		}
	}
	
	private boolean isValidAction(String specifiedAction, List<String> policyActions, Rule currentRule)
	{
		// Get all action targets of the rule and check for a match
		List<String> targetActions = PolicyEvaluator.getRuleTargetActions(currentRule);

		// If the rule has no specified targets, use the policy targets
		if (targetActions == null || targetActions.size() == 0)
		{
			if (policyActions == null || policyActions.size() == 0)
				return true;

			targetActions = policyActions;
		}

		Iterator<String> actIter = targetActions.iterator();

		while (actIter.hasNext())
		{
			if (actIter.next().equals(specifiedAction))
				return true;
		}

		return false;
	}

	/*
	 * Evaluate the given resource request against the rules retrieved from the working policy and the current user
	 * session. NOTE: This function assumes that the policy object retrieved has been validated against the
	 * lxacmlSchema.xsd to contain only valid xml.
	 * 
	 * @param policy The authorization policy associated with the given SPEP. @param resource The target resource as
	 * requested by the SPEP. This is the resource given to the authorization processor in the <code>LXACMLAuthzDecisionQuery<code>.
	 * @param specifiedAction The action specified to be evaluated with this request. May be null if no action
	 * specified. @param principal The principal associated with the auth request @return the Result representing the
	 * outcome of the request processing.
	 * 
	 */
	private DecisionType evaluatePolicyRequest(List<Policy> policies, String resource, String specifiedAction, Map<String, List<String>> principalAttributes, DecisionData decisionData)
	{	
		DecisionType currentDecision = null;
		
		DecisionData localDecisionData= new DecisionData();
		if(decisionData != null)
			localDecisionData = decisionData;

		// for each policy, see if any targets match the resource request
		Iterator<Policy> policyIter = policies.iterator();

		// we'll want to break out of loops on deny
		boolean continueProcessing = true;

		while (policyIter.hasNext())
		{
			if (!continueProcessing)
				break;

			Policy policy = policyIter.next();

			// retrieve a list of all resources and action strings in the policy
			List<String> policyResources = PolicyEvaluator.getPolicyTargetResources(policy);
			List<String> policyActions = PolicyEvaluator.getPolicyTargetActions(policy);

			// add current policy to list of processed policies
			localDecisionData.addProcessedPolicy(policy.getPolicyId());
			this.logger.debug("Processing Policy " + localDecisionData.getCurrentPolicy());
			
			Iterator<String> resIter = policyResources.iterator();

			while (resIter.hasNext())
			{
				if (!continueProcessing)
					break;

				String policyResource = resIter.next();

				this.logger.debug(MessageFormat.format("Policy Target is {0}", policyResource) ); //$NON-NLS-1$

				// match the requested resource against policy derived resources
				if (resource.equals(policyResource) || resource.matches(policyResource))
				{
					this.logger.debug("Matched requested Resource against Policy Target."); //$NON-NLS-1$

					Iterator<Rule> rulesIter = policy.getRules().iterator();

					this.logger.debug(MessageFormat.format("Retrieved {0} rules.", policy.getRules().size()));
					
					while (rulesIter.hasNext())
					{
						if (!continueProcessing)
							break;

						Rule currentRule = rulesIter.next();

						localDecisionData.addProcessedRule(currentRule.getRuleId().toString());
						this.logger.debug("Processing Rule " + localDecisionData.getCurrentRule());
						
						// get all resource targets of the rule and check for a match
						List<String> targetResources = PolicyEvaluator.getRuleTargetResources(currentRule);

						// if the rule has no specified targets, use the policy targets
						if (targetResources == null)
						{
						//	this.logger.debug(Messages.getString("AuthorizationProcessorImpl.159")); //$NON-NLS-1$
							targetResources = policyResources;
						}

						Iterator<String> ruleResources = targetResources.iterator();

						while (ruleResources.hasNext())
						{
							if (!continueProcessing)
								break;

							String ruleResource = ruleResources.next();

							this.logger.debug("Rule Target is  " + ruleResource);
							
							if (resource.equals(ruleResource) || resource.matches(ruleResource))
							{
								this.logger.debug("Matched requested Resource against Rule Target."); //$NON-NLS-1$
								
								if (isValidAction(specifiedAction, policyActions, currentRule))
								{									
									// Process the associated rules 
									DecisionType newDecision = this.processRule(currentRule, resource, principalAttributes);

									// end processing if we hit a deny
									if (newDecision == DecisionType.DENY)
									{
										this.logger.debug("Encountered DENY decision. Terminating Rule processing ..."); //$NON-NLS-1$

										currentDecision = DecisionType.DENY;

										// We also only want to send the deny group target and authz target that matched
										// the  requested resource, so we need to clear and reset these values in decision  data bean.
										localDecisionData.clearTargets();
										continueProcessing = false;
									}
									else if (newDecision == DecisionType.PERMIT)
									{
										this.logger.debug("Permit decision returned. Continuing processing .."); //$NON-NLS-1$

										currentDecision = DecisionType.PERMIT;
									}
									else
										this.logger.debug("No decision could be made. Continuing processing ..");
									
									// add the policy target match and authz match to data object
									localDecisionData.addGroupTarget(policyResource);
									localDecisionData.addMatch(ruleResource);
								}
								else
									this.logger.warn("Invalid Action submitted in Authz Request.");
							}
							else
								this.logger.debug(MessageFormat.format("Requested resource {0} does not match. Skipping ..", resource));
						}
					}
				}
				else
					this.logger.debug("Requested Resource did not match any Policy targets."); //$NON-NLS-1$

			}
		}

		if (currentDecision == DecisionType.DENY)
		{
			Object[] args = { localDecisionData.getCurrentPolicy(), localDecisionData.getCurrentRule(),  localDecisionData.getProcessedPolicies() };
			localDecisionData.setDecisionMessage(MessageFormat.format("Identified DENY state for principal in Policy {0} Rule {1}. Evaluated {2}.", args) );
		}
		else if(currentDecision == DecisionType.PERMIT)
		{
			Object[] args = {localDecisionData.getProcessedPolicies()};
			localDecisionData.setDecisionMessage(MessageFormat.format("Identified PERMIT state for principal. Evaluated  {0}.", args));
		}
		else // If no decision could be made, set info accordingly and return default mode Decision.
		{
			localDecisionData.setDecisionMessage(MessageFormat.format("Policies located and rules evaluated but no explicit outcome detected. Falling through to default state of {0}.", this.defaultMode));
			return ProtocolTools.createDecision(this.defaultMode);
		}
		
		return currentDecision;
		
	}

	/*
	 * Evaluate and process the given rule to determine the outcome of the resource request.
	 * 
	 * @param rule The Rule to evaluate. @param principal The Principal object that contains information to match
	 * against any conditions contained in the given Rule. @return A DecisionType representing the outcome of the Rule
	 * evaluation if one can be made. If a condition contained in the given rule evaluates to False, a decision can not
	 * be made based on the Effect of the Rule (because the condition does not match) and null is returned.
	 */
	@SuppressWarnings("unchecked")//$NON-NLS-1$
	private DecisionType processRule(Rule rule, String requestedResource, Map<String, List<String>> principalAttributes)
	{
		boolean conditionMatches = true;
		String effect = new String();
		ConditionType cond = rule.getCondition();

		this.logger.debug(MessageFormat.format("Evaluating Rule. Effect of Rule is {1}", rule.getRuleId(), rule.getEffect()) ); //$NON-NLS-1$

		// If a condition exists, evaluate its expression
		if (cond != null)
		{
			JAXBElement<ApplyType> expression = (JAXBElement<ApplyType>) cond.getExpression();

			// condition can only hold an apply element so this shouldnt be necessary, but ...
			if (expression != null)
			{
				this.logger.debug("Processing conditions ..."); //$NON-NLS-1$

				// root level apply element (condition can only have 1 apply element)
				JAXBElement<ApplyType> apply = expression;

				// we don't want IllegalArgument Exceptions to halt the auth process, so we'll deal
				// with them here. Invalid parameters in a Rule = ignore that Rule.
				try
				{
					conditionMatches = this.processExpressions(apply, principalAttributes);
				}
				catch (IllegalArgumentException e)
				{
					this.logger.warn("Ignoring bad Rule. " + e.getMessage()); //$NON-NLS-1$
					conditionMatches = false;
				}
			}
		}
		else
			this.logger.debug("No Conditions found. Applying effect of Rule."); //$NON-NLS-1$

		// The condition is the return value of any processed expression.  If the condition matches, we apply
		// the effect of the rule, else we ignore it.
		if (!conditionMatches)
			this.logger.debug("Condition did not match. Ignoring Effect of Rule and returning null."); //$NON-NLS-1$

		effect = rule.getEffect().toString();
		if (conditionMatches && effect.equalsIgnoreCase(ProtocolTools.PERMIT))
			return DecisionType.PERMIT;
		else
			if (conditionMatches && effect.equalsIgnoreCase(ProtocolTools.DENY))
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
	private boolean processExpressions(JAXBElement<ApplyType> rootNode, Map<String, List<String>> principalAttributes)
	{
		String function = null;

		if (rootNode == null || (rootNode.getDeclaredType() != ApplyType.class))
			throw new IllegalArgumentException("Root Node of Condition is not an Apply Element.");  //$NON-NLS-1$

		JAXBElement<ApplyType> apply = rootNode;

		function = apply.getValue().getFunctionId();

		if (function == null)
			throw new IllegalArgumentException("Function ID of root Node does not exist. Unable to parse policy data."); //$NON-NLS-1$

		if (function.equals(Or.FUNCTION_NAME))
			return new Or().evaluateExpression(rootNode, principalAttributes);
		else
			if (function.equals(Not.FUNCTION_NAME))
				return new Not().evaluateExpression(rootNode, principalAttributes);
			else
				if (function.equals(And.FUNCTION_NAME))
					return new And().evaluateExpression(rootNode, principalAttributes);
				else
					if (function.equals(StringRegex.FUNCTION_NAME))
						return new StringRegex().evaluateExpression(rootNode, principalAttributes);
					else
						if (function.equals(StringEqual.FUNCTION_NAME))
							return  new StringEqual().evaluateExpression(rootNode, principalAttributes);

		return false;

	}

	public String getDefaultMode()
	{
		return this.defaultMode;
	}
	
}
