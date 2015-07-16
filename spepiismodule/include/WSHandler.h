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

#include <string>
#include "spep/ws/SOAPUtil.h"
#include "saml2/logging/locallogger.h"


namespace spep {

class SPEP;

namespace isapi {

class SPEPExtension;
class HttpRequest;
enum class RequestResultStatus;


class WSHandler
{
public:
	WSHandler(SPEP *spep, SPEPExtension *extension);

	//!< Performs the WS processing logic.
	RequestResultStatus processRequest(HttpRequest* request);

private:

	RequestResultStatus authzCacheClear(HttpRequest* request);
	RequestResultStatus singleLogout(HttpRequest* request);
	spep::SOAPDocument readRequestDocument(HttpRequest* request, spep::SOAPUtil::SOAPVersion* soapVersion, std::string& characterEncoding);
	RequestResultStatus sendResponseDocument(HttpRequest* request, spep::SOAPDocument soapResponse, spep::SOAPUtil::SOAPVersion soapVersion, const std::string& characterEncoding);

	SPEP *mSpep;
	SPEPExtension *mSpepExtension;
	std::shared_ptr<saml2::LocalLogger> mLocalLogger;
};

	}
}

#endif /*WSHANDLER_H_*/
