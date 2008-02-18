/*
 * Copyright 2008, Queensland University of Technology
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
 * Creation Date: 25/01/2008
 * 
 * Purpose: 
 */

#include "spep/exceptions/InvalidSessionIdentifierException.h"

spep::InvalidSessionIdentifierException::InvalidSessionIdentifierException()
: 
_what( "The session identifier does not refer to a valid session" )
{}

spep::InvalidSessionIdentifierException::~InvalidSessionIdentifierException() throw()
{}

const char *spep::InvalidSessionIdentifierException::what() const throw() 
{ 
	return _what;
}