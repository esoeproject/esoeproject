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
 * Author:
 * Creation Date:
 * 
 * Purpose:
 */
package com.qut.middleware.saml2.identifier;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.saml2.identifier.IdentifierCache;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.identifier.exception.IdentifierCollisionException;
import com.qut.middleware.saml2.identifier.impl.IdentifierCacheImpl;
import com.qut.middleware.saml2.identifier.impl.IdentifierGeneratorImpl;

/**
 * @author Shaun
 *
 */
public class IdentifierCacheCleanupTest
{
	private IdentifierGenerator generator;
	private IdentifierCache identifierCache;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		this.identifierCache = new IdentifierCacheImpl();
		this.generator = new IdentifierGeneratorImpl(identifierCache);
	}

	/**
	 * Test method for {@link com.qut.middleware.saml2.identifier.IdentifierCache#registerIdentifier(java.lang.String)}.
	 */
	@Test
	public final void testRegisterIdentifier()
	{
		String sessionID = this.generator.generateSessionID();
		String sessionID2 = sessionID;
		
		if(!this.identifierCache.containsIdentifier(sessionID))
		{
			fail("Cache was not populated with identifier by generator");
		}
		
		try
		{
			Thread.sleep(2500); // Sleep to allow cache to be cleaned up
			
			this.identifierCache.cleanCache(1);
			
		}
		catch (InterruptedException e)
		{
			fail("Interrupted while sleeping");
		}
		
		
		try
		{
			this.identifierCache.registerIdentifier(sessionID2);
		}
		catch (IdentifierCollisionException e)
		{
			fail("Collision although cache should have been cleaned up");
		}
	}

}
