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
 * Creation Date: Aug 14, 2007
 * 
 * Purpose: 
 */

#include "spep/ws/SOAPUtil.h"
#include "spep/Util.h"

#include "saml2/handlers/impl/UnmarshallerImpl.h"
#include "saml2/handlers/impl/MarshallerImpl.h"
#include "saml2/SAML2Defs.h"


// FIXME: how often does this get created?
spep::SOAPUtil::SOAPUtil(saml2::Logger *logger, const std::string& schemaPath) :
    mLocalLogger(logger, "spep::SOAPUtil"),
    mSoap11Handler(std::make_unique<SOAP11Handler>(logger, schemaPath)),
    mSoap12Handler(std::make_unique<SOAP12Handler>(logger, schemaPath))
{
}

spep::SOAPUtil::~SOAPUtil()
{
}

/**
 * Wraps a given SAML object and marshalls into a SOAP document ready for a web service request/response.
 */
spep::SOAPDocument spep::SOAPUtil::wrapObjectInSOAP(DOMElement *objectElement, const std::string& characterEncoding, spep::SOAPUtil::SOAPVersion soapVersion)
{
    switch (soapVersion)
    {
    case SOAP12:
        mLocalLogger.debug() << "Going to wrap SOAP/1.2 envelope.";
        return mSoap12Handler->wrap(objectElement, characterEncoding);

        // Default to SOAP/1.1 processing
    case SOAP11:
    default:
        mLocalLogger.debug() << "Going to wrap SOAP/1.1 envelope.";
        return mSoap11Handler->wrap(objectElement, characterEncoding);
    }
}

spep::SOAPUtil::SOAP11Handler::SOAP11Handler(saml2::Logger *logger, const std::string& schemaPath) :
    mLocalLogger(logger, "spep::SOAPUtil::SOAP11Handler")
{
    std::vector<std::string> schemas{
        spep::ConfigurationConstants::soap11,
        spep::ConfigurationConstants::samlProtocol,
        spep::ConfigurationConstants::samlAssertion,
        spep::ConfigurationConstants::samlMetadata,
        spep::ConfigurationConstants::lxacml,
        spep::ConfigurationConstants::lxacmlSAMLProtocol,
        spep::ConfigurationConstants::lxacmlSAMLAssertion,
        spep::ConfigurationConstants::lxacmlGroupTarget,
        spep::ConfigurationConstants::lxacmlContext,
        spep::ConfigurationConstants::lxacmlMetadata,
        spep::ConfigurationConstants::esoeProtocol,
        spep::ConfigurationConstants::cacheClearService,
        spep::ConfigurationConstants::spepStartupService,
        spep::ConfigurationConstants::sessionData,
        spep::ConfigurationConstants::attributeConfig };

    mEnvelopeUnmarshaller = std::make_unique<saml2::UnmarshallerImpl< soap::v11::Envelope>>(logger, schemaPath, schemas);
    mEnvelopeMarshaller = std::make_unique<saml2::MarshallerImpl< soap::v11::Envelope>>(logger, schemaPath, schemas,
        "Envelope", "http://schemas.xmlsoap.org/soap/envelope/"
        );
    mBodyUnmarshaller = std::make_unique<saml2::UnmarshallerImpl< soap::v11::Body>>(logger, schemaPath, schemas);
    mBodyMarshaller = std::make_unique<saml2::MarshallerImpl< soap::v11::Body>>(logger, schemaPath, schemas,
        "Body", "http://schemas.xmlsoap.org/soap/envelope/"
        );

    mImplFlags = XMLString::transcode(IMPL_FLAGS);
    mDomImpl = DOMImplementationRegistry::getDOMImplementation(mImplFlags);
}

spep::SOAPUtil::SOAP11Handler::~SOAP11Handler()
{
    if (mImplFlags != NULL)
    {
        XMLString::release(&mImplFlags);
    }
}

spep::SOAPDocument spep::SOAPUtil::SOAP11Handler::wrap(DOMElement *objectElement, const std::string& characterEncoding)
{
    soap::v11::Body body;
    body.any().push_back(*objectElement);

    soap::v11::Envelope envelope;
    envelope.Body(body);

    return mEnvelopeMarshaller->marshallUnSigned(&envelope, false);
}

spep::SOAPUtil::SOAP12Handler::SOAP12Handler(saml2::Logger *logger, const std::string& schemaPath) :
    mLocalLogger(logger, "spep::SOAPUtil::SOAP12Handler")
{
    std::vector<std::string> schemas;
    schemas.push_back(spep::ConfigurationConstants::soap12);
    schemas.push_back(spep::ConfigurationConstants::samlProtocol);
    schemas.push_back(spep::ConfigurationConstants::samlAssertion);
    schemas.push_back(spep::ConfigurationConstants::samlMetadata);
    schemas.push_back(spep::ConfigurationConstants::lxacml);
    schemas.push_back(spep::ConfigurationConstants::lxacmlSAMLProtocol);
    schemas.push_back(spep::ConfigurationConstants::lxacmlSAMLAssertion);
    schemas.push_back(spep::ConfigurationConstants::lxacmlGroupTarget);
    schemas.push_back(spep::ConfigurationConstants::lxacmlContext);
    schemas.push_back(spep::ConfigurationConstants::lxacmlMetadata);
    schemas.push_back(spep::ConfigurationConstants::esoeProtocol);
    schemas.push_back(spep::ConfigurationConstants::cacheClearService);
    schemas.push_back(spep::ConfigurationConstants::spepStartupService);
    schemas.push_back(spep::ConfigurationConstants::sessionData);
    schemas.push_back(spep::ConfigurationConstants::attributeConfig);

    mEnvelopeUnmarshaller = std::make_unique<saml2::UnmarshallerImpl< soap::v12::Envelope>>(logger, schemaPath, schemas);
    mEnvelopeMarshaller = std::make_unique<saml2::MarshallerImpl< soap::v12::Envelope>>(logger, schemaPath, schemas,
        "Envelope", "http://www.w3.org/2003/05/soap-envelope"
        );

    mImplFlags = XMLString::transcode(IMPL_FLAGS);
    mDomImpl = DOMImplementationRegistry::getDOMImplementation(mImplFlags);
}

spep::SOAPUtil::SOAP12Handler::~SOAP12Handler()
{
    if (mImplFlags != NULL)
    {
        XMLString::release(&mImplFlags);
    }
}

spep::SOAPDocument spep::SOAPUtil::SOAP12Handler::wrap(DOMElement *objectElement, const std::string& characterEncoding)
{
    soap::v12::Body body;
    body.any().push_back(*objectElement);

    soap::v12::Envelope envelope;
    envelope.Body(body);

    return mEnvelopeMarshaller->marshallUnSigned(&envelope, false);
}
