/* Copyright 2006, Queensland University of Technology
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
 * Creation Date: 10/09/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.esoe.sessions.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileReader;

import org.junit.Before;
import org.junit.Test;

import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.bean.IdentityAttribute;
import com.qut.middleware.esoe.sessions.bean.impl.IdentityAttributeImpl;
import com.qut.middleware.esoe.sessions.data.impl.SessionCacheDAOImpl;
import com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException;
import com.qut.middleware.esoe.sessions.impl.PrincipalImpl;

public class SessionCacheDAOImplTest
{
	private SessionCacheDAOImpl sessionCacheDAO;

	private String authenticationContext = "test-authn-context";

	private String attributeName = "uid";
	private String attributeValue = "username";

	private String invalidSessionID = "invalid-session";
	private String invalidSessionSAMLID = "invalid-saml-session";
	private String validSessionID = "valid-session";
	private String validSessionSAMLID = "valid-saml-session";

	@Before
	public void setUp() throws Exception
	{
		sessionCacheDAO = new SessionCacheDAOImpl();
		sessionCacheDAO.setSqlMapClient(SqlMapClientBuilder.buildSqlMapClient(new FileReader("tests/test-sqlmap-config.xml")));
		sessionCacheDAO.getSqlMapClient().update("truncateSessions");

		PrincipalImpl principal = new PrincipalImpl();
		principal.setSessionID(this.validSessionID);
		principal.setSAMLAuthnIdentifier(this.validSessionSAMLID);
		principal.setAuthenticationContextClass(this.authenticationContext);
		principal.setLastAccessed(System.currentTimeMillis());
		principal.setAuthnTimestamp(System.currentTimeMillis());
		IdentityAttribute identityAttribute = new IdentityAttributeImpl();
		identityAttribute.addValue(this.attributeValue);
		principal.getAttributes().put(this.attributeName, identityAttribute);

		this.sessionCacheDAO.addSession(principal);
	}

	/* public void testAddSession() throws Exception {}
	 * 
	 * This was already tested in setUp() .. if addSession is broken most tests will fail.
	 */

	@Test
	public void testValidSession1() throws Exception
	{
		assertFalse(this.sessionCacheDAO.validSession(this.invalidSessionID));
	}

	@Test
	public void testValidSession2() throws Exception
	{
		assertTrue(this.sessionCacheDAO.validSession(this.validSessionID));
	}

	@Test
	public void testGetSession1() throws Exception
	{
		Principal principal = this.sessionCacheDAO.getSession(this.validSessionID);
		assertEquals(this.validSessionID, principal.getSessionID());
		assertEquals(this.validSessionSAMLID, principal.getSAMLAuthnIdentifier());
		assertEquals(this.authenticationContext, principal.getAuthenticationContextClass());
		assertTrue(principal.getAttributes().containsKey(this.attributeName));
		assertEquals(this.attributeValue, principal.getAttributes().get(this.attributeName).getValues().get(0));
	}
	
	@Test
	public void testGetSession2() throws Exception
	{
		Principal principal = this.sessionCacheDAO.getSession(this.invalidSessionID);
		assertNull(principal);
	}

	
	@Test
	public void testRemoveSession1() throws Exception
	{
		assertTrue(this.sessionCacheDAO.deleteSession(this.validSessionID));
		assertFalse(this.sessionCacheDAO.validSession(this.validSessionID));
	}

	@Test
	public void testRemoveSession2() throws Exception
	{
		assertFalse(this.sessionCacheDAO.deleteSession(this.invalidSessionID));
	}

	@Test
	public void testGetSize() throws Exception
	{
		assertEquals(1, this.sessionCacheDAO.getSize());
	}

	@Test
	public void testGetSessionBySAMLID1() throws Exception
	{
		Principal principal = this.sessionCacheDAO.getSessionBySAMLID(this.validSessionSAMLID);
		assertEquals(this.validSessionID, principal.getSessionID());
		assertEquals(this.validSessionSAMLID, principal.getSAMLAuthnIdentifier());
		assertEquals(this.authenticationContext, principal.getAuthenticationContextClass());
		assertTrue(principal.getAttributes().containsKey(this.attributeName));
		assertEquals(this.attributeValue, principal.getAttributes().get(this.attributeName).getValues().get(0));
	}

	@Test
	public void testGetSessionBySAMLID2() throws Exception
	{
		Principal principal = this.sessionCacheDAO.getSessionBySAMLID(this.invalidSessionSAMLID);
		assertNull(principal);
	}

	@Test
	public void testGetSessionBySAMLID3() throws Exception
	{
		String newSAMLID = "new-saml-id";
		
		PrincipalImpl principal = new PrincipalImpl();
		principal.setSessionID(this.validSessionID);
		principal.setSAMLAuthnIdentifier(newSAMLID);
		
		this.sessionCacheDAO.updateSessionSAMLID(principal);
		
		Principal principal2 = this.sessionCacheDAO.getSessionBySAMLID(this.validSessionSAMLID);
		assertNull(principal2);
		
		principal2 = this.sessionCacheDAO.getSessionBySAMLID(newSAMLID);
		assertEquals(this.validSessionID, principal2.getSessionID());
		assertEquals(newSAMLID, principal2.getSAMLAuthnIdentifier());
		assertEquals(this.authenticationContext, principal2.getAuthenticationContextClass());
		assertTrue(principal2.getAttributes().containsKey(this.attributeName));
		assertEquals(this.attributeValue, principal2.getAttributes().get(this.attributeName).getValues().get(0));
	}

	public void testCleanCache() throws Exception
	{
	}
	
	@Test
	public void testAddDescriptor() throws Exception
	{
		String entityID = "urn:test:esoe:entity";
		
		Principal principal = this.sessionCacheDAO.getSession(this.validSessionID);

		this.sessionCacheDAO.addDescriptor(principal, entityID);
		
		
		principal = this.sessionCacheDAO.getSession(this.validSessionID);
		
		assertTrue(principal.getActiveEntityList().contains(entityID));
	}
	
	@Test
	public void testAddDescriptorSessionIdentifier() throws Exception
	{
		String entityID = "urn:test:esoe:entity";
		String entitySessionID = "entity-session-" + entityID;
		
		Principal principal = this.sessionCacheDAO.getSession(this.validSessionID);

		this.sessionCacheDAO.addDescriptor(principal, entityID);
		this.sessionCacheDAO.addDescriptorSessionIdentifier(principal, entityID, entitySessionID);
		
		principal = this.sessionCacheDAO.getSession(this.validSessionID);
		
		assertTrue(principal.getActiveEntityList().contains(entityID));
		assertTrue(principal.getActiveEntitySessionIndices(entityID).contains(entitySessionID));
	}
	
	@Test
	public void testUpdatePrincipalAttributes1() throws Exception
	{
		Principal principal = this.sessionCacheDAO.getSession(this.validSessionID);
		
		String newAttributeValue = "username2";
		principal.getAttributes().get(this.attributeName).addValue(newAttributeValue);
		this.sessionCacheDAO.updatePrincipalAttributes(principal);
		
		principal = this.sessionCacheDAO.getSession(this.validSessionID);
		assertTrue(principal.getAttributes().get(this.attributeName).getValues().contains(this.attributeValue));
		assertTrue(principal.getAttributes().get(this.attributeName).getValues().contains(newAttributeValue));
	}
	
	@Test(expected = InvalidSessionIdentifierException.class)
	public void testUpdatePrincipalAttributes2() throws Exception
	{
		Principal principal = this.sessionCacheDAO.getSession(this.validSessionID);
		
		String newAttributeValue = "username2";
		principal.getAttributes().get(this.attributeName).addValue(newAttributeValue);
		this.sessionCacheDAO.updatePrincipalAttributes(principal);
		
		principal = this.sessionCacheDAO.getSession(this.validSessionID);
		assertTrue(principal.getAttributes().get(this.attributeName).getValues().contains(this.attributeValue));
		assertTrue(principal.getAttributes().get(this.attributeName).getValues().contains(newAttributeValue));
	}

}
