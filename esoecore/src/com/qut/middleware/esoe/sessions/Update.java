/* Copyright 2006, Queensland University of Technology
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
 * Creation Date: 28/09/2006
 * 
 * Purpose: Interface to allow session data to be updated in the underlying data store.
 */
package com.qut.middleware.esoe.sessions;

import java.util.List;

import com.qut.middleware.esoe.authn.bean.AuthnIdentityAttribute;
import com.qut.middleware.esoe.sessions.exception.SessionCacheUpdateException;

/** */
public interface Update
{
	/**
	 * Adds a entity ID / session index pair to a principal session. The principal object passed in will be updated as
	 * well as any underlying data stores and/or caches.
	 * 
	 * @param principal
	 *            The principal object.
	 * @param entityID
	 *            The entity identifier
	 * @param sessionIndex
	 *            The session index to store
	 */
	public void addEntitySessionIndex(Principal principal, String entityID, String sessionIndex) throws SessionCacheUpdateException;

	/**
	 * This method allows authentication handlers to have future interaction with a principals identity information to
	 * add extra details generally when the principals authentication state changes in some way, for example higher
	 * levels of credentials proven.
	 * 
	 * Adds the given list of attribtues to the principal session. The principal object passed in will be updated as
	 * well as any underlying data stores and/or caches.
	 * 
	 * @param sessionID
	 *            The principal's session identifier.
	 * @param authnIdentityAttributes
	 *            Identity attributes that should be added to the principals session
	 */
	public void addPrincipalAttributes(Principal principal, List<AuthnIdentityAttribute> authnIdentityAttributes) throws SessionCacheUpdateException;
}
