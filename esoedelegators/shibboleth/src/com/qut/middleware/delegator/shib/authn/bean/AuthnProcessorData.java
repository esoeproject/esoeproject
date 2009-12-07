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
 * Purpose: Interface for describing databean sent between client facing servlet and authn processor
 */
package com.qut.middleware.delegator.shib.authn.bean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface AuthnProcessorData
{
	public final static String SESSION_NAME = AuthnProcessorData.class.getName();
	/**
	 * @return HTTP Request object
	 */
	public HttpServletRequest getHttpRequest();

	/**
	 * @param request
	 *            HTTP Request object
	 */
	public void setHttpRequest(HttpServletRequest request);

	/**
	 * @return HTTP Response object
	 */
	public HttpServletResponse getHttpResponse();

	/**
	 * @param response
	 *            HTTP Response object
	 */
	public void setHttpResponse(HttpServletResponse response);
	
	/**
	 * @return The session ID associated with this authn data.
	 */
	public String getSessionID();

	/**
	 * @param sessionID
	 *            The session ID associated with this authn data.
	 */
	public void setSessionID(String sessionID);
}
