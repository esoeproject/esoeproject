/* 
 * Copyright 2007, Queensland University of Technology
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
 * Creation Date: 04/07/2007
 * 
 * Purpose: Integrates ESOE/SPEP into Blackboard
 */
package com.qut.middleware.spep.integrators.blackboard;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import blackboard.platform.BbServiceManager;
import blackboard.platform.log.LogService;
import blackboard.platform.security.authentication.BaseAuthenticationModule;
import blackboard.platform.security.authentication.BbAuthenticationFailedException;
import blackboard.platform.security.authentication.BbCredentialsNotFoundException;
import blackboard.platform.security.authentication.BbSecurityException;
import blackboard.platform.security.authentication.HttpAuthConfig;
import blackboard.platform.security.authentication.SessionStub;

import com.qut.middleware.spep.filter.SPEPFilter;

public class BlackboardIntegrator extends BaseAuthenticationModule
{
	/* Hard to determine if we should be extending the above class or using the interface, some blackboard docs specified that as of
	 * 6.1 implementations MUST extend BaseAuthenticationModule... why? They didn't say...
	 */
	
	private final String AUTH_TYPE = "esoe-spep";
	private String AUTH_KEYS[] = {"impl", "userID", "fullName", "mail", "roles", "logoutURL"}; 
	
	private HttpAuthConfig authConfig;
	
	/* Integrate with Blackboard log service */
	private LogService logger;
	private Logger slf4jLogger = LoggerFactory.getLogger(BlackboardIntegrator.class);
	
	public BlackboardIntegrator()
	{
		this.logger = BbServiceManager.getLogService();
	}
	
	public String doAuthenticate(HttpServletRequest request, HttpServletResponse response) throws BbSecurityException, BbAuthenticationFailedException, BbCredentialsNotFoundException
	{
		HashMap<String, List<Object>> attributes = (HashMap<String, List<Object>>)request.getSession().getAttribute(SPEPFilter.ATTRIBUTES);
		
		/* Ensure SPEP filter has protected this request */
		if(attributes == null)
		{
			/* Enable failover to standard blackboard authentication if SPEP has chosen not to get involved */
			this.logger.logInfo("Failing over from SPEP to Blackboard default as SPEP has not populated the environment");
			return super.doAuthenticate(request, response);
			// throw new BbCredentialsNotFoundException("User credentials were not located in the session");
		}
		
		/* Get the UserIdentifier value from the session map to return */
		
		StringTokenizer userIDAttributeTokens = new StringTokenizer(authConfig.getProperty("userID").toString(), ", ");
		// Search through the attribute names in the order given to find one we can use.
		while(userIDAttributeTokens.hasMoreTokens())
		{
			String attributeName = userIDAttributeTokens.nextToken();
			List<Object> userIDs = attributes.get(attributeName);
			if (userIDs != null && userIDs.size() > 0)
			{
				String userID = (String) userIDs.get(0);
				
				// Check if we got a value, and return it
				if(userID != null && userID.length() > 0)
				{
					this.logger.logInfo("Got user identifier from attribute " + attributeName + "  Value was: " + userID + "  Starting blackboard session as this user");
					return userID;
				}
			}
		}
		
		this.logger.logInfo("Attempted to start user session but couldn't find user identifer, about to abort");
		throw new BbCredentialsNotFoundException("User credentials were located in the session but not user identifier was provided");
	}

	public void doLogout(HttpServletRequest request, HttpServletResponse response) throws BbSecurityException
	{
		this.slf4jLogger.debug("executing doLogout()");
		
		URL logoutURL = (URL)authConfig.getProperty("logoutURL");
		
		/* Clear up the users blackboard session */
		SessionStub sessionStub = new SessionStub( request );
		sessionStub.disassociateCurrentSessionAndUser();

		try
		{
			this.slf4jLogger.debug("About to redirect to URL: " + logoutURL);
			this.logger.logInfo("About to redirect to URL: " + logoutURL);
			response.sendRedirect(logoutURL.toExternalForm());
		}
		catch (IOException e)
		{
			this.logger.logError("Unable to redirect to ESOE logout portal");
		}
	}
	
	public String getAuthType()
	{
		return this.AUTH_TYPE;
	}

	public String[] getPropKeys()
	{
		return this.AUTH_KEYS;
	}

	public void setConfig(HttpAuthConfig config)
	{
		this.authConfig = config;		
	}
}
