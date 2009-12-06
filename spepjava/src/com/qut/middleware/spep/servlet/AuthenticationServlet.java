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
 * Creation Date: 27/11/2006
 * 
 * Purpose: Implements a servlet for authentication operations over the SAML
 * 		HTTP POST binding.
 */
package com.qut.middleware.spep.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.text.MessageFormat;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.metadata.bean.saml.IdentityProviderRole;
import com.qut.middleware.metadata.bean.saml.TrustedESOERole;
import com.qut.middleware.metadata.exception.MetadataStateException;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.saml2.BindingConstants;
import com.qut.middleware.spep.Initializer;
import com.qut.middleware.spep.SPEP;
import com.qut.middleware.spep.authn.AuthnProcessorData;
import com.qut.middleware.spep.authn.impl.AuthnProcessorDataImpl;
import com.qut.middleware.spep.authn.impl.AuthnProcessorImpl;
import com.qut.middleware.spep.exception.AuthenticationException;

/**
 * Implements a servlet for authentication operations over the SAML HTTP POST binding.
 */
public class AuthenticationServlet extends HttpServlet
{
	private static final long serialVersionUID = 7156272888750450687L;
	private static final int BUFFER_LEN = 4096;
	private SPEP spep;
	private boolean initDone = false;
	private MessageFormat samlMessageFormat;
	private static final String IMPLEMENTED_BINDING = BindingConstants.httpPost;

	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(AuthenticationServlet.class.getName());

	/**
	 * 
	 */
	public AuthenticationServlet() throws IOException
	{
		super();
		this.initDone = false;

		this.logger.debug("Loading response template from jar");
		
		InputStream inputStream = this.getClass().getResourceAsStream("samlRequestTemplate.html"); //$NON-NLS-1$
		InputStreamReader in = new InputStreamReader(inputStream);
		try
		{
			StringBuffer stringBuffer = new StringBuffer();
			char[] charBuffer = new char[AuthenticationServlet.BUFFER_LEN];

			while (in.read(charBuffer, 0, AuthenticationServlet.BUFFER_LEN) >= 0)
			{
				stringBuffer.append(charBuffer);
				charBuffer = new char[AuthenticationServlet.BUFFER_LEN];
			}

			this.samlMessageFormat = new MessageFormat(stringBuffer.toString());
		}
		finally
		{
			if (in != null)
				in.close();

			if (inputStream != null)
				inputStream.close();
		}
	}

	@Override
	public void init() throws ServletException
	{
		super.init();
		initSPEP();
	}

	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		initSPEP();
	}
	
	@Override
	public void destroy()
	{
		super.destroy();
		Initializer.cleanup( this.getServletContext() );
	}

	private synchronized void initSPEP() throws ServletException
	{
		if (this.initDone)
			return;

		ServletContext context = this.getServletConfig().getServletContext();

		try
		{
			this.spep = Initializer.init(context);
		}
		catch (Exception e)
		{
			this.logger.error("Initializer exception: " + e.getLocalizedMessage());
			throw new ServletException(e);
		}

		if (this.spep == null)
		{
			throw new ServletException(Messages.getString("AuthenticationServlet.0")); //$NON-NLS-1$
		}

		this.logger.debug(Messages.getString("AuthenticationServlet.15")); //$NON-NLS-1$

		this.initDone = true;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		initSPEP();

		// Ensure SPEP startup.
		if (!this.spep.isStarted())
		{
			// Don't allow anything to occur if SPEP hasn't started correctly.
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			this.logger.error(Messages.getString("AuthenticationServlet.16")); //$NON-NLS-1$
			throw new ServletException(Messages.getString("AuthenticationServlet.17")); //$NON-NLS-1$
		}

		try
		{
			this.logger.debug(Messages.getString("AuthenticationServlet.18")); //$NON-NLS-1$

			String document = buildAuthnRequestDocument(request.getParameter("redirectURL"), request, response); //$NON-NLS-1$
			PrintStream out = new PrintStream(response.getOutputStream());
			
			/* Set cookie to allow javascript enabled browsers to autosubmit, ensures navigation with the back button is not broken because auto submit is active for only a very short period */
			Cookie autoSubmit = new Cookie("spepAutoSubmit", "enabled");
			autoSubmit.setMaxAge(172800); //set expiry to be 48 hours just to make sure we still work with badly configured clocks skewed from GMT
			autoSubmit.setPath("/");
			response.addCookie(autoSubmit);

			response.setStatus(HttpServletResponse.SC_OK);
			response.setHeader("Content-Type", "text/html");

			out.print(document);

			out.close();
		}
		catch (AuthenticationException e)
		{
			this.logger.info(Messages.getString("AuthenticationServlet.2") + e.getLocalizedMessage()); //$NON-NLS-1$
			// TODO More descriptive browser output.
			throw new ServletException(e);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		URL serviceURL;
		initSPEP();

		// Ensure SPEP startup.
		if (!this.spep.isStarted())
		{
			// Don't allow anything to occur if SPEP hasn't started correctly.
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			throw new ServletException(Messages.getString("AuthenticationServlet.19")); //$NON-NLS-1$
		}

		String base64SAMLDocument = request.getParameter("SAMLResponse"); //$NON-NLS-1$

		if (base64SAMLDocument == null || base64SAMLDocument.length() == 0)
		{
			this.logger.info(Messages.getString("AuthenticationServlet.13")); //$NON-NLS-1$
			throw new ServletException(Messages.getString("AuthenticationServlet.14")); //$NON-NLS-1$
		}

		AuthnProcessorData data = new AuthnProcessorDataImpl();
		data.setRequest(request);
		data.setResponse(response);
		data.setResponseDocument(Base64.decodeBase64(base64SAMLDocument.getBytes()));

		this.logger.debug(Messages.getString("AuthenticationServlet.5")); //$NON-NLS-1$

		try
		{
			this.spep.getAuthnProcessor().processAuthnResponse(data);
		}
		catch (AuthenticationException e)
		{
			this.logger.info(Messages.getString("AuthenticationServlet.6") + e.getLocalizedMessage()); //$NON-NLS-1$
			throw new ServletException(e);
		}

		String sessionID = data.getSessionID();
		if (sessionID == null)
		{
			throw new ServletException(Messages.getString("AuthenticationServlet.7")); //$NON-NLS-1$
		}

		this.logger.debug(MessageFormat.format(Messages.getString("AuthenticationServlet.20"), sessionID)); //$NON-NLS-1$
		Cookie cookie = new Cookie(this.spep.getTokenName(), sessionID);

		cookie.setPath("/"); //$NON-NLS-1$
		response.addCookie(cookie);

		String base64RequestURL = data.getRequestURL();
		if (base64RequestURL != null)
		{
			String requestURL = new String(Base64.decodeBase64(base64RequestURL.getBytes()));

			this.logger.debug(MessageFormat.format(Messages.getString("AuthenticationServlet.21"), requestURL)); //$NON-NLS-1$
			response.sendRedirect(requestURL);
		}
		else
		{
			this.logger.debug(MessageFormat.format(Messages.getString("AuthenticationServlet.22"), this.spep.getDefaultUrl())); //$NON-NLS-1$
			response.sendRedirect(this.spep.getDefaultUrl());
		}
	}

	/*
	 * Builds string representation of an AuthnRequest to be sent to ESOE for principal authentication.
	 * 
	 */
	private String buildAuthnRequestDocument(String requestedURL, HttpServletRequest request, HttpServletResponse response) throws IOException, AuthenticationException
	{
		byte[] samlRequestEncoded;
		AuthnProcessorData data = new AuthnProcessorDataImpl();
		data.setRequest(request);
		data.setResponse(response);
		/* Base64 strings do not have spaces in them. So if one does, it means
		 * that something strange has happened to make the servlet engine translate
		 * the plus symbols into spaces. We just need to translate them back.
		 */
		requestedURL = requestedURL.replace(' ', '+');
		data.setRequestURL( requestedURL );
		
		this.logger.debug("RequestedURL: " + requestedURL);

		String ssoURL;
		try
		{
			MetadataProcessor metadataProcessor = this.spep.getMetadataProcessor();
			IdentityProviderRole identityProviderRole;
			if (this.spep.enableCompatibility())
			{
				identityProviderRole = metadataProcessor.getEntityRoleData(this.spep.getTrustedESOEIdentifier(), IdentityProviderRole.class);
			}
			else
			{
				identityProviderRole = metadataProcessor.getEntityRoleData(this.spep.getTrustedESOEIdentifier(), TrustedESOERole.class);
			}
			ssoURL = identityProviderRole.getSingleSignOnService(IMPLEMENTED_BINDING);
		}
		catch (MetadataStateException e)
		{
			throw new AuthenticationException("Authentication could not be completed because the metadata state is invalid. Exception was: " + e.getMessage(), e);
		}

		data.setDestinationURL(ssoURL);
		this.spep.getAuthnProcessor().generateAuthnRequest(data);

		samlRequestEncoded = Base64.encodeBase64(data.getRequestDocument());

		String base64SAMLDocument = new String(samlRequestEncoded); //$NON-NLS-1$
		this.logger.debug(Messages.getString("AuthenticationServlet.10")); //$NON-NLS-1$
		this.logger.debug("Using ssoURL of: " + ssoURL);
		this.logger.debug("Using samlDocument encode of: \n" + base64SAMLDocument);

		String document = this.samlMessageFormat.format(new Object[] { ssoURL, base64SAMLDocument });

		this.logger.debug(MessageFormat.format(Messages.getString("AuthenticationServlet.12"), Integer.valueOf(document.length()))); //$NON-NLS-1$
		this.logger.debug("Request document: \n" + document);

		return document;
	}
}
