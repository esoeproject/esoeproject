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

import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.metadata.processor.impl.MetadataUpdateThread;
import com.qut.middleware.spep.attribute.AttributeProcessor;
import com.qut.middleware.spep.authn.AuthnProcessor;
import com.qut.middleware.spep.impl.IdentifierCacheMonitor;
import com.qut.middleware.spep.pep.PolicyEnforcementProcessor;
import com.qut.middleware.spep.pep.SessionGroupCache;
import com.qut.middleware.spep.sessions.SessionCache;

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
	public MetadataProcessor getMetadataProcessor();
	
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
	 * @return The entity identifier for the trusted ESOE that this SPEP talks to.
	 */
	public String getTrustedESOEIdentifier();
	
	/**
	 * @return The entity identifier of this SPEP.
	 */
	public String getSPEPIdentifier();
	
	/**
	 * @return The session group cache.
	 */
	public SessionGroupCache getSessionGroupCache();
	
	/**
	 * @return The session cache
	 */
	public SessionCache getSessionCache();
	
	/**
	 * @return The identifier cache monitor
	 */
	public IdentifierCacheMonitor getIdentifierCacheMonitor();
	
	/**
	 * @return The metadata update thread.
	 */
	public MetadataUpdateThread getMetadataUpdateThread();
	
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
	
	/**
	 * @return The default action for lazy session init.
	 */
	public defaultAction getLazyInitDefaultAction();
	
	/**
	 * @return Boolean value indicating whether attribute queries are disabled for this SPEP.
	 */
	public boolean disableAttributeQuery();
	
	/**
	 * @return Boolean value indicating whether policy enforcement is disabled for this SPEP.
	 */
	public boolean disablePolicyEnforcement();
	
	/**
	 * @return Boolean value indicating whether the SPEP startup protocol is disabled for this SPEP.
	 */
	public boolean disableSPEPStartup();

	/**
	 * @return Boolean value indicating whether the SPEP will attempt to be compatible with a non-ESOE identity provider.
	 */
	public boolean enableCompatibility();
}
