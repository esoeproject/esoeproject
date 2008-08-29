/* Copyright 2006, Queensland University of Technology
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
 * Creation Date: Oct 25, 2007
 * 
 * Purpose: 
 */

#ifndef SPEPEXTENSION_H_
#define SPEPEXTENSION_H_

#include <fstream>

#include <winsock2.h>
#include <windows.h>
#include <httpfilt.h>

#include "spep/config/ConfigurationReader.h"
#include "spep/SPEP.h"

#include "ISAPIRequest.h"
#include "StreamLogHandler.h"

namespace spep{ namespace isapi{

	class WSHandler;
	class SSOHandler;
	
	class SPEPExtension
	{
	
		friend class WSHandler;
		friend class SSOHandler;
		
		private:
		spep::SPEP *_spep;
		std::ofstream _stream;
		StreamLogHandler _handler;
		std::string _spepWebappURL;
		std::string _spepSSOURL;
		std::string _spepWebServicesURL;
		std::string _spepAuthzCacheClearURL;
		std::string _spepSingleLogoutURL;
		WSHandler *_wsHandler;
		SSOHandler *_ssoHandler;
		
		public:
		SPEPExtension( spep::ConfigurationReader &configReader, std::string logFile );
		~SPEPExtension();
		
		/**
		 * Performs the SPEP extension logic.
		 */
		DWORD processRequest( ISAPIRequest* request );
		
	};
	
}}

#endif /*SPEPEXTENSION_H_*/
