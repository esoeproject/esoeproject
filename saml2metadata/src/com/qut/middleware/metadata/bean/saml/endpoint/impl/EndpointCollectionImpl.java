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

package com.qut.middleware.metadata.bean.saml.endpoint.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.qut.middleware.metadata.bean.saml.endpoint.Endpoint;
import com.qut.middleware.metadata.bean.saml.endpoint.EndpointCollection;

public class EndpointCollectionImpl implements EndpointCollection
{
	private List<Endpoint> endpointList;
	private Random random;
	
	public EndpointCollectionImpl(Random random)
	{
		this.random = random;
		this.endpointList = new ArrayList<Endpoint>();
	}

	public String getEndpoint(String binding)
	{
		List<Endpoint> validEndpoints = new ArrayList<Endpoint>();
		
		for (Endpoint endpoint : this.endpointList)
		{
			if (binding.equals(endpoint.getBinding()))
			{
				validEndpoints.add(endpoint);
			}
		}
		
		if (validEndpoints.size() == 0)
		{
			return null;
		}
		
		// Pick a random endpoint from the ones that matched.
		int index = this.random.nextInt(validEndpoints.size());
		
		Endpoint endpoint = validEndpoints.get(index);
		if (endpoint != null)
		{
			return endpoint.getLocation();
		}
		
		return null;
	}

	public List<Endpoint> getEndpointList()
	{
		return this.endpointList;
	}
}
