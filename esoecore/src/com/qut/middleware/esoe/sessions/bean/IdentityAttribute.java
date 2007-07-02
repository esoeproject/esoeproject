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
 * Purpose: Interface for the identity attribute data bean.
 */
package com.qut.middleware.esoe.sessions.bean;

import java.util.List;

/** */
public interface IdentityAttribute
{
	/**
	 * Accessor for attribute type.
	 * 
	 * @return String value of attribute type.
	 */
	public String getType();

	/**
	 * Mutator for attribute type.
	 * 
	 * @param type Attribute type to set.
	 * 
	 * Currently supported values from schema sessiondata-schema.xsd:
	 * String
	 * Integer
	 * Float
	 * Date
	 * Boolean
	 * Other
	 */
	public void setType(String type);

	/**
	 * Accessor for list of values for this attribute.
	 * 
	 * @return List of values.
	 */
	public List<Object> getValues();

	/**
	 * Mutator for list of values. Adds a value to the list.
	 * 
	 * @param value
	 *            The value to add.
	 */
	public void addValue(Object value);

	/**
	 * Returns a list of handlers that are responsible for populating this attribute.
	 * 
	 * @return The list of handlers.
	 */
	public List<String> getHandlers();
}
