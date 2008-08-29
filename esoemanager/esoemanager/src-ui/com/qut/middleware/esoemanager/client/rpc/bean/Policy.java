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

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Policy implements IsSerializable
{
	boolean invalid;
	boolean activated;

	String policyID;
	String description;

	Target target;
	List<Rule> rules;

	public boolean isActivated()
	{
		return activated;
	}

	public void setActivated(boolean activated)
	{
		this.activated = activated;
	}

	public String getPolicyID()
	{
		return policyID;
	}

	public void setPolicyID(String policyID)
	{
		this.policyID = policyID;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public List<Rule> getRules()
	{
		return rules;
	}

	public void setRules(List<Rule> rules)
	{
		this.rules = rules;
	}

	public Target getTarget()
	{
		return target;
	}

	public void setTarget(Target target)
	{
		this.target = target;
	}

	public boolean isInvalid()
	{
		return invalid;
	}

	public void setInvalid(boolean invalid)
	{
		this.invalid = invalid;
	}
}
