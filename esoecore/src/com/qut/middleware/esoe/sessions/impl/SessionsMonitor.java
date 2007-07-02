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
 * Author: Andre Zitelli
 * Creation Date: 16/1/2007
 *
 * Purpose: A Thread to monitor session data and clear the SessionCache and IdentifierCache of expired
 * entries at regular intervals.
 */
package com.qut.middleware.esoe.sessions.impl;

import java.text.MessageFormat;
import java.util.Random;

import org.apache.log4j.Logger;

import com.qut.middleware.esoe.MonitorThread;
import com.qut.middleware.esoe.sessions.cache.SessionCache;
import com.qut.middleware.saml2.identifier.IdentifierCache;

/** A Thread to monitor session data and clear the <code>SessionCache</code> and <code>IdentifierCache</code> of expired
 * entries at regular intervals.
 */
public class SessionsMonitor extends Thread implements MonitorThread {

		private int timeout;
		private int interval;
		private IdentifierCache idCache;
		private SessionCache sessionCache;
		
		private volatile boolean running;
		
		/* Local logging instance */
		private Logger logger = Logger.getLogger(this.getClass().getName());

		/**
		 * Constructor
		 * 
		 * @param idCache
		 *            The cache to use for generating identifiers.
		 * @param sessionCache
		 *            The cache to use for user sessions.
		 * @param interval
		 *            The interval at which to purge cache of expired entries, in seconds.
		 * @param timeout
		 *            The time after which to remove cached objects, in seconds.
		 */
		public SessionsMonitor(IdentifierCache idCache, SessionCache sessionCache, int interval, int timeout)
		{
			if (idCache == null)
				throw new IllegalArgumentException(Messages.getString("SessionsMonitor.0"));   //$NON-NLS-1$

			if (sessionCache == null)
				throw new IllegalArgumentException(Messages.getString("SessionsMonitor.1"));  //$NON-NLS-1$
		
			if (interval <= 0 || (interval > Integer.MAX_VALUE / 1000))
				throw new IllegalArgumentException(Messages.getString("SessionsMonitor.2"));   //$NON-NLS-1$

			if (timeout < 0 || (timeout > Integer.MAX_VALUE / 1000))
				throw new IllegalArgumentException(Messages.getString("SessionsMonitor.3"));  //$NON-NLS-1$
		
			this.timeout = timeout * 1000;
			this.interval = interval * 1000;
			this.idCache = idCache;
			this.sessionCache =  sessionCache;
			
			this.setName("Sessions Monitor Thread {" + new Integer(new Random().nextInt()) + "}" ); //$NON-NLS-1$ //$NON-NLS-2$
			
			this.logger.info(MessageFormat.format(Messages.getString("SessionsMonitor.10"), interval, timeout)); //$NON-NLS-1$
		
			this.setDaemon(true);
			
			this.start();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run()
		{
			this.setRunning(true);
			while (this.isRunning())
			{			
				try
				{
					sleep(this.interval);
					
					// clean the identifier cache up
					int numRemoved = this.idCache.cleanCache(this.timeout);

					this.logger.debug(Messages.getString("SessionsMonitor.5") + numRemoved + Messages.getString("SessionsMonitor.6")); //$NON-NLS-1$ //$NON-NLS-2$ 			
					
					// clean the session cache
					numRemoved = this.sessionCache.cleanCache(this.timeout);
					
					this.logger.debug(Messages.getString("SessionsMonitor.7") + numRemoved + Messages.getString("SessionsMonitor.8")); //$NON-NLS-1$ //$NON-NLS-2$
										
				}
				catch (InterruptedException e)
				{
					if(!this.isRunning())
						break;
				}
				
			}
			
			this.logger.info(Messages.getString("SessionsMonitor.9")); //$NON-NLS-1$
			
			return;
		}
			
		
		/** Terminate the thread.
		 * 
		 */
		public void shutdown()
		{
			this.setRunning(false);
			
			this.interrupt();
		}
		
		/** Whether the thread will remain alive.
		 * 
		 * @return this.running
		 */
		protected synchronized boolean isRunning()
		{
			return this.running;
		}
		
		/** Whether the thread should keep running.
		 * 
		 * @param running
		 */
		protected synchronized void setRunning(boolean running)
		{
			this.running = running;
		}	
}
