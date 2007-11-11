/*
 * Copyright 2006-2007, Queensland University of Technology
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
 * Author: Bradley Beddoes
 * Creation Date: 31/01/2007
 * 
 * Purpose: Stores the results of unmarshalling a metadata document, both the Metadata object itself and a map of public keys used in the
 * authentication network
 */

#ifndef METADATAOUTPUT_H_
#define METADATAOUTPUT_H_

/* STL */
#include <map>
#include <string>

/* Apache XML Security */
#include <xsec/enc/XSECCryptoKey.hpp>
#include <xsec/enc/OpenSSL/OpenSSLCryptoKeyRSA.hpp>
#include <xsec/enc/OpenSSL/OpenSSLCryptoKeyDSA.hpp>

/* Local Codebase */
#include "saml2/SAML2Defs.h"

namespace saml2
{

	/** Class to store key data from the metadata */
	class SAML2EXPORT KeyData
	{
		
		public:
			
			/**
			 * Copies the key data from another KeyData object
			 */
			KeyData& operator= (const KeyData &rhs)
			{
				this->type = rhs.type;
				this->keyName = rhs.keyName;
				this->modulus = rhs.modulus;
				this->exponent = rhs.exponent;
				this->p = rhs.p;
				this->q = rhs.q;
				this->g = rhs.g;
				this->y = rhs.y;
				this->j = rhs.j;
				return *this;
			}
			
			KeyData(){}
			
			/**
			 * Copies the key data from another KeyData object
			 */
			KeyData( const KeyData &rhs )
			{
				this->operator=( rhs );
			}
			
			/**
			 * Creates a new XSECCryptoKey object and returns it. This object must be
			 * deleted by the calling class
			 */
			XSECCryptoKey *createXSECCryptoKey();
		
			/* identify the type of the key */
			enum KeyType
			{
				RSA,
				DSA
			} type;
			
			/* key name */
			std::string keyName;
		
			/* data for RSA key - all base64 encoded */
			std::string modulus;
			std::string exponent;
			
			/* data for DSA key - all base64 encoded */
			std::string p;
			std::string q;
			std::string g;
			std::string y;
			std::string j;
		
	};
	
	
	template <class T>
	class MetadataOutput
	{
		public:
			
			
			MetadataOutput();
			MetadataOutput( const MetadataOutput& metadataOutput );
			~MetadataOutput();
			
			MetadataOutput& operator= (const MetadataOutput& metadataOutput);

			T* xmlObj;
			std::map<std::string, KeyData> keyList;
	};
	
	template <class T>
	MetadataOutput<T>::MetadataOutput( )
	{
		xmlObj = NULL;
	}
	
	template <class T>
	MetadataOutput<T>::MetadataOutput( const MetadataOutput& metadataOutput )
	{
		this->xmlObj = metadataOutput.xmlObj;
		this->keyList = metadataOutput.keylist;
	}

	template <class T>
	MetadataOutput<T>::~MetadataOutput()
	{
		delete xmlObj;
	}

	template <class T>
	MetadataOutput<T>& MetadataOutput<T>::operator= (const MetadataOutput<T>& metadataOutput)
	{
		this->xmlObj = metadataOutput.xmlObj;
		this->keyList = metadataOutput.keylist;

		return *this;
	}
	
}
#endif
