package com.qut.middleware.esoe.pdp;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.pdp.cache.AuthzCacheUpdateFailureRepository;
import com.qut.middleware.esoe.pdp.cache.bean.FailedAuthzCacheUpdate;
import com.qut.middleware.esoe.pdp.cache.bean.impl.FailedAuthzCacheUpdateImpl;
import com.qut.middleware.esoe.pdp.cache.impl.AuthzCacheUpdateFailureRepositoryImpl;
@SuppressWarnings({"unqualified-field-access", "nls"})
public class AuthzCacheUpdateFailureRepositoryTest
{

	AuthzCacheUpdateFailureRepository testRepo = new AuthzCacheUpdateFailureRepositoryImpl();
	
	@Before
	public void setUp() throws Exception
	{
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public final void testAdd()
	{
		FailedAuthzCacheUpdate failure = new FailedAuthzCacheUpdateImpl();
		
		testRepo.add(failure);

		assertTrue(testRepo.getSize() == 1);
		
	}

	@Test
	public final void testClearFailures()
	{
		FailedAuthzCacheUpdate failure = new FailedAuthzCacheUpdateImpl();
		FailedAuthzCacheUpdate failure2 = new FailedAuthzCacheUpdateImpl();
		
		testRepo.add(failure);
		testRepo.add(failure2);

		assertTrue(testRepo.getSize() == 2);
		
		testRepo.clearFailures();
		
		assertTrue(testRepo.getSize() == 0);
	}

	@Test
	public final void testGetFailures()
	{
		FailedAuthzCacheUpdate failure = new FailedAuthzCacheUpdateImpl();
		
		testRepo.add(failure);

		assertTrue(testRepo.getFailures().indexOf(failure) != 1);
	}

	@Test
	public final void testRemove()
	{
		FailedAuthzCacheUpdate failure = new FailedAuthzCacheUpdateImpl();
		FailedAuthzCacheUpdate failure2 = new FailedAuthzCacheUpdateImpl();
		
		testRepo.add(failure);
		testRepo.add(failure2);
	
		testRepo.remove(failure2);
		
		assertTrue(testRepo.getFailures().indexOf(failure) == 0);
		
		assertTrue(testRepo.getFailures().indexOf(failure2) == -1);
		
		testRepo.remove(failure);
		
		assertTrue(testRepo.getFailures().indexOf(failure) == -1);
		
		assertTrue(testRepo.getSize() == 0);
	}

	@Test
	public final void testGetSize()
	{
		FailedAuthzCacheUpdate failure1 = new FailedAuthzCacheUpdateImpl();
		FailedAuthzCacheUpdate failure2 = new FailedAuthzCacheUpdateImpl();
		FailedAuthzCacheUpdate failure3 = new FailedAuthzCacheUpdateImpl();
		
		testRepo.add(failure1);
		testRepo.add(failure2);
		testRepo.add(failure3);
	
		assertTrue(testRepo.getSize() == 3);
	}

}
