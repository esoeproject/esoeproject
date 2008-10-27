package com.qut.middleware.esoe.sessions.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.esoe.sessions.Terminate;
import com.qut.middleware.esoe.sessions.cache.SessionCache;
import com.qut.middleware.esoe.sessions.exception.SessionCacheUpdateException;

public class TerminateImpl implements Terminate
{
	private SessionCache sessionCache;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public TerminateImpl(SessionCache sessionCache)
	{
		if (sessionCache == null)
		{
			throw new IllegalArgumentException("Session cache cannot be null");
		}
		
		this.sessionCache = sessionCache;
		
		this.logger.info("Created TerminateImpl");
	}
	
	public void terminateSession(String sessionID) throws SessionCacheUpdateException
	{
		// Pretty simple.. just remove the session from the cache.
		this.sessionCache.removeSession(sessionID);
		
		this.logger.info("Successfully terminated session ID {}", sessionID);
	}

}
