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
 * Creation Date: 08/01/2007
 * 
 * Purpose: Implements the session cache object, of which only one exists per SPEP.
 */

#include "spep/sessions/impl/SessionCacheImpl.h"

#include "saml2/exceptions/InvalidParameterException.h"
#include "spep/exceptions/InvalidStateException.h"
#include "spep/exceptions/InvalidSessionIdentifierException.h"

#include <iterator>

#include "spep/UnicodeStringConversion.h"

#include <unicode/calendar.h>

#include <boost/date_time/posix_time/posix_time.hpp>


spep::SessionCacheImpl::SessionCacheImpl( spep::ReportingProcessor *reportingProcessor )
:
_localReportingProcessor( reportingProcessor->localReportingProcessor( "spep::SessionCacheImpl" ) ),
_sessionIDs(),
_esoeSessions(),
_unauthenticatedSessions()
{}

void spep::SessionCacheImpl::getPrincipalSession(spep::PrincipalSession &principalSession, std::string sessionID)
{
	ScopedLock lock(_principalSessionsMutex);
	
	// Find operation on map returns an iterator which either points at end() or 
	// at a kv pair which is the requested object.
	SessionIDMap::const_iterator iter( _sessionIDs.find( sessionID ) );
	if ( iter != _sessionIDs.end() )
	{
		// From the SessionIDMap we now have the esoe session identifier.
		// So we can look up the value and return it now.
		ESOESessionMap::const_iterator esoeIter( _esoeSessions.find( iter->second ) );
		if ( esoeIter == _esoeSessions.end() )
		{
			// Serious problem. Inconsistent map. No ESOE session ID for the given session.
			
			this->_localReportingProcessor.log( FATAL, "Session cache has become inconsistent. No ESOE session found for principal session, even though the session ID was found successfully." );
			throw InvalidStateException( "Session cache has become inconsistent" );
		}
		
		principalSession = esoeIter->second;
		
		// If the session hasn't expired, return it.
		if( principalSession.getSessionNotOnOrAfter() > boost::posix_time::second_clock::universal_time() )
		{
			return;
		}
		
		this->_localReportingProcessor.log( DEBUG, "Session expiry: " + sessionID + " was set to expire at " + boost::posix_time::to_simple_string(principalSession.getSessionNotOnOrAfter()) );
		
		// Expired. Terminate it.
		this->terminatePrincipalSession( principalSession.getESOESessionID() );
	}
	
	// Session wasn't found
	throw InvalidSessionIdentifierException();
}

void spep::SessionCacheImpl::getPrincipalSessionByEsoeSessionID(spep::PrincipalSession &principalSession, std::wstring esoeSessionID)
{
	ScopedLock lock(_principalSessionsMutex);
	
	ESOESessionMap::const_iterator iter( _esoeSessions.find( esoeSessionID ) );
	if ( iter != _esoeSessions.end() )
	{
		// Already have the ESOE session identifier. No need to retrieve it.
		std::pair<std::wstring, PrincipalSession> principalSessionPair = *iter;
		principalSession = principalSessionPair.second;
		
		// If the session hasn't expired, return it.
		if( principalSession.getSessionNotOnOrAfter() > boost::posix_time::second_clock::universal_time() )
		{
			return;
		}
		
		// Expired. Terminate it.
		// It would have at least 1 session ID in the list.. so .. terminate using that.
		this->terminatePrincipalSession( esoeSessionID );
	}
	
	// Session wasn't found
	throw InvalidSessionIdentifierException();
}

void spep::SessionCacheImpl::insertPrincipalSession( std::string sessionID, spep::PrincipalSession &principalSession )
{
	ScopedLock lock(_principalSessionsMutex);
	
	if (principalSession.getESOESessionID().length() == 0)
	{
		SAML2LIB_INVPARAM_EX( "Principal session had no ESOE session identifier" );
	}
	
	ESOESessionMap::iterator esoeIter( _esoeSessions.find( principalSession.getESOESessionID() ) );
	// Check if this is a new session..
	if ( esoeIter == _esoeSessions.end() )
	{	
		// Insert the principal session into the esoe session map.
		std::pair<ESOESessionMap::iterator, bool> result = 
			_esoeSessions.insert( std::make_pair( principalSession.getESOESessionID(), principalSession ) );
			
		// Ensure that the insert operation worked.
		if (! result.second)
		{
			SAML2LIB_INVPARAM_EX( "Failed to insert the principal session" );
		}
		
		// Make sure the session object contains the local session ID so the session gets cleaned up properly.
		bool found = false;
		std::vector<std::string> &principalSessionIDList = result.first->second.getSessionIDList();
		for( std::vector<std::string>::iterator sessionIDIter = principalSessionIDList.begin();
			sessionIDIter != principalSessionIDList.end(); ++sessionIDIter )
		{
			if( sessionIDIter->compare( sessionID ) == 0 )
			{
				found = true;
				break;
			}
		}
		
		if( !found )
		{
			principalSessionIDList.push_back( sessionID );
		}
	}
	// otherwise it's a new index for a session we already know about.
	else
	{
		PrincipalSession::ESOESessionIndexMapType::iterator esoeSessionIndexMapIterator;
		for( esoeSessionIndexMapIterator = principalSession.getESOESessionIndexMap().begin();
			esoeSessionIndexMapIterator != principalSession.getESOESessionIndexMap().end();
			++esoeSessionIndexMapIterator )
		{
			std::wstring esoeSessionIndex( esoeSessionIndexMapIterator->first );
			std::string localSessionID( esoeSessionIndexMapIterator->second );
			esoeIter->second.addESOESessionIndexAndLocalSessionID( esoeSessionIndex, localSessionID );
		}
	}
	
	// Insert into the map with SessionID as key
	std::pair<SessionIDMap::iterator, bool> secondResult =
		_sessionIDs.insert( std::make_pair( sessionID, principalSession.getESOESessionID() ) );
}

void spep::SessionCacheImpl::terminatePrincipalSession(const std::wstring esoeSessionID)
{
	ScopedLock lock(_principalSessionsMutex);
	
	// Loop up the principal session by ESOE session ID
	ESOESessionMap::iterator esoeIter = _esoeSessions.find(esoeSessionID);
	if ( esoeIter != _esoeSessions.end() )
	{
		// Go through the list of all local session identifiers for that principal session
		// and terminate them all.
		for( PrincipalSession::SessionIDListType::iterator sessionIDIter = esoeIter->second.getSessionIDList().begin();
			sessionIDIter != esoeIter->second.getSessionIDList().end();
			++sessionIDIter )
		{
			std::string sessionID( *sessionIDIter );
			SessionIDMap::iterator removeIter = _sessionIDs.find( sessionID );
			
			if( removeIter != _sessionIDs.end() )
			{
				_sessionIDs.erase( removeIter );
			}
		}
		
		// After we're done, remove the ESOE session ID from the session cache.
		_esoeSessions.erase( esoeIter );
	}
}

void spep::SessionCacheImpl::getUnauthenticatedSession(spep::UnauthenticatedSession &unauthenticatedSession, std::wstring requestID)
{
	ScopedLock lock(_unauthenticatedSessionsMutex);
	
	// Get the unauthenticated session by SAML request ID.
	UnauthenticatedSessionMap::const_iterator iter = _unauthenticatedSessions.find( requestID );
	if (iter != _unauthenticatedSessions.end())
	{
		unauthenticatedSession = iter->second;
		
		return;
	}
	
	throw InvalidSessionIdentifierException();
}

void spep::SessionCacheImpl::insertUnauthenticatedSession(spep::UnauthenticatedSession &unauthenticatedSession)
{
	ScopedLock lock(_unauthenticatedSessionsMutex);
	
	if (unauthenticatedSession.getAuthnRequestSAMLID().length() == 0)
	{
		SAML2LIB_INVPARAM_EX( "Unauthenticated session had no AuthnRequest SAML ID associated with it." );
	}
	
	std::pair< UnauthenticatedSessionMap::iterator, bool > result =
		_unauthenticatedSessions.insert( std::make_pair( unauthenticatedSession.getAuthnRequestSAMLID(), unauthenticatedSession ) );
	// If the insert failed the second element of the result pair will be false.
	if (!result.second)
	{
		SAML2LIB_INVPARAM_EX( "Failed to insert the unauthenticated session" );
	}
}

void spep::SessionCacheImpl::terminateUnauthenticatedSession(std::wstring requestID)
{
	ScopedLock lock(_unauthenticatedSessionsMutex);
	
	// Find by request ID and terminate
	UnauthenticatedSessionMap::iterator iter = _unauthenticatedSessions.find( requestID );
	if (iter != _unauthenticatedSessions.end())
	{
		_unauthenticatedSessions.erase(iter);
	}
}

void spep::SessionCacheImpl::terminateExpiredSessions( int sessionCacheTimeout )
{
	{
		ScopedLock lock(_principalSessionsMutex);
		
		for( ESOESessionMap::iterator iter = _esoeSessions.begin(); iter != _esoeSessions.end(); /* increment in loop body */ )
		{
			// If the session hasn't expired, skip it.
			if( iter->second.getSessionNotOnOrAfter() > boost::posix_time::second_clock::universal_time() )
			{
				++iter;
				continue;
			}
			// Otherwise, remove it.
			else
			{
				// This works because the iterator is incremented before the 
				// erase() call is made, but the old value is still passed in.
				_esoeSessions.erase( iter++ );
			}
		}
	}
	{
		ScopedLock lock(_unauthenticatedSessionsMutex);
		
		for( UnauthenticatedSessionMap::iterator iter = _unauthenticatedSessions.begin(); iter != _unauthenticatedSessions.end(); /* increment in loop body */ )
		{
			// If the session hasn't expired, skip it.
			if( iter->second.getIdleTime() < sessionCacheTimeout )
			{
				++iter;
				continue;
			}
			// Otherwise, remove it.
			else
			{
				// This works because the iterator is incremented before the 
				// erase() call is made, but the old value is still passed in.
				_unauthenticatedSessions.erase( iter++ );
			}
		}
	}
}
