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
 * Author: Bradley Beddoes
 * Creation Date: 28/02/2007
 * 
 * Purpose: Tests SPEPProcessorImpl for correct operation of termination of single principal authz cache clear to remote SPEP
 */
package com.qut.middleware.esoe.spep;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3._2000._09.xmldsig_.Signature;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.crypto.KeyStoreResolver;
import com.qut.middleware.esoe.crypto.impl.KeyStoreResolverImpl;
import com.qut.middleware.esoe.metadata.Metadata;
import com.qut.middleware.esoe.metadata.exception.InvalidMetadataEndpointException;
import com.qut.middleware.esoe.pdp.cache.AuthzCacheUpdateFailureRepository;
import com.qut.middleware.esoe.pdp.cache.bean.FailedAuthzCacheUpdate;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.bean.impl.IdentityDataImpl;
import com.qut.middleware.esoe.sessions.impl.PrincipalImpl;
import com.qut.middleware.esoe.spep.SPEPProcessor;
import com.qut.middleware.esoe.spep.Startup;
import com.qut.middleware.esoe.spep.impl.SPEPProcessorImpl;
import com.qut.middleware.esoe.ws.WSClient;
import com.qut.middleware.esoe.ws.exception.WSClientException;
import com.qut.middleware.saml2.StatusCodeConstants;
import com.qut.middleware.saml2.VersionConstants;
import com.qut.middleware.saml2.exception.InvalidSAMLResponseException;
import com.qut.middleware.saml2.exception.KeyResolutionException;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.identifier.IdentifierCache;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Request;
import com.qut.middleware.saml2.schemas.esoe.protocol.ClearAuthzCacheRequest;
import com.qut.middleware.saml2.schemas.esoe.protocol.ClearAuthzCacheResponse;
import com.qut.middleware.saml2.schemas.protocol.Status;
import com.qut.middleware.saml2.schemas.protocol.StatusCode;
import com.qut.middleware.saml2.schemas.protocol.StatusResponseType;
import com.qut.middleware.saml2.validator.SAMLResponseValidator;
import com.qut.middleware.saml2.validator.SAMLValidator;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

public class SPEPProcessorTest
{
	private AuthzCacheUpdateFailureRepository failureRep;
	private Metadata metadata;
	private WSClient webServiceClient;
	private KeyStoreResolver keyStoreResolver;
	private Marshaller<ClearAuthzCacheResponse> clearAuthzCacheResponseMarshaller;
	private Startup startup;
	private SAMLValidator samlValidator;
	private SAMLResponseValidator respVal;

	private IdentifierGenerator idGenerator;
	private IdentifierCache identifierCache;

	private final String SAMLID = "_12345-12345-123";
	private final String DESC1 = "_098098-098098";
	private final String DESC2 = "_098098-0980981";
	private final String esoeKeyAlias = "esoeprimary";
	public Map<Integer,String> endpoints;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		this.failureRep = createMock(AuthzCacheUpdateFailureRepository.class);
		this.metadata = createMock(Metadata.class);
		this.startup = createMock(Startup.class);
		this.samlValidator = createMock(SAMLValidator.class);
		this.respVal = createMock(SAMLResponseValidator.class);
		this.webServiceClient = createMock(WSClient.class);
		this.idGenerator = createMock(IdentifierGenerator.class);

		this.endpoints = Collections.synchronizedMap(new HashMap<Integer,String>());
		this.endpoints.put(0,"http://spep.qut.edu.au/ws/Service");
		this.endpoints.put(1,"http://spep2.qut.edu.au/ws/Service");

		String keyStorePath = System.getProperty("user.dir") + File.separator + "tests" + File.separator + "testdata" + File.separator
				+ "testskeystore.ks";
		String keyStorePassword = "Es0EKs54P4SSPK";
		String esoeKeyAlias = "esoeprimary";
		String esoeKeyPassword = "Es0EKs54P4SSPK";

		this.keyStoreResolver = new KeyStoreResolverImpl(new File(keyStorePath), keyStorePassword, esoeKeyAlias,
				esoeKeyPassword);

		String[] clearAuthzCacheSchemas = new String[] { ConfigurationConstants.esoeProtocol,
				ConfigurationConstants.samlAssertion, ConfigurationConstants.samlProtocol };
		this.clearAuthzCacheResponseMarshaller = new MarshallerImpl<ClearAuthzCacheResponse>(
				ClearAuthzCacheRequest.class.getPackage().getName() + ":" + Request.class.getPackage().getName(),
				clearAuthzCacheSchemas, keyStoreResolver.getKeyAlias(), keyStoreResolver.getPrivateKey());

	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception
	{
	}

	private void setUpMock()
	{
		/* Start the replay for all our configured mock objects */
		replay(failureRep);
		replay(metadata);
		replay(startup);
		replay(samlValidator);
		replay(respVal);
		replay(webServiceClient);
		replay(idGenerator);
	}

	private void tearDownMock()
	{
		/* Verify the mock responses */
		verify(failureRep);
		verify(metadata);
		verify(startup);
		verify(samlValidator);
		verify(respVal);
		verify(webServiceClient);
		verify(idGenerator);
	}

	/*
	 * Creates SPEPProcessor and tests for correct outcome from clearPrincipalSPEPCaches in success state with single
	 * SPEP
	 */
	@Test
	public void testSPEPProcessor1()
	{
		Principal principal = new PrincipalImpl(null, 360);
		principal.setSAMLAuthnIdentifier(this.SAMLID);
		principal.addActiveDescriptor(this.DESC1);

		try
		{
			SPEPProcessor spepProcessor = new SPEPProcessorImpl(this.metadata, this.startup, this.failureRep,
					this.webServiceClient, this.idGenerator, this.samlValidator, this.keyStoreResolver);

			
			// test that the startup object is as expected
			assertEquals("SPEPProcessor did not return expected startup object", this.startup, spepProcessor.getStartup());
			
			// for block coverage reports ONLY. This method is deprecated
			assertEquals("SPEPProcessor did not return expected metadata object", this.metadata, spepProcessor.getMetadata());
			
			expect(this.metadata.resolveCacheClearService(this.DESC1)).andReturn(this.endpoints);
			expect(this.metadata.getESOEIdentifier()).andReturn("_esoeid1234").anyTimes();
			expect(this.samlValidator.getResponseValidator()).andReturn(this.respVal).anyTimes();
			expect(this.idGenerator.generateSAMLID()).andReturn("_456-456").anyTimes();
			this.respVal.validate((StatusResponseType) notNull());
			this.respVal.validate((StatusResponseType) notNull());
			expect(this.webServiceClient.authzCacheClear((String) notNull(), eq(this.endpoints.get(0)))).andReturn(generateResponse());
			expect(this.webServiceClient.authzCacheClear((String) notNull(), eq(this.endpoints.get(1)))).andReturn(generateResponse());
			expect(this.metadata.resolveKey(esoeKeyAlias)).andReturn(this.keyStoreResolver.getPublicKey()).anyTimes();

			setUpMock();
			spepProcessor.clearPrincipalSPEPCaches(principal);
			tearDownMock();
		}
		catch (MarshallerException e)
		{
			e.printStackTrace();
			fail("Unexpected MarshallerException was thrown");
		}
		catch (UnmarshallerException e)
		{
			e.printStackTrace();
			fail("Unexpected UnmarshallerException was thrown");
		}
		catch (InvalidMetadataEndpointException e)
		{
			e.printStackTrace();
			fail("Unexpected InvalidMetadataEndpointException was thrown");
		}
		catch (InvalidSAMLResponseException e)
		{
			e.printStackTrace();
			fail("Unexpected InvalidSAMLResponseException was thrown");
		}
		catch (WSClientException e)
		{
			e.printStackTrace();
			fail("Unexpected WSClientException was thrown");
		}
		catch (KeyResolutionException e)
		{
			e.printStackTrace();
			fail("Unexpected KeyResolutionException was thrown");
		}
	}
	
	/*
	 * Creates SPEPProcessor and tests for correct outcome from clearPrincipalSPEPCaches in success state with multiple
	 * SPEP
	 */
	@Test
	public void testSPEPProcessor1a()
	{
		Principal principal = new PrincipalImpl(null, 360);
		principal.setSAMLAuthnIdentifier(this.SAMLID);
		principal.addActiveDescriptor(this.DESC1);
		principal.addActiveDescriptor(this.DESC2);

		try
		{
			SPEPProcessor spepProcessor = new SPEPProcessorImpl(this.metadata, this.startup, this.failureRep,
					this.webServiceClient, this.idGenerator, this.samlValidator, this.keyStoreResolver);

			expect(this.metadata.resolveCacheClearService(this.DESC1)).andReturn(this.endpoints);
			expect(this.metadata.resolveCacheClearService(this.DESC2)).andReturn(this.endpoints);
			expect(this.metadata.getESOEIdentifier()).andReturn("_esoeid1234").anyTimes();
			expect(this.samlValidator.getResponseValidator()).andReturn(this.respVal).anyTimes();
			expect(this.idGenerator.generateSAMLID()).andReturn("_456-456").anyTimes();
			this.respVal.validate((StatusResponseType) notNull());
			this.respVal.validate((StatusResponseType) notNull());
			this.respVal.validate((StatusResponseType) notNull());
			this.respVal.validate((StatusResponseType) notNull());
			expect(this.webServiceClient.authzCacheClear((String) notNull(), eq(this.endpoints.get(0)))).andReturn(generateResponse()).times(2);
			expect(this.webServiceClient.authzCacheClear((String) notNull(), eq(this.endpoints.get(1)))).andReturn(generateResponse()).times(2);
			expect(this.metadata.resolveKey(esoeKeyAlias)).andReturn(this.keyStoreResolver.getPublicKey()).anyTimes();

			setUpMock();
			spepProcessor.clearPrincipalSPEPCaches(principal);
			tearDownMock();
		}
		catch (MarshallerException e)
		{
			e.printStackTrace();
			fail("Unexpected MarshallerException was thrown");
		}
		catch (UnmarshallerException e)
		{
			e.printStackTrace();
			fail("Unexpected UnmarshallerException was thrown");
		}
		catch (InvalidMetadataEndpointException e)
		{
			e.printStackTrace();
			fail("Unexpected InvalidMetadataEndpointException was thrown");
		}
		catch (InvalidSAMLResponseException e)
		{
			e.printStackTrace();
			fail("Unexpected InvalidSAMLResponseException was thrown");
		}
		catch (WSClientException e)
		{
			e.printStackTrace();
			fail("Unexpected WSClientException was thrown");
		}
		catch (KeyResolutionException e)
		{
			e.printStackTrace();
			fail("Unexpected KeyResolutionException was thrown");
		}
	}
	
	/*
	 * Creates SPEPProcessor and tests to ensure that failure authz update handler is correctly populated
	 */
	@Test
	public void testSPEPProcessor2()
	{
		Principal principal = new PrincipalImpl(null, 360);
		principal.setSAMLAuthnIdentifier(this.SAMLID);
		principal.addActiveDescriptor(this.DESC1);
		principal.addActiveDescriptor(this.DESC2);

		try
		{
			SPEPProcessor spepProcessor = new SPEPProcessorImpl(this.metadata, this.startup, this.failureRep,
					this.webServiceClient, this.idGenerator, this.samlValidator, this.keyStoreResolver);

			expect(this.metadata.resolveCacheClearService(this.DESC1)).andReturn(this.endpoints);
			expect(this.metadata.resolveCacheClearService(this.DESC2)).andReturn(this.endpoints);
			expect(this.metadata.getESOEIdentifier()).andReturn("_esoeid1234").anyTimes();
			expect(this.samlValidator.getResponseValidator()).andReturn(this.respVal).anyTimes();
			expect(this.idGenerator.generateSAMLID()).andReturn("_456-456").anyTimes();
			this.respVal.validate((StatusResponseType) notNull());
			this.respVal.validate((StatusResponseType) notNull());
			this.respVal.validate((StatusResponseType) notNull());
			this.respVal.validate((StatusResponseType) notNull());
			expect(this.webServiceClient.authzCacheClear((String) notNull(), eq(this.endpoints.get(0)))).andReturn(generateInvalidResponse()).times(2);
			expect(this.webServiceClient.authzCacheClear((String) notNull(), eq(this.endpoints.get(1)))).andReturn(generateInvalidResponse()).times(2);
			expect(this.metadata.resolveKey(esoeKeyAlias)).andReturn(this.keyStoreResolver.getPublicKey()).anyTimes();
			this.failureRep.add((FailedAuthzCacheUpdate)notNull());
			this.failureRep.add((FailedAuthzCacheUpdate)notNull());
			this.failureRep.add((FailedAuthzCacheUpdate)notNull());
			this.failureRep.add((FailedAuthzCacheUpdate)notNull());

			setUpMock();
			spepProcessor.clearPrincipalSPEPCaches(principal);
			tearDownMock();
		}
		catch (MarshallerException e)
		{
			e.printStackTrace();
			fail("Unexpected MarshallerException was thrown");
		}
		catch (UnmarshallerException e)
		{
			e.printStackTrace();
			fail("Unexpected UnmarshallerException was thrown");
		}
		catch (InvalidMetadataEndpointException e)
		{
			e.printStackTrace();
			fail("Unexpected InvalidMetadataEndpointException was thrown");
		}
		catch (InvalidSAMLResponseException e)
		{
			e.printStackTrace();
			fail("Unexpected InvalidSAMLResponseException was thrown");
		}
		catch (WSClientException e)
		{
			e.printStackTrace();
			fail("Unexpected WSClientException was thrown");
		}
		catch (KeyResolutionException e)
		{
			e.printStackTrace();
			fail("Unexpected KeyResolutionException was thrown");
		}
	}
	
	/*
	 * Creates SPEPProcessor and tests to ensure that invalid descriptorID's do not invoke further processing
	 */
	@Test
	public void testSPEPProcessor3()
	{
		Principal principal = new PrincipalImpl(null, 360);
		principal.setSAMLAuthnIdentifier(this.SAMLID);
		principal.addActiveDescriptor(this.DESC1);
		principal.addActiveDescriptor(this.DESC2);
		principal.addActiveDescriptor("_iaminvalid-123");

		try
		{
			SPEPProcessor spepProcessor = new SPEPProcessorImpl(this.metadata, this.startup, this.failureRep,
					this.webServiceClient, this.idGenerator, this.samlValidator, this.keyStoreResolver);

			expect(this.metadata.resolveCacheClearService(this.DESC1)).andReturn(this.endpoints);
			expect(this.metadata.resolveCacheClearService(this.DESC2)).andReturn(this.endpoints);
			expect(this.metadata.resolveCacheClearService("_iaminvalid-123")).andThrow(new InvalidMetadataEndpointException());
			expect(this.metadata.getESOEIdentifier()).andReturn("_esoeid1234").anyTimes();
			expect(this.samlValidator.getResponseValidator()).andReturn(this.respVal).anyTimes();
			expect(this.idGenerator.generateSAMLID()).andReturn("_456-456").anyTimes();
			this.respVal.validate((StatusResponseType) notNull());
			this.respVal.validate((StatusResponseType) notNull());
			this.respVal.validate((StatusResponseType) notNull());
			this.respVal.validate((StatusResponseType) notNull());
			expect(this.webServiceClient.authzCacheClear((String) notNull(), eq(this.endpoints.get(0)))).andReturn(generateInvalidResponse()).times(2);
			expect(this.webServiceClient.authzCacheClear((String) notNull(), eq(this.endpoints.get(1)))).andReturn(generateInvalidResponse()).times(2);
			expect(this.metadata.resolveKey(esoeKeyAlias)).andReturn(this.keyStoreResolver.getPublicKey()).anyTimes();
			this.failureRep.add((FailedAuthzCacheUpdate)notNull());
			this.failureRep.add((FailedAuthzCacheUpdate)notNull());
			this.failureRep.add((FailedAuthzCacheUpdate)notNull());
			this.failureRep.add((FailedAuthzCacheUpdate)notNull());

			setUpMock();
			spepProcessor.clearPrincipalSPEPCaches(principal);
			tearDownMock();
		}
		catch (MarshallerException e)
		{
			e.printStackTrace();
			fail("Unexpected MarshallerException was thrown");
		}
		catch (UnmarshallerException e)
		{
			e.printStackTrace();
			fail("Unexpected UnmarshallerException was thrown");
		}
		catch (InvalidMetadataEndpointException e)
		{
			e.printStackTrace();
			fail("Unexpected InvalidMetadataEndpointException was thrown");
		}
		catch (InvalidSAMLResponseException e)
		{
			e.printStackTrace();
			fail("Unexpected InvalidSAMLResponseException was thrown");
		}
		catch (WSClientException e)
		{
			e.printStackTrace();
			fail("Unexpected WSClientException was thrown");
		}
		catch (KeyResolutionException e)
		{
			e.printStackTrace();
			fail("Unexpected KeyResolutionException was thrown");
		}
	}	
	
	private String generateResponse() throws MarshallerException
	{
		ClearAuthzCacheResponse response = new ClearAuthzCacheResponse();
		StatusCode code = new StatusCode();
		code.setValue(StatusCodeConstants.success);
		Status status = new Status();
		status.setStatusCode(code);
		response.setStatus(status);

		// Timestamps MUST be set to UTC, no offset
		TimeZone utc = new SimpleTimeZone(0, ConfigurationConstants.timeZone);
		GregorianCalendar cal = new GregorianCalendar(utc);
		response.setIssueInstant(new XMLGregorianCalendarImpl(cal));
		response.setInResponseTo("_456-456");
		response.setSignature(new Signature());
		response.setID("_restest1234");
		response.setVersion(VersionConstants.saml20);

		return this.clearAuthzCacheResponseMarshaller.marshallSigned(response);
	}
	
	private String generateInvalidResponse() throws MarshallerException
	{
		ClearAuthzCacheResponse response = new ClearAuthzCacheResponse();
		StatusCode code = new StatusCode();
		code.setValue(StatusCodeConstants.requestDenied);
		Status status = new Status();
		status.setStatusCode(code);
		response.setStatus(status);

		// Timestamps MUST be set to UTC, no offset
		TimeZone utc = new SimpleTimeZone(0, ConfigurationConstants.timeZone);
		GregorianCalendar cal = new GregorianCalendar(utc);
		response.setIssueInstant(new XMLGregorianCalendarImpl(cal));
		response.setInResponseTo("_456-456");
		response.setSignature(new Signature());
		response.setID("_restest1234");
		response.setVersion(VersionConstants.saml20);

		return this.clearAuthzCacheResponseMarshaller.marshallSigned(response);
	}

}
