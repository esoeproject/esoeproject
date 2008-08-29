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
 * Author: Bradley Beddoes
 * Creation Date: 23/10/2006
 * 
 * Purpose: Exception thrown when a signature block reference validation exception has occured
 */
package com.qut.middleware.saml2.exception;

/** Exception thrown when a signature block reference validation exception has occured. */
public class ReferenceValueException extends Exception
{
	private static final long serialVersionUID = 6439684401066474590L;
	private Object jaxbObject;

	/**
	 * Exception thrown when a signature block reference validation exception has occuredd.
	 * 
	 * @param message Human readable message indicating why this exception was thrown
	 * @param cause Any exception which caused this exception to be thrown, may be null
	 */
	public ReferenceValueException(String message, Exception cause, Object jaxbObject)
	{
		super(message, cause);
		this.jaxbObject = jaxbObject;
	}
	
	/**
	 * Note that since signature validation has failed at this stage, the object returned
	 * here should not be trusted for anything other than generating log output.
	 * @return The JAXB Object containing the document that failed signature validation.
	 */
	public Object getJAXBObject()
	{
		return this.jaxbObject;
	}
}
