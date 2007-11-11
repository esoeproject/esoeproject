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
 * Creation Date: 20/06/2007
 * 
 * Purpose: 
 */
 
#include "spep/config/SPEPConfigData.h"
#include "spep/UnicodeStringConversion.h"

#include "saml2/exceptions/InvalidParameterException.h"

spep::SPEPConfigData::SPEPConfigData()
:
_started( false )
{
}

spep::SPEPConfigData::SPEPConfigData( const spep::SPEPConfigData &other )
:
_started( other._started ),
_defaultUrl( other._defaultUrl ),
_loginRedirect( other._loginRedirect ),
_tokenName( other._tokenName ),
_tokenDomain( other._tokenDomain ),
_schemaPath( other._schemaPath ),
_attributeNamePrefix( other._attributeNamePrefix ),
_attributeValueSeparator( other._attributeValueSeparator ),
_usernameAttribute( other._usernameAttribute ),
_caBundle( other._caBundle ),
_spepIdentifier( other._spepIdentifier ),
_esoeIdentifier( other._esoeIdentifier ),
_attributeConsumingServiceIndex( other._attributeConsumingServiceIndex ),
_assertionConsumerServiceIndex( other._assertionConsumerServiceIndex ),
_allowedTimeSkew( other._allowedTimeSkew ),
_logoutClearCookies( other._logoutClearCookies ),
_ipAddresses( other._ipAddresses ),
_keyResolver( other._keyResolver ),
_attributeRenameMap( other._attributeRenameMap )
{
}

spep::SPEPConfigData::SPEPConfigData( const spep::ConfigurationReader& config )
{
	this->_started = false;
	
	// Grab the required config values from the ConfigurationReader instance.
	this->_defaultUrl = config.getStringValue( CONFIGURATION_DEFAULTURL );
	this->_loginRedirect = config.getStringValue( CONFIGURATION_LOGINREDIRECT );
	this->_tokenName = config.getStringValue( CONFIGURATION_TOKENNAME );
	this->_tokenDomain = config.getStringValue( CONFIGURATION_TOKENDOMAIN );
	this->_schemaPath = config.getStringValue( CONFIGURATION_SCHEMAPATH );
	this->_attributeNamePrefix = config.getStringValue( CONFIGURATION_ATTRIBUTENAMEPREFIX );
	this->_attributeValueSeparator = config.getStringValue( CONFIGURATION_ATTRIBUTEVALUESEPARATOR );
	this->_usernameAttribute = config.getStringValue( CONFIGURATION_USERNAMEATTRIBUTE );
	this->_caBundle = config.getStringValue( CONFIGURATION_CABUNDLE, std::string() ); // Default value empty string
	this->_spepIdentifier = UnicodeStringConversion::toWString( config.getStringValue( CONFIGURATION_SPEPIDENTIFIER ) );
	this->_esoeIdentifier = UnicodeStringConversion::toWString( config.getStringValue( CONFIGURATION_ESOEIDENTIFIER ) );
	this->_attributeConsumingServiceIndex = config.getIntegerValue( CONFIGURATION_ATTRIBUTECONSUMINGSERVICEINDEX );
	this->_assertionConsumerServiceIndex = config.getIntegerValue( CONFIGURATION_ASSERTIONCONSUMERSERVICEINDEX );
	// Allow no entries to be entered here.
	this->_logoutClearCookies = config.getMultiValue( CONFIGURATION_LOGOUTCLEARCOOKIE, 0 ); 
	
	std::string ipAddressStringValue( config.getStringValue( CONFIGURATION_IPADDRESSES ) );
	
	// Split the ip address list up into separate values and make them into a vector.
	// std::stringstream has this neat feature of being able to split on whitespace.
	std::stringstream ipAddressStringStream;
	ipAddressStringStream << ipAddressStringValue;
	std::string ipAddress;
	while( ipAddressStringStream >> ipAddress )
	{
		this->_ipAddresses.push_back( UnicodeStringConversion::toWString( ipAddress ) );
	}
	
	std::vector<std::string> attributeRenameConfig( config.getMultiValue( CONFIGURATION_ATTRIBUTERENAME, 0 ) );
	for( std::vector<std::string>::iterator iter = attributeRenameConfig.begin(); iter != attributeRenameConfig.end(); ++iter )
	{
		std::stringstream attributeSplitter;
		attributeSplitter << *iter << std::ends;
		
		std::string originalName, newName;
		attributeSplitter >> originalName;
		attributeSplitter >> newName;
		
		this->_attributeRenameMap[originalName] = newName;
	}
	
	this->_allowedTimeSkew = config.getIntegerValue( CONFIGURATION_ALLOWEDTIMESKEW );

	std::string keyPath( config.getStringValue( CONFIGURATION_KEYPATH ) );
	std::string spepKeyAlias( config.getStringValue( CONFIGURATION_SPEPKEYALIAS ) );
	std::string spepPublicKeyFilename( config.getStringValue( CONFIGURATION_SPEPPUBLICKEYFILENAME ) );
	std::string spepPrivateKeyFilename( config.getStringValue( CONFIGURATION_SPEPPRIVATEKEYFILENAME ) );
	std::string metadataPublicKeyFilename( config.getStringValue( CONFIGURATION_METADATAPUBLICKEYFILENAME ) );
	
	// Create a new key resolver and load the key files from disk.
	this->_keyResolver = KeyResolver( keyPath, spepKeyAlias );
	this->_keyResolver.loadMetadataKey( metadataPublicKeyFilename );
	this->_keyResolver.loadSPEPPublicKey( spepPublicKeyFilename );
	this->_keyResolver.loadSPEPPrivateKey( spepPrivateKeyFilename );
}

spep::SPEPConfigData& spep::SPEPConfigData::operator=( const spep::SPEPConfigData &other )
{
	this->_started = other._started;
	this->_defaultUrl = other._defaultUrl;
	this->_loginRedirect = other._loginRedirect;
	this->_tokenName = other._tokenName;
	this->_tokenDomain = other._tokenDomain;
	this->_schemaPath = other._schemaPath;
	this->_attributeNamePrefix = other._attributeNamePrefix;
	this->_attributeValueSeparator = other._attributeValueSeparator;
	this->_usernameAttribute = other._usernameAttribute;
	this->_caBundle = other._caBundle;
	this->_spepIdentifier = other._spepIdentifier;
	this->_esoeIdentifier = other._esoeIdentifier;
	this->_attributeConsumingServiceIndex = other._attributeConsumingServiceIndex;
	this->_assertionConsumerServiceIndex = other._assertionConsumerServiceIndex;
	this->_allowedTimeSkew = other._allowedTimeSkew;
	this->_logoutClearCookies = other._logoutClearCookies;
	this->_ipAddresses = other._ipAddresses;
	this->_keyResolver = other._keyResolver;
	this->_attributeRenameMap = other._attributeRenameMap;
	
	return *this;
}

bool spep::SPEPConfigData::isStarted()
{
	return this->_started;
}

void spep::SPEPConfigData::setStarted( bool started )
{
	this->_started = started;
}

std::string spep::SPEPConfigData::getDefaultUrl()
{
	return this->_defaultUrl;
}

std::string spep::SPEPConfigData::getLoginRedirect()
{
	return this->_loginRedirect;
}

std::string spep::SPEPConfigData::getTokenName()
{
	return this->_tokenName;
}

std::string spep::SPEPConfigData::getTokenDomain()
{
	return this->_tokenDomain;
}

std::string spep::SPEPConfigData::getSchemaPath()
{
	return this->_schemaPath;
}

std::string spep::SPEPConfigData::getAttributeNamePrefix()
{
	return this->_attributeNamePrefix;
}

std::string spep::SPEPConfigData::getAttributeValueSeparator()
{
	return this->_attributeValueSeparator;
}

std::string spep::SPEPConfigData::getUsernameAttribute()
{
	return this->_usernameAttribute;
}

std::string spep::SPEPConfigData::getCABundle()
{
	return this->_caBundle;
}

std::wstring spep::SPEPConfigData::getSPEPIdentifier()
{
	return this->_spepIdentifier;
}

std::wstring spep::SPEPConfigData::getESOEIdentifier()
{
	return this->_esoeIdentifier;
}

int spep::SPEPConfigData::getAttributeConsumingServiceIndex()
{
	return this->_attributeConsumingServiceIndex;
}

int spep::SPEPConfigData::getAssertionConsumerServiceIndex()
{
	return this->_assertionConsumerServiceIndex;
}

int spep::SPEPConfigData::getAllowedTimeSkew()
{
	return this->_allowedTimeSkew;
}

const std::vector<std::string>& spep::SPEPConfigData::getLogoutClearCookies()
{
	return this->_logoutClearCookies;
}

const std::vector<std::wstring>& spep::SPEPConfigData::getIPAddresses()
{
	return this->_ipAddresses;
}

spep::KeyResolver* spep::SPEPConfigData::getKeyResolver()
{
	return &(this->_keyResolver);
}

const std::map<std::string,std::string>& spep::SPEPConfigData::getAttributeRenameMap()
{
	return this->_attributeRenameMap;
}
