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
 * Creation Date: 05/03/2007
 * 
 * Purpose: Interface to allow remote delegated authn handlers to process authentication specific to some known protocol and insert into the ESOE
 */

package com.qut.middleware.esoe.delegauthn;

import com.qut.middleware.esoe.delegauthn.bean.DelegatedAuthenticationData;

public interface DelegatedAuthenticationProcessor
{
	/** Results that this interface will return from its publicly facing methods */
	public static enum result
	{
		/**
		 * Indicates that the request has been processed successfully.
		 */
		Successful, Failure
	};
	
	/**
	 * Takes incoming RegisterPrincipalRequest, verifies it and if successful attempts to register
	 * the principal into our system to use internal resources and be subject to normal LXACML, Attribute Request etc.
	 * @param processorData Populated processor data object
	 * @return Successful if session is populated and able to be used, Failed otherwise
	 */
	public result execute(DelegatedAuthenticationData processorData);
}
