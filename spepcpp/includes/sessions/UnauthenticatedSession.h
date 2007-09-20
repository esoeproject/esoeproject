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
 * Creation Date: 08/01/2007
 * 
 * Purpose: 
 */

#ifndef UNAUTHENTICATEDSESSION_H_
#define UNAUTHENTICATEDSESSION_H_

#include "ipc/Serialization.h"
#include "unicode/utypes.h"
#include "unicode/unistr.h"
#include <iostream>

// For CygWin std::wstring workaround.
#include "SAML2Defs.h"

#include <boost/date_time/posix_time/posix_time.hpp>

namespace spep
{
	
	class UnauthenticatedSession
	{
		friend class spep::ipc::access;
		
		private:
		std::wstring _authnRequestSAMLID;
		std::string _requestURL;
		boost::posix_time::ptime _timestamp;
		
		template <class Archive>
		void serialize( Archive &ar, const unsigned int &version )
		{
			ar & _authnRequestSAMLID & _requestURL & _timestamp;
		}
		
		public:
		UnauthenticatedSession();
		UnauthenticatedSession(const UnauthenticatedSession &rhs);
		
		/**
		 * Sets the AuthnRequest SAML identifier for this session
		 */
		void setAuthnRequestSAMLID(std::wstring authnRequestSAMLID);
		std::wstring getAuthnRequestSAMLID();
		
		/**
		 * Sets the original request URL for this session. This would be the URL
		 * that caused the SPEP to request authentication.
		 */
		void setRequestURL(std::string requestURL);
		std::string getRequestURL();
		
		/**
		 * Updates the timestamp on this session to record that the session has not
		 * been idle.
		 */
		void updateTime();
		/**
		 * Gets the amount of time since updateTime() was last called.
		 */
		long getIdleTime();
		
		UnauthenticatedSession &operator=( const UnauthenticatedSession &rhs );

	};
	
}

#endif /* UNAUTHENTICATEDSESSION_H_ */
