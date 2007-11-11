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
 * Purpose: 
 */

#ifndef SESSIONGROUPCACHEIMPL_H_
#define SESSIONGROUPCACHEIMPL_H_

#include <unicode/unistr.h>

#include <vector>
#include <map>
#include <string>

#include "spep/pep/SessionGroupCache.h"
#include "spep/Util.h"

#include "spep/sessions/PrincipalSession.h"
#include "spep/reporting/ReportingProcessor.h"
#include "spep/reporting/LocalReportingProcessor.h"

namespace spep
{
	
	/**
	 * Authorization target cache. Caches regular expression resource matchers, and the 
	 * corresponding decisions cached against them.
	 */
	class SPEPEXPORT AuthzTargetCache
	{
		friend class GroupCache;
		typedef std::map<UnicodeString, Decision> DecisionMap;

		public:
		~AuthzTargetCache();
		
		private:
		DecisionMap _decisions;
		
		AuthzTargetCache();
		Decision makeCachedAuthzDecision( UnicodeString resource );
		void updateCache( std::vector<UnicodeString> &authzTargets, Decision decision );
	};
	
	/**
	 * Group target cache. Caches regular expression matchers for group targets, and an
	 * authorization target cache for each
	 */
	class SPEPEXPORT GroupCache
	{
		friend class SessionGroupCacheImpl;
		typedef std::map<UnicodeString, AuthzTargetCache*> AuthzTargetMap;
		
		public:
		~GroupCache();
		
		private:
		AuthzTargetMap _authzTargets;
		
		GroupCache();
		Decision makeCachedAuthzDecision( UnicodeString resource );
		void updateCache( UnicodeString groupTarget, std::vector<UnicodeString> &authzTargets, Decision decision );
	};
	
	/**
	 * The session group cache implementation class
	 */
	class SPEPEXPORT SessionGroupCacheImpl : public SessionGroupCache
	{
		typedef std::map<std::wstring, GroupCache*> GroupCacheMap;
		typedef std::map< UnicodeString, std::vector<UnicodeString> > GroupTargetMap;
			
		private:
		LocalReportingProcessor _localReportingProcessor;
		mutable Mutex _cacheMutex;
		bool _initialized;
		GroupCacheMap _groupCaches;
		GroupTargetMap _groupTargets;
		Decision _defaultPolicyDecision;

		GroupCache *createDefaultGroupCache();
		
		// Unimplemented private copy constructor and assignment operators, so we don't accidentally copy it.
		SessionGroupCacheImpl( const SessionGroupCacheImpl& rhs );
		SessionGroupCacheImpl& operator=( const SessionGroupCacheImpl& rhs );
	
		public:
		/**
		 * Checks if a target resource matches the given pattern.
		 * @param target The resource to match against
		 * @param pattern The regular expression to use when matching.
		 */
		static bool targetMatch( const UnicodeString &target, const UnicodeString &pattern );

		~SessionGroupCacheImpl();
		SessionGroupCacheImpl( ReportingProcessor *reportingProcessor, Decision defaultPolicyDecision );
		/** @see spep::SessionGroupCache */
		/*@{*/
		virtual void updateCache( std::wstring &esoeSessionID, UnicodeString groupTarget, std::vector<UnicodeString> &authzTargets, Decision decision );
		virtual void clearCache( std::map< UnicodeString, std::vector<UnicodeString> > &groupTargets );
		virtual Decision makeCachedAuthzDecision( std::wstring esoeSessionID, UnicodeString resource );
		/*@}*/
		
	};
	
}

#endif /*SESSIONGROUPCACHEIMPL_H_*/
