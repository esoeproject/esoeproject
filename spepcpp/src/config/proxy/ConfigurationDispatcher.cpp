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
 * Creation Date: 19/06/2007
 * 
 * Purpose: 
 */

#include "config/proxy/ConfigurationDispatcher.h"

static const char *getSPEPConfigData = CONFIGURATION_getSPEPConfigData;

spep::ipc::ConfigurationDispatcher::ConfigurationDispatcher( spep::Configuration *configuration )
:
_configuration( configuration ),
_prefix( CONFIGURATION )
{
}

spep::ipc::ConfigurationDispatcher::~ConfigurationDispatcher()
{
}

bool spep::ipc::ConfigurationDispatcher::dispatch( spep::ipc::MessageHeader &header, spep::ipc::Engine &en )
{
	
	std::string dispatch = header.getDispatch();
	
	// Make sure the prefix matches the expected prefix for this dispatcher.
	if ( dispatch.compare( 0, strlen( CONFIGURATION ), _prefix ) != 0 )
		return false;
		
	if ( dispatch.compare( getSPEPConfigData ) == 0 )
	{
		
		SPEPConfigData data( _configuration->getSPEPConfigData() );
		
		if( header.getType() == SPEPIPC_REQUEST )
		{
			en.sendResponseHeader();
			en.sendObject( data );
		}
		
		return true;
	}
	
	return false;
	
}
