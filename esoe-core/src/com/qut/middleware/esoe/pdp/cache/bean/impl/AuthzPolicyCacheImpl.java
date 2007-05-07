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
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.qut.middleware.esoe.pdp.cache.bean.AuthzPolicyCache;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Policy;


public class AuthzPolicyCacheImpl implements AuthzPolicyCache
{	

	private Map<String, Vector<Policy>> cache;
	
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock readLock = this.rwl.readLock();
    private final Lock writeLock = this.rwl.writeLock();
	
    
	/**
	 * Default constructor
	 */
	public AuthzPolicyCacheImpl()
	{
		this.cache = Collections.synchronizedMap(new HashMap<String, Vector<Policy>>());
	}
	
	
	/* Internal cache is stored as a map of (String) policyID -> (Policy) policy objects
	 * So we index then via policy ID for faster retrieval.
	 * 
	 * 
	 * @see com.qut.middleware.esoe.pdp.cache.bean.AuthzPolicyCache#add(com.qut.middleware.esoe.xml.lxacml.Policy)
	 */
	public void add(String descriptorID, Vector<Policy> policies)
	{
		try
		{
			this.writeLock.lock();
						
			this.cache.put(descriptorID, policies);
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
	public Map<String, Vector<Policy>> getCache()
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
	public Vector<Policy> getPolicies(String descriptorID)
	{		
		try
		{
			this.readLock.lock();
			
			return this.cache.get(descriptorID);
		}
		finally
		{
			this.readLock.unlock();
		}
		
	}


	/* 
	 * @see com.qut.middleware.esoe.pdp.cache.bean.AuthzPolicyCache#remove(com.qut.middleware.esoe.xml.lxacml.Policy)
	 */
	public boolean remove(String descriptorID)
	{
		try
		{
			this.writeLock.lock();
			
			return (this.cache.remove(descriptorID) != null);
		}
		finally
		{
			this.writeLock.unlock();
		}
	}


	/* 
	 * @see com.qut.middleware.esoe.pdp.cache.bean.AuthzPolicyCache#setCache(com.qut.middleware.esoe.xml.lxacml.Policy)
	 */
	public void setCache(Map<String, Vector<Policy>> newData)
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
