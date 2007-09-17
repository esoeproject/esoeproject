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
 * Purpose: Interface for the session group cache.
 */
package com.qut.middleware.spep.pep;

import java.util.List;
import java.util.Map;

import com.qut.middleware.spep.pep.PolicyEnforcementProcessor.decision;
import com.qut.middleware.spep.sessions.PrincipalSession;

/** Interface for the session group cache. This is used to cache GroupTarget and AuthzTarget data
 * so that authorization decisions for resource access can be made locally.
 * */
public interface SessionGroupCache
{
	/**
	 * Makes an authorization decision from cached data.
	 * @param principalSession The authenticated client session
	 * @param resource The resource being requested
	 * @return The authz decision, or decision.cache if not enough data cached
	 */
	public decision makeCachedAuthzDecision(PrincipalSession principalSession, String resource);
	
	/**
	 * Makes an authorization decision from cached data when an action is specified.
	 * @param principalSession The authenticated client session
	 * @param resource The resource being requested
	 * @param action The action being undertaken by the user
	 * @return The authz decision, or decision.cache if not enough data cached
	 */
	public decision makeCachedAuthzDecision(PrincipalSession principalSession, String resource, String action);
	
	/**
	 * Processes a list of resources to form cached data.
	 * @param principalSession The client session
	 * @param groupTarget The group target
	 * @param authzTargets The resources
	 * @param decision The decision to cache for these resources
	 */
	public void updateCache(PrincipalSession principalSession, String groupTarget, List<String> authzTargets, String action, decision decision);
	
	/**
	 * Clears the Authz cache
	 * @param groupTargetMap The new list of group targets to cache against.
	 */
	public void clearCache(Map<String,List<String>> groupTargetMap);
	
	/**
	 * Clears an individual cache belonging to a Principal
	 * @param principal The PrincipalSession to remove
	 */
	public void clearPrincipalSession(PrincipalSession principal);
}
