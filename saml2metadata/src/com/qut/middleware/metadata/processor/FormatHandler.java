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
 * Creation Date: 11/04/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.metadata.processor;

import com.qut.middleware.metadata.bean.EntityData;
import com.qut.middleware.metadata.cache.MetadataCache;
import com.qut.middleware.metadata.exception.InvalidMetadataException;
import com.qut.middleware.metadata.source.DynamicMetadataSource;
import com.qut.middleware.metadata.source.MetadataSource;

/**
 * Interface for handling different metadata formats.
 */
public interface FormatHandler
{
	/**
	 * Checks whether this handler can handle the format of the specified source.
	 * @param source
	 * @return Boolean value indicating ability to handle this source.
	 */
	public boolean canHandle(MetadataSource source);
	
	/**
	 * Processes the document given, and updates the cache with the data contained therein.
	 * @param source
	 * @param cache
	 * @param document
	 * @throws InvalidMetadataException If the document is invalid in some implementation-defined way.
	 */
	public void updateCache(MetadataSource source, MetadataCache cache, byte[] document) throws InvalidMetadataException;
	
	
	/**
	 * Checks whether this handler can handle the format of the specified dynamic source.
	 * @param source
	 * @return Boolean value indicating ability to handle this source.
	 */
	public boolean canHandle(DynamicMetadataSource source);
	
	/**
	 * Processes the document given, and does a dynamic update on the cache with the data contained therein.
	 * @param source
	 * @param cache
	 * @param entityID TODO
	 * @param document
	 * @return The EntityData 
	 * @throws InvalidMetadataException If the document is invalid in some implementation-defined way.
	 */
	public EntityData dynamicUpdateCache(DynamicMetadataSource source, MetadataCache cache, String entityID, byte[] document) throws InvalidMetadataException;
}
