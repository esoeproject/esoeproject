package com.qut.middleware.esoe.metadata.cache.impl;

import java.util.List;
import java.util.Map;

import com.qut.middleware.esoe.metadata.cache.CacheData;
import com.qut.middleware.esoe.metadata.cache.MetadataCache.State;
import com.qut.middleware.saml2.sec.KeyData;

public class CacheDataImpl implements CacheData
{
	private Map<String,List<String>> logoutServices;
	private Map<String, String> assertionConsumerServices;
	private Map<String,Map<Integer,String>> cacheClearServices;
	private Map<String, KeyData> keyData;
	private String currentRevision;
	private State state;
	
	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.metadata.cache.CacheData#setSingleLogoutServices(java.util.Map)
	 */
	public void setSingleLogoutServices(Map<String,List<String>> logoutServices)
	{
		this.logoutServices = logoutServices;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.metadata.cache.CacheData#getSingleLogoutServices()
	 */
	public Map<String,List<String>> getSingleLogoutServices()
	{
		return this.logoutServices;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.metadata.cache.CacheData#setAssertionConsumerServices(java.util.Map)
	 */
	public void setAssertionConsumerServices(Map<String, String> assertionConsumerServices)
	{
		this.assertionConsumerServices = assertionConsumerServices;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.metadata.cache.CacheData#getAssertionConsumerServices()
	 */
	public Map<String, String> getAssertionConsumerServices()
	{
		return this.assertionConsumerServices;
	}	
	
	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.metadata.cache.CacheData#setCacheClearServices(java.util.Map)
	 */
	public void setCacheClearServices(Map<String,Map<Integer,String>> cacheClearServices)
	{
		this.cacheClearServices = cacheClearServices;
	}
		
	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.metadata.cache.CacheData#getCacheClearServices()
	 */
	public Map<String,Map<Integer,String>> getCacheClearServices()
	{
		return this.cacheClearServices;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.metadata.cache.CacheData#setCurrentRevision(java.lang.String)
	 */
	public void setCurrentRevision(String revision)
	{
		this.currentRevision = revision;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.metadata.cache.CacheData#getCurrentRevision()
	 */
	public String getCurrentRevision()
	{
		return this.currentRevision;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.metadata.cache.CacheData#getKeyMap()
	 */
	public Map<String, KeyData> getKeyMap()
	{
		return this.keyData;
	}

	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.metadata.cache.CacheData#setKeyMap(java.util.Map)
	 */
	public void setKeyMap(Map<String, KeyData> keyData)
	{
		this.keyData = keyData;
	}

	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.metadata.cache.CacheData#getState()
	 */
	public State getState()
	{
		return this.state;
	}

	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.metadata.cache.CacheData#setState(com.qut.middleware.esoe.metadata.cache.MetadataCache.State)
	 */
	public void setState(State state)
	{
		this.state = state;
	}
	
	
}
