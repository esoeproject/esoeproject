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

#ifndef SSOHANDLER_H_
#define SSOHANDLER_H_

#include <string>

#include "HttpRequest.h"
#include "SPEPExtension.h"

#include "spep/SPEP.h"

namespace spep {
    namespace isapi {

        class SSOHandler
        {
        public:
            SSOHandler(spep::SPEP* spep, SPEPExtension* spepExtension);
			DWORD handleRequest(HttpRequest* request);

        private:

            SSOHandler(const SSOHandler& other);
            SSOHandler& operator=(const SSOHandler& other);

			DWORD handleSSOGetRequest(HttpRequest* request);
			DWORD handleSSOPostRequest(HttpRequest* request);
			std::string buildAuthnRequestDocument(HttpRequest* request, const std::string& base64RedirectURL, const std::string& baseRequestURL);
			BOOL parseGetRequestQueryString(HttpRequest* request);
            
            spep::SPEP *mSpep;
            SPEPExtension *mSpepExtension;
        };

    }
}

#endif /*SSOHANDLER_H_*/
