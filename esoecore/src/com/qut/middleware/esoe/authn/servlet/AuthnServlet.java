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
 * Creation Date: 05/10/2006
 * 
 * Purpose: Control point for principal authentication and identification, conforms to 2.4 servlet spec.
 */
package com.qut.middleware.esoe.authn.servlet;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.authn.AuthnProcessor;
import com.qut.middleware.esoe.authn.bean.AuthnProcessorData;
import com.qut.middleware.esoe.authn.bean.impl.AuthnProcessorDataImpl;
import com.qut.middleware.esoe.authn.exception.AuthnFailureException;
import com.qut.middleware.esoe.logout.LogoutProcessor;

/** Control point for principal authentication and identification, conforms to 2.4 servlet spec. */
public class AuthnServlet extends HttpServlet
{
	private static final long serialVersionUID = -7214377113642690032L;
	private final String DYNAMIC_RESPONSE_URL_SESSION_NAME = "com.qut.middleware.esoe.authn.servlet.dynamicresponseurl"; //$NON-NLS-1$

	protected AuthnProcessor authnProcessor;
	protected String authnDynamicURLParam;
	protected String sessionTokenName;
	protected String sessionDomain;
	protected String disableSSOTokenName;

	private String servletInfo = Messages.getString("AuthnServlet.0"); //$NON-NLS-1$

	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(AuthnServlet.class.getName());

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		execAuthnProcessor(request, response);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		execAuthnProcessor(request, response);
	}

	/**
	 * Submits the request to the authentication processor to handle
	 * 
	 * @param request
	 *            Container request object associated with the request
	 * @param response
	 *            Container response object associated with the request
	 * @throws IOException
	 */
	private void execAuthnProcessor(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		AuthnProcessorData data;
		AuthnProcessor.result result;
		String encodedURL, dynamicResponseURL;
		byte[] decodedURL;

		data = (AuthnProcessorData) request.getSession().getAttribute(AuthnProcessorData.SESSION_NAME);

		if (data == null)
		{
			this.logger.debug(Messages.getString("AuthnServlet.19")); //$NON-NLS-1$
			data = new AuthnProcessorDataImpl();
		}

		data.setHttpRequest(request);
		data.setHttpResponse(response);
		processCookies(data);

		/* Determine if we have previously setup our dynamic URL */
		dynamicResponseURL = (String) request.getSession().getAttribute(this.DYNAMIC_RESPONSE_URL_SESSION_NAME);
		if (dynamicResponseURL == null)
		{
			/* Determine if a BASE64 encoded dynamic response URL has been supplied with the request */
			encodedURL = request.getParameter(this.authnDynamicURLParam);
			if (encodedURL != null)
			{
				this.logger.debug(Messages.getString("AuthnServlet.24")); //$NON-NLS-1$
				decodedURL = Base64.decodeBase64(encodedURL.getBytes()); //$NON-NLS-1$
				dynamicResponseURL = new String(decodedURL, "UTF-16");

				/*
				 * Store this value in the session in case the authn system requires further interactions with the
				 * browser so that the final success redirect is still set correctly
				 */
				request.getSession().setAttribute(this.DYNAMIC_RESPONSE_URL_SESSION_NAME, dynamicResponseURL);
			}
		}
		else
			this.logger.debug(Messages.getString("AuthnServlet.25")); //$NON-NLS-1$

		try
		{
			result = this.authnProcessor.execute(data);
			this.logger.debug(Messages.getString("AuthnServlet.26") + result.toString()); //$NON-NLS-1$

			switch (result)
			{
				case Completed:
					this.logger.debug(Messages.getString("AuthnServlet.27")); //$NON-NLS-1$
					request.getSession().removeAttribute(AuthnProcessorData.SESSION_NAME);

					setSessionCookie(data);
					if (data.getRedirectTarget() == null)
						throw new AuthnFailureException(Messages.getString("AuthnServlet.3")); //$NON-NLS-1$

					if (dynamicResponseURL != null)
					{
						/* Remove the dynamic URL from the session as we are about to finish with it */
						request.getSession().removeAttribute(this.DYNAMIC_RESPONSE_URL_SESSION_NAME);
						response.sendRedirect(dynamicResponseURL);
					}
					else
						response.sendRedirect(data.getRedirectTarget());
					break;

				case UserAgent:
					this.logger.debug(Messages.getString("AuthnServlet.29")); //$NON-NLS-1$
					request.getSession().setAttribute(AuthnProcessorData.SESSION_NAME, data);

					if (data.getRedirectTarget() != null)
						response.sendRedirect(data.getRedirectTarget());
					else
						if (data.getErrorCode() > 0)
							response.sendError(data.getErrorCode(), data.getErrorMessage());
						else
							throw new AuthnFailureException(Messages.getString("AuthnServlet.5")); //$NON-NLS-1$
					break;

				case Failure:
					this.logger.debug(Messages.getString("AuthnServlet.28")); //$NON-NLS-1$
					request.getSession().removeAttribute(AuthnProcessorData.SESSION_NAME);

					clearSessionCookie(data);
					if (data.getRedirectTarget() == null)
						throw new AuthnFailureException(Messages.getString("AuthnServlet.4")); //$NON-NLS-1$
					response.sendRedirect(data.getRedirectTarget());
					break;

				case Invalid:
					this.logger.debug(Messages.getString("AuthnServlet.30")); //$NON-NLS-1$
					request.getSession().removeAttribute(AuthnProcessorData.SESSION_NAME);

					clearSessionCookie(data);
					if (data.getInvalidURL() == null)
						throw new AuthnFailureException(Messages.getString("AuthnServlet.6")); //$NON-NLS-1$
					response.sendRedirect(data.getInvalidURL());
					break;
			}
		}
		catch (AuthnFailureException afe)
		{
			this.logger.debug(Messages.getString("AuthnServlet.31") + afe.getLocalizedMessage() + Messages.getString("AuthnServlet.32")); //$NON-NLS-1$ //$NON-NLS-2$
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#getServletInfo()
	 */
	@Override
	public String getServletInfo()
	{
		return this.servletInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#init()
	 */
	@Override
	public void init() throws ServletException
	{
		FileInputStream configFile;
		Properties props;
		WebApplicationContext webAppContext;

		try
		{
			configFile = new FileInputStream(System.getProperty("esoe.data") + ConfigurationConstants.ESOE_CONFIG);

			props = new java.util.Properties();

			props.load(configFile);

			/* Spring integration to make our servlet aware of IoC */
			webAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());

			this.authnProcessor = (AuthnProcessor) webAppContext.getBean(ConfigurationConstants.AUTHN_PROCESSOR, com.qut.middleware.esoe.authn.AuthnProcessor.class);

			if (this.authnProcessor == null)
			{
				this.logger.error(MessageFormat.format(Messages.getString("AuthnServlet.1"), ConfigurationConstants.AUTHN_PROCESSOR)); //$NON-NLS-1$
				throw new IllegalArgumentException(Messages.getString("AuthnServlet.1") + ConfigurationConstants.AUTHN_PROCESSOR); //$NON-NLS-1$
			}

			this.authnDynamicURLParam = props.getProperty(ConfigurationConstants.AUTHN_DYNAMIC_URL_PARAM);
			this.sessionTokenName = props.getProperty(ConfigurationConstants.ESOE_SESSION_TOKEN_NAME);
			this.sessionDomain = props.getProperty(ConfigurationConstants.ESOE_SESSION_DOMAIN);
			this.disableSSOTokenName = props.getProperty(ConfigurationConstants.DISABLE_SSO_TOKEN_NAME);

			if (this.authnDynamicURLParam == null)
			{
				this.logger.error(Messages.getString("AuthnServlet.10") //$NON-NLS-1$
						+ ConfigurationConstants.AUTHN_DYNAMIC_URL_PARAM + Messages.getString("AuthnServlet.11")); //$NON-NLS-1$
				throw new IllegalArgumentException(Messages.getString("AuthnServlet.10") //$NON-NLS-1$
						+ ConfigurationConstants.AUTHN_DYNAMIC_URL_PARAM + Messages.getString("AuthnServlet.11")); //$NON-NLS-1$
			}
			if (this.sessionTokenName == null)
			{
				this.logger.error(Messages.getString("AuthnServlet.10") //$NON-NLS-1$
						+ ConfigurationConstants.ESOE_SESSION_TOKEN_NAME + Messages.getString("AuthnServlet.11")); //$NON-NLS-1$
				throw new IllegalArgumentException(Messages.getString("AuthnServlet.10") //$NON-NLS-1$
						+ ConfigurationConstants.ESOE_SESSION_TOKEN_NAME + Messages.getString("AuthnServlet.11")); //$NON-NLS-1$
			}
			if (this.sessionDomain == null)
			{
				this.logger.error(Messages.getString("AuthnServlet.10") //$NON-NLS-1$
						+ ConfigurationConstants.ESOE_SESSION_DOMAIN + Messages.getString("AuthnServlet.11")); //$NON-NLS-1$
				throw new IllegalArgumentException(Messages.getString("AuthnServlet.10") //$NON-NLS-1$
						+ ConfigurationConstants.ESOE_SESSION_DOMAIN + Messages.getString("AuthnServlet.11")); //$NON-NLS-1$
			}
			if (this.disableSSOTokenName == null)
			{
				this.logger.error(Messages.getString("AuthnServlet.10") //$NON-NLS-1$
						+ ConfigurationConstants.DISABLE_SSO_TOKEN_NAME + Messages.getString("AuthnServlet.11")); //$NON-NLS-1$
				throw new IllegalArgumentException(Messages.getString("AuthnServlet.10") //$NON-NLS-1$
						+ ConfigurationConstants.DISABLE_SSO_TOKEN_NAME + Messages.getString("AuthnServlet.11")); //$NON-NLS-1$
			}

			this.logger.info(Messages.getString("AuthnServlet.33")); //$NON-NLS-1$
		}
		catch (BeansException e)
		{
			this.logger.error(MessageFormat.format(Messages.getString("AuthnServlet.12"), ConfigurationConstants.AUTHN_PROCESSOR, e.getLocalizedMessage())); //$NON-NLS-1$
			throw new ServletException(MessageFormat.format(Messages.getString("AuthnServlet.12"), ConfigurationConstants.AUTHN_PROCESSOR, e.getLocalizedMessage())); //$NON-NLS-1$
		}
		catch (MalformedURLException e)
		{
			this.logger.error(Messages.getString("AuthnServlet.14") //$NON-NLS-1$
					+ ConfigurationConstants.ESOE_CONFIG + Messages.getString("AuthnServlet.15") + e.getLocalizedMessage()); //$NON-NLS-1$
			throw new ServletException(Messages.getString("AuthnServlet.14") //$NON-NLS-1$
					+ ConfigurationConstants.ESOE_CONFIG + Messages.getString("AuthnServlet.15")); //$NON-NLS-1$
		}
		catch (IllegalStateException e)
		{
			this.logger.error(Messages.getString("AuthnServlet.16") + e.getLocalizedMessage()); //$NON-NLS-1$
			throw new ServletException(Messages.getString("AuthnServlet.16")); //$NON-NLS-1$
		}
		catch (IOException e)
		{
			this.logger.error(Messages.getString("AuthnServlet.17") //$NON-NLS-1$
					+ ConfigurationConstants.ESOE_CONFIG + Messages.getString("AuthnServlet.18") + e.getLocalizedMessage()); //$NON-NLS-1$
			throw new ServletException(Messages.getString("AuthnServlet.17") //$NON-NLS-1$
					+ ConfigurationConstants.ESOE_CONFIG + Messages.getString("AuthnServlet.18")); //$NON-NLS-1$
		}
	}

	/**
	 * Iteraties through all cookies presented by user request and retrieves details about SSO and any current session
	 * 
	 * @param data
	 *            Local request AuthnProcessoreData bean
	 */
	private void processCookies(AuthnProcessorData data)
	{
		Cookie[] cookies = data.getHttpRequest().getCookies();
		if (cookies != null)
		{
			this.logger.debug(Messages.getString("AuthnServlet.20")); //$NON-NLS-1$
			for (Cookie cookie : cookies)
			{
				this.logger.debug(Messages.getString("AuthnServlet.21") + cookie.getName() + Messages.getString("AuthnServlet.22") + cookie.getValue()); //$NON-NLS-1$ //$NON-NLS-2$
				/* Allow automated handlers to not perform any function if user demands manual input */
				if (cookie.getName().equals(this.disableSSOTokenName) && cookie.getValue().equals("true")) //$NON-NLS-1$
				{
					this.logger.debug(Messages.getString("AuthnServlet.23")); //$NON-NLS-1$
					data.setAutomatedSSO(false);
				}
				if (cookie.getName().equals(this.sessionTokenName))
				{
					data.setSessionID(cookie.getValue());
				}
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
		Cookie sessionCookie = new Cookie(this.sessionTokenName, data.getSessionID());
		sessionCookie.setDomain(this.sessionDomain);
		sessionCookie.setMaxAge(-1); // negative indicates session scope cookie
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
		logger.debug("Clearing esoe session cookie " + sessionTokenName);
		
		/* Remove the value of the users session cookie at the ESOE */
		Cookie sessionCookie = new Cookie(this.sessionTokenName, ""); //$NON-NLS-1$
		sessionCookie.setDomain(this.sessionDomain);
		sessionCookie.setSecure(false);
		sessionCookie.setMaxAge(0);
		sessionCookie.setPath("/");
		data.getHttpResponse().addCookie(sessionCookie);
	}
}
