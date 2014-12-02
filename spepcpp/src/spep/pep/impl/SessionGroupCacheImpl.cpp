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
 * Creation Date: 25/09/2006
 * 
 * Purpose: 
 */
 
#include "spep/pep/SessionGroupCache.h"
#include "spep/pep/impl/SessionGroupCacheImpl.h"
#include "spep/Util.h"
#include "spep/UnicodeStringConversion.h"

#include <unicode/regex.h>
#include <unicode/parseerr.h>
#include <unicode/utypes.h>
#include <unicode/ustring.h>

#include <iostream>
#include <sstream>

#include <boost/lexical_cast.hpp>

spep::SessionGroupCacheImpl::SessionGroupCacheImpl(saml2::Logger *logger, spep::Decision defaultPolicyDecision) :
    mLocalLogger(logger, "spep::SessionGroupCacheImpl"),
    mCacheMutex(),
    mInitialized(false),
    mDefaultPolicyDecision(defaultPolicyDecision)
{
}

spep::SessionGroupCacheImpl::~SessionGroupCacheImpl()
{
    for (auto iter : mGroupCaches) {
        delete iter.second;
    }
}

bool spep::SessionGroupCacheImpl::targetMatch(const UnicodeString &pattern, const UnicodeString &target)
{
    // TODO Opportunity for caching of compiled regex patterns is here.
    UParseError parseError;
    UErrorCode errorCode = U_ZERO_ERROR;
    // Perform the regular expression matching here.
    UBool result = RegexPattern::matches(pattern, target, parseError, errorCode);

    if (U_FAILURE(errorCode))
    {
        // TODO throw u_errorName( errorCode )
        throw std::exception();
    }

    // FALSE is defined by ICU. This line for portability.
    return (result != FALSE);
}

spep::GroupCache *spep::SessionGroupCacheImpl::createDefaultGroupCache()
{
    // Create a new group cache
    GroupCache *groupCache = new GroupCache();

    ScopedLock lock(mCacheMutex);

    // Populate the cache with the group targets
    for (auto iter: mGroupTargets) {
        groupCache->updateCache(iter.first, iter.second, Decision::CACHE);
    }

    return groupCache;
}

void spep::SessionGroupCacheImpl::updateCache(const std::wstring &sessionID, const UnicodeString& groupTarget, std::vector<UnicodeString> &authzTargets, spep::Decision decision)
{
    ScopedLock lock(mCacheMutex);

    // operator[] here is ok because we're creating the entry anyway.
    GroupCache *groupCache = mGroupCaches[sessionID];
    if (groupCache == NULL)
    {
        // If the group cache doesn't exist, create it and insert into the map.
        groupCache = createDefaultGroupCache();
        mGroupCaches[sessionID] = groupCache;
    }

    // Perform a cache update.
    groupCache->updateCache(groupTarget, authzTargets, decision);
}

void spep::SessionGroupCacheImpl::clearCache(std::map< UnicodeString, std::vector<UnicodeString> > &groupTargets)
{
    ScopedLock lock(mCacheMutex);

    for (auto iter = mGroupCaches.begin(); iter != mGroupCaches.end(); ++iter)
    {
        // deleting these while they're still in the map would be bad.
        // but we are inside a lock so it doesn't matter.
        GroupCache *groupCache = iter->second;
        delete groupCache;
    }

    mGroupCaches.clear();

    mGroupTargets.clear();
    mGroupTargets = groupTargets;

    mLocalLogger.debug() << "Cleared cache. " << boost::lexical_cast<std::string>(groupTargets.size()) << " group targets in cache now.";
    for (auto groupTargetIterator = groupTargets.begin();
        groupTargetIterator != groupTargets.end(); ++groupTargetIterator)
    {
        mLocalLogger.debug() << "Group target " << UnicodeStringConversion::toString(groupTargetIterator->first);
        for (auto authzTargetIterator = groupTargetIterator->second.begin();
            authzTargetIterator != groupTargetIterator->second.end(); ++authzTargetIterator)
        {
            mLocalLogger.debug() << "- authz target " << UnicodeStringConversion::toString(*authzTargetIterator);
        }
    }

    // TODO This was previously in updateCache() as the last line. Why?
    mInitialized = true;
}

spep::Decision spep::SessionGroupCacheImpl::makeCachedAuthzDecision(const std::wstring& sessionID, const UnicodeString& resource)
{
    {
        std::stringstream ss;
        ss << "Going to make cached authz decision for session [" << UnicodeStringConversion::toString(sessionID);
        ss << "] resource: " << UnicodeStringConversion::toString(resource) << std::ends;

        mLocalLogger.debug() << ss.str();
    }

    if (!mInitialized)
    {
        mLocalLogger.error() << "Session group cache has not been initialized. Rejecting authorization request.";
        return Decision::ERROR;
    }

    ScopedLock lock(mCacheMutex);

    // Find the group cache for this session.
    auto iter = mGroupCaches.find(sessionID);
    if (iter == mGroupCaches.end() || iter->second == NULL)
    {
        // Not there, we need a new set of authz decisions from the PDP.
        mLocalLogger.debug() << "No authorization cache for this session. Returning.";
        return Decision::CACHE;
    }

    GroupCache *groupCache = iter->second;
    mLocalLogger.debug() << "Got authorization cache for session. Going to perform cached authorization.";

    // Go into the group cache to make the decision.
    Decision result = groupCache->makeCachedAuthzDecision(resource);

    // No decision was made, so return the default policy decision.
    if (result == Decision::NONE)
    {
        std::stringstream ss;
        ss << "No matching policy for resource: " << UnicodeStringConversion::toString(resource) << ". Falling back to default policy decision." << std::ends;
        mLocalLogger.debug() << ss.str();
        return mDefaultPolicyDecision;
    }

    {
        std::stringstream ss;
        ss << "Resource [" << UnicodeStringConversion::toString(resource) << "] <- session [" << UnicodeStringConversion::toString(sessionID) << "] .. result is ";

        if (result == Decision::PERMIT)
        {
            ss << "PERMIT";
        }
        else if (result == Decision::DENY)
        {
            ss << "DENY";
        }
        else if (result == Decision::CACHE)
        {
            ss << "CACHE";
        }
        else if (result == Decision::ERROR)
        {
            ss << "ERROR";
        }
        else
        {
            ss << "An invalid decision";
        }

        ss << std::ends;
        mLocalLogger.info() << ss.str();
    }

    return result;
}

spep::AuthzTargetCache::AuthzTargetCache()
{
}

spep::AuthzTargetCache::~AuthzTargetCache()
{
}

spep::Decision spep::AuthzTargetCache::makeCachedAuthzDecision(const UnicodeString& resource)
{
    Decision result;

    // Loop through the cache of decisions
    for (auto iter = mDecisions.begin(); iter != mDecisions.end(); ++iter)
    {
        // If the resource matches this authz target..
        if (SessionGroupCacheImpl::targetMatch(iter->first, resource))
        {
            // Get the decision
            Decision nodeDecision = iter->second;
            if (nodeDecision == Decision::NONE)
            {
                // If it's empty, the cache needs to be updated.
                nodeDecision = Decision::CACHE;
            }

            // Add the decision at this node to the resulting decision
            result += nodeDecision;

            // If it's a deny, we can fail-fast
            if (result == Decision::DENY)
            {
                return result;
            }
        }
    }

    return result;
}

void spep::AuthzTargetCache::updateCache(std::vector<UnicodeString> &authzTargets, spep::Decision decision)
{
    // Set all the authz target decisions to this decision
    std::vector<UnicodeString>::iterator iter;
    for (iter = authzTargets.begin(); iter != authzTargets.end(); ++iter)
    {
        mDecisions[*iter] = decision;
    }
}

spep::GroupCache::GroupCache()
{
}

spep::GroupCache::~GroupCache()
{
    // Delete all the authz target caches from the map.
    AuthzTargetMap::iterator iter;
    for (iter = mAuthzTargets.begin(); iter != mAuthzTargets.end(); ++iter)
    {
        delete iter->second;
    }
}

spep::Decision spep::GroupCache::makeCachedAuthzDecision(const UnicodeString& resource)
{
    Decision result;

    // Loop through authz target caches
    for (auto iter = mAuthzTargets.begin(); iter != mAuthzTargets.end(); ++iter)
    {
        // If the group target matches..
        if (SessionGroupCacheImpl::targetMatch(iter->first, resource))
        {
            AuthzTargetCache *authzTargetCache = iter->second;

            if (nullptr == authzTargetCache)
            {
                // No authz target cache there, we need to update the cache.
                result = Decision::CACHE;
            }
            else
            {
                // Get the decision from the authz target cache
                Decision nodeDecision = authzTargetCache->makeCachedAuthzDecision(resource);

                // Add it to the result decision.
                result += nodeDecision;

                // If it's a deny, we can fail fast.
                if (result == Decision::DENY)
                {
                    return result;
                }
            }
        }
    }

    return result;
}

void spep::GroupCache::updateCache(const UnicodeString& groupTarget, std::vector<UnicodeString> &authzTargets, spep::Decision decision)
{
    AuthzTargetCache *authzTargetCache = mAuthzTargets[groupTarget];

    if (authzTargetCache == nullptr)
    {
        authzTargetCache = new AuthzTargetCache();
        mAuthzTargets[groupTarget] = authzTargetCache;
    }

    authzTargetCache->updateCache(authzTargets, decision);
}
