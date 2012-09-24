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
 * Purpose: Provides a source for a metadata 
 */

package com.qut.middleware.metadata.source;

import com.qut.middleware.metadata.exception.MetadataSourceException;
import com.qut.middleware.metadata.processor.MetadataProcessor;

/**
 * MetadataSource interface.
 * 
 * Implementation note:
 */
public interface MetadataSource
{
	public static int DEFAULT_PRIORITY = 0;
	public static boolean DEFAULT_MANDATORY = true;
	public static boolean DEFAULT_TRUSTED = true;
	
	/**
	 * Location for the MetadataSource is considered to be immutable, and so every
	 * call to this method on the same object must return the same value EVERY
	 * time.
	 * The only intended way to change the Location of a source is to delete that 
	 * source and create a new one with the new Location.
	 * The Location of the metadata is used for caching, etc. It is important that
	 * potential collisions between Locations be addressed correctly in implementing 
	 * classes.
	 * Two MetadataSource objects that return the same Location are considered to be
	 * the same MetadataSource for all intents and purposes.
	 * @return The location that this MetadataSource retrieves its data from.
	 */
	public String getLocation();
	
	/**
	 * @return The identifier associated with the format of metadata that this 
	 * source provides.
	 */
	public String getFormat();
	
	/**
	 * Updates the MetadataSource, and pushes any updated data into the provided 
	 * MetadataProcessor to be cached.
	 */
	public void updateMetadata(MetadataProcessor processor) throws MetadataSourceException;
	
	/**
	 * Accessor method for the "priority" option of a metadata source. The priority
	 * determines which MetadataSource should take highest priority when overwriting
	 * values in the cache.
	 * If there is an entity identifier that exists in two documents with differing
	 * priorities, the numerically greatest (most largely positive) priority will
	 * be the entry that is cached.
	 * If the priority values are equal, whichever value is cached first will remain
	 * there. The result will therefore be undefined, since the order in which 
	 * sources are cached may differ between each execution.
	 * 
	 * Implementors should consider using DEFAULT_PRIORITY by default. This will 
	 * ensure that sources with configured priorities will not be overridden by 
	 * sources with only default priority.
	 */
	public int getPriority();
	
	/**
	 * Accessor method for the "mandatory" option of a metadata source. If a source is
	 * mandatory, the inability to load a source of metadata is considered to be an
	 * error, and will be handled in an implementation-defined way.
	 * 
	 * Implementors should consider DEFAULT_MANDATORY as a reasonable default value, 
	 * as this will give consistent behaviour across implementations.
	 */
	public boolean isMandatory();
	
	/**
	 * Accessor method for the "trusted" option of a metadata source. If a source is
	 * trusted, the document signature must be validated.
	 * 
	 * The implication of trusting a metadata source is defined externally. Each
	 * entity derived from a trusted source will also be marked as trusted. Care
	 * should be taken to ensure the trusted sources have a higher priority, so the
	 * untrusted sources will not clobber cached entities from trusted sources.
	 */
	public boolean isTrusted();
}
