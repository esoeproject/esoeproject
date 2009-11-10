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
#include <map>

#include "spep/Util.h"
#include "spep/ipc/Serialization.h"

#include "saml2/resolver/ExternalKeyResolver.h"

#define PATH_SEPARATOR_CHAR '/'

namespace spep
{
	
	class SPEPEXPORT KeyResolver : public saml2::ExternalKeyResolver
	{
	
		friend class spep::ipc::access;
		
		public:
		
		KeyResolver();
		virtual ~KeyResolver();
		
		KeyResolver( const KeyResolver &other );
		KeyResolver& operator=( const KeyResolver &other );
		
		KeyResolver( std::string keystorePath, std::string keystorePassword, std::string spepKeyAlias, std::string spepKeyPassword );
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

		virtual XSECCryptoKey* resolveKey( DSIGKeyInfoList *list );
		virtual XSECKeyInfoResolver* clone() const;
		XSECCryptoKey* resolveKey(std::string keyName);
		
		private:
		/**
		 * Loads the SPEP public key from the data stored internally - usually called after deserialization
		 */
		void loadSPEPPublicKey();
		/**
		 * Loads the SPEP private key from the data stored internally - usually called after deserialization
		 */
		void loadSPEPPrivateKey();
		
		void deleteKeys();
		
		template <class Archive>
		void serialize( Archive &ar, const unsigned int version )
		{
			ar & _spepKeyAlias;
			ar & _spepPublicKeyB64;
			ar & _spepPrivateKeyB64;
		}
		
		XSECCryptoKey* _spepPublicKey;
		// for serialization
		std::string _spepPublicKeyB64;
		
		XSECCryptoKey* _spepPrivateKey;
		// for serialization
		std::string _spepPrivateKeyB64;
		
		std::string _spepKeyAlias;

		std::map<std::string,std::string> _trustedCerts;
		
	};
	
}

#endif /*KEYRESOLVER_H_*/
