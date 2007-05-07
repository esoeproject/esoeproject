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
 * Creation Date: 20/03/2007
 * 
 * Purpose: Tests to ensure prefix mapping is correct
 */
package com.qut.middleware.saml2.namespace;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NamespacePrefixMapperTest
{
	/* Tests to ensure correct prefix's are returned */
	@Test
	public void testGetPreferredPrefix1()
	{
		NamespacePrefixMapperImpl mapper = new NamespacePrefixMapperImpl();
		String prefix;
		
		prefix = mapper.getPreferredPrefix("http://www.w3.org/2001/XMLSchema", "testcase", true);
		assertEquals("Ensures returned prefix is correct", "xs", prefix);
			
		prefix = mapper.getPreferredPrefix("http://www.w3.org/2001/XMLSchema-instance", "testcase", true);
		assertEquals("Ensures returned prefix is correct", "xsi", prefix);

		prefix = mapper.getPreferredPrefix("http://www.w3.org/2000/09/xmldsig#", "testcase", true);
		assertEquals("Ensures returned prefix is correct", "ds", prefix);
		
		prefix = mapper.getPreferredPrefix("http://www.w3.org/2001/04/xmlenc#", "testcase", true);
		assertEquals("Ensures returned prefix is correct", "xenc", prefix);

		prefix = mapper.getPreferredPrefix("urn:oasis:names:tc:SAML:2.0:protocol", "testcase", true);
		assertEquals("Ensures returned prefix is correct", "samlp", prefix);

		prefix = mapper.getPreferredPrefix("urn:oasis:names:tc:SAML:2.0:assertion", "testcase", true);
		assertEquals("Ensures returned prefix is correct", "saml", prefix);

		prefix = mapper.getPreferredPrefix("urn:oasis:names:tc:SAML:2.0:metadata", "testcase", true);
		assertEquals("Ensures returned prefix is correct", "md", prefix);

		prefix = mapper.getPreferredPrefix("http://www.qut.com/middleware/SessionDataSchema", "testcase", true);
		assertEquals("Ensures returned prefix is correct", "session", prefix);

		prefix = mapper.getPreferredPrefix("http://www.qut.com/middleware/lxacmlSchema", "testcase", true);
		assertEquals("Ensures returned prefix is correct", "lxacml", prefix);

		prefix = mapper.getPreferredPrefix("http://www.qut.com/middleware/lxacmlSAMLProtocolSchema", "testcase", true);
		assertEquals("Ensures returned prefix is correct", "lxacmlp", prefix);

		prefix = mapper.getPreferredPrefix("http://www.qut.com/middleware/lxacmlSAMLAssertionSchema", "testcase", true);
		assertEquals("Ensures returned prefix is correct", "lxacmla", prefix);

		prefix = mapper.getPreferredPrefix("http://www.qut.com/middleware/lxacmlGroupTargetSchema", "testcase", true);
		assertEquals("Ensures returned prefix is correct", "group", prefix);
		
		prefix = mapper.getPreferredPrefix("http://www.qut.com/middleware/lxacmlPDPSchema", "testcase", true);
		assertEquals("Ensures returned prefix is correct", "lxacml-md", prefix);

		prefix = mapper.getPreferredPrefix("http://www.qut.com/middleware/lxacmlContextSchema", "testcase", true);
		assertEquals("Ensures returned prefix is correct", "lxacml-context", prefix);

		prefix = mapper.getPreferredPrefix("http://www.qut.com/middleware/ESOEProtocolSchema", "testcase", true);
		assertEquals("Ensures returned prefix is correct", "esoe", prefix);
		
		prefix = mapper.getPreferredPrefix("http://www.qut.com/middleware/cacheClearServiceSchema", "testcase", true);
		assertEquals("Ensures returned prefix is correct", "clear", prefix);
		
		prefix = mapper.getPreferredPrefix("http://www.qut.com/middleware/spepStartupServiceSchema", "testcase", true);
		assertEquals("Ensures returned prefix is correct", "spep", prefix);
	}
	
	/* Tests to ensure exception on invalid input */
	@Test(expected=IllegalArgumentException.class)
	public void testGetPreferredPrefix2()
	{
		NamespacePrefixMapperImpl mapper = new NamespacePrefixMapperImpl();
		String prefix;
		
		prefix = mapper.getPreferredPrefix(null, "testcase", true);
	}

}
