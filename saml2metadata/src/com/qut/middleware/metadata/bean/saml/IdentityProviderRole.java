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
 * Creation Date: 09/04/2008
 * 
 * Purpose: Interface for SAML Identity Provider role.
 */

package com.qut.middleware.metadata.bean.saml;

import java.util.List;

import com.qut.middleware.metadata.bean.saml.endpoint.Endpoint;

public interface IdentityProviderRole extends SAMLRole
{
	public List<String> getNameIDFormatList();

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
	 * @return A list of one or more single sign-on endpoints.
	 */
	public List<Endpoint> getSingleSignOnServiceList();

	/**
	 * @return A random single sign-on endpoint that supports the given binding,
	 *         or null if none exist.
	 */
	public String getSingleSignOnService(String binding);
}
