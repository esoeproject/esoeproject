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

 
#include "sessions/proxy/SessionCacheDispatcher.h"

#include "ipc/Exceptions.h"
#include "ipc/MessageHeader.h"

namespace spep{ namespace ipc{
static const char *insertPrincipalSession = SESSIONCACHE_insertPrincipalSession;
static const char *getPrincipalSession = SESSIONCACHE_getPrincipalSession;
static const char *getPrincipalSessionByEsoeSessionID = SESSIONCACHE_getPrincipalSessionByEsoeSessionID;
static const char *terminatePrincipalSession = SESSIONCACHE_terminatePrincipalSession;
static const char *insertUnauthenticatedSession = SESSIONCACHE_insertUnauthenticatedSession;
static const char *getUnauthenticatedSession = SESSIONCACHE_getUnauthenticatedSession;
static const char *terminateUnauthenticatedSession = SESSIONCACHE_terminateUnauthenticatedSession;
static const char *terminateExpiredSessions = SESSIONCACHE_terminateExpiredSessions;
} }

 
spep::ipc::SessionCacheDispatcher::SessionCacheDispatcher(SessionCache *sessionCache)
: _sessionCache(sessionCache),
_prefix(SESSIONCACHEPREFIX)
{}

spep::ipc::SessionCacheDispatcher::~SessionCacheDispatcher()
{}
		
bool spep::ipc::SessionCacheDispatcher::dispatch( spep::ipc::MessageHeader &header, spep::ipc::Engine &en )
{
	std::string dispatch = header.getDispatch();
	if ( dispatch.compare( 0, strlen(SESSIONCACHEPREFIX), _prefix ) != 0 )
		return false;
	
	/* Insert client session call dispatcher */
	if ( dispatch.compare( insertPrincipalSession ) == 0 )
	{
		SessionCache_InsertClientSessionCommand command;
		en.getObject( command );
		
		// Try to perform the insert
		_sessionCache->insertPrincipalSession( command.sessionID, command.principalSession );
		
		if ( header.getType() == SPEPIPC_REQUEST )
		{
			throw InvocationTargetException( "No return type from this method" );
		}
		return true;
	}
	
	/* Get client session call dispatcher */
	if ( dispatch.compare( getPrincipalSession ) == 0 )
	{
		std::string sessionID;
		en.getObject( sessionID );
		
		PrincipalSession principalSession;
		
		// Try to find the session
		_sessionCache->getPrincipalSession( principalSession, sessionID );
		
		if ( header.getType() == SPEPIPC_REQUEST )
		{
			// If the client is expecting a response, send one.
			en.sendResponseHeader();
			en.sendObject( principalSession );
		}
			
		return true;
	}
	
	/* Get client session call dispatcher */
	if ( dispatch.compare( getPrincipalSessionByEsoeSessionID ) == 0 )
	{
		//virtual const PrincipalSession* getPrincipalSessionByEsoeSessionID(const UnicodeString esoeSessionID) const;

		std::wstring esoeSessionID;
		en.getObject( esoeSessionID );
		
		PrincipalSession principalSession;
		
		// Try to find the session
		_sessionCache->getPrincipalSessionByEsoeSessionID( principalSession, esoeSessionID );
		
		if ( header.getType() == SPEPIPC_REQUEST )
		{
			// If the client is expecting a response, send one.
			en.sendResponseHeader();
			en.sendObject( principalSession );
		}
			
		return true;
	}
	
	/* Terminate client session call dispatcher */
	if ( dispatch.compare( terminatePrincipalSession ) == 0 )
	{
		std::wstring sessionID;
		en.getObject( sessionID );
		
		// Try to terminate the session
		_sessionCache->terminatePrincipalSession( sessionID );
		
		if ( header.getType() == SPEPIPC_REQUEST )
		{
			throw InvocationTargetException( "No return type from this method" );
		}
		
		return true;
	}
	
	/* Insert unauthenticated session call dispatcher */
	if ( dispatch.compare( insertUnauthenticatedSession ) == 0 )
	{
		UnauthenticatedSession unauthenticatedSession;
		en.getObject( unauthenticatedSession );
		
		_sessionCache->insertUnauthenticatedSession( unauthenticatedSession );
		
		if ( header.getType() == SPEPIPC_REQUEST )
		{
			throw InvocationTargetException( "No return type from this method" );
		}
		
		return true;
	}
	
	/* Get unauthenticated session call dispatcher */
	if ( dispatch.compare( getUnauthenticatedSession ) == 0 )
	{
		std::wstring requestID;
		en.getObject( requestID );
		
		UnauthenticatedSession unauthenticatedSession;
		
		_sessionCache->getUnauthenticatedSession( unauthenticatedSession, requestID );
		
		if ( header.getType() == SPEPIPC_REQUEST )
		{
			en.sendResponseHeader();
			en.sendObject( unauthenticatedSession );
		}

		return true;
	}
	
	/* Terminate unauthenticated session call dispatcher */
	if ( dispatch.compare( terminateUnauthenticatedSession ) == 0 )
	{
		std::wstring requestID;
		en.getObject( requestID );
		
		_sessionCache->terminateUnauthenticatedSession( requestID );
		
		if ( header.getType() == SPEPIPC_REQUEST )
		{
			throw InvocationTargetException( "No return type from this method" );
		}

		return true;
	}
	
	/* Terminate expired sessions call dispatcher */
	if ( dispatch.compare( terminateExpiredSessions ) == 0 )
	{
		int sessionCacheTimeout;
		en.getObject( sessionCacheTimeout );
		
		_sessionCache->terminateExpiredSessions( sessionCacheTimeout );
		
		if ( header.getType() == SPEPIPC_REQUEST )
		{
			throw InvocationTargetException( "No return type from this method" );
		}
		
		return true;
	}
	
	return false;
}
