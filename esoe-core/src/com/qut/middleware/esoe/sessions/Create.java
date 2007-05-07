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
 * Author: Shaun Mangelsdorf / BRadley Beddoes
 * Creation Date: 28/09/2006 / 05/03/2007
 * 
 * Purpose: Interface to allow sessions to be created in the local cache.
 */
package com.qut.middleware.esoe.sessions;

import java.util.List;

import com.qut.middleware.esoe.authn.bean.AuthnIdentityAttribute;
import com.qut.middleware.esoe.sessions.exception.DataSourceException;
import com.qut.middleware.esoe.sessions.exception.DuplicateSessionException;
import com.qut.middleware.saml2.schemas.assertion.AttributeType;

/** Interface to allow sessions to be created in the local cache.
 * */
public interface Create
{

	/** Results that this interface will return from its publicly facing methods */
	public static enum result
	{
		/**
		 * Return value indicating that the session has been created successfully.
		 */
		SessionCreated
	};

	/**
	 * This function creates a session and caches the data for the session with a unique session identifier and the user
	 * identification string they used to authenticate to the system. This should be the same value which is used to
	 * resolve all their identity details from backend identity stores.
	 * 
	 * @param sessionID
	 *            The session identifier.
	 * @param principalAuthnIdentifier
	 *            The name token used to identify the principal when they authenticated eg 'beddoes' or 'n12345' as usernames
	 * @param authenticationContextClass
	 *            An authentication context class value as specified in com.qut.middleware.saml2.AuthenticationContextConstants.
	 * @param principalIdentifiers
	 * @throws DataSourceException if there is an error communicating with the session datasource.
	 * @throws DuplicateSessionException if the session alredy exists.
	 * @return result.SessionCreated if the session was created successfully.
	 */
	public result createLocalSession(String sessionID, String principalAuthnIdentifier, String authenticationContextClass, List<AuthnIdentityAttribute> principalIdentifiers) throws DataSourceException, DuplicateSessionException;
	
	/**
	 * Allows the external authentication sources whom we have trusted to create a session from whatever protocol they natively understand and have it inserted into the ESOE
	 * for use on SPEP protected applications.
	 * 
	 * @param sessionID
	 *            The session identifier.
	 * @param principalAuthnIdentifier
	 *            The name token used to identify the principal when they authenticated eg 'beddoes' or 'n12345' as usernames, could be UNKNOWN if no login name was released
	 * @param authenticationContextClass
	 *            An authentication context class value as specified in com.qut.middleware.saml2.AuthenticationContextConstants.
	 * @param principalAttributes
	 * 			  List of attributes supplied by the principal at remote delegator for use in their authn session. These must be keyed to the same values as Friendly name for internally sourced Attributes
	 * @throws DataSourceException if there is an error communicating with the session datasource.
	 * @throws DuplicateSessionException if the session alredy exists.
	 * @return result.SessionCreated if the session was created successfully.
	 */
	public result createDelegatedSession(String sessionID, String principalAuthnIdentifier, String authenticationContextClass, List<AttributeType>principalAttributes)throws DataSourceException, DuplicateSessionException;
}
