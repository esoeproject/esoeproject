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
 * Creation Date: 06/03/2007
 * 
 * Purpose: Control point for delegated principal authentication from Shibboleth 1.x identity providers
 */
package com.qut.middleware.delegator.shib.servlet;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.qut.middleware.delegator.shib.ConfigurationConstants;
import com.qut.middleware.delegator.shib.authn.AuthnProcessor;
import com.qut.middleware.delegator.shib.authn.bean.AuthnProcessorData;
import com.qut.middleware.delegator.shib.authn.bean.impl.AuthnProcessorDataImpl;

public class ShibbolethServlet extends HttpServlet
{
	private AuthnProcessor authnProcessor;
	private String ssoURL;
	private String sessionTokenName;
	private String sessionDomain;
	private String deniedURL;
	private String acceptURL;
	private String failURL;

	private static final long serialVersionUID = 6215353757431245631L;

	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(ShibbolethServlet.class.getName());

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		execAuthnProcessor(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException
	{
		execAuthnProcessor(request, response);
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

		FileInputStream configFile;
		Properties props;
		WebApplicationContext webAppContext;

		try
		{
			configFile = new FileInputStream(System.getProperty("shibdeleg.data") + ConfigurationConstants.DELEGATOR_CONFIG);
			props = new java.util.Properties();
			props.load(configFile);

			/* Spring integration to make our servlet aware of IoC */
			webAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());

			this.authnProcessor = (AuthnProcessor) webAppContext.getBean(ConfigurationConstants.AUTHN_PROCESSOR,
					com.qut.middleware.delegator.shib.authn.AuthnProcessor.class);

			this.ssoURL = props.getProperty(ConfigurationConstants.SSO_URL);
			this.sessionTokenName = props.getProperty(ConfigurationConstants.SESSION_TOKEN_NAME);
			this.sessionDomain = props.getProperty(ConfigurationConstants.COOKIE_SESSION_DOMAIN);
			this.deniedURL = props.getProperty(ConfigurationConstants.DENIED_URL);
			this.acceptURL = props.getProperty(ConfigurationConstants.ACCEPT_URL);
			this.failURL = props.getProperty(ConfigurationConstants.FAIL_URL);
		}
		catch (BeansException e)
		{
			this.logger
					.error("Exception state encountered while attempting to resolve bean for authnProcessor identified as "
							+ ConfigurationConstants.AUTHN_PROCESSOR);
			throw new ServletException(
					"Exception state encountered while attempting to resolve bean for authnProcessor identified as "
							+ ConfigurationConstants.AUTHN_PROCESSOR, e);
		}
		catch (MalformedURLException e)
		{
			this.logger.error("Unable to locate configuration properties file in servlet context path named "
					+ ConfigurationConstants.DELEGATOR_CONFIG);
			throw new ServletException("Unable to locate configuration properties file in servlet context path named "
					+ ConfigurationConstants.DELEGATOR_CONFIG, e);
		}
		catch (IllegalStateException e)
		{
			this.logger.error("Spring configuration is not in an appropriate state to resolve configured beans");
			throw new ServletException(
					"Spring configuration is not in an appropriate state to resolve configured beans");
		}
		catch (IOException e)
		{
			this.logger.error("IOException while attempting to load configuration properties for shibboleth delegator");
			throw new ServletException(
					"IOException while attempting to load configuration properties for shibboleth delegator", e);
		}
	}

	private void execAuthnProcessor(HttpServletRequest request, HttpServletResponse response) throws ServletException
	{
		AuthnProcessor.result result;
		AuthnProcessorData data;

		String acceptedPolicy = request.getParameter(ConfigurationConstants.ACCEPTED_IDENTITY_FORM_ELEMENT);

		if (acceptedPolicy != null && acceptedPolicy.equals(ConfigurationConstants.ACCEPTED_POLICY))
		{
			data = (AuthnProcessorData) request.getSession().getAttribute(AuthnProcessorData.SESSION_NAME);
			request.getSession().removeAttribute(AuthnProcessorData.SESSION_NAME);
			
			if (data == null)
			{
				try
				{
					response.sendRedirect(this.failURL);
					return;
				}
				catch (IOException e)
				{
					throw new ServletException("Shib authentication delegator unable to send redirect, I/O Exception", e);
				}
			}
			
			data.setHttpRequest(request);
			data.setHttpResponse(response);

			this.logger.info("Remote session was accepted for establishment by principal identified by: "
					+ data.getSessionID() + " at local epoch timestamp of: " + System.currentTimeMillis());
			setSessionCookie(data);
			try
			{
				response.sendRedirect(this.ssoURL);
			}
			catch (IOException e)
			{
				throw new ServletException("Shib authentication delegator unable to send redirect, I/O Exception", e);
			}
			return;
		}

		if (acceptedPolicy != null && acceptedPolicy.equals(ConfigurationConstants.DENIED_POLICY))
		{
			data = (AuthnProcessorData) request.getSession().getAttribute(AuthnProcessorData.SESSION_NAME);
			data.setHttpRequest(request);
			data.setHttpResponse(response);
			this.logger.info("Remote session was not accepted for establishment by principal identified by: "
					+ data.getSessionID() + " denying any further actions");
			try
			{
				response.sendRedirect(this.deniedURL);
			}
			catch (IOException e)
			{
				throw new ServletException("Shib authentication delegator unable to send redirect, I/O Exception", e);
			}
		}
		else
		{
			data = new AuthnProcessorDataImpl();
			data.setHttpRequest(request);
			data.setHttpResponse(response);

			try
			{
				this.logger.debug("About to do shibboleth authn processor logic");

                result = this.authnProcessor.execute(data);

                this.logger.debug("Shibboleth authentication authority processor indicated result of " + result.toString());

				switch (result)
				{
					case Completed:
						request.getSession().setAttribute(AuthnProcessorData.SESSION_NAME, data);
						response.sendRedirect(this.acceptURL);
						break;

					case Failure:
						data.getHttpResponse().sendRedirect(this.failURL);
						break;
				}
			}
			catch (IOException e)
			{
				throw new ServletException("Shib authentication delegator unable to send redirect, I/O Exception", e);
			}
		}

	}

	/**
	 * Sets the session cookie for this principal
	 * 
	 * @param data
	 */
	private void setSessionCookie(AuthnProcessorData data)
	{
		this.logger.debug("Setting session cookie with values of - Domain: " + this.sessionDomain + " Name: "
				+ this.sessionTokenName + " value: " + data.getSessionID());
		Cookie sessionCookie = new Cookie(this.sessionTokenName, data.getSessionID());
		sessionCookie.setDomain(this.sessionDomain);
		sessionCookie.setSecure(false);
		sessionCookie.setPath("/");
		data.getHttpResponse().addCookie(sessionCookie);
	}

	/**
	 * Clears a provided session identifying cookie when some invalid value has been presented
	 * 
	 * @param data
	 *            Local request AuthnProcessoreData bean
	 */
	private void clearSessionCookie(AuthnProcessorData data)
	{
		/* Remove the value of the users session cookie at the ESOE */
		Cookie sessionCookie = new Cookie(this.sessionTokenName, "");
		sessionCookie.setDomain(this.sessionDomain);
		sessionCookie.setSecure(true);
		data.getHttpResponse().addCookie(sessionCookie);
	}

}
