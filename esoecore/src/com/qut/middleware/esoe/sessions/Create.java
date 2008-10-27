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
 * Author: Shaun Mangelsdorf / Bradley Beddoes
 * Creation Date: 28/09/2006 / 05/03/2007
 * 
 * Purpose: Interface to allow sessions to be created in the underlying data store.
 */
package com.qut.middleware.esoe.sessions;

import java.util.List;

import com.qut.middleware.esoe.authn.bean.AuthnIdentityAttribute;
import com.qut.middleware.esoe.sessions.exception.DuplicateSessionException;
import com.qut.middleware.esoe.sessions.exception.SessionCacheUpdateException;
import com.qut.middleware.saml2.schemas.assertion.AttributeType;

/**
 * Interface to allow sessions to be created in the local cache.
 * */
public interface Create
{
	
	/**
	 * This function creates a session and caches the data for the session with a unique session identifier and the user
	 * identification string they used to authenticate to the system. This should be the same value which is used to
	 * resolve all their identity details from backend identity stores.
	 * 
	 * @param sessionID
	 *            The session identifier.
	 * @param principalAuthnIdentifier
	 *            The name token used to identify the principal when they authenticated. e.g. their username.
	 * @param authenticationContextClass
	 *            An authentication context class value as specified in
	 *            {@link com.qut.middleware.saml2.AuthenticationContextConstants}
	 * @param authnIdentityAttributes
	 *            The attributes derived from the authentication process which should be added to the session.
	 * @throws SessionCacheUpdateException
	 *             if there is an error communicating with the session datasource or the session cannot be created.
	 */
	public void createLocalSession(String sessionID, String principalAuthnIdentifier, String authenticationContextClass, List<AuthnIdentityAttribute> principalIdentifiers) throws SessionCacheUpdateException;

	/**
	 * Allows the external authentication sources whom we have trusted to create a session from whatever protocol they
	 * natively understand and have it inserted into the ESOE for use on SPEP protected applications.
	 * 
	 * @param sessionID
	 *            The session identifier.
	 * @param principalAuthnIdentifier
	 *            The name token used to identify the principal when they authenticated. e.g. their username, but could
	 *            be UNKNOWN if no login name was released
	 * @param authenticationContextClass
	 *            An authentication context class value as specified in
	 *            {@link com.qut.middleware.saml2.AuthenticationContextConstants}
	 * @param principalAttributes
	 *            List of attributes supplied by the principal at remote delegator for use in their authn session. These
	 *            must be keyed to the same values as Friendly name for internally sourced Attributes
	 * @throws SessionCacheUpdateException
	 *             if there is an error communicating with the session datasource or the session cannot be created.
	 */
	public void createDelegatedSession(String sessionID, String principalAuthnIdentifier, String authenticationContextClass, List<AttributeType> principalAttributes) throws SessionCacheUpdateException;
}
