package com.qut.middleware.esoe.sso.impl;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.sso.bean.FailedLogout;
import com.qut.middleware.esoe.sso.bean.FailedLogoutRepository;
import com.qut.middleware.esoe.sso.bean.impl.FailedLogoutImpl;
import com.qut.middleware.esoe.sso.bean.impl.FailedLogoutRepositoryImpl;

@SuppressWarnings("nls")
public class FailedLogoutRepositoryTest {

	private FailedLogoutRepository logoutFailures;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws  Exception
	{
		this.logoutFailures = new FailedLogoutRepositoryImpl();		
	}
	
	@Test
	public void testAdd() 
	{
		FailedLogout failure = new FailedLogoutImpl();
		failure.setEndPoint("www.test.co");
		failure.setRequestDocument("<xml>test</xml>");
		Date date = new Date(System.currentTimeMillis());
		failure.setTimeStamp(date);		
		
		this.logoutFailures.add(failure);
		
		assert(this.logoutFailures.getSize() == 1);
		
		FailedLogout returned = this.logoutFailures.getFailures().get(0);
		assertEquals("Returned data does not match Set data. ", "www.test.co", returned.getEndPoint());
		assertEquals("Returned data does not match Set data. ", "<xml>test</xml>", returned.getRequestDocument());
		assertEquals("Returned data does not match Set data. ", date.getTime(), returned.getTimeStamp().getTime());
	}

	@Test
	public void testClearFailures() 
	{
		this.logoutFailures.add(new FailedLogoutImpl());
		this.logoutFailures.add(new FailedLogoutImpl());
		this.logoutFailures.add(new FailedLogoutImpl());
		
		assert(this.logoutFailures.getSize() == 3);
		
		this.logoutFailures.clearFailures();
		
		assert(this.logoutFailures.getSize() == 0);
	}

		@Test
	public void testGetFailures() 
	{
		Date date = new Date(System.currentTimeMillis());
		
		FailedLogout failure = new FailedLogoutImpl();
		failure.setEndPoint("www.test.1");
		failure.setRequestDocument("<xml>test</xml>");
		failure.setTimeStamp(date);		
		
		
		FailedLogout failure2 = new FailedLogoutImpl();
		failure2.setEndPoint("www.test.2");
		failure2.setRequestDocument("<xml>test</xml>");
		failure2.setTimeStamp(date);	
		
		this.logoutFailures.add(failure);
		this.logoutFailures.add(failure2);
		
		assertEquals("Failure rep size is incorrect." , 2, this.logoutFailures.getSize());
		
		// ensure returned list size == getSize()
		List<FailedLogout> returnedList = this.logoutFailures.getFailures();
		
		assert(returnedList.size() == this.logoutFailures.getSize());
		
		assertEquals("Returned data does not match Set data. ", "www.test.1", returnedList.get(0).getEndPoint());
		assertEquals("Returned data does not match Set data. ", "www.test.2", returnedList.get(1).getEndPoint());		
	}

	@Test
	public void testRemove()
	{
		Date date = new Date(System.currentTimeMillis());
		
		FailedLogout failure = new FailedLogoutImpl();
		failure.setEndPoint("www.test.1");
		failure.setRequestDocument("<xml>test</xml>");
		failure.setTimeStamp(date);		
		
		
		FailedLogout failure2 = new FailedLogoutImpl();
		failure2.setEndPoint("www.test.2");
		failure2.setRequestDocument("<xml>test</xml>");
		failure2.setTimeStamp(date);	
		
		this.logoutFailures.add(failure);
		this.logoutFailures.add(failure2);
		
		// attempting to remove a non existent object should not alter the size
		this.logoutFailures.remove(new FailedLogoutImpl());		
		assertEquals("Unexpected return from test remove. ", 2, this.logoutFailures.getSize() );
		
		this.logoutFailures.remove(failure);		
		assertEquals("Unexpected return from test remove. ", 1, this.logoutFailures.getSize() );		
		assertEquals("Unexpected return from test remove. ", false, this.logoutFailures.getFailures().contains(failure));
		
		assertEquals("Unexpected return from test remove. ", true, this.logoutFailures.getFailures().contains(failure2));
		this.logoutFailures.remove(failure2);		
		assertEquals("Unexpected return from test remove. ", 0, this.logoutFailures.getSize() );		
		assertEquals("Unexpected return from test remove. ", false, this.logoutFailures.getFailures().contains(failure));
		
	}

	@Test
	public void testGetSize() 
	{
		assertEquals("Failure rep size is incorrect." , 0, this.logoutFailures.getSize());
	}

}
