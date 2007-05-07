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
 * Author: Bradley Beddoes
 * Creation Date: 1/5/07
 * 
 * Purpose: Implementation og AuthorizationPolicy
 */
package com.qut.middleware.esoemanager.bean.impl;

import java.util.Date;

import com.qut.middleware.esoemanager.bean.AuthorizationPolicyBean;

public class AuthorizationPolicyBeanImpl implements AuthorizationPolicyBean
{
	boolean modified;
	Date lastUpdated;
	String lxacmlPolicy;
	
	/* (non-Javadoc)
	 * @see com.qut.middleware.esoemanager.bean.impl.AuthorizationPolicyBean#getLastUpdated()
	 */
	public Date getLastUpdated()
	{
		return this.lastUpdated;
	}
	/* (non-Javadoc)
	 * @see com.qut.middleware.esoemanager.bean.impl.AuthorizationPolicyBean#setLastUpdated(java.util.Date)
	 */
	public void setLastUpdated(Date lastUpdated)
	{
		this.lastUpdated = lastUpdated;
	}
	/* (non-Javadoc)
	 * @see com.qut.middleware.esoemanager.bean.impl.AuthorizationPolicyBean#getLxacmlPolicy()
	 */
	public String getLxacmlPolicy()
	{
		return this.lxacmlPolicy;
	}
	/* (non-Javadoc)
	 * @see com.qut.middleware.esoemanager.bean.impl.AuthorizationPolicyBean#setLxacmlPolicy(java.lang.String)
	 */
	public void setLxacmlPolicy(String lxacmlPolicy)
	{
		this.lxacmlPolicy = lxacmlPolicy;
	}
	/* (non-Javadoc)
	 * @see com.qut.middleware.esoemanager.bean.impl.AuthorizationPolicyBean#isModified()
	 */
	public boolean isModified()
	{
		return this.modified;
	}
	/* (non-Javadoc)
	 * @see com.qut.middleware.esoemanager.bean.impl.AuthorizationPolicyBean#setModified(boolean)
	 */
	public void setModified(boolean modified)
	{
		this.modified = modified;
	}
}
