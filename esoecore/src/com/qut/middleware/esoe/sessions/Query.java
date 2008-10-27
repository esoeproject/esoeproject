/*
 * Copyright 2006, Queensland University of Technology Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Author: Shaun Mangelsdorf Creation Date: 28/09/2006
 * 
 * Purpose: Interface to allow queries to be made against the active session store
 */

package com.qut.middleware.esoe.sessions;

/** */
public interface Query
{
	/**
	 * Queries the session cache for a session with the given session identifier. A session that has expired will be
	 * considered non-existent.
	 * 
	 * @param sessionID
	 *            The session ID to query.
	 * @return Principal object for session, or null if the session was not found.
	 */
	public Principal queryAuthnSession(String sessionID);

	/**
	 * Queries the session cache for a session with the given session identifier. Checks for session validity including
	 * expiry time and returns true if the session is valid.
	 * 
	 * @param sessionID
	 *            The session ID to query.
	 * @return Boolean value indicating whether the session is valid.
	 */
	public boolean validAuthnSession(String sessionID);

	/**
	 * Queries the session cache for the principal that belongs to the given SAML identifier. A session that has expired
	 * will be considered non-existent.
	 * 
	 * @param samlID
	 *            The SAML ID to query.
	 * @param nameIDFormat
	 *            The format of the SAML identifier being queried. This would generally be a value from
	 *            {@link com.qut.midddleware.saml2.NameIDFormatConstants}
	 * @return The principal session that corresponds to the given SAML ID
	 */
	public Principal querySAMLSession(String samlID);
}
