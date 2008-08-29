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

import com.qut.middleware.metadata.bean.saml.IdentityProviderRole;
import com.qut.middleware.metadata.bean.saml.endpoint.Endpoint;
import com.qut.middleware.metadata.bean.saml.endpoint.EndpointCollection;

public class IdentityProviderRoleImpl implements IdentityProviderRole
{
	private List<String> keyNames;
	private List<String> nameIDFormat;
	private EndpointCollection singleLogoutServiceEndpoints;
	private EndpointCollection singleSignOnServiceEndpoints;
	
	public IdentityProviderRoleImpl(List<String> keyNames, List<String> nameIDFormatList, EndpointCollection singleLogoutServiceEndpoints, EndpointCollection singleSignOnServiceEndpoints)
	{
		if (keyNames == null) throw new IllegalArgumentException("Key names list cannot be null");
		if (nameIDFormatList == null) throw new IllegalArgumentException("Name ID format cannot be null");
		if (singleLogoutServiceEndpoints == null) throw new IllegalArgumentException("Single logout service endpoints collection cannot be null");
		if (singleSignOnServiceEndpoints == null) throw new IllegalArgumentException("Single sign on endpoints collection cannot be null");
		
		this.keyNames = keyNames;
		this.nameIDFormat = nameIDFormatList;
		this.singleLogoutServiceEndpoints = singleLogoutServiceEndpoints;
		this.singleSignOnServiceEndpoints = singleSignOnServiceEndpoints;
	}
	
	public List<String> getKeyNames()
	{
		return this.keyNames;
	}

	public List<String> getNameIDFormatList()
	{
		return this.nameIDFormat;
	}

	public String getSingleLogoutServiceEndpoint(String binding)
	{
		return this.singleLogoutServiceEndpoints.getEndpoint(binding);
	}

	public List<Endpoint> getSingleLogoutServiceEndpointList()
	{
		return this.singleLogoutServiceEndpoints.getEndpointList();
	}

	public String getSingleSignOnService(String binding)
	{
		return this.singleSignOnServiceEndpoints.getEndpoint(binding);
	}

	public List<Endpoint> getSingleSignOnServiceList()
	{
		return this.singleSignOnServiceEndpoints.getEndpointList();
	}
}
