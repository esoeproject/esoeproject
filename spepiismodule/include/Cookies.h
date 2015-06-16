/* Copyright 2007, Queensland University of Technology
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
 * Creation Date: Oct 31, 2007
 * 
 * Purpose: 
 */

#ifndef COOKIES_H_
#define COOKIES_H_

#include "HttpRequest.h"

#include <unordered_map>
#include <string>

#define COOKIES_EXPIRES_TIME_STRING_FORMAT "%a, %d-%b-%Y %H:%M:%S GMT"

namespace spep { namespace isapi {

    class Cookies
    {
    public:
		Cookies(HttpRequest *request);
        std::string operator[](const std::string& name);
		void addCookie(HttpRequest *request, const char *name, const char *value, const char *path = NULL, const char *domain = NULL, bool secureOnly = false, int expires = 0);

    private:
        std::unordered_map<std::string, std::string> mValues;
    };
	
} }

#endif /*COOKIES_H_*/
