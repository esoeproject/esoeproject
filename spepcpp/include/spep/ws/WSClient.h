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

#include "spep/Util.h"
#include "spep/UnicodeStringConversion.h"
#include "spep/ws/WSProcessorData.h"
#include "spep/ws/SOAPUtil.h"

#include "saml2/SAML2Defs.h"

#include <curl/curl.h>

#include <string>

#include "saml2/logging/api.h"
#include "saml2/logging/api.h"

#define WSCLIENT_CHARACTER_ENCODING "UTF-16"

namespace spep
{
	
	class SPEPEXPORT WSClient
	{
		
		private:
			
		class RawSOAPDocument
		{
			
			public:
			RawSOAPDocument();
			RawSOAPDocument( const RawSOAPDocument& other );
			
			~RawSOAPDocument();
			
			RawSOAPDocument& operator=( const RawSOAPDocument& other );
			
			unsigned char *data;
			long len;
			long pos;
			
		};
			
		saml2::LocalLogger _localLogger;
		std::string _caBundle;
		SOAPUtil *_soapUtil;
		Mutex _wsClientMutex;

		void doSOAPRequest(WSProcessorData& data, const std::string& endpoint);

		/** 
		 * Callback function to be called by cURL. The userp pointer MUST be a RawSOAPDocument. 
		 * See cURL documentation for more info. 
		 */
		static std::size_t curlWriteCallback( void *buffer, std::size_t size, std::size_t nmemb, void *userp );
		static std::size_t curlReadCallback( void *buffer, std::size_t size, std::size_t nmemb, void *userp );
		static int debugCallback( CURL *curl, curl_infotype info, char *msg, std::size_t len, void *userp );
		
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
		WSClient( saml2::Logger *logger, std::string caBundle, SOAPUtil *soapUtil );
		~WSClient();
		
		template <typename Res>
		Res* doWSCall(const std::string& endpoint, DOMDocument* requestDocument, saml2::Unmarshaller<Res> *resUnmarshaller, SOAPUtil::SOAPVersion soapVersion = SOAPUtil::SOAP11)
		{
			//ScopedLock(_wsClientMutex);
			WSProcessorData data;
			
			data.setSOAPRequestDocument( _soapUtil->wrapObjectInSOAP( requestDocument->getDocumentElement(), WSCLIENT_CHARACTER_ENCODING, soapVersion ) );
			_localLogger.debug() << "Created SOAP request bound for endpoint " << endpoint << ". About to perform SOAP action.";
			_localLogger.trace() << UnicodeStringConversion::toString( UnicodeStringConversion::toUnicodeString( data.getSOAPRequestDocument() ) );
			
			data.setCharacterEncoding( "UTF-16" );
			this->doSOAPRequest( data, endpoint );
			
			_localLogger.debug() << "Performed SOAP action at " << endpoint << ". About to process response.";
			_localLogger.trace() << UnicodeStringConversion::toString( UnicodeStringConversion::toUnicodeString( data.getSOAPResponseDocument() ) );
			return( _soapUtil->unwrapObjectFromSOAP( resUnmarshaller, data.getSOAPResponseDocument(), soapVersion ) );
		}
		
	};
	
}

#endif /*_WSCLIENT_H*/
