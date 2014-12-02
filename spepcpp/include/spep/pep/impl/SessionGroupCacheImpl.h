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
#include "saml2/logging/api.h"
#include "saml2/logging/api.h"

namespace spep
{
	
    /**
     * Authorization target cache. Caches regular expression resource matchers, and the
     * corresponding decisions cached against them.
     */
    class SPEPEXPORT AuthzTargetCache
    {
        friend class GroupCache;
        typedef std::map<UnicodeString, Decision> DecisionMap; // TODO: check to see if this can be an unordered_map

    public:
        ~AuthzTargetCache();

    private:
        DecisionMap mDecisions;

        AuthzTargetCache();
        Decision makeCachedAuthzDecision(const UnicodeString& resource);
        void updateCache(std::vector<UnicodeString> &authzTargets, Decision decision);
    };

    /**
     * Group target cache. Caches regular expression matchers for group targets, and an
     * authorization target cache for each
     */
    class SPEPEXPORT GroupCache
    {
        friend class SessionGroupCacheImpl;
        typedef std::map<UnicodeString, AuthzTargetCache*> AuthzTargetMap; // TODO: check to see if this can be an unordered_map

    public:
        ~GroupCache();

    private:
        AuthzTargetMap mAuthzTargets;

        GroupCache();
        Decision makeCachedAuthzDecision(const UnicodeString& resource);
        void updateCache(const UnicodeString& groupTarget, std::vector<UnicodeString> &authzTargets, Decision decision);
    };

    /**
     * The session group cache implementation class
     */
    class SPEPEXPORT SessionGroupCacheImpl : public SessionGroupCache
    {
        typedef std::map<std::wstring, GroupCache*> GroupCacheMap;
        typedef std::map< UnicodeString, std::vector<UnicodeString> > GroupTargetMap;

    public:
        /**
         * Checks if a target resource matches the given pattern.
         * @param target The resource to match against
         * @param pattern The regular expression to use when matching.
         */
        static bool targetMatch(const UnicodeString &target, const UnicodeString &pattern);

        virtual ~SessionGroupCacheImpl();
        SessionGroupCacheImpl(saml2::Logger *logger, Decision defaultPolicyDecision);
        /** @see spep::SessionGroupCache */
        /*@{*/
        virtual void updateCache(const std::wstring &esoeSessionID, const UnicodeString& groupTarget, std::vector<UnicodeString> &authzTargets, Decision decision) override;
        virtual void clearCache(std::map< UnicodeString, std::vector<UnicodeString> > &groupTargets) override;
        virtual Decision makeCachedAuthzDecision(const std::wstring& esoeSessionID, const UnicodeString& resource) override;
        /*@}*/

    private:

        saml2::LocalLogger mLocalLogger;
        mutable Mutex mCacheMutex;
        bool mInitialized;
        GroupCacheMap mGroupCaches;
        GroupTargetMap mGroupTargets;
        Decision mDefaultPolicyDecision;

        GroupCache *createDefaultGroupCache();

        // Unimplemented private copy constructor and assignment operators, so we don't accidentally copy it.
        SessionGroupCacheImpl(const SessionGroupCacheImpl& rhs);
        SessionGroupCacheImpl& operator=(const SessionGroupCacheImpl& rhs);
    };

}

#endif /*SESSIONGROUPCACHEIMPL_H_*/
