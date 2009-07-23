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

#ifndef LOGGERPROXY_H_
#define LOGGERPROXY_H_

#include "spep/Util.h"
#include "spep/ipc/Socket.h"

#include "saml2/logging/api.h"

#include <string>

namespace spep { namespace ipc {

	class SPEPEXPORT LoggerProxy : public saml2::Logger
	{

		private:
		ClientSocketPool *_socketPool;

		public:
		LoggerProxy( ClientSocketPool *socketPool );

		/// @see saml2::Logger::log
		/**@{*/
		virtual void registerHandler( saml2::Handler* handler );
		virtual void log( saml2::LogLevel level, const std::string& name, const std::string& msg );
		/**@}*/
		virtual ~LoggerProxy();

	};

} }

#endif /*LOGGERPROXY_H_*/
