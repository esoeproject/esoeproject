/* Copyright 2009, Queensland University of Technology
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
 * Creation Date: 23/06/2009
 *
 * Purpose:
 */

#include "spep/logging/proxy/LoggerDispatcher.h"

static const char *_logger_ipc_log = LOGGER_log;

spep::ipc::LoggerDispatcher::LoggerDispatcher( saml2::Logger *logger )
:
_prefix(LOGGER),
_logger(logger)
{
}

bool spep::ipc::LoggerDispatcher::dispatch( spep::ipc::MessageHeader &header, spep::ipc::Engine &en )
{
	std::string dispatch = header.getDispatch();

	// Make sure the prefix matches the expected prefix for this dispatcher.
	if ( dispatch.compare( 0, strlen( LOGGER ), _prefix ) != 0 )
		return false;

	if ( dispatch.compare( _logger_ipc_log ) == 0 )
	{
		Logger_LogCommand command;
		en.getObject(command);

		_logger->log(command.level, command.name, command.msg);

		if ( header.getType() == SPEPIPC_REQUEST )
		{
			throw InvocationTargetException( "No return type from this method" );
		}

		return true;
	}

	return false;
}

spep::ipc::LoggerDispatcher::~LoggerDispatcher()
{
}
