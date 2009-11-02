/* 
 * Copyright 2008, Queensland University of Technology
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
 * Creation Date: 07/09/2008
 * 
 * Purpose: Provides an interface for access to sessions data. Note that due to
 * 		the nature of the session cache, this just extends the SessionCache
 * 		interface.
 */

package com.qut.middleware.esoe.sessions.data;

import java.util.List;

import com.qut.middleware.esoe.authn.bean.AuthnIdentityAttribute;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.exception.DuplicateSessionException;
import com.qut.middleware.esoe.sessions.exception.InvalidDescriptorIdentifierException;
import com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException;
import com.qut.middleware.esoe.sessions.exception.SessionCacheDAOException;
import com.qut.middleware.esoe.sessions.exception.SessionCacheUnavailableException;

public interface SessionCacheDAO
{

	/** Determine whether the given session ID represents a valid session. A session is considered valid if it is contained in the database
	 * and it's sesssionNotOnOrAfter value has not been exceeded.
	 * 
	 * @param sessionID The session ID to query for.
	 * @return true if session is valid else false.
	 */
	public boolean validSession(String sessionID) throws SessionCacheDAOException;

	/** Update the samlAUthnIdentifier in the database with the value contained in the given data.
	 * 
	 * @param data The principal to update.
	 * @throws DuplicateSessionException  ??? 
	 * @throws SessionCacheDAOException 
	 */
	public void updateSessionSAMLID(Principal data) throws DuplicateSessionException, SessionCacheDAOException;

	/** Remove the session and all associated active entity sessions from the database.
	 * 
	 * @param sessionID The indentifier of the session to remove.
	 * @return true if the session exists and was removed, else false.
	 */
	public boolean deleteSession(String sessionID) throws SessionCacheDAOException;

	/** Get the number of active sessions in the database.
	 * 
	 * @return Num sessions.
	 */
	public int getSize() throws SessionCacheDAOException;

	/** Retrieve all principal session details for the given samlAuthnIdentifier. 
	 * 
	 * @param samlID SAML authn id of the principal to retrieve.
	 * @return A popultaed Principal containing all session information.
	 */
	public Principal getSessionBySAMLID(String samlID) throws SessionCacheDAOException;

	/** Retrieve all principal session details for the given session identifier. The lastAccessed timestamp must be updated in
	 * the database (and hence reflected in the returned Principal object) when this method is called.
	 * 
	 * @param sessionID The session ID of the principal to retrieve.
	 * @return A popultaed Principal containing all session information.
	 */
	public Principal getSession(String sessionID) throws SessionCacheDAOException;
	
	/**  Retrieve all principal session details for the given session identifier. 
	 * 
	 * @param sessionID The session ID of the principal to retrieve.
	 * @param updateLastAccessed  The lastAccessed timestamp must be updated if set to true, else lastAccessed will not be updated.
	 * @return A popultaed Principal containing all session information.
	 */
	public Principal getSession(String sessionID, boolean updateLastAccessed) throws SessionCacheDAOException;
	
	/** Retrieve all sessions that have not been accessed in the last n milliseconds. The imp,ementation MUST only retrieve records 
	 * that have active entity sessions for performance.
	 * 
	 * @param idleTime Number of milliseconds after which a session is determined to be idle.
	 * @return A List of session identifiers of idle sessions.
	 */
	public List<String> getIdleSessions(long idleTime) throws SessionCacheDAOException;

	/** Clear the database of all sessions that are considered expired. Expired sessions are sessions that have exceeded their
	 * allocated notOnOrAfter value. Ie: notOnOrAfter is greater than NOW.
	 * 
	 * @return The number of sessions deleted from the database.
	 * @throws SessionCacheUnavailableException If the query cannot be run.
	 */
	public int deleteExpiredSessions() throws SessionCacheDAOException;

	/** Clear the database of all sessions that are considered idle. Idle sessions are sessions that have exceeded their
	 * allocated idleGradeExpiryTime value. Ie: idleGradeExpiryTime is greater than NOW.
	 * 
	 * @return The number of sessions deleted from the database.
	 * @throws SessionCacheUnavailableException If the query cannot be run.
	 */
	public int deleteIdleSessions() throws SessionCacheDAOException;
	
	public void addSession(Principal principal) throws SessionCacheDAOException;
	
	public void addDescriptor(Principal principal, String entityID) throws InvalidSessionIdentifierException, SessionCacheDAOException;
	
	public void addDescriptorSessionIdentifier(Principal principal, String entityID, String descriptorSessionID)
		throws InvalidSessionIdentifierException, InvalidDescriptorIdentifierException, SessionCacheDAOException;
	
	public void updatePrincipalAttributes(Principal principal) throws SessionCacheDAOException;
	
	/** Update the idleGraceExpiryTime and delete any active entity sessions for the given principal. This method can be called after 
	 * a Principal has been logged out of any SPEP's for which an active session existed.
	 * 
	 * @param principal The principal to update.
	 * @param idleGraceExpiryTime The time at which an idle session will be considered expired.
	 * @throws SessionCacheDAOException
	 */
	public boolean updateIdleEntitySessions(Principal principal, long idleGraceExpiryTime) throws SessionCacheDAOException;
}
