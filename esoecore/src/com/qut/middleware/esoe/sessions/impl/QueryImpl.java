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
 * Creation Date: 09/10/2006
 * 
 * Purpose: Implements the Query interface.
 */
package com.qut.middleware.esoe.sessions.impl;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.esoe.sessions.Messages;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.Query;
import com.qut.middleware.esoe.sessions.cache.SessionCache;
import com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException;

/** Implements the Query interface. */
public class QueryImpl implements Query
{
	private SessionCache cache;
	
	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(QueryImpl.class.getName());

	/**
	 * Constructor
	 * 
	 * @param cache
	 *            The session cache to use.
	 */
	public QueryImpl(SessionCache cache)
	{
		if (cache == null)
		{
			throw new IllegalArgumentException(Messages.getString("QueryImpl.SessionCacheNull")); //$NON-NLS-1$
		}
		
		this.cache = cache;
		
		this.logger.info(Messages.getString("QueryImpl.0")); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.Query#queryAuthnSession(java.lang.String)
	 */
	public Principal queryAuthnSession(String sessionID) throws InvalidSessionIdentifierException
	{
		Principal principal = this.cache.getSession(sessionID);
		if (principal == null)
		{
			this.logger.debug(MessageFormat.format(Messages.getString("QueryImpl.1"), sessionID)); //$NON-NLS-1$
			throw new InvalidSessionIdentifierException();
		}

		this.logger.debug(MessageFormat.format(Messages.getString("QueryImpl.2"), sessionID, principal.getPrincipalAuthnIdentifier())); //$NON-NLS-1$
		return principal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.Query#querySAMLSession(java.lang.String)
	 */
	public Principal querySAMLSession(String samlID) throws InvalidSessionIdentifierException
	{
		Principal principal = this.cache.getSessionBySAMLID(samlID);
		if (principal == null)
		{
			this.logger.debug(MessageFormat.format(Messages.getString("QueryImpl.3"), samlID)); //$NON-NLS-1$
			throw new InvalidSessionIdentifierException();
		}

		this.logger.debug(MessageFormat.format(Messages.getString("QueryImpl.4"), samlID, principal.getPrincipalAuthnIdentifier())); //$NON-NLS-1$
		return principal;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sessions.Query#validAuthnSession(java.lang.String)
	 */
	public void validAuthnSession(String sessionID) throws InvalidSessionIdentifierException
	{
		if(! this.cache.validSession(sessionID) )
		{
			this.logger.error(Messages.getString("QueryImpl.5") + sessionID); //$NON-NLS-1$
			throw new InvalidSessionIdentifierException();
		}
	}

}
