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
 * Creation Date: 22/06/2009
 *
 * Purpose:
 */
#include "saml2/logging/LogStreamMimic.h"
#include "saml2/logging/LocalLogger.h"

XERCES_CPP_NAMESPACE_USE

saml2::LogStreamMimic::LogStreamMimic( const LogStreamMimic& other )
:
_logger( other._logger ),
_level( other._level ),
_stringStream() // This will never have data in it when it's copied. Just let it be.
{
}

saml2::LogStreamMimic::LogStreamMimic( LocalLogger* logger, LogLevel level )
:
_logger( logger ),
_level( level ),
_stringStream()
{}

saml2::LogStreamMimic::~LogStreamMimic()
{
	// If we've written at least 1 byte, log it.
	if( this->_stringStream.tellp() > 0 )
	{
		this->_logger->log( this->_level, this->_stringStream.str() );
	}
}

template<>
saml2::LogStreamMimic& saml2::LogStreamMimic::operator<< <XMLCh const*>( XMLCh const* const& str )
{
	char* transcoded = XMLString::transcode(str);
	this->_stringStream << std::string(transcoded);
	XMLString::release(&transcoded);

	return *this;
}
