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
 * Purpose: Tests keyData impl
 */
package com.qut.middleware.saml2.sec;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertEquals;

import java.security.PublicKey;

import org.junit.Test;

import com.qut.middleware.saml2.schemas.metadata.KeyTypes;

public class KeyDataTest
{

	@Test
	public void testKeyData1()
	{
		PublicKey key = createMock(PublicKey.class);
		KeyTypes use = KeyTypes.SIGNING;
		
		KeyData keyData = new KeyData(use, key);
		
		assertEquals("Assert key use is correct", use, keyData.getUse());
		assertEquals("Assert key is the same key supplied to constructor", key, keyData.getPk());
	}

	@Test
	public void testKeyData2()
	{
		PublicKey key = createMock(PublicKey.class);
		
		KeyData keyData = new KeyData(key);

		assertEquals("Assert key is the same key supplied to constructor", key, keyData.getPk());
	}
}
