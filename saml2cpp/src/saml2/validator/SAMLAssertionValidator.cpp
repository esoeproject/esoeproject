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
 * Purpose: Implementation of SAML Assertion validation
 */
 
 /* Boost */
#include <boost/date_time/posix_time/ptime.hpp>
#include <boost/date_time/posix_time/posix_time_duration.hpp>
#include <boost/date_time/posix_time/time_formatters.hpp>

/* STL */
#include <string>

/* Local Codebase */
#include "saml2/constants/VersionConstants.h"
#include "saml2/exceptions/IdentifierCacheException.h"
#include "saml2/exceptions/InvalidSAMLAssertionException.h"
#include "saml2/exceptions/InvalidParameterException.h"
#include "saml2/validator/SAMLAssertionValidator.h"

namespace saml2
{
	SAMLAssertionValidator::SAMLAssertionValidator(IdentifierCache* identifierCache, int allowedTimeSkew)
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
	
	void SAMLAssertionValidator::validate(saml2::assertion::AssertionType* assertion)
	{
		if ( assertion == NULL )
		{
			SAML2LIB_ASSERTION_EX("Supplied assertion was NULL");
		}

		if ( !(assertion->Version() == versions::SAML_20) )
		{
			SAML2LIB_ASSERTION_EX("Not a SAML 2.0 compliant assertion");
		}

		if ( !(assertion->Subject().present()) )
		// TODO Fix this commented bit.. it doesn't work for some reason.
		//|| (assertion->Subject().get().NameID() == NULL && assertion->Subject().get().EncryptedID() == NULL) )
		{
			SAML2LIB_ASSERTION_EX("Assertion subject was not presented at all or did not have embedded nameID or encryptedID");
		}

		if ( assertion->Subject().get().SubjectConfirmation().empty() )
		{
			SAML2LIB_ASSERTION_EX("Assertion subject confirmation data was not presented");
		}
		
		saml2::assertion::SubjectType::SubjectConfirmation_sequence& confirmations = assertion->Subject().get().SubjectConfirmation();
		
		/* Processes subjectconfirmation fields, at present we only look at Subject confirmation data */
		saml2::assertion::SubjectType::SubjectConfirmation_iterator i = confirmations.begin();
		while ( i != confirmations.end() )
		{
			saml2::assertion::SubjectConfirmationType::SubjectConfirmationData_optional& confirmationData = i->SubjectConfirmationData();
			if ( confirmationData.present() )
			{
				if( confirmationData->NotOnOrAfter().present() )
				{
					saml2::assertion::SubjectConfirmationDataType::NotOnOrAfter_type xmlTime = confirmationData->NotOnOrAfter().get();
					boost::posix_time::ptime curTime = boost::posix_time::microsec_clock::universal_time();
					
					/* Make sure assertion hasn't expired, making sure we account for the allowed time skew */
					if( (xmlTime + this->allowedTimeSkew) < curTime )
					{
						SAML2LIB_ASSERTION_EX("Assertion not on or after timestamp is outside of allowable time skew");
					}
				}
				if( confirmationData->InResponseTo().present() )
				{
					std::string xmlId;
					
					/* We can be confident using this conversion here as the dateTime field should never contain non ascii characters */
					xmlId = boost::date_time::convert_string_type<wchar_t, char> ( confirmationData->InResponseTo().get() );
					
					if( !this->identifierCache->containsIdentifier(xmlId))
					{
						SAML2LIB_ASSERTION_EX("Assertion subject confirmation ID was not generated locally. Possible attack.");
					}
				}
			}
			i++;
		}
		
		if(assertion->Conditions().present())
		{
			/* Ensure we have an audience restriction */
			// TODO Broken. Fix.
			//if( assertion->Conditions().get().AudienceRestriction().empty() )
			//{
			//	SAML2LIB_ASSERTION_EX("Assertion did not have audience restriction details set by issuer");
			//}
		}
		else
		{
			SAML2LIB_ASSERTION_EX("Assertion did not carry conditions from issuer for its acceptance");
		}
		
		/* Make sure the identifier hasn't already been used and we aren't seeing an attempted replay attack */
		try
		{
			std::string xmlId;
			/* We can be confident using this conversion here as the dateTime field should never contain non ascii characters */
			xmlId = boost::date_time::convert_string_type<wchar_t, char> (assertion->ID());
			
			this->identifierCache->registerIdentifier(xmlId);
		}
		catch (IdentifierCacheException &exc)
		{
			SAML2LIB_ASSERTION_EX("Assertion ID has already been used. Possible replay attack");
		}
	}
}
