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
 * Creation Date: 10/04/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.metadata.processor;

import java.util.List;

import com.qut.middleware.metadata.bean.EntityData;
import com.qut.middleware.metadata.bean.Role;
import com.qut.middleware.metadata.exception.MetadataCacheUpdateException;
import com.qut.middleware.metadata.exception.MetadataStateException;
import com.qut.middleware.metadata.source.DynamicMetadataSource;
import com.qut.middleware.metadata.source.MetadataSource;
import com.qut.middleware.saml2.ExternalKeyResolver;

public interface MetadataProcessor extends ExternalKeyResolver
{
	/**
	 * Retrieves an entity data entry from the underlying cache.
	 * @param entityID The entity identifier to use for lookup.
	 * @return The retrieved entity data, or null if it does not exist.
	 * @throws MetadataStateException If the metadata processor or cache are in an invalid state.
	 */
	public EntityData getEntityData(String entityID) throws MetadataStateException;
	
	/**
	 * Retrieves the requested role from the entity data entry in the underlying cache.
	 * @param entityID The entity identifier to use for lookup.
	 * @param clazz The class of the requested role.
	 * @return The role data requested, or null if the entity does not exist or does not have the requested role.
	 * @throws MetadataStateException
	 */
	public <T extends Role> T getEntityRoleData(String entityID, Class<T> clazz) throws MetadataStateException;
	
	/**
	 * Begins to update the metadata cache globally, by requesting that each metadata
	 * source update.
	 */
	public void update();
	
	/**
	 * Updates the metadata cache information for the provided source.
	 * Wipes everything previously cached from the source and builds a new
	 * set of cached data from the metadata document provided.
	 * @param source
	 * @param document
	 * @throws MetadataCacheUpdateException 
	 */
	public void updateFromSource(MetadataSource source, byte[] document) throws MetadataCacheUpdateException;
	
	/**
	 * Updates the metadata cache information for the provided dynamic source.
	 * Does not wipe anything previously cached from the source.
	 * @param source
	 * @param document
	 * @return TODO
	 * @throws MetadataCacheUpdateException 
	 */
	public EntityData updateFromDynamicSource(DynamicMetadataSource source, String entityID, byte[] document) throws MetadataCacheUpdateException;
	
	public List<String> getEntityList();
}
