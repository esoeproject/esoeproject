/**
 * 
 */
package com.qut.middleware.esoe.authn.plugins.spnego.handlers;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.authn.bean.AuthnIdentityAttribute;
import com.qut.middleware.esoe.authn.bean.AuthnProcessorData;
import com.qut.middleware.esoe.authn.bean.impl.AuthnProcessorDataImpl;
import com.qut.middleware.esoe.authn.exception.SessionCreationException;
import com.qut.middleware.esoe.authn.pipeline.Handler;
import com.qut.middleware.esoe.authn.plugins.spnego.SPNEGOAuthenticator;
import com.qut.middleware.esoe.authn.plugins.spnego.handler.SPNEGOHandler;
import com.qut.middleware.esoe.sessions.Create;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.esoe.sessions.exception.DataSourceException;
import com.qut.middleware.esoe.sessions.exception.DuplicateSessionException;
import com.qut.middleware.esoe.sessions.exception.SessionCacheUpdateException;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;

@SuppressWarnings("nls")
public class SPNEGOHandlerTest
{

	private SPNEGOHandler handler;
	private SPNEGOAuthenticator authenticator;
	private SessionsProcessor sessionsProcessor;
	private IdentifierGenerator identifierGenerator;
	private AuthnProcessorData authData;
	private	HttpServletRequest request;
	private List<AuthnIdentityAttribute> identAttrib;
	private List<String> targetNetworks;
	private Create create;
	private HttpServletResponse response;
	
	private String userAgentID = "Custom V8 qutAdSSOenabled";
	private String principal = "user@kerberos.domain";
	private String sessionID = "78349hjksaf";
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		this.sessionsProcessor = createMock(SessionsProcessor.class);
		this.identifierGenerator = createMock(IdentifierGenerator.class);
		this.authenticator = createMock(SPNEGOAuthenticator.class);
		this.request = createMock(HttpServletRequest.class);
		this.create = createMock(Create.class);
		this.response = createMock(HttpServletResponse.class);
			
		this.authData = new AuthnProcessorDataImpl();
		this.identAttrib = new ArrayList<AuthnIdentityAttribute>();
		this.targetNetworks = new ArrayList<String>();
		this.targetNetworks.add("0.0.0.0/16");
		
		this.handler = new SPNEGOHandler(this.authenticator, this.sessionsProcessor, this.identifierGenerator, this.identAttrib, "http://google.com", this.userAgentID, this.targetNetworks);
	}

	
	/** Test invalid constructor args to expand block coverage.
	 * 
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testInvalidConstruction1()
	{
		this.handler = new SPNEGOHandler(null, this.sessionsProcessor, this.identifierGenerator, this.identAttrib, "http://google.com", "Mozilla", this.targetNetworks);		
	}
	
	/** Test invalid constructor args to expand block coverage.
	 * 
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testInvalidConstruction2()
	{
		this.handler = new SPNEGOHandler(this.authenticator, null, this.identifierGenerator, this.identAttrib, "http://google.com", "Mozilla", this.targetNetworks);		
	}
	
	/** Test invalid constructor args to expand block coverage.
	 * 
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testInvalidConstruction3()
	{
		this.handler = new SPNEGOHandler(this.authenticator, this.sessionsProcessor, null, this.identAttrib, "http://google.com", "Mozilla", this.targetNetworks);		
	}
	
	/** Test invalid constructor args to expand block coverage.
	 * 
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testInvalidConstruction4()
	{
		this.handler = new SPNEGOHandler(this.authenticator, this.sessionsProcessor, this.identifierGenerator, null, "http://google.com", "Mozilla", this.targetNetworks);		
	}
	
	/** Test invalid constructor args to expand block coverage.
	 * 
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testInvalidConstruction5()
	{
		this.handler = new SPNEGOHandler(this.authenticator, this.sessionsProcessor, this.identifierGenerator, this.identAttrib, null, "Mozilla", this.targetNetworks);		
	}
	
	/** Test invalid constructor args to expand block coverage.
	 * 
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testInvalidConstruction6()
	{
		this.handler = new SPNEGOHandler(this.authenticator, this.sessionsProcessor, this.identifierGenerator, this.identAttrib, "http://google.com", null, this.targetNetworks);		
	}
	
	/** Test invalid constructor args to expand block coverage.
	 * 
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testInvalidConstruction7()
	{
		this.handler = new SPNEGOHandler(this.authenticator, this.sessionsProcessor, this.identifierGenerator, this.identAttrib, "http://google.com", "Mozilla", null);		
	}
	
	/** Test the execution of an SPNEGO handler when processing a browser request with an spnego token
	 * that has been sucesfully authenticated. Returns successful session creation.
	 * 
	 * Test method for {@link com.qut.middleware.esoe.authn.plugins.spnego.handler.SPNEGOHandler#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)}.
	 */
	@Test
	public void testExecute1() 
	{
		expect(this.identifierGenerator.generateSessionID()).andReturn(this.sessionID).anyTimes();
		expect(this.authenticator.authenticate((String)notNull())).andReturn(this.principal).anyTimes();
		expect(this.request.getHeader("User-agent")).andReturn(this.userAgentID).anyTimes();
		expect(this.request.getHeader("Authorization")).andReturn("Insert SPNEGO data here").anyTimes();
		expect(this.request.getRemoteAddr()).andReturn("0.0.1.1").anyTimes();
		
		expect(this.request.getCharacterEncoding()).andReturn("UTF-8").anyTimes();
		
		this.authData.setHttpRequest(this.request);
		
		expect(this.authData.getHttpRequest().getScheme()).andReturn("Negotiate").anyTimes();
		
		Handler.result result = null;
		try
		{
		
			// setup session creation mocks
			expect(this.sessionsProcessor.getCreate()).andReturn(this.create).anyTimes();
		
			this.create.createLocalSession((String)notNull(), (String)notNull(), (String)notNull(), (List<AuthnIdentityAttribute>)anyObject())	;
			expectLastCall().once().andThrow(new SessionCacheUpdateException());
		
			replayMocks();
						
			result = this.handler.execute(this.authData);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		//	fail("Unexpected exception occured.");
		}
		
		assertEquals("Unexpected Authentication code returned. ", Handler.result.Successful, result);
	}

	
	
	
	
	/** Test the execution of an SPNEGO handler when processing a browser request with an spnego token
	 * that has NOT been sucesfully authenticated (ie: the token has expired or some such kerberos type error).
	 * Returns no Action.
	 * 
	 * Test method for {@link com.qut.middleware.esoe.authn.plugins.spnego.handler.SPNEGOHandler#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)}.
	 */
	@Test
	public void testExecute1a() 
	{
		expect(this.identifierGenerator.generateSessionID()).andReturn(this.sessionID);
		expect(this.request.getHeader("User-agent")).andReturn(this.userAgentID);
		expect(this.request.getHeader("Authorization")).andReturn("Insert SPNEGO data here");
		expect(this.request.getRemoteAddr()).andReturn("0.0.1.1");
		expect(this.request.getCharacterEncoding()).andReturn("UTF-8");
		expect(this.request.getScheme()).andReturn("Negotiate");
		expect(this.request.getRemoteAddr()).andReturn("1.1.1.1");
		
		this.authData.setHttpRequest(this.request);
		
		// set a null return value from authenticator to indicate kerberos error
		expect(this.authenticator.authenticate((String)notNull())).andReturn(null).anyTimes();
		
		Handler.result result = null;
		try
		{
		
			// setup session creation mocks
			expect(this.sessionsProcessor.getCreate()).andReturn(this.create).anyTimes();
		
			this.create.createLocalSession((String)notNull(), (String)notNull(), (String)notNull(), (List<AuthnIdentityAttribute>)anyObject())	;
			expectLastCall().once();

			replayMocks();
							
			result = this.handler.execute(this.authData);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		//	fail("Unexpected exception occured.");
		}
		
		assertEquals("Unexpected Authentication code returned. ", Handler.result.NoAction, result);
	}
	
	
	/** Test the execution of an SPNEGO handler when processing a browser request with NO spnego token.
	 * The handler should send the http negotiate challenge and return useragent.
	 * 
	 * Test method for {@link com.qut.middleware.esoe.authn.plugins.spnego.handler.SPNEGOHandler#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)}.
	 */
	@Test
	public void testExecute1b() throws Exception
	{
		expect(this.identifierGenerator.generateSessionID()).andReturn(this.sessionID).anyTimes();
		expect(this.authenticator.authenticate((String)notNull())).andReturn(this.principal).anyTimes();
		expect(this.request.getHeader("User-agent")).andReturn(this.userAgentID).anyTimes();
		expect(this.request.getHeader("Authorization")).andReturn(null).anyTimes();
		expect(this.request.getCharacterEncoding()).andReturn("UTF-8").anyTimes();
		
		this.authData.setHttpRequest(this.request);
		this.authData.setHttpResponse(this.response);
		
		expect(this.authData.getHttpRequest().getScheme()).andReturn("Negotiate").anyTimes();
		expect(this.authData.getHttpRequest().getRemoteAddr()).andReturn("0.0.1.1").anyTimes();
		
		this.response.addHeader((String)notNull(), (String)notNull());
		expectLastCall().anyTimes();
		
		Handler.result result = null;
		
		replayMocks();
					
		result = this.handler.execute(this.authData);
		
		assertEquals("Unexpected Authentication code returned. ", Handler.result.UserAgent, result);
	}

	
	/** Test the execution of an SPNEGO handler when processing a browser request from a simulated
	 * browser that does not contain the correct user-agent header value. The handler should take no action.
	 * 
	 * Test method for {@link com.qut.middleware.esoe.authn.plugins.spnego.handler.SPNEGOHandler#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)}.
	 */
	@Test
	public void testExecute2() 
	{
		expect(this.identifierGenerator.generateSessionID()).andReturn(this.sessionID).anyTimes();
		expect(this.authenticator.authenticate((String)notNull())).andReturn(this.principal).anyTimes();
		expect(this.request.getHeader("User-agent")).andReturn("IE 6 has 'revoluationary' tabbed browsing").anyTimes();
		expect(this.request.getHeader("Authorization")).andReturn("Insert SPNEGO data here").anyTimes();
		expect(this.request.getRemoteAddr()).andReturn("0.0.1.1").anyTimes();
		
		expect(this.request.getCharacterEncoding()).andReturn("UTF-8").anyTimes();
		
		this.authData.setHttpRequest(this.request);
		
		expect(this.authData.getHttpRequest().getScheme()).andReturn("Negotiate").anyTimes();
		
		Handler.result result = null;
		try
		{
		
			// setup session creation mocks
			expect(this.sessionsProcessor.getCreate()).andReturn(this.create).anyTimes();
		
			this.create.createLocalSession((String)notNull(), (String)notNull(), (String)notNull(), (List<AuthnIdentityAttribute>)anyObject())	;
			expectLastCall().atLeastOnce();
		
			replayMocks();
						
			result = this.handler.execute(this.authData);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		//	fail("Unexpected exception occured.");
		}
		
		assertEquals("Unexpected Authentication code returned. ", Handler.result.NoAction, result);
	}
	
	
	/** Test the execution of an SPNEGO handler when processing request whereby the user session fails to be 
	 * created. Should return invalid.
	 * 
	 * Test method for {@link com.qut.middleware.esoe.authn.plugins.spnego.handler.SPNEGOHandler#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)}.
	 */
	@Test //(expected = SessionCreationException.class)
	public void testExecute3() 
	{
		expect(this.identifierGenerator.generateSessionID()).andReturn(this.sessionID).anyTimes();
		expect(this.authenticator.authenticate((String)notNull())).andReturn(this.principal).anyTimes();
		expect(this.request.getHeader("User-agent")).andReturn(this.userAgentID).anyTimes();
		expect(this.request.getHeader("Authorization")).andReturn("Insert SPNEGO data here").anyTimes();
		expect(this.request.getRemoteAddr()).andReturn("0.0.1.1").anyTimes();
		
		expect(this.request.getCharacterEncoding()).andReturn("UTF-8").anyTimes();
		
		this.authData.setHttpRequest(this.request);
		
		expect(this.authData.getHttpRequest().getScheme()).andReturn("Negotiate").anyTimes();
		
		Handler.result result = null;
		try
		{		
			// setup session creation mocks
			expect(this.sessionsProcessor.getCreate()).andReturn(this.create).anyTimes();
		
			// mock a failed session creation ** DataSourceException **
			try 
			{
				this.create.createLocalSession((String)notNull(), (String)notNull(), (String)notNull(), (List<AuthnIdentityAttribute>)anyObject())	;
				expectLastCall().andThrow(new SessionCacheUpdateException());
			} 
			catch (SessionCacheUpdateException e)
			{
				//fail("Unexpected exception thrown:" + e.getLocalizedMessage());
			}
		
			replayMocks();
						
			result = this.handler.execute(this.authData);
		}
		catch(SessionCreationException e)
		{
			//
		}
		
		assertEquals("Unexpected Authentication code returned. ", Handler.result.Invalid, result);
	}
	
	

	/** Test the execution of an SPNEGO handler when processing request whereby the user session fails to be 
	 * created. Should return invalid. 
	 * 
	 * Test method for {@link com.qut.middleware.esoe.authn.plugins.spnego.handler.SPNEGOHandler#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)}.
	 */
	@Test
	public void testExecute4() 
	{
		expect(this.identifierGenerator.generateSessionID()).andReturn(this.sessionID);
		expect(this.request.getHeader("User-agent")).andReturn(this.userAgentID);
		expect(this.request.getHeader("Authorization")).andReturn("Insert SPNEGO data here");
		expect(this.request.getRemoteAddr()).andReturn("0.0.1.1");
		expect(this.request.getCharacterEncoding()).andReturn("UTF-8");
		expect(this.request.getScheme()).andReturn("Negotiate");
		expect(this.request.getRemoteAddr()).andReturn("1.1.1.1");
		
		this.authData.setHttpRequest(this.request);
		
		// set a null return value from authenticator to indicate kerberos error
		expect(this.authenticator.authenticate((String)notNull())).andReturn(this.principal).anyTimes();
		
		Handler.result result = null;
		try
		{		
			// setup session creation mocks
			expect(this.sessionsProcessor.getCreate()).andReturn(this.create).anyTimes();
		
			this.create.createLocalSession((String)notNull(), (String)notNull(), (String)notNull(), (List<AuthnIdentityAttribute>)anyObject())	;
			expectLastCall().andThrow(new SessionCacheUpdateException());

			replayMocks();
							
			result = this.handler.execute(this.authData);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail("Unexpected exception occured.");
		}
		
		assertEquals("Unexpected Authentication code returned. ", Handler.result.Invalid, result);
	}
	
	
	/** Test behaviour of SPNEGO handler when processing request whereby the user is not on a 
	 * target network. Should be no action.
	 * 
	 * Test method for {@link com.qut.middleware.esoe.authn.plugins.spnego.handler.SPNEGOHandler#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)}.
	 */
	@Test
	public void testExecute5() 
	{
		expect(this.identifierGenerator.generateSessionID()).andReturn(this.sessionID).anyTimes();
		expect(this.authenticator.authenticate((String)notNull())).andReturn(this.principal).anyTimes();
		expect(this.request.getHeader("User-agent")).andReturn(this.userAgentID).anyTimes();
		expect(this.request.getHeader("Authorization")).andReturn("Insert SPNEGO data here").anyTimes();
		
		expect(this.request.getCharacterEncoding()).andReturn("UTF-8").anyTimes();
		
		this.authData.setHttpRequest(this.request);
		
		// disable SSO
		this.authData.setAutomatedSSO(false);
		
		expect(this.authData.getHttpRequest().getScheme()).andReturn("Negotiate").anyTimes();
		
		Handler.result result = null;
		try
		{		
			// setup session creation mocks
			expect(this.sessionsProcessor.getCreate()).andReturn(this.create).anyTimes();
		
			this.create.createLocalSession((String)notNull(), (String)notNull(), (String)notNull(), (List<AuthnIdentityAttribute>)anyObject()) ;
			expectLastCall().atLeastOnce();
		
			replayMocks();
						
			result = this.handler.execute(this.authData);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		//	fail("Unexpected exception occured.");
		}
		
		assertEquals("Unexpected Authentication code returned. ", Handler.result.NoAction, result);
	}
	/** Test behaviour of SPNEGO handler when processing request whereby the user has requested
	 * that SSO be disabled. Should be no action.
	 * 
	 * Test method for {@link com.qut.middleware.esoe.authn.plugins.spnego.handler.SPNEGOHandler#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)}.
	 */
	@Test
	public void testExecute6() 
	{
		expect(this.identifierGenerator.generateSessionID()).andReturn(this.sessionID).anyTimes();
		expect(this.authenticator.authenticate((String)notNull())).andReturn(this.principal).anyTimes();
		expect(this.request.getHeader("User-agent")).andReturn(this.userAgentID).anyTimes();
		expect(this.request.getHeader("Authorization")).andReturn("Insert SPNEGO data here").anyTimes();
		// Make sure the handler is actually checking the remote address
		expect(this.request.getRemoteAddr()).andReturn("2.0.1.1").atLeastOnce();
		
		expect(this.request.getCharacterEncoding()).andReturn("UTF-8").anyTimes();
		
		this.authData.setHttpRequest(this.request);
		
		expect(this.authData.getHttpRequest().getScheme()).andReturn("Negotiate").anyTimes();
		
		Handler.result result = null;
		try
		{
		
			// setup session creation mocks
			expect(this.sessionsProcessor.getCreate()).andReturn(this.create).anyTimes();
		
			this.create.createLocalSession((String)notNull(), (String)notNull(), (String)notNull(), (List<AuthnIdentityAttribute>)anyObject()) ;
			expectLastCall().atLeastOnce();
			
			replayMocks();
						
			result = this.handler.execute(this.authData);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		//	fail("Unexpected exception occured.");
		}
		
		assertEquals("Unexpected Authentication code returned. ", Handler.result.NoAction, result);
	}
	
	/**
	 * Test method for {@link com.qut.middleware.esoe.authn.plugins.spnego.handler.SPNEGOHandler#getAuthenticator()}.
	 */
	@Test
	public void testGetAuthenticator()
	{
		assertEquals("returned authenticator not the same. ", this.authenticator, this.handler.getAuthenticator());
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.authn.plugins.spnego.handler.SPNEGOHandler#getHandlerName()}.
	 */
	@Test
	public void testGetHandlerName()
	{
		assertEquals("returned authenticator not the same. ", this.handler.getHandlerName(), "SPNEGOHandler");
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.authn.plugins.spnego.handler.SPNEGOHandler#getIdentifierGenerator()}.
	 */
	@Test
	public void testGetIdentifierGenerator()
	{
		assertEquals("returned authenticator not the same. ", this.identifierGenerator, this.handler.getIdentifierGenerator());
		
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.authn.plugins.spnego.handler.SPNEGOHandler#getSessionsProcessor()}.
	 */
	@Test
	public void testGetSessionsProcessor()
	{
		assertEquals("returned authenticator not the same. ", this.sessionsProcessor, this.handler.getSessionsProcessor());
		
	}

	private void replayMocks()
	{
		replay(this.identifierGenerator);
		replay(this.sessionsProcessor);
		replay(this.authenticator);
		replay(this.request);
		replay(this.create);
		replay(this.response);
	}

	private void verifyMocks()
	{
		verify(this.identifierGenerator);
		verify(this.sessionsProcessor);
		verify(this.authenticator);
		verify(this.request);
		verify(this.create);
		verify(this.response);
	}
}
