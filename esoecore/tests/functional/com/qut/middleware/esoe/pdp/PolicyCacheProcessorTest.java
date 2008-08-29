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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3._2000._09.xmldsig_.Signature;

import com.qut.middleware.crypto.KeystoreResolver;
import com.qut.middleware.crypto.impl.KeystoreResolverImpl;
import com.qut.middleware.esoe.authz.cache.AuthzCacheUpdateFailureRepository;
import com.qut.middleware.esoe.authz.cache.PolicyCacheProcessor;
import com.qut.middleware.esoe.authz.cache.impl.AuthzCacheUpdateFailureRepositoryImpl;
import com.qut.middleware.esoe.authz.cache.impl.PolicyCacheProcessorImpl;
import com.qut.middleware.esoe.authz.cache.sqlmap.PolicyCacheDao;
import com.qut.middleware.esoe.authz.cache.sqlmap.impl.PolicyCacheData;
import com.qut.middleware.esoe.authz.cache.sqlmap.impl.PolicyCacheQueryData;
import com.qut.middleware.esoe.pdp.cache.AuthzPolicyCache;
import com.qut.middleware.esoe.pdp.cache.impl.AuthzPolicyCacheImpl;
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
import com.qut.middleware.saml2.identifier.impl.IdentifierCacheImpl;
import com.qut.middleware.saml2.identifier.impl.IdentifierGeneratorImpl;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Policy;
import com.qut.middleware.saml2.schemas.esoe.lxacml.context.Request;
import com.qut.middleware.saml2.schemas.esoe.protocol.ClearAuthzCacheRequest;
import com.qut.middleware.saml2.schemas.esoe.protocol.ClearAuthzCacheResponse;
import com.qut.middleware.saml2.schemas.protocol.Status;
import com.qut.middleware.saml2.schemas.protocol.StatusCode;
import com.qut.middleware.saml2.validator.SAMLValidator;
import com.qut.middleware.saml2.validator.impl.SAMLValidatorImpl;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

@SuppressWarnings("nls")
public class PolicyCacheProcessorTest
{
	private PolicyCacheProcessorImpl testCacheProcessor;
	private AuthzCacheUpdateFailureRepository failureRep;
	private AuthzPolicyCache testCache;
	private MetadataProcessor metadata;
	private Map<Integer,String> endpoints;
	private PolicyCacheDao policyCacheDao;
	private WSClient webServiceClient;
	private String validSpep = "_123456-validspep";
	private String validSpep2 = "urn:spep:entity:2";
	private String invalidSpep = "_123456-invalidspep";
	private KeystoreResolver keyStoreResolver;
	private Marshaller<ClearAuthzCacheResponse> clearAuthzCacheResponseMarshaller;
	private Marshaller<Policy> policyMarshaller;
	private IdentifierGeneratorImpl idGenerator;
	private IdentifierCache identifierCache;
	private SAMLValidator validator;
	
	// provides a full list of policies
	PolicyCacheQueryData policyCacheQueryAll;
	// provides a list of updated policies
	PolicyCacheQueryData policyCacheQueryUpdated;
	
	List<PolicyCacheData> fullList ;
	List<PolicyCacheData> updateList;
	private String esoeKeyAlias;
	private int spepIndex;
	
	/**
	 * @throws java.lang.Exception
	 */
	@SuppressWarnings({ "unqualified-field-access", "nls" })
	@Before
	public void setUp() throws Exception
	{
		this.testCache = new AuthzPolicyCacheImpl();
		this.failureRep = new AuthzCacheUpdateFailureRepositoryImpl();
			
		this.endpoints = new HashMap<Integer,String>();
		this.endpoints.put(0, "www.blah.com");
		
		int skew = (Integer.MAX_VALUE / 1000 -1);
		
		String keyStorePath = "tests" + File.separator + "testdata" + File.separator + "testskeystore.ks";
		String keyStorePassword = "Es0EKs54P4SSPK";
		esoeKeyAlias = "esoeprimary";
		String esoeKeyPassword = "Es0EKs54P4SSPK";
	
		this.keyStoreResolver = new KeystoreResolverImpl(new File(keyStorePath), keyStorePassword, esoeKeyAlias, esoeKeyPassword);
				
		String[] clearAuthzCacheSchemas = new String[]{SchemaConstants.esoeProtocol, SchemaConstants.samlAssertion, SchemaConstants.samlProtocol};
		this.clearAuthzCacheResponseMarshaller = new MarshallerImpl<ClearAuthzCacheResponse>(ClearAuthzCacheRequest.class.getPackage().getName() + ":" + Request.class.getPackage().getName(), clearAuthzCacheSchemas, keyStoreResolver);
		this.policyMarshaller = new MarshallerImpl<Policy>(Policy.class.getPackage().getName(), new String[] { SchemaConstants.lxacml });
		
		this.validator = new SAMLValidatorImpl(new IdentifierCacheImpl(), skew);
		this.identifierCache = new IdentifierCacheImpl();
		this.idGenerator = new IdentifierGeneratorImpl(this.identifierCache);
		
		webServiceClient = createMock(WSClient.class);
		expect(this.webServiceClient.authzCacheClear( (byte[])anyObject(),(String)notNull() ) ).andReturn(this.getTestAuthzResponse()).anyTimes();
		replay(this.webServiceClient);
		
		// Emulate 3 policies with 3 different ID's for a full update,
		// 1 with an existing entity ID but a new policy for an update
		// 1 with a new entity ID for an add
		// 1 with an existing entity ID, but no policies for a remove
		fullList = new Vector<PolicyCacheData>();
		updateList = new Vector<PolicyCacheData>();
		
		this.spepIndex = 12345;
		
		// SETUP policies to be included in startup rebuild - 4 Policies to start
		PolicyCacheData testPolicy = new PolicyCacheData();
		testPolicy.setLxacmlPolicy(this.getTestPolicy1());
		testPolicy.setEntityID(this.validSpep);
		testPolicy.setPollAction("U");
		fullList.add(testPolicy);
		
		PolicyCacheData testPolicy2 = new PolicyCacheData();
		testPolicy2.setLxacmlPolicy(this.getTestPolicy2());
		testPolicy2.setEntityID(this.invalidSpep);
		testPolicy2.setSequenceId(new BigDecimal(6));
		fullList.add(testPolicy2);
		
		PolicyCacheData testPolicy3 = new PolicyCacheData();
		testPolicy3.setLxacmlPolicy(this.getTestPolicy2());
		testPolicy3.setEntityID(this.validSpep2);
		testPolicy3.setSequenceId(new BigDecimal(5));
		fullList.add(testPolicy3);
		
		PolicyCacheData testPolicy4 = new PolicyCacheData();
		testPolicy4.setLxacmlPolicy(this.getTestPolicy2());
		testPolicy4.setEntityID("new:spep:entity");
		testPolicy4.setSequenceId(new BigDecimal(3));
		testPolicy4.setPollAction("A");
		fullList.add(testPolicy4);
	

		policyCacheQueryUpdated = new PolicyCacheQueryData();
		policyCacheQueryUpdated.setEntityID(this.validSpep);
		policyCacheQueryUpdated.setSequenceId(new BigDecimal(1) );
		
		policyCacheQueryAll = new PolicyCacheQueryData();
		policyCacheQueryAll.setEntityID(this.validSpep);
		policyCacheQueryAll.setSequenceId(new BigDecimal(2) );
		
		policyCacheDao = createMock(PolicyCacheDao.class);	
		expect(this.policyCacheDao.queryLastSequenceId()).andReturn(5l).once();
		expect(this.policyCacheDao.queryLastSequenceId()).andReturn(10l).anyTimes();
		expect(this.policyCacheDao.queryPolicyCache((PolicyCacheQueryData)notNull() )).andReturn(fullList).once();
		expect(this.policyCacheDao.queryPolicyCache((PolicyCacheQueryData)notNull()) ).andReturn(updateList).anyTimes();
		//replay(this.policyCacheDao);
		
		EntityData entityData = createMock(EntityData.class);
		SPEPRole spepRole = createMock(SPEPRole.class);
		
		expect(entityData.getRoleData(SPEPRole.class)).andReturn(spepRole).anyTimes();
		
		List<IndexedEndpoint> indexedEndpointList = new ArrayList<IndexedEndpoint>();
		for (Map.Entry<Integer, String> endpoint : this.endpoints.entrySet())
		{
			indexedEndpointList.add(new IndexedEndpointImpl(BindingConstants.soap, endpoint.getValue(), endpoint.getKey().intValue()));
		}
		
		expect(spepRole.getCacheClearServiceEndpointList()).andReturn(indexedEndpointList).anyTimes();
		expect(spepRole.getCacheClearServiceEndpoint(BindingConstants.soap, this.spepIndex)).andReturn(this.endpoints.get(0)).anyTimes();
		
		replay(entityData);
		replay(spepRole);
		
		// setup  SPEPs to resolve
		this.metadata = createMock(MetadataProcessor.class);
		expect(this.metadata.getEntityData(this.invalidSpep)).andReturn(null).anyTimes();
		expect(this.metadata.getEntityData((String)notNull())).andReturn(entityData).atLeastOnce();
		expect(this.metadata.resolveKey(esoeKeyAlias)).andReturn(this.keyStoreResolver.getLocalPublicKey()).anyTimes();
		replay(this.metadata);
		
	}

	@After
	public void tearDown()
	{
		if(this.testCacheProcessor != null && this.testCacheProcessor.isAlive())
			this.testCacheProcessor.shutdown();
	}
	
	/**
	 * Test method for {@link com.qut.middleware.esoe.pdp.cache.impl.PolicyCacheProcessorImpl#PolicyCacheProcessorImpl(com.qut.middleware.esoe.pdp.cache.CacheUpdateFailureMonitor)}.
	 */
	@Test
	public final void testPolicyCacheProcessorImpl() throws Exception
	{
		replay(this.policyCacheDao);
	
		this.testCacheProcessor = new PolicyCacheProcessorImpl(failureRep, testCache, metadata, 
				policyCacheDao, webServiceClient, keyStoreResolver, idGenerator, validator, 1, esoeKeyAlias);
	
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
	public final void testSpepStartingNotification1() throws InterruptedException,  Exception
	{
		replay(this.policyCacheDao);
	
		this.testCacheProcessor = new PolicyCacheProcessorImpl(failureRep, testCache, metadata, 
				policyCacheDao, webServiceClient, keyStoreResolver, idGenerator, validator, 1, esoeKeyAlias);
		
		Thread.sleep(1000);
		
		PolicyCacheProcessor.result result;
		
		result = this.testCacheProcessor.spepStartingNotification(this.validSpep, this.spepIndex);
		
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
	public final void testSpepStartingNotification2() throws InterruptedException , Exception
	{
		replay(this.policyCacheDao);
		
		this.testCacheProcessor = new PolicyCacheProcessorImpl(failureRep, testCache, metadata, 
				policyCacheDao, webServiceClient, keyStoreResolver, idGenerator, validator, 1, esoeKeyAlias);
	
		Thread.sleep(1000);
		
		PolicyCacheProcessor.result result;
		
		result = this.testCacheProcessor.spepStartingNotification(this.invalidSpep, 0);
		
		// assert Invalid SPEP endpoint 
		assertEquals("Test return code for SPEP startup notification. " , PolicyCacheProcessor.result.Failure,  result);

	}
	
	/** Test that full cache rebuilds are occuring correctly
	 * 
	 */
	@Test
	public final void testPolicycacheUpdateFull() throws Exception
	{
		replay(this.policyCacheDao);
	
		this.testCacheProcessor = new PolicyCacheProcessorImpl(failureRep, testCache, metadata, 
				policyCacheDao, webServiceClient, keyStoreResolver, idGenerator, validator, 1, esoeKeyAlias);
	
		Thread.sleep(2000);
		
		assertEquals(4, this.testCache.getSize() );
		
	}
	
	
	/** Test that updated policies are being rebuilt correctly. This test is aimed at ensuring that a policy change
	 * for a specified entity is replaced in the cache by the new policy.
	 * 
	 */
	@Test
	public void testPolicycacheUpdateReplace() throws Exception
	{

		// In order for an update to succeed the policy ID must be the same, else it will add the new policy
		// instead of updating, so we trick the processor into thinking the polocy ID's are the same by setting
		// it in the data below
		PolicyCacheData testPolicy = new PolicyCacheData();
		testPolicy.setLxacmlPolicy(this.getTestPolicy2());
		testPolicy.setEntityID(this.validSpep);
		testPolicy.setPolicyId("urn:policy:complexity:1");
		testPolicy.setPollAction("U");
		testPolicy.setSequenceId(new BigDecimal(333333l));
		updateList.add(testPolicy);
			
		replay(this.policyCacheDao);

		this.testCacheProcessor = new PolicyCacheProcessorImpl(failureRep, testCache, metadata, 
				policyCacheDao, webServiceClient, keyStoreResolver, idGenerator, validator, 1, esoeKeyAlias);
	
		Thread.sleep(5000);
		
		// should be the same size, but the policy should have been replaced with new
		assertEquals(4, this.testCache.getSize() );
		
		byte[]policyXml = this.policyMarshaller.marshallUnSigned(this.testCache.getPolicies(this.validSpep).get(0));

		assertTrue(this.testCache.getPolicies(this.validSpep).size() == 1);
		
	//	String policyA = new String(this.getTestPolicy2(), "UTF-16");
	//	assertTrue(new String(policyXml, "UTF-16").equals( policyA) );
		
	}
	
	
	/** Test that added policies are being rebuilt correctly. This test is aimed at ensuring that a new policy 
	 * for an entity not currently stored in the cache is added correctly.
	 * 
	 */
	@Test
	public void testPolicycacheUpdateAdd() throws Exception
	{
		// In order for an update to succeed the policy ID must be the same, else it will add the new policy
		// instead of updating, so we trick the processor into thinking the polocy ID's are the same by setting
		// it in the data below
		PolicyCacheData testPolicy = new PolicyCacheData();
		testPolicy.setLxacmlPolicy(this.getTestPolicy2());
		testPolicy.setEntityID(this.validSpep);
		testPolicy.setPolicyId("urn:policy:complexity:1");
		testPolicy.setPollAction("A");
		testPolicy.setSequenceId(new BigDecimal(333333l));
		updateList.add(testPolicy);
			
		replay(this.policyCacheDao);
	
		this.testCacheProcessor = new PolicyCacheProcessorImpl(failureRep, testCache, metadata, 
				policyCacheDao, webServiceClient, keyStoreResolver, idGenerator, validator, 1, esoeKeyAlias);
	
		Thread.sleep(5000);
		
		// cache should be the same size, but an extra policy should have been added for this entity id
		assertEquals(4, this.testCache.getSize() );
		
		assertTrue(this.testCache.getPolicies(this.validSpep).size() == 2);
		
	}
	
	/** Test that removed policies are being rebuilt correctly
	 * 
	 */
	@Test
	public void testPolicycacheUpdateRemove() throws Exception
	{
		// In order for an update to succeed the policy ID must be the same, else it will add the new policy
		// instead of updating, so we trick the processor into thinking the polocy ID's are the same by setting
		// it in the data below
		PolicyCacheData testPolicy = new PolicyCacheData();
		testPolicy.setLxacmlPolicy(this.getTestPolicy1());
		testPolicy.setEntityID(this.validSpep);
		testPolicy.setPolicyId("urn:policy:complexity:1");
		testPolicy.setPollAction("D");
		testPolicy.setSequenceId(new BigDecimal(6l));
		updateList.add(testPolicy);
			
		replay(this.policyCacheDao);
	
		this.testCacheProcessor = new PolicyCacheProcessorImpl(failureRep, testCache, metadata, 
				policyCacheDao, webServiceClient, keyStoreResolver, idGenerator, validator, 1, esoeKeyAlias);
	
		Thread.sleep(5000);
		
		// cache should be the same size, but the policy should have been deleted, leving no policies for this entity
		assertEquals(4, this.testCache.getSize() );
		
		assertEquals(0, this.testCache.getPolicies(this.validSpep).size());
	}
	
	/** Test that the thread is running and polling correctly.
	 * 
	 * Test method for {@link com.qut.middleware.esoe.pdp.cache.impl.PolicyCacheProcessorImpl#run()}.
	 */
	@Test
	public final void testRun() throws Exception
	{	
		replay(this.policyCacheDao);
	
		this.testCacheProcessor = new PolicyCacheProcessorImpl(failureRep, testCache, metadata, 
				policyCacheDao, webServiceClient, keyStoreResolver, idGenerator, validator, 1, esoeKeyAlias);
	
		assertTrue(this.testCacheProcessor.isAlive()); 	
		
		Thread.sleep(2000);
		
		assertEquals(4, this.testCache.getSize());
	}

	
	private byte[] getTestPolicy1()
	{
		String path = "tests" + File.separator+ "testdata"+  File.separator  ;
			
		try
		{
			// Get the size of the file
			File file = new File(path + "Policy1.xml");
			long length = file.length();
			byte[] byteArray = new byte[(int) length];

			InputStream fileStream = new FileInputStream(file);
			fileStream.read(byteArray);
			fileStream.close();

			return byteArray;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	private byte[] getTestPolicy2()
	{
		String path = "tests" + File.separator+ "testdata"+  File.separator  ;
			
		try
		{
			// Get the size of the file
			File file = new File(path + "Policy2.xml");
			long length = file.length();
			byte[] byteArray = new byte[(int) length];

			InputStream fileStream = new FileInputStream(file);
			fileStream.read(byteArray);
			fileStream.close();

			return byteArray;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	
	private byte[] getTestAuthzResponse()
	{
		byte[] responseDocument = null;
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
		replay(this.policyCacheDao);
		
		this.testCacheProcessor = new PolicyCacheProcessorImpl(failureRep, testCache, metadata, 
				policyCacheDao, webServiceClient, keyStoreResolver, idGenerator, validator, 1, esoeKeyAlias);
	
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
				policyCacheDao, webServiceClient, keyStoreResolver, idGenerator, validator, 5, esoeKeyAlias);
	}
	
	/** Test invalid parameters in constructor
	 *
	 */ 
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction2() throws Exception
	{
		this.testCacheProcessor = new PolicyCacheProcessorImpl(failureRep, null, metadata, 
				policyCacheDao, webServiceClient, keyStoreResolver, idGenerator, validator, 5, esoeKeyAlias);
	}
	
	/** Test invalid parameters in constructor
	 *
	 */ 
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction3() throws Exception
	{
		this.testCacheProcessor = new PolicyCacheProcessorImpl(failureRep, testCache, null, 
				policyCacheDao, webServiceClient, keyStoreResolver, idGenerator, validator, 5, esoeKeyAlias);
	}
	
	/** Test invalid parameters in constructor
	 *
	 */ 
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction4() throws Exception
	{
		this.testCacheProcessor = new PolicyCacheProcessorImpl(failureRep, testCache, metadata, 
				null, webServiceClient, keyStoreResolver, idGenerator, validator, 5, esoeKeyAlias);
	}
	
	/** Test invalid parameters in constructor
	 *
	 */ 
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction5() throws Exception
	{
		this.testCacheProcessor = new PolicyCacheProcessorImpl(failureRep, testCache, metadata, 
				policyCacheDao, null, keyStoreResolver, idGenerator, validator, 5, esoeKeyAlias);
	}
	
	/** Test invalid parameters in constructor
	 *
	 */ 
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction6() throws Exception
	{
		this.testCacheProcessor = new PolicyCacheProcessorImpl(failureRep, testCache, metadata, 
				policyCacheDao, webServiceClient, null, idGenerator, validator, 5, esoeKeyAlias);
	}
	
	/** Test invalid parameters in constructor
	 *
	 */ 
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction7() throws Exception
	{
		this.testCacheProcessor = new PolicyCacheProcessorImpl(failureRep, testCache, metadata, 
				policyCacheDao, webServiceClient, keyStoreResolver, null, validator, 5, esoeKeyAlias);
	}
	
	/** Test invalid parameters in constructor
	 *
	 */ 
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction8() throws Exception
	{
		this.testCacheProcessor = new PolicyCacheProcessorImpl(failureRep, testCache, metadata, 
				policyCacheDao, webServiceClient, keyStoreResolver, idGenerator, null, 5, esoeKeyAlias);
	}
	
	/** Test invalid parameters in constructor
	 *
	 */ 
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction9() throws Exception
	{
		this.testCacheProcessor = new PolicyCacheProcessorImpl(failureRep, testCache, metadata, 
				policyCacheDao, webServiceClient, keyStoreResolver, idGenerator, validator, -5, esoeKeyAlias);
	}
}
