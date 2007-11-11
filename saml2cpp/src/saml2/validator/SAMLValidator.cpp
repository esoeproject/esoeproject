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
 * Creation Date: 14/02/2007
 * 
 * Purpose: Implementation of gateway to all SAML validation operations supported by saml2lib-cpp
 */
 
/* Local Codebase */
#include "saml2/validator/SAMLValidator.h"

namespace saml2
{
	SAMLValidator::SAMLValidator(saml2::IdentifierCache* identifierCache, int allowedTimeSkew): requestValidator(identifierCache, allowedTimeSkew),
																								authnRequestValidator(identifierCache, allowedTimeSkew),
																								responseValidator(identifierCache, allowedTimeSkew),
																								assertionValidator(identifierCache, allowedTimeSkew)
	{
	}
			
	saml2::SAMLRequestValidator SAMLValidator::getRequestValidator()
	{
		return this->requestValidator;
	}

	saml2::SAMLAuthnRequestValidator SAMLValidator::getAuthnRequestValidator()
	{
		return this->authnRequestValidator;
	}

	saml2::SAMLResponseValidator SAMLValidator::getResponseValidator()
	{
		return this-> responseValidator;
	}

	saml2::SAMLAssertionValidator SAMLValidator::getAssertionValidator()
	{
		return this->assertionValidator;
	}
}
