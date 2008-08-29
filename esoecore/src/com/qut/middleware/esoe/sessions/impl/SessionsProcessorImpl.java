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
 * Author: Shaun Mangelsdorf
 * Creation Date: 10/10/2006
 *
 * Purpose: Implements the sessions processor interface
 */
package com.qut.middleware.esoe.sessions.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.esoe.sessions.Create;
import com.qut.middleware.esoe.sessions.Messages;
import com.qut.middleware.esoe.sessions.Query;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.esoe.sessions.Terminate;
import com.qut.middleware.esoe.sessions.Update;

/** Implements the sessions processor interface.*/
public class SessionsProcessorImpl implements SessionsProcessor
{
	private Create create;
	private Query query;
	private Terminate terminate;
	private Update update;
	
	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(SessionsProcessorImpl.class.getName());

	/**
	 * Constructor. Takes in an instance of the objects to be fed to other classes
	 * 
	 * @param create
	 * @param query
	 * @param terminate
	 * @param update
	 */
	public SessionsProcessorImpl(Create create, Query query, Terminate terminate, Update update)
	{
		if (create == null)
		{
			throw new IllegalArgumentException(Messages.getString("SessionsProcessorImpl.CreateNull")); //$NON-NLS-1$
		}
		if (query == null)
		{
			throw new IllegalArgumentException(Messages.getString("SessionsProcessorImpl.QueryNull")); //$NON-NLS-1$
		}
		if (terminate == null)
		{
			throw new IllegalArgumentException(Messages.getString("SessionsProcessorImpl.TerminateNull")); //$NON-NLS-1$
		}
		if (update == null)
		{
			throw new IllegalArgumentException(Messages.getString("SessionsProcessorImpl.UpdateNull")); //$NON-NLS-1$
		}
		this.create = create;
		this.query = query;
		this.terminate = terminate;
		this.update = update;

		this.logger.info(Messages.getString("SessionsProcessorImpl.0")); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.SessionsProcessor#getCreate()
	 */
	public Create getCreate()
	{
		return this.create;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.SessionsProcessor#getQuery()
	 */
	public Query getQuery()
	{
		return this.query;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.SessionsProcessor#getTerminate()
	 */
	public Terminate getTerminate()
	{
		return this.terminate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.SessionsProcessor#getUpdate()
	 */
	public Update getUpdate()
	{
		return this.update;
	}
}
