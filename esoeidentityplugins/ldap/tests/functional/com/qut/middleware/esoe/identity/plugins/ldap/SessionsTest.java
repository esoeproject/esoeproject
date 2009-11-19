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
package com.qut.middleware.esoe.identity.plugins.ldap;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import com.qut.middleware.esoe.authn.bean.AuthnIdentityAttribute;
import com.qut.middleware.esoe.authn.bean.impl.AuthnIdentityAttributeImpl;
import com.qut.middleware.esoe.identity.plugins.ldap.handler.LDAPHandler;
import com.qut.middleware.esoe.logout.LogoutThreadPool;
import com.qut.middleware.esoe.sessions.Create;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.Query;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.esoe.sessions.Terminate;
import com.qut.middleware.esoe.sessions.Update;
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
import com.qut.middleware.esoe.sessions.impl.CreateImpl;
import com.qut.middleware.esoe.sessions.impl.PrincipalImpl;
import com.qut.middleware.esoe.sessions.impl.QueryImpl;
import com.qut.middleware.esoe.sessions.impl.SessionsProcessorImpl;
import com.qut.middleware.esoe.sessions.impl.TerminateImpl;
import com.qut.middleware.esoe.sessions.impl.UpdateImpl;
import com.qut.middleware.esoe.sessions.sqlmap.SessionsDAO;
import com.qut.middleware.metadata.processor.MetadataProcessor;
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
	private MetadataProcessor metadata;

	private LogoutThreadPool logout;

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

		this.logout = createMock(LogoutThreadPool.class);
		//expect(logout.getEndPoints(entityID)).andReturn(endpoints);
		//expect(logout.performSingleLogout((String)notNull(), (List<String>)notNull(), eq(entityID), anyBoolean())).andReturn(LogoutMechanism.result.LogoutSuccessful).anyTimes();
		replay(this.logout);

		this.sessionCache = new SessionCacheImpl(this.logout);

		FileInputStream attributeStream = new FileInputStream(xmlConfig);
		byte[] attributeData = new byte[(int)xmlConfig.length()];
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

		this.resolver = new IdentityResolverImpl(new Vector<Handler>(0,1));
		this.ldapHandler = new LDAPHandler(template, this.IDENTIFIER, this.SEARCH_BASE, this.sessionConfigData);
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
	public final void testCreate() throws Exception
	{
		/*
		 * Test case: Create session normally
		 */
		String sessionID1 = this.generator.generateSessionID();
		String principalName1 = "beddoes";

		this.sessionsProcessor.getCreate().createLocalSession(sessionID1, principalName1, null, null);

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
	public final void testCreate2() throws Exception
	{
		/*
		 * Test case: Creating a session that already exists.
		 */
		String sessionID1 = this.generator.generateSessionID();
		String principalName1 = "beddoes";

		this.sessionsProcessor.getCreate().createLocalSession(sessionID1, principalName1, null, null);

		boolean caught = false;

		this.sessionsProcessor.getCreate().createLocalSession(sessionID1, principalName1, null, null);
		assertTrue("Did detect duplicate session ID.", caught);

	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.sessions.impl.SessionsProcessorImpl#getCreate()}.
	 * Ensures data provided by AuthnHandler is correctly inserted into principal identity
	 */
	@Test
	public final void testCreate3() throws Exception
	{
		/*
		 * Test case: Create session normally
		 */
		String sessionID1 = this.generator.generateSessionID();
		String principalName1 = "beddoes";

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
	public final void testQuery() throws Exception
	{
		/*
		 * Test case: Querying a session by session ID normally.
		 */
		String sessionID1 = this.generator.generateSessionID();
		PrincipalImpl data = new PrincipalImpl();
		data.setPrincipalAuthnIdentifier("staffabc");
		data.setSessionID(sessionID1);

		this.sessionCache.addSession(data);

		Principal principal1 = this.sessionsProcessor.getQuery().queryAuthnSession(sessionID1);

		assertSame("Didn't get same principal back", data, principal1);
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.sessions.impl.SessionsProcessorImpl#getQuery()}.
	 */
	@Test
	public final void testQuery2() throws Exception
	{
		/*
		 * Test case: Querying a session by session ID when it doesn't exist.
		 */
		String sessionID1 = this.generator.generateSessionID();
		Principal principal1 = null;
		boolean caught = false;

		principal1 = this.sessionsProcessor.getQuery().queryAuthnSession(sessionID1);

		assertTrue("Non-existant session ID did not generate error", caught);
		assertNull("Didn't get null principal back", principal1);
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.sessions.impl.SessionsProcessorImpl#getQuery()}.
	 */
	@Test
	public final void testQuery3() throws Exception
	{
		/*
		 * Test case: Querying a session by SAML ID normally.
		 */
		String sessionID1 = this.generator.generateSessionID();
		String samlID1 = this.generator.generateSAMLID();
		IdentityData identityData1 = new IdentityDataImpl();
		PrincipalImpl  data = new PrincipalImpl();
		data.setPrincipalAuthnIdentifier("staffabc");
		data.setSessionID(sessionID1);

		this.sessionCache.addSession(data);
		data.setSAMLAuthnIdentifier(samlID1);

		Principal principal1 = this.sessionsProcessor.getQuery().querySAMLSession(samlID1);

		assertSame("Didn't get same principal back", data, principal1);
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.sessions.impl.SessionsProcessorImpl#getQuery()}.
	 */
	@Test(expected = InvalidSessionIdentifierException.class)
	public final void testQuery4() throws Exception
	{
		/*
		 * Test case: Querying a session by SAML ID when it doesn't exist.
		 */
		String samlID1 = this.generator.generateSAMLID();
		Principal principal1 = null;
		boolean caught = false;

		principal1 = this.sessionsProcessor.getQuery().querySAMLSession(samlID1);

		assertTrue("Non-existant SAML ID did not generate error", caught);
		assertNull("Didn't get null principal back", principal1);
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.sessions.impl.SessionsProcessorImpl#getTerminate()}.
	 */
	@Test
	public final void testTerminate() throws Exception
	{
		/*
		 * Test case: Terminating a session normally.
		 */
		String sessionID1 = this.generator.generateSessionID();
		IdentityData identityData1 = new IdentityDataImpl();
		PrincipalImpl data = new PrincipalImpl();
		data.setPrincipalAuthnIdentifier("staffabc");
		data.setSessionID(sessionID1);

		this.sessionCache.addSession(data);

		assertNotNull("Session was just added but could not be retrieved", this.sessionCache.getSession(sessionID1));

		this.sessionsProcessor.getTerminate().terminateSession(sessionID1);

		assertNull("Session was just terminated but could still be retrieved", this.sessionCache.getSession(sessionID1));
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.sessions.impl.SessionsProcessorImpl#getTerminate()}.
	 */
	@Test(expected = InvalidSessionIdentifierException.class)
	public final void testTerminate2() throws Exception
	{
		/*
		 * Test case: Terminating a session when it doesn't exist.
		 */
		String sessionID1 = this.generator.generateSessionID();

		this.sessionsProcessor.getTerminate().terminateSession(sessionID1);
	}
}
