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

#ifndef SESSIONCACHEIMPL_H_
#define SESSIONCACHEIMPL_H_

#include "spep/Util.h"
#include "saml2/logging/api.h"
#include "saml2/logging/api.h"
#include "spep/sessions/SessionCache.h"
#include "spep/Util.h"

#include <map>

#include <boost/thread/mutex.hpp>

namespace spep {
	
	class SPEPEXPORT SessionCacheImpl : public SessionCache
	{
		
		typedef std::map<std::string,std::wstring> SessionIDMap;
		typedef std::map<std::wstring,PrincipalSession> ESOESessionMap;
		typedef std::map<std::wstring,UnauthenticatedSession> UnauthenticatedSessionMap;
		
		public:
		SessionCacheImpl( saml2::Logger *logger );
		
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
		saml2::LocalLogger _localLogger;
		SessionIDMap _sessionIDs;
		ESOESessionMap _esoeSessions; // Client sessions referenced by esoe session ID
		mutable Mutex _principalSessionsMutex;
		UnauthenticatedSessionMap _unauthenticatedSessions;
		mutable Mutex _unauthenticatedSessionsMutex;
		
	};
	
}

#endif /*SESSIONCACHEIMPL_H_*/
