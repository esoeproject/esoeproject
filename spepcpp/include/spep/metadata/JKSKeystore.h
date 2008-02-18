/*
 * Copyright 2007, Queensland University of Technology
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
 * Creation Date: 14/12/2007
 * 
 * Purpose: 
 */

#ifndef JKSKEYSTORE_H_
#define JKSKEYSTORE_H_

#ifdef WIN32

#include <winsock2.h>
#include <windows.h>

typedef UINT64 uint64_t;
typedef UINT32 uint32_t;
typedef UINT16 uint16_t;

#endif //WIN32

#include <string>
#include <map>
#include <vector>
#include <sys/types.h>

namespace spep
{

	class JKSPrivateKeyData
	{
		public:
		JKSPrivateKeyData();
		JKSPrivateKeyData( const JKSPrivateKeyData& other );
		JKSPrivateKeyData& operator=( const JKSPrivateKeyData& other );
		~JKSPrivateKeyData();
		uint64_t creationTimeMillis;
		std::string alias;
		uint32_t len;
		unsigned char *data;
	};
	
	class JKSTrustedCertData
	{
		public:
		JKSTrustedCertData();
		JKSTrustedCertData( const JKSTrustedCertData& other );
		JKSTrustedCertData& operator=( const JKSTrustedCertData& other );
		~JKSTrustedCertData();
		uint64_t creationTimeMillis;
		std::string alias;
		std::string algorithm;
		uint32_t len;
		unsigned char *data;
	};
	
	/**
	 * Describes a JKS Keystore
	 * Implements reading a keystore from disk, decrypting private keys and validating the
	 * keystore signature given the keystore password.
	 */
	class JKSKeystore
	{
		public:
		class PrivateKeyPasswordResolver
		{
			public:
			virtual bool getPrivateKeyPassword( const std::string& alias, std::size_t*, const unsigned char** ) = 0;
			virtual ~PrivateKeyPasswordResolver();
		};
	
		/**
		 * Constructs a JKS keystore from a keystore file, using the given password.
		 * It is assumed that the same password is used for private keys.
		 */
		JKSKeystore( const std::string& filename, const std::string& keystorePassword );
	
		/**
		 * Constructs a JKS keystore from a keystore file, using the given password.
		 * The private key password supplied will be used for ALL private keys in the
		 * keystore. Any that fail will cause the keystore processing to terminate.
		 */
		JKSKeystore( const std::string& filename, const std::string& keystorePassword, const std::string& privateKeyPassword );
	
		/**
		 * Constructs a JKS keystore from a keystore file, using the given password.
		 * The private key passwords will be resolved from the given password map, which
		 * is indexed by alias. The value in each map entry is the key in US-ASCII format.
		 */
		JKSKeystore( const std::string& filename, const std::string& keystorePassword, const std::map<std::string,std::string>& passwordMap );
	
		/**
		 * Constructs a JKS keystore from a keystore file, using the given password.
		 * The private key passwords will be resolved by making a call to the given resolver, 
		 * which will set the size and pointer to the password data. This is the
		 * only method which supports Unicode passwords. The password data given by the
		 * call to the password resolver MUST be UTF-16BE encoded with NO byte order mark.
		 */
		JKSKeystore( const std::string& filename, const std::string keystorePassword, PrivateKeyPasswordResolver* pwResolver );
	
		/**
		 * Destroys the JKS keystore in-memory representation and releases all resources
		 * held. Accessing a pointer returned by this keystore after it is destroyed will
		 * cause undefined behaviour.
		 */
		~JKSKeystore();
	
		/**
		 * Gets the raw private key data associated with the given alias.
		 * The pointer that is returned is a direct pointer into the JKSKeystore
		 * data, so if modifications are required, take a copy of it and modify
		 * the copy. Do not delete the returned pointer.
		 */
		const JKSPrivateKeyData* getKeyData( const std::string& alias );
	
		/**
		 * Gets the raw certificate data associated with the given alias.
		 * The pointer that is returned is a direct pointer into the JKSKeystore
		 * data, so if modifications are required, take a copy of it and modify
		 * the copy. Do not delete the returned pointer.
		 */
		const JKSTrustedCertData* getCertificateData( const std::string& alias );
		
		/**
		 * Gets the certificate chain associated with the given alias.
		 * The vector that is returned is a direct reference to the JKSKeystore
		 * data, and all pointers point directly into the keystore structure.
		 * If modifications are required, take a copy of the data and modify the
		 * copy. Do not delete any pointers in the map data.
		 */
		const std::vector<JKSTrustedCertData>& getCertificateChain( const std::string& alias );
	
		private:
	
		std::map<std::string,JKSPrivateKeyData> _pkeyDataMap;
		std::map<std::string,JKSTrustedCertData> _certDataMap;
		std::map< std::string, std::vector<JKSTrustedCertData> > _certChainMap;
	
		std::vector<unsigned char*> _freeList;
	
		uint16_t twoByteToUnsignedInt( const unsigned char* data );
		uint32_t fourByteToUnsignedInt( const unsigned char* data );
		uint64_t eightByteToUnsignedInt( const unsigned char* data );
	
		/**
		 * Internal use functions for parsing the keystore
		 */
		/** @{ */
		void initWithPassword( const std::string& filename, const std::string& keystorePassword, const std::string* privateKeyPassword = NULL );
		void parse( const unsigned char *data, std::size_t length );
		const unsigned char* parseKeyEntry( const unsigned char *data, std::size_t length );
		void parsePrivateKey( const std::string& alias, uint64_t creationTime, const unsigned char *data, uint32_t length );
		const unsigned char* parseTrustedCertEntry( const std::string& alias, uint64_t creationTime, const unsigned char *data, uint32_t length, bool chain = false );
		void parseTrustedCert( const std::string& alias, uint64_t creationTime, const std::string& algorithm, const unsigned char *data, uint32_t length, bool chain );
		/** @} */
	
	
		PrivateKeyPasswordResolver *_passwordResolver;
		std::size_t _keystorePasswordLength;
		const unsigned char *_keystorePassword;
	
		/**
		 * Different variations on resolving keystore passwords
		 */
		/** @{ */
		class SinglePasswordResolver : public PrivateKeyPasswordResolver
		{
			public:
			SinglePasswordResolver( const std::string& password );
			virtual ~SinglePasswordResolver();
			virtual bool getPrivateKeyPassword( const std::string& alias, std::size_t*, const unsigned char** );
			private:
			unsigned char *_data;
			std::size_t _length;
		};
		class MapPasswordResolver : public PrivateKeyPasswordResolver
		{
			public:
			MapPasswordResolver( const std::map<std::string,std::string>& passwordMap );
			virtual ~MapPasswordResolver();
			virtual bool getPrivateKeyPassword( const std::string& alias, std::size_t*, const unsigned char** );
		};
		/** @} */
	};

}

#endif /*JKSKEYSTORE_H_*/
