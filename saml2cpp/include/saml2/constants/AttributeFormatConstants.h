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
 * Purpose: Stores references to all SAML 2.0 attribute format values. 
 * Document: saml-core-2.0-os.pdf, 8.2
 */
 
#ifndef ATTRIBUTEFORMATCONSTANTS_H_
#define ATTRIBUTEFORMATCONSTANTS_H_
 
 #include "saml2/SAML2Defs.h"
 
/* STL */
 #include <string>
 
 namespace saml2
 {
 	namespace attribute
 	{
		/**
		 * The interpretation of the attribute name is left to individual implementations.
		 */
 		SAML2CONSTANT const std::wstring UNSPECIFIED = L"urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified";
		
		/**
		 * The attribute name follows the convention for URI references [RFC 2396], for example as used in XACML [XACML]
		 * attribute identifiers. The interpretation of the URI content or naming scheme is application- specific. See
		 * [SAMLProf] for attribute profiles that make use of this identifier.
		 */
 		SAML2CONSTANT const std::wstring URI = L"urn:oasis:names:tc:SAML:2.0:attrname-format:uri";
		
		/**
		 * The class of strings acceptable as the attribute name MUST be drawn from the set of values belonging to the
		 * primitive type xs:Name as defined in [Schema2] Section 3.3.6. See [SAMLProf] for attribute profiles that make use
		 * of this identifier.
		 */
 		SAML2CONSTANT const std::wstring BASIC = L"urn:oasis:names:tc:SAML:2.0:attrname-format:basic";
 	}
 }
 
 #endif
