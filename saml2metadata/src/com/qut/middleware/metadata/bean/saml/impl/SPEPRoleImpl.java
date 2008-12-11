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
 * Creation Date: 17/04/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.metadata.bean.saml.impl;

import java.util.List;
import java.util.Map;

import com.qut.middleware.metadata.bean.saml.SPEPRole;
import com.qut.middleware.metadata.bean.saml.attribute.AttributeConsumingService;
import com.qut.middleware.metadata.bean.saml.endpoint.EndpointCollection;
import com.qut.middleware.metadata.bean.saml.endpoint.IndexedEndpoint;
import com.qut.middleware.metadata.bean.saml.endpoint.IndexedEndpointCollection;

public class SPEPRoleImpl extends ServiceProviderRoleImpl implements SPEPRole
{
	private IndexedEndpointCollection cacheClearServiceEndpoints;

	public SPEPRoleImpl(List<String> keyNames, List<String> nameIDFormatList, IndexedEndpointCollection assertionConsumerServiceEndpoints, EndpointCollection singleLogoutServiceEndpoints, Map<Integer,AttributeConsumingService> attributeConsumingServiceMap, IndexedEndpointCollection artifactResolutionEndpoints, IndexedEndpointCollection cacheClearServiceEndpoints)
	{
		super(keyNames, nameIDFormatList, assertionConsumerServiceEndpoints, singleLogoutServiceEndpoints, attributeConsumingServiceMap, artifactResolutionEndpoints);
		
		if (cacheClearServiceEndpoints == null) throw new IllegalArgumentException("Cache clear service endpoints collection cannot be null");
		
		this.cacheClearServiceEndpoints = cacheClearServiceEndpoints;
	}

	public String getCacheClearServiceEndpoint(String binding, int index)
	{
		return this.cacheClearServiceEndpoints.getEndpoint(binding, index);
	}

	public List<IndexedEndpoint> getCacheClearServiceEndpointList()
	{
		return this.cacheClearServiceEndpoints.getEndpointList();
	}
}
