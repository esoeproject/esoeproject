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
 * Purpose: Defines a Logger, the target of log messages.
 */

#ifndef LOGGER_H_
#define LOGGER_H_

#include "saml2/SAML2Defs.h"

#include "saml2/logging/Handler.h"
#include "saml2/logging/LogLevel.h"

#include <string>
#include <vector>

namespace saml2
{

	/**
	 * @class Logger
	 * @brief Passes logging calls to handlers that have been registered.
	 *
	 * This class defines a "Logger" - the central point of the logging system.
	 * All calls from local loggers go through here and are passed to the
	 * logging handlers accordingly.
	 *
	 * Calls to log() are thread safe, however the thread safety of the "log"
	 * method is dependent on the thread safety of the handlers that have been
	 * registered with this logger.
	 *
	 * No attempt is made to be thread safe when calling registerHandler()
	 * and log() at the same time.
	 */
	class SAML2EXPORT Logger
    {
		public:

		/**
		 * Initializes the logger with no logging handlers.
		 */
		Logger();

		/**
		 * Initializes the logger with a list of logging handlers.
		 * @param handlers The list of handlers to use
		 */
		Logger(const std::vector<Handler*>& handlers);

		virtual ~Logger();

		/**
		 * Adds a logging handler to this logger. If the same handler
		 * is added multiple times, it will be logged to multiple times.
		 */
		virtual void registerHandler(Handler* handler);

		/**
		 * Logs a message to each of the logging handlers.
		 * @param level The log level to log the message as.
		 * @param name The name of the local logger that logged this.
		 * @param msg The message to log.
		 */
		virtual void log(LogLevel level, const std::string& name, const std::string& msg);

		static std::string timestamp();

    private:

        Logger(const Logger& other);
        Logger& operator=(const Logger& other);

        std::vector<Handler*> mHandlers;
	};
}

#endif /*LOGGER_H_*/
