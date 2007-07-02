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
 * Author: Shaun Mangelsdorf
 * Creation Date: 19/10/2006
 * 
 * Purpose: Interface for the attribute authority, which completes attribute request.
 */
package com.qut.middleware.esoe.aa;

import com.qut.middleware.esoe.aa.bean.AAProcessorData;
import com.qut.middleware.esoe.aa.exception.InvalidPrincipalException;
import com.qut.middleware.esoe.aa.exception.InvalidRequestException;

/** */
public interface AttributeAuthorityProcessor
{
	/** Results that this interface will return from its publicly facing methods */
	public static enum result
	{
		/**
		 * Indicates that the request has been processed successfully.
		 */
		Successful
	};
	
	/** Processes an attribute query and generates a SAML response.
	 * 
	 * @param processorData The processor data bean containing the query
	 * @return result.Successful if the request was successful
	 * @throws InvalidPrincipalException if the principal contained in the processorData was not found.
	 * @throws InvalidRequestException if the SAML Request document contained in the processorData
	 * fails validation.
	 */
	public result execute(AAProcessorData processorData) throws InvalidPrincipalException, InvalidRequestException;
}
