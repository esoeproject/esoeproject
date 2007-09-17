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

import java.math.BigDecimal;


/** Represents a row in the SERVICE_POLICIES database. **/
public class PolicyCacheData
{
	private String entityID = null;
	private byte[] xacmlPolicy = null;
	private BigDecimal sequenceId = null;
	private String pollAction;
	private String policyId;
	
	
	/** Default constructor. Initializes all internal types to zero size objects.
	 * 
	 */
	public PolicyCacheData()
	{
		this.entityID = "";
		this.xacmlPolicy = new byte[]{};
		this.sequenceId = new BigDecimal(0l);
		this.policyId = "";
		this.pollAction = "";
	}
	
	/** Get the date the cache data was last updated.
	 * 
	 * @return the dateLastUpdated
	 */
	public BigDecimal getSequenceId()
	{
		return this.sequenceId;
	}
	
	/** Set the date the cache data was last updated.
	 *  
	 * @param dateLastUpdated the dateLastUpdated to set
	 */
	public void setSequenceId(BigDecimal sequenceId)
	{
		this.sequenceId = sequenceId;
	}
	
	/** Get the entityID of the the SPEP that the policies belong to.
	 * 
	 * @return the entityID
	 */
	public String getEntityID()
	{
		return this.entityID;
	}
	
	/** Set the entityID of the the SPEP that the policies belong to.
	 * 
	 * @param entityID the entitID to set
	 */
	public void setEntityID(String entityID)
	{
		this.entityID = entityID;
	}
	
	/** Get the string representation of the lxacml policy belonging to the SPEP. 
	 * 
	 * @return the xacmlPolicy
	 */
	public byte[] getLxacmlPolicy()
	{
		return this.xacmlPolicy;
	}
	
	/** Set the string representation of the lxacml policy belonging to the SPEP.
	 * 
	 * @param lxacmlPolicy the xacmlPolicy to set.
	 */
	public void setLxacmlPolicy(byte[] lxacmlPolicy)
	{
		this.xacmlPolicy = lxacmlPolicy;
	}
	
	/** Get the action to perform on the policy object when a poll is made
	 * 
	 * @return String representation of the poll action to perform. May return a 0 sized string.
	 */
	public String getPollAction()
	{
		return this.pollAction;
	}
	
	/** Set the action to perform on the policy object when a poll is made
	 * 
	 * @paramThe action to perform.
	 */
	public void setPollAction(String  pollAction)
	{
		this.pollAction = pollAction;
	}
	
	public String getPolicyId()
	{
		return this.policyId;
	}

	public void setPolicyId(String policyId)
	{
		this.policyId = policyId;
	}

}
