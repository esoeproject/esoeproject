package com.qut.middleware.delegator.openid.authn.bean.impl;
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
 * Creation Date: 06/03/2007
 * 
 * Purpose: Implementation of AuthnProcessorData
 */

import static org.easymock.EasyMock.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qut.middleware.delegator.openid.authn.bean.AuthnProcessorData;

public class AuthnProcessorDataImpl implements AuthnProcessorData
{
	private HttpServletRequest request;
	private HttpServletResponse response;
	private String redirectTarget;
	private String sessionID;
	
	/* (non-Javadoc)
	 * @see com.qut.middleware.delegator.shib.authn.bean.AuthnProcessorData#getHttpRequest()
	 */
	public HttpServletRequest getHttpRequest()
	{
		return this.request;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.delegator.shib.authn.bean.AuthnProcessorData#getHttpResponse()
	 */
	public HttpServletResponse getHttpResponse()
	{
		return this.response;
	}

	public String getSessionID()
	{
		return this.sessionID;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.delegator.shib.authn.bean.AuthnProcessorData#setHttpRequest(javax.servlet.http.HttpServletRequest)
	 */
	public void setHttpRequest(HttpServletRequest request)
	{
		this.request = request;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.delegator.shib.authn.bean.AuthnProcessorData#setHttpResponse(javax.servlet.http.HttpServletResponse)
	 */
	public void setHttpResponse(HttpServletResponse response)
	{
		this.response = response;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.delegator.shib.authn.bean.AuthnProcessorData#setSessionID(java.lang.String)
	 */
	public void setSessionID(String sessionID)
	{
		this.sessionID = sessionID;
	}

}
