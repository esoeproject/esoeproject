/* Copyright 2006-2007, Queensland University of Technology
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
 * Author: Shaun Mangelsdorf
 * Creation Date: 12/03/2007
 * 
 * Purpose: 
 */

#ifndef POLICYENFORCEMENTPROCESSORDATA_H_
#define POLICYENFORCEMENTPROCESSORDATA_H_

#include <unicode/unistr.h>

#include <string>

#include "saml2/handlers/SAMLDocument.h"

#include "spep/Util.h"
#include "spep/pep/Decision.h"

namespace spep
{
	
    class SPEPEXPORT PolicyEnforcementProcessorData
    {

    public:
        /**
         * Sets the SAML request document. The meaning of this document is context dependant, and
         * may also be used to return a SAML request document from a method.
         */
        void setRequestDocument(const saml2::SAMLDocument& requestDocument);
        const saml2::SAMLDocument& getRequestDocument() const;

        /**
         * Sets the SAML response document. The meaning of this document is context dependant, and
         * may also be used to return a SAML response document from a method.
         */
        void setResponseDocument(const saml2::SAMLDocument& responseDocument);
        const saml2::SAMLDocument& getResponseDocument() const;

        /**
         * Sets the ESOE session identifier to be used when making the authorization decision.
         * This is effectively the Subject of the authorization request
         */
        void setESOESessionID(const std::wstring& esoeSessionID);
        std::wstring getESOESessionID() const;

        /**
         * Sets the resource being accessed, and therefore the resource on which the
         * authorization decision should be made.
         */
        void setResource(const UnicodeString& resource);
        UnicodeString getResource() const;

        /**
         * Sets the decision that was reached by the PEP.
         */
        void setDecision(Decision decision);
        Decision getDecision() const;

    private:
        saml2::SAMLDocument mRequestDocument;
        saml2::SAMLDocument mResponseDocument;
        std::wstring mEsoeSessionID;
        UnicodeString mResource;
        Decision mDecision;

    };

}

#endif /*POLICYENFORCEMENTPROCESSORDATA_H_*/
