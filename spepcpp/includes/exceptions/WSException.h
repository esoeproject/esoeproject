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
 * Creation Date: Aug 13, 2007
 * 
 * Purpose: 
 */
#ifndef WSEXCEPTION_H_
#define WSEXCEPTION_H_

#include <string>
#include <exception>

namespace spep
{
	
	class WSException : public std::exception
	{
		
		private:
		std::string _message;
		
		public:
		WSException( std::string message );
		virtual ~WSException() throw();
		
		virtual const char *what() const throw();
		
	};
}

#endif /*WSEXCEPTION_H_*/
