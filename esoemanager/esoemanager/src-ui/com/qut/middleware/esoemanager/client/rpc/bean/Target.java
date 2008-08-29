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

public class Target  implements IsSerializable
{
	List<String> resources;
	List<String> actions;
	
	/**
	 * @return the resources
	 */
	public List<String> getResources()
	{
		return resources;
	}
	/**
	 * @param resources the resources to set
	 */
	public void setResources(List<String> resources)
	{
		this.resources = resources;
	}
	/**
	 * @return the actions
	 */
	public List<String> getActions()
	{
		return actions;
	}
	/**
	 * @param actions the actions to set
	 */
	public void setActions(List<String> actions)
	{
		this.actions = actions;
	}
}
