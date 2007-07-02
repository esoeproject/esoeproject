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
 * Purpose: Tests execution of AuthnProcessorImpl
 */
package com.qut.middleware.esoe.authn.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.authn.AuthnProcessor;
import com.qut.middleware.esoe.authn.bean.AuthnProcessorData;
import com.qut.middleware.esoe.authn.bean.impl.AuthnProcessorDataImpl;
import com.qut.middleware.esoe.authn.exception.AuthnFailureException;
import com.qut.middleware.esoe.authn.exception.HandlerRegistrationException;
import com.qut.middleware.esoe.authn.exception.SessionCreationException;
import com.qut.middleware.esoe.authn.pipeline.Handler;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.Query;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException;
import com.qut.middleware.esoe.spep.SPEPProcessor;

@SuppressWarnings(value = { "unqualified-field-access", "nls" })
public class AuthnProcessorImplTest
{
	private AuthnProcessor authnProcessor;
	private AuthnProcessorData data;
	private List<Handler> registeredHandlers;
	private Handler handler;
	private Handler handlerPassive;
	private Handler handlerNonPassive;
	private String sessionTokenName;
	private String sessionDomain;
	private SPEPProcessor spepProcessor;
	private SessionsProcessor sessionsProcessor;
	private Query query;
	private Principal principal;

	HttpServletRequest request;
	HttpServletResponse response;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		this.registeredHandlers = new ArrayList<Handler>();
		this.handler = createMock(Handler.class);
		this.handlerPassive = createMock(Handler.class);
		this.handlerNonPassive = createMock(Handler.class);
		this.spepProcessor = createMock(SPEPProcessor.class);
		this.sessionsProcessor = createMock(SessionsProcessor.class);
		this.query = createMock(Query.class);
		this.principal = createMock(Principal.class);
		this.sessionTokenName = "bamsID";
		this.sessionDomain = ".esoe.qut.edu.au";
		request = createMock(HttpServletRequest.class);
		response = createMock(HttpServletResponse.class);

		this.data = new AuthnProcessorDataImpl();
		this.data.setSessionID("1234-1234");
		this.data.setHttpRequest(request);
		this.data.setHttpResponse(response);
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
		replay(this.handler);
		replay(this.handlerPassive);
		replay(this.handlerNonPassive);
		replay(this.request);
		replay(this.response);
		replay(this.spepProcessor);
		replay(this.sessionsProcessor);
		replay(this.query);
		replay(this.principal);
	}

	private void tearDownMock()
	{
		/* Start the replay for all our configured mock objects */
		verify(this.handler);
		verify(this.handlerPassive);
		verify(this.handlerNonPassive);
		verify(this.request);
		verify(this.response);
		verify(this.spepProcessor);
		verify(this.query);
		verify(this.principal);
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.impl.AuthnProcessorImpl#execute(Vector<com.qut.middleware.esoe.authn.pipeline.Handler>, String, String)}.
	 * Tests to ensure we get HandlerRegistrationException when no handlers are presented
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testAuthnProcessorImpl() throws HandlerRegistrationException
	{
		this.authnProcessor = new AuthnProcessorImpl(this.spepProcessor, this.sessionsProcessor, this.registeredHandlers);
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.impl.AuthnProcessorImpl#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)}.
	 * Tests to ensure that UserAgent is returned when Handler generates UserAgent return value
	 */
	@Test
	public void testExecute1() throws HandlerRegistrationException, SessionCreationException, AuthnFailureException
	{
		/* All of our expections for required mockups */
		expect(this.handler.execute((AuthnProcessorData) notNull())).andReturn(Handler.result.UserAgent);
		expect(this.handler.getHandlerName()).andReturn("TestName").anyTimes();
		expect(sessionsProcessor.getQuery()).andReturn(query);
		try
		{
			query.validAuthnSession((String)notNull());
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("Should not get InvalidSessionIdentifierException in this test");
		}
		setUpMock();

		this.registeredHandlers.add(this.handler);
		this.authnProcessor = new AuthnProcessorImpl(this.spepProcessor, this.sessionsProcessor, this.registeredHandlers);

		AuthnProcessor.result result = this.authnProcessor.execute(this.data);
		assertEquals("Ensure correct return value for UserAgent being returned from handler",
				AuthnProcessor.result.UserAgent, result);
		tearDownMock();
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.impl.AuthnProcessorImpl#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)}.
	 * Tests to ensure that Failure is returned when Handler generates Failure return value
	 */
	@Test
	public void testExecute1a() throws HandlerRegistrationException, SessionCreationException, AuthnFailureException
	{
		/* All of our expections for required mockups */
		expect(this.handler.execute((AuthnProcessorData) notNull())).andReturn(Handler.result.Failure);
		expect(this.handler.getHandlerName()).andReturn("TestName").anyTimes();
		expect(sessionsProcessor.getQuery()).andReturn(query);
		try
		{
			query.validAuthnSession((String)notNull());
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("Should not get InvalidSessionIdentifierException in this test");
		}
		setUpMock();

		this.registeredHandlers.add(this.handler);
		this.authnProcessor = new AuthnProcessorImpl(this.spepProcessor, this.sessionsProcessor, this.registeredHandlers);

		AuthnProcessor.result result = this.authnProcessor.execute(this.data);
		assertEquals("Ensure correct return value for UserAgent being returned from handler",
				AuthnProcessor.result.Failure, result);
		tearDownMock();
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.impl.AuthnProcessorImpl#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)}.
	 * Tests to ensure that Invalid is returned when Handler generates Invalid return value
	 */
	@Test
	public void testExecute1b() throws HandlerRegistrationException, SessionCreationException, AuthnFailureException
	{
		/* All of our expections for required mockups */
		expect(this.handler.execute((AuthnProcessorData) notNull())).andReturn(Handler.result.Invalid);
		expect(this.handler.getHandlerName()).andReturn("TestName").anyTimes();
		expect(sessionsProcessor.getQuery()).andReturn(query);
		try
		{
			query.validAuthnSession((String)notNull());
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("Should not get InvalidSessionIdentifierException in this test");
		}
		setUpMock();

		this.registeredHandlers.add(this.handler);
		this.authnProcessor = new AuthnProcessorImpl(this.spepProcessor, this.sessionsProcessor, this.registeredHandlers);

		AuthnProcessor.result result = this.authnProcessor.execute(this.data);
		assertEquals("Ensure correct return value for UserAgent being returned from handler",
				AuthnProcessor.result.Invalid, result);
		tearDownMock();
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.impl.AuthnProcessorImpl#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)}.
	 * Tests to ensure that failure is returned when Handler generates SessionCreationException
	 */
	@Test
	public void testExecute1f() throws HandlerRegistrationException, SessionCreationException, AuthnFailureException
	{
		/* All of our expections for required mockups */
		expect(this.handler.execute((AuthnProcessorData) notNull())).andThrow(
				new SessionCreationException("TEST exception from testExecute1f()"));
		expect(this.handler.getHandlerName()).andReturn("TestName").anyTimes();
		expect(sessionsProcessor.getQuery()).andReturn(query);
		try
		{
			query.validAuthnSession((String)notNull());
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("Should not get InvalidSessionIdentifierException in this test");
		}
		setUpMock();

		registeredHandlers.add(handler);
		authnProcessor = new AuthnProcessorImpl(this.spepProcessor, this.sessionsProcessor, this.registeredHandlers);

		AuthnProcessor.result result = this.authnProcessor.execute(this.data);
		assertEquals("Ensure correct return value for UserAgent being returned from handler",
				AuthnProcessor.result.Failure, result);
		tearDownMock();
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.impl.AuthnProcessorImpl#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)}.
	 * Tests to ensure that if no Non Passive handler is defined AuthnFailureException is generated
	 */
	@Test(expected = AuthnFailureException.class)
	public void testExecute2() throws HandlerRegistrationException, SessionCreationException, AuthnFailureException
	{
		/* All of our expections for required mockups */
		expect(this.handler.execute((AuthnProcessorData) notNull())).andReturn(Handler.result.NoAction);
		expect(this.handler.getHandlerName()).andReturn("TestName").anyTimes();
		expect(sessionsProcessor.getQuery()).andReturn(query);
		try
		{
			query.validAuthnSession((String)notNull());
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("Should not get InvalidSessionIdentifierException in this test");
		}
		setUpMock();

		registeredHandlers.add(handler);
		authnProcessor = new AuthnProcessorImpl(this.spepProcessor, this.sessionsProcessor, this.registeredHandlers);

		authnProcessor.execute(data);
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.impl.AuthnProcessorImpl#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)}.
	 * Tests to ensure that if single passive handler is defined Completed is returned
	 */
	@Test
	public void testExecute4() throws HandlerRegistrationException, SessionCreationException, AuthnFailureException
	{
		/* All of our expections for required mockups */
		expect(this.handler.execute((AuthnProcessorData) notNull())).andReturn(Handler.result.Successful);
		expect(this.handler.getHandlerName()).andReturn("TestName").anyTimes();
		expect(sessionsProcessor.getQuery()).andReturn(query);
		try
		{
			query.validAuthnSession((String)notNull());
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("Should not get InvalidSessionIdentifierException in this test");
		}

		expect(sessionsProcessor.getQuery()).andReturn(query);
		try
		{
			expect(query.queryAuthnSession((String)notNull())).andReturn(principal);
			spepProcessor.clearPrincipalSPEPCaches(principal);
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("InvalidSessionIdentifierException should not occur in this test");
		}

		setUpMock();

		registeredHandlers.add(handler);
		authnProcessor = new AuthnProcessorImpl(this.spepProcessor, this.sessionsProcessor, this.registeredHandlers);

		AuthnProcessor.result result = this.authnProcessor.execute(this.data);
		assertEquals("Ensure correct return value for Completed being returned from handler",
				AuthnProcessor.result.Completed, result);

		tearDownMock();
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.impl.AuthnProcessorImpl#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)}.
	 * Tests to ensure that if passive and non passive handlers are defined Completed is still returned and that the non
	 * passive handler is executed
	 */
	@Test
	public void testExecute5() throws HandlerRegistrationException, SessionCreationException, AuthnFailureException
	{
		/* All of our expections for required mockups */
		expect(handlerPassive.execute((AuthnProcessorData) notNull())).andReturn(Handler.result.SuccessfulNonPrincipal);
		expect(handlerPassive.getHandlerName()).andReturn("TestName").anyTimes();
		expect(sessionsProcessor.getQuery()).andReturn(query);
		try
		{
			query.validAuthnSession((String)notNull());
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("Should not get InvalidSessionIdentifierException in this test");
		}
		expect(handlerNonPassive.execute((AuthnProcessorData) notNull())).andReturn(Handler.result.Successful);
		expect(handlerNonPassive.getHandlerName()).andReturn("TestName2").anyTimes();
		expect(sessionsProcessor.getQuery()).andReturn(query);
		try
		{
			expect(query.queryAuthnSession((String)notNull())).andReturn(principal);
			spepProcessor.clearPrincipalSPEPCaches(principal);
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("InvalidSessionIdentifierException should not occur in this test");
		}
		
		setUpMock();

		registeredHandlers.add(handlerPassive);
		registeredHandlers.add(handlerNonPassive);
		authnProcessor = new AuthnProcessorImpl(this.spepProcessor, this.sessionsProcessor, this.registeredHandlers);

		AuthnProcessor.result result = this.authnProcessor.execute(this.data);
		assertEquals("Ensure correct return value for Completed being returned from handler",
				AuthnProcessor.result.Completed, result);
		tearDownMock();
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.impl.AuthnProcessorImpl#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)}.
	 * Tests to ensure that if passive and non passive handlers are defined Completed is still returned and that the
	 * passive handler is executed
	 */
	@Test
	public void testExecute5a() throws HandlerRegistrationException, SessionCreationException, AuthnFailureException
	{
		/* All of our expections for required mockups */
		expect(handlerPassive.execute((AuthnProcessorData) notNull())).andReturn(Handler.result.SuccessfulNonPrincipal);
		expect(handlerPassive.getHandlerName()).andReturn("TestName").anyTimes();
		expect(sessionsProcessor.getQuery()).andReturn(query);
		try
		{
			query.validAuthnSession((String)notNull());
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("Should not get InvalidSessionIdentifierException in this test");
		}
		expect(handlerNonPassive.execute((AuthnProcessorData) notNull())).andReturn(Handler.result.Successful);
		expect(handlerNonPassive.getHandlerName()).andReturn("TestName2").anyTimes();
		expect(sessionsProcessor.getQuery()).andReturn(query);
		try
		{
			expect(query.queryAuthnSession((String)notNull())).andReturn(principal);
			spepProcessor.clearPrincipalSPEPCaches(principal);
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("InvalidSessionIdentifierException should not occur in this test");
		}
		setUpMock();

		registeredHandlers.add(handlerNonPassive);
		registeredHandlers.add(handlerPassive);
		authnProcessor = new AuthnProcessorImpl(this.spepProcessor, this.sessionsProcessor, this.registeredHandlers);

		AuthnProcessor.result result = this.authnProcessor.execute(this.data);
		assertEquals("Ensure correct return value for Completed being returned from handler",
				AuthnProcessor.result.Completed, result);
		tearDownMock();
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.impl.AuthnProcessorImpl#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)}.
	 * Tests to ensure that if multiple passive and non passive handlers are defined Completed is still returned
	 */
	@Test
	public void testExecute5b() throws HandlerRegistrationException, SessionCreationException, AuthnFailureException
	{
		/* All of our expections for required mockups */
		expect(handlerPassive.execute((AuthnProcessorData) notNull())).andReturn(Handler.result.SuccessfulNonPrincipal)
				.anyTimes();
		expect(sessionsProcessor.getQuery()).andReturn(query);
		try
		{
			query.validAuthnSession((String)notNull());
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("Should not get InvalidSessionIdentifierException in this test");
		}
		expect(handlerPassive.getHandlerName()).andReturn("TestName").anyTimes();
		expect(handlerNonPassive.execute((AuthnProcessorData) notNull())).andReturn(Handler.result.Successful)
				.anyTimes();
		expect(handlerNonPassive.getHandlerName()).andReturn("TestName2").anyTimes();
		expect(sessionsProcessor.getQuery()).andReturn(query);
		try
		{
			expect(query.queryAuthnSession((String)notNull())).andReturn(principal);
			spepProcessor.clearPrincipalSPEPCaches(principal);
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("InvalidSessionIdentifierException should not occur in this test");
		}
		setUpMock();

		registeredHandlers.add(handlerPassive);
		registeredHandlers.add(handlerNonPassive);
		registeredHandlers.add(handlerPassive);
		authnProcessor = new AuthnProcessorImpl(this.spepProcessor, this.sessionsProcessor, this.registeredHandlers);

		AuthnProcessor.result result = this.authnProcessor.execute(this.data);
		assertEquals("Ensure correct return value for Completed being returned from handler",
				AuthnProcessor.result.Completed, result);
		tearDownMock();
	}
	
	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.impl.AuthnProcessorImpl#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)}.
	 * Tests to ensure that if multiple passive and non passive handlers are defined and the data bean indicates that it is upto a certain handler (in this case handler with name Test2)
	 * that no operations are invoked on earlier handlers.
	 */
	@Test
	public void testExecute5c() throws HandlerRegistrationException, SessionCreationException, AuthnFailureException
	{
		data.setCurrentHandler("TestName2");
		
		/* All of our expections for required mockups */
		expect(handlerPassive.execute((AuthnProcessorData) notNull())).andReturn(Handler.result.SuccessfulNonPrincipal)
				.anyTimes();
		expect(sessionsProcessor.getQuery()).andReturn(query);
		try
		{
			query.validAuthnSession((String)notNull());
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("Should not get InvalidSessionIdentifierException in this test");
		}
		expect(handlerPassive.getHandlerName()).andReturn("TestName").anyTimes();
		expect(handlerNonPassive.execute((AuthnProcessorData) notNull())).andReturn(Handler.result.Successful)
				.anyTimes();
		expect(handlerNonPassive.getHandlerName()).andReturn("TestName2").anyTimes();
		expect(sessionsProcessor.getQuery()).andReturn(query);
		try
		{
			expect(query.queryAuthnSession((String)notNull())).andReturn(principal);
			spepProcessor.clearPrincipalSPEPCaches(principal);
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("InvalidSessionIdentifierException should not occur in this test");
		}
		setUpMock();

		registeredHandlers.add(handlerPassive);
		registeredHandlers.add(handlerNonPassive);

		authnProcessor = new AuthnProcessorImpl(this.spepProcessor, this.sessionsProcessor, this.registeredHandlers);

		AuthnProcessor.result result = this.authnProcessor.execute(this.data);
		assertEquals("Ensure correct return value for Completed being returned from handler",
				AuthnProcessor.result.Completed, result);
		tearDownMock();
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.authn.impl.AuthnProcessorImpl#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)}.
	 * Tests to ensure that if multiple handlers are competing to provide authn i.e ignoring the value of
	 * data.getSuccesfulAuthn() that AuthnFailureException is thrown
	 */
	@Test(expected = AuthnFailureException.class)
	public void testExecute6() throws HandlerRegistrationException, SessionCreationException, AuthnFailureException
	{
		/* All of our expections for required mockups */
		expect(handlerPassive.execute((AuthnProcessorData) notNull())).andReturn(Handler.result.SuccessfulNonPrincipal)
				.anyTimes();
		expect(handlerPassive.getHandlerName()).andReturn("TestName").anyTimes();
		expect(handlerNonPassive.execute((AuthnProcessorData) notNull())).andReturn(Handler.result.Successful)
				.anyTimes();
		expect(handlerNonPassive.getHandlerName()).andReturn("TestName").anyTimes();
		expect(sessionsProcessor.getQuery()).andReturn(query);
		try
		{
			query.validAuthnSession((String)notNull());
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("Should not get InvalidSessionIdentifierException in this test");
		}
		setUpMock();

		registeredHandlers.add(handlerPassive);
		registeredHandlers.add(handlerNonPassive);
		registeredHandlers.add(handlerPassive);
		registeredHandlers.add(handlerNonPassive);
		authnProcessor = new AuthnProcessorImpl(this.spepProcessor, this.sessionsProcessor, this.registeredHandlers);

		authnProcessor.execute(data);
	}

	/**
	 * Tests to ensure that handler addition works correctly
	 */
	@Test
	public void testGetRegisteredHandlers() throws HandlerRegistrationException
	{
		registeredHandlers.add(handlerPassive);
		registeredHandlers.add(handlerNonPassive);
		registeredHandlers.add(handlerPassive);
		registeredHandlers.add(handlerNonPassive);
		authnProcessor = new AuthnProcessorImpl(this.spepProcessor, this.sessionsProcessor, this.registeredHandlers);
		assertEquals("Assert getRegisteredHandlers returns correct values", registeredHandlers, authnProcessor
				.getRegisteredHandlers());
	}
}
