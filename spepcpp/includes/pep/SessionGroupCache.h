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
 * Creation Date: 13/02/2007
 * 
 * Purpose: Caches authorization decisions made by the PDP.
 */

#ifndef SESSIONGROUPCACHE_H_
#define SESSIONGROUPCACHE_H_

#include "ipc/Serialization.h"
#include "sessions/PrincipalSession.h"
#include "pep/Decision.h"

namespace spep
{
	
	/**
	 * Caches authorization decisions made by the PDP.
	 */
	class SessionGroupCache
	{
		public:
		virtual ~SessionGroupCache(){}
		/**
		 * Updates the group cache for the given session ID.
		 * @param esoeSessionID The session ID to update the group cache for.
		 * @param groupTarget The group target match to update
		 * @param authzTargets The authorization targets to update
		 * @param decision The decision to cache against the authorization targets
		 */
		virtual void updateCache( std::wstring &esoeSessionID, UnicodeString groupTarget, std::vector<UnicodeString> &authzTargets, Decision decision ) = 0;
		
		/**
		 * Clears the authorization cache completely.
		 * @param groupTargets The new list of group targets to use for caching.
		 */
		virtual void clearCache( std::map< UnicodeString, std::vector<UnicodeString> > &groupTargets ) = 0;
		
		/**
		 * Uses the cache to make an authorization decision.
		 */
		virtual Decision makeCachedAuthzDecision( std::wstring esoeSessionID, UnicodeString resource ) = 0;
	};
	


}

#endif /*SESSIONGROUPCACHE_H_*/
