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
 * Creation Date: 13/11/2006
 * 
 * Purpose: Tests the Authentication processor.
 */
package com.qut.middleware.spep.authn;

import static com.qut.middleware.test.functional.Capture.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Before;
import org.junit.Test;
import org.w3._2000._09.xmldsig_.Signature;
import org.w3c.dom.Element;

import com.qut.middleware.crypto.KeystoreResolver;
import com.qut.middleware.crypto.impl.KeystoreResolverImpl;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.saml2.AuthenticationContextConstants;
import com.qut.middleware.saml2.ConfirmationMethodConstants;
import com.qut.middleware.saml2.ConsentIdentifierConstants;
import com.qut.middleware.saml2.SchemaConstants;
import com.qut.middleware.saml2.StatusCodeConstants;
import com.qut.middleware.saml2.VersionConstants;
import com.qut.middleware.saml2.exception.InvalidSAMLRequestException;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.Unmarshaller;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.handler.impl.UnmarshallerImpl;
import com.qut.middleware.saml2.identifier.IdentifierCache;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.identifier.impl.IdentifierCacheImpl;
import com.qut.middleware.saml2.identifier.impl.IdentifierGeneratorImpl;
import com.qut.middleware.saml2.schemas.assertion.Assertion;
import com.qut.middleware.saml2.schemas.assertion.AudienceRestriction;
import com.qut.middleware.saml2.schemas.assertion.AuthnContext;
import com.qut.middleware.saml2.schemas.assertion.AuthnStatement;
import com.qut.middleware.saml2.schemas.assertion.ConditionAbstractType;
import com.qut.middleware.saml2.schemas.assertion.Conditions;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.assertion.Subject;
import com.qut.middleware.saml2.schemas.assertion.SubjectConfirmation;
import com.qut.middleware.saml2.schemas.assertion.SubjectConfirmationDataType;
import com.qut.middleware.saml2.schemas.protocol.AuthnRequest;
import com.qut.middleware.saml2.schemas.protocol.LogoutRequest;
import com.qut.middleware.saml2.schemas.protocol.Response;
import com.qut.middleware.saml2.schemas.protocol.Status;
import com.qut.middleware.saml2.schemas.protocol.StatusCode;
import com.qut.middleware.saml2.schemas.protocol.StatusResponseType;
import com.qut.middleware.saml2.validator.SAMLValidator;
import com.qut.middleware.saml2.validator.impl.SAMLValidatorImpl;
import com.qut.middleware.spep.ConfigurationConstants;
import com.qut.middleware.spep.attribute.AttributeProcessor;
import com.qut.middleware.spep.authn.impl.AuthnProcessorDataImpl;
import com.qut.middleware.spep.authn.impl.AuthnProcessorImpl;
import com.qut.middleware.spep.exception.AuthenticationException;
import com.qut.middleware.spep.exception.LogoutException;
import com.qut.middleware.spep.sessions.PrincipalSession;
import com.qut.middleware.spep.sessions.SessionCache;
import com.qut.middleware.spep.sessions.UnauthenticatedSession;
import com.qut.middleware.spep.sessions.impl.PrincipalSessionImpl;
import com.qut.middleware.spep.sessions.impl.UnauthenticatedSessionImpl;
import com.qut.middleware.test.functional.Capture;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

/** */
@SuppressWarnings({"nls","boxing"})
public class AuthnProcessorTest
{
	private String[] schemas = new String[]{SchemaConstants.samlProtocol, SchemaConstants.samlAssertion};
	private AuthnProcessor authnProcessor;
	private String keyName;
	private PrivateKey key;
	private SessionCache sessionCache;
	private IdentifierGenerator identifierGenerator;
	private MetadataProcessor metadata;
	private String sessionID, sessionID2;
	private IdentifierCache identifierCache;
	private SAMLValidator samlValidator;
	private String samlID;
	private String spepIdentifier;
	private Capture<PrincipalSession> captureprincipalSession;
	private AttributeProcessor attributeProcessor;
	private String inResponseTo;
	private String requestURL;
	private PublicKey publicKey;
	private Marshaller<Response> responseMarshaller;
	private String[] logoutSchemas;
	private String logoutPackages;
	private Marshaller<LogoutRequest> logoutRequestMarshaller;
	private Unmarshaller<JAXBElement<StatusResponseType>> logoutResponseUnmarshaller;
	private int assertionConsumerIndex;
	private int attributeConsumingIndex;
	private String assertionConsumerServiceLocation;
	private KeystoreResolver keyStoreResolver;
	private String serviceHost;
	private String ssoURL;
	private HttpServletRequest request;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{		
		File in = new File( "tests" + File.separator + "testdata" + File.separator + "testkeystore.ks");
		this.keyStoreResolver = new KeystoreResolverImpl(in, "Es0EKs54P4SSPK", "esoeprimary", "Es0EKs54P4SSPK");
		this.key = keyStoreResolver.getLocalPrivateKey();
		this.publicKey = keyStoreResolver.getLocalPublicKey();
		this.keyName = keyStoreResolver.getLocalKeyAlias();
		
		this.responseMarshaller = new MarshallerImpl<Response>(Response.class.getPackage().getName(), this.schemas, keyStoreResolver);
		
		this.logoutSchemas = new String[]{SchemaConstants.samlProtocol, SchemaConstants.samlAssertion};
		this.logoutPackages = LogoutRequest.class.getPackage().getName();
		this.logoutRequestMarshaller = new MarshallerImpl<LogoutRequest>(this.logoutPackages, this.logoutSchemas, keyStoreResolver);
		this.logoutResponseUnmarshaller = new UnmarshallerImpl<JAXBElement<StatusResponseType>>(StatusResponseType.class.getPackage().getName(), this.logoutSchemas, keyStoreResolver);

		this.inResponseTo = "a809238409128304912834-182305912038498320984";
		
		this.requestURL = "http://spep.url/request";
		this.assertionConsumerServiceLocation = "http://spep.url/service/sso";
		this.serviceHost = "http://spep.url/service/sso";
		this.ssoURL = "/service/sso";
		
		UnauthenticatedSession unauthenticatedSession = new UnauthenticatedSessionImpl();
		unauthenticatedSession.setRequestURL(this.requestURL);

		this.spepIdentifier = "spep.identifier";
		
		this.identifierCache = createMock(IdentifierCache.class);
		expect(this.identifierCache.containsIdentifier((String)notNull())).andReturn(true).anyTimes();
		this.identifierCache.registerIdentifier((String)notNull());
		expectLastCall().anyTimes();
		
		this.request = createMock(HttpServletRequest.class);
		expect(this.request.getServerName()).andReturn("spep.url").anyTimes();

		this.sessionID = "123412384091283985721938798271394871234871298579-9012385987129347912837491827349871234987";
		this.sessionID2 = "123412384091283985721938798271394871234871298579-9012385987129347912837491827349871234987234";
		this.samlID = "iajoweofijqwoeirjqpweijtpwoiejpoqiwjerpoiquwer";

		this.samlValidator = new SAMLValidatorImpl(this.identifierCache, 180);
		
		this.metadata = createMock(MetadataProcessor.class);
		expect(this.metadata.resolveKey(this.keyName)).andReturn(this.publicKey).anyTimes();
		//expect(this.metadata.getSPEPIdentifier()).andReturn(this.spepIdentifier).anyTimes();
		
		this.captureprincipalSession = new Capture<PrincipalSession>();
		this.sessionCache = createMock(SessionCache.class);
		this.sessionCache.putUnauthenticatedSession((String)notNull(), (UnauthenticatedSession)notNull());
		expectLastCall().anyTimes();
		expect(this.sessionCache.getUnauthenticatedSession(eq(this.inResponseTo))).andReturn(unauthenticatedSession).anyTimes();
		
		this.identifierGenerator = createMock(IdentifierGenerator.class);
		expect(this.identifierGenerator.generateSessionID()).andReturn(this.sessionID).anyTimes();
		expect(this.identifierGenerator.generateSAMLID()).andReturn(this.samlID).anyTimes();
		
		this.attributeProcessor = createMock(AttributeProcessor.class);
		this.attributeProcessor.doAttributeProcessing(capture(this.captureprincipalSession));
		expectLastCall().anyTimes();
		
		this.assertionConsumerIndex = 0;
		this.attributeConsumingIndex = 0;
		
		this.authnProcessor = new AuthnProcessorImpl(this.attributeProcessor, this.metadata, this.sessionCache, this.samlValidator, this.identifierGenerator, keyStoreResolver, this.serviceHost, this.ssoURL, this.assertionConsumerIndex, this.attributeConsumingIndex, this.spepIdentifier);
	}
	
	private void startMock()
	{
		replay(this.metadata);
		replay(this.sessionCache);
		replay(this.identifierGenerator);
		replay(this.identifierCache);
		replay(this.attributeProcessor);
		replay(this.request);
	}
	
	private void endMock()
	{
		verify(this.metadata);
		verify(this.sessionCache);
		verify(this.identifierGenerator);
		verify(this.identifierCache);
		verify(this.attributeProcessor);
		verify(this.request);
	}

	/**
	 * Test method for {@link com.qut.middleware.spep.authn.AuthnProcessor#processAuthnResponse(AuthnProcessorData)}.
	 * Tests to ensure successful authn when principal has not yet engaged a session with this SPEP
	 * @throws Exception 
	 */
	@Test
	public void testProcessAuthnResponse1a() throws Exception
	{
		IdentifierGenerator idGenerator = new IdentifierGeneratorImpl(new IdentifierCacheImpl());
		String samlSessionID = idGenerator.generateSAMLSessionID();
		String samlSessionIndex = idGenerator.generateSAMLSessionID();
		
		byte[] responseDocument;
		
		Response response = generateResponse(idGenerator, samlSessionID, samlSessionIndex, 2);
		
		responseDocument = this.responseMarshaller.marshallSigned(response);
		
		AuthnProcessorData data = new AuthnProcessorDataImpl();
		data.setResponseDocument(responseDocument);
		
		this.sessionCache.putPrincipalSession(eq(this.sessionID), capture(this.captureprincipalSession));
		expectLastCall().atLeastOnce();

		//expect(this.metadata.getSPEPAssertionConsumerLocation()).andReturn(this.assertionConsumerServiceLocation).anyTimes();
		expect(this.sessionCache.getPrincipalSessionByEsoeSessionID(samlSessionID)).andReturn(null);
		
		startMock();
		this.authnProcessor.processAuthnResponse(data);
		
		String returnedSessionID = data.getSessionID();
		
		endMock();
		
		assertEquals("Expected two client sessions", 2, this.captureprincipalSession.getCaptured().size());
		for (PrincipalSession principalSession : this.captureprincipalSession.getCaptured())
		{
			assertEquals(this.sessionID, principalSession.getEsoeSessionIndex().get(samlSessionIndex));
			assertEquals(this.sessionID, returnedSessionID);
			assertEquals(samlSessionID, principalSession.getEsoeSessionID());
			assertEquals(this.requestURL, data.getRequestURL());
		}
	}

	/** Test a failed authn response. This is a serious error on the esoe side and must be logged approriately.
	 * 
	 * Test method for {@link com.qut.middleware.spep.authn.AuthnProcessor#processAuthnResponse(AuthnProcessorData)}.
	 * @throws Exception 
	 */
	@Test (expected = AuthenticationException.class)
	public void testProcessAuthnResponse1aa() throws Exception
	{
		this.sessionCache.putPrincipalSession(eq(this.sessionID), capture(this.captureprincipalSession));
		expectLastCall().atLeastOnce();

		//expect(this.metadata.getSPEPAssertionConsumerLocation()).andReturn(this.assertionConsumerServiceLocation).anyTimes();
		
		startMock();
		
		byte[] responseDocument;
			
		Response response = this.generateFailedAuthnResponse();
		
		responseDocument = this.responseMarshaller.marshallSigned(response);
		
		AuthnProcessorData data = new AuthnProcessorDataImpl();
		data.setResponseDocument(responseDocument);
		
		this.authnProcessor.processAuthnResponse(data);	
		endMock();
	}

	
	/**
	 * Test to ensure correct exception when no matching unauthenticated session is located
	 * Test method for {@link com.qut.middleware.spep.authn.AuthnProcessor#processAuthnResponse(AuthnProcessorData)}.
	 * @throws Exception 
	 */
	@Test(expected = AuthenticationException.class)
	public void testProcessAuthnResponse1b() throws Exception
	{
		this.sessionCache.putPrincipalSession(eq(this.sessionID), capture(this.captureprincipalSession));
		expectLastCall().atLeastOnce();

		//expect(this.metadata.getSPEPAssertionConsumerLocation()).andReturn(this.assertionConsumerServiceLocation).anyTimes();
		
		String wrongID = "some-random-id";
		expect(this.sessionCache.getUnauthenticatedSession(wrongID)).andReturn(null).once();

		startMock();
		
		byte[] responseDocument;
		
		IdentifierGenerator idGenerator = new IdentifierGeneratorImpl(this.identifierCache);
		String samlSessionID = idGenerator.generateSAMLSessionID();
		String samlSessionIndex = idGenerator.generateSAMLSessionID();
		
		Response response = generateResponse(idGenerator, samlSessionID, samlSessionIndex, 2);
		response.setInResponseTo(wrongID);
		
		responseDocument = this.responseMarshaller.marshallSigned(response);
		
		AuthnProcessorData data = new AuthnProcessorDataImpl();
		data.setResponseDocument(responseDocument);
		
		this.authnProcessor.processAuthnResponse(data);
		
	}

	
	/** Test a Response with a destination filed that does not match the target SPEP.
	 * 
	 * Test method for {@link com.qut.middleware.spep.authn.AuthnProcessor#processAuthnResponse(AuthnProcessorData)}.
	 * @throws Exception 
	 */
	@Test(expected = AuthenticationException.class)
	public void testProcessAuthnResponse1c() throws Exception
	{
		IdentifierGenerator idGenerator = new IdentifierGeneratorImpl(this.identifierCache);	
		String samlSessionID = idGenerator.generateSAMLSessionID();
		String samlSessionIndex = idGenerator.generateSAMLSessionID();
		
		expect(this.sessionCache.getPrincipalSessionByEsoeSessionID(samlSessionID)).andReturn(null);
		
		this.sessionCache.putPrincipalSession(eq(this.sessionID), capture(this.captureprincipalSession));
		expectLastCall().atLeastOnce();

		//expect(this.metadata.getSPEPAssertionConsumerLocation()).andReturn("new.service.location/hello").anyTimes();
		
		startMock();
		
		byte[] responseDocument;	
		
		Response response = generateResponse(idGenerator, samlSessionID, samlSessionIndex, 2);
		response.setInResponseTo(this.inResponseTo);
		
		responseDocument = this.responseMarshaller.marshallSigned(response);
		
		AuthnProcessorData data = new AuthnProcessorDataImpl();
		data.setResponseDocument(responseDocument);
		
		this.authnProcessor.processAuthnResponse(data);
		
	}
	
	
	/**
	 *  Test that an expired authnstatement will be rejected.
	 *  
	 * Test method for {@link com.qut.middleware.spep.authn.AuthnProcessor#processAuthnResponse(AuthnProcessorData)}.
	 * @throws Exception 
	 */
	@Test (expected = AuthenticationException.class)
	public void testProcessAuthnResponse1d() throws Exception
	{
		IdentifierGenerator idGenerator = new IdentifierGeneratorImpl(this.identifierCache);
		String samlSessionID = idGenerator.generateSAMLSessionID();
		String samlSessionIndex = idGenerator.generateSAMLSessionID();
		
		byte[] responseDocument;
		
		Response response = generateResponse(idGenerator, samlSessionID, samlSessionIndex, -1);
		
		responseDocument = this.responseMarshaller.marshallSigned(response);
		
		AuthnProcessorData data = new AuthnProcessorDataImpl();
		data.setResponseDocument(responseDocument);
		
		this.sessionCache.putPrincipalSession(eq(this.sessionID), capture(this.captureprincipalSession));
		expectLastCall().atLeastOnce();

		//expect(this.metadata.getSPEPAssertionConsumerLocation()).andReturn(this.assertionConsumerServiceLocation).anyTimes();
		expect(this.sessionCache.getPrincipalSessionByEsoeSessionID(samlSessionID)).andReturn(null);
		
		startMock();
		
		this.authnProcessor.processAuthnResponse(data);		
		endMock();
	}
	
	/**
	 * Test method for {@link com.qut.middleware.spep.authn.AuthnProcessor#processAuthnResponse(AuthnProcessorData)}.
	 * Tests to ensure that correct principal data is returned and updated when seperate new session is established by principal
	 * who already has a unique local session
	 * @throws Exception 
	 */
	@Test
	public void testProcessAuthnResponse1e() throws Exception
	{
		IdentifierGenerator idGenerator = new IdentifierGeneratorImpl(new IdentifierCacheImpl());
		byte[] responseDocument;
		
		String samlSessionID = idGenerator.generateSAMLSessionID();
		
		/* Generate a principal as if they have already established a session */
		String samlSessionIndex = idGenerator.generateSAMLSessionID();
		
		PrincipalSession principalSession = new PrincipalSessionImpl();
		principalSession.setEsoeSessionID(samlSessionID);
		principalSession.addESOESessionIndexAndLocalSessionID(samlSessionIndex, this.sessionID2);
		
		TimeZone utc = new SimpleTimeZone(0, "UTC"); 
		GregorianCalendar cal = new GregorianCalendar(utc);
		cal.add(Calendar.SECOND, 60);
		principalSession.setSessionNotOnOrAfter(cal.getTime());
		
		/* Generate the second request, noting same samlSessionID but unique sessionIndex */
		String samlSessionIndex2 = idGenerator.generateSAMLSessionID();
		Response response = generateResponse(idGenerator, samlSessionID, samlSessionIndex2, 2);
		responseDocument = this.responseMarshaller.marshallSigned(response);
		AuthnProcessorData data = new AuthnProcessorDataImpl();
		data.setResponseDocument(responseDocument);
		
		//expect(this.metadata.getSPEPAssertionConsumerLocation()).andReturn(this.assertionConsumerServiceLocation).anyTimes();
		expect(this.sessionCache.getPrincipalSessionByEsoeSessionID(samlSessionID)).andReturn(principalSession);
		this.sessionCache.putPrincipalSession(eq(this.sessionID), capture(this.captureprincipalSession));
		expectLastCall().atLeastOnce();
		
		
		startMock();
		this.authnProcessor.processAuthnResponse(data);;
		
		String returnedSessionID = data.getSessionID();
		
		endMock();
		endMock();
		
		assertEquals("Expected two client sessions", 2, this.captureprincipalSession.getCaptured().size());
		for (PrincipalSession ps : this.captureprincipalSession.getCaptured())
		{
			assertEquals(this.sessionID2, ps.getEsoeSessionIndex().get(samlSessionIndex));
			assertEquals(this.sessionID, ps.getEsoeSessionIndex().get(samlSessionIndex2));
			assertEquals(this.sessionID, returnedSessionID);
			assertEquals(samlSessionID, ps.getEsoeSessionID());
			assertEquals(this.requestURL, data.getRequestURL());
		}
	}
	
	/**
	 * Test method for {@link com.qut.middleware.spep.authn.AuthnProcessor#processAuthnResponse(AuthnProcessorData)}.
	 * Tests to ensure that new esoe session index is added when principal who already has an active session starts a new session on the local SPEP
	 * @throws Exception 
	 */
	@Test
	public void testProcessAuthnResponse1f() throws Exception
	{
		IdentifierGenerator idGenerator = new IdentifierGeneratorImpl(new IdentifierCacheImpl());
		byte[] responseDocument;
		
		String samlSessionID = idGenerator.generateSAMLSessionID();
		
		/* Generate a principal as if they have already established a session */
		String samlSessionIndex = idGenerator.generateSAMLSessionID();
		
		PrincipalSession principalSession = new PrincipalSessionImpl();
		principalSession.setEsoeSessionID(samlSessionID);
		principalSession.addESOESessionIndexAndLocalSessionID(samlSessionIndex, this.sessionID2);
		
		TimeZone utc = new SimpleTimeZone(0, "UTC"); 
		GregorianCalendar cal = new GregorianCalendar(utc);
		cal.add(Calendar.HOUR, 1);
		principalSession.setSessionNotOnOrAfter(cal.getTime());
		
		/* Generate the second request, noting same samlSessionID but unique sessionIndex */
		String samlSessionIndex2 = idGenerator.generateSAMLSessionID();
		Response response = generateResponse(idGenerator, samlSessionID, samlSessionIndex2, 2);
		responseDocument = this.responseMarshaller.marshallSigned(response);
		AuthnProcessorData data = new AuthnProcessorDataImpl();
		data.setResponseDocument(responseDocument);
		
		//expect(this.metadata.getSPEPAssertionConsumerLocation()).andReturn(this.assertionConsumerServiceLocation).anyTimes();
		expect(this.sessionCache.getPrincipalSessionByEsoeSessionID(samlSessionID)).andReturn(principalSession);
		this.sessionCache.putPrincipalSession(eq(this.sessionID), capture(this.captureprincipalSession));
		expectLastCall().atLeastOnce();
		
		startMock();
		
		this.authnProcessor.processAuthnResponse(data);
		endMock();
		
		assertTrue( principalSession.getEsoeSessionIndex().containsKey(samlSessionIndex2) );
	}
	
	/**
	 * Test method for {@link com.qut.middleware.spep.authn.AuthnProcessor#processAuthnResponse(AuthnProcessorData)}.
	 * @throws Exception 
	 */
	@Test(expected = AuthenticationException.class)
	public void testProcessAuthnResponse2a() throws Exception
	{
		startMock();
		
		this.authnProcessor.processAuthnResponse(null);
		
		endMock();
	}
	

	/**
	 * Test method for {@link com.qut.middleware.spep.authn.AuthnProcessor#verifySession(String)}
	 * @throws Exception 
	 */
	@Test
	public void testVerifySession1() throws Exception
	{
		PrincipalSession principalSession = createMock(PrincipalSession.class);

		replay(principalSession);
		
		expect(this.sessionCache.getPrincipalSession(this.sessionID)).andReturn(principalSession);
		
		startMock();
		
		assertEquals(principalSession, this.authnProcessor.verifySession(this.sessionID));
		
		endMock();
		
		verify(principalSession);
	}
	

	/**
	 * Test method for {@link com.qut.middleware.spep.authn.AuthnProcessor#verifySession(String)}
	 * @throws Exception 
	 */
	@Test
	public void testVerifySession2() throws Exception
	{
		PrincipalSession principalSession = null;

		expect(this.sessionCache.getPrincipalSession(this.sessionID)).andReturn(principalSession);
		
		startMock();
		
		assertEquals(principalSession, this.authnProcessor.verifySession(this.sessionID));
		
		endMock();
	}
	
	/**
	 * Test method for {@link com.qut.middleware.spep.authn.AuthnProcessor#generateAuthnRequest(AuthnProcessorData)}.
	 * @throws UnmarshallerException 
	 * @throws InvalidSAMLRequestException 
	 * @throws AuthenticationException 
	 */
	@Test
	public void testGenerateAuthnRequest() throws UnmarshallerException, InvalidSAMLRequestException, AuthenticationException
	{
		startMock();

		AuthnProcessorData data = new AuthnProcessorDataImpl();
		data.setRequest(this.request);


		this.authnProcessor.generateAuthnRequest(data);
		byte[] requestDocument = data.getRequestDocument();
		
		//System.out.println(new PrettyXml("    ").makePretty(requestDocument));
		
		Unmarshaller<AuthnRequest> authnRequestUnmarshaller = new UnmarshallerImpl<AuthnRequest>(AuthnRequest.class.getPackage().getName(), this.schemas);
		
		AuthnRequest authnRequest = authnRequestUnmarshaller.unMarshallUnSigned(requestDocument);
		
		this.samlValidator.getRequestValidator().validate(authnRequest);
		
		endMock();
	}
	
	/**
	 * Tests to ensure that a principal whom has multiple local sessions has them all terminated when LogoutRequest with all local SessionIndex's is recieved
	 * Test method for {@link com.qut.middleware.spep.authn.AuthnProcessor#generateAuthnRequest(AuthnProcessorData)}.
	 * @throws Exception 
	 */
	@Test
	public void testLogoutPrincipal1a() throws Exception
	{
		String ESOESessionID = "_12345-12345";
		
		/* Setup our ESOE provided SessionIndex's and Local sessionID's which are stored in users client */
		String sessionIndex1 = "12345abc";
		String sessionID1 = "_10809809t0q9w0e9it0923i9i02395ui092135i09i35-0i2-059i09qiw059iu02395i09iq0395u03";
		
		String sessionIndex2 = "12345def";
		String sessionID2 = "_20809809t0q9w0e9it0923i9i02395ui092135i09i35-0i2-059i09qiw059iu02395i09iq0395u03";
		
		String sessionIndex3 = "12345ghi";
		String sessionID3 = "_30809809t0q9w0e9it0923i9i02395ui092135i09i35-0i2-059i09qiw059iu02395i09iq0395u03";
		
		String sessionIndex4 = "12345jkl";
		String sessionID4 = "_40809809t0q9w0e9it0923i9i02395ui092135i09i35-0i2-059i09qiw059iu02395i09iq0395u03";
		
		TimeZone utc = new SimpleTimeZone(0, "UTC"); 
		GregorianCalendar cal = new GregorianCalendar(utc);
		cal.add(Calendar.SECOND, 360);
		
		PrincipalSession principalSession = new PrincipalSessionImpl();

		principalSession.setEsoeSessionID(ESOESessionID);
		principalSession.setSessionNotOnOrAfter(cal.getTime());
		principalSession.addESOESessionIndexAndLocalSessionID(sessionIndex1, sessionID1);
		principalSession.addESOESessionIndexAndLocalSessionID(sessionIndex2, sessionID2);
		principalSession.addESOESessionIndexAndLocalSessionID(sessionIndex3, sessionID3);
		principalSession.addESOESessionIndexAndLocalSessionID(sessionIndex4, sessionID4);

		List<String> sessionIndices = new Vector<String>();
		sessionIndices.add(sessionIndex1);
		sessionIndices.add(sessionIndex2);
		sessionIndices.add(sessionIndex3);
		sessionIndices.add(sessionIndex4);
		
		Element requestDocument = generateLogoutRequest(this.samlID, sessionIndices, ESOESessionID);
		
		expect(this.sessionCache.getPrincipalSessionByEsoeSessionID(ESOESessionID)).andReturn(principalSession);
		
		this.sessionCache.terminateIndividualPrincipalSession(principalSession, sessionIndex1); 
		this.sessionCache.terminateIndividualPrincipalSession(principalSession, sessionIndex2); 
		this.sessionCache.terminateIndividualPrincipalSession(principalSession, sessionIndex3); 
		this.sessionCache.terminateIndividualPrincipalSession(principalSession, sessionIndex4); 

		startMock();
		
		Element responseDocument = this.authnProcessor.logoutPrincipal(requestDocument);
		
		JAXBElement<StatusResponseType> response = this.logoutResponseUnmarshaller.unMarshallSigned(responseDocument);
		assertTrue("Ensures the response document is generated in a success state", response.getValue().getStatus().getStatusCode().getValue().equals(StatusCodeConstants.success));
		
		endMock();
	}
	
	/**
	 * Tests to ensure that a principal whom has multiple local sessions has only those specified in the request removed if all are not presented by the ESOE
	 * Test method for {@link com.qut.middleware.spep.authn.AuthnProcessor#generateAuthnRequest(AuthnProcessorData)}.
	 * @throws Exception 
	 */
	@Test
	public void testLogoutPrincipal1b() throws Exception
	{
		String ESOESessionID = "_12345-12345";
		
		/* Setup our ESOE provided SessionIndex's and Local sessionID's which are stored in users client */
		String sessionIndex1 = "12345abc";
		String sessionID1 = "_10809809t0q9w0e9it0923i9i02395ui092135i09i35-0i2-059i09qiw059iu02395i09iq0395u03";
		
		String sessionIndex2 = "12345def";
		String sessionID2 = "_20809809t0q9w0e9it0923i9i02395ui092135i09i35-0i2-059i09qiw059iu02395i09iq0395u03";
		
		String sessionIndex3 = "12345ghi";
		String sessionID3 = "_30809809t0q9w0e9it0923i9i02395ui092135i09i35-0i2-059i09qiw059iu02395i09iq0395u03";
		
		String sessionIndex4 = "12345jkl";
		String sessionID4 = "_40809809t0q9w0e9it0923i9i02395ui092135i09i35-0i2-059i09qiw059iu02395i09iq0395u03";
		
		TimeZone utc = new SimpleTimeZone(0, "UTC"); 
		GregorianCalendar cal = new GregorianCalendar(utc);
		cal.add(Calendar.SECOND, 360);
		
		PrincipalSession principalSession = new PrincipalSessionImpl();

		principalSession.setEsoeSessionID(ESOESessionID);
		principalSession.setSessionNotOnOrAfter(cal.getTime());
		principalSession.addESOESessionIndexAndLocalSessionID(sessionIndex1, sessionID1);
		principalSession.addESOESessionIndexAndLocalSessionID(sessionIndex2, sessionID2);
		principalSession.addESOESessionIndexAndLocalSessionID(sessionIndex3, sessionID3);
		principalSession.addESOESessionIndexAndLocalSessionID(sessionIndex4, sessionID4);

		List<String> sessionIndices = new Vector<String>();
		sessionIndices.add(sessionIndex1);
		sessionIndices.add(sessionIndex2);
		
		Element requestDocument = generateLogoutRequest(this.samlID, sessionIndices, ESOESessionID);
		
		expect(this.sessionCache.getPrincipalSessionByEsoeSessionID(ESOESessionID)).andReturn(principalSession);
		
		this.sessionCache.terminateIndividualPrincipalSession(principalSession, sessionIndex1); 
		this.sessionCache.terminateIndividualPrincipalSession(principalSession, sessionIndex2);  

		startMock();
		
		Element responseDocument = this.authnProcessor.logoutPrincipal(requestDocument);
		
		JAXBElement<StatusResponseType> response = this.logoutResponseUnmarshaller.unMarshallSigned(responseDocument);
		assertTrue("Ensures the response document is generated in a success state", response.getValue().getStatus().getStatusCode().getValue().equals(StatusCodeConstants.success));
		
		endMock();
	}
	
	/**
	 * Tests to ensure that a principal whom has multiple local sessions has them all removed if the ESOE specifies no session indexs
	 * Test method for {@link com.qut.middleware.spep.authn.AuthnProcessor#generateAuthnRequest(AuthnProcessorData)}.
	 * @throws Exception 
	 */
	@Test
	public void testLogoutPrincipal1c() throws Exception
	{
		String ESOESessionID = "_12345-12345";
		
		/* Setup our ESOE provided SessionIndex's and Local sessionID's which are stored in users client */
		String sessionIndex1 = "12345abc";
		String sessionID1 = "_10809809t0q9w0e9it0923i9i02395ui092135i09i35-0i2-059i09qiw059iu02395i09iq0395u03";
		
		String sessionIndex2 = "12345def";
		String sessionID2 = "_20809809t0q9w0e9it0923i9i02395ui092135i09i35-0i2-059i09qiw059iu02395i09iq0395u03";
		
		String sessionIndex3 = "12345ghi";
		String sessionID3 = "_30809809t0q9w0e9it0923i9i02395ui092135i09i35-0i2-059i09qiw059iu02395i09iq0395u03";
		
		String sessionIndex4 = "12345jkl";
		String sessionID4 = "_40809809t0q9w0e9it0923i9i02395ui092135i09i35-0i2-059i09qiw059iu02395i09iq0395u03";
		
		TimeZone utc = new SimpleTimeZone(0, "UTC"); 
		GregorianCalendar cal = new GregorianCalendar(utc);
		cal.add(Calendar.SECOND, 360);
		
		PrincipalSession principalSession = new PrincipalSessionImpl();

		principalSession.setEsoeSessionID(ESOESessionID);
		principalSession.setSessionNotOnOrAfter(cal.getTime());
		principalSession.addESOESessionIndexAndLocalSessionID(sessionIndex1, sessionID1);
		principalSession.addESOESessionIndexAndLocalSessionID(sessionIndex2, sessionID2);
		principalSession.addESOESessionIndexAndLocalSessionID(sessionIndex3, sessionID3);
		principalSession.addESOESessionIndexAndLocalSessionID(sessionIndex4, sessionID4);

		List<String> sessionIndices = new Vector<String>();
		
		Element requestDocument = generateLogoutRequest(this.samlID, sessionIndices, ESOESessionID);
		
		expect(this.sessionCache.getPrincipalSessionByEsoeSessionID(ESOESessionID)).andReturn(principalSession);
		
		this.sessionCache.terminatePrincipalSession(principalSession);  

		startMock();
		
		Element responseDocument = this.authnProcessor.logoutPrincipal(requestDocument);
		
		JAXBElement<StatusResponseType> response = this.logoutResponseUnmarshaller.unMarshallSigned(responseDocument);
		assertTrue("Ensures the response document is generated in a success state", response.getValue().getStatus().getStatusCode().getValue().equals(StatusCodeConstants.success));
		
		
		endMock();
	}
	
	/**
	 * Ensure that for a principal who is not known to the local system that a status of unknown is returned
	 * Test method for {@link com.qut.middleware.spep.authn.AuthnProcessor#generateAuthnRequest(AuthnProcessorData)}.
	 * @throws Exception 
	 */
	@Test
	public void testLogoutPrincipal2() throws Exception
	{
		String ESOESessionID = "_12345-12345";
		
		/* Setup our ESOE provided SessionIndex's and Local sessionID's which are stored in users client */
		String sessionIndex1 = "12345abc";
		String sessionID1 = "_10809809t0q9w0e9it0923i9i02395ui092135i09i35-0i2-059i09qiw059iu02395i09iq0395u03";
		
		String sessionIndex2 = "12345def";
		String sessionID2 = "_20809809t0q9w0e9it0923i9i02395ui092135i09i35-0i2-059i09qiw059iu02395i09iq0395u03";
		
		TimeZone utc = new SimpleTimeZone(0, "UTC"); 
		GregorianCalendar cal = new GregorianCalendar(utc);
		cal.add(Calendar.SECOND, 360);

		List<String> sessionIndices = new Vector<String>();
		sessionIndices.add(sessionIndex1);
		sessionIndices.add(sessionIndex2);
		
		Element requestDocument = generateLogoutRequest(this.samlID, sessionIndices, ESOESessionID);
		
		expect(this.sessionCache.getPrincipalSessionByEsoeSessionID(ESOESessionID)).andReturn(null);
		startMock();
		
		Element responseDocument = null;
		try
		{
			responseDocument = this.authnProcessor.logoutPrincipal(requestDocument);
		}
		catch (LogoutException e)
		{
			// We actually expect this
		}
		
		JAXBElement<StatusResponseType> response = this.logoutResponseUnmarshaller.unMarshallSigned(responseDocument);
		assertTrue("Ensures the response document is generated in a success state", response.getValue().getStatus().getStatusCode().getValue().equals(StatusCodeConstants.unknownPrincipal));
				
		endMock();
	}
	
	
	
	
	/** Test invalid constructor arguments.
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction1() throws Exception
	{
		this.authnProcessor = new AuthnProcessorImpl(null, this.metadata, this.sessionCache, this.samlValidator, this.identifierGenerator, keyStoreResolver, this.serviceHost, this.ssoURL, this.assertionConsumerIndex, this.attributeConsumingIndex, this.spepIdentifier);
	}
	
	/** Test invalid constructor arguments.
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction2() throws Exception
	{
		this.authnProcessor = new AuthnProcessorImpl(this.attributeProcessor, null, this.sessionCache, this.samlValidator, this.identifierGenerator, keyStoreResolver, this.serviceHost, this.ssoURL, this.assertionConsumerIndex, this.attributeConsumingIndex, this.spepIdentifier);
	}
	
	/** Test invalid constructor arguments.
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction3() throws Exception
	{
		this.authnProcessor = new AuthnProcessorImpl(this.attributeProcessor, this.metadata, null, this.samlValidator, this.identifierGenerator, keyStoreResolver, this.serviceHost, this.ssoURL, this.assertionConsumerIndex, this.attributeConsumingIndex, this.spepIdentifier);
	}
	
	/** Test invalid constructor arguments.
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction4() throws Exception
	{
		this.authnProcessor = new AuthnProcessorImpl(this.attributeProcessor, this.metadata, this.sessionCache, null, this.identifierGenerator, keyStoreResolver, this.serviceHost, this.ssoURL, this.assertionConsumerIndex, this.attributeConsumingIndex, this.spepIdentifier);
	}
	
	/** Test invalid constructor arguments.
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction5() throws Exception
	{
		this.authnProcessor = new AuthnProcessorImpl(this.attributeProcessor, this.metadata, this.sessionCache, this.samlValidator, null, this.keyStoreResolver, this.serviceHost, this.ssoURL, this.assertionConsumerIndex, this.attributeConsumingIndex, this.spepIdentifier);
	}
	
	/** Test invalid constructor arguments.
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction6() throws Exception
	{
		this.authnProcessor = new AuthnProcessorImpl(this.attributeProcessor, this.metadata, this.sessionCache, this.samlValidator, this.identifierGenerator, keyStoreResolver, this.serviceHost, this.ssoURL, -1133, this.attributeConsumingIndex, this.spepIdentifier);
	}
	
	/** Test invalid constructor arguments.
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction7() throws Exception
	{
		this.authnProcessor = new AuthnProcessorImpl(this.attributeProcessor, this.metadata, this.sessionCache, this.samlValidator, this.identifierGenerator, keyStoreResolver, this.serviceHost, this.ssoURL, this.assertionConsumerIndex, -3847584, this.spepIdentifier);
	}
	
	/** Test invalid constructor arguments.
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction8() throws Exception
	{
		this.authnProcessor = new AuthnProcessorImpl(this.attributeProcessor, this.metadata, this.sessionCache, this.samlValidator, this.identifierGenerator, keyStoreResolver, this.serviceHost, this.ssoURL, this.assertionConsumerIndex, this.attributeConsumingIndex, null);
	}
	
	
	
	private Element generateLogoutRequest(String generatedSAMLID, List<String> sessionIndices, String nameID) throws MarshallerException
	{
		NameIDType issuer = new NameIDType();
		issuer.setValue(this.spepIdentifier);
		NameIDType principal = new NameIDType();
		principal.setValue(nameID);
		
		GregorianCalendar future = new GregorianCalendar();
		future.add(Calendar.HOUR, 1);
		
		LogoutRequest logoutRequest = new LogoutRequest();
		logoutRequest.setID(generatedSAMLID);
		logoutRequest.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		logoutRequest.setIssuer(issuer);
		logoutRequest.setNameID(principal);
		logoutRequest.setNotOnOrAfter(new XMLGregorianCalendarImpl(future));
		logoutRequest.setReason("just because.");
		logoutRequest.setSignature(new Signature());
		logoutRequest.setVersion(VersionConstants.saml20);
		
		logoutRequest.getSessionIndices().addAll(sessionIndices);
		
		return this.logoutRequestMarshaller.marshallSignedElement(logoutRequest);
	}
	
	
	/* Generates a successful authn response.
	 * 
	 */
	private Response generateResponse(IdentifierGenerator idGenerator, String samlSessionID, String samlSessionIndex, int sessionLifetime)
	{
		String esoeIdentifier = "esoe.url";
		NameIDType issuer = new NameIDType();
		issuer.setValue(esoeIdentifier);
		
		NameIDType subjectNameID = new NameIDType();
		subjectNameID.setValue(samlSessionID);
		
		Subject subject = new Subject();
		subject.setNameID(subjectNameID);
		
		AuthnContext authnContext = new AuthnContext();
		authnContext.setAuthnContextClassRef(AuthenticationContextConstants.previousSession);
		
		GregorianCalendar expiry = new GregorianCalendar();
		expiry.add(Calendar.HOUR, sessionLifetime);
		
		Status status = new Status();
		StatusCode statusCode = new StatusCode();
		statusCode.setValue(StatusCodeConstants.success);
		status.setStatusCode(statusCode);
		
		AuthnStatement authnStatement = new AuthnStatement();
		authnStatement.setAuthnContext(authnContext);
		authnStatement.setAuthnInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		authnStatement.setSessionNotOnOrAfter(new XMLGregorianCalendarImpl(expiry));
		authnStatement.setSessionIndex(samlSessionIndex);
		
		Assertion assertion = new Assertion();
	
		Conditions conditions = new Conditions();
		conditions.setNotOnOrAfter(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		
		// set audience restriction to SPEP receiving the response. IE this SPEP ID
		List<ConditionAbstractType> audienceRestrictions = conditions.getConditionsAndOneTimeUsesAndAudienceRestrictions();
		AudienceRestriction restrict = new AudienceRestriction();
		restrict.getAudiences().add(this.assertionConsumerServiceLocation);
		audienceRestrictions.add(restrict);
		
		/* subject MUST contain a SubjectConfirmation */
		SubjectConfirmation confirmation = new SubjectConfirmation();
		confirmation.setMethod(ConfirmationMethodConstants.bearer);
		SubjectConfirmationDataType confirmationData = new SubjectConfirmationDataType();
		confirmationData.setRecipient(this.assertionConsumerServiceLocation);
		confirmationData.setInResponseTo(this.spepIdentifier);
		confirmationData.setNotOnOrAfter(this.generateXMLCalendar(10));
		confirmation.setSubjectConfirmationData(confirmationData);
		
		subject.getSubjectConfirmationNonID().add(confirmation);
		
		String assID = idGenerator.generateSAMLID();
		assertion.setID(assID);
		assertion.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		assertion.setIssuer(issuer);
		assertion.setVersion(VersionConstants.saml20);
		assertion.getAuthnStatementsAndAuthzDecisionStatementsAndAttributeStatements().add(authnStatement);
		assertion.setConditions(conditions);
		assertion.setSubject(subject);
		
		Response response = new Response();
		
		
		String resID = idGenerator.generateSAMLID();
		response.setID(resID);
		response.setIssuer(issuer);
		
		TimeZone utc = new SimpleTimeZone(0, "UTC"); 
		GregorianCalendar cal = new GregorianCalendar(utc);
		
		response.setIssueInstant(new XMLGregorianCalendarImpl(cal));
		response.setInResponseTo(this.inResponseTo);
		response.setSignature(new Signature());
		response.setVersion(VersionConstants.saml20);
		response.getEncryptedAssertionsAndAssertions().add(assertion);
		response.setStatus(status);
		
		response.setDestination(this.assertionConsumerServiceLocation);
		
		return response;
	}
	
	
	/* Generate a failed authn response.
	 * 
	 */
	private Response generateFailedAuthnResponse()
	{
		Signature signature;
		Status status;
		StatusCode statusCode, embededStatusCode;
		Response response;

		/* Generate failed status, two layers of code supplied */
		statusCode = new StatusCode();
		statusCode.setValue(StatusCodeConstants.authnFailed);
		embededStatusCode = new StatusCode();
		embededStatusCode.setValue("Authn failedon ESOE. Better luck next time.");
		statusCode.setStatusCode(embededStatusCode);

		status = new Status();
		status.setStatusCode(statusCode);
		status.setStatusMessage("Authn failed on ESOE. Better luck next time.");

		/* Generate Issuer to attach to response */
		String esoeIdentifier = "esoe.url";
		NameIDType issuer = new NameIDType();
		issuer.setValue(esoeIdentifier);

		/* Generate placeholder <Signature/> block for SAML2lib-j in response */
		signature = new Signature();

		/* Generate our response */
		response = new Response();
		response.setID(this.identifierGenerator.generateSAMLID());
		response.setInResponseTo(this.inResponseTo);
		response.setVersion(VersionConstants.saml20);
		response.setIssueInstant(generateXMLCalendar(0));
		response.setDestination(this.assertionConsumerServiceLocation);
		response.setConsent(ConsentIdentifierConstants.unspecified);

		response.setIssuer(issuer);
		response.setSignature(signature);
		response.setStatus(status);

		return response;
	}
	
	/**
	 * Generates an XML calander instance.
	 * 
	 * @param offset
	 *            How long into the future the calendar should represent in seconds
	 * @return The created calendar for the current time + offset
	 */
	private XMLGregorianCalendar generateXMLCalendar(int offset)
	{		
		// Timestamps MUST be set to UTC
		TimeZone utc = new SimpleTimeZone(0, ConfigurationConstants.timeZone); 
		GregorianCalendar cal = new GregorianCalendar(utc);
		cal.add(Calendar.SECOND, offset);
		return new XMLGregorianCalendarImpl(cal);
		
	}
}
