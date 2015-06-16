/*
 * Copyright 2008, Queensland University of Technology
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
 * Creation Date: 04/01/2008
 * 
 * Purpose: 
 */

#ifndef WSHANDLER_H_
#define WSHANDLER_H_

#include "HttpRequest.h"

#include "spep/SPEP.h"
#include "spep/ws/SOAPUtil.h"

#include <string>

#include <winsock2.h>
#include <windows.h>

namespace spep {
    namespace isapi {

        class SPEPExtension;

        class WSHandler
        {
        public:
            WSHandler(SPEP *spep, SPEPExtension *extension);

            /**
             * Performs the WS processing logic.
             */
			DWORD processRequest(HttpRequest* request);
			DWORD authzCacheClear(HttpRequest* request);
			DWORD singleLogout(HttpRequest* request);

        private:
            SPEP *mSpep;
            SPEPExtension *mSpepExtension;

			spep::SOAPDocument readRequestDocument(HttpRequest* request, spep::SOAPUtil::SOAPVersion* soapVersion, std::string& characterEncoding);
			DWORD sendResponseDocument(HttpRequest* request, spep::SOAPDocument soapResponse, spep::SOAPUtil::SOAPVersion soapVersion, const std::string& characterEncoding);
        };

    }
}

#endif /*WSHANDLER_H_*/
