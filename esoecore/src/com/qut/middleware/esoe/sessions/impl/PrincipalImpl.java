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

import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.datatype.XMLGregorianCalendar;

import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.bean.IdentityAttribute;
import com.qut.middleware.esoe.sessions.bean.IdentityData;
import com.qut.middleware.esoe.sessions.bean.impl.IdentityDataImpl;
import com.qut.middleware.esoe.sessions.exception.InvalidDescriptorIdentifierException;
import com.qut.middleware.esoe.util.CalendarUtils;

/** */
public class PrincipalImpl implements Principal
{
	private String sessionID;
	private List<String> entities;
	private Map<String, List<String>> entitySessionIdentifiers;
	private IdentityData identityData;
	private String principalAuthnIdentifier;
	private String samlAuthnIdentifier;
	private String authenticationContextClass;
	private long lastAccessed;
	private long authnTimestamp;
	private XMLGregorianCalendar sessionNotOnOrAfter;
	// Required for shared object distribution. ANY methods called from a shared object MUST be locked.
	private ReentrantReadWriteLock sharedLock;
		
	/**
	 * Constructor.
	 * 
	 * @param identityData
	 *            The identity data for this principal.
	 * @param sessionLength
	 * 			  The time in seconds that principal sessions are active on remote SPEP for.
	 */
	public PrincipalImpl(IdentityData identityData, int sessionLength)
	{
		this.entities = new Vector<String>(0, 1);
		this.entitySessionIdentifiers = new ConcurrentHashMap<String, List<String>>();
		this.identityData = identityData;
		this.lastAccessed = System.currentTimeMillis();
		this.sessionNotOnOrAfter = CalendarUtils.generateXMLCalendar(sessionLength);
		this.sharedLock = new ReentrantReadWriteLock();
	}
	
	/**
	 * Constructor.
	 * 
	 * @param sessionLength
	 * 			  The time in seconds that principal sessions are active on remote SPEP for.
	 */
	public PrincipalImpl(int sessionLength)
	{
		this.entities = new Vector<String>(0, 1);
		this.entitySessionIdentifiers = new ConcurrentHashMap<String, List<String>>();
		this.lastAccessed = System.currentTimeMillis();
		this.sessionNotOnOrAfter = CalendarUtils.generateXMLCalendar(sessionLength);
		this.identityData = new IdentityDataImpl();
		this.sharedLock = new ReentrantReadWriteLock();
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.Principal#addActiveEntity(java.lang.String)
	 */
	public void addActiveDescriptor(String entityID)
	{
		this.entities.add(entityID);
		this.entitySessionIdentifiers.put(entityID, new Vector<String>(0, 1));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.Principal#addEntitySessionIdentifier(java.lang.String, java.lang.String)
	 */
	public void addDescriptorSessionIdentifier(String descriptorID, String descriptorSessionID)
			throws InvalidDescriptorIdentifierException
	{
		List<String> entitySessions = getDescriptorSessionIdentifiers(descriptorID);
		if (entitySessions == null)
		{
			throw new InvalidDescriptorIdentifierException();
		}

		entitySessions.add(descriptorSessionID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.Principal#getActiveEntities()
	 */
	public List<String> getActiveDescriptors()
	{
		return this.entities;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.Principal#getAttributes()
	 */
	public Map<String, IdentityAttribute> getAttributes()
	{
		return this.identityData.getAttributes();
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sessions.Principal#getAuthenticationContextClass()
	 */
	public String getAuthenticationContextClass()
	{
		return this.authenticationContextClass;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sessions.Principal#getAuthnTimestamp()
	 */
	public long getAuthnTimestamp()
	{
		return this.authnTimestamp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.Principal#getEntitySessionIdentifiers(java.lang.String)
	 */
	public List<String> getDescriptorSessionIdentifiers(String descriptorID) throws InvalidDescriptorIdentifierException
	{
		List<String> entitySessions = this.entitySessionIdentifiers.get(descriptorID);
		if (entitySessions == null)
		{
			throw new InvalidDescriptorIdentifierException();
		}

		return entitySessions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.Principal#getPrincipal()
	 */
	public String getPrincipalAuthnIdentifier()
	{
		this.sharedLock.readLock().lock();
		
		try
		{
			return this.principalAuthnIdentifier;
		}
		finally
		{
			this.sharedLock.readLock().unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.Principal#getSAMLAuthnIdentifier()
	 */
	public String getSAMLAuthnIdentifier()
	{
		return this.samlAuthnIdentifier;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.Principal#getSessionID()
	 */
	public String getSessionID()
	{
		return this.sessionID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.Principal#putAttribute(java.lang.String,
	 *      com.qut.middleware.esoe.sessions.bean.IdentityAttribute)
	 */
	public void putAttribute(String key, IdentityAttribute value)
	{
		this.identityData.getAttributes().put(key, value);
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sessions.Principal#setAuthenticationContextClass(java.lang.String)
	 */
	public void setAuthenticationContextClass(String authenticationContextClass)
	{
		this.authenticationContextClass = authenticationContextClass;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sessions.Principal#setAuthnTimestamp(long)
	 */
	public void setAuthnTimestamp(long authnTimestamp)
	{
		this.authnTimestamp = authnTimestamp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.Principal#setPrincipal(java.lang.String)
	 */
	public void setPrincipalAuthnIdentifier(String principalAuthnIdentifier)
	{
		this.principalAuthnIdentifier = principalAuthnIdentifier;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.Principal#setSAMLAuthnIdentifier(java.lang.String)
	 */
	public void setSAMLAuthnIdentifier(String samlAuthnIdentifier)
	{
		this.samlAuthnIdentifier = samlAuthnIdentifier;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.Principal#setSessionID(java.lang.String)
	 */
	public void setSessionID(String sessionID)
	{
		this.sessionID = sessionID;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sessions.Principal#getLastAccessed()
	 */
	public long getLastAccessed()
	{
		this.sharedLock.readLock().lock();
		
		try
		{
			return this.lastAccessed;
		}
		finally
		{
			this.sharedLock.readLock().unlock();
		}
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sessions.Principal#setLastAccessed(long)
	 */
	public void setLastAccessed(long lastAccessedTimestamp)
	{
		this.sharedLock.writeLock().lock();
		
		try
		{
			this.lastAccessed = lastAccessedTimestamp;
		}
		finally
		{
			this.sharedLock.writeLock().unlock();
		}
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sessions.Principal#getSessionNotOnOrAfter()
	 */
	public XMLGregorianCalendar getSessionNotOnOrAfter()
	{
		return this.sessionNotOnOrAfter;
	}
	
	
	
}
