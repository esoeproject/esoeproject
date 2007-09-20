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

#include "esoe-schema-saml-protocol.hxx"

#include "identifier/IdentifierGenerator.h"
#include "handlers/Marshaller.h"
#include "handlers/Unmarshaller.h"
#include "validator/SAMLValidator.h"
#include "SAML2Defs.h"

#include "Util.h"
#include "UnicodeStringConversion.h"
#include "reporting/ReportingProcessor.h"
#include "reporting/LocalReportingProcessor.h"
#include "ws/WSClient.h"
#include "metadata/Metadata.h"
#include "metadata/KeyResolver.h"
#include "startup/StartupProcessor.h"

#include <boost/thread.hpp>

namespace spep
{
	
	/**
	 * Handles operations related to SPEP startup, and ensuring that the SPEP has started
	 */
	class StartupProcessorImpl : public StartupProcessor
	{
	
		private:
		
		StartupProcessorImpl( const StartupProcessorImpl& other );
		StartupProcessorImpl& operator=( const StartupProcessorImpl& other );
		
		class StartupProcessorThread
		{
			private:
			LocalReportingProcessor _localReportingProcessor;
			StartupProcessorImpl *_startupProcessor;
			int _startupRetryInterval;
			
			public:
			StartupProcessorThread( ReportingProcessor *reportingProcessor, StartupProcessorImpl *startupProcessor, int startupRetryInterval );
			StartupProcessorThread( const StartupProcessorThread& other );
			void operator()();
			StartupProcessorThread& operator=( const StartupProcessorThread& other );
		};
		
		LocalReportingProcessor _localReportingProcessor;
		ReportingProcessor *_reportingProcessor;
		
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
		saml2::SAMLDocument buildRequest( const std::wstring &samlID );
		
		/**
		 * Processes a ValidateInitializationResponse document and stores the decision made by the ESOE
		 */
		void processResponse( const saml2::SAMLDocument& responseDocument, const std::wstring &expectedSAMLID );
		
		public:
		
		StartupProcessorImpl( ReportingProcessor *reportingProcessor, WSClient *wsClient, Metadata *metadata, KeyResolver *keyResolver, saml2::IdentifierGenerator *identifierGenerator, saml2::SAMLValidator *samlValidator, std::string schemaPath, std::wstring spepIdentifier, const std::vector<std::wstring>& ipAddresses, std::string nodeID, int authzCacheIndex, int startupRetryInterval );
		
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
