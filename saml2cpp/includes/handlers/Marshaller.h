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
 * Purpose: Abstract class representing all marshalling operations supported by saml2lib-cpp
 */

#ifndef MARSHALLER_H_
#define MARSHALLER_H_

/* STL */
#include <vector>

/* Xerces - for DOMElement */
#include <xercesc/dom/DOMElement.hpp>

/* Local codebase */
#include "handlers/SAMLDocument.h"
#include "SAML2Defs.h"

XERCES_CPP_NAMESPACE_USE

namespace saml2
{
	template <class T>
	class Marshaller
	{
		public:
			virtual ~Marshaller()
			{}

			/**
			 * Marshalls content that requires enveloped XML signature creation
			 * 
			 * This marshaller will validate the supplied object to schema, the final document will include enveloped digital signatures for the elemets listed
			 */
			virtual SAMLDocument marshallSigned( T* xmlObj, std::vector<std::string> idList) = 0;

			/**
			 * Marshalls content that does not require any signing in the final generated XML document.
			 * 
			 * This marshaller does not undertake any validation against schema, it is assumed that the supplied object for xmlObj has been supplied with all
			 * required data for generating a valid XML document. 
			 */
			virtual SAMLDocument marshallUnSigned( T* xmlObj ) = 0;

			/**
			 * Marshalls content that does not require any signing in the final generated XML node.
			 * 
			 * This marshaller does not undertake any validation of schema, it is assumed that the supplied object for xmlObj has been supplied with all
			 * required data for generating a valid XML element. 
			 */
			virtual SAMLDocument marshallUnSignedElement( T* xmlObj ) = 0;
			
			/**
			 * Translates generated DOMElement into a series of bytes to return to caller
			 */
			virtual SAMLDocument generateOutput( DOMElement* elem ) = 0;
	};
}

#endif /*MARSHALLER_H_*/
