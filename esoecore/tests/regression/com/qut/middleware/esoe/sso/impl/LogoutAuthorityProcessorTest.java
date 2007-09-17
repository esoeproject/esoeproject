package com.qut.middleware.esoe.sso.impl;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.JAXBElement;

import org.junit.Before;
import org.junit.Test;
import org.w3._2000._09.xmldsig_.Signature;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.crypto.KeyStoreResolver;
import com.qut.middleware.esoe.crypto.impl.KeyStoreResolverImpl;
import com.qut.middleware.esoe.metadata.Metadata;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.Query;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.esoe.sessions.Terminate;
import com.qut.middleware.esoe.sessions.cache.SessionCache;
import com.qut.middleware.esoe.sso.SSOProcessor;
import com.qut.middleware.esoe.sso.SSOProcessor.result;
import com.qut.middleware.esoe.sso.bean.FailedLogoutRepository;
import com.qut.middleware.esoe.sso.bean.SSOProcessorData;
import com.qut.middleware.esoe.sso.bean.impl.FailedLogoutRepositoryImpl;
import com.qut.middleware.esoe.sso.bean.impl.SSOProcessorDataImpl;
import com.qut.middleware.esoe.sso.exception.InvalidRequestException;
import com.qut.middleware.esoe.sso.exception.InvalidSessionIdentifierException;
import com.qut.middleware.esoe.ws.WSClient;
import com.qut.middleware.esoe.ws.exception.WSClientException;
import com.qut.middleware.saml2.StatusCodeConstants;
import com.qut.middleware.saml2.VersionConstants;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.identifier.impl.IdentifierCacheImpl;
import com.qut.middleware.saml2.identifier.impl.IdentifierGeneratorImpl;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.protocol.LogoutResponse;
import com.qut.middleware.saml2.schemas.protocol.Status;
import com.qut.middleware.saml2.schemas.protocol.StatusCode;
import com.qut.middleware.saml2.schemas.protocol.StatusResponseType;
import com.qut.middleware.saml2.validator.SAMLValidator;
import com.qut.middleware.saml2.validator.impl.SAMLValidatorImpl;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

@SuppressWarnings( { "unqualified-field-access", "nls" })
public class LogoutAuthorityProcessorTest
{

	private LogoutAuthorityProcessor logoutAuthorityProcessor;
	private SSOProcessorData data;
	private FailedLogoutRepository failedLogouts;
	private SAMLValidator samlValidator;
	private SessionsProcessor sessionsProcessor;
	private Metadata metadata;
	private Principal principal;
	private Terminate terminator;
	private SessionCache sessionCache;
	private String validSessionIdentifier = "83uihas7983y2r2r2r";
	private WSClient wsClient;
	private Query query;
	private Marshaller<JAXBElement<StatusResponseType>> logoutResponseMarshaller;
	private String[] logoutSchemas;
	
	List<String> testEntities;
	List<String> testEndpoints;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		try
		{
			samlValidator = new SAMLValidatorImpl(new IdentifierCacheImpl(), 120);
			failedLogouts = new FailedLogoutRepositoryImpl();
			data = new SSOProcessorDataImpl();
			sessionsProcessor = createMock(SessionsProcessor.class);
			principal = createMock(Principal.class);
			metadata = createMock(Metadata.class);
			terminator = createMock(Terminate.class);
			sessionCache = createMock(SessionCache.class);
			wsClient = createMock(WSClient.class);
			query = createMock(Query.class);

		}
		catch (Exception e)
		{
			fail("Unexpected exception state thrown when creating parameters.");
		}

		testEntities = new Vector<String>();
		testEntities.add("https://spep.test.1/");
		testEntities.add("https://spep.test.2/");

		testEndpoints = new Vector<String>();
		testEndpoints.add("http://test.com");

		String keyStorePath = "tests/testdata/testskeystore.ks";
		
		String keyStorePassword = "Es0EKs54P4SSPK";
		String esoeKeyAlias = "esoeprimary";
		String esoeKeyPassword = "Es0EKs54P4SSPK";

		KeyStoreResolver keyStoreResolver = new KeyStoreResolverImpl(new File(keyStorePath), keyStorePassword, esoeKeyAlias, esoeKeyPassword);

		this.logoutSchemas = new String[] { ConfigurationConstants.samlProtocol, ConfigurationConstants.samlAssertion };
		this.logoutResponseMarshaller = new MarshallerImpl<JAXBElement<StatusResponseType>>(StatusResponseType.class.getPackage().getName(), this.logoutSchemas, keyStoreResolver.getKeyAlias(), keyStoreResolver.getPrivateKey());

		List<String> testIndicies = new Vector<String>();
		testIndicies.add("test1-index");
		testIndicies.add("test2:FFE45C9D00ACFF4EDABB367D-INDEX");

		expect(principal.getSessionID()).andReturn(this.validSessionIdentifier).anyTimes();
		expect(principal.getActiveDescriptors()).andReturn(testEntities).anyTimes();
		expect(principal.getSAMLAuthnIdentifier()).andReturn("testSamlID").anyTimes();
		expect(principal.getDescriptorSessionIdentifiers((String) notNull())).andReturn(testIndicies).anyTimes();
		expect(principal.getPrincipalAuthnIdentifier()).andReturn("TestUser:1").anyTimes();
		expect(sessionCache.getSession((String) notNull())).andReturn(principal).anyTimes();
		expect(query.queryAuthnSession((String) notNull())).andReturn(principal).anyTimes();
		expect(sessionsProcessor.getQuery()).andReturn(query).anyTimes();
		expect(sessionsProcessor.getTerminate()).andReturn(terminator).anyTimes();
		expect(metadata.resolveSingleLogoutService((String) notNull())).andReturn(this.testEndpoints).anyTimes();
		expect(metadata.getEsoeEntityID()).andReturn("12345-12345").anyTimes();
		expect(metadata.resolveKey("esoeprimary")).andReturn(keyStoreResolver.getPublicKey()).anyTimes();

		this.logoutAuthorityProcessor = new LogoutAuthorityProcessor(failedLogouts, samlValidator, sessionsProcessor, metadata, new IdentifierGeneratorImpl(new IdentifierCacheImpl()), keyStoreResolver, wsClient);
	}

	private void setupMock()
	{
		replay(sessionsProcessor);
		replay(principal);
		replay(metadata);
		replay(terminator);
		replay(sessionCache);
		replay(wsClient);
		replay(query);
	}

	private void teardownMock()
	{
		verify(sessionsProcessor);
		verify(principal);
		verify(metadata);
		verify(terminator);
		verify(sessionCache);
		verify(wsClient);
		verify(query);
	}

	/*
	 * Test the execution of a valid Logout Request.
	 * 
	 */
	@SuppressWarnings("nls")
	@Test
	public void testExecute1() throws WSClientException
	{	
		try
		{
			byte[] responseDoc = this.generateLogoutResponse(StatusCodeConstants.success, "Logged out successfully", "_logreq1234-1234");
			expect(wsClient.singleLogout((byte[])notNull(), (String)notNull())).andReturn(responseDoc).anyTimes();
			data.setSessionID(this.validSessionIdentifier);
			this.terminator.terminateSession((String)anyObject());
			expectLastCall().anyTimes();
		}
		catch(com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException  e)
		{
			fail("Exception state should not be thrown in this test: " + e.getMessage());
		}
		catch (MarshallerException e)
		{
			fail("Exception state should not be thrown in this test: " + e.getMessage());
		}
		
		setupMock();
		
		try
		{
			result result = this.logoutAuthorityProcessor.execute(data);
					
			assertEquals("Recieved incorrect result. ", SSOProcessor.result.LogoutSuccessful, result);
			
			// check out the logout states, should be one for each active spep 			
			assertEquals("Logout count differes from expected", this.testEntities.size(), data.getLogoutStates().size());
			
			return;
		}
		catch(InvalidSessionIdentifierException e)
		{
			fail("Unexpected Exception was thrown.");
		}		
		catch(InvalidRequestException e)
		{
			fail("Unexpected Exception was thrown.");
		}
		teardownMock();	
	}

	
	/*
	 * Test the execution of a valid Logout Request.
	 * 
	 */
	@SuppressWarnings("nls")
	@Test
	public void testExecute2() throws WSClientException
	{	
		try
		{
			byte[] responseDoc = this.generateLogoutResponse(StatusCodeConstants.success, "Logged out successfully", "_logreq1234-1234");
			expect(wsClient.singleLogout((byte[])notNull(), (String)notNull())).andReturn(responseDoc).anyTimes();
			data.setSessionID(this.validSessionIdentifier);
			this.terminator.terminateSession((String)anyObject());
			expectLastCall().anyTimes();
		}
		catch(com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException  e)
		{
			fail("Exception state should not be thrown in this test: " + e.getMessage());
		}
		catch (MarshallerException e)
		{
			fail("Exception state should not be thrown in this test: " + e.getMessage());
		}
		
		setupMock();
		
		try
		{
			// test returning URL is set correctly
			data.setResponseURL("return.me.here");
			
			result result = this.logoutAuthorityProcessor.execute(data);
						
			assertEquals("Recieved incorrect result. ", SSOProcessor.result.LogoutSuccessful, result);
			
			assertEquals("Unexpected value set for responseURL", "return.me.here", data.getResponseURL());
			
			return;
		}
		catch(InvalidSessionIdentifierException e)
		{
			fail("Unexpected Exception was thrown.");
		}		
		catch(InvalidRequestException e)
		{
			fail("Unexpected Exception was thrown.");
		}
		teardownMock();	
	}
	
	
	/*
	 * Test the execution of an Invalid Logout Request. Invalid session ID should result in Exception being thrown.
	 * 
	 */
	@SuppressWarnings("nls")
	@Test
	public void testExecute3() throws WSClientException
	{		
		data.setSessionID("invalid-blah");
		
		setupMock();
		
		try
		{
			result result = this.logoutAuthorityProcessor.execute(data);

			assertEquals("Recieved incorrect result. ", SSOProcessor.result.LogoutSuccessful, result);

		}
		catch (InvalidSessionIdentifierException e)
		{
			// good, we want one of these
			assert (true);
			return;
		}
		catch (InvalidRequestException e)
		{
			// bad, we don't want one of these
			fail("Unexpected Exception was thrown.");
		}

		// no exception = bad
		fail("Expected Exception was not thrown.");
	}

	
	
	private byte[] generateLogoutResponse(String statusCodeValue, String statusMessage, String inResponseTo)
			throws MarshallerException
	{
		byte[] responseDocument = null;

		NameIDType issuer = new NameIDType();
		issuer.setValue("_1234-spep");

		Status status = new Status();
		StatusCode statusCode = new StatusCode();
		statusCode.setValue(statusCodeValue);
		status.setStatusCode(statusCode);
		status.setStatusMessage(statusMessage);

		StatusResponseType response = new StatusResponseType();
		response.setID("_resp12-123445");
		response.setInResponseTo(inResponseTo);
		response.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		response.setIssuer(issuer);
		response.setSignature(new Signature());
		response.setStatus(status);
		response.setVersion(VersionConstants.saml20);
		
		LogoutResponse logoutResponse = new LogoutResponse(response);

		responseDocument = this.logoutResponseMarshaller.marshallSigned(logoutResponse);

		return responseDocument;
	}

}
