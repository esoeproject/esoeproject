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
 * Creation Date: 13/05/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.metadata.bean.saml;

import java.util.List;

import com.qut.middleware.metadata.bean.saml.endpoint.Endpoint;

public interface LXACMLPDPRole extends SAMLRole
{
	/**
	 * @return A random LXACML authz service endpoint that supports the given
	 *         binding, or null if none exist.
	 */
	public String getLXACMLAuthzServiceEndpoint(String binding);

	/**
	 * @return A list of zero or more LXACML authz service endpoints.
	 */
	public List<Endpoint> getLXACMLAuthzServiceEndpointList();
}
