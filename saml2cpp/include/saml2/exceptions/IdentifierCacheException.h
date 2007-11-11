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
 * Creation Date: 13/02/2007
 * 
 * Purpose: Exception thrown when a unique identifier could not be generated
 */
 
#ifndef IDENTIFIERCACHEEXCEPTION_H_
#define IDENTIFIERCACHEEXCEPTION_H_

#include "saml2/SAML2Defs.h"

/* STL */
#include <string>

/* Xerces */
#include <xercesc/util/PlatformUtils.hpp>
#include <xercesc/util/XMLString.hpp>

namespace saml2
{
	class SAML2EXPORT IdentifierCacheException : public std::exception
	{
		public:
			IdentifierCacheException( const char *filename, int line, std::string message );
			IdentifierCacheException( const char *filename, int line, std::string message, std::string cause );
			IdentifierCacheException( const char *filename, int line, std::string message, const XMLCh* cause );
			virtual ~IdentifierCacheException() throw() {};

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
			
		private:
			const char *filename;
			int line;
			std::string message;
			std::string cause;
	};
	
	/* saml2lib-cpp macro for new exception */
	#define SAML2LIB_IDC_EX(message)				throw saml2::IdentifierCacheException( __FILE__, __LINE__, message );
	#define SAML2LIB_IDC_EX_CAUSE(message, cause)  	throw saml2::IdentifierCacheException( __FILE__, __LINE__, message, cause );
}

#endif /*IDENTIFIERCACHEEXCEPTION_H_*/
