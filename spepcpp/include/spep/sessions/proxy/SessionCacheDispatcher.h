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

#ifndef SESSIONCACHEDISPATCHER_H_
#define SESSIONCACHEDISPATCHER_H_

#include "spep/Util.h"
#include "spep/sessions/SessionCache.h"

#include "spep/ipc/Dispatcher.h"
#include "spep/ipc/MessageHeader.h"
#include "spep/ipc/Engine.h"
#include "spep/ipc/Serialization.h"

#include <string>

namespace spep { namespace ipc {

#define SESSIONCACHEPREFIX "spep/sessions/SessionCache/"
#define SESSIONCACHE_insertPrincipalSession SESSIONCACHEPREFIX "insertPrincipalSession"
#define SESSIONCACHE_getPrincipalSession SESSIONCACHEPREFIX "getPrincipalSession"
#define SESSIONCACHE_getPrincipalSessionByEsoeSessionID SESSIONCACHEPREFIX "getPrincipalSessionByEsoeSessionID"
#define SESSIONCACHE_terminatePrincipalSession SESSIONCACHEPREFIX "terminatePrincipalSession"
#define SESSIONCACHE_insertUnauthenticatedSession SESSIONCACHEPREFIX "insertUnauthenticatedSession"
#define SESSIONCACHE_getUnauthenticatedSession SESSIONCACHEPREFIX "getUnauthenticatedSession"
#define SESSIONCACHE_terminateUnauthenticatedSession SESSIONCACHEPREFIX "terminateUnauthenticatedSession"
#define SESSIONCACHE_terminateAllPrincipalSessions SESSIONCACHEPREFIX "terminateAllPrincipalSessions"
#define SESSIONCACHE_terminateExpiredSessions SESSIONCACHEPREFIX "terminateExpiredSessions"

	/** Serializable object for the parameters to SessionCache::insertClientSession */
	class SessionCache_InsertClientSessionCommand
	{
		
		friend class spep::ipc::access;
		template <class Archive>
		void serialize( Archive &ar, const unsigned int version )
		{ ar & sessionID & principalSession; }
		
		public:
		SessionCache_InsertClientSessionCommand() : sessionID(), principalSession() {}
		SessionCache_InsertClientSessionCommand( std::string &sID, PrincipalSession &prSession )
		: sessionID(sID), principalSession(prSession) {}
		
		std::string sessionID;
		PrincipalSession principalSession;
		
	};

	/**
	 * Dispatcher implementation for the session cache.
	 */
	class SPEPEXPORT SessionCacheDispatcher : public Dispatcher
	{
		
		SessionCache* _sessionCache;
		std::string _prefix;
		
		public:
		SessionCacheDispatcher(SessionCache *sessionCache);
		virtual ~SessionCacheDispatcher();
		
		virtual bool dispatch( MessageHeader &header, Engine &en );
		
	};
	
} }

#endif /*SESSIONCACHEDISPATCHER_H_*/
