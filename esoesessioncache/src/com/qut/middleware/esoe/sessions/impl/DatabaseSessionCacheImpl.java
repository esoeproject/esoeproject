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
 * Creation Date: 12/09/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.esoe.sessions.impl;

import java.text.MessageFormat;
import java.util.GregorianCalendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.esoe.logout.LogoutThreadPool;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.cache.SessionCache;
import com.qut.middleware.esoe.sessions.data.SessionCacheDAO;
import com.qut.middleware.esoe.sessions.exception.InvalidDescriptorIdentifierException;
import com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException;
import com.qut.middleware.esoe.sessions.exception.SessionCacheDAOException;
import com.qut.middleware.esoe.sessions.exception.SessionCacheUpdateException;
import com.qut.middleware.esoe.util.CalendarUtils;

public class DatabaseSessionCacheImpl implements SessionCache
{
	private SessionCacheDAO sessionCacheDAO;
	private LogoutThreadPool logoutPool;
	private int idleGraceTimePeriod;
		
	/* Local logging instance */
	Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	public DatabaseSessionCacheImpl(SessionCacheDAO sessionCacheDAO, LogoutThreadPool logoutPool, int idleGraceTimePeriod)
	{
		if(sessionCacheDAO == null)
			throw new IllegalArgumentException("sessionCacheDAO object cannot be null.");
		
		if(logoutPool == null)
			throw new IllegalArgumentException("logoutPool cannot be null.");
		
		if(idleGraceTimePeriod < 0 || idleGraceTimePeriod > Integer.MAX_VALUE )
			throw new IllegalArgumentException("idleGraceTimePeriod must be greater than 0 and less than INTEGER.MAX_VALUE.");
		
		this.sessionCacheDAO = sessionCacheDAO;
		this.logoutPool = logoutPool;
		this.idleGraceTimePeriod = idleGraceTimePeriod * 1000;		
		
		this.logger.info(MessageFormat.format("DatabaseSessionCache successfully created with idleGraceTime period of {0} seconds.", idleGraceTimePeriod) );
	}
	

	public void addEntitySessionIndex(Principal principal, String entityID, String descriptorSessionID) throws SessionCacheUpdateException 
	{
		try 
		{			
			this.sessionCacheDAO.addDescriptorSessionIdentifier(principal, entityID, descriptorSessionID);
		} 
		catch (SessionCacheDAOException e)
		{
			this.logger.error("DAO exception encountered. Unable to update session indicies.");
			this.logger.debug("Exception: ", e);
			throw new SessionCacheUpdateException("Unable to add entity session index. ", e);
		} 
		catch (InvalidSessionIdentifierException e)
		{			
			throw new SessionCacheUpdateException("Exception while adding principal to session cache. Unable to continue. Message was: " + e.getMessage(), e);
		} 
		catch (InvalidDescriptorIdentifierException e)
		{			
			throw new SessionCacheUpdateException("Exception while adding principal to session cache. Unable to continue. Message was: " + e.getMessage(), e);
		}
	}


	public void addSession(Principal data) throws SessionCacheUpdateException
	{
		try
		{
			this.sessionCacheDAO.addSession(data);
		}
		catch (SessionCacheDAOException e)
		{
			this.logger.error("Failed to add session to database.");
	    	this.logger.debug(e.getMessage());
			throw new SessionCacheUpdateException("Exception while adding principal to session cache. Unable to continue. Message was: " + e.getMessage(), e);
		}
	}

	public int cleanCache(int age) 
	{
		try
		{
			GregorianCalendar thisCal = CalendarUtils.generateXMLCalendar().toGregorianCalendar();;
					
			// delete any sessions passed hard limit expiry
			int expiredRemoved = this.sessionCacheDAO.deleteExpiredSessions();
		
			// call dao clean cache to delete all expired idle sessions
			int idleRemoved = this.sessionCacheDAO.deleteIdleSessions();
		
			// get remaining idle sessions (this will be sessions with active entity sessions only)
			List<String> idle = this.sessionCacheDAO.getIdleSessions(age);
			
			this.logger.debug("Retrieved " + idle.size() + " idle sessions from DB ..");
			
			int logouts = 0;
			for (String sessionID: idle)
			{
				Principal nextIdle = this.sessionCacheDAO.getSession(sessionID, false);
			
				if(nextIdle != null)
				{
					this.logger.debug(MessageFormat.format("Session {0} has been idle for {1} seconds. Logging out active entity sessions.", sessionID,  (System.currentTimeMillis() - nextIdle.getLastAccessed()) / 1000 ) );
					
					// logout idle , update db active entity sessions
					this.logoutPool.createLogoutTask(nextIdle, true);
					
					long idleGraceExpiryTime = System.currentTimeMillis() + this.idleGraceTimePeriod;
					this.sessionCacheDAO.updateIdleEntitySessions(nextIdle, idleGraceExpiryTime);
					
					logouts ++;				
				}
			}
			
			long duration = System.currentTimeMillis() - thisCal.getTimeInMillis();			
				
			this.logger.info(MessageFormat.format("Completed cache cleanup in {0} milliseconds. {1} Expired sessions removed, {2} Expired idle sessions removed. {3} idle sessions logged out. Current Cache size is {4}.", duration ,  expiredRemoved, idleRemoved, logouts,  this.getSize()) );
		
			return (idleRemoved + expiredRemoved);
		}
	    catch (SessionCacheDAOException e)
		{
	    	this.logger.error("Failed to clean expired sessions.");
	    	this.logger.debug(e.getMessage());
	    	return 0;
		}	    
	}
	
	
	public Principal getSession(String sessionID)
	{
		try 
		{
			return this.sessionCacheDAO.getSession(sessionID);
		} 
		catch (SessionCacheDAOException e) 
		{
			this.logger.error("Principal session {} could not be retrieved.", sessionID);
			this.logger.debug("Stack trace : ", e);
	
			return null;
		}
	}

	
	public Principal getSessionBySAMLID(String samlID)
	{
		try {
			return this.sessionCacheDAO.getSessionBySAMLID(samlID);
		} 
		catch (SessionCacheDAOException e)
		{
			this.logger.error("Principal session {} could not be retrieved.", samlID);
			this.logger.debug(e.getCause().getStackTrace().toString());
	
			return null;
		}
	}

	
	public int getSize()
	{
		try 
		{
			return this.sessionCacheDAO.getSize();
		} 
		catch (SessionCacheDAOException e)
		{
			this.logger.error("Unable to obtain session cache size.");
			this.logger.debug(e.getCause().getStackTrace().toString());
	
			return 0;
		}
	}

	
	public void removeSession(String sessionID) throws SessionCacheUpdateException 
	{
		try 
		{
			this.sessionCacheDAO.deleteSession(sessionID);
		} 
		catch (SessionCacheDAOException e)
		{
			this.logger.error("Principal session could not be removed.");
			this.logger.debug(e.getCause().getStackTrace().toString());
	
			throw new SessionCacheUpdateException("Unable to perform session removal for " + sessionID);
		}
	}
	
	
	public void updatePrincipalAttributes(Principal principal) throws SessionCacheUpdateException
	{
		try
		{
			this.sessionCacheDAO.updatePrincipalAttributes(principal);
		}
		catch (SessionCacheDAOException e)
		{
			this.logger.error("Principal session attributes could not be updated");
			this.logger.debug(e.getCause().getStackTrace().toString());
	
			throw new SessionCacheUpdateException("BleeeH");
		}
	}	

	
	public boolean validSession(String sessionID) 
	{
		try 
		{
			return this.sessionCacheDAO.validSession(sessionID);
		} 
		catch (SessionCacheDAOException e) 
		{
			this.logger.error("Session ID {} could not be validated.", sessionID);
			this.logger.debug(e.getCause().getStackTrace().toString());
	
			return false;
		}
	}

	
	public long getLastCleaned() {
		// NOT implemented in database cache
		return 0l;
	}

}
