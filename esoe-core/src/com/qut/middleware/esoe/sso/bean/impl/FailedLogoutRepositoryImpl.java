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
 * Author: Andre Zitelli
 * Creation Date: 14/12/2006
 * 
 * Purpose: Thread safe implementation of FailedLogoutRepository.
 */
package com.qut.middleware.esoe.sso.bean.impl;

import java.util.List;
import java.util.Vector;

import com.qut.middleware.esoe.sso.bean.FailedLogout;
import com.qut.middleware.esoe.sso.bean.FailedLogoutRepository;

/** Thread safe implementation of FailedLogoutRepository. */

public class FailedLogoutRepositoryImpl implements FailedLogoutRepository {

	private List<FailedLogout> failures;
	
	
    /** Initilaizes the internal failure repository to a 0 sized list.
     * 
     * Default constructor
     */
    public FailedLogoutRepositoryImpl()
    {
    	// using vector for synchronized behaviour
    	this.failures = new Vector<FailedLogout>();
    }
    
    
	/** Add the cache failure object to the repository. Null parameters are ignored.
	 * 
	 * 
	 * @see com.qut.middleware.esoe.pdp.cache.bean.FailedLogoutRepository#add(com.qut.middleware.esoe.pdp.cache.bean.FailedLogout)
	 */
	public void add(FailedLogout failure)
	{
		if(failure != null)
			this.failures.add(failure);
	}

	
	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.pdp.cache.bean.FailedLogoutRepository#clearFailures()
	 */
	public void clearFailures()
	{
		this.failures.clear();
	}

	
	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.pdp.cache.bean.FailedLogoutRepository#getFailures()
	 */
	public List<FailedLogout> getFailures()
	{
		return this.failures;
	}
	
	
	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.pdp.cache.bean.FailedLogoutRepository#remove(com.qut.middleware.esoe.pdp.cache.bean.FailedAuthzCacheUpdate)
	 */
	public void remove(FailedLogout failure)
	{
		this.failures.remove(failure);
	}
	
	
	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.pdp.cache.bean.FailedLogoutRepository#getSize()
	 */
	public int getSize()
	{
		return(this.failures == null ? 0 : this.failures.size() );
	}

}
