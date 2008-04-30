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
 * Creation Date: 20/06/2007
 * 
 * Purpose: 
 */

#include "spep/config/proxy/ConfigurationProxy.h"
#include "spep/config/proxy/ConfigurationDispatcher.h"

static const char *getSPEPConfigData = CONFIGURATION_getSPEPConfigData;

spep::ipc::ConfigurationProxy::ConfigurationProxy( spep::ipc::ClientSocketPool *socketPool )
:
_socketPool( socketPool )
{
}

spep::SPEPConfigData spep::ipc::ConfigurationProxy::getSPEPConfigData()
{
	std::string dispatch( ::getSPEPConfigData );
	spep::ipc::NoData noData;
	
	ClientSocketLease clientSocket( _socketPool );
	return clientSocket->makeRequest< spep::SPEPConfigData >( dispatch, noData );
}

spep::ipc::ConfigurationProxy::~ConfigurationProxy()
{
}
