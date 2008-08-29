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
 * Author: Bradley Beddoes
 * Creation Date: 28/09/2006
 * 
 * Purpose: Interface specifying operations that must be present on implemented logic to handle SAML SSO interactions for authn and single logout
 */
package com.qut.middleware.esoe.logout;

import com.qut.middleware.esoe.logout.bean.LogoutProcessorData;
import com.qut.middleware.esoe.logout.exception.InvalidRequestException;
import com.qut.middleware.esoe.logout.exception.InvalidSessionIdentifierException;

/** Interface specifying operations that must be present on implemented logic to handle SAML SSO interactions for authn and single logout. */
public interface LogoutProcessor
{
	/** Results that this interface will return from its publicly facing methods */
	public static enum result
	{
		/** Indicates that the logout operation was successful */
		LogoutSuccessful, 
		/** Failure result when attempting to send a logout request to an SPEP */
		LogoutRequestFailed
	};
	
	/** Performs the logic required to logout a principal session from all currently active sessions on all
	 * SPEP's which contain an active session for the principal. The implentation must ensure that any failed 
	 * logouts are queued for later delivery. It may also store logout state for statistical use or display to the user 
	 * by a web session interface.
	 * 
	 * @param data Instantiated and partially populated data bean to operate on
	 * @return A LogoutProcessor.result value indicating the result of the execute function on the supplied request
	 * @throws InvalidSessionIdentifierException if the sessionID contained in the given data bean is not valid.
	 * @throws InvalidRequestException
	 */
	public result execute(LogoutProcessorData data) throws InvalidSessionIdentifierException, InvalidRequestException;
}
