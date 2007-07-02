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
 * Author: Bradley beddoes
 * Creation Date: 05/10/2006
 * 
 * Purpose: Defines interface which all authn processor implementations must adhere to
 */
package com.qut.middleware.esoe.authn;

import java.util.List;

import com.qut.middleware.esoe.authn.bean.AuthnProcessorData;
import com.qut.middleware.esoe.authn.exception.AuthnFailureException;
import com.qut.middleware.esoe.authn.pipeline.Handler;

/** */
public interface AuthnProcessor
{
	/** Results that this interface will return from its publicly facing methods */
	public static enum result
	{
		/** Indicates that the authentication operation was completed successfully */
		Completed,
		/** Indicates a failure (but client interaction expected failure) occurred while processing the authentication operation */
		Failure,
		/** Indicates an invalid state in the authentication system */
		Invalid,
		/** Indicates a failure in the authentication system because of the way the user-agent communicated */
		UserAgent
	};

	/**
	 * Main AuthnProcessor logic. Will process all registered handlers attempting to authenticate the principal, once a
	 * user is successfully authenticated by a handler they will have appropriate identifiers for their session set in
	 * the user-agent. Where a handler has specified that the user-agent must undertake some action (such as URI
	 * redirect) this operation will be passed back to the caller for implementation.
	 * 
	 * 
	 * 
	 * @param data
	 *            Bean passed from authnservlet containing request specific detail
	 * @return The outcome of the implemented handlers
	 * @throws AuthnFailureException
	 */
	public result execute(AuthnProcessorData data) throws AuthnFailureException;

	/**
	 * @return A list of handlers that are registered to the AuthnProcessor instance
	 */
	public List<Handler> getRegisteredHandlers();
}
