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
 * Purpose: Thrown when an invalid SAML request has been presented
 */
 
/* STL */
#include <iostream>
#include <string>

/* Xerces */
#include <xercesc/util/PlatformUtils.hpp>
#include <xercesc/util/XMLString.hpp>

/* Local Codebase */
#include "exceptions/InvalidSAMLRequestException.h"

XERCES_CPP_NAMESPACE_USE

namespace saml2
{
	InvalidSAMLRequestException::InvalidSAMLRequestException( const char *filename, int line, std::string message)
	{
		this->message = message;
		this->filename = filename;
		this->line = line;
	}
	
	InvalidSAMLRequestException::InvalidSAMLRequestException( const char *filename, int line, std::string message, std::string cause)
	{
		this->message = message;
		this->cause = cause;
		this->filename = filename;
		this->line = line;
	}
	
	InvalidSAMLRequestException::InvalidSAMLRequestException( const char *filename, int line, std::string message, const XMLCh* cause)
	{
		char* tmp = XMLString::transcode(cause);
		this->message = message;
		this->cause = std::string(tmp);
		this->filename = filename;
		this->line = line;
		
		delete tmp;
	}
			
	void InvalidSAMLRequestException::printStackTrace() const
	{
		std::cerr << "Exception thrown in execution of SAML2lib SAML request validation" << std::endl;
		std::cerr << message << std::endl;
		std::cerr << "    at " << filename << ":" << line << std::endl;
		
		if(cause.length() > 0)
		{
			std::cerr << "Caused by: " << std::endl;
			std::cerr << cause << std::endl;
		}
	}
}
