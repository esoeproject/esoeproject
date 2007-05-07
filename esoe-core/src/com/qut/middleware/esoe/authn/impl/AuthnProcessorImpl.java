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
 * Author: Bradley Beddoes
 * Creation Date: 06/10/2006
 * 
 * Purpose: Default implementation of AuthnProcessor
 */
package com.qut.middleware.esoe.authn.impl;

import java.util.List;

import org.apache.log4j.Logger;

import com.qut.middleware.esoe.authn.AuthnProcessor;
import com.qut.middleware.esoe.authn.bean.AuthnProcessorData;
import com.qut.middleware.esoe.authn.exception.AuthnFailureException;
import com.qut.middleware.esoe.authn.exception.HandlerRegistrationException;
import com.qut.middleware.esoe.authn.exception.SessionCreationException;
import com.qut.middleware.esoe.authn.pipeline.Handler;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException;
import com.qut.middleware.esoe.spep.SPEPProcessor;

public class AuthnProcessorImpl implements AuthnProcessor
{
	SPEPProcessor spepProcessor;
	SessionsProcessor sessionsProcessor;
	private final List<Handler> registeredHandlers;

	private Logger logger = Logger.getLogger(AuthnProcessorImpl.class.getName());

	/**
	 * @param sessionTokenName
	 * @param sessionDomain
	 * @param registeredHandlers
	 *            Vector of registered handlers for authn in the esoe
	 * @throws HandlerRegistrationException
	 *             Thrown when no handlers have been supplied
	 */
	public AuthnProcessorImpl(SPEPProcessor spepProcessor, SessionsProcessor sessionsProcessor, List<Handler> registeredHandlers) throws HandlerRegistrationException
	{
		/* Ensure that a stable base is created when this Processor is setup */
		if (spepProcessor == null)
		{
			this.logger.fatal(Messages.getString("AuthnProcessorImpl.14")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("AuthnProcessorImpl.15")); //$NON-NLS-1$
		}
		if (sessionsProcessor == null)
		{
			this.logger.fatal(Messages.getString("AuthnProcessorImpl.16")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("AuthnProcessorImpl.17")); //$NON-NLS-1$
		}
		if (registeredHandlers == null || registeredHandlers.size() == 0)
		{
			this.logger.fatal(Messages.getString("AuthnProcessorImpl.0")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("AuthnProcessorImpl.0")); //$NON-NLS-1$
		}

		this.spepProcessor = spepProcessor;
		this.sessionsProcessor = sessionsProcessor;
		this.registeredHandlers = registeredHandlers;

		if (registeredHandlers.size() == 0)
		{
			this.logger.fatal(Messages.getString("AuthnProcessorImpl.5")); //$NON-NLS-1$
			throw new HandlerRegistrationException(Messages.getString("AuthnProcessorImpl.6")); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.authn.AuthnProcessor#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)
	 */
	public result execute(AuthnProcessorData data) throws AuthnFailureException
	{
		Handler.result result;

		data.setSuccessfulAuthn(false);
		
		/* Determine if a sessionID is set and if so if its valid in our system */
		if(data.getSessionID() != null && data.getSessionID().length() > 0)
		{
			try
			{
				this.sessionsProcessor.getQuery().validAuthnSession(data.getSessionID());
			}
			catch (InvalidSessionIdentifierException e)
			{
				// Not major error. This could happen if session expires but browser submits old data,
				// reset session ID and continue with processing
				this.logger.error(Messages.getString("AuthnProcessorImpl.19")); //$NON-NLS-1$
				this.logger.debug(e.getLocalizedMessage(), e);
				data.setSessionID(null);
			}
		}

		/* Even with iterators no sync required here as don't reasonbly expect the structure of the underlying list to be modied */
		for (Handler handler : this.registeredHandlers)
		{
			/* For continuing session don't re-evaluate handlers we have already passed */
			if(data.getCurrentHandler() != null && data.getCurrentHandler().length() > 0 && !data.getCurrentHandler().equals(handler.getHandlerName()))
				continue;
				
			data.setCurrentHandler(handler.getHandlerName());
			this.logger.debug(Messages.getString("AuthnProcessorImpl.7") + handler.getHandlerName()); //$NON-NLS-1$
			try
			{
				result = handler.execute(data);
				this.logger
						.debug(Messages.getString("AuthnProcessorImpl.8") + handler.getHandlerName() + Messages.getString("AuthnProcessorImpl.9") + result.toString()); //$NON-NLS-1$ //$NON-NLS-2$
				switch (result)
				{
					/* Ensure that multiple handlers aren't competing to identify the principal in the stack */
					case Successful:
						if (!data.getSuccessfulAuthn())
							data.setSuccessfulAuthn(true);
						else
							throw new AuthnFailureException(Messages.getString("AuthnProcessorImpl.3")); //$NON-NLS-1$
						
						/* Handler completed reset current handler value */
						data.setCurrentHandler(null);
						break;
					case SuccessfulNonPrincipal:
						/* Handler completed reset current handler value */
						data.setCurrentHandler(null);
						break;
					case NoAction:
						/* Handler completed reset current handler value */
						data.setCurrentHandler(null);
						break;
					case Failure:
						return AuthnProcessor.result.Failure;
					case UserAgent:
						return AuthnProcessor.result.UserAgent;
					case Invalid:
						return AuthnProcessor.result.Invalid;
				}
			}
			catch (SessionCreationException sce)
			{
				this.logger.warn(Messages.getString("AuthnProcessorImpl.10")); //$NON-NLS-1$
				return AuthnProcessor.result.Failure;
			}
		}

		if (!data.getSuccessfulAuthn())
		{
			this.logger.warn(Messages.getString("AuthnProcessorImpl.4")); //$NON-NLS-1$
			throw new AuthnFailureException(Messages.getString("AuthnProcessorImpl.4")); //$NON-NLS-1$
		}

		this.logger.debug(Messages.getString("AuthnProcessorImpl.13") + data.getSessionID()); //$NON-NLS-1$
		
		/* If the principal is traversing authn for a second time in their session
		 * generally for privilledge level escalation this will ensure any authorization
		 * results are cleared at any SPEP they have visited, if they are new it simply returns
		 */
		try
		{
			this.spepProcessor.clearPrincipalSPEPCaches(this.sessionsProcessor.getQuery().queryAuthnSession(data.getSessionID()));
		}
		catch (InvalidSessionIdentifierException e)
		{
			this.logger.error(Messages.getString("AuthnProcessorImpl.18") + data.getSessionID()); //$NON-NLS-1$
			return AuthnProcessor.result.Failure;
		}

		return AuthnProcessor.result.Completed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.authn.AuthnProcessor#getRegisteredHandlers()
	 */
	public List<Handler> getRegisteredHandlers()
	{
		return this.registeredHandlers;
	}
}
