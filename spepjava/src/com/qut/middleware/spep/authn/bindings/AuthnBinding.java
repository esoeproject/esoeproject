/* Copyright 2008, Queensland University of Technology
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
 * Creation Date: 12/12/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.spep.authn.bindings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qut.middleware.spep.SPEP;
import com.qut.middleware.spep.authn.AuthnProcessorData;
import com.qut.middleware.spep.exception.AuthenticationException;

public interface AuthnBinding
{
	/**
	 * Handles a request, whatever stage it is up to.
	 * @param request The request object from the servlet container
	 * @param response The response object
	 * @param data The Authentication processor data bean
	 * @param spep The SPEP instance.
	 */
	public void handleRequest(HttpServletRequest request, HttpServletResponse response, AuthnProcessorData data, SPEP spep) throws AuthenticationException;
	
	/**
	 * @return The standard URN identifier for this binding.
	 */
	public String getBindingIdentifier();
}
