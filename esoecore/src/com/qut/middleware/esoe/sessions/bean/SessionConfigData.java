/* Copyright 2006, Queensland University of Technology
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
 * Creation Date: 28/09/2006
 * 
 * Purpose: Interface for the session data bean. Stores information from the
 * 		configuration file for the sessions processor.
 */
package com.qut.middleware.esoe.sessions.bean;

import java.util.List;

import com.qut.middleware.saml2.schemas.esoe.sessions.IdentityType;

/** */
public interface SessionConfigData
{
	/**
	 * Retrieves the configuration Identity object.
	 * 
	 * @return Identity object obtained from configuration.
	 */
	public List<IdentityType> getIdentity();
}
