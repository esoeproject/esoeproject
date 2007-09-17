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
package com.qut.middleware.esoe.spep;

import static com.qut.middleware.test.Capture.capture;
import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.w3._2000._09.xmldsig_.Signature;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.crypto.impl.KeyStoreResolverImpl;
import com.qut.middleware.esoe.metadata.Metadata;
import com.qut.middleware.esoe.pdp.cache.PolicyCacheProcessor;
import com.qut.middleware.esoe.pdp.cache.PolicyCacheProcessor.result;
import com.qut.middleware.esoe.spep.bean.SPEPProcessorData;
import com.qut.middleware.esoe.spep.bean.impl.SPEPProcessorDataImpl;
import com.qut.middleware.esoe.spep.exception.DatabaseFailureException;
import com.qut.middleware.esoe.spep.exception.DatabaseFailureNoSuchSPEPException;
import com.qut.middleware.esoe.spep.exception.InvalidRequestException;
import com.qut.middleware.esoe.spep.exception.SPEPCacheUpdateException;
import com.qut.middleware.esoe.spep.impl.StartupImpl;
import com.qut.middleware.saml2.VersionConstants;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.identifier.IdentifierCache;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.identifier.impl.IdentifierCacheImpl;
import com.qut.middleware.saml2.identifier.impl.IdentifierGeneratorImpl;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.esoe.protocol.ValidateInitializationRequest;
import com.qut.middleware.saml2.validator.SAMLValidator;
import com.qut.middleware.saml2.validator.impl.SAMLValidatorImpl;
import com.qut.middleware.test.Capture;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

/** */
@SuppressWarnings({"nls","boxing"})
public class StartupTest
{

	private Startup startup;
	private Marshaller<ValidateInitializationRequest> requestMarshaller;
	private Capture<ValidateInitializationRequest> captureObject;
	private SPEPRegistrationCache spepRegistrationCache;
	private Metadata metadata;
	private String keyName;
	private PrivateKey key;
	private KeyStoreResolverImpl keyStoreResolver;
	private PolicyCacheProcessor policyCacheProcessor;
	private String esoeID = "_847329uhfde789fwy94";
	private IdentifierGenerator identifierGenerator;
	private SAMLValidator samlValidator;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		int skew = 180;
		IdentifierCache identifierCache = new IdentifierCacheImpl();
		this.identifierGenerator = new IdentifierGeneratorImpl(new IdentifierCacheImpl());
		this.samlValidator = new SAMLValidatorImpl(identifierCache, skew);
		this.spepRegistrationCache = createMock(SPEPRegistrationCache.class);
		this.captureObject = new Capture<ValidateInitializationRequest>();
		this.spepRegistrationCache.registerSPEP(capture(this.captureObject));
		this.metadata = createMock(Metadata.class);
		this.policyCacheProcessor = createMock(PolicyCacheProcessor.class);
		
		String keyStorePath = "tests/testdata/testskeystore.ks";
		String keyStorePassword = "Es0EKs54P4SSPK";
		String esoeKeyAlias = "esoeprimary";
		String esoeKeyPassword = "Es0EKs54P4SSPK";
		
		this.keyStoreResolver = new KeyStoreResolverImpl(new File(keyStorePath), keyStorePassword, esoeKeyAlias, esoeKeyPassword);
		
		PublicKey publicKey = this.keyStoreResolver.getPublicKey();
		
		expect(this.metadata.resolveKey("esoeprimary")).andReturn(publicKey);
		expect(this.metadata.getEsoeEntityID()).andReturn(this.esoeID).anyTimes();
		
		this.keyName = "esoeprimary";
		this.key = this.keyStoreResolver.getPrivateKey();
		
		expect(this.policyCacheProcessor.spepStartingNotification((String)notNull(), anyInt())).andReturn(result.Success).anyTimes();
		
		this.startup = new StartupImpl(samlValidator, identifierGenerator, this.spepRegistrationCache, this.metadata, this.keyStoreResolver, this.policyCacheProcessor);
		
		String[] schemas = new String[]{ConfigurationConstants.esoeProtocol};
		this.requestMarshaller = new MarshallerImpl<ValidateInitializationRequest>(ValidateInitializationRequest.class.getPackage().getName(), schemas, this.keyName, this.key);
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.spep.Startup#registerSPEPStartup(com.qut.middleware.esoe.spep.bean.SPEPProcessorData)}.
	 */
	@Test
	public final void testRegisterSPEPStartup()
	{
		ValidateInitializationRequest request = new ValidateInitializationRequest();
		int authzCacheIndex = 0;
		String date = "date";
		String system = "system";
		String environment = "environment";
		String version = "version";
		String issuerNameID = "nameID";
		String ipAddress = "ipaddr";

		request.setID("spep.test.url");
		request.getIpAddress().add(ipAddress);
		request.setAuthzCacheIndex(authzCacheIndex);
		request.setCompileDate(date);
		request.setCompileSystem(system);
		request.setVersion(version);
		request.setEnvironment(environment);
		request.setSwVersion(version);
		request.setVersion(VersionConstants.saml20);
		NameIDType issuer = new NameIDType();
		issuer.setValue(issuerNameID);
		request.setIssuer(issuer);
		request.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		request.setSignature(new Signature());
		request.setNodeId("nodeID:74834");
		
		byte[] requestDocument = null;
		try
		{
			requestDocument = this.requestMarshaller.marshallSigned(request);
		}
		catch (MarshallerException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Marshaller failed");
		}
		
		////System.out.println(new PrettyXml("    ").makePretty(requestDocument));
		
		replay(this.spepRegistrationCache);
		replay(this.metadata);
		replay(this.policyCacheProcessor);
		
		SPEPProcessorData data = new SPEPProcessorDataImpl();
		data.setRequestDocument(requestDocument);
		
		try
		{
			this.startup.registerSPEPStartup(data);
		}
		catch (InvalidRequestException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Invalid request, apparently");
		}
		catch (DatabaseFailureNoSuchSPEPException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Database failure. No such SPEP.");
		}
		catch (SPEPCacheUpdateException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Cache update exception");
		}
		catch (DatabaseFailureException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Database failure.");
		}
		
		verify(this.spepRegistrationCache);
		verify(this.metadata);
		verify(this.policyCacheProcessor);
		
		List<ValidateInitializationRequest> captured = this.captureObject.getCaptured();
		assertEquals("Incorrect number of objects captured", 1, captured.size());
		
		ValidateInitializationRequest capturedObject = captured.get(0);
		assertEquals("IP Address didn't come back the same", ipAddress, capturedObject.getIpAddress().get(0));
		assertEquals("Compile date didn't come back the same", date, capturedObject.getCompileDate());
		assertEquals("Compile system didn't come back the same", system, capturedObject.getCompileSystem());
		assertEquals("Authz cache index didn't come back the same", authzCacheIndex, capturedObject.getAuthzCacheIndex());
	}
	
	
		
	/** Test construction params.
	 * 
	 *
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction1() throws Exception
	{
		this.startup = new StartupImpl(null, this.identifierGenerator, this.spepRegistrationCache, this.metadata, this.keyStoreResolver, this.policyCacheProcessor);
		
	}
	
	/** Test construction params.
	 * 
	 *
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction2() throws Exception
	{
		this.startup = new StartupImpl(this.samlValidator, null, this.spepRegistrationCache, this.metadata, this.keyStoreResolver, this.policyCacheProcessor);
		
	}
	
	/** Test construction params.
	 * 
	 *
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction3() throws Exception
	{
		this.startup = new StartupImpl(this.samlValidator, this.identifierGenerator, null, this.metadata, this.keyStoreResolver, this.policyCacheProcessor);
		
	}
	
	/** Test construction params.
	 * 
	 *
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction4() throws Exception
	{
		this.startup = new StartupImpl(this.samlValidator, this.identifierGenerator, this.spepRegistrationCache, null, this.keyStoreResolver, this.policyCacheProcessor);
		
	}
	
	/** Test construction params.
	 * 
	 *
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction5() throws Exception
	{
		this.startup = new StartupImpl(this.samlValidator, this.identifierGenerator, this.spepRegistrationCache, this.metadata, null, this.policyCacheProcessor);
		
	}
	
	/** Test construction params.
	 * 
	 *
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction6() throws Exception
	{
		this.startup = new StartupImpl(this.samlValidator, this.identifierGenerator, this.spepRegistrationCache, this.metadata, this.keyStoreResolver, null);
		
	}
}
