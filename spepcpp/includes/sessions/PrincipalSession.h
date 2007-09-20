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
 * Creation Date: 05/03/2007
 * 
 * Purpose: 
 */

#ifndef PRINCIPALSESSION_H_
#define PRINCIPALSESSION_H_

#include <vector>
#include <map>
#include <string>

#include "unicode/utypes.h"
#include "unicode/unistr.h"

#include "SAML2Defs.h"

#include "ipc/Serialization.h"

#include <boost/date_time/posix_time/ptime.hpp>

namespace spep
{
	
	class PrincipalSession
	{
		
		friend class spep::ipc::access;
		
		public:
		// Wide string format for ESOE Session Index, since it is an XML String.
		// Key - ESOE Session Index, Value - Local Session ID
		typedef std::map<std::wstring, std::string> ESOESessionIndexMapType;
		typedef std::map< UnicodeString, std::vector<UnicodeString> > AttributeMapType;
		typedef std::vector<std::string> SessionIDListType;
		typedef boost::posix_time::ptime TimeType;
		
		PrincipalSession();
		PrincipalSession( const PrincipalSession& other );
		PrincipalSession& operator=( const PrincipalSession& other );
		
		/** 
		 * Sets the ESOE session identifier for this principal session 
		 */
		void setESOESessionID( std::wstring &esoeSessionID );
		std::wstring &getESOESessionID();
		/** 
		 * Sets the expire time for this session 
		 */
		void setSessionNotOnOrAfter( TimeType sessionNotOnOrAfter );
		TimeType getSessionNotOnOrAfter();
		/** 
		 * Adds a ESOE session index to local session ID mapping 
		 */
		void addESOESessionIndexAndLocalSessionID( std::wstring &esoeSessionIndex, std::string &localSessionID );
		/** 
		 * Gets the map of ESOE session index to local session ID 
		 */
		ESOESessionIndexMapType &getESOESessionIndexMap();
		/** 
		 * Gets the list of local session IDs established for this principal session 
		 */
		SessionIDListType &getSessionIDList();
		/**
		 * Gets the attribute map for this session 
		 */
		AttributeMapType &getAttributeMap();
		
		private:
		template <class Archive>
		void serialize( Archive &ar, const unsigned int version )
		{
			ar  & this->_esoeSessionID
				& this->_sessionNotOnOrAfter
				& this->_sessionIDList
				& this->_attributeMap
				& this->_esoeSessionIndexMap;
		}
		
		std::wstring _esoeSessionID;
		TimeType _sessionNotOnOrAfter;
		SessionIDListType _sessionIDList;
		AttributeMapType _attributeMap;
		ESOESessionIndexMapType _esoeSessionIndexMap;
		
	};
	
}

#endif /*PRINCIPALSESSION_H_*/
