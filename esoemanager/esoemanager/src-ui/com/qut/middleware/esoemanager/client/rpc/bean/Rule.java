/* Copyright 2008, Queensland University of Technology
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
 */
package com.qut.middleware.esoemanager.client.rpc.bean;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Condition;


public class Rule  implements IsSerializable
{
	String effect;
	String ruleID;
	String description;

	Target target;
	Condition condition;
	
	
	/**
	 * @return the effect
	 */
	public String getEffect()
	{
		return effect;
	}
	/**
	 * @param effect the effect to set
	 */
	public void setEffect(String effect)
	{
		this.effect = effect;
	}
	/**
	 * @return the ruleID
	 */
	public String getRuleID()
	{
		return ruleID;
	}
	/**
	 * @param ruleID the ruleID to set
	 */
	public void setRuleID(String ruleID)
	{
		this.ruleID = ruleID;
	}
	/**
	 * @return the description
	 */
	public String getDescription()
	{
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}
	/**
	 * @return the targets
	 */
	public Target getTarget()
	{
		return target;
	}
	/**
	 * @param targets the targets to set
	 */
	public void setTarget(Target target)
	{
		this.target = target;
	}
	/**
	 * @return the condition
	 */
	public Condition getCondition()
	{
		return condition;
	}
	/**
	 * @param condition the condition to set
	 */
	public void setCondition(Condition condition)
	{
		this.condition = condition;
	}
	
	
}
