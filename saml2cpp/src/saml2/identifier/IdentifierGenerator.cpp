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
 * Creation Date: 5/2/2007
 * 
 * Purpose: Implementation of all identifier generator functionality to SAML2lib-cpp
 */

/* Boost */
#include <boost/date_time/string_convert.hpp>
#include <boost/date_time/posix_time/posix_time.hpp>

/* Openssl */
#include <openssl/rand.h>

/* STL */
#include <string>
#include <time.h>
#include <stdio.h>

/* Local Codebase */
#include "saml2/identifier/IdentifierGenerator.h"
#include "saml2/identifier/IdentifierCache.h"
#include "saml2/exceptions/InvalidParameterException.h"
#include "saml2/exceptions/IdentifierException.h"
#include "saml2/exceptions/IdentifierCacheException.h"
#include "saml2/SAML2Defs.h"

namespace saml2
{
		 IdentifierGenerator::IdentifierGenerator(IdentifierCache* cache)
		 {
		 	if( cache == NULL )
			{
				SAML2LIB_INVPARAM_EX("Supplied identifier cache was NULL");
			}
		 	this->cache = cache;
		 }
 
		std::wstring IdentifierGenerator::generateSAMLID()
		{
			std::string id = XS_ID_DELIM;
			id = id + generate(20);
			id = id + ID_DELIM + generate(16);

			try
			{
				cache->registerIdentifier(id);
			}
			catch(IdentifierCacheException &exc)
			{
				SAML2LIB_ID_EX_CAUSE("Identifier generator unable to complete due to cache error", exc.getCause());
			}
			
			std::wstring retVal = boost::date_time::convert_string_type<char, wchar_t> ( id.c_str());
			return retVal;
		}
	
		std::wstring IdentifierGenerator::generateSAMLAuthnID()
		{
			std::string id = XS_ID_DELIM;
			id = id + generate(20);
			id = id + ID_DELIM + generate(16);
			
			try
			{
				cache->registerIdentifier(id);
			}
			catch(IdentifierCacheException &exc)
			{
				SAML2LIB_ID_EX_CAUSE("Identifier generator unable to complete due to cache error", exc.getCause());
			}
			
			std::wstring retVal = boost::date_time::convert_string_type<char, wchar_t> ( id.c_str());
			return retVal;
		}
	
		std::wstring IdentifierGenerator::generateSAMLSessionID()
		{
			std::string id = generate(10);
			
			try
			{
				cache->registerIdentifier(id);
			}
			catch(IdentifierCacheException &exc)
			{
				SAML2LIB_ID_EX_CAUSE("Identifier generator unable to complete due to cache error", exc.getCause());
			}
			
			std::wstring retVal = boost::date_time::convert_string_type<char, wchar_t> ( id.c_str());
			return retVal;
		}

		std::string IdentifierGenerator::generateXMLKeyName()
		{
			std::string id = generate(8);
			
			return id;
		}
	
		std::string IdentifierGenerator::generateSessionID()
		{
			/* 20 characters to store a epoch time stamp is currently overkill, but its enough 
			 * to ensure storage of even high end 64bit long data in character form */
			char timeString[20];
			//time_t rawtime;
			struct tm timeptr;
			std::string id;
			
			id = generate(20);
			id = id + ID_DELIM + generate(20);
			id = id + ID_DELIM;
			
			//time ( &rawtime );
  			//localtime_r ( &rawtime, &timeptr );

			timeptr = boost::posix_time::to_tm( boost::posix_time::second_clock::local_time() );
			strftime( timeString, 20, TIME_FORMAT, &timeptr );
					
			id = id + std::string(timeString);
			
			try
			{
				cache->registerIdentifier(id);
			}
			catch(IdentifierCacheException &exc)
			{
				SAML2LIB_ID_EX_CAUSE("Identifier generator unable to complete due to cache error", exc.getCause());
			}
			
			return id;
		}
		
		std::string IdentifierGenerator::generate(const unsigned int length)	
		{		
			if( length > MAX_BYTES )
				SAML2LIB_ID_EX("Requested number of bytes exceeds MAX_BYTES can't continue");
				
			const unsigned int numBytes = length;
			const unsigned int numChars = ( length * 2 );
			int result;
			char hexBytes[MAX_CHARS];
			unsigned char randBytes[MAX_BYTES];
			
			/* Ensure OpenSSL rand engine is seeded with enough data and ready to operate */
			if(!RAND_status())
				SAML2LIB_ID_EX("OpenSSL is not seeded with enough random data to generate cryptographically strong data, please initialise");
			
			/* Get a random number of bytes using openssl default RNG generator */
			result = RAND_bytes(randBytes, numBytes);
			if(!result)
				SAML2LIB_ID_EX("OpenSSL returned failure event for RAND_bytes call");
			
			/* Convert random bytes to Hex to meet xs:ID requirements */
			for(std::size_t i = 0; i < numBytes; i++) 
				snprintf(&hexBytes[(i * 2)], 3, HEX_FORMAT, randBytes[i]);
					
			return std::string((char *)hexBytes, numChars);		
		}
 }
