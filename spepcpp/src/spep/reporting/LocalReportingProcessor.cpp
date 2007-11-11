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
 * Purpose: 
 */
 
#include "spep/reporting/ReportingLevels.h"
#include "spep/reporting/LocalReportingProcessor.h"

spep::LocalReportingProcessor::LocalReportingProcessor( std::string name, spep::ReportingProcessor *reportingProcessor )
:
_name(name),
_reportingProcessor(reportingProcessor)
{
}

spep::LocalReportingProcessor::LocalReportingProcessor( const spep::LocalReportingProcessor &other )
:
_name( other._name ),
_reportingProcessor( other._reportingProcessor )
{
}

spep::LocalReportingProcessor& spep::LocalReportingProcessor::operator=( const spep::LocalReportingProcessor& other )
{
	this->_name = other._name;
	this->_reportingProcessor = other._reportingProcessor;
	
	return *this;
}

void spep::LocalReportingProcessor::log( Level level, const std::string &message )
{
	this->_reportingProcessor->log(this->_name, level, message);
}

