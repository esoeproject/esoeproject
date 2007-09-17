package com.qut.middleware.esoe.metadata.cache.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.qut.middleware.esoe.metadata.cache.CacheData;
import com.qut.middleware.esoe.metadata.cache.MetadataCache;
import com.qut.middleware.saml2.sec.KeyData;

public class MetadataCacheImpl implements MetadataCache
{

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock readLock = this.rwl.readLock();
    private final Lock writeLock = this.rwl.writeLock();
    private State state;
	private CacheData data;
	
	
	/** Initlializes this object with a new, but empty internal CacheData object. State is set
	 * to State.Uninitialized.
	 * 
	 */
	public MetadataCacheImpl()
	{
		this.setCacheData(new CacheDataImpl());
		this.setState(MetadataCache.State.UnInitialized);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.metadata.cache.MetadataCache#getAssertionConsumerServices()
	 */
	public Map<String, String> getAssertionConsumerServices()
	{
		try
		{
			this.readLock.lock();
			
			return this.data.getAssertionConsumerServices();
		}
		finally
		{
			this.readLock.unlock();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.metadata.cache.MetadataCache#getAssertionConsumerServiceIdentifierTypes()
	 */
	public Map<String, List<String>> getAssertionConsumerServiceIdentifierTypes()
	{
		try
		{
			this.readLock.lock();
			
			return this.data.getAssertionConsumerServiceIdentifierTypes();
		}
		finally
		{
			this.readLock.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.metadata.cache.MetadataCache#getCacheClearServices()
	 */
	public Map<String, Map<Integer,String>> getCacheClearServices()
	{
		try
		{
			this.readLock.lock();
			
			return this.data.getCacheClearServices();
		}
		finally
		{
			this.readLock.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.metadata.cache.MetadataCache#getCurrentRevision()
	 */
	public String getCurrentRevision()
	{
		try
		{
			this.readLock.lock();
			
			return this.data.getCurrentRevision();
		}
		finally
		{
			this.readLock.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.metadata.cache.MetadataCache#getSingleLogoutServices()
	 */
	public Map<String, List<String>> getSingleLogoutServices()
	{
		try
		{
			this.readLock.lock();
			
			return this.data.getSingleLogoutServices();
		}
		finally
		{
			this.readLock.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.metadata.cache.MetadataCache#hasData()
	 */
	public boolean hasData() 
	{
		try
		{
			this.readLock.lock();
			
			return (this.data.getAssertionConsumerServices() != null ||
					this.data.getCacheClearServices() != null ||
					this.data.getSingleLogoutServices() != null );
		}
		finally
		{
			this.readLock.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.metadata.cache.MetadataCache#setCacheData(com.qut.middleware.esoe.metadata.cache.CacheData)
	 *
	 * @pre cachedata != null
	 */
	public void setCacheData(CacheData cachedata)
	{
		try
		{
			this.writeLock.lock();
			
			if(null != cachedata)
				this.data = cachedata;
		}
		finally
		{
			this.writeLock.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.metadata.cache.MetadataCache#getKeyMap()
	 */
	public Map<String, KeyData> getKeyMap()
	{
		return this.data.getKeyMap();
	}
	
	
	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.metadata.cache.MetadataCache#setState(com.qut.middleware.esoe.metadata.cache.MetadataCache.State)
	 */
	public void setState(State cacheState)
	{
		try
		{
			this.writeLock.lock();
			
			this.state = cacheState;
		}
		finally
		{
			this.writeLock.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.metadata.cache.MetadataCache#getState()
	 */
	public State getState()
	{
		State state;
		
		try
		{
			this.readLock.lock();
			
			state = this.state;
		}
		finally
		{
			this.readLock.unlock();
		}
		
		return state;
	}

}
