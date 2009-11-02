#ifndef __GLOBALFIXTURES_H
#define __GLOBALFIXTURES_H

#include <string>
#include <vector>
#include <map>

#include <saml2/resolver/ExternalKeyResolver.h>
#include <saml2/logging/Logger.h>
#include <saml2/validator/SAMLValidator.h>
#include <saml2/identifier/IdentifierGenerator.h>
#include <saml2/identifier/IdentifierCache.h>

#include <spep/attribute/AttributeProcessor.h>
#include <spep/authn/AuthnProcessor.h>
#include <spep/metadata/Metadata.h>
#include <spep/metadata/KeyResolver.h>
#include <spep/pep/PolicyEnforcementProcessor.h>
#include <spep/pep/SessionGroupCache.h>
#include <spep/pep/Decision.h>
#include <spep/sessions/SessionCache.h>
#include <spep/startup/StartupProcessor.h>
#include <spep/ws/SOAPUtil.h>
#include <spep/ws/WSClient.h>
#include <spep/ws/WSProcessor.h>

namespace speptest {
	class GlobalFixtures {
		public:
		GlobalFixtures();
		~GlobalFixtures();
		protected:
		spep::KeyResolver* keyResolver;

		int allowedTimeSkew;
		int metadataInterval;
		int attributeConsumingServiceIndex;
		int assertionConsumerServiceIndex;
		int authzCacheIndex;
		int startupRetryInterval;
		int sessionCacheInterval;
		int sessionCacheTimeout;
		std::string nodeID;
		std::string caBundle;

		spep::Decision defaultPolicyDecision;

		std::string schemaPath;
		std::wstring spepIdentifier;
		std::wstring esoeIdentifier;
		std::string metadataURL;
		std::string ssoRedirect;
		std::string serviceHost;

		std::string keystorePath;
		std::string keystorePassword;
		std::string spepKeyAlias;
		std::string spepKeyPassword;

		std::vector<std::wstring> ipAddresses;
		std::map<std::string,std::string> attributeRenameMap;

		saml2::Logger* logger;
		std::vector<saml2::Handler*> handlers;
		saml2::IdentifierCache *identifierCache;
		saml2::IdentifierGenerator *identifierGenerator;
		saml2::SAMLValidator *samlValidator;
		spep::Metadata *metadata;
		spep::SOAPUtil *soapUtil;
		spep::WSClient *wsClient;
		spep::AttributeProcessor *attributeProcessor;
		spep::SessionCache *sessionCache;
		// spep::SessionCacheThread sessionCacheThread;
		spep::AuthnProcessor *authnProcessor;
		spep::SessionGroupCache *sessionGroupCache;
		spep::PolicyEnforcementProcessor *policyEnforcementProcessor;
		spep::StartupProcessor *startupProcessor;
		spep::WSProcessor *wsProcessor;
	};
}

#endif
