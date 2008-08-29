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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.qut.middleware.metadata.bean.saml.ServiceProviderRole;
import com.qut.middleware.metadata.bean.saml.attribute.AttributeConsumingService;
import com.qut.middleware.metadata.bean.saml.endpoint.Endpoint;
import com.qut.middleware.metadata.bean.saml.endpoint.EndpointCollection;
import com.qut.middleware.metadata.bean.saml.endpoint.IndexedEndpoint;
import com.qut.middleware.metadata.bean.saml.endpoint.IndexedEndpointCollection;

public class ServiceProviderRoleImpl implements ServiceProviderRole
{
	private List<String> keyNames;
	private List<String> nameIDFormatList;
	private IndexedEndpointCollection assertionConsumerServiceEndpoints;
	private EndpointCollection singleLogoutServiceEndpoints;
	private Map<Integer, AttributeConsumingService> attributeConsumingServiceMap;

	public ServiceProviderRoleImpl(List<String> keyNames, List<String> nameIDFormatList, IndexedEndpointCollection assertionConsumerServiceEndpoints, EndpointCollection singleLogoutServiceEndpoints, Map<Integer,AttributeConsumingService> attributeConsumingServiceMap)
	{
		if (keyNames == null) throw new IllegalArgumentException("Key names list cannot be null");
		if (nameIDFormatList == null) throw new IllegalArgumentException("Name ID format cannot be null");
		if (assertionConsumerServiceEndpoints == null) throw new IllegalArgumentException("Assertion consumer service endpoints collection cannot be null");
		if (singleLogoutServiceEndpoints == null) throw new IllegalArgumentException("Single logout service endpoints collection cannot be null");
		if (attributeConsumingServiceMap == null) throw new IllegalArgumentException("Attribute consuming service map cannot be null");

		this.keyNames = keyNames;
		this.nameIDFormatList = nameIDFormatList;
		this.assertionConsumerServiceEndpoints = assertionConsumerServiceEndpoints;
		this.singleLogoutServiceEndpoints = singleLogoutServiceEndpoints;
		this.attributeConsumingServiceMap = attributeConsumingServiceMap;
	}
	
	public List<String> getKeyNames()
	{
		return this.keyNames;
	}

	public List<String> getNameIDFormatList()
	{
		return this.nameIDFormatList;
	}

	public String getAssertionConsumerServiceEndpoint(String binding, int index)
	{
		return this.assertionConsumerServiceEndpoints.getEndpoint(binding, index);
	}

	public List<IndexedEndpoint> getAssertionConsumerServiceEndpointList()
	{
		return this.assertionConsumerServiceEndpoints.getEndpointList();
	}

	public String getSingleLogoutServiceEndpoint(String binding)
	{
		return this.singleLogoutServiceEndpoints.getEndpoint(binding);
	}

	public List<Endpoint> getSingleLogoutServiceEndpointList()
	{
		return this.singleLogoutServiceEndpoints.getEndpointList();
	}

	public AttributeConsumingService getAttributeConsumingService(int index)
	{
		return this.attributeConsumingServiceMap.get(index);
	}

	public List<AttributeConsumingService> getAttributeConsumingServiceList()
	{
		List<AttributeConsumingService> values = new ArrayList<AttributeConsumingService>();
		values.addAll(this.attributeConsumingServiceMap.values());
		
		return values;
	}
}
