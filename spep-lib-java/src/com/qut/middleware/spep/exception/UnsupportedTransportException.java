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
 * Creation Date: 22/11/2006
 * 
 * Purpose: Indicates that an unsupported transport was used in a URL. 
 */
package com.qut.middleware.spep.exception;

/** Indicates that an unsupported transport was used in a URL.  */
public class UnsupportedTransportException extends Exception
{
	private static final long serialVersionUID = -7463866506526676487L;
	
	/**
	 * @param message The message explaining what caused the exception to be generated.
	 */
	public UnsupportedTransportException(String message)
	{
		super(message);
	}
}
