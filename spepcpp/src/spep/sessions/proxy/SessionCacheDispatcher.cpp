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
 * Creation Date: 08/02/2007
 * 
 * Purpose: 
 */

 
#include "spep/sessions/proxy/SessionCacheDispatcher.h"

#include "spep/ipc/Exceptions.h"
#include "spep/ipc/MessageHeader.h"

namespace spep{ namespace ipc{
    static const std::string INSERT_PRINCIPAL_SESSION = SESSIONCACHE_insertPrincipalSession;
    static const std::string GET_PRINCIPAL_SESSION = SESSIONCACHE_getPrincipalSession;
    static const std::string GET_PRINCIPAL_SESSION_BY_ESOE_SESSION_ID = SESSIONCACHE_getPrincipalSessionByEsoeSessionID;
    static const std::string TERMINATE_PRINCIPAL_SESSION = SESSIONCACHE_terminatePrincipalSession;
    static const std::string INSERT_UNAUTHENTICATED_SESSION = SESSIONCACHE_insertUnauthenticatedSession;
    static const std::string GET_UNAUTHENTICATED_SESSION = SESSIONCACHE_getUnauthenticatedSession;
    static const std::string TERMINATE_UNAUTHENTICATED_SESSION = SESSIONCACHE_terminateUnauthenticatedSession;
    static const std::string TERMINATE_EXPIRED_SESSIONS = SESSIONCACHE_terminateExpiredSessions;
    
} }


spep::ipc::SessionCacheDispatcher::SessionCacheDispatcher(SessionCache *sessionCache) :
    mSessionCache(sessionCache),
    mPrefix(SESSIONCACHEPREFIX)
{}

spep::ipc::SessionCacheDispatcher::~SessionCacheDispatcher()
{}

bool spep::ipc::SessionCacheDispatcher::dispatch(spep::ipc::MessageHeader &header, spep::ipc::Engine &en)
{
    std::string dispatch = header.getDispatch();
    if (dispatch.compare(0, strlen(SESSIONCACHEPREFIX), mPrefix) != 0)
        return false;

    /* Insert client session call dispatcher */
    if (dispatch == INSERT_PRINCIPAL_SESSION)
    {
        SessionCache_InsertClientSessionCommand command;
        en.getObject(command);

        // Try to perform the insert
        mSessionCache->insertPrincipalSession(command.mSessionID, command.mPrincipalSession);

        if (header.getType() == SPEPIPC_REQUEST)
        {
            NoData noData;
            // If the client is expecting a response, send one.
            en.sendResponseHeader();
            en.sendObject(noData);
        }
        return true;
    }

    /* Get client session call dispatcher */
    if (dispatch == GET_PRINCIPAL_SESSION)
    {
        std::string sessionID;
        en.getObject(sessionID);

        PrincipalSession principalSession;

        // Try to find the session
        mSessionCache->getPrincipalSession(principalSession, sessionID);

        if (header.getType() == SPEPIPC_REQUEST)
        {
            // If the client is expecting a response, send one.
            en.sendResponseHeader();
            en.sendObject(principalSession);
        }

        return true;
    }

    /* Get client session call dispatcher */
    if (dispatch == GET_PRINCIPAL_SESSION_BY_ESOE_SESSION_ID)
    {
        //virtual const PrincipalSession* getPrincipalSessionByEsoeSessionID(const UnicodeString esoeSessionID) const;

        std::wstring esoeSessionID;
        en.getObject(esoeSessionID);

        PrincipalSession principalSession;

        // Try to find the session
        mSessionCache->getPrincipalSessionByEsoeSessionID(principalSession, esoeSessionID);

        if (header.getType() == SPEPIPC_REQUEST)
        {
            // If the client is expecting a response, send one.
            en.sendResponseHeader();
            en.sendObject(principalSession);
        }

        return true;
    }

    /* Terminate client session call dispatcher */
    if (dispatch == TERMINATE_PRINCIPAL_SESSION)
    {
        std::wstring sessionID;
        en.getObject(sessionID);

        // Try to terminate the session
        mSessionCache->terminatePrincipalSession(sessionID);

        if (header.getType() == SPEPIPC_REQUEST)
        {
            throw InvocationTargetException("No return type from this method");
        }

        return true;
    }

    /* Insert unauthenticated session call dispatcher */
    if (dispatch == INSERT_UNAUTHENTICATED_SESSION)
    {
        UnauthenticatedSession unauthenticatedSession;
        en.getObject(unauthenticatedSession);

        mSessionCache->insertUnauthenticatedSession(unauthenticatedSession);

        if (header.getType() == SPEPIPC_REQUEST)
        {
            NoData noData;
            // If the client is expecting a response, send one.
            en.sendResponseHeader();
            en.sendObject(noData);
        }

        return true;
    }

    /* Get unauthenticated session call dispatcher */
    if (dispatch == GET_UNAUTHENTICATED_SESSION)
    {
        std::wstring requestID;
        en.getObject(requestID);

        UnauthenticatedSession unauthenticatedSession;

        mSessionCache->getUnauthenticatedSession(unauthenticatedSession, requestID);

        if (header.getType() == SPEPIPC_REQUEST)
        {
            en.sendResponseHeader();
            en.sendObject(unauthenticatedSession);
        }

        return true;
    }

    /* Terminate unauthenticated session call dispatcher */
    if (dispatch == TERMINATE_UNAUTHENTICATED_SESSION)
    {
        std::wstring requestID;
        en.getObject(requestID);

        mSessionCache->terminateUnauthenticatedSession(requestID);

        if (header.getType() == SPEPIPC_REQUEST)
        {
            throw InvocationTargetException("No return type from this method");
        }

        return true;
    }

    /* Terminate expired sessions call dispatcher */
    if (dispatch == TERMINATE_EXPIRED_SESSIONS)
    {
        int sessionCacheTimeout;
        en.getObject(sessionCacheTimeout);

        mSessionCache->terminateExpiredSessions(sessionCacheTimeout);

        if (header.getType() == SPEPIPC_REQUEST)
        {
            throw InvocationTargetException("No return type from this method");
        }

        return true;
    }

    return false;
}
