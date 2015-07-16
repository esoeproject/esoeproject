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

namespace spep {

AuthnProcessorData::AuthnProcessorData()
{
	_disableAttributeQuery = false;
}

std::string AuthnProcessorData::getRequestURL() const
{
	return _requestURL;
}

void AuthnProcessorData::setRequestURL(const std::string &requestURL)
{
	_requestURL = requestURL;
}

std::string AuthnProcessorData::getBaseRequestURL() const
{
	return _baseRequestURL;
}

void AuthnProcessorData::setBaseRequestURL(const std::string& baseRequestURL)
{
	_baseRequestURL = baseRequestURL;
}

std::string AuthnProcessorData::getSessionID() const
{
	return _sessionID;
}

void AuthnProcessorData::setSessionID(const std::string &sessionID)
{
	_sessionID = sessionID;
}

const saml2::SAMLDocument& AuthnProcessorData::getRequestDocument() const
{
	return _requestDocument;
}

void AuthnProcessorData::setRequestDocument(const saml2::SAMLDocument& document)
{
	_requestDocument = document;
}

const saml2::SAMLDocument& AuthnProcessorData::getResponseDocument() const
{
	return _responseDocument;
}

void AuthnProcessorData::setResponseDocument(const saml2::SAMLDocument& document)
{
	_responseDocument = document;
}

void AuthnProcessorData::setDisableAttributeQuery(bool value)
{
	_disableAttributeQuery = value;
}

bool AuthnProcessorData::getDisableAttributeQuery() const
{
	return _disableAttributeQuery;
}

void AuthnProcessorData::setRemoteIpAddress(const std::string &remoteIpAddress)
{
	_remoteIpAddress = remoteIpAddress;
}

std::string AuthnProcessorData::getRemoteIpAddress() const
{
	return _remoteIpAddress;
}

}