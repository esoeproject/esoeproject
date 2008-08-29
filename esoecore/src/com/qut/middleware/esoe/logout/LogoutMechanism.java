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
 * Creation Date: 20/03/2008
 */
package com.qut.middleware.esoe.logout;

import java.util.List;

import com.qut.middleware.esoe.logout.exception.InvalidSessionIdentifierException;

/** A simple logout mechanism to be used to perform the logout of a principal session on a single end point (SPEP). It
 * also contains several convenience methods for endpoint and session resolution.
 */
public interface LogoutMechanism
{

	/** Results that this interface will return from its publicly facing methods */
	public static enum result
	{
		/** Indicates that the logout operation was successful */
		LogoutSuccessful, 
		/** Failure result when attempting to send a logout request to an SPEP */
		LogoutRequestFailed
	};
	
		
	/** Log the given session out of the given endpoint. If the attempted logout fails and the storeFailedLogout param
	 * is set to true, the implementation MUST store the attempted logout request in a FailedLogoutRepository with
	 * appropriate values.
	 * 
	 * PRE: Given params are not null and logout sent = success -> successful result returned OR
	 * 		Given params are not null and logout sent = failure -> failure recorded if appropriate argument set and failure result returned.
	 * 
	 * @param sessionID The session identifiers on the SPEP to log out. Translates to session indicies in SAML land.
	 * @param endpoint The endpoint of the SPEP to log out of.
	 * @param storeFailedLogout Boolean indicating whether or not to add failed logouts to the failure repository.
	 * @return Success or failure of operation.
	 * @throws InvalidSessionIdentifierException If the given session ID is not valid on the endpoint.
	 */
	public result performSingleLogout(String samlAuthnID, List<String> sessionIDs, String endpoint, boolean storeFailedLogout)  ;
	
	
	/** Convenience method for classes who may not have access to a Metadata object. Returns all endpoints associated
	 *  with the given service.
	 * 
	 * @param entityID The identifier of the SPEP entity (service).
	 * @return A list of endpoints associated with the entity if exists, else an empty list.
	 */
	public List<String> getEndPoints(String entityID);
	
}
