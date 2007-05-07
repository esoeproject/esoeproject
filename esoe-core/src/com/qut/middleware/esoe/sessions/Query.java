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
 * Purpose: Interface to allow queries to be made against the active session cache.
 */

package com.qut.middleware.esoe.sessions;

import com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException;

/** */
public interface Query
{
	/**
	 * Queries the session cache for a session with the given session identifier.
	 * 
	 * @param sessionID
	 *            The session ID to query.
	 * @return Principal object for session
	 * @throws InvalidSessionIdentifierException
	 *             if session is not found
	 */
	public Principal queryAuthnSession(String sessionID) throws InvalidSessionIdentifierException;
	
	/**
	 * Queries the session cache for a session with the given session identifier.
	 * 
	 * @param sessionID
	 *            The session ID to query.
	 * @throws InvalidSessionIdentifierException
	 *             if session is not found
	 */
	public void validAuthnSession(String sessionID) throws InvalidSessionIdentifierException;

	/**
	 * Queries the session cache for a session with the given SAML identifier.
	 * 
	 * @param samlID
	 *            The SAML ID to query.
	 * @return Principal object for session
	 * @throws InvalidSessionIdentifierException
	 *             if session is not found.
	 */
	public Principal querySAMLSession(String samlID) throws InvalidSessionIdentifierException;
}
