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
 * Creation Date: 05/02/2007
 * 
 * Purpose: Thrown when an invalid SAML assertion has been presented
 */
 
#ifndef INVALIDSAMLASSERTIONEXCEPTION_H_
#define INVALIDSAMLASSERTIONEXCEPTION_H_

#include "saml2/SAML2Defs.h"

/* STL */
#include <string>

/* Xerces */
#include <xercesc/util/PlatformUtils.hpp>

namespace saml2
{
	class SAML2EXPORT InvalidSAMLAssertionException : public std::exception
	{
		private:
			const char *filename;
			int line;
			std::string message;
			std::string cause;

		public:
			InvalidSAMLAssertionException( const char *filename, int line, std::string message );
			InvalidSAMLAssertionException( const char *filename, int line, std::string message, std::string cause );
			InvalidSAMLAssertionException( const char *filename, int line, std::string message, const XMLCh* cause );
			virtual ~InvalidSAMLAssertionException() throw() {};

			const char *getFilename() const
			{
				return filename;
			}

			int getLineNumber() const
			{
				return line;
			}
			
			std::string getMessage() const
			{
				return message;
			}
			
			std::string getCause() const
			{
				return cause;
			}
			
			virtual const char * what() const throw()
			{
				return message.c_str();
			}

			void printStackTrace() const;
	};
	
	/* saml2lib-cpp macro for new exception */
	#define SAML2LIB_ASSERTION_EX(message)				throw saml2::InvalidSAMLAssertionException( __FILE__, __LINE__, message );
	#define SAML2LIB_ASSERTION_EX_CAUSE(message, cause)  	throw saml2::InvalidSAMLAssertionException( __FILE__, __LINE__, message, cause );
}

#endif /*INVALIDSAMLASSERTIONEXCEPTION_H_*/
