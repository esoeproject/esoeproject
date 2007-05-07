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
 * Author: Shaun Mangelsdorf
 * Creation Date: 17/11/2006
 * 
 * Purpose: Contains fields required to make a query about SPEP Registration data
 */
package com.qut.middleware.esoe.spep.sqlmap.impl;

/** */
public class SPEPRegistrationQueryData
{
	private String descriptorID;
	private String nodeID;
	
	/**
	 * @return the descriptorID
	 */
	public String getDescriptorID()
	{
		return this.descriptorID;
	}
	
	/**
	 * @param descriptorID the descriptorID to set
	 */
	public void setDescriptorID(String descriptorID)
	{
		this.descriptorID = descriptorID;
	}
	
	/**
	 * @return the nodeID
	 */
	public String getNodeID()
	{
		return this.nodeID;
	}
	
	/**
	 * @param nodeID the nodeID to set
	 */
	public void setNodeID(String nodeID)
	{
		this.nodeID = nodeID;
	}
}
