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
 * Purpose: Defines a logging Handler, an object that receives log messages
 *   and uses them in some way.
 */

#ifndef HANDLER_H_
#define HANDLER_H_

#include <string>

#include "saml2/SAML2Defs.h"

#include "saml2/logging/LogLevel.h"

namespace saml2
{

	/**
	 * @class Handler
	 * @brief Base class for logging handlers.
	 *
	 * Provides a base class for logging handlers. The Handler::log method is
	 * overridden in derived classes to implement logging output.
	 */
	class SAML2EXPORT Handler
	{

		public:

		/**
		 * Virtual destructor. Override to maintain correct behaviour.
		 */
		virtual ~Handler() {}

		/**
		 * Returns the minimum logging level that will be handled by this Handler.
		 */
		virtual LogLevel minimumLevel() = 0;

		/**
		 * Virtual log method. Override to provide logging implementation.
		 * Thread safety must be guaranteed in the derived class if logging
		 * is to be thread safe.
		 * @param level The logging level of the message being logged.
		 * @param name The local name of the logger that logged this message.
		 * @param msg The message to be logged.
		 */
		virtual void log( LogLevel level, const std::string& name, const std::string& msg ) = 0;

	};

}

#endif /*HANDLER_H_*/
