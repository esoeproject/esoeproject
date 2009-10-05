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
 * Creation Date: 19/09/2007
 *
 * Purpose: Defines a LocalLogger, a Logger that is aware of its own name and
 *   therefore doesn't require it to be passed for each log call.
 */

#ifndef LOCALLOGGER_H_
#define LOCALLOGGER_H_

#include <string>

#include "saml2/SAML2Defs.h"

#include "saml2/logging/Logger.h"
#include "saml2/logging/LogLevel.h"
#include "saml2/logging/LogStreamMimic.h"

namespace saml2
{

	/**
	 * @class LocalLogger
	 * @brief Defines a local logger.
	 *
	 * Defines a local logger, which knows of its "name". The name of the
	 * logger is then implicitly passed to the Logger for each log call.
	 *
	 * Saves needing to type the name every time you log something.
	 */
	class SAML2EXPORT LocalLogger
	{

		private:
		Logger *_logger;
		std::string _name;

		LocalLogger( const LocalLogger& other );
		LocalLogger& operator=( const LocalLogger& other );

		public:

		/**
		 * Constructs a LocalLogger pointing at the given Logger, with the
		 * given name.
		 */
		LocalLogger( Logger *logger, const std::string& name );

		/**
		 * Logs a message to the Logger that was passed to this LocalLogger
		 * at instantiation.
		 */
		void log( LogLevel level, const std::string& msg );

		/**
		 * Log stream mimic instantiators for the various logging levels
		 */
		/**@{*/

		LogStreamMimic insane();
		LogStreamMimic trace();
		LogStreamMimic debug();
		LogStreamMimic info();
		LogStreamMimic notice();
		LogStreamMimic warn();
		LogStreamMimic error();

		/**@}*/
	};

}

#endif /*LOCALLOGGER_H_*/
