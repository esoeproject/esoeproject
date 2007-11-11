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

#ifndef SSOHANDLER_H_
#define SSOHANDLER_H_

#include "spep/SPEP.h"

#include "APRDefinitions.h"

#include "httpd.h"
#include "http_config.h"
#include "http_core.h"
#include "http_log.h"
#include "http_main.h"
#include "http_protocol.h"
#include "http_request.h"
#include "util_script.h"
//#include "http_connection.h"

namespace spep { namespace apache {
	
	class SSOHandler
	{
	
		private:
		spep::SPEP *_spep;
		int handleSSOGetRequest( request_rec *req );
		int handleSSOPostRequest( request_rec *req );
		std::string buildAuthnRequestDocument( apr_pool_t *pool, std::string &base64RedirectURL );
		apr_status_t parseGetRequestQueryString( request_rec *req, apr_table_t *queryParams );
		
		SSOHandler( const SSOHandler& other );
		SSOHandler& operator=( const SSOHandler& other );
		
		public:
		SSOHandler( spep::SPEP *spep );
		int handleRequest( request_rec *req );
		
	};
	
} }

#endif /*SSOHANDLER_H_*/
