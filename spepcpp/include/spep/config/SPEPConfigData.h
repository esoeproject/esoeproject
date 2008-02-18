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
 * Creation Date: 19/06/2007
 * 
 * Purpose: Contains the configuration data still required by the SPEP after 
 * 		initialization has occurred. 
 */

#ifndef SPEPCONFIGDATA_H_
#define SPEPCONFIGDATA_H_

#include <string>
#include <vector>
#include <map>

#include "spep/Util.h"
#include "spep/config/ConfigurationReader.h"
#include "spep/metadata/KeyResolver.h"

#include <unicode/unistr.h>

namespace spep
{
	
	class SPEPEXPORT SPEPConfigData
	{
		
		public:
		SPEPConfigData();
		SPEPConfigData( const SPEPConfigData &other );
		SPEPConfigData( const ConfigurationReader &config );
		SPEPConfigData& operator=( const SPEPConfigData &other );
		
		template <class Archive>
		void serialize( Archive &ar, unsigned int version )
		{
			ar & _started;
			ar & _lazyInit;
			ar & _lazyInitDefaultPermit;
			ar & _defaultUrl;
			ar & _loginRedirect;
			ar & _ssoRedirect;
			ar & _tokenName;
			ar & _schemaPath;
			ar & _attributeNamePrefix;
			ar & _attributeValueSeparator;
			ar & _usernameAttribute;
			ar & _caBundle;
			ar & _globalESOECookieName;
			ar & _serviceHost;
			ar & _spepIdentifier;
			ar & _esoeIdentifier;
			ar & _attributeConsumingServiceIndex;
			ar & _assertionConsumerServiceIndex;
			ar & _allowedTimeSkew;
			ar & _logoutClearCookies;
			ar & _ipAddresses;
			ar & _lazyInitResources;
			ar & _keyResolver;
			ar & _attributeRenameMap;
		}
		
		bool isStarted();
		bool isLazyInit();
		bool isLazyInitDefaultPermit();
		void setStarted( bool started );
		std::string getDefaultUrl();
		std::string getLoginRedirect();
		std::string getSSORedirect();
		std::string getTokenName();
		std::string getSchemaPath();
		std::string getAttributeNamePrefix();
		std::string getAttributeValueSeparator();
		std::string getUsernameAttribute();
		std::string getCABundle();
		std::string getGlobalESOECookieName();
		std::string getServiceHost();
		std::wstring getSPEPIdentifier();
		std::wstring getESOEIdentifier();
		int getAttributeConsumingServiceIndex();
		int getAssertionConsumerServiceIndex();
		int getAllowedTimeSkew();
		const std::vector<std::string>& getLogoutClearCookies();
		const std::vector<std::wstring>& getIPAddresses();
		const std::vector<UnicodeString>& getLazyInitResources();
		KeyResolver* getKeyResolver();
		const std::map<std::string,std::string>& getAttributeRenameMap();
		
		private:
		bool _started;
		bool _lazyInit;
		bool _lazyInitDefaultPermit;
		std::string _defaultUrl;
		std::string _loginRedirect;
		std::string _ssoRedirect;
		std::string _tokenName;
		std::string _schemaPath;
		std::string _attributeNamePrefix;
		std::string _attributeValueSeparator;
		std::string _usernameAttribute;
		std::string _caBundle;
		std::string _globalESOECookieName;
		std::string _serviceHost;
		std::wstring _spepIdentifier;
		std::wstring _esoeIdentifier;
		int _attributeConsumingServiceIndex;
		int _assertionConsumerServiceIndex;
		int _allowedTimeSkew;
		std::vector<std::string> _logoutClearCookies;
		std::vector<std::wstring> _ipAddresses;
		std::vector<UnicodeString> _lazyInitResources;
		KeyResolver _keyResolver;
		std::map<std::string,std::string> _attributeRenameMap;
		
	};
	
}

#endif /*SPEPCONFIGDATA_H_*/
