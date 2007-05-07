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
package com.qut.middleware.esoe.spep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.crypto.KeyStoreResolver;
import com.qut.middleware.esoe.crypto.impl.KeyStoreResolverImpl;

/** */
@SuppressWarnings({"nls", "unqualified-field-access"})
public class KeyStoreResolverTest
{

	private KeyStoreResolver keyStoreResolver;
	private String esoeKeyAlias;
	private String esoeKeyPassword;
	private String keyStorePassword;
	private String keyStorePath;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		this.keyStorePath = "secure/esoekeystore.ks";
		this.keyStorePassword = "Es0EKs54P4SSPK";
		this.esoeKeyAlias = "esoeprimary";
		this.esoeKeyPassword = "Es0EKs54P4SSPK";
		this.keyStoreResolver = new KeyStoreResolverImpl(new File(keyStorePath), keyStorePassword, esoeKeyAlias, esoeKeyPassword);
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.crypto.KeyStoreResolver#resolveKey(java.lang.String)}.
	 */
	@Test
	public final void testResolveKey()
	{
		assertNotNull(this.keyStoreResolver.resolveKey(this.esoeKeyAlias));
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.crypto.KeyStoreResolver#getPrivateKey()}.
	 */
	@Test
	public final void testGetESOEPrivateKey()
	{
		assertNotNull(this.keyStoreResolver.getPrivateKey());
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.crypto.KeyStoreResolver#getKeyAlias()}.
	 */
	@Test
	public final void testGetESOEPrivateKeyAlias()
	{
		assertEquals(this.esoeKeyAlias,this.keyStoreResolver.getKeyAlias());
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.crypto.KeyStoreResolver#resolveCertificate(java.lang.String)}.
	 */
	@Test
	public final void testResolveCertificate()
	{
		assertNotNull(this.keyStoreResolver.resolveCertificate(this.esoeKeyAlias));
		
		// now try with the wrong alias
		assertNull(this.keyStoreResolver.resolveCertificate("wrooongnALias"));
	}
	
	/**
	 * Test constructor block coverage.
	 */
	@Test (expected = IllegalArgumentException.class)
	public final void testConstruction1()
	{
		this.keyStoreResolver = new KeyStoreResolverImpl(null, keyStorePassword, esoeKeyAlias, esoeKeyPassword);
		
	}
	
	/**
	 * Test constructor block coverage.
	 */
	@Test (expected = IllegalArgumentException.class)
	public final void testConstruction2()
	{
		this.keyStoreResolver = new KeyStoreResolverImpl(new File(keyStorePath), null, esoeKeyAlias, esoeKeyPassword);
		
	}
	
	/**
	 * Test constructor block coverage.
	 */
	@Test (expected = IllegalArgumentException.class)
	public final void testConstruction3()
	{
		this.keyStoreResolver = new KeyStoreResolverImpl(new File(keyStorePath), keyStorePassword, null, esoeKeyPassword);
		
	}
	
	/**
	 * Give the resolver a null ESOE private key password. This will cause the getPrivateKey()
	 * operation to fail, as our key has a password on it.
	 */
	@Test (expected = UnsupportedOperationException.class)
	public final void testConstruction4()
	{
		this.keyStoreResolver = new KeyStoreResolverImpl(new File(keyStorePath), keyStorePassword, esoeKeyAlias, null);
		
	}
	
	/**
	 * Test constructor block coverage. Non existent keystore.
	 */
	@Test (expected = UnsupportedOperationException.class)
	public final void testConstruction5()
	{
		this.keyStoreResolver = new KeyStoreResolverImpl(new File("nothing/here/dude/ks.ks"), keyStorePassword, esoeKeyAlias, esoeKeyPassword);
		
	}
	
	/**
	 * Test constructor block coverage. Wrong keystore password.
	 */
	@Test (expected = UnsupportedOperationException.class)
	public final void testConstruction6()
	{
		this.keyStoreResolver = new KeyStoreResolverImpl(new File(keyStorePath), "wrooonnggg!", esoeKeyAlias, esoeKeyPassword);
		
	}
	
	/**
	 * Test constructor block coverage. Wrong ESOE key alias. This will not result in a constrcutor exception,
	 * but subsequent calls to getPrivateKey() will throw an exception.
	 */
	@Test (expected = IllegalArgumentException.class)
	public final void testConstruction7()
	{
		this.keyStoreResolver = new KeyStoreResolverImpl(new File(keyStorePath), keyStorePassword, "wrooonnggg!", esoeKeyPassword);
		
		this.keyStoreResolver.getPrivateKey();
	}
}
