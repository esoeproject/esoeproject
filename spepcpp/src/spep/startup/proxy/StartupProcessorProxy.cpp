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
 * Creation Date: Aug 8, 2007
 * 
 * Purpose: 
 */

#include "spep/startup/proxy/StartupProcessorProxy.h"
#include "spep/startup/proxy/StartupProcessorDispatcher.h"

static const std::string ALLOW_PROCESSING = STARTUPPROCESSOR_allowProcessing;

spep::ipc::StartupProcessorProxy::StartupProcessorProxy(spep::ipc::ClientSocketPool *socketPool) :
    mSocketPool(socketPool)
{
}

spep::ipc::StartupProcessorProxy::~StartupProcessorProxy()
{
}

spep::StartupResult spep::ipc::StartupProcessorProxy::allowProcessing()
{
    spep::ipc::NoData noData;

    ClientSocketLease clientSocket(mSocketPool);
    return static_cast<spep::StartupResult>(clientSocket->makeRequest<unsigned int>(ALLOW_PROCESSING, noData));
}

void spep::ipc::StartupProcessorProxy::beginSPEPStart()
{
    // Actually, this call is anticipated by the dispatcher and made transparently. So we can ignore it.
}
