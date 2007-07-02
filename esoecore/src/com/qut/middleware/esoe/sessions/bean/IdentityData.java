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
 * Purpose: Interface for the identity data bean. Used to store identity attributes relating
 * to an authenticated principal, which can then be retrieved from their session.
 */
package com.qut.middleware.esoe.sessions.bean;

import java.util.List;
import java.util.Map;

import com.qut.middleware.saml2.schemas.esoe.sessions.IdentityType;

public interface IdentityData
{
	/**
	 * Accessor to get identity.
	 * 
	 * @return IdentityType list of identities contained in this data type.
	 */
	public List<IdentityType> getIdentity();

	/**
	 * Mutator for identity.
	 * 
	 * @param identity
	 *            The identity list to store.
	 */
	public void setIdentity(List<IdentityType> identity);

	/**
	 * Accessor for principal.
	 * 
	 * @return String principal name.
	 */
	public String getPrincipalAuthnIdentifier();

	/**.
	 * Mutator for principal.
	 * 
	 * @param principalName The principal to store.
	 */
	public void setPrincipalAuthnIdentifier(String principalAuthnIdentifier);

	/**
	 * Accessor for session identifier
	 * 
	 * @return String session identifier
	 */
	public String getSessionID();

	/**
	 * Mutator for session identifier
	 * 
	 * @param sessionID
	 *            Session identifer to store
	 */
	public void setSessionID(String sessionID);

	/**
	 * Accessor for attribute map. The map should be of the form: <attributeName, IdentityAttribute>. 
	 * 
	 * @return Map of attribute names to IdentityAttribute objects
	 */
	public Map<String, IdentityAttribute> getAttributes();

	/**
	 * Gets the name of the handler that is currently processing this identity data
	 * 
	 * @return Handler name
	 */
	public String getCurrentHandler();

	/**
	 * Sets the name of the handler that is processing the identity data
	 * 
	 * @param handler
	 *            The handler name to store
	 */
	public void setCurrentHandler(String handler);
}
