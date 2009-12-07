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
 * Purpose: Functional tests for AuthenticationAuthorityProcessorImpl
 */

package com.qut.middleware.esoe.sso.impl;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3._2000._09.xmldsig_.Signature;

import com.qut.middleware.crypto.KeystoreResolver;
import com.qut.middleware.crypto.impl.KeystoreResolverImpl;
import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.Query;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.esoe.sessions.Update;
import com.qut.middleware.esoe.sessions.exception.InvalidDescriptorIdentifierException;
import com.qut.middleware.esoe.sessions.exception.SessionCacheUpdateException;
import com.qut.middleware.esoe.sso.SSOProcessor;
import com.qut.middleware.esoe.sso.bean.SSOProcessorData;
import com.qut.middleware.esoe.sso.bean.impl.SSOProcessorDataImpl;
import com.qut.middleware.esoe.sso.exception.InvalidRequestException;
import com.qut.middleware.esoe.sso.exception.InvalidSessionIdentifierException;
import com.qut.middleware.esoe.sso.pipeline.Handler;
import com.qut.middleware.metadata.bean.EntityData;
import com.qut.middleware.metadata.bean.saml.SPEPRole;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.saml2.AuthenticationContextConstants;
import com.qut.middleware.saml2.BindingConstants;
import com.qut.middleware.saml2.NameIDFormatConstants;
import com.qut.middleware.saml2.SchemaConstants;
import com.qut.middleware.saml2.StatusCodeConstants;
import com.qut.middleware.saml2.exception.InvalidSAMLResponseException;
import com.qut.middleware.saml2.exception.KeyResolutionException;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.exception.ReferenceValueException;
import com.qut.middleware.saml2.exception.SignatureValueException;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.Unmarshaller;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.handler.impl.UnmarshallerImpl;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.identifier.impl.IdentifierCacheImpl;
import com.qut.middleware.saml2.schemas.assertion.AudienceRestriction;
import com.qut.middleware.saml2.schemas.assertion.Conditions;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.assertion.Subject;
import com.qut.middleware.saml2.schemas.protocol.AuthnRequest;
import com.qut.middleware.saml2.schemas.protocol.NameIDPolicy;
import com.qut.middleware.saml2.schemas.protocol.Response;
import com.qut.middleware.saml2.validator.SAMLValidator;
import com.qut.middleware.saml2.validator.impl.SAMLValidatorImpl;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

@SuppressWarnings(value = { "unqualified-field-access", "nls" })
public class AuthenticationAuthorityProcessorFuncTest
{
	private SSOProcessorImpl authAuthorityProcessor;
	private SSOProcessorData data;
	private SAMLValidator samlValidator;
	private SessionsProcessor sessionsProcessor;
	private Query query;
	private Update update;
	private IdentifierGenerator identifierGenerator;
	private MetadataProcessor metadata;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private Principal principal;
	private Map<String, String> identifierMap;

	private String[] schemas;

	private Marshaller<AuthnRequest> marshaller;
	private Unmarshaller<Response> unmarshaller;

	private PublicKey pk;
	private KeystoreResolver keyStoreResolver;

	private String spepKeyAlias;
	private String spepKeyPassword;
	private String issuer = "_bbb7b47de6cd6c227ba78c340137afcbab08cf94-efb1d452f76659a1b10519ab5d53c03c";

	private List<String> defaultSupportedType;
	private EntityData entityData;
	private SPEPRole spepRole;
	private Properties properties;
	private List<Handler> handlers;

	public AuthenticationAuthorityProcessorFuncTest()
	{
		// Not Implemented
	}

	/**
	 * Creates a valid SAML AuthnRequest like would be supplied by an SPEP
	 * 
	 * @param allowCreation
	 *            field to modify in the request to cause a difference in generated output.
	 * @param tzOffset
	 *            Specify an offset when creating xml timestamps. Specification says this must be set to 0. Any
	 *            variations should cause SAML validation to reject the document.
	 * @return String containing SAML AuthnRequest
	 */
	private byte[] generateValidRequest(boolean allowCreation, int tzOffset) throws Exception
	{
		AudienceRestriction audienceRestriction = new AudienceRestriction();
		Conditions conditions = new Conditions();
		NameIDType nameID = new NameIDType();
		NameIDType issuer = new NameIDType();
		NameIDPolicy policy = new NameIDPolicy();
		Subject subject = new Subject();
		Signature signature = new Signature();
		AuthnRequest authnRequest = new AuthnRequest();
		byte[] result;

		/* GMT timezone */
		SimpleTimeZone gmt = new SimpleTimeZone(tzOffset, ConfigurationConstants.timeZone);

		/* GregorianCalendar with the GMT time zone */
		GregorianCalendar calendar = new GregorianCalendar(gmt);
		XMLGregorianCalendar xmlCalendar = new XMLGregorianCalendarImpl(calendar);

		audienceRestriction.getAudiences().add("spep-n1.qut.edu.au");
		audienceRestriction.getAudiences().add("spep-n2.qut.edu.au");
		conditions.getConditionsAndOneTimeUsesAndAudienceRestrictions().add(audienceRestriction);

		nameID.setValue("beddoes@qut.com");
		nameID.setFormat("urn:oasis:names:tc:SAML:2.0:something");

		subject.setNameID(nameID);
		issuer.setValue(this.issuer);

		policy.setAllowCreate(allowCreation);
		authnRequest.setNameIDPolicy(policy);

		authnRequest.setSignature(signature);
		authnRequest.setSubject(subject);
		authnRequest.setConditions(conditions);

		authnRequest.setForceAuthn(false);
		authnRequest.setIsPassive(false);
		authnRequest.setAssertionConsumerServiceIndex(0);
		authnRequest.setProviderName("spep-n1");
		authnRequest.setID(this.issuer);
		authnRequest.setVersion("2.0");
		authnRequest.setIssueInstant(xmlCalendar);
		authnRequest.setIssuer(issuer);

		result = marshaller.marshallSigned(authnRequest);

		SAMLValidator validator = new SAMLValidatorImpl(new IdentifierCacheImpl(), 100);

		validator.getRequestValidator().validate(authnRequest);

		return Base64.encodeBase64(result);
	}

	/**
	 * Creates an Invalid SAML AuthnRequest. The genertaed request is invalid because SAML version is not 2.0.
	 * 
	 * @return String containing SAML AuthnRequest
	 */
	private byte[] generateInvalidRequest() throws Exception
	{
		AudienceRestriction audienceRestriction = new AudienceRestriction();
		Conditions conditions = new Conditions();
		NameIDType nameID = new NameIDType();
		NameIDType issuer = new NameIDType();
		NameIDPolicy policy = new NameIDPolicy();
		Subject subject = new Subject();
		Signature signature = new Signature();
		AuthnRequest authnRequest = new AuthnRequest();
		byte[] result;

		/* GMT timezone */
		SimpleTimeZone gmt = new SimpleTimeZone(0, "GMT+10");

		/* GregorianCalendar with the GMT time zone */
		GregorianCalendar calendar = new GregorianCalendar(gmt);
		XMLGregorianCalendar xmlCalendar = new XMLGregorianCalendarImpl(calendar);

		audienceRestriction.getAudiences().add("spep-n1.qut.edu.au");
		audienceRestriction.getAudiences().add("spep-n2.qut.edu.au");
		conditions.getConditionsAndOneTimeUsesAndAudienceRestrictions().add(audienceRestriction);

		nameID.setValue("beddoes@qut.com");
		nameID.setFormat("urn:oasis:names:tc:SAML:2.0:something");

		subject.setNameID(nameID);
		issuer.setValue(this.issuer);

		policy.setAllowCreate(true);
		authnRequest.setNameIDPolicy(policy);

		authnRequest.setSignature(signature);
		authnRequest.setSubject(subject);
		authnRequest.setConditions(conditions);

		authnRequest.setForceAuthn(false);
		authnRequest.setIsPassive(false);
		authnRequest.setAssertionConsumerServiceIndex(0);
		authnRequest.setProviderName("spep-n1");
		authnRequest.setID("abe567de6-122wert67");

		/* Set invalid version to trip up validator */
		authnRequest.setVersion("1.0");
		authnRequest.setIssueInstant(xmlCalendar);
		authnRequest.setIssuer(issuer);

		result = marshaller.marshallSigned(authnRequest);
		return Base64.encodeBase64(result);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
	{
		try
		{
			properties = new Properties();
			handlers = new ArrayList<Handler>();
			samlValidator = new SAMLValidatorImpl(new IdentifierCacheImpl(), 120);
			sessionsProcessor = createMock(SessionsProcessor.class);
			identifierGenerator = createMock(IdentifierGenerator.class);
			request = createMock(HttpServletRequest.class);
			response = createMock(HttpServletResponse.class);
			principal = createMock(Principal.class);
			query = createMock(Query.class);
			metadata = createMock(MetadataProcessor.class);
			update = createMock(Update.class);

			data = new SSOProcessorDataImpl();
		}
		catch (Exception e)
		{
			fail("Unexpected exception state thrown when creating authnRequest");
		}

		try
		{
			// use a test keystore that currently has valid metadata
			String keyStorePath = "tests" + File.separator + "testdata" + File.separator + "testspkeystore.ks";

			String keyStorePassword = "esoekspass";
			spepKeyAlias = "54f748a6c6b8a4f8";
			spepKeyPassword = "9d600hGZQV7591nWVtNcwAtU";

			keyStoreResolver = new KeystoreResolverImpl(new File(keyStorePath), keyStorePassword, spepKeyAlias, spepKeyPassword);
			pk = keyStoreResolver.getLocalPublicKey();
			identifierMap = new HashMap<String, String>();
			identifierMap.put(NameIDFormatConstants.emailAddress, "mail");

			this.defaultSupportedType = new ArrayList<String>();
			this.defaultSupportedType.add(NameIDFormatConstants.trans);

			schemas = new String[] { SchemaConstants.samlProtocol, SchemaConstants.samlAssertion };

			/* Supplied private/public key will be in RSA format */
			marshaller = new MarshallerImpl<AuthnRequest>(AuthnRequest.class.getPackage().getName(), schemas, keyStoreResolver);
			unmarshaller = new UnmarshallerImpl<Response>(Response.class.getPackage().getName(), schemas, metadata);
		}
		catch (Exception e)
		{
			e.printStackTrace();

			fail("Unexpected exception on creating marshaller: " + e.getCause());
		}
	}

	private void setUpMock()
	{
		/* Start the replay for all our configured mock objects */
		replay(this.identifierGenerator);
		replay(this.sessionsProcessor);
		replay(this.query);
		replay(this.metadata);
		replay(request);
		replay(response);
		replay(principal);
		replay(update);
		if (entityData != null)
		{
			replay(entityData);
			replay(spepRole);
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception
	{
		// Not Implemented
	}

	private void tearDownMock()
	{
		/* Verify the mock responses */
		verify(this.identifierGenerator);
		verify(this.sessionsProcessor);
		verify(this.query);
		verify(this.metadata);
		verify(request);
		verify(response);
		verify(principal);
		verify(update);
		if (entityData != null)
		{
			verify(entityData);
			verify(spepRole);
		}
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sso.impl.SSOProcessorImpl#execute(com.qut.middleware.esoe.sso.bean.SSOProcessorData)}.
	 * Ensures null parameters are trapped
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testExecute1() throws Exception
	{
		authAuthorityProcessor = new SSOProcessorImpl(samlValidator, sessionsProcessor, this.metadata, identifierGenerator, metadata, keyStoreResolver, identifierMap, handlers, properties);
		authAuthorityProcessor.execute(null);
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sso.impl.SSOProcessorImpl#execute(com.qut.middleware.esoe.sso.bean.SSOProcessorData)}.
	 * Tests for successful sso authn response creation within an allowed time skew, should set AuthnContextClassRef to
	 * PasswordProtectedTransport
	 */
	@Test
	public void testExecute2() throws Exception
	{
		String authnIdentifier = "12345-12345";
		List<String> entities = new ArrayList<String>();
		entities.add("12345-12345");

		authAuthorityProcessor = new SSOProcessorImpl(samlValidator, sessionsProcessor, this.metadata, identifierGenerator, metadata, keyStoreResolver, identifierMap, handlers, properties);
		data.setHttpRequest(request);
		data.setHttpResponse(response);
		data.setSessionID("1234567890");
		data.setIssuerID("12345-12345");
		data.setSamlBinding(BindingConstants.httpPost);

		data.setRequestDocument(generateValidRequest(true, 0));

		expect(metadata.resolveKey(this.spepKeyAlias)).andReturn(pk).atLeastOnce();
		expect(sessionsProcessor.getQuery()).andReturn(query).atLeastOnce();
		expect(query.queryAuthnSession("1234567890")).andReturn(principal).atLeastOnce();
		
		entityData = createMock(EntityData.class);
		spepRole = createMock(SPEPRole.class);
		expect(entityData.getRoleData(SPEPRole.class)).andReturn(spepRole).anyTimes();
		expect(spepRole.getNameIDFormatList()).andReturn(this.defaultSupportedType).anyTimes();
		expect(spepRole.getAssertionConsumerServiceEndpoint(BindingConstants.httpPost, 0)).andReturn("https://spep.qut.edu.au/sso/aa").anyTimes();
		
		expect(metadata.getEntityData(this.issuer)).andReturn(entityData).anyTimes();
		expect(metadata.getEntityRoleData(this.issuer, SPEPRole.class)).andReturn(spepRole).anyTimes();

		expect(principal.getSAMLAuthnIdentifier()).andReturn(authnIdentifier).atLeastOnce();
	//	expect(principal.getActiveEntityList()).andReturn(entities).atLeastOnce();

		/* User originally authenticated basically within the same request timeframe */
		expect(principal.getAuthnTimestamp()).andReturn(System.currentTimeMillis() - 200).atLeastOnce();
		expect(principal.getAuthenticationContextClass()).andReturn(AuthenticationContextConstants.passwordProtectedTransport).atLeastOnce();
		expect(principal.getPrincipalAuthnIdentifier()).andReturn("beddoes").atLeastOnce();

		//principal.addEntitySessionIndex((String) notNull(), (String) notNull());

		TimeZone utc = new SimpleTimeZone(0, ConfigurationConstants.timeZone);
		GregorianCalendar cal = new GregorianCalendar(utc);
		// add skew offset that will keep notonorafter within allowable session range
		cal.add(Calendar.SECOND, 1000);
		expect(principal.getSessionNotOnOrAfter()).andReturn(new XMLGregorianCalendarImpl(cal).toGregorianCalendar().getTimeInMillis() + 2000000).atLeastOnce();

		expect(sessionsProcessor.getUpdate()).andReturn(update).anyTimes();
		expect(identifierGenerator.generateSAMLSessionID()).andReturn("_1234567-1234567-samlsessionid").anyTimes();
	
		update.addEntitySessionIndex((Principal)notNull(), "1234567890", this.issuer);

		expect(request.getServerName()).andReturn("http://esoe-unittest.code").anyTimes();
		expect(identifierGenerator.generateSAMLID()).andReturn("_1234567-1234567").once();
		expect(identifierGenerator.generateSAMLID()).andReturn("_890123-890123").once();

		setUpMock();

		SSOProcessor.result result = authAuthorityProcessor.execute(data);

		assertEquals("Ensure success result for response creation", SSOProcessor.result.SSOGenerationSuccessful, result);

		Response samlResponse = unmarshaller.unMarshallSigned(data.getResponseDocument());
		assertTrue("Asserts the response document InReplyTo field is the same value as the original request id", samlResponse.getInResponseTo().equals(this.issuer));

		// now validate it
		this.samlValidator.getResponseValidator().validate(samlResponse);
		
		tearDownMock();
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sso.impl.SSOProcessorImpl#execute(com.qut.middleware.esoe.sso.bean.SSOProcessorData)}.
	 * Tests for successful sso authn response creation within an allowed time skew, should set AuthnContextClassRef to
	 * previousSession
	 */
	@Test
	public void testExecute2a() throws Exception
	{
		String authnIdentifier = "12345-12345";
		List<String> entities = new ArrayList<String>();
		entities.add("12345-12345");

		authAuthorityProcessor = new SSOProcessorImpl(samlValidator, sessionsProcessor, this.metadata, identifierGenerator, metadata, keyStoreResolver, identifierMap, handlers, properties);
		data.setHttpRequest(request);
		data.setHttpResponse(response);
		data.setSessionID("1234567890");
		data.setSamlBinding(BindingConstants.httpPost);

		data.setRequestDocument(generateValidRequest(true, 0));

		expect(metadata.resolveKey(this.spepKeyAlias)).andReturn(pk).atLeastOnce();
		expect(sessionsProcessor.getQuery()).andReturn(query).atLeastOnce();
		expect(query.queryAuthnSession("1234567890")).andReturn(principal).atLeastOnce();

		entityData = createMock(EntityData.class);
		spepRole = createMock(SPEPRole.class);
		expect(entityData.getRoleData(SPEPRole.class)).andReturn(spepRole).anyTimes();
		expect(spepRole.getNameIDFormatList()).andReturn(this.defaultSupportedType).anyTimes();
		expect(spepRole.getAssertionConsumerServiceEndpoint(BindingConstants.httpPost, 0)).andReturn("https://spep.qut.edu.au/sso/aa").anyTimes();
		
		expect(metadata.getEntityData(this.issuer)).andReturn(entityData).anyTimes();
		expect(metadata.getEntityRoleData(this.issuer, SPEPRole.class)).andReturn(spepRole).anyTimes();

		expect(principal.getSAMLAuthnIdentifier()).andReturn(authnIdentifier).atLeastOnce();
		expect(principal.getActiveEntityList()).andReturn(entities).atLeastOnce();

		/* User originally authenticated a long time in the past */
		expect(principal.getAuthnTimestamp()).andReturn(System.currentTimeMillis()).atLeastOnce();
		expect(principal.getAuthenticationContextClass()).andReturn(AuthenticationContextConstants.previousSession).anyTimes();
		expect(principal.getPrincipalAuthnIdentifier()).andReturn("beddoes").atLeastOnce();

		GregorianCalendar cal = new GregorianCalendar();
		cal.add(Calendar.SECOND, 100);
		expect(principal.getSessionNotOnOrAfter()).andReturn(new XMLGregorianCalendarImpl(cal).toGregorianCalendar().getTimeInMillis()).atLeastOnce();

		principal.addEntitySessionIndex((String) notNull(), (String) notNull());

		expect(sessionsProcessor.getUpdate()).andReturn(update).anyTimes();
		expect(identifierGenerator.generateSAMLSessionID()).andReturn("_1234567-1234567-samlsessionid");
		
		expect(request.getServerName()).andReturn("http://esoe-unittest.code");
		expect(identifierGenerator.generateSAMLID()).andReturn("_1234567-1234567-samlid").once();
		expect(identifierGenerator.generateSAMLID()).andReturn("_1234567-1234568-samlid").anyTimes();

		setUpMock();

		SSOProcessor.result result = authAuthorityProcessor.execute(data);

		assertEquals("Ensure success result for response creation", SSOProcessor.result.SSOGenerationSuccessful, result);

		Response samlResponse = unmarshaller.unMarshallSigned(data.getResponseDocument());
		assertTrue("Asserts the response document InReplyTo field is the same value as the original request id", samlResponse.getInResponseTo().equals(this.issuer));

		tearDownMock();
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sso.impl.SSOProcessorImpl#execute(com.qut.middleware.esoe.sso.bean.SSOProcessorData)}.
	 * Tests for successful sso authn response creation with a timezone offset disaalowed by the specification. IE: not
	 * set to UTC+0. SAML validation should reject the reponse creation.
	 */
	@Test(expected = InvalidRequestException.class)
	public void testExecute2c() throws Exception
	{
		String authnIdentifier = "12345-12345";
		List<String> entities = new ArrayList<String>();
		entities.add("12345-12345");

		authAuthorityProcessor = new SSOProcessorImpl(samlValidator, sessionsProcessor, this.metadata, identifierGenerator, metadata, keyStoreResolver, identifierMap, handlers, properties);
		data.setHttpRequest(request);
		data.setHttpResponse(response);
		data.setSessionID("1234567890");
		data.setSamlBinding(BindingConstants.httpPost);

		data.setRequestDocument(generateInvalidRequest());

		expect(metadata.resolveKey(this.spepKeyAlias)).andReturn(pk).atLeastOnce();
		expect(sessionsProcessor.getQuery()).andReturn(query).atLeastOnce();
		expect(query.queryAuthnSession("1234567890")).andReturn(principal).atLeastOnce();

		entityData = createMock(EntityData.class);
		spepRole = createMock(SPEPRole.class);
		expect(entityData.getRoleData(SPEPRole.class)).andReturn(spepRole).anyTimes();
		expect(spepRole.getNameIDFormatList()).andReturn(this.defaultSupportedType).anyTimes();
		expect(spepRole.getAssertionConsumerServiceEndpoint(BindingConstants.httpPost, 0)).andReturn("https://spep.qut.edu.au/sso/aa").anyTimes();
		
		expect(metadata.getEntityData(this.issuer)).andReturn(entityData).anyTimes();
		expect(metadata.getEntityRoleData(this.issuer, SPEPRole.class)).andReturn(spepRole).anyTimes();

		expect(principal.getSAMLAuthnIdentifier()).andReturn(authnIdentifier).atLeastOnce();
		expect(principal.getActiveEntityList()).andReturn(entities).atLeastOnce();

		/* User originally authenticated basically within the same request timeframe */
		expect(principal.getAuthnTimestamp()).andReturn(System.currentTimeMillis() - 200).atLeastOnce();

		expect(principal.getAuthenticationContextClass()).andReturn(AuthenticationContextConstants.passwordProtectedTransport).atLeastOnce();

		expect(principal.getPrincipalAuthnIdentifier()).andReturn("beddoes").atLeastOnce();

		GregorianCalendar cal = new GregorianCalendar();
		expect(principal.getSessionNotOnOrAfter()).andReturn(new XMLGregorianCalendarImpl(cal).toGregorianCalendar().getTimeInMillis()).atLeastOnce();

		principal.addEntitySessionIndex((String) notNull(), (String) notNull());

		expect(sessionsProcessor.getUpdate()).andReturn(update).anyTimes();
		expect(identifierGenerator.generateSAMLSessionID()).andReturn("_1234567-1234567-samlsessionid");
		
		expect(request.getServerName()).andReturn("http://esoe-unittest.code");
		expect(identifierGenerator.generateSAMLID()).andReturn("_1234567-1234567").once();
		expect(identifierGenerator.generateSAMLID()).andReturn("_890123-890123").once();

		setUpMock();

		// authn processor will validate the request with the wrong timezone and reject it
		authAuthorityProcessor.execute(data);
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sso.impl.SSOProcessorImpl#execute(com.qut.middleware.esoe.sso.bean.SSOProcessorData)}.
	 * Ensures that InvalidRequestException is thrown when invalid SAML document is transfered
	 */
	@Test (expected = InvalidRequestException.class)
	public void testExecute3() throws Exception
	{
		String esoeIdentifier = "esoe";
		
		authAuthorityProcessor = new SSOProcessorImpl(samlValidator, sessionsProcessor, this.metadata, identifierGenerator, metadata, keyStoreResolver, identifierMap, handlers, properties);
		data.setHttpRequest(request);
		data.setHttpResponse(response);
		data.setSessionID("1234567890");
		data.setSamlBinding(BindingConstants.httpPost);

		/* Modify document after signing to get invalid state */
		data.setRequestDocument(generateInvalidRequest());

		expect(metadata.resolveKey(this.spepKeyAlias)).andReturn(pk).atLeastOnce();
		expect(sessionsProcessor.getQuery()).andReturn(query).atLeastOnce();
		expect(query.queryAuthnSession("1234567890")).andReturn(principal).atLeastOnce();

		entityData = createMock(EntityData.class);
		spepRole = createMock(SPEPRole.class);
		expect(entityData.getRoleData(SPEPRole.class)).andReturn(spepRole).anyTimes();
		expect(spepRole.getNameIDFormatList()).andReturn(this.defaultSupportedType).anyTimes();
		expect(spepRole.getAssertionConsumerServiceEndpoint(BindingConstants.httpPost, 0)).andReturn("http://spep.url/assertions").anyTimes();
		
		expect(metadata.getEntityData(this.issuer)).andReturn(entityData).anyTimes();
		expect(metadata.getEntityRoleData(this.issuer, SPEPRole.class)).andReturn(spepRole).anyTimes();
		
		expect(identifierGenerator.generateSAMLID()).andReturn("_1234567-1234567").once();

		setUpMock();

		authAuthorityProcessor.execute(data);

		tearDownMock();
		
		Response samlResponse = unmarshaller.unMarshallSigned(data.getResponseDocument());
		assertTrue("Asserts the response document InReplyTo field is the same value as the original request id", samlResponse.getInResponseTo().equals("abe567de6-122wert67"));
		assertTrue("Asserts that the response statuscode is of type requester", samlResponse.getStatus().getStatusCode().getValue().equals(StatusCodeConstants.requester));
		assertTrue("Asserts that the statuscode has an appropriate message", samlResponse.getStatus().getStatusMessage().contains("SAML"));
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sso.impl.SSOProcessorImpl#execute(com.qut.middleware.esoe.sso.bean.SSOProcessorData)}.
	 * Ensures that InvalidRequestException is thrown when invalid session identifier is set and session creation is not
	 * allowed
	 */
	@Test (expected = InvalidSessionIdentifierException.class)
	public void testExecute3a() throws Exception
	{
		String authnIdentifier = "12345-12345";
		List<String> entities = new ArrayList<String>();
		entities.add("12345-12345");

		authAuthorityProcessor = new SSOProcessorImpl(samlValidator, sessionsProcessor, this.metadata, identifierGenerator, metadata, keyStoreResolver, identifierMap, handlers, properties);
		data.setHttpRequest(request);
		data.setHttpResponse(response);
		data.setSessionID("1234567890");
		data.setSamlBinding(BindingConstants.httpPost);

		/* Modify document after signing to get invalid state */
		data.setRequestDocument(generateValidRequest(false, 0));

		expect(metadata.resolveKey(this.spepKeyAlias)).andReturn(pk).atLeastOnce();
		expect(sessionsProcessor.getQuery()).andReturn(query).atLeastOnce();
		
		expect(query.queryAuthnSession("1234567890")).andReturn(null);
		
		entityData = createMock(EntityData.class);
		spepRole = createMock(SPEPRole.class);
		expect(entityData.getRoleData(SPEPRole.class)).andReturn(spepRole).anyTimes();
		expect(spepRole.getNameIDFormatList()).andReturn(this.defaultSupportedType).anyTimes();
		expect(spepRole.getAssertionConsumerServiceEndpoint(BindingConstants.httpPost, 0)).andReturn("https://spep.qut.edu.au/sso/aa").anyTimes();
		
		expect(metadata.getEntityData(this.issuer)).andReturn(entityData).anyTimes();
		expect(metadata.getEntityRoleData(this.issuer, SPEPRole.class)).andReturn(spepRole).anyTimes();

		expect(principal.getSAMLAuthnIdentifier()).andReturn(authnIdentifier).atLeastOnce();
		expect(principal.getActiveEntityList()).andReturn(entities).atLeastOnce();
		;
		/* User originally authenticated basically within the same request timeframe */
		expect(principal.getAuthnTimestamp()).andReturn(System.currentTimeMillis() - 200).atLeastOnce();
		;
		expect(principal.getAuthenticationContextClass()).andReturn(AuthenticationContextConstants.passwordProtectedTransport).atLeastOnce();
		;

		expect(sessionsProcessor.getUpdate()).andReturn(update).anyTimes();
		expect(identifierGenerator.generateSAMLSessionID()).andReturn("_1234567-1234567-samlsessionid");
		try
		{
			update.addEntitySessionIndex((Principal)notNull(), "1234567890", this.issuer);
		}
		catch (SessionCacheUpdateException e)
		{
			fail("Unexpected InvalidSessionIdentifierException: " + e.getMessage());
		}

		expect(request.getServerName()).andReturn("http://esoe-unittest.code");
		expect(identifierGenerator.generateSAMLID()).andReturn("_1234567-1234567").once();
		expect(identifierGenerator.generateSAMLID()).andReturn("_890123-890123").once();

		setUpMock();

		authAuthorityProcessor.execute(data);

		tearDownMock();
		
		Response samlResponse = unmarshaller.unMarshallSigned(data.getResponseDocument());
		assertTrue("Asserts the response document InReplyTo field is the same value as the original request id", samlResponse.getInResponseTo().equals(this.issuer));
		assertTrue("Asserts that the response statuscode is of type requester", samlResponse.getStatus().getStatusCode().getValue().equals(StatusCodeConstants.responder));
		assertTrue("Asserts that the statuscode has an appropriate message", samlResponse.getStatus().getStatusMessage().contains("session"));
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sso.impl.SSOProcessorImpl#execute(com.qut.middleware.esoe.sso.bean.SSOProcessorData)}.
	 * Ensures that InvalidRequestException is thrown when transit modified SAML document is transfered
	 */
	@Test(expected = InvalidRequestException.class)
	public void testExecute4() throws Exception
	{
		String esoeIdentifier = "esoe";
		
		authAuthorityProcessor = new SSOProcessorImpl(samlValidator, sessionsProcessor, this.metadata, identifierGenerator, metadata, keyStoreResolver, identifierMap, handlers, properties);
		data.setHttpRequest(request);
		data.setHttpResponse(response);
		data.setSessionID("1234567890");
		data.setSamlBinding(BindingConstants.httpPost);

		/* Modify document after signing to get invalid state */
		byte[] doc = generateValidRequest(true, 0);
		doc[10] = '~';
		data.setRequestDocument(doc);

		expect(metadata.resolveKey(this.spepKeyAlias)).andReturn(pk).atLeastOnce();
		expect(sessionsProcessor.getQuery()).andReturn(query).atLeastOnce();
		expect(query.queryAuthnSession("1234567890")).andReturn(principal).atLeastOnce();

		setUpMock();

		authAuthorityProcessor.execute(data);

		tearDownMock();
	}
}
