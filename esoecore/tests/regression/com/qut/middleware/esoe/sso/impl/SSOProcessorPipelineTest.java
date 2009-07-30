/* Copyright 2008, Queensland University of Technology
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
 * Creation Date: 30/10/2008
 * 
 * Purpose:
 */

package com.qut.middleware.esoe.sso.impl;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.crypto.KeystoreResolver;
import com.qut.middleware.crypto.impl.KeystoreResolverImpl;
import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.Query;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.esoe.sessions.Update;
import com.qut.middleware.esoe.sessions.impl.PrincipalImpl;
import com.qut.middleware.esoe.sso.SSOProcessor;
import com.qut.middleware.esoe.sso.bean.SSOProcessorData;
import com.qut.middleware.esoe.sso.bean.SSOProcessorData.SSOAction;
import com.qut.middleware.esoe.sso.bean.impl.SSOProcessorDataImpl;
import com.qut.middleware.esoe.sso.pipeline.Handler;
import com.qut.middleware.esoe.sso.pipeline.Handler.result;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.protocol.AuthnRequest;
import com.qut.middleware.saml2.schemas.protocol.NameIDPolicy;
import com.qut.middleware.saml2.validator.SAMLValidator;

public class SSOProcessorPipelineTest
{
	private List<Object> mocked;
	private SAMLValidator samlValidator;
	private SessionsProcessor sessionsProcessor;
	private MetadataProcessor metadataProcessor;
	private IdentifierGenerator identifierGenerator;
	private KeystoreResolver keyStoreResolver;
	private String esoeIdentifier;
	private String spepIdentifier;
	private Map<String,String> identifierAttributeMapping;
	private int minimalTimeRemaining;
	private int allowedTimeSkew;
	private SSOProcessor ssoProcessor;
	private Handler handler1;
	private String handlerName1;
	private Handler handler2;
	private String handlerName2;
	private List<Handler> handlers;
	private Query query;
	private String sessionID;
	private Principal principal;
	private Cookie validSessionCookie;
	private String sessionCookieName;
	private String invalidSessionID;
	private Cookie invalidSessionCookie;
	private Properties configuration;
	private String commonDomainTokenName;
	private String authnRedirectURL;
	private String authnDynamicURLParam;
	private String ssoURL;
	private String sessionDomain;
	private String commonDomain;
	private Update update;

	private void startMock()
	{
		for (Object o : this.mocked)
		{
			replay(o);
		}
	}

	private void endMock()
	{
		for (Object o : this.mocked)
		{
			verify(o);
		}
	}

	@Before
	public void setUp() throws Exception
	{
		this.mocked = new ArrayList<Object>();

		this.samlValidator = EasyMock.createMock(SAMLValidator.class);
		this.mocked.add(this.samlValidator);
		this.sessionsProcessor = EasyMock.createMock(SessionsProcessor.class);
		this.mocked.add(this.sessionsProcessor);
		this.metadataProcessor = EasyMock.createMock(MetadataProcessor.class);
		this.mocked.add(this.metadataProcessor);
		this.identifierGenerator = EasyMock.createMock(IdentifierGenerator.class);
		this.mocked.add(this.identifierGenerator);

		this.allowedTimeSkew = 120;
		this.minimalTimeRemaining = 120;
		this.identifierAttributeMapping = new HashMap<String, String>();
		this.esoeIdentifier = "http://esoe.example.com";
		this.spepIdentifier = "http://spep.example.com";

		String keyStorePath =  "tests/testdata/testskeystore.ks";
		String keyStorePassword = "Es0EKs54P4SSPK";
		String esoeKeyAlias = "esoeprimary";
		String esoeKeyPassword = "Es0EKs54P4SSPK";

		this.keyStoreResolver = new KeystoreResolverImpl(new File(keyStorePath), keyStorePassword, esoeKeyAlias, esoeKeyPassword);

		this.handler1 = createMock(Handler.class);
		this.mocked.add(this.handler1);
		this.handlerName1 = "test-handler-1";
		expect(this.handler1.getHandlerName()).andReturn(this.handlerName1).anyTimes();
		this.handler2 = createMock(Handler.class);
		this.mocked.add(this.handler2);
		this.handlerName2 = "test-handler-2";
		expect(this.handler2.getHandlerName()).andReturn(this.handlerName2).anyTimes();
		
		this.handlers = new ArrayList<Handler>();
		this.handlers.add(this.handler1);
		this.handlers.add(this.handler2);

		this.query = createMock(Query.class);
		this.mocked.add(this.query);
		expect(this.sessionsProcessor.getQuery()).andReturn(this.query).anyTimes();
		
		this.update = createMock(Update.class);
		this.mocked.add(this.update);
		expect(this.sessionsProcessor.getUpdate()).andReturn(this.update).anyTimes();
		
		this.sessionID = "_session-id";
		this.invalidSessionID = "_invalid-session";
		PrincipalImpl principalImpl = new PrincipalImpl();
		this.principal = principalImpl;
		expect(this.query.queryAuthnSession(this.sessionID)).andReturn(this.principal).anyTimes();
		
		principalImpl.setAuthenticationContextClass("authn-context");
		principalImpl.setAuthnTimestamp(System.currentTimeMillis());
		principalImpl.setLastAccessed(System.currentTimeMillis());
		principalImpl.setPrincipalAuthnIdentifier("principal");
		principalImpl.setSAMLAuthnIdentifier("_principal-saml-id");
		principalImpl.setSessionID(this.sessionID);
		principalImpl.setSessionNotOnOrAfter(System.currentTimeMillis() + 10000);
		
		this.sessionCookieName = "esoeSession";
		this.validSessionCookie = new Cookie(this.sessionCookieName, this.sessionID);
		this.invalidSessionCookie = new Cookie(this.sessionCookieName, this.invalidSessionID);
		
		this.commonDomainTokenName = "_saml_idp";
		this.authnRedirectURL = "http://esoe.example.com/signin";
		this.authnDynamicURLParam = "rc";
		this.ssoURL = "http://esoe.example.com/sso";
		this.sessionDomain = "esoe.example.com";
		this.commonDomain = ".example.com";
		
		this.configuration = new Properties();
		this.configuration.setProperty(ConfigurationConstants.ESOE_SESSION_TOKEN_NAME, this.sessionCookieName);
		this.configuration.setProperty(ConfigurationConstants.ESOE_IDENTIFIER, this.esoeIdentifier);
		this.configuration.setProperty(ConfigurationConstants.COMMON_DOMAIN_TOKEN_NAME, this.commonDomainTokenName);
		this.configuration.setProperty(ConfigurationConstants.AUTHN_REDIRECT_URL, this.authnRedirectURL);
		this.configuration.setProperty(ConfigurationConstants.AUTHN_DYNAMIC_URL_PARAM, this.authnDynamicURLParam);
		this.configuration.setProperty(ConfigurationConstants.SSO_URL, this.ssoURL);
		this.configuration.setProperty(ConfigurationConstants.ESOE_SESSION_DOMAIN, this.sessionDomain);
		this.configuration.setProperty(ConfigurationConstants.COMMON_DOMAIN, this.commonDomain);
		
		this.ssoProcessor = new SSOProcessorImpl(this.samlValidator, this.sessionsProcessor, this.metadataProcessor, this.identifierGenerator, this.keyStoreResolver, this.keyStoreResolver, this.identifierAttributeMapping, this.handlers, this.configuration);
	}

	@Test
	// Unauthenticated user with AllowCreate=true in request
	public void testExecute1a() throws Exception
	{
		expect(this.handler1.executeRequest((SSOProcessorData)notNull())).andReturn(result.Successful).once();
		//expect(this.handler1.executeResponse((SSOProcessorData)notNull())).andReturn(result.Successful).once();
		
		AuthnRequest authnRequest = generateAuthnRequest(true);
		
		HttpServletRequest request = createMock(HttpServletRequest.class);
		this.mocked.add(request);
		
		Cookie[] cookies = new Cookie[0];
		expect(request.getCookies()).andReturn(cookies).once();
		
		this.startMock();

		SSOProcessorData data = new SSOProcessorDataImpl();
		data.setAuthnRequest(authnRequest);
		data.setRemoteAddress("127.0.0.1");
		data.setHttpRequest(request);
		
		assertEquals(SSOProcessor.result.ForceAuthn, this.ssoProcessor.execute(data));

		this.endMock();
	}

	@Test
	// Unauthenticated user with AllowCreate=false in request
	public void testExecute1b() throws Exception
	{
		expect(this.handler1.executeRequest((SSOProcessorData)notNull())).andReturn(result.Successful).once();
		//expect(this.handler1.executeResponse((SSOProcessorData)notNull())).andReturn(result.Successful).once();

		// Expect a response to be generated.
		expect(this.identifierGenerator.generateSAMLID()).andReturn("_samlid-test1b-1").once();
		
		AuthnRequest authnRequest = this.generateAuthnRequest(false);
		
		HttpServletRequest request = createMock(HttpServletRequest.class);
		this.mocked.add(request);
		
		Cookie[] cookies = new Cookie[0];
		expect(request.getCookies()).andReturn(cookies).once();
		
		this.startMock();

		SSOProcessorData data = new SSOProcessorDataImpl();
		data.setAuthnRequest(authnRequest);
		data.setRemoteAddress("127.0.0.1");
		data.setHttpRequest(request);
		
		assertEquals(SSOProcessor.result.ForcePassiveAuthn, this.ssoProcessor.execute(data));

		this.endMock();
	}
	
	@Test
	// Invalid cookie with AllowCreate=true in request
	public void testExecute1c() throws Exception
	{
		expect(this.handler1.executeRequest((SSOProcessorData)notNull())).andReturn(result.Successful).once();
		//expect(this.handler1.executeResponse((SSOProcessorData)notNull())).andReturn(result.Successful).once();
		
		expect(this.query.queryAuthnSession(this.invalidSessionID)).andReturn(null).anyTimes();
		
		AuthnRequest authnRequest = generateAuthnRequest(true);
		
		HttpServletRequest request = createMock(HttpServletRequest.class);
		this.mocked.add(request);
		
		Cookie[] cookies = new Cookie[1];
		cookies[0] = this.invalidSessionCookie;
		expect(request.getCookies()).andReturn(cookies).once();
		
		this.startMock();

		SSOProcessorData data = new SSOProcessorDataImpl();
		data.setAuthnRequest(authnRequest);
		data.setRemoteAddress("127.0.0.1");
		data.setHttpRequest(request);
		
		assertEquals(SSOProcessor.result.ForceAuthn, this.ssoProcessor.execute(data));

		this.endMock();
	}

	@Test
	// Invalid cookie with AllowCreate=false in request
	public void testExecute1d() throws Exception
	{
		expect(this.handler1.executeRequest((SSOProcessorData)notNull())).andReturn(result.Successful).once();
		//expect(this.handler1.executeResponse((SSOProcessorData)notNull())).andReturn(result.Successful).once();

		// Expect a response to be generated.
		expect(this.identifierGenerator.generateSAMLID()).andReturn("_samlid-test1d-1").once();
		
		expect(this.query.queryAuthnSession(this.invalidSessionID)).andReturn(null).anyTimes();
		
		AuthnRequest authnRequest = this.generateAuthnRequest(false);
		
		HttpServletRequest request = createMock(HttpServletRequest.class);
		this.mocked.add(request);
		
		Cookie[] cookies = new Cookie[1];
		cookies[0] = this.invalidSessionCookie;
		expect(request.getCookies()).andReturn(cookies).once();
		
		this.startMock();

		SSOProcessorData data = new SSOProcessorDataImpl();
		data.setAuthnRequest(authnRequest);
		data.setRemoteAddress("127.0.0.1");
		data.setHttpRequest(request);
		
		assertEquals(SSOProcessor.result.ForcePassiveAuthn, this.ssoProcessor.execute(data));

		this.endMock();
	}

	@Test
	// Authenticated user with AllowCreate=true in request
	public void testExecute2a() throws Exception
	{
		expect(this.handler1.executeRequest((SSOProcessorData)notNull())).andReturn(result.Successful).once();
		expect(this.handler1.executeResponse((SSOProcessorData)notNull())).andReturn(result.Successful).once();
		
		String sessionIndex = "abcd1234";
		expect(this.identifierGenerator.generateSAMLSessionID()).andReturn(sessionIndex).once();
		
		this.update.addEntitySessionIndex(this.principal, this.spepIdentifier, sessionIndex);
		expectLastCall().once();
		
		AuthnRequest authnRequest = generateAuthnRequest(true);
		
		HttpServletRequest request = createMock(HttpServletRequest.class);
		this.mocked.add(request);
		
		Cookie[] cookies = new Cookie[1];
		cookies[0] = this.validSessionCookie;
		expect(request.getCookies()).andReturn(cookies).once();
		
		this.startMock();

		SSOProcessorData data = new SSOProcessorDataImpl();
		data.setAuthnRequest(authnRequest);
		data.setRemoteAddress("127.0.0.1");
		data.setHttpRequest(request);
		
		assertEquals(SSOProcessor.result.SSOGenerationSuccessful, this.ssoProcessor.execute(data));

		this.endMock();
	}

	@Test
	// Authenticated user with AllowCreate=false in request
	public void testExecute2b() throws Exception
	{
		expect(this.handler1.executeRequest((SSOProcessorData)notNull())).andReturn(result.Successful).once();
		expect(this.handler1.executeResponse((SSOProcessorData)notNull())).andReturn(result.Successful).once();
		
		String sessionIndex = "abcd1234";
		expect(this.identifierGenerator.generateSAMLSessionID()).andReturn(sessionIndex).once();
		
		this.update.addEntitySessionIndex(this.principal, this.spepIdentifier, sessionIndex);
		expectLastCall().once();
		
		AuthnRequest authnRequest = generateAuthnRequest(false);
		
		HttpServletRequest request = createMock(HttpServletRequest.class);
		this.mocked.add(request);
		
		Cookie[] cookies = new Cookie[1];
		cookies[0] = this.validSessionCookie;
		expect(request.getCookies()).andReturn(cookies).once();
		
		this.startMock();

		SSOProcessorData data = new SSOProcessorDataImpl();
		data.setAuthnRequest(authnRequest);
		data.setRemoteAddress("127.0.0.1");
		data.setHttpRequest(request);
		
		assertEquals(SSOProcessor.result.SSOGenerationSuccessful, this.ssoProcessor.execute(data));

		this.endMock();
	}

	@Test
	// Authenticated user with current handler set to handler1 and current action set to request
	public void testExecute3a() throws Exception
	{
		expect(this.handler1.executeRequest((SSOProcessorData)notNull())).andReturn(result.Successful).once();
		expect(this.handler1.executeResponse((SSOProcessorData)notNull())).andReturn(result.Successful).once();
		
		String sessionIndex = "abcd1234";
		expect(this.identifierGenerator.generateSAMLSessionID()).andReturn(sessionIndex).once();
		
		this.update.addEntitySessionIndex(this.principal, this.spepIdentifier, sessionIndex);
		expectLastCall().once();
		
		AuthnRequest authnRequest = generateAuthnRequest(true);
		
		HttpServletRequest request = createMock(HttpServletRequest.class);
		this.mocked.add(request);
		
		Cookie[] cookies = new Cookie[1];
		cookies[0] = this.validSessionCookie;
		expect(request.getCookies()).andReturn(cookies).once();
		
		this.startMock();

		SSOProcessorData data = new SSOProcessorDataImpl();
		data.setAuthnRequest(authnRequest);
		data.setRemoteAddress("127.0.0.1");
		data.setHttpRequest(request);
		data.setCurrentAction(SSOAction.REQUEST_PROCESSING);
		data.setCurrentHandler(this.handlerName1);
		
		assertEquals(SSOProcessor.result.SSOGenerationSuccessful, this.ssoProcessor.execute(data));

		this.endMock();
	}

	@Test
	// Authenticated user with current handler set to handler2 and current action set to request
	public void testExecute3b() throws Exception
	{
		expect(this.handler2.executeRequest((SSOProcessorData)notNull())).andReturn(result.Successful).once();
		expect(this.handler1.executeResponse((SSOProcessorData)notNull())).andReturn(result.Successful).once();
		
		String sessionIndex = "abcd1234";
		expect(this.identifierGenerator.generateSAMLSessionID()).andReturn(sessionIndex).once();
		
		this.update.addEntitySessionIndex(this.principal, this.spepIdentifier, sessionIndex);
		expectLastCall().once();
		
		AuthnRequest authnRequest = generateAuthnRequest(true);
		
		HttpServletRequest request = createMock(HttpServletRequest.class);
		this.mocked.add(request);
		
		Cookie[] cookies = new Cookie[1];
		cookies[0] = this.validSessionCookie;
		expect(request.getCookies()).andReturn(cookies).once();
		
		this.startMock();

		SSOProcessorData data = new SSOProcessorDataImpl();
		data.setAuthnRequest(authnRequest);
		data.setRemoteAddress("127.0.0.1");
		data.setHttpRequest(request);
		data.setCurrentAction(SSOAction.REQUEST_PROCESSING);
		data.setCurrentHandler(this.handlerName2);
		
		assertEquals(SSOProcessor.result.SSOGenerationSuccessful, this.ssoProcessor.execute(data));

		this.endMock();
	}

	@Test
	// Authenticated user with current action set to sso processing
	public void testExecute3c() throws Exception
	{
		expect(this.handler1.executeResponse((SSOProcessorData)notNull())).andReturn(result.Successful).once();
		
		String sessionIndex = "abcd1234";
		expect(this.identifierGenerator.generateSAMLSessionID()).andReturn(sessionIndex).once();
		
		this.update.addEntitySessionIndex(this.principal, this.spepIdentifier, sessionIndex);
		expectLastCall().once();
		
		AuthnRequest authnRequest = generateAuthnRequest(true);
		
		HttpServletRequest request = createMock(HttpServletRequest.class);
		this.mocked.add(request);
		
		Cookie[] cookies = new Cookie[1];
		cookies[0] = this.validSessionCookie;
		expect(request.getCookies()).andReturn(cookies).once();
		
		this.startMock();

		SSOProcessorData data = new SSOProcessorDataImpl();
		data.setAuthnRequest(authnRequest);
		data.setRemoteAddress("127.0.0.1");
		data.setHttpRequest(request);
		data.setCurrentAction(SSOAction.SSO_PROCESSING);
		
		assertEquals(SSOProcessor.result.SSOGenerationSuccessful, this.ssoProcessor.execute(data));

		this.endMock();
	}

	@Test
	// Authenticated user with current handler set to handler1 and current action set to response
	public void testExecute3d() throws Exception
	{
		expect(this.handler1.executeResponse((SSOProcessorData)notNull())).andReturn(result.Successful).once();
		
		AuthnRequest authnRequest = generateAuthnRequest(true);
		
		HttpServletRequest request = createMock(HttpServletRequest.class);
		this.mocked.add(request);
		
		this.startMock();

		SSOProcessorData data = new SSOProcessorDataImpl();
		data.setAuthnRequest(authnRequest);
		data.setRemoteAddress("127.0.0.1");
		data.setHttpRequest(request);
		data.setCurrentAction(SSOAction.RESPONSE_PROCESSING);
		data.setCurrentHandler(this.handlerName1);
		
		assertEquals(SSOProcessor.result.SSOGenerationSuccessful, this.ssoProcessor.execute(data));

		this.endMock();
	}

	@Test
	// Authenticated user with current handler set to handler2 and current action set to response
	public void testExecute3e() throws Exception
	{
		expect(this.handler2.executeResponse((SSOProcessorData)notNull())).andReturn(result.Successful).once();
		
		AuthnRequest authnRequest = generateAuthnRequest(true);
		
		HttpServletRequest request = createMock(HttpServletRequest.class);
		this.mocked.add(request);
		
		this.startMock();

		SSOProcessorData data = new SSOProcessorDataImpl();
		data.setAuthnRequest(authnRequest);
		data.setRemoteAddress("127.0.0.1");
		data.setHttpRequest(request);
		data.setCurrentAction(SSOAction.RESPONSE_PROCESSING);
		data.setCurrentHandler(this.handlerName2);
		
		assertEquals(SSOProcessor.result.SSOGenerationSuccessful, this.ssoProcessor.execute(data));

		this.endMock();
	}

	@Test
	// Authenticated user with request falling through to handler2
	public void testExecute4a() throws Exception
	{
		expect(this.handler1.executeRequest((SSOProcessorData)notNull())).andReturn(result.NoAction).once();
		expect(this.handler2.executeRequest((SSOProcessorData)notNull())).andReturn(result.Successful).once();
		expect(this.handler1.executeResponse((SSOProcessorData)notNull())).andReturn(result.Successful).once();
		
		String sessionIndex = "abcd1234";
		expect(this.identifierGenerator.generateSAMLSessionID()).andReturn(sessionIndex).once();
		
		this.update.addEntitySessionIndex(this.principal, this.spepIdentifier, sessionIndex);
		expectLastCall().once();
		
		AuthnRequest authnRequest = generateAuthnRequest(true);
		
		HttpServletRequest request = createMock(HttpServletRequest.class);
		this.mocked.add(request);
		
		Cookie[] cookies = new Cookie[1];
		cookies[0] = this.validSessionCookie;
		expect(request.getCookies()).andReturn(cookies).once();
		
		this.startMock();

		SSOProcessorData data = new SSOProcessorDataImpl();
		data.setAuthnRequest(authnRequest);
		data.setRemoteAddress("127.0.0.1");
		data.setHttpRequest(request);
		data.setCurrentAction(SSOAction.REQUEST_PROCESSING);
		data.setCurrentHandler(this.handlerName1);
		
		assertEquals(SSOProcessor.result.SSOGenerationSuccessful, this.ssoProcessor.execute(data));

		this.endMock();
	}

	@Test
	// Authenticated user with response falling through to handler2
	public void testExecute4b() throws Exception
	{
		expect(this.handler1.executeRequest((SSOProcessorData)notNull())).andReturn(result.Successful).once();
		expect(this.handler1.executeResponse((SSOProcessorData)notNull())).andReturn(result.NoAction).once();
		expect(this.handler2.executeResponse((SSOProcessorData)notNull())).andReturn(result.Successful).once();
		
		String sessionIndex = "abcd1234";
		expect(this.identifierGenerator.generateSAMLSessionID()).andReturn(sessionIndex).once();
		
		this.update.addEntitySessionIndex(this.principal, this.spepIdentifier, sessionIndex);
		expectLastCall().once();
		
		AuthnRequest authnRequest = generateAuthnRequest(true);
		
		HttpServletRequest request = createMock(HttpServletRequest.class);
		this.mocked.add(request);
		
		Cookie[] cookies = new Cookie[1];
		cookies[0] = this.validSessionCookie;
		expect(request.getCookies()).andReturn(cookies).once();
		
		this.startMock();

		SSOProcessorData data = new SSOProcessorDataImpl();
		data.setAuthnRequest(authnRequest);
		data.setRemoteAddress("127.0.0.1");
		data.setHttpRequest(request);
		data.setCurrentAction(SSOAction.REQUEST_PROCESSING);
		data.setCurrentHandler(this.handlerName1);
		
		assertEquals(SSOProcessor.result.SSOGenerationSuccessful, this.ssoProcessor.execute(data));

		this.endMock();
	}

	@Test
	// Authenticated user with handler rejecting request
	public void testExecute5a() throws Exception
	{
		expect(this.handler1.executeRequest((SSOProcessorData)notNull())).andReturn(result.InvalidRequest).once();
		
		AuthnRequest authnRequest = generateAuthnRequest(true);
		
		HttpServletRequest request = createMock(HttpServletRequest.class);
		this.mocked.add(request);
		
		this.startMock();

		SSOProcessorData data = new SSOProcessorDataImpl();
		data.setAuthnRequest(authnRequest);
		data.setRemoteAddress("127.0.0.1");
		data.setHttpRequest(request);
		data.setCurrentAction(SSOAction.REQUEST_PROCESSING);
		data.setCurrentHandler(this.handlerName1);
		
		assertEquals(SSOProcessor.result.SSOGenerationFailed, this.ssoProcessor.execute(data));

		this.endMock();
	}

	@Test
	// Authenticated user with handler refusing to respond
	public void testExecute5b() throws Exception
	{
		expect(this.handler1.executeRequest((SSOProcessorData)notNull())).andReturn(result.Successful).once();
		expect(this.handler1.executeResponse((SSOProcessorData)notNull())).andReturn(result.UnwillingToRespond).once();
		
		String sessionIndex = "abcd1234";
		expect(this.identifierGenerator.generateSAMLSessionID()).andReturn(sessionIndex).once();
		
		this.update.addEntitySessionIndex(this.principal, this.spepIdentifier, sessionIndex);
		expectLastCall().once();
		
		AuthnRequest authnRequest = generateAuthnRequest(true);
		
		HttpServletRequest request = createMock(HttpServletRequest.class);
		this.mocked.add(request);
		
		Cookie[] cookies = new Cookie[1];
		cookies[0] = this.validSessionCookie;
		expect(request.getCookies()).andReturn(cookies).once();
		
		this.startMock();

		SSOProcessorData data = new SSOProcessorDataImpl();
		data.setAuthnRequest(authnRequest);
		data.setRemoteAddress("127.0.0.1");
		data.setHttpRequest(request);
		data.setCurrentAction(SSOAction.REQUEST_PROCESSING);
		data.setCurrentHandler(this.handlerName1);
		
		assertEquals(SSOProcessor.result.SSOGenerationFailed, this.ssoProcessor.execute(data));

		this.endMock();
	}

	private AuthnRequest generateAuthnRequest(boolean allowCreate)
	{
		AuthnRequest authnRequest = new AuthnRequest();
		
		NameIDType issuer = new NameIDType();
		issuer.setValue(this.spepIdentifier);
		
		authnRequest.setIssuer(issuer);
		
		NameIDPolicy nameIDPolicy = new NameIDPolicy();
		authnRequest.setNameIDPolicy(nameIDPolicy);
		nameIDPolicy.setAllowCreate(allowCreate);
		return authnRequest;
	}
}
