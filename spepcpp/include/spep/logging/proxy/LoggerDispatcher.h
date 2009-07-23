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

#ifndef LOGGERDISPATCHER_H_
#define LOGGERDISPATCHER_H_

#include "saml2/logging/api.h"

#include "spep/Util.h"
#include "spep/ipc/Dispatcher.h"

#define LOGGER "saml2/logger/Logger/"
#define LOGGER_log LOGGER "log"

namespace spep { namespace ipc {

	class SPEPEXPORT Logger_LogCommand
	{
		public:
		Logger_LogCommand(){}
		Logger_LogCommand(saml2::LogLevel l, const std::string& n, const std::string& m)
		: level(l), name(n), msg(m) {}
		saml2::LogLevel level;
		std::string name;
		std::string msg;
		template <class Archive>
		void serialize( Archive &ar, const unsigned int version )
		{ ar & level & name & msg; }
	};

	class SPEPEXPORT LoggerDispatcher : public Dispatcher
	{

		private:
		std::string _prefix;
		saml2::Logger *_logger;

		public:
		LoggerDispatcher( saml2::Logger *logger );
		virtual bool dispatch( MessageHeader &header, Engine &en );
		virtual ~LoggerDispatcher();

	};

} }

#endif /*LOGGERDISPATCHER_H_*/
