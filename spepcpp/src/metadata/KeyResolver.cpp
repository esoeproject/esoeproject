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

 
#include "metadata/KeyResolver.h"
#include "exceptions/KeyResolverException.h"

#include "Util.h"

spep::KeyResolver::KeyResolver()
:
_spepPublicKey(NULL),
_spepPrivateKey(NULL),
_metadataKey(NULL)
{
}

spep::KeyResolver::~KeyResolver()
{
	this->deleteKeys();
}

spep::KeyResolver::KeyResolver( const spep::KeyResolver &other )
:
_spepPublicKey(NULL),
_spepPublicKeyData( other._spepPublicKeyData ),
_spepPrivateKey(NULL),
_spepPrivateKeyData( other._spepPrivateKeyData ),
_metadataKey(NULL),
_metadataKeyData( other._metadataKeyData ),
_spepKeyName( other._spepKeyName ),
_path( other._path )
{
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
	_spepPublicKeyData = other._spepPublicKeyData;
	_spepPrivateKeyData = other._spepPrivateKeyData;
	_metadataKeyData = other._metadataKeyData;
	_spepKeyName = other._spepKeyName;
	_path = other._path;

	this->deleteKeys();

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
	
	return *this;
}

spep::KeyResolver::KeyResolver( std::string path, std::string spepKeyName )
:
_spepPublicKey(NULL),
_spepPrivateKey(NULL),
_metadataKey(NULL),
_spepKeyName(spepKeyName),
_path(path)
{
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
	
	if( _metadataKey != NULL )
	{
		delete _metadataKey;
	}
}

bool spep::KeyResolver::loadKey( char *&keyData, int &fsize, std::string &file )
{
	try
	{
		// Construct the path and open the file.
		std::string fullpath = _path + PATH_SEPARATOR_CHAR + file;
		std::ifstream keyInput;
		keyInput.open( fullpath.c_str() );
		
		// Figure out how long the file is and allocate a buffer to load it.
		keyInput.seekg( 0, std::ios::end );
		fsize = keyInput.tellg();
		keyData = new char[fsize + 1];
		
		// Seek to the beginning and read the file into the buffer.
		keyInput.seekg( 0, std::ios::beg );
		keyInput.read( keyData, fsize );
		keyInput.close();
		keyData[fsize] = '\0';
	}
	catch (std::exception &ex)
	{
		// TODO Handle better
		return false;
	}
	
	return true;
}

void spep::KeyResolver::loadSPEPPublicKey()
{
	if( _spepPublicKey != NULL )
	{
		delete _spepPublicKey;
	}
	
	// OpenSSLCryptoX509 object to hold the key until it is cloned.
	std::auto_ptr<OpenSSLCryptoX509> x509( new OpenSSLCryptoX509() );
	x509->loadX509PEM( this->_spepPublicKeyData.c_str(), this->_spepPublicKeyData.length() );
	
	_spepPublicKey = x509->clonePublicKey();
}

void spep::KeyResolver::loadSPEPPublicKey( std::string file )
{
	// TODO What if there's already an SPEP public key loaded?
	
	char* keyData;
	int fsize;
	
	bool result = this->loadKey( keyData, fsize, file );
	// Make sure it will get deleted if we throw.
	AutoArray<char> keyDataAutoArray( keyData );
	
	if (!result)
	{
		throw KeyResolverException( "Couldn't load key from file. An error occurred." );
	}
	
	this->_spepPublicKeyData = std::string( keyData, fsize+1 );
	
	this->loadSPEPPublicKey();
}

void spep::KeyResolver::loadSPEPPrivateKey( std::string file )
{
	char* keyData = NULL;
	int fsize = 0;
	
	bool result = this->loadKey( keyData, fsize, file );
	// Make sure it will get deleted if we throw.
	AutoArray<char> keyDataAutoArray( keyData );
	
	if (!result)
	{
		throw KeyResolverException( "Couldn't load key from file. An error occurred." );
	}
	
	this->_spepPrivateKeyData = std::string( keyData, fsize+1 );
	
	this->loadSPEPPrivateKey();
}

void spep::KeyResolver::loadSPEPPrivateKey()
{	
	if( _spepPrivateKey != NULL )
	{
		delete _spepPrivateKey;
	}
	
	// Read the key data into an OpenSSL RSA key.
	BIO* bioMem = BIO_new( BIO_s_mem() );
	BIO_puts( bioMem, this->_spepPrivateKeyData.c_str() );
	EVP_PKEY* rawkey = PEM_read_bio_PrivateKey( bioMem, NULL, 0, NULL );
	_spepPrivateKey = new OpenSSLCryptoKeyRSA( rawkey );
	
	BIO_free( bioMem );
}

void spep::KeyResolver::loadMetadataKey( std::string file )
{
	char* keyData;
	int fsize;
	
	bool result = this->loadKey( keyData, fsize, file );
	// Make sure it will get deleted if we throw.
	AutoArray<char> keyDataAutoArray( keyData );
	
	if (!result)
	{
		throw KeyResolverException( "Couldn't load key from file. An error occurred." );
	}
	
	this->_metadataKeyData = std::string( keyData, fsize+1 );
	
	this->loadMetadataKey();
}

void spep::KeyResolver::loadMetadataKey()
{
	if( _spepPublicKey != NULL )
	{
		delete _spepPublicKey;
	}

	// OpenSSLCryptoX509 object to hold the key until it is cloned.
	std::auto_ptr<OpenSSLCryptoX509> x509( new OpenSSLCryptoX509() );
	x509->loadX509PEM( this->_metadataKeyData.c_str(), this->_metadataKeyData.length() );
	
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

std::string spep::KeyResolver::getSPEPKeyName()
{
	return _spepKeyName;
}

XSECCryptoKey* spep::KeyResolver::getMetadataKey()
{
	if( !_metadataKey )
		loadMetadataKey();
	return _metadataKey;
}
