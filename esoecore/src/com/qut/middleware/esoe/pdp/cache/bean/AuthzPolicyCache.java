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

import com.qut.middleware.saml2.schemas.esoe.lxacml.Policy;

/** A global cache object used to store LXACML Policy objects retrieved from in an external data source. Implementations of this 
 * Interface MUST ensure that all operations are thread safe.
 *  */
public interface AuthzPolicyCache
{
	public final static long SEQUENCE_UNINITIALIZED = -1647l;
	
	/**
	 * Add a policy object to the cache. The implementation must ignore null values.
	 * 
	 * @param entityID
	 *            The entity ID (value of <Issuer>) that will be used to retrieve the policy set.
	 * @param policy
	 *            The policy set to add.
	 */
	public void add(String entityID, List<Policy> policy);

	/**
	 * Remove the requested policy set from the cache.
	 * 
	 * @param entityID
	 *            The policy set to remove, as identified by the descriptor ID.
	 * @return true if the descriptor exists and policy is removed, else false.
	 */
	public boolean remove(String entityID);

	/**
	 * Retrieve the policies associated with the entityID. The implementation MUST ensure that the returned
	 * list is thread safe.
	 * 
	 * @param entityID
	 *            The entityID of the policies to retrieve.
	 * @return A zero or more sized List of policies associated with the given entity.
	 */
	public List<Policy> getPolicies(String entityID);

	/**
	 * Get the map representation of the cache. The cache object is a map of entity ID strings to the corresponding
	 * List of Policy objects. The implementation of this method MUST ensure that the returned reference is synchronized.
	 * contents.
	 * 
	 * @deprecated Removed in .04 beta.
	 * @return A copy of the internal map maintained by this cache obkect.
	 */
	public Map<String, List<Policy>> getCache();

	/**
	 * Set the cache map object. The implementation of this method MUST ensure that only one thread can set the cache at
	 * any time, and that all read access is blocked until the cache update has completed.
	 * 
	 * @pre newData != null
	 * @param newData The cache to replace the existing cache.
	 */
	public void setCache(Map<String, List<Policy>> newData);
	
	/** Set the sequence number used to determine the latest build of the Policy cache.
	 * 
	 * @param sequenceId 
	 */
	public void setBuildSequenceId(long sequenceId);
	
	/** Set the sequence number used to determine the latest build of the Policy cache.
	 *
	 *@return the sequenceId as set by this.setBuildSequenceId if called, else the value
	 * of SEQUENCE_UNINITIALIZED as initialized by the implementing constructor.  
	 */
	public long getBuildSequenceId();
	
	/**
	 *  Retrieve the number of PolicySet objects stored in the cache.
	 *  
	 */
	public int getSize();
}
