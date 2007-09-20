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
 * Creation Date: Aug 7, 2007
 * 
 * Purpose: 
 */

#include "exceptions/MetadataException.h"

spep::MetadataException::MetadataException( std::string message )
:
_message( message )
{
}

spep::MetadataException::~MetadataException() throw()
{
}

const std::string& spep::MetadataException::getMessage()
{
	return this->_message;
}

const char *spep::MetadataException::what() const throw()
{
	return this->_message.c_str();
}
