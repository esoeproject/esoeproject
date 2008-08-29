/* Copyright 2008, Queensland University of Technology
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
 */
package com.qut.middleware.esoemanager.metadata.logic.impl;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.qut.middleware.esoemanager.metadata.logic.MetadataCache;

public class MetadataCacheImpl implements MetadataCache
{

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock readLock = this.rwl.readLock();
    private final Lock writeLock = this.rwl.writeLock();
    
    private byte[] completeMD;
    private byte[] samlMD;
	
	
	/** Initlializes this object with a null value for cached metadata
	 * 
	 */
	public MetadataCacheImpl()
	{
		this.completeMD = null;
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
			
			return (this.completeMD != null );
		}
		finally
		{
			this.readLock.unlock();
		}
	}

	public void setCompleteMD(byte[] cachedata)
	{
		try
		{
			this.writeLock.lock();
			
			if(null != cachedata)
				this.completeMD = cachedata;
		}
		finally
		{
			this.writeLock.unlock();
		}
	}


	public byte[] getCompleteMD()
	{
		try
		{
			this.readLock.lock();
			
			return (this.completeMD);
		}
		finally
		{
			this.readLock.unlock();
		}
	}
	
	public void setSamlMD(byte[] cachedata)
	{
		try
		{
			this.writeLock.lock();
			
			if(null != cachedata)
				this.samlMD = cachedata;
		}
		finally
		{
			this.writeLock.unlock();
		}
	}


	public byte[] getSamlMD()
	{
		try
		{
			this.readLock.lock();
			
			return (this.samlMD);
		}
		finally
		{
			this.readLock.unlock();
		}
	}
}
