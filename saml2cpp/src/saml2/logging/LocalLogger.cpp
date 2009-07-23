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
 * Creation Date: 21/06/2009
 *
 * Purpose:
 */

#include "saml2/logging/LocalLogger.h"

saml2::LocalLogger::LocalLogger( saml2::Logger *logger, const std::string& name )
:
_logger( logger ),
_name( name )
{
}

void saml2::LocalLogger::log( saml2::LogLevel level, const std::string& msg )
{
	this->_logger->log( level, this->_name, msg );
}

saml2::LogStreamMimic saml2::LocalLogger::insane()
{
	return LogStreamMimic( this, INSANE );
}

saml2::LogStreamMimic saml2::LocalLogger::trace()
{
	return LogStreamMimic( this, TRACE );
}

saml2::LogStreamMimic saml2::LocalLogger::debug()
{
	return LogStreamMimic( this, DEBUG );
}

saml2::LogStreamMimic saml2::LocalLogger::info()
{
	return LogStreamMimic( this, INFO );
}

saml2::LogStreamMimic saml2::LocalLogger::notice()
{
	return LogStreamMimic( this, NOTICE );
}

saml2::LogStreamMimic saml2::LocalLogger::warn()
{
	return LogStreamMimic( this, WARN );
}

saml2::LogStreamMimic saml2::LocalLogger::error()
{
	return LogStreamMimic( this, ERROR );
}
