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
 * Creation Date: 25/09/2006
 * 
 * Purpose: 
 */

#ifndef REPORTINGPROCESSOR_H_
#define REPORTINGPROCESSOR_H_

#include <stdio.h>
#include <stdarg.h>

#include <iostream>
#include <string>
#include <vector>

#include "reporting/ReportingLevels.h"
#include "reporting/Handler.h"

namespace spep
{
	/* Need this to keep the compiler happy */
	class LocalReportingProcessor;

	class ReportingProcessor
	{
		
		public:
		
		~ReportingProcessor();
		
		/**
		 * Logs a standard message to all logging handlers
		 */
		void log(const std::string &name, const Level level, const std::string &message) const;
		
		/**
		 * Creates a local reporting processor that will pass the provided 'name' to the reporting
		 * processor for each message logged. This object should be deleted by the class that 
		 * requested it when it is no longer needed.
		 */
		LocalReportingProcessor localReportingProcessor(const std::string name);
		
		/**
		 * This reporting processor takes ownership of the handler objects, in the sense that the 
		 * memory should be managed inside the reporting processor after the handler is registered. 
		 * Any changes to the objects after registration will cause undefined behaviour.
		 */
		void registerHandler(Handler *handler);
		
		/**
		 * Returns a list of registered handlers for this reporting processor.
		 */
		const std::vector<Handler*> *registeredHandlers() const;
		
		/**
		 * Makes a timestamp string suitable for use in logging.
		 */
		static std::string timestamp();
		
		
		private:
		std::vector<Handler*> _handlers;
		
	};
	
}

#endif /* REPORTINGPROCESSOR_H_ */
