/* 
 * Copyright 2006, Queensland University of Technology
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
 * Creation Date: 13/10/2006
 * 
 * Purpose: A global cache object to store a list of authorization policies.
 */
package com.qut.middleware.esoe.pdp.cache.bean;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.qut.middleware.saml2.schemas.esoe.lxacml.Policy;

/** */
public interface AuthzPolicyCache
{
	/**
	 * Add a policy object to the cache.
	 * 
	 * @param descriptorID
	 *            The descriptor ID that will be used to retrieve the policy set.
	 * @param policy
	 *            The policy set to add.
	 */
	public void add(String descriptorID, Vector<Policy> policy);

	/**
	 * Remove the requested policy set from the cache.
	 * 
	 * @param descriptorID
	 *            The policy set to remove, as identified by the descriptor ID.
	 * @return true if the descriptor exists and policy is removed, else false.
	 */
	public boolean remove(String descriptorID);

	/**
	 * Retrieve the policies associated with the descriptor ID string. The implementation MUST ensure that the returned
	 * list is thread safe.
	 * 
	 * @param descriptorID
	 *            The descriptor ID of the objects to retrieve.
	 * @return The policy object if exists, else null.
	 */
	public List<Policy> getPolicies(String descriptorID);

	/**
	 * Get the map representation of the cache. The cache object is a map of policy ID strings to the corresponding
	 * policy set object. The implementation of this method MUST ensure that the returned reference is synchronized.
	 * contents.
	 * 
	 * @return The cache map.
	 */
	public Map<String, Vector<Policy>> getCache();

	/**
	 * Set the cache map object. The implementation of this method MUST ensure that only one thread can set the cache at
	 * any time, and that all read access is blocked until the cache update has completed.
	 * 
	 * @pre newData != null
	 * @param newData The cache to replace the existing cache.
	 */
	public void setCache(Map<String, Vector<Policy>> newData);
	
	
	/**
	 *  Retrieve the number of PolicySet objects stored in the cache.
	 *  
	 */
	public int getSize();
}
