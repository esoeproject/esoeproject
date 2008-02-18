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
 * Creation Date: 26/02/2007
 * 
 * Purpose: 
 */

 
#include "spep/metadata/KeyResolver.h"
#include "spep/metadata/JKSKeystore.h"
#include "spep/exceptions/KeyResolverException.h"

#include "spep/Util.h"
#include "spep/Base64.h"

#include <openssl/x509.h>

spep::KeyResolver::KeyResolver()
:
_spepPublicKey(NULL),
_spepPublicKeyData(NULL),
_spepPublicKeyLength(0),
_spepPrivateKey(NULL),
_spepPrivateKeyData(NULL),
_spepPrivateKeyLength(0),
_metadataKey(NULL),
_metadataKeyData(NULL),
_metadataKeyLength(0)
{
}

spep::KeyResolver::~KeyResolver()
{
	this->deleteKeys();
}

spep::KeyResolver::KeyResolver( const spep::KeyResolver &other )
:
_spepPublicKey(NULL),
_spepPublicKeyData(NULL),
_spepPublicKeyLength(0),
_spepPrivateKey(NULL),
_spepPrivateKeyData(NULL),
_spepPrivateKeyLength(0),
_metadataKey(NULL),
_metadataKeyData(NULL),
_metadataKeyLength(0),
_spepKeyAlias( other._spepKeyAlias )
{
	if( other._spepPublicKeyData != NULL )
	{
		_spepPublicKeyLength = other._spepPublicKeyLength;
		_spepPublicKeyData = new char[ _spepPublicKeyLength ];
		std::memcpy( _spepPublicKeyData, other._spepPublicKeyData, _spepPublicKeyLength );
	}
	
	if( other._spepPrivateKeyData != NULL )
	{
		_spepPrivateKeyLength = other._spepPrivateKeyLength;
		_spepPrivateKeyData = new char[ _spepPrivateKeyLength ];
		std::memcpy( _spepPrivateKeyData, other._spepPrivateKeyData, _spepPrivateKeyLength );
	}
	
	if( other._metadataKeyData != NULL )
	{
		_metadataKeyLength = other._metadataKeyLength;
		_metadataKeyData = new char[ _metadataKeyLength ];
		std::memcpy( _metadataKeyData, other._metadataKeyData, _metadataKeyLength );
	}
	
	if( other._spepPublicKey != NULL )
	{
		_spepPublicKey = other._spepPublicKey->clone();
	}
	
	if( other._spepPrivateKey != NULL )
	{
		_spepPrivateKey = other._spepPrivateKey->clone();
	}
	
	if( other._metadataKey != NULL )
	{
		_metadataKey = other._metadataKey->clone();
	}
}

spep::KeyResolver& spep::KeyResolver::operator=( const spep::KeyResolver &other )
{
	this->deleteKeys();

	if( other._spepPublicKeyData != NULL )
	{
		_spepPublicKeyLength = other._spepPublicKeyLength;
		_spepPublicKeyData = new char[ _spepPublicKeyLength ];
		std::memcpy( _spepPublicKeyData, other._spepPublicKeyData, _spepPublicKeyLength );
	}
	
	if( other._spepPrivateKeyData != NULL )
	{
		_spepPrivateKeyLength = other._spepPrivateKeyLength;
		_spepPrivateKeyData = new char[ _spepPrivateKeyLength ];
		std::memcpy( _spepPrivateKeyData, other._spepPrivateKeyData, _spepPrivateKeyLength );
	}
	
	if( other._metadataKeyData != NULL )
	{
		_metadataKeyLength = other._metadataKeyLength;
		_metadataKeyData = new char[ _metadataKeyLength ];
		std::memcpy( _metadataKeyData, other._metadataKeyData, _metadataKeyLength );
	}
	
	if( other._spepPublicKey != NULL )
	{
		_spepPublicKey = other._spepPublicKey->clone();
	}
	
	if( other._spepPrivateKey != NULL )
	{
		_spepPrivateKey = other._spepPrivateKey->clone();
	}
	
	if( other._metadataKey != NULL )
	{
		_metadataKey = other._metadataKey->clone();
	}
	if( other._spepPublicKey != NULL )
	{
		_spepPublicKey = other._spepPublicKey->clone();
	}
	else
	{
		_spepPublicKey = NULL;
	}
	
	if( other._spepPrivateKey != NULL )
	{
		_spepPrivateKey = other._spepPrivateKey->clone();
	}
	else
	{
		_spepPrivateKey = NULL;
	}
	
	if( other._metadataKey != NULL )
	{
		_metadataKey = other._metadataKey->clone();
	}
	else
	{
		_metadataKey = NULL;
	}
	
	_spepKeyAlias = other._spepKeyAlias;
	
	return *this;
}

spep::KeyResolver::KeyResolver( std::string keystorePath, std::string keystorePassword, std::string spepKeyAlias, std::string spepKeyPassword, std::string metadataKeyAlias )
:
_spepPublicKey(NULL),
_spepPublicKeyData(NULL),
_spepPublicKeyLength(0),
_spepPrivateKey(NULL),
_spepPrivateKeyData(NULL),
_spepPrivateKeyLength(0),
_metadataKey(NULL),
_metadataKeyData(NULL),
_metadataKeyLength(0),
_spepKeyAlias(spepKeyAlias)
{
	//std::map<std::string,std::string> passwordMap;
	//passwordMap[spepKeyAlias] = spepKeyPassword;
	//JKSKeystore keystore( keystorePath, keystorePassword, passwordMap );
	JKSKeystore keystore( keystorePath, keystorePassword, spepKeyPassword );
	
	const JKSPrivateKeyData *spepPkeyData = keystore.getKeyData( spepKeyAlias );
	const JKSTrustedCertData *spepCertData = &(keystore.getCertificateChain( spepKeyAlias ).at(0));
	const JKSTrustedCertData *metadataCertData = keystore.getCertificateData( metadataKeyAlias );
	
	{
		Base64Encoder encoder;
		encoder.push( reinterpret_cast<const char*>(spepCertData->data), spepCertData->len );
		encoder.close();
		Base64Document cert = encoder.getResult();
		
		_spepPublicKeyLength = cert.getLength();
		_spepPublicKeyData = new char[ _spepPublicKeyLength ];
		std::memcpy( _spepPublicKeyData, cert.getData(), _spepPublicKeyLength );
	}
	
	_spepPrivateKeyLength = spepPkeyData->len;
	_spepPrivateKeyData = new char[ _spepPrivateKeyLength ];
	std::memcpy( _spepPrivateKeyData, spepPkeyData->data, _spepPrivateKeyLength );
	
	{
		Base64Encoder encoder;
		encoder.push( reinterpret_cast<const char*>(metadataCertData->data), metadataCertData->len );
		encoder.close();
		Base64Document cert = encoder.getResult();
		
		_metadataKeyLength = cert.getLength();
		_metadataKeyData = new char[ _metadataKeyLength ];
		std::memcpy( _metadataKeyData, cert.getData(), _metadataKeyLength );
	}
}

void spep::KeyResolver::deleteKeys()
{
	if( _spepPublicKeyData != NULL )
	{
		delete[] _spepPublicKeyData;
	}
	
	if( _spepPublicKey != NULL )
	{
		delete _spepPublicKey;
	}
	
	if( _spepPrivateKeyData != NULL )
	{
		delete[] _spepPrivateKeyData;
	}
	
	if( _spepPrivateKey != NULL )
	{
		delete _spepPrivateKey;
	}
	
	if( _metadataKeyData != NULL )
	{
		delete[] _metadataKeyData;
	}
	
	if( _metadataKey != NULL )
	{
		delete _metadataKey;
	}
}

void spep::KeyResolver::loadSPEPPublicKey()
{
	if( _spepPublicKey != NULL )
	{
		delete _spepPublicKey;
	}
	
	// OpenSSLCryptoX509 object to hold the key until it is cloned.
	std::auto_ptr<OpenSSLCryptoX509> x509( new OpenSSLCryptoX509() );
	x509->loadX509Base64Bin( reinterpret_cast<const char*>(this->_spepPublicKeyData), this->_spepPublicKeyLength );
	
	_spepPublicKey = x509->clonePublicKey();
}
#include <boost/lexical_cast.hpp>
void spep::KeyResolver::loadSPEPPrivateKey()
{	
	if( _spepPrivateKey != NULL )
	{
		delete _spepPrivateKey;
	}
	
	// Read the key data into an OpenSSL RSA key.
	BIO* bioMem = BIO_new_mem_buf( this->_spepPrivateKeyData, this->_spepPrivateKeyLength );
	
	/* 
	 * TODO Find out how much (if any) leaks from here.
	 */
	PKCS8_PRIV_KEY_INFO *pkeyInfo = NULL;
	d2i_PKCS8_PRIV_KEY_INFO_bio( bioMem, &pkeyInfo );
	
	if( pkeyInfo == NULL )
	{
		throw std::exception();
	}
	
	EVP_PKEY* rawkey = EVP_PKCS82PKEY( pkeyInfo );
	_spepPrivateKey = new OpenSSLCryptoKeyRSA( rawkey );
	
	BIO_free( bioMem );
}

void spep::KeyResolver::loadMetadataKey()
{
	if( _spepPublicKey != NULL )
	{
		delete _spepPublicKey;
	}

	// OpenSSLCryptoX509 object to hold the key until it is cloned.
	std::auto_ptr<OpenSSLCryptoX509> x509( new OpenSSLCryptoX509() );
	x509->loadX509Base64Bin( reinterpret_cast<const char*>(this->_metadataKeyData), this->_metadataKeyLength );
	
	_metadataKey = x509->clonePublicKey();
}

XSECCryptoKey* spep::KeyResolver::getSPEPPublicKey()
{
	if( !_spepPublicKey )
		loadSPEPPublicKey();
	return _spepPublicKey;
}

XSECCryptoKey* spep::KeyResolver::getSPEPPrivateKey()
{
	if( !_spepPrivateKey )
		loadSPEPPrivateKey();
	return _spepPrivateKey;
}

std::string spep::KeyResolver::getSPEPKeyAlias()
{
	return _spepKeyAlias;
}

XSECCryptoKey* spep::KeyResolver::getMetadataKey()
{
	if( !_metadataKey )
		loadMetadataKey();
	return _metadataKey;
}
