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
 * Creation Date: 16/04/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.metadata.processor.saml;

import com.qut.middleware.metadata.bean.impl.EntityDataImpl;
import com.qut.middleware.metadata.exception.InvalidMetadataException;
import com.qut.middleware.saml2.schemas.metadata.EntityDescriptor;

public interface SAMLEntityDescriptorProcessor
{
	/**
	 * Processes the given EntityDescriptor, and adds appropriate role information to
	 * the EntityDataImpl instance given.
	 * @param entityData
	 * @param descriptor
	 * @throws InvalidMetadataException If the EntityDescriptor is invalid in some implementation-defined way.
	 */
	public void process(EntityDataImpl entityData, EntityDescriptor descriptor) throws InvalidMetadataException;
}
