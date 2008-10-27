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
 * Creation Date: 07/10/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.esoe.sessions;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.qut.middleware.esoe.sessions.bean.IdentityAttribute;

/**
 * Interface for the principal object.
 * 
 * All modification to this object should be done from within the sessions processor logic. Modifications by other code
 * should go through the {@link Update}, {@link Create}, {@link Query} and {@link Terminate} interfaces.
 */
public interface Principal extends Serializable
{
	/*  Immutable session data */

	/** Gets SAML Authentication Context Class */
	public String getAuthenticationContextClass();

	/** Gets authentication timestamp */
	public long getAuthnTimestamp();

	/** Gets ESOE session identifier (i.e. cookie value) */
	public String getSessionID();

	/** Gets the transient Authn identifier for this session */
	public String getSAMLAuthnIdentifier();

	/** Gets the principal authentication identifier */
	public String getPrincipalAuthnIdentifier();

	/** Gets the NotOnOrAfter timestamp for this session */
	public long getSessionNotOnOrAfter();

	/*  Mutable session data */

	/** Gets the principal attribute map */
	public Map<String, IdentityAttribute> getAttributes();

	/** Adds an attribute name/value pair to the principal attribute map, overwriting any existing value(s) */
	public void putAttribute(String key, IdentityAttribute value);

	/** Gets the last accessed timestamp for this session */
	public long getLastAccessed();

	/** Sets the last accessed timestamp for this session */
	public void setLastAccessed(long timeMillis);

	/** Adds the entity ID and session index to this session. */
	public void addEntitySessionIndex(String entityID, String sessionIndex);

	/** Gets the list of active entity IDs for this session. 
	 *  @return A List of active entity strings if exists, else null.
	 */
	public List<String> getActiveEntityList();

	/** Gets the list of session indices for this session, for the given entity ID 
	 * @return A List of active entity strings if exists, else null.
	 * */
	public List<String> getActiveEntitySessionIndices(String entityID);
}
