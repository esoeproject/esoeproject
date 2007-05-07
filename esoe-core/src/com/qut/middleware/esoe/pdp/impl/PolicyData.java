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
package com.qut.middleware.esoe.pdp.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/** */
public class PolicyData
{
	
	private Vector<String> matches;
	private Vector<String> processedRules;
	private Vector<String> processedPolicies;
	private Map<String, List<String>>groupTargetAuthzTargetMap;
	private String groupTargetMarker;
	
	/**
	 * Default constructor
	 */
	public PolicyData()
	{
		this.matches = new Vector<String>();
		this.processedPolicies = new Vector<String>();
		this.processedRules = new Vector<String>();
		this.groupTargetAuthzTargetMap = Collections.synchronizedMap(new HashMap<String,List<String>>());
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
	
	
	/** Add a policy which has been processed.
	 * 
	 * @param policyID The policy ID to add
	 */
	public void addProcessedPolicy(String policyID)
	{
		this.processedPolicies.add(policyID);
		
	}
	
	
	/** Get the current rule being processed.
	 * 
	 * @return The last rule added to the list of processed rules in this object.
	 */
	public String getCurrentRule()
	{
		return this.processedRules.lastElement();
	}
	
	
	/** Get the current policy being processed.
	 * 
	 * @return The last policy added to the list of processed rules in this object.
	 */
	public String getCurrentPolicy()
	{
		return this.processedPolicies.lastElement();
	}
	
	
	/** Get the current resource match.
	 * 
	 * @return The last match added to the list of matches in this object
	 */
	public String getCurrentMatch()
	{
		return this.matches.lastElement();
	}
	
	/** Add a processed rule to this object.
	 * 
	 * @param ruleID The rule ID to add
	 */
	public void addProcessedRule(String ruleID)
	{
		this.processedRules.add(ruleID);
	}
	
	
	/** 
	 * Retrieves a map of grouptargets -> list of authz targets for that group target
	 * 
	 * @return Map of grouptargets
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
	
	/** Returns a formatted representation of the policies processed.
	 * 
	 * @return A comma separated string representation of policies added to this object
	 */
	@SuppressWarnings("nls")
	public String getProcessedPolicies()
	{
		String value = new String();
		
		Iterator<String> iter = this.processedPolicies.iterator();
		
		int counter = 1;
		while(iter.hasNext())
		{
			if(counter == 1)
				value = "{"+ iter.next();			
			else
				value += "," + iter.next();
			
			if(counter == this.processedPolicies.size())
				value += "}";
				
			counter++;
		}
		
		return value;
	}
	
	
	/** Returns a formatted representation of the rules processed.
	 * 
	 * @return A comma separated string representation of rules added to this object.
	 */
	@SuppressWarnings("nls")
	public String getProcessedRules()
	{
		String value = new String();
		
		Iterator<String> iter = this.processedRules.iterator();
		
		int counter = 1;
		while(iter.hasNext())
		{
			if(counter == 1)
				value = "{"+ iter.next();			
			else
				value += "," + iter.next();
			
			if(counter == this.processedRules.size())
				value += "}";
				
			counter++;
		}
		
		return value;
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