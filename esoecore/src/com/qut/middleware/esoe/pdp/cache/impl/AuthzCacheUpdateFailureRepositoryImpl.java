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
 * Creation Date:
 * 
 * Purpose: Thread safe implementation of AuthzCacheUpdateFailureRepository
 */
package com.qut.middleware.esoe.pdp.cache.impl;

import java.util.List;
import java.util.Vector;

import com.qut.middleware.esoe.pdp.cache.AuthzCacheUpdateFailureRepository;
import com.qut.middleware.esoe.pdp.cache.bean.FailedAuthzCacheUpdate;

public class AuthzCacheUpdateFailureRepositoryImpl implements AuthzCacheUpdateFailureRepository
{

	private List<FailedAuthzCacheUpdate> failures;
	
	
    /** Initializes the internal failure repository to a 0 sized list.
     * 
     * Default constructor
     */
    public AuthzCacheUpdateFailureRepositoryImpl()
    {
    	// using synchronized implementation of vector
    	this.failures = new Vector<FailedAuthzCacheUpdate>();
    }
    
    
	/* Add the cache failure object to the repository. Null parameters are ignored.
	 * 
	 * 
	 * @see com.qut.middleware.esoe.pdp.cache.bean.AuthzCacheUpdateFailureRepository#add(com.qut.middleware.esoe.pdp.cache.bean.FailedAuthzCacheUpdate)
	 */
	public void add(FailedAuthzCacheUpdate failure)
	{
		if(failure != null && !this.failures.contains(failure))
			this.failures.add(failure);
	}

	
	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.pdp.cache.bean.AuthzCacheUpdateFailureRepository#clearFailures()
	 */
	public void clearFailures()
	{
		this.failures.clear();
	}

	
	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.pdp.cache.bean.AuthzCacheUpdateFailureRepository#getFailures()
	 */
	public List<FailedAuthzCacheUpdate> getFailures()
	{
		return this.failures;
	}

	
	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.pdp.cache.bean.AuthzCacheUpdateFailureRepository#remove(com.qut.middleware.esoe.pdp.cache.bean.FailedAuthzCacheUpdate)
	 */
	public void remove(FailedAuthzCacheUpdate failure)
	{
		this.failures.remove(failure);
	}

	
	public int getSize()
	{
		return(this.failures == null ? 0 : this.failures.size() );
	}
}
