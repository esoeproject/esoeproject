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
 * Author: Shaun Mangelsdorf / Bradley Beddoes
 * Creation Date: 13/11/2006 / 03/03/2007
 * 
 * Purpose: Implements the SessionCache interface.
 */
package com.qut.middleware.spep.sessions.impl;

import java.text.MessageFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.spep.sessions.Messages;
import com.qut.middleware.spep.sessions.PrincipalSession;
import com.qut.middleware.spep.sessions.SessionCache;
import com.qut.middleware.spep.sessions.UnauthenticatedSession;
import com.qut.middleware.spep.util.CalendarUtils;

/** */
public class SessionCacheImpl implements SessionCache
{
	protected Map<String, PrincipalSession> sessions;
	protected Map<String, PrincipalSession> esoeSessions;
	protected Map<String, UnauthenticatedSession> unauthenticatedSessions;
	private CleanupThread cleanupThread;
	protected long sessionCacheTimeout;
	protected long sessionCacheInterval;

	private ReentrantLock lock;

	protected void lock()
	{
		this.lock.lock();
	}

	protected void unlock()
	{
		this.lock.unlock();
	}

	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(SessionCacheImpl.class.getName());

	/**
	 * Default constructor.
	 * 
	 * @param reportingProcessor
	 *            The processor to use for logging.
	 * @param sessionCacheTimeout
	 *            Expiry age in seconds for cached sessions. Sessions older than specified time will be removed. from
	 *            the cache at regular intervals.
	 * @param sessionCacheInterval
	 *            interval in seconds session cache between polls to remove expired entries.
	 */
	public SessionCacheImpl(long sessionCacheTimeout, long sessionCacheInterval)
	{
		if (sessionCacheTimeout > Long.MAX_VALUE / 1000)
		{
			throw new IllegalArgumentException(Messages.getString("SessionCacheImpl.13")); //$NON-NLS-1$
		}
		if (sessionCacheInterval > Long.MAX_VALUE / 1000)
		{
			throw new IllegalArgumentException(Messages.getString("SessionCacheImpl.14")); //$NON-NLS-1$
		}

		this.sessionCacheTimeout = sessionCacheTimeout * 1000;
		this.sessionCacheInterval = sessionCacheInterval * 1000;

		this.sessions = new HashMap<String, PrincipalSession>();
		this.esoeSessions = new HashMap<String, PrincipalSession>();
		this.unauthenticatedSessions = new HashMap<String, UnauthenticatedSession>();

		this.cleanupThread = new CleanupThread();
		this.cleanupThread.start();

		this.lock = new ReentrantLock(true);

		this.logger.info(Messages.getString("SessionCacheImpl.0")); //$NON-NLS-1$
	}
	
	public void cleanup()
	{
		this.cleanupThread.stopRunning();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.spep.sessions.SessionCache#getClientSession(java.lang.String)
	 */
	public PrincipalSession getPrincipalSession(String sessionID)
	{
		PrincipalSession principalSession;

		lock();
		try
		{
			principalSession = this.sessions.get(sessionID);
		}
		finally
		{
			unlock();
		}

		if (principalSession != null)
		{
			XMLGregorianCalendar thisXmlCal = CalendarUtils.generateXMLCalendar();
			GregorianCalendar thisCal = thisXmlCal.toGregorianCalendar();

			if (thisCal.getTime().before(principalSession.getSessionNotOnOrAfter()))
			{
				this.logger.debug("Continuing with cached session " + principalSession.getEsoeSessionID() + " current time is: " + thisCal.getTime() + " sessionNotOnAfter was set to: " + principalSession.getSessionNotOnOrAfter());
				return principalSession;
			}

			this.logger.info("Terminating session " + principalSession.getEsoeSessionID() + " current time is: " + thisCal.getTime() + " sessionNotOnAfter was set to: " + principalSession.getSessionNotOnOrAfter());
			this.logger.debug(MessageFormat.format(Messages.getString("SessionCacheImpl.9"), principalSession.getEsoeSessionID())); //$NON-NLS-1$

			terminatePrincipalSession(principalSession);
		}

		this.logger.debug(MessageFormat.format(Messages.getString("SessionCacheImpl.8"), sessionID)); //$NON-NLS-1$

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.spep.sessions.SessionCache#getClientSessionByEsoeSessionIndex(java.lang.String)
	 */
	public PrincipalSession getPrincipalSessionByEsoeSessionID(String esoeSessionID)
	{
		PrincipalSession principalSession;

		lock();
		try
		{
			principalSession = this.esoeSessions.get(esoeSessionID);
		}
		finally
		{
			unlock();
		}

		if (principalSession != null)
		{
			XMLGregorianCalendar thisXmlCal = CalendarUtils.generateXMLCalendar();
			GregorianCalendar thisCal = thisXmlCal.toGregorianCalendar();

			if (thisCal.getTime().before(principalSession.getSessionNotOnOrAfter()))
			{
				this.logger.info("Continuing with cached session " + principalSession.getEsoeSessionID() + " current time is: " + thisCal.getTime() + " sessionNotOnAfter was set to: " + principalSession.getSessionNotOnOrAfter());
				return principalSession;
			}

			this.logger.info("Terminating session " + principalSession.getEsoeSessionID() + " current time is: " + thisCal.getTime() + " sessionNotOnAfter was set to: " + principalSession.getSessionNotOnOrAfter());
			this.logger.debug(MessageFormat.format(Messages.getString("SessionCacheImpl.9"), principalSession.getEsoeSessionID())); //$NON-NLS-1$

			terminatePrincipalSession(principalSession);
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.spep.sessions.SessionCache#putClientSession(java.lang.String,
	 *      com.qut.middleware.spep.sessions.PrincipalSession)
	 */
	public void putPrincipalSession(String sessionID, PrincipalSession principalSession)
	{
		if (principalSession.getEsoeSessionID() == null)
		{
			this.logger.error(MessageFormat.format(Messages.getString("SessionCacheImpl.1"), sessionID)); //$NON-NLS-1$
			return;
		}

		lock();
		try
		{
			this.sessions.put(sessionID, principalSession);
			this.esoeSessions.put(principalSession.getEsoeSessionID(), principalSession);
		}
		finally
		{
			unlock();
		}

		this.logger.debug(MessageFormat.format(Messages.getString("SessionCacheImpl.2"), sessionID)); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.spep.sessions.SessionCache#terminateClientSession(java.lang.String)
	 */
	public void terminatePrincipalSession(PrincipalSession principalSession)
	{
		lock();
		try
		{
			/* Terminate all SPEP sessionID's that reference this principal */
			for (String sessionID : principalSession.getSessionIDList())
			{
				this.sessions.remove(sessionID);
			}

			this.esoeSessions.remove(principalSession.getEsoeSessionID());
			this.logger.debug(MessageFormat.format(Messages.getString("SessionCacheImpl.3"), principalSession.getEsoeSessionID())); //$NON-NLS-1$		
		}
		finally
		{
			unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.spep.sessions.SessionCache#terminateIndividualPrincipalSession(com.qut.middleware.spep.sessions.PrincipalSession,
	 *      java.lang.String)
	 */
	public void terminateIndividualPrincipalSession(PrincipalSession principalSession, String esoeSessionIndex)
	{
		lock();
		try
		{
			Map<String, String> sessionIndex = principalSession.getEsoeSessionIndex();
			if (sessionIndex != null)
			{
				String localSessionID = sessionIndex.get(esoeSessionIndex);

				if (localSessionID != null)
				{
					/* Remove this session from local sessionID cache */
					this.sessions.remove(localSessionID);
					principalSession.getEsoeSessionIndex().remove(esoeSessionIndex);

					/* If the principal has no further local mappings then terminate their ESOE mapping */
					if (principalSession.getEsoeSessionIndex().size() == 0)
					{
						this.esoeSessions.remove(principalSession.getEsoeSessionID());
					}

					this.logger.debug(MessageFormat.format(Messages.getString("SessionCacheImpl.3"), principalSession.getEsoeSessionID())); //$NON-NLS-1$
				}
			}
		}
		finally
		{
			unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.spep.sessions.SessionCache#getUnauthenticatedSession(java.lang.String)
	 */
	public UnauthenticatedSession getUnauthenticatedSession(String requestID)
	{
		UnauthenticatedSession unauthenticatedSession;
		lock();
		try
		{
			unauthenticatedSession = this.unauthenticatedSessions.get(requestID);
		}
		finally
		{
			unlock();
		}

		if (unauthenticatedSession != null)
			unauthenticatedSession.updateTime();

		return unauthenticatedSession;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.spep.sessions.SessionCache#putUnauthenticatedSession(java.lang.String,
	 *      com.qut.middleware.spep.sessions.UnauthenticatedSession)
	 */
	public void putUnauthenticatedSession(String requestID, UnauthenticatedSession unauthenticatedSession)
	{
		lock();
		try
		{
			this.unauthenticatedSessions.put(requestID, unauthenticatedSession);
		}
		finally
		{
			unlock();
		}

		unauthenticatedSession.updateTime();

		this.logger.debug(MessageFormat.format(Messages.getString("SessionCacheImpl.4"), requestID)); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.spep.sessions.SessionCache#terminateUnauthenticatedSession(java.lang.String)
	 */
	public void terminateUnauthenticatedSession(String requestID)
	{
		lock();
		try
		{
			this.unauthenticatedSessions.remove(requestID);
		}
		finally
		{
			unlock();
		}

		this.logger.debug(MessageFormat.format(Messages.getString("SessionCacheImpl.5"), requestID)); //$NON-NLS-1$
	}

	/**
	 * Cleans up unauthenticated sessions which have timed out dependant on some local setting. Authenticated sessions
	 * are terminated based on the time SessionNotOnOrAfter value set by the ESOE
	 * 
	 */
	private class CleanupThread extends Thread
	{
		/* Local logging instance */
		private Logger logger = LoggerFactory.getLogger(CleanupThread.class.getName());
		private boolean running;

		protected CleanupThread()
		{
			super("SPEP Session Cache cleanup thread"); //$NON-NLS-1$
		}

		@Override
		public void run()
		{
			this.running = true;
			while (this.running)
			{
				try
				{
					Thread.sleep(SessionCacheImpl.this.sessionCacheInterval);

					this.logger.debug(Messages.getString("SessionCacheImpl.10")); //$NON-NLS-1$
					cleanup();
				}
				catch (Exception e)
				{
					this.logger.error(MessageFormat.format(Messages.getString("SessionCacheImpl.11"), e.getMessage())); //$NON-NLS-1$
				}
			}
		}
		
		public void stopRunning()
		{
			this.running = false;
			this.interrupt();
		}

		private void cleanup()
		{
			List<String> expired = new Vector<String>();

			lock();
			try
			{
				this.logger.info(Messages.getString("SessionCacheImpl.15")); //$NON-NLS-1$
				/* Remove principal sessions that have expired */
				for (String esoeID : SessionCacheImpl.this.esoeSessions.keySet())
				{
					PrincipalSession principal = SessionCacheImpl.this.esoeSessions.get(esoeID);
					if (principal != null)
					{
						XMLGregorianCalendar thisXmlCal = CalendarUtils.generateXMLCalendar();
						GregorianCalendar thisCal = thisXmlCal.toGregorianCalendar();

						if (thisCal.getTime().after(principal.getSessionNotOnOrAfter()))
						{
							this.logger.info("Terminating session " +principal.getEsoeSessionID() + " current time is: " + thisCal + " sessionNotOnAfter was set to: " + principal.getSessionNotOnOrAfter());
							this.logger.debug(Messages.getString("SessionCacheImpl.16") + principal.getEsoeSessionID() + Messages.getString("SessionCacheImpl.17")); //$NON-NLS-1$ //$NON-NLS-2$
							terminatePrincipalSession(principal);
						}
					}
				}

				// Now clean up the unauthenticated sessions
				for (Entry<String, UnauthenticatedSession> entry : SessionCacheImpl.this.unauthenticatedSessions.entrySet())
				{
					if (entry.getValue().getIdleTime() > SessionCacheImpl.this.sessionCacheTimeout)
					{
						expired.add(entry.getKey());
					}
				}

				for (String requestID : expired)
				{
					this.logger.debug(Messages.getString("SessionCacheImpl.18") + requestID + Messages.getString("SessionCacheImpl.19")); //$NON-NLS-1$ //$NON-NLS-2$
					terminateUnauthenticatedSession(requestID);
				}
			}
			finally
			{
				unlock();
			}
		}
	}
}
