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
 * Creation Date: 13/11/2006
 * 
 * Purpose: Tests the session cache.
 */
package com.qut.middleware.spep.sessions;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.spep.sessions.impl.SessionCacheImpl;

/** */
@SuppressWarnings({"nls"})
public class SessionCacheTest
{
	private SessionCache sessionCache;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		this.sessionCache = new SessionCacheImpl(1, 1);
	}

	/**
	 * Test to ensure that principal session can be correctly stored
	 * Test method for {@link com.qut.middleware.spep.sessions.SessionCache#putPrincipalSession(java.lang.String, com.qut.middleware.spep.sessions.PrincipalSession)}.
	 */
	@Test
	public void testPutPrincipalSession()
	{
		PrincipalSession principalSession = createMock(PrincipalSession.class);
		String sessionID = "59872938759238745982374958273498572345";
		String samlID = "_u5uaq0e9w5j0q9235i09qi23059iq0a9wi5q9235i-q0w95j0q923j509iq03925i0q923jk5popaosdt";
		
		expect(principalSession.getEsoeSessionID()).andReturn(samlID).anyTimes();

		expect(principalSession.getSessionNotOnOrAfter()).andReturn(new Date(System.currentTimeMillis() + 30000)).anyTimes();

		replay(principalSession);
		
		assertNull("Session returned from empty cache", this.sessionCache.getPrincipalSession(sessionID));
		
		this.sessionCache.putPrincipalSession(sessionID, principalSession);

		assertSame("No session returned", principalSession, this.sessionCache.getPrincipalSession(sessionID));
		assertSame("No session returned", principalSession, this.sessionCache.getPrincipalSessionByEsoeSessionID(samlID));
		
		verify(principalSession);
	}

	
	/**
	 * Test to ensure that an incomplete (invalid) principal session is NOT stored
	 * Test method for {@link com.qut.middleware.spep.sessions.SessionCache#putPrincipalSession(java.lang.String, com.qut.middleware.spep.sessions.PrincipalSession)}.
	 */
	@Test
	public void testPutPrincipalSessiona()
	{
		PrincipalSession principalSession = createMock(PrincipalSession.class);
		String sessionID = "59872938759238745982374958273498572345";
		String samlID = "_u5uaq0e9w5j0q9235i09qi23059iq0a9wi5q9235i-q0w95j0q923j509iq03925i0q923jk5popaosdt";
		
		expect(principalSession.getEsoeSessionID()).andReturn(null).anyTimes();

		expect(principalSession.getSessionNotOnOrAfter()).andReturn(new Date(System.currentTimeMillis() + 30000)).anyTimes();

		replay(principalSession);
		
		assertNull("Session returned from empty cache", this.sessionCache.getPrincipalSession(sessionID));
		
		this.sessionCache.putPrincipalSession(sessionID, principalSession);

		assertNull("No session returned", this.sessionCache.getPrincipalSession(sessionID));
		assertNull("No session returned", this.sessionCache.getPrincipalSessionByEsoeSessionID(samlID));
		
		verify(principalSession);
	}
	
	/**
	 * Asserts that a principal who has expired, while initially inserted in the cache (at some time in the past) is never returned to caller
	 * Test method for {@link com.qut.middleware.spep.sessions.SessionCache#putPrincipalSession(java.lang.String, com.qut.middleware.spep.sessions.PrincipalSession)}.
	 */
	@Test
	public void testPutPrincipalSession1()
	{
		PrincipalSession principalSession = createMock(PrincipalSession.class);
		String sessionID = "59872938759238745982374958273498572345";
		String samlID = "_u5uaq0e9w5j0q9235i09qi23059iq0a9wi5q9235i-q0w95j0q923j509iq03925i0q923jk5popaosdt";
		List<String> sessionIDList = new ArrayList<String>();
		sessionIDList.add(sessionID);
		
		expect(principalSession.getEsoeSessionID()).andReturn(samlID).anyTimes();
		expect(principalSession.getSessionNotOnOrAfter()).andReturn(new Date(System.currentTimeMillis() - 1000)).anyTimes();
		expect(principalSession.getSessionIDList()).andReturn(sessionIDList);
		replay(principalSession);
		
		/* Check to insure its not returned by local SessionID */
		assertNull("Session returned from empty cache", this.sessionCache.getPrincipalSession(sessionID));
		this.sessionCache.putPrincipalSession(sessionID, principalSession);
		assertNull("Session returned though it has expired.", this.sessionCache.getPrincipalSession(sessionID));
		assertNull("Session returned though it has expired.", this.sessionCache.getPrincipalSessionByEsoeSessionID(samlID));
		
		verify(principalSession);
	}
	
	/**
	 * Asserts that a principal who has expired, while initially inserted in the cache (at some time in the past) is never returned to caller
	 * Test method for {@link com.qut.middleware.spep.sessions.SessionCache#putPrincipalSession(java.lang.String, com.qut.middleware.spep.sessions.PrincipalSession)}.
	 */
	@Test
	public void testPutPrincipalSession1a()
	{
		PrincipalSession principalSession = createMock(PrincipalSession.class);
		String sessionID = "59872938759238745982374958273498572345";
		String samlID = "_u5uaq0e9w5j0q9235i09qi23059iq0a9wi5q9235i-q0w95j0q923j509iq03925i0q923jk5popaosdt";
		List<String> sessionIDList = new ArrayList<String>();
		sessionIDList.add(sessionID);
		
		expect(principalSession.getEsoeSessionID()).andReturn(samlID).anyTimes();
		expect(principalSession.getSessionNotOnOrAfter()).andReturn(new Date(System.currentTimeMillis() - 1)).anyTimes();
		expect(principalSession.getSessionIDList()).andReturn(sessionIDList);
		replay(principalSession);
			
		/* Check to ensure its not returned by ESOE SessionID */
		assertNull("Session returned from empty cache", this.sessionCache.getPrincipalSession(sessionID));
		this.sessionCache.putPrincipalSession(sessionID, principalSession);
		assertNull("Session returned though it has expired.", this.sessionCache.getPrincipalSessionByEsoeSessionID(samlID));
		assertNull("Session returned though it has expired.", this.sessionCache.getPrincipalSession(sessionID));
		
		verify(principalSession);
	}

	/**
	 * Tests to ensure successful creation of unauthenticated session
	 * Test method for {@link com.qut.middleware.spep.sessions.SessionCache#putUnauthenticatedSession(java.lang.String, com.qut.middleware.spep.sessions.UnauthenticatedSession)}.
	 */
	@Test
	public void testPutUnauthenticatedSession1()
	{
		UnauthenticatedSession PrincipalSession = createMock(UnauthenticatedSession.class);
		String sessionID = "59872938759238745982374958273498572345";

		PrincipalSession.updateTime();
		expectLastCall().anyTimes();
		expect(PrincipalSession.getIdleTime()).andReturn(new Long(0)).anyTimes();

		replay(PrincipalSession);
		
		assertNull("Session returned from empty cache", this.sessionCache.getUnauthenticatedSession(sessionID));
		
		this.sessionCache.putUnauthenticatedSession(sessionID, PrincipalSession);

		assertSame("No session returned", PrincipalSession, this.sessionCache.getUnauthenticatedSession(sessionID));
		
		verify(PrincipalSession);
	}

	/**
	 * Tests to ensure correct termination of unauthentication session
	 * Test method for {@link com.qut.middleware.spep.sessions.SessionCache#terminateUnauthenticatedSession(java.lang.String)}.
	 */
	@Test
	public void testTerminateUnauthenticatedSession1()
	{
		UnauthenticatedSession PrincipalSession = createMock(UnauthenticatedSession.class);
		String sessionID = "59872938759238745982374958273498572345";
		
		PrincipalSession.updateTime();
		expectLastCall().anyTimes();

		replay(PrincipalSession);
		
		assertNull("Session returned from empty cache", this.sessionCache.getUnauthenticatedSession(sessionID));
		
		this.sessionCache.putUnauthenticatedSession(sessionID, PrincipalSession);

		assertSame("No session returned", PrincipalSession, this.sessionCache.getUnauthenticatedSession(sessionID));
		
		this.sessionCache.terminateUnauthenticatedSession(sessionID);
		
		assertNull("Session returned after being removed", this.sessionCache.getUnauthenticatedSession(sessionID));
		
		verify(PrincipalSession);
	}

	/**
	 * Test to ensure that a single session is terminated with others left present when requested
	 * Test method for {@link com.qut.middleware.spep.sessions.SessionCache#terminatePrincipalSession(java.lang.String)}.
	 */
	@Test
	public void testTerminateIndividualPrincipalSession1()
	{
		PrincipalSession principalSession = createMock(PrincipalSession.class);
		String sessionID = "59872938759238745982374958273498572345";
		String sessionID2 = "259872938759238745982374958273498572345";
		String esoeSessionIndex1 = "123456789";
		String esoeSessionIndex2 = "012345678";
		String samlID = "_9509280t9q0we9i0q9i3209i029ti09q2ji3t-q-9jt09j230t9qi2039iq09234";
		
		Map<String, String> esoeSessionIndex = new HashMap<String, String> ();
		esoeSessionIndex.put(esoeSessionIndex1, sessionID);
		esoeSessionIndex.put(esoeSessionIndex2, sessionID2);
		
		expect(principalSession.getEsoeSessionID()).andReturn(samlID).anyTimes();
		expect(principalSession.getEsoeSessionIndex()).andReturn(esoeSessionIndex).times(3);
		expect(principalSession.getSessionNotOnOrAfter()).andReturn(new Date(System.currentTimeMillis() + 30000)).anyTimes();
		replay(principalSession);
		
		assertNull("Session returned from empty cache", this.sessionCache.getPrincipalSession(sessionID));
		
		this.sessionCache.putPrincipalSession(sessionID, principalSession);
		this.sessionCache.putPrincipalSession(sessionID2, principalSession);

		assertSame("Incorrect session returned", principalSession, this.sessionCache.getPrincipalSession(sessionID));
		assertSame("Incorrect session returned", principalSession, this.sessionCache.getPrincipalSession(sessionID2));
		assertSame("Incorrect session returned", principalSession, this.sessionCache.getPrincipalSessionByEsoeSessionID(samlID));
		
		this.sessionCache.terminateIndividualPrincipalSession(principalSession, esoeSessionIndex1);
		
		/* Ensure that only the reference to the first SessionIndex / sessionID are removed */
		assertNull("Session returned after being removed", this.sessionCache.getPrincipalSession(sessionID));
		assertNotNull("Session returned after being removed", this.sessionCache.getPrincipalSessionByEsoeSessionID(samlID));
		assertNotNull("Session returned after being removed", this.sessionCache.getPrincipalSession(sessionID2));
		
		verify(principalSession);
	}
	
	/**
	 * Test to ensure that if there is only a single session remaining that the overall principal object is also terminated
	 * Test method for {@link com.qut.middleware.spep.sessions.SessionCache#terminatePrincipalSession(java.lang.String)}.
	 */
	@Test
	public void testTerminateIndividualPrincipalSession()
	{
		PrincipalSession principalSession = createMock(PrincipalSession.class);
		String sessionID = "59872938759238745982374958273498572345";
		String esoeSessionIndex1 = "123456789";
		String samlID = "_9509280t9q0we9i0q9i3209i029ti09q2ji3t-q-9jt09j230t9qi2039iq09234";
		
		Map<String, String> esoeSessionIndex = new HashMap<String, String> ();
		esoeSessionIndex.put(esoeSessionIndex1, sessionID);
		
		expect(principalSession.getEsoeSessionID()).andReturn(samlID).anyTimes();
		expect(principalSession.getEsoeSessionIndex()).andReturn(esoeSessionIndex).times(3);
		expect(principalSession.getSessionNotOnOrAfter()).andReturn(new Date(System.currentTimeMillis() + 30000)).anyTimes();
		replay(principalSession);
		
		assertNull("Session returned from empty cache", this.sessionCache.getPrincipalSession(sessionID));
		
		this.sessionCache.putPrincipalSession(sessionID, principalSession);

		assertSame("Incorrect session returned", principalSession, this.sessionCache.getPrincipalSession(sessionID));
		assertSame("Incorrect session returned", principalSession, this.sessionCache.getPrincipalSessionByEsoeSessionID(samlID));
		
		this.sessionCache.terminateIndividualPrincipalSession(principalSession, esoeSessionIndex1);
		
		/* Ensure that only the reference to the first SessionIndex / sessionID are removed */
		assertNull("Session returned after being removed", this.sessionCache.getPrincipalSession(sessionID));
		assertNull("Session returned after being removed", this.sessionCache.getPrincipalSessionByEsoeSessionID(samlID));
		
		verify(principalSession);
	}
}
