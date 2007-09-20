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

#ifndef _WSCLIENT_H
#define _WSCLIENT_H

#include "reporting/LocalReportingProcessor.h"
#include "reporting/ReportingProcessor.h"

#include "ws/WSProcessorData.h"

#include "SAML2Defs.h"

#include <curl/curl.h>

#include <string>

namespace spep
{
	
	class WSClient
	{
		
		private:
			
		class RawSOAPDocument
		{
			
			public:
			RawSOAPDocument();
			RawSOAPDocument( const RawSOAPDocument& other );
			
			~RawSOAPDocument();
			
			RawSOAPDocument& operator=( const RawSOAPDocument& other );
			
			char *data;
			std::size_t len;
			std::size_t pos;
			
		};
			
		LocalReportingProcessor _localReportingProcessor;
		CURL *_curl;
		
		void createSOAPRequest( WSProcessorData& data );
		void createSAMLResponse( WSProcessorData& data );
		
		void doSOAPRequest( WSProcessorData& data, std::string endpoint );
		void doWSCall( WSProcessorData& data, std::string endpoint );
		/** 
		 * Callback function to be called by cURL. The userp pointer MUST be a RawSOAPDocument. 
		 * See cURL documentation for more info. 
		 */
		static std::size_t curlWriteCallback( void *buffer, std::size_t size, std::size_t nmemb, void *userp );
		static std::size_t curlReadCallback( void *buffer, std::size_t size, std::size_t nmemb, void *userp );
		
		public:
		/**
		 * Constructs a new WSClient object.
		 * 
		 * While this method by itself is thread safe, care must be taken to ensure that
		 * if another thread will be initializing a cURL handle at the same time,
		 * the curl_global_init function has been called prior to constructing this object.
		 * 
		 * See http://curl.haxx.se/libcurl/c/curl_easy_init.html for details.
		 */
		WSClient( ReportingProcessor *reportingProcessor );
		virtual ~WSClient();
		virtual void attributeAuthority( WSProcessorData& data, std::string endpoint );
		virtual void policyDecisionPoint( WSProcessorData& data, std::string endpoint );
		virtual void spepStartup( WSProcessorData& data, std::string endpoint );
		
	};
	
}

#endif /*_WSCLIENT_H*/
