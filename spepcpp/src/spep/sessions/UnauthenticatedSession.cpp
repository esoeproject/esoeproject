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
 * Creation Date: 08/01/2007
 * 
 * Purpose: 
 */
 
#include "spep/sessions/UnauthenticatedSession.h"

#include "time.h"

spep::UnauthenticatedSession::UnauthenticatedSession()
:
_authnRequestSAMLID(),
_requestURL(),
_timestamp( boost::posix_time::second_clock::local_time() )
{}

spep::UnauthenticatedSession::UnauthenticatedSession(const spep::UnauthenticatedSession &rhs)
:
_authnRequestSAMLID(rhs._authnRequestSAMLID),
_requestURL(rhs._requestURL),
_timestamp(rhs._timestamp)
{}

std::wstring spep::UnauthenticatedSession::getAuthnRequestSAMLID()
{
	return this->_authnRequestSAMLID;
}

void spep::UnauthenticatedSession::setAuthnRequestSAMLID(std::wstring authnRequestSAMLID)
{
	this->_authnRequestSAMLID = authnRequestSAMLID;
}

std::string spep::UnauthenticatedSession::getRequestURL()
{
	return this->_requestURL;
}

void spep::UnauthenticatedSession::setRequestURL(std::string requestURL)
{
	this->_requestURL = requestURL;
}

long spep::UnauthenticatedSession::getIdleTime()
{
	boost::posix_time::ptime currentTime = boost::posix_time::second_clock::local_time();
	return boost::posix_time::time_period( this->_timestamp, currentTime ).length().total_seconds();
}

void spep::UnauthenticatedSession::updateTime()
{
	this->_timestamp = boost::posix_time::second_clock::local_time();
}

spep::UnauthenticatedSession &spep::UnauthenticatedSession::operator=( const spep::UnauthenticatedSession &rhs )
{
	this->_authnRequestSAMLID = rhs._authnRequestSAMLID;
	this->_requestURL = rhs._requestURL;
	this->_timestamp = rhs._timestamp;
	
	return *this;
}
