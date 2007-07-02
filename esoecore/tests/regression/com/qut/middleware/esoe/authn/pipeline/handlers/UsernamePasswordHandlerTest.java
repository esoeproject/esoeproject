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
 * Creation Date: 10/10/2006
 * 
 * Purpose: Executes all paths of UsernamePasswordHandler to verify logic
 * 
 * Built using easymock objects http://www.easymock.org
 */
package com.qut.middleware.esoe.authn.pipeline.handlers;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.authn.bean.AuthnIdentityAttribute;
import com.qut.middleware.esoe.authn.bean.AuthnProcessorData;
import com.qut.middleware.esoe.authn.bean.impl.AuthnProcessorDataImpl;
import com.qut.middleware.esoe.authn.exception.SessionCreationException;
import com.qut.middleware.esoe.authn.pipeline.Authenticator;
import com.qut.middleware.esoe.authn.pipeline.Handler;
import com.qut.middleware.esoe.authn.pipeline.UserPassAuthenticator;
import com.qut.middleware.esoe.sessions.Create;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.esoe.sessions.exception.DataSourceException;
import com.qut.middleware.esoe.sessions.exception.DuplicateSessionException;
import com.qut.middleware.saml2.AuthenticationContextConstants;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.test.Capture;

@SuppressWarnings(value = { "unqualified-field-access", "nls" })
public class UsernamePasswordHandlerTest
{
	private String METHOD_POST = "POST";
	private String METHOD_GET = "GET";
	private String FORM_USER_IDENTIFIER = "esoeauthn_user";
	private String FORM_PASSWORD_IDENTIFIER = "esoeauthn_pw";
	private String FORM_RESPONSE_IDENTIFIER = "esoeauthn_response";

	UsernamePasswordHandler handler;
	SessionsProcessor sessionsProcessor;
	UserPassAuthenticator authenticator;
	Create create;
	AuthnProcessorData data;
	IdentifierGenerator identifierGenerator;
	String redirectTarget;
	String failURL;
	List<AuthnIdentityAttribute> ident;

	Capture<Cookie> captured;
	
	private String usernamePasswordURL = "https://esoe.url/usernamePass.html";
	private String failNameVal = "rc=authn";

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
	{
		captured = new Capture<Cookie>();

		this.identifierGenerator = createMock(IdentifierGenerator.class);
		this.authenticator = createMock(UserPassAuthenticator.class);
		this.sessionsProcessor = createMock(SessionsProcessor.class);
		this.create = createMock(Create.class);
		HttpServletRequest request = createMock(HttpServletRequest.class);
		HttpServletResponse response = createMock(HttpServletResponse.class);
		this.ident = new ArrayList<AuthnIdentityAttribute>();

		/* Default AuthnProcessorData state */
		this.data = new AuthnProcessorDataImpl();
		this.data.setHttpRequest(request);
		this.data.setHttpResponse(response);
		this.data.setSessionID("1234-1234");
	}

	private void setUpMock(HttpServletRequest request, HttpServletResponse response)
	{
		/* Start the replay for all our configured mock objects */
		replay(this.identifierGenerator);
		replay(this.create);
		replay(this.sessionsProcessor);
		replay(this.authenticator);
		replay(request);
		replay(response);
	}

	private void tearDownMock(HttpServletRequest request, HttpServletResponse response)
	{
		/* Verify the mock responses */
		verify(identifierGenerator);
		verify(authenticator);
		verify(create);
		verify(sessionsProcessor);
		verify(request);
		verify(response);
	}

	private void setupHandler()
	{
		this.redirectTarget = "http://esoe.qut.edu.au/logon/index.do";

		this.failURL = "http://esoe.qut.edu.au/logon/difficulties.do";
		
		this.handler = new UsernamePasswordHandler(this.authenticator, this.sessionsProcessor,
				this.identifierGenerator, this.ident, usernamePasswordURL, failNameVal, this.redirectTarget, this.failURL);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception
	{
		// Not Implemented
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.pipeline.handlers.UsernamePasswordHandler#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)}.
	 * Tests to ensure the execute method returns if another handler has performend authn
	 */
	@Test
	public void testExecute1()
	{
		Handler.result result;

		setupHandler();

		data.setSuccessfulAuthn(true);

		try
		{
			result = handler.execute(data);
			assertEquals(
					"Asserts this handler returns without further action when another handler has provided successful authn",
					Handler.result.NoAction, result);
		}
		catch (SessionCreationException sce)
		{
			fail("SessionCreationException detected but this should not be evaluated");
		}
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.pipeline.handlers.UsernamePasswordHandler#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)}.
	 * Tests to ensure the execute method returns the default URL to the user-agent for the GET fail state
	 */
	@Test
	public void testExecute2()
	{
		Handler.result result;

		/* All of our expections for required mockups */
		expect(this.identifierGenerator.generateSessionID()).andReturn(data.getSessionID()).anyTimes();
		expect(this.authenticator.authenticate("beddoes", "itscandyyoulikeit")).andReturn(
				Authenticator.result.Successful).anyTimes();
		expect(data.getHttpRequest().getMethod()).andReturn(this.METHOD_GET).anyTimes();

		setUpMock(data.getHttpRequest(), data.getHttpResponse());

		setupHandler();

		data.setSuccessfulAuthn(false);
		try
		{
			result = handler.execute(data);
			assertEquals("Asserts this handler returns correct value when a GET request is submitted",
					Handler.result.UserAgent, result);
			assertTrue("Asserts the redirect is set to the correct location", data.getRedirectTarget().matches(
					usernamePasswordURL + ".*"));
			tearDownMock(data.getHttpRequest(), data.getHttpResponse());
		}
		catch (SessionCreationException sce)
		{
			fail("SessionCreationException detected but this should not be evaluated");
		}
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.pipeline.handlers.UsernamePasswordHandler#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)}.
	 * Tests to ensure the execute method returns if a null username is entered
	 */
	@Test
	public void testExecute3()
	{
		Handler.result result;

		/* All of our expections for required mockups */
		expect(data.getHttpRequest().getParameter(this.FORM_USER_IDENTIFIER)).andReturn(null).anyTimes();
		expect(data.getHttpRequest().getParameter(this.FORM_PASSWORD_IDENTIFIER)).andReturn("itscandyyoulikeit")
				.anyTimes();
		expect(data.getHttpRequest().getParameter(this.FORM_RESPONSE_IDENTIFIER)).andReturn(null).anyTimes();
		expect(data.getHttpRequest().getMethod()).andReturn(this.METHOD_POST).anyTimes();

		setUpMock(data.getHttpRequest(), data.getHttpResponse());

		setupHandler();

		data.setSuccessfulAuthn(false);
		try
		{
			result = handler.execute(data);
			assertEquals("Asserts this handler returns to configured redirect url when username is null",
					Handler.result.Failure, result);
			assertTrue("Asserts the redirect is set to the correct location", data.getRedirectTarget().matches(
					usernamePasswordURL + ".*"));
			tearDownMock(data.getHttpRequest(), data.getHttpResponse());
		}
		catch (SessionCreationException sce)
		{
			fail("SessionCreationException detected but this should not be evaluated");
		}
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.pipeline.handlers.UsernamePasswordHandler#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)}.
	 * Tests to ensure the execute method returns if a null password is entered
	 */
	@Test
	public void testExecute3a()
	{
		Handler.result result;

		/* All of our expections for required mockups */
		expect(data.getHttpRequest().getParameter(this.FORM_USER_IDENTIFIER)).andReturn("beddoes").anyTimes();
		expect(data.getHttpRequest().getParameter(this.FORM_PASSWORD_IDENTIFIER)).andReturn(null).anyTimes();
		expect(data.getHttpRequest().getParameter(this.FORM_RESPONSE_IDENTIFIER)).andReturn(null).anyTimes();
		expect(data.getHttpRequest().getMethod()).andReturn(this.METHOD_POST).anyTimes();

		setUpMock(data.getHttpRequest(), data.getHttpResponse());

		setupHandler();

		data.setSuccessfulAuthn(false);
		try
		{
			result = handler.execute(data);
			assertEquals("Asserts this handler returns to configured redirect url when password is null",
					Handler.result.Failure, result);
			assertTrue("Asserts the redirect is set to the correct location", data.getRedirectTarget().matches(
					usernamePasswordURL + ".*"));
			tearDownMock(data.getHttpRequest(), data.getHttpResponse());
		}
		catch (SessionCreationException sce)
		{
			fail("SessionCreationException detected but this should not be evaluated");
		}
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.pipeline.handlers.UsernamePasswordHandler#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)}.
	 * Tests to ensure the execute method returns if a null username and password is entered
	 */
	@Test
	public void testExecute3b()
	{
		Handler.result result;

		/* All of our expections for required mockups */
		expect(data.getHttpRequest().getParameter(this.FORM_USER_IDENTIFIER)).andReturn(null).anyTimes();
		expect(data.getHttpRequest().getParameter(this.FORM_PASSWORD_IDENTIFIER)).andReturn(null).anyTimes();
		expect(data.getHttpRequest().getParameter(this.FORM_RESPONSE_IDENTIFIER)).andReturn(null).anyTimes();
		expect(data.getHttpRequest().getMethod()).andReturn(this.METHOD_POST).anyTimes();

		setUpMock(data.getHttpRequest(), data.getHttpResponse());

		setupHandler();

		data.setSuccessfulAuthn(false);
		try
		{
			result = handler.execute(data);
			assertEquals("Asserts this handler returns to configured redirect url when username and password are null",
					Handler.result.Failure, result);
			assertTrue("Asserts the redirect is set to the correct location", data.getRedirectTarget().matches(
					usernamePasswordURL + ".*"));
			tearDownMock(data.getHttpRequest(), data.getHttpResponse());
		}
		catch (SessionCreationException sce)
		{
			fail("SessionCreationException detected but this should not be evaluated");
		}
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.pipeline.handlers.UsernamePasswordHandler#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)}.
	 * Tests valid response for failed authentication
	 */
	@Test
	public void testExecute4()
	{
		Handler.result result;

		/* All of our expections for required mockups */
		expect(this.identifierGenerator.generateSessionID()).andReturn(data.getSessionID()).anyTimes();
		expect(this.authenticator.authenticate("beddoes", "itscandyyoulike")).andReturn(Authenticator.result.Failure)
				.anyTimes();
		expect(data.getHttpRequest().getMethod()).andReturn(this.METHOD_POST).anyTimes();
		expect(data.getHttpRequest().getParameter(this.FORM_USER_IDENTIFIER)).andReturn("beddoes").anyTimes();
		expect(data.getHttpRequest().getParameter(this.FORM_PASSWORD_IDENTIFIER)).andReturn("itscandyyoulike")
				.anyTimes();
		expect(data.getHttpRequest().getParameter(this.FORM_RESPONSE_IDENTIFIER)).andReturn(null).anyTimes();

		setUpMock(data.getHttpRequest(), data.getHttpResponse());

		setupHandler();

		data.setSuccessfulAuthn(false);

		try
		{
			result = handler.execute(data);
			assertEquals("Asserts this handler returns failure when incorrect credentials are entered",
					Handler.result.Failure, result);
			assertTrue("Asserts the redirect is set to the correct location", data.getRedirectTarget().matches(
					usernamePasswordURL + ".*"));
			tearDownMock(data.getHttpRequest(), data.getHttpResponse());
		}
		catch (SessionCreationException sce)
		{
			fail("SessionCreationException detected but this should not be evaluated");
		}
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.pipeline.handlers.UsernamePasswordHandler#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)}.
	 * Tests valid response for failed authentication with supplied response identifier that is invalid
	 */
	@Test
	public void testExecute4b()
	{
		Handler.result result;

		/* All of our expections for required mockups */
		expect(this.identifierGenerator.generateSessionID()).andReturn(data.getSessionID()).anyTimes();
		expect(this.authenticator.authenticate("beddoes", "itscandyyoulike")).andReturn(Authenticator.result.Failure)
				.anyTimes();
		expect(data.getHttpRequest().getMethod()).andReturn(this.METHOD_POST).anyTimes();
		expect(data.getHttpRequest().getParameter(this.FORM_USER_IDENTIFIER)).andReturn("beddoes").anyTimes();
		expect(data.getHttpRequest().getParameter(this.FORM_PASSWORD_IDENTIFIER)).andReturn("itscandyyoulike")
				.anyTimes();
		expect(data.getHttpRequest().getParameter(this.FORM_RESPONSE_IDENTIFIER)).andReturn("15").anyTimes();

		setUpMock(data.getHttpRequest(), data.getHttpResponse());

		setupHandler();

		data.setSuccessfulAuthn(false);

		try
		{
			result = handler.execute(data);
			assertEquals("Asserts this handler returns failure when incorrect credentials are entered",
					Handler.result.Failure, result);
			assertTrue("Asserts the redirect is set to the correct location", data.getRedirectTarget().matches(
					usernamePasswordURL + ".*"));
			tearDownMock(data.getHttpRequest(), data.getHttpResponse());
		}
		catch (SessionCreationException sce)
		{
			fail("SessionCreationException detected but this should not be evaluated");
		}
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.pipeline.handlers.UsernamePasswordHandler#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)}.
	 * Tests complete valid success for the execute method
	 */
	@Test
	public void testExecute5()
	{
		Handler.result result;

		/* All of our expections for required mockups */
		expect(this.identifierGenerator.generateSessionID()).andReturn(data.getSessionID()).anyTimes();
		expect(this.authenticator.authenticate("beddoes", "itscandyyoulikeit")).andReturn(
				Authenticator.result.Successful).anyTimes();
		expect(data.getHttpRequest().getMethod()).andReturn(this.METHOD_POST).anyTimes();
		expect(data.getHttpRequest().getParameter(this.FORM_USER_IDENTIFIER)).andReturn("beddoes").anyTimes();
		expect(data.getHttpRequest().getParameter(this.FORM_PASSWORD_IDENTIFIER)).andReturn("itscandyyoulikeit")
				.anyTimes();
		expect(data.getHttpRequest().getParameter(this.FORM_RESPONSE_IDENTIFIER)).andReturn(null).anyTimes();
		expect(sessionsProcessor.getCreate()).andReturn(create).anyTimes();
		try
		{
			expect(
					create.createLocalSession(data.getSessionID(), "beddoes",
							AuthenticationContextConstants.passwordProtectedTransport, this.ident)).andReturn(
					Create.result.SessionCreated);
		}
		catch (DuplicateSessionException dse)
		{
			fail("DuplicateSourceException should never be generated in this test");
		}
		catch (DataSourceException dse)
		{
			fail("DataSourceException should never be generated in this test");
		}

		setUpMock(data.getHttpRequest(), data.getHttpResponse());

		setupHandler();

		data.setSuccessfulAuthn(false);

		try
		{
			result = handler.execute(data);
			assertEquals("Asserts this handler returns correctly when authentication is completed as expected",
					Handler.result.Successful, result);
			
			assertTrue("Asserts the session identifier is set", data.getSessionID().matches(".*-.*"));
			
			tearDownMock(data.getHttpRequest(), data.getHttpResponse());
		}
		catch (SessionCreationException sce)
		{
			fail("SessionCreationException detected but this should not be evaluated");
		}
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.pipeline.handlers.UsernamePasswordHandler#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)}.
	 * Ensures that a DataSourceException is correctly handled.
	 */
	@Test
	public void testExecute6()
	{
		Handler.result result;

		/* All of our expections for required mockups */
		expect(this.identifierGenerator.generateSessionID()).andReturn(data.getSessionID()).anyTimes();
		expect(this.authenticator.authenticate("beddoes", "itscandyyoulikeit")).andReturn(
				Authenticator.result.Successful).anyTimes();
		expect(data.getHttpRequest().getMethod()).andReturn(this.METHOD_POST).anyTimes();
		expect(data.getHttpRequest().getParameter(this.FORM_USER_IDENTIFIER)).andReturn("beddoes").anyTimes();
		expect(data.getHttpRequest().getParameter(this.FORM_PASSWORD_IDENTIFIER)).andReturn("itscandyyoulikeit")
				.anyTimes();
		expect(data.getHttpRequest().getParameter(this.FORM_RESPONSE_IDENTIFIER)).andReturn(null).anyTimes();
		expect(sessionsProcessor.getCreate()).andReturn(create).anyTimes();
		try
		{
			expect(
					create.createLocalSession(data.getSessionID(), "beddoes",
							AuthenticationContextConstants.passwordProtectedTransport, this.ident)).andThrow(
					new DataSourceException()).atLeastOnce();
		}
		catch (DuplicateSessionException dse)
		{
			fail("DuplicateSourceException should never be generated in this test");
		}
		catch (DataSourceException dse)
		{
			fail("DataSourceException should never be generated in this test");
		}

		setUpMock(data.getHttpRequest(), data.getHttpResponse());

		setupHandler();

		data.setSuccessfulAuthn(false);

		try
		{
			result = handler.execute(data);
			tearDownMock(data.getHttpRequest(), data.getHttpResponse());
			assertEquals("Asserts this handler returns correctly when the exception is thrown", Handler.result.Invalid,
					result);
			assertTrue("Asserts the failURL is correctly populated when exceptions are thrown", data.getInvalidURL()
					.matches(this.failURL + ".*"));
		}
		catch (SessionCreationException sce)
		{
			fail("SessionCreationException detected but this should not be evaluated");
		}
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.pipeline.handlers.UsernamePasswordHandler#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)}.
	 * Ensures that a DuplicateSessionException is correctly handled.
	 */
	@Test
	public void testExecute6b()
	{
		Handler.result result;

		/* All of our expections for required mockups */
		expect(this.identifierGenerator.generateSessionID()).andReturn(data.getSessionID()).anyTimes();
		expect(this.authenticator.authenticate("beddoes", "itscandyyoulikeit")).andReturn(
				Authenticator.result.Successful).anyTimes();
		expect(data.getHttpRequest().getMethod()).andReturn(this.METHOD_POST).anyTimes();
		expect(data.getHttpRequest().getParameter(this.FORM_USER_IDENTIFIER)).andReturn("beddoes").anyTimes();
		expect(data.getHttpRequest().getParameter(this.FORM_PASSWORD_IDENTIFIER)).andReturn("itscandyyoulikeit")
				.anyTimes();
		expect(data.getHttpRequest().getParameter(this.FORM_RESPONSE_IDENTIFIER)).andReturn(null).anyTimes();
		expect(sessionsProcessor.getCreate()).andReturn(create).anyTimes();
		try
		{
			expect(
					create.createLocalSession(data.getSessionID(), "beddoes",
							AuthenticationContextConstants.passwordProtectedTransport, this.ident)).andThrow(
					new DuplicateSessionException()).atLeastOnce();
		}
		catch (DuplicateSessionException dse)
		{
			fail("DuplicateSourceException should never be generated in this test");
		}
		catch (DataSourceException dse)
		{
			fail("DataSourceException should never be generated in this test");
		}

		setUpMock(data.getHttpRequest(), data.getHttpResponse());

		setupHandler();

		data.setSuccessfulAuthn(false);

		try
		{
			result = handler.execute(data);
			tearDownMock(data.getHttpRequest(), data.getHttpResponse());
			assertEquals("Asserts this handler returns correctly when the exception is thrown", Handler.result.Invalid,
					result);
			assertTrue("Asserts the failURL is correctly populated when exceptions are thrown", data.getInvalidURL()
					.matches(this.failURL + ".*"));
		}
		catch (SessionCreationException sce)
		{
			fail("SessionCreationException detected but this should not be evaluated");
		}
	}
}
