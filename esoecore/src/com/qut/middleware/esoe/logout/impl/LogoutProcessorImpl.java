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
 * Author: Andre Zitelli
 * Creation Date: 14/12/2006
 * 
 * Purpose: Performs the logic to Logout SSO sessions.
 */
package com.qut.middleware.esoe.logout.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.crypto.KeystoreResolver;
import com.qut.middleware.esoe.logout.LogoutMechanism;
import com.qut.middleware.esoe.logout.LogoutProcessor;
import com.qut.middleware.esoe.logout.bean.LogoutProcessorData;
import com.qut.middleware.esoe.logout.bean.SSOLogoutState;
import com.qut.middleware.esoe.logout.bean.impl.SSOLogoutStateImpl;
import com.qut.middleware.esoe.logout.exception.InvalidSessionIdentifierException;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.esoe.sessions.Terminate;
import com.qut.middleware.esoe.sessions.exception.InvalidDescriptorIdentifierException;
import com.qut.middleware.esoe.sessions.exception.SessionCacheUpdateException;

/** Performs the logic to Logout sessions. */

public class LogoutProcessorImpl implements LogoutProcessor 
{	
	private SessionsProcessor sessionsProcessor;
	private LogoutMechanism logoutMechanism;
	
	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(LogoutProcessorImpl.class.getName());

	/** 
	 * 
	 * @param logoutFailures The repository to be used for recording FailedLogouts
	 * @param sessionsProcessor Used to manipulate user SSO Sessions
	 * @param wsClient For sending LogoutRequests to associated SPEP endpoints.
	 * @param metadata For resolving SPEP endpoints.
	 */
	public LogoutProcessorImpl(SessionsProcessor sessionsProcessor, LogoutMechanism logoutMechanism) 
	{		
		if (sessionsProcessor == null)
			throw new IllegalArgumentException(Messages.getString("LogoutProcessor.3")); //$NON-NLS-1$
		
		if(logoutMechanism == null)
			throw new IllegalArgumentException("Logout mechanism cannot be null !");
		
		this.sessionsProcessor = sessionsProcessor;
		this.logoutMechanism = logoutMechanism;
		
		this.logger.info(Messages.getString("LogoutProcessor.7")); //$NON-NLS-1$		
	}
	
	
	public result execute(LogoutProcessorData data) throws InvalidSessionIdentifierException
	{
		if(data == null)
			throw new IllegalArgumentException(Messages.getString("LogoutProcessor.8"));  //$NON-NLS-1$
		
		Principal principal = null;
		List<SSOLogoutState> logoutStates = new Vector<SSOLogoutState>();
		
		principal = this.sessionsProcessor.getQuery().queryAuthnSession(data.getSessionID());	
		
		if(principal == null ||  data.getSessionID() == null || !data.getSessionID().equals(principal.getSessionID()) )
		{
			throw new InvalidSessionIdentifierException(Messages.getString("LogoutProcessor.9")); //$NON-NLS-1$
		}

		this.logger.debug(Messages.getString("LogoutProcessor.25")); //$NON-NLS-1$
		
		// obtain active entities (SPEPS logged into) for user, iterate through and send logout request to each SPEP
		List<String> activeDescriptors = principal.getActiveEntityList();
		if(activeDescriptors != null)
		{
			Iterator<String> entitiesIterator = activeDescriptors.iterator();
			while(entitiesIterator.hasNext())
			{
				String entity = entitiesIterator.next();
				
				// resolve all enpoints for the given entity and send logout request
				List<String> endPoints = new Vector<String>();
				endPoints = this.logoutMechanism.getEndPoints(entity);
											
				Iterator<String> endpointIter = endPoints.iterator();
				while (endpointIter.hasNext())
				{
					String endPoint = endpointIter.next();
					
					List<String> indicies  = principal.getActiveEntitySessionIndices(entity);
											
					// store the state of the logout request for reporting if required
					SSOLogoutState logoutState = new SSOLogoutStateImpl();
					logoutState.setSPEPURL(entity);
				
					LogoutMechanism.result result =this.logoutMechanism.performSingleLogout(principal.getSAMLAuthnIdentifier(), indicies, endPoint, true);
				
					if(result == LogoutMechanism.result.LogoutSuccessful)
					{
						logoutState.setLogoutState(true);
						logoutState.setLogoutStateDescription(Messages.getString("LogoutProcessor.11")); //$NON-NLS-1$
					}
					else
					{							
						logoutState.setLogoutState(false);
						logoutState.setLogoutStateDescription(Messages.getString("LogoutProcessor.12"));										
					}
				
					logoutStates.add(logoutState);
				}
			}
		}		
		else
			this.logger.debug(Messages.getString("LogoutProcessor.26")); //$NON-NLS-1$
		
		// set the state of logout attempts for all user active SSO entities
		data.setLogoutStates(logoutStates);
		
		// no matter the outcome of sent LogoutRequests, we will terminate the users SSO session
		this.logger.info(Messages.getString("LogoutProcessor.15") + principal.getPrincipalAuthnIdentifier()); //$NON-NLS-1$
		
		Terminate terminate = this.sessionsProcessor.getTerminate();
		
		try
		{
			terminate.terminateSession(data.getSessionID());
		}
		catch (SessionCacheUpdateException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result.LogoutSuccessful;
		
	}
	
}
