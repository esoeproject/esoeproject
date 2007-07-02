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
 * Creation Date: 24/10/2006
 * 
 * Purpose: Control point for SSO module, enables Logout for entire authentication network.
 */
package com.qut.middleware.esoe.sso.servlet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.sso.SSOProcessor;
import com.qut.middleware.esoe.sso.bean.SSOProcessorData;
import com.qut.middleware.esoe.sso.bean.impl.SSOProcessorDataImpl;
import com.qut.middleware.esoe.sso.exception.InvalidRequestException;
import com.qut.middleware.esoe.sso.exception.InvalidSessionIdentifierException;

public class SSOLogoutServlet extends HttpServlet
{
	private static final long serialVersionUID = 5192046378667020982L;
	private final String LOGOUT_REQUEST_FORM_ELEMENT = "esoelogout_nonsso"; //$NON-NLS-1$
	private final String LOGOUT_RESPONSE_FORM_ELEMENT = "esoelogout_response"; //$NON-NLS-1$
	private final String DISABLE_SSO_FORM_ELEMENT = "disablesso"; //$NON-NLS-1$
	
	private SSOProcessor logoutAuthorityProcessor;	
	private String sessionTokenName;
	private String disableSSOTokenName;
	private String logoutURL;
	private String logoutResponseURL;
	private String sessionDomain;
	
	/* Local logging instance */
	private Logger logger = Logger.getLogger(SSOLogoutServlet.class.getName());
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{		
		this.execLogoutAuthorityProcessor(request, response);
	}
	
	/* Get methods should not really be utilized, but may be required on occasion.
	 * (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
	{
		try
		{
			if(this.logoutURL != null)
			{
				response.sendRedirect(this.logoutURL);
				return;
			}
			
			throw new ServletException(Messages.getString("SSOLogoutServlet.18")); //$NON-NLS-1$
			
		}
		catch(IOException e)
		{
			this.logger.warn(Messages.getString("SSOLogoutServlet.0")); //$NON-NLS-1$
		}
	}
	
	private void execLogoutAuthorityProcessor(HttpServletRequest request, HttpServletResponse response)
	{		
		// retrieve the required parameters from user request. These are: 
		// esoelogout_nonsso (required)
		// esoelogout_response=NUM (optional redirect destination)
		// disablesso (optional)
				
		this.logger.debug(Messages.getString("SSOLogoutServlet.1") + request.getRemoteAddr() ); //$NON-NLS-1$
		
		SSOProcessorData data = (SSOProcessorData) request.getSession().getAttribute(SSOProcessorData.SESSION_NAME);
		
		if(data == null)
			data = new SSOProcessorDataImpl();
		
		String logoutRequestParam = request.getParameter(this.LOGOUT_REQUEST_FORM_ELEMENT);
		
		if(logoutRequestParam == null)
		{
			this.generateErrorResponse(response, Messages.getString("SSOLogoutServlet.2"));	 //$NON-NLS-1$
		}
		else
		{
			this.processCookieData(request, data);
			
			// the result of the logout call can only result in an exception or success. We only care
			// about the exceptions. If none thrown, send the user on their way
			try
			{	
				this.logoutAuthorityProcessor.execute(data);
			}
			catch(InvalidRequestException e)
			{
				this.generateErrorResponse(response, Messages.getString("SSOLogoutServlet.3"));	 //$NON-NLS-1$
				return;
			}
			catch(InvalidSessionIdentifierException e)
			{
				this.generateErrorResponse(response, Messages.getString("SSOLogoutServlet.4")); //$NON-NLS-1$
				return;
			}
			
			// examine the request for the presense of 'disablesso' parameter. If it exists, we attempt to set a cookie
			// that will be used do disable SSO throughout the SSO environment. If it does not exist, we 
			// determine if one had been set previously and set its value to false.
			String disableSSOString = request.getParameter(this.DISABLE_SSO_FORM_ELEMENT);
			if(disableSSOString != null)
			{
				this.logger.debug(Messages.getString("SSOLogoutServlet.5")); //$NON-NLS-1$
				Cookie disableSSOCookie = new Cookie(this.disableSSOTokenName, "true"); //$NON-NLS-1$
				disableSSOCookie.setDomain(this.sessionDomain);
				disableSSOCookie.setSecure(true);
				response.addCookie(disableSSOCookie);
			}
			else
			{
				this.logger.debug(Messages.getString("SSOLogoutServlet.6")); //$NON-NLS-1$
				Cookie disableSSOCookie = new Cookie(this.disableSSOTokenName, "false"); //$NON-NLS-1$
				disableSSOCookie.setDomain(this.sessionDomain);
				disableSSOCookie.setSecure(true);
				response.addCookie(disableSSOCookie);
			}
			
			/* Remove the value of the users session cookie at the ESOE */
			Cookie sessionCookie = new Cookie(this.sessionTokenName, ""); //$NON-NLS-1$
			sessionCookie.setDomain(this.sessionDomain);
			sessionCookie.setSecure(true);
			response.addCookie(sessionCookie);
			
			this.logger.info(MessageFormat.format(Messages.getString("SSOLogoutServlet.7"), data.getSessionID()) ); //$NON-NLS-1$
			
			try
			{				
				// logout request processing completed
				String userResponseURL = request.getParameter(this.LOGOUT_RESPONSE_FORM_ELEMENT);
				if(userResponseURL != null)
					response.sendRedirect(userResponseURL);
				else
					response.sendRedirect(this.logoutResponseURL); 
			}
			catch(IOException e)
			{
				this.logger.warn(Messages.getString("SSOLogoutServlet.8")); //$NON-NLS-1$
			}
			
		}
	}	
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init(ServletConfig servletConfig) throws ServletException
	{
		super.init(servletConfig);
		
		URL configFile;
		Properties props;

		try
		{
			configFile = this.getServletContext().getResource(ConfigurationConstants.ESOE_CONFIG);
			props = new java.util.Properties();

			props.load(configFile.openStream());

			/* Spring integration to make our servlet aware of IoC */
			WebApplicationContext webAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(this
					.getServletContext());

			this.logoutAuthorityProcessor = (SSOProcessor) webAppContext.getBean(
					ConfigurationConstants.LOGOUT_AUTHORITY_PROCESSOR, com.qut.middleware.esoe.sso.SSOProcessor.class);

			if (this.logoutAuthorityProcessor == null)
				throw new IllegalArgumentException(Messages.getString("SSOLogoutServlet.9")); //$NON-NLS-1$

			this.sessionTokenName = props.getProperty(ConfigurationConstants.SESSION_TOKEN_NAME);
			this.logoutURL = props.getProperty(ConfigurationConstants.LOGOUT_REDIRECT_URL);
			this.disableSSOTokenName = props.getProperty(ConfigurationConstants.DISABLE_SSO_TOKEN_NAME);
			this.logoutResponseURL = props.getProperty(ConfigurationConstants.LOGOUT_RESPONSE_REDIRECT_URL);
			this.sessionDomain = props.getProperty(ConfigurationConstants.COOKIE_SESSION_DOMAIN);
			
			if (this.sessionTokenName == null)
				throw new IllegalArgumentException(Messages.getString("SSOLogoutServlet.20")+ ConfigurationConstants.SESSION_TOKEN_NAME ); //$NON-NLS-1$
			
			if (this.logoutURL == null)
				throw new IllegalArgumentException(Messages.getString("SSOLogoutServlet.21")+ ConfigurationConstants.LOGOUT_REDIRECT_URL ); //$NON-NLS-1$

			if (this.disableSSOTokenName == null)
				throw new IllegalArgumentException(Messages.getString("SSOLogoutServlet.22")+ ConfigurationConstants.DISABLE_SSO_TOKEN_NAME ); //$NON-NLS-1$

			if (this.logoutResponseURL == null)
				throw new IllegalArgumentException(Messages.getString("SSOLogoutServlet.23")+ ConfigurationConstants.LOGOUT_RESPONSE_REDIRECT_URL ); //$NON-NLS-1$
			
			if (this.sessionDomain == null)
				throw new IllegalArgumentException(Messages.getString("SSOLogoutServlet.24")+ ConfigurationConstants.COOKIE_SESSION_DOMAIN ); //$NON-NLS-1$

			this.logger.info(MessageFormat.format(Messages.getString("SSOLogoutServlet.10"), this.sessionTokenName , this.logoutURL)); //$NON-NLS-1$
		}
		catch (BeansException e)
		{
			this.logger.fatal(Messages.getString("SSOLogoutServlet.11") + e.getLocalizedMessage());  //$NON-NLS-1$
			throw new ServletException(Messages.getString("SSOLogoutServlet.12")+ ConfigurationConstants.LOGOUT_AUTHORITY_PROCESSOR ); //$NON-NLS-1$
		}
		catch (MalformedURLException e)
		{
			throw new ServletException(Messages.getString("SSOLogoutServlet.13") + ConfigurationConstants.ESOE_CONFIG );  //$NON-NLS-1$
		}
		catch (IllegalStateException e)
		{
			this.logger.fatal(e.getLocalizedMessage()); 
			throw new ServletException(); 
		}
		catch (IOException e)
		{
			this.logger.fatal(e.getLocalizedMessage()); 
			throw new ServletException(Messages.getString("SSOLogoutServlet.14") + ConfigurationConstants.ESOE_CONFIG );  //$NON-NLS-1$
		}
	}
	
	
	/* Extract the appropriate data from the appropriate cookies.
	 * 
	 */
	private void processCookieData(HttpServletRequest request, SSOProcessorData data)
	{
		Cookie[] cookies;

		/* Set the value of sessionID for processors */
		cookies = request.getCookies();
		if (cookies != null)
		{
			for (Cookie cookie : cookies)
			{
				this.logger.debug("Processing client cookie: " + cookie.getValue()); //$NON-NLS-1$
				
				if (cookie.getName().equals(this.sessionTokenName))
				{
					this.logger.debug(Messages.getString("SSOLogoutServlet.15") + cookie.getValue()); //$NON-NLS-1$
					data.setSessionID(cookie.getValue());
				}
			}
		}
		else
			this.logger.debug(Messages.getString("SSOLogoutServlet.17"));  //$NON-NLS-1$
		
	}
		
	
	/**
	 * Sends the user to the servlet configured error page for internal errors due to an inability to recover from some
	 * request
	 * 
	 * @param response
	 *            Current response object
	 * @param message
	 *            Message that should be displayed to the user for this error
	 */
	private void generateErrorResponse(HttpServletResponse response, String message)
	{
		this.logger.debug(MessageFormat.format(Messages.getString("SSOLogoutServlet.16"), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message) );  //$NON-NLS-1$
		
		try
		{
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
		}
		catch (IOException e)
		{
			this.logger.error(e.getLocalizedMessage()); 
		}
	}
}
