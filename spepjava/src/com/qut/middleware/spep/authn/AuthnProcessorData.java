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
	
	public String getBindingIdentifier();
	
	public void setBindingIdentifier(String bindingIdentifier);
	
	public boolean isReturningRequest();
	
	public void setReturningRequest();
	
	public String getDestinationURL();
	
	public void setDestinationURL(String destinationURL);

	public String getSSORequestServerName();
	
	public void setSSORequestServerName(String ssoRequestServerName);
	
	public String getSSORequestURI();
	
	public void setSSORequestURI(String ssoRequestURI);
}
