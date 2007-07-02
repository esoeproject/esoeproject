package com.qut.middleware.esoe.metadata;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.codec.binary.Hex;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.metadata.cache.CacheData;
import com.qut.middleware.esoe.metadata.cache.MetadataCache;
import com.qut.middleware.esoe.metadata.cache.MetadataCache.State;
import com.qut.middleware.esoe.metadata.cache.impl.CacheDataImpl;
import com.qut.middleware.saml2.sec.KeyData;

public class CacheDataTest {

	private CacheData testData;
	
	@Before
	public void setUp() throws Exception
	{
		this.testData = new CacheDataImpl();
	}

	
	@Test
	public void testSetSingleLogoutServices()
	{
		// set up some logout services
		List<String> testList = new Vector<String>();
		testList.add("https://logout.one.com/logout");
		testList.add("https://spep.other.com/services/logout");
		
		Map<String, List<String>> logoutServices = new HashMap<String, List<String>>();
		logoutServices.put("test 1", testList);
		logoutServices.put("test 2", testList);
		
		this.testData.setSingleLogoutServices(logoutServices);
		
		assertEquals("Unexpected return ", logoutServices, this.testData.getSingleLogoutServices());
	}

	@Test
	public void testGetSingleLogoutServices()
	{
		// set up some logout services
		List<String> testList = new Vector<String>();
		testList.add("https://logout.one.com/logout");
		testList.add("https://spep.other.com/services/logout");
		
		Map<String, List<String>> logoutServices = new HashMap<String, List<String>>();
		logoutServices.put("test 1", testList);
		logoutServices.put("test 2", testList);
		
		this.testData.setSingleLogoutServices(logoutServices);
		
		assertEquals("Unexpected return ", logoutServices, this.testData.getSingleLogoutServices());
	}

	@Test
	public void testSetAssertionConsumerServices()
	{
		// set up some assertion services
		Map<String, String> assertionConsumerServices = new HashMap<String, String>();
		assertionConsumerServices.put("test 1", "http://some.service.com/");
		assertionConsumerServices.put("test 2", "http://some.newservice.com/");
		
		this.testData.setAssertionConsumerServices(assertionConsumerServices);
		
		assertEquals("Unexpected return ", assertionConsumerServices, this.testData.getAssertionConsumerServices());

	}

	@Test
	public void testGetAssertionConsumerServices()
	{
		// set up some assertion services
		Map<String, String> assertionConsumerServices = new HashMap<String, String>();
		assertionConsumerServices.put("test 1", "http://some.service.com/");
		assertionConsumerServices.put("test 2", "http://some.newservice.com/");
		
		this.testData.setAssertionConsumerServices(assertionConsumerServices);
		
		assertEquals("Unexpected return ", assertionConsumerServices, this.testData.getAssertionConsumerServices());

	}

	@Test
	public void testSetCacheClearServices()
	{
		// set up some cache clear services
		Map<Integer,String> testList = new HashMap<Integer,String>();
		testList.put(1, "https://logout.one.com/cleacache");
		testList.put(2, "https://spep.other.com/services/cacheclear");
		
		Map<String, Map<Integer,String>> cacheClearServices = new HashMap<String, Map<Integer,String>>();
		cacheClearServices.put("test 1", testList);
		cacheClearServices.put("test 2", testList);
		
		this.testData.setCacheClearServices(cacheClearServices);
		
		assertEquals("Unexpected return ", cacheClearServices, this.testData.getCacheClearServices());
	}

	@Test
	public void testGetCacheClearServices() 
	{
		// set up some logout services
		Map<Integer,String> testList = new HashMap<Integer,String>();
		testList.put(1, "https://logout.one.com/cleacache");
		testList.put(2, "https://spep.other.com/services/cacheclear");
		
		Map<String, Map<Integer,String>> cacheClearServices = new HashMap<String, Map<Integer,String>>();
		cacheClearServices.put("test 1", testList);
		cacheClearServices.put("test 2", testList);
		
		this.testData.setCacheClearServices(cacheClearServices);
		
		// assert same object comes back
		assertEquals("Unexpected return ", cacheClearServices, this.testData.getCacheClearServices());
	}

	@Test
	public void testSetCurrentRevision()
	{
		String revision = new String(Hex.encodeHex(new String("somenew hash value").getBytes()));
		
		this.testData.setCurrentRevision(revision);
		
		
		assertEquals("Unexpected return value", revision, this.testData.getCurrentRevision());
	}

	@Test
	public void testGetCurrentRevision()
	{
		String revision = new String(Hex.encodeHex(new String("somenew xx %%^#$*$ hash value").getBytes()));
		
		this.testData.setCurrentRevision(revision);
		
		
		assertEquals("Unexpected return value", revision, this.testData.getCurrentRevision());
	}

	@Test
	public void testGetKeyMap() 
	{
		Map<String, KeyData> km = new HashMap<String, KeyData>();
		
		this.testData.setKeyMap(km);
		
		assertEquals("Unexpected return value ", km, this.testData.getKeyMap());
	}

	@Test
	public void testSetKeyMap()
	{		
		this.testData.setKeyMap(null);
		
		assertEquals("Unexpected return value ", null, this.testData.getKeyMap());
	}

	
	@Test
	public void testState()
	{
		this.testData.setState(State.Initialized);
		
		assertEquals(testData.getState(), MetadataCache.State.valueOf("Initialized"));
	}
}
