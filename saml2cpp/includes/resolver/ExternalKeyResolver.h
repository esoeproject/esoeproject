/*
 * Copyright 2006-2007, Queensland University of Technology
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
 * Author: Bradley Beddoes
 * Creation Date: 19/12/2006
 * 
 * Purpose: Resolves cryptographic keys for the SAML2lib-cpp library from remote sources
 */

#ifndef EXTERNALKEYRESOLVER_
#define EXTERNALKEYRESOLVER_

/* XML Security */
#include <xsec/enc/XSECKeyInfoResolver.hpp>
#include <xsec/enc/XSECCryptoKey.hpp>

namespace saml2
{
	class ExternalKeyResolver : public XSECKeyInfoResolver
	{
		public:
			virtual ~ExternalKeyResolver(){}
	};
}

#endif /*EXTERNALKEYRESOLVER_*/
