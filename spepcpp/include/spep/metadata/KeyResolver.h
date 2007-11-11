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
		
		KeyResolver( std::string path, std::string spepKeyName );
		/**
		 * Loads the SPEP public key from the PEM file specified
		 */
		void loadSPEPPublicKey( std::string file );
		/**
		 * Loads the SPEP private key from the PEM file specified
		 */
		void loadSPEPPrivateKey( std::string file );
		/**
		 * Loads the Metadata public key from the PEM file specified
		 */
		void loadMetadataKey( std::string file );
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
		std::string getSPEPKeyName();
		/**
		 * Returns the metadata key
		 */
		XSECCryptoKey* getMetadataKey();
		
		private:
		/**
		 * Loads a key from the file specified
		 */
		bool loadKey( char *&keyData, int &fsize, std::string &file );
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
			ar & _path;
			ar & _spepKeyName;
			ar & _spepPublicKeyData;
			ar & _spepPrivateKeyData;
			ar & _metadataKeyData;
		}
		
		XSECCryptoKey* _spepPublicKey;
		std::string _spepPublicKeyData; // for serialization
		
		XSECCryptoKey* _spepPrivateKey;
		std::string _spepPrivateKeyData; // for serialization
		
		XSECCryptoKey* _metadataKey;
		std::string _metadataKeyData; // for serialization
		
		std::string _spepKeyName;
		std::string _path;
		
	};
	
}

#endif /*KEYRESOLVER_H_*/
