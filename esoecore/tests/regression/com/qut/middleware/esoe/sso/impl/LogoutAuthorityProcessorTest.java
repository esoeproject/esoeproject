package com.qut.middleware.esoe.sso.impl;

import static org.easymock.EasyMock.anyBoolean;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.JAXBElement;

import org.junit.Before;
import org.junit.Test;
import org.w3._2000._09.xmldsig_.Signature;

import com.qut.middleware.crypto.KeystoreResolver;
import com.qut.middleware.crypto.impl.KeystoreResolverImpl;
import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.logout.LogoutMechanism;
import com.qut.middleware.esoe.logout.LogoutProcessor;
import com.qut.middleware.esoe.logout.LogoutProcessor.result;
import com.qut.middleware.esoe.logout.bean.FailedLogoutRepository;
import com.qut.middleware.esoe.logout.bean.LogoutProcessorData;
import com.qut.middleware.esoe.logout.bean.SSOLogoutState;
import com.qut.middleware.esoe.logout.bean.impl.FailedLogoutRepositoryImpl;
import com.qut.middleware.esoe.logout.exception.InvalidSessionIdentifierException;
import com.qut.middleware.esoe.logout.impl.LogoutProcessorImpl;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.Query;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.esoe.sessions.Terminate;
import com.qut.middleware.esoe.sessions.cache.SessionCache;
import com.qut.middleware.esoe.ws.WSClient;
import com.qut.middleware.metadata.bean.EntityData;
import com.qut.middleware.metadata.bean.saml.SPEPRole;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.saml2.SchemaConstants;
import com.qut.middleware.saml2.StatusCodeConstants;
import com.qut.middleware.saml2.VersionConstants;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.identifier.impl.IdentifierCacheImpl;
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

	private LogoutProcessorImpl logoutAuthorityProcessor;
	private LogoutProcessorData data;
	private FailedLogoutRepository failedLogouts;
	private SAMLValidator samlValidator;
	private SessionsProcessor sessionsProcessor;
	private MetadataProcessor metadata;
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
	private LogoutMechanism logout;

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
			data = createMock(LogoutProcessorData.class);
			sessionsProcessor = createMock(SessionsProcessor.class);
			principal = createMock(Principal.class);
			metadata = createMock(MetadataProcessor.class);
			terminator = createMock(Terminate.class);
			sessionCache = createMock(SessionCache.class);
			wsClient = createMock(WSClient.class);
			query = createMock(Query.class);

		}
		catch (Exception e)
		{
			fail("Unexpected exception state thrown when creating parameters.");
		}

		String entityID1 = "https://spep.test.1/";
		String entityID2 = "https://spep.test.2/";

		testEntities = new Vector<String>();
		testEntities.add(entityID1);
		testEntities.add(entityID2);
		
		List<String> endpoints1 = new ArrayList<String>();
		endpoints1.add(entityID1);

		List<String> endpoints2 = new ArrayList<String>();
		endpoints2.add(entityID2);

		testEndpoints = new Vector<String>();
		testEndpoints.add("http://test.com");

		String keyStorePath = "tests/testdata/testskeystore.ks";
		
		String keyStorePassword = "Es0EKs54P4SSPK";
		String esoeKeyAlias = "esoeprimary";
		String esoeKeyPassword = "Es0EKs54P4SSPK";

		KeystoreResolver keyStoreResolver = new KeystoreResolverImpl(new File(keyStorePath), keyStorePassword, esoeKeyAlias, esoeKeyPassword);

		this.logoutSchemas = new String[] { SchemaConstants.samlProtocol, SchemaConstants.samlAssertion };
		this.logoutResponseMarshaller = new MarshallerImpl<JAXBElement<StatusResponseType>>(StatusResponseType.class.getPackage().getName(), this.logoutSchemas, keyStoreResolver);

		List<String> testIndicies = new Vector<String>();
		testIndicies.add("test1-index");
		testIndicies.add("test2:FFE45C9D00ACFF4EDABB367D-INDEX");

		EntityData entityData1 = createMock(EntityData.class);
		expect(entityData1.getEntityID()).andReturn(entityID1).anyTimes();
		SPEPRole spepRole1 = createMock(SPEPRole.class);
		expect(entityData1.getRoleData(SPEPRole.class)).andReturn(spepRole1).anyTimes();
		
		expect(metadata.getEntityData(entityID1)).andReturn(entityData1).anyTimes();
		expect(metadata.getEntityRoleData(entityID1, SPEPRole.class)).andReturn(spepRole1).anyTimes();
		
		EntityData entityData2 = createMock(EntityData.class);
		expect(entityData2.getEntityID()).andReturn(entityID2).anyTimes();
		SPEPRole spepRole2 = createMock(SPEPRole.class);
		expect(entityData2.getRoleData(SPEPRole.class)).andReturn(spepRole2).anyTimes();
		
		expect(metadata.getEntityData(entityID2)).andReturn(entityData2).anyTimes();
		expect(metadata.getEntityRoleData(entityID2, SPEPRole.class)).andReturn(spepRole2).anyTimes();
		
		
		replay(entityData1); replay(spepRole1);
		replay(entityData2); replay(spepRole2);

		expect(principal.getSessionID()).andReturn(this.validSessionIdentifier).anyTimes();
		expect(principal.getActiveDescriptors()).andReturn(testEntities).anyTimes();
		expect(principal.getSAMLAuthnIdentifier()).andReturn("testSamlID").anyTimes();
		expect(principal.getDescriptorSessionIdentifiers((String) notNull())).andReturn(testIndicies).anyTimes();
		expect(principal.getPrincipalAuthnIdentifier()).andReturn("TestUser:1").anyTimes();
		expect(sessionCache.getSession((String) notNull())).andReturn(principal).anyTimes();
		expect(query.queryAuthnSession((String) notNull())).andReturn(principal).anyTimes();
		expect(sessionsProcessor.getQuery()).andReturn(query).anyTimes();
		expect(sessionsProcessor.getTerminate()).andReturn(terminator).anyTimes();
		//expect(metadata.resolveSingleLogoutService((String) notNull())).andReturn(this.testEndpoints).anyTimes();
		expect(metadata.resolveKey("esoeprimary")).andReturn(keyStoreResolver.getLocalPublicKey()).anyTimes();
		expect(data.getSessionID()).andReturn(this.validSessionIdentifier).anyTimes();
		data.setLogoutStates((List<SSOLogoutState>)notNull());
		expectLastCall().anyTimes();

		this.logout = createMock(LogoutMechanism.class);
		expect(logout.getEndPoints(entityID1)).andReturn(endpoints1);
		expect(logout.performSingleLogout((String)notNull(), (List<String>)notNull(), eq(entityID1), anyBoolean())).andReturn(LogoutMechanism.result.LogoutSuccessful).anyTimes();
		expect(logout.getEndPoints(entityID2)).andReturn(endpoints2);
		expect(logout.performSingleLogout((String)notNull(), (List<String>)notNull(), eq(entityID2), anyBoolean())).andReturn(LogoutMechanism.result.LogoutSuccessful).anyTimes();
		replay(this.logout);
		
		this.logoutAuthorityProcessor = new LogoutProcessorImpl(sessionsProcessor, this.logout);
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
		replay(data);
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
		verify(data);
	}

	/*
	 * Test the execution of a valid Logout Request.
	 * 
	 */
	@SuppressWarnings("nls")
	@Test
	public void testExecute1() throws Exception
	{	
		byte[] responseDoc = this.generateLogoutResponse(StatusCodeConstants.success, "Logged out successfully", "_logreq1234-1234");
		expect(wsClient.singleLogout((byte[])notNull(), (String)notNull())).andReturn(responseDoc).anyTimes();
		this.terminator.terminateSession((String)anyObject());
		expectLastCall().anyTimes();
			
		setupMock();
		
		result result = this.logoutAuthorityProcessor.execute(data);
				
		assertEquals("Recieved incorrect result. ", LogoutProcessor.result.LogoutSuccessful, result);
		
		// check out the logout states, should be one for each active spep 			
		assertEquals("Logout count differes from expected", this.testEntities.size(), data.getLogoutStates().size());
		
		teardownMock();	
	}

	
	/*
	 * Test the execution of a valid Logout Request.
	 * 
	 */
	@SuppressWarnings("nls")
	@Test
	public void testExecute2() throws Exception
	{
		byte[] responseDoc = this.generateLogoutResponse(StatusCodeConstants.success, "Logged out successfully", "_logreq1234-1234");
		expect(wsClient.singleLogout((byte[])notNull(), (String)notNull())).andReturn(responseDoc).anyTimes();
		data.setSessionID(this.validSessionIdentifier);
		this.terminator.terminateSession((String)anyObject());
		expectLastCall().anyTimes();
		
		setupMock();
		
		// Used to check the URL being returned from the logout authority...
		// Removed now since it doesn't get a reference.
		
		result result = this.logoutAuthorityProcessor.execute(data);
					
		assertEquals("Recieved incorrect result. ", LogoutProcessor.result.LogoutSuccessful, result);
		
		teardownMock();	
	}
	
	
	/*
	 * Test the execution of an Invalid Logout Request. Invalid session ID should result in Exception being thrown.
	 * 
	 */
	@SuppressWarnings("nls")
	@Test(expected = InvalidSessionIdentifierException.class)
	public void testExecute3() throws Exception
	{		
		data.setSessionID("invalid-blah");
		
		setupMock();
	
		result result = this.logoutAuthorityProcessor.execute(data);

		assertEquals("Recieved incorrect result. ", LogoutProcessor.result.LogoutSuccessful, result);
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
