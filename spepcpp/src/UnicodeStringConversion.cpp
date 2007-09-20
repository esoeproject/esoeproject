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


#include "UnicodeStringConversion.h"

spep::UTF8Converter::UTF8Converter()
:
_uConverter( ucnv_open( "UTF8", &_error ) )
{
	if( U_FAILURE( _error ) )
	{
		_uConverter = NULL;
	}
}

spep::UTF8Converter::~UTF8Converter()
{
	ucnv_close( _uConverter );
}

UConverter *spep::UTF8Converter::getUConverter()
{
	return _uConverter;
}

int spep::UTF8Converter::getMaxCharSize()
{
	return ucnv_getMaxCharSize( _uConverter );
}

UnicodeString spep::UnicodeStringConversion::toUnicodeString( const std::string &src )
{
	UTF8Converter utf8Converter;
	UErrorCode error = U_ZERO_ERROR;
	
	UnicodeString unicodeResult( src.c_str(), src.length(), utf8Converter.getUConverter(), error );
	
	if( U_FAILURE( error ) )
	{
		unicodeResult.remove();
		unicodeResult.setToBogus();
	}
	
	return unicodeResult;
}

UnicodeString spep::UnicodeStringConversion::toUnicodeString( const std::wstring &src )
{
	UErrorCode error = U_ZERO_ERROR;
	
	std::size_t srcLength = src.length();
	int32_t destLength = srcLength;
	
	AutoArray<UChar> buf( destLength );
	u_strFromWCS ( buf.get(), srcLength, &destLength, src.c_str(), srcLength, &error );
	
	UnicodeString unicodeResult( buf.get(), destLength );
	
	if (U_FAILURE( error ))
	{
		unicodeResult.remove();
		unicodeResult.setToBogus();
	}
	
	return unicodeResult;
}

UnicodeString spep::UnicodeStringConversion::toUnicodeString( const XMLCh *src )
{
	std::size_t len = XMLString::stringLen( src );
	
	// Concept here is based on the ICU Transcoder from Xerces - that mapping from XMLCh to 
	// UChar is a straight copy operation, even for different data sizes.
	
	AutoArray<UChar> tmp( len );
	for( std::size_t i=0; i<len; ++i )
		tmp[i] = src[i];
	
	return UnicodeString( tmp.get(), len );
}

UnicodeString spep::UnicodeStringConversion::toUnicodeString( const unsigned char *src, int srcLength )
{
	int signatureLength = 0;
	UErrorCode error = U_ZERO_ERROR;
	const char* unicodeType = ucnv_detectUnicodeSignature( (const char*)(src), srcLength, &signatureLength, &error );
	UConverter *sourceConverter = ucnv_open( unicodeType, &error );
	
	// Strip the unicode signature off the document... Not interested in that.
	const char* documentStart = (const char*)src + signatureLength;
	int documentLength = srcLength - signatureLength;
	
	UnicodeString result( documentStart, documentLength, sourceConverter, error );
	
	if( U_FAILURE( error ) )
	{
		result.remove();
		result.setToBogus();
	}
	
	return result;
}

std::wstring spep::UnicodeStringConversion::toWString( const UnicodeString &src )
{
	UErrorCode error = U_ZERO_ERROR;
	
	std::size_t srcLength = src.length();
	int32_t destLength = srcLength;
	
	AutoArray<wchar_t> buf( destLength );
	u_strToWCS ( buf.get(), srcLength, &destLength, src.getBuffer(), srcLength, &error );
	
	std::wstring result( buf.get(), destLength );
	
	if (U_FAILURE( error ))
	{
		result.clear();
	}
	
	return result;
}

std::string spep::UnicodeStringConversion::toString( const std::wstring &src )
{
	return toString( toUnicodeString( src ) );
}

std::wstring spep::UnicodeStringConversion::toWString( const std::string &src )
{
	return toWString( toUnicodeString( src ) );
}

std::string spep::UnicodeStringConversion::toString( const UnicodeString &src )
{
	UTF8Converter utf8Converter;
	
	int buflen = src.length() * 4;
	
	AutoArray<char> buf( buflen );
	
	UErrorCode error = U_ZERO_ERROR;
	src.extract( buf.get(), buflen, utf8Converter.getUConverter(), error );
	if( U_FAILURE( error ) )
	{
		// TODO Handle this more cleanly
		return std::string();
	}
	
	// TODO Length?
	return std::string( buf.get() );
}
