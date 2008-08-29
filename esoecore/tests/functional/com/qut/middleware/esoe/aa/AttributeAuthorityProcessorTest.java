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
package com.qut.middleware.esoe.aa;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.w3._2000._09.xmldsig_.Signature;

import com.qut.middleware.crypto.KeystoreResolver;
import com.qut.middleware.crypto.impl.KeystoreResolverImpl;
import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.aa.bean.AAProcessorData;
import com.qut.middleware.esoe.aa.bean.impl.AAProcessorDataImpl;
import com.qut.middleware.esoe.aa.exception.InvalidPrincipalException;
import com.qut.middleware.esoe.aa.exception.InvalidRequestException;
import com.qut.middleware.esoe.aa.impl.AttributeAuthorityProcessorImpl;
import com.qut.middleware.esoe.logout.LogoutMechanism;
import com.qut.middleware.esoe.logout.LogoutThreadPool;
import com.qut.middleware.esoe.sessions.Create;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.Query;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.esoe.sessions.Terminate;
import com.qut.middleware.esoe.sessions.Update;
import com.qut.middleware.esoe.sessions.bean.IdentityAttribute;
import com.qut.middleware.esoe.sessions.bean.SessionConfigData;
import com.qut.middleware.esoe.sessions.bean.impl.IdentityAttributeImpl;
import com.qut.middleware.esoe.sessions.bean.impl.IdentityDataImpl;
import com.qut.middleware.esoe.sessions.bean.impl.SessionConfigDataImpl;
import com.qut.middleware.esoe.sessions.cache.SessionCache;
import com.qut.middleware.esoe.sessions.cache.impl.SessionCacheImpl;
import com.qut.middleware.esoe.sessions.exception.DuplicateSessionException;
import com.qut.middleware.esoe.sessions.identity.IdentityResolver;
import com.qut.middleware.esoe.sessions.identity.impl.IdentityResolverImpl;
import com.qut.middleware.esoe.sessions.identity.pipeline.Handler;
import com.qut.middleware.esoe.sessions.identity.pipeline.impl.NullHandlerImpl;
import com.qut.middleware.esoe.sessions.impl.CreateImpl;
import com.qut.middleware.esoe.sessions.impl.PrincipalImpl;
import com.qut.middleware.esoe.sessions.impl.QueryImpl;
import com.qut.middleware.esoe.sessions.impl.SessionsProcessorImpl;
import com.qut.middleware.esoe.sessions.impl.TerminateImpl;
import com.qut.middleware.esoe.sessions.impl.UpdateImpl;
import com.qut.middleware.esoe.sessions.sqlmap.SessionsDAO;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.saml2.SchemaConstants;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.identifier.IdentifierCache;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.identifier.impl.IdentifierCacheImpl;
import com.qut.middleware.saml2.identifier.impl.IdentifierGeneratorImpl;
import com.qut.middleware.saml2.schemas.assertion.AttributeType;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.assertion.Subject;
import com.qut.middleware.saml2.schemas.protocol.AttributeQuery;
import com.qut.middleware.saml2.validator.SAMLValidator;
import com.qut.middleware.saml2.validator.impl.SAMLValidatorImpl;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

/**
 * @author shaun
 *
 */
@SuppressWarnings({"nls","unused"})
public class AttributeAuthorityProcessorTest
{
	private AttributeAuthorityProcessor aaProcessor;
	private File xmlConfigFile;
	private URL schemaPath;
	private SessionConfigData sessionConfigData;
	private SessionCache sessionCache;
	private IdentityResolver identityResolver;
	private Handler handler;
	private Create create;
	private Query query;
	private Terminate terminate;
	private Update update;
	private SessionsProcessor sessionsProcessor;
	private IdentifierCache identifierCache;
	private IdentifierGenerator identifierGenerator;
	protected MetadataProcessor metadata;
	private PrivateKey key;
	private String keyName;
	private Marshaller<AttributeQuery> attributeQueryMarshaller;
	private SessionsDAO sessionsDAO;
	private LogoutMechanism logout;
	private LogoutThreadPool logoutThreadPool;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		int skew = 60, interval = 180, timeout = 2;

		this.xmlConfigFile = new File(this.getClass().getResource("sessiondata.xml").toURI());
		this.schemaPath = this.getClass().getResource(SchemaConstants.sessionData);
		
		File attributePolicy = new File("tests" + File.separatorChar + "testdata" + File.separatorChar + "ReleasedAttributes.xml");
		FileInputStream attributeStream = new FileInputStream(attributePolicy);
		byte[] attributeData = new byte[(int)attributePolicy.length()];
		attributeStream.read(attributeData);
		
		String entityID = "http://test.service.com";
		Integer entID = new Integer("1");
		
		this.metadata = createMock(MetadataProcessor.class);
		
		this.sessionsDAO = createMock(SessionsDAO.class);
		expect(sessionsDAO.getEntID(entityID)).andReturn(entID);
		expect(sessionsDAO.selectActiveAttributePolicy(entID)).andReturn(attributeData);
		
		replay(this.metadata);
		replay(this.sessionsDAO);

		this.sessionConfigData = new SessionConfigDataImpl(sessionsDAO, metadata, entityID);
		
		this.logout = createMock(LogoutMechanism.class);
		this.logoutThreadPool = createMock(LogoutThreadPool.class);
		//expect(logout.getEndPoints(entityID)).andReturn(endpoints);
		//expect(logout.performSingleLogout((String)notNull(), (List<String>)notNull(), eq(entityID), anyBoolean())).andReturn(LogoutMechanism.result.LogoutSuccessful).anyTimes();
		replay(this.logout);
		
		this.sessionCache = new SessionCacheImpl(this.logoutThreadPool);
		this.identityResolver = new IdentityResolverImpl(new Vector<Handler>(0,1));
		
		this.handler = new NullHandlerImpl();
		this.identifierCache = new IdentifierCacheImpl();
		this.identifierGenerator = new IdentifierGeneratorImpl(this.identifierCache);
		
		this.create = new CreateImpl(this.sessionCache, this.sessionConfigData, this.identityResolver, this.identifierGenerator, 360);
		this.query = new QueryImpl(this.sessionCache);
		this.terminate = new TerminateImpl(this.sessionCache);
		this.update = new UpdateImpl(this.sessionCache);
		
		this.sessionsProcessor = new SessionsProcessorImpl(this.create, this.query, this.terminate, this.update);
		
		this.identifierGenerator = new IdentifierGeneratorImpl(new IdentifierCacheImpl());
		
		String keyStorePath = "tests" + File.separator + "testdata" + File.separator + "testskeystore.ks";
		String keyStorePassword = "Es0EKs54P4SSPK";
		String esoeKeyAlias = "esoeprimary";
		String esoeKeyPassword = "Es0EKs54P4SSPK";
		
		KeystoreResolver keyStoreResolver = new KeystoreResolverImpl(new File(keyStorePath), keyStorePassword, esoeKeyAlias, esoeKeyPassword);
		
		PublicKey publicKey = keyStoreResolver.resolveKey(esoeKeyAlias);
		
		this.metadata = createMock(MetadataProcessor.class);
		expect(this.metadata.resolveKey(esoeKeyAlias)).andReturn(publicKey).anyTimes();
		
		replay(this.metadata);
		
		this.key = keyStoreResolver.getLocalPrivateKey();
		this.keyName = "esoeprimary";
		SAMLValidator samlValidator = new SAMLValidatorImpl(this.identifierCache, skew);
		
		this.aaProcessor = new AttributeAuthorityProcessorImpl(this.metadata, this.sessionsProcessor, samlValidator, this.identifierGenerator, keyStoreResolver, 60, "_jfalskjflkeworijqowiejroiajsotijaspgkjplakeprtqwoer");
		
		this.attributeQueryMarshaller = new MarshallerImpl<AttributeQuery>(AttributeQuery.class.getPackage().getName(), new String[] { SchemaConstants.samlProtocol },
				keyStoreResolver);
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.aa.AttributeAuthorityProcessor#execute(com.qut.middleware.esoe.aa.bean.AAProcessorData)}.
	 */
	@Test
	public final void testProcessRequest() throws Exception
	{
		String sessionID = this.identifierGenerator.generateSessionID();
		String samlID = this.identifierGenerator.generateSAMLAuthnID();
		
		String usernameKey = "username"; 
		IdentityAttribute usernameValue = new IdentityAttributeImpl();
		usernameValue.setType("String");
		usernameValue.getValues().add("beddoes");
		
		String mailKey = "mail"; 
		IdentityAttribute mailValue = new IdentityAttributeImpl();
		mailValue.setType("String");
		mailValue.getValues().add("beddoes@qut.edu.au");
		
		Principal principal = new PrincipalImpl(new IdentityDataImpl(), 360);
		principal.getAttributes().put(usernameKey, usernameValue);
		principal.getAttributes().put(mailKey, mailValue);
		principal.setPrincipalAuthnIdentifier("beddoes");
		principal.setSAMLAuthnIdentifier(samlID);
		principal.setSessionID(sessionID);
		
		try
		{
			this.sessionCache.addSession(principal);
			this.sessionCache.updateSessionSAMLID(principal);
		}
		catch (DuplicateSessionException e)
		{
			fail("Duplicate session in empty session cache");
		}
		
		AAProcessorData data = new AAProcessorDataImpl();
		String id = "abc"; // AttributeQuery attribute
		String destination = "https://site.url/roar"; // AttributeQuery attribute
		String consent = "https://site.url/roar"; // AttributeQuery attribute
		String issuer = this.identifierGenerator.generateSessionID(); // AttributeQuery / Issuer / NameID value

		byte[] requestDocument = null;
		try
		{
			AttributeQuery attributeQuery = new AttributeQuery();
			attributeQuery.setID(id);
			// NOTE: use of this calendar object relies on default TimeZone (usually UTC). Production
			// code should not rely on this default but should explicitly set TimeZone to UTC+0.
			attributeQuery.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
			attributeQuery.setDestination(destination);
			attributeQuery.setConsent(consent);
			attributeQuery.setVersion("2.0");
			
			Subject subject = new Subject();
			NameIDType subjectNameID = new NameIDType();
			subjectNameID.setValue(samlID);
			
			subject.setNameID(subjectNameID);
			attributeQuery.setSubject(subject);
			
			NameIDType issuerNameID = new NameIDType();
			issuerNameID.setValue(issuer);
			attributeQuery.setIssuer(issuerNameID);
			
			AttributeType attributeUsername = new AttributeType();
			attributeUsername.setName("username");
			AttributeType attributeMail = new AttributeType();
			attributeMail.setName("mail");
			AttributeType attributeSurname = new AttributeType();
			attributeSurname.setName("surname");
			attributeQuery.getAttributes().add(attributeUsername);
			attributeQuery.getAttributes().add(attributeMail);
			attributeQuery.getAttributes().add(attributeSurname);
			
			Signature signature = new Signature();
			attributeQuery.setSignature(signature);
			
			requestDocument = this.attributeQueryMarshaller.marshallSigned(attributeQuery); 
		}
		catch (MarshallerException e)
		{
			e.getCause().printStackTrace();
			fail("Marshaller exception occurred: " + e.getLocalizedMessage() + "\n" + e.getCause().getLocalizedMessage());
		}
		
		if(requestDocument == null || requestDocument.length == 0)
		{
			fail("Request was empty");
			return;
		}
		
		data.setRequestDocument(requestDocument);
		
		System.out.println(new String(requestDocument, "UTF-16"));
		
		try
		{
			this.aaProcessor.execute(data);
		}
		catch (InvalidPrincipalException e)
		{
			fail("Principal was found to be invalid even though it was added to session cache");
		}
		catch (InvalidRequestException e)
		{
			fail("Request was found to be invalid: " + e.getMessage() + e.getCause());
		}
		
		////System.out.println("* - " + data.getResponseDocument());
	}
	
	/**
	 * Test method for {@link com.qut.middleware.esoe.aa.AttributeAuthorityProcessor#execute(com.qut.middleware.esoe.aa.bean.AAProcessorData)}.
	 */
	@Test
	public final void testProcessRequest2()
	{
		String sessionID = this.identifierGenerator.generateSessionID();
		String samlID = this.identifierGenerator.generateSAMLAuthnID();
		
		AAProcessorData data = new AAProcessorDataImpl();
		String id = "abc"; // AttributeQuery attribute
		String destination = "https://site.url/roar"; // AttributeQuery attribute
		String consent = "https://site.url/roar"; // AttributeQuery attribute
		String issuer = this.identifierGenerator.generateSessionID(); // AttributeQuery / Issuer / NameID value

		byte[] requestDocument = null;
		try
		{
			AttributeQuery attributeQuery = new AttributeQuery();
			attributeQuery.setID(id);
			attributeQuery.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
			attributeQuery.setDestination(destination);
			attributeQuery.setConsent(consent);
			attributeQuery.setVersion("2.0");
			
			Subject subject = new Subject();
			NameIDType subjectNameID = new NameIDType();
			subjectNameID.setValue(samlID);
			
			subject.setNameID(subjectNameID);
			attributeQuery.setSubject(subject);
			
			NameIDType issuerNameID = new NameIDType();
			issuerNameID.setValue(issuer);
			attributeQuery.setIssuer(issuerNameID);
			
			AttributeType attributeUsername = new AttributeType();
			attributeUsername.setName("username");
			AttributeType attributeMail = new AttributeType();
			attributeMail.setName("mail");
			AttributeType attributeSurname = new AttributeType();
			attributeSurname.setName("surname");
			attributeQuery.getAttributes().add(attributeUsername);
			attributeQuery.getAttributes().add(attributeMail);
			attributeQuery.getAttributes().add(attributeSurname);
			
			Signature signature = new Signature();
			attributeQuery.setSignature(signature);
			
			requestDocument = this.attributeQueryMarshaller.marshallSigned(attributeQuery); 
		}
		catch (MarshallerException e)
		{
			e.getCause().printStackTrace();
			fail("Marshaller exception occurred: " + e.getLocalizedMessage() + "\n" + e.getCause().getLocalizedMessage());
		}
		
		if(requestDocument == null || requestDocument.length == 0)
		{
			fail("Request was empty");
			return;
		}
		
		data.setRequestDocument(requestDocument);
		
		boolean caught = false;
		
		try
		{
			this.aaProcessor.execute(data);
		}
		catch (InvalidPrincipalException e)
		{
			caught = true;
		}
		catch (InvalidRequestException e)
		{
			fail("Request was found to be invalid: " + e.getMessage() + e.getCause());
		}
		
		assertTrue("Invalid principal was still accepted", caught);
	}
	
	/**
	 * Test method for {@link com.qut.middleware.esoe.aa.AttributeAuthorityProcessor#execute(com.qut.middleware.esoe.aa.bean.AAProcessorData)}.
	 */
	@Test
	public final void testProcessRequest3()
	{
		String sessionID = this.identifierGenerator.generateSessionID();
		String samlID = this.identifierGenerator.generateSAMLAuthnID();
		
		String usernameKey = "username"; 
		IdentityAttribute usernameValue = new IdentityAttributeImpl();
		usernameValue.setType("String");
		usernameValue.getValues().add("beddoes");
		
		String mailKey = "mail"; 
		IdentityAttribute mailValue = new IdentityAttributeImpl();
		mailValue.setType("String");
		mailValue.getValues().add("beddoes@qut.edu.au");
		
		Principal principal = new PrincipalImpl(new IdentityDataImpl(), 360);
		principal.getAttributes().put(usernameKey, usernameValue);
		principal.getAttributes().put(mailKey, mailValue);
		principal.setPrincipalAuthnIdentifier("beddoes");
		principal.setSAMLAuthnIdentifier(samlID);
		principal.setSessionID(sessionID);
		
		try
		{
			this.sessionCache.addSession(principal);
			this.sessionCache.updateSessionSAMLID(principal);
		}
		catch (DuplicateSessionException e)
		{
			fail("Duplicate session in empty session cache");
		}
		
		AAProcessorData data = new AAProcessorDataImpl();
		String id = "abc"; // AttributeQuery attribute
		String destination = "https://site.url/roar"; // AttributeQuery attribute
		String consent = "https://site.url/roar"; // AttributeQuery attribute
		String issuer = this.identifierGenerator.generateSessionID(); // AttributeQuery / Issuer / NameID value

		byte[] requestDocument = null;
		try
		{
			AttributeQuery attributeQuery = new AttributeQuery();
			attributeQuery.setID(id);
			GregorianCalendar instant = new GregorianCalendar();
			Date date = new Date(new Date().getTime() - 60*60*1000); // 1 hour old
			instant.setTime(date);
			attributeQuery.setIssueInstant(new XMLGregorianCalendarImpl(instant));
			attributeQuery.setDestination(destination);
			attributeQuery.setConsent(consent);
			attributeQuery.setVersion("2.0");
			
			Subject subject = new Subject();
			NameIDType subjectNameID = new NameIDType();
			subjectNameID.setValue(samlID);
			
			subject.setNameID(subjectNameID);
			attributeQuery.setSubject(subject);
			
			NameIDType issuerNameID = new NameIDType();
			issuerNameID.setValue(issuer);
			attributeQuery.setIssuer(issuerNameID);
			
			AttributeType attributeUsername = new AttributeType();
			attributeUsername.setName("username");
			AttributeType attributeMail = new AttributeType();
			attributeMail.setName("mail");
			AttributeType attributeSurname = new AttributeType();
			attributeSurname.setName("surname");
			attributeQuery.getAttributes().add(attributeUsername);
			attributeQuery.getAttributes().add(attributeMail);
			attributeQuery.getAttributes().add(attributeSurname);
			
			Signature signature = new Signature();
			attributeQuery.setSignature(signature);
			
			requestDocument = this.attributeQueryMarshaller.marshallSigned(attributeQuery); 
		}
		catch (MarshallerException e)
		{
			e.getCause().printStackTrace();
			fail("Marshaller exception occurred: " + e.getLocalizedMessage() + "\n" + e.getCause().getLocalizedMessage());
		}
		
		if(requestDocument == null || requestDocument.length == 0)
		{
			fail("Request was empty");
			return;
		}
		
		data.setRequestDocument(requestDocument);
		
		boolean caught = false;
		
		try
		{
			this.aaProcessor.execute(data);
		}
		catch (InvalidPrincipalException e)
		{
			fail("Principal was invalid.");
		}
		catch (InvalidRequestException e)
		{
			caught = true;
		}
		
		assertTrue("Expired request was still accepted", caught);
	}
	

	/**
	 * Test method for {@link com.qut.middleware.esoe.aa.AttributeAuthorityProcessor#execute(com.qut.middleware.esoe.aa.bean.AAProcessorData)}.
	 */
	@Test
	public final void testProcessRequest4()
	{
		String sessionID = this.identifierGenerator.generateSessionID();
		String samlID = this.identifierGenerator.generateSAMLAuthnID();
		
		String usernameKey = "username"; 
		IdentityAttribute usernameValue = new IdentityAttributeImpl();
		usernameValue.setType("String");
		usernameValue.getValues().add("beddoes");
		
		String mailKey = "mail"; 
		IdentityAttribute mailValue = new IdentityAttributeImpl();
		mailValue.setType("String");
		mailValue.getValues().add("beddoes@qut.edu.au");
		
		Principal principal = new PrincipalImpl(new IdentityDataImpl(), 360);
		principal.getAttributes().put(usernameKey, usernameValue);
		principal.getAttributes().put(mailKey, mailValue);
		principal.setPrincipalAuthnIdentifier("beddoes");
		principal.setSAMLAuthnIdentifier(samlID);
		principal.setSessionID(sessionID);
		
		try
		{
			this.sessionCache.addSession(principal);
			this.sessionCache.updateSessionSAMLID(principal);
		}
		catch (DuplicateSessionException e)
		{
			fail("Duplicate session in empty session cache");
		}
		
		AAProcessorData data = new AAProcessorDataImpl();
		String id = "abc"; // AttributeQuery attribute
		String destination = "https://site.url/roar"; // AttributeQuery attribute
		String consent = "https://site.url/roar"; // AttributeQuery attribute
		String issuer = this.identifierGenerator.generateSessionID(); // AttributeQuery / Issuer / NameID value

		byte[] requestDocument = null;
		try
		{
			AttributeQuery attributeQuery = new AttributeQuery();
			attributeQuery.setID(id);
			attributeQuery.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
			attributeQuery.setDestination(destination);
			attributeQuery.setConsent(consent);
			attributeQuery.setVersion("2.0");
			
			Subject subject = new Subject();
			NameIDType subjectNameID = new NameIDType();
			subjectNameID.setValue(samlID);
			
			subject.setNameID(subjectNameID);
			attributeQuery.setSubject(subject);
			
			NameIDType issuerNameID = new NameIDType();
			issuerNameID.setValue(issuer);
			attributeQuery.setIssuer(issuerNameID);
			
			Signature signature = new Signature();
			attributeQuery.setSignature(signature);
			
			requestDocument = this.attributeQueryMarshaller.marshallSigned(attributeQuery); 
		}
		catch (MarshallerException e)
		{
			e.getCause().printStackTrace();
			fail("Marshaller exception occurred: " + e.getLocalizedMessage() + "\n" + e.getCause().getLocalizedMessage());
		}
		
		if(requestDocument == null || requestDocument.length == 0)
		{
			fail("Request was empty");
			return;
		}
		
		data.setRequestDocument(requestDocument);
		
		try
		{
			this.aaProcessor.execute(data);
		}
		catch (InvalidPrincipalException e)
		{
			fail("Principal was found to be invalid even though it was added to session cache");
		}
		catch (InvalidRequestException e)
		{
			fail("Request was found to be invalid: " + e.getMessage() + e.getCause());
		}
		
		//System.out.println(new PrettyXml("    ").makePretty(data.getResponseDocument()));
		
		//System.out.println("* - " + data.getResponseDocument());
	}

}
