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

package com.qut.middleware.metadata.processor.impl;

import java.math.BigInteger;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.metadata.bean.EntityData;
import com.qut.middleware.metadata.bean.Role;
import com.qut.middleware.metadata.cache.MetadataCache;
import com.qut.middleware.metadata.exception.InvalidMetadataException;
import com.qut.middleware.metadata.exception.MetadataCacheUpdateException;
import com.qut.middleware.metadata.exception.MetadataSourceException;
import com.qut.middleware.metadata.exception.MetadataStateException;
import com.qut.middleware.metadata.processor.FormatHandler;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.metadata.source.DynamicMetadataSource;
import com.qut.middleware.metadata.source.MetadataSource;
import com.qut.middleware.saml2.exception.KeyResolutionException;

public class MetadataProcessorImpl implements MetadataProcessor
{
	private MetadataCache cache;
	private List<FormatHandler> formatHandlers;
	private List<MetadataSource> sources;
	private List<String> haltingSources;

	private Logger logger = LoggerFactory.getLogger(MetadataProcessorImpl.class);
	
	public MetadataProcessorImpl(MetadataCache cache, List<FormatHandler> formatHandlers, List<MetadataSource> sources)
	{
		this.cache = cache;
		this.formatHandlers = formatHandlers;
		this.sources = sources;
		
		this.haltingSources = new ArrayList<String>();
		for (MetadataSource source : this.sources)
		{
			if (source.isMandatory())
			{
				this.logger.debug("Adding mandatory metadata source {} with format {} to list.", source.getLocation(), source.getFormat());
				this.haltingSources.add(source.getLocation());
			}
		}
	}
	
	public EntityData getEntityData(String entityID) throws MetadataStateException
	{
		if (this.haltingSources.size() == 0)
			return this.cache.getEntityData(entityID, this);
		
		throw new MetadataStateException("Mandatory metadata sources have not been loaded successfully.");
	}

	public <T extends Role> T getEntityRoleData(String entityID, Class<T> clazz) throws MetadataStateException
	{
		EntityData entityData = this.getEntityData(entityID);
		if (entityData == null) return null;
		
		return entityData.getRoleData(clazz);
	}
	
	public void update()
	{
		for (MetadataSource source : this.sources)
		{
			try
			{
				source.updateMetadata(this);
				this.logger.debug("Successfully checked for updated metadata from source {}", source.getLocation());
				if (source.isMandatory()) this.haltingSources.remove(source.getLocation());
			}
			catch (MetadataSourceException e)
			{
				if (source.isMandatory() && this.haltingSources.contains(source.getLocation()))
				{
					this.logger.error("Failed metadata update from source {}. This is a mandatory source, " +
							"and has NOT had a successful update yet. This error WILL prevent normal operation.", 
							source.getLocation());
					this.logger.error("Failed metadata update exception", e);
				}
				else
				{
					this.logger.warn("Failed metadata update from source {}. This is not a mandatory source. Exception follows", source.getLocation());
					this.logger.warn("Failed metadata update exception", e);
				}
			}
		}
	}
	
	public void updateFromSource(MetadataSource source, byte[] document) throws MetadataCacheUpdateException
	{
		for (FormatHandler handler : this.formatHandlers)
		{
			if (handler.canHandle(source))
			{
				try
				{
					this.logger.debug("Found a format handler for source at location: {}  format: {}", source.getLocation(), source.getFormat());
					handler.updateCache(source, this.cache, document);
					return;
				}
				catch (InvalidMetadataException e)
				{
					String message = "Metadata document was deemed invalid by the format handler. Metadata source location: " + source.getLocation() + " .. format: " + source.getFormat() + " .. message was: " + e.getMessage();
					this.logger.error(message);
					this.logger.debug(message, e);
					throw new MetadataCacheUpdateException(message, e);
				}
			}
		}
		
		this.logger.error(
			"No format handler available for format {}. Discarding update from metadata source {}", 
			source.getFormat(), source.getLocation()
		);
		
		throw new MetadataCacheUpdateException("No format handler was available. Discarding metadata update from source " + source.getLocation());
	}

	public EntityData updateFromDynamicSource(DynamicMetadataSource source, String entityID, byte[] document) throws MetadataCacheUpdateException
	{
		for (FormatHandler handler : this.formatHandlers)
		{
			if (handler.canHandle(source))
			{
				try
				{
					this.logger.debug("Found a format handler for dynamic source format: {}", source.getFormat());
					return handler.dynamicUpdateCache(source, this.cache, entityID, document);
				}
				catch (InvalidMetadataException e)
				{
					String message = "Metadata document was deemed invalid by the format handler. Dynamic metadata source format: " + source.getFormat() + " .. message was: " + e.getMessage();
					this.logger.error(message);
					this.logger.debug(message, e);
					throw new MetadataCacheUpdateException(message, e);
				}
			}
		}
		
		this.logger.error(
			"No format handler available for format {}. Discarding update from dynamic metadata source", 
			source.getFormat()
		);
		
		throw new MetadataCacheUpdateException("No format handler was available. Discarding metadata update from dynamic source");
	}

	public PublicKey resolveKey(String keyName) throws KeyResolutionException
	{
		return this.cache.resolveKey(keyName);
	}
	
	public PublicKey resolveKey(String issuerDN, BigInteger serialNumber) throws KeyResolutionException
	{
		return this.cache.resolveKey(issuerDN, serialNumber);
	}
}
