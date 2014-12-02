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

/* STL */
#include <string>
#include <iostream>

/* Xerces */
#include <xercesc/util/XMLString.hpp>
#include <xercesc/util/XMLChar.hpp>
#include <xercesc/framework/LocalFileInputSource.hpp>
#include <xercesc/framework/Wrapper4InputSource.hpp>

/* Local Codebase */
#include "saml2/resolver/ResourceResolver.h"
#include "saml2/SAML2Defs.h"

XERCES_CPP_NAMESPACE_USE

namespace saml2
{
	ResourceResolver::ResourceResolver(const std::string& schemaPath)
	{
        std::string path = schemaPath;
        std::cout << path << std::endl;
        path.append(FILE_SEPERATOR);
		baseSchemaPath = XMLString::transcode(path.c_str());
	}

	ResourceResolver::~ResourceResolver()
	{
		XMLString::release( &baseSchemaPath );
	}

	/* TODO: Introduce caching here so disk reads only happen once */
	LocalFileInputSource* ResourceResolver::loadSchema(const XMLCh* location)
	{
        char* temp = XMLString::transcode(baseSchemaPath);
        std::string path(temp);
        XMLString::release(&temp);

         char* temp2 = XMLString::transcode(location);
         std::string locazion(temp2);
         XMLString::release(&temp2);

        std::cout << " dodgy path: " << path << std::endl;
        std::cout << " dodgy location: " << locazion << std::endl;

		return new LocalFileInputSource(baseSchemaPath, location);
	}

    InputSource* ResourceResolver::resolveEntity(const XMLCh *const publicId, const XMLCh *const systemId)
	{
		if(systemId != NULL)
		{
            return loadSchema(systemId);
		}

		return NULL;
	}	
}
