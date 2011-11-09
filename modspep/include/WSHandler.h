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
 * Creation Date: Jul 30, 2007
 * 
 */

#ifndef WSHANDLER_H_
#define WSHANDLER_H_

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

namespace spep { namespace apache {

	class WSHandler
	{
		
	private:
		SPEP *_spep;

		typedef boost::shared_ptr<saml2::LocalLogger> LocalLoggerPtr;
		LocalLoggerPtr m_localLogger;
		
		int authzCacheClear( request_rec *req );
		int singleLogout( request_rec *req );
		
		spep::SOAPDocument readRequestDocument( request_rec *req, spep::SOAPUtil::SOAPVersion *soapVersion, std::string &characterEncoding );
		void sendResponseDocument( request_rec *req, spep::SOAPDocument soapResponse );
		
		WSHandler( const WSHandler& other );
		WSHandler& operator=( const WSHandler& other );
		
		public:
		WSHandler( SPEP *spep );
		int handleRequest( request_rec *req );
		
	};

} }

#endif /*WSHANDLER_H_*/
