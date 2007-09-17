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
 * Creation Date: 12/10/2006
 * 
 * Purpose: Implements the AuthzPolicyCache interface
 */
package com.qut.middleware.esoe.pdp.cache.bean.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.qut.middleware.esoe.pdp.cache.bean.AuthzPolicyCache;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Policy;


public class AuthzPolicyCacheImpl implements AuthzPolicyCache
{	
	private Map<String, List<Policy>> cache;
	
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock readLock = this.rwl.readLock();
    private final Lock writeLock = this.rwl.writeLock();
	private long sequenceId;
    
	/**
	 * Default constructor
	 */
	public AuthzPolicyCacheImpl()
	{		
		this.cache = Collections.synchronizedMap(new HashMap<String, List<Policy>>());
		this.sequenceId = SEQUENCE_UNINITIALIZED;
	}
	
	
	/* Internal cache is stored as a map of (String) descriptorID -> List of Policy objects. NOTE: The List of policies 
	 * stored is a clone of the given list.<br>
	 * 
	 * PRE: policies is not null.
	 * 
	 * 
	 * @see com.qut.middleware.esoe.pdp.cache.bean.AuthzPolicyCache#add(com.qut.middleware.esoe.xml.lxacml.Policy)
	 */
	public void add(String entityID, List<Policy> policies)
	{
		try
		{
			this.writeLock.lock();
						
			Vector<Policy> clonedList = new Vector<Policy>();
			
			if(policies != null)
				clonedList.addAll(policies);
			
			this.cache.put(entityID, (Vector<Policy>)clonedList.clone());
		}
		finally
		{
			this.writeLock.unlock();
		}
	}
	

	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.pdp.cache.bean.AuthzPolicyCache#getCache()
	 */
	public Map<String, List<Policy>> getCache()
	{
		try
		{
			this.readLock.lock();			
						
			return this.cache;
		}
		finally
		{
			this.readLock.unlock();
		}
	}
	

	/*
	 * @see com.qut.middleware.esoe.pdp.cache.bean.AuthzPolicyCache#getPolicy(java.lang.String)
	 */
	public List<Policy> getPolicies(String entityID)
	{		
		try
		{
			this.readLock.lock();
			
			Vector<Policy> clonedList = new Vector<Policy>();
			List<Policy>policies = this.cache.get(entityID);
		
			if(policies != null)
				clonedList.addAll(policies);
			
			return (Vector<Policy>)clonedList.clone();						
		}
		finally
		{
			this.readLock.unlock();
		}
		
	}


	/* 
	 * @see com.qut.middleware.esoe.pdp.cache.bean.AuthzPolicyCache#remove(com.qut.middleware.esoe.xml.lxacml.Policy)
	 */
	public boolean remove(String entityID)
	{
		try
		{
			this.writeLock.lock();
			
			return (this.cache.remove(entityID) != null);
		}
		finally
		{
			this.writeLock.unlock();
		}
	}


	/* 
	 * @see com.qut.middleware.esoe.pdp.cache.bean.AuthzPolicyCache#setCache(com.qut.middleware.esoe.xml.lxacml.Policy)
	 */
	public void setCache(Map<String, List<Policy>> newData)
	{
		try
		{
			this.writeLock.lock();
			
			if(newData != null)
				this.cache = newData;
		}
		finally
		{
			this.writeLock.unlock();
		}
		
	}


	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.pdp.cache.bean.AuthzPolicyCache#getBuildSequenceId()
	 */
	public long getBuildSequenceId()
	{
		try
		{
			this.readLock.lock();
		
			return this.sequenceId;
		}
		finally
		{
			this.readLock.unlock();
		}
		
	}

	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.pdp.cache.bean.AuthzPolicyCache#setBuildSequenceId(long)
	 */
	public void setBuildSequenceId(long sequenceId)
	{
		try
		{
			this.writeLock.lock();
		
			this.sequenceId = sequenceId;
		}
		finally
		{
			this.writeLock.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.pdp.cache.bean.AuthzPolicyCache#getSize()
	 */
	public int getSize()
	{
		try
		{
			this.readLock.lock();
		
			return this.cache.size();
		}
		finally
		{
			this.readLock.unlock();
		}
	}

}
