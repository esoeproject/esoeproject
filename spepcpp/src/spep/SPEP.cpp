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

#include "spep/UnicodeStringConversion.h"

// Define a "short sleep" to be 100 milliseconds.
#define SHORT_SLEEP_NANOSECONDS (20*1000*1000)
#define LONGER_SLEEP_NANOSECONDS (500*1000*1000)

#define ONE_SECOND_NANOSECONDS (1000*1000*1000)

spep::SPEP* spep::SPEP::initializeClient( int spepDaemonPort )
{
	spep::SPEP *spep = new spep::SPEP;

	spep->_mode = SPEP_MODE_CLIENT;
	spep->_isStarted = false;

#ifdef WIN32
	spep->_socketPool = new spep::ipc::ClientSocketPool( spepDaemonPort, 20 );
#else
	spep->_socketPool = new spep::ipc::ClientSocketPool( spepDaemonPort, 1 );
#endif //WIN32

	spep->_logger = new spep::ipc::LoggerProxy(spep->_socketPool);

	saml2::LocalLogger localLogger(spep->_logger, "spep::SPEP");

	spep::ipc::ConfigurationProxy configurationProxy( spep->_socketPool );
	spep->_spepConfigData = new SPEPConfigData( configurationProxy.getSPEPConfigData() );

	spep->_startupProcessor = new spep::ipc::StartupProcessorProxy( spep->_socketPool );

	// This will get the background thread firing to let the SPEP startup request occur.

	if( !spep->_spepConfigData->disableSPEPStartup() )
		spep->_startupProcessor->allowProcessing();

	spep->_identifierCache = new spep::ipc::IdentifierCacheProxy( spep->_socketPool );
	spep->_identifierGenerator = new saml2::IdentifierGenerator( spep->_identifierCache );
	spep->_samlValidator = new saml2::SAMLValidator( spep->_identifierCache, spep->_spepConfigData->getAllowedTimeSkew() );

	spep->_metadata = new spep::ipc::MetadataProxy( spep->_socketPool );

	spep->_soapUtil = new SOAPUtil( spep->_logger, spep->_spepConfigData->getSchemaPath() );

	spep->_wsClient = new WSClient( spep->_logger, spep->_spepConfigData->getCABundle(), spep->_soapUtil );

	spep->_attributeProcessor = new AttributeProcessor( spep->_logger, spep->_metadata, spep->_spepConfigData->getKeyResolver(), spep->_identifierGenerator, spep->_wsClient, spep->_samlValidator, spep->_spepConfigData->getSchemaPath(), spep->_spepConfigData->getAttributeRenameMap() );
	spep->_sessionCache = new spep::ipc::SessionCacheProxy( spep->_socketPool );

	spep->_authnProcessor = new AuthnProcessor( spep->_logger, spep->_attributeProcessor, spep->_metadata, spep->_sessionCache, spep->_samlValidator, spep->_identifierGenerator, spep->_spepConfigData->getKeyResolver(), spep->_spepConfigData->getSPEPIdentifier(), spep->_spepConfigData->getSSORedirect(), spep->_spepConfigData->getServiceHost(), spep->_spepConfigData->getSchemaPath(), spep->_spepConfigData->getAttributeConsumingServiceIndex(), spep->_spepConfigData->getAssertionConsumerServiceIndex() );

	spep->_sessionGroupCache = new spep::ipc::SessionGroupCacheProxy( spep->_socketPool );
	spep->_policyEnforcementProcessor = new PolicyEnforcementProcessor( spep->_logger, spep->_wsClient, spep->_sessionGroupCache, spep->_sessionCache, spep->_metadata, spep->_identifierGenerator, spep->_samlValidator, spep->_spepConfigData->getKeyResolver(), spep->_spepConfigData->getSchemaPath() );

	spep->_wsProcessor = new WSProcessor( spep->_logger, spep->_authnProcessor, spep->_policyEnforcementProcessor, spep->_soapUtil );

	spep->_serviceID = spep->_socketPool->getServiceID();

	return spep;
}

void spep::SPEP::reinitializeClient()
{
	// Reinitialize everything that relies on something from the config data.
	// Things that rely only on the client socket should still work without being reinstantiated.

	if( this->_spepConfigData != NULL )
	{
		delete this->_spepConfigData;
	}
	spep::ipc::ConfigurationProxy configurationProxy( this->_socketPool );
	this->_spepConfigData = new SPEPConfigData( configurationProxy.getSPEPConfigData() );

	if( this->_soapUtil != NULL )
	{
		delete this->_soapUtil;
	}
	this->_soapUtil = new SOAPUtil( this->_logger, this->_spepConfigData->getSchemaPath() );

	if( this->_samlValidator != NULL )
	{
		delete this->_samlValidator;
	}
	this->_samlValidator = new saml2::SAMLValidator( this->_identifierCache, this->_spepConfigData->getAllowedTimeSkew() );

	if( this->_attributeProcessor != NULL )
	{
		delete this->_attributeProcessor;
	}
	this->_attributeProcessor = new AttributeProcessor( this->_logger, this->_metadata, this->_spepConfigData->getKeyResolver(), this->_identifierGenerator, this->_wsClient, this->_samlValidator, this->_spepConfigData->getSchemaPath(), this->_spepConfigData->getAttributeRenameMap() );

	if( this->_authnProcessor != NULL )
	{
		delete this->_authnProcessor;
	}
	this->_authnProcessor = new AuthnProcessor( this->_logger, this->_attributeProcessor, this->_metadata, this->_sessionCache, this->_samlValidator, this->_identifierGenerator, this->_spepConfigData->getKeyResolver(), this->_spepConfigData->getSPEPIdentifier(), this->_spepConfigData->getSSORedirect(), this->_spepConfigData->getServiceHost(), this->_spepConfigData->getSchemaPath(), this->_spepConfigData->getAttributeConsumingServiceIndex(), this->_spepConfigData->getAssertionConsumerServiceIndex() );

	if( this->_policyEnforcementProcessor != NULL )
	{
		delete this->_policyEnforcementProcessor;
	}
	this->_policyEnforcementProcessor = new PolicyEnforcementProcessor( this->_logger, this->_wsClient, this->_sessionGroupCache, this->_sessionCache, this->_metadata, this->_identifierGenerator, this->_samlValidator, this->_spepConfigData->getKeyResolver(), this->_spepConfigData->getSchemaPath() );

	if( this->_wsClient != NULL )
	{
		delete this->_wsClient;
	}
	this->_wsClient = new WSClient( this->_logger, this->_spepConfigData->getCABundle(), this->_soapUtil );

	if( this->_wsProcessor != NULL )
	{
		delete this->_wsProcessor;
	}
	this->_wsProcessor = new WSProcessor( this->_logger, this->_authnProcessor, this->_policyEnforcementProcessor, this->_soapUtil );

	this->_serviceID = this->_socketPool->getServiceID();
	// Can't assume we're still connected to a "started" SPEP
	this->_isStarted = false;
}

spep::SPEP* spep::SPEP::initializeServer( spep::ConfigurationReader &configurationReader, std::vector<saml2::Handler*> handlers )
{
	spep::SPEP *spep = new spep::SPEP;

	spep->_mode = SPEP_MODE_SERVER;
	spep->_isStarted = false;

	spep->_logger = new saml2::Logger();

	for( std::vector<saml2::Handler*>::iterator handlerIterator = handlers.begin();
		handlerIterator != handlers.end();  ++handlerIterator )
	{
		spep->_logger->registerHandler( *handlerIterator );
	}

	saml2::LocalLogger localLogger(spep->_logger, "spep::SPEP");
	localLogger.debug() << "Beginning to initialize server-side SPEP components.";

	spep->_configuration = new ConfigurationImpl( configurationReader );
	spep->_spepConfigData = new SPEPConfigData( spep->_configuration->getSPEPConfigData() );

	int allowedTimeSkew = configurationReader.getIntegerValue( CONFIGURATION_ALLOWEDTIMESKEW );
	int metadataInterval = configurationReader.getIntegerValue( CONFIGURATION_METADATAINTERVAL );
	int attributeConsumingServiceIndex = configurationReader.getIntegerValue( CONFIGURATION_ATTRIBUTECONSUMINGSERVICEINDEX );
	int assertionConsumerServiceIndex = configurationReader.getIntegerValue( CONFIGURATION_ASSERTIONCONSUMERSERVICEINDEX );
	int authzCacheIndex = configurationReader.getIntegerValue( CONFIGURATION_AUTHZCACHEINDEX );
	int startupRetryInterval = configurationReader.getIntegerValue( CONFIGURATION_STARTUPRETRYINTERVAL );
	int sessionCacheInterval = configurationReader.getIntegerValue( CONFIGURATION_SESSIONCACHEINTERVAL );
	int sessionCacheTimeout = configurationReader.getIntegerValue( CONFIGURATION_SESSIONCACHETIMEOUT );
	std::string nodeID( configurationReader.getStringValue( CONFIGURATION_NODEIDENTIFIER ) );
	std::string caBundle( configurationReader.getStringValue( CONFIGURATION_CABUNDLE, std::string() ) );

	Decision defaultPolicyDecision( configurationReader.getStringValue( CONFIGURATION_DEFAULTPOLICYDECISION ) );

	std::string schemaPath( configurationReader.getStringValue( CONFIGURATION_SCHEMAPATH ) );
	std::wstring spepIdentifier( UnicodeStringConversion::toWString( configurationReader.getStringValue( CONFIGURATION_SPEPIDENTIFIER ) ) );
	std::wstring esoeIdentifier( UnicodeStringConversion::toWString( configurationReader.getStringValue( CONFIGURATION_ESOEIDENTIFIER ) ) );
	std::string metadataURL( configurationReader.getStringValue( CONFIGURATION_METADATAURL ) );

	spep->_identifierCache = new saml2::IdentifierCache();
	spep->_identifierGenerator = new saml2::IdentifierGenerator( spep->_identifierCache );
	spep->_samlValidator = new saml2::SAMLValidator( spep->_identifierCache, allowedTimeSkew );

	spep->_metadata = new MetadataImpl( spep->_logger, schemaPath, spepIdentifier, esoeIdentifier, metadataURL, caBundle, spep->_spepConfigData->getKeyResolver(), assertionConsumerServiceIndex, metadataInterval );

	spep->_soapUtil = new SOAPUtil( spep->_logger, schemaPath );

	spep->_wsClient = new WSClient( spep->_logger, caBundle, spep->_soapUtil );

	spep->_attributeProcessor = new AttributeProcessor( spep->_logger, spep->_metadata, spep->_spepConfigData->getKeyResolver(), spep->_identifierGenerator, spep->_wsClient, spep->_samlValidator, schemaPath, spep->_spepConfigData->getAttributeRenameMap() );
	spep->_sessionCache = new SessionCacheImpl( spep->_logger );
	spep->_sessionCacheThread = new SessionCacheThread( spep->_logger, spep->_sessionCache, sessionCacheTimeout, sessionCacheInterval );
	spep->_authnProcessor = new AuthnProcessor( spep->_logger, spep->_attributeProcessor, spep->_metadata, spep->_sessionCache, spep->_samlValidator, spep->_identifierGenerator, spep->_spepConfigData->getKeyResolver(), spepIdentifier, spep->_spepConfigData->getSSORedirect(), spep->_spepConfigData->getServiceHost(), schemaPath, attributeConsumingServiceIndex, assertionConsumerServiceIndex );
	spep->_sessionGroupCache = new SessionGroupCacheImpl( spep->_logger, defaultPolicyDecision );
	spep->_policyEnforcementProcessor = new PolicyEnforcementProcessor( spep->_logger, spep->_wsClient, spep->_sessionGroupCache, spep->_sessionCache, spep->_metadata, spep->_identifierGenerator, spep->_samlValidator, spep->_spepConfigData->getKeyResolver(), schemaPath );
	spep->_startupProcessor = new StartupProcessorImpl( spep->_logger, spep->_wsClient, spep->_metadata, spep->_spepConfigData->getKeyResolver(), spep->_identifierGenerator, spep->_samlValidator, schemaPath, spepIdentifier, spep->_spepConfigData->getIPAddresses(), nodeID, authzCacheIndex, startupRetryInterval );
	spep->_wsProcessor = new WSProcessor( spep->_logger, spep->_authnProcessor, spep->_policyEnforcementProcessor, spep->_soapUtil );

	return spep;
}

spep::SPEP* spep::SPEP::initializeStandalone( spep::ConfigurationReader& configReader, std::vector<saml2::Handler*> handlers )
{
	SPEP* spep = SPEP::initializeServer( configReader, handlers );

	saml2::LocalLogger localLogger(spep->_logger, "spep::SPEP");

	localLogger.debug() << "Server-side SPEP components initialized. Switching to standalone mode.";
	spep->_mode = SPEP_MODE_STANDALONE;

	return spep;
}

spep::SPEP::~SPEP()
{
	if( this->_identifierCache != NULL ) delete this->_identifierCache;
	if( this->_identifierGenerator != NULL ) delete this->_identifierGenerator;
	if( this->_samlValidator != NULL ) delete this->_samlValidator;
	if( this->_socketPool != NULL ) delete this->_socketPool;
	if( this->_authnProcessor != NULL ) delete this->_authnProcessor;
	if( this->_attributeProcessor != NULL ) delete this->_attributeProcessor;
	if( this->_metadata != NULL ) delete this->_metadata;
	if( this->_policyEnforcementProcessor != NULL ) delete this->_policyEnforcementProcessor;
	if( this->_sessionGroupCache != NULL ) delete this->_sessionGroupCache;
	if( this->_sessionCache != NULL ) delete this->_sessionCache;
	if( this->_sessionCacheThread != NULL ) delete this->_sessionCacheThread;
	if( this->_startupProcessor != NULL ) delete this->_startupProcessor;
	if( this->_logger != NULL ) delete this->_logger;
	if( this->_configuration != NULL ) delete this->_configuration;
	if( this->_spepConfigData != NULL ) delete this->_spepConfigData;
	if( this->_soapUtil != NULL ) delete this->_soapUtil;
	if( this->_wsClient != NULL ) delete this->_wsClient;
	if( this->_wsProcessor != NULL ) delete this->_wsProcessor;
}

spep::SPEP::SPEP()
:
/* Initialize all these to null so that we don't seg fault by trying to delete them. */
_isStarted( false ),
_serviceID(),
_mutex(),
_identifierCache( NULL ),
_identifierGenerator( NULL ),
_samlValidator( NULL ),
_socketPool( NULL ),
_authnProcessor( NULL ),
_attributeProcessor( NULL ),
_metadata( NULL ),
_policyEnforcementProcessor( NULL ),
_sessionGroupCache( NULL ),
_sessionCache( NULL ),
_sessionCacheThread( NULL ),
_startupProcessor( NULL ),
_logger( NULL ),
_configuration( NULL ),
_spepConfigData( NULL ),
_soapUtil( NULL ),
_wsClient( NULL ),
_wsProcessor( NULL )
{
}

void spep::SPEP::checkConnection()
{
	if( _mode == SPEP_MODE_CLIENT )
	{
		ScopedLock lock( _mutex );

		if( _serviceID != this->_socketPool->getServiceID() )
		{
			this->reinitializeClient();
		}
	}
}

spep::AuthnProcessor *spep::SPEP::getAuthnProcessor()
{
	this->checkConnection();
	return this->_authnProcessor;
}

spep::AttributeProcessor *spep::SPEP::getAttributeProcessor()
{
	this->checkConnection();
	return this->_attributeProcessor;
}

spep::Metadata *spep::SPEP::getMetadata()
{
	this->checkConnection();
	return this->_metadata;
}

spep::PolicyEnforcementProcessor *spep::SPEP::getPolicyEnforcementProcessor()
{
	this->checkConnection();
	return this->_policyEnforcementProcessor;
}

spep::SessionGroupCache *spep::SPEP::getSessionGroupCache()
{
	this->checkConnection();
	return this->_sessionGroupCache;
}

spep::SessionCache *spep::SPEP::getSessionCache()
{
	this->checkConnection();
	return this->_sessionCache;
}

spep::StartupProcessor *spep::SPEP::getStartupProcessor()
{
	this->checkConnection();
	return this->_startupProcessor;
}

saml2::Logger *spep::SPEP::getLogger()
{
	this->checkConnection();
	return this->_logger;
}

spep::Configuration *spep::SPEP::getConfiguration()
{
	this->checkConnection();
	return this->_configuration;
}

spep::SPEPConfigData *spep::SPEP::getSPEPConfigData()
{
	this->checkConnection();
	return this->_spepConfigData;
}

spep::WSProcessor *spep::SPEP::getWSProcessor()
{
	this->checkConnection();
	return this->_wsProcessor;
}

saml2::IdentifierCache *spep::SPEP::getIdentifierCache()
{
	this->checkConnection();
	return this->_identifierCache;
}

bool spep::SPEP::isStarted()
{
	this->checkConnection();

	if( this->_spepConfigData->disableSPEPStartup() ) return true;

	// Return quickly if we have a cached 'started' result.
	if( this->_isStarted ) return true;

	StartupResult result = this->_startupProcessor->allowProcessing();
	switch (result)
	{
		case STARTUP_NONE:
		this->_startupProcessor->beginSPEPStart();
		break;

		case STARTUP_WAIT:
		break;

		case STARTUP_ALLOW:
		this->_isStarted = true;
		return true;

		case STARTUP_FAIL:
		break;
	}

	return false;
}
