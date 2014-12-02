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

#include "spep/pep/proxy/SessionGroupCacheProxy.h"
#include "spep/pep/proxy/SessionGroupCacheDispatcher.h"

static const std::string UPDATE_CACHE = SESSIONGROUPCACHE_updateCache;
static const std::string CLEAR_CACHE = SESSIONGROUPCACHE_clearCache;
static const std::string MAKE_CACHE_AUTHZ_DECISION = SESSIONGROUPCACHE_makeCachedAuthzDecision;


spep::ipc::SessionGroupCacheProxy::SessionGroupCacheProxy(spep::ipc::ClientSocketPool *socketPool) :
    mSocketPool(socketPool)
{
}

spep::ipc::SessionGroupCacheProxy::~SessionGroupCacheProxy()
{
}

void spep::ipc::SessionGroupCacheProxy::updateCache(const std::wstring &sessionID, const UnicodeString& groupTarget, std::vector<UnicodeString> &authzTargets, spep::Decision decision)
{
    SessionGroupCache_UpdateCacheCommand command(sessionID, groupTarget, authzTargets, decision);

    ClientSocketLease clientSocket(mSocketPool);
    clientSocket->makeNonBlockingRequest<SessionGroupCache_UpdateCacheCommand>(UPDATE_CACHE, command);
}

void spep::ipc::SessionGroupCacheProxy::clearCache(std::map< UnicodeString, std::vector<UnicodeString> > &groupTargets)
{
    ClientSocketLease clientSocket(mSocketPool);
    clientSocket->makeNonBlockingRequest< std::map< UnicodeString, std::vector<UnicodeString> > >(CLEAR_CACHE, groupTargets);
}

spep::Decision spep::ipc::SessionGroupCacheProxy::makeCachedAuthzDecision(const std::wstring& sessionID, const UnicodeString& resource)
{
    SessionGroupCache_MakeCachedAuthzDecisionCommand command(sessionID, resource);

    ClientSocketLease clientSocket(mSocketPool);
    return clientSocket->makeRequest< spep::Decision, SessionGroupCache_MakeCachedAuthzDecisionCommand >(MAKE_CACHE_AUTHZ_DECISION, command);
}
