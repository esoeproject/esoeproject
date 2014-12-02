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

#include "spep/startup/impl/StartupProcessorImpl.h"
#include "spep/UnicodeStringConversion.h"

#include "saml2/constants/VersionConstants.h"
#include "saml2/constants/StatusCodeConstants.h"

#include "spep/exceptions/SPEPStartupException.h"
#include "spep/exceptions/InvalidStateException.h"

#include "saml2/handlers/impl/UnmarshallerImpl.h"
#include "saml2/handlers/impl/MarshallerImpl.h"
#include "saml2/exceptions/InvalidSAMLResponseException.h"

#include <boost/lexical_cast.hpp>

//#define COMPILE_DATE __DATE__

#ifndef COMPILE_SYSTEM

#ifdef __GNUC__
#define COMPILE_SYSTEM __VERSION__
#endif /* __GNUC__ */

#ifdef WIN32
#define COMPILE_OS "Win32 "

#ifdef _MSC_VER
#define COMPILE_CXX COMPILE_OS "Visual C++ " #_MSC_VER
#else /*_MSC_VER*/
#define COMPILE_SYSTEM COMPILE_OS "Unknown compiler"
#endif /*_MSC_VER*/

#endif /*WIN32*/

#ifndef COMPILE_SYSTEM/*(2)*/
#define COMPILE_SYSTEM "Unknown compile system"
#endif /*COMPILE_SYSTEM(2)*/

#endif /* COMPILE_SYSTEM */

#ifdef PACKAGE_STRING
#define COMPILE_VERSION PACKAGE_STRING
#else /*PACKAGE_STRING*/
#define COMPILE_VERSION "Unsupported version"
#endif /*PACKAGE_STRING*/

#define ENVIRONMENT L"Unspecified environment"

spep::StartupProcessorImpl::StartupProcessorImpl(saml2::Logger *logger, spep::WSClient *wsClient, spep::Metadata *metadata, spep::KeyResolver *keyResolver, saml2::IdentifierGenerator *identifierGenerator, saml2::SAMLValidator *samlValidator, const std::string& schemaPath, const std::wstring& spepIdentifier, const std::vector<std::wstring>& ipAddresses, const std::string& nodeID, int authzCacheIndex, int startupRetryInterval) :
    mLocalLogger(logger, "spep::StartupProcessorImpl"),
    mLogger(logger),
    mWSClient(wsClient),
    mMetadata(metadata),
    mKeyResolver(keyResolver),
    mSamlValidator(samlValidator),
    mStartupResult(STARTUP_NONE),
    mSpepIdentifier(spepIdentifier),
    mIPAddresses(ipAddresses),
    mNodeID(nodeID),
    mAuthzCacheIndex(authzCacheIndex),
    mStartupRetryInterval(startupRetryInterval),
    mIdentifierGenerator(identifierGenerator)
{
    std::vector<std::string> spepStartupSchemas{ ConfigurationConstants::esoeProtocol };

    mValidateInitializationRequestMarshaller = std::make_unique<saml2::MarshallerImpl
        <middleware::ESOEProtocolSchema::ValidateInitializationRequestType>>(
        logger, schemaPath, spepStartupSchemas, "ValidateInitializationRequest", "http://www.qut.com/middleware/ESOEProtocolSchema",
        mKeyResolver->getSPEPKeyAlias(), mKeyResolver->getSPEPPrivateKey()
        );

    mValidateInitializationResponseUnmarshaller = std::make_unique<saml2::UnmarshallerImpl
        <middleware::ESOEProtocolSchema::ValidateInitializationResponseType>>
        (logger, schemaPath, spepStartupSchemas, mMetadata);
}

spep::StartupProcessorImpl::~StartupProcessorImpl()
{
}

XERCES_CPP_NAMESPACE::DOMDocument* spep::StartupProcessorImpl::buildRequest(const std::wstring &samlID)
{
    mLocalLogger.debug() << "Going to build SPEP startup request.";

    saml2::assertion::NameIDType issuer(mSpepIdentifier);

    middleware::ESOEProtocolSchema::ValidateInitializationRequestType validateInitializationRequest;
    validateInitializationRequest.ID(samlID);
    validateInitializationRequest.Version(saml2::versions::SAML_20);
    validateInitializationRequest.IssueInstant(xml_schema::date_time());
    validateInitializationRequest.Issuer(issuer);
    validateInitializationRequest.nodeId(UnicodeStringConversion::toWString(boost::lexical_cast<std::string>(mNodeID)));
    validateInitializationRequest.authzCacheIndex(mAuthzCacheIndex);

    validateInitializationRequest.compileDate(UnicodeStringConversion::toWString(std::string(__DATE__)));
    validateInitializationRequest.compileSystem(UnicodeStringConversion::toWString(std::string(COMPILE_SYSTEM)));
    validateInitializationRequest.sw_version(UnicodeStringConversion::toWString(COMPILE_VERSION));
    validateInitializationRequest.environment(ENVIRONMENT);
    for (auto iter = mIPAddresses.begin(); iter != mIPAddresses.end(); ++iter)
    {
        validateInitializationRequest.ipAddress().push_back(*iter);
    }

    std::vector<std::string> idList{ UnicodeStringConversion::toString(samlID) };

    DOMDocument *requestDocument = mValidateInitializationRequestMarshaller->generateDOMDocument(&validateInitializationRequest);
    requestDocument = mValidateInitializationRequestMarshaller->validate(requestDocument);
    mValidateInitializationRequestMarshaller->sign(requestDocument, idList);

    return requestDocument;
}

void spep::StartupProcessorImpl::processResponse(middleware::ESOEProtocolSchema::ValidateInitializationResponseType* response, const std::wstring &expectedSAMLID)
{
    try
    {
        // Validate the SAML Response.
        mSamlValidator->getResponseValidator().validate(response);
    }
    catch (saml2::InvalidSAMLResponseException& ex)
    {
        // Response was rejected explicitly.
        mLocalLogger.error() << "SAML response was rejected by SAML Validator. Reason: " << ex.getMessage();
        throw SPEPStartupException("SAML response was rejected by SAML Validator.");
    }
    catch (std::exception& ex)
    {
        // Error occurred validating the response. Reject it anyway.
        mLocalLogger.error() << "Error occurred in the SAML Validator. Message: " << std::string(ex.what());
        throw SPEPStartupException("Error occurred in the SAML Validator.");
    }

    // TODO Check issuer

    xml_schema::uri &statusCodeValue = response->Status().StatusCode().Value();
    if (saml2::statuscode::SUCCESS.compare(0, statusCodeValue.length(), statusCodeValue.c_str()) == 0)
    {
        // Success. Permit the SPEP startup.
        mLocalLogger.info() << "SPEP startup SUCCESS. Beginning normal operation.";
        return;
    }

    if (response->Status().StatusMessage().present())
    {
        mLocalLogger.error() << "SPEP startup FAILED. Retrying later. Message from ESOE was: " << UnicodeStringConversion::toString(response->Status().StatusMessage().get().c_str());
    }
    else
    {
        mLocalLogger.error() << "SPEP startup FAILED. Retrying later. No message from ESOE to explain failure.";
    }

    throw SPEPStartupException("Response from ESOE did not indicate successful SPEP startup.");
}

spep::StartupResult spep::StartupProcessorImpl::allowProcessing()
{
    ScopedLock lock(mStartupResultMutex);
    return mStartupResult;
}

void spep::StartupProcessorImpl::setStartupResult(StartupResult startupResult)
{
    ScopedLock lock(mStartupResultMutex);
    mStartupResult = startupResult;
}

void spep::StartupProcessorImpl::beginSPEPStart()
{
    // Make sure we only start 1 thread..
    {
        ScopedLock lock(mStartupResultMutex);

        if (allowProcessing() != STARTUP_NONE)
            return;

        // Set the startup result to 'wait' so that everything else will block.
        setStartupResult(STARTUP_WAIT);
    }

    StartupProcessorThread threadObject(mLogger, this, mStartupRetryInterval);
    mThreadGroup.create_thread(threadObject);
}

void spep::StartupProcessorImpl::doStartup()
{
    try
    {
        // Generate the request document
        std::wstring samlID(mIdentifierGenerator->generateSAMLID());

        xml_schema::dom::auto_ptr<DOMDocument> requestDocument(buildRequest(samlID));

        std::string endpoint(mMetadata->getSPEPStartupServiceEndpoint());

        {
            std::stringstream ss;
            ss << "About to send SPEP startup WS query to ESOE endpoint: " << endpoint << std::ends;
            mLocalLogger.debug() << ss.str();
        }

        // Perform the web service call.
        try
        {
            std::auto_ptr<middleware::ESOEProtocolSchema::ValidateInitializationResponseType> response(
                mWSClient->doWSCall(endpoint, requestDocument.get(), mValidateInitializationResponseUnmarshaller.get())
                );

            mLocalLogger.debug() << "Received response from web service endpoint. Going to process.";

            // Process the response.
            processResponse(response.get(), samlID);
        }
        catch (saml2::UnmarshallerException& ex)
        {
            std::stringstream ss;
            ss << "SPEP startup ERROR. Exception when unmarshalling startup response. Exception was: " << ex.getMessage() << ". Cause was: " << ex.getCause() << std::ends;
            mLocalLogger.debug() << ss.str();
            throw SPEPStartupException("Exception occurred while unmarshalling SPEP startup response.");
        }
        catch (std::exception& ex)
        {
            std::stringstream ss;
            ss << "SPEP startup ERROR. Exception when unmarshalling startup response. Exception was: " << ex.what() << std::ends;
            mLocalLogger.debug() << ss.str();
            throw SPEPStartupException("Exception occurred while unmarshalling SPEP startup response.");
        }

        // If we made it here, startup was successful..
        setStartupResult(STARTUP_ALLOW);
    }
    catch (saml2::MarshallerException& ex)
    {
        std::stringstream ss;
        ss << "Failed to marshal request document. Error was: " << ex.getMessage() << " .. cause: " << ex.getCause() << std::ends;

        // .. otherwise it failed for some reason.
        mLocalLogger.debug() << ss.str();
        setStartupResult(STARTUP_FAIL);
    }
    catch (std::exception& ex)
    {
        std::stringstream ss;
        ss << "Failed SPEP startup. Exception message was: " << ex.what() << std::ends;

        // .. otherwise it failed for some reason.
        mLocalLogger.debug() << ss.str();
        setStartupResult(STARTUP_FAIL);
    }
}

spep::StartupProcessorImpl::StartupProcessorThread::StartupProcessorThread(saml2::Logger *logger, spep::StartupProcessorImpl *startupProcessor, int startupRetryInterval) :
    mLogger(logger),
    mLocalLogger(logger, "spep::StartupProcessor"),
    mStartupProcessor(startupProcessor),
    mStartupRetryInterval(startupRetryInterval)
{
}

spep::StartupProcessorImpl::StartupProcessorThread::StartupProcessorThread(const spep::StartupProcessorImpl::StartupProcessorThread& other) :
    mLogger(other.mLogger),
    mLocalLogger(other.mLogger, "spep::StartupProcessor"),
    mStartupProcessor(other.mStartupProcessor),
    mStartupRetryInterval(other.mStartupRetryInterval)
{
}

void spep::StartupProcessorImpl::StartupProcessorThread::operator()()
{
    boost::xtime nextUpdate;

    mLocalLogger.debug() << "SPEP startup handler begins.";
    // Loop until we're allowed to start.
    while (mStartupProcessor->allowProcessing() != STARTUP_ALLOW)
    {
        try
        {
            mStartupProcessor->doStartup();
        }
        catch (...)
        {
            mLocalLogger.error() << "Unexpected throw from doStartup() .. ignoring and continuing loop.";
        }

        if (mStartupProcessor->allowProcessing() == STARTUP_ALLOW) break;

        if (boost::xtime_get(&nextUpdate, boost::TIME_UTC_) == 0)
        {
            mLocalLogger.error() << "Couldn't get UTC time from boost::xtime_get";
        }

        nextUpdate.sec += mStartupRetryInterval;

        boost::thread::sleep(nextUpdate);

    }
    mLocalLogger.debug() << "SPEP startup handler exiting loop.";
}

