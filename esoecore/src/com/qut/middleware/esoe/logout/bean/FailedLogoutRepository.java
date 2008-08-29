package com.qut.middleware.esoe.logout.bean;

import java.util.List;

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
 * Creation Date: 15/12/2006
 * 
 * Purpose: A repository for storing logout request failures. NOTE: The implementation of this
 * interface MUST ensure that the underlying data structure used for storing the failure objects
 * is synchronised and thread safe.
 */

/** A repository for storing logout request failures. NOTE: The implementation of this
 * interface MUST ensure that the underlying data structure used for storing the failure objects
 * is thread safe.
 */
public interface FailedLogoutRepository {

	/** Add a failure object to the repository.
	 * 
	 * @param failure The failure to add.
	 */
	public void add(FailedLogout failure);
	
	
	/** Remove a failure object from the repository.
	 * 
	 * @param failure The failure to remove.
	 */
	public void remove(FailedLogout failure);
	
	
	/** Retrieve the list of failure objects in the repository.
	 * 
	 * @return A list of failures in the repository. May be zero sized.
	 */
	public List<FailedLogout> getFailures();
	
	
	/** Clear the repository of all cache failure objects.
	 */
	public void clearFailures();	
	
	
	/** returns the number of failure objects in the repository
	 * 
	 * @return num failures.
	 */
	public int getSize();
	
	/** Returns whether or not the given failure currently exists in the repository. The comparison must return
	 * true the given failure matches a failure in the repository according to the object's .equals() method.
	 *  
	 * @param failure The FailedLogout to compare to stored failures.
	 * @return true if a match is found, esle false.
	 */
	public boolean containsFailure(FailedLogout failure); 
	
}
