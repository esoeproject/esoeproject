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
 * Purpose: Local reporting processor for use in classes that require continual log
 * 		output.
 */


#ifndef LOCALREPORTINGPROCESSOR_H_
#define LOCALREPORTINGPROCESSOR_H_

#include <string>

#include "reporting/ReportingLevels.h"
#include "reporting/ReportingProcessor.h"

namespace spep
{
	
	/**
	 * Represents the logging processor for a particular class. Keeps class name so that it 
	 * doesn't need to be passed to each call.
	 */
	class LocalReportingProcessor
	{
		
		private:
		std::string _name;
		ReportingProcessor *_reportingProcessor;
		
		public:
		LocalReportingProcessor( std::string name, ReportingProcessor *reportingProcessor );
		LocalReportingProcessor( const LocalReportingProcessor& other );
		LocalReportingProcessor& operator=( const LocalReportingProcessor& other );
		
		void log( Level level, const std::string &message );
		
	};
	
}

#endif /*LOCALREPORTINGPROCESSOR_H_*/
