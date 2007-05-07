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
 * Purpose: Indicates that an invalid principal was referenced.
 */
package com.qut.middleware.esoe.aa.exception;

public class InvalidPrincipalException extends Exception
{
	private static final long serialVersionUID = -930141575895825907L;
	
	/**
	 * Constructor.
	 * @param message Message explaining the reason for the exception
	 */
	public InvalidPrincipalException(String message)
	{
		super(message);
	}
}
