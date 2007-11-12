package com.qut.middleware.esoe.metadata;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import static org.junit.Assert.*;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.metadata.cache.MetadataCache;
import com.qut.middleware.esoe.metadata.exception.InvalidMetadataEndpointException;
import com.qut.middleware.esoe.metadata.impl.MetadataImpl;
import com.qut.middleware.saml2.exception.KeyResolutionException;
import com.qut.middleware.saml2.sec.KeyData;

public class MetadataTest {
	
	private MetadataCache testcache;
	private Metadata metadata;
	
	private String revision = "TEST-REVISION-4372467834";
	private String spepID1 = "TEST-SPEP-1";
	private String spepID2 = "TEST-SPEP-2";
	private String spepId1withIndex0 = "0:TEST-SPEP-1";
	private String spepId1withIndex1 = "1:TEST-SPEP-1";
	private String spepId2withIndex0 = "0:TEST-SPEP-2";

	// in realtime these would all contain different URLS pointing to different services,
	// eg one for assertions, one for cacheClear etc etc
	private String spep1URL0 = "https://some.service.url/spep1";
	private String spep1URL1 = "https://someother.service.url/spep1";
	private String spep2URL = "https://some.service.url/spep2";
	
	private String keyName = "testKey";
	private String esoeID = "ESOE-ID-738273892";
	private Map<String, KeyData> keyMap;
	private PublicKey pubKey;
	private Vector<String> testServiceList;
	Map<Integer,String> testServiceMap;
	
	@Before
	public void setUp() throws Exception 
	{
		this.testcache = createMock(MetadataCache.class);
		
		// set assertion service mocks
		Map<String, String> assertions = new HashMap<String, String>(); 
		assertions.put(this.spepId1withIndex0, this.spep1URL0);
		assertions.put(this.spepId1withIndex1, this.spep1URL1);
		assertions.put(this.spepId2withIndex0, this.spep2URL);
		
		expect(this.testcache.getAssertionConsumerServices()).andReturn(assertions).anyTimes();
		
		int i = 0;
		
		// set cache clear service mocks
		Map<String, Map<Integer,String>> cacheClears = new HashMap<String, Map<Integer,String>>();
		testServiceMap = new HashMap<Integer,String>();
		testServiceMap.put(Integer.valueOf(i++), this.spep1URL0);
		testServiceMap.put(Integer.valueOf(i++), this.spep1URL1);
		
		cacheClears.put(this.spepID1, testServiceMap);
		cacheClears.put(this.spepID2, testServiceMap);
		
		expect(this.testcache.getCacheClearServices()).andReturn(cacheClears).anyTimes();
		
		// set logout service mocks (using the same list for brevity)
		Map<String, List<String>> logoutServices = new HashMap<String, List<String>>();		
		
		this.testServiceList = new Vector<String>();
		this.testServiceList.add("node.one");
		
		logoutServices.put(this.spepID1, testServiceList);
		logoutServices.put(this.spepID2, testServiceList);
		
		expect(this.testcache.getSingleLogoutServices()).andReturn(logoutServices).anyTimes();
		
		expect(this.testcache.getCurrentRevision()).andReturn(this.revision).anyTimes();
		
		// set key map mocks
		this.keyMap = new HashMap<String, KeyData>();
		this.pubKey = createMock(PublicKey.class);
		this.keyMap.put(this.keyName, new KeyData(pubKey));
		
		expect(this.testcache.getKeyMap()).andReturn(this.keyMap).anyTimes();
		expect(this.testcache.getState()).andReturn(MetadataCache.State.Initialized).anyTimes();
		
		this.metadata = new MetadataImpl(this.esoeID, this.testcache);
		
	}

	@After
	public void tearDown() throws Exception 
	{
	}

	
	@Test
	public void testGetCurrentRevision()
	{
		replay(this.testcache);
		
		String revision = this.metadata.getCurrentRevision();
		
		assertEquals(revision, this.revision, revision);
	}

	@Test
	public void testResolveKey() 
	{
		replay(this.testcache);
		
		try
		{
			assertEquals(this.pubKey, this.metadata.resolveKey(this.keyName));
		}
		catch(KeyResolutionException e)
		{
			fail("Unexpected exception thrown");
		}
	}

	@Test
	public void testResolveAssertionConsumerServiceValid()
	{
		replay(this.testcache);
	
		try
		{
			// attempt to resolve assertion for index 0 of spep 1
			assertEquals(this.spep1URL0, this.metadata.resolveAssertionConsumerService(this.spepID1, 0));
		
			// attempt to resolve assertion for index 1 of spep 1
			assertEquals(this.spep1URL1, this.metadata.resolveAssertionConsumerService(this.spepID1, 1));
		
			// attempt to resolve assertion for index 0 of spep 2
			assertEquals(this.spep2URL, this.metadata.resolveAssertionConsumerService(this.spepID2, 0));
		
		}
		catch(InvalidMetadataEndpointException e)
		{
			fail("Unexpected exception thrown" + e.toString());
		}
		
	}

	
	/** Attemot to resolve an invalid spep
	 * 
	 *
	 */
	@Test
	public void testResolveAssertionConsumerServiceInValid()
	{
		replay(this.testcache);
	
		try
		{
			// attempt to resolve assertion for index 0 of spep 1
			this.metadata.resolveAssertionConsumerService(this.spepID1, 43);
			
					
		}
		catch(InvalidMetadataEndpointException e)
		{
			// we want one of these
			return;
		}
		
		fail("No Exception thrown");
		
	}
	
	
	@Test
	public void testResolveCacheClearService()
	{
		replay(this.testcache);
		
		try
		{
			// attempt to resolve clear cache service list
			assertEquals(this.testServiceMap, this.metadata.resolveCacheClearService(this.spepID1));
		
		}
		catch(InvalidMetadataEndpointException e)
		{
			fail("Unexpected exception thrown" + e.toString());
		}
	}

	@Test
	public void testResolveSingleLogoutService() 
	{
		replay(this.testcache);
		
		try
		{
			// attempt to resolve clear cache service list
			assertEquals(this.testServiceList, this.metadata.resolveSingleLogoutService(this.spepID2));
		
		}
		catch(InvalidMetadataEndpointException e)
		{
			fail("Unexpected exception thrown" + e.toString());
		}
	}

	@Test
	public void testGetESOEIdentifier()
	{
		replay(this.testcache);
		
		assertEquals(this.esoeID, this.metadata.getEsoeEntityID());
		
	
	}

	
	
}
