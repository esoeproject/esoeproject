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
 * Creation Date: 15/05/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.metadata.bean.saml.endpoint;

import java.util.List;

public interface IndexedEndpointCollection
{
	/**
	 * @param binding The binding to search for an endpoint for.
	 * @param index The index to search for an endpoint for.
	 * @return A random endpoint location that supports this binding and has
	 * 		the given index, or null if none were found.
	 */
	public String getEndpoint(String binding, int index);
	
	/**
	 * @param index The index to search for an endpoint for.
	 * @return A list of all endpoints in this collection that have the given
	 * 		index, or an empty list if none were found.
	 */
	public List<IndexedEndpoint> getEndpointList(int index);
	
	/**
	 * @param binding The binding to search for an endpoint for.
	 * @return A list of all endpoints in this collection that supports this
	 * 		binding, or an empty list if none were found.
	 */
	public List<IndexedEndpoint> getEndpointList(String binding);
	
	/**
	 * @return A list of all endpoints in this collection.
	 */
	public List<IndexedEndpoint> getEndpointList();
}
