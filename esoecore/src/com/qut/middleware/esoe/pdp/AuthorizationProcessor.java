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
 * Creation Date: 4/10/2006
 * 
 * Purpose: Processes authorization requests recieved from external sources. Policies are generated
 * from LAXACML documents stored in an arbitrary location. The LAXCML policies are parsed and
 * used to match the incoming resource request. A SAML 2.0 response is then generated and sent based
 * on the outcome of the policy rules matching.
 *  
 */
package com.qut.middleware.esoe.pdp;

import com.qut.middleware.esoe.pdp.bean.AuthorizationProcessorData;
import com.qut.middleware.esoe.pdp.exception.InvalidRequestException;

/** */
public interface AuthorizationProcessor 
{
	/** Results that this interface will return from its publicly facing methods */
	public static enum result {
		/** Indicates a successful result */
		Successful
	};
		
	 
	/** Validate the given <code>AuthorizationProcessorData</code> and process the internal
	 * <code>Request</code> against all authorization policies. If the request contained in 
	 * the given processor data is a valid opensaml 2.0 request, this method shall initiate
	 * processing factors to determine the authorization status of the request.
	 * 
	 * On completed validation of the request, the implementation MUST guarantee to set the
	 * <code>AuthorizationProcessorData</code> object with a valid open SAML 2.0 response
	 * object AND the <code>Response</code> object will be guaranteed to contain a valid 
	 * LXACMLAuthzDecisionStatement containing the result data of the authorization request.
	 * This MUST happen regardless of external errors affecting processing of the request (IE 
	 * before any exceptions are thrown).
	 *  
	 * @param request An AuthorizationProcessorData object containing a valid SAML 2.0 request
	 * object, which inturn must contain a valid LXACML authorization request statement for some
	 * specified resource.
	 * @return A result indicating that the request was successful
	 * 
	 * @throws InvalidRequestException if the embedded <code>Request</code> is not a valid SAML 2.0 
	 * request, or if some external error occurs in the processing of the authorization request.
	 */
	public result execute(AuthorizationProcessorData request) throws InvalidRequestException;

	 
}
