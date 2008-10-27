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
 * Creation Date: 02/10/2006
 * 
 * Purpose: Interface for the local session cache. Stores information about authenticated principals.
 */

package com.qut.middleware.esoe.sessions.cache;


import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.exception.SessionCacheUpdateException;

/**
 * Session cache interface for the underlying data store for all session data.
 * 
 * All methods on this interface should only be called from within the sessions processor logic. Calls from outside
 * should go through the {@link Update}, {@link Create}, {@link Query} and {@link Terminate} interfaces.
 */
public interface SessionCache
{
	/**
	 * Adds a session to the session cache. This method will throw an exception if the session ID or SAML ID of the
	 * Principal already exists.
	 * 
	 * @param principal
	 *            The principal to add to the session cache.
	 * @throws SessionCacheUpdateException
	 *             If the add operation failed for some reason.
	 */
	public void addSession(Principal principal) throws SessionCacheUpdateException;

	/**
	 * Retrieves a session from the session cache using its ESOE session identifier (i.e. cookie value)
	 * 
	 * @param sessionID
	 *            The ESOE session identifier
	 * @return The principal object for the corresponding session, else null if not exists.
	 */
	public Principal getSession(String sessionID);

	/**
	 * Retrieves a session from the session cache using its SAML Authn identifier (i.e. transient NameID)
	 * 
	 * @param samlID
	 *            The SAML Authn identifier
	 * @return The principal object for the corresponding session if exists, else null.
	 */
	public Principal getSessionBySAMLID(String samlID);

	/**
	 * Verifies that a session exists in the underlying data store and is valid.
	 * 
	 * @param sessionID
	 *            The ESOE session identifier
	 * @return Boolean value indicating session validity.
	 */
	public boolean validSession(String sessionID);

	/**
	 * Removes a session from the underlying data store. This method will throw an exception if the specified session ID
	 * does not exist.
	 * 
	 * @param sessionID
	 *            The ESOE session identifier
	 * @throws SessionCacheUpdateException
	 *             If the remove operation failed for some reason.
	 */
	public void removeSession(String sessionID) throws SessionCacheUpdateException;

	/**
	 * Adds an entity ID / session index pair to the underlying data store for the given principal. The principal object
	 * must be modified to include these values before this method is called, so no modification to the Principal is
	 * necessary. This method will throw an exception if the specified principal does not exist in the data store.
	 * 
	 * @param principal
	 *            The principal object to perform the update for.
	 * @param entityID
	 *            The entity ID corresponding to the session index.
	 * @param sessionIndex
	 *            The session index being added.
	 * @throws SessionCacheUpdateException
	 *             If the update failed for some reason.
	 */
	public void addEntitySessionIndex(Principal principal, String entityID, String sessionIndex) throws SessionCacheUpdateException;

	/**
	 * Updates the principal attributes in the underlying data store. To maintain consistency this method must be called
	 * after any update to the principal attributes. This method will throw an exception if the specified principal does
	 * not exist in the data store.
	 * 
	 * @param principal
	 *            The principal object to perform the update for.
	 * @throws SessionCacheUpdateException
	 *             If the update failed for some reason.
	 */
	public void updatePrincipalAttributes(Principal principal) throws SessionCacheUpdateException;
	
	
	public int getSize();
	
	public long getLastCleaned();
	
	public int cleanCache(int timeout);
}
