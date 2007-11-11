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
 * Purpose: Validates SAML 2.0 authentication operations to specification
 */
 
#ifndef SAMLAUTHNVALIDATOR_H_
#define SAMLAUTHNVALIDATOR_H_

#include "saml2/SAML2Defs.h"

/* XSD */
#include "saml2/bindings/saml-schema-protocol-2.0.hxx"

/* Local Codebase */
#include "saml2/identifier/IdentifierCache.h"
#include "saml2/bindings/saml-schema-protocol-2.0.hxx"

namespace saml2
{
	class SAML2EXPORT SAMLAuthnRequestValidator
	{
		public:
			/* Constructor
			 * 
			 * @param identifierCache Application implementation of the IdentiferCache object - caller responsible for memory management
			 * @param allowedTimeSkew Time in seconds an assertion time stamp may differ from UTC
			 */
			SAMLAuthnRequestValidator(IdentifierCache* identifierCache, int allowedTimeSkew);
			
			/**
			 * Validate a SAML 2.0 authn request. 
			 * 
			 * Any invalid request will throw an exception state, valid documents do not cause any response.
			 * (NB: XML Cryptography verification with unmarshaller/marshaller is expected to have been utilised before calling this function)
			 * 
			 * @param request An unmarshalled request object.
			 * @throws InvalidSAMLRequestException if there is an error validating the request.
			 */
			void validate(saml2::protocol::AuthnRequestType* authnRequest);
			
		private:
			boost::posix_time::time_duration allowedTimeSkew;
			saml2::IdentifierCache* identifierCache;
	};
}

#endif /*SAMLAUTHNVALIDATOR_H_*/
