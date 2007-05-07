package com.qut.middleware.spep.sessions;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.spep.sessions.impl.UnauthenticatedSessionImpl;

@SuppressWarnings("nls")
public class UnauthenticatedSessionTest
{

	private UnauthenticatedSession session;
	
	private String authnRequestSAMLID;
	private String requestURL;
	private long time;
	
	@Before
	public void setUp() throws Exception 
	{
		this.session = new UnauthenticatedSessionImpl();
		
		this.authnRequestSAMLID = "_8943787354";
		this.requestURL = "some.requested/url";
		this.time = System.currentTimeMillis();
	}

	@Test
	public void testGetAuthnRequestSAMLID()
	{
		this.session.setAuthnRequestSAMLID(this.authnRequestSAMLID);
		
		assertEquals(this.authnRequestSAMLID, this.session.getAuthnRequestSAMLID());
	}

	
	@Test
	public void testGetRequestURL() 
	{
		this.session.setRequestURL(this.requestURL);
		
		assertEquals(this.requestURL, this.session.getRequestURL());
	}

	
	@Test
	public void testGetIdleTime() throws Exception
	{
		// we'll allow a 1 second difference in case of test performance issues
		long skew = 1000;
		
		this.session.updateTime();
		
		Thread.sleep(1000);
		
		long myIdleCalculation = (System.currentTimeMillis() - this.time) /1000;
		long upperLimit = myIdleCalculation + skew;
		long lowerLimit = myIdleCalculation - skew;
		
		assertTrue("Idle time out of bounds.",  (this.session.getIdleTime() >= lowerLimit) && (this.session.getIdleTime() <= upperLimit) );
	}

}
