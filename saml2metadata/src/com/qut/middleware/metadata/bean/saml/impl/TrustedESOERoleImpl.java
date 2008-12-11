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

import com.qut.middleware.metadata.bean.saml.TrustedESOERole;
import com.qut.middleware.metadata.bean.saml.endpoint.Endpoint;
import com.qut.middleware.metadata.bean.saml.endpoint.EndpointCollection;
import com.qut.middleware.metadata.bean.saml.endpoint.IndexedEndpointCollection;

public class TrustedESOERoleImpl extends ESOERoleImpl implements TrustedESOERole
{
	private EndpointCollection spepStartupServiceEndpoints;
	private EndpointCollection lxacmlAuthzServiceEndpoints;

	public TrustedESOERoleImpl(List<String> keyNames, List<String> nameIDFormat, EndpointCollection singleLogoutEndpoints, EndpointCollection singleSignOnEndpoints, IndexedEndpointCollection artifactResolutionEndpoints, EndpointCollection attributeServiceEndpoints, EndpointCollection spepStartupServiceEndpoints, EndpointCollection lxacmlAuthzServiceEndpoints)
	{
		super(keyNames, nameIDFormat, singleLogoutEndpoints, singleSignOnEndpoints, artifactResolutionEndpoints, attributeServiceEndpoints);
		
		if (spepStartupServiceEndpoints == null) throw new IllegalArgumentException("SPEP startup endpoints collection cannot be null");
		if (lxacmlAuthzServiceEndpoints == null) throw new IllegalArgumentException("LXACML AuthzService endpoints collection cannot be null");
		
		this.spepStartupServiceEndpoints = spepStartupServiceEndpoints;
		this.lxacmlAuthzServiceEndpoints = lxacmlAuthzServiceEndpoints;
	}

	public String getSPEPStartupServiceEndpoint(String binding)
	{
		return this.spepStartupServiceEndpoints.getEndpoint(binding);
	}

	public List<Endpoint> getSPEPStartupServiceEndpointList()
	{
		return this.spepStartupServiceEndpoints.getEndpointList();
	}

	public String getLXACMLAuthzServiceEndpoint(String binding)
	{
		return this.lxacmlAuthzServiceEndpoints.getEndpoint(binding);
	}

	public List<Endpoint> getLXACMLAuthzServiceEndpointList()
	{
		return this.lxacmlAuthzServiceEndpoints.getEndpointList();
	}
}
