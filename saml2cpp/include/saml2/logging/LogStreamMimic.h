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
 * Purpose: Defines the logging levels supported by this logging API.
 */

#ifndef LOGSTREAMMIMIC_H_
#define LOGSTREAMMIMIC_H_

#include <string>
#include <sstream>

#include "saml2/logging/LogLevel.h"

namespace saml2
{

	// Declare this here, because we can't include the file until
	// LogStreamMimic is already defined, otherwise something will
	// implode thanks to cyclic includes.
	class LocalLogger;

	/**
	 * @class LogStreamMimic
	 * @brief Provides stream-like functions for a local logger.
	 *
	 * This class defines a "stream mimic" for the logger. The concept
	 * here is that once the object is created, the calling code can use
	 * it as they would a stream and ignore the implementation details.
	 *
	 * The idea is that when the object goes out of scope it will be
	 * destroyed naturally, and in the destructor we can pass the result
	 * string to the logger to be logged.
	 */
	class LogStreamMimic
	{
		// For access to the copy constructor. This is the only class trusted to use it properly ;)
		friend class LocalLogger;

		private:

		LocalLogger *_logger;
		LogLevel _level;
		std::stringstream _stringStream;

		// Only used by LocalLogger to return the object.
		LogStreamMimic( const LogStreamMimic& other );

		// Not required. Leave it undefined.
		LogStreamMimic& operator=( const LogStreamMimic& other );

		public:

		/**
		 * Creates a LogStreamMimic to point at the specified logger.
		 */
		LogStreamMimic( LocalLogger* logger, LogLevel level );

		/**
		 * This is where the fun happens. The log call is made in the
		 * destructor so that we dump the message once we've finished
		 * building it in the stringstream.
		 */
		~LogStreamMimic();

		/**
		 * Defines the "stream-like" operator for this object. Essentially
		 * it just duplicates whatever functionality the basic stringstream
		 * type has for the given type.
		 */
		template <typename T>
		LogStreamMimic& operator<<( const T& obj )
		{
			this->_stringStream << obj;

			return *this;
		}

	};

}

#endif /*LOGSTREAMMIMIC_H_*/
