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


spep::ipc::SessionCacheProxy::SessionCacheProxy( spep::ipc::ClientSocketPool *socketPool )
:
_socketPool(socketPool)
{}

void spep::ipc::SessionCacheProxy::getPrincipalSession(spep::PrincipalSession &principalSession, const std::string& sessionID)
{
	std::string dispatch(::getPrincipalSession);
	
	ClientSocketLease clientSocket( _socketPool );
	std::string localSessionID(sessionID);
	principalSession = clientSocket->makeRequest<PrincipalSession, std::string>( dispatch, localSessionID );
}

void spep::ipc::SessionCacheProxy::getPrincipalSessionByEsoeSessionID(spep::PrincipalSession &principalSession, const std::wstring& esoeSessionIndex)
{
	std::string dispatch(::getPrincipalSessionByEsoeSessionID);
	
	ClientSocketLease clientSocket( _socketPool );
	std::wstring localEsoeSessionIndex(esoeSessionIndex);
	principalSession = clientSocket->makeRequest<PrincipalSession, std::wstring>(dispatch, localEsoeSessionIndex);
}

void spep::ipc::SessionCacheProxy::insertPrincipalSession(const std::string& sessionID, spep::PrincipalSession &principalSession)
{
	std::string dispatch(::insertPrincipalSession);
	std::string localSessionID(sessionID);
	SessionCache_InsertClientSessionCommand command( localSessionID, principalSession );
	
	ClientSocketLease clientSocket( _socketPool );
	// Make a blocking request (even though we don't need a return value) so that we know the session has been inserted.
	clientSocket->makeRequest<NoData, SessionCache_InsertClientSessionCommand>( dispatch, command );
}

void spep::ipc::SessionCacheProxy::terminatePrincipalSession(const std::wstring& sessionID)
{
	std::string dispatch(::terminatePrincipalSession);
	
	ClientSocketLease clientSocket( _socketPool );
	std::wstring localSessionID(sessionID);
	clientSocket->makeNonBlockingRequest( dispatch, localSessionID );
}

void spep::ipc::SessionCacheProxy::getUnauthenticatedSession(spep::UnauthenticatedSession &unauthenticatedSession, const std::wstring& requestID)
{
	std::string dispatch(::getUnauthenticatedSession);
	std::wstring requestIDLocal = requestID;
	
	ClientSocketLease clientSocket( _socketPool );
	unauthenticatedSession = clientSocket->makeRequest<UnauthenticatedSession, std::wstring>( dispatch, requestIDLocal );
}

void spep::ipc::SessionCacheProxy::insertUnauthenticatedSession(spep::UnauthenticatedSession &unauthenticatedSession)
{
	std::string dispatch(::insertUnauthenticatedSession);
	
	ClientSocketLease clientSocket( _socketPool );
	// Make a blocking request (even though we don't need a return value) so that we know the session has been inserted.
	clientSocket->makeRequest<NoData, UnauthenticatedSession>( dispatch, unauthenticatedSession );
}

void spep::ipc::SessionCacheProxy::terminateUnauthenticatedSession(const std::wstring& requestID)
{
	std::string dispatch(::terminateUnauthenticatedSession);
	
	ClientSocketLease clientSocket( _socketPool );
	std::wstring localRequestID(requestID);
	clientSocket->makeNonBlockingRequest( dispatch, localRequestID );
}

void spep::ipc::SessionCacheProxy::terminateExpiredSessions( int sessionCacheTimeout )
{
	std::string dispatch(::terminateExpiredSessions);
	
	ClientSocketLease clientSocket( _socketPool );
	clientSocket->makeNonBlockingRequest( dispatch, sessionCacheTimeout );
}
