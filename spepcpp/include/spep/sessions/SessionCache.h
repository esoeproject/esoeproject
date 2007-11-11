/* Copyright 2006-2007, Queensland University of Technology
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
 * Creation Date: 08/01/2007
 * 
 * Purpose: Defines the session cache, where both authenticated and unauthenticated
 * 		sessions are stored.
 */

#ifndef SESSIONCACHE_H_
#define SESSIONCACHE_H_

#include "spep/Util.h"
#include "spep/sessions/PrincipalSession.h"
#include "spep/sessions/UnauthenticatedSession.h"

#include <map>

namespace spep
{
	
	class SPEPEXPORT SessionCache
	{
		public:
		virtual ~SessionCache(){}
		
		/**
		 * Gets the principal session represented by the given local session identifier and assigns it to 
		 * the reference variable provided.
		 * @param principalSession Output variable for the principal session
		 * @param localSessionID The local session identifier to attempt to resolve
		 */
		virtual void getPrincipalSession(PrincipalSession& principalSession, std::string localSessionID) = 0;
		/**
		 * Gets the principal session represented by the given ESOE session identifier and assigns it to 
		 * the reference variable provided.
		 * @param principalSession Output variable for the principal session
		 * @param esoeSessionID The ESOE session identifier to attempt to resolve
		 */
		virtual void getPrincipalSessionByEsoeSessionID(PrincipalSession& principalSession, std::wstring esoeSessionID) = 0;
		/**
		 * Creates a new principal session in the sessions cache, or updates the cache when a local session ID 
		 * is added to a principal session.
		 * @param sessionID The local session ID
		 * @param principalSession The principal session to insert
		 */
		virtual void insertPrincipalSession(std::string sessionID, PrincipalSession &principalSession) = 0;
		/**
		 * Terminates the principal session represented by the local session identifier provided.
		 * @param sessionID A local session identifier of the session to terminate.
		 */ 
		virtual void terminatePrincipalSession(std::wstring sessionID) = 0;
		
		/**
		 * Retrieves the unauthenticated session from the session cache for the given requestID and assigns
		 * it to the reference variable provided.
		 * @param unauthenticatedSession Output variable for the unauthenticated session.
		 * @param requestID The ID of the AuthnRequest SAML document generated for this session
		 */
		virtual void getUnauthenticatedSession(UnauthenticatedSession &unauthenticatedSession, std::wstring requestID) = 0;
		/**
		 * Insert an unauthenticated session into the session cache.
		 * @param unauthenticatedSession The new unauthenticated session to insert.
		 */
		virtual void insertUnauthenticatedSession(UnauthenticatedSession &unauthenticatedSession) = 0;
		/**
		 * Terminates an unauthenticated session in the session cache.
		 * @param requestID The ID of the AuthnRequest SAML document for the session that is to be terminated.
		 */
		virtual void terminateUnauthenticatedSession(std::wstring requestID) = 0;
		/**
		 * Terminates all sessions that have expired, using the given session timeout.
		 * @param sessionCacheTimeout The session timeout value to use.
		 */
		virtual void terminateExpiredSessions( int sessionCacheTimeout ) = 0;
	};
	
}

#endif /* SESSIONCACHE_H_ */
