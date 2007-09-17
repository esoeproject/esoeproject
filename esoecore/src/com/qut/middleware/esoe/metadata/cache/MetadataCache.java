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
 * Purpose: Provides an interface to the metadata document used for authentication control. 
 * Implementations will glean their information from an internal data structure rather than
 * setting fields in individual methods. This is done so that only one entrant method is
 * provided for setting cache data, allowing the renewal of cache data to remain atomic. All 
 * implementing classes MUST ensure that ALL methods are synchronised, placing a lock on 
 * internal data when updates are occurring.
 * 
 */

package com.qut.middleware.esoe.metadata.cache;

import java.util.List;
import java.util.Map;

import com.qut.middleware.saml2.sec.KeyData;

public interface MetadataCache
{		
	public static enum State
	{
		Initialized,
		UnInitialized,
		Error; 
	};
	
	/** Check the cache for data and return bool value.
	 * 
	 * @return Returns true if the cache contains any cache data, else false.
	 */
	public boolean hasData();
	
	
	/** Set the state of the cache. One of MetadataCache.State.x.
	 * 
	 * @param State of the cache data.
	 */
	public void setState(State cacheState);
	
	
	/** Check the state of the cache.
	 * 
	 * @return Returns the state of the cache data.
	 */
	public State getState();
	
	
	/** Get the hash value representing the revision of the cache metadata.
	 * 
	 * @return revision hash value of metadata.
	 */
	public String getCurrentRevision();
	
	
	/** Retrieve a list of assertionConsumer services contained in the cache.
	 * 
	 * @return Map of descriptorID -> consumer service.
	 */
	public Map<String, String>  getAssertionConsumerServices();
	
	/** Returns a list of acceptable identifier types for this SP to be used in generation of responses
	 * 
	 * @return List of acceptable NameID format identifiers
	 */
	public Map<String, List<String>> getAssertionConsumerServiceIdentifierTypes();
	
	
	/** Get the list of logout services contained in the cache.
	 * 
	 * @return Map of descriptorID -> List of logout services.
	 */
	public Map<String, List<String>> getSingleLogoutServices();
	
	
	/** Get cache clear services from the metadata cache.
	 * 
	 * @return A map of DescriptorID -> ( map of node index -> cache clear service ).
	 */
	public Map<String, Map<Integer,String>> getCacheClearServices();
	
	
	/** Get a the map of KeyData from the cache.
	 * 
	 * @return A map of keyname -> KeyData objects. 
	 */
	public Map<String, KeyData> getKeyMap();
	
	
	/** Set the internal data of the cache. The implementation MUST ensure that the entire
	 * cache is locked while the update is occcurring.
	 * 
	 */
	public void setCacheData(CacheData cachedata);
	
}
