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
package com.qut.middleware.esoe.sso;

import com.qut.middleware.esoe.sso.bean.SSOProcessorData;
import com.qut.middleware.esoe.sso.exception.InvalidRequestException;
import com.qut.middleware.esoe.sso.exception.InvalidSessionIdentifierException;

/** Interface specifying operations that must be present on implemented logic to handle SAML SSO interactions for authn and single logout. */
public interface SSOProcessor
{
	/** Results that this interface will return from its publicly facing methods */
	public static enum result
	{
		/** Indicates that the SSO Generation was successful */
		SSOGenerationSuccessful, 
		/** Force authorization using a passive handler */
		ForcePassiveAuthn,
		/** Force authorization using a non-passive handler */
		ForceAuthn
	};
	
	/** Perform required logic.
	 * 
	 * @param data Instantiated and partially populated data bean to operate on
	 * @return A SSOProcessor.result value indicating the result of the execute function on the supplied request
	 * @throws InvalidSessionIdentifierException
	 * @throws InvalidRequestException
	 */
	public result execute(SSOProcessorData data) throws InvalidSessionIdentifierException, InvalidRequestException;
}
