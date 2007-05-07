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
 * Author:
 * Creation Date:
 * 
 * Purpose:
 */
package com.qut.middleware.esoe.authn.servlet;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.context.WebApplicationContext;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.authn.AuthnProcessor;
import com.qut.middleware.esoe.authn.bean.AuthnProcessorData;
import com.qut.middleware.esoe.authn.bean.impl.AuthnProcessorDataImpl;
import com.qut.middleware.esoe.authn.exception.AuthnFailureException;
import com.qut.middleware.test.Capture;

@SuppressWarnings(value = { "unqualified-field-access", "nls" })
public class AuthnServletTest
{
	AuthnServlet authnServlet;
	AuthnProcessor authnProcessor;
	AuthnProcessorData data;

	HttpServletRequest request;
	HttpServletResponse response;
	HttpSession session;
	ServletConfig servletConfig;
	ServletContext servletContext;
	WebApplicationContext webApplicationContext;

	// use this as esoe config file
	String configTestFile = "tests/testdata/test.config";
	
	Capture<String> captured;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		captured = new Capture<String>();

		authnProcessor = createMock(AuthnProcessor.class);
		request = createMock(HttpServletRequest.class);
		response = createMock(HttpServletResponse.class);
		session = createMock(HttpSession.class);
		servletConfig = createMock(ServletConfig.class);
		servletContext = createMock(ServletContext.class);
		webApplicationContext = createMock(WebApplicationContext.class);

		data = new AuthnProcessorDataImpl();
		data.setInvalidURL("http://esoe.qut.edu.au/logon/difficulties.do");
		data.setRedirectTarget("http://esoe.url/sso1");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception
	{
		// Not Implemented
	}

	private void setUpMock()
	{
		/* Start the replay for all our configured mock objects */
		replay(request);
		replay(response);
		replay(authnProcessor);
		replay(session);
		replay(servletConfig);
		replay(servletContext);
		replay(webApplicationContext);
	}

	private void tearDownMock()
	{
		/* Verify the mock responses */
		verify(request);
		verify(response);
		verify(authnProcessor);
		verify(session);
		verify(servletConfig);
		verify(servletContext);
		verify(webApplicationContext);
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.servlet.AuthnServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * Tests to ensure that the user is redirected to a URL when AuthnProcessor.result.Failure is returned
	 */
	@Test
	public void testDoGet1() throws IOException, ServletException, AuthnFailureException
	{
		Cookie[] cookies = new Cookie[5];

		Cookie random = new Cookie("RadomCookie", "itscandyireallyreallylikeit");
		Cookie random2 = new Cookie("RadomCookieName", "someothervalue");
		Cookie random3 = new Cookie("QUTCLIENT", "yourdeathwillcomesoonshitcookieandwhenitdoesitwillbeswift");

		cookies = new Cookie[] { random, random2, random3 };
		
		/* All of our expections for required mockups */
		expect(servletConfig.getServletContext()).andReturn(servletContext).atLeastOnce();
	
		File esoeConfigFile = new File(configTestFile);
		
		expect(servletContext.getResource(ConfigurationConstants.ESOE_CONFIG)).andReturn(new URL("file:" + esoeConfigFile.getAbsolutePath()));
	
		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("authnProcessor", com.qut.middleware.esoe.authn.AuthnProcessor.class))
				.andReturn(authnProcessor);

		expect(session.getAttribute("com.qut.middleware.esoe.authn.servlet.dynamicresponseurl")).andReturn(
				"http://esoe.url/sso");

		expect(request.getSession()).andReturn(session).anyTimes();
		expect(session.getAttribute(AuthnProcessorData.SESSION_NAME)).andReturn(data);
		expect(request.getCookies()).andReturn(cookies).anyTimes();
		session.setAttribute(eq(AuthnProcessorData.SESSION_NAME), notNull());
		expect(this.authnProcessor.execute((AuthnProcessorData) notNull())).andReturn(AuthnProcessor.result.Failure);
		response.sendRedirect(data.getRedirectTarget());
		setUpMock();

		authnServlet = new AuthnServlet();
		authnServlet.init(this.servletConfig);
		authnServlet.doGet(request, response);

		tearDownMock();
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.servlet.AuthnServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * Tests to ensure that the user is redirected to a URL when AuthnProcessor.result.Completed is returned
	 */
	@Test
	public void testDoGet1a() throws IOException, ServletException, AuthnFailureException
	{
		/* All of our expections for required mockups */
		expect(servletConfig.getServletContext()).andReturn(servletContext).atLeastOnce();
		expect(servletContext.getResource(ConfigurationConstants.ESOE_CONFIG)).andReturn(
				new URL("file:"+ new File(configTestFile).getAbsolutePath()) );
		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("authnProcessor", com.qut.middleware.esoe.authn.AuthnProcessor.class))
				.andReturn(authnProcessor);

		expect(session.getAttribute("com.qut.middleware.esoe.authn.servlet.dynamicresponseurl")).andReturn(
				"http://esoe.url/sso");

		expect(request.getSession()).andReturn(session).anyTimes();
		expect(session.getAttribute(AuthnProcessorData.SESSION_NAME)).andReturn(data);
		expect(request.getCookies()).andReturn(null).anyTimes();
		session.removeAttribute("com.qut.middleware.esoe.authn.servlet.dynamicresponseurl");
		expect(this.authnProcessor.execute((AuthnProcessorData) notNull())).andReturn(AuthnProcessor.result.Completed);
		response.addCookie((Cookie)notNull());
		response.sendRedirect("http://esoe.url/sso");
		session.removeAttribute("com.qut.middleware.esoe.authn.bean");
		setUpMock();

		authnServlet = new AuthnServlet();
		authnServlet.init(this.servletConfig);
		authnServlet.doGet(request, response);

		tearDownMock();
	}
	
	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.servlet.AuthnServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * Tests to ensure that the user is redirected to a URL when AuthnProcessor.result.Completed is returned and URL is not dynamic
	 */
	@Test
	public void testDoGet1b() throws IOException, ServletException, AuthnFailureException
	{
		/* All of our expections for required mockups */
		expect(servletConfig.getServletContext()).andReturn(servletContext).atLeastOnce();
		expect(servletContext.getResource(ConfigurationConstants.ESOE_CONFIG)).andReturn(
				new URL("file:"+ new File(configTestFile).getAbsolutePath()) );
		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("authnProcessor", com.qut.middleware.esoe.authn.AuthnProcessor.class))
				.andReturn(authnProcessor);

		expect(session.getAttribute("com.qut.middleware.esoe.authn.servlet.dynamicresponseurl")).andReturn(
				null);
		expect(request.getParameter((String)notNull())).andReturn(null);

		expect(request.getSession()).andReturn(session).anyTimes();
		expect(session.getAttribute(AuthnProcessorData.SESSION_NAME)).andReturn(data);
		expect(request.getCookies()).andReturn(null).anyTimes();
		expect(this.authnProcessor.execute((AuthnProcessorData) notNull())).andReturn(AuthnProcessor.result.Completed);
		response.addCookie((Cookie)notNull());
		response.sendRedirect(data.getRedirectTarget());
		session.removeAttribute("com.qut.middleware.esoe.authn.bean");
		setUpMock();

		authnServlet = new AuthnServlet();
		authnServlet.init(this.servletConfig);
		authnServlet.doGet(request, response);

		tearDownMock();
	}
	
	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.servlet.AuthnServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * Tests to ensure that the user is redirected to a URL when AuthnProcessor.result.Failure is returned and URL is not dynamic
	 */
	@Test
	public void testDoGetc() throws IOException, ServletException, AuthnFailureException
	{
		Cookie[] cookies = new Cookie[5];

		Cookie random = new Cookie("RadomCookie", "itscandyireallyreallylikeit");
		Cookie random2 = new Cookie("RadomCookieName", "someothervalue");
		Cookie random3 = new Cookie("QUTCLIENT", "yourdeathwillcomesoonshitcookieandwhenitdoesitwillbeswift");

		cookies = new Cookie[] { random, random2, random3 };
		
		/* All of our expections for required mockups */
		expect(servletConfig.getServletContext()).andReturn(servletContext).atLeastOnce();
		expect(servletContext.getResource(ConfigurationConstants.ESOE_CONFIG)).andReturn(
				new URL("file:"+ new File(configTestFile).getAbsolutePath()) );
		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("authnProcessor", com.qut.middleware.esoe.authn.AuthnProcessor.class))
				.andReturn(authnProcessor);
		expect(request.getParameter((String)notNull())).andReturn(null);
		expect(session.getAttribute("com.qut.middleware.esoe.authn.servlet.dynamicresponseurl")).andReturn(
				null);

		expect(request.getSession()).andReturn(session).anyTimes();
		expect(session.getAttribute(AuthnProcessorData.SESSION_NAME)).andReturn(data);
		expect(request.getCookies()).andReturn(cookies).anyTimes();
		session.setAttribute(eq(AuthnProcessorData.SESSION_NAME), notNull());
		expect(this.authnProcessor.execute((AuthnProcessorData) notNull())).andReturn(AuthnProcessor.result.Failure);
		response.sendRedirect(data.getRedirectTarget());
		setUpMock();

		authnServlet = new AuthnServlet();
		authnServlet.init(this.servletConfig);
		authnServlet.doGet(request, response);

		tearDownMock();
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.servlet.AuthnServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * Tests to ensure that Automated SSO service is disabled when appropriate cookie is passed
	 */
	@Test
	public void testDoGet2() throws IOException, ServletException, AuthnFailureException
	{
		Cookie[] cookies = new Cookie[5];

		Cookie random = new Cookie("RadomCookie", "itscandyireallyreallylikeit");
		Cookie random2 = new Cookie("RadomCookieName", "someothervalue");
		Cookie random3 = new Cookie("QUTCLIENT", "yourdeathwillcomesooncookieandwhenitdoesitwillbeswift");
		Cookie disableSSO = new Cookie("esoeNoAuto", "true");

		cookies = new Cookie[] { random, random2, random3, disableSSO };

		/* All of our expections for required mockups */
		expect(servletConfig.getServletContext()).andReturn(servletContext).atLeastOnce();
		expect(servletContext.getResource(ConfigurationConstants.ESOE_CONFIG)).andReturn(
				new URL("file:"+ new File(configTestFile).getAbsolutePath()) );
		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("authnProcessor", com.qut.middleware.esoe.authn.AuthnProcessor.class))
				.andReturn(authnProcessor);

		expect(session.getAttribute("com.qut.middleware.esoe.authn.servlet.dynamicresponseurl")).andReturn(
				"http://esoe.url/sso");
		session.removeAttribute("com.qut.middleware.esoe.authn.servlet.dynamicresponseurl");
		
		expect(request.getSession()).andReturn(session).anyTimes();
		expect(session.getAttribute(AuthnProcessorData.SESSION_NAME)).andReturn(data);
		expect(request.getCookies()).andReturn(cookies).anyTimes();
		expect(this.authnProcessor.execute((AuthnProcessorData) notNull())).andReturn(AuthnProcessor.result.Completed);
		response.addCookie((Cookie)notNull());
		response.sendRedirect("http://esoe.url/sso");
		session.removeAttribute("com.qut.middleware.esoe.authn.bean");
		setUpMock();

		authnServlet = new AuthnServlet();
		authnServlet.init(this.servletConfig);
		authnServlet.doGet(request, response);

		assertFalse("Automatic SSO must be false", data.getAutomatedSSO());

		tearDownMock();
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.servlet.AuthnServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * Tests to ensure that the user is redirected to correct URL when AuthnProcessor.result.Failure is returned
	 */
	@Test
	public void testDoPost1() throws IOException, ServletException, AuthnFailureException
	{
		/* All of our expections for required mockups */
		expect(servletConfig.getServletContext()).andReturn(servletContext).atLeastOnce();
		expect(servletContext.getResource(ConfigurationConstants.ESOE_CONFIG)).andReturn(
				new URL("file:"+ new File(configTestFile).getAbsolutePath()) );
		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("authnProcessor", com.qut.middleware.esoe.authn.AuthnProcessor.class))
				.andReturn(authnProcessor);

		expect(session.getAttribute("com.qut.middleware.esoe.authn.servlet.dynamicresponseurl")).andReturn(
				"http://esoe.url/sso");

		expect(request.getSession()).andReturn(session).anyTimes();
		session.setAttribute(eq(AuthnProcessorData.SESSION_NAME), notNull());
		expect(session.getAttribute(AuthnProcessorData.SESSION_NAME)).andReturn(data);
		expect(request.getCookies()).andReturn(null).anyTimes();
		expect(this.authnProcessor.execute((AuthnProcessorData) notNull())).andReturn(AuthnProcessor.result.Failure);
		response.sendRedirect(data.getRedirectTarget());
		setUpMock();

		authnServlet = new AuthnServlet();
		authnServlet.init(this.servletConfig);
		authnServlet.doPost(request, response);

		tearDownMock();
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.servlet.AuthnServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * Tests to ensure that the user is redirected to correct URL when AuthnProcessor.result.Completed is returned
	 */
	@Test
	public void testDoPost1a() throws IOException, ServletException, AuthnFailureException
	{
		Cookie[] cookies = new Cookie[5];

		Cookie random = new Cookie("RadomCookie", "itscandyireallyreallylikeit");
		Cookie random2 = new Cookie("RadomCookieName", "someothervalue");
		Cookie random3 = new Cookie("QUTCLIENT", "yourdeathwillcomesooncookieandwhenitdoesitwillbeswift");

		cookies = new Cookie[] { random, random2, random3 };

		/* All of our expections for required mockups */
		expect(servletConfig.getServletContext()).andReturn(servletContext).atLeastOnce();
		expect(servletContext.getResource(ConfigurationConstants.ESOE_CONFIG)).andReturn(
				new URL("file:"+ new File(configTestFile).getAbsolutePath()) );
		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("authnProcessor", com.qut.middleware.esoe.authn.AuthnProcessor.class))
				.andReturn(authnProcessor);

		expect(session.getAttribute("com.qut.middleware.esoe.authn.servlet.dynamicresponseurl")).andReturn(
				"http://esoe.url/sso");
		session.removeAttribute("com.qut.middleware.esoe.authn.servlet.dynamicresponseurl");
		
		expect(request.getSession()).andReturn(session).anyTimes();
		expect(session.getAttribute(AuthnProcessorData.SESSION_NAME)).andReturn(data);
		expect(request.getCookies()).andReturn(cookies).anyTimes();
		expect(this.authnProcessor.execute((AuthnProcessorData) notNull())).andReturn(AuthnProcessor.result.Completed);
		response.addCookie((Cookie)notNull());
		response.sendRedirect("http://esoe.url/sso");
		session.removeAttribute("com.qut.middleware.esoe.authn.bean");
		setUpMock();

		authnServlet = new AuthnServlet();
		authnServlet.init(this.servletConfig);
		authnServlet.doPost(request, response);

		tearDownMock();
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.servlet.AuthnServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * Tests to ensure that the user is redirected to correct URL when AuthnProcessor.result.UserAgent is returned
	 */
	@Test
	public void testDoPost1b() throws IOException, ServletException, AuthnFailureException
	{
		Cookie[] cookies = new Cookie[5];

		Cookie random = new Cookie("RadomCookie", "itscandyireallyreallylikeit");
		Cookie random2 = new Cookie("RadomCookieName", "someothervalue");
		Cookie random3 = new Cookie("QUTCLIENT", "yourdeathwillcomesooncookieandwhenitdoesitwillbeswift");

		cookies = new Cookie[] { random, random2, random3 };

		/* All of our expections for required mockups */
		expect(servletConfig.getServletContext()).andReturn(servletContext).atLeastOnce();
		expect(servletContext.getResource(ConfigurationConstants.ESOE_CONFIG)).andReturn(
				new URL("file:"+ new File(configTestFile).getAbsolutePath()) );
		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("authnProcessor", com.qut.middleware.esoe.authn.AuthnProcessor.class))
				.andReturn(authnProcessor);

		expect(session.getAttribute("com.qut.middleware.esoe.authn.servlet.dynamicresponseurl")).andReturn(
				"http://esoe.url/sso");

		expect(request.getSession()).andReturn(session).anyTimes();
		expect(session.getAttribute(AuthnProcessorData.SESSION_NAME)).andReturn(data);
		session.setAttribute(eq(AuthnProcessorData.SESSION_NAME), notNull());
		expect(request.getCookies()).andReturn(cookies).anyTimes();
		expect(this.authnProcessor.execute((AuthnProcessorData) notNull())).andReturn(AuthnProcessor.result.UserAgent);
		response.sendRedirect(data.getRedirectTarget());
		setUpMock();

		authnServlet = new AuthnServlet();
		authnServlet.init(this.servletConfig);
		authnServlet.doPost(request, response);

		tearDownMock();
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.servlet.AuthnServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * Tests to ensure that the user is redirected to correct URL when AuthnProcessor.result.Invalid is returned
	 */
	@Test
	public void testDoPost1c() throws IOException, ServletException, AuthnFailureException
	{
		Cookie[] cookies = new Cookie[5];

		Cookie random = new Cookie("RadomCookie", "itscandyireallyreallylikeit");
		Cookie random2 = new Cookie("RadomCookieName", "someothervalue");
		Cookie random3 = new Cookie("QUTCLIENT", "yourdeathwillcomesooncookieandwhenitdoesitwillbeswift");

		cookies = new Cookie[] { random, random2, random3 };

		/* All of our expections for required mockups */
		expect(servletConfig.getServletContext()).andReturn(servletContext).atLeastOnce();
		expect(servletContext.getResource(ConfigurationConstants.ESOE_CONFIG)).andReturn(
				new URL("file:"+ new File(configTestFile).getAbsolutePath()) );
		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("authnProcessor", com.qut.middleware.esoe.authn.AuthnProcessor.class))
				.andReturn(authnProcessor);

		expect(session.getAttribute("com.qut.middleware.esoe.authn.servlet.dynamicresponseurl")).andReturn(
				"http://esoe.url/sso");

		expect(request.getSession()).andReturn(session).anyTimes();
		expect(session.getAttribute(AuthnProcessorData.SESSION_NAME)).andReturn(data);
		expect(request.getCookies()).andReturn(cookies).anyTimes();
		expect(this.authnProcessor.execute((AuthnProcessorData) notNull())).andReturn(AuthnProcessor.result.Invalid);
		response.addCookie((Cookie)notNull());
		response.sendRedirect(data.getInvalidURL());
		setUpMock();

		authnServlet = new AuthnServlet();
		authnServlet.init(this.servletConfig);
		authnServlet.doPost(request, response);

		tearDownMock();
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.servlet.AuthnServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * Tests to ensure that Automated SSO service is disabled when appropriate cookie is passed
	 */
	@Test
	public void testDoPost2() throws IOException, ServletException, AuthnFailureException
	{
		Cookie[] cookies = new Cookie[5];

		Cookie random = new Cookie("RadomCookie", "itscandyireallyreallylikeit");
		Cookie random2 = new Cookie("RadomCookieName", "someothervalue");
		Cookie random3 = new Cookie("QUTCLIENT", "yourdeathwillcomesooncookieandwhenitdoesitwillbeswift");
		Cookie disableSSO = new Cookie("esoeNoAuto", "true");

		cookies = new Cookie[] { random, random2, random3, disableSSO };

		/* All of our expections for required mockups */
		expect(servletConfig.getServletContext()).andReturn(servletContext).atLeastOnce();
		expect(servletContext.getResource(ConfigurationConstants.ESOE_CONFIG)).andReturn(
				new URL("file:"+ new File(configTestFile).getAbsolutePath()) );
		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("authnProcessor", com.qut.middleware.esoe.authn.AuthnProcessor.class))
				.andReturn(authnProcessor);

		expect(session.getAttribute("com.qut.middleware.esoe.authn.servlet.dynamicresponseurl")).andReturn(
				"http://esoe.url/sso");
		session.removeAttribute("com.qut.middleware.esoe.authn.servlet.dynamicresponseurl");
		
		expect(request.getSession()).andReturn(session).anyTimes();
		expect(session.getAttribute(AuthnProcessorData.SESSION_NAME)).andReturn(data);
		expect(request.getCookies()).andReturn(cookies).anyTimes();
		expect(this.authnProcessor.execute((AuthnProcessorData) notNull())).andReturn(AuthnProcessor.result.Completed);
		response.addCookie((Cookie)notNull());
		response.sendRedirect("http://esoe.url/sso");
		session.removeAttribute("com.qut.middleware.esoe.authn.bean");
		setUpMock();

		authnServlet = new AuthnServlet();
		authnServlet.init(this.servletConfig);
		authnServlet.doPost(request, response);

		assertFalse("Automatic SSO must be false", data.getAutomatedSSO());

		tearDownMock();
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.servlet.AuthnServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * Tests to ensure that the user is redirected to configured 500 error location when redirect values aren't
	 * correctly set by implementors
	 */
	@Test
	public void testDoPost3() throws IOException, ServletException, AuthnFailureException
	{
		data.setInvalidURL(null);
		data.setRedirectTarget(null);

		/* All of our expections for required mockups */
		expect(servletConfig.getServletContext()).andReturn(servletContext).atLeastOnce();
		expect(servletContext.getResource(ConfigurationConstants.ESOE_CONFIG)).andReturn(
				new URL("file:"+ new File(configTestFile).getAbsolutePath()) );
		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("authnProcessor", com.qut.middleware.esoe.authn.AuthnProcessor.class))
				.andReturn(authnProcessor);

		expect(session.getAttribute("com.qut.middleware.esoe.authn.servlet.dynamicresponseurl")).andReturn(
				"http://esoe.url/sso");

		expect(request.getSession()).andReturn(session).anyTimes();
		expect(session.getAttribute(AuthnProcessorData.SESSION_NAME)).andReturn(data);
		expect(request.getCookies()).andReturn(null).anyTimes();
		expect(this.authnProcessor.execute((AuthnProcessorData) notNull())).andReturn(AuthnProcessor.result.Completed);
		response.addCookie((Cookie)notNull());
		response.sendError(500);
		setUpMock();

		authnServlet = new AuthnServlet();
		authnServlet.init(this.servletConfig);
		authnServlet.doPost(request, response);

		tearDownMock();
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.servlet.AuthnServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * Tests to ensure that the user is redirected to configured 500 error location when redirect values aren't
	 * correctly set by implementors
	 */
	@Test
	public void testDoPost3a() throws IOException, ServletException, AuthnFailureException
	{
		data.setInvalidURL(null);
		data.setRedirectTarget(null);

		/* All of our expections for required mockups */
		expect(servletConfig.getServletContext()).andReturn(servletContext).atLeastOnce();
		expect(servletContext.getResource(ConfigurationConstants.ESOE_CONFIG)).andReturn(
				new URL("file:"+ new File(configTestFile).getAbsolutePath()) );
		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("authnProcessor", com.qut.middleware.esoe.authn.AuthnProcessor.class))
				.andReturn(authnProcessor);

		expect(session.getAttribute("com.qut.middleware.esoe.authn.servlet.dynamicresponseurl")).andReturn(
				"http://esoe.url/sso");

		expect(request.getSession()).andReturn(session).anyTimes();
		expect(session.getAttribute(AuthnProcessorData.SESSION_NAME)).andReturn(data);
		expect(request.getCookies()).andReturn(null).anyTimes();
		expect(this.authnProcessor.execute((AuthnProcessorData) notNull())).andReturn(AuthnProcessor.result.Failure);
		response.sendError(500);
		setUpMock();

		authnServlet = new AuthnServlet();
		authnServlet.init(this.servletConfig);
		authnServlet.doPost(request, response);

		tearDownMock();
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.servlet.AuthnServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * Tests to ensure that the user is redirected to configured 500 error location when redirect values aren't
	 * correctly set by implementors
	 */
	@Test
	public void testDoPost3b() throws IOException, ServletException, AuthnFailureException
	{
		data.setInvalidURL(null);
		data.setRedirectTarget(null);

		/* All of our expections for required mockups */
		expect(servletConfig.getServletContext()).andReturn(servletContext).atLeastOnce();
		expect(servletContext.getResource(ConfigurationConstants.ESOE_CONFIG)).andReturn(
				new URL("file:"+ new File(configTestFile).getAbsolutePath()) );
		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("authnProcessor", com.qut.middleware.esoe.authn.AuthnProcessor.class))
				.andReturn(authnProcessor);

		expect(session.getAttribute("com.qut.middleware.esoe.authn.servlet.dynamicresponseurl")).andReturn(
				"http://esoe.url/sso");

		expect(request.getSession()).andReturn(session).anyTimes();
		expect(session.getAttribute(AuthnProcessorData.SESSION_NAME)).andReturn(data);
		expect(request.getCookies()).andReturn(null).anyTimes();
		expect(this.authnProcessor.execute((AuthnProcessorData) notNull())).andReturn(AuthnProcessor.result.UserAgent);
		response.sendError(500);
		setUpMock();

		authnServlet = new AuthnServlet();
		authnServlet.init(this.servletConfig);
		authnServlet.doPost(request, response);

		tearDownMock();
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.servlet.AuthnServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * Tests to ensure that the user is redirected to configured 500 error location when redirect values aren't
	 * correctly set by implementors
	 */
	@Test
	public void testDoPost3c() throws IOException, ServletException, AuthnFailureException
	{
		data.setInvalidURL(null);
		data.setRedirectTarget(null);

		/* All of our expections for required mockups */
		expect(servletConfig.getServletContext()).andReturn(servletContext).atLeastOnce();
		expect(servletContext.getResource(ConfigurationConstants.ESOE_CONFIG)).andReturn(
				new URL("file:"+ new File(configTestFile).getAbsolutePath()) );
		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("authnProcessor", com.qut.middleware.esoe.authn.AuthnProcessor.class))
				.andReturn(authnProcessor);

		expect(session.getAttribute("com.qut.middleware.esoe.authn.servlet.dynamicresponseurl")).andReturn(
				"http://esoe.url/sso");

		expect(request.getSession()).andReturn(session).anyTimes();
		expect(session.getAttribute(AuthnProcessorData.SESSION_NAME)).andReturn(data);
		expect(request.getCookies()).andReturn(null).anyTimes();
		expect(this.authnProcessor.execute((AuthnProcessorData) notNull())).andReturn(AuthnProcessor.result.Invalid);
		response.addCookie((Cookie)notNull());
		response.sendError(500);
		setUpMock();

		authnServlet = new AuthnServlet();
		authnServlet.init(this.servletConfig);
		authnServlet.doPost(request, response);

		tearDownMock();
	}

	/**
	 * Tests to ensure that servlet info is correctly returned when it is queried
	 */
	@Test
	public void testGetServletInfo1() throws ServletException, IOException
	{
		expect(servletConfig.getServletContext()).andReturn(servletContext).atLeastOnce();
		expect(servletContext.getResource(ConfigurationConstants.ESOE_CONFIG)).andReturn(
				new URL("file:"+ new File(configTestFile).getAbsolutePath()) );
		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("authnProcessor", com.qut.middleware.esoe.authn.AuthnProcessor.class))
				.andReturn(authnProcessor);

		setUpMock();

		authnServlet = new AuthnServlet();
		authnServlet.init(this.servletConfig);
		assertNotNull("Asserts that information about the servlet is validly returned", authnServlet.getServletInfo());

		tearDownMock();
	}
	
	/**
	 * Block coverage tests for init function. Test null authnprocessor bean.
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testInit1() throws ServletException, IOException
	{
		expect(servletConfig.getServletContext()).andReturn(servletContext).atLeastOnce();
		expect(servletContext.getResource(ConfigurationConstants.ESOE_CONFIG)).andReturn(
				new URL("file:"+ new File(configTestFile).getAbsolutePath()) );
		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("authnProcessor", com.qut.middleware.esoe.authn.AuthnProcessor.class))
				.andReturn(null);

		setUpMock();

		authnServlet = new AuthnServlet();
		authnServlet.init(this.servletConfig);
		assertNotNull("Asserts that information about the servlet is validly returned", authnServlet.getServletInfo());

		tearDownMock();
	}
	

	@Test
	public void testAutomatedSSO1()
	{
		data.setAutomatedSSO(false);
		assertFalse("Asserts correct value for automatedSSO", data.getAutomatedSSO());
	}
}
