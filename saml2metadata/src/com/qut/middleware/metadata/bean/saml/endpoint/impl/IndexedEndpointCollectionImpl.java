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

package com.qut.middleware.metadata.bean.saml.endpoint.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.qut.middleware.metadata.bean.saml.endpoint.Endpoint;
import com.qut.middleware.metadata.bean.saml.endpoint.IndexedEndpoint;
import com.qut.middleware.metadata.bean.saml.endpoint.IndexedEndpointCollection;

public class IndexedEndpointCollectionImpl implements IndexedEndpointCollection
{
	private Random random;
	private List<IndexedEndpoint> indexedEndpointList;

	public IndexedEndpointCollectionImpl(Random random)
	{
		this.random = random;
		this.indexedEndpointList = new ArrayList<IndexedEndpoint>();
	}

	public String getEndpoint(String binding, int index)
	{
		List<IndexedEndpoint> validEndpoints = new ArrayList<IndexedEndpoint>();
		
		for (IndexedEndpoint endpoint : this.indexedEndpointList)
		{
			if (endpoint.getBinding().equals(binding) && endpoint.getIndex() == index)
			{
				validEndpoints.add(endpoint);
			}
		}
		
		if (validEndpoints.size() == 0)
		{
			return null;
		}
		
		// Pick a random endpoint from the ones that matched.
		int i = this.random.nextInt(validEndpoints.size());
		
		Endpoint endpoint = validEndpoints.get(i);
		if (endpoint != null)
		{
			return endpoint.getLocation();
		}
		
		return null;
	}

	public List<IndexedEndpoint> getEndpointList(int index)
	{
		List<IndexedEndpoint> validEndpoints = new ArrayList<IndexedEndpoint>();
		
		for (IndexedEndpoint endpoint : this.indexedEndpointList)
		{
			if (endpoint.getIndex() == index)
			{
				validEndpoints.add(endpoint);
			}
		}
		
		return validEndpoints;
	}

	public List<IndexedEndpoint> getEndpointList(String binding)
	{
		List<IndexedEndpoint> validEndpoints = new ArrayList<IndexedEndpoint>();
		
		for (IndexedEndpoint endpoint : this.indexedEndpointList)
		{
			if (endpoint.getBinding().equals(binding))
			{
				validEndpoints.add(endpoint);
			}
		}
		
		return validEndpoints;
	}

	public List<IndexedEndpoint> getEndpointList()
	{
		return this.indexedEndpointList;
	}

}
