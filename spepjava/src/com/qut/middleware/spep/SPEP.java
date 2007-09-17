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
 * Creation Date: 09/11/2006
 * 
 * Purpose: Interface for filters and other implementations of the SPEP to use this library.
 */
package com.qut.middleware.spep;

import java.util.List;

import javax.servlet.http.Cookie;

import com.qut.middleware.spep.attribute.AttributeProcessor;
import com.qut.middleware.spep.authn.AuthnProcessor;
import com.qut.middleware.spep.metadata.Metadata;
import com.qut.middleware.spep.pep.PolicyEnforcementProcessor;
import com.qut.middleware.spep.pep.SessionGroupCache;

/** Interface for filters and other implementations of the SPEP to use this library.*/
public interface SPEP
{
	public enum defaultAction {Permit, Deny};
	
	/**
	 * @return The attribute processor.
	 */
	public AttributeProcessor getAttributeProcessor();
	
	/**
	 * @return The authentication processor.
	 */
	public AuthnProcessor getAuthnProcessor();
	
	/**
	 * @return The metadata instance.
	 */
	public Metadata getMetadata();
	
	/**
	 * @return The policy enforcement processor.
	 */
	public PolicyEnforcementProcessor getPolicyEnforcementProcessor();
	
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
	
	/**
	 * @return The session group cache.
	 */
	public SessionGroupCache getSessionGroupCache();
	
	/**
	 * @return Boolean true if SPEP has started successfully.
	 */
	public boolean isStarted();
	
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
	 * @return Determines the site wide ESOE session token, has no purpose except to indicate ESOE has quite probably established a session for this user already
	 */
	public String getEsoeGlobalTokenName();
	
	public defaultAction getLazyInitDefaultAction();

}
