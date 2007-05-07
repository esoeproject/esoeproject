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
 * Purpose: Contains information about an authenticated principal.
 */
package com.qut.middleware.esoe.sessions;

import java.util.List;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import com.qut.middleware.esoe.sessions.bean.IdentityAttribute;
import com.qut.middleware.esoe.sessions.exception.InvalidDescriptorIdentifierException;

/** Contains information about an authenticated principal. */

public interface Principal
{
	
	/**
	 * Accessor for SAML authentication context class.
	 * 
	 * @return String indicating SAML AuthnContextClassRef of how principal was authenticated
	 */
	public String getAuthenticationContextClass();
	
	/**
	 * Mutator for SAML authentication context class.
	 * 
	 * @param authenticationContextClass
	 *            Value to set.
	 */
	public void setAuthenticationContextClass(String authenticationContextClass);
	
	/**
	 * Mutator for list of active entities. An active descriptor is an SPEP for which the user has
	 * been authenticated.
	 * 
	 * @param descriptorID Descriptor to add to list.
	 */
	public void addActiveDescriptor(String descriptorID);

	/**
	 * Adds a session identifier to the list of identifiers associated with an SPEP.
	 * 
	 * @param descriptorID
	 *            The descriptor identifier.
	 * @param descriptorSessionID
	 *            The session identifier to be added.
	 * @throws InvalidDescriptorIdentifierException
	 */
	public void addDescriptorSessionIdentifier(String descriptorID, String descriptorSessionID)
			throws InvalidDescriptorIdentifierException;

	/**
	 * Accessor for list of active entities. An active descriptor is an SPEP for which the user has
	 * been authenticated.
	 * 
	 * @return List of Strings - active entities.
	 */
	public List<String> getActiveDescriptors();

	/**
	 * Accessor to attribute map. Attributes of the authenticated principal.
	 * 
	 * @return Map of Strings to IdentityAttibutes.
	 */
	public Map<String, IdentityAttribute> getAttributes();

	/**
	 * Accessor to retrieve list of session identifiers associated with an SPEP.
	 * 
	 * @param descriptorID The descriptor identifier. 
	 * @return The list of associated session identifiers.
	 * @throws InvalidDescriptorIdentifierException If the descriptor is not registered with the ESOE.
	 */
	public List<String> getDescriptorSessionIdentifiers(String descriptorID) throws InvalidDescriptorIdentifierException;

	/**
	 * Accessor to principal name. 
	 * 
	 * @return String principal name
	 */
	public String getPrincipalAuthnIdentifier();

	/**
	 * Accessor to SAML Authentication Identifier.
	 * 
	 * @return String type identifier.
	 */
	public String getSAMLAuthnIdentifier();

	/**
	 * Accessor to session identifier.
	 * 
	 * @return String principal's session identifier.
	 */
	public String getSessionID();
	
	/** Accessor to Authentication Timestamp.
	 * 
	 * @return long authentication timestamp.
	 */
	public long getAuthnTimestamp();
	
	/**
	 * Accessor for Session not on or after value
	 * 
	 * @return XMLGregrianCalendar time that sessions for principal should stop be honoured by SPEP
	 */
	public XMLGregorianCalendar getSessionNotOnOrAfter();

	/**
	 * Mutator for attribute map.
	 * 
	 * @param key attribute name.
	 * @param value attribute value.
	 */
	public void putAttribute(String key, IdentityAttribute value);

	/**
	 * Mutator for principalAuthnIdentifier. 
	 * 
	 * @param principalAuthnIdentifier
	 *            Principal name to set.
	 */
	public void setPrincipalAuthnIdentifier(String principalAuthnIdentifier);

	/**
	 * Mutator for SAML Authentication Identifier.
	 * 
	 * @param samlAuthnIdentifier
	 *            Value to set.
	 */
	public void setSAMLAuthnIdentifier(String samlAuthnIdentifier);

	/**
	 * Mutator for session identifier.
	 * 
	 * @param sessionID session ID to set for pincipal.
	 */
	public void setSessionID(String sessionID);
	
	/**
	 * Mutator for authentication timestamp.
	 * 
	 * @param authnTimestamp timestamp to set.
	 */
	public void setAuthnTimestamp(long authnTimestamp);
	
	
	/**
	 * Accessor for last accessed timestamp.
	 * 
	 * @return lastUpdated timestamp.
	 */
	public long getLastAccessed();
	
	
	/**
	 * Mutator for last accessed timestamp.
	 * 
	 * @param lastAccessedTimestamp timestamp to set.
	 */
	public void setLastAccessed(long lastAccessedTimestamp);
}
