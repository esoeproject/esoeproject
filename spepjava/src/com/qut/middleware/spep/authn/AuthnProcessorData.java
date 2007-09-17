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
 * Creation Date: 24/11/2006
 * 
 * Purpose: Interface for storing intermediate Authentication data while the session
 * 		is being established.
 */
package com.qut.middleware.spep.authn;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/** Interface for storing intermediate Authentication data while the session
 * 		is being established.*/
public interface AuthnProcessorData
{
	/** Get the URL to be used to send Authn Requests to.
	 * 
	 * @return The request URL
	 */
	public String getRequestURL();
	
	/** Set the URL to be used to send Authn Requests to.
	 * 
	 * @param requestURL The request URL
	 */
	public void setRequestURL(String requestURL);
	
	/** Get the string representation of an AuthnRequest to be used to authenticate the Principal.
	 * 
	 * @return The request document
	 */
	public byte[] getRequestDocument();
	
	
	/** Set the string representation of an AuthnRequest to be used to authenticate the Principal.
	 * 
	 * @param requestDocument The request document.
	 */
	public void setRequestDocument(byte[] requestDocument);
	
	
	/** Get the string representation of the SAML Response sent from the ESOE in response to an 
	 * AuthnRequest.
	 * 
	 * @return The response document.
	 */
	public byte[] getResponseDocument();
	
	/** Set the string representation of the SAML Response sent from the ESOE in response to an 
	 * AuthnRequest.
	 * 
	 * @param responseDocument The response document.
	 */
	public void setResponseDocument(byte[] responseDocument);
	
	/** Get the client session ID used in authenticated sessions.
	 * 
	 * @return The session ID.
	 */
	public String getSessionID();
	
	/** Set the client session ID used in authenticated sessions.
	 * 
	 * @param sessionID The session ID
	 */
	public void setSessionID(String sessionID);
	
	public HttpServletRequest getRequest();

	public void setRequest(HttpServletRequest request);

	public HttpServletResponse getResponse();

	public void setResponse(HttpServletResponse response);
}
