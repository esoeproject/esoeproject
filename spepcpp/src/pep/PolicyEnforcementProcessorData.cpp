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
 * Creation Date: 12/03/2007
 * 
 * Purpose: 
 */

#include "pep/PolicyEnforcementProcessorData.h"

void spep::PolicyEnforcementProcessorData::setRequestDocument( const saml2::SAMLDocument& document )
{
	_requestDocument = document;
}

const saml2::SAMLDocument& spep::PolicyEnforcementProcessorData::getRequestDocument()
{
	return _requestDocument;
}

void spep::PolicyEnforcementProcessorData::setResponseDocument( const saml2::SAMLDocument& document )
{
	_responseDocument = document;
}

const saml2::SAMLDocument& spep::PolicyEnforcementProcessorData::getResponseDocument()
{
	return _responseDocument;
}

void spep::PolicyEnforcementProcessorData::setESOESessionID( std::wstring esoeSessionID )
{
	_esoeSessionID = esoeSessionID;
}

std::wstring spep::PolicyEnforcementProcessorData::getESOESessionID()
{
	return _esoeSessionID;
}

void spep::PolicyEnforcementProcessorData::setResource( UnicodeString resource )
{
	_resource.setTo( resource );
}

UnicodeString spep::PolicyEnforcementProcessorData::getResource()
{
	return _resource;
}

void spep::PolicyEnforcementProcessorData::setDecision( spep::Decision decision )
{
	_decision = decision;
}

spep::Decision spep::PolicyEnforcementProcessorData::getDecision()
{
	return _decision;
}
