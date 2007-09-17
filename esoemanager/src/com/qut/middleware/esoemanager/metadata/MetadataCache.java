/* Copyright 2006, Queensland University of Technology
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
 * Author: Andre Zitelli
 * Creation Date: 24/01/2007
 * 
 * Purpose: Provides an interface to the metadata document used by the ESOE and all SPEP's in the authentication network
 * 
 */

package com.qut.middleware.esoemanager.metadata;


public interface MetadataCache
{		
	/** Check the cache for data and return bool value.
	 * 
	 * @return Returns true if the cache contains any cache data, else false.
	 */
	public boolean hasData();
	
	/** Get the internal data of the cache. The implementation MUST ensure that the entire
	 * cache is at least read locked while the get is occcurring.
	 * 
	 */
	public byte[] getCacheData();
	
	/** Set the internal data of the cache. The implementation MUST ensure that the entire
	 * cache is locked while the update is occcurring.
	 * 
	 */
	public void setCacheData(byte[] cachedata);
	
}
