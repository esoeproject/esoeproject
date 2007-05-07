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
 * Purpose: Metadata Cache default implementation
 */
package com.qut.middleware.esoemanager.metadata.impl;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.qut.middleware.esoemanager.metadata.MetadataCache;

public class MetadataCacheImpl implements MetadataCache
{

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock readLock = this.rwl.readLock();
    private final Lock writeLock = this.rwl.writeLock();
    
    private String cacheData;
	
	
	/** Initlializes this object with a null value for cached metadata
	 * 
	 */
	public MetadataCacheImpl()
	{
		this.cacheData = null;
	}
	

	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoemanager.metadata.MetadataCache#hasData()
	 */
	public boolean hasData() 
	{
		try
		{
			this.readLock.lock();
			
			return (this.cacheData != null );
		}
		finally
		{
			this.readLock.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoemanager.metadata.MetadataCache#setCacheData(String)
	 *
	 * @pre cachedata != null
	 */
	public void setCacheData(String cachedata)
	{
		try
		{
			this.writeLock.lock();
			
			if(null != cachedata)
				this.cacheData = cachedata;
		}
		finally
		{
			this.writeLock.unlock();
		}
	}


	/* (non-Javadoc)
	 * @see com.qut.middleware.esoemanager.metadata.MetadataCache#getCacheData()
	 */
	public String getCacheData()
	{
		try
		{
			this.readLock.lock();
			
			return (this.cacheData);
		}
		finally
		{
			this.readLock.unlock();
		}
	}
}
