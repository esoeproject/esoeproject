package com.qut.middleware.esoe.sso.impl;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.sso.bean.FailedLogout;
import com.qut.middleware.esoe.sso.bean.impl.FailedLogoutImpl;

public class FailedLogoutTest 
{

	private FailedLogout failure = new FailedLogoutImpl();
	private String endpoint = "http://something.com";
	private byte[] document = new String("<doc>Hi there</doc>").getBytes();
	private Date time = new Date();
	private String authnId = "678gg6et9f634";
	
	@Before
	public void setUp()
	{
		this.failure.setEndPoint(this.endpoint);
		this.failure.setRequestDocument(this.document);
		this.failure.setTimeStamp(this.time);
		this.failure.setAuthnId(this.authnId);
	}
	
	
	@Test
	public void testGetEndPoint()
	{
			assertTrue(this.failure.getEndPoint().equals(this.endpoint));
		
	}

	@Test
	public void testGetRequestDocument() 
	{
			assertTrue(this.failure.getRequestDocument().equals(this.document));
	}

	@Test
	public void testGetTimeStamp() 
	{
		assertTrue(this.failure.getTimeStamp().equals(this.time));
	}

	
	/* Test equals override. Two LogoutFailures should be equal if all fields contained in each
	 * object are equal.
	 */
	@Test
	public void testEqualsObject()
	{
		// first compare to an incomplete object. Must fail.
		FailedLogout testObject = new FailedLogoutImpl();
		testObject.setEndPoint("http://something.com");
		
		assertTrue(!testObject.equals(this.failure));
		// now add correct field so all fields match
		testObject.setAuthnId(this.authnId);
		
		assertTrue(testObject.equals(this.failure));
	}

}
