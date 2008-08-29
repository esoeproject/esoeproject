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
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.or;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

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

import com.qut.middleware.crypto.KeystoreResolver;
import com.qut.middleware.crypto.impl.KeystoreResolverImpl;
import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.authz.cache.AuthzCacheUpdateFailureRepository;
import com.qut.middleware.esoe.authz.cache.bean.FailedAuthzCacheUpdate;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.impl.PrincipalImpl;
import com.qut.middleware.esoe.spep.impl.SPEPProcessorImpl;
import com.qut.middleware.esoe.ws.WSClient;
import com.qut.middleware.metadata.bean.EntityData;
import com.qut.middleware.metadata.bean.saml.SPEPRole;
import com.qut.middleware.metadata.bean.saml.endpoint.IndexedEndpoint;
import com.qut.middleware.metadata.bean.saml.endpoint.impl.IndexedEndpointImpl;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.saml2.BindingConstants;
import com.qut.middleware.saml2.SchemaConstants;
import com.qut.middleware.saml2.StatusCodeConstants;
import com.qut.middleware.saml2.VersionConstants;
import com.qut.middleware.saml2.exception.MarshallerException;
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
	private MetadataProcessor metadata;
	private WSClient webServiceClient;
	private KeystoreResolver keyStoreResolver;
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
	private final String esoeIdentifier = "_esoeid1234";
	public Map<Integer,String> endpoints;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		this.failureRep = createMock(AuthzCacheUpdateFailureRepository.class);
		this.metadata = createMock(MetadataProcessor.class);
		this.startup = createMock(Startup.class);
		this.samlValidator = createMock(SAMLValidator.class);
		this.respVal = createMock(SAMLResponseValidator.class);
		this.webServiceClient = createMock(WSClient.class);
		this.idGenerator = createMock(IdentifierGenerator.class);

		this.endpoints = Collections.synchronizedMap(new HashMap<Integer,String>());
		String endpointLocation1 = "http://spep.qut.edu.au/ws/Service";
		this.endpoints.put(0,endpointLocation1);
		String endpointLocation2 = "http://spep2.qut.edu.au/ws/Service";
		this.endpoints.put(1,endpointLocation2);

		String keyStorePath = "tests" + File.separator + "testdata" + File.separator
				+ "testskeystore.ks";
		String keyStorePassword = "Es0EKs54P4SSPK";
		String esoeKeyAlias = "esoeprimary";
		String esoeKeyPassword = "Es0EKs54P4SSPK";

		this.keyStoreResolver = new KeystoreResolverImpl(new File(keyStorePath), keyStorePassword, esoeKeyAlias,
				esoeKeyPassword);

		String[] clearAuthzCacheSchemas = new String[] { SchemaConstants.esoeProtocol,
				SchemaConstants.samlAssertion, SchemaConstants.samlProtocol };
		this.clearAuthzCacheResponseMarshaller = new MarshallerImpl<ClearAuthzCacheResponse>(
				ClearAuthzCacheRequest.class.getPackage().getName() + ":" + Request.class.getPackage().getName(),
				clearAuthzCacheSchemas, keyStoreResolver);

		EntityData entityData = createMock(EntityData.class);
		SPEPRole spepRole = createMock(SPEPRole.class);
		expect(entityData.getRoleData(SPEPRole.class)).andReturn(spepRole).anyTimes();
		expect(spepRole.getCacheClearServiceEndpoint(or(eq(this.DESC1),eq(this.DESC2)), eq(0))).andReturn(endpointLocation1).anyTimes();
		expect(spepRole.getCacheClearServiceEndpoint(or(eq(this.DESC1),eq(this.DESC2)), eq(1))).andReturn(endpointLocation2).anyTimes();
		
		List<IndexedEndpoint> indexedEndpointList = new ArrayList<IndexedEndpoint>();
		indexedEndpointList.add(new IndexedEndpointImpl(BindingConstants.soap, endpointLocation1, 0));
		indexedEndpointList.add(new IndexedEndpointImpl(BindingConstants.soap, endpointLocation2, 1));
		expect(spepRole.getCacheClearServiceEndpointList()).andReturn(indexedEndpointList).anyTimes();
		
		replay(entityData);
		replay(spepRole);
		
		expect(metadata.getEntityData((String)notNull())).andReturn(entityData).anyTimes();
		expect(metadata.getEntityRoleData((String)notNull(), eq(SPEPRole.class))).andReturn(spepRole).anyTimes();
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
	public void testSPEPProcessor1() throws Exception
	{
		Principal principal = new PrincipalImpl(null, 360);
		principal.setSAMLAuthnIdentifier(this.SAMLID);
		principal.addActiveDescriptor(this.DESC1);

		SPEPProcessor spepProcessor = new SPEPProcessorImpl(this.metadata, this.startup, this.failureRep,
				this.webServiceClient, this.idGenerator, this.samlValidator, this.keyStoreResolver);

		
		// test that the startup object is as expected
		assertEquals("SPEPProcessor did not return expected startup object", this.startup, spepProcessor.getStartup());
		
		//expect(this.metadata.resolveCacheClearService(this.DESC1)).andReturn(this.endpoints);
		expect(this.samlValidator.getResponseValidator()).andReturn(this.respVal).anyTimes();
		expect(this.idGenerator.generateSAMLID()).andReturn("_456-456").anyTimes();
		this.respVal.validate((StatusResponseType) notNull());
		this.respVal.validate((StatusResponseType) notNull());
		expect(this.webServiceClient.authzCacheClear((byte[]) notNull(), eq(this.endpoints.get(0)))).andReturn(generateResponse());
		expect(this.webServiceClient.authzCacheClear((byte[]) notNull(), eq(this.endpoints.get(1)))).andReturn(generateResponse());
		expect(this.metadata.resolveKey(esoeKeyAlias)).andReturn(this.keyStoreResolver.getLocalPublicKey()).anyTimes();

		setUpMock();
		spepProcessor.clearPrincipalSPEPCaches(principal);
		tearDownMock();
	}
	
	/*
	 * Creates SPEPProcessor and tests for correct outcome from clearPrincipalSPEPCaches in success state with multiple
	 * SPEP
	 */
	@Test
	public void testSPEPProcessor1a() throws Exception
	{
		Principal principal = new PrincipalImpl(null, 360);
		principal.setSAMLAuthnIdentifier(this.SAMLID);
		principal.addActiveDescriptor(this.DESC1);
		principal.addActiveDescriptor(this.DESC2);

		SPEPProcessor spepProcessor = new SPEPProcessorImpl(this.metadata, this.startup, this.failureRep,
				this.webServiceClient, this.idGenerator, this.samlValidator, this.keyStoreResolver);

//		expect(this.metadata.resolveCacheClearService(this.DESC1)).andReturn(this.endpoints);
//		expect(this.metadata.resolveCacheClearService(this.DESC2)).andReturn(this.endpoints);
//		expect(this.metadata.getEsoeEntityID()).andReturn("_esoeid1234").anyTimes();
		expect(this.samlValidator.getResponseValidator()).andReturn(this.respVal).anyTimes();
		expect(this.idGenerator.generateSAMLID()).andReturn("_456-456").anyTimes();
		this.respVal.validate((StatusResponseType) notNull());
		this.respVal.validate((StatusResponseType) notNull());
		this.respVal.validate((StatusResponseType) notNull());
		this.respVal.validate((StatusResponseType) notNull());
		expect(this.webServiceClient.authzCacheClear((byte[]) notNull(), eq(this.endpoints.get(0)))).andReturn(generateResponse()).times(2);
		expect(this.webServiceClient.authzCacheClear((byte[]) notNull(), eq(this.endpoints.get(1)))).andReturn(generateResponse()).times(2);
		expect(this.metadata.resolveKey(esoeKeyAlias)).andReturn(this.keyStoreResolver.getLocalPublicKey()).anyTimes();

		setUpMock();
		spepProcessor.clearPrincipalSPEPCaches(principal);
		tearDownMock();
	}
	
	/*
	 * Creates SPEPProcessor and tests to ensure that failure authz update handler is correctly populated
	 */
	@Test
	public void testSPEPProcessor2() throws Exception
	{
		Principal principal = new PrincipalImpl(null, 360);
		principal.setSAMLAuthnIdentifier(this.SAMLID);
		principal.addActiveDescriptor(this.DESC1);
		principal.addActiveDescriptor(this.DESC2);

		SPEPProcessor spepProcessor = new SPEPProcessorImpl(this.metadata, this.startup, this.failureRep,
				this.webServiceClient, this.idGenerator, this.samlValidator, this.keyStoreResolver);

//		expect(this.metadata.resolveCacheClearService(this.DESC1)).andReturn(this.endpoints);
//		expect(this.metadata.resolveCacheClearService(this.DESC2)).andReturn(this.endpoints);
//		expect(this.metadata.getEsoeEntityID()).andReturn("_esoeid1234").anyTimes();
		expect(this.samlValidator.getResponseValidator()).andReturn(this.respVal).anyTimes();
		expect(this.idGenerator.generateSAMLID()).andReturn("_456-456").anyTimes();
		this.respVal.validate((StatusResponseType) notNull());
		this.respVal.validate((StatusResponseType) notNull());
		this.respVal.validate((StatusResponseType) notNull());
		this.respVal.validate((StatusResponseType) notNull());
		expect(this.webServiceClient.authzCacheClear((byte[]) notNull(), eq(this.endpoints.get(0)))).andReturn(generateInvalidResponse()).times(2);
		expect(this.webServiceClient.authzCacheClear((byte[]) notNull(), eq(this.endpoints.get(1)))).andReturn(generateInvalidResponse()).times(2);
		expect(this.metadata.resolveKey(esoeKeyAlias)).andReturn(this.keyStoreResolver.getLocalPublicKey()).anyTimes();
		this.failureRep.add((FailedAuthzCacheUpdate)notNull());
		this.failureRep.add((FailedAuthzCacheUpdate)notNull());
		this.failureRep.add((FailedAuthzCacheUpdate)notNull());
		this.failureRep.add((FailedAuthzCacheUpdate)notNull());

		setUpMock();
		spepProcessor.clearPrincipalSPEPCaches(principal);
		tearDownMock();
	}
	
	/*
	 * Creates SPEPProcessor and tests to ensure that invalid descriptorID's do not invoke further processing
	 */
	@Test
	public void testSPEPProcessor3() throws Exception
	{
		Principal principal = new PrincipalImpl(null, 360);
		principal.setSAMLAuthnIdentifier(this.SAMLID);
		principal.addActiveDescriptor(this.DESC1);
		principal.addActiveDescriptor(this.DESC2);
		principal.addActiveDescriptor("_iaminvalid-123");

		SPEPProcessor spepProcessor = new SPEPProcessorImpl(this.metadata, this.startup, this.failureRep,
				this.webServiceClient, this.idGenerator, this.samlValidator, this.keyStoreResolver);
//
//		expect(this.metadata.resolveCacheClearService(this.DESC1)).andReturn(this.endpoints);
//		expect(this.metadata.resolveCacheClearService(this.DESC2)).andReturn(this.endpoints);
//		expect(this.metadata.resolveCacheClearService("_iaminvalid-123")).andThrow(new InvalidMetadataEndpointException());
//		expect(this.metadata.getEsoeEntityID()).andReturn("_esoeid1234").anyTimes();
		expect(this.samlValidator.getResponseValidator()).andReturn(this.respVal).anyTimes();
		expect(this.idGenerator.generateSAMLID()).andReturn("_456-456").anyTimes();
		this.respVal.validate((StatusResponseType) notNull());
		expectLastCall().atLeastOnce();
		expect(this.webServiceClient.authzCacheClear((byte[]) notNull(), eq(this.endpoints.get(0)))).andReturn(generateInvalidResponse()).atLeastOnce();
		expect(this.webServiceClient.authzCacheClear((byte[]) notNull(), eq(this.endpoints.get(1)))).andReturn(generateInvalidResponse()).atLeastOnce();
		expect(this.metadata.resolveKey(esoeKeyAlias)).andReturn(this.keyStoreResolver.getLocalPublicKey()).anyTimes();
		this.failureRep.add((FailedAuthzCacheUpdate)notNull());
		expectLastCall().atLeastOnce();

		setUpMock();
		spepProcessor.clearPrincipalSPEPCaches(principal);
		tearDownMock();
	}	
	
	private byte[] generateResponse() throws MarshallerException
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
	
	private byte[] generateInvalidResponse() throws MarshallerException
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
