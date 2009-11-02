/* 
 * Copyright 2008, Queensland University of Technology
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
 * Creation Date: 08/09/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.esoe.sessions.data.impl;

public class SessionData
{
	String sessionID;
	String samlAuthnIdentifier;
	String principalAuthnIdentifier;
	String authenticationClassContext;
	Long authnTimestamp;
	Long sessionNotOnOrAfter;
	Long lastAccessed;
	Long idleGraceExpiry;
	byte[] attributes;

	public String getSessionID()
	{
		return sessionID;
	}

	public void setSessionID(String sessionID)
	{
		this.sessionID = sessionID;
	}

	public String getSamlAuthnIdentifier()
	{
		return samlAuthnIdentifier;
	}

	public void setSamlAuthnIdentifier(String samlAuthnIdentifier)
	{
		this.samlAuthnIdentifier = samlAuthnIdentifier;
	}

	public String getPrincipalAuthnIdentifier()
	{
		return principalAuthnIdentifier;
	}

	public void setPrincipalAuthnIdentifier(String principalAuthnIdentifier)
	{
		this.principalAuthnIdentifier = principalAuthnIdentifier;
	}

	public String getAuthenticationClassContext()
	{
		return authenticationClassContext;
	}

	public void setAuthenticationClassContext(String authenticationClassContext)
	{
		this.authenticationClassContext = authenticationClassContext;
	}

	public Long getAuthnTimestamp()
	{
		return authnTimestamp;
	}

	public void setAuthnTimestamp(Long authnTimestamp)
	{
		this.authnTimestamp = authnTimestamp;
	}

	public Long getSessionNotOnOrAfter()
	{
		return sessionNotOnOrAfter;
	}

	public void setSessionNotOnOrAfter(Long sessionNotOnOrAfter)
	{
		this.sessionNotOnOrAfter = sessionNotOnOrAfter;
	}

	public Long getLastAccessed()
	{
		return lastAccessed;
	}

	public void setLastAccessed(Long lastAccessed)
	{
		this.lastAccessed = lastAccessed;
	}

	public Long getIdleGraceExpiry()
	{
		return idleGraceExpiry;
	}

	public void setIdleGraceExpiry(Long idleGraceExpiry)
	{
		this.idleGraceExpiry = idleGraceExpiry;
	}

	public byte[] getAttributes()
	{
		return attributes;
	}

	public void setAttributes(byte[] attributes)
	{
		this.attributes = attributes;
	}

}
