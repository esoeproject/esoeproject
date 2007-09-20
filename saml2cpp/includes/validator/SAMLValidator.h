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
 * Creation Date: 01/02/2007
 * 
 * Purpose: Gateway to all SAML validation operations supported by saml2lib-cpp
 */
 
#ifndef SAMLVALIDATOR_H_
#define SAMLVALIDATOR_H_

/* Local codebase */
#include "identifier/IdentifierCache.h"
#include "validator/SAMLAssertionValidator.h"
#include "validator/SAMLAuthnRequestValidator.h"
#include "validator/SAMLRequestValidator.h"
#include "validator/SAMLResponseValidator.h"

namespace saml2
{
	class SAMLValidator
	{
		public:
			/* Constructor
			 * 
			 * @param identifierCache Application implementation of the IdentiferCache object - caller responsible for memory management
			 * @param allowedTimeSkew Time in seconds an assertion time stamp may differ from UTC
			 */
			SAMLValidator(saml2::IdentifierCache* identifierCache, int allowedTimeSkew);
			
			/**
			 * @return A SAML request validator instance.
			 */
			saml2::SAMLRequestValidator getRequestValidator();
			/**
			 * @return A SAML authn request validator instance.
			 */
			saml2::SAMLAuthnRequestValidator getAuthnRequestValidator();
			/**
			 * @return A SAML response validator instance.
			 */
			saml2::SAMLResponseValidator getResponseValidator();
			/**
			 * @return A SAML assertion validator instance.
			 */
			saml2::SAMLAssertionValidator getAssertionValidator();
			
		private:
			saml2::SAMLRequestValidator requestValidator;
			saml2::SAMLAuthnRequestValidator authnRequestValidator;
			saml2::SAMLResponseValidator responseValidator;
			saml2::SAMLAssertionValidator assertionValidator;
	};
}

#endif /*SAMLVALIDATOR_H_*/
