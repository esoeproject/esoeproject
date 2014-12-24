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
 * Creation Date: 19/12/2006
 * 
 * Purpose: Resolves schema resources locally for the SAML2lib-cpp library
 */

#ifndef RESOURCERESOLVER_H_
#define RESOURCERESOLVER_H_

#include "saml2/SAML2Defs.h"

/* STL */
#include <string>

/* Xerces C++ */
#include <xercesc/util/XMLEntityResolver.hpp>
#include <xercesc/framework/LocalFileInputSource.hpp>

XERCES_CPP_NAMESPACE_USE

namespace saml2
{
	class SAML2EXPORT ResourceResolver : public XMLEntityResolver
	{
	public:
		/*
		 * Constructor
		 *
		 * @param baseSchemaPath Base path on disk that schema can be loaded from
		 */
		ResourceResolver(const std::string& schemaPath);

		/*
		 * Destructor
		 *
		 * Releases Xerces string data
		 */
		virtual ~ResourceResolver();

		/*
		*  Handlers for the XMLEntityResolver interface
		*/
		InputSource* resolveEntity(XMLResourceIdentifier* resourceIdentifier) override;


	private:

		/* SAX2 function - we dont override this anymore but use it internally
		* Returns a DOMInputSource to match the requested schema, implementation of DOMEntityResolver pure virtual method
		*/
		virtual InputSource * resolveEntity(const XMLCh *const publicId, const XMLCh *const systemId);

		/*
		 * Loads a schema file from disk
		 */
		LocalFileInputSource * loadSchema(const XMLCh* location);

		XMLCh* baseSchemaPath;
	};
}

#endif /*RESOURCERESOLVER_H_*/
