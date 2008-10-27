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
 * 
 * Author: Shaun Mangelsdorf
 * Creation Date: 29/09/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.spep.ws.exception;

public class WSProcessorException extends Exception
{
	private static final long serialVersionUID = -5547387576149377905L;

	public WSProcessorException()
	{
	}

	public WSProcessorException(String arg0)
	{
		super(arg0);
	}

	public WSProcessorException(Throwable arg0)
	{
		super(arg0);
	}

	public WSProcessorException(String arg0, Throwable arg1)
	{
		super(arg0, arg1);
	}
}
