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
 * Creation Date:
 * 
 * Purpose: A repository for storing authz cache update failures. NOTE: The implementation of this
 * interface MUST ensure that the underlying data structure used for storing the failure objects
 * is synchronised and thread safe.
 */

package com.qut.middleware.esoe.pdp.cache;

import java.util.List;

import com.qut.middleware.esoe.pdp.cache.bean.FailedAuthzCacheUpdate;

/** */
public interface AuthzCacheUpdateFailureRepository
{
	/** Add a failure object to the repository. The implementation should check for
	 * the existence of the requested addition and add it only if !exists.
	 * 
	 * @param failure The failure to add.
	 */
	public void add(FailedAuthzCacheUpdate failure);
	
	
	/** Remove a failure object from the repository.
	 * 
	 * @param failure The failure to remove.
	 */
	public void remove(FailedAuthzCacheUpdate failure);
	
	
	/** Retrieve the list of failure objects in the repository.
	 * 
	 * @return A list of failures in the repository. May be zero sized.
	 */
	public List<FailedAuthzCacheUpdate> getFailures();
	
	
	/** Clear the repository of all cache failure objects.
	 */
	public void clearFailures();	
	
	
	/** returns the number of failure objects in the repository
	 * 
	 * @return num failures.
	 */
	public int getSize();
	
}
