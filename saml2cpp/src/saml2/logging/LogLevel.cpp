/*
 * Copyright 2007-2009, Shaun Mangelsdorf
 * Includes modifications from the original, by the same author.
 *
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
 * Creation Date: 16/08/2007
 *
 * Purpose: Methods for working with logging levels.
 */

#include "saml2/logging/LogLevel.h"

std::string saml2::logLevelAsString( saml2::LogLevel level )
{
	switch( level )
	{
		case ERROR:
		return "ERROR";

		case WARN:
		return "WARN";

		case NOTICE:
		return "NOTICE";

		case INFO:
		return "INFO";

		case DEBUG:
		return "DEBUG";

		case TRACE:
		return "TRACE";

		case INSANE:
		return "INSANE";
	}

	return "UNKNOWN";
}

std::ostream& operator<<( std::ostream& stream, saml2::LogLevel level )
{
	stream << saml2::logLevelAsString( level );
	return stream;
}
