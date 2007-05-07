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
 * Creation Date: 13/11/2006
 * 
 * Purpose: Indicates that there was a security-related error with the requested operation
 */
package com.qut.middleware.spep.exception;

/** Indicates that there was a security-related error with the requested operation.*/
public class SecurityException extends Exception
{
	private static final long serialVersionUID = -4175489883363027533L;

	/**
	 * Constructor
	 * @param message The message explaining why the exception was generated
	 */
	public SecurityException(String message)
	{
		super(message);
	}
	
	/**
	 * Constructor
	 * @param cause The exception that caused this exception to be generated
	 */
	public SecurityException(Exception cause)
	{
		super(cause);
	}
}
