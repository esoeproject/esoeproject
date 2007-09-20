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
 * Purpose: Validates SAML 2.0 assertion operations to specification
 */
 
#ifndef SAMLASSERTIONVALIDATOR_H_
#define SAMLASSERTIONVALIDATOR_H_

/* XSD */
#include "saml-schema-assertion-2.0.hxx"

/* Local Codebase */
#include "identifier/IdentifierCache.h"

namespace saml2
{
	class SAMLAssertionValidator
	{
		public:
			/* Constructor
			 * 
			 * @param identifierCache Application implementation of the IdentiferCache object - caller responsible for memory management
			 * @param allowedTimeSkew Time in seconds an assertion time stamp may differ from UTC
			 */
			SAMLAssertionValidator(IdentifierCache* identifierCache, int allowedTimeSkew);
			
			/**
			 * Validate a SAML 2.0 assertion.
			 * 
			 * Any invalid request will throw an exception state, valid documents do not cause any response.
			 * 
			 * @param assertion An unmarshalled assertion object.
			 * @throws InvalidSAMLAssertionException if there is an error validating the assertion.
			 */
			void validate(saml2::assertion::AssertionType* assertion);
			
		private:
			boost::posix_time::time_duration allowedTimeSkew;
			saml2::IdentifierCache* identifierCache;
	};
}

#endif /*SAMLASSERTIONVALIDATOR_H_*/
