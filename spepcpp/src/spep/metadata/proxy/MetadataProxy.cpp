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
 * Creation Date: 15/03/2007
 * 
 * Purpose: 
 */

#include "spep/metadata/proxy/MetadataProxy.h"
#include "spep/metadata/proxy/MetadataDispatcher.h"

#include "saml2/exceptions/InvalidParameterException.h"

static const char *getSPEPIdentifier = METADATA_getSPEPIdentifier;
static const char *getESOEIdentifier = METADATA_getESOEIdentifier;
static const char *getSingleSignOnEndpoint = METADATA_getSingleSignOnEndpoint;
static const char *getSingleLogoutEndpoint = METADATA_getSingleLogoutEndpoint;
static const char *getAttributeServiceEndpoint = METADATA_getAttributeServiceEndpoint;
static const char *getAuthzServiceEndpoint = METADATA_getAuthzServiceEndpoint;
static const char *getSPEPStartupServiceEndpoint = METADATA_getSPEPStartupServiceEndpoint;
static const char *resolveKey = METADATA_resolveKey;

spep::ipc::MetadataProxy::MetadataProxy( spep::ipc::ClientSocketPool *socketPool )
:
_socketPool( socketPool )
{
}

spep::ipc::MetadataProxy::~MetadataProxy()
{
	CryptoKeyPointerList::iterator iter;
	for( iter = _cryptoKeyList.begin(); iter != _cryptoKeyList.end(); ++iter )
	{
		// TODO Investigate if we need to delete keys here.
		//XSECCryptoKey *key = *iter;
		// They shouldn't be in the list if they're NULL.
		//if( key != NULL )
		//	delete key;
	}
}

const std::wstring spep::ipc::MetadataProxy::getSPEPIdentifier() const
{
	std::string dispatch( ::getSPEPIdentifier );
	spep::ipc::NoData noData;
	
	ClientSocketLease clientSocket( _socketPool );
	return clientSocket->makeRequest< std::wstring >( dispatch, noData );
}

const std::wstring spep::ipc::MetadataProxy::getESOEIdentifier() const
{
	std::string dispatch( ::getESOEIdentifier );
	spep::ipc::NoData noData;
	
	ClientSocketLease clientSocket( _socketPool );
	return clientSocket->makeRequest< std::wstring >( dispatch, noData );
}

const std::string spep::ipc::MetadataProxy::getSingleSignOnEndpoint() const
{
	std::string dispatch( ::getSingleSignOnEndpoint );
	spep::ipc::NoData noData;
	
	ClientSocketLease clientSocket( _socketPool );
	return clientSocket->makeRequest< std::string >( dispatch, noData );
}

const std::string spep::ipc::MetadataProxy::getSingleLogoutEndpoint() const
{
	std::string dispatch( ::getSingleLogoutEndpoint );
	spep::ipc::NoData noData;
	
	ClientSocketLease clientSocket( _socketPool );
	return clientSocket->makeRequest< std::string >( dispatch, noData );
}

const std::string spep::ipc::MetadataProxy::getAttributeServiceEndpoint() const
{
	std::string dispatch( ::getAttributeServiceEndpoint );
	spep::ipc::NoData noData;
	
	ClientSocketLease clientSocket( _socketPool );
	return clientSocket->makeRequest< std::string >( dispatch, noData );
}

const std::string spep::ipc::MetadataProxy::getAuthzServiceEndpoint() const
{
	std::string dispatch( ::getAuthzServiceEndpoint );
	spep::ipc::NoData noData;
	
	ClientSocketLease clientSocket( _socketPool );
	return clientSocket->makeRequest< std::string >( dispatch, noData );
}

const std::string spep::ipc::MetadataProxy::getSPEPStartupServiceEndpoint() const
{
	std::string dispatch( ::getSPEPStartupServiceEndpoint );
	spep::ipc::NoData noData;
	
	ClientSocketLease clientSocket( _socketPool );
	return clientSocket->makeRequest< std::string >( dispatch, noData );
}

XSECCryptoKey *spep::ipc::MetadataProxy::resolveKey (DSIGKeyInfoList *lst)
{
	
	if ( lst->getSize() < 1 )
	{
		SAML2LIB_INVPARAM_EX( "List did not have at least one keyinfo" );
	}
	
	DSIGKeyInfo *keyInfo = lst->item(0);
	const XMLCh *keyNameXMLString = keyInfo->getKeyName();
	std::auto_ptr< XercesCharStringAdapter > keyNameChars( new XercesCharStringAdapter( XMLString::transcode( keyNameXMLString ) ) );
	
	std::string keyName( keyNameChars->get(), XMLString::stringLen( keyNameXMLString ) );
	
	saml2::KeyData keyData( this->resolveKey( keyName ) );
	
	// TODO Memory leak here?
	// This XSECCryptoKey must be deleted in this class, since xml-security-c won't.
	XSECCryptoKey *cryptoKey = keyData.createXSECCryptoKey();
	// Store the pointer in a list to be destructed when this class goes out of scope.
	
	if( cryptoKey != NULL )
	{
		_cryptoKeyList.push_back( cryptoKey );
	}
	
	return cryptoKey;
	
}

saml2::KeyData spep::ipc::MetadataProxy::resolveKey (std::string keyName)
{
	std::string dispatch( ::resolveKey );
	
	ClientSocketLease clientSocket( _socketPool );
	return clientSocket->makeRequest< saml2::KeyData >( dispatch, keyName );
}

XSECKeyInfoResolver* spep::ipc::MetadataProxy::clone() const
{
	return new MetadataProxy( this->_socketPool );
}
