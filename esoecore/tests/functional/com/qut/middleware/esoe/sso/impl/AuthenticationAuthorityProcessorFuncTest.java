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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
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

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.crypto.KeyStoreResolver;
import com.qut.middleware.esoe.crypto.impl.KeyStoreResolverImpl;
import com.qut.middleware.esoe.metadata.Metadata;
import com.qut.middleware.esoe.metadata.exception.InvalidMetadataEndpointException;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.Query;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.esoe.sessions.Update;
import com.qut.middleware.esoe.sessions.exception.InvalidDescriptorIdentifierException;
import com.qut.middleware.esoe.sso.SSOProcessor;
import com.qut.middleware.esoe.sso.bean.SSOProcessorData;
import com.qut.middleware.esoe.sso.bean.impl.SSOProcessorDataImpl;
import com.qut.middleware.esoe.sso.exception.InvalidRequestException;
import com.qut.middleware.esoe.sso.exception.InvalidSessionIdentifierException;
import com.qut.middleware.saml2.AuthenticationContextConstants;
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
	private AuthenticationAuthorityProcessor authAuthorityProcessor;
	private SSOProcessorData data;
	private SAMLValidator samlValidator;
	private SessionsProcessor sessionsProcessor;
	private Query query;
	private Update update;
	private IdentifierGenerator identifierGenerator;
	private Metadata metadata;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private Principal principal;

	private String[] schemas;

	private Marshaller<AuthnRequest> marshaller;
	private Unmarshaller<Response> unmarshaller;

	private PrivateKey privKey;
	private PublicKey pk;
	private KeyStoreResolver keyStoreResolver;

	private String spepKeyAlias;
	private String spepKeyPassword;
	private String issuer = "_bbb7b47de6cd6c227ba78c340137afcbab08cf94-efb1d452f76659a1b10519ab5d53c03c";
	
		
	public AuthenticationAuthorityProcessorFuncTest()
	{
		// Not Implemented
	}

	/**
	 * Creates a valid SAML AuthnRequest like would be supplied by an SPEP
	 * 
	 * @param allowCreation field to modify in the request to cause a difference in generated output.
	 * @param tzOffset Specify an offset when creating xml timestamps. Specification says this must
	 * be set to 0. Any variations should cause SAML validation to reject the document.
	 * @return String containing SAML AuthnRequest
	 */
	private String generateValidRequest(boolean allowCreation, int tzOffset)
	{
		try
		{
			AudienceRestriction audienceRestriction = new AudienceRestriction();
			Conditions conditions = new Conditions();
			NameIDType nameID = new NameIDType();
			NameIDType issuer = new NameIDType();
			NameIDPolicy policy = new NameIDPolicy();
			Subject subject = new Subject();
			Signature signature = new Signature();
			AuthnRequest authnRequest = new AuthnRequest();
			String result;

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
			
			String tmp = new String(Base64.encodeBase64(result.getBytes()));
				
			////System.out.println(result); 
			////System.out.println("\n\n"+tmp);
			SAMLValidator validator = new SAMLValidatorImpl(new IdentifierCacheImpl(), 100);
			
			validator.getRequestValidator().validate(authnRequest);
			
			return result;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Unexpected exception state thrown when creating authnRequest");
			return null;
		}
	}

	/**
	 * Creates an Invalid SAML AuthnRequest. The genertaed request is invalid because 
	 * SAML version is not 2.0.
	 * 
	 * @return String containing SAML AuthnRequest
	 */
	private String generateInvalidRequest()
	{
		try
		{
			AudienceRestriction audienceRestriction = new AudienceRestriction();
			Conditions conditions = new Conditions();
			NameIDType nameID = new NameIDType();
			NameIDType issuer = new NameIDType();
			NameIDPolicy policy = new NameIDPolicy();
			Subject subject = new Subject();
			Signature signature = new Signature();
			AuthnRequest authnRequest = new AuthnRequest();
			String result;

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
			return result;
		}
		catch (Exception e)
		{
			fail("Unexpected exception state thrown when creating authnRequest");
			return null;
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
	{
		try
		{
			samlValidator = new SAMLValidatorImpl(new IdentifierCacheImpl(), 120);
			sessionsProcessor = createMock(SessionsProcessor.class);
			identifierGenerator = createMock(IdentifierGenerator.class);
			request = createMock(HttpServletRequest.class);
			response = createMock(HttpServletResponse.class);
			principal = createMock(Principal.class);
			query = createMock(Query.class);
			metadata = createMock(Metadata.class);
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
			String keyStorePath = System.getProperty("user.dir") + File.separator + "tests" + File.separator + "testdata" + File.separator + "testspkeystore.ks";

			String keyStorePassword = "esoekspass";
			spepKeyAlias = "54f748a6c6b8a4f8";
			spepKeyPassword = "9d600hGZQV7591nWVtNcwAtU";

			keyStoreResolver = new KeyStoreResolverImpl(new File(keyStorePath), keyStorePassword, spepKeyAlias, spepKeyPassword);
			privKey = keyStoreResolver.getPrivateKey();
			pk = keyStoreResolver.resolveKey(spepKeyAlias);
			
			schemas = new String[] { ConfigurationConstants.samlProtocol, ConfigurationConstants.samlAssertion };

			/* Supplied private/public key will be in RSA format */
			marshaller = new MarshallerImpl<AuthnRequest>(AuthnRequest.class.getPackage().getName(), schemas, spepKeyAlias, privKey);
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
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sso.impl.AuthenticationAuthorityProcessor#execute(com.qut.middleware.esoe.sso.bean.SSOProcessorData)}.
	 * Ensures null parameters are trapped
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testExecute1() throws UnmarshallerException, MarshallerException, InvalidSessionIdentifierException,
			InvalidRequestException
	{
		authAuthorityProcessor = new AuthenticationAuthorityProcessor(samlValidator, sessionsProcessor, this.metadata,
				identifierGenerator, metadata, keyStoreResolver, ConfigurationConstants.samlProtocol, 120, 20);
		authAuthorityProcessor.execute(null);
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sso.impl.AuthenticationAuthorityProcessor#execute(com.qut.middleware.esoe.sso.bean.SSOProcessorData)}.
	 * Tests for successful sso authn response creation within an allowed time skew, should set AuthnContextClassRef to
	 * PasswordProtectedTransport
	 */
	@Test
	public void testExecute2() throws UnmarshallerException, MarshallerException, FileNotFoundException, IOException,
			InvalidRequestException, InvalidSessionIdentifierException,
			com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException
	{
		try
		{
			String authnIdentifier = "12345-12345";
			List<String> entities = new ArrayList<String>();
			entities.add("12345-12345");

			authAuthorityProcessor = new AuthenticationAuthorityProcessor(samlValidator, sessionsProcessor,
					this.metadata, identifierGenerator, metadata, keyStoreResolver,
					ConfigurationConstants.samlProtocol, 120, 20);
			data.setHttpRequest(request);
			data.setHttpResponse(response);
			data.setSessionID("1234567890");
			data.setDescriptorID("12345-12345");
		
			data.setRequestDocument(generateValidRequest(true, 0));

			expect(metadata.resolveKey(this.spepKeyAlias)).andReturn(pk).atLeastOnce();
			expect(sessionsProcessor.getQuery()).andReturn(query).atLeastOnce();
			expect(query.queryAuthnSession("1234567890")).andReturn(principal).atLeastOnce();
			

			expect(metadata.resolveAssertionConsumerService(this.issuer, 0)).andReturn(
					"https://spep.qut.edu.au/sso/aa");

			expect(principal.getSAMLAuthnIdentifier()).andReturn(authnIdentifier).atLeastOnce();
			expect(principal.getActiveDescriptors()).andReturn(entities).atLeastOnce();
			
			/* User originally authenticated basically within the same request timeframe */
			expect(principal.getAuthnTimestamp()).andReturn(System.currentTimeMillis() - 200).atLeastOnce();		
			expect(principal.getAuthenticationContextClass()).andReturn(
					AuthenticationContextConstants.passwordProtectedTransport).atLeastOnce();
			expect(principal.getPrincipalAuthnIdentifier()).andReturn("beddoes").atLeastOnce();
			
			principal.addDescriptorSessionIdentifier((String)notNull(), (String)notNull());
			
			TimeZone utc = new SimpleTimeZone(0, ConfigurationConstants.timeZone); 
			GregorianCalendar cal = new GregorianCalendar(utc);
			// add skew offset that will keep notonorafter within allowable session range
			cal.add(Calendar.SECOND, 1000);
			expect(principal.getSessionNotOnOrAfter()).andReturn(new XMLGregorianCalendarImpl(cal)).atLeastOnce();

			expect(sessionsProcessor.getUpdate()).andReturn(update).anyTimes();
			expect(identifierGenerator.generateSAMLSessionID()).andReturn("_1234567-1234567-samlsessionid");
			update.updateDescriptorList("1234567890", this.issuer);
			update.updateDescriptorSessionIdentifierList("1234567890", this.issuer, "_1234567-1234567-samlsessionid");

			expect(request.getServerName()).andReturn("http://esoe-unittest.code");
			expect(metadata.getESOEIdentifier()).andReturn("esoeID");
			expect(identifierGenerator.generateSAMLID()).andReturn("_1234567-1234567").once();
			expect(identifierGenerator.generateSAMLID()).andReturn("_890123-890123").once();

			setUpMock();

			SSOProcessor.result result = authAuthorityProcessor.execute(data);

			assertEquals("Ensure success result for response creation", SSOProcessor.result.SSOGenerationSuccessful,
					result);

			try
			{
				Response samlResponse = unmarshaller.unMarshallSigned(data.getResponseDocument());
				assertTrue(
						"Asserts the response document InReplyTo field is the same value as the original request id",
						samlResponse.getInResponseTo().equals(this.issuer));
			
				// now validate it
				this.samlValidator.getResponseValidator().validate(samlResponse);
				
			}
			catch (SignatureValueException e)
			{
				e.printStackTrace();
				fail("This exception should not occur in this test");
			}
			catch (ReferenceValueException e)
			{
				e.printStackTrace();
				fail("This exception should not occur in this test");
			}

			catch(InvalidSAMLResponseException e)
			{
				e.printStackTrace();
				fail("This exception should not occur in this test");
			}
			tearDownMock();
		}
		catch (InvalidMetadataEndpointException imee)
		{
			fail("Unexpected InvalidMetadataEndpointException: " + imee.getMessage());
		}
		catch (InvalidDescriptorIdentifierException ieie)
		{
			fail("Unexpected InvalidEntityIdentifierException: " + ieie.getMessage());
		}
		catch (KeyResolutionException e)
		{
			fail("Unexpected InvalidEntityIdentifierException: " + e.getMessage());
		}
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sso.impl.AuthenticationAuthorityProcessor#execute(com.qut.middleware.esoe.sso.bean.SSOProcessorData)}.
	 * Tests for successful sso authn response creation within an allowed time skew, should set AuthnContextClassRef to
	 * previousSession
	 */
	@Test
	public void testExecute2a() throws UnmarshallerException, MarshallerException, InvalidRequestException,
			InvalidSessionIdentifierException,
			com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException
	{
		try
		{
			String authnIdentifier = "12345-12345";
			List<String> entities = new ArrayList<String>();
			entities.add("12345-12345");

			authAuthorityProcessor = new AuthenticationAuthorityProcessor(samlValidator, sessionsProcessor,
					this.metadata, identifierGenerator, metadata, keyStoreResolver,
					ConfigurationConstants.samlProtocol, 120, 20);
			data.setHttpRequest(request);
			data.setHttpResponse(response);
			data.setSessionID("1234567890");

			data.setRequestDocument(generateValidRequest(true, 0));

			expect(metadata.resolveKey(this.spepKeyAlias)).andReturn(pk).atLeastOnce();
			expect(sessionsProcessor.getQuery()).andReturn(query).atLeastOnce();
			expect(query.queryAuthnSession("1234567890")).andReturn(principal).atLeastOnce();
			

			expect(metadata.resolveAssertionConsumerService(this.issuer, 0)).andReturn(
					"https://spep.qut.edu.au/sso/aa");

			expect(principal.getSAMLAuthnIdentifier()).andReturn(authnIdentifier).atLeastOnce();
			expect(principal.getActiveDescriptors()).andReturn(entities).atLeastOnce();
	
			/* User originally authenticated a long time in the past */
			expect(principal.getAuthnTimestamp()).andReturn(System.currentTimeMillis()).atLeastOnce();
			expect(principal.getAuthenticationContextClass()).andReturn(AuthenticationContextConstants.previousSession)
					.anyTimes();
			expect(principal.getPrincipalAuthnIdentifier()).andReturn("beddoes").atLeastOnce();

			GregorianCalendar cal = new GregorianCalendar();
			cal.add(Calendar.SECOND, 100);
			expect(principal.getSessionNotOnOrAfter()).andReturn(new XMLGregorianCalendarImpl(cal)).atLeastOnce();

			principal.addDescriptorSessionIdentifier((String)notNull(), (String)notNull());
			
			expect(sessionsProcessor.getUpdate()).andReturn(update).anyTimes();
			expect(identifierGenerator.generateSAMLSessionID()).andReturn("_1234567-1234567-samlsessionid");
			update.updateDescriptorList("1234567890", this.issuer);
			update.updateDescriptorSessionIdentifierList("1234567890", this.issuer, "_1234567-1234567-samlsessionid");

			expect(request.getServerName()).andReturn("http://esoe-unittest.code");
			expect(metadata.getESOEIdentifier()).andReturn("esoeID");
			expect(identifierGenerator.generateSAMLID()).andReturn("_1234567-1234567-samlid").once();
			expect(identifierGenerator.generateSAMLID()).andReturn("_1234567-1234568-samlid").anyTimes();

			setUpMock();

			SSOProcessor.result result = authAuthorityProcessor.execute(data);

			assertEquals("Ensure success result for response creation", SSOProcessor.result.SSOGenerationSuccessful,
					result);

			try
			{
				Response samlResponse = unmarshaller.unMarshallSigned(data.getResponseDocument());
				assertTrue(
						"Asserts the response document InReplyTo field is the same value as the original request id",
						samlResponse.getInResponseTo().equals(this.issuer));
			}
			catch (SignatureValueException e)
			{
				e.printStackTrace();
				fail("This exception should not occur in this test");
			}
			catch (ReferenceValueException e)
			{
				e.printStackTrace();
				fail("This exception should not occur in this test");
			}

			tearDownMock();
		}
		catch (InvalidMetadataEndpointException imee)
		{
			fail("Unexpected InvalidMetadataEndpointException: " + imee.getMessage());
		}
		catch (InvalidDescriptorIdentifierException ieie)
		{
			fail("Unexpected InvalidEntityIdentifierException: " + ieie.getMessage());
		}
		catch (KeyResolutionException e)
		{
			fail("Unexpected InvalidEntityIdentifierException: " + e.getMessage());
		}
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sso.impl.AuthenticationAuthorityProcessor#execute(com.qut.middleware.esoe.sso.bean.SSOProcessorData)}.
	 * Tests for successful sso authn response creation with a timezone offset disaalowed by the specification. IE: not set to UTC+0. SAML
	 * validation should reject the reponse creation.
	 */
	@Test
	public void testExecute2c() throws UnmarshallerException, MarshallerException, FileNotFoundException, IOException,
			InvalidSessionIdentifierException,
			com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException
	{
		try
		{
			String authnIdentifier = "12345-12345";
			List<String> entities = new ArrayList<String>();
			entities.add("12345-12345");

			authAuthorityProcessor = new AuthenticationAuthorityProcessor(samlValidator, sessionsProcessor,
					this.metadata, identifierGenerator, metadata, keyStoreResolver,
					ConfigurationConstants.samlProtocol, 120, 20);
			data.setHttpRequest(request);
			data.setHttpResponse(response);
			data.setSessionID("1234567890");

			data.setRequestDocument(generateInvalidRequest());

			expect(metadata.resolveKey(this.spepKeyAlias)).andReturn(pk).atLeastOnce();
			expect(sessionsProcessor.getQuery()).andReturn(query).atLeastOnce();
			expect(query.queryAuthnSession("1234567890")).andReturn(principal).atLeastOnce();
			

			expect(metadata.resolveAssertionConsumerService(this.issuer, 0)).andReturn(
					"https://spep.qut.edu.au/sso/aa");

			expect(principal.getSAMLAuthnIdentifier()).andReturn(authnIdentifier).atLeastOnce();
			expect(principal.getActiveDescriptors()).andReturn(entities).atLeastOnce();
			;
			/* User originally authenticated basically within the same request timeframe */
			expect(principal.getAuthnTimestamp()).andReturn(System.currentTimeMillis() - 200).atLeastOnce();
			;
			expect(principal.getAuthenticationContextClass()).andReturn(
					AuthenticationContextConstants.passwordProtectedTransport).atLeastOnce();
			;
			expect(principal.getPrincipalAuthnIdentifier()).andReturn("beddoes").atLeastOnce();
			
			GregorianCalendar cal = new GregorianCalendar();
			expect(principal.getSessionNotOnOrAfter()).andReturn(new XMLGregorianCalendarImpl(cal)).atLeastOnce();

			principal.addDescriptorSessionIdentifier((String)notNull(), (String)notNull());
			
			expect(sessionsProcessor.getUpdate()).andReturn(update).anyTimes();
			expect(identifierGenerator.generateSAMLSessionID()).andReturn("_1234567-1234567-samlsessionid");
			update.updateDescriptorList("1234567890", this.issuer);
			update.updateDescriptorSessionIdentifierList("1234567890", this.issuer, "_1234567-1234567-samlsessionid");

			expect(request.getServerName()).andReturn("http://esoe-unittest.code");
			expect(metadata.getESOEIdentifier()).andReturn("esoeID");
			expect(identifierGenerator.generateSAMLID()).andReturn("_1234567-1234567").once();
			expect(identifierGenerator.generateSAMLID()).andReturn("_890123-890123").once();

			setUpMock();

			try
			{
				// authn processor will validate the request with the wrong timezone and reject it
				authAuthorityProcessor.execute(data);
			
			}
			catch(InvalidRequestException e)
			{
				return;
			}
		
			// the SAML request was not rejected as expected
			fail("SAML Request was not rejected.");
			
		}
		catch (InvalidMetadataEndpointException imee)
		{
			fail("Unexpected InvalidMetadataEndpointException: " + imee.getMessage());
		}
		catch (InvalidDescriptorIdentifierException ieie)
		{
			fail("Unexpected InvalidEntityIdentifierException: " + ieie.getMessage());
		}
		catch (KeyResolutionException e)
		{
			fail("Unexpected InvalidEntityIdentifierException: " + e.getMessage());
		}
	}
	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sso.impl.AuthenticationAuthorityProcessor#execute(com.qut.middleware.esoe.sso.bean.SSOProcessorData)}.
	 * Ensures that InvalidRequestException is thrown when invalid SAML document is transfered
	 */
	@Test
	public void testExecute3()
	{
		try
		{
			authAuthorityProcessor = new AuthenticationAuthorityProcessor(samlValidator, sessionsProcessor,
					this.metadata, identifierGenerator, metadata, keyStoreResolver,
					ConfigurationConstants.samlProtocol, 120, 20);
			data.setHttpRequest(request);
			data.setHttpResponse(response);
			data.setSessionID("1234567890");

			/* Modify document after signing to get invalid state */
			data.setRequestDocument(generateInvalidRequest());

			expect(metadata.resolveKey(this.spepKeyAlias)).andReturn(pk).atLeastOnce();
			expect(sessionsProcessor.getQuery()).andReturn(query).atLeastOnce();
			expect(query.queryAuthnSession("1234567890")).andReturn(principal).atLeastOnce();
			
			expect(metadata.resolveAssertionConsumerService(this.issuer, 0)).andReturn("http://spep.url/assertions");
			expect(metadata.getESOEIdentifier()).andReturn("esoe").atLeastOnce();
			expect(identifierGenerator.generateSAMLID()).andReturn("_1234567-1234567").once();

			setUpMock();

			authAuthorityProcessor.execute(data);

			tearDownMock();
		}
		catch (InvalidRequestException e)
		{
			try
			{
				Response samlResponse = unmarshaller.unMarshallSigned(data.getResponseDocument());
				assertTrue(
						"Asserts the response document InReplyTo field is the same value as the original request id",
						samlResponse.getInResponseTo().equals("abe567de6-122wert67"));
				assertTrue("Asserts that the response statuscode is of type requester", samlResponse.getStatus()
						.getStatusCode().getValue().equals(StatusCodeConstants.requester));
				assertTrue("Asserts that the statuscode has an appropriate message", samlResponse.getStatus()
						.getStatusMessage().contains("SAML"));
			}
			catch (SignatureValueException sve)
			{
				fail("This exception should not occur in this test " + sve.getMessage());
			}
			catch (ReferenceValueException rve)
			{
				fail("This exception should not occur in this test " + rve.getMessage());
			}
			catch (UnmarshallerException ue)
			{
				fail("Unexpected UnmarshallerException: " + ue.getMessage());
			}
		}
		catch (UnmarshallerException e)
		{
			fail("Unexpected UnmarshallerException: " + e.getMessage());
		}
		catch (MarshallerException e)
		{
			fail("Unexpected MarshallerException: " + e.getMessage());
		}
		catch (com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException e)
		{
			fail("Unexpected InvalidSessionIdentifierException: " + e.getMessage());
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("Unexpected InvalidSessionIdentifierException: " + e.getMessage());
		}
		catch (KeyResolutionException e)
		{
			fail("Unexpected InvalidEntityIdentifierException: " + e.getMessage());
		}
		catch (InvalidMetadataEndpointException e)
		{
			fail("Unexpected InvalidMetadataEndpointException: " + e.getMessage());
		}
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sso.impl.AuthenticationAuthorityProcessor#execute(com.qut.middleware.esoe.sso.bean.SSOProcessorData)}.
	 * Ensures that InvalidRequestException is thrown when invalid session identifier is set and session creation is not allowed
	 */
	@Test
	public void testExecute3a()
	{
		try
		{
			String authnIdentifier = "12345-12345";
			List<String> entities = new ArrayList<String>();
			entities.add("12345-12345");

			authAuthorityProcessor = new AuthenticationAuthorityProcessor(samlValidator, sessionsProcessor,
					this.metadata, identifierGenerator, metadata, keyStoreResolver,
					ConfigurationConstants.samlProtocol, 120, 20);
			data.setHttpRequest(request);
			data.setHttpResponse(response);
			data.setSessionID("1234567890");

			/* Modify document after signing to get invalid state */
			data.setRequestDocument(generateValidRequest(false, 0));

			expect(metadata.resolveKey(this.spepKeyAlias)).andReturn(pk).atLeastOnce();
			expect(sessionsProcessor.getQuery()).andReturn(query).atLeastOnce();
			try
			{
				expect(query.queryAuthnSession("1234567890")).andThrow(
						new com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException());
			}
			catch (com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException e)
			{
				fail("Unexpected InvalidSessionIdentifierException: " + e.getMessage());
			}
			

			expect(metadata.resolveAssertionConsumerService(this.issuer, 0)).andReturn(
					"https://spep.qut.edu.au/sso/aa");

			expect(principal.getSAMLAuthnIdentifier()).andReturn(authnIdentifier).atLeastOnce();
			expect(principal.getActiveDescriptors()).andReturn(entities).atLeastOnce();
			;
			/* User originally authenticated basically within the same request timeframe */
			expect(principal.getAuthnTimestamp()).andReturn(System.currentTimeMillis() - 200).atLeastOnce();
			;
			expect(principal.getAuthenticationContextClass()).andReturn(
					AuthenticationContextConstants.passwordProtectedTransport).atLeastOnce();
			;

			expect(sessionsProcessor.getUpdate()).andReturn(update).anyTimes();
			expect(identifierGenerator.generateSAMLSessionID()).andReturn("_1234567-1234567-samlsessionid");
			try
			{
				update.updateDescriptorList("1234567890", this.issuer);
				update.updateDescriptorSessionIdentifierList("1234567890", this.issuer,
						"_1234567-1234567-samlsessionid");
			}
			catch (com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException e)
			{
				fail("Unexpected InvalidSessionIdentifierException: " + e.getMessage());
			}

			expect(request.getServerName()).andReturn("http://esoe-unittest.code");
			expect(metadata.getESOEIdentifier()).andReturn("esoeID");
			expect(identifierGenerator.generateSAMLID()).andReturn("_1234567-1234567").once();
			expect(identifierGenerator.generateSAMLID()).andReturn("_890123-890123").once();

			setUpMock();

			authAuthorityProcessor.execute(data);

			tearDownMock();
		}
		catch (InvalidRequestException e)
		{
			fail("Unexpected InvalidRequestException: " + e.getMessage());
		}
		catch (UnmarshallerException e)
		{
			fail("Unexpected UnmarshallerException: " + e.getMessage());
		}
		catch (MarshallerException e)
		{
			fail("Unexpected MarshallerException: " + e.getMessage());
		}
		catch (InvalidSessionIdentifierException e)
		{
			try
			{
				Response samlResponse = unmarshaller.unMarshallSigned(data.getResponseDocument());
				assertTrue(
						"Asserts the response document InReplyTo field is the same value as the original request id",
						samlResponse.getInResponseTo().equals(this.issuer));
				assertTrue("Asserts that the response statuscode is of type requester", samlResponse.getStatus()
						.getStatusCode().getValue().equals(StatusCodeConstants.responder));
				assertTrue("Asserts that the statuscode has an appropriate message", samlResponse.getStatus()
						.getStatusMessage().contains("session"));
			}
			catch (SignatureValueException sve)
			{
				fail("This exception should not occur in this test " + sve.getMessage());
			}
			catch (ReferenceValueException rve)
			{
				fail("This exception should not occur in this test " + rve.getMessage());
			}
			catch (UnmarshallerException ue)
			{
				fail("Unexpected UnmarshallerException: " + ue.getMessage());
			}
		}
		catch (InvalidDescriptorIdentifierException e)
		{
			fail("Unexpected InvalidEntityIdentifierException: " + e.getMessage());
		}
		catch (InvalidMetadataEndpointException e)
		{
			fail("Unexpected InvalidMetadataEndpointException: " + e.getMessage());
		}
		catch (KeyResolutionException e)
		{
			fail("Unexpected InvalidEntityIdentifierException: " + e.getMessage());
		}
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sso.impl.AuthenticationAuthorityProcessor#execute(com.qut.middleware.esoe.sso.bean.SSOProcessorData)}.
	 * Ensures that InvalidRequestException is thrown when transit modified SAML document is transfered
	 */
	@Test(expected = InvalidRequestException.class)
	public void testExecute4() throws InvalidRequestException
	{
		try
		{
			authAuthorityProcessor = new AuthenticationAuthorityProcessor(samlValidator, sessionsProcessor,
					this.metadata, identifierGenerator, metadata, keyStoreResolver,
					ConfigurationConstants.samlProtocol, 120, 20);
			data.setHttpRequest(request);
			data.setHttpResponse(response);
			data.setSessionID("1234567890");

			/* Modify document after signing to get invalid state */
			data.setRequestDocument(generateValidRequest(true, 0) + "a");

			expect(metadata.resolveKey(this.spepKeyAlias)).andReturn(pk).atLeastOnce();
			expect(sessionsProcessor.getQuery()).andReturn(query).atLeastOnce();
			expect(query.queryAuthnSession("1234567890")).andReturn(principal).atLeastOnce();
			
			setUpMock();

			authAuthorityProcessor.execute(data);

			tearDownMock();
		}
		catch (UnmarshallerException e)
		{
			fail("Unexpected UnmarshallerException: " + e.getMessage());
		}
		catch (MarshallerException e)
		{
			fail("Unexpected MarshallerException: " + e.getMessage());
		}
		catch (com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException e)
		{
			fail("Unexpected InvalidSessionIdentifierException: " + e.getMessage());
		}
		catch (InvalidSessionIdentifierException e)
		{
			fail("Unexpected InvalidSessionIdentifierException: " + e.getMessage());
		}
		catch (KeyResolutionException e)
		{
			fail("Unexpected InvalidEntityIdentifierException: " + e.getMessage());
		}
	}
}
