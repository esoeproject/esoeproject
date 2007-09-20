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

#include "reporting/ReportingProcessor.h"
#include "reporting/LocalReportingProcessor.h"
#include "reporting/Handler.h"

#include <boost/date_time/posix_time/ptime.hpp>
#include <boost/date_time/posix_time/conversion.hpp>
#include <boost/date_time/posix_time/posix_time_types.hpp>
#include <boost/date_time/posix_time/posix_time_io.hpp>

spep::ReportingProcessor::~ReportingProcessor()
{
}
 
void spep::ReportingProcessor::log(const std::string &name, const Level level, const std::string &message) const
{
	std::vector<spep::Handler*>::const_iterator iter = this->_handlers.begin();
	std::vector<spep::Handler*>::const_iterator iter_end = this->_handlers.end();
	
	for (/* init done */; iter != iter_end; ++iter)
		(*iter)->log(name, level, message);
}
		
spep::LocalReportingProcessor spep::ReportingProcessor::localReportingProcessor(const std::string name)
{
	return spep::LocalReportingProcessor( name, this );
}

void spep::ReportingProcessor::registerHandler(Handler *handler)
{
	this->_handlers.push_back(handler);
}

const std::vector<spep::Handler*> *spep::ReportingProcessor::registeredHandlers() const
{
	return &(this->_handlers);
}

std::string spep::ReportingProcessor::timestamp()
{
	boost::posix_time::ptime now( boost::posix_time::second_clock::local_time() );
	
	std::stringstream ss;
	std::string format("%Y-%m-%d %H:%M:%S");
	
	// stupid std::stringstream... wants to delete the facet itself instead of letting it be cleaned up by going out of scope.
	boost::posix_time::time_facet *output_facet = new boost::posix_time::time_facet( format.c_str() );
	ss.imbue( std::locale( std::locale::classic(), output_facet ) );

	ss << now;
	return ss.str();
}
