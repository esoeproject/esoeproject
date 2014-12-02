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
 * Creation Date: 6/2/2007
 * 
 * Purpose: Provides all caching functionality to identifier generators to ensure no item is used twice within a reason time frame
 */

/* Boost */
#include <boost/thread/recursive_mutex.hpp>

/* C */
#include <time.h>

/* STL */
#include <string>
#include <map>

/* Local Codebase */
#include "saml2/identifier/IdentifierCache.h"
#include "saml2/exceptions/IdentifierCacheException.h"
#include "saml2/SAML2Defs.h"

namespace saml2
{		
		IdentifierCache::IdentifierCache()
		{
		}
        IdentifierCache::~IdentifierCache()
        {
        }
				
		void IdentifierCache::registerIdentifier(const std::string& identifier)
		{
			/* Lock automatically released when it goes out of scope */
			boost::recursive_mutex::scoped_lock lock (mutex);				

			if (containsIdentifier(identifier))
				SAML2LIB_IDC_EX("Attempt to register identifier that already exists");
				
			time_t rawtime;
			time(&rawtime);
			
			cacheData.insert(std::make_pair(identifier, rawtime));
		}
	
		bool IdentifierCache::containsIdentifier(const std::string& identifier)
		{
			/* Lock automatically released when it goes out of scope */
			boost::recursive_mutex::scoped_lock lock(mutex);
			std::map<std::string, long>::iterator i = cacheData.find(identifier);
			if(i != this->cacheData.end())
				return true;
				
			return false;
		}

		int IdentifierCache::cleanCache(long age)
		{		
			time_t rawtime;
			long expire;
			int count = 0;
			time(&rawtime);
			
			/* Lock automatically released when it goes out of scope */
			boost::recursive_mutex::scoped_lock lock(mutex);
			std::map<std::string, long>::iterator i = cacheData.begin();
			
			while (i != cacheData.end())
			{
				expire = i->second + age;
				
				/* Remove the identifier. This line works because the iterator is actually
				 * incremented *before* the erase() call is made, but the old value is
				 * still passed into the function.
				 */
				if (expire < rawtime)
				{
					cacheData.erase(i++);
					count ++;
				}
				else
					i++;
			}
			return count;
		}
 }
