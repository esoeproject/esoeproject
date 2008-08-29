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
 * Creation Date: 09/11/2006
 * 
 * Purpose: A Thread to monitor the identifer cache and purge it of expired entries
 * at regular intervals as specified.
 */
package com.qut.middleware.spep.impl;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.saml2.identifier.IdentifierCache;

/** A Thread to monitor the identifer cache and purge it of expired entries
 * at regular intervals as specified.
 */
public class IdentifierCacheMonitor extends Thread
{
	private long timeout;
	private long interval;
	private IdentifierCache identifierCache;
	private volatile boolean running;


	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(IdentifierCacheMonitor.class.getName());
	
	/** 
	 * Constructor
	 * @param interval The interval in seconds between cache purges.
	 * @param timeout Max age inseconds for cache entries to stay active. Entries older
	 * than specified time will be removed at regular intervals.
	 */
	public IdentifierCacheMonitor(IdentifierCache identifierCache, long interval, long timeout)
	{
		if(identifierCache == null)
		{
			throw new IllegalArgumentException(Messages.getString("IdentifierCacheMonitor.1")); //$NON-NLS-1$
		}
		if(interval <= 0 || interval > Integer.MAX_VALUE/1000)
		{
			throw new IllegalArgumentException(Messages.getString("IdentifierCacheMonitor.2")); //$NON-NLS-1$
		}
		if(timeout <= 0 || timeout > Integer.MAX_VALUE/1000)
		{
			throw new IllegalArgumentException(Messages.getString("IdentifierCacheMonitor.3")); //$NON-NLS-1$
		}
		
		this.timeout = timeout * 1000;
		this.interval = interval * 1000;
		this.identifierCache = identifierCache;
		
		
		this.setName(Messages.getString("IdentifierCacheMonitor.4")); //$NON-NLS-1$
		this.start();
		
		this.logger.info(MessageFormat.format(Messages.getString("IdentifierCacheMonitor.5"), this.getName(), this.interval, this.timeout)); //$NON-NLS-1$
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run()
	{
		this.running = true;
		
		while(this.running)
		{
			
			try
			{
				this.doCachePurge();
			
				Thread.sleep(this.interval);
			}
			catch(InterruptedException e)
			{
				// ignore
			}
		}
	}
	
	public void stopRunning()
	{
		this.running = false;
		this.interrupt();
	}
	
	/* Clear identifier cache of expired entries.
	 * 
	 */
	private void doCachePurge()
	{
		int numRemoved = this.identifierCache.cleanCache((int)this.timeout);
		this.logger.debug(MessageFormat.format(Messages.getString("IdentifierCacheMonitor.6"), numRemoved)); //$NON-NLS-1$
	}

}
