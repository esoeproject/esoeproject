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
 * Purpose: Implements MessageHeader methods.
 */

#include "spep/ipc/MessageHeader.h"

spep::ipc::MessageHeader::MessageHeader(){}

spep::ipc::MessageHeader::MessageHeader( spep::ipc::MessageType messageType, std::string dispatch )
:
_messageType(messageType),
_dispatch(dispatch)
{}

spep::ipc::MessageType spep::ipc::MessageHeader::getType()
{
	return _messageType;
}

std::string &spep::ipc::MessageHeader::getDispatch()
{
	return _dispatch;
}

bool spep::ipc::MessageHeader::operator==(spep::ipc::MessageHeader &rhs)
{
	return rhs._messageType == _messageType && rhs._dispatch == _dispatch;
}

bool spep::ipc::MessageHeader::operator!=(spep::ipc::MessageHeader &rhs)
{
	return rhs._messageType != _messageType || rhs._dispatch != _dispatch;
}
