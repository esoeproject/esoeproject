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
 * Purpose: Tests Resource Resolver impl
 */
package com.qut.middleware.saml2.resolver;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.ls.LSInput;

public class ResourceResolverTest
{
	ResourceResolver resolver;

	@Before
	public void setUp() throws Exception
	{
		this.resolver = new ResourceResolver();
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void testResolveResource1()
	{
		LSInput input = this.resolver.resolveResource("test", "http://www.w3.org/2000/09/xmldsig#", "xml", "xml", "xmlbase");
		assertNotNull("Ensures returned LSInput is not null", input);
	}

	@Test
	public void testResolveResource2()
	{
		LSInput input = this.resolver.resolveResource("test", "http://www.w3.org/2001/04/xmlenc#", "xml", "xml", "xmlbase");
		assertNotNull("Ensures returned LSInput is not null", input);
	}

	@Test
	public void testResolveResource3()
	{
		LSInput input = this.resolver.resolveResource("test", "http://www.w3.org/XML/1998/namespace", "xml", "xml", "xmlbase");
		assertNotNull("Ensures returned LSInput is not null", input);
	}

	@Test
	public void testResolveResource4()
	{
		LSInput input = this.resolver.resolveResource("test", "urn:oasis:names:tc:SAML:2.0:assertion", "xml", "xml", "xmlbase");
		assertNotNull("Ensures returned LSInput is not null", input);
	}

	@Test
	public void testResolveResource5()
	{
		LSInput input = this.resolver.resolveResource("test", "urn:oasis:names:tc:SAML:2.0:protocol", "xml", "xml", "xmlbase");
		assertNotNull("Ensures returned LSInput is not null", input);
	}

	@Test
	public void testResolveResource6()
	{
		LSInput input = this.resolver.resolveResource("test", "urn:oasis:names:tc:SAML:2.0:metadata", "xml", "xml", "xmlbase");
		assertNotNull("Ensures returned LSInput is not null", input);
	}

	@Test
	public void testResolveResource7()
	{
		LSInput input = this.resolver.resolveResource("test", "http://www.qut.com/middleware/lxacmlSchema", "xml", "xml", "xmlbase");
		assertNotNull("Ensures returned LSInput is not null", input);
	}

	@Test
	public void testResolveResource8()
	{
		LSInput input = this.resolver.resolveResource("test", "http://www.qut.com/middleware/lxacmlPDPSchema", "xml", "xml", "xmlbase");
		assertNotNull("Ensures returned LSInput is not null", input);
	}

	@Test
	public void testResolveResource9()
	{
		LSInput input = this.resolver.resolveResource("test", "http://www.qut.com/middleware/lxacmlContextSchema", "xml", "xml", "xmlbase");
		assertNotNull("Ensures returned LSInput is not null", input);
	}

	@Test
	public void testResolveResource10()
	{
		LSInput input = this.resolver.resolveResource("test", "http://www.qut.com/middleware/lxacmlGroupTargetSchema", "xml", "xml", "xmlbase");
		assertNotNull("Ensures returned LSInput is not null", input);
	}

	@Test
	public void testResolveResource11()
	{
		LSInput input = this.resolver.resolveResource("test", "http://www.qut.com/middleware/lxacmlSAMLAssertionSchema", "xml", "xml", "xmlbase");
		assertNotNull("Ensures returned LSInput is not null", input);
	}

	@Test
	public void testResolveResource12()
	{
		LSInput input = this.resolver.resolveResource("test", "http://www.qut.com/middleware/lxacmlSAMLProtocolSchema", "xml", "xml", "xmlbase");
		assertNotNull("Ensures returned LSInput is not null", input);
	}

	@Test
	public void testResolveResource13()
	{
		LSInput input = this.resolver.resolveResource("test", "http://www.qut.com/middleware/SessionDataSchema", "xml", "xml", "xmlbase");
		assertNotNull("Ensures returned LSInput is not null", input);
	}

	@Test
	public void testResolveResource14()
	{
		LSInput input = this.resolver.resolveResource("test", "http://www.qut.com/middleware/ESOEProtocolSchema", "xml", "xml",	"xmlbase");
		assertNotNull("Ensures returned LSInput is not null", input);
	}

	@Test
	public void testResolveResource15()
	{
		LSInput input = this.resolver.resolveResource("test", "http://www.qut.com/middleware/cacheClearServiceSchema", "xml", "xml", "xmlbase");
		assertNotNull("Ensures returned LSInput is not null", input);
	}

	@Test
	public void testResolveResource16()
	{
		LSInput input = this.resolver.resolveResource(null, null, null, null, null);
		assertNotNull("Ensures returned LSInput is not null", input);

	}

}
