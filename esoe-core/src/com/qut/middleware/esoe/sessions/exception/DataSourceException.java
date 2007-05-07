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
 * Purpose: Thrown when an error occurs with the underlying data source while
 * trying to resolve an attribute.
 */
package com.qut.middleware.esoe.sessions.exception;

/** Thrown when an error occurs with the underlying data source while
 * trying to resolve an attribute.
 * */
public class DataSourceException extends Exception
{
	private static final long serialVersionUID = 2284245833596082494L;

	/**
	 * Default constructor.
	 */
	public DataSourceException()
	{
		super();
	}

	/**
	 * Constructor where the cause is specified.
	 * 
	 * @param cause
	 *            The exception which caused this one to be generated.
	 */
	public DataSourceException(Exception cause)
	{
		super(cause);
	}

	/**
	 * Constructor where the message is specified.
	 * 
	 * @param message
	 *            The message describing the error condition.
	 */
	public DataSourceException(String message)
	{
		super(message);
	}

}
