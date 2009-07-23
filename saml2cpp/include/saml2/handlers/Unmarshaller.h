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
 * Creation Date: 18/12/2006
 * 
 * Purpose: Abstract class representing all unmarshalling operations supported by saml2lib-cpp
 */

#ifndef UNMARSHALLER_H_
#define UNMARSHALLER_H_

/* OpenSSL */
#include <xsec/enc/OpenSSL/OpenSSLCryptoX509.hpp>

/* STL */
#include <string>

/* Xerces */
#include <xercesc/dom/DOMElement.hpp>

/* Local Codebase */
#include "saml2/handlers/MetadataOutput.h"
#include "saml2/handlers/SAMLDocument.h"
#include "saml2/SAML2Defs.h"

XERCES_CPP_NAMESPACE_USE

namespace saml2
{
	template <class T>
	class Unmarshaller
	{
		public:
			virtual ~Unmarshaller()
			{}
			
			/*
			 * Unmarshalls an instance of a SAML document and verifies all cryptography, on success returns the
			 * associated XSD generated object representation for further processing. Will utilise external key resolver.
			 */
			virtual T* unMarshallSigned (const SAMLDocument& document, bool keepDOM = false) = 0;
			
			/*
			 * Unmarshalls an instance of a SAML document and verifies all cryptography, on success returns the
			 * associated XSD generated object representation for further processing
			 */
			virtual T* unMarshallSigned (const SAMLDocument& document, XSECCryptoKey* pk, bool keepDOM = false) = 0;
			
			/*
			 * Unmarshalls an instance of a SAML document with no verification of cryptography, on success returns the
			 * associated XSD generated object representation for further processing
			 */
			virtual T* unMarshallUnSigned (const SAMLDocument& document, bool keepDOM = false) = 0;
			
			/*
	 		 * Unmarshalls an instance of DOMElement with no verification of cryptography, on success returns the
	 		 * associated XSD generated object representation for further processing, this is generally to be used for ##anyType elements
	 		 * extracted from an already processed XSD document, there is no element level validation performed, callers should exercise care
	 		 * with returned objects when using.g
			 */
			virtual T* unMarshallUnSignedElement (DOMElement* elem, bool keepDOM = false) = 0;
			
			/*
			 * Unmarshalls an instance of a SAML metadata document and verifies all cryptography, on success returns the
			 * associated XSD generated object representation for further processing. Additionally all keys are extracted from
			 * metadata and stored in a map with a key of the name of the crpto key and object of type XSECCryptoKey
			 */
			virtual saml2::MetadataOutput<T>* unMarshallMetadata (const SAMLDocument& document, bool keepDOM = false) = 0;
			/*
			 * Performs all cryptography operations associated with validating cryptography in the supplied domDocument
			 */
			virtual void validateSignature(DOMDocument* doc, XSECCryptoKey* pk = NULL) = 0;
	};
}

#endif
