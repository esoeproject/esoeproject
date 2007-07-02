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
 * Creation Date: 20/11/2006
 * 
 * Purpose: Contains data from the policy cache database.
 */
package com.qut.middleware.esoe.pdp.cache.sqlmap.impl;

import java.util.Date;

/** Represents a row in the SERVICE_POLICIES database. **/
public class PolicyCacheData
{
	private String descriptorID = null;
	private String xacmlPolicy = null;
	private Date dateLastUpdated = null;
	
	/** Get the date the cache data was last updated.
	 * 
	 * @return the dateLastUpdated
	 */
	public Date getDateLastUpdated()
	{
		return this.dateLastUpdated;
	}
	
	/** Set the date the cache data was last updated.
	 *  
	 * @param dateLastUpdated the dateLastUpdated to set
	 */
	public void setDateLastUpdated(Date dateLastUpdated)
	{
		this.dateLastUpdated = dateLastUpdated;
	}
	
	/** Get the descriptorID of the the SPEP that the policies belong to.
	 * 
	 * @return the descriptorID
	 */
	public String getDescriptorID()
	{
		return this.descriptorID;
	}
	
	/** Set the descriptorID of the the SPEP that the policies belong to.
	 * 
	 * @param descriptorID the descriptorID to set
	 */
	public void setDescriptorID(String descriptorID)
	{
		this.descriptorID = descriptorID;
	}
	
	/** Get the string representation of the lxacml policy belonging to the SPEP. 
	 * 
	 * @return the xacmlPolicy
	 */
	public String getLxacmlPolicy()
	{
		return this.xacmlPolicy;
	}
	
	/** Set the string representation of the lxacml policy belonging to the SPEP.
	 * 
	 * @param lxacmlPolicy the xacmlPolicy to set.
	 */
	public void setLxacmlPolicy(String lxacmlPolicy)
	{
		this.xacmlPolicy = lxacmlPolicy;
	}
}
