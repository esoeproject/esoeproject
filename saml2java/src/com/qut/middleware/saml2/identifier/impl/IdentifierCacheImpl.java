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
 * Creation Date: 20/10/2006
 * 
 * Purpose: Implements IdentifierCache interface to prevent replay attacks.
 */
package com.qut.middleware.saml2.identifier.impl;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.saml2.identifier.IdentifierCache;
import com.qut.middleware.saml2.identifier.exception.IdentifierCollisionException;

/** Implements IdentifierCache interface to prevent replay attacks. */
public class IdentifierCacheImpl implements IdentifierCache
{
	private ConcurrentMap<String, Date> cache;
	private ReentrantLock lock;
	
	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(IdentifierCacheImpl.class.getName());

	/**
	 * Default constructor.
	 */
	public IdentifierCacheImpl()
	{		
		this.cache = new ConcurrentHashMap<String, Date>();
		this.lock = new ReentrantLock();

		this.logger
				.info(Messages.getString("IdentifierCacheImpl.6")); //$NON-NLS-1$ 
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.core.identifier.IdentifierCache#registerIdentifier(java.lang.String)
	 */
	public void registerIdentifier(String identifier) throws IdentifierCollisionException
	{
		this.logger.debug(Messages.getString("IdentifierCacheImpl.8")); //$NON-NLS-1$

		this.lock.lock();
		try
		{
			if (this.containsIdentifier(identifier))
			{
				this.logger.error(Messages.getString("IdentifierCacheImpl.9")); //$NON-NLS-1$
				throw new IdentifierCollisionException(Messages.getString("IdentifierCacheImpl.9")); //$NON-NLS-1$
			}
			
			if(identifier != null)
				this.cache.put(identifier, new Date());
		}
		finally
		{
			this.lock.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.saml2.identifier.IdentifierCache#containsIdentifier(java.lang.String)
	 */
	public boolean containsIdentifier(String identifier)
	{
		this.lock.lock();
		try
		{
			return this.cache.containsKey(identifier);
		}
		finally
		{
			this.lock.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.saml2.identifier.IdentifierCache#cleanCache()
	 */
	public int cleanCache(int age)
	{
		int numRemoved = 0;
		
		Set<Entry<String, Date>> entryList = this.cache.entrySet();
		Iterator<Entry<String, Date>> entryIterator = entryList.iterator();
		
		while (entryIterator.hasNext())
		{
			Entry<String, Date> entry = entryIterator.next();

			long now = new Date().getTime();
			long expire = (entry.getValue().getTime() + (age));

			if (expire < now)
			{
				this.logger.debug(Messages.getString("IdentifierCacheImpl.4")); //$NON-NLS-1$
				entryIterator.remove();
				numRemoved ++;
			}
		}
		
		return numRemoved;
	}
	
	
}
