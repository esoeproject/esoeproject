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
 
#include "spep/config/ConfigurationReader.h"

#include <boost/lexical_cast.hpp>

#include "saml2/exceptions/InvalidParameterException.h"

#include <cstdio>

#ifdef _MSC_VER
#define snprintf _snprintf
#endif /*_MSC_VER*/

#define OPTION_TYPE_STRING_SINGLE boost::program_options::value< std::vector<std::string> >
#define OPTION_TYPE_STRING_MULTI boost::program_options::value< std::vector<std::string> >
#define OPTION_TYPE_INTEGER_SINGLE boost::program_options::value< std::vector<std::string> >

void spep::ConfigurationReader::addOptions( boost::program_options::options_description &optionsDescription )
{
	optionsDescription.add_options()
		// Keystore settings
		( CONFIGURATION_KEYSTOREPATH, OPTION_TYPE_STRING_SINGLE(), "" )
		( CONFIGURATION_KEYSTOREPASSWORD, OPTION_TYPE_STRING_SINGLE(), "" )
		( CONFIGURATION_SPEPKEYALIAS, OPTION_TYPE_STRING_SINGLE(), "" )
		( CONFIGURATION_SPEPKEYPASSWORD, OPTION_TYPE_STRING_SINGLE(), "" )
		( CONFIGURATION_METADATAKEYALIAS, OPTION_TYPE_STRING_SINGLE(), "" )
	
		// Mandatory config
		( CONFIGURATION_SCHEMAPATH, OPTION_TYPE_STRING_SINGLE(), "" )
		( CONFIGURATION_ESOEIDENTIFIER, OPTION_TYPE_STRING_SINGLE(), "" )
		( CONFIGURATION_SPEPIDENTIFIER, OPTION_TYPE_STRING_SINGLE(), "" )
		( CONFIGURATION_METADATAURL, OPTION_TYPE_STRING_SINGLE(), "" )
		( CONFIGURATION_SERVERINFO, OPTION_TYPE_STRING_SINGLE(), "" )
		( CONFIGURATION_NODEIDENTIFIER, OPTION_TYPE_STRING_SINGLE(), "" )
		( CONFIGURATION_ATTRIBUTECONSUMINGSERVICEINDEX, OPTION_TYPE_INTEGER_SINGLE(), "" )
		( CONFIGURATION_ASSERTIONCONSUMERSERVICEINDEX, OPTION_TYPE_INTEGER_SINGLE(), "" )
		( CONFIGURATION_AUTHZCACHEINDEX, OPTION_TYPE_INTEGER_SINGLE(), "" )
		( CONFIGURATION_SERVICEHOST, OPTION_TYPE_INTEGER_SINGLE(), "" )
		( CONFIGURATION_IPADDRESSES, OPTION_TYPE_STRING_SINGLE(), "" )
		( CONFIGURATION_LOGINREDIRECT, OPTION_TYPE_STRING_SINGLE(), "" )
		( CONFIGURATION_DEFAULTURL, OPTION_TYPE_STRING_SINGLE(), "" )
		( CONFIGURATION_LOGOUTCLEARCOOKIE, OPTION_TYPE_STRING_MULTI(), "" )
		( CONFIGURATION_SPEPDAEMONPORT, OPTION_TYPE_INTEGER_SINGLE(), "" )
	
		// Attribute -> environment config
		( CONFIGURATION_ATTRIBUTERENAME, OPTION_TYPE_STRING_MULTI(), "" )
		( CONFIGURATION_USERNAMEATTRIBUTE, OPTION_TYPE_STRING_SINGLE(), "" )
		( CONFIGURATION_ATTRIBUTENAMEPREFIX, OPTION_TYPE_STRING_SINGLE(), "" )
		( CONFIGURATION_ATTRIBUTEVALUESEPARATOR, OPTION_TYPE_STRING_SINGLE(), "" )
	
		// Extra config
		( CONFIGURATION_CABUNDLE, OPTION_TYPE_STRING_SINGLE(), "" )
	
		// Lazy session init
		( CONFIGURATION_LAZYINIT, OPTION_TYPE_INTEGER_SINGLE(), "" )
		( CONFIGURATION_LAZYINITDEFAULTACTION, OPTION_TYPE_INTEGER_SINGLE(), "" )
		( CONFIGURATION_LAZYINITRESOURCE, OPTION_TYPE_STRING_MULTI(), "" )
	
		// Advanced options
		( CONFIGURATION_SSOREDIRECT, OPTION_TYPE_INTEGER_SINGLE(), "" )
		( CONFIGURATION_SPEPTOKENNAME, OPTION_TYPE_STRING_SINGLE(), "" )
		( CONFIGURATION_COMMONDOMAINTOKENNAME, OPTION_TYPE_INTEGER_SINGLE(), "" )
		( CONFIGURATION_STARTUPRETRYINTERVAL, OPTION_TYPE_INTEGER_SINGLE(), "" )
		( CONFIGURATION_METADATAINTERVAL, OPTION_TYPE_INTEGER_SINGLE(), "" )
		( CONFIGURATION_ALLOWEDTIMESKEW, OPTION_TYPE_INTEGER_SINGLE(), "" )
		( CONFIGURATION_IDENTIFIERCACHEINTERVAL, OPTION_TYPE_INTEGER_SINGLE(), "" )
		( CONFIGURATION_IDENTIFIERCACHETIMEOUT, OPTION_TYPE_INTEGER_SINGLE(), "" )
		( CONFIGURATION_SESSIONCACHETIMEOUT, OPTION_TYPE_INTEGER_SINGLE(), "" )
		( CONFIGURATION_SESSIONCACHEINTERVAL, OPTION_TYPE_INTEGER_SINGLE(), "" )
		( CONFIGURATION_DEFAULTPOLICYDECISION, OPTION_TYPE_STRING_SINGLE(), "" )
		
		// Disabling functionality
		( CONFIGURATION_DISABLEATTRIBUTEQUERY, OPTION_TYPE_STRING_SINGLE(), "" )
		( CONFIGURATION_DISABLEPOLICYENFORCEMENT, OPTION_TYPE_STRING_SINGLE(), "" )
		( CONFIGURATION_DISABLESPEPSTARTUP, OPTION_TYPE_STRING_SINGLE(), "" )
	;

}

void spep::ConfigurationReader::setStringValue( boost::program_options::variables_map &variablesMap, std::string variableName, bool optional )
{
	const auto count = variablesMap.count( variableName );
	char buf[MESSAGE_CONFIG_BUFFER_SIZE];
	
	if( count <= 0 )
	{
		if( optional )
		{
			return;
		}
		
		// No value specified
		snprintf( buf, MESSAGE_CONFIG_BUFFER_SIZE, MESSAGE_CONFIG_VARIABLE_NOT_PRESENT, variableName.c_str() );
	}
	else if( count == 1 )
	{
		const std::vector<std::string> &values = variablesMap[variableName].as< std::vector<std::string> >();
		
		// Put it in the variable map and return.
		this->_stringValues[variableName] = values[0];
		return;
	}
	else
	{
		// Too many values specified
		snprintf( buf, MESSAGE_CONFIG_BUFFER_SIZE, MESSAGE_CONFIG_NOT_MULTIVALUED, variableName.c_str() );
	}
	
	std::cerr << std::string(buf) << std::endl;
	
	_valid = false;
}

void spep::ConfigurationReader::setIntegerValue( boost::program_options::variables_map &variablesMap, std::string variableName, bool optional )
{
	const auto count = variablesMap.count( variableName );
	char buf[MESSAGE_CONFIG_BUFFER_SIZE];
	
	if( count <= 0 )
	{
		if( optional )
		{
			return;
		}
		
		// No value specified
		snprintf( buf, MESSAGE_CONFIG_BUFFER_SIZE, MESSAGE_CONFIG_VARIABLE_NOT_PRESENT, variableName.c_str() );
	}
	else if( count == 1 )
	{
		const std::vector<std::string> &values = variablesMap[variableName].as< std::vector<std::string> >();
		
		try
		{
			int intValue = boost::lexical_cast<int>( values[0] );
		
			// Put it in the variable map and return.
			this->_intValues[variableName] = intValue;
			
			return;
		}
		catch ( boost::bad_lexical_cast )
		{
			snprintf( buf, MESSAGE_CONFIG_BUFFER_SIZE, MESSAGE_CONFIG_NOT_A_NUMBER, variableName.c_str() );
		}
	}
	else
	{
		// Too many values specified
		snprintf( buf, MESSAGE_CONFIG_BUFFER_SIZE, MESSAGE_CONFIG_NOT_MULTIVALUED, variableName.c_str() );
	}
	
	std::cerr << std::string(buf) << std::endl;
	
	_valid = false;
}

void spep::ConfigurationReader::setMultiValue( boost::program_options::variables_map &variablesMap, std::string variableName, size_t minOccurs )
{
	size_t count = variablesMap.count( variableName );
	char buf[MESSAGE_CONFIG_BUFFER_SIZE];
	
	if( count == 0 && minOccurs == 0 )
	{
		return;
	}
	else if( count < minOccurs )
	{
		// Not enough values were specified in the config file.
		snprintf( buf, MESSAGE_CONFIG_BUFFER_SIZE, MESSAGE_CONFIG_VARIABLE_NOT_PRESENT, variableName.c_str() );
	}
	else
	{
		const std::vector<std::string> &values = variablesMap[variableName].as< std::vector<std::string> >();

		// Put it in the variable map and return.
		std::vector<std::string> &valueOutputVector = this->_multiValues[variableName];
		
		for( std::vector<std::string>::const_iterator valueIterator = values.begin();
			valueIterator != values.end();
			++valueIterator )
		{
			valueOutputVector.push_back( *valueIterator );
		}
		
		return;
	}
	
	std::cerr << std::string(buf) << std::endl;
	
	_valid = false;
}

int spep::ConfigurationReader::getIntegerValue( std::string variableName ) const
{
	std::map<std::string, int>::const_iterator iterator = this->_intValues.find( variableName );
	if( iterator == this->_intValues.end() )
	{
		SAML2LIB_INVPARAM_EX( "The requested parameter does not exist" );
	}
	
	return iterator->second;
}

std::string spep::ConfigurationReader::getStringValue( std::string variableName ) const
{
	std::map<std::string, std::string>::const_iterator iterator = this->_stringValues.find( variableName );
	if( iterator == this->_stringValues.end() )
	{
		SAML2LIB_INVPARAM_EX( "The requested parameter does not exist" );
	}
	
	return iterator->second;
}

std::vector<std::string> spep::ConfigurationReader::getMultiValue( std::string variableName, size_t minOccurs ) const
{
	std::map<std::string, std::vector<std::string> >::const_iterator iterator = this->_multiValues.find( variableName );
	if( iterator == this->_multiValues.end() )
	{
		if( minOccurs <= 0 )
		{
			return std::vector<std::string>();
		}
		SAML2LIB_INVPARAM_EX( "The requested parameter does not exist" );
	}
	
	return iterator->second;
}

int spep::ConfigurationReader::getIntegerValue( std::string variableName, int defaultValue ) const
{
	std::map<std::string, int>::const_iterator iterator = this->_intValues.find( variableName );
	if( iterator == this->_intValues.end() )
	{
		return defaultValue;
	}
	
	return iterator->second;
}

std::string spep::ConfigurationReader::getStringValue( std::string variableName, std::string defaultValue ) const
{
	std::map<std::string, std::string>::const_iterator iterator = this->_stringValues.find( variableName );
	if( iterator == this->_stringValues.end() )
	{
		return defaultValue;
	}
	
	return iterator->second;
}

spep::ConfigurationReader::ConfigurationReader( boost::program_options::variables_map &variablesMap )
{
	_valid = true;
	
	// Keystore settings
	this->setStringValue( variablesMap, CONFIGURATION_KEYSTOREPATH );
	this->setStringValue( variablesMap, CONFIGURATION_KEYSTOREPASSWORD );
	this->setStringValue( variablesMap, CONFIGURATION_SPEPKEYALIAS );
	this->setStringValue( variablesMap, CONFIGURATION_SPEPKEYPASSWORD );

	// Mandatory config
	this->setStringValue( variablesMap, CONFIGURATION_SCHEMAPATH );
	this->setStringValue( variablesMap, CONFIGURATION_ESOEIDENTIFIER );
	this->setStringValue( variablesMap, CONFIGURATION_SPEPIDENTIFIER );
	this->setStringValue( variablesMap, CONFIGURATION_METADATAURL );
	this->setStringValue( variablesMap, CONFIGURATION_SERVERINFO );
	this->setStringValue( variablesMap, CONFIGURATION_NODEIDENTIFIER );
	this->setIntegerValue( variablesMap, CONFIGURATION_ATTRIBUTECONSUMINGSERVICEINDEX );
	this->setIntegerValue( variablesMap, CONFIGURATION_ASSERTIONCONSUMERSERVICEINDEX );
	this->setIntegerValue( variablesMap, CONFIGURATION_AUTHZCACHEINDEX );
	this->setStringValue( variablesMap, CONFIGURATION_SERVICEHOST );
	this->setStringValue( variablesMap, CONFIGURATION_IPADDRESSES );
	this->setStringValue( variablesMap, CONFIGURATION_LOGINREDIRECT );
	this->setStringValue( variablesMap, CONFIGURATION_DEFAULTURL );

	this->setMultiValue( variablesMap, CONFIGURATION_LOGOUTCLEARCOOKIE, 0 );
	this->setIntegerValue( variablesMap, CONFIGURATION_SPEPDAEMONPORT );

	// Attribute -> environment config
	this->setMultiValue( variablesMap, CONFIGURATION_ATTRIBUTERENAME, 0 );
	this->setStringValue( variablesMap, CONFIGURATION_USERNAMEATTRIBUTE );
	this->setStringValue( variablesMap, CONFIGURATION_ATTRIBUTENAMEPREFIX );
	this->setStringValue( variablesMap, CONFIGURATION_ATTRIBUTEVALUESEPARATOR );

	// Extra config
	this->setStringValue( variablesMap, CONFIGURATION_CABUNDLE, true );

	// Lazy session init
	this->setStringValue( variablesMap, CONFIGURATION_LAZYINIT );
	this->setStringValue( variablesMap, CONFIGURATION_LAZYINITDEFAULTACTION, true );
	this->setMultiValue( variablesMap, CONFIGURATION_LAZYINITRESOURCE, 0 );

	// Advanced options
	this->setStringValue( variablesMap, CONFIGURATION_SSOREDIRECT );
	this->setStringValue( variablesMap, CONFIGURATION_SPEPTOKENNAME );
	this->setStringValue( variablesMap, CONFIGURATION_COMMONDOMAINTOKENNAME );
	this->setIntegerValue( variablesMap, CONFIGURATION_STARTUPRETRYINTERVAL );
	this->setIntegerValue( variablesMap, CONFIGURATION_METADATAINTERVAL );
	this->setIntegerValue( variablesMap, CONFIGURATION_ALLOWEDTIMESKEW );
	this->setIntegerValue( variablesMap, CONFIGURATION_IDENTIFIERCACHEINTERVAL );
	this->setIntegerValue( variablesMap, CONFIGURATION_IDENTIFIERCACHETIMEOUT );
	this->setIntegerValue( variablesMap, CONFIGURATION_SESSIONCACHETIMEOUT );
	this->setIntegerValue( variablesMap, CONFIGURATION_SESSIONCACHEINTERVAL );
	this->setStringValue( variablesMap, CONFIGURATION_DEFAULTPOLICYDECISION );
	
	// Disabling functionality
	this->setStringValue( variablesMap, CONFIGURATION_DISABLEATTRIBUTEQUERY, "false" );
	this->setStringValue( variablesMap, CONFIGURATION_DISABLEPOLICYENFORCEMENT, "false" );
	this->setStringValue( variablesMap, CONFIGURATION_DISABLESPEPSTARTUP, "false" );

	// Deprecated/unused options
	this->setStringValue( variablesMap, CONFIGURATION_METADATAKEYALIAS, "" );
}

bool spep::ConfigurationReader::isValid()
{
	return this->_valid;
}
