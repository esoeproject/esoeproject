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
 * Purpose: Implements the Terminate interface.
 */
package com.qut.middleware.esoe.sessions.impl;

import java.text.MessageFormat;

import org.apache.log4j.Logger;

import com.qut.middleware.esoe.sessions.Messages;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.Terminate;
import com.qut.middleware.esoe.sessions.cache.SessionCache;
import com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException;

/** Implements the Terminate interface.*/
public class TerminateImpl implements Terminate
{
	private SessionCache cache;
	
	/* Local logging instance */
	private Logger logger = Logger.getLogger(TerminateImpl.class.getName());

	/**
	 * Constructor
	 * 
	 * @param cache
	 *            The session cache to use.
	 */
	public TerminateImpl(SessionCache cache)
	{
		if (cache == null)
		{
			throw new IllegalArgumentException(Messages.getString("TerminateImpl.SessionCacheNull")); //$NON-NLS-1$
		}
		this.cache = cache;
		
		this.logger.info(Messages.getString("TerminateImpl.0")); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.Terminate#terminateSession(java.lang.String)
	 */
	public void terminateSession(String sessionID) throws InvalidSessionIdentifierException
	{
		Principal principal = this.cache.removeSession(sessionID);

		if (principal == null)
		{
			this.logger.warn(MessageFormat.format(Messages.getString("TerminateImpl.1"), sessionID)); //$NON-NLS-1$
			throw new InvalidSessionIdentifierException();
		}
		
		this.logger.debug(MessageFormat.format(Messages.getString("TerminateImpl.2"), sessionID)); //$NON-NLS-1$
	}

}
