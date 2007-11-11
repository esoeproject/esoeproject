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

spep::PrincipalSession::PrincipalSession( const spep::PrincipalSession& other )
:
_esoeSessionID( other._esoeSessionID ),
_sessionNotOnOrAfter( other._sessionNotOnOrAfter ),
_sessionIDList( other._sessionIDList ),
_attributeMap( other._attributeMap ),
_esoeSessionIndexMap( other._esoeSessionIndexMap )
{
}

spep::PrincipalSession& spep::PrincipalSession::operator=( const spep::PrincipalSession& other )
{
	this->_esoeSessionID = other._esoeSessionID;
	this->_sessionNotOnOrAfter = other._sessionNotOnOrAfter;
	this->_sessionIDList = other._sessionIDList;
	this->_attributeMap = other._attributeMap;
	this->_esoeSessionIndexMap = other._esoeSessionIndexMap;
	
	return *this;
}

void spep::PrincipalSession::setESOESessionID( std::wstring &esoeSessionID )
{
	this->_esoeSessionID = esoeSessionID;
}

std::wstring &spep::PrincipalSession::getESOESessionID()
{
	return this->_esoeSessionID;
}

void spep::PrincipalSession::setSessionNotOnOrAfter( spep::PrincipalSession::TimeType sessionNotOnOrAfter )
{
	this->_sessionNotOnOrAfter = sessionNotOnOrAfter;
}

spep::PrincipalSession::TimeType spep::PrincipalSession::getSessionNotOnOrAfter()
{
	return this->_sessionNotOnOrAfter;
}

void spep::PrincipalSession::addESOESessionIndexAndLocalSessionID( std::wstring &esoeSessionIndex, std::string &localSessionID )
{
	ESOESessionIndexMapType::iterator esoeSessionIndexIterator = this->_esoeSessionIndexMap.lower_bound( esoeSessionIndex );
	
	if( esoeSessionIndexIterator != this->_esoeSessionIndexMap.end() && esoeSessionIndex == esoeSessionIndexIterator->first )
	{
		// TODO Error condition? Key already exists
		throw std::exception();
	}
	
	// Insert the esoe session index and local session identifier into the map.
	this->_esoeSessionIndexMap.insert( esoeSessionIndexIterator, std::make_pair( esoeSessionIndex, localSessionID ) );
}

spep::PrincipalSession::ESOESessionIndexMapType &spep::PrincipalSession::getESOESessionIndexMap()
{
	return this->_esoeSessionIndexMap;
}

spep::PrincipalSession::SessionIDListType &spep::PrincipalSession::getSessionIDList()
{
	return this->_sessionIDList;
}

spep::PrincipalSession::AttributeMapType &spep::PrincipalSession::getAttributeMap()
{
	return this->_attributeMap;
}
