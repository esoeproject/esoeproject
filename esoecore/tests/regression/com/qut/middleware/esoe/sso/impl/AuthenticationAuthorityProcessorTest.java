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
package com.qut.middleware.esoe.sso.impl;

import static org.easymock.EasyMock.createMock;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.crypto.KeyStoreResolver;
import com.qut.middleware.esoe.metadata.Metadata;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.saml2.ExternalKeyResolver;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.validator.SAMLValidator;

@SuppressWarnings("unqualified-field-access")
public class AuthenticationAuthorityProcessorTest
{
	private AuthenticationAuthorityProcessor authAuthorityProcessor;
	private SAMLValidator samlValidator;
	private SessionsProcessor sessionsProcessor;
	private IdentifierGenerator identifierGenerator;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private Metadata metadata;
	private ExternalKeyResolver externalKeyResolver;
	private KeyStoreResolver keyStoreResolver;
	
	
	public AuthenticationAuthorityProcessorTest()
	{
		samlValidator = createMock(SAMLValidator.class);
		sessionsProcessor = createMock(SessionsProcessor.class);
		identifierGenerator = createMock(IdentifierGenerator.class);
		request = createMock(HttpServletRequest.class);
		response = createMock(HttpServletResponse.class);
		metadata = createMock(Metadata.class);
		externalKeyResolver = createMock(ExternalKeyResolver.class);
		keyStoreResolver = createMock(KeyStoreResolver.class);
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception
	{
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.sso.impl.AuthenticationAuthorityProcessor#AuthenticationAuthorityProcessor(com.qut.middleware.saml2.validator.SAMLValidator, com.qut.middleware.esoe.sessions.SessionsProcessor, com.qut.middleware.esoe.spep.SPEPProcessor, com.qut.middleware.saml2.identifier.IdentifierGenerator, java.lang.String, int)}.
	 * Ensures null parameters are trapped
	 */
	@Test (expected=IllegalArgumentException.class)
	public void testAuthenticationAuthorityProcessor1() throws UnmarshallerException, MarshallerException
	{
			authAuthorityProcessor = new AuthenticationAuthorityProcessor(null, sessionsProcessor,
					this.metadata, identifierGenerator, externalKeyResolver, keyStoreResolver, 
					ConfigurationConstants.samlProtocol, 120, 20);
	}
	
	/**
	 * Test method for {@link com.qut.middleware.esoe.sso.impl.AuthenticationAuthorityProcessor#AuthenticationAuthorityProcessor(com.qut.middleware.saml2.validator.SAMLValidator, com.qut.middleware.esoe.sessions.SessionsProcessor, com.qut.middleware.esoe.spep.SPEPProcessor, com.qut.middleware.saml2.identifier.IdentifierGenerator, java.lang.String, int)}.
	 * Ensures null parameters are trapped
	 */
	@Test (expected=IllegalArgumentException.class)
	public void testAuthenticationAuthorityProcessor1a() throws UnmarshallerException, MarshallerException
	{
			authAuthorityProcessor = new AuthenticationAuthorityProcessor( samlValidator, null, this.metadata, identifierGenerator, externalKeyResolver, keyStoreResolver, ConfigurationConstants.samlProtocol, 120, 20);
	}
	
	/**
	 * Test method for {@link com.qut.middleware.esoe.sso.impl.AuthenticationAuthorityProcessor#AuthenticationAuthorityProcessor(com.qut.middleware.saml2.validator.SAMLValidator, com.qut.middleware.esoe.sessions.SessionsProcessor, com.qut.middleware.esoe.spep.SPEPProcessor, com.qut.middleware.saml2.identifier.IdentifierGenerator, java.lang.String, int)}.
	 * Ensures null parameters are trapped
	 */
	@Test (expected=IllegalArgumentException.class)
	public void testAuthenticationAuthorityProcessor1b() throws UnmarshallerException, MarshallerException
	{
			authAuthorityProcessor = new AuthenticationAuthorityProcessor( samlValidator, sessionsProcessor, null, identifierGenerator, externalKeyResolver, keyStoreResolver, ConfigurationConstants.samlProtocol, 120, 20);
	}
	
	/**
	 * Test method for {@link com.qut.middleware.esoe.sso.impl.AuthenticationAuthorityProcessor#AuthenticationAuthorityProcessor(com.qut.middleware.saml2.validator.SAMLValidator, com.qut.middleware.esoe.sessions.SessionsProcessor, com.qut.middleware.esoe.spep.SPEPProcessor, com.qut.middleware.saml2.identifier.IdentifierGenerator, java.lang.String, int)}.
	 * Ensures null parameters are trapped
	 */
	@Test (expected=IllegalArgumentException.class)
	public void testAuthenticationAuthorityProcessor1c() throws UnmarshallerException, MarshallerException
	{
			authAuthorityProcessor = new AuthenticationAuthorityProcessor( samlValidator, sessionsProcessor, this.metadata, null, externalKeyResolver, keyStoreResolver, ConfigurationConstants.samlProtocol, 120, 20);
	}
	
	/**
	 * Test method for {@link com.qut.middleware.esoe.sso.impl.AuthenticationAuthorityProcessor#AuthenticationAuthorityProcessor(com.qut.middleware.saml2.validator.SAMLValidator, com.qut.middleware.esoe.sessions.SessionsProcessor, com.qut.middleware.esoe.spep.SPEPProcessor, com.qut.middleware.saml2.identifier.IdentifierGenerator, java.lang.String, int)}.
	 * Ensures null parameters are trapped
	 */
	@Test (expected=IllegalArgumentException.class)
	public void testAuthenticationAuthorityProcessor1d() throws UnmarshallerException, MarshallerException
	{
			authAuthorityProcessor = new AuthenticationAuthorityProcessor( samlValidator, sessionsProcessor, this.metadata, identifierGenerator, externalKeyResolver, keyStoreResolver, null, 120, 20);
	}
	
	/**
	 * Test method for {@link com.qut.middleware.esoe.sso.impl.AuthenticationAuthorityProcessor#AuthenticationAuthorityProcessor(com.qut.middleware.saml2.validator.SAMLValidator, com.qut.middleware.esoe.sessions.SessionsProcessor, com.qut.middleware.esoe.spep.SPEPProcessor, com.qut.middleware.saml2.identifier.IdentifierGenerator, java.lang.String, int)}.
	 * Ensures null parameters are trapped
	 */
	@Test (expected=IllegalArgumentException.class)
	public void testAuthenticationAuthorityProcessor1e() throws UnmarshallerException, MarshallerException
	{
			authAuthorityProcessor = new AuthenticationAuthorityProcessor( samlValidator, sessionsProcessor, this.metadata, identifierGenerator, null, keyStoreResolver, ConfigurationConstants.samlProtocol, 120, 20);
	}
	
	/**
	 * Test method for {@link com.qut.middleware.esoe.sso.impl.AuthenticationAuthorityProcessor#AuthenticationAuthorityProcessor(com.qut.middleware.saml2.validator.SAMLValidator, com.qut.middleware.esoe.sessions.SessionsProcessor, com.qut.middleware.esoe.spep.SPEPProcessor, com.qut.middleware.saml2.identifier.IdentifierGenerator, java.lang.String, int)}.
	 * Ensures null parameters are trapped
	 */
	@Test (expected=IllegalArgumentException.class)
	public void testAuthenticationAuthorityProcessor1f() throws UnmarshallerException, MarshallerException
	{
			authAuthorityProcessor = new AuthenticationAuthorityProcessor( samlValidator, sessionsProcessor, this.metadata, identifierGenerator, externalKeyResolver, null, ConfigurationConstants.samlProtocol, 120, 20);
	}
}
