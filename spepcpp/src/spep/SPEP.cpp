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

#include "saml2/logging/api.h"

#include "spep/SPEP.h"

#include "spep/sessions/proxy/SessionCacheProxy.h"
#include "spep/sessions/impl/SessionCacheImpl.h"
#include "spep/metadata/proxy/MetadataProxy.h"
#include "spep/metadata/impl/MetadataImpl.h"
#include "spep/config/proxy/ConfigurationProxy.h"
#include "spep/config/impl/ConfigurationImpl.h"
#include "spep/pep/proxy/SessionGroupCacheProxy.h"
#include "spep/pep/impl/SessionGroupCacheImpl.h"
#include "spep/startup/proxy/StartupProcessorProxy.h"
#include "spep/startup/impl/StartupProcessorImpl.h"
#include "spep/identifier/proxy/IdentifierCacheProxy.h"
#include "spep/logging/proxy/LoggerProxy.h"
#include "spep/ws/SOAPUtil.h"


// Define a "short sleep" to be 100 milliseconds.
#define SHORT_SLEEP_NANOSECONDS (20*1000*1000)
#define LONGER_SLEEP_NANOSECONDS (500*1000*1000)

#define ONE_SECOND_NANOSECONDS (1000*1000*1000)

namespace spep {

    SPEP::SPEP() :
        /* Initialize all these to nullptr so that we don't seg fault by trying to delete them. */
        mIsStarted(false),
        mServiceID(),
        mMutex(),
        mIdentifierCache(nullptr),
        mIdentifierGenerator(nullptr),
        mSamlValidator(nullptr),
        mSocketPool(nullptr),
        mAuthnProcessor(nullptr),
        mAttributeProcessor(nullptr),
        mMetadata(nullptr),
        mPolicyEnforcementProcessor(nullptr),
        mSessionGroupCache(nullptr),
        mSessionCache(nullptr),
        mSessionCacheThread(nullptr),
        mStartupProcessor(nullptr),
        mLogger(nullptr),
        mConfiguration(nullptr),
        mSpepConfigData(nullptr),
        mSoapUtil(nullptr),
        mWSClient(nullptr),
        mWSProcessor(nullptr)
    {
    }

    spep::SPEP::~SPEP()
    {
        delete mIdentifierCache;
        delete mIdentifierGenerator;
        delete mSamlValidator;
        delete mSocketPool;
        delete mAuthnProcessor;
        delete mAttributeProcessor;
        delete mMetadata;
        delete mPolicyEnforcementProcessor;
        delete mSessionGroupCache;
        delete mSessionCache;
        delete mSessionCacheThread;
        delete mStartupProcessor;
        delete mLogger;
        delete mConfiguration;
        delete mSpepConfigData;
        delete mSoapUtil;
        delete mWSClient;
        delete mWSProcessor;
    }


    SPEP* SPEP::initializeClient(int spepDaemonPort)
    {
        SPEP *spep = new SPEP;

        spep->mMode = SPEP_MODE_CLIENT;
        spep->mIsStarted = false;

#ifdef WIN32
        spep->mSocketPool = new ipc::ClientSocketPool(spepDaemonPort, 20);
#else //!WIN32
#ifdef SPEP_CLIENT_THREADS
        spep->mSocketPool = new ipc::ClientSocketPool(spepDaemonPort, SPEP_CLIENT_THREADS);
#else //!SPEP_CLIENT_THREADS
        spep->mSocketPool = new ipc::ClientSocketPool(spepDaemonPort, 1);
#endif //SPEP_CLIENT_THREADS
#endif //WIN32

        spep->mLogger = new ipc::LoggerProxy(spep->mSocketPool);

        saml2::LocalLogger localLogger(spep->mLogger, "spep::SPEP");

        spep->mStartupProcessor = new ipc::StartupProcessorProxy(spep->mSocketPool);

        spep->mIdentifierCache = new ipc::IdentifierCacheProxy(spep->mSocketPool);
        spep->mIdentifierGenerator = new saml2::IdentifierGenerator(spep->mIdentifierCache);
        spep->mMetadata = new ipc::MetadataProxy(spep->mSocketPool);

        spep->mSessionCache = new ipc::SessionCacheProxy(spep->mSocketPool);

        spep->mSessionGroupCache = new ipc::SessionGroupCacheProxy(spep->mSocketPool);

        try {
            spep->reinitializeClient();

            // This will get the background thread firing to let the SPEP startup request occur.
            if (!spep->mSpepConfigData->disableSPEPStartup())
                spep->mStartupProcessor->allowProcessing();

            spep->mServiceID = spep->mSocketPool->getServiceID();
        }
        catch (ipc::SocketException& e) {
            // There's not much we can do with it. Just stop the caller from dying.
        }
        catch (std::runtime_error& e)
        {
            localLogger.error() << "Error reinitializing SPEP client. Error was: " << e.what();
        }

        return spep;
    }

    void SPEP::reinitializeClient()
    {
        // Reinitialize everything that relies on something from the config data.
        // Things that rely only on the client socket should still work without being reinstantiated.

        delete mSpepConfigData;
        mSpepConfigData = nullptr;

        ipc::ConfigurationProxy configurationProxy(mSocketPool);
        mSpepConfigData = new SPEPConfigData(configurationProxy.getSPEPConfigData());

        if (mSpepConfigData == nullptr)
            throw std::runtime_error("Error creating config data.");

        delete mSoapUtil;
        mSoapUtil = nullptr;

        mSoapUtil = new SOAPUtil(mLogger, mSpepConfigData->getSchemaPath());
        if (mSoapUtil == nullptr)
            throw std::runtime_error("Error creating soap util.");

        delete mSamlValidator;
        mSamlValidator = nullptr;

        mSamlValidator = new saml2::SAMLValidator(mIdentifierCache, mSpepConfigData->getAllowedTimeSkew());
        if (mSamlValidator == nullptr)
            throw std::runtime_error("Error creating SAML Validator.");

        delete mWSClient;
        mWSClient = nullptr;

        mWSClient = new WSClient(mLogger, mSpepConfigData->getCABundle(), mSoapUtil);
        if (mWSClient == nullptr)
            throw std::runtime_error("Error creating WS client.");

        delete mAttributeProcessor;
        mAttributeProcessor = nullptr;

        mAttributeProcessor = new AttributeProcessor(mLogger, mMetadata, mSpepConfigData->getKeyResolver(), mIdentifierGenerator, mWSClient, mSamlValidator, mSpepConfigData->getSchemaPath(), mSpepConfigData->getAttributeRenameMap());
        if (mAttributeProcessor == nullptr)
            throw std::runtime_error("Error creating Attribute Processor.");

        delete mAuthnProcessor;
        mAuthnProcessor = nullptr;

        mAuthnProcessor = new AuthnProcessor(mLogger, mAttributeProcessor, mMetadata, mSessionCache, mSamlValidator, mIdentifierGenerator, mSpepConfigData->getKeyResolver(), mSpepConfigData->getSPEPIdentifier(), mSpepConfigData->getSSORedirect(), mSpepConfigData->getServiceHost(), mSpepConfigData->getSchemaPath(), mSpepConfigData->getAttributeConsumingServiceIndex(), mSpepConfigData->getAssertionConsumerServiceIndex());
        if (mAuthnProcessor == nullptr)
            throw std::runtime_error("Error creating Authn Processor.");

        delete mPolicyEnforcementProcessor;
        mPolicyEnforcementProcessor = nullptr;

        mPolicyEnforcementProcessor = new PolicyEnforcementProcessor(mLogger, mWSClient, mSessionGroupCache, mSessionCache, mMetadata, mIdentifierGenerator, mSamlValidator, mSpepConfigData->getKeyResolver(), mSpepConfigData->getSchemaPath());
        if (mPolicyEnforcementProcessor == nullptr)
            throw std::runtime_error("Error creating Policy Enforcement Processor.");

        delete mWSProcessor;
        mWSProcessor = nullptr;

        mWSProcessor = new WSProcessor(mLogger, mAuthnProcessor, mPolicyEnforcementProcessor, mSoapUtil);
        if (mWSProcessor == nullptr)
            throw std::runtime_error("Error creating WS Processor.");

        mServiceID = mSocketPool->getServiceID();
        // Can't assume we're still connected to a "started" SPEP
        mIsStarted = false;
    }

    SPEP* SPEP::initializeServer(ConfigurationReader &configurationReader, const std::vector<saml2::Handler*>& handlers)
    {
        SPEP *spep = nullptr;
        spep = new SPEP;

        spep->mMode = SPEP_MODE_SERVER;
        spep->mIsStarted = false;

        spep->mLogger = new saml2::Logger();

        for (auto handlerIterator = handlers.begin();
            handlerIterator != handlers.end();  ++handlerIterator)
        {
            spep->mLogger->registerHandler(*handlerIterator);
        }

        saml2::LocalLogger localLogger(spep->mLogger, "spep::SPEP");
        localLogger.debug() << "Beginning to initialize server-side SPEP components.";

        spep->mConfiguration = new ConfigurationImpl(configurationReader);
        spep->mSpepConfigData = new SPEPConfigData(spep->mConfiguration->getSPEPConfigData());

        int allowedTimeSkew = configurationReader.getIntegerValue(CONFIGURATION_ALLOWEDTIMESKEW);
        int metadataInterval = configurationReader.getIntegerValue(CONFIGURATION_METADATAINTERVAL);
        int attributeConsumingServiceIndex = configurationReader.getIntegerValue(CONFIGURATION_ATTRIBUTECONSUMINGSERVICEINDEX);
        int assertionConsumerServiceIndex = configurationReader.getIntegerValue(CONFIGURATION_ASSERTIONCONSUMERSERVICEINDEX);
        int authzCacheIndex = configurationReader.getIntegerValue(CONFIGURATION_AUTHZCACHEINDEX);
        int startupRetryInterval = configurationReader.getIntegerValue(CONFIGURATION_STARTUPRETRYINTERVAL);
        int sessionCacheInterval = configurationReader.getIntegerValue(CONFIGURATION_SESSIONCACHEINTERVAL);
        int sessionCacheTimeout = configurationReader.getIntegerValue(CONFIGURATION_SESSIONCACHETIMEOUT);
        std::string nodeID(configurationReader.getStringValue(CONFIGURATION_NODEIDENTIFIER));
        std::string caBundle(configurationReader.getStringValue(CONFIGURATION_CABUNDLE, std::string()));

        Decision defaultPolicyDecision(configurationReader.getStringValue(CONFIGURATION_DEFAULTPOLICYDECISION));

        const std::string schemaPath(configurationReader.getStringValue(CONFIGURATION_SCHEMAPATH));
        const std::wstring spepIdentifier(UnicodeStringConversion::toWString(configurationReader.getStringValue(CONFIGURATION_SPEPIDENTIFIER)));
        const std::wstring esoeIdentifier(UnicodeStringConversion::toWString(configurationReader.getStringValue(CONFIGURATION_ESOEIDENTIFIER)));
        const std::string metadataURL(configurationReader.getStringValue(CONFIGURATION_METADATAURL));

        spep->mIdentifierCache = new saml2::IdentifierCache();
        spep->mIdentifierGenerator = new saml2::IdentifierGenerator(spep->mIdentifierCache);
        spep->mSamlValidator = new saml2::SAMLValidator(spep->mIdentifierCache, allowedTimeSkew);

        spep->mMetadata = new MetadataImpl(spep->mLogger, schemaPath, spepIdentifier, esoeIdentifier, metadataURL, caBundle, spep->mSpepConfigData->getKeyResolver(), assertionConsumerServiceIndex, metadataInterval);

        spep->mSoapUtil = new SOAPUtil(spep->mLogger, schemaPath);

        spep->mWSClient = new WSClient(spep->mLogger, caBundle, spep->mSoapUtil);

        spep->mAttributeProcessor = new AttributeProcessor(spep->mLogger, spep->mMetadata, spep->mSpepConfigData->getKeyResolver(), spep->mIdentifierGenerator, spep->mWSClient, spep->mSamlValidator, schemaPath, spep->mSpepConfigData->getAttributeRenameMap());
        spep->mSessionCache = new SessionCacheImpl(spep->mLogger);
        spep->mSessionCacheThread = new SessionCacheThread(spep->mLogger, spep->mSessionCache, sessionCacheTimeout, sessionCacheInterval);
        spep->mAuthnProcessor = new AuthnProcessor(spep->mLogger, spep->mAttributeProcessor, spep->mMetadata, spep->mSessionCache, spep->mSamlValidator, spep->mIdentifierGenerator, spep->mSpepConfigData->getKeyResolver(), spepIdentifier, spep->mSpepConfigData->getSSORedirect(), spep->mSpepConfigData->getServiceHost(), schemaPath, attributeConsumingServiceIndex, assertionConsumerServiceIndex);
        spep->mSessionGroupCache = new SessionGroupCacheImpl(spep->mLogger, defaultPolicyDecision);
        spep->mPolicyEnforcementProcessor = new PolicyEnforcementProcessor(spep->mLogger, spep->mWSClient, spep->mSessionGroupCache, spep->mSessionCache, spep->mMetadata, spep->mIdentifierGenerator, spep->mSamlValidator, spep->mSpepConfigData->getKeyResolver(), schemaPath);
        spep->mStartupProcessor = new StartupProcessorImpl(spep->mLogger, spep->mWSClient, spep->mMetadata, spep->mSpepConfigData->getKeyResolver(), spep->mIdentifierGenerator, spep->mSamlValidator, schemaPath, spepIdentifier, spep->mSpepConfigData->getIPAddresses(), nodeID, authzCacheIndex, startupRetryInterval);
        spep->mWSProcessor = new WSProcessor(spep->mLogger, spep->mAuthnProcessor, spep->mPolicyEnforcementProcessor, spep->mSoapUtil);

        return spep;
    }

    SPEP* SPEP::initializeStandalone(ConfigurationReader& configReader, const std::vector<saml2::Handler*>& handlers)
    {
        SPEP* spep = SPEP::initializeServer(configReader, handlers);

        saml2::LocalLogger localLogger(spep->mLogger, "spep::SPEP");

        localLogger.debug() << "Server-side SPEP components initialized. Switching to standalone mode.";
        spep->mMode = SPEP_MODE_STANDALONE;

        return spep;
    }

    void SPEP::checkConnection()
    {
        if (mMode == SPEP_MODE_CLIENT)
        {
            ScopedLock lock(mMutex);

            if (mServiceID.empty() || mServiceID != mSocketPool->getServiceID())
            {
                try {
                    reinitializeClient();
                }
                catch (ipc::SocketException& e) {
                }
                catch (std::runtime_error& e)
                {
                    saml2::LocalLogger localLogger(mLogger, "spep::SPEP::checkConnection");
                    localLogger.error() << "Error reinitializing SPEP client. Error was: " << e.what();
                }
            }
        }
    }

    AuthnProcessor *SPEP::getAuthnProcessor()
    {
        checkConnection();
        return mAuthnProcessor;
    }

    AttributeProcessor *SPEP::getAttributeProcessor()
    {
        checkConnection();
        return mAttributeProcessor;
    }

    Metadata *SPEP::getMetadata()
    {
        checkConnection();
        return mMetadata;
    }

    PolicyEnforcementProcessor *SPEP::getPolicyEnforcementProcessor()
    {
        checkConnection();
        return mPolicyEnforcementProcessor;
    }

    SessionGroupCache *SPEP::getSessionGroupCache()
    {
        checkConnection();
        return mSessionGroupCache;
    }

    SessionCache *SPEP::getSessionCache()
    {
        checkConnection();
        return mSessionCache;
    }

    StartupProcessor *SPEP::getStartupProcessor()
    {
        checkConnection();
        return mStartupProcessor;
    }

    saml2::Logger *SPEP::getLogger()
    {
        checkConnection();
        return mLogger;
    }

    Configuration *SPEP::getConfiguration()
    {
        checkConnection();
        return mConfiguration;
    }

    SPEPConfigData *SPEP::getSPEPConfigData()
    {
        checkConnection();
        return mSpepConfigData;
    }

    WSProcessor *SPEP::getWSProcessor()
    {
        checkConnection();
        return mWSProcessor;
    }

    saml2::IdentifierCache *SPEP::getIdentifierCache()
    {
        checkConnection();
        return mIdentifierCache;
    }

    bool SPEP::isStarted()
    {
        checkConnection();
        if (mServiceID.empty()) return false;

        if (mSpepConfigData->disableSPEPStartup()) return true;

        // Return quickly if we have a cached 'started' result.
        if (mIsStarted) return true;

        StartupResult result = mStartupProcessor->allowProcessing();
        switch (result)
        {
        case STARTUP_NONE:
            mStartupProcessor->beginSPEPStart();
            break;

        case STARTUP_WAIT:
            break;

        case STARTUP_ALLOW:
            mIsStarted = true;
            return true;

        case STARTUP_FAIL:
            break;
        }

        return false;
    }

}