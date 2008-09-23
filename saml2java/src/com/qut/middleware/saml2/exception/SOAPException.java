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
 * Creation Date: 19/09/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.saml2.exception;

import java.util.Collection;

import org.w3c.dom.Element;

public class SOAPException extends Exception
{
	private static final long serialVersionUID = -643355673660006011L;
	private boolean fault = false;
	private String faultCode = null;
	private String faultMessage = null;
	private Collection<Element> faultDetail = null;

	public SOAPException()
	{
		super();
	}

	public SOAPException(String arg0, Throwable arg1)
	{
		super(arg0, arg1);
	}

	public SOAPException(String arg0)
	{
		super(arg0);
	}

	public SOAPException(Throwable arg0)
	{
		super(arg0);
	}
	
	public SOAPException(String faultCode, String faultMessage, Collection<Element> faultDetail)
	{
		this.fault = true;
		this.faultCode = faultCode;
		this.faultMessage = faultMessage;
		this.faultDetail = faultDetail;
	}

	public boolean isFault()
	{
		return this.fault;
	}

	public String getFaultCode()
	{
		return this.faultCode;
	}

	public String getFaultMessage()
	{
		return this.faultMessage;
	}

	public Collection<Element> getFaultDetail()
	{
		return this.faultDetail;
	}
}
