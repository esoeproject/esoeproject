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
 * Creation Date: Jul 4, 2007
 * 
 * Purpose: 
 */

#ifndef REQUESTPARAMETERS_H_
#define REQUESTPARAMETERS_H_

#include "APRDefinitions.h"

#include "handlers/SAMLDocument.h"

#include <memory>

#include "httpd.h"

namespace spep { namespace apache {
	
	typedef saml2::ManagedDocument<char,std::size_t> HTTPContentDocument;
	
	class RequestParameters
	{
	
		private:
		apr_table_t *_params;
		request_rec *_req;
		
		RequestParameters( const RequestParameters& other );
		RequestParameters& operator=( const RequestParameters& other );
		
		/**
		 * Reads the content from the request. Note that it appends a semi-colon to the body.
		 * This is to get around a bug in libapreq2 that causes it to choke on large bodies
		 * of text without a terminating semi-colon.
		 */
		HTTPContentDocument readHTTPRequestContent();
		
		public:
		RequestParameters( request_rec *req );
		const char *operator[]( const char *name );
		void sendResponseDocument( const char *document, std::size_t length );
		
	};
	
} }

#endif /*REQUESTPARAMETERS_H_*/
