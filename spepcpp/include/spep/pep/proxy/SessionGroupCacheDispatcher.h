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
 * Creation Date: 14/02/2007
 * 
 * Purpose: 
 */

#ifndef SESSIONGROUPCACHEDISPATCHER_H_
#define SESSIONGROUPCACHEDISPATCHER_H_

#include <map>
#include <vector>
#include <string>
#include <unicode/unistr.h>
#include "spep/Util.h"
#include "spep/ipc/Dispatcher.h"
#include "spep/pep/impl/SessionGroupCacheImpl.h"

namespace spep{ namespace ipc{
	
#define SESSIONGROUPCACHE "spep/pep/SessionGroupCache/"
#define SESSIONGROUPCACHE_updateCache SESSIONGROUPCACHE "updateCache"
#define SESSIONGROUPCACHE_clearCache SESSIONGROUPCACHE "clearCache"
#define SESSIONGROUPCACHE_makeCachedAuthzDecision SESSIONGROUPCACHE "makeCachedAuthzDecision"
	
	/**
	 * Serializable object for the parameters to SessionGroupCache::makeCachedAuthzDecision
	 */
	class SPEPEXPORT SessionGroupCache_MakeCachedAuthzDecisionCommand
	{
		
		public:
		SessionGroupCache_MakeCachedAuthzDecisionCommand(){}
		SessionGroupCache_MakeCachedAuthzDecisionCommand( std::wstring sID, UnicodeString r )
		: sessionID(sID), resource(r) {}
		std::wstring sessionID;
		UnicodeString resource;
		template <class Archive>
		void serialize( Archive &ar, const unsigned int version )
		{ ar & sessionID & resource; }
		
	};
	
	/**
	 * Serializable object for the parameters to SessionGroupCache::updateCache
	 */
	class SPEPEXPORT SessionGroupCache_UpdateCacheCommand
	{
		
		public:
		SessionGroupCache_UpdateCacheCommand(){}
		SessionGroupCache_UpdateCacheCommand( std::wstring sID, UnicodeString gT, std::vector<UnicodeString> aT, spep::Decision d )
		: sessionID(sID), groupTarget(gT), authzTargets(aT), decision(d) {}
		std::wstring sessionID;
		UnicodeString groupTarget;
		std::vector<UnicodeString> authzTargets;
		spep::Decision decision;
		template <class Archive>
		void serialize( Archive &ar, const unsigned int version )
		{ ar & sessionID & groupTarget & authzTargets & decision; }
		
	};
	
	/**
	 * Dispatcher implementation for the session group cache.
	 */
	class SPEPEXPORT SessionGroupCacheDispatcher : public Dispatcher
	{
		
		SessionGroupCache *_sessionGroupCache;
		std::string _prefix;
		
		public:
		/**
		 * Constructor
		 * @param sessionGroupCache The session group cache to use when dispatching.
		 */
		SessionGroupCacheDispatcher(SessionGroupCache *sessionGroupCache);
		virtual ~SessionGroupCacheDispatcher();
		virtual bool dispatch( MessageHeader &header, Engine &en );
		
	};
	
} }

#endif /*SESSIONGROUPCACHEDISPATCHER_H_*/
