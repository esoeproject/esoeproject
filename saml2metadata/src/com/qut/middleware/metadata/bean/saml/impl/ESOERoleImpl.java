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

import com.qut.middleware.metadata.bean.saml.ESOERole;
import com.qut.middleware.metadata.bean.saml.endpoint.Endpoint;
import com.qut.middleware.metadata.bean.saml.endpoint.EndpointCollection;

public class ESOERoleImpl extends IdentityProviderRoleImpl implements ESOERole
{
	private EndpointCollection attributeServiceEndpoints;

	public ESOERoleImpl(List<String> keyNames, List<String> nameIDFormat, EndpointCollection singleLogoutEndpoints, EndpointCollection singleSignOnEndpoints, EndpointCollection attributeServiceEndpoints)
	{
		super(keyNames, nameIDFormat, singleLogoutEndpoints, singleSignOnEndpoints);
		
		if (attributeServiceEndpoints == null) throw new IllegalArgumentException("Attribute service endpoints collection cannot be null");
		
		this.attributeServiceEndpoints = attributeServiceEndpoints;
	}

	public String getAttributeServiceEndpoint(String binding)
	{
		return this.attributeServiceEndpoints.getEndpoint(binding);
	}

	public List<Endpoint> getAttributeServiceEndpointList()
	{
		return this.attributeServiceEndpoints.getEndpointList();
	}

}
