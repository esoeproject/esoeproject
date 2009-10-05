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
 * Purpose: Defines the logging levels supported by this logging API.
 */

#ifndef LOGLEVEL_H_
#define LOGLEVEL_H_

#include <string>

#include "saml2/SAML2Defs.h"

namespace saml2
{

	/**
	 * Defines the logging levels allowed.
	 *
	 * The comments for each of these levels are suggested uses only.
	 */
	enum LogLevel
	{
// Bad, bad, bad... but it won't break anything hopefully
#if defined(WIN32) && defined(ERROR)
#undef ERROR
#endif
		/// Error messages - things that may halt or severely impede operation
		ERROR = 800,
		/// Warning messages - things that may require attention
		WARN = 700,
		/// Notice messages - messages that may be of some importance
		NOTICE = 600,
		/// Information messages - anything of interest to an admin crawling logs
		INFO = 500,
		/// Debug messages - higher level information on program flow
		DEBUG = 400,
		/// Trace messages - low level information for a developer to trace program execution
		TRACE = 300,
		/// Insane messages - for dumping large quantities of data to be used in heavy debugging
		INSANE = 200

	};

	/**
	 * Gets the logging level value represented as a string with the given character type.
	 */
	std::string logLevelAsString( LogLevel level );

}

/**
 * operator<< overload for writing to an output stream.
 */
SAML2EXPORT std::ostream& operator<<( std::ostream& stream, saml2::LogLevel level );

#endif /*LOGLEVEL_H_*/
