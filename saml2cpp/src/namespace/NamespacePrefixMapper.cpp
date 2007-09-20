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
 * Author: Bradley Beddoes
 * Creation Date: 15/01/2007
 * 
 * Purpose: Ensures marshalled XML documents are created with namespaces that have specific meaning to humans instead of generic ns1, ns2...
 */

/* STL */
#include <map>

/* Xerces */
#include <xercesc/validators/schema/SchemaGrammar.hpp>

/* Local Codebase */
#include "namespace/NamespacePrefixMapper.h"

XERCES_CPP_NAMESPACE_USE

namespace saml2
{
	NamespacePrefixMapper::NamespacePrefixMapper()
	{
		this->setupPrefixMap();
	}

	std::map <char*, char*> NamespacePrefixMapper::getNamespaces()
	{
		return this->map;
	}

	void NamespacePrefixMapper::setupPrefixMap()
	{
		/* Define all of our custom mappings */
		this->map["xmlns:xs"] = "http://www.w3.org/2001/XMLSchema"; 

		this->map["xmlns:xsi"] = "http://www.w3.org/2001/XMLSchema-instance";

		this->map["xmlns:ds"] = "http://www.w3.org/2000/09/xmldsig#";

		this->map["xmlns:xenc"] = "http://www.w3.org/2001/04/xmlenc#";

		this->map["xmlns:samlp"] = "urn:oasis:names:tc:SAML:2.0:protocol";

		this->map["xmlns:saml"] = "urn:oasis:names:tc:SAML:2.0:assertion";

		this->map["xmlns:md"] = "urn:oasis:names:tc:SAML:2.0:metadata";

		this->map["xmlns:session"] = "http://www.qut.com/middleware/SessionDataSchema";

		this->map["xmlns:lxacml"] = "http://www.qut.com/middleware/lxacmlSchema";

		this->map["xmlns:lxacmlp"] = "http://www.qut.com/middleware/lxacmlSAMLProtocolSchema";

		this->map["xmlns:lxacmla"] = "http://www.qut.com/middleware/lxacmlSAMLAssertionSchema";

		this->map["xmlns:group"] = "http://www.qut.com/middleware/lxacmlGroupTargetSchema";

		this->map["xmlns:lxacml-md"] = "http://www.qut.com/middleware/lxacmlPDPSchema";

		this->map["xmlns:lxacml-context"] = "http://www.qut.com/middleware/lxacmlContextSchema";

		this->map["xmlns:esoe"] = "http://www.qut.com/middleware/ESOEProtocolSchema";

		this->map["xmlns:clear"] = "http://www.qut.com/middleware/cacheClearServiceSchema";
		
		this->map["xmlns:spep"] = "http://www.qut.com/middleware/spepStartupServiceSchema";
	}
}
