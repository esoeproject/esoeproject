/* 
 * Copyright 2007, Queensland University of Technology
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
 * Creation Date: 09/10/2007
 * 
 * Purpose: An Interface of basic types to wrap around the SPEP interface. This allows us to implement much cleaner dynamic proxying between the
 * SPEP filter implementation and SPEP itself removing all the need to define complex interface types such as Metadata in the remote app.
 */
package com.qut.middleware.spep;

import java.util.List;

import javax.servlet.http.Cookie;

import com.qut.middleware.spep.sessions.PrincipalSession;

public interface SPEPProxy
{
	public enum defaultAction {permit, deny};
	
	/* Maps exactly to PolicyEnformentPoint.decision */
	public enum decision { permit, deny, error, notcached };
	
	/**
	 * @return Boolean true if SPEP has started successfully.
	 */
	public boolean isStarted();

	/**
	 * @return Determines the site wide ESOE session token, has no purpose except to indicate ESOE has quite probably established a session for this user already
	 */
	public String getEsoeGlobalTokenName();
	
	/**
	 * Validates that a session exists in the session cache and has not expired.
	 * @param sessionID The session ID to search for
	 * @return Client session, or null if it couldn't be verified.
	 */
	public PrincipalSession verifySession(String sessionID);
	
	/**
	 * Makes an authorization decision.
	 * @param sessionID The session ID to evaluate the decision for.
	 * @param resource The resource being accessed
	 * @return The decision made by or on behalf of the PDP
	 */
	public decision makeAuthzDecision(String sessionID, String resource);
	
	/**
	 * Makes an authorization decision.
	 * @param sessionID The session ID to evaluate the decision for.
	 * @param resource The resource being accessed
	 * @param action The action being undertaken on the resource
	 * @return The decision made by or on behalf of the PDP
	 */
	public decision makeAuthzDecision(String sessionID, String resource, String action);
	
	/**
	 * Live list of cookies to be cleared when a session is logged out.
	 * This list should not be modified in any way. Any cookies needing to be
	 * modified should be cloned first.
	 * @return List of cookies.
	 */
	public List<Cookie> getLogoutClearCookies();
	
	/**
	 * @return if this spep is operating in lazy mode or not
	 */
	public boolean isLazyInit();
	
	/**
	 * @return A list of resources which should be matched against to determine if SPEP should without get involved in the request and invoke the default action inverse
	 */
	public List<String> getLazyInitResources();
	
	/**
	 * @return The default action for lazy session init.
	 */
	public defaultAction getLazyInitDefaultAction();
	
	/** The name of the authentication token used by the system.
	 * 
	 * @return The name for the SPEP token.
	 */
	public String getTokenName();
	
	/**
	 * @return The service URL being presented by this spep (or pool of spep).
	 */
	public String getServiceHost();
	
	/**
	 * @return SSO web application redirect (relative)
	 */
	public String getSsoRedirect();
	
	/**
	 * @return The URL to redirect new sessions to.
	 */
	public String getDefaultUrl();
	
}
