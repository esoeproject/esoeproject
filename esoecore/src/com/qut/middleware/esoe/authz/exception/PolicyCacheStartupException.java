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
 * Author: Andre Zitelli
 * Creation Date: 10/10/2006
 * 
 * Purpose: An exception class for policy cache startup errors.
 */
package com.qut.middleware.esoe.authz.exception;

/** */
public class PolicyCacheStartupException extends Exception
{
	private static final long serialVersionUID = -6948439105551454958L;
	
	/**
	 * @param message The message that explains what caused the exception
	 */
	public PolicyCacheStartupException(String message)
	{
		super(message);
	}
}
