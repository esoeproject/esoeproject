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
 * Purpose: Logging levels for use by the reporting processor. 
 */


#ifndef REPORTINGLEVELS_H_
#define REPORTINGLEVELS_H_

#include <iostream>

#include "spep/Util.h"

#ifdef ERROR
#undef ERROR
#endif /*ERROR*/

namespace spep
{
	
	/**
	 * The logging levels used by the SPEP.
	 */
	enum Level
	{
		DEBUG = 1,
		INFO = 2,
		AUTHZ = 3,
		AUTHN = 4,
		WARN = 5,
		ERROR = 6,
		FATAL = 7
	};
}

SPEPEXPORT std::ostream& operator<<( std::ostream &lhs, spep::Level rhs );

#endif /*REPORTINGLEVELS_H_*/
