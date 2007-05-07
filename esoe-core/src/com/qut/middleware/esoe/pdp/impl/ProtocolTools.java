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
 * Creation Date:
 * 
 * Purpose: Convenience class for generating various objects used in communication between the PDP
 * and endpoints.
 * 
 */
package com.qut.middleware.esoe.pdp.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;


import com.qut.middleware.saml2.schemas.esoe.lxacml.AttributeAssignment;
import com.qut.middleware.saml2.schemas.esoe.lxacml.EffectType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Obligation;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Obligations;
import com.qut.middleware.saml2.schemas.esoe.lxacml.assertion.LXACMLAuthzDecisionStatement;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.DecisionType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Request;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Response;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Result;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Status;
import com.qut.middleware.saml2.schemas.esoe.lxacml.grouptarget.GroupTarget;

/** */
public class ProtocolTools
{
	
	private static String ATTRIBUTE_ID = "lxacmlpdp:obligation:cachetargets:updateusercache"; //$NON-NLS-1$
	private static String OBLIGATION_ID = "lxacmlpdp:obligation:cachetargets"; //$NON-NLS-1$
	
	protected static final String PERMIT = "PERMIT"; //$NON-NLS-1$
	protected static final String DENY = "DENY"; //$NON-NLS-1$
		
	/** Create an obligation type object with the given effect.
	 * 
	 * @param effectType one of EffectType.PERMIT or EffectType.DENY. If the given effect string 
	 * does not match one of these, an IllegalArgumentException is thrown.
	 * @return A new Obligation object of the given effect type.
	 */
	public static Obligation createObligation(String effectType) 
	{
		Obligation t = new Obligation();
		t.setObligationId(OBLIGATION_ID);
		
		if(effectType != null && effectType.equalsIgnoreCase(DENY))
			t.setFulfillOn(EffectType.DENY);
		else if(effectType != null && effectType.equalsIgnoreCase(PERMIT))
			t.setFulfillOn(EffectType.PERMIT);
		else
			throw new IllegalArgumentException(Messages.getString("ProtocolTools.4")); //$NON-NLS-1$
		
		return t;
	}
	
	/**
	 * Creates a status object
	 * @param message The message to include
	 * @return The Status object that was created
	 */
	public static Status createStatus(String message)
	{
		Status t = new Status();
		t.setStatusMessage(message);
		return t;		
	}
	
	
	/**
	 * @param effectType The DecisionType to EffectType. One of PERMIT or DENY
	 * @return The Decision that was created
	 */
	public static DecisionType createDecision(String effectType)
	{
		if(effectType != null && effectType.equalsIgnoreCase(DENY))
			return DecisionType.DENY;
		else if(effectType != null && effectType.equalsIgnoreCase(PERMIT))
			return DecisionType.PERMIT;
		else
			throw new IllegalArgumentException(Messages.getString("ProtocolTools.5"));				 //$NON-NLS-1$
	}
	
	/**
	 * @param obligations The obligations to be sent back
	 * @param decision The decision that was made
	 * @param status The status object
	 * @return The Result object that was created
	 */
	public static Result createResult(Obligations obligations, DecisionType decision, Status status )
	{
		Result t = new Result();
		t.setDecision(decision);
		t.setObligations(obligations);
		t.setStatus(status);
		
		return t;
		
	}
	
	
	/**
	 * Creates a list of <code>AttributeAssigment</code> objects based on the given array
	 * of resources. The attributeID of each created assigment will be set to
	 * lxacmlpdp:obligation:cachetargets:updateusercache. Each <code>AttributeAssigment</code>
	 * will be set with a <code>GroupTarget</code> object, containing the escaped markup
	 * value of each resource specified.
	 * 
	 * @param groupTargets The group targets to use
	 * @return The List of AttributeAssignments generated
	 * 
	 */
	public static List<AttributeAssignment> createAttributeAssignments(Map<String,List<String>> groupTargets)
	{
		List<AttributeAssignment> attributes = new Vector<AttributeAssignment>();
				
		AttributeAssignment attr = new AttributeAssignment();
		attr.setAttributeId(ATTRIBUTE_ID);
		
		Iterator<String> iter = groupTargets.keySet().iterator();
				
		// for each procided group target
		while(iter.hasNext())
		{
			String groupTargetID = iter.next();
			
			// add the resource as the target ID
			GroupTarget target = new GroupTarget();
			target.setGroupTargetID(groupTargetID);
			
			// add all authz targets
			Iterator<String> authzIter = groupTargets.get(groupTargetID).iterator();
			
			while(authzIter.hasNext())
			{
				target.getAuthzTargets().add(authzIter.next());
			}
			
			// TODO run regex over resource string to add escaping
			attr.getContent().add(target);			
		}
				
		attributes.add(attr);
		
		return attributes;
	}
	
			
	/**
	 * @param authzRequest The request
	 * @param result The result that was generated
	 * @return The LXACMLAuthzDecisionStatement generated
	 */
	public static LXACMLAuthzDecisionStatement generateAuthzDecisionStatement(Request authzRequest, Result result)
	{		
		LXACMLAuthzDecisionStatement decision = new LXACMLAuthzDecisionStatement();
		
		// generate a lxacml response (NOTE this is in a different namespace to a SAML Response)
		Response response = new Response();
		
		response.setResult(result);
	
		decision.setRequest(authzRequest);
		
		decision.setResponse(response);
		
		return decision;
	}
}
