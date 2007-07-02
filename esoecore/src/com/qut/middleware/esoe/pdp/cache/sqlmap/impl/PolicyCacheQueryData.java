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
 * Purpose: Contains data used to query the policy cache database.
 */
package com.qut.middleware.esoe.pdp.cache.sqlmap.impl;

import java.util.Date;

/** */
public class PolicyCacheQueryData
{
	private String descriptorID = null;
	private Date dateLastUpdated = null;
	
	/** Get the last updated time. The underlying database connector will use this timestamp
	 * to retrieve policies modified after this time.
	 * 
	 * @return the dateLastUpdated
	 */
	public Date getDateLastUpdated()
	{
		return this.dateLastUpdated;
	}
	
	/** Set the last updated time. The underlying database connector will use this timestamp
	 * to retrieve policies modified after this time.
	 * 
	 * @param dateLastUpdated the dateLastUpdated to set
	 */
	public void setDateLastUpdated(Date dateLastUpdated)
	{
		this.dateLastUpdated = dateLastUpdated;
	}
	
	/** Get the descriptor ID to retrieve policies for.
	 * 
	 * @return the descriptorID
	 */
	public String getDescriptorID()
	{
		return this.descriptorID;
	}
	
	/** Set the descriptor ID to retrieve policies for.
	 * 
	 * @param descriptorID the descriptorID to set
	 */
	public void setDescriptorID(String descriptorID)
	{
		this.descriptorID = descriptorID;
	}
}
