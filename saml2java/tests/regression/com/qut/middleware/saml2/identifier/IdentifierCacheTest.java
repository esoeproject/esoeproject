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
 * Creation Date: 15/12/2006
 * 
 * Purpose: tests identifer generation cache
 */
package com.qut.middleware.saml2.identifier;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.saml2.identifier.IdentifierCache;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.identifier.exception.IdentifierCollisionException;
import com.qut.middleware.saml2.identifier.impl.IdentifierCacheImpl;
import com.qut.middleware.saml2.identifier.impl.IdentifierGeneratorImpl;

public class IdentifierCacheTest
{
	private IdentifierGenerator generator;
	private IdentifierCache identifierCache;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		this.generator = new IdentifierGeneratorImpl(new IdentifierCacheImpl());
		this.identifierCache = new IdentifierCacheImpl();
	}

	/**
	 * Test method for {@link com.qut.middleware.saml2.identifier.IdentifierCache#registerIdentifier(java.lang.String)}.
	 */
	@Test
	public final void testRegisterIdentifier()
	{
		String sessionID = this.generator.generateSessionID();
		String sessionID2 = this.generator.generateSessionID();
		while (sessionID == sessionID2)
		{
			// I know this should never happen.. but you can never be too sure
			sessionID2 = this.generator.generateSessionID();
		}
		
		try
		{
			this.identifierCache.registerIdentifier(sessionID);
		}
		catch (IdentifierCollisionException e)
		{
			fail("Collision in empty cache");
		}
		
		try
		{
			this.identifierCache.registerIdentifier(sessionID2);
		}
		catch (IdentifierCollisionException e)
		{
			fail("Collision with different session ID");
		}
	}

	/**
	 * Test method for {@link com.qut.middleware.saml2.identifier.IdentifierCache#registerIdentifier(java.lang.String)}.
	 */
	@Test
	public final void testRegisterIdentifier2()
	{
		String sessionID = this.generator.generateSessionID();
		String sessionID2 = sessionID;
		
		try
		{
			this.identifierCache.registerIdentifier(sessionID);
		}
		catch (IdentifierCollisionException e)
		{
			fail("Collision in empty cache");
		}
		
		boolean caught = false;
		try
		{
			this.identifierCache.registerIdentifier(sessionID2);
		}
		catch (IdentifierCollisionException e)
		{
			caught = true;
		}
		
		assertTrue("Collision did not generate error condition", caught);
	}
	
	
	
	/**
	 * Test method for {@link com.qut.middleware.saml2.identifier.IdentifierCache#cleanCache(int)}.
	 */
	@Test
	public final void testCleanCache() throws Exception
	{
		String sessionID = this.generator.generateSessionID();
		String sessionID2 = this.generator.generateSessionID();
		
		String sessionID3 = this.generator.generateSessionID();
		String sessionID4 = this.generator.generateSessionID();
		
		try
		{
			this.identifierCache.registerIdentifier(sessionID);
			this.identifierCache.registerIdentifier(sessionID2);
	
			// sleep for 10 secs so the identifiers have different timestamps
			Thread.sleep(10000);
			
			this.identifierCache.registerIdentifier(sessionID3);
			this.identifierCache.registerIdentifier(sessionID4);
		}
		catch (IdentifierCollisionException e)
		{
			fail("Collision in empty cache");
		}
		
		// test clean of entries older than 5 seconds, which should be the first 2
		int numRemoved = this.identifierCache.cleanCache(5);
		
		assertEquals("Clean cache removed unexpected values", 2, numRemoved);
	
	}
}
