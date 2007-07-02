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
 * Author: Bradley Beddoes 
 * Creation Date: 03/10/2006 
 * 
 * Purpose: JUnit tests for IdentifierGeneratorImpl
 */
package com.qut.middleware.saml2.identifier;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.saml2.identifier.exception.IdentifierCollisionException;
import com.qut.middleware.saml2.identifier.exception.IdentifierGeneratorException;
import com.qut.middleware.saml2.identifier.impl.IdentifierCacheImpl;
import com.qut.middleware.saml2.identifier.impl.IdentifierGeneratorImpl;

@SuppressWarnings(value = { "unqualified-field-access", "nls" })
public class IdentifierGeneratorTest
{
	private IdentifierCache cache;
	private IdentifierGenerator ident;
	private final int ID_NUM = 100;

	@Before
	public void runBeforeEachTest()
	{
		this.cache = createMock(IdentifierCache.class);
		this.ident = new IdentifierGeneratorImpl(this.cache);
	}

	/* Tests to ensure null constructor param is correctly caught */
	@Test(expected = IllegalArgumentException.class)
	public void testIdentifierGenerator()
	{
		this.ident = new IdentifierGeneratorImpl(null);
	}

	@Test
	public void testGenerateSAMLAuthnIDUnique() throws IdentifierCollisionException
	{
		this.ident = new IdentifierGeneratorImpl(new IdentifierCacheImpl());

		Set<String> idSet = new HashSet<String>();
		String id;

		for (int i = 0; i < this.ID_NUM; i++)
		{
			id = this.ident.generateSAMLAuthnID();
			idSet.add(id);
		}

		assertEquals("Identifiers should be unique", this.ID_NUM, idSet.size()); //$NON-NLS-1$
	}

	@Test
	public void testGenerateSAMLAuthnID() throws IdentifierCollisionException
	{
		cache.registerIdentifier((String) notNull());
		replay(cache);

		String id;
		id = this.ident.generateSAMLAuthnID();
		assertTrue("AuthnID should be of matching format", id.matches(".*-.*")); //$NON-NLS-1$ //$NON-NLS-2$

		verify(cache);
	}

	@Test
	public void testGenerateSAMLID() throws IdentifierCollisionException
	{
		cache.registerIdentifier((String) notNull());
		replay(cache);

		String id;
		id = this.ident.generateSAMLID();
		assertTrue("SAMLID should be of matching format", id.matches("_.*-.*")); //$NON-NLS-1$ //$NON-NLS-2$

		verify(cache);
	}

	@Test
	public void testGenerateSAMLSessionID() throws IdentifierCollisionException
	{
		cache.registerIdentifier((String) notNull());
		replay(cache);

		String id;
		id = this.ident.generateSAMLSessionID();
		assertFalse("SessionID should be of matching format", id.matches(".*-.*")); //$NON-NLS-1$ //$NON-NLS-2$

		verify(cache);
	}

	@Test
	public void testGenerateSessionID() throws IdentifierCollisionException
	{
		cache.registerIdentifier((String) notNull());
		replay(cache);

		String id;
		id = this.ident.generateSessionID();
		assertTrue("SessionID should be of matching format", id.matches(".*-.*-.*")); //$NON-NLS-1$ //$NON-NLS-2$

		verify(cache);
	}

	@Test
	public void testGenerateXMLKeyName() throws IdentifierCollisionException
	{
		String id;
		id = this.ident.generateXMLKeyName();
		assertTrue("Ensures XML key name is of correct length", id.length() > 0); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/* Test to ensure correct operation on identifier collision */
	@Test(expected = IdentifierGeneratorException.class)
	public void testGenerateSAMLAuthnIDException()
	{
		try
		{
			cache.registerIdentifier((String) notNull());
			expectLastCall().andThrow(new IdentifierCollisionException("test exception"));
			replay(cache);
		}
		catch (IdentifierCollisionException e)
		{
			fail("This exception MUST be caught by the library");
		}

		String id;
		id = this.ident.generateSAMLAuthnID();
		assertTrue("AuthnID should be of matching format", id.matches(".*-.*")); //$NON-NLS-1$ //$NON-NLS-2$

		verify(cache);
	}

	@Test(expected = IdentifierGeneratorException.class)
	public void testGenerateSAMLIDException()
	{
		try
		{
			cache.registerIdentifier((String) notNull());
			expectLastCall().andThrow(new IdentifierCollisionException("test exception"));
			replay(cache);
		}
		catch (IdentifierCollisionException e)
		{
			fail("This exception MUST be caught by the library");
		}

		String id;
		id = this.ident.generateSAMLID();
		assertTrue("SAMLID should be of matching format", id.matches("_.*-.*")); //$NON-NLS-1$ //$NON-NLS-2$

		verify(cache);
	}

	@Test(expected = IdentifierGeneratorException.class)
	public void testGenerateSAMLSessionIDException()
	{
		try
		{
			cache.registerIdentifier((String) notNull());
			expectLastCall().andThrow(new IdentifierCollisionException("test exception"));
			replay(cache);
		}
		catch (IdentifierCollisionException e)
		{
			fail("This exception MUST be caught by the library");
		}

		String id;
		id = this.ident.generateSAMLSessionID();
		assertFalse("SessionID should be of matching format", id.matches(".*-.*")); //$NON-NLS-1$ //$NON-NLS-2$

		verify(cache);
	}

	@Test(expected = IdentifierGeneratorException.class)
	public void testGenerateSessionIDException()
	{
		try
		{
			cache.registerIdentifier((String) notNull());
			expectLastCall().andThrow(new IdentifierCollisionException("test exception"));
			replay(cache);
		}
		catch (IdentifierCollisionException e)
		{
			fail("This exception MUST be caught by the library");
		}

		String id;
		id = this.ident.generateSessionID();
		assertTrue("SessionID should be of matching format", id.matches(".*-.*-.*")); //$NON-NLS-1$ //$NON-NLS-2$

		verify(cache);
	}
}
