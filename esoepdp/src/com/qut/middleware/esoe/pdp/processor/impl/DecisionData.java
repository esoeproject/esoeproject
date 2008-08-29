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
 * Creation Date: 24/10/2006
 * 
 * Purpose: A data type to hold values required when processing an authz request. This object
 * is used to store processing state for the authz processor. It is also used for reporting 
 * of processed rules and policies. NOTE: This class is not thread safe. Care should be taken
 * when scoping instantiations.
 * 
 */
package com.qut.middleware.esoe.pdp.processor.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/** An object to hold state data with regards to the processing of policies. Can be used to populate return values of decision
 * statements after a list of policies has been evaluated.
 * 
 * */
public class DecisionData
{
	
	private Vector<String> matches;
	private String groupTargetMarker;
	private String decisionMessage;	
	private String currentPolicy;
	private String currentRule;
	
	// a map of group target to corresponding authz targets
	private Map<String, List<String>>groupTargetAuthzTargetMap;

	// a map of processed policies to corresponding processed rules 
	private Map<String, List<String>> processedPolicies; 

	/**
	 * Default constructor
	 */
	public DecisionData()
	{
		this.matches = new Vector<String>();
		this.processedPolicies = new HashMap<String, List<String>>();
		this.groupTargetAuthzTargetMap = new HashMap<String,List<String>>();
		this.groupTargetMarker = ""; //$NON-NLS-1$
	}
	
	
	/** Retrieves a list resource of matches stored in this object.
	 * 
	 * @return List of matches
	 */
	public List<String> getMatches()
	{
		return this.matches;
	}
	
	
	/** 
	 * Adds an authz target resource match to the list of matches in this.getCurrentGroupTarget().
	 * If the match already exists within the context of the current group target, it will not
	 * be added.
	 * 
	 * @param targetResource the resource to add.
	 */
	@SuppressWarnings("unchecked")
	public void addMatch(String targetResource)
	{
		if(!this.matches.contains(targetResource))
		{
			this.matches.add(targetResource);
		}
		
		this.groupTargetAuthzTargetMap.put(this.groupTargetMarker, (Vector<String>)this.matches.clone());		
	}
	
		
	/** Add a policy which has been processed. Adding a processed Policy updates the currentPolicy field
	 * to be the new addition.
	 * 
	 * @param policyID The policy ID to add
	 */
	public void addProcessedPolicy(String policyID)
	{
		if(policyID != null)
		{
			this.processedPolicies.put(policyID, new Vector<String>());
			this.currentPolicy = policyID;
		}
	}
	
	/** Get the last processed Policy to be added.
	 * 
	 * @return The last policy added to the list of processed rules in this object. If there is no current Policy, returns null.
	 */
	public String getCurrentPolicy()
	{
		return this.currentPolicy;
	}
	
	
	/** Get the current rule being processed.
	 * 
	 * @return The last rule added to the list of processed rules in this object. If there is no current Rule, returns null.
	 */
	public String getCurrentRule()
	{
		return this.currentRule;
	}	
	
	
	/** Get the current resource match.
	 * 
	 * @return The last match added to the list of matches in this object. If there is no current match, returns null.
	 */
	public String getCurrentMatch()
	{
		if(this.matches.size() != 0)
			return this.matches.lastElement();
		else
			return null;
	}
	
	/** Add a processed rule to this object. The Rule will be added to the internal Map of the currently processed Policy
	 * and it will be marked as the currentRule.
	 * 
	 * @param ruleID The rule ID to add
	 */
	public void addProcessedRule(String ruleID)
	{
		if(ruleID != null)
		{
			this.processedPolicies.get(this.currentPolicy).add(ruleID);
			this.currentRule = ruleID;
		}
	}
	
	
	/** 
	 * Retrieves a map of grouptargets -> list of authz targets for that group target
	 * 
	 * @return Map of grouptargets. May be zero sized.
	 */
	public Map<String, List<String>> getGroupTargets()
	{
		return this.groupTargetAuthzTargetMap;
	}


	/** Add a group target to the data object. If the target exists, the request is ignored. If the
	 * target is indeed a new target, the current
	 * 
	 * @param policyTarget
	 */
	public void addGroupTarget(String policyTarget)
	{
		if(! this.groupTargetAuthzTargetMap.containsKey(policyTarget))
		{
			this.groupTargetMarker = policyTarget;
			this.matches.clear();
			this.groupTargetAuthzTargetMap.put(policyTarget, new Vector<String>());
		}
	}
	
	/** Returns a formatted representation of the policies processed and the processed Rules contained in those policies.
	 * 
	 * @return A comma separated string representation of policies added to this object
	 */
	@SuppressWarnings("nls")
	public String getProcessedPolicies()
	{
		String value = new String();

		for (String policy : this.processedPolicies.keySet())
		{
			int counter = 1;
			value += "{Policy : " + policy;

			List<String> rules = this.processedPolicies.get(policy);
			for(String rule : rules)
			{
				if(counter == 1)
					value += " : Rules ["+ rule;			
				else
					value += "," + rule;
			
				if(counter == rules.size())
					value += "]";
		
				counter++;
			}
			
			value += "}";
			
		}
		
		return value;
	}
	
	
	/** Get the message set by the processor indicating information about the Decision made. 
	 * 
	 */
	public String getDecisionMessage()
	{
		return decisionMessage;
	}
	
	/** Set the message indicating information about the Decision made. 
	 * 
	 */
	public void setDecisionMessage(String decisionMessage) 
	{
		this.decisionMessage = decisionMessage;
	}
	
	/** Clears all grouptarget and authz target lists.
	 * 
	 *
	 */
	public void clearTargets()
	{
		this.groupTargetAuthzTargetMap.clear();
	}
}