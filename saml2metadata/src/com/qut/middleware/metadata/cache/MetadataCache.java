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
 * Creation Date: 09/04/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.metadata.cache;

import java.util.List;
import java.util.Map;

import com.qut.middleware.metadata.bean.EntityData;
import com.qut.middleware.metadata.bean.KeyEntry;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.metadata.source.DynamicMetadataSource;
import com.qut.middleware.metadata.source.MetadataSource;
import com.qut.middleware.saml2.ExternalKeyResolver;

public interface MetadataCache extends ExternalKeyResolver
{
	/**
	 * Gets the EntityData corresponding to the given EntityID, if it has been
	 * cached. If the entity has not been cached, the cache will attempt to
	 * initiate dynamic resolution of the entity (if dynamic loading has been
	 * configured).
	 */
	public EntityData getEntityData(String entityID, MetadataProcessor processor);

	/**
	 * Updates the cache from the specified MetadataSource, removing all
	 * previously cached objects for the supplied MetadataSource. Two
	 * MetadataSource objects that have the same Location value will be assumed
	 * to be the same MetadataSource, and so existing data for that Location
	 * will be overwritten on subsequent updates.
	 */
	public void update(MetadataSource source, List<EntityData> entities, List<KeyEntry> newKeys);
	
	/**
	 * Performs a dynamic update of the metadata cache. This differs from the
	 * above, in that it is intended only to be a partial update, so existing
	 * entries from the same source will not be removed.
	 */
	public void dynamicUpdate(DynamicMetadataSource source, List<EntityData> entities, List<KeyEntry> newKeys);
	
	/**
	 * Sets the interval after which a new dynamic entity will be expired from
	 * the cache.
	 * @param expiryInterval New expiry interval in milliseconds.
	 */
	public void setDynamicEntityExpiryInterval(long expiryInterval);
	
	/**
	 * @return A list of active entities aggregated from metadata sources.
	 */
	public List<String> getEntityList();
}
