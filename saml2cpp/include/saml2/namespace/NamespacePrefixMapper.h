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
 * Creation Date: 25/10/2006
 * 
 * Purpose: Ensures marshalled XML documents are created with namespaces that have specific meaning to humans instead of generic ns1, ns2...
 */

#ifndef NAMESPACEPREFIXMAPPER_H_
#define NAMESPACEPREFIXMAPPER_H_

#include "saml2/SAML2Defs.h"

/* STL */
#include <map>

namespace saml2
{
	class SAML2EXPORT NamespacePrefixMapper
	{
	public:
		/*
		 * Constructor
		 *
		 * Sets up prefix mapping for the library
		 */
		NamespacePrefixMapper();

		/*
		 * Method to get active mappings.
		 *
		 * @return A copy of all mapped namespaces for the library to use
		 */
		std::map < const char*, const char* > getNamespaces();

		/*
		* Method to get active mappings.
		*
		* @return A reference of all mapped namespaces for the library to use
		*/
		const std::map<const char*, const char*>& getNamespacesRef();

	private:

		std::map < const char*, const char* > map;
	};
}

#endif /*NAMESPACEPREFIXMAPPER_H_*/
