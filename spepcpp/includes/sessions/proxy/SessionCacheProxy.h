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
 * Creation Date: 07/02/2007
 * 
 * Purpose: 
 */

#ifndef SESSIONCACHEPROXY_H_
#define SESSIONCACHEPROXY_H_

#include "sessions/SessionCache.h"

#include "ipc/Socket.h"

namespace spep { namespace ipc {
	
	class SessionCacheProxy : public spep::SessionCache
	{
		
		public:
		SessionCacheProxy( spep::ipc::ClientSocket *clientSocket );
		
		/** @see spep::SessionCache */
		/*@{*/
		virtual void getPrincipalSession(PrincipalSession& principalSession, std::string localSessionID);
		virtual void getPrincipalSessionByEsoeSessionID(PrincipalSession& principalSession, std::wstring esoeSessionID);
		virtual void insertPrincipalSession(std::string sessionID, PrincipalSession &principalSession);
		virtual void terminatePrincipalSession(std::wstring sessionID);
		
		virtual void getUnauthenticatedSession(UnauthenticatedSession &unauthenticatedSession, std::wstring requestID);
		virtual void insertUnauthenticatedSession(UnauthenticatedSession &unauthenticatedSession);
		virtual void terminateUnauthenticatedSession(std::wstring requestID);
		
		virtual void terminateExpiredSessions( int sessionCacheTimeout );
		/*@}*/

		private:
		// Defined mutable because even for const methods we still need to make an RPC call.
		mutable spep::ipc::ClientSocket *_clientSocket;
		
	};
	
} }

#endif /*SESSIONCACHEPROXY_H_*/
