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
 * Purpose: Interface for an authentication handler.
 */
package com.qut.middleware.esoe.authn.pipeline;

import com.qut.middleware.esoe.authn.bean.AuthnProcessorData;
import com.qut.middleware.esoe.authn.exception.SessionCreationException;

/** */
public interface Handler
{
	/** Results that this interface will return from its publicly facing methods */
	public static enum result
	{
		/** Indicates that the request was successful */
		Successful, 
		/** Indicates that the request was successful but did not generate a principal object */
		SuccessfulNonPrincipal, 
		/** Indicates a failure in the authentication handler */
		Failure, 
		/** Indicates an invalid state in the authentication handler */
		Invalid,
		/** Indicates that this handler took no action, and no error was generated */
		NoAction, 
		/** Indicates a failure in the authentication handler because of the way the user-agent communicated */
		UserAgent
	};
	
	/**
	 * Main Handler logic.
	 * Undertakes processing related to specific authn requirements that the implimentation is designed to solve, either
	 * for principal identification or non principal identification but related authn tasks. The processing will conform to one of
	 * the broad handler categories of Automated, Non Automated, Non Passive or Passive.
	 * 
	 * @param data An AuthnProcessorData bean containing details about the principals request
	 * @return The outcome of this handler.
	 * @throws SessionCreationException 
	 */
	public result execute(AuthnProcessorData data) throws SessionCreationException;
	
	/**
	 * @return The name of this handler
	 */
	public String getHandlerName();
}
