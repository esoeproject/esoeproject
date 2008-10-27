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
package com.qut.middleware.spep;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.w3._2000._09.xmldsig_.Signature;
import org.w3c.dom.Element;

import com.qut.middleware.crypto.KeystoreResolver;
import com.qut.middleware.crypto.impl.KeystoreResolverImpl;
import com.qut.middleware.metadata.bean.EntityData;
import com.qut.middleware.metadata.bean.saml.TrustedESOERole;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.saml2.SchemaConstants;
import com.qut.middleware.saml2.StatusCodeConstants;
import com.qut.middleware.saml2.VersionConstants;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.identifier.impl.IdentifierCacheImpl;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.esoe.protocol.ValidateInitializationResponse;
import com.qut.middleware.saml2.schemas.protocol.Status;
import com.qut.middleware.saml2.schemas.protocol.StatusCode;
import com.qut.middleware.saml2.validator.SAMLValidator;
import com.qut.middleware.saml2.validator.impl.SAMLValidatorImpl;
import com.qut.middleware.spep.StartupProcessor.result;
import com.qut.middleware.spep.impl.StartupProcessorImpl;
import com.qut.middleware.spep.ws.WSClient;
import com.qut.middleware.spep.ws.exception.WSClientException;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

/**
 * @author Shaun
 *
 */
@SuppressWarnings("nls")
public class StartupProcessorTest
{
	private IdentifierGenerator identifierGenerator;
	private WSClient wsClient;
	private SAMLValidator samlValidator;
	private KeystoreResolver keyStoreResolver;
	private String serverInfo;
	private StartupProcessor startupProcessor;
	private MarshallerImpl<ValidateInitializationResponse> validateInitializationResponseMarshaller;
	private String validateInitializationPackages;
	private String spepIdentifier;
	private List<String> ipAddressList;
	private MetadataProcessor metadata;
	private String spepStartupService;
	private IdentifierCacheImpl identifierCache;
	private int spepNodeID = 0;
	private String esoeIdentifier = "ESOE:TEST:ID";
	private List<Object> mocked;
	private EntityData esoeEntityData;
	private TrustedESOERole esoeRole;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		this.mocked = new ArrayList<Object>();
		
		this.spepIdentifier = "http://spep.example.com";
		this.spepStartupService = "http://esoe.example.com/spepStartup";
		
		this.identifierGenerator = createMock(IdentifierGenerator.class);
		this.mocked.add(this.identifierGenerator);
		this.wsClient = createMock(WSClient.class);
		this.mocked.add(this.wsClient);
		this.identifierCache = new IdentifierCacheImpl();
		this.samlValidator = new SAMLValidatorImpl(this.identifierCache, 180);
		this.serverInfo = "Server Info";
		
		File in = new File( "tests" + File.separator + "testdata" + File.separator + "testkeystore.ks");
		String esoeKeyAlias = "esoeprimary";
		String esoeKeyPass = "Es0EKs54P4SSPK";
		this.keyStoreResolver = new KeystoreResolverImpl(in, esoeKeyPass, esoeKeyAlias, esoeKeyPass);
		
		this.ipAddressList = new Vector<String>();
		this.ipAddressList.add("127.0.0.1");
		
		this.metadata = createMock(MetadataProcessor.class);
		this.mocked.add(this.metadata);
		this.esoeEntityData = createMock(EntityData.class);
		this.mocked.add(this.esoeEntityData);
		this.esoeRole = createMock(TrustedESOERole.class);
		this.mocked.add(this.esoeRole);
		expect(this.metadata.getEntityData(this.esoeIdentifier)).andReturn(this.esoeEntityData).anyTimes();
		expect(this.metadata.getEntityRoleData(this.esoeIdentifier, TrustedESOERole.class)).andReturn(this.esoeRole).anyTimes();
		expect(this.esoeEntityData.getRoleData(TrustedESOERole.class)).andReturn(this.esoeRole).anyTimes();
		expect(this.esoeRole.getSPEPStartupServiceEndpoint((String)notNull())).andReturn(this.spepStartupService).anyTimes();
//		expect(this.metadata.getSPEPStartupServiceEndpoint()).andReturn(this.spepStartupService);
//		expect(this.metadata.getSPEPAssertionConsumerLocation()).andReturn("assertionUrl").anyTimes();
		expect(this.metadata.resolveKey(esoeKeyAlias)).andReturn(this.keyStoreResolver.getLocalPublicKey()).anyTimes();
			
		this.startupProcessor = new StartupProcessorImpl(this.metadata, this.spepIdentifier, this.esoeIdentifier, this.identifierGenerator, this.wsClient, this.samlValidator, this.keyStoreResolver, this.ipAddressList, this.serverInfo, this.spepNodeID, 20000, false, false);
		
		String[] validateInitializationSchemas = new String[]{SchemaConstants.esoeProtocol};
		this.validateInitializationPackages = ValidateInitializationResponse.class.getPackage().getName();
		this.validateInitializationResponseMarshaller = new MarshallerImpl<ValidateInitializationResponse>(this.validateInitializationPackages, validateInitializationSchemas, this.keyStoreResolver);
	}
	
	private void startMock()
	{
		for (Object o : this.mocked) replay(o);
	}
	
	private void endMock()
	{
		for (Object o : this.mocked) verify(o);
	}

	/**
	 * Test method for {@link com.qut.middleware.spep.StartupProcessor#allowProcessing()}.
	 * @throws Exception 
	 */
	@Test
	public void testAllowProcessing1() throws Exception
	{
		String samlID = "_u598t98quw09u50293u509u2059uq89ut098u-9utq908ut98u20398u5098q2u35098qyw09t8yq098";
		this.identifierCache.registerIdentifier(samlID);
		expect(this.identifierGenerator.generateSAMLID()).andReturn(samlID);
		expect(this.wsClient.spepStartup((Element)notNull(), eq(this.spepStartupService))).andReturn(buildResponse(samlID, StatusCodeConstants.success));
//		expect(this.metadata.getESOEIdentifier()).andReturn(this.esoeID).anyTimes();
		
		startMock();
		
		this.startupProcessor.beginSPEPStartup();
		
		while (result.wait.equals(this.startupProcessor.allowProcessing()))
		{
			Thread.sleep(100);
		}
		
		assertTrue(MessageFormat.format("Expected allow, got {0}", this.startupProcessor.allowProcessing()), result.allow.equals(this.startupProcessor.allowProcessing()));
		
		endMock();
	}

	/**
	 * Test method for {@link com.qut.middleware.spep.StartupProcessor#allowProcessing()}.
	 * @throws Exception 
	 */
	@Test
	public void testAllowProcessing2() throws Exception
	{
		String samlID = "_u598t98quw09u50293u509u2059uq89ut098u-9utq908ut98u20398u5098q2u35098qyw09t8yq098";
		this.identifierCache.registerIdentifier(samlID);
		expect(this.identifierGenerator.generateSAMLID()).andReturn(samlID);
		expect(this.wsClient.spepStartup((Element)notNull(), eq(this.spepStartupService))).andReturn(buildResponse(samlID, StatusCodeConstants.requestDenied));
//		expect(this.metadata.getESOEIdentifier()).andReturn(this.esoeID).anyTimes();
		
		startMock();
		
		this.startupProcessor.beginSPEPStartup();
		
		while (result.wait.equals(this.startupProcessor.allowProcessing()))
		{
			Thread.sleep(100);
		}
		
		assertTrue(MessageFormat.format("Expected fail, got {0}", this.startupProcessor.allowProcessing()), result.fail.equals(this.startupProcessor.allowProcessing()));
		
		endMock();
	}
	
	
	/**
	 * Test method for {@link com.qut.middleware.spep.StartupProcessor#allowProcessing()}.
	 * @throws Exception 
	 *
	 * Test the case where a response is recieved from someone other than expected ESOE. Expect fail.
	 */
	@Test
	public void testAllowProcessing3() throws Exception
	{
		String samlID = "_u598t98quw09u50293u509u2059uq89ut098u-9utq908ut98u20398u5098q2u35098qyw09t8yq098";
		this.identifierCache.registerIdentifier(samlID);
		expect(this.identifierGenerator.generateSAMLID()).andReturn(samlID);
		expect(this.wsClient.spepStartup((Element)notNull(), eq(this.spepStartupService))).andReturn(buildResponse(samlID, StatusCodeConstants.requestDenied));
		
		// change is here
//		expect(this.metadata.getESOEIdentifier()).andReturn("No-ESOE").anyTimes();
		
		startMock();
		
		this.startupProcessor.beginSPEPStartup();
		
		while (result.wait.equals(this.startupProcessor.allowProcessing()))
		{
			Thread.sleep(100);
		}
		
		assertTrue(MessageFormat.format("Expected fail, got {0}", this.startupProcessor.allowProcessing()), result.fail.equals(this.startupProcessor.allowProcessing()));
		
		endMock();
	}
	
	
	/**
	 * Test method for {@link com.qut.middleware.spep.StartupProcessor#allowProcessing()}.
	 * @throws Exception 
	 *
	 * Test the case where an invalid response is recieved from ESOE. Expect fail.
	 */
	@Test
	public void testAllowProcessing4() throws Exception
	{
		String samlID = "_u598t98quw09u50293u509u2059uq89ut098u-9utq908ut98u20398u5098q2u35098qyw09t8yq098";
		this.identifierCache.registerIdentifier(samlID);
		expect(this.identifierGenerator.generateSAMLID()).andReturn(samlID);
		expect(this.wsClient.spepStartup((Element)notNull(), eq(this.spepStartupService))).andReturn(buildResponse("_4736247324", "blah"));
		
		// change is here
//		expect(this.metadata.getESOEIdentifier()).andReturn("No-ESOE").anyTimes();
		
		startMock();
		
		this.startupProcessor.beginSPEPStartup();
		
		while (result.wait.equals(this.startupProcessor.allowProcessing()))
		{
			Thread.sleep(100);
		}
		
		assertTrue(MessageFormat.format("Expected fail, got {0}", this.startupProcessor.allowProcessing()), result.fail.equals(this.startupProcessor.allowProcessing()));
		
		endMock();
	}
	
	
	/**
	 * Test method for {@link com.qut.middleware.spep.StartupProcessor#allowProcessing()}.
	 * @throws Exception 
	 *
	 * Test the case where the web service client cannot send request. Expect fail.
	 */
	@Test
	public void testAllowProcessing5() throws Exception
	{
		String samlID = "_u598t98quw09u50293u509u2059uq89ut098u-9utq908ut98u20398u5098q2u35098qyw09t8yq098";
		this.identifierCache.registerIdentifier(samlID);
		expect(this.identifierGenerator.generateSAMLID()).andReturn(samlID);
		expect(this.wsClient.spepStartup((Element)notNull(), eq(this.spepStartupService))).andThrow(new WSClientException("Unable to send ws request."));
		
		// change is here
//		expect(this.metadata.getESOEIdentifier()).andReturn("No-ESOE").anyTimes();
		
		startMock();
		
		this.startupProcessor.beginSPEPStartup();
		
		while (result.wait.equals(this.startupProcessor.allowProcessing()))
		{
			Thread.sleep(100);
		}
		
		assertTrue(MessageFormat.format("Expected fail, got {0}", this.startupProcessor.allowProcessing()), result.fail.equals(this.startupProcessor.allowProcessing()));
		
		endMock();
	}
	private Element buildResponse(String samlID, String statusCodeValue) throws MarshallerException
	{
		String issuerValue = this.esoeIdentifier;
		
		NameIDType issuer = new NameIDType();
		issuer.setValue(issuerValue);
		
		Status status = new Status();
		StatusCode statusCode = new StatusCode();
		status.setStatusCode(statusCode);
		statusCode.setValue(statusCodeValue);
		
		ValidateInitializationResponse response = new ValidateInitializationResponse();
		response.setID("_98tiajoeitj0q29jt0923059iqiw5i23i5-0i5i0q92u5098uq0uoaisjlkjqj6o6");
		response.setInResponseTo(samlID);
		response.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		response.setIssuer(issuer);
		response.setSignature(new Signature());
		response.setVersion(VersionConstants.saml20);
		response.setStatus(status);
		// ordinarily this would be set to assertion consumer service location by the ESOE but for mocked testing it'll do
		response.setDestination(this.spepStartupService);
		
		return this.validateInitializationResponseMarshaller.marshallSignedElement(response);
	}

	
	/** Test invalid params.
	 * 
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction1() throws Exception
	{
		this.startupProcessor = new StartupProcessorImpl(null, this.spepIdentifier, this.esoeIdentifier, this.identifierGenerator, this.wsClient, this.samlValidator, this.keyStoreResolver, this.ipAddressList, this.serverInfo, this.spepNodeID, 20000, false, false);
	}
	
	/** Test invalid params.
	 * 
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction2() throws Exception
	{
		this.startupProcessor = new StartupProcessorImpl(this.metadata, null,  this.esoeIdentifier, this.identifierGenerator, this.wsClient, this.samlValidator, this.keyStoreResolver, this.ipAddressList, this.serverInfo, this.spepNodeID, 20000, false, false);
	}
	
	/** Test invalid params.
	 * 
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction3() throws Exception
	{
		this.startupProcessor = new StartupProcessorImpl(this.metadata, this.spepIdentifier,  this.esoeIdentifier, null, this.wsClient, this.samlValidator, this.keyStoreResolver, this.ipAddressList, this.serverInfo, this.spepNodeID, 20000, false, false);
	}
	
	/** Test invalid params.
	 * 
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction4() throws Exception
	{
		this.startupProcessor = new StartupProcessorImpl(this.metadata, this.spepIdentifier,  this.esoeIdentifier, this.identifierGenerator, null, this.samlValidator, this.keyStoreResolver, this.ipAddressList, this.serverInfo, this.spepNodeID, 20000, false, false);
	}
	
	/** Test invalid params.
	 * 
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction5() throws Exception
	{
		this.startupProcessor = new StartupProcessorImpl(this.metadata, this.spepIdentifier,  this.esoeIdentifier, this.identifierGenerator, this.wsClient, null, this.keyStoreResolver, this.ipAddressList, this.serverInfo, this.spepNodeID, 20000, false, false);
	}
	
	/** Test invalid params.
	 * 
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction6() throws Exception
	{
		this.startupProcessor = new StartupProcessorImpl(this.metadata, this.spepIdentifier,  this.esoeIdentifier, this.identifierGenerator, this.wsClient, this.samlValidator, null, this.ipAddressList, this.serverInfo, this.spepNodeID, 20000, false, false);
	}
	
	/** Test invalid params.
	 * 
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction7() throws Exception
	{
		this.startupProcessor = new StartupProcessorImpl(this.metadata, this.spepIdentifier,  this.esoeIdentifier, this.identifierGenerator, this.wsClient, this.samlValidator, this.keyStoreResolver, null, this.serverInfo, this.spepNodeID, 20000, false, false);
	}
	
	/** Test invalid params.
	 * 
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction8() throws Exception
	{
		this.startupProcessor = new StartupProcessorImpl(this.metadata, this.spepIdentifier,  this.esoeIdentifier, this.identifierGenerator, this.wsClient, this.samlValidator, this.keyStoreResolver, this.ipAddressList, null, this.spepNodeID, 20000, false, false);
	}
	
	/** Test invalid params.
	 * 
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction9() throws Exception
	{
		this.startupProcessor = new StartupProcessorImpl(this.metadata, this.spepIdentifier,  this.esoeIdentifier, this.identifierGenerator, this.wsClient, this.samlValidator, this.keyStoreResolver, this.ipAddressList, this.serverInfo, -1, 20000, false, false);
	}
}
