/* 
 * Copyright 2008, Queensland University of Technology
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
 * Creation Date: 26/08/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.esoe.sso.plugins.redirect.exception;

public class RedirectBindingException extends Exception
{
	private static final long serialVersionUID = -46133801052715884L;

	public RedirectBindingException()
	{
	}

	public RedirectBindingException(String message)
	{
		super(message);
	}

	public RedirectBindingException(Throwable cause)
	{
		super(cause);
	}

	public RedirectBindingException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
