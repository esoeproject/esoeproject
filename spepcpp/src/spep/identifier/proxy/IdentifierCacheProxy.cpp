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

static const std::string REGISTER_IDENTIFIER = IDENTIFIERCACHE_registerIdentifier;
static const std::string CONTAINS_IDENTIFIER = IDENTIFIERCACHE_containsIdentifier;
static const std::string CLEAN_CACHE = IDENTIFIERCACHE_cleanCache;

spep::ipc::IdentifierCacheProxy::IdentifierCacheProxy(spep::ipc::ClientSocketPool *socketPool) :
mSocketPool(socketPool)
{
}

spep::ipc::IdentifierCacheProxy::~IdentifierCacheProxy()
{
}

void spep::ipc::IdentifierCacheProxy::registerIdentifier(const std::string& identifier)
{	
	ClientSocketLease clientSocket(mSocketPool);
    clientSocket->makeNonBlockingRequest(REGISTER_IDENTIFIER, identifier);
}

bool spep::ipc::IdentifierCacheProxy::containsIdentifier(const std::string& identifier)
{
	ClientSocketLease clientSocket(mSocketPool);
    return clientSocket->makeRequest<bool>(CONTAINS_IDENTIFIER, identifier);
}

int spep::ipc::IdentifierCacheProxy::cleanCache(long age)
{
	ClientSocketLease clientSocket(mSocketPool);
    return clientSocket->makeRequest<int>(CLEAN_CACHE, age);
}

