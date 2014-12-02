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

#include "saml2/logging/Logger.h"

#include <boost/date_time/posix_time/ptime.hpp>
#include <boost/date_time/posix_time/conversion.hpp>
#include <boost/date_time/posix_time/posix_time_types.hpp>
#include <boost/date_time/posix_time/posix_time_io.hpp>

saml2::Logger::Logger() 
{
}

saml2::Logger::Logger(const std::vector<saml2::Handler*>& handlers) : mHandlers(handlers) 
{
}

saml2::Logger::~Logger() 
{
}

void saml2::Logger::registerHandler(saml2::Handler* handler)
{
	mHandlers.push_back(handler);
}

void saml2::Logger::log(saml2::LogLevel level, const std::string& name, const std::string& msg)
{
    for (const auto handler: mHandlers)
    {
        handler->log(level, name, msg);
    }
}

std::string saml2::Logger::timestamp()
{
	boost::posix_time::ptime now( boost::posix_time::second_clock::local_time() );

	std::stringstream ss;
	std::string format("%Y-%m-%d %H:%M:%S");

	// stupid std::stringstream... wants to delete the facet itself instead of letting it be cleaned up by going out of scope.
	boost::posix_time::time_facet *output_facet = new boost::posix_time::time_facet(format.c_str());
	ss.imbue(std::locale( std::locale::classic(), output_facet));

	ss << now;
	return ss.str();
}
