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

package com.qut.middleware.metadata.bean.saml;

import java.util.List;

import com.qut.middleware.metadata.bean.saml.attribute.AttributeConsumingService;
import com.qut.middleware.metadata.bean.saml.endpoint.Endpoint;
import com.qut.middleware.metadata.bean.saml.endpoint.IndexedEndpoint;

public interface ServiceProviderRole extends SAMLRole
{
	public List<String> getNameIDFormatList();

	/**
	 * @return A list of zero or more assertion consumer service endpoints.
	 */
	public List<IndexedEndpoint> getAssertionConsumerServiceEndpointList();

	/**
	 * @return A random assertion consumer service endpoint with the given index
	 *         that supports the given binding, or null if none exist.
	 */
	public String getAssertionConsumerServiceEndpoint(String binding, int index);

	/**
	 * @return A list of zero or more single logout endpoints.
	 */
	public List<Endpoint> getSingleLogoutServiceEndpointList();

	/**
	 * @return A random single logout endpoint that supports the given binding,
	 *         or null if none exist.
	 */
	public String getSingleLogoutServiceEndpoint(String binding);
	
	/**
	 * @return A list of zero or more attribute consuming service information.
	 */
	public List<AttributeConsumingService> getAttributeConsumingServiceList();
	
	/**
	 * @return The attribute consuming service information with the given index,
	 * 		   or null if none exist.
	 */
	public AttributeConsumingService getAttributeConsumingService(int index);
	
	/**
	 * @return A list of zero or more artifact resolve endpoints.
	 */
	public List<IndexedEndpoint> getArtifactResolutionEndpointList();
	
	/**
	 * @return A random artifact resolve endpoint with the given index that 
	 *         supports the given binding, or null if none exist.
	 */
	public String getArtifactResolutionEndpoint(String binding, int index);
}
