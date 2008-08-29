package com.qut.middleware.esoe.authn.plugins.usernamepassword;


import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isNull;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.authn.AuthnProcessor;
import com.qut.middleware.esoe.authn.bean.AuthnIdentityAttribute;
import com.qut.middleware.esoe.authn.bean.AuthnProcessorData;
import com.qut.middleware.esoe.authn.exception.AuthnFailureException;
import com.qut.middleware.esoe.authn.exception.HandlerRegistrationException;
import com.qut.middleware.esoe.authn.impl.AuthnProcessorImpl;
import com.qut.middleware.esoe.authn.pipeline.Handler;
import com.qut.middleware.esoe.authn.plugins.usernamepassword.authenticator.LdapBasicAuthenticator;
import com.qut.middleware.esoe.authn.plugins.usernamepassword.handler.UserPassAuthenticator;
import com.qut.middleware.esoe.authn.plugins.usernamepassword.handler.UsernamePasswordHandler;
import com.qut.middleware.esoe.sessions.Create;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.Query;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.esoe.sessions.exception.DataSourceException;
import com.qut.middleware.esoe.sessions.exception.DuplicateSessionException;
import com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException;
import com.qut.middleware.esoe.spep.SPEPProcessor;
import com.qut.middleware.saml2.AuthenticationContextConstants;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.identifier.impl.IdentifierCacheImpl;
import com.qut.middleware.saml2.identifier.impl.IdentifierGeneratorImpl;

@SuppressWarnings(value={"unqualified-field-access", "nls"})
public class AuthnTest
{
	private String identifier = "uid";
	private Properties props;
	private String ldapServer; 
	private int ldapServerPort = 389;
	private String ldapServerURL; 
	private String ldapBaseDN;
	private String ldapContextBase;
	private String ldapSearchBase;
	private String ldapUser;
	private String ldapUserDN;
	private String ldapUserPassword;
	private String ldapAdminUser;
	private String ldapAdminUserPassword;
	private static final String METHOD_POST = "POST";
	private static final String FORM_USER_IDENTIFIER = "esoeauthn_user";
	private static final String FORM_PASSWORD_IDENTIFIER = "esoeauthn_pw";
	private static final String FORM_RESPONSE_IDENTIFIER = "esoeauthn_response";
		
	private IdentifierGenerator identifierGenerator;
	private UserPassAuthenticator authenticator;
	private Handler handler;
	private AuthnProcessor authnProcessor;
	private SPEPProcessor spepProcessor;
	private SessionsProcessor sessionsProcessor;
	private Create create;
	private Query query;
	private Principal principal;
	
	private String redirectTarget;
	private Vector<Handler> registeredHandlers;
	List<AuthnIdentityAttribute> idAttrib;
	
	private AuthnProcessorData data;
	
	private String requireCredentialsURL, failedNameValue;
	HttpServletRequest request;
	HttpServletResponse response;
	
	@Before
	public void setUp() throws Exception
	{
		props = new Properties();
		FileInputStream reader = new FileInputStream(new File("tests/testdata/functional.properties"));
		props.load(reader);
		
		this.ldapServer = props.getProperty("ldapServer");
		this.ldapServerURL = props.getProperty("ldapServerURL");
		this.ldapBaseDN = props.getProperty("ldapBaseDN");
		this.ldapContextBase = props.getProperty("ldapContextBase");
		this.ldapSearchBase = props.getProperty("ldapSearchBase");
		this.ldapUser = props.getProperty("ldapUser");
		this.ldapUserDN = props.getProperty("ldapUserDN");
		this.ldapUserPassword = props.getProperty("ldapUserPass");
		
		request = createMock(HttpServletRequest.class);
		response = createMock(HttpServletResponse.class);
		
		redirectTarget = "http://esoe.url/logon/authenticate.do";
		
		identifierGenerator = new IdentifierGeneratorImpl(new IdentifierCacheImpl());
		authenticator = new LdapBasicAuthenticator(this.ldapServer, this.ldapServerPort, this.ldapBaseDN, this.identifier, false, true);
		this.spepProcessor = createMock(SPEPProcessor.class);
		this.query = createMock(Query.class);
		this.create = createMock(Create.class);
		this.sessionsProcessor = createMock(SessionsProcessor.class);
		this.principal = createMock(Principal.class);
		this.requireCredentialsURL = "http://esoe.url/userpass.html";
		this.failedNameValue = "rc=authn";

		handler = new UsernamePasswordHandler(authenticator, sessionsProcessor, identifierGenerator, null, requireCredentialsURL, failedNameValue, redirectTarget, "http://esoe.url/failure.do");
		
		registeredHandlers = new Vector<Handler>();
		registeredHandlers.add(handler);
		try
		{
			authnProcessor = new AuthnProcessorImpl(spepProcessor, sessionsProcessor, registeredHandlers);
		}
		catch(HandlerRegistrationException hre)
		{
			fail("This exception should never be generated in test code");
		}
		
		data = createMock(AuthnProcessorData.class);
	}
	
	private void setUpMock()
	{
		/* Start the replay for all our configured mock objects */
		replay(create);
		replay(sessionsProcessor);
		replay(principal);
		replay(query);
		replay(spepProcessor);
		replay(response);
		replay(request);
		replay(data);
	}
	
	private void tearDownMock()
	{
		/* Verify the mock responses */
		verify(create);
		verify(sessionsProcessor);
		verify(principal);
		verify(query);
		verify(spepProcessor);
		verify(response);
		verify(request);
		verify(data);
	}
	
	/**
	 * Tests for successful authentication
	 */
	@Test
	public void testExecute()
	{
		AuthnProcessor.result result;
		
		/* All of our expections for required mockups */
		//data.setSuccessfulAuthn(false);
		expect(data.getHttpRequest()).andReturn(request).anyTimes();
		data.setSessionID((String)notNull());
		expect(request.getMethod()).andReturn(this.METHOD_POST).anyTimes();
		expect(request.getParameter(this.FORM_USER_IDENTIFIER)).andReturn(this.ldapUser).anyTimes();
		expect(request.getParameter(this.FORM_PASSWORD_IDENTIFIER)).andReturn(this.ldapUserPassword).anyTimes();
		expect(request.getParameter(this.FORM_RESPONSE_IDENTIFIER)).andReturn(null).anyTimes();
		expect(sessionsProcessor.getCreate()).andReturn(create).anyTimes();
		expect(sessionsProcessor.getQuery()).andReturn(query);
		expect(data.getSessionID()).andReturn("12345").anyTimes();
		expect(data.getCurrentHandler()).andReturn(null).anyTimes();
		expect(data.getSuccessfulAuthn()).andReturn(false);
		expect(data.getSuccessfulAuthn()).andReturn(false);
		data.setSuccessfulAuthn(true);
		data.setCurrentHandler(null);
		data.setCurrentHandler((String)notNull());
		expect(sessionsProcessor.getQuery()).andReturn(query);
		data.setPrincipalName( this.ldapUser );

		try
		{
			expect(create.createLocalSession(eq("12345"), eq(this.ldapUser), eq(AuthenticationContextConstants.passwordProtectedTransport), (List)isNull())).andReturn(Create.result.SessionCreated).anyTimes();
			expect(query.queryAuthnSession("12345")).andReturn(principal);
			data.setRedirectTarget(redirectTarget);
			spepProcessor.clearPrincipalSPEPCaches(principal);
			query.validAuthnSession("12345");
		}
		catch (DuplicateSessionException dse)
		{
			fail("DuplicateSourceException should never be generated in this test");
		}
		catch (DataSourceException dse)
		{
			fail("DataSourceException should never be generated in this test");
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("InvalidSessionIdentifierException should never be generated in this test");
		}
			
		expect(data.getSuccessfulAuthn()).andReturn(true);
		setUpMock();
		
		try
		{
			result = authnProcessor.execute(data);
			assertEquals("Asserts the authnProcessor completes this authentication request across all authn components", AuthnProcessor.result.Completed, result);
			tearDownMock();
		}
		catch(AuthnFailureException afe)
		{
			fail("This exception should not be thrown in this test code");
		}
	}
	
	/**
	 * Tests for successful failed authentication
	 */
	@Test
	public void testExecute1() throws Exception
	{
		AuthnProcessor.result result;
		
		/* All of our expections for required mockups */
		data.setSuccessfulAuthn(false);
		expect(data.getHttpRequest()).andReturn(request).anyTimes();
		//data.setSessionID((String)notNull());
		expect(request.getMethod()).andReturn(this.METHOD_POST).anyTimes();
		expect(request.getParameter(this.FORM_USER_IDENTIFIER)).andReturn(this.ldapUser).anyTimes();
		expect(request.getParameter(this.FORM_PASSWORD_IDENTIFIER)).andReturn("junk123z@").anyTimes();
		expect(request.getParameter(this.FORM_RESPONSE_IDENTIFIER)).andReturn(null).anyTimes();
		expect(sessionsProcessor.getCreate()).andReturn(create).anyTimes();
		expect(sessionsProcessor.getQuery()).andReturn(query).anyTimes();
		expect(data.getSessionID()).andReturn("12345").anyTimes();
		expect(data.getCurrentHandler()).andReturn(null).anyTimes();
		expect(data.getSuccessfulAuthn()).andReturn(true);
		expect(data.getSuccessfulAuthn()).andReturn(true);
		query.validAuthnSession("12345");
		
		data.setCurrentHandler((String)notNull());
		data.setCurrentHandler(null);
		data.setRedirectTarget((String)notNull());
		
		setUpMock();
		
		try
		{
			result = authnProcessor.execute(data);
			assertEquals("Asserts the authnProcessor completes this authentication request across all authn components", AuthnProcessor.result.Failure, result);
			tearDownMock();
		}
		catch(AuthnFailureException afe)
		{
			fail("This exception should not be thrown in this test code");
		}
	}

}

