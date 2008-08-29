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

public class EsoeDashBean implements IsSerializable
{
	String numServices;
	String activeServices;
	String numNodes;
	String activeNodes;
	String numPolicies;
	String activePolicies;

	public String getNumServices()
	{
		return numServices;
	}
	public void setNumServices(String numServices)
	{
		this.numServices = numServices;
	}
	public String getActiveServices()
	{
		return activeServices;
	}
	public void setActiveServices(String activeServices)
	{
		this.activeServices = activeServices;
	}
	public String getNumNodes()
	{
		return numNodes;
	}
	public void setNumNodes(String numNodes)
	{
		this.numNodes = numNodes;
	}
	public String getActiveNodes()
	{
		return activeNodes;
	}
	public void setActiveNodes(String activeNodes)
	{
		this.activeNodes = activeNodes;
	}
	public String getNumPolicies()
	{
		return numPolicies;
	}
	public void setNumPolicies(String numPolicies)
	{
		this.numPolicies = numPolicies;
	}
	public String getActivePolicies()
	{
		return activePolicies;
	}
	public void setActivePolicies(String activePolicies)
	{
		this.activePolicies = activePolicies;
	}
}
