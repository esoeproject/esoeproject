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
 * Purpose: Validates SAML 2.0 request operations to specification
 */
 
#ifndef SAMLREQUESTVALIDATOR_H_
#define SAMLREQUESTVALIDATOR_H_

/* Boost */
#include <boost/date_time/posix_time/ptime.hpp>
#include <boost/date_time/posix_time/posix_time_duration.hpp>

/* XSD */
#include "saml-schema-protocol-2.0.hxx"

/* Local Codebase */
#include "identifier/IdentifierCache.h"

namespace saml2
{
	class SAMLRequestValidator
	{
		public:
			/* Constructor
			 * 
			 * @param identifierCache Application implementation of the IdentiferCache object - caller responsible for memory management
			 * @param allowedTimeSkew Time in seconds an assertion time stamp may differ from UTC
			 */
			SAMLRequestValidator(saml2::IdentifierCache* identifierCache, int allowedTimeSkew);
			
			/**
			 * Validate a SAML 2.0 request. 
			 * 
			 * Any invalid request will throw an exception state, valid documents do not cause any response
			 * (NB: XML Cryptography verification with unmarshaller/marshaller is expected to have been utilised before calling this function)
			 * 
			 * @param request An unmarshalled request object
			 * @throws InvalidSAMLRequestException if there is an error validating the request.
			 */
			void validate(saml2::protocol::RequestAbstractType* request);
			
		private:
			boost::posix_time::time_duration allowedTimeSkew;
			saml2::IdentifierCache* identifierCache;
	};
}

#endif /*SAMLREQUESTVALIDATOR_H_*/
