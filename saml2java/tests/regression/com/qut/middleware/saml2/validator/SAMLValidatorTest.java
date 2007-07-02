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
 * Creation Date: 27/10/2006
 * 
 * Purpose: Tests SAMLValidator implementations
 */

package com.qut.middleware.saml2.validator;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.qut.middleware.saml2.identifier.impl.IdentifierCacheImpl;
import com.qut.middleware.saml2.validator.impl.SAMLValidatorImpl;

public class SAMLValidatorTest
{
	/**
	 * Test method for {@link com.qut.middleware.saml2.validator.SAMLValidator#getRequestValidator()}.
	 */
	@SuppressWarnings("nls")
	@Test
	public void testValidator()
	{
		SAMLValidator validator = new SAMLValidatorImpl(new IdentifierCacheImpl(), 60);
		
		assertTrue("Ensures instance of SAMLRequestValidator is returned", validator.getRequestValidator() instanceof SAMLRequestValidator);
		assertTrue("Ensures instance of SAMLResponseValidator is returned", validator.getResponseValidator() instanceof SAMLResponseValidator);
		assertTrue("Ensures instance of SAMLAssertionValidator is returned", validator.getAssertionValidator() instanceof SAMLAssertionValidator);
	}
}
