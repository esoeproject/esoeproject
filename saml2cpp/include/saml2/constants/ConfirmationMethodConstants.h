/* 
 * Copyright 2006-2007, Queensland University of Technology
 * Licensed under the Apache License, Version 2.0 (the= L"License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy of 
 * the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an= L"AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations under 
 * the License.
 * 
 * Author: Bradley Beddoes
 * Creation Date: 14/02/2007
 * 
 * Purpose: Stores references to all SAML 2.0 confirmation method identifiers values
 * Docuemnt: saml-profiles-2.0-os.pdf, 3.0
 */
 
#ifndef CONFIRMATIONMETHODCONSTANTS_H_
#define CONFIRMATIONMETHODCONSTANTS_H_
 
 #include "saml2/SAML2Defs.h"
 
 /* STL */
 #include <string>
 
 namespace saml2
 {
 	namespace confirmation
 	{
		/**
		 * One or more <ds:KeyInfo> elements MUST be present within the <SubjectConfirmationData> element. An xsi:type
		 * attribute MAY be present in the <SubjectConfirmationData> element and, if present, MUST be set to
		 * saml:KeyInfoConfirmationDataType (the namespace prefix is arbitrary but must reference the SAML assertion
		 * namespace).
		 */
 		SAML2CONSTANT const std::wstring HOLDER_OF_KEY= L"urn:oasis:names:tc:SAML:2.0:cm:holder-of-key";
	
		/**
		 * Indicates that no other information is available about the context of use of the assertion. The relying party
		 * SHOULD utilize other means to determine if it should process the assertion further, subject to optional
		 * constraints on confirmation using the attributes that MAY be present in the <SubjectConfirmationData> element, as
		 * defined by [SAMLCore].
		 */
 		SAML2CONSTANT const std::wstring SENDR_VOUCHES= L"urn:oasis:names:tc:SAML:2.0:cm:sender-vouches";

		/**
		 * The subject of the assertion is the bearer of the assertion, subject to optional constraints on confirmation
		 * using the attributes that MAY be present in the <SubjectConfirmationData> element, as defined by [SAMLCore].
		 */
 		SAML2CONSTANT const std::wstring BEARER= L"urn:oasis:names:tc:SAML:2.0:cm:bearer";
	}
 }
 
 #endif
