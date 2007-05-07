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
package com.qut.middleware.esoe.pdp;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

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
import com.qut.middleware.esoe.pdp.cache.PolicyCacheProcessor;
import com.qut.middleware.esoe.pdp.cache.bean.AuthzPolicyCache;
import com.qut.middleware.esoe.pdp.cache.bean.impl.AuthzPolicyCacheImpl;
import com.qut.middleware.esoe.pdp.cache.impl.AuthzCacheUpdateFailureRepositoryImpl;
import com.qut.middleware.esoe.pdp.cache.impl.PolicyCacheProcessorImpl;
import com.qut.middleware.esoe.pdp.cache.sqlmap.PolicyCacheDao;
import com.qut.middleware.esoe.pdp.cache.sqlmap.impl.PolicyCacheData;
import com.qut.middleware.esoe.pdp.cache.sqlmap.impl.PolicyCacheQueryData;
import com.qut.middleware.esoe.ws.WSClient;
import com.qut.middleware.saml2.StatusCodeConstants;
import com.qut.middleware.saml2.VersionConstants;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.identifier.IdentifierCache;
import com.qut.middleware.saml2.identifier.impl.IdentifierCacheImpl;
import com.qut.middleware.saml2.identifier.impl.IdentifierGeneratorImpl;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Request;
import com.qut.middleware.saml2.schemas.esoe.protocol.ClearAuthzCacheRequest;
import com.qut.middleware.saml2.schemas.esoe.protocol.ClearAuthzCacheResponse;
import com.qut.middleware.saml2.schemas.protocol.Status;
import com.qut.middleware.saml2.schemas.protocol.StatusCode;
import com.qut.middleware.saml2.validator.SAMLValidator;
import com.qut.middleware.saml2.validator.impl.SAMLValidatorImpl;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

/**
 * @author zedly
 *
 */
@SuppressWarnings("nls")
public class PolicyCacheProcessorTest
{
	private PolicyCacheProcessorImpl testCacheProcessor;
	private AuthzCacheUpdateFailureRepository failureRep;
	private AuthzPolicyCache testCache;
	private Metadata metadata;
	private Map<Integer,String> endpoints;
	private PolicyCacheDao policyCacheDao;
	private WSClient webServiceClient;
	private String validSpep = "_123456-validspep";
	private String invalidSpep = "_123456-invalidspep";
	private KeyStoreResolver keyStoreResolver;
	private Marshaller<ClearAuthzCacheResponse> clearAuthzCacheResponseMarshaller;
	private String clearAuthzCachePackages;
	private IdentifierGeneratorImpl idGenerator;
	private IdentifierCache identifierCache;
	private SAMLValidator validator;
	
	/**
	 * @throws java.lang.Exception
	 */
	@SuppressWarnings({ "unqualified-field-access", "nls" })
	@Before
	public void setUp() throws Exception
	{
		this.testCache = new AuthzPolicyCacheImpl();
		this.failureRep = new AuthzCacheUpdateFailureRepositoryImpl();
		this.metadata = createMock(Metadata.class);
			
		this.endpoints = new HashMap<Integer,String>();
		this.endpoints.put(0, "www.blah.com");
		
		int skew = (Integer.MAX_VALUE / 1000 -1);
		
		String keyStorePath = System.getProperty("user.dir") + File.separator + "secure" + File.separator + "esoekeystore.ks";
		String keyStorePassword = "Es0EKs54P4SSPK";
		String esoeKeyAlias = "esoeprimary";
		String esoeKeyPassword = "Es0EKs54P4SSPK";
	
		this.keyStoreResolver = new KeyStoreResolverImpl(new File(keyStorePath), keyStorePassword, esoeKeyAlias, esoeKeyPassword);
				
		String[] clearAuthzCacheSchemas = new String[]{ConfigurationConstants.esoeProtocol, ConfigurationConstants.samlAssertion, ConfigurationConstants.samlProtocol};
		this.clearAuthzCacheResponseMarshaller = new MarshallerImpl<ClearAuthzCacheResponse>(ClearAuthzCacheRequest.class.getPackage().getName() + ":" + Request.class.getPackage().getName(), clearAuthzCacheSchemas, keyStoreResolver.getKeyAlias(), keyStoreResolver.getPrivateKey());
		
		Map<String, PolicyCacheData> testMap = new HashMap<String, PolicyCacheData>();
		PolicyCacheData testPolicy = new PolicyCacheData();
		testPolicy.setLxacmlPolicy(this.getTestPolicy());
		testPolicy.setDescriptorID(this.validSpep);
		testMap.put(validSpep, testPolicy);
	
		PolicyCacheQueryData policyCacheQuery = new PolicyCacheQueryData();
		policyCacheQuery.setDescriptorID(this.validSpep);
		policyCacheQuery.setDateLastUpdated(new Date(System.currentTimeMillis()));
		
		this.validator = new SAMLValidatorImpl(new IdentifierCacheImpl(), skew);
		this.identifierCache = new IdentifierCacheImpl();
		this.idGenerator = new IdentifierGeneratorImpl(this.identifierCache);
		
		webServiceClient = createMock(WSClient.class);
		expect(this.webServiceClient.authzCacheClear( (String)anyObject(),(String)notNull() ) ).andReturn(this.getTestAuthzResponse()).anyTimes();
		replay(this.webServiceClient);
		
		policyCacheDao = createMock(PolicyCacheDao.class);	
		expect(this.policyCacheDao.queryDateLastUpdated()).andReturn(new Date(System.currentTimeMillis()  + 10000)).anyTimes();
		expect(this.policyCacheDao.queryPolicyCache(policyCacheQuery)).andReturn(testMap).anyTimes();
		expect(this.policyCacheDao.queryPolicyCache((PolicyCacheQueryData)notNull())).andReturn(testMap).anyTimes();
		replay(this.policyCacheDao);
			
		// setup invalid SPEP to resolve
		expect(this.metadata.resolveCacheClearService(this.invalidSpep)).andThrow(new InvalidMetadataEndpointException()).anyTimes();
		expect(this.metadata.resolveCacheClearService(this.validSpep)).andReturn(this.endpoints).atLeastOnce();
		expect(this.metadata.getESOEIdentifier()).andReturn(esoeKeyAlias).anyTimes();
		expect(this.metadata.resolveKey(esoeKeyAlias)).andReturn(this.keyStoreResolver.getPublicKey()).anyTimes();
		replay(this.metadata);
				
		this.testCacheProcessor = new PolicyCacheProcessorImpl(failureRep, testCache, metadata, 
				policyCacheDao, webServiceClient, keyStoreResolver, idGenerator, validator, 5);
	}

	@After
	public void tearDown()
	{
		if(this.testCacheProcessor.isAlive())
			this.testCacheProcessor.shutdown();
	}
	
	/**
	 * Test method for {@link com.qut.middleware.esoe.pdp.cache.impl.PolicyCacheProcessorImpl#PolicyCacheProcessorImpl(com.qut.middleware.esoe.pdp.cache.CacheUpdateFailureMonitor)}.
	 */
	@Test
	public final void testPolicyCacheProcessorImpl()
	{
		assertTrue(this.testCacheProcessor.isAlive());
	}
	
	
	/**
	 * Test method for {@link com.qut.middleware.esoe.pdp.cache.impl.PolicyCacheProcessorImpl#spepStartingNotification(java.lang.String, int)}.
	 *
	 * NOTE: This test is not completely doable because it is not possible to get the SAML ID from the request
	 * generated by the policy cache processor. This ID is needed to generate an authzClearCacheResponse,
	 * which is validated by the policy cache processor. Thus: The policy processor will never return a Success
	 * result because a valid authzCacheClearResponse can not be generated.
	 * 
	 * Test an authz cache clear request to an spep which exists.
	 */
	@Test
	public final void testSpepStartingNotification1() throws InterruptedException
	{
		Thread.sleep(1000);
		
		PolicyCacheProcessor.result result;
		
		result = this.testCacheProcessor.spepStartingNotification(this.validSpep, 0);
		
		// assert valid SPEP endpoint notification succeeds
		
		// ordinarily we would expect a success response from this, but see above for reason why not.
		//assertEquals("Test return code for SPEP startup notification. " , PolicyCacheProcessor.result.Success,   this.testCacheProcessor.spepStartingNotification(this.validSpep, 0));
		assertEquals("Test return code for SPEP startup notification. " , PolicyCacheProcessor.result.Failure,  result);

	}
	
	
	/**
	 * Test method for {@link com.qut.middleware.esoe.pdp.cache.impl.PolicyCacheProcessorImpl#spepStartingNotification(java.lang.String, int)}.
	 *
	 * NOTE: This test is not completely doable because it is not possible to get the SAML ID from the request
	 * generated by the policy cache processor. This ID is needed to generate an authzClearCacheResponse,
	 * which is validated by the policy cache processor. Thus: The policy processor will never return a Success
	 * result because a valid authzCacheClearResponse can not be generated.
	 * 
	 * 
	 */
	@Test
	public final void testSpepStartingNotification2() throws InterruptedException
	{
		Thread.sleep(1000);
		
		PolicyCacheProcessor.result result;
		
		result = this.testCacheProcessor.spepStartingNotification(this.invalidSpep, 0);
		
		// assert valid SPEP endpoint notification succeeds
		assertEquals("Test return code for SPEP startup notification. " , PolicyCacheProcessor.result.Failure,  result);

	}
	
	/** Test that the thread is running and polling correctly.
	 * 
	 * Test method for {@link com.qut.middleware.esoe.pdp.cache.impl.PolicyCacheProcessorImpl#run()}.
	 */
	@Test
	public final void testRun()
	{
		assertTrue(this.testCacheProcessor.isAlive()); 	
		
	}

	
	private String getTestPolicy()
	{
		String path = System.getProperty("user.dir") + File.separator +"tests" + File.separator+ "testdata"+  File.separator  ;
		
			// map of policy sets and associated test SPEP ID's 
		String file = new String(path + "PolicySetComplexity1.xml");
		StringBuffer xmlBuff = new StringBuffer();
			
		try
		{
			InputStream fileStream = new FileInputStream(file);
			Reader reader = new InputStreamReader(fileStream, "UTF-16");
			BufferedReader in = new BufferedReader(reader);
			
			String str;
			while ((str = in.readLine()) != null)
			{
			   	xmlBuff.append(str);
			}
				    
			in.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return xmlBuff.toString();
	}
	
	
	private String getTestAuthzResponse()
	{
		String responseDocument = null;
		ClearAuthzCacheResponse clearAuthzCacheResponse = null;

		NameIDType issuer = new NameIDType();
		issuer.setValue(this.validSpep);
		
		Status status = new Status();
		StatusCode statusCode = new StatusCode();
		statusCode.setValue(StatusCodeConstants.success);
		status.setStatusCode(statusCode);
		status.setStatusMessage("TEST message");
		
		String id = this.idGenerator.generateSAMLID();
		clearAuthzCacheResponse = new ClearAuthzCacheResponse();
		clearAuthzCacheResponse.setInResponseTo(id);
		clearAuthzCacheResponse.setID(id);
		clearAuthzCacheResponse.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		clearAuthzCacheResponse.setIssuer(issuer);
		clearAuthzCacheResponse.setSignature(new Signature());
		clearAuthzCacheResponse.setVersion(VersionConstants.saml20);
		clearAuthzCacheResponse.setStatus(status);
		
		try
		{
			responseDocument = this.clearAuthzCacheResponseMarshaller.marshallSigned(clearAuthzCacheResponse);
		}
		catch(MarshallerException e)
		{
			fail("Unable to marshall test authz cahe response.");
		}
		
		return responseDocument;
	}
	
	@Test
	public final void testShutdown() throws Exception
	{
		Thread.sleep(10000);
		
		this.testCacheProcessor.shutdown();
		
		Thread.sleep(10000);
		
		assertTrue(!this.testCacheProcessor.isAlive());
	}
	
	
	/** Test invalid parameters in constructor
	 *
	 */ 
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction1() throws Exception
	{
		this.testCacheProcessor = new PolicyCacheProcessorImpl(null, testCache, metadata, 
				policyCacheDao, webServiceClient, keyStoreResolver, idGenerator, validator, 5);
	}
	
	/** Test invalid parameters in constructor
	 *
	 */ 
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction2() throws Exception
	{
		this.testCacheProcessor = new PolicyCacheProcessorImpl(failureRep, null, metadata, 
				policyCacheDao, webServiceClient, keyStoreResolver, idGenerator, validator, 5);
	}
	
	/** Test invalid parameters in constructor
	 *
	 */ 
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction3() throws Exception
	{
		this.testCacheProcessor = new PolicyCacheProcessorImpl(failureRep, testCache, null, 
				policyCacheDao, webServiceClient, keyStoreResolver, idGenerator, validator, 5);
	}
	
	/** Test invalid parameters in constructor
	 *
	 */ 
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction4() throws Exception
	{
		this.testCacheProcessor = new PolicyCacheProcessorImpl(failureRep, testCache, metadata, 
				null, webServiceClient, keyStoreResolver, idGenerator, validator, 5);
	}
	
	/** Test invalid parameters in constructor
	 *
	 */ 
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction5() throws Exception
	{
		this.testCacheProcessor = new PolicyCacheProcessorImpl(failureRep, testCache, metadata, 
				policyCacheDao, null, keyStoreResolver, idGenerator, validator, 5);
	}
	
	/** Test invalid parameters in constructor
	 *
	 */ 
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction6() throws Exception
	{
		this.testCacheProcessor = new PolicyCacheProcessorImpl(failureRep, testCache, metadata, 
				policyCacheDao, webServiceClient, null, idGenerator, validator, 5);
	}
	
	/** Test invalid parameters in constructor
	 *
	 */ 
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction7() throws Exception
	{
		this.testCacheProcessor = new PolicyCacheProcessorImpl(failureRep, testCache, metadata, 
				policyCacheDao, webServiceClient, keyStoreResolver, null, validator, 5);
	}
	
	/** Test invalid parameters in constructor
	 *
	 */ 
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction8() throws Exception
	{
		this.testCacheProcessor = new PolicyCacheProcessorImpl(failureRep, testCache, metadata, 
				policyCacheDao, webServiceClient, keyStoreResolver, idGenerator, null, 5);
	}
	
	/** Test invalid parameters in constructor
	 *
	 */ 
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction9() throws Exception
	{
		this.testCacheProcessor = new PolicyCacheProcessorImpl(failureRep, testCache, metadata, 
				policyCacheDao, webServiceClient, keyStoreResolver, idGenerator, validator, -5);
	}
}
