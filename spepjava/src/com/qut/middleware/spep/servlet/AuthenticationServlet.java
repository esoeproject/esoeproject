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

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.saml2.BindingConstants;
import com.qut.middleware.spep.Initializer;
import com.qut.middleware.spep.SPEP;
import com.qut.middleware.spep.authn.AuthnProcessorData;
import com.qut.middleware.spep.authn.bindings.AuthnBinding;
import com.qut.middleware.spep.authn.bindings.AuthnBindingProcessor;
import com.qut.middleware.spep.authn.impl.AuthnProcessorDataImpl;
import com.qut.middleware.spep.exception.AuthenticationException;

/**
 * Implements a servlet for authentication operations over the SAML HTTP POST binding.
 */
public class AuthenticationServlet extends HttpServlet
{
	private static final String HTTP_GET_METHOD = "GET";
	private static final String HTTP_POST_METHOD = "POST";
	
	private static final long serialVersionUID = 7156272888750450687L;
	private static final int BUFFER_LEN = 4096;
	private SPEP spep;
	private boolean initDone = false;
	private static final String IMPLEMENTED_BINDING = BindingConstants.httpPost;
	private static final String AUTHNPROCESSOR_DATA = "com.qut.middleware.spep.authn.authnProcessorData";

	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(AuthenticationServlet.class.getName());

	/**
	 * 
	 */
	public AuthenticationServlet() throws IOException
	{
		super();
		this.initDone = false;
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
		this.doRequest(request, response);
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		this.doRequest(request, response);
	}
	
	private void doRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		initSPEP();

		// Ensure SPEP startup.
		if (!this.spep.isStarted())
		{
			// Don't allow anything to occur if SPEP hasn't started correctly.
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			throw new ServletException("Unable to process authentication because this service has not yet been started successfully.");
		}
		
		try
		{
			AuthnBindingProcessor authnBindingProcessor = this.spep.getAuthnBindingProcessor();
			
			AuthnProcessorData data = (AuthnProcessorData)request.getSession().getAttribute(AUTHNPROCESSOR_DATA);
			if (data == null)
			{
				// Initialize the AuthnProcessorData
				data = new AuthnProcessorDataImpl();
				data.setSSORequestServerName(request.getServerName());
				data.setSSORequestURI(request.getRequestURL().toString());
				request.getSession().setAttribute(AUTHNPROCESSOR_DATA, data);
				
				// New Authn event, set up data bean and decide on a binding to use.
				AuthnBinding authnBinding = authnBindingProcessor.chooseBinding(request);
				data.setBindingIdentifier(authnBinding.getBindingIdentifier());
				
				authnBinding.handleRequest(request, response, data, this.spep);
			}
			else
			{
				// Already initialized, set it as a returning request.
				data.setReturningRequest();
				
				AuthnBinding authnBinding = authnBindingProcessor.getBinding(data.getBindingIdentifier());
				authnBinding.handleRequest(request, response, data, this.spep);
			}
		}
		catch (AuthenticationException e)
		{
			String errorUID = Long.toHexString(System.currentTimeMillis());
			this.logger.error("{} Authentication exception while processing Authn event. Exception was: {}", errorUID, e.getMessage());
			
			request.getSession().invalidate();
			throw new ServletException(errorUID + " An error occurred while processing authentication for your session.");
		}
	}

}
