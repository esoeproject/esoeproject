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
 * Creation Date: Aug 14, 2007
 * 
 * Purpose: 
 */

#ifndef SOAPUTIL_H_
#define SOAPUTIL_H_

// For both SAMLDocument and the template class ManagedDocument
#include "handlers/SAMLDocument.h"

#include <utility>
#include <string>

namespace spep
{
	
	typedef saml2::ManagedDocument<char,std::size_t> SOAPDocument;
	
	class SOAPUtil
	{
	
		static void axiomInit();
		
		public:
		
		enum SOAPVersion
		{
			UNINITIALIZED,
			SOAP11,
			SOAP12
		};
		
		static SOAPDocument wrapDocumentInSOAP( const saml2::SAMLDocument& samlDocument, std::string characterEncoding, SOAPVersion soapVersion );
		static saml2::SAMLDocument unwrapDocumentFromSOAP( const SOAPDocument& soapDocument, std::string characterEncoding, SOAPVersion soapVersion );
		
	};
	
}

#endif /*SOAPUTIL_H_*/
