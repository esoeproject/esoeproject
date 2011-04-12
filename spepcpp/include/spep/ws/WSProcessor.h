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

#include "spep/Util.h"
#include "saml2/logging/api.h"
#include "saml2/logging/api.h"
#include "spep/authn/AuthnProcessor.h"
#include "spep/pep/PolicyEnforcementProcessor.h"

#include "saml2/handlers/SAMLDocument.h"

#include "spep/ws/WSProcessorData.h"

namespace spep
{
	
	class SPEPEXPORT WSProcessor
	{
		
		public:
		WSProcessor( saml2::Logger *logger, AuthnProcessor *authnProcessor, PolicyEnforcementProcessor *policyEnforcementProcessor, SOAPUtil *soapUtil );
		
		SOAPDocument authzCacheClear( SOAPDocument requestDocument, SOAPUtil::SOAPVersion soapVersion, const std::string& characterEncoding );
		SOAPDocument singleLogout( SOAPDocument requestDocument, SOAPUtil::SOAPVersion soapVersion, const std::string& characterEncoding );
		
		private:
		void processSOAPRequest( WSProcessorData& data );
		void createSOAPResponse( WSProcessorData& data );

		saml2::LocalLogger _localLogger;
		AuthnProcessor *_authnProcessor;
		PolicyEnforcementProcessor *_policyEnforcementProcessor;
		SOAPUtil *_soapUtil;
		
	};
	
}
