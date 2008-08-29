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

public class ServiceNode implements IsSerializable
{
	private String nodeIdentifier;
	private String nodeURL;
	private String acs;		// Assertion Consumer Service ie /sso
	private String sls;		// Single Logout Service
	private String ccs;		// Cache Clear Service
	private boolean active;
	
	/**
	 * @return the nodeURL
	 */
	public String getNodeURL()
	{
		return nodeURL;
	}
	/**
	 * @param nodeURL the nodeURL to set
	 */
	public void setNodeURL(String nodeURL)
	{
		this.nodeURL = nodeURL;
	}
	/**
	 * @return the acs
	 */
	public String getAcs()
	{
		return acs;
	}
	/**
	 * @param acs the acs to set
	 */
	public void setAcs(String acs)
	{
		this.acs = acs;
	}
	/**
	 * @return the sls
	 */
	public String getSls()
	{
		return sls;
	}
	/**
	 * @param sls the sls to set
	 */
	public void setSls(String sls)
	{
		this.sls = sls;
	}
	/**
	 * @return the ccs
	 */
	public String getCcs()
	{
		return ccs;
	}
	/**
	 * @param ccs the ccs to set
	 */
	public void setCcs(String ccs)
	{
		this.ccs = ccs;
	}
	/**
	 * @return the state
	 */
	public boolean isActive()
	{
		return active;
	}
	/**
	 * @param state the state to set
	 */
	public void setActive(boolean state)
	{
		this.active = state;
	}
	/**
	 * @return the nodeIdentifier
	 */
	public String getNodeIdentifier()
	{
		return nodeIdentifier;
	}
	/**
	 * @param nodeIdentifier the nodeIdentifier to set
	 */
	public void setNodeIdentifier(String nodeIdentifier)
	{
		this.nodeIdentifier = nodeIdentifier;
	}
}
