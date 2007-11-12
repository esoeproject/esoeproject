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
 * Creation Date: 18/12/2006
 * 
 * Purpose: Exception to indicate that the SPEP failed to initialize.
 */
package com.qut.middleware.spep.filter.exception;

/** Exception to indicate that the SPEP failed to initialize. */
public class SPEPInitializationException extends Exception
{
	private static final long serialVersionUID = 6796930229870653438L;

	/**
	 * @param message The message explaining what caused the exception
	 * @param cause The exception that caused this exception to be thrown
	 */
	public SPEPInitializationException(String message, Exception cause)
	{
		super(message, cause);
	}
	
	/**
	 * @param message The message explaining what caused the exception
	 */
	public SPEPInitializationException(String message)
	{
		super(message);
	}
}
