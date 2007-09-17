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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qut.middleware.spep.authn.AuthnProcessorData;

/** Implements the AuthnProcessorData interface. Not thread safe. */
public class AuthnProcessorDataImpl implements AuthnProcessorData
{

	private byte[] requestDocument;
	private String requestURL;
	private byte[] responseDocument;
	private String sessionID;
	private HttpServletRequest request;
	private HttpServletResponse response;

	public HttpServletRequest getRequest()
	{
		return request;
	}

	public void setRequest(HttpServletRequest request)
	{
		this.request = request;
	}

	public HttpServletResponse getResponse()
	{
		return response;
	}

	public void setResponse(HttpServletResponse response)
	{
		this.response = response;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.authn.AuthnProcessorData#getRequestDocument()
	 */
	public byte[] getRequestDocument()
	{
		return this.requestDocument;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.authn.AuthnProcessorData#getRequestURL()
	 */
	public String getRequestURL()
	{
		return this.requestURL;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.authn.AuthnProcessorData#getResponseDocument()
	 */
	public byte[] getResponseDocument()
	{
		return this.responseDocument;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.authn.AuthnProcessorData#getSessionID()
	 */
	public String getSessionID()
	{
		return this.sessionID;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.authn.AuthnProcessorData#setRequestDocument(java.lang.String)
	 */
	public void setRequestDocument(byte[] requestDocument)
	{
		this.requestDocument = requestDocument;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.authn.AuthnProcessorData#setRequestURL(java.lang.String)
	 */
	public void setRequestURL(String requestURL)
	{
		this.requestURL = requestURL;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.authn.AuthnProcessorData#setResponseDocument(java.lang.String)
	 */
	public void setResponseDocument(byte[] responseDocument)
	{
		this.responseDocument = responseDocument;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.authn.AuthnProcessorData#setSessionID(java.lang.String)
	 */
	public void setSessionID(String sessionID)
	{
		this.sessionID = sessionID;
	}
}
