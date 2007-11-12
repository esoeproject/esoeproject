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
package com.qut.middleware.esoe.sessions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.easymock.EasyMock.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.LdapTemplate;
import org.springframework.ldap.support.LdapContextSource;

import com.qut.middleware.esoe.authn.bean.AuthnIdentityAttribute;
import com.qut.middleware.esoe.authn.bean.impl.AuthnIdentityAttributeImpl;
import com.qut.middleware.esoe.metadata.Metadata;
import com.qut.middleware.esoe.sessions.bean.IdentityAttribute;
import com.qut.middleware.esoe.sessions.bean.IdentityData;
import com.qut.middleware.esoe.sessions.bean.SessionConfigData;
import com.qut.middleware.esoe.sessions.bean.impl.IdentityDataImpl;
import com.qut.middleware.esoe.sessions.bean.impl.SessionConfigDataImpl;
import com.qut.middleware.esoe.sessions.cache.SessionCache;
import com.qut.middleware.esoe.sessions.cache.impl.SessionCacheImpl;
import com.qut.middleware.esoe.sessions.exception.DataSourceException;
import com.qut.middleware.esoe.sessions.exception.DuplicateSessionException;
import com.qut.middleware.esoe.sessions.exception.InvalidDescriptorIdentifierException;
import com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException;
import com.qut.middleware.esoe.sessions.identity.IdentityResolver;
import com.qut.middleware.esoe.sessions.identity.impl.IdentityResolverImpl;
import com.qut.middleware.esoe.sessions.identity.pipeline.Handler;
import com.qut.middleware.esoe.sessions.identity.pipeline.impl.LDAPHandlerImpl;
import com.qut.middleware.esoe.sessions.impl.CreateImpl;
import com.qut.middleware.esoe.sessions.impl.PrincipalImpl;
import com.qut.middleware.esoe.sessions.impl.QueryImpl;
import com.qut.middleware.esoe.sessions.impl.SessionsProcessorImpl;
import com.qut.middleware.esoe.sessions.impl.TerminateImpl;
import com.qut.middleware.esoe.sessions.impl.UpdateImpl;
import com.qut.middleware.esoe.sessions.sqlmap.SessionsDAO;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.identifier.impl.IdentifierCacheImpl;
import com.qut.middleware.saml2.identifier.impl.IdentifierGeneratorImpl;

@SuppressWarnings({"nls","boxing"})
public class SessionsTest
{	
	private String IDENTIFIER = "uid";
	
	private Properties props;
	private String LDAP_SERVER; 
	private String LDAP_SERVER_URL; 
	private String LDAP_BASE_DN;
	private String CONTEXT_BASE;
	private String SEARCH_BASE;
	private String LDAP_USER;
	private String LDAP_USER_DN;
	private String LDAP_USER_PASSWORD;
	private String LDAP_ADMIN_USER;
	private String LDAP_ADMIN_USER_PASSWORD;

	private IdentityResolver resolver;
	private SessionCache sessionCache;
	private Create create;
	private Query query;
	private Update update;
	private Terminate terminate;
	private SessionsProcessor sessionsProcessor;
	private SessionConfigData sessionConfigData;
	private Handler ldapHandler;
	private IdentifierGenerator generator;
	private SessionsDAO sessionsDAO;
	private Metadata metadata;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		props = new Properties();
		FileInputStream reader = new FileInputStream(new File("tests/testdata/functional.properties"));
		props.load(reader);
		
		this.LDAP_SERVER = props.getProperty("ldapServer");
		this.LDAP_SERVER_URL = props.getProperty("ldapServerURL");
		this.LDAP_BASE_DN = props.getProperty("ldapBaseDN");
		this.CONTEXT_BASE = props.getProperty("ldapContextBase");
		this.SEARCH_BASE = props.getProperty("ldapSearchBase");
		this.LDAP_USER = props.getProperty("ldapUser");
		this.LDAP_USER_DN = props.getProperty("ldapUserDN");
		this.LDAP_USER_PASSWORD = props.getProperty("ldapUserPass");
		
		File xmlConfig = new File(this.getClass().getResource("sessiondata.xml").toURI());

		LdapContextSource contextSource = new LdapContextSource();
		contextSource.setBase(this.CONTEXT_BASE);
		contextSource.setPassword(this.LDAP_USER_PASSWORD);
		contextSource.setUrl(this.LDAP_SERVER_URL);
		contextSource.setUserName(this.LDAP_USER_DN);

		contextSource.afterPropertiesSet();
		
		LdapTemplate template = new LdapTemplate(contextSource);

		this.generator = new IdentifierGeneratorImpl(new IdentifierCacheImpl());
		this.sessionCache = new SessionCacheImpl();

		FileInputStream attributeStream = new FileInputStream(xmlConfig);
		byte[] attributeData = new byte[(int)xmlConfig.length()];
		attributeStream.read(attributeData);
		
		String entityID = "http://test.service.com";
		Integer entID = new Integer("1");
		
		this.metadata = createMock(Metadata.class);
		expect(metadata.getEsoeEntityID()).andReturn(entityID);
		
		this.sessionsDAO = createMock(SessionsDAO.class);
		expect(sessionsDAO.getEntID(entityID)).andReturn(entID);
		expect(sessionsDAO.selectActiveAttributePolicy(entID)).andReturn(attributeData);
		
		replay(this.metadata);
		replay(this.sessionsDAO);

		this.sessionConfigData = new SessionConfigDataImpl(sessionsDAO, metadata);

		this.resolver = new IdentityResolverImpl(new Vector<Handler>(0,1));
		this.ldapHandler = new LDAPHandlerImpl(template, this.IDENTIFIER, this.SEARCH_BASE, this.sessionConfigData);
		this.resolver.registerHandler(this.ldapHandler);

		this.create = new CreateImpl(this.sessionCache, this.sessionConfigData, this.resolver, this.generator, 360);
		this.query = new QueryImpl(this.sessionCache);
		this.terminate = new TerminateImpl(this.sessionCache);
		this.update = new UpdateImpl(this.sessionCache);
		
		this.sessionsProcessor = new SessionsProcessorImpl(this.create, this.query, this.terminate, this.update);
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.sessions.impl.SessionsProcessorImpl#getCreate()}.
	 */
	@Test
	public final void testCreate()
	{
		/*
		 * Test case: Create session normally
		 */
		String sessionID1 = this.generator.generateSessionID();
		String principalName1 = "beddoes";
		
		try
		{
			this.sessionsProcessor.getCreate().createLocalSession(sessionID1, principalName1, null, null);
		}
		catch (DataSourceException e)
		{
			fail("Data source error encountered: ".concat(e.getLocalizedMessage()));
		}
		catch (DuplicateSessionException e)
		{
			fail("Duplicate session encountered, identifier generator collided?");
		}
		
		Principal principal1 = this.sessionCache.getSession(sessionID1);
		assertEquals("Didn't get correct principal name", principalName1, principal1.getPrincipalAuthnIdentifier());

		Map<String,IdentityAttribute> attributes = principal1.getAttributes();
		List<String> uids = new Vector<String>(0,1);
		uids.add("beddoes");
		IdentityAttribute uidAttribute = attributes.get("uid");
		List<Object> uidValues = uidAttribute.getValues();
		assertTrue("Didn't return expected value for 'uid'", uidValues.containsAll(uids));

		List<String> sns = new Vector<String>(0,1);
		sns.add("Beddoes");
		assertTrue("Didn't return expected value for 'surname'", attributes.get("surname").getValues().containsAll(sns));

		// Expect no values for this because the only handler set up to resolve it is the NullHandler
		assertEquals("Wrong number of values for 'mail'", 0, attributes.get("mail").getValues().size());
	}
	
	/**
	 * Test method for {@link com.qut.middleware.esoe.sessions.impl.SessionsProcessorImpl#getCreate()}.
	 */
	@Test
	public final void testCreate2()
	{
		/*
		 * Test case: Creating a session that already exists.
		 */
		String sessionID1 = this.generator.generateSessionID();
		String principalName1 = "beddoes";
		
		try
		{
			this.sessionsProcessor.getCreate().createLocalSession(sessionID1, principalName1, null, null);
		}
		catch (DataSourceException e)
		{
			fail("Data source error encountered: ".concat(e.getLocalizedMessage()));
		}
		catch (DuplicateSessionException e)
		{
			fail("Duplicate session encountered, identifier generator collided?");
		}


		
		boolean caught = false;
		
		try
		{
			this.sessionsProcessor.getCreate().createLocalSession(sessionID1, principalName1, null, null);
		}
		catch (DataSourceException e)
		{
			fail("Data source error encountered: ".concat(e.getLocalizedMessage()));
		}
		catch (DuplicateSessionException e)
		{
			caught = true;
		}
		assertTrue("Did detect duplicate session ID.", caught);
		
	}
	
	/**
	 * Test method for {@link com.qut.middleware.esoe.sessions.impl.SessionsProcessorImpl#getCreate()}.
	 * Ensures data provided by AuthnHandler is correctly inserted into principal identity
	 */
	@Test
	public final void testCreate3()
	{
		/*
		 * Test case: Create session normally
		 */
		String sessionID1 = this.generator.generateSessionID();
		String principalName1 = "beddoes";
		
		try
		{
			/* Setup some data that an AuthnHandler would provide */
			List<AuthnIdentityAttribute> authnIdentity = new ArrayList<AuthnIdentityAttribute>();
			AuthnIdentityAttribute id1 = new AuthnIdentityAttributeImpl();
			id1.setName("SecurityLevel");
			id1.getValues().add("Level 1");
			AuthnIdentityAttribute id2 = new AuthnIdentityAttributeImpl();
			id2.setName("uid");
			id2.getValues().add(principalName1);
			
			authnIdentity.add(id1);
			authnIdentity.add(id2);
			
			this.sessionsProcessor.getCreate().createLocalSession(sessionID1, principalName1, null, authnIdentity);
		}
		catch (DataSourceException e)
		{
			fail("Data source error encountered: ".concat(e.getLocalizedMessage()));
		}
		catch (DuplicateSessionException e)
		{
			fail("Duplicate session encountered, identifier generator collided?");
		}
		
		Principal principal1 = this.sessionCache.getSession(sessionID1);
		assertEquals("Didn't get correct principal name", principalName1, principal1.getPrincipalAuthnIdentifier());

		Map<String,IdentityAttribute> attributes = principal1.getAttributes();
		List<String> uids = new Vector<String>(0,1);
		uids.add(principalName1);
		IdentityAttribute uidAttribute = attributes.get("uid");
		List<Object> uidValues = uidAttribute.getValues();
		assertTrue("Didn't return expected value for 'uid'", uidValues.containsAll(uids));
		
		List<String> sns = new Vector<String>(0,1);
		sns.add("Beddoes");
		assertTrue("Didn't return expected value for 'surname'", attributes.get("surname").getValues().containsAll(sns));

		// Expect no values for this because the only handler set up to resolve it is the NullHandler
		assertEquals("Wrong number of values for 'mail'", 0, attributes.get("mail").getValues().size());
		
		// Check to ensure new attribute inserted by authn handler was created
		assertTrue("Ensure that new attributes introduced by the authn handler are registered", attributes.get("SecurityLevel").getValues().contains("Level 1"));
		
		// Check to ensure that attribute data inserted by authn handler is successfully mixed with already present data
		assertTrue("Ensure that new attribute values introduced by the authn handler for attributes already created by the identity processes previously are extended", attributes.get("uid").getValues().contains("jimbob"));
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.sessions.impl.SessionsProcessorImpl#getQuery()}.
	 */
	@Test
	public final void testQuery()
	{
		/*
		 * Test case: Querying a session by session ID normally.
		 */
		String sessionID1 = this.generator.generateSessionID();
		IdentityData identityData1 = new IdentityDataImpl();
		Principal data = new PrincipalImpl(identityData1, 360);
		data.setPrincipalAuthnIdentifier("staffabc");
		data.setSessionID(sessionID1);
		
		try
		{
			this.sessionCache.addSession(data);
		}
		catch (DuplicateSessionException e)
		{
			fail("Duplicate session in empty cache");
		}
		
		Principal principal1 = null;
		try
		{
			principal1 = this.sessionsProcessor.getQuery().queryAuthnSession(sessionID1);
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("Invalid session identifier when session was already added.");
		}
		
		assertSame("Didn't get same principal back", data, principal1);
	}
	
	/**
	 * Test method for {@link com.qut.middleware.esoe.sessions.impl.SessionsProcessorImpl#getQuery()}.
	 */
	@Test
	public final void testQuery2()
	{
		/*
		 * Test case: Querying a session by session ID when it doesn't exist.
		 */
		String sessionID1 = this.generator.generateSessionID();
		Principal principal1 = null;
		boolean caught = false;
		try
		{
			principal1 = this.sessionsProcessor.getQuery().queryAuthnSession(sessionID1);
		}
		catch (InvalidSessionIdentifierException e)
		{
			caught = true;
		}
		
		assertTrue("Non-existant session ID did not generate error", caught);
		assertNull("Didn't get null principal back", principal1);
	}
	
	/**
	 * Test method for {@link com.qut.middleware.esoe.sessions.impl.SessionsProcessorImpl#getQuery()}.
	 */
	@Test
	public final void testQuery3()
	{
		/*
		 * Test case: Querying a session by SAML ID normally.
		 */
		String sessionID1 = this.generator.generateSessionID();
		String samlID1 = this.generator.generateSAMLID();
		IdentityData identityData1 = new IdentityDataImpl();
		Principal data = new PrincipalImpl(identityData1, 360);
		data.setPrincipalAuthnIdentifier("staffabc");
		data.setSessionID(sessionID1);
		try
		{
			this.sessionCache.addSession(data);
			data.setSAMLAuthnIdentifier(samlID1);
			this.sessionCache.updateSessionSAMLID(data);
		}
		catch (DuplicateSessionException e)
		{
			fail("Duplicate session in empty cache");
		}
		
		Principal principal1 = null;
		try
		{
			principal1 = this.sessionsProcessor.getQuery().querySAMLSession(samlID1);
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("Invalid SAML identifier when session was already added.");
		}
		
		assertSame("Didn't get same principal back", data, principal1);
	}
	
	/**
	 * Test method for {@link com.qut.middleware.esoe.sessions.impl.SessionsProcessorImpl#getQuery()}.
	 */
	@Test
	public final void testQuery4()
	{
		/*
		 * Test case: Querying a session by SAML ID when it doesn't exist.
		 */
		String samlID1 = this.generator.generateSAMLID();
		Principal principal1 = null;
		boolean caught = false;
		try
		{
			principal1 = this.sessionsProcessor.getQuery().querySAMLSession(samlID1);
		}
		catch (InvalidSessionIdentifierException e)
		{
			caught = true;
		}
		
		assertTrue("Non-existant SAML ID did not generate error", caught);
		assertNull("Didn't get null principal back", principal1);
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.sessions.impl.SessionsProcessorImpl#getTerminate()}.
	 */
	@Test
	public final void testTerminate()
	{
		/*
		 * Test case: Terminating a session normally.
		 */
		String sessionID1 = this.generator.generateSessionID();
		IdentityData identityData1 = new IdentityDataImpl();
		Principal data = new PrincipalImpl(identityData1, 360);
		data.setPrincipalAuthnIdentifier("staffabc");
		data.setSessionID(sessionID1);
		try
		{
			this.sessionCache.addSession(data);
		}
		catch (DuplicateSessionException e)
		{
			fail("Duplicate session in empty cache");
		}
		
		assertNotNull("Session was just added but could not be retrieved", this.sessionCache.getSession(sessionID1));

		try
		{
			this.sessionsProcessor.getTerminate().terminateSession(sessionID1);
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("Invalid session identifier but session was just added to cache");
		}
		
		assertNull("Session was just terminated but could still be retrieved", this.sessionCache.getSession(sessionID1));
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.sessions.impl.SessionsProcessorImpl#getTerminate()}.
	 */
	@Test
	public final void testTerminate2()
	{
		/*
		 * Test case: Terminating a session when it doesn't exist.
		 */
		String sessionID1 = this.generator.generateSessionID();
		boolean caught = false;
		
		try
		{
			this.sessionsProcessor.getTerminate().terminateSession(sessionID1);
		}
		catch (InvalidSessionIdentifierException e)
		{
			caught = true;
		}
		
		assertTrue("Invalid session specified but still able to be terminated", caught);
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.sessions.impl.SessionsProcessorImpl#getUpdate()}.
	 */
	@Test
	public final void testUpdate()
	{
		/*
		 * Test case: Updating SAML ID normally.
		 */
		String sessionID = this.generator.generateSessionID();
		String samlID = this.generator.generateSAMLID();
		String principal = "staffxyz";
		IdentityData identityData = new IdentityDataImpl();
		Principal data = new PrincipalImpl(identityData, 360);
		data.setSessionID(sessionID);
		data.setPrincipalAuthnIdentifier(principal);
		try
		{
			this.sessionCache.addSession(data);
		}
		catch (DuplicateSessionException e)
		{
			fail("Duplicate session in empty cache");
		}
		
		assertNull("Able to get session by SAML ID before update is done", this.sessionCache.getSessionBySAMLID(samlID));
		
		try
		{
			this.sessionsProcessor.getUpdate().updateSAMLAuthnIdentifier(sessionID, samlID);
		}
		catch (DuplicateSessionException e)
		{
			fail("Duplicate SAML ID in empty cache");
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("Session not found although added to cache.");
		}
		
		assertSame("Got back wrong session from cache for SAML ID", data, this.sessionCache.getSessionBySAMLID(samlID));
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.sessions.impl.SessionsProcessorImpl#getUpdate()}.
	 */
	@Test
	public final void testUpdate2()
	{
		/*
		 * Test case: Updating SAML ID of a session that doesn't exist.
		 */
		String sessionID = this.generator.generateSessionID();
		String samlID = this.generator.generateSAMLID();
		boolean caught = false;
		
		try
		{
			this.sessionsProcessor.getUpdate().updateSAMLAuthnIdentifier(sessionID, samlID);
		}
		catch (DuplicateSessionException e)
		{
			fail("Duplicate SAML ID in empty cache");
		}
		catch (InvalidSessionIdentifierException e)
		{
			caught = true;
		}
		
		assertTrue("No error generated although session did not exist", caught);
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.sessions.impl.SessionsProcessorImpl#getUpdate()}.
	 */
	@Test
	public final void testUpdate3()
	{
		/*
		 * Test case: Updating SAML ID of a session to a conflicting SAML ID
		 */
		String sessionID = this.generator.generateSessionID();
		String samlID = this.generator.generateSAMLID();
		String principal = "staffxyz";
		IdentityData identityData = new IdentityDataImpl();
		Principal data = new PrincipalImpl(identityData, 360);
		data.setSessionID(sessionID);
		data.setPrincipalAuthnIdentifier(principal);
		try
		{
			this.sessionCache.addSession(data);
		}
		catch (DuplicateSessionException e)
		{
			fail("Duplicate session in empty cache");
		}
		
		try
		{
			this.sessionsProcessor.getUpdate().updateSAMLAuthnIdentifier(sessionID, samlID);
		}
		catch (DuplicateSessionException e)
		{
			fail("Duplicate SAML ID in empty cache");
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("Session not found although added to cache.");
		}
		
		String sessionID2 = this.generator.generateSessionID();
		String samlID2 = samlID;
		String principal2 = "staffxyz";
		Principal data2 = new PrincipalImpl(identityData, 360);
		data2.setSessionID(sessionID2);
		data2.setPrincipalAuthnIdentifier(principal2);
		
		try
		{
			this.sessionCache.addSession(data2);
		}
		catch (DuplicateSessionException e)
		{
			fail("Duplicate session in empty cache");
		}
		
		boolean caught = false;
		try
		{
			this.sessionsProcessor.getUpdate().updateSAMLAuthnIdentifier(sessionID2, samlID2);
		}
		catch (DuplicateSessionException e)
		{
			caught = true;
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("Session not found although added to cache.");
		}
		
		assertTrue("Duplicate SAML ID did not generate an error", caught);
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.sessions.impl.SessionsProcessorImpl#getUpdate()}.
	 */
	@Test
	public final void testUpdate4()
	{
		/*
		 * Test case: Updating entity list normally.
		 */
		String sessionID = this.generator.generateSessionID();
		String entityID = this.generator.generateSAMLID(); // doesn't matter what kind of ID it is I guess..
		String principal = "staffxyz";
		IdentityData identityData = new IdentityDataImpl();
		Principal data = new PrincipalImpl(identityData, 360);
		data.setSessionID(sessionID);
		data.setPrincipalAuthnIdentifier(principal);
		try
		{
			this.sessionCache.addSession(data);
		}
		catch (DuplicateSessionException e)
		{
			fail("Duplicate session in empty cache");
		}
		
		try
		{
			this.sessionsProcessor.getUpdate().updateDescriptorList(sessionID, entityID);
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("Couldn't find session that was already added to cache.");
		}
		
		assertTrue("Entity was not added properly.", data.getActiveDescriptors().contains(entityID));
		try
		{
			assertNotNull("No session list generated for entity.", data.getDescriptorSessionIdentifiers(entityID));
		}
		catch (InvalidDescriptorIdentifierException e)
		{
			fail("Invalid entity although entity was added to session.");
		}
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.sessions.impl.SessionsProcessorImpl#getUpdate()}.
	 */
	@Test
	public final void testUpdate5()
	{
		/*
		 * Test case: Updating entity list of a session that doesn't exist.
		 */
		String sessionID = this.generator.generateSessionID();
		String entityID = this.generator.generateSAMLID(); // doesn't matter what kind of ID it is I guess..

		boolean caught = false;
		try
		{
			this.sessionsProcessor.getUpdate().updateDescriptorList(sessionID, entityID);
		}
		catch (InvalidSessionIdentifierException e)
		{
			caught = true;
		}
		
		assertTrue("No error generated although session does not exist", caught);
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.sessions.impl.SessionsProcessorImpl#getUpdate()}.
	 */
	@Test
	public final void testUpdate6()
	{
		/*
		 * Test case: Updating entity session identifiers normally
		 */
		String sessionID = this.generator.generateSessionID();
		String entityID = this.generator.generateSAMLID(); // doesn't matter what kind of ID it is I guess..
		String entitySessionID = this.generator.generateSAMLSessionID(); // again..
		String principal = "staffxyz";
		IdentityData identityData = new IdentityDataImpl();
		Principal data = new PrincipalImpl(identityData, 360);
		data.setSessionID(sessionID);
		data.setPrincipalAuthnIdentifier(principal);
		try
		{
			this.sessionCache.addSession(data);
			data.addActiveDescriptor(entityID);
		}
		catch (DuplicateSessionException e)
		{
			fail("Duplicate session in empty cache");
		}
		
		try
		{
			this.sessionsProcessor.getUpdate().updateDescriptorSessionIdentifierList(sessionID, entityID, entitySessionID);
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("Invalid session identifier even though session exists in cache.");
		}
		catch (InvalidDescriptorIdentifierException e)
		{
			fail("Invalid entity identifier even though entity exists in active entities list");
		}
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.sessions.impl.SessionsProcessorImpl#getUpdate()}.
	 */
	@Test
	public final void testUpdate7()
	{
		/*
		 * Test case: Updating entity session identifiers of a session that doesn't exist.
		 */
		String sessionID = this.generator.generateSessionID();
		String entityID = this.generator.generateSAMLID(); // doesn't matter what kind of ID it is I guess..
		String entitySessionID = this.generator.generateSAMLSessionID(); // again..
		
		boolean caught = false;
		try
		{
			this.sessionsProcessor.getUpdate().updateDescriptorSessionIdentifierList(sessionID, entityID, entitySessionID);
		}
		catch (InvalidSessionIdentifierException e)
		{
			caught = true;
		}
		catch (InvalidDescriptorIdentifierException e)
		{
			fail("No session exists but invalid entity error was triggered");
		}
		
		assertTrue("No session exists but no error was generated", caught);
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.sessions.impl.SessionsProcessorImpl#getUpdate()}.
	 */
	@Test
	public final void testUpdate8()
	{
		/*
		 * Test case: Updating entity session identifiers of an entity that doesn't exist, for a session that does exist.
		 */
		String sessionID = this.generator.generateSessionID();
		String entityID = this.generator.generateSAMLID(); // doesn't matter what kind of ID it is I guess..
		String entitySessionID = this.generator.generateSAMLSessionID(); // again..
		String principal = "staffxyz";
		IdentityData identityData = new IdentityDataImpl();
		Principal data = new PrincipalImpl(identityData, 360);
		data.setSessionID(sessionID);
		data.setPrincipalAuthnIdentifier(principal);
		try
		{
			this.sessionCache.addSession(data);
		}
		catch (DuplicateSessionException e)
		{
			fail("Duplicate session in empty cache");
		}
		
		boolean caught = false;
		try
		{
			this.sessionsProcessor.getUpdate().updateDescriptorSessionIdentifierList(sessionID, entityID, entitySessionID);
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("Session exists but invalid session error was generated");
		}
		catch (InvalidDescriptorIdentifierException e)
		{
			caught = true;
		}
		
		assertTrue("Entity identifier was never added but no error was triggered", caught);
	}
}
