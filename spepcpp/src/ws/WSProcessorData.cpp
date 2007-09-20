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
 * Creation Date: Jul 30, 2007
 * 
 * Purpose: 
 */

#include "ws/WSProcessorData.h"

spep::WSProcessorData::WSProcessorData()
:
_requestSOAPDocument(),
_responseSOAPDocument(),
_requestSAMLDocument(),
_responseSAMLDocument(),
_soapVersion( SOAPUtil::UNINITIALIZED )
{
}

const spep::SOAPDocument& spep::WSProcessorData::getSOAPRequestDocument()
{
	return this->_requestSOAPDocument;
}

void spep::WSProcessorData::setSOAPRequestDocument( const spep::SOAPDocument& document )
{
	this->_requestSOAPDocument = document;
}

const saml2::SAMLDocument& spep::WSProcessorData::getSAMLRequestDocument()
{
	return this->_requestSAMLDocument;
}

void spep::WSProcessorData::setSAMLRequestDocument( const saml2::SAMLDocument& document )
{
	this->_requestSAMLDocument = document;
}

const spep::SOAPDocument& spep::WSProcessorData::getSOAPResponseDocument()
{
	return this->_responseSOAPDocument;
}

void spep::WSProcessorData::setSOAPResponseDocument( const spep::SOAPDocument& document )
{
	this->_responseSOAPDocument = document;
}

const saml2::SAMLDocument& spep::WSProcessorData::getSAMLResponseDocument()
{
	return this->_responseSAMLDocument;
}

void spep::WSProcessorData::setSAMLResponseDocument( const saml2::SAMLDocument& document )
{
	this->_responseSAMLDocument = document;
}

std::string spep::WSProcessorData::getCharacterEncoding()
{
	return this->_characterEncoding;
}

void spep::WSProcessorData::setCharacterEncoding( std::string characterEncoding )
{
	this->_characterEncoding = characterEncoding;
}

spep::SOAPUtil::SOAPVersion spep::WSProcessorData::getSOAPVersion()
{
	return this->_soapVersion;
}

void spep::WSProcessorData::setSOAPVersion( spep::SOAPUtil::SOAPVersion soapVersion )
{
	this->_soapVersion = soapVersion;
}
