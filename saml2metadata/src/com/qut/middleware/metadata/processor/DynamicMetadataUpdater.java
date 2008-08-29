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

package com.qut.middleware.metadata.processor;

import com.qut.middleware.metadata.bean.EntityData;

public interface DynamicMetadataUpdater
{
	/**
	 * Performs a dynamic update with respect to the given entity ID and
	 * returns the updated EntityData object, or null if it was not updated
	 * for an implementation-defined reason.
	 * Initially this will imply that the entity ID has not yet been cached
	 * but implementors should not rely on this.
	 */
	public EntityData dynamicUpdate(MetadataProcessor processor, String entityID);
}
