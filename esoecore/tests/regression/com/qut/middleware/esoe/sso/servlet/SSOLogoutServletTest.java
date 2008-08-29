/**
 * 
 */
package com.qut.middleware.esoe.sso.servlet;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.springframework.web.context.WebApplicationContext;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.logout.bean.LogoutProcessorData;
import com.qut.middleware.esoe.logout.servlet.LogoutServlet;
import com.qut.middleware.esoe.logout.LogoutProcessor;
import com.qut.middleware.esoe.logout.bean.LogoutProcessorData;
import com.qut.middleware.esoe.logout.bean.impl.LogoutProcessorDataImpl;
import com.qut.middleware.esoe.sso.exception.InvalidRequestException;
import com.qut.middleware.esoe.sso.exception.InvalidSessionIdentifierException;

@SuppressWarnings({"nls","unqualified-field-access"})
public class SSOLogoutServletTest {

	public SSOLogoutServletTest()
	{
		// for visibility of line vector
	}

	private LogoutServlet ssoLogoutServlet;
	private LogoutProcessor logoutProcessor;
	private LogoutProcessorData data;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private HttpSession session;
	private ServletConfig servletConfig;
	private ServletContext servletContext;
	private WebApplicationContext webApplicationContext;

	// use this as esoe config file
	String configTestFile = "tests/testdata/test.config";
	
	private String sessionID = "_849374TEST";
	
	private PrintWriter writer;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		writer = new PrintWriter(new ByteArrayOutputStream()); 

		logoutProcessor = createMock(LogoutProcessor.class);
		request = createMock(HttpServletRequest.class);
		response = createMock(HttpServletResponse.class);
		session = createMock(HttpSession.class);
		servletConfig = createMock(ServletConfig.class);
		servletContext = createMock(ServletContext.class);
		webApplicationContext = createMock(WebApplicationContext.class);

		data = new LogoutProcessorDataImpl();
		data.setSessionID(this.sessionID);
		//data.setResponseDocument(new String("<saml>this is a really fake saml document which is fine to test with here</saml>").getBytes());
		
		System.setProperty("esoe.data", "tests/testdata");
	}

	private void setUpMock()
	{
		/* Start the replay for all our configured mock objects */
		replay(request);
		replay(response);
		replay(logoutProcessor);
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
		verify(logoutProcessor);
		verify(servletConfig);
		verify(servletContext);
		verify(webApplicationContext);
	}

		
	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.logout.servlet.LogoutServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * Tests to ensure that the principal is redirected to the esoe login URL when SSOProcessor.result.ForceAuthn is
	 * returned
	 */
	@Test
	public void testDoPost1() throws IOException, ServletException, InvalidSessionIdentifierException,
			InvalidRequestException
	{
		Cookie ssoSessionCookie = new Cookie("sessionTokenName", "itscandyireallyreallylikeit");
		
		Cookie[] cookies = new Cookie[] { ssoSessionCookie };

		/* All of our expections for required mockups */
		expect(servletConfig.getServletContext()).andReturn(servletContext).anyTimes();
		
				
		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("logoutProcessor", com.qut.middleware.esoe.logout.LogoutProcessor.class))
				.andReturn(logoutProcessor);
		
		session.setAttribute(eq("com.qut.middleware.esoe.logout.bean"), notNull());
		expect(request.getParameter("esoelogout_nonsso")).andReturn("").anyTimes();
		expect(request.getParameter("disablesso")).andReturn("").anyTimes();
		expect(request.getParameter("esoelogout_response")).andReturn("qut.com").anyTimes();		
		expect(request.getSession()).andReturn(session).anyTimes();
		expect(request.getRemoteAddr()).andReturn("111.1111.22.22").anyTimes();
		
		expect(session.getAttribute(LogoutProcessorData.SESSION_NAME)).andReturn(data);
		expect(request.getCookies()).andReturn(cookies).anyTimes();
		//expect(this.logoutProcessor.execute((LogoutProcessorData) notNull())).andReturn(LogoutProcessor.result.ForceAuthn);
	
		response.addCookie((Cookie)anyObject());
		expectLastCall().anyTimes();
		response.sendRedirect((String)anyObject());
		expectLastCall().anyTimes();
		
		expect(request.getMethod()).andReturn("POST").anyTimes();

		setUpMock();
		
		ssoLogoutServlet = new LogoutServlet();
		ssoLogoutServlet.init(this.servletConfig);
		ssoLogoutServlet.service(request, response);

		//System.out.println();
		tearDownMock();
	}
	
	
	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.logout.servlet.LogoutServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * Tests to ensure that the principal is redirected to the esoe login URL when SSOProcessor.result.ForceAuthn is
	 * returned.
	 * 
	 * TEST logout with SSO enabled.
	 */
	@Test
	public void testDoPost1a() throws IOException, ServletException, InvalidSessionIdentifierException,
			InvalidRequestException
	{
		Cookie ssoSessionCookie = new Cookie("sessionTokenName", "itscandyireallyreallylikeit");
		
		Cookie[] cookies = new Cookie[] { ssoSessionCookie };

		/* All of our expections for required mockups */
		expect(servletConfig.getServletContext()).andReturn(servletContext).anyTimes();
		
				
		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("logoutProcessor", com.qut.middleware.esoe.logout.LogoutProcessor.class))
				.andReturn(logoutProcessor);
		
		session.setAttribute(eq("com.qut.middleware.esoe.logout.bean"), notNull());
		expect(request.getParameter("esoelogout_nonsso")).andReturn("").anyTimes();
		expect(request.getParameter("disablesso")).andReturn(null).anyTimes();
		expect(request.getParameter("esoelogout_response")).andReturn(null).anyTimes();		
		expect(request.getSession()).andReturn(session).anyTimes();
		expect(request.getRemoteAddr()).andReturn("111.1111.22.22").anyTimes();
		
		expect(session.getAttribute(LogoutProcessorData.SESSION_NAME)).andReturn(data);
		expect(request.getCookies()).andReturn(cookies).anyTimes();
		//expect(this.logoutProcessor.execute((LogoutProcessorData) notNull())).andReturn(LogoutProcessor.result.ForceAuthn);
	
		response.addCookie((Cookie)anyObject());
		expectLastCall().anyTimes();
		response.sendRedirect((String)anyObject());
		expectLastCall().anyTimes();
		
		expect(request.getMethod()).andReturn("POST").anyTimes();

		setUpMock();

		ssoLogoutServlet = new LogoutServlet();
		ssoLogoutServlet.init(this.servletConfig);
		ssoLogoutServlet.service(request, response);

		//System.out.println();
		tearDownMock();
	}
	
	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.logout.servlet.LogoutServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * 
	 *
	 * Test behaviour when the submitted POST does not contain the required logout request form
	 * parameter (esoelogout_nonsso). Should send a 500 error.
	 */
	@Test
	public void testDoPost2() throws IOException, ServletException, InvalidSessionIdentifierException,
			InvalidRequestException
	{		
		/* All of our expections for required mockups */
		expect(servletConfig.getServletContext()).andReturn(servletContext).anyTimes();
		
				
		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("logoutProcessor", com.qut.middleware.esoe.logout.LogoutProcessor.class))
				.andReturn(logoutProcessor);
		
		session.setAttribute(eq("com.qut.middleware.esoe.logout.bean"), notNull());	
		
		// this is our missing parameter
		expect(request.getParameter("esoelogout_nonsso")).andReturn(null).anyTimes();
		
		expect(request.getParameter("disablesso")).andReturn("").anyTimes();
		expect(request.getParameter("esoelogout_response")).andReturn("qut.com").anyTimes();		
		expect(request.getSession()).andReturn(session).anyTimes();
		expect(request.getRemoteAddr()).andReturn("111.1111.22.22").anyTimes();
		
		expect(session.getAttribute(LogoutProcessorData.SESSION_NAME)).andReturn(data).anyTimes();
		
		// if we expect a 500 error set, then the test will not compile unless the called code
		// actually sets the error as a 500, therefore this will be sufficient for our test.
		response.sendError(eq(500), (String)notNull());
		expectLastCall().anyTimes();
		
		expect(request.getMethod()).andReturn("POST").anyTimes();

		setUpMock();

		ssoLogoutServlet = new LogoutServlet();
		ssoLogoutServlet.init(this.servletConfig);
		ssoLogoutServlet.service(request, response);

		tearDownMock();
	}
	
	
	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.logout.servlet.LogoutServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * 
	 *
	 * Test behaviour when the LogoutAuthProcessor throws an exception.
	 */
	@Test
	public void testDoPost3() throws Exception
	{
		Cookie ssoSessionCookie = new Cookie("sessionTokenName", "itscandyireallyreallylikeit");
		
		Cookie[] cookies = new Cookie[] { ssoSessionCookie };

		/* All of our expections for required mockups */
		expect(servletConfig.getServletContext()).andReturn(servletContext).anyTimes();
		
				
		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("logoutProcessor", com.qut.middleware.esoe.logout.LogoutProcessor.class))
				.andReturn(logoutProcessor);
		
		session.setAttribute(eq("com.qut.middleware.esoe.logout.bean"), notNull());
		expect(request.getParameter("esoelogout_nonsso")).andReturn("").anyTimes();
		expect(request.getParameter("disablesso")).andReturn("").anyTimes();
		expect(request.getParameter("esoelogout_response")).andReturn("qut.com").anyTimes();		
		expect(request.getSession()).andReturn(session).anyTimes();
		expect(request.getRemoteAddr()).andReturn("111.1111.22.22").anyTimes();
		
		expect(session.getAttribute(LogoutProcessorData.SESSION_NAME)).andReturn(data);
		expect(request.getCookies()).andReturn(cookies).anyTimes();
		
		// here is the exception
		expect(this.logoutProcessor.execute((LogoutProcessorData) notNull())).andThrow(new InvalidRequestException("Invalid request"));
	
		response.addCookie((Cookie)anyObject());
		expectLastCall().anyTimes();
		
		// we expect the logout servlet to throw a 500
		response.sendError(eq(500), (String)notNull());
		expectLastCall().anyTimes();
		
		expect(request.getMethod()).andReturn("POST").anyTimes();

		setUpMock();

		ssoLogoutServlet = new LogoutServlet();
		ssoLogoutServlet.init(this.servletConfig);
		ssoLogoutServlet.service(request, response);

		//System.out.println();
		tearDownMock();
	}
	
	
	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.logout.servlet.LogoutServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * 
	 *
	 * Same as testDoPost3 but logout processor throws an invalidsession exception.
	 */
	@Test
	public void testDoPost4() throws Exception
	{
		Cookie ssoSessionCookie = new Cookie("sessionTokenName", "itscandyireallyreallylikeit");
		
		Cookie[] cookies = new Cookie[] { ssoSessionCookie };

		/* All of our expections for required mockups */
		expect(servletConfig.getServletContext()).andReturn(servletContext).anyTimes();
		
				
		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("logoutProcessor", com.qut.middleware.esoe.logout.LogoutProcessor.class))
				.andReturn(logoutProcessor);
		
		session.setAttribute(eq("com.qut.middleware.esoe.logout.bean"), notNull());
		expect(request.getParameter("esoelogout_nonsso")).andReturn("").anyTimes();
		expect(request.getParameter("disablesso")).andReturn("").anyTimes();
		expect(request.getParameter("esoelogout_response")).andReturn("qut.com").anyTimes();		
		expect(request.getSession()).andReturn(session).anyTimes();
		expect(request.getRemoteAddr()).andReturn("111.1111.22.22").anyTimes();
		
		expect(session.getAttribute(LogoutProcessorData.SESSION_NAME)).andReturn(data);
		expect(request.getCookies()).andReturn(cookies).anyTimes();
		
		// here is the exception
		expect(this.logoutProcessor.execute((LogoutProcessorData) notNull())).andThrow(new InvalidSessionIdentifierException("Invalid Session"));
	
		response.addCookie((Cookie)anyObject());
		expectLastCall().anyTimes();
		
		// we expect the logout servlet to throw a 500
		response.sendError(eq(500), (String)notNull());
		expectLastCall().anyTimes();
		
		expect(request.getMethod()).andReturn("POST").anyTimes();

		setUpMock();

		ssoLogoutServlet = new LogoutServlet();
		ssoLogoutServlet.init(this.servletConfig);
		ssoLogoutServlet.service(request, response);

		//System.out.println();
		tearDownMock();
	}
	
	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.logout.servlet.LogoutServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * 
	 * Test the behaviour of the servlet when the SSOProcessor does not exist. Should throw InvalidParameterException
	 * during init().
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testInit() throws IOException, ServletException, InvalidSessionIdentifierException,
			InvalidRequestException
	{
		Cookie ssoSessionCookie = new Cookie("sessionTokenName", "itscandyireallyreallylikeit");
		
		Cookie[] cookies = new Cookie[] { ssoSessionCookie };

		/* All of our expections for required mockups */
		expect(servletConfig.getServletContext()).andReturn(servletContext).anyTimes();
		
				
		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("logoutProcessor", com.qut.middleware.esoe.logout.LogoutProcessor.class))
				.andReturn(null);
					
		setUpMock();

		ssoLogoutServlet = new LogoutServlet();
		ssoLogoutServlet.init(this.servletConfig);
		
		//System.out.println();
		tearDownMock();
	}
	
	
	/** Not much to test here. The logout serlvet should simply redirect to a logout form, as it
	 * does not directly support the GET request to perform logouts.
	 * 
	 */
	@Test
	public void testDoGet() throws Exception
	{
		/* All of our expections for required mockups */
		expect(servletConfig.getServletContext()).andReturn(servletContext).anyTimes();
		
				
		expect(servletContext.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).andReturn(
				webApplicationContext);
		expect(webApplicationContext.getBean("logoutProcessor", com.qut.middleware.esoe.logout.LogoutProcessor.class))
				.andReturn(logoutProcessor);
		
		response.sendRedirect((String)anyObject());
		expectLastCall().anyTimes();
		
		expect(request.getMethod()).andReturn("GET").anyTimes();

		setUpMock();
		
		ssoLogoutServlet = new LogoutServlet();
		ssoLogoutServlet.init(this.servletConfig);
	
		this.ssoLogoutServlet.service(this.request, this.response);
		
	}
}
