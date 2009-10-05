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

spep::SessionGroupCacheImpl::SessionGroupCacheImpl( saml2::Logger *logger, spep::Decision defaultPolicyDecision )
:
_localLogger( logger, "spep::SessionGroupCacheImpl" ),
_cacheMutex(),
_initialized(false),
_defaultPolicyDecision(defaultPolicyDecision)
{
}

spep::SessionGroupCacheImpl::~SessionGroupCacheImpl()
{
	GroupCacheMap::iterator iter;
	for (iter = _groupCaches.begin(); iter != _groupCaches.end(); ++iter)
	{
		delete iter->second;
	}	
}

bool spep::SessionGroupCacheImpl::targetMatch( const UnicodeString &pattern, const UnicodeString &target )
{
	// TODO Opportunity for caching of compiled regex patterns is here.
	UParseError parseError;
	UErrorCode errorCode = U_ZERO_ERROR;
	// Perform the regular expression matching here.
	UBool result = RegexPattern::matches( pattern, target, parseError, errorCode );
	
	if ( U_FAILURE( errorCode ) )
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
	
	ScopedLock lock(_cacheMutex);
	
	// Populate the cache with the group targets
	GroupTargetMap::iterator iter;
	for (iter = _groupTargets.begin(); iter != _groupTargets.end(); ++iter)
	{
		groupCache->updateCache( iter->first, iter->second, Decision::CACHE );
	}
	
	return groupCache;
}

void spep::SessionGroupCacheImpl::updateCache( std::wstring &sessionID, UnicodeString groupTarget, std::vector<UnicodeString> &authzTargets, spep::Decision decision )
{
	ScopedLock lock(_cacheMutex);
	
	// operator[] here is ok because we're creating the entry anyway.
	GroupCache *groupCache = _groupCaches[ sessionID ]; 
	if ( groupCache == NULL )
	{
		// If the group cache doesn't exist, create it and insert into the map.
		groupCache = createDefaultGroupCache();
		_groupCaches[ sessionID ] = groupCache;
	}
	
	// Perform a cache update.
	groupCache->updateCache( groupTarget, authzTargets, decision );
}

void spep::SessionGroupCacheImpl::clearCache( std::map< UnicodeString, std::vector<UnicodeString> > &groupTargets )
{
	GroupCacheMap::iterator iter;
	ScopedLock lock(_cacheMutex);
	
	for (iter = _groupCaches.begin(); iter != _groupCaches.end(); ++iter)
	{
		// deleting these while they're still in the map would be bad.
		// but we are inside a lock so it doesn't matter.
		GroupCache *groupCache = iter->second;
		delete groupCache;
	}
	
	_groupCaches.clear();
	
	_groupTargets.clear();
	_groupTargets = groupTargets;
	
	_localLogger.debug() << "Cleared cache. " << boost::lexical_cast<std::string>(groupTargets.size()) << " group targets in cache now.";
	for( std::map< UnicodeString, std::vector<UnicodeString> >::iterator groupTargetIterator = groupTargets.begin();
		groupTargetIterator != groupTargets.end(); ++groupTargetIterator )
	{
		_localLogger.debug() << "Group target " << UnicodeStringConversion::toString( groupTargetIterator->first );
		for( std::vector<UnicodeString>::iterator authzTargetIterator = groupTargetIterator->second.begin();
			authzTargetIterator != groupTargetIterator->second.end(); ++authzTargetIterator )
		{
			_localLogger.debug() << "- authz target " << UnicodeStringConversion::toString( *authzTargetIterator );
		}
	}
	
	// TODO This was previously in updateCache() as the last line. Why?
	_initialized = true;
}

spep::Decision spep::SessionGroupCacheImpl::makeCachedAuthzDecision( std::wstring sessionID, UnicodeString resource )
{
	{
		std::stringstream ss;
		ss << "Going to make cached authz decision for session [" << UnicodeStringConversion::toString( sessionID );
		ss << "] resource: " << UnicodeStringConversion::toString( resource ) << std::ends;

		_localLogger.debug() << ss.str();
	}
	
	if (!_initialized)
	{
		_localLogger.error() << "Session group cache has not been initialized. Rejecting authorization request.";
		return Decision::ERROR;
	}
	
	ScopedLock lock(_cacheMutex);
	
	// Find the group cache for this session.
	GroupCacheMap::iterator iter = _groupCaches.find( sessionID );
	if (iter == _groupCaches.end() || iter->second == NULL)
	{
		// Not there, we need a new set of authz decisions from the PDP.
		_localLogger.debug() << "No authorization cache for this session. Returning.";
		return Decision::CACHE;
	}
	
	GroupCache *groupCache = iter->second;
	_localLogger.debug() << "Got authorization cache for session. Going to perform cached authorization.";
	
	// Go into the group cache to make the decision.
	Decision result = groupCache->makeCachedAuthzDecision( resource );
	
	// No decision was made, so return the default policy decision.
	if (result == Decision::NONE)
	{
		std::stringstream ss;
		ss << "No matching policy for resource: " << UnicodeStringConversion::toString( resource ) << ". Falling back to default policy decision." << std::ends;
		_localLogger.debug() << ss.str();
		return _defaultPolicyDecision;
	}
	
	{
		std::stringstream ss;
		ss << "Resource [" << UnicodeStringConversion::toString( resource ) << "] <- session [" << UnicodeStringConversion::toString( sessionID ) << "] .. result is ";
		
		if( result == Decision::PERMIT )
		{
			ss << "PERMIT";
		}
		else if( result == Decision::DENY )
		{
			ss << "DENY";
		}
		else if( result == Decision::CACHE )
		{
			ss << "CACHE";
		}
		else if( result == Decision::ERROR )
		{
			ss << "ERROR";
		}
		else
		{
			ss << "An invalid decision";
		}
		
		ss << std::ends;
		_localLogger.info() << ss.str();
	}
	
	return result;
}

spep::AuthzTargetCache::AuthzTargetCache()
{
}

spep::AuthzTargetCache::~AuthzTargetCache()
{
}

spep::Decision spep::AuthzTargetCache::makeCachedAuthzDecision( UnicodeString resource )
{
	Decision result;
	
	// Loop through the cache of decisions
	DecisionMap::iterator iter;
	for ( iter = _decisions.begin(); iter != _decisions.end(); ++iter )
	{
		// If the resource matches this authz target..
		if ( SessionGroupCacheImpl::targetMatch( iter->first, resource ) )
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

void spep::AuthzTargetCache::updateCache( std::vector<UnicodeString> &authzTargets, spep::Decision decision )
{
	// Set all the authz target decisions to this decision
	std::vector<UnicodeString>::iterator iter;
	for (iter = authzTargets.begin(); iter != authzTargets.end(); ++iter)
	{
		_decisions[ *iter ] = decision;
	}
}

spep::GroupCache::GroupCache()
{
}

spep::GroupCache::~GroupCache()
{
	// Delete all the authz target caches from the map.
	AuthzTargetMap::iterator iter;
	for (iter = _authzTargets.begin(); iter != _authzTargets.end(); ++iter)
	{
		delete iter->second;
	}
}

spep::Decision spep::GroupCache::makeCachedAuthzDecision( UnicodeString resource )
{
	Decision result;
	
	// Loop through authz target caches
	AuthzTargetMap::iterator iter;
	for (iter = _authzTargets.begin(); iter != _authzTargets.end(); ++iter)
	{
		// If the group target matches..
		if ( SessionGroupCacheImpl::targetMatch( iter->first, resource ) )
		{
			AuthzTargetCache *authzTargetCache = iter->second;
			
			if ( NULL == authzTargetCache )
			{
				// No authz target cache there, we need to update the cache.
				result = Decision::CACHE;
			}
			else
			{
				// Get the decision from the authz target cache
				Decision nodeDecision = authzTargetCache->makeCachedAuthzDecision( resource );
				
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

void spep::GroupCache::updateCache( UnicodeString groupTarget, std::vector<UnicodeString> &authzTargets, spep::Decision decision )
{
	AuthzTargetCache *authzTargetCache = _authzTargets[ groupTarget ];
	
	if (authzTargetCache == NULL)
	{
		authzTargetCache = new AuthzTargetCache();
		_authzTargets[ groupTarget ] = authzTargetCache;
	}
	
	authzTargetCache->updateCache( authzTargets, decision );
}
