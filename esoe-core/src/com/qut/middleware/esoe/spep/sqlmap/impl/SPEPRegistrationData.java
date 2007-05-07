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
 * Purpose: Data object for iBatis connection to SPEP registration database
 */
package com.qut.middleware.esoe.spep.sqlmap.impl;

import java.util.Date;

/** Data object for iBatis connection to SPEP registration database.
 * */
public class SPEPRegistrationData
{
	//DESCRIPTORID VARCHAR2(512) NOT NULL
	private String descriptorID;
	//NODEID VARCHAR2(512) NOT NULL
	private String nodeID;
	//IPADDRESS VARCHAR2(1024) NOT NULL
	private String ipAddress;
	//COMPILEDATE VARCHAR2(30) NOT NULL
	private String compileDate;
	//COMPILESYSTEM VARCHAR2(60) NOT NULL
	private String compileSystem; 
	//VERSION VARCHAR2(100) NOT NULL
	private String version;
	//ENVIRONMENT VARCHAR2(255) NOT NULL
	private String environment;
	//DATE_ADDED DATE NOT NULL
	private Date dateAdded;
	//DATE_UPDATED DATE NOT NULL
	private Date dateUpdated;
	
	/**
	 * @return Compile date
	 */
	public String getCompileDate()
	{
		return this.compileDate;
	}
	
	/**
	 * @param compileDate Compile date
	 */
	public void setCompileDate(String compileDate)
	{
		this.compileDate = compileDate;
	}
	
	/**
	 * @return Compile system
	 */
	public String getCompileSystem()
	{
		return this.compileSystem;
	}
	
	/**
	 * @param compileSystem Compile system
	 */
	public void setCompileSystem(String compileSystem)
	{
		this.compileSystem = compileSystem;
	}
	
	/**
	 * @return Date first added
	 */
	public Date getDateAdded()
	{
		return this.dateAdded;
	}
	
	/**
	 * @param dateAdded Date first added.
	 */
	public void setDateAdded(Date dateAdded)
	{
		this.dateAdded = dateAdded;
	}
	/**
	 * @return Date last updated
	 */
	public Date getDateUpdated()
	{
		return this.dateUpdated;
	}
	/**
	 * @param dateUpdated Date last updated
	 */
	public void setDateUpdated(Date dateUpdated)
	{
		this.dateUpdated = dateUpdated;
	}
	/**
	 * @return Descriptor ID
	 */
	public String getDescriptorID()
	{
		return this.descriptorID;
	}
	/**
	 * @param descriptorID Descriptor ID
	 */
	public void setDescriptorID(String descriptorID)
	{
		this.descriptorID = descriptorID;
	}
	/**
	 * @return Environment string.
	 */
	public String getEnvironment()
	{
		return this.environment;
	}
	/**
	 * @param environment Environment string
	 */
	public void setEnvironment(String environment)
	{
		this.environment = environment;
	}
	/**
	 * @return Space seperated ip address list
	 */
	public String getIpAddress()
	{
		return this.ipAddress;
	}
	/**
	 * @param ipAddress Space seperated ip address list
	 */
	public void setIpAddress(String ipAddress)
	{
		this.ipAddress = ipAddress;
	}
	
	/**
	 * @return The version string.
	 */
	public String getVersion()
	{
		return this.version;
	}
	/**
	 * @param version The version string
	 */
	public void setVersion(String version)
	{
		this.version = version;
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
