package com.qut.middleware.spep;

import static org.junit.Assert.assertTrue;


import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.saml2.identifier.IdentifierCache;
import com.qut.middleware.saml2.identifier.exception.IdentifierCollisionException;
import com.qut.middleware.saml2.identifier.impl.IdentifierCacheImpl;
import com.qut.middleware.spep.impl.IdentifierCacheMonitor;

public class IdentifierCacheMonitorTtest {

	private long timeout = 4; // expire after 4 seconds
	private long interval = 2; // poll every 2 seconds
	private IdentifierCache identifierCache;
	private IdentifierCacheMonitor monitor;
	
	@Before
	public void setUp() throws Exception
	{
		this.identifierCache = new IdentifierCacheImpl();
	}

	@SuppressWarnings("nls")
	@Test
	public void testRun() throws InterruptedException
	{			
		this.monitor = new IdentifierCacheMonitor(this.identifierCache, this.interval, this.timeout);
		
		assertTrue(this.monitor.isAlive());
		
		try
		{
			this.identifierCache.registerIdentifier("BLAH1");
			this.identifierCache.registerIdentifier("BLAH2");
		}
		catch(IdentifierCollisionException  e)
		{
			// cant happen
		}
		
		// sleep 2 seconds = 1 poll interval .. should still be 2 entries
		Thread.sleep(2000);
		
		assertTrue(this.identifierCache.containsIdentifier("BLAH1"));
		assertTrue(this.identifierCache.containsIdentifier("BLAH2"));
		
		// sleep 6 more seconds =  entries should be expired (gone)
		Thread.sleep(6000);
		
		assertTrue("Identifier was still in cache. It should have expired", !this.identifierCache.containsIdentifier("BLAH1"));
		assertTrue("Identifier was still in cache. It should have expired", !this.identifierCache.containsIdentifier("BLAH2"));
		
	}
	
	/** Test invalid constructor params.
	 * 
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction2() throws Exception
	{
		this.monitor = new IdentifierCacheMonitor(null, this.interval, this.timeout);
	}
	
	
	/** Test invalid constructor params.
	 * 
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction3() throws Exception
	{
		this.monitor = new IdentifierCacheMonitor(this.identifierCache, 0, this.timeout);
	}
	
	/** Test invalid constructor params.
	 * 
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction1() throws Exception
	{
		this.monitor = new IdentifierCacheMonitor(this.identifierCache, this.interval, -0113);
	}
	
	
	@Test
	public void testInterrupt()
	{
		this.monitor = new IdentifierCacheMonitor(this.identifierCache, this.interval, this.timeout);
		
		this.monitor.interrupt();
		
		assertTrue(this.monitor.isAlive());
	}

}
