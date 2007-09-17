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

import java.math.BigDecimal;

import com.qut.middleware.esoe.pdp.cache.bean.AuthzPolicyCache;


@SuppressWarnings("nls")
public class PolicyCacheQueryData
{
	private String entityID = null;
	private BigDecimal sequenceId = null;
	private String policyId;
	
	
	/** Default constructor. Initializes all internal types to zero size objects.
	 * 
	 * The value of the descriptorId field is set to ""
	 * The value of the policyId filed is set to ""
	 * The long value of the sequenceId filed is set to AuthzPolicyCache.SEQUENCE_UNINITIALIZED.
	 */
	public PolicyCacheQueryData()
	{
		this.entityID = "";
		this.sequenceId = new BigDecimal(AuthzPolicyCache.SEQUENCE_UNINITIALIZED);
		this.policyId = "";
	}
	
	
	/** Get the last sequence ID from the policies state data source.
	 * 
	 * @return the latest sequence ID.
	 */
	public BigDecimal getSequenceId() 
	{
		return this.sequenceId;
	}


	/** Set the last sequence ID from the policies state data source.
	 * 
	 * @param sequenceId the sequence ID to set.
	 */
	public void setSequenceId(BigDecimal sequenceId) 
	{
		this.sequenceId = sequenceId;
	}
	
	public String getPolicyId() 
	{
		return this.policyId;
	}

	public void setPolicyId(String policyId) {
		this.policyId = policyId;
	}

	/** Get the entityID to retrieve policies for.
	 * 
	 * @return the entityID
	 */
	public String getEntityID()
	{
		return this.entityID;
	}
	
	/** Set the entity ID to retrieve policies for.
	 * 
	 * @param entity ID the descriptorID to set
	 */
	public void setEntityID(String entityID)
	{
		this.entityID = entityID;
	}
}
