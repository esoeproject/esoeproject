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
 * Creation Date: 12/02/2007
 * 
 * Purpose: 
 */

#ifndef INVALIDSESSIONIDENTIFIEREXCEPTION_H_
#define INVALIDSESSIONIDENTIFIEREXCEPTION_H_

#include <exception>

namespace spep
{
	
	class InvalidSessionIdentifierException : public std::exception 
	{
		private:
		const char *_what;
		
		public:
		InvalidSessionIdentifierException();
		virtual ~InvalidSessionIdentifierException() throw();
		virtual const char *what() const throw();
	};
	
}

#endif /*INVALIDSESSIONIDENTIFIEREXCEPTION_H_*/
