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

#include "reporting/LocalReportingProcessor.h"
#include "reporting/ReportingProcessor.h"
#include "authn/AuthnProcessor.h"
#include "pep/PolicyEnforcementProcessor.h"

#include "handlers/SAMLDocument.h"

#include "ws/WSProcessorData.h"

namespace spep
{
	
	class WSProcessor
	{
		
		public:
		WSProcessor( ReportingProcessor *reportingProcessor, AuthnProcessor *authnProcessor, PolicyEnforcementProcessor *policyEnforcementProcessor );
		
		void authzCacheClear( WSProcessorData& data );
		void singleLogout( WSProcessorData& data );
		
		private:
		void processSOAPRequest( WSProcessorData& data );
		void createSOAPResponse( WSProcessorData& data );

		LocalReportingProcessor _localReportingProcessor;
		AuthnProcessor *_authnProcessor;
		PolicyEnforcementProcessor *_policyEnforcementProcessor;
		std::string _contentType;
		
	};
	
}
