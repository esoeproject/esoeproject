package com.qut.middleware.esoe.pdp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.authz.cache.AuthzCacheUpdateFailureRepository;
import com.qut.middleware.esoe.authz.cache.bean.FailedAuthzCacheUpdate;
import com.qut.middleware.esoe.authz.cache.bean.impl.FailedAuthzCacheUpdateImpl;
import com.qut.middleware.esoe.authz.cache.impl.AuthzCacheUpdateFailureRepositoryImpl;
@SuppressWarnings({"unqualified-field-access", "nls"})
public class AuthzCacheUpdateFailureRepositoryTest
{

	AuthzCacheUpdateFailureRepository testRepo;
	FailedAuthzCacheUpdate failure1;
	FailedAuthzCacheUpdate failure2 ;
	FailedAuthzCacheUpdate failure3 ;
	
	@Before
	public void setUp() throws Exception
	{
		this.testRepo = new AuthzCacheUpdateFailureRepositoryImpl();
		
		failure1 = new FailedAuthzCacheUpdateImpl();
		failure1.setEndPoint("url.one");
		failure1.setRequestDocument(new String("<doc1></doc1>").getBytes());
		failure1.setTimeStamp(new Date());
		
		failure2 = new FailedAuthzCacheUpdateImpl();
		failure2.setEndPoint("url.three");
		failure2.setRequestDocument(new String("<doc1></doc1>").getBytes());
		failure2.setTimeStamp(new Date(System.currentTimeMillis() - 3287483));
		
		failure3 = new FailedAuthzCacheUpdateImpl();
		failure3.setEndPoint("url.two");
		failure3.setRequestDocument(new String("<doc1></doc1>").getBytes());
		failure3.setTimeStamp(new Date(System.currentTimeMillis() + 43654));
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public final void testAdd()
	{
		testRepo.add(failure1);

		assertTrue(testRepo.getSize() == 1);
		
	}

	@Test
	public final void testClearFailures()
	{
		testRepo.add(failure1);
		testRepo.add(failure2);

		assertTrue(testRepo.getSize() == 2);
		
		testRepo.clearFailures();
		
		assertTrue(testRepo.getSize() == 0);
	}

	@Test
	public final void testGetFailures()
	{
		testRepo.add(failure1);
		testRepo.add(failure2);
		
		assertTrue("Expected failure was not found", testRepo.containsFailure(failure1));
	}

	@Test
	public final void testRemove()
	{
		testRepo.add(failure1);
		testRepo.add(failure2);
	
		assertEquals("Unexpected repo size", 2, testRepo.getSize());
		
		testRepo.remove(failure2);
		
		assertEquals("Unexpected repo size", 1,  testRepo.getSize());
		
		// make sure it no longer contains the removed failure, but does contain the other
		assertTrue("Unexpected failure found in repo",  ! testRepo.containsFailure(failure2));
		assertTrue("Unexpected failure found in repo",  testRepo.containsFailure(failure1));
	}

	@Test
	public final void testGetSize()
	{
		testRepo.add(failure1);
		testRepo.add(failure2);
		testRepo.add(failure3);
	
		assertTrue(testRepo.getSize() == 3);
	}

}
