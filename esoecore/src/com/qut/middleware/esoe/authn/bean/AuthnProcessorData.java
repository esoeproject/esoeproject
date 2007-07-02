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
 * Creation Date: 05/10/2006
 * 
 * Purpose: Interface for describing databean sent between client facing servlet and authn processor
 */
package com.qut.middleware.esoe.authn.bean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** */
public interface AuthnProcessorData
{
	/** Session identifier for this data bean */
	public static String SESSION_NAME = "com.qut.middleware.esoe.authn.bean"; //$NON-NLS-1$

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
	 * Indicates whether an automated SSO handler is permitted to complete the identification process.
	 * 
	 * @return Value indicating expected behaviour
	 */
	public boolean getAutomatedSSO();

	/**
	 *  
	 * @param automatedSSO
	 *            Boolean indicating whether an automated SSO handler is permitted to complete the identification
	 *            process.
	 */
	public void setAutomatedSSO(boolean automatedSSO);

	/**
	 * @return The session ID associated with this authn data.
	 */
	public String getSessionID();

	/**
	 * @param sessionID
	 *            The session ID associated with this authn data.
	 */
	public void setSessionID(String sessionID);

	/**
	 * @return The location for the client to be redirected after identification has occurred or a non fatal (expected) error occurs.
	 */
	public String getRedirectTarget();

	/**
	 * @param redirectTarget
	 *            The location for the client to be redirected after identification has occurred or a non fatal (expected) error occurs.
	 */
	public void setRedirectTarget(String redirectTarget);

	/**
	 * @return The current handler that is processing the Authn request
	 */
	public String getCurrentHandler();

	/**
	 * @param currentHandler
	 *            The current handler that is processing the Authn request
	 */
	public void setCurrentHandler(String currentHandler);

	/**
	 * @return Returns true if authn has been successfully completed.
	 */
	public boolean getSuccessfulAuthn();

	/**
	 * @param successfulAuthn
	 *            Value indicating if authn has been successfully completed.
	 */
	public void setSuccessfulAuthn(boolean successfulAuthn);

	/**
	 * @return The URL to redirect the client to upon an invalid authentication state being reached.
	 */
	public String getInvalidURL();

	/**
	 * @param failURL
	 *            The URL to redirect the client to upon an invalid authentication state being reached.
	 */
	public void setInvalidURL(String failURL);
		
	/** Get the HTTP error code of the contained request.
	 *
	 */
	public int getErrorCode();
	
	/** Get the HTTP error message of the contained request.
	 *
	 */
	public String getErrorMessage();	

	/** Set the HTTP error code of the contained request.
	 * 
	 * @param errorCode A valid HttpServletResponse error constant.
	 * @param errorMsg
	 *            The message to send to the user agent with the HTTP error code.
	 */
	public void setError(int errorCode, String errorMsg);	
	
	/** Get the name of the principal being authenticated.
	 */
	public String getPrincipalName();
	
	
	/** Set the name of the principal being authenticated.
	 * 
	 * @param principalName
	 *            The principal identifier used to create a session.
	 */
	public void setPrincipalName(String principalName);
}
