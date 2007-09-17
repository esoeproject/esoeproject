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
 * Creation Date: 02/10/2006
 * 
 * Purpose: Implements the session cache interface using a hash map as the
 * underlying data structure.
 */
package com.qut.middleware.esoe.sessions.cache.impl;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.qut.middleware.esoe.sessions.Messages;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.cache.SessionCache;
import com.qut.middleware.esoe.sessions.exception.DuplicateSessionException;

/** 
 * Implements the session cache interface using a linked hash map as the
 * underlying data structure. 
 */
public class SessionCacheImpl implements SessionCache
{
	private Map<String, Principal> sessionMap;

	/* Local logging instance */
	private Logger logger = Logger.getLogger(SessionCacheImpl.class.getName());
	
	/**
	 * Default constructor.
	 */
	public SessionCacheImpl()
	{
		this.sessionMap = Collections.synchronizedMap(new LinkedHashMap<String, Principal>());
		
		this.logger.info(Messages.getString("SessionCacheImpl.3")); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.cache.SessionCache#addSession(com.qut.middleware.esoe.sessions.Principal)
	 */
	public void addSession(Principal data) throws DuplicateSessionException
	{
		// Make sure session ID and principal name are set correctly.
		String sessionID = data.getSessionID();
		String principalName = data.getPrincipalAuthnIdentifier();
		if (sessionID == null || sessionID.length() == 0)
		{
			this.logger.error(Messages.getString("SessionCacheImpl.0")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("SessionCacheImpl.0")); //$NON-NLS-1$
		}
		if (principalName == null || principalName.length() == 0)
		{
			this.logger.error(Messages.getString("SessionCacheImpl.1")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("SessionCacheImpl.1")); //$NON-NLS-1$
		}

		this.logger.debug(MessageFormat.format(Messages.getString("SessionCacheImpl.6"), data.getPrincipalAuthnIdentifier(), data.getSessionID())); //$NON-NLS-1$

		// Make sure we don't already have a principal with that session ID.
		Principal principal = getSession(sessionID);
		if (principal != null)
		{
			this.logger.error(Messages.getString("SessionCacheImpl.7")); //$NON-NLS-1$
			throw new DuplicateSessionException();
		}

		synchronized(this.sessionMap)
		{
			this.sessionMap.put(sessionID, data);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.cache.SessionCache#updateSessionSAMLID(com.qut.middleware.esoe.sessions.Principal)
	 */
	public void updateSessionSAMLID(Principal data) throws DuplicateSessionException
	{
		// Make sure the SAML ID is set correctly.
		String samlID = data.getSAMLAuthnIdentifier();
		if (samlID == null || samlID.length() == 0)
		{
			this.logger.error(Messages.getString("SessionCacheImpl.2")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("SessionCacheImpl.2")); //$NON-NLS-1$
		}

		// Make sure we don't already have a principal with that SAML ID
		Principal principal = getSessionBySAMLID(samlID);
		if (principal != null)
		{
			this.logger.error(Messages.getString("SessionCacheImpl.9")); //$NON-NLS-1$
			throw new DuplicateSessionException();
		}

		this.logger.debug(MessageFormat.format(Messages.getString("SessionCacheImpl.10"), data.getPrincipalAuthnIdentifier(), data.getSAMLAuthnIdentifier())); //$NON-NLS-1$
		
		data.setLastAccessed(System.currentTimeMillis());
		
		synchronized(this.sessionMap)
		{
			this.sessionMap.put(samlID, data);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.cache.SessionCache#getSession(java.lang.String)
	 */
	public Principal getSession(String sessionID)
	{
		Principal princ = this.sessionMap.get(sessionID);
		
		if(null != princ)
			princ.setLastAccessed(System.currentTimeMillis());
		
		return princ;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.cache.SessionCache#getSessionBySamlID(java.lang.String)
	 */
	public Principal getSessionBySAMLID(String samlID)
	{
		Principal princ = this.sessionMap.get(samlID);
		
		if(null != princ)
			princ.setLastAccessed(System.currentTimeMillis());
		
		return princ;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.cache.SessionCache#removeSession(java.lang.String)
	 */
	public Principal removeSession(String sessionID)
	{
		this.logger.debug(MessageFormat.format(Messages.getString("SessionCacheImpl.11"), sessionID)); //$NON-NLS-1$
		Principal p = null;
		
		synchronized(this.sessionMap)
		{
			p = this.sessionMap.remove(sessionID);
			if (p != null)
				this.sessionMap.remove(p.getSAMLAuthnIdentifier());
		}
		
		return p;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sessions.cache.SessionCache#cleanCache(int)
	 */
	public int cleanCache(int age) 
	{
		Set<Entry<String, Principal>> entryList = this.sessionMap.entrySet();
		Iterator<Entry<String, Principal>> entryIterator = entryList.iterator();
		int numRemoved = 0;
		
		synchronized (this.sessionMap)
		{
			while (entryIterator.hasNext())
			{
				Entry<String, Principal> entry = entryIterator.next();

				if(null != entry)
				{
					long now = System.currentTimeMillis();
					long expire = (entry.getValue().getLastAccessed() + age);

					this.logger.trace(MessageFormat.format("Comparing entry expiry time of {0} against current time of {1}.", expire, now) ); //$NON-NLS-1$
					
					if (expire < now)
					{
						this.logger.debug("Removing expired entry from session cache"); //$NON-NLS-1$
						entryIterator.remove();
						numRemoved ++;
					}
				}
			}
		}	
		
		return numRemoved;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sessions.cache.SessionCache#validSession(java.lang.String)
	 */
	public boolean validSession(String sessionID)
	{
		return this.sessionMap.containsKey(sessionID);
	}	
}
