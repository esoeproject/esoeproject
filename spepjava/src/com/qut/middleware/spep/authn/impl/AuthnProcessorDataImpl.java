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
 * Creation Date:  24/11/2006
 * 
 * Purpose: Implements the AuthnProcessorData interface.
 */
package com.qut.middleware.spep.authn.impl;

import com.qut.middleware.spep.authn.AuthnProcessorData;

/** Implements the AuthnProcessorData interface. Not thread safe. */
public class AuthnProcessorDataImpl implements AuthnProcessorData
{
	private String requestURL;
	private String sessionID;
	private String bindingIdentifier;
	private boolean returningRequest;
	private String destinationURL;
	private String ssoRequestServerName;
	private String ssoRequestURI;
	
	public AuthnProcessorDataImpl()
	{
		this.returningRequest = false;
	}
	
	public String getRequestURL()
	{
		return requestURL;
	}
	public void setRequestURL(String requestURL)
	{
		this.requestURL = requestURL;
	}
	public String getSessionID()
	{
		return sessionID;
	}
	public void setSessionID(String sessionID)
	{
		this.sessionID = sessionID;
	}
	public String getBindingIdentifier()
	{
		return bindingIdentifier;
	}
	public void setBindingIdentifier(String bindingIdentifier)
	{
		this.bindingIdentifier = bindingIdentifier;
	}
	public boolean isReturningRequest()
	{
		return returningRequest;
	}
	public void setReturningRequest()
	{
		this.returningRequest = true;
	}
	public String getDestinationURL()
	{
		return destinationURL;
	}
	public void setDestinationURL(String destinationURL)
	{
		this.destinationURL = destinationURL;
	}
	public String getSSORequestServerName()
	{
		return ssoRequestServerName;
	}
	public void setSSORequestServerName(String ssoRequestServerName)
	{
		this.ssoRequestServerName = ssoRequestServerName;
	}
	public String getSSORequestURI()
	{
		return ssoRequestURI;
	}
	public void setSSORequestURI(String ssoRequestURI)
	{
		this.ssoRequestURI = ssoRequestURI;
	}
	
	
}
