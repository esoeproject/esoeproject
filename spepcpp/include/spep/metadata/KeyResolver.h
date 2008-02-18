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
 * Creation Date: 23/02/2007
 * 
 * Purpose: 
 */

#ifndef KEYRESOLVER_H_
#define KEYRESOLVER_H_

#include <xsec/enc/OpenSSL/OpenSSLCryptoX509.hpp>
#include <xsec/enc/OpenSSL/OpenSSLCryptoKeyRSA.hpp>
#include <xsec/enc/XSECCryptoKey.hpp>
#include <openssl/pem.h>

#include <string>
#include <fstream>
#include <iostream>

#include "spep/Util.h"
#include "spep/ipc/Serialization.h"

#define PATH_SEPARATOR_CHAR '/'

namespace spep
{
	
	class SPEPEXPORT KeyResolver
	{
	
		friend class spep::ipc::access;
		
		public:
		
		KeyResolver();
		~KeyResolver();
		
		KeyResolver( const KeyResolver &other );
		KeyResolver& operator=( const KeyResolver &other );
		
		KeyResolver( std::string keystorePath, std::string keystorePassword, std::string spepKeyAlias, std::string spepKeyPassword, std::string metadataKeyAlias );
		/**
		 * Returns the SPEP public key
		 */
		XSECCryptoKey* getSPEPPublicKey();
		/**
		 * Returns the SPEP private key
		 */
		XSECCryptoKey* getSPEPPrivateKey();
		/**
		 * Returns the key pair name for the SPEP key.
		 */
		std::string getSPEPKeyAlias();
		/**
		 * Returns the metadata key
		 */
		XSECCryptoKey* getMetadataKey();
		
		private:
		/**
		 * Loads the SPEP public key from the data stored internally - usually called after deserialization
		 */
		void loadSPEPPublicKey();
		/**
		 * Loads the SPEP private key from the data stored internally - usually called after deserialization
		 */
		void loadSPEPPrivateKey();
		/**
		 * Loads the Metadata public key from the data stored internally - usually called after deserialization
		 */
		void loadMetadataKey();
		
		void deleteKeys();
		
		template <class Archive>
		void serialize( Archive &ar, const unsigned int version )
		{
			ar & _spepKeyAlias;
			ar( _spepPublicKeyData, _spepPublicKeyLength );
			ar( _spepPrivateKeyData, _spepPrivateKeyLength );
			ar( _metadataKeyData, _metadataKeyLength );
		}
		
		XSECCryptoKey* _spepPublicKey;
		// for serialization
		char *_spepPublicKeyData;
		std::size_t _spepPublicKeyLength;
		
		XSECCryptoKey* _spepPrivateKey;
		// for serialization
		char *_spepPrivateKeyData;
		std::size_t _spepPrivateKeyLength;
		
		XSECCryptoKey* _metadataKey;
		// for serialization
		char *_metadataKeyData;
		std::size_t _metadataKeyLength;
		
		std::string _spepKeyAlias;

		
	};
	
}

#endif /*KEYRESOLVER_H_*/
