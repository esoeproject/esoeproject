package com.qut.middleware.esoe.sso.impl;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import com.qut.middleware.esoe.logout.bean.FailedLogout;
import com.qut.middleware.esoe.logout.bean.FailedLogoutRepository;
import com.qut.middleware.esoe.logout.bean.impl.FailedLogoutImpl;
import com.qut.middleware.esoe.logout.bean.impl.FailedLogoutRepositoryImpl;

@SuppressWarnings("nls")
public class FailedLogoutRepositoryTest {

	private FailedLogoutRepository logoutFailures;
	FailedLogout failure1;
	FailedLogout failure2;
	FailedLogout failure3;
	
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws  Exception
	{
		this.logoutFailures = new FailedLogoutRepositoryImpl();		
		
		failure1 = new FailedLogoutImpl();
		failure1.setEndPoint("url.one");
		failure1.setRequestDocument(createMock(Element.class));
		failure1.setTimeStamp(new Date());
		failure1.setAuthnId("743289423");
		
		failure2 = new FailedLogoutImpl();
		failure2.setEndPoint("url.three");
		failure2.setRequestDocument(createMock(Element.class));
		failure2.setTimeStamp(new Date(System.currentTimeMillis() - 3287483));
		failure2.setAuthnId("7894y2r7r");
		
		failure3 = new FailedLogoutImpl();
		failure3.setEndPoint("url.two");
		failure3.setRequestDocument(createMock(Element.class));
		failure3.setTimeStamp(new Date(System.currentTimeMillis() + 43654));
		failure3.setAuthnId("8r4");
	}
	
	@Test
	public void testAdd() 
	{
		this.logoutFailures.add(failure1);
		
		assert(this.logoutFailures.getSize() == 1);
		
		FailedLogout returned = this.logoutFailures.getFailures().get(0);
		assertEquals("Returned data does not match Set data. ", "url.one", returned.getEndPoint());
		assertEquals("Returned data does not match Set data. ", failure1.getRequestDocument(), returned.getRequestDocument());
		assertEquals("Returned data does not match Set data. ", failure1.getTimeStamp().getTime(), returned.getTimeStamp().getTime());

		// TEST to ensure that duplicates are detected
		assertTrue(this.logoutFailures.containsFailure(failure1));
		
		// create a new failure object with the same data. It too should be detected
		FailedLogout duplicate = new FailedLogoutImpl();
		duplicate.setEndPoint("url.one");
		duplicate.setAuthnId("743289423");
		
		assertTrue("repository does not contain expected failure", this.logoutFailures.containsFailure(duplicate));
		
		// make it a non duplicate to ensure it returns false
		duplicate.setEndPoint("yew8fyewfy");
		assertTrue(!this.logoutFailures.containsFailure(duplicate));
	}

	@Test
	public void testClearFailures() 
	{
		this.logoutFailures.add(failure1);
		this.logoutFailures.add(failure2);
		this.logoutFailures.add(failure3);
		
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
		failure.setRequestDocument(createMock(Element.class));
		failure.setTimeStamp(date);		
		failure.setAuthnId("12345");		
		
		FailedLogout failure2 = new FailedLogoutImpl();
		failure2.setEndPoint("www.test.2");
		failure2.setRequestDocument(createMock(Element.class));
		failure2.setTimeStamp(date);	
		failure2.setAuthnId("123");
		
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
		failure.setRequestDocument(createMock(Element.class));
		failure.setTimeStamp(date);		
		failure.setAuthnId("1234");
		
		FailedLogout failure2 = new FailedLogoutImpl();
		failure2.setEndPoint("www.test.2");
		failure2.setRequestDocument(createMock(Element.class));
		failure2.setTimeStamp(date);	
		failure2.setAuthnId("123");
		
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
