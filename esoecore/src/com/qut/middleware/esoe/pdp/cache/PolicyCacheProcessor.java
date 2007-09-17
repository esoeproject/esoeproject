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
 * Author: 
 * Creation Date: 28/09/2006
 * 
 * Purpose: 
 */
package com.qut.middleware.esoe.pdp.cache;


/** */
public interface PolicyCacheProcessor
{
	/** Results that this interface will return from its publicly facing methods */
	public static enum result
	{
		/** Indicates a successful operation */
		Success, 
		/** Indicates that a failure occurred */
		Failure
	};

	/**
	 * This method defines an entry point for a communication stream between a new instance of an SPEP and the policy
	 * cache. Calling of this method with valid parameters MUST initiate a procedure whereby the associated policy is
	 * interrogated, which results in the sending of a clearAuthzCache directive to the requesting endpoint.
	 * 
	 * @param entityID
	 *            The entityID of the policy bound to the requesting SPEP.
	 * @param authzCacheIndex
	 *            The index identifier of the SPEP to respond to.
	 * @return result indicating success or failure of the SPEP notification.
	 */
	public result spepStartingNotification(String entityID, int authzCacheIndex);
}
