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

#include "spep/identifier/proxy/IdentifierCacheProxy.h"
#include "spep/identifier/proxy/IdentifierCacheDispatcher.h"

static const char *registerIdentifier = IDENTIFIERCACHE_registerIdentifier;
static const char *containsIdentifier = IDENTIFIERCACHE_containsIdentifier;
static const char *cleanCache = IDENTIFIERCACHE_cleanCache;

spep::ipc::IdentifierCacheProxy::IdentifierCacheProxy( spep::ipc::ClientSocketPool *socketPool )
:
_socketPool( socketPool )
{
}

void spep::ipc::IdentifierCacheProxy::registerIdentifier(std::string identifier)
{
	std::string dispatch( ::registerIdentifier );
	
	ClientSocketLease clientSocket( _socketPool );
	clientSocket->makeNonBlockingRequest( dispatch, identifier );
}

bool spep::ipc::IdentifierCacheProxy::containsIdentifier(std::string identifier)
{
	std::string dispatch( ::containsIdentifier );
	
	ClientSocketLease clientSocket( _socketPool );
	return clientSocket->makeRequest<bool>( dispatch, identifier );
}

int spep::ipc::IdentifierCacheProxy::cleanCache(long age)
{
	std::string dispatch( ::cleanCache );
	
	ClientSocketLease clientSocket( _socketPool );
	return clientSocket->makeRequest<int>( dispatch, age );
}

spep::ipc::IdentifierCacheProxy::~IdentifierCacheProxy()
{
}
