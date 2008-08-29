/*
 * Copyright 2006, Queensland University of Technology
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
 * Creation Date: 10/10/2006
 *
 * Purpose: Data access interface for a user in LDAP.
 */
package com.qut.middleware.esoe.identity.plugins.ldap.mapper;

/** Data access interface for a user in LDAP. */
public interface User
{
	/**
	 * Adds a value to an underlying attribute.
	 * @param attributeName The attribute to add the value to.
	 * @param value The value to add.
	 */
	public void addAttributeValue(String attributeName, Object value);
}
