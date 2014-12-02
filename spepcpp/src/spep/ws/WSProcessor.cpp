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

#include "spep/ws/WSProcessor.h"
#include "spep/Util.h"
#include "spep/UnicodeStringConversion.h"


spep::WSProcessor::WSProcessor(saml2::Logger *logger, spep::AuthnProcessor *authnProcessor, spep::PolicyEnforcementProcessor *policyEnforcementProcessor, spep::SOAPUtil *soapUtil) :
    mLocalLogger(logger, "spep::WSProcessor"),
    mAuthnProcessor(authnProcessor),
    mPolicyEnforcementProcessor(policyEnforcementProcessor),
    mSoapUtil(soapUtil)
{
}

spep::SOAPDocument spep::WSProcessor::authzCacheClear(const spep::SOAPDocument& requestDocument, spep::SOAPUtil::SOAPVersion soapVersion, const std::string& characterEncoding)
{
    mLocalLogger.trace() << UnicodeStringConversion::toString(UnicodeStringConversion::toUnicodeString(requestDocument));

    std::auto_ptr<middleware::ESOEProtocolSchema::ClearAuthzCacheRequestType> request;
    saml2::Unmarshaller<middleware::ESOEProtocolSchema::ClearAuthzCacheRequestType> *unmarshaller =
       mPolicyEnforcementProcessor->mClearAuthzCacheRequestUnmarshaller.get();
    try
    {
        request.reset(this->mSoapUtil->unwrapObjectFromSOAP(unmarshaller, requestDocument, soapVersion));
        mLocalLogger.debug() << "Unwrapped SOAP document successfully. Going to perform authz cache clear.";
    }
    catch (saml2::UnmarshallerException& ex)
    {
        // TODO Maybe generate a SOAP fault.
        mLocalLogger.error() << "Error occurred unwrapping SOAP envelope. Message was: " << ex.getMessage() << " Cause was: " << ex.getCause();
        return spep::SOAPDocument();
    }

    if (request.get() == NULL)
    {
        mLocalLogger.error() << "Unwrapped SOAP message was NULL(?) Can't continue with authz cache clear request.";
        return spep::SOAPDocument();
    }

    DOMDocument *response = nullptr;
    try
    {
        response =mPolicyEnforcementProcessor->authzCacheClear(request.get());
    }
    catch (std::exception& ex)
    {
        // TODO Maybe generate a SOAP fault.
        mLocalLogger.error() << std::string("Error occurred performing cache clear. Message was: ") << ex.what();
        return spep::SOAPDocument();
    }
    xml_schema::dom::auto_ptr<DOMDocument> responseAutoRelease(response);

    mLocalLogger.debug() << "Performed authz cache clear. Going to wrap response document.";
    try
    {
        SOAPDocument responseDocument = mSoapUtil->wrapObjectInSOAP(response->getDocumentElement(), characterEncoding, SOAPUtil::SOAP11);
        mLocalLogger.debug() << "Wrapped response document. Returning.";
        mLocalLogger.trace() << UnicodeStringConversion::toString(UnicodeStringConversion::toUnicodeString(responseDocument));

        return responseDocument;
    }
    catch (saml2::MarshallerException& ex)
    {
        // TODO Maybe generate a SOAP fault.
        mLocalLogger.error() << "Error occurred wrapping SOAP response. Message was: " << ex.getMessage() << " Cause was: " << ex.getCause();
        return spep::SOAPDocument();
    }
}

spep::SOAPDocument spep::WSProcessor::singleLogout(const spep::SOAPDocument& requestDocument, spep::SOAPUtil::SOAPVersion soapVersion, const std::string& characterEncoding)
{
    mLocalLogger.trace() << UnicodeStringConversion::toString(UnicodeStringConversion::toUnicodeString(requestDocument));

    std::auto_ptr<saml2::protocol::LogoutRequestType> request;
    saml2::Unmarshaller<saml2::protocol::LogoutRequestType> *unmarshaller =
       mAuthnProcessor->mLogoutRequestUnmarshaller.get();
    try
    {
        request.reset(mSoapUtil->unwrapObjectFromSOAP(unmarshaller, requestDocument, soapVersion));
        mLocalLogger.debug() << "Unwrapped SOAP document successfully. Going to perform single logout.";
    }
    catch (saml2::UnmarshallerException& ex)
    {
        // TODO Maybe generate a SOAP fault.
        mLocalLogger.error() << "Error occurred unwrapping SOAP envelope. Message was: " << ex.getMessage() << " Cause was: " << ex.getCause();
        return spep::SOAPDocument();
    }
    catch (std::exception& ex)
    {
        // TODO Maybe generate a SOAP fault.
        mLocalLogger.error() << std::string("Error occurred unwrapping SOAP envelope. Message was: ") << ex.what();
        return spep::SOAPDocument();
    }

    if (request.get() == NULL)
    {
        mLocalLogger.error() << "Unwrapped SOAP message was NULL(?) Can't continue with authz cache clear request.";
        return spep::SOAPDocument();
    }

    DOMDocument *response = nullptr;
    try
    {
        response = mAuthnProcessor->logoutPrincipal(request.get());
    }
    catch (std::exception &ex)
    {
        // TODO Maybe generate a SOAP fault.
        mLocalLogger.error() << std::string("Error occurred performing logout. Message was: ") << ex.what();
        return spep::SOAPDocument();
    }

    xml_schema::dom::auto_ptr<DOMDocument> responseAutoRelease(response);

    mLocalLogger.debug() << "Performed single logout. Going to wrap response document.";
    try
    {
        SOAPDocument responseDocument = mSoapUtil->wrapObjectInSOAP(response->getDocumentElement(), characterEncoding, SOAPUtil::SOAP11);
        mLocalLogger.debug() << "Wrapped response document. Returning.";
        mLocalLogger.trace() << UnicodeStringConversion::toString(UnicodeStringConversion::toUnicodeString(responseDocument));

        return responseDocument;
    }
    catch (saml2::MarshallerException& ex)
    {
        // TODO Maybe generate a SOAP fault.
        mLocalLogger.error() << "Error occurred wrapping SOAP response. Message was: " << ex.getMessage() << " Cause was: " << ex.getCause();
        return spep::SOAPDocument();
    }
    catch (std::exception& ex)
    {
        // TODO Maybe generate a SOAP fault.
        mLocalLogger.error() << std::string("Error occurred wrapping SOAP response. Message was: ") << ex.what();
        return spep::SOAPDocument();
    }
}
