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
 * Creation Date: Jul 9, 2007
 * 
 * Purpose: 
 */

#include "reporting/ReportingLevels.h"

#define OUTPUT_LEVEL(stream,level,value) if( level == value ) { stream << #value; return stream; } 

std::ostream& operator<<( std::ostream &out, spep::Level level )
{
	using namespace spep;
	
	OUTPUT_LEVEL( out, level, DEBUG )
	OUTPUT_LEVEL( out, level, INFO )
	OUTPUT_LEVEL( out, level, AUTHZ )
	OUTPUT_LEVEL( out, level, AUTHN )
	OUTPUT_LEVEL( out, level, WARN )
	OUTPUT_LEVEL( out, level, ERROR )
	OUTPUT_LEVEL( out, level, FATAL )
	
	out << "UNKNOWN";
	return out;
}
