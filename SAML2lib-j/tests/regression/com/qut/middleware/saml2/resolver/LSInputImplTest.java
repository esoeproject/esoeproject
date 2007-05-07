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
 * Author: Bradley Beddoes
 * Creation Date: 20/03/2007
 * 
 * Purpose: Tests LSInput implementation class
 */
package com.qut.middleware.saml2.resolver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;

public class LSInputImplTest
{
	LSInputImpl input;
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		this.input = new LSInputImpl();
	}

	/**
	 * Test method for {@link com.qut.middleware.saml2.resolver.LSInputImpl#getBaseURI()}.
	 */
	@Test
	public void testGetBaseURI()
	{
		String URI = "http://";
		this.input.setBaseURI(URI);
		assertTrue("Ensures getter/setter pair operates correctly", URI.equals(this.input.getBaseURI()));
	}

	/**
	 * Test method for {@link com.qut.middleware.saml2.resolver.LSInputImpl#getByteStream()}.
	 */
	@Test
	public void testGetByteStream() throws FileNotFoundException
	{
		InputStream stream = new FileInputStream("tests/testdata/AuthnRequestSigned.xml");
		this.input.setByteStream(stream);
		assertEquals("Ensures getter/setter pair operates correctly", stream, this.input.getByteStream());
	}

	/**
	 * Test method for {@link com.qut.middleware.saml2.resolver.LSInputImpl#getCertifiedText()}.
	 */
	@Test
	public void testGetCertifiedText()
	{
		this.input.setCertifiedText(false);
		assertFalse("Ensures getter/setter pair operates correctly", this.input.getCertifiedText());
		
		this.input.setCertifiedText(true);
		assertTrue("Ensures getter/setter pair operates correctly", this.input.getCertifiedText());
	}

	/**
	 * Test method for {@link com.qut.middleware.saml2.resolver.LSInputImpl#getCharacterStream()}.
	 */
	@Test
	public void testGetCharacterStream()
	{
		Reader reader = new StringReader("testcase");
		this.input.setCharacterStream(reader);
		assertEquals("Ensures getter/setter pair operates correctly",reader, this.input.getCharacterStream());
	}

	/**
	 * Test method for {@link com.qut.middleware.saml2.resolver.LSInputImpl#getEncoding()}.
	 */
	@Test
	public void testGetEncoding()
	{
		String encoding = "UTF-16";
		this.input.setEncoding(encoding);
		assertTrue("Ensures getter/setter pair operates correctly", encoding.equals(this.input.getEncoding()));
	}

	/**
	 * Test method for {@link com.qut.middleware.saml2.resolver.LSInputImpl#getPublicId()}.
	 */
	@Test
	public void testGetPublicId()
	{
		String publicID = "1234";
		this.input.setPublicId(publicID);
		assertTrue("Ensures getter/setter pair operates correctly", publicID.equals(this.input.getPublicId()));
	}

	/**
	 * Test method for {@link com.qut.middleware.saml2.resolver.LSInputImpl#getStringData()}.
	 */
	@Test
	public void testGetStringData()
	{
		String stringData = "A string..with data!!";
		this.input.setStringData(stringData);
		assertTrue("Ensures getter/setter pair operates correctly", stringData.equals(this.input.getStringData()));
	}

	/**
	 * Test method for {@link com.qut.middleware.saml2.resolver.LSInputImpl#getSystemId()}.
	 */
	@Test
	public void testGetSystemId()
	{
		String systemID = "7890";
		this.input.setSystemId(systemID);
		assertTrue("Ensures getter/setter pair operates correctly", systemID.equals(this.input.getSystemId()));
	}

}
