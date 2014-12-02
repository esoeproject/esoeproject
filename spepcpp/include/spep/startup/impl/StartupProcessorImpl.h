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

#ifndef STARTUPPROCESSORIMPL_H_
#define STARTUPPROCESSORIMPL_H_

#include "saml2/bindings/esoe-schema-saml-protocol.hxx"

#include "saml2/identifier/IdentifierGenerator.h"
#include "saml2/handlers/Marshaller.h"
#include "saml2/handlers/Unmarshaller.h"
#include "saml2/validator/SAMLValidator.h"
#include "saml2/SAML2Defs.h"

#include "spep/Util.h"
#include "spep/UnicodeStringConversion.h"
#include "saml2/logging/api.h"
#include "saml2/logging/api.h"
#include "spep/ws/WSClient.h"
#include "spep/metadata/Metadata.h"
#include "spep/metadata/KeyResolver.h"
#include "spep/startup/StartupProcessor.h"

#include <boost/thread.hpp>

namespace spep
{

    /**
     * Handles operations related to SPEP startup, and ensuring that the SPEP has started
     */
    class SPEPEXPORT StartupProcessorImpl : public StartupProcessor
    {
    public:

        StartupProcessorImpl(saml2::Logger *logger, WSClient *wsClient, Metadata *metadata, KeyResolver *keyResolver, saml2::IdentifierGenerator *identifierGenerator, saml2::SAMLValidator *samlValidator, const std::string& schemaPath, const std::wstring& spepIdentifier, const std::vector<std::wstring>& ipAddresses, const std::string& nodeID, int authzCacheIndex, int startupRetryInterval);
        virtual ~StartupProcessorImpl();

        /**
         * Returns a result indicating whether the SPEP should be allowed to process requests
         */
        virtual StartupResult allowProcessing() override;

        /**
         * Sets the result of SPEP startup in the startup processor.
         */
        void setStartupResult(StartupResult startupResult);

        /**
         * Instructs the startup processor to begin the startup request. This method will return after creating
         * a thread, and there is no guarantee that any other action has occurred upon returning.
         */
        virtual void beginSPEPStart() override;

        /**
         * Performs the startup action. This method will block until it has complete. Should never be called
         * directly, except by beginSPEPStart()
         */
        void doStartup();

    private:

        StartupProcessorImpl(const StartupProcessorImpl& other);
        StartupProcessorImpl& operator=(const StartupProcessorImpl& other);

        class StartupProcessorThread
        {
        public:
            StartupProcessorThread(saml2::Logger *logger, StartupProcessorImpl *startupProcessor, int startupRetryInterval);
            StartupProcessorThread(const StartupProcessorThread& other);
            void operator()();
            //StartupProcessorThread& operator=( const StartupProcessorThread& other );

        private:
            saml2::Logger *mLogger;
            saml2::LocalLogger mLocalLogger;
            StartupProcessorImpl *mStartupProcessor;
            int mStartupRetryInterval;

        };

        /**
        * Builds a ValidateInitializationRequest document to be sent to the ESOE when requesting startup
        */
        DOMDocument* buildRequest(const std::wstring& samlID);

        /**
        * Processes a ValidateInitializationResponse document and stores the decision made by the ESOE
        */
        void processResponse(middleware::ESOEProtocolSchema::ValidateInitializationResponseType* response, const std::wstring& expectedSAMLID);

        saml2::LocalLogger mLocalLogger;
        saml2::Logger *mLogger;

        WSClient *mWSClient;
        Metadata *mMetadata;
        KeyResolver *mKeyResolver;

        saml2::SAMLValidator *mSamlValidator;

        mutable Mutex mStartupResultMutex;
        spep::StartupResult mStartupResult;

        std::wstring mSpepIdentifier;
        std::vector<std::wstring> mIPAddresses;
        std::string mNodeID;
        int mAuthzCacheIndex;
        int mStartupRetryInterval;

        std::unique_ptr<saml2::Marshaller<middleware::ESOEProtocolSchema::ValidateInitializationRequestType>> mValidateInitializationRequestMarshaller;
        std::unique_ptr<saml2::Unmarshaller<middleware::ESOEProtocolSchema::ValidateInitializationResponseType>> mValidateInitializationResponseUnmarshaller;
        saml2::IdentifierGenerator *mIdentifierGenerator;

        boost::thread_group mThreadGroup;
    };

}

#endif /* STARTUPPROCESSORIMPL_H_ */
