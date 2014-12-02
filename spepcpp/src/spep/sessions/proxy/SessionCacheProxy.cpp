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

static const std::string INSERT_PRINCIPAL_SESSION = SESSIONCACHE_insertPrincipalSession;
static const std::string GET_PRINCIPAL_SESSION = SESSIONCACHE_getPrincipalSession;
static const std::string GET_PRINCIPAL_SESSION_BY_ESOE_SESSION_ID = SESSIONCACHE_getPrincipalSessionByEsoeSessionID;
static const std::string TERMINATE_PRINCIPAL_SESSION = SESSIONCACHE_terminatePrincipalSession;
static const std::string INSERT_UNAUTHENTICATED_SESSION = SESSIONCACHE_insertUnauthenticatedSession;
static const std::string GET_UNAUTHENTICATED_SESSION = SESSIONCACHE_getUnauthenticatedSession;
static const std::string TERMINATE_UNAUTHENTICATED_SESSION = SESSIONCACHE_terminateUnauthenticatedSession;
static const std::string TERMINATE_EXPIRED_SESSIONS = SESSIONCACHE_terminateExpiredSessions;


spep::ipc::SessionCacheProxy::SessionCacheProxy(spep::ipc::ClientSocketPool *socketPool) :
    mSocketPool(socketPool)
{}

void spep::ipc::SessionCacheProxy::getPrincipalSession(spep::PrincipalSession &principalSession, const std::string& sessionID)
{
    ClientSocketLease clientSocket(mSocketPool);
    std::string localSessionID(sessionID);
    principalSession = clientSocket->makeRequest<PrincipalSession, std::string>(GET_PRINCIPAL_SESSION, localSessionID);
}

void spep::ipc::SessionCacheProxy::getPrincipalSessionByEsoeSessionID(spep::PrincipalSession &principalSession, const std::wstring& esoeSessionIndex)
{
    ClientSocketLease clientSocket(mSocketPool);
    std::wstring localEsoeSessionIndex(esoeSessionIndex);
    principalSession = clientSocket->makeRequest<PrincipalSession, std::wstring>(GET_PRINCIPAL_SESSION_BY_ESOE_SESSION_ID, localEsoeSessionIndex);
}

void spep::ipc::SessionCacheProxy::insertPrincipalSession(const std::string& sessionID, spep::PrincipalSession &principalSession)
{
    std::string localSessionID(sessionID);
    SessionCache_InsertClientSessionCommand command(localSessionID, principalSession);

    ClientSocketLease clientSocket(mSocketPool);
    // Make a blocking request (even though we don't need a return value) so that we know the session has been inserted.
    clientSocket->makeRequest<NoData, SessionCache_InsertClientSessionCommand>(INSERT_PRINCIPAL_SESSION, command);
}

void spep::ipc::SessionCacheProxy::terminatePrincipalSession(const std::wstring& sessionID)
{
    ClientSocketLease clientSocket(mSocketPool);
    std::wstring localSessionID(sessionID);
    clientSocket->makeNonBlockingRequest(TERMINATE_PRINCIPAL_SESSION, localSessionID);
}

void spep::ipc::SessionCacheProxy::getUnauthenticatedSession(spep::UnauthenticatedSession &unauthenticatedSession, const std::wstring& requestID)
{
    std::wstring requestIDLocal = requestID;

    ClientSocketLease clientSocket(mSocketPool);
    unauthenticatedSession = clientSocket->makeRequest<UnauthenticatedSession, std::wstring>(GET_UNAUTHENTICATED_SESSION, requestIDLocal);
}

void spep::ipc::SessionCacheProxy::insertUnauthenticatedSession(spep::UnauthenticatedSession &unauthenticatedSession)
{
    ClientSocketLease clientSocket(mSocketPool);
    // Make a blocking request (even though we don't need a return value) so that we know the session has been inserted.
    clientSocket->makeRequest<NoData, UnauthenticatedSession>(INSERT_UNAUTHENTICATED_SESSION, unauthenticatedSession);
}

void spep::ipc::SessionCacheProxy::terminateUnauthenticatedSession(const std::wstring& requestID)
{
    ClientSocketLease clientSocket(mSocketPool);
    std::wstring localRequestID(requestID);
    clientSocket->makeNonBlockingRequest(TERMINATE_UNAUTHENTICATED_SESSION, localRequestID);
}

void spep::ipc::SessionCacheProxy::terminateExpiredSessions(int sessionCacheTimeout)
{
    ClientSocketLease clientSocket(mSocketPool);
    clientSocket->makeNonBlockingRequest(TERMINATE_EXPIRED_SESSIONS, sessionCacheTimeout);
}
