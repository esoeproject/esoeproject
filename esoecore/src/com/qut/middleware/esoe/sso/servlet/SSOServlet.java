/* 
 * Copyright 2006-2008, Queensland University of Technology
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

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.sso.SSOProcessor;
import com.qut.middleware.esoe.sso.SSOProcessor.result;
import com.qut.middleware.esoe.sso.bean.SSOProcessorData;
import com.qut.middleware.esoe.sso.bean.SSOProcessorData.RequestMethod;
import com.qut.middleware.esoe.sso.bean.impl.SSOProcessorDataImpl;
import com.qut.middleware.esoe.sso.exception.SSOException;
import com.qut.middleware.esoe.util.CalendarUtils;

/**
 * Control point for SSO module, SPEP session establishment and authentication network wide single logout.
 */

public class SSOServlet extends HttpServlet
{
	private static final long serialVersionUID = 4083024809106578744L;

	protected SSOProcessor ssoProcessor;

	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(SSOServlet.class.getName());

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		this.doRequest(request, response, RequestMethod.HTTP_GET);
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
		this.doRequest(request, response, RequestMethod.HTTP_POST);
	}
	
	private void doRequest(HttpServletRequest request, HttpServletResponse response, RequestMethod method) throws ServletException, IOException
	{
		SSOProcessorData data;

		data = (SSOProcessorData) request.getSession().getAttribute(SSOProcessorData.SESSION_NAME);
		
		String remoteAddress = request.getRemoteAddr();
		
		this.logger.debug("[SSO for {}] SSOServlet got {} request. SSO processor data element was {}", new Object[]{
				remoteAddress,
				method.toString(),
				data == null ? "null" : "not null"
		});
		
		if (data == null)
		{
			data = new SSOProcessorDataImpl();
			request.getSession().setAttribute(SSOProcessorData.SESSION_NAME, data);
		}
		
		data.setHttpRequest(request);
		data.setHttpResponse(response);
		data.setRequestMethod(method);
		
		String oldRemoteAddress = data.getRemoteAddress();
		if (oldRemoteAddress != null)
		{
			if (!oldRemoteAddress.equals(remoteAddress))
			{
				this.logger.warn("[SSO for {}] IP address changed. Old address was: {}", remoteAddress, oldRemoteAddress);
			}
		}
		
		data.setRemoteAddress(remoteAddress);

		try
		{
			SSOProcessor.result result = this.ssoProcessor.execute(data);
			this.logger.debug("[SSO for {}] SSOProcessor returned a result of {}", new Object[]{remoteAddress, String.valueOf(result)});
		}
		catch (SSOException e)
		{
			if (!data.isResponded())
			{
				InetAddress inetAddress = Inet4Address.getByName(remoteAddress);
				String code = CalendarUtils.generateXMLCalendar().toString() + "-" + new String(Hex.encodeHex(inetAddress.getAddress()));
				
				this.logger.error("[SSO for {}] {} Error occurred in SSOServlet.doPost. Exception was: {}", code, e.getMessage());
				this.logger.debug(code + " Error occurred in SSOServlet.doPost. Exception follows", e);
				throw new ServletException("An error occurred during the sign-on process, and the session could not be established. Instance error is: " + code);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init() throws ServletException
	{
		try
		{
			/* Spring integration to make our servlet aware of IoC */
			WebApplicationContext webAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());

			this.ssoProcessor = (SSOProcessor) webAppContext.getBean(ConfigurationConstants.SSO_PROCESSOR, com.qut.middleware.esoe.sso.SSOProcessor.class);

			if (this.ssoProcessor == null)
				throw new IllegalArgumentException("Unable to acquire SSO Processor bean from web application context. Missing bean for name: " + ConfigurationConstants.SSO_PROCESSOR);
		}
		catch (BeansException e)
		{
			this.logger.error("Unable to acquire SSO Processor bean from web application context. Error retrieving bean for name: " + e.getLocalizedMessage());
			throw new ServletException("Unable to acquire SSO Processor bean from web application context. Error retrieving bean for name: " + ConfigurationConstants.SSO_PROCESSOR, e);
		}
		catch (IllegalStateException e)
		{
			this.logger.error("Unable to acquire SSO Processor bean from web application context. Currently in an illegal state for retrieving beans.", e);
			throw new ServletException("Unable to acquire SSO Processor bean from web application context. Currently in an illegal state for retrieving beans.", e);
		}
	}
}
