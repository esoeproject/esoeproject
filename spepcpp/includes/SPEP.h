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

#include "config/ConfigurationReader.h"

#include "identifier/IdentifierCache.h"
#include "identifier/IdentifierGenerator.h"
#include "validator/SAMLValidator.h"

#include "ipc/Socket.h"
#include "attribute/AttributeProcessor.h"
#include "authn/AuthnProcessor.h"
#include "metadata/Metadata.h"
#include "metadata/KeyResolver.h"
#include "pep/PolicyEnforcementProcessor.h"
#include "pep/SessionGroupCache.h"
#include "sessions/SessionCache.h"
#include "sessions/SessionCacheThread.h"
#include "startup/StartupProcessor.h"
#include "reporting/ReportingProcessor.h"
#include "config/Configuration.h"
#include "config/SPEPConfigData.h"
#include "ws/WSProcessor.h"
#include "Util.h"

namespace spep
{
	
	class SPEP
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
		
		static SPEP* initialize( ConfigurationReader &configuration );
		
		static SPEP* initializeClient( int spepDaemonPort, std::vector<spep::Handler*> handlers );
		static SPEP* initializeServer( ConfigurationReader &configuration, std::vector<spep::Handler*> handlers );
		
		AuthnProcessor *getAuthnProcessor();
		AttributeProcessor *getAttributeProcessor();
		Metadata *getMetadata();
		PolicyEnforcementProcessor *getPolicyEnforcementProcessor();
		SessionGroupCache *getSessionGroupCache();
		SessionCache *getSessionCache();
		StartupProcessor *getStartupProcessor();
		ReportingProcessor *getReportingProcessor();
		Configuration *getConfiguration();
		SPEPConfigData *getSPEPConfigData();
		WSProcessor *getWSProcessor();
		saml2::IdentifierCache *getIdentifierCache();
		
		bool isStarted();
		
		private:
			
		SPEP( SPEP& other );
		SPEP& operator=( SPEP& other );
		
		void destroy();
		void reinitializeClient();
		void checkConnection();
		
		SPEPMode _mode;
		bool _isStarted;
		long _connectionSequenceID;
		
		Mutex _mutex;
		
		saml2::IdentifierCache *_identifierCache;
		saml2::IdentifierGenerator *_identifierGenerator;
		saml2::SAMLValidator *_samlValidator;
		
		spep::ipc::ClientSocket *_clientSocket;
		
		AuthnProcessor *_authnProcessor;
		AttributeProcessor *_attributeProcessor;
		Metadata *_metadata;
		//KeyResolver *_keyResolver;
		PolicyEnforcementProcessor *_policyEnforcementProcessor;
		SessionGroupCache *_sessionGroupCache;
		SessionCache *_sessionCache;
		SessionCacheThread *_sessionCacheThread;
		StartupProcessor *_startupProcessor;
		ReportingProcessor *_reportingProcessor;
		Configuration *_configuration;
		SPEPConfigData *_spepConfigData;
		WSClient *_wsClient;
		WSProcessor *_wsProcessor;
		
	};
	
}

#endif /*SPEP_H_*/
