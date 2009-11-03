#include "speptest/GlobalFixtures.h"

#include <saml2/logging/Logger.h>
#include <saml2/logging/Handler.h>

#include <spep/metadata/impl/MetadataImpl.h>
#include <spep/sessions/impl/SessionCacheImpl.h>
#include <spep/startup/impl/StartupProcessorImpl.h>
#include <spep/pep/impl/SessionGroupCacheImpl.h>

using namespace spep;

namespace speptest {
	GlobalFixtures::GlobalFixtures()
	:
	MockWeb(),
	allowedTimeSkew(30),
	metadataInterval(1),
	attributeConsumingServiceIndex(1),
	assertionConsumerServiceIndex(1),
	authzCacheIndex(1),
	startupRetryInterval(20),
	sessionCacheInterval(120),
	sessionCacheTimeout(600),
	nodeID("1"),
	caBundle(),
	defaultPolicyDecision(Decision::DENY),
	schemaPath("./schema"),
	spepIdentifier(L"http://test-spep.example.com"),
	esoeIdentifier(L"http://test-esoe.example.com"),
	metadataURL(getBaseURL() + "/esoemanager/metadata/internal"),
	ssoRedirect("/spep/sso?redirectURL=%s"),
	serviceHost("http://test-spep.example.com"),
	keystorePath("./data/keystore.ks"),
	keystorePassword("testks"),
	spepKeyAlias("spep"),
	spepKeyPassword("testkey")
	{
		// This code mostly duplicated from spep::SPEP::initializeServer()

		logger = new saml2::Logger();

		for( std::vector<saml2::Handler*>::iterator handlerIterator = handlers.begin();
			handlerIterator != handlers.end();  ++handlerIterator )
		{
			logger->registerHandler( *handlerIterator );
		}

		saml2::LocalLogger localLogger(logger, "speptest::GlobalFixtures");
		localLogger.debug() << "Beginning to initialize testing SPEP components.";

		ipAddresses.push_back(L"127.0.0.1");

		keyResolver = new KeyResolver( keystorePath, keystorePassword, spepKeyAlias, spepKeyPassword );

		identifierCache = new saml2::IdentifierCache();
		identifierGenerator = new saml2::IdentifierGenerator( identifierCache );
		samlValidator = new saml2::SAMLValidator( identifierCache, allowedTimeSkew );

		metadata = new MetadataImpl( logger, schemaPath, spepIdentifier, esoeIdentifier, metadataURL, caBundle, keyResolver, assertionConsumerServiceIndex, metadataInterval );

		soapUtil = new SOAPUtil( logger, schemaPath );

		wsClient = new WSClient( logger, caBundle, soapUtil );

		attributeProcessor = new AttributeProcessor( logger, metadata, keyResolver, identifierGenerator, wsClient, samlValidator, schemaPath, attributeRenameMap );
		sessionCache = new SessionCacheImpl( logger );
		//sessionCacheThread = new SessionCacheThread( logger, sessionCache, sessionCacheTimeout, sessionCacheInterval );
		authnProcessor = new AuthnProcessor( logger, attributeProcessor, metadata, sessionCache, samlValidator, identifierGenerator, keyResolver, spepIdentifier, ssoRedirect, serviceHost, schemaPath, attributeConsumingServiceIndex, assertionConsumerServiceIndex );
		sessionGroupCache = new SessionGroupCacheImpl( logger, defaultPolicyDecision );
		policyEnforcementProcessor = new PolicyEnforcementProcessor( logger, wsClient, sessionGroupCache, sessionCache, metadata, identifierGenerator, samlValidator, keyResolver, schemaPath );
		startupProcessor = new StartupProcessorImpl( logger, wsClient, metadata, keyResolver, identifierGenerator, samlValidator, schemaPath, spepIdentifier, ipAddresses, nodeID, authzCacheIndex, startupRetryInterval );
		wsProcessor = new WSProcessor( logger, authnProcessor, policyEnforcementProcessor, soapUtil );
	}

	GlobalFixtures::~GlobalFixtures(){}
}
