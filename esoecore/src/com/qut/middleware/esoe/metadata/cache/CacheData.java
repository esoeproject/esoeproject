/* 
 * Copyright 2007, Queensland University of Technology
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
 * Creation Date: 25/01/2007
 * 
 * Purpose: Interface for data to be used in MetadataCache. 
 * 
 */

package com.qut.middleware.esoe.metadata.cache;

import java.util.List;
import java.util.Map;

import com.qut.middleware.esoe.metadata.cache.MetadataCache.State;
import com.qut.middleware.saml2.sec.KeyData;

public interface CacheData
{	
	/** Set the list of logout services.
	 * 
	 * @param logoutServices Map of descriptorID -> List of logout services.
	 */
	public void setSingleLogoutServices(Map<String,List<String>> logoutServices);

	
	/** Get the list of logout services.
	 * 
	 * @return Map of descriptorID -> List of logout services.
	 */
	public Map<String,List<String>> getSingleLogoutServices();

	
	/** Set the list of assertionConsumer services.
	 * 
	 * @param assertionConsumerServices Map of descriptorID -> consumer service.
	 */
	public void setAssertionConsumerServices(Map<String, String> assertionConsumerServices);
	
	
	/** Retrieve a list of assertionConsumer services.
	 * 
	 * @return Map of descriptorID -> consumer service.
	 */
	public Map<String, String> getAssertionConsumerServices();
	
	
	/** Set cache clear services.
	 * 
	 * @param cacheClearServices A list of DescriptorID -> List of services.
	 */
	public void setCacheClearServices(Map<String,Map<Integer,String>> cacheClearServices);

		
	/** Get cache clear services.
	 * 
	 * @return A list of DescriptorID -> List of services.
	 */
	public Map<String,Map<Integer,String>> getCacheClearServices();
	
	
	/** Set the hash value representing the revision of the cache data.
	 * 
	 * @param revision hash value of metadata.
	 */
	public void setCurrentRevision(String revision);
	
	
	/** Get the hash value representing the revision of the cache data.
	 * 
	 * @return revision hash value .
	 */
	public String getCurrentRevision();
	
	/** Returns a list of acceptable identifier types for this SP to be used in generation of responses
	 * @return List of acceptable NameID format identifiers
	 */
	public Map<String, List<String>> getAssertionConsumerServiceIdentifierTypes();

	/** Set a list of acceptable identifier types for a service provider ACS
	 * @param assertionConsumerServiceIdentifierTypes
	 */
	public void setAssertionConsumerServiceIdentifierTypes(Map<String, List<String>> assertionConsumerServiceIdentifierTypes);
	
	
	/** Get the keyMap as obtained from the metadata file. 
	 * 
	 * @return Map of SPEP ID's -> KeyData object
	 */
	public Map<String, KeyData> getKeyMap();
	
	
	/** set the keyMap as obtained from the metadata file. 
	 * 
	 * @param keyMap Map of SPEP ID's -> KeyData object
	 */
	public void setKeyMap(Map<String, KeyData> keyMap);
	
	
	/** Set the state of the cache.
	 * 
	 * @param state One of MetadataCache.State.x
	 */
	public void setState(State state);
	
	/** Get the state of the cache.
	 * 
	 * @param state One of MetadataCache.State.x
	 */
	public State getState();
	
}
