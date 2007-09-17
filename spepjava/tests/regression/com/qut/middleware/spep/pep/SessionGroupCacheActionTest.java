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
 * Creation Date: 13/12/2006
 * 
 * Purpose: Unit tests for SessionGroupCache implementation
 */
package com.qut.middleware.spep.pep;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.spep.pep.PolicyEnforcementProcessor.decision;
import com.qut.middleware.spep.pep.impl.SessionGroupCacheImpl;
import com.qut.middleware.spep.sessions.PrincipalSession;
import com.qut.middleware.spep.sessions.impl.PrincipalSessionImpl;;

/** */
public class SessionGroupCacheActionTest
{

	private SessionGroupCache sessionGroupCache;
	private PrincipalSession principalSession;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
	}

	/**
	 * Test method for {@link com.qut.middleware.spep.pep.SessionGroupCache#makeCachedAuthzDecision(com.qut.middleware.spep.sessions.PrincipalSession, java.lang.String)}.
	 */
	@Test
	public void testMakeCachedAuthzDecision1a()
	{
		this.sessionGroupCache = new SessionGroupCacheImpl(decision.deny);
		this.principalSession = createMock(PrincipalSession.class);

		String groupTarget1 = "/.*.jsp";
		List<String> authzTargets1 = new Vector<String>();
		authzTargets1.add("/admin/.*.jsp");
		
		String groupTarget2 = "/admin/.*";
		List<String> authzTargets2 = new Vector<String>();
		authzTargets2.add("/admin/secure/.*");
		
		String groupTarget3 = "/admin/secure/.*";
		List<String> authzTargets3 = new Vector<String>();
		authzTargets3.add(".*/secure/.*.gif");
		
		Map<String,List<String>> groupTargetMap = new HashMap<String, List<String>>();
		groupTargetMap.put(groupTarget1, authzTargets1);
		groupTargetMap.put(groupTarget2, authzTargets2);
		groupTargetMap.put(groupTarget3, authzTargets3);
		this.sessionGroupCache.clearCache(groupTargetMap);
		
		String action_read = "read";
		String action_write = "write";
		
		this.sessionGroupCache.updateCache(principalSession, groupTarget1, authzTargets1, action_read, decision.permit);
		this.sessionGroupCache.updateCache(principalSession, groupTarget1, authzTargets1, action_write, decision.deny);
		this.sessionGroupCache.updateCache(principalSession, groupTarget2, authzTargets2, null, decision.permit);
		
		
		String resource1 = "/somepage.jsp";
		decision decision1 = decision.deny;
		String resource2 = "/admin/somepage.jsp";
		decision decision2 = decision.permit;
		String action2 = action_read;
		String resource3 = "/admin/secure/somepage.jsp";
		decision decision3 = decision.permit;
		String action3 = action_read;
		String resource4 = "/admin/secure/icon.gif";
		decision decision4 = decision.notcached;
		String resource5 = "/admin/somepage.jsp";
		decision decision5 = decision.deny;
		String action5 = action_write;
		
		
		assertEquals("Decision 1 was incorrect", decision1, this.sessionGroupCache.makeCachedAuthzDecision(this.principalSession, resource1));
		assertEquals("Decision 2 was incorrect", decision2, this.sessionGroupCache.makeCachedAuthzDecision(this.principalSession, resource2, action2));
		assertEquals("Decision 3 was incorrect", decision3, this.sessionGroupCache.makeCachedAuthzDecision(this.principalSession, resource3, action3));
		assertEquals("Decision 4 was incorrect", decision4, this.sessionGroupCache.makeCachedAuthzDecision(this.principalSession, resource4));
		assertEquals("Decision 2 was incorrect", decision5, this.sessionGroupCache.makeCachedAuthzDecision(this.principalSession, resource5, action5));
	}

	/**
	 * Test method for {@link com.qut.middleware.spep.pep.SessionGroupCache#makeCachedAuthzDecision(com.qut.middleware.spep.sessions.PrincipalSession, java.lang.String)}.
	 */
	@Test
	public void testMakeCachedAuthzDecision1b()
	{
		this.sessionGroupCache = new SessionGroupCacheImpl(decision.permit);
		this.principalSession = createMock(PrincipalSession.class);

		String groupTarget1 = "/.*.jsp";
		List<String> authzTargets1 = new Vector<String>();
		authzTargets1.add("/admin/.*.jsp");
		
		String groupTarget2 = "/admin/.*";
		List<String> authzTargets2 = new Vector<String>();
		authzTargets2.add("/admin/secure/.*");
		
		String groupTarget3 = "/admin/secure/.*";
		List<String> authzTargets3 = new Vector<String>();
		authzTargets3.add(".*/secure/.*.gif");

		Map<String,List<String>> groupTargetMap = new HashMap<String, List<String>>();
		groupTargetMap.put(groupTarget1, authzTargets1);
		groupTargetMap.put(groupTarget2, authzTargets2);
		groupTargetMap.put(groupTarget3, authzTargets3);
		this.sessionGroupCache.clearCache(groupTargetMap);
		
		String action_read = "read";
		String action_write = "write";

		this.sessionGroupCache.updateCache(principalSession, groupTarget1, authzTargets1, null, decision.permit);
		this.sessionGroupCache.updateCache(principalSession, groupTarget2, authzTargets2, action_read, decision.permit);
		this.sessionGroupCache.updateCache(principalSession, groupTarget2, authzTargets2, action_write, decision.deny);
		
		
		String resource1 = "/somepage.jsp";
		decision decision1 = decision.permit;
		String resource2 = "/admin/somepage.jsp";
		decision decision2 = decision.permit;
		String resource3 = "/admin/secure/somepage.jsp";
		decision decision3 = decision.deny;
		String action3 = action_write;
		String resource4 = "/admin/secure/icon.gif";
		decision decision4 = decision.notcached;
		String action4 = action_read;
		String resource5 = "/admin/secure/icon.gif";
		decision decision5 = decision.deny;
		
		
		assertEquals("Decision 1 was incorrect", decision1, this.sessionGroupCache.makeCachedAuthzDecision(this.principalSession, resource1));
		assertEquals("Decision 2 was incorrect", decision2, this.sessionGroupCache.makeCachedAuthzDecision(this.principalSession, resource2));
		assertEquals("Decision 3 was incorrect", decision3, this.sessionGroupCache.makeCachedAuthzDecision(this.principalSession, resource3, action3));
		assertEquals("Decision 4 was incorrect", decision4, this.sessionGroupCache.makeCachedAuthzDecision(this.principalSession, resource4, action4));
		assertEquals("Decision 5 was incorrect", decision5, this.sessionGroupCache.makeCachedAuthzDecision(this.principalSession, resource5));
	}

	/**
	 * Test method for {@link com.qut.middleware.spep.pep.SessionGroupCache#makeCachedAuthzDecision(com.qut.middleware.spep.sessions.PrincipalSession, java.lang.String)}.
	 */
	@Test
	public void testMakeCachedAuthzDecision2a()
	{
		this.sessionGroupCache = new SessionGroupCacheImpl(decision.deny);
		this.principalSession = createMock(PrincipalSession.class);

		String groupTarget1 = "/.*.jsp";
		List<String> authzTargets1 = new Vector<String>();
		authzTargets1.add("/admin/.*.jsp");
		
		String groupTarget2 = "/admin/.*";
		List<String> authzTargets2 = new Vector<String>();
		authzTargets2.add("/admin/secure/.*");
		
		String groupTarget3 = "/admin/secure/.*";
		List<String> authzTargets3 = new Vector<String>();
		authzTargets3.add(".*/secure/.*.gif");
		
		Map<String,List<String>> groupTargetMap = new HashMap<String, List<String>>();
		groupTargetMap.put(groupTarget1, authzTargets1);
		groupTargetMap.put(groupTarget2, authzTargets2);
		groupTargetMap.put(groupTarget3, authzTargets3);
		this.sessionGroupCache.clearCache(groupTargetMap);
		
		String action_read = "read";
		String action_write = "write";
		String action_admin = "admin";
		
		this.sessionGroupCache.updateCache(principalSession, groupTarget1, authzTargets1, action_write, decision.deny);
		this.sessionGroupCache.updateCache(principalSession, groupTarget2, authzTargets2, action_read, decision.permit);
		this.sessionGroupCache.updateCache(principalSession, groupTarget3, authzTargets3, action_read, decision.permit);

		String resource1 = "/somepage.jsp";
		decision decision1 = decision.deny;
		String action1 = action_write;
		String resource2 = "/admin/somepage.jsp";
		decision decision2 = decision.deny;
		String action2 = action_admin;
		String resource3 = "/admin/secure/somepage.jsp";
		decision decision3 = decision.deny;
		String action3 = action_write;
		String resource4 = "/admin/secure/icon.gif";
		decision decision4 = decision.deny;
		String action4 = action_admin;
		
		
		assertEquals("Decision 1 was incorrect", decision1, this.sessionGroupCache.makeCachedAuthzDecision(this.principalSession, resource1, action1));
		assertEquals("Decision 2 was incorrect", decision2, this.sessionGroupCache.makeCachedAuthzDecision(this.principalSession, resource2, action2));
		assertEquals("Decision 3 was incorrect", decision3, this.sessionGroupCache.makeCachedAuthzDecision(this.principalSession, resource3, action3));
		assertEquals("Decision 4 was incorrect", decision4, this.sessionGroupCache.makeCachedAuthzDecision(this.principalSession, resource4, action4));
	}

	/**
	 * Test method for {@link com.qut.middleware.spep.pep.SessionGroupCache#makeCachedAuthzDecision(com.qut.middleware.spep.sessions.PrincipalSession, java.lang.String)}.
	 */
	@Test
	public void testMakeCachedAuthzDecision2b()
	{
		this.sessionGroupCache = new SessionGroupCacheImpl(decision.permit);
		this.principalSession = createMock(PrincipalSession.class);

		String groupTarget1 = "/.*.jsp";
		List<String> authzTargets1 = new Vector<String>();
		authzTargets1.add("/admin/.*.jsp");
		
		String groupTarget2 = "/admin/.*";
		List<String> authzTargets2 = new Vector<String>();
		authzTargets2.add("/admin/secure/.*");
		
		String groupTarget3 = "/admin/secure/.*";
		List<String> authzTargets3 = new Vector<String>();
		authzTargets3.add(".*/secure/.*.gif");
		
		Map<String,List<String>> groupTargetMap = new HashMap<String, List<String>>();
		groupTargetMap.put(groupTarget1, authzTargets1);
		groupTargetMap.put(groupTarget2, authzTargets2);
		groupTargetMap.put(groupTarget3, authzTargets3);
		this.sessionGroupCache.clearCache(groupTargetMap);
		
		String action_read = "read";
		String action_write = "write";
		String action_admin = "admin";
		
		this.sessionGroupCache.updateCache(principalSession, groupTarget1, authzTargets1, null, decision.deny);
		this.sessionGroupCache.updateCache(principalSession, groupTarget2, authzTargets2, action_write, decision.permit);
		this.sessionGroupCache.updateCache(principalSession, groupTarget3, authzTargets3, null, decision.permit);
		
		String resource1 = "/somepage.jsp";
		decision decision1 = decision.permit;
		String action1 = action_write;
		String resource2 = "/admin/somepage.jsp";
		decision decision2 = decision.deny;
		String action2 = action_admin;
		String resource3 = "/admin/secure/somepage.jsp";
		decision decision3 = decision.deny;
		String action3 = action_read;
		String resource4 = "/admin/secure/icon.gif";
		decision decision4 = decision.permit;
		String action4 = action_write;
		
		
		assertEquals("Decision 1 was incorrect", decision1, this.sessionGroupCache.makeCachedAuthzDecision(this.principalSession, resource1, action1));
		assertEquals("Decision 2 was incorrect", decision2, this.sessionGroupCache.makeCachedAuthzDecision(this.principalSession, resource2, action2));
		assertEquals("Decision 3 was incorrect", decision3, this.sessionGroupCache.makeCachedAuthzDecision(this.principalSession, resource3, action3));
		assertEquals("Decision 4 was incorrect", decision4, this.sessionGroupCache.makeCachedAuthzDecision(this.principalSession, resource4, action4));
	}
}
