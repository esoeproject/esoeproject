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

spep::UnauthenticatedSession::UnauthenticatedSession() :
    mAuthnRequestSAMLID(),
    mRequestURL(),
    mTimestamp(boost::posix_time::second_clock::local_time())
{}

spep::UnauthenticatedSession::UnauthenticatedSession(const spep::UnauthenticatedSession &rhs) :
    mAuthnRequestSAMLID(rhs.mAuthnRequestSAMLID),
    mRequestURL(rhs.mRequestURL),
    mTimestamp(rhs.mTimestamp)
{}

spep::UnauthenticatedSession &spep::UnauthenticatedSession::operator=(const spep::UnauthenticatedSession &rhs)
{
    mAuthnRequestSAMLID = rhs.mAuthnRequestSAMLID;
    mRequestURL = rhs.mRequestURL;
    mTimestamp = rhs.mTimestamp;

    return *this;
}

std::wstring spep::UnauthenticatedSession::getAuthnRequestSAMLID() const
{
    return mAuthnRequestSAMLID;
}

void spep::UnauthenticatedSession::setAuthnRequestSAMLID(const std::wstring& authnRequestSAMLID)
{
    mAuthnRequestSAMLID = authnRequestSAMLID;
}

std::string spep::UnauthenticatedSession::getRequestURL() const
{
    return mRequestURL;
}

void spep::UnauthenticatedSession::setRequestURL(const std::string& requestURL)
{
    mRequestURL = requestURL;
}

void spep::UnauthenticatedSession::updateTime()
{
    mTimestamp = boost::posix_time::second_clock::local_time();
}

long spep::UnauthenticatedSession::getIdleTime() const
{
    boost::posix_time::ptime currentTime = boost::posix_time::second_clock::local_time();
    return boost::posix_time::time_period(mTimestamp, currentTime).length().total_seconds();
}
