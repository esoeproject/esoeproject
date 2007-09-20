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
 * Creation Date: 13/02/2007
 * 
 * Purpose: Provides includes necessary for any class that wishes to be serializable. 
 */

#ifndef SERIALIZATION_H_
#define SERIALIZATION_H_

namespace spep
{
	namespace ipc
	{
		
		class access
		{
			
			public:
			/**
			 * Emulate the same structure as the boost serialization library.
			 * @param t Object to serialize
			 * @param ar Archive to serialize out to
			 * @param version Version number. Unused at the moment.
			 */
			template <class T, class Archive>
			static void serialize( T &t, Archive &ar, const unsigned int version )
			{
				t.serialize( ar, version );
			}
			
		};
		
	}
}

#endif /*SERIALIZATION_H_*/
