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
 * Purpose: Creates and interprets authentication related SAML documents, and
 * 		manipulates the session cache accordingly. 
 */
 
#ifndef AUTHNPROCESSOR_H_
#define AUTHNPROCESSOR_H_
 
#include "spep/Util.h"
#include "spep/sessions/PrincipalSession.h"
#include "spep/sessions/SessionCache.h"
#include "saml2/logging/api.h"
#include "saml2/logging/api.h"
#include "spep/attribute/AttributeProcessor.h"
#include "spep/metadata/Metadata.h"
#include "spep/metadata/KeyResolver.h"
#include "saml2/validator/SAMLValidator.h"
#include "saml2/identifier/IdentifierGenerator.h"

#include "spep/authn/AuthnProcessorData.h"

#include "saml2/handlers/Marshaller.h"
#include "saml2/handlers/Unmarshaller.h"
#include "saml2/bindings/saml-schema-assertion-2.0.hxx"
#include "saml2/bindings/saml-schema-protocol-2.0.hxx"

namespace spep
{
	
	/**
	 * Creates SAML request documents and processes SAML response documents related to
	 * authentication. Also is responsible for creating sessions in the session cache,
	 * as well as initiating attribute resolution for a new authenticated session.
	 */
	class SPEPEXPORT AuthnProcessor
	{
		friend class WSProcessor;
		
		public:
		
		/**
		 * Constructor
		 * @param schemaPath A std::string representation of the absolute path to schema files on the file system
		 */
		AuthnProcessor(saml2::Logger *logger, AttributeProcessor *attributeProcessor, Metadata *metadata, SessionCache *sessionCache, saml2::SAMLValidator *samlValidator, saml2::IdentifierGenerator *identifierGenerator, KeyResolver *keyStoreResolver, const std::wstring& spepIdentifier, const std::string& ssoRedirect, const std::string& serviceHost, const std::string& schemaPath, int attributeConsumingServiceIndex, int assertionConsumerServiceIndex);
		
		/**
		 * Destructor
		 */
		~AuthnProcessor();
		
		/**
		 * Processes an SAML response document, searching for valid AuthnStatements contained within
		 * @param data The data object containing a SAML response document
		 */
		void processAuthnResponse(AuthnProcessorData &data);
		
		/**
		 * Generates a SAML AuthnRequest document to be used in establishing a session. An unauthenticated
		 * session is established as part of this. If the request URL in the data object is populated, it will
		 * be stored so that the user agent can be redirected back to their original request location after
		 * authentication is finished.
		 * @param data The data object to contain the SAML request document.
		 */
		void generateAuthnRequest(AuthnProcessorData &data);
		
		/**
		 * Verifies that the session identifier corresponds to an active, valid, authenticated session.
		 * @param sessionID The local session identifier to search for.
		 * @return The session object for the local session identifier.
		 */
		PrincipalSession verifySession(const std::string &sessionID);
		
		/**
		 * Processes a SAML request document an attempts to perform a logout operation from the document.
		 * @param data The data object containing a SAML request document
		 */
		DOMDocument* logoutPrincipal(saml2::protocol::LogoutRequestType *logoutRequest);
		
		private:
		
		/**
		 * Processes an AuthnStatement. If valid, the unauthenticated session is terminated and an 
		 * authenticated session (PrincipalSession) created.
		 */
		std::pair<bool, std::string> processAuthnStatement(const saml2::assertion::AuthnStatementType&, const saml2::assertion::AssertionType&, const std::string& remoteAddress, bool disableAttributeQuery);
		DOMDocument* generateLogoutResponse(const std::wstring &statusCodeValue, const std::wstring &statusMessage, const std::wstring &inResponseTo = L"");
		static std::string hostnameFromURL(const std::string& url);
		
		saml2::LocalLogger mLocalLogger;
		AttributeProcessor *mAttributeProcessor;
		Metadata *mMetadata;
		SessionCache *mSessionCache;
		saml2::SAMLValidator *mSamlValidator;
		saml2::IdentifierGenerator *mIdentifierGenerator;
		KeyResolver *mKeyResolver;
		std::wstring mSpepIdentifier;
		std::string mSSORedirect;
		std::string mServiceHost;
		int mAttributeConsumingServiceIndex;
		int mAssertionConsumerServiceIndex;
		
		std::unique_ptr<saml2::Marshaller<saml2::protocol::AuthnRequestType>> mAuthnRequestMarshaller;
		std::unique_ptr<saml2::Unmarshaller<saml2::protocol::ResponseType>> mResponseUnmarshaller;
		
		std::unique_ptr<saml2::Marshaller<saml2::protocol::ResponseType>> mLogoutResponseMarshaller;
		std::unique_ptr<saml2::Unmarshaller<saml2::protocol::LogoutRequestType>> mLogoutRequestUnmarshaller;
		
	};
	
}

#endif /*AUTHNPROCESSOR_H_*/
