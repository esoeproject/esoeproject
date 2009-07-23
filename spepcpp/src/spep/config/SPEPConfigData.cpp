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

#include <algorithm>

spep::SPEPConfigData::SPEPConfigData()
:
_started( false )
{
}

spep::SPEPConfigData::SPEPConfigData( const spep::SPEPConfigData &other )
:
_started( other._started ),
_lazyInit( other._lazyInit ),
_lazyInitDefaultPermit( other._lazyInitDefaultPermit ),
_disableAttributeQuery( other._disableAttributeQuery ),
_disablePolicyEnforcement( other._disablePolicyEnforcement ),
_disableSPEPStartup( other._disableSPEPStartup ),
_defaultUrl( other._defaultUrl ),
_loginRedirect( other._loginRedirect ),
_ssoRedirect( other._ssoRedirect ),
_tokenName( other._tokenName ),
_schemaPath( other._schemaPath ),
_attributeNamePrefix( other._attributeNamePrefix ),
_attributeValueSeparator( other._attributeValueSeparator ),
_usernameAttribute( other._usernameAttribute ),
_caBundle( other._caBundle ),
_globalESOECookieName( other._globalESOECookieName ),
_serviceHost( other._serviceHost ),
_spepIdentifier( other._spepIdentifier ),
_esoeIdentifier( other._esoeIdentifier ),
_attributeConsumingServiceIndex( other._attributeConsumingServiceIndex ),
_assertionConsumerServiceIndex( other._assertionConsumerServiceIndex ),
_allowedTimeSkew( other._allowedTimeSkew ),
_logoutClearCookies( other._logoutClearCookies ),
_ipAddresses( other._ipAddresses ),
_lazyInitResources( other._lazyInitResources ),
_keyResolver( other._keyResolver ),
_attributeRenameMap( other._attributeRenameMap )
{
}

spep::SPEPConfigData::SPEPConfigData( const spep::ConfigurationReader& config )
{
	this->_started = false;
	
	// Grab the required config values from the ConfigurationReader instance.
	std::string lazyInitString( config.getStringValue( CONFIGURATION_LAZYINIT ) );
	std::string disableAttributeQueryString( config.getStringValue( CONFIGURATION_DISABLEATTRIBUTEQUERY, "false" ) );
	std::string disablePolicyEnforcementString( config.getStringValue( CONFIGURATION_DISABLEPOLICYENFORCEMENT, "false" ) );
	std::string disableSPEPStartupString( config.getStringValue( CONFIGURATION_DISABLESPEPSTARTUP, "false" ) );
	std::string lazyInitDefaultAction( config.getStringValue( CONFIGURATION_LAZYINITDEFAULTACTION, std::string() ) );
	
	std::transform( lazyInitString.begin(), lazyInitString.end(), lazyInitString.begin(), ::tolower );
	std::transform( disableAttributeQueryString.begin(), disableAttributeQueryString.end(), disableAttributeQueryString.begin(), ::tolower );
	std::transform( disablePolicyEnforcementString.begin(), disablePolicyEnforcementString.end(), disablePolicyEnforcementString.begin(), ::tolower );
	std::transform( disableSPEPStartupString.begin(), disableSPEPStartupString.end(), disableSPEPStartupString.begin(), ::tolower );
	std::transform( lazyInitDefaultAction.begin(), lazyInitDefaultAction.end(), lazyInitDefaultAction.begin(), ::tolower );
	
	this->_lazyInit = ( lazyInitString.compare( "true" ) == 0 );
	this->_lazyInitDefaultPermit = ( lazyInitDefaultAction.compare( "permit" ) == 0 );
	this->_defaultUrl = config.getStringValue( CONFIGURATION_DEFAULTURL );
	
	this->_disableAttributeQuery = ( disableAttributeQueryString.compare( "true" ) == 0 );
	this->_disablePolicyEnforcement = ( disablePolicyEnforcementString.compare( "true" ) == 0 );
	this->_disableSPEPStartup = ( disableSPEPStartupString.compare( "true" ) == 0 );
	
	this->_loginRedirect = config.getStringValue( CONFIGURATION_LOGINREDIRECT );
	this->_ssoRedirect = config.getStringValue( CONFIGURATION_SSOREDIRECT );
	this->_tokenName = config.getStringValue( CONFIGURATION_SPEPTOKENNAME );
	this->_schemaPath = config.getStringValue( CONFIGURATION_SCHEMAPATH );
	this->_attributeNamePrefix = config.getStringValue( CONFIGURATION_ATTRIBUTENAMEPREFIX );
	this->_attributeValueSeparator = config.getStringValue( CONFIGURATION_ATTRIBUTEVALUESEPARATOR );
	this->_usernameAttribute = config.getStringValue( CONFIGURATION_USERNAMEATTRIBUTE );
	this->_caBundle = config.getStringValue( CONFIGURATION_CABUNDLE, std::string() ); // Default value empty string
	this->_globalESOECookieName = config.getStringValue( CONFIGURATION_COMMONDOMAINTOKENNAME );
	this->_serviceHost = config.getStringValue( CONFIGURATION_SERVICEHOST );
	this->_spepIdentifier = UnicodeStringConversion::toWString( config.getStringValue( CONFIGURATION_SPEPIDENTIFIER ) );
	this->_esoeIdentifier = UnicodeStringConversion::toWString( config.getStringValue( CONFIGURATION_ESOEIDENTIFIER ) );
	this->_attributeConsumingServiceIndex = config.getIntegerValue( CONFIGURATION_ATTRIBUTECONSUMINGSERVICEINDEX );
	this->_assertionConsumerServiceIndex = config.getIntegerValue( CONFIGURATION_ASSERTIONCONSUMERSERVICEINDEX );
	// Allow no entries to be entered here.
	this->_logoutClearCookies = config.getMultiValue( CONFIGURATION_LOGOUTCLEARCOOKIE, 0 );
	
	std::vector<std::string> lazyInitResources = config.getMultiValue( CONFIGURATION_LAZYINITRESOURCE, 0 );
	for( std::vector<std::string>::iterator iter = lazyInitResources.begin(); iter != lazyInitResources.end(); ++iter )
	{
		this->_lazyInitResources.push_back( UnicodeStringConversion::toUnicodeString( *iter ) );
	}
	
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

	std::string keystorePath( config.getStringValue( CONFIGURATION_KEYSTOREPATH ) );
	std::string keystorePassword( config.getStringValue( CONFIGURATION_KEYSTOREPASSWORD ) );
	std::string spepKeyAlias( config.getStringValue( CONFIGURATION_SPEPKEYALIAS ) );
	std::string spepKeyPassword( config.getStringValue( CONFIGURATION_SPEPKEYPASSWORD ) );
	
	// Create a new key resolver and load the keystore.
	this->_keyResolver = KeyResolver( keystorePath, keystorePassword, spepKeyAlias, spepKeyPassword );
}

spep::SPEPConfigData& spep::SPEPConfigData::operator=( const spep::SPEPConfigData &other )
{
	this->_started = other._started;
	this->_lazyInit = other._lazyInit;
	this->_lazyInitDefaultPermit = other._lazyInitDefaultPermit;
	this->_disableAttributeQuery = other._disableAttributeQuery;
	this->_disablePolicyEnforcement = other._disablePolicyEnforcement;
	this->_disableSPEPStartup = other._disableSPEPStartup;
	this->_defaultUrl = other._defaultUrl;
	this->_loginRedirect = other._loginRedirect;
	this->_ssoRedirect = other._ssoRedirect;
	this->_tokenName = other._tokenName;
	this->_schemaPath = other._schemaPath;
	this->_attributeNamePrefix = other._attributeNamePrefix;
	this->_attributeValueSeparator = other._attributeValueSeparator;
	this->_usernameAttribute = other._usernameAttribute;
	this->_caBundle = other._caBundle;
	this->_globalESOECookieName = other._globalESOECookieName;
	this->_serviceHost = other._serviceHost;
	this->_spepIdentifier = other._spepIdentifier;
	this->_esoeIdentifier = other._esoeIdentifier;
	this->_attributeConsumingServiceIndex = other._attributeConsumingServiceIndex;
	this->_assertionConsumerServiceIndex = other._assertionConsumerServiceIndex;
	this->_allowedTimeSkew = other._allowedTimeSkew;
	this->_logoutClearCookies = other._logoutClearCookies;
	this->_ipAddresses = other._ipAddresses;
	this->_lazyInitResources = other._lazyInitResources;
	this->_keyResolver = other._keyResolver;
	this->_attributeRenameMap = other._attributeRenameMap;
	
	return *this;
}

bool spep::SPEPConfigData::isStarted()
{
	if( this->_disableSPEPStartup ) return true;
	
	return this->_started;
}

bool spep::SPEPConfigData::isLazyInit()
{
	return this->_lazyInit;
}

bool spep::SPEPConfigData::isLazyInitDefaultPermit()
{
	return this->_lazyInitDefaultPermit;
}

bool spep::SPEPConfigData::disableAttributeQuery()
{
	return this->_disableAttributeQuery;
}

bool spep::SPEPConfigData::disablePolicyEnforcement()
{
	return this->_disablePolicyEnforcement;
}

bool spep::SPEPConfigData::disableSPEPStartup()
{
	return this->_disableSPEPStartup;
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

std::string spep::SPEPConfigData::getSSORedirect()
{
	return this->_ssoRedirect;
}

std::string spep::SPEPConfigData::getTokenName()
{
	return this->_tokenName;
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

std::string spep::SPEPConfigData::getGlobalESOECookieName()
{
	return this->_globalESOECookieName;
}

std::string spep::SPEPConfigData::getServiceHost()
{
	return this->_serviceHost;
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

const std::vector<UnicodeString>& spep::SPEPConfigData::getLazyInitResources()
{
	return this->_lazyInitResources;
}

spep::KeyResolver* spep::SPEPConfigData::getKeyResolver()
{
	return &(this->_keyResolver);
}

const std::map<std::string,std::string>& spep::SPEPConfigData::getAttributeRenameMap()
{
	return this->_attributeRenameMap;
}
