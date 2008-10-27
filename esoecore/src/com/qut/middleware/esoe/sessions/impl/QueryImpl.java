/* Copyright 2008, Queensland University of Technology
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
 * Creation Date: 08/10/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.esoe.sessions.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.Query;
import com.qut.middleware.esoe.sessions.cache.SessionCache;

public class QueryImpl implements Query
{
	private SessionCache sessionCache;

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public QueryImpl(SessionCache sessionCache)
	{
		if (sessionCache == null)
		{
			throw new IllegalArgumentException("Session cache cannot be null.");
		}
		
		this.sessionCache = sessionCache;
		
		this.logger.info("Created QueryImpl");
	}
	
	public Principal queryAuthnSession(String sessionID)
	{
		// Grab session and perform some checks before it's returned.
		Principal principal = this.sessionCache.getSession(sessionID);
				
		// Validate locally. Keep the logic in the same place.
		if (!this.validateSession(principal))
		{
			this.logger.warn("Query for session by session ID {} returned an invalid session.", sessionID);
			return null;
		}
		
		this.logger.debug("Query for session by session ID {} returned a valid principal.", sessionID);
		return principal;
	}

	public Principal querySAMLSession(String samlID)
	{
		// Grab the session and perform some checks before it's returned.
		Principal principal = this.sessionCache.getSessionBySAMLID(samlID);
			
		// Validate locally. Keep the logic in the same place.
		if (!this.validateSession(principal))
		{
			this.logger.error("Query for session by SAML ID {} returned an invalid session.", samlID);
			return null;
		}
		
		this.logger.debug("Query for session by SAML ID {} returned a valid principal.", samlID);
		return principal;
	}

	public boolean validAuthnSession(String sessionID)
	{
		// No need to retrieve the entire session object here. Validation is done by the session cache.
		if (this.sessionCache.validSession(sessionID))
		{
			// Seems kind of dumb checking for a boolean value then returning that value, but we need to do it this way to do logging.
			this.logger.info("Successfully verified session ID {} as a valid session", sessionID);
			return true;
		}
		else
		{
			this.logger.info("Couldn't verify session ID {} as a valid session", sessionID);
			return false;
		}
	}
	
	private boolean validateSession(Principal session)
	{
		boolean valid = true;
		
		// Just perform some simple checks to make sure the principal session is valid before accepting it.
		if (session == null)
		{
			this.logger.debug("Null Principal passed to validate. Returning false.");
			valid =  false;
		}
		else
		{
			if (session.getSessionNotOnOrAfter() < System.currentTimeMillis())
			{
				this.logger.debug("Principal session has exceeded notOnOrAfter time frame.");
				valid =  false;
			}
		}
		// TODO Make this more rigorous.
		return  valid; 
	}
}
