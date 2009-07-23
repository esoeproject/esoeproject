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
 * Creation Date: 04/06/2007
 * 
 * Purpose: 
 */

#ifndef CONFIGURATIONREADER_H_
#define CONFIGURATIONREADER_H_

#include <map>
#include <vector>
#include <iostream>
#include <boost/program_options/variables_map.hpp>
#include <boost/program_options/option.hpp>
#include <boost/program_options/options_description.hpp>

#include "spep/Util.h"

// Keystore settings
#define CONFIGURATION_KEYSTOREPATH "keystorePath"
#define CONFIGURATION_KEYSTOREPASSWORD "keystorePassword"
#define CONFIGURATION_SPEPKEYALIAS "spepKeyAlias"
#define CONFIGURATION_SPEPKEYPASSWORD "spepKeyPassword"
#define CONFIGURATION_METADATAKEYALIAS "metadataKeyAlias"

// Mandatory config
#define CONFIGURATION_SCHEMAPATH "schemaPath"
#define CONFIGURATION_ESOEIDENTIFIER "esoeIdentifier"
#define CONFIGURATION_SPEPIDENTIFIER "spepIdentifier"
#define CONFIGURATION_METADATAURL "metadataUrl"
#define CONFIGURATION_SERVERINFO "serverInfo"
#define CONFIGURATION_NODEIDENTIFIER "nodeIdentifier"
#define CONFIGURATION_ATTRIBUTECONSUMINGSERVICEINDEX "attributeConsumingServiceIndex"
#define CONFIGURATION_ASSERTIONCONSUMERSERVICEINDEX "assertionConsumerServiceIndex"
#define CONFIGURATION_AUTHZCACHEINDEX "authzCacheIndex"
#define CONFIGURATION_SERVICEHOST "serviceHost"
#define CONFIGURATION_IPADDRESSES "ipAddresses"
#define CONFIGURATION_LOGINREDIRECT "loginRedirect"
#define CONFIGURATION_DEFAULTURL "defaultUrl"
#define CONFIGURATION_LOGOUTCLEARCOOKIE "logoutClearCookie"
#define CONFIGURATION_SPEPDAEMONPORT "spepDaemonPort"

// Attribute -> environment config
#define CONFIGURATION_ATTRIBUTERENAME "attributeRename"
#define CONFIGURATION_USERNAMEATTRIBUTE "usernameAttribute"
#define CONFIGURATION_ATTRIBUTENAMEPREFIX "attributeNamePrefix"
#define CONFIGURATION_ATTRIBUTEVALUESEPARATOR "attributeValueSeparator"

// Extra config
#define CONFIGURATION_CABUNDLE "caBundle"

// Lazy session init
#define CONFIGURATION_LAZYINIT "lazyInit"
#define CONFIGURATION_LAZYINITDEFAULTACTION "lazyInitDefaultAction"
#define CONFIGURATION_LAZYINITRESOURCE "lazyInit-resource"

// Advanced options
#define CONFIGURATION_SSOREDIRECT "ssoRedirect"
#define CONFIGURATION_SPEPTOKENNAME "spepTokenName"
#define CONFIGURATION_COMMONDOMAINTOKENNAME "commonDomainTokenName"
#define CONFIGURATION_STARTUPRETRYINTERVAL "startupRetryInterval"
#define CONFIGURATION_METADATAINTERVAL "metadataInterval"
#define CONFIGURATION_ALLOWEDTIMESKEW "allowedTimeSkew"
#define CONFIGURATION_IDENTIFIERCACHEINTERVAL "identifierCacheInterval"
#define CONFIGURATION_IDENTIFIERCACHETIMEOUT "identifierCacheTimeout"
#define CONFIGURATION_SESSIONCACHETIMEOUT "sessionCacheTimeout"
#define CONFIGURATION_SESSIONCACHEINTERVAL "sessionCacheInterval"
#define CONFIGURATION_DEFAULTPOLICYDECISION "defaultPolicyDecision"

// Disabling functionality
#define CONFIGURATION_DISABLEATTRIBUTEQUERY "disableAttributeQuery"
#define CONFIGURATION_DISABLEPOLICYENFORCEMENT "disablePolicyEnforcement"
#define CONFIGURATION_DISABLESPEPSTARTUP "disableSPEPStartup"

#define MESSAGE_CONFIG_BUFFER_SIZE 512
#define MESSAGE_CONFIG_VARIABLE_NOT_PRESENT "The property %s was not present in the configuration file."
#define MESSAGE_CONFIG_NOT_MULTIVALUED "The property %s was specified multiple times, but is not a multi valued property"
#define MESSAGE_CONFIG_NOT_A_NUMBER "The property %s had an invalid value. Expected a number."

namespace spep {
	
	class SPEPEXPORT ConfigurationReader
	{
		
		private:
		// Undefined copy constructor.
		ConfigurationReader( ConfigurationReader& other );
		
		std::map<std::string, std::string> _stringValues;
		std::map<std::string, int> _intValues;
		std::map< std::string, std::vector<std::string> > _multiValues;
		bool _valid;
		
		void setStringValue( boost::program_options::variables_map &variablesMap, std::string variableName, bool optional = false );
		void setIntegerValue( boost::program_options::variables_map &variablesMap, std::string variableName, bool optional = false );
		void setMultiValue( boost::program_options::variables_map &variablesMap, std::string variableName, size_t minOccurs = 1 );
		
		public:
		static void addOptions( boost::program_options::options_description &optionsDescription );
		
		ConfigurationReader( boost::program_options::variables_map &variablesMap );
		bool isValid();
		
		int getIntegerValue( std::string variableName ) const;
		std::string getStringValue( std::string variableName ) const;
		std::vector<std::string> getMultiValue( std::string variableName, size_t minOccurs = 1 ) const;
		
		int getIntegerValue( std::string variableName, int defaultValue ) const;
		std::string getStringValue( std::string variableName, std::string defaultValue ) const;
		
	};
	
}

#endif /*CONFIGURATIONREADER_H_*/

