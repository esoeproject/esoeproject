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
 * Creation Date: 05/03/2007
 * 
 * Purpose: 
 */

#include "spep/sessions/PrincipalSession.h"

spep::PrincipalSession::PrincipalSession()
{
}

spep::PrincipalSession::PrincipalSession(const spep::PrincipalSession& other) :
    mEsoeSessionID(other.mEsoeSessionID),
    mSessionNotOnOrAfter(other.mSessionNotOnOrAfter),
    mSessionIDList(other.mSessionIDList),
    mAttributeMap(other.mAttributeMap),
    mEsoeSessionIndexMap(other.mEsoeSessionIndexMap)
{
}

spep::PrincipalSession& spep::PrincipalSession::operator=(const spep::PrincipalSession& other)
{
    mEsoeSessionID = other.mEsoeSessionID;
    mSessionNotOnOrAfter = other.mSessionNotOnOrAfter;
    mSessionIDList = other.mSessionIDList;
    mAttributeMap = other.mAttributeMap;
    mEsoeSessionIndexMap = other.mEsoeSessionIndexMap;

    return *this;
}

void spep::PrincipalSession::setESOESessionID(const std::wstring& esoeSessionID)
{
    mEsoeSessionID = esoeSessionID;
}

std::wstring spep::PrincipalSession::getESOESessionID() const
{
    return mEsoeSessionID;
}

void spep::PrincipalSession::setSessionNotOnOrAfter(spep::PrincipalSession::TimeType sessionNotOnOrAfter)
{
    mSessionNotOnOrAfter = sessionNotOnOrAfter;
}

spep::PrincipalSession::TimeType spep::PrincipalSession::getSessionNotOnOrAfter() const
{
    return mSessionNotOnOrAfter;
}

void spep::PrincipalSession::addESOESessionIndexAndLocalSessionID(const std::wstring& esoeSessionIndex, const std::string& localSessionID)
{
    auto esoeSessionIndexIterator = mEsoeSessionIndexMap.lower_bound(esoeSessionIndex);

    if (esoeSessionIndexIterator != mEsoeSessionIndexMap.end() && esoeSessionIndex == esoeSessionIndexIterator->first)
    {
        // TODO Error condition? Key already exists
        throw std::exception();
    }

    // Insert the esoe session index and local session identifier into the map.
    mEsoeSessionIndexMap.insert(esoeSessionIndexIterator, std::make_pair(esoeSessionIndex, localSessionID));
}

spep::PrincipalSession::ESOESessionIndexMapType &spep::PrincipalSession::getESOESessionIndexMap()
{
    return mEsoeSessionIndexMap;
}

spep::PrincipalSession::SessionIDListType &spep::PrincipalSession::getSessionIDList()
{
    return mSessionIDList;
}

spep::PrincipalSession::AttributeMapType &spep::PrincipalSession::getAttributeMap()
{
    return mAttributeMap;
}
