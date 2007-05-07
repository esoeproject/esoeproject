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
 * Purpose: Interface for handlers to be used in the identity resolver's pipeline
 */
package com.qut.middleware.esoe.sessions.identity.pipeline;

import com.qut.middleware.esoe.sessions.bean.IdentityData;
import com.qut.middleware.esoe.sessions.exception.DataSourceException;

/** 
 * Interface for handlers to be used in the identity resolver's pipeline.
 * */
public interface Handler
{
	/** Results that this interface will return from its publicly facing methods */
	public static enum result
	{
		/** Returned when the handler successfully processes the attribute */
		Successful
	};

	/**
	 * Utilises the value of data.identity to iterate through all values in the identity configuration. To attempt to
	 * populate values, the handler MUST ensure that this attribute is configured to be handled by the implementing
	 * class.
	 * 
	 * <br>PRE: data is not null<br>
	 * 
	 * @param data
	 *            The identity data to populate
	 * @return A result indicating if the operation was successful.
	 * @throws DataSourceException
	 *             If an error occurs with the data source for the handler.
	 */
	public result execute(IdentityData data) throws DataSourceException;

	/**
	 * Returns the name of this handler, as it is to be specified in the configuration file for this component.
	 * 
	 * @return String name of this handler.
	 */
	public String getHandlerName();
}
