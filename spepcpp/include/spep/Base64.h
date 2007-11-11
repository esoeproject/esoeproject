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

#ifndef BASE64_H_
#define BASE64_H_

#include "spep/Util.h"

#include <string>

#include <openssl/bio.h>
#include <openssl/evp.h>
#include <openssl/buffer.h>

#include "saml2/handlers/SAMLDocument.h"

namespace spep {
	
	// Important! Base64Document doesn't mean it is base64 encoded.
	// Just means it came from a base64 operation.
	typedef saml2::ManagedDocument<char,std::size_t> Base64Document;
	
	class SPEPEXPORT Base64Exception : public std::exception
	{
		private:
		std::string _message;
		
		public:
		Base64Exception( std::string message );
		virtual ~Base64Exception() throw();
		virtual const char *what() const throw();
	};

	class SPEPEXPORT Base64Encoder
	{
		private:
		Base64Document _document;
		bool _closed;
		BIO *_base64;
		
		Base64Encoder( const Base64Encoder& other );
		Base64Encoder& operator=( const Base64Encoder& other );
		
		public:
		Base64Encoder();
		~Base64Encoder();
		void push( const char *bytes, std::size_t len );
		void close();
		const Base64Document& getResult() const;
	};
	
	class SPEPEXPORT Base64Decoder
	{
		private:
		Base64Document _document;
		bool _closed;
		spep::CArray<char> _encoded;
		std::size_t _length;
		
		Base64Decoder( const Base64Decoder& other );
		Base64Decoder& operator=( const Base64Decoder& other );
		
		public:
		Base64Decoder();
		~Base64Decoder();
		void push( const char *bytes, std::size_t len );
		void close();
		const Base64Document& getResult() const;
	};
	
}

#endif /*BASE64_H_*/
