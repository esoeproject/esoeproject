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
 * Purpose: Interface to allow session data to be updated in the local cache.
 */
package com.qut.middleware.esoe.sessions;

import java.util.List;

import com.qut.middleware.esoe.authn.bean.AuthnIdentityAttribute;
import com.qut.middleware.esoe.sessions.exception.DuplicateSessionException;
import com.qut.middleware.esoe.sessions.exception.InvalidDescriptorIdentifierException;
import com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException;

/** */
public interface Update
{
	/**
	 * This method takes a session identifier and SAML identifier and stores the value in the associated principal.
	 * 
	 * @param sessionID
	 *             The principal's session identifier.
	 * @param samlID
	 *            The SAML identifier to store
	 * @throws DuplicateSessionException
	 * @throws InvalidSessionIdentifierException
	 */
	public void updateSAMLAuthnIdentifier(String sessionID, String samlID) throws DuplicateSessionException,
			InvalidSessionIdentifierException;

	/**
	 * This method takes a session identifier and descriptor identifier and stores the value in the associated principal.
	 * 
	 * @param sessionID
	 *            The principal's session identifier.
	 * @param descriptorID
	 *            The SPEP identifier to store.
	 * @throws InvalidSessionIdentifierException
	 */
	public void updateDescriptorList(String sessionID, String descriptorID) throws InvalidSessionIdentifierException;

	/**
	 * This method takes a session identifier, descriptor identifier and SAML session index and stores the value in the
	 * associated principal.
	 * 
	 * @param sessionID
	 *             The principal's session identifier.
	 * @param descriptorID
	 *            The descriptor identifier
	 * @param descriptorSessionID
	 *            The session index to store
	 * @throws InvalidSessionIdentifierException
	 * @throws InvalidDescriptorIdentifierException
	 */
	public void updateDescriptorSessionIdentifierList(String sessionID, String descriptorID, String descriptorSessionID)
			throws InvalidSessionIdentifierException, InvalidDescriptorIdentifierException;
	
	/**
	 * This method allows authentication handlers to have future interaction with a principals identity information to add extra details
	 * generally when the principals authentication state changes in some way, for example higher levels of credentials proven.
	 * @param sessionID
	 * 				 The principal's session identifier.
	 * @param authnIdentityAttributes
	 * 				Identity attributes that should be added to the principals session
	 * @throws InvalidSessionIdentifierException
	 */
	public void updatePrincipalAttributes(String sessionID, List<AuthnIdentityAttribute> authnIdentityAttributes) 
			throws InvalidSessionIdentifierException;
}
