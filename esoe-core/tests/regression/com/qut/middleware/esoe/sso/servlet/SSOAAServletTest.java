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
package com.qut.middleware.esoe.sso.servlet;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.matches;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;
import java.util.Vector;

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
import com.qut.middleware.esoe.sso.SSOProcessor;
import com.qut.middleware.esoe.sso.bean.SSOProcessorData;
import com.qut.middleware.esoe.sso.bean.impl.SSOProcessorDataImpl;
import com.qut.middleware.esoe.sso.exception.InvalidRequestException;
import com.qut.middleware.esoe.sso.exception.InvalidSessionIdentifierException;

@SuppressWarnings(value = { "unqualified-field-access", "nls" })
public class SSOAAServletTest
{
	public SSOAAServletTest()
	{
		writer = new PrintWriter(new ByteArrayOutputStream()); 
	}
	
	
	private SSOAAServlet ssoAAServlet;
	private SSOProcessor ssoProcessor;
	private SSOProcessorData data;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private HttpSession session;
	private ServletConfig servletConfig;
	private ServletContext servletContext;
	private PrintWriter writer;
	
	//	 use this as esoe config file
	String configTestFile = "tests/testdata/test.config";
	
	private WebApplicationContext webApplicationContext;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{		
		ssoProcessor = createMock(SSOProcessor.class);
		request = createMock(HttpServletRequest.class);
		response = createMock(HttpServletResponse.class);
		session = createMock(HttpSession.class);
		servletConfig = createMock(ServletConfig.class);
		servletContext = createMock(ServletContext.class);
		webApplicationContext = createMock(WebApplicationContext.class);

		data = new SSOProcessorDataImpl();
		data.setResponseDocument("<saml>this is a really fake saml document which is fine to test with here</saml>");
	}

	private void setUpMock()
	{
		/* Start the replay for all our configured mock objects */
		replay(request);
		replay(response);
		replay(ssoProcessor);
		replay(session);
		replay(servletConfig);
		replay(servletContext);
		replay(webApplicationContext);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception
	{
		// Not Implemented
	}

	private void tearDownMock()
	{
		/* Verify the mock responses */
		verify(request);
		verify(response);
		verify(ssoProcessor);
		verify(session);
		verify(servletConfig);
		verify(servletContext);
		verify(webApplicationContext);
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sso.servlet.SSOAAServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * Tests to ensure that the principal is redirected to the login page for GET
	 * requests that are not returning from an SPEP.
	 */
	@Test
	public void testDoGet1() 
	{
		try
		{
		expect(servletConfig.getServletContext()).andReturn(servletContext).atLeastOnce();
		expect(servletContext.getResource(ConfigurationConstants.ESOE_CONFIG)).andReturn(
				new URL("file:"+ new File(configTestFile).getAbsolutePath()) );

		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("authnAuthorityProcessor", com.qut.middleware.esoe.sso.SSOProcessor.class))
				.andReturn(ssoProcessor);
		
		expect(session.getAttribute("com.qut.middleware.esoe.sso.bean")).andReturn(null);

		expect(request.getSession()).andReturn(session).anyTimes();
		
		response.sendRedirect((String)notNull());
		
		setUpMock();
		
		ssoAAServlet = new SSOAAServlet();
		ssoAAServlet.init(this.servletConfig);
		ssoAAServlet.doGet(request, response);
		
		tearDownMock();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sso.servlet.SSOAAServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * Tests to ensure that the principal is given a successfull SAML response when valid data already exists in the session
	 */
	@Test
	public void testDoGet2() throws IOException, ServletException
	{
		Cookie[] cookies = new Cookie[1];
		Cookie esoeSession = new Cookie("esoeSession", "_12345");

		cookies = new Cookie[] { esoeSession };
		
		expect(servletConfig.getServletContext()).andReturn(servletContext).atLeastOnce();
		expect(servletContext.getResource(ConfigurationConstants.ESOE_CONFIG)).andReturn(
				new URL("file:"+ new File(configTestFile).getAbsolutePath()) );
		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("authnAuthorityProcessor", com.qut.middleware.esoe.sso.SSOProcessor.class))
				.andReturn(ssoProcessor);
		
		expect(session.getAttribute("com.qut.middleware.esoe.sso.bean")).andReturn(data);

		expect(request.getSession()).andReturn(session).anyTimes();	
		expect(request.getCookies()).andReturn(cookies).anyTimes();
		
		try
		{
			expect(this.ssoProcessor.execute((SSOProcessorData) notNull())).andReturn(SSOProcessor.result.SSOGenerationSuccessful);
			expect(response.getWriter()).andReturn(writer);
			session.removeAttribute(SSOProcessorData.SESSION_NAME);
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("Exception not expected in this test " + e.getMessage());
		}
		catch (InvalidRequestException e)
		{
			fail("Exception not expected in this test " + e.getMessage());
		}
		
		setUpMock();

		ssoAAServlet = new SSOAAServlet();
		ssoAAServlet.init(this.servletConfig);
		ssoAAServlet.doGet(request, response);
		tearDownMock();
	}
	
	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sso.servlet.SSOAAServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * Tests to ensure that the user gets a SAML response when an existing session requires ForcePassiveAuthn (this should however never occur in our impl)
	 */
	@Test
	public void testDoGet2a() throws IOException, ServletException
	{
		/* All of our expections for required mockups */
		expect(servletConfig.getServletContext()).andReturn(servletContext).anyTimes();
		expect(servletContext.getResource(ConfigurationConstants.ESOE_CONFIG)).andReturn(
				new URL("file:"+ new File(configTestFile).getAbsolutePath()) );

		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("authnAuthorityProcessor", com.qut.middleware.esoe.sso.SSOProcessor.class))
				.andReturn(ssoProcessor);
		
		try
		{
			expect(request.getSession()).andReturn(session).anyTimes();
			expect(session.getAttribute(SSOProcessorData.SESSION_NAME)).andReturn(data);
			session.removeAttribute(SSOProcessorData.SESSION_NAME);
			expect(request.getCookies()).andReturn(null).anyTimes();
			expect(this.ssoProcessor.execute((SSOProcessorData) notNull())).andReturn(
					SSOProcessor.result.ForcePassiveAuthn);
			response.addCookie((Cookie)notNull());
			expect(response.getWriter()).andReturn(writer);
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("InvalidSessionIdentifierException should not occur here");
		}
		catch (InvalidRequestException e)
		{
			fail("InvalidRequestException should not occur here");
		}
		setUpMock();

		ssoAAServlet = new SSOAAServlet();
		ssoAAServlet.init(this.servletConfig);
		ssoAAServlet.doGet(request, response);

		tearDownMock();
	}
	
	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sso.servlet.SSOAAServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * Tests to ensure that the user gets a SAML response when session establishment attempt results in
	 * InvalidSessionIdentifierException
	 */
	@Test
	public void testDoGet3() throws IOException, ServletException
	{
		/* All of our expections for required mockups */
		expect(servletConfig.getServletContext()).andReturn(servletContext).anyTimes();
		expect(servletContext.getResource(ConfigurationConstants.ESOE_CONFIG)).andReturn(
				new URL("file:"+ new File(configTestFile).getAbsolutePath()) );

		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("authnAuthorityProcessor", com.qut.middleware.esoe.sso.SSOProcessor.class))
				.andReturn(ssoProcessor);
		
		try
		{
			expect(request.getSession()).andReturn(session).anyTimes();
			expect(session.getAttribute(SSOProcessorData.SESSION_NAME)).andReturn(data);
			expect(request.getCookies()).andReturn(null).anyTimes();
			expect(this.ssoProcessor.execute((SSOProcessorData) notNull())).andThrow(
					new InvalidSessionIdentifierException("mock"));
			response.addCookie((Cookie)notNull());
			expect(response.getWriter()).andReturn(writer);
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("InvalidSessionIdentifierException should not occur here");
		}
		catch (InvalidRequestException e)
		{
			fail("InvalidRequestException should not occur here");
		}
		setUpMock();

		ssoAAServlet = new SSOAAServlet();
		ssoAAServlet.init(this.servletConfig);
		ssoAAServlet.doGet(request, response);

		tearDownMock();
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sso.servlet.SSOAAServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * Tests to ensure that the user gets a SAML response when existing session establishment attempt results in
	 * InvalidRequestException
	 */
	@Test
	public void testDoGet3a() throws IOException, ServletException
	{
		/* All of our expections for required mockups */
		expect(servletConfig.getServletContext()).andReturn(servletContext).anyTimes();
		expect(servletContext.getResource(ConfigurationConstants.ESOE_CONFIG)).andReturn(
				new URL("file:" + System.getProperty("user.dir") + File.separator + "tests" + File.separator
						+ "testdata" + File.separator + "test.config"));

		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("authnAuthorityProcessor", com.qut.middleware.esoe.sso.SSOProcessor.class))
				.andReturn(ssoProcessor);
		
		try
		{
			expect(request.getSession()).andReturn(session).anyTimes();
			expect(session.getAttribute(SSOProcessorData.SESSION_NAME)).andReturn(data);
			expect(request.getCookies()).andReturn(null).anyTimes();
			expect(this.ssoProcessor.execute((SSOProcessorData) notNull())).andThrow(
					new InvalidRequestException("mock"));
			response.addCookie((Cookie)notNull());
			expect(response.getWriter()).andReturn(writer);
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("InvalidSessionIdentifierException should not occur here");
		}
		catch (InvalidRequestException e)
		{
			fail("InvalidRequestException should not occur here");
		}
		setUpMock();

		ssoAAServlet = new SSOAAServlet();
		ssoAAServlet.init(this.servletConfig);
		ssoAAServlet.doGet(request, response);

		tearDownMock();
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sso.servlet.SSOAAServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * Tests to ensure that the principal is redirected to the esoe login URL when SSOProcessor.result.ForceAuthn is
	 * returned
	 */
	@Test
	public void testDoPost1() throws IOException, ServletException, InvalidSessionIdentifierException,
			InvalidRequestException
	{
		Cookie[] cookies = new Cookie[5];

		Cookie random = new Cookie("RadomCookie", "itscandyireallyreallylikeit");
		Cookie random2 = new Cookie("RadomCookieName", "someothervalue");
		Cookie random3 = new Cookie("QUTCLIENT", "yourdeathwillcomesoonshitcookieandwhenitdoesitwillbeswift");

		cookies = new Cookie[] { random, random2, random3 };

		/* All of our expections for required mockups */
		expect(servletConfig.getServletContext()).andReturn(servletContext).anyTimes();
		expect(servletContext.getResource(ConfigurationConstants.ESOE_CONFIG)).andReturn(
				new URL("file:" + System.getProperty("user.dir") + File.separator + "tests" + File.separator
						+ "testdata" + File.separator + "test.config"));

		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("authnAuthorityProcessor", com.qut.middleware.esoe.sso.SSOProcessor.class))
				.andReturn(ssoProcessor);
		
		session.setAttribute(eq("com.qut.middleware.esoe.sso.bean"), notNull());

		expect(request.getSession()).andReturn(session).anyTimes();
		response.addCookie((Cookie)notNull());
		
		expect(session.getAttribute(SSOProcessorData.SESSION_NAME)).andReturn(data);
		expect(request.getCookies()).andReturn(cookies).anyTimes();
		expect(this.ssoProcessor.execute((SSOProcessorData) notNull())).andReturn(SSOProcessor.result.ForceAuthn);
		response.sendRedirect(matches("https://esoe-dev.qut.edu.au:8443/signin.*"));

		setUpMock();

		ssoAAServlet = new SSOAAServlet();
		ssoAAServlet.init(this.servletConfig);
		ssoAAServlet.doPost(request, response);

		tearDownMock();
	}

	
	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sso.servlet.SSOAAServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * Tests to ensure that the principal is redirected to the esoe login URL when SSOProcessor.result.ForceAuthn is
	 * returned.
	 * 
	 * TEST a login request that generates an error response.
	 */
	@Test
	public void testDoPost1a() throws IOException, ServletException, InvalidSessionIdentifierException,
			InvalidRequestException
	{
		Cookie[] cookies = new Cookie[5];

		Cookie random = new Cookie("RadomCookie", "itscandyireallyreallylikeit");
		Cookie random2 = new Cookie("RadomCookieName", "someothervalue");
		Cookie random3 = new Cookie("QUTCLIENT", "yourdeathwillcomesoonshitcookieandwhenitdoesitwillbeswift");

		cookies = new Cookie[] { random, random2, random3 };

		/* All of our expections for required mockups */
		expect(servletConfig.getServletContext()).andReturn(servletContext).anyTimes();
		expect(servletContext.getResource(ConfigurationConstants.ESOE_CONFIG)).andReturn(
				new URL("file:" + System.getProperty("user.dir") + File.separator + "tests" + File.separator
						+ "testdata" + File.separator + "test.config"));

		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("authnAuthorityProcessor", com.qut.middleware.esoe.sso.SSOProcessor.class))
				.andReturn(ssoProcessor);
				
		expect(request.getSession()).andReturn(session).anyTimes();
		
		// here wel will return  null datatypes for force the error response
		expect(session.getAttribute(SSOProcessorData.SESSION_NAME)).andReturn(null);
		expect(request.getParameter("SAMLRequest")).andReturn(null).anyTimes();
					
		response.sendError(eq(500), (String)notNull());

		setUpMock();

		ssoAAServlet = new SSOAAServlet();
		ssoAAServlet.init(this.servletConfig);
		ssoAAServlet.doPost(request, response);

		tearDownMock();
	}

	
	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sso.servlet.SSOAAServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * Tests to ensure that the user gets a SAML response when session establishment is successful
	 */
	@Test
	public void testDoPost2() throws IOException, ServletException
	{
		Cookie[] cookies = new Cookie[5];

		Cookie random = new Cookie("RadomCookie", "itscandyireallyreallylikeit");
		Cookie random2 = new Cookie("RadomCookieName", "someothervalue");
		Cookie random3 = new Cookie("QUTCLIENT", "yourdeathwillcomesooncookieandwhenitdoesitwillbeswift");

		cookies = new Cookie[] { random, random2, random3 };

		/* All of our expections for required mockups */
		expect(servletConfig.getServletContext()).andReturn(servletContext).anyTimes();
		expect(servletContext.getResource(ConfigurationConstants.ESOE_CONFIG)).andReturn(
				new URL("file:" + System.getProperty("user.dir") + File.separator + "tests" + File.separator
						+ "testdata" + File.separator + "test.config"));

		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("authnAuthorityProcessor", com.qut.middleware.esoe.sso.SSOProcessor.class))
				.andReturn(ssoProcessor);
		
		try
		{
			expect(request.getSession()).andReturn(session).anyTimes();
			expect(session.getAttribute(SSOProcessorData.SESSION_NAME)).andReturn(data);
			session.removeAttribute(SSOProcessorData.SESSION_NAME);
			expect(request.getCookies()).andReturn(cookies).anyTimes();
			expect(this.ssoProcessor.execute((SSOProcessorData) notNull())).andReturn(
					SSOProcessor.result.SSOGenerationSuccessful);
			expect(response.getWriter()).andReturn(writer);
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("InvalidSessionIdentifierException should not occur here");
		}
		catch (InvalidRequestException e)
		{
			fail("InvalidRequestException should not occur here");
		}
		setUpMock();

		ssoAAServlet = new SSOAAServlet();
		ssoAAServlet.init(this.servletConfig);
		ssoAAServlet.doPost(request, response);

		tearDownMock();
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sso.servlet.SSOAAServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * Tests to ensure that the user gets a SAML response when session establishment requires ForcePassiveAuthn
	 */
	@Test
	public void testDoPost2a() throws IOException, ServletException
	{
		/* All of our expections for required mockups */
		expect(servletConfig.getServletContext()).andReturn(servletContext).anyTimes();
		expect(servletContext.getResource(ConfigurationConstants.ESOE_CONFIG)).andReturn(
				new URL("file:" + System.getProperty("user.dir") + File.separator + "tests" + File.separator
						+ "testdata" + File.separator + "test.config"));

		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("authnAuthorityProcessor", com.qut.middleware.esoe.sso.SSOProcessor.class))
				.andReturn(ssoProcessor);
		
		try
		{
			expect(request.getSession()).andReturn(session).anyTimes();
			expect(session.getAttribute(SSOProcessorData.SESSION_NAME)).andReturn(data);
			session.removeAttribute(SSOProcessorData.SESSION_NAME);
			expect(request.getCookies()).andReturn(null).anyTimes();
			expect(this.ssoProcessor.execute((SSOProcessorData) notNull())).andReturn(
					SSOProcessor.result.ForcePassiveAuthn);
			expect(response.getWriter()).andReturn(writer);
			response.addCookie((Cookie)notNull());
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("InvalidSessionIdentifierException should not occur here");
		}
		catch (InvalidRequestException e)
		{
			fail("InvalidRequestException should not occur here");
		}
		setUpMock();

		ssoAAServlet = new SSOAAServlet();
		ssoAAServlet.init(this.servletConfig);
		ssoAAServlet.doPost(request, response);

		tearDownMock();
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sso.servlet.SSOAAServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * Tests to ensure that the user gets a SAML response when session establishment attempt results in
	 * InvalidSessionIdentifierException
	 */
	@Test
	public void testDoPost3() throws IOException, ServletException
	{
		/* All of our expections for required mockups */
		expect(servletConfig.getServletContext()).andReturn(servletContext).anyTimes();
		expect(servletContext.getResource(ConfigurationConstants.ESOE_CONFIG)).andReturn(
				new URL("file:" + System.getProperty("user.dir") + File.separator + "tests" + File.separator
						+ "testdata" + File.separator + "test.config"));

		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("authnAuthorityProcessor", com.qut.middleware.esoe.sso.SSOProcessor.class))
				.andReturn(ssoProcessor);
		
		try
		{
			expect(request.getSession()).andReturn(session).anyTimes();
			expect(session.getAttribute(SSOProcessorData.SESSION_NAME)).andReturn(data);
			expect(request.getCookies()).andReturn(null).anyTimes();
			expect(this.ssoProcessor.execute((SSOProcessorData) notNull())).andThrow(
					new InvalidSessionIdentifierException("mock"));
			expect(response.getWriter()).andReturn(writer);
			response.addCookie((Cookie)notNull());
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("InvalidSessionIdentifierException should not occur here");
		}
		catch (InvalidRequestException e)
		{
			fail("InvalidRequestException should not occur here");
		}
		setUpMock();

		ssoAAServlet = new SSOAAServlet();
		ssoAAServlet.init(this.servletConfig);
		ssoAAServlet.doPost(request, response);

		tearDownMock();
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sso.servlet.SSOAAServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * Tests to ensure that the user gets a SAML response when session establishment attempt results in
	 * InvalidRequestException
	 */
	@Test
	public void testDoPost3a() throws IOException, ServletException
	{
		/* All of our expections for required mockups */
		expect(servletConfig.getServletContext()).andReturn(servletContext).anyTimes();
		expect(servletContext.getResource(ConfigurationConstants.ESOE_CONFIG)).andReturn(
				new URL("file:" + System.getProperty("user.dir") + File.separator + "tests" + File.separator
						+ "testdata" + File.separator + "test.config"));

		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("authnAuthorityProcessor", com.qut.middleware.esoe.sso.SSOProcessor.class))
				.andReturn(ssoProcessor);
		
		try
		{
			expect(request.getSession()).andReturn(session).anyTimes();
			expect(session.getAttribute(SSOProcessorData.SESSION_NAME)).andReturn(data);
			expect(request.getCookies()).andReturn(null).anyTimes();
			expect(this.ssoProcessor.execute((SSOProcessorData) notNull())).andThrow(
					new InvalidRequestException("mock"));
			response.addCookie((Cookie)notNull());
			expect(response.getWriter()).andReturn(writer);
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("InvalidSessionIdentifierException should not occur here");
		}
		catch (InvalidRequestException e)
		{
			fail("InvalidRequestException should not occur here");
		}
		setUpMock();

		ssoAAServlet = new SSOAAServlet();
		ssoAAServlet.init(this.servletConfig);
		ssoAAServlet.doPost(request, response);

		tearDownMock();
	}
}
