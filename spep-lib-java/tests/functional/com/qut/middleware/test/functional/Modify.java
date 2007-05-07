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
 * Creation Date: 09/11/2006
 * 
 * Purpose: Allows a mock object to capture and alter the data in an object that would otherwise 
 * not be accessible from a public scope. This object must be overriden to define an arbitrary 
 * operation that will be performed on the parameter.
 */
package com.qut.middleware.test.functional;

import static org.easymock.EasyMock.reportMatcher;

import org.easymock.IArgumentMatcher;

import com.qut.middleware.test.functional.Modify;

/** */
public abstract class Modify<T> implements IArgumentMatcher
{
	public void appendTo(StringBuffer buffer)
	{
		buffer.append("modify()");
	}

	public boolean matches(Object parameter)
	{
		this.operate((T) parameter);

		return true;
	}

	public static <T> T modify(Modify<T> modify)
	{
		reportMatcher(modify);

		return null;
	}

	public abstract void operate(T object);
}
