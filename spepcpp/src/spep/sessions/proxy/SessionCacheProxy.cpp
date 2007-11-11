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

#include "spep/sessions/proxy/SessionCacheProxy.h"
#include "spep/sessions/proxy/SessionCacheDispatcher.h"
#include "spep/ipc/Socket.h"


using namespace spep;

static const char *insertPrincipalSession = SESSIONCACHE_insertPrincipalSession;
static const char *getPrincipalSession = SESSIONCACHE_getPrincipalSession;
static const char *getPrincipalSessionByEsoeSessionID = SESSIONCACHE_getPrincipalSessionByEsoeSessionID;
static const char *terminatePrincipalSession = SESSIONCACHE_terminatePrincipalSession;
static const char *insertUnauthenticatedSession = SESSIONCACHE_insertUnauthenticatedSession;
static const char *getUnauthenticatedSession = SESSIONCACHE_getUnauthenticatedSession;
static const char *terminateUnauthenticatedSession = SESSIONCACHE_terminateUnauthenticatedSession;
static const char *terminateExpiredSessions = SESSIONCACHE_terminateExpiredSessions;


spep::ipc::SessionCacheProxy::SessionCacheProxy( spep::ipc::ClientSocket *clientSocket )
:
_clientSocket(clientSocket)
{}

void spep::ipc::SessionCacheProxy::getPrincipalSession(spep::PrincipalSession &principalSession, std::string sessionID)
{
	std::string dispatch(::getPrincipalSession);
	principalSession = _clientSocket->makeRequest<PrincipalSession, std::string>( dispatch, sessionID );
}

void spep::ipc::SessionCacheProxy::getPrincipalSessionByEsoeSessionID(spep::PrincipalSession &principalSession, std::wstring esoeSessionIndex)
{
	std::string dispatch(::getPrincipalSessionByEsoeSessionID);
	principalSession = _clientSocket->makeRequest<PrincipalSession, std::wstring>( dispatch, esoeSessionIndex );
}

void spep::ipc::SessionCacheProxy::insertPrincipalSession(std::string sessionID, spep::PrincipalSession &principalSession)
{
	std::string dispatch(::insertPrincipalSession);
	SessionCache_InsertClientSessionCommand command( sessionID, principalSession );
	_clientSocket->makeNonBlockingRequest( dispatch, command );
}

void spep::ipc::SessionCacheProxy::terminatePrincipalSession(std::wstring sessionID)
{
	std::string dispatch(::terminatePrincipalSession);
	_clientSocket->makeNonBlockingRequest( dispatch, sessionID );
}

void spep::ipc::SessionCacheProxy::getUnauthenticatedSession(spep::UnauthenticatedSession &unauthenticatedSession, std::wstring requestID)
{
	std::string dispatch(::getUnauthenticatedSession);
	std::wstring requestIDLocal = requestID;
	unauthenticatedSession = _clientSocket->makeRequest<UnauthenticatedSession, std::wstring>( dispatch, requestIDLocal );
}

void spep::ipc::SessionCacheProxy::insertUnauthenticatedSession(spep::UnauthenticatedSession &unauthenticatedSession)
{
	std::string dispatch(::insertUnauthenticatedSession);
	_clientSocket->makeNonBlockingRequest( dispatch, unauthenticatedSession );
}

void spep::ipc::SessionCacheProxy::terminateUnauthenticatedSession(std::wstring requestID)
{
	std::string dispatch(::terminateUnauthenticatedSession);
	_clientSocket->makeNonBlockingRequest( dispatch, requestID );
}

void spep::ipc::SessionCacheProxy::terminateExpiredSessions( int sessionCacheTimeout )
{
	std::string dispatch(::terminateExpiredSessions);
	_clientSocket->makeNonBlockingRequest( dispatch, sessionCacheTimeout );
}
