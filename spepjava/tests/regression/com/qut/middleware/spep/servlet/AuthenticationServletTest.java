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
 * Creation Date: 01/12/2006
 * 
 * Purpose: Tests the authentication servlet.
 */
package com.qut.middleware.spep.servlet;

import static com.qut.middleware.test.regression.Capture.capture;
import static com.qut.middleware.test.regression.Modify.modify;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.spep.SPEP;
import com.qut.middleware.spep.authn.AuthnProcessor;
import com.qut.middleware.spep.authn.AuthnProcessorData;
import com.qut.middleware.spep.exception.AuthenticationException;
import com.qut.middleware.spep.metadata.Metadata;
import com.qut.middleware.test.regression.Capture;
import com.qut.middleware.test.regression.LineVectorOutputStream;
import com.qut.middleware.test.regression.Modify;

/** */
@SuppressWarnings({"nls"})
public class AuthenticationServletTest
{
	private AuthenticationServlet authenticationServlet;
	private SPEP spep;
	private ServletContext servletContext;
	private ServletConfig servletConfig;
	private AuthnProcessor authnProcessor;
	private Metadata metadata;
	private String tokenName;
	private String tokenDomain;
	private String authnRequest;
	private String singleSignOnEndpoint;
	private String defaultRequestURL;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		this.tokenName = "spep-session";
		this.tokenDomain = "spep-dev.qut.edu.au";
		this.authnRequest = "This is an authn request.";
		this.singleSignOnEndpoint = "this is the single sign-on endpoint";
		this.defaultRequestURL = "http://this.is.the.default/url";
		
		this.authnProcessor = createMock(AuthnProcessor.class);
		
		this.metadata = createMock(Metadata.class);
		expect(this.metadata.getSingleSignOnEndpoint()).andReturn(this.singleSignOnEndpoint).anyTimes();
		
		this.spep = createMock(SPEP.class);
		expect(this.spep.getAuthnProcessor()).andReturn(this.authnProcessor).anyTimes();
		expect(this.spep.getMetadata()).andReturn(this.metadata).anyTimes();
		expect(this.spep.getTokenName()).andReturn(this.tokenName).anyTimes();
		expect(this.spep.getTokenDomain()).andReturn(this.tokenDomain).anyTimes();
		expect(this.spep.getDefaultUrl()).andReturn(this.defaultRequestURL).anyTimes();
		expect(this.spep.isStarted()).andReturn(Boolean.TRUE).anyTimes();
		
		this.servletContext = createMock(ServletContext.class);
		expect(this.servletContext.getAttribute((String)notNull())).andReturn(this.spep).anyTimes();
		
		this.servletConfig = createMock(ServletConfig.class);
		expect(this.servletConfig.getServletContext()).andReturn(this.servletContext).anyTimes();

		this.authenticationServlet = new AuthenticationServlet();
	}
	
	private void startMock()
	{
		replay(this.spep);
		replay(this.servletContext);
		replay(this.servletConfig);
		replay(this.authnProcessor);
		replay(this.metadata);
	}
	
	private void endMock()
	{
		verify(this.spep);
		verify(this.servletContext);
		verify(this.servletConfig);
		verify(this.authnProcessor);
		verify(this.metadata);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testGet1() throws Exception
	{
		final LineVectorOutputStream outputStream = new LineVectorOutputStream();

		String redirectURL = "http://spep-dev.qut.edu.au/redirect";
		String base64RedirectURL = new String(Base64.encodeBase64(redirectURL.getBytes("UTF-8")));
		
		Modify<AuthnProcessorData> modifyAuthnProcessorData = new ModifyAuthnProcessorData( null, null, AuthenticationServletTest.this.authnRequest );
		
		this.authnProcessor.generateAuthnRequest(modify(modifyAuthnProcessorData));
		expectLastCall().anyTimes();

		startMock();
		
		this.authenticationServlet.init(this.servletConfig);
		
		ServletOutputStream out = new OutputStreamServletOutputStream( outputStream );
		
		HttpServletRequest request = createMock(HttpServletRequest.class);
		expect(request.getParameter("redirectURL")).andReturn(base64RedirectURL).anyTimes();
		
		HttpServletResponse response = createMock(HttpServletResponse.class);
		expect(response.getOutputStream()).andReturn(out).anyTimes();
		
		response.setStatus(200);
		expectLastCall().anyTimes();
		
		replay(request);
		replay(response);
		
		this.authenticationServlet.doGet(request, response);
		
		verify(request);
		verify(response);
		
		String base64Document = new String(Base64.encodeBase64(this.authnRequest.getBytes("UTF-8")));
		
		boolean foundURL = false, foundDocument = false;
		for (String line : outputStream.getLines())
		{
			if (line.contains(this.singleSignOnEndpoint))
			{
				foundURL = true;
			}
			if (line.contains(base64Document))
			{
				foundDocument = true;
			}
			
			//System.out.println(line);
		}
		
		assertTrue("Didn't find URL in generated HTML", foundURL);
		assertTrue("Didn't find authnRequest in generated HTML", foundDocument);
		
		endMock();
	}

	/**
	 * @throws Exception
	 */
	@Test(expected = ServletException.class)
	public void testGet2() throws Exception
	{
		final LineVectorOutputStream outputStream = new LineVectorOutputStream();

		String redirectURL = "http://spep-dev.qut.edu.au/redirect";
		String base64RedirectURL = new String(Base64.encodeBase64(redirectURL.getBytes("UTF-8")));
		
		this.authnProcessor.generateAuthnRequest((AuthnProcessorData)notNull());
		expectLastCall().andThrow(new AuthenticationException("test error")).anyTimes();
		
		startMock();
		
		this.authenticationServlet.init(this.servletConfig);
		
		ServletOutputStream out = new OutputStreamServletOutputStream( outputStream );
		
		HttpServletRequest request = createMock(HttpServletRequest.class);
		expect(request.getParameter("redirectURL")).andReturn(base64RedirectURL).anyTimes();
		
		HttpServletResponse response = createMock(HttpServletResponse.class);
		expect(response.getOutputStream()).andReturn(out).anyTimes();
		
		response.setStatus(200);
		expectLastCall().anyTimes();
		
		replay(request);
		replay(response);
		
		this.authenticationServlet.doGet(request, response);
		
		verify(request);
		verify(response);
		
		endMock();
	}


	/**
	 * @throws Exception
	 */
	@Test(expected = ServletException.class)
	public void testPost1a() throws Exception
	{
		final LineVectorOutputStream outputStream = new LineVectorOutputStream();
		
		startMock();
		
		this.authenticationServlet.init(this.servletConfig);
		
		// Empty saml response
		String samlResponse = null;
		
		ServletOutputStream out = new OutputStreamServletOutputStream( outputStream );
		
		HttpServletRequest request = createMock(HttpServletRequest.class);
		expect(request.getParameter("SAMLResponse")).andReturn(samlResponse).anyTimes();
		
		HttpServletResponse response = createMock(HttpServletResponse.class);
		expect(response.getOutputStream()).andReturn(out).anyTimes();
		
		response.setStatus(200);
		expectLastCall().anyTimes();
		
		replay(request);
		replay(response);
		
		this.authenticationServlet.doPost(request, response);
		
		verify(request);
		verify(response);
		
		endMock();
	}

	/**
	 * @throws Exception
	 */
	@Test(expected = ServletException.class)
	public void testPost1b() throws Exception
	{
		final LineVectorOutputStream outputStream = new LineVectorOutputStream();
		
		// Cause authn to fail validation
		this.authnProcessor.processAuthnResponse((AuthnProcessorData)notNull());
		expectLastCall().andThrow(new AuthenticationException("test exception"));
		
		startMock();
		
		this.authenticationServlet.init(this.servletConfig);
		
		String samlResponse = new String(Base64.encodeBase64("some response document".getBytes("UTF-8")));
		
		ServletOutputStream out = new OutputStreamServletOutputStream( outputStream );
		
		HttpServletRequest request = createMock(HttpServletRequest.class);
		expect(request.getParameter("SAMLResponse")).andReturn(samlResponse).anyTimes();
		
		HttpServletResponse response = createMock(HttpServletResponse.class);
		
		replay(request);
		replay(response);
		
		this.authenticationServlet.doPost(request, response);
		
		verify(request);
		verify(response);
		
		endMock();
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testPost2a() throws Exception
	{
		final LineVectorOutputStream outputStream = new LineVectorOutputStream();
		final String sessionID = "9809283409182304981234-923-501209348091234";
		final String base64RequestURL = new String(Base64.encodeBase64(this.defaultRequestURL.getBytes("UTF-8")));
		
		Modify<AuthnProcessorData> modifyAuthnProcessorData = new ModifyAuthnProcessorData( sessionID, null, null );
		
		Capture<Cookie> captureCookie = new Capture<Cookie>();
		
		this.authnProcessor.processAuthnResponse(modify(modifyAuthnProcessorData));
		expectLastCall().anyTimes();
		
		startMock();
		
		this.authenticationServlet.init(this.servletConfig);
		
		String samlResponse = new String(Base64.encodeBase64("some response document".getBytes("UTF-8")));
		
		ServletOutputStream out = new OutputStreamServletOutputStream( outputStream );
		
		HttpServletRequest request = createMock(HttpServletRequest.class);
		expect(request.getParameter("SAMLResponse")).andReturn(samlResponse).anyTimes();
		
		HttpServletResponse response = createMock(HttpServletResponse.class);
		response.addCookie(capture(captureCookie));
		expectLastCall().once();
		// Make sure we get redirected to the default URL
		response.sendRedirect(this.defaultRequestURL);
		expectLastCall().once();
		
		replay(request);
		replay(response);
		
		this.authenticationServlet.doPost(request, response);
		
		verify(request);
		verify(response);
		
		Cookie spepCookie = null;
		for (Cookie cookie : captureCookie.getCaptured())
		{
			if (cookie.getName().equals(this.tokenName))
			{
				spepCookie = cookie;
				break;
			}
		}
		
		assertNotNull(spepCookie);
		assertEquals(this.tokenName, spepCookie.getName());
		assertEquals(sessionID, spepCookie.getValue());
		assertEquals(this.tokenDomain, spepCookie.getDomain());
		
		endMock();
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testPost2b() throws Exception
	{
		final LineVectorOutputStream outputStream = new LineVectorOutputStream();
		final String sessionID = "9809283409182304981234-923-501209348091234";
		final String requestURL = "http://lol.request.url/somepage.jsp";
		final String base64RequestURL = new String(Base64.encodeBase64(requestURL.getBytes("UTF-8")));
		
		Modify<AuthnProcessorData> modifyAuthnProcessorData = new ModifyAuthnProcessorData( sessionID, base64RequestURL, null );
		
		Capture<Cookie> captureCookie = new Capture<Cookie>();
		
		this.authnProcessor.processAuthnResponse(modify(modifyAuthnProcessorData));
		expectLastCall().anyTimes();
		
		startMock();
		
		this.authenticationServlet.init(this.servletConfig);
		
		String samlResponse = new String(Base64.encodeBase64("some response document".getBytes("UTF-8")));
		
		ServletOutputStream out = new OutputStreamServletOutputStream( outputStream );
		
		HttpServletRequest request = createMock(HttpServletRequest.class);
		expect(request.getParameter("SAMLResponse")).andReturn(samlResponse).anyTimes();
		
		HttpServletResponse response = createMock(HttpServletResponse.class);
		response.addCookie(capture(captureCookie));
		expectLastCall().once();
		// Make sure we get redirected to the session URL
		response.sendRedirect(requestURL);
		expectLastCall().once();
		
		
		replay(request);
		replay(response);
		
		this.authenticationServlet.doPost(request, response);
		
		verify(request);
		verify(response);
		
		Cookie spepCookie = null;
		for (Cookie cookie : captureCookie.getCaptured())
		{
			if (cookie.getName().equals(this.tokenName))
			{
				spepCookie = cookie;
				break;
			}
		}
		
		assertNotNull(spepCookie);
		assertEquals(this.tokenName, spepCookie.getName());
		assertEquals(sessionID, spepCookie.getValue());
		assertEquals(this.tokenDomain, spepCookie.getDomain());
		
		endMock();
	}
}

class OutputStreamServletOutputStream extends ServletOutputStream
{
	private OutputStream out;
	
	public OutputStreamServletOutputStream( OutputStream out )
	{
		this.out = out;
	}
	
	@Override
	public void write(int b) throws IOException
	{
		this.out.write(b);
	}
}

class ModifyAuthnProcessorData extends Modify<AuthnProcessorData>
{
	private String sessionID;
	private String base64RequestURL;
	private String requestDocument;

	public ModifyAuthnProcessorData(String sessionID, String base64RequestURL, String requestDocument)
	{
		this.sessionID = sessionID;
		this.base64RequestURL = base64RequestURL;
		this.requestDocument = requestDocument;
	}

	@Override
	public void operate(AuthnProcessorData object)
	{
		object.setSessionID(this.sessionID);
		object.setRequestURL(this.base64RequestURL);
		object.setRequestDocument(this.requestDocument);
	}
	
}

