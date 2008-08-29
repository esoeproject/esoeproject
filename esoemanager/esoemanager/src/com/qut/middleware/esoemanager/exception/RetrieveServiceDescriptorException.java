/* Copyright 2008, Queensland University of Technology
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
 */
package com.qut.middleware.esoemanager.exception;

public class RetrieveServiceDescriptorException extends Exception
{
	/**
	 * Constructor where a message is specified.
	 * @param message The message explaining the exception.
	 */
	public RetrieveServiceDescriptorException(String message)
	{
		super(message);
	}

	/**
	 * Constructor where a message is specified along with a cause.
	 * @param message The message explaining the exception.
	 * @param cause The exception that caused the request to be made invalid.
	 */
	public RetrieveServiceDescriptorException(String message, Exception cause)
	{
		super(message, cause);
	}
}
