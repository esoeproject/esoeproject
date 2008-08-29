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
package com.qut.middleware.esoemanager.client.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;

public class EsoeManagerServiceException extends Exception implements IsSerializable
{
	public EsoeManagerServiceException()
	{
		super();
	}

	public EsoeManagerServiceException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public EsoeManagerServiceException(String message)
	{
		super(message);
	}

	public EsoeManagerServiceException(Throwable cause)
	{
		super(cause);
	}
}
