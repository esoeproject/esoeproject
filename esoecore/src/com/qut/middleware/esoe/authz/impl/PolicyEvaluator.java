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
 * Author: Andre Zitelli
 * Creation Date: 23/10/2006
 * 
 * Purpose:
 */
package com.qut.middleware.esoe.authz.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.qut.middleware.esoe.pdp.processor.impl.DecisionData;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Action;
import com.qut.middleware.saml2.schemas.esoe.lxacml.AttributeValueType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Obligation;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Obligations;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Policy;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Resource;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Rule;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Target;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.DecisionType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Result;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Status;
import com.qut.middleware.saml2.schemas.esoe.lxacml.grouptarget.GroupTarget;

/** */
public class PolicyEvaluator
{
		
	/** Extract all resources from resources contained within the given policy.
	 * 
	 * @param policy The policy to find resources for.
	 * @return A list of resources specified for the policy
	 */
	public static List<String> getPolicyTargetResources(Policy policy)
	{
		List<String> resources = new Vector<String>();
		
		// Add any rule defined resources
		Target target = policy.getTarget();
		
		List<String> targetResources = getTargetResources(target);
		
		Iterator<String> iter = targetResources.iterator();
		while(iter.hasNext())
		{
			String resourceKey = iter.next();
			
			if( !resources.contains(resourceKey) )
			{
				resources.add(resourceKey);
			}	
		}
		
		return resources;
	}
	
	/** Extract all actions from actions contained within the given policy.
	 * 
	 * @param policy The policy to find actions for.
	 * @return A list of actions specified for the policy, may be NULL if no actions exist
	 */
	public static List<String> getPolicyTargetActions(Policy policy)
	{
		List<String> resources = new Vector<String>();
		
		// Add any rule defined resources
		Target target = policy.getTarget();
		
		List<String> targetActions = getTargetActions(target);
		
		if(targetActions == null)
			return null;
		
		Iterator<String> iter = targetActions.iterator();
		while(iter.hasNext())
		{
			String resourceKey = iter.next();
			
			if( !resources.contains(resourceKey) )
			{
				resources.add(resourceKey);
			}	
		}
		
		return resources;
	}
	
	/** Get the string respresentations of all resources defined in the given rule. The resources are
	 * defined within the target, which is in turn defined within the given rule.
	 * 
	 * @param rule The rule to interrogate.
	 * @return A list of resource strings as extracted from the given rule, else null if no resources exist.
	 */
	public static List<String> getRuleTargetResources(Rule rule)
	{
		List<String> resources = new Vector<String>();
		
		try
		{
			// Add any rule defined resources
			Target target = rule.getTarget();
			
			if(target == null)
				return null;
			
			List<String> targetResources = getTargetResources(target);
			
			// Check for and remove any duplicates
			Iterator<String> iter = targetResources.iterator();
			while(iter.hasNext())
			{
				String resourceKey = iter.next();
				
				if( !resources.contains(resourceKey) )
				{
					resources.add(resourceKey);
				}	
			}
		}
		// some may view the handling of NPE runtime as bad form .. however, in this case since the
		// presence of ANY null values in the tree for the given object will result in not being able
		// to return the necessary data, we're going to make an exception. We also do not want to
		// reduce performance by checking for null values, as they should not exist in the first place
		// if the schema is enforced.
		catch(NullPointerException e)
		{
			return null;
		}
		
		return resources;
	}
	
	/** Get the string representations of all actions defined in the given rule. The actions are
	 * defined within the target, which is in turn defined within the given rule.
	 * 
	 * @param rule The rule to interrogate.
	 * @return A list of action strings as extracted from the given rule, else NULL if no resources exist.
	 */
	public static List<String> getRuleTargetActions(Rule rule)
	{
		List<String> resources = new Vector<String>();
		
		try
		{
			// Add any rule defined resources
			Target target = rule.getTarget();
			
			if(target == null)
				return null;
			
			List<String> targetActions = getTargetActions(target);
			
			if(targetActions == null)
				return null;
			
			// Check for and remove any duplicates
			Iterator<String> iter = targetActions.iterator();
			while(iter.hasNext())
			{
				String resourceKey = iter.next();
				
				if( !resources.contains(resourceKey) )
				{
					resources.add(resourceKey);
				}	
			}
		}
		// some may view the handling of NPE runtime as bad form .. however, in this case since the
		// presence of ANY null values in the tree for the given object will result in not being able
		// to return the necessary data, we're going to make an exception. We also do not want to
		// reduce performance by checking for null values, as they should not exist in the first place
		// if the schema is enforced.
		catch(NullPointerException e)
		{
			return null;
		}
		
		return resources;
	}
	
	
	/** Get the Policy Resource target that matches targetMatcher and creates a populated GroupTarget object
	 * containing all AuthzTargets that also match the targetMatcher string. A GroupTarget corresponds to
	 * a defined Policy/Target/Resources/Resource, Whereas an AuthzTarget corresponds to a 
	 * Rule/Target/Resources/Resource. Note: if multiple (exactly the same) Rule targets are encountered,
	 * they will only be added as AuthzTargets the first time they are encountered.
	 * 
	 * @pre params != null
	 * @param policy The Policy to interrogate.
	 * @param targetMatcher A string representing which targets to match. The logic will attempt to match
	 * the policy's targets to the given string. If a match is encountered it will try to match Rule targets
	 * to the given string.
	 * @return A GroupTarget object contained matching targets if a Policy Target match occurs, else null.
	 */
	public static GroupTarget getMatchingGroupTarget(Policy policy, String targetMatcher)
	{
		GroupTarget groupTarget = null;

		// retrieve a list of all resources strings in the policy
		List<String> policyResources = PolicyEvaluator.getPolicyTargetResources(policy);
		
		Iterator<String> resIter = policyResources.iterator();
		while (resIter.hasNext())
		{
			String policyResource = resIter.next();

			// match the requested resource against policy derived resources
			if (targetMatcher.equals(policyResource) || targetMatcher.matches(policyResource)) 
			{
				groupTarget = new GroupTarget();
				groupTarget.setGroupTargetID(policyResource);
			
				List<String> authzTargets = new Vector<String>();
				
				Iterator<Rule> rulesIter = policy.getRules().iterator();
				while (rulesIter.hasNext())
				{
					Rule currentRule = rulesIter.next();
					
					// get all resource targets of the rule and check for a match
					List<String> ruleTargets = getRuleTargetResources(currentRule);

					// if the rule has no specified targets, use the policy targets
					if (ruleTargets == null)
						ruleTargets= policyResources;

					Iterator<String> ruleResources = ruleTargets.iterator();
					while (ruleResources.hasNext())
					{						
						String ruleResource = ruleResources.next();

						if( !authzTargets.contains(ruleResource) )
							authzTargets.add(ruleResource);								
		
					}
				}
				
				groupTarget.getAuthzTargets().addAll(authzTargets);
			}
		}	

		return groupTarget;
	}
	
	/** Extract all action strings from actions contained within the given target.
	 * 
	 * @param target A Target as obtained from a Policy object
	 * 
	 */
	private static List<String> getTargetActions(Target target)
	{
		List<String> resources = new Vector<String>();

		try
		{
			// get all the resources within the target
			if(target.getActions() == null)
				return null;
				
			Iterator<Action> actionIter = target.getActions().getActions().iterator();
		
			while(actionIter.hasNext())
			{			
				Action current = actionIter.next();
				
				AttributeValueType attributes = current.getAttributeValue();
				List<Object> valueList = attributes.getContent();
				Iterator<Object> attIter = valueList.iterator();
				while(attIter.hasNext())
				{
					String res = attIter.next().toString().trim();
					resources.add(res);
				}			
				
			}
		}
		// some may view the handling of NPE runtime as bad form .. however, in this case since the
		// presence of ANY null values in the tree for the given object will result in not being able
		// to return the necessary data, we're going to make an exception. We also do not want to
		// reduce performance by checking for null values, as they should not exist in the first place
		// if the schema is enforced.
		catch(NullPointerException e)
		{
			return null;
		}
		
		return resources;
	}
	
	/** Extract all resource strings from resources contained within the given target.
	 * 
	 * @param target A Target as obtained from a Policy object
	 * 
	 */
	private static List<String> getTargetResources(Target target)
	{
		List<String> resources = new Vector<String>();

		try
		{
			// get all the resources within the target 
			Iterator<Resource> resourceIter = target.getResources().getResources().iterator();
		
			while(resourceIter.hasNext())
			{			
				Resource current = resourceIter.next();
				
				AttributeValueType attributes = current.getAttributeValue();
				List<Object> valueList = attributes.getContent();
				Iterator<Object> attIter = valueList.iterator();
				while(attIter.hasNext())
				{
					String res = attIter.next().toString().trim();
					resources.add(res);
				}			
				
			}
		}
		// some may view the handling of NPE runtime as bad form .. however, in this case since the
		// presence of ANY null values in the tree for the given object will result in not being able
		// to return the necessary data, we're going to make an exception. We also do not want to
		// reduce performance by checking for null values, as they should not exist in the first place
		// if the schema is enforced.
		catch(NullPointerException e)
		{
			return null;
		}
		
		return resources;
	}
	
		
	/**
	 * Creates a deny result with the status message set to message. This method will create all
	 * sub components of the result object aswell. The obligation will be set to DENY, with an 
	 * obligation ID of "lxacmlpdp:obligation:cachetargets".
	 * @param statusMessage 
	 * @param policyData 
	 * @return The Result object
	 */
	public Result createDenyResult(String statusMessage, DecisionData policyData)
	{		
		DecisionType decision;
		Status status;
		Obligations obl;
		
		decision = ProtocolTools.createDecision(ProtocolTools.DENY);
		status = ProtocolTools.createStatus(statusMessage);
		obl = new Obligations();
				
		Obligation ob = ProtocolTools.createObligation(ProtocolTools.DENY);
		
		// create assignments and add to obligation
		ob.getAttributeAssignments().addAll(ProtocolTools.createAttributeAssignments(policyData.getGroupTargets()) );
		
		// add obligations to list 
		obl.getObligations().add(ob);
		
		return ProtocolTools.createResult(obl, decision, status);
		
	}
	
	
	/** Create a permit result object. This method interrogates the given policy data to 
	 * populate the Result with required fields. In particular, the policy data object must
	 * ensure that group targets are correctly populated.
	 *  
	 * @param message The message to include in the Result.
	 * @param policyData The data object that contains the required data.
	 * @return The Result object.
	 */
	public Result createPermitResult(String message, DecisionData policyData)
	{
		DecisionType decision;
		Status status;
		Obligations obligations = new Obligations();
		
		decision = ProtocolTools.createDecision(ProtocolTools.PERMIT);
		status = ProtocolTools.createStatus(message);
				
		Obligation ob = ProtocolTools.createObligation(ProtocolTools.PERMIT);
		
		// create assignments and add to obligation
		ob.getAttributeAssignments().addAll(ProtocolTools.createAttributeAssignments(policyData.getGroupTargets()) );
		
		// add obligations to list 
		obligations.getObligations().add(ob);
		
		return ProtocolTools.createResult(obligations, decision, status);
	}
	
	
	/**
	 * Creates a default result according to class specified default. 
	 * 
	 * @param message The message to include in the returned <code>Result</code> The default mode will be appended to the end
	 * of the message.
	 * @param defaultMode The default mode
	 * @return The Result object
	 */
	public Result createDefaultResult(String message, String defaultMode, DecisionData policyData) 
	{
		DecisionType decision = ProtocolTools.createDecision(defaultMode);
		Status status = ProtocolTools.createStatus(message); 
		Obligations obs = new Obligations();
		
		Obligation ob = ProtocolTools.createObligation(defaultMode);
		
		// create assignments and add to obligation
		ob.getAttributeAssignments().addAll(ProtocolTools.createAttributeAssignments(policyData.getGroupTargets()) );
		
		// add obligations to list 
		obs.getObligations().add(ob);		
		
		return ProtocolTools.createResult(obs, decision, status);
		
	}
	
	
	/**  Creates a Result with the given Decision and status message. This method will create all
	 * sub components of the result object aswell. The obligation will be set to whatever the decision is,
	 * with an  obligation ID of "lxacmlpdp:obligation:cachetargets".
	 * 
	 * @param  decision The decision to use when determining what values will be set in Result.
	 * @param statusMessage The message to set in the Result.
	 * @param groupTargets The Targets to set as Authz Targets in created Obligations. 
	 * @return The Result object created as an amalgamation of given parameters.
	 * @throws NullPointerException if any parameters are null.
	 */
	public Result createResult(DecisionType decision, String statusMessage, Map<String,List<String>> groupTargets)
	{			
		Status status;
		Obligations obligations = new Obligations();;
		status = ProtocolTools.createStatus(statusMessage);
				
		Obligation obligation = ProtocolTools.createObligation(decision.value());
		
		// create assignments and add to obligation
		obligation.getAttributeAssignments().addAll(ProtocolTools.createAttributeAssignments(groupTargets));
		
		// add obligations to list 
		obligations.getObligations().add(obligation);
		
		return ProtocolTools.createResult(obligations, decision, status);
		
	}
}
