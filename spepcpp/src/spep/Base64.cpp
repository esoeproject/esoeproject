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
 * Creation Date: Aug 28, 2007
 * 
 * Purpose: 
 */

#include "spep/Base64.h"

#define BASE64DECODE_BUFSIZE 512

spep::Base64Exception::Base64Exception( std::string message )
:
_message( message )
{
}

spep::Base64Exception::~Base64Exception() throw()
{
}

const char *spep::Base64Exception::what() const throw()
{
	return _message.c_str();
}

spep::Base64Encoder::Base64Encoder()
:
_document(),
_closed( false ),
_base64( BIO_new( BIO_f_base64() ) )
{
	BIO_set_flags( _base64, BIO_FLAGS_BASE64_NO_NL );
	
	// Ensure that after this object is instantiated, it can be used immediately.
	BIO *mem = BIO_new( BIO_s_mem() );
	_base64 = BIO_push( _base64, mem );
}

spep::Base64Encoder::~Base64Encoder()
{
	if( this->_base64 != NULL )
	{
		// Encoder was never closed. No matter, clean up the OpenSSL stuff anyway..
		BIO_free_all( this->_base64 );
	}
}

void spep::Base64Encoder::push( const char *bytes, std::size_t len )
{
	if( this->_closed )
	{
		throw Base64Exception( "Attempt to push data into a closed Base64Encoder." );
	}
	
	// If the encoder is still open, this->_base64 is still valid to be written to
	BIO_write( this->_base64, bytes, len );
}

void spep::Base64Encoder::close()
{
	if( this->_closed )
	{
		throw Base64Exception( "Attempt to close a closed Base64Encoder." );
	}
	
	// Flush the BIO to close off the base64 "stream"
	BIO_flush( this->_base64 );
	this->_closed = true;
	
	// Grab the memory buffer
	BUF_MEM *base64Result = NULL;
	BIO_get_mem_ptr( this->_base64, &base64Result );
	
	// Duplicate it into our own C++ style array
	char *buf = new char[ base64Result->length ];
	std::memcpy( buf, base64Result->data, base64Result->length );
	
	// Give the array to a manager object so it gets cleaned up
	_document = Base64Document( buf, base64Result->length );
	
	// Clean up the OpenSSL stuff here to release the memory asap.
	BIO_free_all( this->_base64 );
	this->_base64 = NULL;
}

const spep::Base64Document& spep::Base64Encoder::getResult() const
{
	if( !this->_closed )
	{
		throw Base64Exception( "Attempt to get result from an open Base64Encoder." );
	}
	
	// If the encoder is closed, the document will have already been instantiated.
	return _document;
}

spep::Base64Decoder::Base64Decoder()
:
_document(),
_closed(),
_encoded( 1 ), // Give it a length of '1' because a malloc of 0 length is just stupid.
_length( 0 ) // But set the _length field to 0 so we don't have an unitialized byte at the start
{
	_encoded[0] = '\0';
}

spep::Base64Decoder::~Base64Decoder()
{
	// All memory is managed inherently in the data types used in Base64Decoder.
}

void spep::Base64Decoder::push( const char *bytes, std::size_t len )
{
	// size_t is always unsigned.. right? Maybe.
	if( len <= 0 ) return;
	
	// Resize the array to the new length
	std::size_t newLength = this->_length + len;
	this->_encoded.resize( newLength );
	
	// Copy the bytes into the new section at the end.
	std::memcpy( &(this->_encoded[this->_length]), bytes, len );
	this->_length = newLength;
}

void spep::Base64Decoder::close()
{
	// Initialize the decoded document to an (essentially) empty buffer
	CArray<char> doc(1);
	std::size_t len = 0;
	
	BIO *mem, *base64;
	
	base64 = BIO_new( BIO_f_base64() );
	mem = BIO_new_mem_buf( this->_encoded.get(), this->_length );

	BIO_set_flags(base64, BIO_FLAGS_BASE64_NO_NL);
	
	base64 = BIO_push( base64, mem );
	
	char buf[BASE64DECODE_BUFSIZE];
	int i;
	
	while( ( i = BIO_read( base64, buf, BASE64DECODE_BUFSIZE ) ) > 0 )
	{
		// Calculate the new length of the buffer
		std::size_t newLength = len + i;
		doc.resize( newLength );
		
		// Append to the data already in the buffer
		std::memcpy( &(doc[len]), buf, i );
		len = newLength;
	}
	
	/* We may throw after here, but we need this cleaned up.
	 * Cleanup doesn't affect the CArray object and that is where the document is now stored.
	 */
	BIO_free_all( base64 );

	/* Magic number used by OpenSSL to signal that:
	 * "the operation is not implemented in the specific BIO type"
	 * see man BIO_read(3)
	 * 
	 * Really this should never happen
	 */
	if( i == -2 )
	{
		throw Base64Exception( "BIO type did not support BIO_read. This should never happen. OpenSSL base64 functionality is malfunctioning?" );
	}
	
	// The other two possibilities are 0 and -1. Both are acceptable and mean EOF.
	
	// Make sure we have some document data.
	if( len <= 0 )
	{
		throw Base64Exception( "No document data decoded. Can't create the decoded document." );
	}
	
	// Copy the document and put it in a managed object so that all memory is taken care of.
	char *decoded = new char[len];
	std::memcpy( decoded, doc.get(), len );
	
	this->_document = Base64Document( decoded, len );
}

const spep::Base64Document& spep::Base64Decoder::getResult() const
{
	return _document;
}
