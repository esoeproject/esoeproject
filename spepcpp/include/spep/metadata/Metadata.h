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

#ifndef METADATA_H_
#define METADATA_H_

#include <string>
#include "saml2/SAML2Defs.h"

#include "unicode/utypes.h"
#include "unicode/unistr.h"

#include "spep/Util.h"
#include "spep/metadata/KeyResolver.h"
#include "saml2/logging/api.h"

#include "saml2/handlers/MetadataOutput.h"
#include "saml2/resolver/ExternalKeyResolver.h"

namespace spep
{
	
	class SPEPEXPORT Metadata : public saml2::ExternalKeyResolver
	{
		
		public:
		virtual ~Metadata(){}
		virtual const std::wstring getSPEPIdentifier() const = 0;
		virtual const std::wstring getESOEIdentifier() const = 0;
		virtual const std::string getSingleSignOnEndpoint() const = 0;
		virtual const std::string getSingleLogoutEndpoint() const = 0;
		virtual const std::string getAttributeServiceEndpoint() const = 0;
		virtual const std::string getAuthzServiceEndpoint() const = 0;
		virtual const std::string getSPEPStartupServiceEndpoint() const = 0;
		virtual XSECCryptoKey *resolveKey (DSIGKeyInfoList *lst) = 0;
		virtual saml2::KeyData resolveKey (std::string keyName) = 0;
		
	};
	
}

#endif /* METADATA_H_ */
