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

static const char *updateCache = SESSIONGROUPCACHE_updateCache;
static const char *clearCache = SESSIONGROUPCACHE_clearCache;
static const char *makeCachedAuthzDecision = SESSIONGROUPCACHE_makeCachedAuthzDecision;


spep::ipc::SessionGroupCacheProxy::SessionGroupCacheProxy( spep::ipc::ClientSocket *clientSocket )
:
_clientSocket( clientSocket )
{
}

spep::ipc::SessionGroupCacheProxy::~SessionGroupCacheProxy()
{
}

void spep::ipc::SessionGroupCacheProxy::updateCache( std::wstring &sessionID, UnicodeString groupTarget, std::vector<UnicodeString> &authzTargets, spep::Decision decision )
{
	std::string dispatch( ::updateCache );
	SessionGroupCache_UpdateCacheCommand command( sessionID, groupTarget, authzTargets, decision );
	_clientSocket->makeNonBlockingRequest<SessionGroupCache_UpdateCacheCommand>( dispatch, command );
}

void spep::ipc::SessionGroupCacheProxy::clearCache( std::map< UnicodeString, std::vector<UnicodeString> > &groupTargets )
{
	std::string dispatch( ::clearCache );
	_clientSocket->makeNonBlockingRequest< std::map< UnicodeString, std::vector<UnicodeString> > >( dispatch, groupTargets );
}

spep::Decision spep::ipc::SessionGroupCacheProxy::makeCachedAuthzDecision( std::wstring sessionID, UnicodeString resource )
{
	std::string dispatch( ::makeCachedAuthzDecision );
	SessionGroupCache_MakeCachedAuthzDecisionCommand command( sessionID, resource );
	return _clientSocket->makeRequest< spep::Decision, SessionGroupCache_MakeCachedAuthzDecisionCommand >( dispatch, command );
}
