package com.qut.middleware.esoe.pdp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.pdp.cache.bean.AuthzPolicyCache;
import com.qut.middleware.esoe.pdp.cache.bean.impl.AuthzPolicyCacheImpl;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Policy;
import com.qut.middleware.saml2.schemas.esoe.lxacml.PolicySet;


@SuppressWarnings("nls")
public class AuthzCacheTest 
{	
	private AuthzPolicyCache testCache;
	
	@Before
	public void setUp() throws Exception
	{
		this.testCache = new AuthzPolicyCacheImpl();		
	}


	@Test
	public void testAuthzCache()
	{		
		Object test = this.testCache.getCache();
		
		assertNotNull(test);
	}

	@Test
	public void testAdd1()
	{
		Policy testPolicy = new Policy();
		testPolicy.setPolicyId("473642842jdsah78");
		testPolicy.setDescription("Test policy from JUnit test");
		
		Policy testPolicy2 = new Policy();
		testPolicy2.setPolicyId("473642842jsah78gg55");
		testPolicy2.setDescription("Test policy 2 from JUnit test");
		
		Policy testPolicy3 = new Policy();
		testPolicy3.setPolicyId("4736h7fddsah785555");
		testPolicy3.setDescription("Test policy 3 from JUnit test");
		
		Vector<Policy> pSet = new Vector<Policy>();
		pSet.add(testPolicy);
		pSet.add(testPolicy2);
		pSet.add(testPolicy3);
		
		this.testCache.add("urn:test:descriptor:id:1", pSet);
		
		assertEquals("Test return size of cache", 1, this.testCache.getSize());
		
		// test objects are as expected
	//	assertEquals("Cache object inconsistent. ", pSet.getDescription(), this.testCache.getPolicies("urn:test:descriptor:id:1").getDescription() );
	}
	
	@Test
	public void testGetPolicy() 
	{
		Policy testPolicy = new Policy();
		testPolicy.setPolicyId("1234");
		testPolicy.setDescription("Test policy from JUnit test");
		
		Vector<Policy> pSet = new Vector<Policy>();
		pSet.add(testPolicy);
		
		this.testCache.add("urn:1234", pSet);
		
		Vector<Policy> returned = (Vector<Policy>)this.testCache.getPolicies("urn:1234");
		
		assertEquals("Check returned policy size of set. ", 1, returned.size()) ;
		
	}

	@Test
	public void testRemove()
	{
		Policy testPolicy = new Policy();
		testPolicy.setPolicyId("1");
		testPolicy.setDescription("Test remove policy from JUnit test");
		
		Vector<Policy> pSet = new Vector<Policy>();
		pSet.add(testPolicy);
		
		this.testCache.add("urn:1234", pSet);
		// removing a non existent policy should return false
		assertTrue("Remove non existent policy returned true. ", !this.testCache.remove("blah") );
	
		// removing the policy we just added should return true
		assertTrue("Remove existing cache object returned false. ", this.testCache.remove("urn:1234"));
		
	}

	
	
	@Test
	public void testGetCache()
	{
		assertNotNull(this.testCache.getCache());
	}
	
	
	@Test
	public void testSetCache()
	{
		Map newCache = new HashMap();
		Policy test1 = new Policy();
		test1.setPolicyId("1");
		test1.setDescription("Test policy ");
		
		Policy test2 = new Policy();
		test2.setPolicyId("2");
		test2.setDescription("Test policy ");
		
		Policy test3 = new Policy();
		test3.setPolicyId("3");
		test3.setDescription("Test policy 3, yeah");
		
		newCache.put(test1.getPolicyId(), test1);
		newCache.put("2", test2);
		newCache.put(test3.getPolicyId(), test3);
		
		this.testCache.setCache(newCache);
		
		
		
	}

}
