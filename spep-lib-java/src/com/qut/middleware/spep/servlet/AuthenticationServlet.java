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
import java.nio.CharBuffer;
import java.text.MessageFormat;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.qut.middleware.spep.Initializer;
import com.qut.middleware.spep.SPEP;
import com.qut.middleware.spep.authn.AuthnProcessorData;
import com.qut.middleware.spep.authn.impl.AuthnProcessorDataImpl;
import com.qut.middleware.spep.authn.impl.AuthnProcessorImpl;
import com.qut.middleware.spep.exception.AuthenticationException;

/** Implements a servlet for authentication operations over the SAML
 * 		HTTP POST binding.*/
public class AuthenticationServlet extends HttpServlet
{
	private static final long serialVersionUID = 7156272888750450687L;
	private static final int BUFFER_LEN = 4096;
	private SPEP spep;
	private boolean initDone = false;
	
	/* Local logging instance */
	private Logger logger = Logger.getLogger(AuthnProcessorImpl.class.getName());

	/**
	 * 
	 */
	public AuthenticationServlet()
	{
		super();
		this.initDone = false;
	}
	
	private synchronized void initSPEP() throws ServletException
	{
		if (this.initDone) return;
		
		ServletContext context = this.getServletConfig().getServletContext();
		
		try
		{
			this.spep = Initializer.init(context);
		}
		catch (Exception e)
		{
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

			String document = buildAuthnRequestDocument(request.getParameter("redirectURL")); //$NON-NLS-1$
			PrintStream out = new PrintStream(response.getOutputStream());
			response.setStatus(HttpServletResponse.SC_OK);
			out.print(document);
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
		
		String samlDocument = new String(Base64.decodeBase64(base64SAMLDocument.getBytes("UTF-8"))); //$NON-NLS-1$
		
		AuthnProcessorData data = new AuthnProcessorDataImpl();
		data.setResponseDocument(samlDocument);
		
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
		cookie.setDomain(this.spep.getTokenDomain());
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
	
	/* Builds string representation of an AuthnRequest to be sent to ESOE for principal 
	 * authentication.
	 * 
	 */
	private String buildAuthnRequestDocument(String requestedURL) throws IOException, AuthenticationException
	{
		AuthnProcessorData data = new AuthnProcessorDataImpl();
		data.setRequestURL(requestedURL);
		
		this.spep.getAuthnProcessor().generateAuthnRequest(data);
		
		String requestDocument = data.getRequestDocument();

		String base64SAMLDocument = new String(Base64.encodeBase64(requestDocument.getBytes("UTF-8"))); //$NON-NLS-1$
		String ssoURL = this.spep.getMetadata().getSingleSignOnEndpoint();
		
		this.logger.debug(Messages.getString("AuthenticationServlet.10")); //$NON-NLS-1$
		
		InputStream inputStream = this.getClass().getResourceAsStream("samlRequestTemplate.html"); //$NON-NLS-1$
		InputStreamReader in = new InputStreamReader(inputStream);
		
		StringBuffer stringBuffer = new StringBuffer();
		CharBuffer charBuffer = CharBuffer.allocate(AuthenticationServlet.BUFFER_LEN);
		while (in.read(charBuffer) >= 0)
		{
			charBuffer.flip();
			stringBuffer.append(charBuffer.toString());
			charBuffer.clear();
		}
		
		String document = MessageFormat.format(stringBuffer.toString(), new Object[]{ssoURL, base64SAMLDocument});

		this.logger.debug(MessageFormat.format(Messages.getString("AuthenticationServlet.12"), Integer.valueOf(document.length()))); //$NON-NLS-1$
		
		return document;
	}
}
