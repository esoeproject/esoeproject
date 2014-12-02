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

#include "spep/ws/WSProcessorData.h"

spep::WSProcessorData::WSProcessorData() :
    mRequestSOAPDocument(),
    mResponseSOAPDocument(),
    mRequestSAMLDocument(),
    mResponseSAMLDocument(),
    mSoapVersion(SOAPUtil::UNINITIALIZED)
{
}

const spep::SOAPDocument& spep::WSProcessorData::getSOAPRequestDocument()
{
    return mRequestSOAPDocument;
}

void spep::WSProcessorData::setSOAPRequestDocument(const spep::SOAPDocument& document)
{
    mRequestSOAPDocument = document;
}

const saml2::SAMLDocument& spep::WSProcessorData::getSAMLRequestDocument()
{
    return mRequestSAMLDocument;
}

void spep::WSProcessorData::setSAMLRequestDocument(const saml2::SAMLDocument& document)
{
    mRequestSAMLDocument = document;
}

const spep::SOAPDocument& spep::WSProcessorData::getSOAPResponseDocument()
{
    return mResponseSOAPDocument;
}

void spep::WSProcessorData::setSOAPResponseDocument(const spep::SOAPDocument& document)
{
    mResponseSOAPDocument = document;
}

const saml2::SAMLDocument& spep::WSProcessorData::getSAMLResponseDocument()
{
    return mResponseSAMLDocument;
}

void spep::WSProcessorData::setSAMLResponseDocument(const saml2::SAMLDocument& document)
{
    mResponseSAMLDocument = document;
}

std::string spep::WSProcessorData::getCharacterEncoding() const
{
    return mCharacterEncoding;
}

void spep::WSProcessorData::setCharacterEncoding(std::string characterEncoding)
{
    mCharacterEncoding = characterEncoding;
}

spep::SOAPUtil::SOAPVersion spep::WSProcessorData::getSOAPVersion() const
{
    return mSoapVersion;
}

void spep::WSProcessorData::setSOAPVersion(spep::SOAPUtil::SOAPVersion soapVersion)
{
    mSoapVersion = soapVersion;
}
