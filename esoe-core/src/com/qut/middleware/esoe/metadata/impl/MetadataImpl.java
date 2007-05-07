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
 * Creation Date: 25/10/2006
 * 
 * Purpose: Implements the Metadata interface.
 */
package com.qut.middleware.esoe.metadata.impl;

import java.security.PublicKey;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.qut.middleware.esoe.metadata.Metadata;
import com.qut.middleware.esoe.metadata.cache.MetadataCache;
import com.qut.middleware.esoe.metadata.exception.InvalidMetadataEndpointException;
import com.qut.middleware.esoe.spep.Messages;
import com.qut.middleware.saml2.exception.KeyResolutionException;
import com.qut.middleware.saml2.sec.KeyData;

/** Implements the Metadata interface. */
public class MetadataImpl implements Metadata
{
	protected static final int BUFFER_LEN = 4096;
	protected static final long SHORT_SLEEP = 20; // milliseconds. The length of time to wait before polling hasData again.

	private String esoeIdentifier;
	private MetadataCache metadataCache;
		
	/* Local logging instance */
	private Logger logger = Logger.getLogger(MetadataImpl.class.getName());
	
		
	/** Constructor.
	 * 
	 * @param esoeIdentifier The identifier of the ESOE.
	 * @param metadataCache The cache to obtain the metadata from.
	 * 
	 */
	public MetadataImpl(String esoeIdentifier, MetadataCache metadataCache)
	{
		if(esoeIdentifier == null)
		{
			throw new IllegalArgumentException("esoeIdentifier cannot be null"); //$NON-NLS-1$
		}		
		if(metadataCache == null)
		{
			throw new IllegalArgumentException("MetadataCache cannot be null"); //$NON-NLS-1$
		}
		
		this.esoeIdentifier = esoeIdentifier;
		this.metadataCache = metadataCache;
		
		this.logger.info(MessageFormat.format(Messages.getString("MetadataImpl.5"), esoeIdentifier)); //$NON-NLS-1$
		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.spep.Metadata#getCurrentRevision()
	 */
	public String getCurrentRevision()
	{
		return this.metadataCache.getCurrentRevision();
	}
	
	
	/** NOTE: implementation of this method will block calls if there is no initial data in the cache.
	 *  
	 * @see com.qut.middleware.esoe.spep.Metadata#obtainSigningKey(java.lang.String)
	 * 
	 * @return The PublicKey matching the keyname if exists, else null.
	 */
	public PublicKey resolveKey(String keyName) throws KeyResolutionException
	{
		MetadataCache.State state = this.metadataCache.getState();
		
		while(state == MetadataCache.State.UnInitialized)
		{
			try
			{
				state = this.metadataCache.getState();
				
				Thread.sleep(SHORT_SLEEP);
			}
			catch (InterruptedException e)
			{
				// we dont want this object blocking threads that are trying to shutdown
				return null;
			}
		}
				
		KeyData keyData = this.metadataCache.getKeyMap().get(keyName);
			
		if(keyData == null)
		{
			this.logger.debug(Messages.getString("MetadataImpl.7")); //$NON-NLS-1$
			return null;
		}
			
		return keyData.getPk();
				
	}

	/**	NOTE: implementation of this method will block calls if there is no initial data in the cache.
	 *  
	 * @see com.qut.middleware.esoe.spep.Metadata#resolveAssertionConsumerService(java.lang.String, java.lang.String)
	 */
	public String resolveAssertionConsumerService(String descriptorID, int index)
			throws InvalidMetadataEndpointException
	{
		MetadataCache.State state = this.metadataCache.getState();
		
		while(state == MetadataCache.State.UnInitialized)
		{
			try
			{
				state = this.metadataCache.getState();
				
				Thread.sleep(SHORT_SLEEP);
			}
			catch (InterruptedException e)
			{
				// we dont want this object blocking threads that are trying to shutdown
				return new String();
			}
		}
		
		Map<String, String> consumerServices = this.metadataCache.getAssertionConsumerServices();
			
		if(null != consumerServices)
		{
			String assertionConsumerServiceLocation = consumerServices.get(generateKey(descriptorID, index));

			if(assertionConsumerServiceLocation != null)
			{
				return assertionConsumerServiceLocation;
			}
		}
			
		this.logger.debug(MessageFormat.format(Messages.getString("MetadataImpl.9"), descriptorID, Integer.toString(index))); //$NON-NLS-1$
		throw new InvalidMetadataEndpointException();		
		
	}

	/** NOTE: implementation of this method will block calls if there is no initial data in the cache.
	 * 
	 * @see com.qut.middleware.esoe.spep.Metadata#resolveCacheClearService(java.lang.String)
	 */
	public Map<Integer,String> resolveCacheClearService(String descriptorID) throws InvalidMetadataEndpointException
	{
		MetadataCache.State state = this.metadataCache.getState();
			
		while(state == MetadataCache.State.UnInitialized)
		{
			try
			{
				state = this.metadataCache.getState();
			
				Thread.sleep(SHORT_SLEEP);
			}
			catch (InterruptedException e)
			{
				// we dont want this object blocking threads that are trying to shutdown
				return new HashMap<Integer,String>();
			}
		}
		
		Map<String, Map<Integer,String>> cacheClearServices = this.metadataCache.getCacheClearServices();
		
		if(null != cacheClearServices)
		{
			Map<Integer,String> cacheClearServiceLocations = cacheClearServices.get(descriptorID);
		
			if(cacheClearServiceLocations != null)
			{
				return cacheClearServiceLocations;
			}
		}
		
		this.logger.debug(MessageFormat.format(Messages.getString("MetadataImpl.11"), descriptorID)); //$NON-NLS-1$
		throw new InvalidMetadataEndpointException();
		
	}

	/** NOTE: implementation of this method will block calls if there is no initial data in the cache.
	 * 
	 * @see com.qut.middleware.esoe.spep.Metadata#resolveSingleLogoutService(java.lang.String)
	 */
	public List<String> resolveSingleLogoutService(String descriptorID) throws InvalidMetadataEndpointException
	{
		MetadataCache.State state = this.metadataCache.getState();
				
		while(state == MetadataCache.State.UnInitialized)
		{
			try
			{
				state = this.metadataCache.getState();
				
				Thread.sleep(SHORT_SLEEP);
			}
			catch (InterruptedException e)
			{
				// we dont want this object blocking threads that are trying to shutdown
				return new Vector<String>();
			}
		}
		
		Map<String, List<String>> singleLogoutServices = this.metadataCache.getSingleLogoutServices();
		
		if(null != singleLogoutServices)
		{		
			List<String> singleLogoutServiceLocations = singleLogoutServices.get(descriptorID);
			
			if(singleLogoutServiceLocations != null)
			{
				return singleLogoutServiceLocations;
			}
		}
		
		this.logger.debug(MessageFormat.format(Messages.getString("MetadataImpl.13"), descriptorID)); //$NON-NLS-1$
		throw new InvalidMetadataEndpointException();
	
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.metadata.Metadata#getESOEIdentifier()
	 */
	public String getESOEIdentifier()
	{
		return this.esoeIdentifier;
	}
	
	/*
	 * 
	 */
	private String generateKey(String id, int index)
	{
		return Integer.toString(index) + ":" + id; //$NON-NLS-1$
	}
}
