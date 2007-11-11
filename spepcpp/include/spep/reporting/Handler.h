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
 * Creation Date: 12/01/2007
 * 
 * Purpose: Provides a base class for reporting processor handler classes to inherit.
 */

#ifndef HANDLER_H_
#define HANDLER_H_

#include <string>

#include "spep/Util.h"
#include "spep/reporting/ReportingLevels.h"

namespace spep
{
	
	/**
	 * Base class for logging handler implementation classes.
	 */
	class SPEPEXPORT Handler
	{
		public:
		/**
		 * Logs a message to the handler.
		 * @param name The class name logging the message.
		 * @param level The level of the log message
		 * @param message The message to log
		 */
		virtual void log(const std::string &name, const Level level, const std::string &message) = 0;
		virtual ~Handler(){}
	};
	
}

#endif /*HANDLER_H_*/
