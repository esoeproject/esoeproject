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
 * Creation Date: 08/01/2007
 * 
 * Purpose: Makes attribute queries over web service calls and interprets 
 * 		attributes passed between IDP and SPEP
 */

#ifndef ATTRIBUTEPROCESSOR_H_
#define ATTRIBUTEPROCESSOR_H_

#include "spep/Util.h"
#include "spep/attribute/AttributeProcessorData.h"
#include "spep/sessions/PrincipalSession.h"
#include "spep/metadata/Metadata.h"
#include "spep/metadata/KeyResolver.h"

#include "saml2/handlers/Marshaller.h"
#include "saml2/handlers/Unmarshaller.h"
#include "saml2/validator/SAMLValidator.h"
#include "saml2/identifier/IdentifierGenerator.h"
#include "spep/ws/WSClient.h"

// For CygWin std::wstring workaround.
#include "saml2/SAML2Defs.h"

#include "saml2/bindings/saml-schema-assertion-2.0.hxx"
#include "saml2/bindings/saml-schema-protocol-2.0.hxx"

#include "saml2/logging/api.h"
#include "saml2/logging/api.h"

namespace spep
{
	
	/**
	 * @class AttributeProcessor
	 * @brief Makes SAML requests to resolve attributes for newly created sessions.
	 */
	class SPEPEXPORT AttributeProcessor
	{
	public:
		
		typedef std::vector<saml2::assertion::AttributeStatementType*> AttributeStatementPointerList;
		
		/**
		 * Constructor
		 */
		AttributeProcessor(saml2::Logger *logger, Metadata *metadata, KeyResolver *keyResolver, saml2::IdentifierGenerator *identifierGenerator, spep::WSClient *wsClient, saml2::SAMLValidator *samlValidator, const std::string& schemaPath, const std::map<std::string,std::string>& attributeRenameMap);
		
		/**
		 * Destructor
		 */
		~AttributeProcessor();
		
		/**
		 * Performs attribute processing for the principal session and updates the session cache
		 * with the attribute data.
		 * @param principalSession The principal session that has already been authenticated.
		 */
		void doAttributeProcessing(PrincipalSession &principalSession);
		
		/**
		 * Internal method for building an attribute query for a particular principal session.
		 * The SAML ID provided will be the ID attribute for the AttributeQuery element.
		 */
		DOMDocument* buildAttributeQuery(PrincipalSession &principalSession, const std::wstring &samlID);
		
		/**
		 * Processes an attribute response and populates the session with attribute data.
		 * The session is NOT cleared of attributes before the response is processed.
		 * 
		 * The SAML ID provided is the ID of the AttributeQuery that caused this response to
		 * be generated. If the InResponseTo value of the Response does not match this, an
		 * exception will be thrown.
		 */
		void processAttributeResponse(saml2::protocol::ResponseType *response, PrincipalSession &principalSession, const std::wstring &samlID);
		
		/**
		 * Processes a list of attribute statements and adds attribute data to the session.
		 * We are passing a list of pointers so that copy constructors don't need to be invoked.
		 * 
		 * This is being done explicitly in a separate method so that we can easily support
		 * the push model for attribute processing later if need be.
		 */
		void processAttributeStatements(AttributeStatementPointerList &attributeStatements, PrincipalSession &principalSession);
		
	private:
		
		/// Disable copy constructor by declaring it privately.
		AttributeProcessor(const AttributeProcessor& other);
		/// Disable assignment by declaring operator= privately.
		AttributeProcessor& operator=(const AttributeProcessor& other);

		saml2::LocalLogger mLocalLogger;
		Metadata *mMetadata;
		KeyResolver *mKeyResolver;
		saml2::IdentifierGenerator *mIdentifierGenerator;
		WSClient *mWSClient;
		saml2::SAMLValidator *mSamlValidator;
		std::unique_ptr<saml2::Marshaller<saml2::protocol::AttributeQueryType>> mAttributeQueryMarshaller;
		std::unique_ptr<saml2::Unmarshaller<saml2::protocol::ResponseType>> mResponseUnmarshaller;
		std::map<UnicodeString, UnicodeString> mAttributeRenameMap;
		
	};
	
}

#endif /* ATTRIBUTEPROCESSOR_H_ */
