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
 * Creation Date: 06/10/2006
 * 
 * Purpose: Implements the IdentityData interface, providing a structure for attribute
 * 		resolution.
 */
package com.qut.middleware.esoe.sessions.bean.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.qut.middleware.esoe.sessions.bean.IdentityAttribute;
import com.qut.middleware.esoe.sessions.bean.IdentityData;
import com.qut.middleware.saml2.schemas.esoe.sessions.IdentityType;


/**
 * Implements the IdentityData interface, providing a structure for attribute resolution.
 */
public class IdentityDataImpl implements IdentityData
{
	private Map<String, IdentityAttribute> attributes;
	private List<IdentityType> identity;
	private String principalAuthnIdentifier;
	private String sessionID;
	private String currentHandler;

	/**
	 * Default constructor. Initializes internal attributes map to an empty map.
	 */
	public IdentityDataImpl()
	{
		this.attributes = new ConcurrentHashMap<String, IdentityAttribute>();
	}

	/** Retrieve the identity attributes from the identity data. The map is considered a live 
	 * list. Therefore the addition of attributes to this data object should be achieved via
	 * this.getAttributes().put(attributeName, IdentityAttribute).
	 * 
	 * @return A 0 or more sized Map of attributeName -> IdentityAttribute.
	 * 
	 * 
	 * @see com.qut.middleware.esoe.sessions.bean.IdentityData#getAttributes()
	 */
	public Map<String, IdentityAttribute> getAttributes()
	{
		return this.attributes;
	}

	/** 
	 * @return The list of identities contained herein, else null if none exist.
	 * 
	 * @see com.qut.middleware.esoe.sessions.bean.IdentityData#getIdentity()
	 */
	public List<IdentityType> getIdentity()
	{
		return this.identity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.bean.IdentityData#getPrincipal()
	 */
	public String getPrincipalAuthnIdentifier()
	{
		return this.principalAuthnIdentifier;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.bean.IdentityData#getSessionID()
	 */
	public String getSessionID()
	{
		return this.sessionID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.bean.IdentityData#setIdentity(com.qut.middleware.esoe.xml.sessions.IdentityType)
	 */
	public void setIdentity(List<IdentityType> identity)
	{
		this.identity = identity;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.bean.IdentityData#setPrincipal(java.lang.String)
	 */
	public void setPrincipalAuthnIdentifier(String principalAuthnIdentifier)
	{
		this.principalAuthnIdentifier = principalAuthnIdentifier;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.bean.IdentityData#setSessionID(java.lang.String)
	 */
	public void setSessionID(String sessionID)
	{
		this.sessionID = sessionID;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sessions.bean.IdentityData#getCurrentHandler()
	 */
	public String getCurrentHandler()
	{
		return this.currentHandler;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sessions.bean.IdentityData#setCurrentHandler(java.lang.String)
	 */
	public void setCurrentHandler(String handler)
	{
		this.currentHandler = handler;
	}

}
