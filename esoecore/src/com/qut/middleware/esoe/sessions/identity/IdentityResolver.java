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
 * Creation Date: 28/09/2006
 * 
 * Purpose: The Indentity Resolver is primarily concerned with the utilisation 
 * of the identity handler pipeline. The session processor will traverse the 
 * entire list, and each handler will be called to resolve the identity data.
 */
package com.qut.middleware.esoe.sessions.identity;

import java.util.List;

import com.qut.middleware.esoe.sessions.bean.IdentityData;
import com.qut.middleware.esoe.sessions.exception.DataSourceException;
import com.qut.middleware.esoe.sessions.exception.DuplicateSessionException;
import com.qut.middleware.esoe.sessions.exception.HandlerRegistrationException;
import com.qut.middleware.esoe.sessions.identity.pipeline.Handler;

/**
 * The Indentity Resolver is primarily concerned with the utilisation 
 * of the identity handler pipeline. The session processor will traverse the 
 * entire list, and each handler will be called to resolve the identity data.
 *  */
public interface IdentityResolver
{
	/** Results that this interface will return from its publicly facing methods */
	public static enum result
	{
		/** Returned when the resolver successfully processes */
		Successful
	};

	/**
	 * Registers a handler to become part of the attribute resolution pipeline.
	 * 
	 * @param handler
	 *            The handler to register.
	 * 
	 * @pre handler not null
	 * @post handler is registered in the pipeline
	 */
	public void registerHandler(Handler handler);

	/**
	 * Accessor method that returns a list of registered handlers.
	 * 
	 * @return List of Handlers
	 */
	public List<Handler> getRegisteredHandlers();

	/**
	 * <Details about this method>
	 * 
	 * @param data
	 * @throws DataSourceException
	 * @throws HandlerRegistrationException
	 * @throws DuplicateSessionException
	 */
	public void execute(IdentityData data) throws DataSourceException, HandlerRegistrationException,
			DuplicateSessionException;
}
