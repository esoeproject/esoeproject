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
 * Creation Date: 28/06/2007
 * 
 * Purpose: 
 */

#ifndef UNICODESTRINGCONVERSION_H_
#define UNICODESTRINGCONVERSION_H_

#include <unicode/unistr.h>
#include <unicode/ustring.h>
#include <unicode/ucnv.h>

/** Xerces stuff and array adapter code. */
#include "spep/Util.h"
#include "saml2/handlers/SAMLDocument.h"

namespace spep
{
		
	class SPEPEXPORT UTF8Converter
	{
		
		UConverter *_uConverter;
		UErrorCode _error;
		
		public:
		
		UTF8Converter();
		~UTF8Converter();
		UConverter *getUConverter();
		int getMaxCharSize();
		
	};

	/**
	 * Provides conversion functions for Unicode strings inside the SPEP
	 */
	class SPEPEXPORT UnicodeStringConversion
	{
		
		public:
		static UnicodeString toUnicodeString( const std::string& src );
		static UnicodeString toUnicodeString( const std::wstring& src );
		static UnicodeString toUnicodeString( const XMLCh *src );
		static UnicodeString toUnicodeString( const unsigned char *src, int srclen );
		static std::wstring toWString( const UnicodeString& src );
		static std::string toString( const std::wstring& src );
		static std::wstring toWString( const std::string& src );
		static std::string toString( const UnicodeString& src );

		inline static std::wstring toWString( const char *src )
		{
			return toWString( std::string(src) );
		}

		inline static std::string toString( const wchar_t *src )
		{
			return toString( std::wstring(src) );
		}
		
		template <typename CharT, typename SizeT, typename RefCountT>
		static UnicodeString toUnicodeString( const saml2::ManagedDocument<CharT,SizeT,RefCountT>& managedDocument )
		{
			return toUnicodeString( (const unsigned char*)managedDocument.getData(), managedDocument.getLength() );
		}
	};
	
}
#endif /*UNICODESTRINGCONVERSION_H_*/
