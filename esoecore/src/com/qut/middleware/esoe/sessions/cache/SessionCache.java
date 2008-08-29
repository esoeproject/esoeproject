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
 * Creation Date: 02/10/2006
 * 
 * Purpose: Interface for the local session cache. Stores information about authenticated principals.
 */
package com.qut.middleware.esoe.sessions.cache;

import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.exception.DuplicateSessionException;

/** 
 * Interface for the local session cache. Stores information about authenticated principals. 
 */
public interface SessionCache
{
	/**
	 * Adds the session data to the session cache.
	 * 
	 * @param data
	 *            Principal to add.
	 * @throws DuplicateSessionException
	 */
	public void addSession(Principal data) throws DuplicateSessionException;

	/**
	 * Updates the session data when a SAML ID is added to the session data. The implementing method
	 * MUST update the principals lastUsed timestamp ehwn a call is made.
	 * 
	 * @param data
	 *            Principal to update.
	 * @throws DuplicateSessionException
	 */
	public void updateSessionSAMLID(Principal data) throws DuplicateSessionException;

	/**
	 * Removes the specified sesson from the session cache, and returns the value that was removed.
	 * 
	 * @param sessionID
	 *            Session ID of principal to remove.
	 * @return Principal object that was removed.
	 */
	public Principal removeSession(String sessionID);

	/**
	 * Retrieves the specified session from the session cache. The implementing method
	 * MUST update the principals lastUsed timestamp ehwn a call is made.
	 * 
	 * @param sessionID
	 *            Session ID of principal to retrieve
	 * @return Principal object referenced by given session ID.
	 */
	public Principal getSession(String sessionID);
	
	/**
	 * Determines if a sessionID is currently validly held in the cache.
	 * 
	 * @param sessionID
	 *            Session ID of principal to retrieve
	 * @return Principal object referenced by given session ID.
	 */
	public boolean validSession(String sessionID);

	/**
	 * Retrieves the session matching the given SAML ID from the session cache. The implementing method
	 * MUST update the principals lastUsed timestamp ehwn a call is made.
	 * 
	 * @param samlID
	 *            SAML ID of principal to retrieve
	 * @return Principal object referenced by given SAML ID.
	 */
	public Principal getSessionBySAMLID(String samlID);
		
	/** Clear any entries from the cache that are older than specified age. Implementing classes must set setLastCleaned()
	 *  BEFORE any cache cleanup processing occurs. They must also lock the method to ensure that only ONE thread
	 *  can ever cause this method to execute at any given time.
	 * 
	 * @param age The age in seconds which an entry remains valid.
	 * @return The number of entries removed from the cache.
	 */
	public int cleanCache(int age);
		
	/** Retrieve the long timestamp of when the session cache was last cleaned. 
	 * 
	 * @return timestamp of the last cache cleanup.
	 */
	public long getLastCleaned();
	
	/** Set the long timestamp of when the session cache was last cleaned. 
	 * 
	 */
	public void setLastCleaned(long lastCleaned);
	
	/** Returns the number of active Principal sessions contained in the cache. Must return the actual representation
	 * of user sessions, regardless of underlying implementation of session cache.
	 * 
	 * @return number of principal sessions.
	 */
	public int getSize();
	
}
