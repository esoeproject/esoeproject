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
 * Creation Date: 09/05/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.metadata.source;

import com.qut.middleware.metadata.bean.EntityData;
import com.qut.middleware.metadata.exception.MetadataSourceException;
import com.qut.middleware.metadata.processor.MetadataProcessor;

public interface DynamicMetadataSource
{
	/**
	 * Performs a dynamic update with respect to the given entity ID and
	 * returns the updated EntityData object, or null if it was not updated
	 * for an implementation-defined reason.
	 */
	public EntityData updateDynamicMetadata(MetadataProcessor processor, String entityID) throws MetadataSourceException;
	
	/**
	 * @return The identifier associated with the format of metadata that this 
	 * source provides.
	 */
	public String getFormat();
	
	/**
	 * Determines whether the dynamic metadata source will give "trusted" data for
	 * the given entity ID. If the data is to be trusted, the document signature
	 * must be validated.
	 * 
	 * The implication of trusting a metadata source is defined externally. Each
	 * entity derived from a trusted source will also be marked as trusted. Care
	 * should be taken to ensure the trusted sources have a higher priority, so the
	 * untrusted sources will not clobber cached entities from trusted sources.
	 */
	public boolean isTrusted(String entityID);
}
