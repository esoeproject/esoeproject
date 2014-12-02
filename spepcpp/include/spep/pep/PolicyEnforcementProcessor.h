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
 * Creation Date: 25/09/2006
 * 
 * Purpose: 
 */

#ifndef POLICYENFORCEMENTPROCESSOR_H_
#define POLICYENFORCEMENTPROCESSOR_H_

#include <unicode/unistr.h>

#include "spep/Util.h"
#include "spep/pep/Decision.h"
#include "spep/pep/PolicyEnforcementProcessorData.h"
#include "spep/pep/SessionGroupCache.h"
#include "spep/metadata/Metadata.h"
#include "saml2/identifier/IdentifierGenerator.h"
#include "spep/metadata/KeyResolver.h"
#include "saml2/logging/api.h"
#include "saml2/logging/api.h"
#include "spep/sessions/SessionCache.h"
#include "spep/ws/WSClient.h"

#include "saml2/handlers/Marshaller.h"
#include "saml2/handlers/Unmarshaller.h"
#include "saml2/validator/SAMLValidator.h"

#include "saml2/bindings/lxacml-schema-context.hxx"
#include "saml2/bindings/lxacml-schema-grouptarget.hxx"
#include "saml2/bindings/lxacml-schema-saml-protocol.hxx"
#include "saml2/bindings/lxacml-schema-saml-assertion.hxx"
#include "saml2/bindings/saml-schema-assertion-2.0.hxx"
#include "saml2/bindings/esoe-schema-saml-protocol.hxx"

#include <string>


#define ATTRIBUTE_ID L"lxacmlpdp:obligation:cachetargets:updateusercache"
#define OBLIGATION_ID L"lxacmlpdp:obligation:cachetargets"


namespace spep
{
	
    class SPEPEXPORT PolicyEnforcementProcessor
    {
        friend class WSProcessor;

    public:

        PolicyEnforcementProcessor(saml2::Logger *logger, WSClient *wsClient, SessionGroupCache *sessionGroupCache, SessionCache *sessionCache, Metadata *metadata, saml2::IdentifierGenerator *identifierGenerator, saml2::SAMLValidator *samlValidator, KeyResolver *keyResolver, const std::string& schemaPath);
        ~PolicyEnforcementProcessor();

        /**
         * Makes an authorization decision for the given session to access the given
         * resource. This method will attempt to use a local cache to make the decision
         * before generating a request and asking the PDP to make a decision.
         *
         * Cache updates are performed automatically by this method.
         */
        void makeAuthzDecision(PolicyEnforcementProcessorData &data);

        /**
         * Performs a clear on the authorization cache. All cached data for all sessions
         * is immediately flushed.
         */
        DOMDocument* authzCacheClear(middleware::ESOEProtocolSchema::ClearAuthzCacheRequestType *request);

        /**
         * Generates a SAML document to perform an authorization query to the ESOE
         */
        DOMDocument* generateAuthzDecisionQuery(PolicyEnforcementProcessorData &data);

        /**
         * Processes a SAML response and returns an authorization decision from the ESOE
         */
        void processAuthzDecisionStatement(PolicyEnforcementProcessorData &data, saml2::protocol::ResponseType *response);

        /**
         * Processes a set of Obligations from the ESOE and performs cache updates.
         */
        void processObligations(PolicyEnforcementProcessorData &data, middleware::lxacmlSchema::ObligationsType &obligations);

        /**
         * Generates a response to a ClearAuthzCacheRequest, with the given data.
         */
        DOMDocument* generateClearAuthzCacheResponse(const std::wstring &inResponseTo, const std::wstring &statusMessage, const std::wstring &statusCodeValue);


    private:

        saml2::LocalLogger mLocalLogger;
        SessionGroupCache *mSessionGroupCache;
        SessionCache *mSessionCache;
        Metadata *mMetadata;
        saml2::IdentifierGenerator *mIdentifierGenerator;
        saml2::SAMLValidator *mSamlValidator;
        WSClient *mWSClient;

        std::unique_ptr<saml2::Marshaller<middleware::lxacmlSAMLProtocolSchema::LXACMLAuthzDecisionQueryType>> mLxacmlAuthzDecisionQueryMarshaller;
        std::unique_ptr<saml2::Unmarshaller<saml2::protocol::ResponseType>> mResponseUnmarshaller;
        std::unique_ptr<saml2::Unmarshaller<middleware::lxacmlSAMLAssertionSchema::LXACMLAuthzDecisionStatementType>> mLxacmlAuthzDecisionStatementUnmarshaller;
        std::unique_ptr<saml2::Unmarshaller<middleware::ESOEProtocolSchema::ClearAuthzCacheRequestType>> mClearAuthzCacheRequestUnmarshaller;
        std::unique_ptr<saml2::Marshaller<middleware::ESOEProtocolSchema::ClearAuthzCacheResponseType>> mClearAuthzCacheResponseMarshaller;
        std::unique_ptr<saml2::Unmarshaller<middleware::lxacmlGroupTargetSchema::GroupTargetType>> mGroupTargetUnmarshaller;

    };

}

#endif /*POLICYENFORCEMENTPROCESSOR_H_*/
