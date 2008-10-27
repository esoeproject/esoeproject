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
 * Creation Date: 06/10/2006
 * 
 * Purpose: Implements the Principal interface.
 */

package com.qut.middleware.esoe.sessions.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.bean.IdentityAttribute;

public class PrincipalImpl implements Principal
{
	private static final long serialVersionUID = 6552811894747586130L;
	
	private List<String> activeEntities;
	private Map<String, List<String>> entitySessionIndexMap;
	private Map<String, IdentityAttribute> attributeMap;
	private String authenticationContextClass;
	private long authenticationTimestamp;
	private long lastAccessed;
	private String principalAuthnIdentifier;
	private long sessionNotOnOrAfter;
	private String sessionID;
	private String samlAuthnIdentifier;
	
	public PrincipalImpl()
	{
		this.activeEntities = new ArrayList<String>();
		this.entitySessionIndexMap = new ConcurrentHashMap<String, List<String>>();
		this.attributeMap = new ConcurrentHashMap<String, IdentityAttribute>();
		
		this.authenticationContextClass = null;
		this.authenticationTimestamp = 0;
		this.lastAccessed = 0;
		this.principalAuthnIdentifier = null;
		this.sessionNotOnOrAfter = 0;
		this.sessionID = null;
		this.samlAuthnIdentifier = null;
	}

	public void addEntitySessionIndex(String entityID, String sessionIndex)
	{
		List<String> sessionIndexList;
		
		// Get the existing session indices for the given entity ID, ...
		if (this.entitySessionIndexMap.containsKey(entityID))
		{
			sessionIndexList = this.entitySessionIndexMap.get(entityID);
		}
		// ... or a new empty list there is no entry.
		else
		{
			sessionIndexList = new ArrayList<String>();
		}
		
		// Add the session index and update the map.
		sessionIndexList.add(sessionIndex);
		this.entitySessionIndexMap.put(entityID, sessionIndexList);
		
		// also add the active entity to list if it doen't exist
		if(!this.activeEntities.contains(entityID))
			this.activeEntities.add(entityID);
	}

	public List<String> getActiveEntityList()
	{
		// We're returning by reference, so make it read-only
		return Collections.unmodifiableList(this.activeEntities);
	}

	public List<String> getActiveEntitySessionIndices(String entityID)
	{
		List<String> sessionIndexList = this.entitySessionIndexMap.get(entityID);
		if (sessionIndexList == null) return null;
		
		// We're returning by reference, so make it read-only
		return Collections.unmodifiableList(sessionIndexList);
	}

	public Map<String, IdentityAttribute> getAttributes()
	{
		// Returning by reference, so read-only.
		return Collections.unmodifiableMap(this.attributeMap);
	}

	public String getAuthenticationContextClass()
	{
		return this.authenticationContextClass;
	}
	
	public void setAuthenticationContextClass(String authenticationContextClass)
	{
		this.authenticationContextClass = authenticationContextClass;
	}

	public long getAuthnTimestamp()
	{
		return this.authenticationTimestamp;
	}
	
	public void setAuthnTimestamp(long authenticationTimestamp)
	{
		this.authenticationTimestamp = authenticationTimestamp;
	}

	public long getLastAccessed()
	{
		return this.lastAccessed;
	}

	public void setLastAccessed(long timeMillis)
	{
		this.lastAccessed = timeMillis;
	}

	public String getPrincipalAuthnIdentifier()
	{
		return this.principalAuthnIdentifier;
	}
	
	public void setPrincipalAuthnIdentifier(String principalAuthnIdentifier)
	{
		this.principalAuthnIdentifier = principalAuthnIdentifier;
	}

	public String getSessionID()
	{
		return this.sessionID;
	}
	
	public void setSessionID(String sessionID)
	{
		this.sessionID = sessionID;
	}

	public long getSessionNotOnOrAfter()
	{
		return this.sessionNotOnOrAfter;
	}
	
	public void setSessionNotOnOrAfter(long sessionNotOnOrAfter)
	{
		this.sessionNotOnOrAfter = sessionNotOnOrAfter;
	}

	public String getSAMLAuthnIdentifier()
	{
		return this.samlAuthnIdentifier;
	}
	
	public void setSAMLAuthnIdentifier(String samlAuthnIdentifier)
	{
		this.samlAuthnIdentifier = samlAuthnIdentifier;
	}

	public void putAttribute(String key, IdentityAttribute value)
	{
		this.attributeMap.put(key, value);
	}

}
