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

import java.io.FileInputStream;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.sso.SSOProcessor;
import com.qut.middleware.esoe.sso.bean.SSOProcessorData;
import com.qut.middleware.esoe.sso.bean.impl.SSOProcessorDataImpl;
import com.qut.middleware.esoe.sso.exception.InvalidRequestException;
import com.qut.middleware.esoe.sso.exception.InvalidSessionIdentifierException;
import com.qut.middleware.saml2.BindingConstants;

/**
 * Control point for SSO module, SPEP session establishment and authentication network wide single logout.
 */

public class SSOServlet extends HttpServlet
{
	private static final long serialVersionUID = 4083024809106578744L;

	private final String SAML_REQUEST_ELEMENT = "SAMLRequest"; //$NON-NLS-1$
	private final String SAML_REQUEST_ENCODING = "SAMLEncoding"; //$NON-NLS-1$
	private final String SAML_RELAY_STATE = "RelayState"; //$NON-NLS-1$
	private final String SAML_SIG_ALGORITHM = "SigAlg"; //$NON-NLS-1$
	private final String SAML_REQUEST_SIGNATURE = "Signature"; //$NON-NLS-1$

	private final String SAML_RESPONSE_TEMPLATE = "samlResponseTemplate.html"; //$NON-NLS-1$

	private MessageFormat samlMessageFormat;
	protected SSOProcessor ssoProcessor;
	protected String sessionTokenName, commonDomainTokenName, authnRedirectURL, authnDynamicURLParam, ssoURL,
			sessionDomain, commonDomain;

	private final String samlResponseTemplate;

	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(SSOServlet.class.getName());

	/**
	 * Generic constructor for SSOServlet
	 */
	public SSOServlet()
	{
		try
		{
			URL location = SSOServlet.class.getResource(this.SAML_RESPONSE_TEMPLATE);

			if (location == null)
				throw new IllegalArgumentException(Messages.getString("SSOServlet.3")); //$NON-NLS-1$

			this.samlResponseTemplate = FileCopyUtils.copyToString(new InputStreamReader(location.openStream()));
			this.samlMessageFormat = new MessageFormat(this.samlResponseTemplate);
		}
		catch (FileNotFoundException e)
		{
			throw new IllegalArgumentException(Messages.getString("SSOServlet.4")); //$NON-NLS-1$
		}
		catch (IOException e)
		{
			throw new IllegalArgumentException(Messages.getString("SSOServlet.5")); //$NON-NLS-1$
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

		this.logger.debug(Messages.getString("SSOServlet.0")); //$NON-NLS-1$
		data = (SSOProcessorData) request.getSession().getAttribute(SSOProcessorData.SESSION_NAME);

		/*
		 * If this is a request due to previous allow of ForceAuthn by spep then retrieve details from session and
		 * continue
		 */
		if (data != null)
		{
			this.logger.debug(Messages.getString("SSOServlet.30")); //$NON-NLS-1$
			data.setHttpRequest(request);
			data.setHttpResponse(response);

			processCookies(request, data);

			/*
			 * Set the data value of returning request to be true to prevent additional processing by Authentication
			 * Authority
			 */
			data.setReturningRequest(true);

			processRequest(request, response, data);
		}
		else
		{
			/* Determine if this is a HTTP Redirect Binding SAML AuthnRequest */
			String samlRequest = request.getParameter(this.SAML_REQUEST_ELEMENT);
			String relayState = request.getParameter(this.SAML_RELAY_STATE);
			String encoding = request.getParameter(this.SAML_REQUEST_ENCODING);
			String sigAlg = request.getParameter(this.SAML_SIG_ALGORITHM);
			String signature = request.getParameter(this.SAML_REQUEST_SIGNATURE);

			if (samlRequest != null && samlRequest.length() > 0)
			{
				this.logger.debug(Messages.getString("SSOServlet.33")); //$NON-NLS-1$
				data = new SSOProcessorDataImpl();

				data.setSamlBinding(BindingConstants.httpRedirect);
				data.setRequestDocument(samlRequest.getBytes());
				data.setRelayState(relayState);
				data.setSamlEncoding(encoding);
				data.setSigAlg(sigAlg);
				data.setSignature(signature);

				data.setHttpRequest(request);
				data.setHttpResponse(response);

				processCookies(request, data);

				processRequest(request, response, data);
			}
			else
			{
				/* Nothing we support just redirect to the default authentication service */
				this.logger.debug(Messages.getString("SSOServlet.31")); //$NON-NLS-1$

				/* All other get requests are forced to login portal */
				response.sendRedirect(this.authnRedirectURL);
			}
		}
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
		/* Determine if this is a HTTP Post binding SAML AuthnRequest */
		SSOProcessorData data;
		String samlRequest;
		String relayState;

		this.logger.debug(Messages.getString("SSOServlet.32")); //$NON-NLS-1$

		data = new SSOProcessorDataImpl();

		samlRequest = request.getParameter(this.SAML_REQUEST_ELEMENT);
		relayState = request.getParameter(this.SAML_RELAY_STATE);

		if (samlRequest == null)
		{
			this.logger.info(Messages.getString("SSOServlet.34")); //$NON-NLS-1$
			generateErrorResponse(response, Messages.getString("SSOServlet.1")); //$NON-NLS-1$
			return;
		}

		this.logger.debug(Messages.getString("SSOServlet.33")); //$NON-NLS-1$
		data.setSamlBinding(BindingConstants.httpPost);
		data.setRequestDocument(samlRequest.getBytes());
		data.setRelayState(relayState);

		data.setHttpRequest(request);
		data.setHttpResponse(response);

		processCookies(request, data);
		
		processRequest(request, response, data);
	}

	/**
	 * Removes data stored in the session that is no longer required after various processing completes
	 * 
	 * @param request
	 *            Current request object
	 */
	private void cleanSessionState(HttpServletRequest request)
	{
		this.logger.debug(Messages.getString("SSOServlet.38")); //$NON-NLS-1$
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
		this.logger.warn(Messages.getString("SSOServlet.39") + message); //$NON-NLS-1$
		try
		{
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
		}
		catch (IOException e)
		{
			this.logger.error(Messages.getString("SSOServlet.40") + e.getLocalizedMessage()); //$NON-NLS-1$
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
	private void generateForceAuthnResponse(HttpServletRequest request, HttpServletResponse response, SSOProcessorData data)
	{
		try
		{
			byte[] encodedURL;
			String encodedURLString;

			this.logger.debug(Messages.getString("SSOServlet.41")); //$NON-NLS-1$

			/* Set this request to returning before storing */
			data.setReturningRequest(true);
			request.getSession().setAttribute(SSOProcessorData.SESSION_NAME, data);

			this.logger.debug(Messages.getString("SSOServlet.42") + this.ssoURL + Messages.getString("SSOServlet.43")); //$NON-NLS-1$ //$NON-NLS-2$

			/* Base64 encode dynamic redirect URL */
			encodedURL = Base64.encodeBase64(this.ssoURL.getBytes("UTF-16")); //$NON-NLS-1$
			encodedURLString = new String(encodedURL);

			response.sendRedirect(this.authnRedirectURL + "?" + this.authnDynamicURLParam + "=" + encodedURLString); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch (IOException e)
		{
			this.logger.warn(Messages.getString("SSOServlet.44") + e.getLocalizedMessage()); //$NON-NLS-1$
			generateErrorResponse(response, Messages.getString("SSOServlet.12")); //$NON-NLS-1$
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
			String responseRelayState;

			response.setContentType("text/html");

			/* Set cookie to allow javascript enabled browsers to autosubmit, ensures navigation with the back button is not broken because auto submit is active for only a very short period */
			Cookie autoSubmit = new Cookie("esoeAutoSubmit", "enabled");
			autoSubmit.setMaxAge(172800); //set expiry to be 48 hours just to make sure we still work with badly configured clocks skewed from GMT
			autoSubmit.setPath("/");
			response.addCookie(autoSubmit);

			this.logger.debug(Messages.getString("SSOServlet.45")); //$NON-NLS-1$

			if (data.getResponseDocument() == null)
			{
				this.logger.debug(Messages.getString("SSOServlet.46")); //$NON-NLS-1$
				generateErrorResponse(response, Messages.getString("SSOServlet.10")); //$NON-NLS-1$
			}

			this.logger.trace(Messages.getString("SSOServlet.47") + data.getResponseDocument()); //$NON-NLS-1$

			responseRelayState = data.getRelayState();
			if (responseRelayState == null)
				responseRelayState = new String("");

			/* Encode SAML Response in base64 */
			samlResponseEncoded = Base64.encodeBase64(data.getResponseDocument()); //$NON-NLS-1$
			responseArgs = new Object[] { data.getResponseEndpoint(), new String(samlResponseEncoded), responseRelayState };
			htmlOutput = this.samlMessageFormat.format(responseArgs);

			this.logger.trace(Messages.getString("SSOServlet.48") + htmlOutput); //$NON-NLS-1$

			writer.print(htmlOutput);
			writer.flush();
		}
		catch (UnsupportedEncodingException de)
		{
			this.logger.warn(Messages.getString("SSOServlet.49") + de.getLocalizedMessage()); //$NON-NLS-1$
			generateErrorResponse(response, Messages.getString("SSOServlet.11")); //$NON-NLS-1$
		}
		catch (IOException ioe)
		{
			this.logger.warn(Messages.getString("SSOServlet.50") + ioe.getLocalizedMessage()); //$NON-NLS-1$
			generateErrorResponse(response, Messages.getString("SSOServlet.12")); //$NON-NLS-1$
		}
	}

	/**
	 * Clears a provided session identifying cookie when some invalid value has been presented
	 * 
	 * @param data
	 *            Local request AuthnProcessoreData bean
	 */
	private void clearSessionCookie(SSOProcessorData data)
	{
		logger.debug("{} Clearing esoe session cookie {}" , data.getHttpRequest().getRemoteAddr(), sessionTokenName);
		
		/* Remove the value of the users session cookie at the ESOE */
		Cookie sessionCookie = new Cookie(this.sessionTokenName, ""); //$NON-NLS-1$
		sessionCookie.setDomain(this.sessionDomain);
		sessionCookie.setSecure(false);
		sessionCookie.setMaxAge(0);
		sessionCookie.setPath("/");
		data.getHttpResponse().addCookie(sessionCookie);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init() throws ServletException
	{
		FileInputStream configFile;
		Properties props;

		try
		{
			configFile = new FileInputStream(System.getProperty("esoe.data") + ConfigurationConstants.ESOE_CONFIG);
			props = new java.util.Properties();

			props.load(configFile);

			/* Spring integration to make our servlet aware of IoC */
			WebApplicationContext webAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());

			this.ssoProcessor = (SSOProcessor) webAppContext.getBean(ConfigurationConstants.SSO_PROCESSOR, com.qut.middleware.esoe.sso.SSOProcessor.class);

			if (this.ssoProcessor == null)
				throw new IllegalArgumentException(Messages.getString("SSOServlet.2") + ConfigurationConstants.SSO_PROCESSOR); //$NON-NLS-1$

			this.sessionTokenName = props.getProperty(ConfigurationConstants.ESOE_SESSION_TOKEN_NAME);
			this.commonDomainTokenName = props.getProperty(ConfigurationConstants.COMMON_DOMAIN_TOKEN_NAME);
			this.authnRedirectURL = props.getProperty(ConfigurationConstants.AUTHN_REDIRECT_URL);
			this.authnDynamicURLParam = props.getProperty(ConfigurationConstants.AUTHN_DYNAMIC_URL_PARAM);
			this.ssoURL = props.getProperty(ConfigurationConstants.SSO_URL);
			this.sessionDomain = props.getProperty(ConfigurationConstants.ESOE_SESSION_DOMAIN);
			this.commonDomain = props.getProperty(ConfigurationConstants.COMMON_DOMAIN);

			if (this.sessionTokenName == null)
				throw new IllegalArgumentException(Messages.getString("SSOServlet.13") //$NON-NLS-1$
						+ ConfigurationConstants.ESOE_SESSION_TOKEN_NAME + Messages.getString("SSOServlet.14")); //$NON-NLS-1$

			if (this.authnRedirectURL == null)
				throw new IllegalArgumentException(Messages.getString("SSOServlet.15") //$NON-NLS-1$
						+ ConfigurationConstants.AUTHN_REDIRECT_URL + Messages.getString("SSOServlet.16")); //$NON-NLS-1$

			if (this.authnDynamicURLParam == null)
				throw new IllegalArgumentException(Messages.getString("SSOServlet.17") //$NON-NLS-1$
						+ ConfigurationConstants.AUTHN_DYNAMIC_URL_PARAM + Messages.getString("SSOServlet.18")); //$NON-NLS-1$

			if (this.ssoURL == null)
				throw new IllegalArgumentException(Messages.getString("SSOServlet.19") //$NON-NLS-1$
						+ ConfigurationConstants.SSO_URL + Messages.getString("SSOServlet.20")); //$NON-NLS-1$

			if (this.sessionDomain == null)
				throw new IllegalArgumentException(Messages.getString("SSOServlet.19") //$NON-NLS-1$
						+ ConfigurationConstants.ESOE_SESSION_DOMAIN + Messages.getString("SSOServlet.20")); //$NON-NLS-1$

			if (this.commonDomainTokenName == null)
				throw new IllegalArgumentException(Messages.getString("SSOServlet.19") //$NON-NLS-1$
						+ ConfigurationConstants.COMMON_DOMAIN_TOKEN_NAME + Messages.getString("SSOServlet.20")); //$NON-NLS-1$

			if (this.commonDomain == null)
				throw new IllegalArgumentException(Messages.getString("SSOServlet.19") //$NON-NLS-1$
						+ ConfigurationConstants.COMMON_DOMAIN + Messages.getString("SSOServlet.20")); //$NON-NLS-1$

			this.logger.info(Messages.getString("SSOServlet.51") + this.sessionTokenName //$NON-NLS-1$
					+ Messages.getString("SSOServlet.52") + this.authnRedirectURL + Messages.getString("SSOServlet.53") //$NON-NLS-1$ //$NON-NLS-2$
					+ this.authnDynamicURLParam + Messages.getString("SSOServlet.54") + this.ssoURL); //$NON-NLS-1$
		}
		catch (BeansException e)
		{
			this.logger.error(Messages.getString("SSOServlet.55") + e.getLocalizedMessage()); //$NON-NLS-1$
			throw new ServletException(Messages.getString("SSOServlet.21") //$NON-NLS-1$
					+ ConfigurationConstants.SSO_PROCESSOR + Messages.getString("SSOServlet.22")); //$NON-NLS-1$
		}
		catch (MalformedURLException e)
		{
			throw new ServletException(Messages.getString("SSOServlet.23") //$NON-NLS-1$
					+ ConfigurationConstants.ESOE_CONFIG + Messages.getString("SSOServlet.24")); //$NON-NLS-1$
		}
		catch (IllegalStateException e)
		{
			this.logger.error(Messages.getString("SSOServlet.56") + e.getLocalizedMessage()); //$NON-NLS-1$
			throw new ServletException(Messages.getString("SSOServlet.25")); //$NON-NLS-1$
		}
		catch (IOException e)
		{
			this.logger.error(Messages.getString("SSOServlet.57") + e.getLocalizedMessage()); //$NON-NLS-1$
			throw new ServletException(Messages.getString("SSOServlet.26") //$NON-NLS-1$
					+ ConfigurationConstants.ESOE_CONFIG + Messages.getString("SSOServlet.27")); //$NON-NLS-1$
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
	protected void processCookies(HttpServletRequest request, SSOProcessorData data)
	{
		Cookie[] cookies;

		/* Set the value of sessionID for processors */
		cookies = request.getCookies();
		if (cookies != null)
		{
			this.logger.debug("{} - Processing cookies presented in the request", data.getHttpRequest().getRemoteAddr());
			for (Cookie cookie : cookies)
			{
				this.logger.debug(Messages.getString("SSOServlet.59") + cookie.getName() + Messages.getString("SSOServlet.60") + cookie.getValue()); //$NON-NLS-1$ //$NON-NLS-2$
				if (cookie.getName().equals(this.sessionTokenName))
					data.setSessionID(cookie.getValue());
			}
		}
	}

	/**
	 * Sets the common domain cookie
	 * 
	 * @param data
	 */
	private void setCommonCookie(SSOProcessorData data)
	{
		Cookie commonDomainCookie = new Cookie(this.commonDomainTokenName, data.getCommonCookieValue());
		commonDomainCookie.setDomain(this.commonDomain);
		commonDomainCookie.setMaxAge(-1); // negative indicates session scope cookie
		commonDomainCookie.setPath("/");

		data.getHttpResponse().addCookie(commonDomainCookie);
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
	protected void processRequest(HttpServletRequest request, HttpServletResponse response, SSOProcessorData data)
	{

		SSOProcessor.result result;

		try
		{
			/* Hand logic processing to the Authentication Authority */
			result = this.ssoProcessor.execute(data);
			this.logger.debug(Messages.getString("SSOServlet.61") + result.toString()); //$NON-NLS-1$

			switch (result)
			{
				case SSOGenerationSuccessful:
					setCommonCookie(data);
					cleanSessionState(request);
					generateResponse(response, data);
					break;
				case ForcePassiveAuthn:
					cleanSessionState(request);
					this.clearSessionCookie(data);
					generateResponse(response, data);
					break;
				case ForceAuthn:
					this.clearSessionCookie(data);
					generateForceAuthnResponse(request, response, data);
					break;
			}
		}
		catch (InvalidSessionIdentifierException isie)
		{
			this.logger.warn(Messages.getString("SSOServlet.62")); //$NON-NLS-1$
			this.clearSessionCookie(data);

			if (data.getResponseDocument() != null)
				generateResponse(response, data);
			else
				generateErrorResponse(response, Messages.getString("SSOServlet.28")); //$NON-NLS-1$
		}
		catch (InvalidRequestException ire)
		{
			this.logger.warn(Messages.getString("SSOServlet.63")); //$NON-NLS-1$
			this.clearSessionCookie(data);

			if (data.getResponseDocument() != null)
				generateResponse(response, data);
			else
				generateErrorResponse(response, Messages.getString("SSOServlet.29")); //$NON-NLS-1$
		}
	}
}
