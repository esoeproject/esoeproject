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
 * Creation Date: 21/03/2007
 * 
 * Purpose: Tests to ensure the SAMLValidatorImpl returns correctly
 */

package com.qut.middleware.saml2.validator.impl;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.qut.middleware.saml2.identifier.IdentifierCache;

public class SAMLValidatorImplTest
{

	/* Tests to ensure that null parameter for cache throws appropriate exception */
	@Test (expected=IllegalArgumentException.class)
	public void testSAMLValidatorImpl1()
	{
		SAMLValidatorImpl validator = new SAMLValidatorImpl(null, 100);
	}
	
	/* Tests to ensure that invalid parameter for time throws appropriate exception */
	@Test (expected=IllegalArgumentException.class)
	public void testSAMLValidatorImpl2()
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		SAMLValidatorImpl validator = new SAMLValidatorImpl(cache, (Integer.MAX_VALUE / 1000) + 1);
	}

	@Test
	public void testGetAssertionValidator()
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		SAMLValidatorImpl validator = new SAMLValidatorImpl(cache, 100);
		assertNotNull("Ensures the assertion validator is returned correctly", validator.getAssertionValidator());
	}

	@Test
	public void testGetRequestValidator()
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		SAMLValidatorImpl validator = new SAMLValidatorImpl(cache, 100);
		assertNotNull("Ensures the request validator is returned correctly", validator.getRequestValidator());
	}

	@Test
	public void testGetResponseValidator()
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		SAMLValidatorImpl validator = new SAMLValidatorImpl(cache, 100);
		assertNotNull("Ensures the response validator is returned correctly", validator.getResponseValidator());
	}

	@Test
	public void testGetAuthnRequestValidator()
	{
		IdentifierCache cache = createMock(IdentifierCache.class);
		SAMLValidatorImpl validator = new SAMLValidatorImpl(cache, 100);
		assertNotNull("Ensures the AuthnRequest validator is returned correctly", validator.getAuthnRequestValidator());
	}

}
