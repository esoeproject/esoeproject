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
 * Creation Date: 09/03/2007
 * 
 * Purpose: 
 */
#ifndef ATTRIBUTEPROCESSORDATA_H_
#define ATTRIBUTEPROCESSORDATA_H_

#include "handlers/SAMLDocument.h"

namespace spep
{
	
	class AttributeProcessorData
	{
		
		public:
		AttributeProcessorData();
		
		/**
		 * Sets the SAML request document. The meaning of this document is context dependant, and
		 * may also be used to return a SAML request document from a method.
		 */ 
		void setRequestDocument( const saml2::SAMLDocument& requestDocument );
		const saml2::SAMLDocument& getRequestDocument();
		
		/**
		 * Sets the SAML response document. The meaning of this document is context dependant, and
		 * may also be used to return a SAML response document from a method.
		 */ 
		void setResponseDocument( const saml2::SAMLDocument& responseDocument );
		const saml2::SAMLDocument& getResponseDocument();

		private:
		saml2::SAMLDocument _requestDocument;
		saml2::SAMLDocument _responseDocument;
		
	};
	
}

#endif /*ATTRIBUTEPROCESSORDATA_H_*/
