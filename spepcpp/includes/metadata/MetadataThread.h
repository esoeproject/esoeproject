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
 * Creation Date: 27/02/2007
 * 
 * Purpose: 
 */

#ifndef METADATATHREAD_H_
#define METADATATHREAD_H_

#include <curl/curl.h>
#include <openssl/evp.h>

#include <iostream>

#include "SAML2Defs.h"

#include "reporting/ReportingProcessor.h"
#include "reporting/LocalReportingProcessor.h"
//#include "metadata/impl/MetadataImpl.h"

#include "handlers/Unmarshaller.h"
#include "handlers/impl/UnmarshallerImpl.h"

#include "saml-schema-metadata-2.0.hxx"
#include "lxacml-schema-metadata.hxx"
#include "spepstartup-schema-saml-metadata.hxx"

namespace spep
{
	
	class MetadataImpl;
	
	class MetadataThread
	{
		
		friend class MetadataImpl;
		
		private:
		

		MetadataThread( const MetadataThread& other );
		MetadataThread& operator=( const MetadataThread& other );
		
		class RawMetadata
		{
			public:
			
			/**
			 * @param ctx The (reusable!) hash context. Will not be freed on destruction.
			 */
			RawMetadata( EVP_MD_CTX *ctx );
			RawMetadata( const RawMetadata &rhs );
			~RawMetadata();
			
			RawMetadata& operator=( const RawMetadata &rhs );
			
			EVP_MD_CTX *hashContext;
			char *data;
			std::size_t len;
			std::string hashValue;
			bool failed;
		};
		
		class ThreadHandler
		{
			private:
			ThreadHandler& operator=( ThreadHandler& other );
			
			MetadataThread *_metadataThread;
			LocalReportingProcessor _localReportingProcessor;
			
			public:
			ThreadHandler( MetadataThread *metadataThread, ReportingProcessor *reportingProcessor );
			/** Boost likes to clone the objects given to it.. so we define a copy constructor */
			ThreadHandler( const ThreadHandler& other );
			/** Operator to be called by boost threads. Does not return. */
			void operator()();
		};
		
		/** 
		 * Callback function to be called by cURL. The userp pointer MUST be a RawMetadata. 
		 * See cURL documentation for more info. 
		 */
		static std::size_t curlCallback( void *buffer, std::size_t size, std::size_t nmemb, void *userp );
		
		ReportingProcessor *_reportingProcessor;
		LocalReportingProcessor _localReportingProcessor;
		MetadataImpl *_metadata;
		int _interval;
		const EVP_MD *_hashType;
		EVP_MD_CTX _hashContext;
		CURL *_curl;
		saml2::Unmarshaller<saml2::metadata::EntitiesDescriptorType> *_metadataUnmarshaller;
		bool _die;
		
		public:

		/**
		 * Constructs a new MetadataThread object
		 * 
		 * While this method by itself is thread safe, care must be taken to ensure that
		 * if another thread will be initializing a cURL handle at the same time,
		 * the curl_global_init function has been called prior to constructing this object.
		 * 
		 * See http://curl.haxx.se/libcurl/c/curl_easy_init.html for details.
		 * 
		 * @param reportingProcessor The global reporting processor.
		 * @param metadata The metadata instance.
		 * @param interval Interval between metadata updates, in seconds.
		 */
		MetadataThread( ReportingProcessor *reportingProcessor, MetadataImpl *metadata, std::string schemaPath, int interval );
		/** Returns an object to be used by boost threads. */
		ThreadHandler getThreadHandler();
		/** Perform the metadata retrieval operation. Invoked by ThreadHandler::operator() */ 
		void doGetMetadata();
		RawMetadata getRawMetadata( std::string metadataURL );
		
	};
	
}

#endif /*METADATATHREAD_H_*/
