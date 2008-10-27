package com.qut.middleware.esoe.sessions;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.logout.LogoutMechanism;
import com.qut.middleware.esoe.logout.LogoutThreadPool;
import com.qut.middleware.esoe.sessions.bean.impl.IdentityDataImpl;
import com.qut.middleware.esoe.sessions.cache.SessionCache;
import com.qut.middleware.esoe.sessions.cache.impl.SessionCacheImpl;
import com.qut.middleware.esoe.sessions.exception.DuplicateSessionException;
import com.qut.middleware.esoe.sessions.exception.SessionCacheUpdateException;
import com.qut.middleware.esoe.sessions.impl.PrincipalImpl;
import com.qut.middleware.esoe.sessions.impl.SessionsMonitor;
import com.qut.middleware.saml2.identifier.IdentifierCache;
import com.qut.middleware.saml2.identifier.impl.IdentifierCacheImpl;
@SuppressWarnings("nls")
public class SessionsMonitorTest 
{
	
	private SessionsMonitor monitor;
	private IdentifierCache idcache;
	private SessionCache sessioncache;
	private int interval;
	private int timeout;
	private LogoutThreadPool logoutThreadPool;
	
	@SuppressWarnings("unqualified-field-access")
	@Before
	public void setUp() throws Exception 
	{
		this.idcache = new IdentifierCacheImpl();
		
		this.logoutThreadPool = createMock(LogoutThreadPool.class);
		expect(this.logoutThreadPool.createLogoutTask((Principal)notNull(), eq(false)) ).andReturn("BlahTaskID").anyTimes();
		replay(this.logoutThreadPool);
		
		this.sessioncache = new SessionCacheImpl(this.logoutThreadPool);
		this.interval = 3;
		this.timeout = 8;

		this.monitor = new SessionsMonitor(idcache, sessioncache, interval, timeout);		
	}

	@Test
	public void testRun() 
	{
		assertTrue(this.monitor.isAlive());
	}

	@Test
	public void testSessionsMonitor() throws Exception
	{
		assertTrue(this.monitor.isAlive());
		
		// Add a few principals to test behaviour
		PrincipalImpl data = new PrincipalImpl();
		data.setPrincipalAuthnIdentifier("testuser-1");
		data.setSessionID("somerandomsessionID1");
		data.setSAMLAuthnIdentifier("dhithere");
		data.setSessionNotOnOrAfter(System.currentTimeMillis() + 100000);
		
		PrincipalImpl data2 = new PrincipalImpl();
		data2.setPrincipalAuthnIdentifier("testuser-2");
		data2.setSessionID("somerandomsessionID2");
		data2.setSAMLAuthnIdentifier("hithere");
		data2.setSessionNotOnOrAfter(System.currentTimeMillis() + 100000);
		
		try
		{
			this.sessioncache.addSession(data);
			this.sessioncache.addSession(data2);
			
			// gettting session data will refresh it's last accessed timestamp, so this one should not be removed.
			this.sessioncache.getSession("somerandomsessionID2");
			
			// sleep while it polls
			Thread.sleep(4000);
			
			// ensure the first record has been removed as its now expired
			assertEquals(null, this.sessioncache.getSession("somerandomsessionID1"));
			
			// ensure the second record still exists as it has been accessed and therefore not expired
			assertEquals(data2, this.sessioncache.getSession("somerandomsessionID2"));
	
		}
		catch(SessionCacheUpdateException e)
		{
			e.printStackTrace();
		}
	}

	@Test
	public void testShutdown() throws Exception
	{
		Thread.sleep(5000);
		
		assertTrue(this.monitor.isAlive());
		
		this.monitor.shutdown();
		
		Thread.sleep(5000);
		
		assertTrue("Monitor Thread should be dead.", !this.monitor.isAlive());
	}
	
}
