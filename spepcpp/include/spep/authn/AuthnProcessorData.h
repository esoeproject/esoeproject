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
 * Creation Date: 19/02/2007
 * 
 * Purpose: Contains data for calls to the authentication processor.
 */

#ifndef AUTHNPROCESSORDATA_H_
#define AUTHNPROCESSORDATA_H_

#include <string>

#include "spep/Util.h"
#include "saml2/handlers/SAMLDocument.h"

namespace spep
{
	
	/**
	 * Contains data used for or returned from calls to the authentication processor.
	 */
	class SPEPEXPORT AuthnProcessorData
	{
		
		public:
			
		AuthnProcessorData();
		
		/**
		 * Sets the URL originally request by the user agent, before authentication was invoked.
		 */
		void setRequestURL( const std::string &requestURL );
		std::string getRequestURL();
		
		/**
		 * Sets the base URL for the host the request was made to.
		 */
		void setBaseRequestURL( const std::string& baseRequestURL );
		std::string getBaseRequestURL();

		/**
		 * Sets the local session ID assigned to the session by the authentication processor
		 */		
		void setSessionID( const std::string &sessionID );
		std::string getSessionID();
		
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
		
		/**
		 * Sets whether or not to disable attribute querying for this session
		 */
		void setDisableAttributeQuery( bool value );
		bool getDisableAttributeQuery();

		/**
		 * Sets the remote user's IP address. 
		 */
		void setRemoteIpAddress(const std::string &remoteIpAddress);
		std::string getRemoteIpAddress() const;
		
		private:
		std::string _requestURL;
		std::string _baseRequestURL;
		std::string _sessionID;
		std::string _remoteIpAddress;
		saml2::SAMLDocument _requestDocument;
		saml2::SAMLDocument _responseDocument;
		bool _disableAttributeQuery;
		
	};
	
}

#endif /*AUTHNPROCESSORDATA_H_*/
