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
 * Purpose: Implementation of SAML Response validation
 */
 
/* Local Codebase */
#include "constants/VersionConstants.h"
#include "exceptions/IdentifierCacheException.h"
#include "exceptions/InvalidSAMLResponseException.h"
#include "exceptions/InvalidParameterException.h"
#include "validator/SAMLResponseValidator.h"

namespace saml2
{
	SAMLResponseValidator::SAMLResponseValidator(IdentifierCache* identifierCache, int allowedTimeSkew)
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
	
	void SAMLResponseValidator::validate(saml2::protocol::StatusResponseType* response)
	{
		if (response == NULL)
		{
			SAML2LIB_RESPONSE_EX("Submitted response was NULL");
		}

		saml2::protocol::StatusResponseType::IssueInstant::type xmlTime = response->IssueInstant();
		boost::posix_time::ptime curTime = boost::posix_time::microsec_clock::universal_time();
		
		/* Make sure that the time doesn't differ by any more than the amount of clock skew we are allowed. */
		if( ((xmlTime + this->allowedTimeSkew) < curTime ) || ((xmlTime - this->allowedTimeSkew) > curTime ) )
		{
			SAML2LIB_RESPONSE_EX("Response IssueInstant has differed by more then the allowed time skew");
		}

		/* Check the version of the SAML document. */
		if (!(response->Version() == versions::SAML_20))
		{
			SAML2LIB_RESPONSE_EX("Not a SAML 2.0 compliant assertion");
		}
		
		if (response->InResponseTo().present())
		{
			std::string xmlId;
			/* We can be confident using this conversion here as the dateTime field should never contain non ascii characters */
			xmlId = boost::date_time::convert_string_type<wchar_t, char> (response->InResponseTo().get());
			if (!this->identifierCache->containsIdentifier(xmlId))
			{
				SAML2LIB_RESPONSE_EX("Response InResponseTo attribute did not specify an id of a request created locally");
			}
		}
		
		/* Make sure the identifier hasn't already been used and we aren't seeing an attempted replay attack */
		try
		{
			std::string xmlId;
			/* We can be confident using this conversion here as the dateTime field should never contain non ascii characters */
			xmlId = boost::date_time::convert_string_type<wchar_t, char> (response->ID());
			
			this->identifierCache->registerIdentifier(xmlId);
		}
		catch (IdentifierCacheException &exc)
		{
			SAML2LIB_RESPONSE_EX("Response ID has previously been used, possible reply attack");
		}
	}
}
