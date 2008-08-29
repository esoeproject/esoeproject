/*
 * Copyright 2008, Queensland University of Technology
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
 * Creation Date: 15/04/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.metadata.cache.impl;

import java.math.BigInteger;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.crypto.IssuerSerialPair;
import com.qut.middleware.crypto.impl.IssuerSerialPairImpl;
import com.qut.middleware.metadata.bean.EntityData;
import com.qut.middleware.metadata.bean.KeyEntry;
import com.qut.middleware.metadata.bean.impl.EntityDataImpl;
import com.qut.middleware.metadata.bean.impl.NullEntityRole;
import com.qut.middleware.metadata.cache.MetadataCache;
import com.qut.middleware.metadata.processor.DynamicMetadataUpdater;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.metadata.source.DynamicMetadataSource;
import com.qut.middleware.metadata.source.MetadataSource;
import com.qut.middleware.saml2.exception.KeyResolutionException;

public class MetadataCacheImpl implements MetadataCache
{
	private static String nullEntitySourceLocation = "local:metadataCache:cachedNull";
	/* Default expiry interval - 24 hours (in milliseconds) */
	private static long defaultExpiryInterval = (24L * 60L * 60L * 1000L);
	
	private ReentrantReadWriteLock lock;
	private Map<String,EntityData> entityMap;
	private Map<String,KeyEntry> keyAliasMap;
	private Map<IssuerSerialPair,KeyEntry> keyMap;
	
	private DynamicMetadataUpdater dynamicMetadataUpdater;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private long expiryInterval;
	
	public MetadataCacheImpl(DynamicMetadataUpdater dynamicMetadataUpdater)
	{
		this.lock = new ReentrantReadWriteLock();
		this.entityMap = new HashMap<String, EntityData>();
		this.keyAliasMap = new HashMap<String, KeyEntry>();
		this.keyMap = new HashMap<IssuerSerialPair, KeyEntry>();
		this.dynamicMetadataUpdater = dynamicMetadataUpdater;
		this.expiryInterval = defaultExpiryInterval;
	}
	
	public EntityData getEntityData(String entityID, MetadataProcessor processor)
	{
		/* 
		 * Care must be taken not to call to another class while holding the read lock.
		 * If a thread that holds a read lock attempts to get the write lock at the same
		 * time, it will never succeed.
		 */
		this.lock.readLock().lock();
		
		EntityData entityData;
		try
		{
			entityData = this.entityMap.get(entityID);
		}
		finally
		{
			this.lock.readLock().unlock();
		}
		
		if (entityData == null)
		{
			entityData = this.dynamicMetadataUpdater.dynamicUpdate(processor, entityID);
			if (entityData == null)
			{
				EntityData nullData = new EntityDataImpl(MetadataCacheImpl.nullEntitySourceLocation, Integer.MIN_VALUE);
				nullData.addRoleData(new NullEntityRole());
				
				this.lock.writeLock().lock();
				try
				{
					EntityData updatedData = this.entityMap.get(entityID);
					if (updatedData == null)
					{
						this.entityMap.put(entityID, nullData);
					}
					else
					{
						// It has been updated in the meantime. Return it.
						if (updatedData.getRoleData(NullEntityRole.class) != null)
							return updatedData;
						
						// If it's a cached null, return null.
						return null;
					}
				}
				finally
				{
					this.lock.writeLock().unlock();
				}
			}
		}
		else
		{
			// Check if the entity has been marked as a cached null.
			if (entityData.getRoleData(NullEntityRole.class) != null)
			{
				entityData = null;
			}
		}
		
		return entityData;
	}

	public void update(MetadataSource source, List<EntityData> entities, List<KeyEntry> newKeyMap)
	{
		if (source == null)
		{
			this.logger.error("Provided metadata source was null. Unable to process.");
			throw new IllegalArgumentException("Provided metadata source was null. Unable to process.");
		}
		
		if (entities == null)
		{
			this.logger.error("Provided entities list was null for metadata source " + source.getLocation());
			throw new IllegalArgumentException("Provided entities list was null for metadata source " + source.getLocation());
		}
		
		if (newKeyMap == null)
		{
			this.logger.error("Provided key map was null for metadata source " + source.getLocation());
			throw new IllegalArgumentException("Provided key map was null for metadata source " + source.getLocation());
		}
		
		String location = source.getLocation();
		long startTime = System.currentTimeMillis();
		// Get the size before locking, because depending on the implementation of the list it might not be O(1)
		int size = entities.size();
		int keySize = newKeyMap.size();
		
		this.logger.debug("Locking for metadata cache update from source {}", location);
		
		this.lock.writeLock().lock();
		try
		{
			this.logger.debug("Got lock. Updating metadata cache with {} entries from source {}", size, location);
			
			// Remove previous entries from the metadata source being updated.
			Set<Map.Entry<String,EntityData>> entrySet = this.entityMap.entrySet();
			Iterator<Map.Entry<String,EntityData>> iterator = entrySet.iterator();
			while (iterator.hasNext())
			{
				Map.Entry<String,EntityData> entry = iterator.next();
				if (entry.getValue().getMetadataSourceLocation().equals(location))
				{
					iterator.remove();
				}
			}
			
			// Cache the update, replacing all entries with a lower priority
			for (EntityData entity : entities)
			{
				this.logger.debug("Adding entity {} from source {} to metadata cache.", entity.getEntityID(), source.getLocation());
				EntityData oldEntity = this.entityMap.get(entity.getEntityID());
				if (oldEntity == null || oldEntity.getPriority() < entity.getPriority())
				{
					this.entityMap.put(entity.getEntityID(), entity);
				}
				else
				{
					this.logger.debug("Couldn't add entity {} from source {} to metadata cache - entity already exists.", entity.getEntityID(), source.getLocation());
				}
			}

			this.logger.debug("Updating key cache with {} key entries from source {}", keySize, location);
			
			// Remove previous key entries from the metadata source being updated.
			Set<Map.Entry<String,KeyEntry>> keyAliasEntrySet = this.keyAliasMap.entrySet();
			Iterator<Map.Entry<String,KeyEntry>> keyAliasIterator = keyAliasEntrySet.iterator();
			while (keyAliasIterator.hasNext())
			{
				KeyEntry keyEntry = keyAliasIterator.next().getValue();
				if (source.getLocation().equals(keyEntry.getMetadataSourceLocation()))
				{
					keyAliasIterator.remove();
				}
			}
			
			Set<Map.Entry<IssuerSerialPair, KeyEntry>> keyEntrySet = this.keyMap.entrySet();
			Iterator<Entry<IssuerSerialPair, KeyEntry>> keyIterator = keyEntrySet.iterator();
			while (keyIterator.hasNext())
			{
				KeyEntry keyEntry = keyIterator.next().getValue();
				if (source.getLocation().equals(keyEntry.getMetadataSourceLocation()))
				{
					keyIterator.remove();
				}
			}
			
			// Cache the update, replacing all entries with a lower priority
			for (KeyEntry keyEntry : newKeyMap)
			{
				String keyAlias = keyEntry.getKeyAlias();
				String issuerDN = keyEntry.getIssuerDN();
				BigInteger serialNumber = keyEntry.getSerialNumber();
				
				// If the key name is null, don't add it to the key alias map
				if (keyAlias == null)
				{
					this.logger.debug("Found key with no name from source {} - Not adding to key alias map.", source.getLocation());
				}
				else
				{
					KeyEntry existing = this.keyAliasMap.get(keyAlias);
					// Additional condition - if the existing element is of the same priority, assume it's the same
					// source and overwrite it anyway.
					if (existing == null || existing.getPriority() < keyEntry.getPriority())
					{
						this.logger.debug("Found key with name '{}' from source {} - Added to key alias map.", keyAlias, source.getLocation());
						this.keyAliasMap.put(keyAlias, keyEntry);
					}
					else
					{
						this.logger.debug("Couldn't add key with name '{}' from source {} - Key is already cached from a higher/equal priority source.", keyAlias, source.getLocation());
					}
				}
				
				// If either of the values is null, don't add it to the key issuer/serial map
				if (issuerDN == null || serialNumber == null)
				{
					this.logger.debug("Found key with no issuer/serial from source {} - Not adding to key issuer/serial map.", source.getLocation());
				}
				else
				{
					IssuerSerialPair issuerSerialPair = new IssuerSerialPairImpl(issuerDN, serialNumber);
					KeyEntry existing = this.keyMap.get(issuerSerialPair);
					// Additional condition - if the existing element is of the same priority, assume it's the same
					// source and overwrite it anyway.
					if (existing == null || existing.getPriority() < keyEntry.getPriority())
					{
						this.logger.debug("Found key with issuer '{}' serial '{}' from source {} - Added to key alias map.", 
								new Object[]{issuerDN, String.valueOf(serialNumber), source.getLocation()});
						this.keyMap.put(issuerSerialPair, keyEntry);
					}
					else
					{
						this.logger.debug("Found key with issuer '{}' serial '{}' from source {} - Key is already cached from a higher/equal priority source.", 
								new Object[]{issuerDN, String.valueOf(serialNumber), source.getLocation()});
					}
				}
			}
		}
		finally
		{
			this.lock.writeLock().unlock();
		}

		long endTime = System.currentTimeMillis();
		this.logger.info("Finished updating metadata cache with {} entries and {} key entries from metadata source {}. Update took {} ms.", 
				new Object[]{size, keySize, location, (endTime - startTime)});
	}

	public void dynamicUpdate(DynamicMetadataSource source, List<EntityData> entities, List<KeyEntry> newKeyMap)
	{
		if (source == null)
		{
			this.logger.error("Provided metadata source was null. Unable to process.");
			throw new IllegalArgumentException("Provided metadata source was null. Unable to process.");
		}
		
		if (entities == null)
		{
			this.logger.error("Provided entities list was null for dynamic metadata source");
			throw new IllegalArgumentException("Provided entities list was null for dynamic metadata source");
		}
		
		if (newKeyMap == null)
		{
			this.logger.error("Provided key map was null for dynamic metadata source");
			throw new IllegalArgumentException("Provided key map was null for dynamic metadata source");
		}
		
		long startTime = System.currentTimeMillis();
		long expiryTime = startTime + this.expiryInterval;
		
		// Detect pre-expiry and return gracefully.
		if (expiryTime < startTime)
		{
			if (this.expiryInterval > 0)
			{
				this.logger.warn("New dynamic entity data is pre-expired. This appears to be caused by an overflow error. Check the configured expiry interval.");
			}
			
			return;
		}
		// Get the size before locking, because depending on the implementation of the list it might not be O(1)
		int size = entities.size();
		int keySize = newKeyMap.size();
		
		this.logger.debug("Locking for metadata cache update from dynamic source");
		
		this.lock.writeLock().lock();
		try
		{
			this.logger.debug("Got lock. Updating metadata cache with {} dynamic entries", size);
			
			for (EntityData entity : entities)
			{
				EntityData oldEntity = this.entityMap.get(entity.getEntityID());
				// Additional condition - if the existing element is of the same priority, assume it's the same
				// source and overwrite it anyway.
				if (oldEntity == null || oldEntity.getPriority() <= entity.getPriority())
				{
					entity.setExpiryTimeMillis(expiryTime);
					this.entityMap.put(entity.getEntityID(), entity);
				}
			}
			
			for (KeyEntry keyEntry : newKeyMap)
			{
				String keyAlias = keyEntry.getKeyAlias();
				String issuerDN = keyEntry.getIssuerDN();
				BigInteger serialNumber = keyEntry.getSerialNumber();
				
				// If the key name is null, don't add it to the key alias map
				if (keyAlias == null)
				{
					this.logger.debug("Found key with no name from dynamic source. Not adding to key alias map.");
				}
				else
				{
					KeyEntry existing = this.keyAliasMap.get(keyAlias);
					// Additional condition - if the existing element is of the same priority, assume it's the same
					// source and overwrite it anyway.
					if (existing == null || existing.getPriority() <= keyEntry.getPriority())
					{
						this.logger.debug("Found key with name '{}' from dynamic source - Added to key alias map.", keyAlias);
						this.keyAliasMap.put(keyAlias, keyEntry);
					}
					else
					{
						this.logger.debug("Couldn't add key with name '{}' from dynamic source - Key is already cached from a higher priority source.", keyAlias);
					}
				}
				
				// If either of the values is null, don't add it to the key issuer/serial map
				if (issuerDN == null || serialNumber == null)
				{
					this.logger.debug("Found key with no issuer/serial from dynamic source. Not adding to key issuer/serial map.");
				}
				else
				{
					IssuerSerialPair issuerSerialPair = new IssuerSerialPairImpl(issuerDN, serialNumber);
					KeyEntry existing = this.keyMap.get(issuerSerialPair);
					// Additional condition - if the existing element is of the same priority, assume it's the same
					// source and overwrite it anyway.
					if (existing == null || existing.getPriority() <= keyEntry.getPriority())
					{
						this.logger.debug("Found key with issuer '{}' serial '{}' from dynamic source - Added to key alias map.", issuerDN, String.valueOf(serialNumber));
						this.keyMap.put(issuerSerialPair, keyEntry);
					}
					else
					{
						this.logger.debug("Couldn't add key with issuer '{}' serial '{}' from dynamic source - Key is already cached from a higher priority source.", issuerDN, String.valueOf(serialNumber));
					}
				}
			}
		}
		finally
		{
			this.lock.writeLock().unlock();
		}

		long endTime = System.currentTimeMillis();
		this.logger.info("Finished updating metadata cache with {} entries and {} key entries from dynamic metadata source. Update took {} ms. New entries will expire at {}", 
				new Object[]{size, keySize, (endTime - startTime), new Date(expiryTime).toString()});
	}

	public void setDynamicEntityExpiryInterval(long expiryInterval)
	{
		this.expiryInterval = expiryInterval;
	}

	public PublicKey resolveKey(String keyName) throws KeyResolutionException
	{
		KeyEntry entry = this.keyAliasMap.get(keyName);
		if (entry != null)
		{
			this.logger.debug("Resolving key by name '{}' yielded key from source {}", keyName, entry.getMetadataSourceLocation());
			return entry.getPublicKey();
		}

		this.logger.debug("Resolving key by name '{}' failed.", keyName);
		throw new KeyResolutionException("Key with name '" + keyName + "' could not be resolved by name");
	}
	
	public PublicKey resolveKey(String issuerDN, BigInteger serialNumber) throws KeyResolutionException
	{
		IssuerSerialPair issuerSerialPair = new IssuerSerialPairImpl(issuerDN, serialNumber);
		KeyEntry entry = this.keyMap.get(issuerSerialPair);
		
		if (entry != null)
		{
			this.logger.debug("Resolving key by issuer '{}' serial '{}' yielded key from source {}", 
					new Object[]{issuerDN, serialNumber, entry.getMetadataSourceLocation()});
			return entry.getPublicKey();
		}
		this.logger.debug("Resolving key by issuer '{}', serial '{}' failed.", issuerDN, String.valueOf(serialNumber));
		throw new KeyResolutionException("Key with name issuer '" + issuerDN + "', serial '" + String.valueOf(serialNumber) + "' could not be resolved by issuer/serial");
	}
}
