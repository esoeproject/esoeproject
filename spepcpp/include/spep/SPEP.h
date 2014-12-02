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
 * Creation Date: 25/09/2006
 *
 * Purpose: Provides a central point for calls to this library.
 */

#ifndef SPEP_H_
#define SPEP_H_

// This needs to be included first on Win32
#include "spep/UnicodeStringConversion.h"

#include "spep/Util.h"
#include "spep/config/ConfigurationReader.h"

#include "saml2/identifier/IdentifierCache.h"
#include "saml2/identifier/IdentifierGenerator.h"
#include "saml2/validator/SAMLValidator.h"

#include "spep/ipc/Socket.h"
#include "spep/attribute/AttributeProcessor.h"
#include "spep/authn/AuthnProcessor.h"
#include "spep/metadata/Metadata.h"
#include "spep/metadata/KeyResolver.h"
#include "spep/pep/PolicyEnforcementProcessor.h"
#include "spep/pep/SessionGroupCache.h"
#include "spep/sessions/SessionCache.h"
#include "spep/sessions/SessionCacheThread.h"
#include "spep/startup/StartupProcessor.h"
#include "saml2/logging/api.h"
#include "spep/config/Configuration.h"
#include "spep/config/SPEPConfigData.h"
#include "spep/ws/WSProcessor.h"

namespace spep
{

    class SPEPEXPORT SPEP
    {
        enum SPEPMode
        {
            SPEP_MODE_SERVER,
            SPEP_MODE_CLIENT,
            SPEP_MODE_STANDALONE
        };

    public:

        SPEP();
        ~SPEP();

        static SPEP* initializeClient(int spepDaemonPort);
        static SPEP* initializeServer(ConfigurationReader &configuration, const std::vector<saml2::Handler*>& handlers);
        static SPEP* initializeStandalone(ConfigurationReader &configuration, const std::vector<saml2::Handler*>& handlers);

        AuthnProcessor *getAuthnProcessor();
        AttributeProcessor *getAttributeProcessor();
        Metadata *getMetadata();
        PolicyEnforcementProcessor *getPolicyEnforcementProcessor();
        SessionGroupCache *getSessionGroupCache();
        SessionCache *getSessionCache();
        StartupProcessor *getStartupProcessor();
        saml2::Logger *getLogger();
        Configuration *getConfiguration();
        SPEPConfigData *getSPEPConfigData();
        WSProcessor *getWSProcessor();
        saml2::IdentifierCache *getIdentifierCache();

        bool isStarted();

    private:

        SPEP(SPEP& other);
        SPEP& operator=(SPEP& other);

        void destroy();
        void reinitializeClient();
        void checkConnection();

        SPEPMode mMode;
        bool mIsStarted;
        std::string mServiceID;

        Mutex mMutex;

        saml2::IdentifierCache *mIdentifierCache;
        saml2::IdentifierGenerator *mIdentifierGenerator;
        saml2::SAMLValidator *mSamlValidator;

        AuthnProcessor *mAuthnProcessor;
        AttributeProcessor *mAttributeProcessor;
        Metadata *mMetadata;
        PolicyEnforcementProcessor *mPolicyEnforcementProcessor;
        SessionGroupCache *mSessionGroupCache;
        SessionCache *mSessionCache;
        SessionCacheThread *mSessionCacheThread;
        StartupProcessor *mStartupProcessor;

        saml2::Logger *mLogger;
        Configuration *mConfiguration;
        SPEPConfigData *mSpepConfigData;
        SOAPUtil *mSoapUtil;
        WSClient *mWSClient;
        WSProcessor *mWSProcessor;
        spep::ipc::ClientSocketPool *mSocketPool;
    };

}

#endif /*SPEP_H_*/
