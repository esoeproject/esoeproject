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
 
#ifndef IDENTIFIERCACHE_H_
#define IDENTIFIERCACHE_H_

/* Boost */
#include <boost/thread/recursive_mutex.hpp>

/* STL */
#include <string>
#include <map>

namespace saml2
{
	/*
	 * Identifier cache implementation, should be singleton in any implementing application
	 */
	class IdentifierCache
	{
		public:
			IdentifierCache();
			
			/*
			 * Registers newly generated identifiers into the session cache
			 * @param identifier The identifier to register
			 * 
			 * @exception IdentifierCacheException if some error state is encountered
			 */
			virtual void registerIdentifier(std::string identifier);
		
			/*
			 * Determines if a session identifier is already present in the cache
			 * @param identifier The identifier to register
			 * 
			 * @exception IdentifierCacheException if some error state is encountered
			 */
			virtual bool containsIdentifier(std::string identifier);
		
			/*
			 * Removes all cache entries whose timestamps are further in the past from the present time then age allows for
			 * @param identifier The identifier to register
			 * 
			 * @exception IdentifierCacheException if some error state is encountered
			 */
			virtual int cleanCache(long age);
			
			/*
			 * Virtual destructor.
			 */
			virtual ~IdentifierCache();
		
		private:
			/*
			 * Copy constructor and assignment operator.
			 */
			IdentifierCache( const IdentifierCache& other );
			IdentifierCache& operator= (const IdentifierCache& identifierCache);
			
			std::map<std::string, long> cacheData;
			boost::recursive_mutex mutex;
	};
}

#endif /*IDENTIFIERCACHE_H_*/
