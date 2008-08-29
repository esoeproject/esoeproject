/* 
 * Copyright 2006, Queensland University of Technology
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy of 
 * the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations under 
 * the License.
 * 
 * Author: Shaun Mangelsdorf
 * Creation Date: 06/10/2006
 * 
 * Purpose: Runs tests to ensure SessionCacheImpl functionality.
 */
package com.qut.middleware.esoe.sessions;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.MessageFormat;

import org.junit.Test;

import com.qut.middleware.esoe.logout.LogoutThreadPool;
import com.qut.middleware.esoe.logout.LogoutThreadPool;
import com.qut.middleware.esoe.sessions.bean.impl.IdentityDataImpl;
import com.qut.middleware.esoe.sessions.cache.SessionCache;
import com.qut.middleware.esoe.sessions.cache.impl.SessionCacheImpl;
import com.qut.middleware.esoe.sessions.exception.DuplicateSessionException;
import com.qut.middleware.esoe.sessions.impl.PrincipalImpl;

/** */
@SuppressWarnings("nls")
public class SessionCacheTest
{
	private SessionCache cache;

	private final String principal1 = "staffxyz"; //$NON-NLS-1$
	private final String samlID1 = "afsdkfjahsdkljfahlsdkj"; //$NON-NLS-01$
	private final String sessionID1 = "98598qyu892579827398572"; //$NON-NLS-01$

	private final String principal2 = "staffabc"; //$NON-NLS-01$
	private final String samlID2 = "mezxncvmzxbnioqtuwetiup"; //$NON-NLS-01$
	private final String sessionID2 = "6987358973458979847498"; //$NON-NLS-01$

	private final String principal3 = "stafffhg"; //$NON-NLS-01$
	private final String samlID3 = "gnqiopehjyptoiajsdopig"; //$NON-NLS-01$
	private final String sessionID3 = "4980984098109519819815"; //$NON-NLS-01$

	private LogoutThreadPool logout;

	/**
	 * 
	 */
	@Test
	public final void testAddSession1()
	{
		this.logout = createMock(LogoutThreadPool.class);
		//expect(logout.getEndPoints(entityID)).andReturn(endpoints);
		//expect(logout.performSingleLogout((String)notNull(), (List<String>)notNull(), eq(entityID), anyBoolean())).andReturn(LogoutThreadPool.result.LogoutSuccessful).anyTimes();
		replay(this.logout);
		
		this.cache = new SessionCacheImpl(logout);

		Principal data = new PrincipalImpl(new IdentityDataImpl(), 360);
		data.setPrincipalAuthnIdentifier(this.principal1);
		data.setSAMLAuthnIdentifier(this.samlID1);
		data.setSessionID(this.sessionID1);
		try
		{
			this.cache.addSession(data);
		}
		catch(DuplicateSessionException ex)
		{
			fail("Duplicate session in empty session cache.");
			return;
		}
		
		data = new PrincipalImpl(new IdentityDataImpl(), 360);
		data.setPrincipalAuthnIdentifier(this.principal2);
		data.setSAMLAuthnIdentifier(this.samlID2);
		data.setSessionID(this.sessionID2);
		try
		{
			this.cache.addSession(data);
		}
		catch(DuplicateSessionException ex)
		{
			fail("Duplicate session in empty session cache.");
			return;
		}
		
		Principal principal = this.cache.getSession(this.sessionID1);
		assertEquals("First added principal - Comparing session principal name", principal.getPrincipalAuthnIdentifier(),
				this.principal1);
		assertEquals("First added principal - Comparing session ID", principal.getSessionID(), this.sessionID1);
		assertEquals("First added principal - Comparing SAML ID", principal.getSAMLAuthnIdentifier(), this.samlID1);

		principal = this.cache.getSession(this.sessionID2);
		assertEquals("Second added principal - Comparing session principal name", principal.getPrincipalAuthnIdentifier(),
				this.principal2);
		assertEquals("Second added principal - Comparing session ID", principal.getSessionID(), this.sessionID2);
		assertEquals("Second added principal - Comparing SAML ID", principal.getSAMLAuthnIdentifier(), this.samlID2);
	}
	
	/**
	 * 
	 */
	@Test
	public final void testAddSession2()
	{
		this.logout = createMock(LogoutThreadPool.class);
		//expect(logout.getEndPoints(entityID)).andReturn(endpoints);
		//expect(logout.performSingleLogout((String)notNull(), (List<String>)notNull(), eq(entityID), anyBoolean())).andReturn(LogoutThreadPool.result.LogoutSuccessful).anyTimes();
		replay(this.logout);
		
		this.cache = new SessionCacheImpl(logout);

		Principal data = new PrincipalImpl(new IdentityDataImpl(), 360);
		boolean trapped = false;
		data.setPrincipalAuthnIdentifier(this.principal3);
		data.setSAMLAuthnIdentifier(this.samlID3);
		try
		{
			this.cache.addSession(data);
		}
		catch (IllegalArgumentException ex)
		{
			trapped = true;
		}
		catch(DuplicateSessionException ex)
		{
			fail("Duplicate session when session should have been rejected.");
			return;
		}
		
		assertTrue("Empty session ID is not rejected", trapped);

		data = new PrincipalImpl(new IdentityDataImpl(), 360);
		trapped = false;
		data.setSAMLAuthnIdentifier(this.samlID3);
		data.setSessionID(this.sessionID3);
		try
		{
			this.cache.addSession(data);
		}
		catch (IllegalArgumentException ex)
		{
			trapped = true;
		}
		catch(DuplicateSessionException ex)
		{
			fail("Duplicate session when session should have been rejected.");
			return;
		}

		assertTrue("Empty principal name is not rejected", trapped);
	}

	/**
	 * 
	 */
	@Test
	public final void testGetSessionBySamlID1()
	{
		this.logout = createMock(LogoutThreadPool.class);
		//expect(logout.getEndPoints(entityID)).andReturn(endpoints);
		//expect(logout.performSingleLogout((String)notNull(), (List<String>)notNull(), eq(entityID), anyBoolean())).andReturn(LogoutThreadPool.result.LogoutSuccessful).anyTimes();
		replay(this.logout);
		
		this.cache = new SessionCacheImpl(logout);

		Principal data = new PrincipalImpl(new IdentityDataImpl(), 360);
		data.setPrincipalAuthnIdentifier(this.principal1);
		data.setSAMLAuthnIdentifier(this.samlID1);
		data.setSessionID(this.sessionID1);
		
		try
		{
			this.cache.addSession(data);
			
			this.cache.updateSessionSAMLID(data);
		}
		catch(DuplicateSessionException ex)
		{
			fail("Duplicate session in empty session cache.");
			return;
		}

		Principal principal = this.cache.getSessionBySAMLID(this.samlID1);
		assertEquals("First added principal - Comparing session principal name", principal.getPrincipalAuthnIdentifier(),
				this.principal1);
		assertEquals("First added principal - Comparing session ID", principal.getSessionID(), this.sessionID1);
		assertEquals("First added principal - Comparing SAML ID", principal.getSAMLAuthnIdentifier(), this.samlID1);
	}

	/* Test invalid update for block coverage.
	 * 
	 */
	@Test
	public void testUpdateSessionID()
	{
		this.logout = createMock(LogoutThreadPool.class);
		//expect(logout.getEndPoints(entityID)).andReturn(endpoints);
		//expect(logout.performSingleLogout((String)notNull(), (List<String>)notNull(), eq(entityID), anyBoolean())).andReturn(LogoutThreadPool.result.LogoutSuccessful).anyTimes();
		replay(this.logout);
		
		this.cache = new SessionCacheImpl(logout);

		// attempt to update an invalid principal. A valid one is updated in the previous test.
		try
		{
			Principal invalidData = new PrincipalImpl(new IdentityDataImpl(), 360);
			invalidData.setPrincipalAuthnIdentifier("test");
			//invalidData.setSAMLAuthnIdentifier("_blahd76test");
			invalidData.setSessionID("90480f8d9");
			
			// first set some invalid data to set off an exception for block coverage
			this.cache.updateSessionSAMLID(invalidData);	
			
			fail("Illegal argument exception not thrown.");
			
		}
		catch(IllegalArgumentException e)
		{
			// we expect one of these
			//e.printStackTrace();
		}
		catch(DuplicateSessionException e)
		{
			fail("Unexpected exception thrown.");
		}
		
	}
	
	
	/**
	 * 
	 */
	@Test
	public final void testRemoveSession()
	{
		this.logout = createMock(LogoutThreadPool.class);
		//expect(logout.getEndPoints(entityID)).andReturn(endpoints);
		//expect(logout.performSingleLogout((String)notNull(), (List<String>)notNull(), eq(entityID), anyBoolean())).andReturn(LogoutThreadPool.result.LogoutSuccessful).anyTimes();
		replay(this.logout);
		
		this.cache = new SessionCacheImpl(logout);

		Principal data = new PrincipalImpl(new IdentityDataImpl(), 360);
		data.setPrincipalAuthnIdentifier(this.principal1);
		data.setSAMLAuthnIdentifier(this.samlID1);
		data.setSessionID(this.sessionID1);
		try
		{
			this.cache.addSession(data);
		}
		catch(DuplicateSessionException ex)
		{
			fail("Duplicate session in empty session cache.");
			return;
		}

		Principal principal = this.cache.getSession(this.sessionID1);
		assertEquals("Session added to cache", principal.getPrincipalAuthnIdentifier(), this.principal1);

		principal = this.cache.removeSession(this.sessionID1);
		assertEquals("Removed correct object", principal.getPrincipalAuthnIdentifier(), this.principal1);

		principal = this.cache.getSession(this.sessionID1);
		assertNull("Retrieving removed object returns null", principal);

		principal = this.cache.removeSession(this.sessionID2);
		assertNull("Removing non existent object returns null", principal);
	}
	
	
	@Test
	public void testCleanCache() throws Exception
	{
		String sessionID = "635472596wfd67d6";
		Principal data = new PrincipalImpl(10);
		data.setSessionID(sessionID);
		data.setPrincipalAuthnIdentifier("Test");
		
		this.logout = createMock(LogoutThreadPool.class);
		expect(this.logout.createLogoutTask((Principal)notNull(), eq(false)) ).andReturn("BlahTaskID").anyTimes();
		
		replay(this.logout);
		
		this.cache = new SessionCacheImpl(logout);
		this.cache.addSession(data);
		
		// prove its in there
		assertEquals(data, this.cache.getSession(sessionID));
				
		// we have to sleep for a touch, because calling getSession() updates the entry lastAccessed
		// timestamp. If you're running these tests on a slow box you might want to raise it a little
		Thread.sleep(100);
		
		// no getSize method .. setting to 0 should remove all entries
		this.cache.cleanCache(0);
		
		assertEquals(null, this.cache.getSession(sessionID));
	}
	
	@Test
	public void testValidSession() throws Exception
	{
		String sessionID = "635472596wfd67d6";
		Principal data = new PrincipalImpl(10);
		data.setSessionID(sessionID);
		data.setPrincipalAuthnIdentifier("Test");
		data.setAuthnTimestamp(System.currentTimeMillis());
		
		this.logout = createMock(LogoutThreadPool.class);
		//expect(logout.getEndPoints(entityID)).andReturn(endpoints);
		//expect(logout.performSingleLogout((String)notNull(), (List<String>)notNull(), eq(entityID), anyBoolean())).andReturn(LogoutThreadPool.result.LogoutSuccessful).anyTimes();
		replay(this.logout);
		
		this.cache = new SessionCacheImpl(logout);
		this.cache.addSession(data);
		
		// the session is valid is it exists in the cache
		assertTrue(!this.cache.validSession("notinthere"));
		
		assertTrue(this.cache.validSession(sessionID));
	}

}
