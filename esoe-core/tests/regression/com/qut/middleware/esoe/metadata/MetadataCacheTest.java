package com.qut.middleware.esoe.metadata;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.metadata.cache.CacheData;
import com.qut.middleware.esoe.metadata.cache.MetadataCache;
import com.qut.middleware.esoe.metadata.cache.impl.CacheDataImpl;
import com.qut.middleware.esoe.metadata.cache.impl.MetadataCacheImpl;

@SuppressWarnings("nls")
public class MetadataCacheTest {

	private MetadataCache testCache;
	
	@Before
	public void setUp() throws Exception 
	{
		this.testCache = new MetadataCacheImpl();
	}
	

	@Test
	public void testGetAssertionConsumerServices()
	{		
		CacheData data = this.createTestData();
		this.testCache.setCacheData(data);
		
		assertEquals("Unexpected return ", data.getAssertionConsumerServices(), this.testCache.getAssertionConsumerServices());
	}

	@Test
	public void testGetCacheClearServices()
	{
		CacheData data = this.createTestData();
		this.testCache.setCacheData(data);
		
		assertEquals("Unexpected return ", data.getCacheClearServices(), this.testCache.getCacheClearServices());
	}

	@Test
	public void testGetCurrentRevision()
	{
		CacheData data = this.createTestData();
		this.testCache.setCacheData(data);
		
		assertEquals("Unexpected return ", data.getCurrentRevision(), this.testCache.getCurrentRevision());
	}

	@Test
	public void testGetSingleLogoutServices()
	{
		CacheData data = this.createTestData();
		this.testCache.setCacheData(data);
		
		assertEquals("Unexpected return ", data.getSingleLogoutServices(), this.testCache.getSingleLogoutServices());
	
	}

	
	@Test
	public void testGetKeyMap()
	{
		CacheData data = this.createTestData();
		this.testCache.setCacheData(data);
		
		assertEquals("Unexpected return ", data.getKeyMap(), this.testCache.getKeyMap());
	}
	
	@Test
	public void testHasData()
	{
		assertEquals(false, this.testCache.hasData());
		
		// test a populated cache
		this.testCache.setCacheData(this.createTestData());
		
		assertEquals(true, this.testCache.hasData());
				
		// test an empty cache
		this.testCache.setCacheData(new CacheDataImpl());
		
		assertEquals(false, this.testCache.hasData());
	}

	@Test
	public void testSetCacheData()
	{		
		CacheData cacheData = this.createTestData();
		
		this.testCache.setCacheData(cacheData);
	
		assertEquals(this.testCache.getSingleLogoutServices(), cacheData.getSingleLogoutServices());
		assertEquals(this.testCache.getAssertionConsumerServices(), cacheData.getAssertionConsumerServices());
	
	}

	
	/* SET up all the cache data for comparisons here
	 * 
	 */
	private CacheData createTestData()
	{
		CacheData data = new CacheDataImpl();
		
		// set up some assertion services
		Map<String, String> assertionConsumerServices = new HashMap<String, String>();
		assertionConsumerServices.put("test 1", "http://some.service.com/");
		assertionConsumerServices.put("test 2", "http://some.newservice.com/");
		data.setAssertionConsumerServices(assertionConsumerServices);
		
		// set up some logout services
		Map<Integer,String> testMap = new HashMap<Integer,String>();
		testMap.put(1, "https://logout.one.com/cleacache");
		testMap.put(2, "https://spep.other.com/services/cacheclear");
		
		Map<String, Map<Integer,String>> cacheClearServices = new HashMap<String, Map<Integer,String>>();
		cacheClearServices.put("test 1", testMap);
		cacheClearServices.put("test 2", testMap);		
		data.setCacheClearServices(cacheClearServices);
		
		// set up some logout services
		List<String> testList = new Vector<String>();
		testList.add("https://logout.one.com/logout");
		testList.add("https://spep.other.com/services/logout");
		
		Map<String, List<String>> logoutServices = new HashMap<String, List<String>>();
		logoutServices.put("test 1", testList);
		logoutServices.put("test 2", testList);
		data.setSingleLogoutServices(logoutServices);
		
		data.setCurrentRevision("R1.9.434.%^#DHD");
		
		return data;
	}
	
	/**
	 * A simple test to shut the code coverage tool warnings up, with regards to enums.
	 * 
	 */
	@Test
	public void testState()
	{
		assertEquals(MetadataCache.State.Initialized, MetadataCache.State.valueOf("Initialized"));
		
		MetadataCache.State.values();
		
		this.testCache.setState(MetadataCache.State.Initialized);
		
		assertEquals(MetadataCache.State.Initialized, this.testCache.getState());	
		
	}
}
