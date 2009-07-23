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
 * Creation Date: 22/06/2009
 *
 * Purpose:
 */

#include "spep/logging/proxy/LoggerProxy.h"
#include "spep/logging/proxy/LoggerDispatcher.h"

static const char *_logger_ipc_log = LOGGER_log;

spep::ipc::LoggerProxy::LoggerProxy( spep::ipc::ClientSocketPool *socketPool )
:
_socketPool( socketPool )
{
}

void spep::ipc::LoggerProxy::registerHandler(saml2::Handler *handler)
{
	// It can't work.. it just can't.. so we don't try
	throw InvocationTargetException("registerHandler not implemented for spep::ipc::LoggerProxy");
}

void spep::ipc::LoggerProxy::log(saml2::LogLevel level, const std::string& name, const std::string& msg)
{
	std::string dispatch( ::_logger_ipc_log );

	Logger_LogCommand command( level, name, msg );

	ClientSocketLease clientSocket( _socketPool );
	clientSocket->makeNonBlockingRequest( dispatch, command );
}

spep::ipc::LoggerProxy::~LoggerProxy()
{
}
