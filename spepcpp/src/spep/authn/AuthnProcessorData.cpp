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
 * Creation Date: 19/02/2007
 * 
 * Purpose: 
 */

#include "spep/authn/AuthnProcessorData.h"

spep::AuthnProcessorData::AuthnProcessorData()
{
	_disableAttributeQuery = false;
}

std::string spep::AuthnProcessorData::getRequestURL()
{
	return _requestURL;
}

void spep::AuthnProcessorData::setRequestURL( const std::string &requestURL )
{
	_requestURL = requestURL;
}

std::string spep::AuthnProcessorData::getBaseRequestURL()
{
	return _baseRequestURL;
}

void spep::AuthnProcessorData::setBaseRequestURL( const std::string& baseRequestURL )
{
	_baseRequestURL = baseRequestURL;
}

std::string spep::AuthnProcessorData::getSessionID()
{
	return _sessionID;
}

void spep::AuthnProcessorData::setSessionID( const std::string &sessionID )
{
	_sessionID = sessionID;
}

const saml2::SAMLDocument& spep::AuthnProcessorData::getRequestDocument()
{
	return _requestDocument;
}

void spep::AuthnProcessorData::setRequestDocument( const saml2::SAMLDocument& document )
{
	_requestDocument = document;
}

const saml2::SAMLDocument& spep::AuthnProcessorData::getResponseDocument()
{
	return _responseDocument;
}

void spep::AuthnProcessorData::setResponseDocument( const saml2::SAMLDocument& document )
{
	_responseDocument = document;
}

void spep::AuthnProcessorData::setDisableAttributeQuery( bool value )
{
	_disableAttributeQuery = value;
}

bool spep::AuthnProcessorData::getDisableAttributeQuery()
{
	return _disableAttributeQuery;
}
