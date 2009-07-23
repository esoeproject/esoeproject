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
 * Creation Date: Jul 9, 2007
 *
 * Purpose:
 */

#include "StreamLogHandler.h"

#include "saml2/logging/api.h"

#include <iostream>

spep::daemon::StreamLogHandler::StreamLogHandler( std::ostream &out, saml2::LogLevel level )
:
_out(out),
_level( level )
{
}

void spep::daemon::StreamLogHandler::log( saml2::LogLevel level, const std::string& name, const std::string& msg )
{
	if( level >= this->_level )
	{
		_out << ::saml2::Logger::timestamp() << " [" << level << "] " << name << " - " << msg << std::endl << std::flush;
	}
}

saml2::LogLevel spep::daemon::StreamLogHandler::minimumLevel()
{
	return this->_level;
}

spep::daemon::StreamLogHandler::~StreamLogHandler()
{
}
