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

#include "spep/startup/proxy/StartupProcessorDispatcher.h"

static const char *allowProcessing = STARTUPPROCESSOR_allowProcessing;

spep::ipc::StartupProcessorDispatcher::StartupProcessorDispatcher( spep::StartupProcessor *startupProcessor )
:
_startupProcessor( startupProcessor ),
_prefix( STARTUPPROCESSOR )
{
}

spep::ipc::StartupProcessorDispatcher::~StartupProcessorDispatcher()
{
}

bool spep::ipc::StartupProcessorDispatcher::dispatch( spep::ipc::MessageHeader &header, spep::ipc::Engine &en )
{
	
	std::string dispatch = header.getDispatch();
	
	// Make sure the prefix matches the expected prefix for this dispatcher.
	if ( dispatch.compare( 0, strlen( STARTUPPROCESSOR ), _prefix ) != 0 )
		return false;
		
	if ( dispatch.compare( allowProcessing ) == 0 )
	{
		
		unsigned int result;
		
		spep::StartupResult startupResult = this->_startupProcessor->allowProcessing();
		
		// Because we anticipate the beginSPEPStart() call, we can take it out of the equation by doing it here.
		if( startupResult == spep::STARTUP_NONE )
		{
			this->_startupProcessor->beginSPEPStart();
			startupResult = spep::STARTUP_WAIT;
		}
		
		result = static_cast<unsigned int>( startupResult );
		
		if( header.getType() == SPEPIPC_REQUEST )
		{
			en.sendResponseHeader();
			en.sendObject( result );
		}
		
		return true;
	}
	
	return false;
	
}
