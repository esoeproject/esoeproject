/* Copyright 2006, Queensland University of Technology
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
 * Purpose: Interface for the bean that contains data regarding a startup request.
 */
package com.qut.middleware.esoe.spep.bean;

import com.qut.middleware.esoe.bean.SAMLProcessorData;

/** Interface for the bean that contains data regarding a startup request. */
public interface SPEPProcessorData extends SAMLProcessorData
{
	/**
	 * Accessor for request descriptor ID. The descriptor ID uniquely identifies an SPEP.
	 * 
	 * @return The request descriptor ID.
	 */
	public String getRequestDescriptorID();
	
	/**
	 * Mutator for request descriptor ID. The descriptor ID uniquely identifies an SPEP.
	 * 
	 * @param requestDescriptorID The request descriptor ID.
	 */
	public void setRequestDescriptorID(String requestDescriptorID);
	
	/** Accessor for authorization cache index. The index is used to identify a particular
	 * node in an SPEP configuration. An SPEP can have mutliple nodes, each with different IP
	 * addresses. The cache index is used to resolve the particular location of a node.
	 * 
	 * @return The authorization cache index.
	 */
	public int getAuthzCacheIndex();
	
	/** Mutator for authorization cache index. The index is used to identify a particular
	 * node in an SPEP configuration. An SPEP can have mutliple nodes, each with different IP
	 * addresses. The cache index is used to resolve the particular location of a node.
	 * 
	 * @param authzCacheIndex The authorization cache index.
	 */
	public void setAuthzCacheIndex(int authzCacheIndex);
	
}
