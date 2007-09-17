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
 * Purpose: Indicates that the SPEP could not be registered
 */
package com.qut.middleware.esoe.spep.exception;

/** */
public class DatabaseFailureNoSuchSPEPException extends Exception
{
	private static final long serialVersionUID = 7488843736759602692L;
	
	public DatabaseFailureNoSuchSPEPException(String message)
	{
		super(message);
	}
	
	public DatabaseFailureNoSuchSPEPException(String message, Exception cause)
	{
		super(message, cause);
	}
	
	public DatabaseFailureNoSuchSPEPException(Exception cause)
	{
		super(cause);
	}
}
