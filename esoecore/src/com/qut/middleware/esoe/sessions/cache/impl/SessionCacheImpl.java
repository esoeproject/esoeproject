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
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.esoe.authn.bean.AuthnIdentityAttribute;
import com.qut.middleware.esoe.logout.LogoutThreadPool;
import com.qut.middleware.esoe.sessions.Messages;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.cache.SessionCache;
import com.qut.middleware.esoe.sessions.exception.DuplicateSessionException;
import com.qut.middleware.esoe.sessions.exception.InvalidDescriptorIdentifierException;
import com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException;
import com.qut.middleware.esoe.util.CalendarUtils;

/** 
 * Implements the session cache interface using a linked hash map as the
 * underlying data structure. 
 */
public class SessionCacheImpl implements SessionCache
{
	private ConcurrentMap<String, Principal> sessionMap;
	private ReentrantLock lock;	
	private LogoutThreadPool logoutPool;
	
	private volatile long lastCleaned;
	
	/* Local logging instance */
	Logger logger = LoggerFactory.getLogger(SessionCacheImpl.class.getName());
	
	/** Constructor for multi threaded logout model.
	 * 
	 * @param logoutMechanism The logout mechanism to use when logging expired user sessions out of SPEPs.
	 * @param minThreads Minimum number of threads to maintain for logout calls. 
	 * @param maxThreads Maximum number of threads to maintain for logout calls.
	 */
	public SessionCacheImpl(LogoutThreadPool logoutThreadPool)
	{		
		if(logoutThreadPool == null)
			throw new IllegalArgumentException("Param logoutThreadpool MUST NOT be null.");
		
		this.logoutPool = logoutThreadPool;
		
		this.lock = new ReentrantLock();
		this.lastCleaned = System.currentTimeMillis();
		this.sessionMap = new ConcurrentHashMap<String, Principal>();
		
		this.logger.info("Successfully created Session Cache using a threaded logout pool." ); 
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

		this.sessionMap.put(sessionID, data);
		
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
		
		this.sessionMap.put(samlID, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.cache.SessionCache#getSession(java.lang.String)
	 */
	public Principal getSession(String sessionID)
	{
		if(sessionID == null)
			return null;
		
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
		if(samlID == null)
			return null;
		
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
	public boolean removeSession(String sessionID)
	{
		if(sessionID == null)
			return false;
				
		this.logger.debug(MessageFormat.format(Messages.getString("SessionCacheImpl.11"), sessionID)); //$NON-NLS-1$
				
		Principal p = this.sessionMap.remove(sessionID);
		
		if (p != null && p.getSAMLAuthnIdentifier() != null)
			this.sessionMap.remove(p.getSAMLAuthnIdentifier());
	
		return (p != null);
	}


	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sessions.cache.SessionCache#validSession(java.lang.String)
	 */
	public boolean validSession(String sessionID)
	{
		return this.sessionMap.containsKey(sessionID);
	}

	
	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sessions.cache.SessionCache#lastCleaned()
	 */
	public synchronized long getLastCleaned()
	{
			return this.lastCleaned;
	}

	
	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sessions.cache.SessionCache#setLastCleaned()
	 */
	public synchronized void setLastCleaned(long lastCleaned)
	{
			this.lastCleaned = lastCleaned;
	}	
	
	
	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sessions.cache.SessionCache#cleanCache(int)
	 * 
	 * Clean up algorithm is as follows:
	 * 
	 * 	For each entry in the sessions cache
	 * 		if the session ID is not a principal ID
	 * 			check to see if it's expired
	 * 			if expired
	 * 				remove from cache (both associated cache entries) 							
	 * 			else if idle time exceeds set limit
	 * 				if no active SPEP sessions
	 * 					remove from cache (both associated cache entries) 							
	 * 				else
	 * 					logout any active SPEP sessions (active session list is cleared for that principal object)
	 */
	public int cleanCache(int age) 
	{					
		this.logger.trace("Clean cache called ...");
				
		// we only want one thread to call clean at any one time
		if(this.lock.tryLock())
		{
			int idleRemoved = 0;
			int expiredRemoved = 0;
			int logouts = 0;
			
			try
			{			
				// we'll set at start and end to stop any other processes cleaning it at random intervals.
				this.setLastCleaned(System.currentTimeMillis());
				
				this.logger.debug(MessageFormat.format("Starting cache clean. Current Map size is {0}." , this.sessionMap.size()) );
				
				Set<Entry<String, Principal>> entryList = this.sessionMap.entrySet();
				Iterator<Entry<String, Principal>> entryIterator = entryList.iterator();
				
				XMLGregorianCalendar thisXmlCal = CalendarUtils.generateXMLCalendar();
				GregorianCalendar thisCal = thisXmlCal.toGregorianCalendar();
				
				int numIterations = 0;
				while (entryIterator.hasNext())
				{
					numIterations ++;
					
					Entry<String, Principal> entry = entryIterator.next();
					if(entry != null)
					{						
						long now = System.currentTimeMillis();
						long idle = (now - entry.getValue().getLastAccessed());
						
						XMLGregorianCalendar xmlCalendar = entry.getValue().getSessionNotOnOrAfter();
						GregorianCalendar notOnOrAfterCal = xmlCalendar.toGregorianCalendar();
		
						boolean logoutSuccess = false;
					
						String principalSessionID = entry.getValue().getSAMLAuthnIdentifier();
						
						// The removeSession() method expects the SAML session ID for removals, rather than the SAML authn ID,
						// so if the entry currently being processed is a SAML authn ID, skip it. That way, we can call removeSession()
						// with the expected session ID (confused yet?)
						if(entry.getKey().equals(principalSessionID))
							continue;
						
						this.logger.trace(MessageFormat.format("Processing session {0} with principal ID {1}", entry.getKey(), principalSessionID) );
						
						// Remove any sessions that have been idle too long
						this.logger.trace(MessageFormat.format("Comparing Session notOnOrAfter time of {0} against current time of {1}.",  notOnOrAfterCal.getTime(), thisCal.getTime()) ); //$NON-NLS-1$
					
						if (thisCal.after(notOnOrAfterCal))
						{			
							this.logger.debug(MessageFormat.format("Session ID {0} has passed the maximum valid time. ", entry.getKey()) ); //$NON-NLS-1$
							
							// Remove both associated sessions and update tally
							this.removeSession(entry.getKey());
							expiredRemoved ++;
						}
						else
						{
							// Remove any sessions that have been idle too long
							this.logger.trace(MessageFormat.format("Comparing session idle time of {0} against max idle time of {1}.", idle, age) ); //$NON-NLS-1$
							
							if (idle > age)
							{								
								this.logger.debug(MessageFormat.format("Idle time exceeded for session ID {0}. ", entry.getKey()) ); //$NON-NLS-1$
						
								// The session may have been previously logged out of any active sessions, but not removed from the
								// cache. If this is the case and still no active sessions, remove it.
								if(entry.getValue().getActiveDescriptors().size() == 0)
								{
									this.logger.debug(MessageFormat.format("Idle Session {0} has no active descriptors. Removing from cache. ", entry.getKey()) );
									
									// remove associated sessions from cache
									this.removeSession(entry.getKey());
									idleRemoved ++;
								}
								else
								{
									this.logger.debug(MessageFormat.format("Logging idle session {0} out of active descriptors.", entry.getKey()) );
								
									// don't store any logout states , we don't care.
									logoutSuccess = (this.logoutPool.createLogoutTask(entry.getValue(), false) != null);
									
									if(logoutSuccess)
									{
										this.logger.debug("Successfully added LogoutTask to thread pool.");
										logouts ++;
									}
									else
										this.logger.warn("Failed to add LogoutTask to thread pool. Consider increasing thread limit to increase throughput.");
								}
							}
						}
					}					
				}
			
				this.logger.debug(MessageFormat.format("Cleanup process did {0} iterations over cache Map.", numIterations) );
				
				
				long duration = System.currentTimeMillis() - thisCal.getTimeInMillis();
		
				this.logger.info(MessageFormat.format("Completed cache cleanup in {0} milliseconds. {1} Idle, {2} Expired sessions removed. {3} sessions logged out. Current Map size is {4}.", duration ,  idleRemoved, expiredRemoved, logouts,  this.sessionMap.size()) );
			
				this.setLastCleaned(System.currentTimeMillis());

				return (expiredRemoved + idleRemoved);
			}
			finally
			{
				this.lock.unlock();
			}		
		}
		else
		{
			this.logger.debug("Lock is already held by another thread. Ignoring cleanup call.");
			return 0 ;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.sessions.cache.SessionCache#getSize()
	 */
	public int getSize()
	{
		return (this.sessionMap.size()/2);
	}
	
	public void addDescriptor(Principal principal, String entityID) throws InvalidSessionIdentifierException
	{
		// noop, not required for this implementation
	}
	
	public void addDescriptorSessionIdentifier(Principal principal, String entityID, String descriptorSessionID) throws InvalidSessionIdentifierException, InvalidDescriptorIdentifierException
	{
		// noop, not required for this implementation
	}
	
	public void updatePrincipalAttributes(Principal principal, List<AuthnIdentityAttribute> authnIdentityAttributes) throws InvalidSessionIdentifierException
	{
		// noop, not required for this implementation
	}
}
