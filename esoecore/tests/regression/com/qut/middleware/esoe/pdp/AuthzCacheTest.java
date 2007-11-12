package com.qut.middleware.esoe.pdp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
		// check the list size to ensure its as expected
		assertTrue("Size after remove incorrect", this.testCache.getSize() == 1);
		
		// removing the policy we just added should return true
		assertTrue("Remove existing cache object returned false. ", this.testCache.remove("urn:1234"));
		
		// check the list size to ensure its as expected
		assertTrue("Size after remove incorrect",  this.testCache.getSize() == 0);
		
	}

		
	@Test
	public void testSetCache()
	{
		Map<String, List<Policy>> newCache = new HashMap<String, List<Policy>>();
		Policy test1 = new Policy();
		test1.setPolicyId("1");
		test1.setDescription("Test policy ");
		
		Policy test2 = new Policy();
		test2.setPolicyId("2");
		test2.setDescription("Test policy ");
		
		Policy test3 = new Policy();
		test3.setPolicyId("3");
		test3.setDescription("Test policy 3, yeah");
		
		Vector<Policy> policies = new Vector<Policy>();
		policies.add(test1);
		policies.add(test2);
		policies.add(test3);
		
		newCache.put("1", policies);
		
		// test adding same policies under different entity to ensure no mix of data
		newCache.put("2", policies);
			
		this.testCache.setCache(newCache);		
		
		assertEquals(this.testCache.getSize(), 2);
		assertEquals(this.testCache.getPolicies("1").size(), 3);
		assertEquals(this.testCache.getPolicies("2").size(), 3);
	}


	@Test
	public void testConcurrency() throws Exception 
	{
		this.testSetCache();
	
		new WriteThread(this.testCache);	
		
		new ReadThread(this.testCache);
		
		Thread.sleep(1000);
		
		// make sure the policy we are checking is as expected
		assertTrue("Modified policy ID not encountered", this.testCache.getPolicies("1").get(2).getPolicyId().equals("200"));
		
	}
	
	class ReadThread extends Thread
	{
		private AuthzPolicyCache cache;
		boolean running = true;
		
		public ReadThread()
		{
			// for whiny hudson
		}
		
		public ReadThread(AuthzPolicyCache cache)
		{
			this.cache = cache;
			this.start();
		}
		
		public void run()
		{
			while(running)
			{
				this.testIterateModifiedPolicy();
			}
		}
	
		private void testIterateModifiedPolicy()
		{
			Iterator<Policy> iter = this.cache.getPolicies("1").iterator();
			while(iter.hasNext())
			{
				Policy next = iter.next();
				//System.out.println("Reading policy desc : " + next.getDescription() );
			}
		}
	}
	
	
	class WriteThread extends Thread
	{
		private AuthzPolicyCache cache;
		boolean running = true;
		
		public WriteThread()
		{
			// for whiny hudson
		}
		
		public WriteThread(AuthzPolicyCache cache)
		{
			this.cache = cache;
			this.start();
		}
		
		public void run()
		{
			int count = 0;
			while(running)
			{
				count ++;
				
				this.testModifyPolicyIterator(count);
				
				try
				{
					//this.sleep(1000);
				}
				catch(Exception e)
				{
					//
				}
			}
		}
		
	
		private void testModifyPolicyIterator(int count)
		{
			Policy test1 = new Policy();
			test1.setPolicyId("200");
			
			boolean replacedPolId = false;
			
			List<Policy> policies = this.cache.getPolicies("1");
			Iterator<Policy> iter = policies.iterator();
			while(iter.hasNext())
			{
				Policy next = iter.next();
				if( next.getPolicyId().equals("1"))
				{
					iter.remove();
					replacedPolId  = true;
				}
				
			//	System.out.println("Modified policy " + next.getDescription());
				next.setDescription("Test policy modified " + count + " times");
			}
			
			if(replacedPolId)
				policies.add(test1) ;
			
			// replace cache policy list
			this.cache.add("1", policies );
			
		}
	}
	

}
