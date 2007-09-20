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
 * Purpose: Implementation of SAML authentication validation
 */
 
/* Local Codebase */
#include "constants/VersionConstants.h"
#include "exceptions/IdentifierCacheException.h"
#include "exceptions/InvalidSAMLAuthnRequestException.h"
#include "exceptions/InvalidParameterException.h"
#include "validator/SAMLAuthnRequestValidator.h"

namespace saml2
{
	SAMLAuthnRequestValidator::SAMLAuthnRequestValidator(IdentifierCache* identifierCache, int allowedTimeSkew)
	{
		
		if( identifierCache == NULL )
		{
			SAML2LIB_INVPARAM_EX("Supplied identifier cache was NULL");
		}
		
		if(allowedTimeSkew > INT_MAX / 1000)
		{
			SAML2LIB_INVPARAM_EX("Supplied value of allowed time skew was too large, skew MUST be in seconds");
		}
		
		this->allowedTimeSkew = boost::posix_time::milliseconds( allowedTimeSkew * 1000 );
		this->identifierCache = identifierCache;
	}
	
	void SAMLAuthnRequestValidator::validate(saml2::protocol::AuthnRequestType* authnRequest)
	{
		if( !authnRequest->NameIDPolicy().present())
		{
			SAML2LIB_AUTHNREQ_EX("NameIDPolicy was not presented for this request");
		}
		
		if (!authnRequest->AssertionConsumerServiceIndex().present())
		{
			SAML2LIB_AUTHNREQ_EX("AssertionConsumerServiceIndex was not presented for this request");
		}
	}
}
