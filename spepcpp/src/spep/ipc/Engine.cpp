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
 * Creation Date: 07/03/2007
 * 
 * Purpose: Implements the non-templated methods of spep::ipc::Engine.
 */

#include "spep/ipc/Engine.h"

	
spep::ipc::MessageHeader spep::ipc::Engine::getRequestHeader()
{
	MessageHeader requestHeader;
	getObject(requestHeader);
	
	return requestHeader;
}

void spep::ipc::Engine::sendErrorResponseHeader()
{
	MessageHeader responseHeader( SPEPIPC_RESPONSE_ERROR, std::string() );
	
	_archive.out() << responseHeader;
}

void spep::ipc::Engine::sendResponseHeader()
{
	MessageHeader responseHeader( SPEPIPC_RESPONSE, std::string() );
	
	_archive.out() << responseHeader;
}
