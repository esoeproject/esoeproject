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
	
		private:
		
		StartupProcessorImpl( const StartupProcessorImpl& other );
		StartupProcessorImpl& operator=( const StartupProcessorImpl& other );
		
		class StartupProcessorThread
		{
			private:
			saml2::Logger *_logger;
			saml2::LocalLogger _localLogger;
			StartupProcessorImpl *_startupProcessor;
			int _startupRetryInterval;
			
			public:
			StartupProcessorThread( saml2::Logger *logger, StartupProcessorImpl *startupProcessor, int startupRetryInterval );
			StartupProcessorThread( const StartupProcessorThread& other );
			void operator()();
			//StartupProcessorThread& operator=( const StartupProcessorThread& other );
		};
		
		saml2::LocalLogger _localLogger;
		saml2::Logger *_logger;
		
		WSClient *_wsClient;
		Metadata *_metadata;
		KeyResolver *_keyResolver;
		
		saml2::SAMLValidator *_samlValidator;
			
		mutable Mutex _startupResultMutex;
		spep::StartupResult _startupResult;
		
		std::wstring _spepIdentifier;
		std::vector<std::wstring> _ipAddresses;
		std::string _nodeID;
		int _authzCacheIndex;
		int _startupRetryInterval;
		
		saml2::Marshaller<middleware::ESOEProtocolSchema::ValidateInitializationRequestType> *_validateInitializationRequestMarshaller;
		saml2::Unmarshaller<middleware::ESOEProtocolSchema::ValidateInitializationResponseType> *_validateInitializationResponseUnmarshaller;
		saml2::IdentifierGenerator *_identifierGenerator;
		
		boost::thread_group _threadGroup;
		
		/**
		 * Builds a ValidateInitializationRequest document to be sent to the ESOE when requesting startup
		 */
		DOMDocument* buildRequest( const std::wstring &samlID );
		
		/**
		 * Processes a ValidateInitializationResponse document and stores the decision made by the ESOE
		 */
		void processResponse( middleware::ESOEProtocolSchema::ValidateInitializationResponseType* response, const std::wstring &expectedSAMLID );
		
		public:
		
		StartupProcessorImpl( saml2::Logger *logger, WSClient *wsClient, Metadata *metadata, KeyResolver *keyResolver, saml2::IdentifierGenerator *identifierGenerator, saml2::SAMLValidator *samlValidator, std::string schemaPath, std::wstring spepIdentifier, const std::vector<std::wstring>& ipAddresses, std::string nodeID, int authzCacheIndex, int startupRetryInterval );
		
		virtual ~StartupProcessorImpl();
		
		/**
		 * Returns a result indicating whether the SPEP should be allowed to process requests
		 */
		virtual StartupResult allowProcessing();
		
		/**
		 * Sets the result of SPEP startup in the startup processor.
		 */
		void setStartupResult( StartupResult startupResult );
		
		/**
		 * Instructs the startup processor to begin the startup request. This method will return after creating
		 * a thread, and there is no guarantee that any other action has occurred upon returning.
		 */
		virtual void beginSPEPStart();
		
		/**
		 * Performs the startup action. This method will block until it has complete. Should never be called
		 * directly, except by beginSPEPStart()
		 */
		void doStartup();
		
	};
	
}

#endif /* STARTUPPROCESSORIMPL_H_ */
