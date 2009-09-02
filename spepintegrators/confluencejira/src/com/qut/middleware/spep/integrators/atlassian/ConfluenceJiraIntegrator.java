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
 * Creation Date: 06/06/2007
 * 
 * Purpose: Integrates ESOE / SPEP with Confluence and Jira
 */
package com.qut.middleware.spep.integrators.atlassian;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import bucket.container.ContainerManager;

import com.atlassian.seraph.auth.AuthenticatorException;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.user.EntityException;
import com.atlassian.user.Group;
import com.atlassian.user.GroupManager;
import com.atlassian.user.User;
import com.atlassian.user.UserManager;
import com.qut.middleware.spep.filter.SPEPFilter;

public class ConfluenceJiraIntegrator extends DefaultAuthenticator
{
	private static final long serialVersionUID = 234789254349L;

	private final String DEFAULT_GROUP_NAME = "confluence-users";
	private final String ANONYMOUS_DEFAULT_ACCOUNT = "esoe-confluence-anon";

	private UserManager userManager;
	private GroupManager groupManager;
	private Group defaultGroup;

	private Properties props;

	private String userIDAttribute;
	private String fullNameAttribute;
	private String mailAttribute;
	private String rolesAttribute;
	private String logoutURL;

	/* Local logging instance */
	private Logger logger = Logger.getLogger(ConfluenceJiraIntegrator.class.getName());

	public ConfluenceJiraIntegrator()
	{
		try
		{
			this.userManager = (UserManager) ContainerManager.getComponent("userManager");
			this.groupManager = (GroupManager) ContainerManager.getComponent("groupManager");
			this.defaultGroup = this.groupManager.getGroup(this.DEFAULT_GROUP_NAME);
		}
		catch (EntityException e)
		{
			this.logger.fatal("Could not perform initial interaction with confluence/jira");
			throw new IllegalStateException("Could not perform initial interaction with confluence/jira", e);
		}

		InputStream propStream = ConfluenceJiraIntegrator.class.getResourceAsStream("integrator.properties");
		
		if(propStream == null)
		{
			this.logger.fatal("Could not load intial properties file, could not be found in path, terminating startup");
			throw new IllegalArgumentException("Could not load intial properties file, could not be found in path, terminating startup");
		}
		
		props = new Properties();
		try
		{
			props.load(propStream);
			
			this.userIDAttribute = props.getProperty("userID");
			if(this.userIDAttribute == null)
			{
				this.logger.fatal("Could not load value for userID property");
				throw new IllegalArgumentException("Could not load value for userID property");
			}
			
			this.fullNameAttribute = props.getProperty("fullName");
			if(this.fullNameAttribute == null)
			{
				this.logger.fatal("Could not load value for fullName property");
				throw new IllegalArgumentException("Could not load value for fullName property");
			}
			
			this.mailAttribute = props.getProperty("mail");
			if(this.mailAttribute == null)
			{
				this.logger.fatal("Could not load value for mail property");
				throw new IllegalArgumentException("Could not load value for mail property");
			}
			
			this.rolesAttribute = props.getProperty("roles");
			if(this.rolesAttribute == null)
			{
				this.logger.fatal("Could not load value for roles property");
				throw new IllegalArgumentException("Could not load value for roles property");
			}
			
			this.logoutURL = props.getProperty("logoutURL");
			if(this.logoutURL == null)
			{
				this.logger.fatal("Could not load value for logoutURL  property");
				throw new IllegalArgumentException("Could not load value for logoutURL property");
			}
			
		}
		catch (IOException e)
		{
			this.logger.fatal("Could not load intial properties file, terminating startup");
			throw new IllegalArgumentException("Could not load intial properties file, terminating startup", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.atlassian.seraph.auth.DefaultAuthenticator#logout(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public boolean logout(HttpServletRequest request, HttpServletResponse response) throws AuthenticatorException
	{
		super.logout(request, response);
		
		HttpSession httpSession = request.getSession();
		try
		{
			this.logger.debug("Logging out user from current confluence session");
			request.getSession().setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, null);
			request.getSession().setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
			response.sendRedirect(this.logoutURL);
		}
		catch (IOException e)
		{
			this.logger.error(e.getLocalizedMessage());
			this.logger.debug(e);
			return false;
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.atlassian.seraph.auth.DefaultAuthenticator#getUser(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public Principal getUser(HttpServletRequest request, HttpServletResponse response)
	{
		HttpSession httpSession = request.getSession();
		User user;
		List<Object> userIdentifiers;

		HashMap<String, List<Object>> attributeMap = (HashMap<String, List<Object>>) httpSession.getAttribute(SPEPFilter.ATTRIBUTES);
		
		/* Possible lazy init or no attributes sent from ESOE */
		if(attributeMap == null)
			return null;

		userIdentifiers = attributeMap.get(this.userIDAttribute);

		/* Check if the principal is already logged in */
		if (httpSession != null && httpSession.getAttribute(DefaultAuthenticator.LOGGED_IN_KEY) != null)
		{
			user = (User) httpSession.getAttribute(DefaultAuthenticator.LOGGED_IN_KEY);
			if(user != null)
				return user;
		}

		/* Either the user has not logged in or a different user is now active */
		httpSession.removeAttribute(DefaultAuthenticator.LOGGED_IN_KEY);

		/*
		 * If we haven't been supplied with a user identifier attribute, map them to a default anonymous account that has
		 * little to no privilledges
		 */
		if (userIdentifiers == null)
		{
			this.logger.debug("loading anonymous user, couldn't find any user identifiers");
			return this.loadAnonymousUser(httpSession, response);
		}

		this.logger.debug("Doing user login to confluence/jira");
		for (Object id : userIdentifiers)
		{
			try
			{
				user = this.userManager.getUser((String) id);
				if (user != null)
				{
					this.logger.debug("Found existing user identified by " + (String) id + " establishing session for that user");
					updateAttributes(user, attributeMap);
					updateGroupMembership(user, attributeMap, response);
					httpSession.setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, user);
					return user;
				}
			}
			catch (EntityException e)
			{
				this.logger.error("Unable to create new user for confluence/jira - " + e.getLocalizedMessage());
				this.logger.debug(e);
				errorResponse(response);
			}
		}

		/* User is not currently registered in confluence/jira dynamically provision an account */
		user = registerUserAccount(userIdentifiers);

		if (user == null)
		{
			errorResponse(response);
			return null;
		}

		updateAttributes(user, attributeMap);
		updateGroupMembership(user, attributeMap, response);
		httpSession.setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, user);
		httpSession.setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);

		/* When we have a new user account show them the dashboard */
		try
		{
			response.sendRedirect("/dashboard.action");
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return user;
	}

	private User loadAnonymousUser(HttpSession httpSession, HttpServletResponse response)
	{
		try
		{
			User user = this.userManager.getUser(this.ANONYMOUS_DEFAULT_ACCOUNT);
			if (user != null)
			{
				httpSession.setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, user);
				return user;
			}
			else
			{
				this.logger.error("Unable to create anonymous user session - default anonymous account " + this.ANONYMOUS_DEFAULT_ACCOUNT + "is not present in confluence");
				errorResponse(response);
				return null;
			}
		}
		catch (EntityException e)
		{
			this.logger.error("Unable to create anonymous user session - default anonymous account " + this.ANONYMOUS_DEFAULT_ACCOUNT + "is not present in confluence");
			this.logger.debug(e);
			errorResponse(response);
			return null;
		}
	}

	private User registerUserAccount(List<Object> userIdentifiers)
	{
		User user;

		try
		{
			/* Ensure we got a username, if not this user is essentially anonymous */
			this.logger.info("Dynamically provisioning new account in confluence/jira for first time user identified as " + (String) userIdentifiers.get(0));
			/* Create a new user, in the case the user presents with multiple identifiers... utilise the first */
			user = this.userManager.createUser((String) userIdentifiers.get(0));

			return user;
		}
		catch (EntityException e)
		{
			this.logger.error("Unable to create new user for confluence/jira - " + e.getLocalizedMessage());
			this.logger.debug(e);
			return null;
		}
	}

	private void updateAttributes(User user, HashMap<String, List<Object>> attributes)
	{
		List<Object> fullName;
		List<Object> emailAddresses;

		fullName = attributes.get(this.fullNameAttribute);

		/* If multiple email addresses are present we'll use the first */
		emailAddresses = attributes.get(this.mailAttribute);

		this.logger.debug("Updating user data in confluence/jira for user " + user.getName());

		if (fullName != null)
			user.setFullName((String) fullName.get(0));

		if (emailAddresses != null)
			user.setEmail((String) emailAddresses.get(0));
	}

	private void updateGroupMembership(User user, HashMap<String, List<Object>> attributes, HttpServletResponse response)
	{
		Group group;
		List<Object> roles;

		try
		{
			/* Add user to the default confluence group otherwise they won't get anywhere */
			this.groupManager.addMembership(this.defaultGroup, user);

			roles = attributes.get(this.rolesAttribute);
			if (roles != null)
			{
				for (Object role : roles)
				{

					this.logger.debug("Attempting to map role " + (String) role + " to confluence/jira group");
					group = groupManager.getGroup((String) role);

					/*
					 * Ensure group exists, admins must still manually create groups, if its non existant ignore this
					 * role
					 */
					if (group != null)
					{
						this.logger.debug("Mapped role " + (String) role + " to confluence/jira group of the same name, adding user permissions");

						/* Only add membership if they haven't been previously given it of course */
						if (!groupManager.hasMembership(group, user))
							groupManager.addMembership(group, user);
					}

				}
			}
		}
		catch (EntityException e)
		{
			this.logger.error("Unable to create new user for confluence/jira - " + e.getLocalizedMessage());
			this.logger.debug(e);
			errorResponse(response);
		}
	}

	private void errorResponse(HttpServletResponse response)
	{
		try
		{
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "While attempting to provision your account internal errors in Confluence or Jira occured, contact system administrators for resolution");
		}
		catch (IOException e)
		{
			this.logger.error("Unable to create error response");
			this.logger.debug(e);
		}
	}

}
