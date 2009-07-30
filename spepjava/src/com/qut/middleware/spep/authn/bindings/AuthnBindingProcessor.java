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

import com.qut.middleware.spep.exception.AuthenticationException;

public interface AuthnBindingProcessor
{
	/**
	 * Chooses a binding from the supported bindings, taking into account bindings which have endpoints in metadata for
	 * the trusted ESOE entity
	 * 
	 * @return Instance of the handler for the chosen binding.
	 * @throws AuthenticationException If an error occurs while choosing a binding for authentication
	 */
	public AuthnBinding chooseBinding(HttpServletRequest request) throws AuthenticationException;
	
	/**
	 * Retrieves the binding with the given binding identifier.
	 * @param bindingIdentifier The binding identifier.
	 * @return The binding object, or null if none could be found.
	 */
	public AuthnBinding getBinding(String bindingIdentifier);
}
