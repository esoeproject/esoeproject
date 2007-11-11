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
 * Creation Date: Sep 7, 2007
 * 
 * Purpose: 
 */

#include "spep/identifier/proxy/IdentifierCacheDispatcher.h"

static const char *registerIdentifier = IDENTIFIERCACHE_registerIdentifier;
static const char *containsIdentifier = IDENTIFIERCACHE_containsIdentifier;
static const char *cleanCache = IDENTIFIERCACHE_cleanCache;

spep::ipc::IdentifierCacheDispatcher::IdentifierCacheDispatcher( saml2::IdentifierCache *identifierCache )
:
_prefix( IDENTIFIERCACHE ),
_identifierCache( identifierCache )
{
}

bool spep::ipc::IdentifierCacheDispatcher::dispatch( spep::ipc::MessageHeader &header, spep::ipc::Engine &en )
{
	std::string dispatch = header.getDispatch();
	
	// Make sure the prefix matches the expected prefix for this dispatcher.
	if ( dispatch.compare( 0, strlen( IDENTIFIERCACHE ), _prefix ) != 0 )
		return false;
	
	if ( dispatch.compare( registerIdentifier ) == 0 )
	{
		std::string identifier;
		en.getObject( identifier );
		
		_identifierCache->registerIdentifier( identifier );
		
		if ( header.getType() == SPEPIPC_REQUEST )
		{
			throw InvocationTargetException( "No return type from this method" );
		}
		
		return true;
	}
	
	if ( dispatch.compare( containsIdentifier ) == 0 )
	{
		std::string identifier;
		en.getObject( identifier );
		
		bool result = _identifierCache->containsIdentifier( identifier );
		
		if ( header.getType() == SPEPIPC_REQUEST )
		{
			// Return the value
			en.sendResponseHeader();
			en.sendObject( result );
		}
		
		return true;
	}
	
	if ( dispatch.compare( cleanCache ) == 0 )
	{
		long age;
		en.getObject( age );
		
		int result = _identifierCache->cleanCache( age );
		
		
		if ( header.getType() == SPEPIPC_REQUEST )
		{
			// Return the value
			en.sendResponseHeader();
			en.sendObject( result );
		}
		
		return true;
	}
	
	return false;
}

spep::ipc::IdentifierCacheDispatcher::~IdentifierCacheDispatcher()
{
}
