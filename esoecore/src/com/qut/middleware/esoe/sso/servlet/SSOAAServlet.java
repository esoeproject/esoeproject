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
 * Purpose: Control point for SSO module, SPEP session establishment and authentication network wide
 * single logout.
 */
package com.qut.middleware.esoe.sso.servlet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.authn.servlet.Messages;
import com.qut.middleware.esoe.log4j.InsaneLogLevel;
import com.qut.middleware.esoe.sso.SSOProcessor;
import com.qut.middleware.esoe.sso.bean.SSOProcessorData;
import com.qut.middleware.esoe.sso.bean.impl.SSOProcessorDataImpl;
import com.qut.middleware.esoe.sso.exception.InvalidRequestException;
import com.qut.middleware.esoe.sso.exception.InvalidSessionIdentifierException;

/** Control point for SSO module, SPEP session establishment and authentication network wide single logout. */

public class SSOAAServlet extends HttpServlet
{
	private static final long serialVersionUID = 4083024809106578744L;
	private final String SAML_REQUEST_FORM_ELEMENT = "SAMLRequest"; //$NON-NLS-1$
	private final String SAML_RESPONSE_TEMPLATE = "samlResponseTemplate.html"; //$NON-NLS-1$

	private MessageFormat samlMessageFormat;
	private SSOProcessor authAuthorityProcessor;
	private String sessionTokenName, authnRedirectURL, authnDynamicURLParam, ssoURL, sessionDomain;

	private final String samlResponseTemplate;

	/* Local logging instance */
	private Logger logger = Logger.getLogger(SSOAAServlet.class.getName());

	/**
	 * Generic constructor for SSOAAServlet
	 */
	public SSOAAServlet()
	{
		try
		{
			URL location = SSOAAServlet.class.getResource(this.SAML_RESPONSE_TEMPLATE);

			if (location == null)
				throw new IllegalArgumentException(Messages.getString("SSOAAServlet.3")); //$NON-NLS-1$

			this.samlResponseTemplate = FileCopyUtils.copyToString(new InputStreamReader(location.openStream()));
			this.samlMessageFormat = new MessageFormat(this.samlResponseTemplate);
		}
		catch (FileNotFoundException e)
		{
			throw new IllegalArgumentException(Messages.getString("SSOAAServlet.4")); //$NON-NLS-1$
		}
		catch (IOException e)
		{
			throw new IllegalArgumentException(Messages.getString("SSOAAServlet.5")); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		SSOProcessorData data;

		this.logger.debug(Messages.getString("SSOAAServlet.0")); //$NON-NLS-1$
		data = (SSOProcessorData) request.getSession().getAttribute(SSOProcessorData.SESSION_NAME);

		/*
		 * If this is a request due to previous allow of ForceAuthn by spep then retrieve 
		 * details from session and continue
		 */
		if (data != null)
		{
			this.logger.debug(Messages.getString("SSOAAServlet.30")); //$NON-NLS-1$
			data.setHttpRequest(request);
			data.setHttpResponse(response);

			processCookies(request, data);

			/*
			 * Set the data value of returning request to be true to prevent additional
			 * processing by Authentication Authority
			 */
			data.setReturningRequest(true);

			processRequest(request, response, data);
		}
		else
		{
			this.logger.debug(Messages.getString("SSOAAServlet.31")); //$NON-NLS-1$
			
			/* All other get requests are forced to login portal */
			response.sendRedirect(this.authnRedirectURL);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException
	{
		this.logger.debug(Messages.getString("SSOAAServlet.32")); //$NON-NLS-1$
		execAuthenticationAuthorityProcessor(request, response);
	}

	/**
	 * Submits the request to the authentication authority processor to handle.
	 * 
	 * @param request
	 *            Container request object associated with the request.
	 * @param response
	 *            Container response object associated with the request.
	 */
	private void execAuthenticationAuthorityProcessor(HttpServletRequest request, HttpServletResponse response)
	{
		SSOProcessorData data;
		String samlRequestEncoded;
		byte[] samlRequestBytes;
		String samlRequestDocument;

		data = (SSOProcessorData) request.getSession().getAttribute(SSOProcessorData.SESSION_NAME);
		try
		{
			if (data == null)
			{
				this.logger.debug(Messages.getString("SSOAAServlet.33")); //$NON-NLS-1$
				data = new SSOProcessorDataImpl();

				/* SAML 2.0 Post Browser Profile specifies request is base64 encoded, decode and store for processors */
				samlRequestEncoded = request.getParameter(this.SAML_REQUEST_FORM_ELEMENT);

				if (samlRequestEncoded == null)
				{
					this.logger.info(Messages.getString("SSOAAServlet.34")); //$NON-NLS-1$
					generateErrorResponse(response, Messages.getString("SSOAAServlet.1")); //$NON-NLS-1$
					return;
				}

				this.logger.log(InsaneLogLevel.INSANE, Messages.getString("SSOAAServlet.35") + samlRequestEncoded); //$NON-NLS-1$
				samlRequestBytes = Base64.decodeBase64(samlRequestEncoded.getBytes("UTF-8")); //$NON-NLS-1$
				samlRequestDocument = new String(samlRequestBytes);
				
				this.logger.log(InsaneLogLevel.INSANE, Messages.getString("SSOAAServlet.36") + samlRequestDocument); //$NON-NLS-1$
				data.setRequestDocument(samlRequestDocument);
			}

			data.setHttpRequest(request);
			data.setHttpResponse(response);

			processCookies(request, data);

			processRequest(request, response, data);
		}
		catch (UnsupportedEncodingException de)
		{
			this.logger.warn(Messages.getString("SSOAAServlet.37") + de.getLocalizedMessage()); //$NON-NLS-1$
			generateErrorResponse(response, Messages.getString("SSOAAServlet.6")); //$NON-NLS-1$
		}
	}

	/**
	 * Removes data stored in the session that is no longer required after various processing completes
	 * 
	 * @param request
	 *            Current request object
	 */
	private void cleanSessionState(HttpServletRequest request)
	{
		this.logger.debug(Messages.getString("SSOAAServlet.38")); //$NON-NLS-1$
		request.getSession().removeAttribute(SSOProcessorData.SESSION_NAME);
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
		this.logger.warn(Messages.getString("SSOAAServlet.39") + message); //$NON-NLS-1$
		try
		{
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
		}
		catch (IOException e)
		{
			this.logger.error(Messages.getString("SSOAAServlet.40") + e.getLocalizedMessage()); //$NON-NLS-1$
		}
	}

	/**
	 * Generates a response to the client browser when the principal has not yet authenticated and the SPEP advises it
	 * will allow session establishment
	 * 
	 * @param request
	 *            Current request object
	 * @param response
	 *            Generated response object
	 * @param data
	 *            Principals SSOProcessorData bean
	 */
	private void generateForceAuthnResponse(HttpServletRequest request, HttpServletResponse response,
			SSOProcessorData data)
	{
		try
		{
			byte[] encodedURL;
			String encodedURLString;
			
			this.logger.debug(Messages.getString("SSOAAServlet.41")); //$NON-NLS-1$

			/* Set this request to returning before storing */
			data.setReturningRequest(true);
			request.getSession().setAttribute(SSOProcessorData.SESSION_NAME, data);

			this.logger.debug(Messages.getString("SSOAAServlet.42") + this.ssoURL + Messages.getString("SSOAAServlet.43")); //$NON-NLS-1$ //$NON-NLS-2$
			
			/* Base64 encode dynamic redirect URL */
			encodedURL = Base64.encodeBase64(this.ssoURL.getBytes("UTF-8")); //$NON-NLS-1$
			encodedURLString = new String(encodedURL);

			response.sendRedirect(this.authnRedirectURL + "?" + this.authnDynamicURLParam + "=" + encodedURLString); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch (IOException e)
		{
			this.logger.warn(Messages.getString("SSOAAServlet.44") + e.getLocalizedMessage()); //$NON-NLS-1$
			generateErrorResponse(response, Messages.getString("SSOAAServlet.12")); //$NON-NLS-1$
		}
	}

	/**
	 * Generates a response to users browser and base64 encodes form contents as required for the post profile in SAML
	 * 2.0
	 * 
	 * @param response
	 *            Container response object associated with the request
	 * @param data
	 *            The SSOProcessorData bean which has been associated with this request
	 */
	private void generateResponse(HttpServletResponse response, SSOProcessorData data)
	{
		try
		{
			Object[] responseArgs;
			byte[] samlResponseEncoded;
			String htmlOutput;
			PrintWriter writer = response.getWriter();
			
			this.logger.debug(Messages.getString("SSOAAServlet.45")); //$NON-NLS-1$

			if (data.getResponseDocument() == null)
			{
				this.logger.debug(Messages.getString("SSOAAServlet.46")); //$NON-NLS-1$
				generateErrorResponse(response, Messages.getString("SSOAAServlet.10")); //$NON-NLS-1$
			}

			this.logger.log(InsaneLogLevel.INSANE, Messages.getString("SSOAAServlet.47") + data.getResponseDocument()); //$NON-NLS-1$
			/* Encode SAML Response in base64 */
			samlResponseEncoded = Base64.encodeBase64(data.getResponseDocument().getBytes("UTF-8")); //$NON-NLS-1$
			responseArgs = new Object[] { data.getResponseEndpoint(), new String(samlResponseEncoded) };
			htmlOutput = this.samlMessageFormat.format(responseArgs);
			
			this.logger.log(InsaneLogLevel.INSANE, Messages.getString("SSOAAServlet.48") + htmlOutput); //$NON-NLS-1$

			writer.print(htmlOutput);
			writer.flush();
		}
		catch (UnsupportedEncodingException de)
		{
			this.logger.warn(Messages.getString("SSOAAServlet.49") + de.getLocalizedMessage()); //$NON-NLS-1$
			generateErrorResponse(response, Messages.getString("SSOAAServlet.11")); //$NON-NLS-1$
		}
		catch (IOException ioe)
		{
			this.logger.warn(Messages.getString("SSOAAServlet.50") + ioe.getLocalizedMessage()); //$NON-NLS-1$
			generateErrorResponse(response, Messages.getString("SSOAAServlet.12")); //$NON-NLS-1$
		}
	}
	
	/**
	 * Clears a provided session identifying cookie when some invalid value has been presented
	 * @param data Local request AuthnProcessoreData bean
	 */
	private void clearSessionCookie(SSOProcessorData data)
	{
		/* Remove the value of the users session cookie at the ESOE */
		Cookie sessionCookie = new Cookie(this.sessionTokenName, ""); //$NON-NLS-1$
		sessionCookie.setDomain(this.sessionDomain);
		sessionCookie.setSecure(true);
		data.getHttpResponse().addCookie(sessionCookie);
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

			this.authAuthorityProcessor = (SSOProcessor) webAppContext.getBean(
					ConfigurationConstants.AUTHN_AUTHORITY_PROCESSOR, com.qut.middleware.esoe.sso.SSOProcessor.class);

			if (this.authAuthorityProcessor == null)
				throw new IllegalArgumentException(
						Messages.getString("SSOAAServlet.2") + ConfigurationConstants.AUTHN_AUTHORITY_PROCESSOR); //$NON-NLS-1$

			this.sessionTokenName = props.getProperty(ConfigurationConstants.SESSION_TOKEN_NAME);
			this.authnRedirectURL = props.getProperty(ConfigurationConstants.AUTHN_REDIRECT_URL);
			this.authnDynamicURLParam = props.getProperty(ConfigurationConstants.AUTHN_DYNAMIC_URL_PARAM);
			this.ssoURL = props.getProperty(ConfigurationConstants.SSO_URL);
			this.sessionDomain = props.getProperty(ConfigurationConstants.COOKIE_SESSION_DOMAIN);

			if (this.sessionTokenName == null)
				throw new IllegalArgumentException(Messages.getString("SSOAAServlet.13") //$NON-NLS-1$
						+ ConfigurationConstants.SESSION_TOKEN_NAME + Messages.getString("SSOAAServlet.14")); //$NON-NLS-1$

			if (this.authnRedirectURL == null)
				throw new IllegalArgumentException(Messages.getString("SSOAAServlet.15") //$NON-NLS-1$
						+ ConfigurationConstants.AUTHN_REDIRECT_URL + Messages.getString("SSOAAServlet.16")); //$NON-NLS-1$

			if (this.authnDynamicURLParam == null)
				throw new IllegalArgumentException(Messages.getString("SSOAAServlet.17") //$NON-NLS-1$
						+ ConfigurationConstants.AUTHN_DYNAMIC_URL_PARAM + Messages.getString("SSOAAServlet.18")); //$NON-NLS-1$

			if (this.ssoURL == null)
				throw new IllegalArgumentException(Messages.getString("SSOAAServlet.19") //$NON-NLS-1$
						+ ConfigurationConstants.SSO_URL + Messages.getString("SSOAAServlet.20")); //$NON-NLS-1$
			
			if (this.sessionDomain == null)
				throw new IllegalArgumentException(Messages.getString("SSOAAServlet.19") //$NON-NLS-1$
						+ ConfigurationConstants.COOKIE_SESSION_DOMAIN + Messages.getString("SSOAAServlet.20")); //$NON-NLS-1$

			this.logger.info(Messages.getString("SSOAAServlet.51") + this.sessionTokenName //$NON-NLS-1$
					+ Messages.getString("SSOAAServlet.52") + this.authnRedirectURL + Messages.getString("SSOAAServlet.53") //$NON-NLS-1$ //$NON-NLS-2$
					+ this.authnDynamicURLParam + Messages.getString("SSOAAServlet.54") + this.ssoURL); //$NON-NLS-1$
		}
		catch (BeansException e)
		{
			this.logger.fatal(Messages.getString("SSOAAServlet.55") + e.getLocalizedMessage()); //$NON-NLS-1$
			throw new ServletException(Messages.getString("SSOAAServlet.21") //$NON-NLS-1$
					+ ConfigurationConstants.AUTHN_AUTHORITY_PROCESSOR + Messages.getString("SSOAAServlet.22")); //$NON-NLS-1$
		}
		catch (MalformedURLException e)
		{
			throw new ServletException(Messages.getString("SSOAAServlet.23") //$NON-NLS-1$
					+ ConfigurationConstants.ESOE_CONFIG + Messages.getString("SSOAAServlet.24")); //$NON-NLS-1$
		}
		catch (IllegalStateException e)
		{
			this.logger.fatal(Messages.getString("SSOAAServlet.56") + e.getLocalizedMessage()); //$NON-NLS-1$
			throw new ServletException(Messages.getString("SSOAAServlet.25")); //$NON-NLS-1$
		}
		catch (IOException e)
		{
			this.logger.fatal(Messages.getString("SSOAAServlet.57") + e.getLocalizedMessage()); //$NON-NLS-1$
			throw new ServletException(Messages.getString("SSOAAServlet.26") //$NON-NLS-1$
					+ ConfigurationConstants.ESOE_CONFIG + Messages.getString("SSOAAServlet.27")); //$NON-NLS-1$
		}
	}

	/**
	 * Processes cookies looking for those which are important to the system
	 * 
	 * @param request
	 *            Current request object
	 * @param data
	 *            SSOProcessorData bean for this session
	 */
	private void processCookies(HttpServletRequest request, SSOProcessorData data)
	{
		Cookie[] cookies;

		/* Set the value of sessionID for processors */
		cookies = request.getCookies();
		if (cookies != null)
		{
			this.logger.debug(Messages.getString("SSOAAServlet.58")); //$NON-NLS-1$
			for (Cookie cookie : cookies)
			{
				this.logger.debug(Messages.getString("SSOAAServlet.59") + cookie.getName() + Messages.getString("SSOAAServlet.60") + cookie.getValue()); //$NON-NLS-1$ //$NON-NLS-2$
				if (cookie.getName().equals(this.sessionTokenName))
					data.setSessionID(cookie.getValue());
			}
		}
	}

	/**
	 * Hands all logic processing to the AuthenticationAuthority
	 * 
	 * @param request
	 *            Current request object
	 * @param response
	 *            Current response object
	 * @param data
	 *            SSOProcessorData bean for this session
	 */
	private void processRequest(HttpServletRequest request, HttpServletResponse response, SSOProcessorData data)
	{
		SSOProcessor.result result;

		try
		{
			/* Hand logic processing to the Authentication Authority */
			result = this.authAuthorityProcessor.execute(data);
			this.logger.debug(Messages.getString("SSOAAServlet.61") + result.toString()); //$NON-NLS-1$
			
			switch (result)
			{
				case SSOGenerationSuccessful:
					generateResponse(response, data);
					cleanSessionState(request);
					break;
				case LogoutSuccessful:
					break;
				case ForcePassiveAuthn:
					generateResponse(response, data);
					cleanSessionState(request);
					this.clearSessionCookie(data);
					break;
				case ForceAuthn:
					generateForceAuthnResponse(request, response, data);
					this.clearSessionCookie(data);
					break;
			}
		}
		catch (InvalidSessionIdentifierException isie)
		{
			this.logger.warn(Messages.getString("SSOAAServlet.62")); //$NON-NLS-1$
			this.clearSessionCookie(data);
			
			if (data.getResponseDocument() != null)
				generateResponse(response, data);
			else
				generateErrorResponse(response, Messages.getString("SSOAAServlet.28")); //$NON-NLS-1$
		}
		catch (InvalidRequestException ire)
		{
			this.logger.warn(Messages.getString("SSOAAServlet.63")); //$NON-NLS-1$
			this.clearSessionCookie(data);
			
			if (data.getResponseDocument() != null)
				generateResponse(response, data);
			else
				generateErrorResponse(response, Messages.getString("SSOAAServlet.29")); //$NON-NLS-1$
		}
	}
}
