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
 * Purpose: Exception thrown when an underlying library has caused an exception to occur or some generic localised error state has occured.
 */
#ifndef MARSHALLEREXCEPTION_H_
#define MARSHALLEREXCEPTION_H_

/* STL */
#include <string>

/* Xerces */
#include <xercesc/util/PlatformUtils.hpp>

namespace saml2
{
	class MarshallerException : public std::exception
	{
		private:
			const char *filename;
			int line;
			std::string message;
			std::string cause;

		public:
			MarshallerException( const char *filename, int line, std::string message );
			MarshallerException( const char *filename, int line, std::string message, std::string cause );
			MarshallerException( const char *filename, int line, std::string message, const XMLCh* cause );
			virtual ~MarshallerException() throw() {};

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
	#define SAML2LIB_MAR_EX(message)				throw saml2::MarshallerException( __FILE__, __LINE__, message );
	#define SAML2LIB_MAR_EX_CAUSE(message, cause)  	throw saml2::MarshallerException( __FILE__, __LINE__, message, cause );
}

#endif /*MARSHALLEREXCEPTION_H_*/
