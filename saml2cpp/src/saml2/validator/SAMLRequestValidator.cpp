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
 * Purpose: Implementation of SAML Request validation
 */
 
/* Local Codebase */
#include "saml2/SAML2Defs.h"
#include "saml2/constants/VersionConstants.h"
#include "saml2/exceptions/IdentifierCacheException.h"
#include "saml2/exceptions/InvalidSAMLRequestException.h"
#include "saml2/exceptions/InvalidParameterException.h"
#include "saml2/validator/SAMLRequestValidator.h"

/* Boost */
#include <boost/date_time/posix_time/ptime.hpp>
#include <boost/date_time/posix_time/posix_time_duration.hpp>
#include <boost/date_time/posix_time/time_formatters.hpp>

/* STL */
#include <string>

/* XSD */
#include "saml2/bindings/saml-schema-protocol-2.0.hxx"

#include "limits.h"

namespace saml2
{
	/**
	 * @param identifierCache An implementation of IdentifierCache through which SAML ID's can be verified against for uniqueness
	 * @param allowedTimeSkew Time skew in seconds thats documents may slip
	 */
	SAMLRequestValidator::SAMLRequestValidator(IdentifierCache* identifierCache, int allowedTimeSkew)
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

	void SAMLRequestValidator::validate(saml2::protocol::RequestAbstractType* request)
	{
		if( request == NULL )
		{
			SAML2LIB_REQUEST_EX("Submitted request was NULL");
		}
				
		saml2::protocol::AuthnRequestType::IssueInstant_type xmlTime = request->IssueInstant();
		boost::posix_time::ptime curTime = boost::posix_time::microsec_clock::universal_time();
		
		/* Make sure that the time doesn't differ by any more than the amount of clock skew we are allowed. */
		if( ((xmlTime + this->allowedTimeSkew) < curTime ) || ((xmlTime - this->allowedTimeSkew) > curTime ) )
		{
			SAML2LIB_REQUEST_EX("Request IssueInstant has differed by more then the allowed time skew");
		}
		
		/* Ensure that an issuer has been provided for this request */
		if( !request->Issuer().present() )
		{
			SAML2LIB_REQUEST_EX("Request did not have Issuer identification element present");
		}
		
		/* Check the version of the SAML document. */
		if (!(request->Version() == versions::SAML_20))
		{
			SAML2LIB_REQUEST_EX("Not a SAML 2.0 compliant assertion");
		}
		
		/* Make sure the identifier hasn't already been used and we aren't seeing an attempted replay attack */
		try
		{
			std::string xmlId;
			/* We can be confident using this conversion here as the dateTime field should never contain non ascii characters */
			xmlId = boost::date_time::convert_string_type<wchar_t, char> (request->ID());
			
			this->identifierCache->registerIdentifier(xmlId);
		}
		catch (IdentifierCacheException &exc)
		{
			SAML2LIB_REQUEST_EX("Request ID has previously been used, possible reply attack");
		}
	}
}
