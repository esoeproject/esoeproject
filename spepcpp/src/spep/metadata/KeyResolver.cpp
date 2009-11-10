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

#include <xsec/dsig/DSIGKeyInfo.hpp>
#include <xsec/dsig/DSIGKeyInfoName.hpp>

spep::KeyResolver::KeyResolver()
:
_spepPublicKey(NULL),
_spepPublicKeyB64(),
_spepPrivateKey(NULL),
_spepPrivateKeyB64(),
_trustedCerts()
{
}

spep::KeyResolver::~KeyResolver()
{
	this->deleteKeys();
}

spep::KeyResolver::KeyResolver( const spep::KeyResolver &other )
:
_spepPublicKey(NULL),
_spepPublicKeyB64( other._spepPublicKeyB64 ),
_spepPrivateKey(NULL),
_spepPrivateKeyB64( other._spepPrivateKeyB64 ),
_spepKeyAlias( other._spepKeyAlias ),
_trustedCerts( other._trustedCerts )
{
	if( other._spepPublicKey != NULL )
	{
		_spepPublicKey = other._spepPublicKey->clone();
	}
	
	if( other._spepPrivateKey != NULL )
	{
		_spepPrivateKey = other._spepPrivateKey->clone();
	}
}

spep::KeyResolver& spep::KeyResolver::operator=( const spep::KeyResolver &other )
{
	this->deleteKeys();

	if( other._spepPublicKey != NULL )
	{
		_spepPublicKey = other._spepPublicKey->clone();
	}

	_spepPublicKeyB64 = other._spepPublicKeyB64;
	
	if( other._spepPrivateKey != NULL )
	{
		_spepPrivateKey = other._spepPrivateKey->clone();
	}

	_spepPrivateKeyB64 = other._spepPrivateKeyB64;
	
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
	
	_spepKeyAlias = other._spepKeyAlias;

	_trustedCerts = other._trustedCerts;
	
	return *this;
}

spep::KeyResolver::KeyResolver( std::string keystorePath, std::string keystorePassword, std::string spepKeyAlias, std::string spepKeyPassword )
:
_spepPublicKey(NULL),
_spepPublicKeyB64(),
_spepPrivateKey(NULL),
_spepPrivateKeyB64(),
_spepKeyAlias(spepKeyAlias),
_trustedCerts()
{
	JKSKeystore keystore( keystorePath, keystorePassword, spepKeyPassword );
	
	const JKSPrivateKeyData *spepPkeyData = keystore.getKeyData( spepKeyAlias );
	const JKSTrustedCertData *spepCertData = &(keystore.getCertificateChain( spepKeyAlias ).at(0));
	
	{
		Base64Encoder encoder;
		encoder.push( reinterpret_cast<const char*>(spepCertData->data), spepCertData->len );
		encoder.close();
		Base64Document cert = encoder.getResult();
		
		_spepPublicKeyB64.assign(cert.getData(), cert.getLength());
	}

	{
		Base64Encoder encoder;
		encoder.push( reinterpret_cast<const char*>(spepPkeyData->data), spepPkeyData->len );
		encoder.close();
		Base64Document key = encoder.getResult();

		_spepPrivateKeyB64.assign(key.getData(), key.getLength());
	}
	std::vector<std::string> trustedCertAliases( keystore.getCertificateAliases() );
	for( std::vector<std::string>::iterator iter = trustedCertAliases.begin();
		iter != trustedCertAliases.end(); ++iter )
	{
		std::string keyAlias(*iter);
		const JKSTrustedCertData *certData = keystore.getCertificateData( keyAlias );

		Base64Encoder encoder;
		encoder.push( reinterpret_cast<const char*>(certData->data), certData->len );
		encoder.close();
		Base64Document cert = encoder.getResult();

		std::string encodedCert( cert.getData(), cert.getLength() );

		_trustedCerts[keyAlias] = encodedCert;
	}
}

void spep::KeyResolver::deleteKeys()
{
	if( _spepPublicKey != NULL )
	{
		delete _spepPublicKey;
	}
	
	if( _spepPrivateKey != NULL )
	{
		delete _spepPrivateKey;
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
	x509->loadX509Base64Bin( _spepPublicKeyB64.c_str(), _spepPublicKeyB64.length() );
	
	_spepPublicKey = x509->clonePublicKey();
}

void spep::KeyResolver::loadSPEPPrivateKey()
{	
	if( _spepPrivateKey != NULL )
	{
		delete _spepPrivateKey;
	}

	Base64Decoder decoder;
	decoder.push( _spepPrivateKeyB64.c_str(), _spepPrivateKeyB64.length() );
	decoder.close();
	Base64Document pkey = decoder.getResult();
	
	// Read the key data into an OpenSSL RSA key.
	BIO* bioMem = BIO_new_mem_buf( const_cast<char*>(pkey.getData()), pkey.getLength() );
	
	PKCS8_PRIV_KEY_INFO *pkeyInfo = NULL;
	// TODO This line leaks 4 blocks. (2918 bytes)
	d2i_PKCS8_PRIV_KEY_INFO_bio( bioMem, &pkeyInfo );
	
	if( pkeyInfo == NULL )
	{
		throw std::exception();
	}
	
	EVP_PKEY* rawkey = EVP_PKCS82PKEY( pkeyInfo );
	_spepPrivateKey = new OpenSSLCryptoKeyRSA( rawkey );
	
	BIO_free( bioMem );
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

XSECCryptoKey *spep::KeyResolver::resolveKey (DSIGKeyInfoList *list)
{
	if( list->isEmpty() )
		return NULL;

	// Loop through the key info list and look for a name.
	for( DSIGKeyInfoList::size_type i = 0; i < list->getSize(); ++i )
	{
		DSIGKeyInfo *keyInfo = list->item(i);
		if( keyInfo->getKeyInfoType() != DSIGKeyInfo::KEYINFO_NAME ) continue;

		// This keyInfo is a key name, so cast it and grab the name as a Xerces char*
		DSIGKeyInfoName* keyInfoName = reinterpret_cast<DSIGKeyInfoName*>( keyInfo );
		std::auto_ptr<XercesCharStringAdapter> keyNameChars( new XercesCharStringAdapter( XMLString::transcode( keyInfoName->getKeyName() ) ) );

		std::string keyName( keyNameChars->get() );

		try
		{
			XSECCryptoKey* key = this->resolveKey( keyName );
			return key;
		}
		catch( std::exception e )
		{
		}
	}

	// No key data found/returned. Return null now.
	return NULL;
}

XSECCryptoKey* spep::KeyResolver::resolveKey(std::string keyName)
{
	std::map<std::string,std::string>::iterator iter =  _trustedCerts.find(keyName);
	if (iter == _trustedCerts.end()) return NULL;

	std::auto_ptr<OpenSSLCryptoX509> x509( new OpenSSLCryptoX509() );
	x509->loadX509Base64Bin( reinterpret_cast<const char*>(iter->second.c_str()), iter->second.length() );

	XSECCryptoKey *key = x509->clonePublicKey();
	return key;
}

XSECKeyInfoResolver* spep::KeyResolver::clone() const
{
	return new spep::KeyResolver(*this);
}

