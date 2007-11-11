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
 * Creation Date: 18/06/2007
 * 
 * Purpose: 
 */

#ifndef COOKIES_H_
#define COOKIES_H_

#include <vector>
#include <string>

#include "httpd.h"

#include "APRDefinitions.h"

#define COOKIES_EXPIRES_TIME_STRING_FORMAT "%a, %d-%b-%Y %H:%M:%S GMT"

namespace spep{ namespace apache{
	
	/**
	 * @class Cookies
	 * @brief Stores information about cookies that came in from a request, and
	 * sets cookies to be send in the response.
	 */
	class Cookies
	{
		
		private:
			
		Cookies( const Cookies& other );
		Cookies& operator=( const Cookies& other );
		
		apr_table_t *_cookieTable;
		std::vector<char*> _cookieStrings;
		request_rec *_req;
		
		apr_table_t *createCookieTableFromRequest( request_rec *req );
		
		public:
		
		/**
		 * Creates a Cookies object with no cookie values.
		 * Used for setting cookies in a response.
		 * 
		 * All calls to operator[] will return NULL.
		 */
		Cookies();
		/**
		 * Creates a Cookies object containing the cookie values sent in the request.
		 *
		 * Important: A Cookies object instantiated in this way MUST NOT be held
		 * beyond the lifetime of the request_rec
		 * Doing so will result in undefined and illegal behaviour.
		 */
		Cookies( request_rec *req );
		/**
		 * Retrieves a cookie value that was send in the request.
		 * 
		 * Cookies set with addCookie(..) are not returned.
		 */
		const char *operator[]( std::string name );
		void addCookie( request_rec *req, const char *name, const char *value, const char *path = NULL, const char *domain = NULL, bool secureOnly = false, int expires = 0 );
		void sendCookies( request_rec *req );
		
	};
	
}}
#endif /*COOKIES_H_*/
