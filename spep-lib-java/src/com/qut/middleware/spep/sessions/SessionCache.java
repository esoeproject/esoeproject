/* 
 * Copyright 2006, Queensland University of Technology
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
 * Creation Date: 13/11/2006
 * 
 * Purpose: Interface for the session cache that maintains all PrincipalSession objects
 */
package com.qut.middleware.spep.sessions;


/** Interface for the session cache that maintains all PrincipalSession objects. */
public interface SessionCache
{
	/**
	 * Retrieves a session from the cache.
	 * @param sessionID The session ID
	 * @return The session object
	 */
	public PrincipalSession getPrincipalSession(String sessionID);
	
	/**
	 * Retrieves a session from the cache.
	 * @param esoeSessionID The esoe session ID
	 * @return The session object
	 */
	public PrincipalSession getPrincipalSessionByEsoeSessionID(String esoeSessionID);
	
	/**
	 * Stores a session in the cache.
	 * @param sessionID The session ID
	 * @param principalSession The session object
	 */
	public void putPrincipalSession(String sessionID, PrincipalSession principalSession);
	
	/**
	 * Terminates all records of principal interaction with the SPEP
	 * @param principalSession The principal session
	 */
	public void terminatePrincipalSession(PrincipalSession principalSession);
	
	/**
	 * Terminates a unique session with the SPEP, additionally if the principal has no further outstanding sessions with the ESOE
	 * everything is terminated
	 *  @param principalSession The principal session
	 *  @param esoeSessionIndex The session index to terminate
	 */
	public void terminateIndividualPrincipalSession(PrincipalSession principalSession, String esoeSessionIndex);
	
	/**
	 * Retrieves an unauthenticated session from the cache.
	 * @param requestID The request ID
	 * @return The unauthenticated session object
	 */
	public UnauthenticatedSession getUnauthenticatedSession(String requestID);
	
	/**
	 * Stores an unauthenticated session in the cache.
	 * @param requestID The request ID
	 * @param unauthenticatedSession The unauthenticated session object
	 */
	public void putUnauthenticatedSession(String requestID, UnauthenticatedSession unauthenticatedSession);
	
	/**
	 * Terminates an unauthenticated session
	 * @param requestID The request ID
	 */
	public void terminateUnauthenticatedSession(String requestID);
}
