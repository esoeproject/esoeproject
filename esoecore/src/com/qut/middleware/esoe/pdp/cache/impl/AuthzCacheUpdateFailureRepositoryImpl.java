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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.qut.middleware.esoe.pdp.cache.AuthzCacheUpdateFailureRepository;
import com.qut.middleware.esoe.pdp.cache.bean.FailedAuthzCacheUpdate;

public class AuthzCacheUpdateFailureRepositoryImpl implements AuthzCacheUpdateFailureRepository
{

	private List<FailedAuthzCacheUpdate> failures;
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock readLock = this.rwl.readLock();
    private final Lock writeLock = this.rwl.writeLock();
	
	
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
		try
		{
			this.writeLock.lock();
		
			if(failure != null && !this.failures.contains(failure))
				this.failures.add(failure);				
		}
		finally
		{
			this.writeLock.unlock();
		}
	}

	
	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.pdp.cache.bean.AuthzCacheUpdateFailureRepository#clearFailures()
	 */
	public void clearFailures()
	{
		try
		{
			this.writeLock.lock();
		
			this.failures.clear();
		}
		finally
		{
			this.writeLock.unlock();
		}
	}

	
	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.pdp.cache.bean.AuthzCacheUpdateFailureRepository#getFailures()
	 * 
	 * NOTE: This method returns a clone of the unerlying list. Modifications to the list of failures should be
	 * performed via add() and remove().
	 */
	public List<FailedAuthzCacheUpdate> getFailures()
	{
		Vector<FailedAuthzCacheUpdate> clonedList = new Vector<FailedAuthzCacheUpdate>();
		
		try
		{
			this.readLock.lock();
			
			clonedList.addAll(this.failures);
		}
		finally
		{
			this.readLock.unlock();
		}
		
		return (Vector<FailedAuthzCacheUpdate>)clonedList.clone();
	}

	
	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.pdp.cache.bean.AuthzCacheUpdateFailureRepository#remove(com.qut.middleware.esoe.pdp.cache.bean.FailedAuthzCacheUpdate)
	 */
	public void remove(FailedAuthzCacheUpdate failure)
	{
		try
		{
			this.writeLock.lock();
		
			this.failures.remove(failure);
		}
		finally
		{
			this.writeLock.unlock();
		}
	}

	
	public int getSize()
	{
		try
		{
			this.readLock.lock();

			return(this.failures == null ? 0 : this.failures.size() );
		}
		finally
		{
			this.readLock.unlock();
		}
	}
}
