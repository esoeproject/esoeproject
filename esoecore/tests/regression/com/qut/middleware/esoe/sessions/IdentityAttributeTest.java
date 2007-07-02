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
 * Creation Date: 11/10/2006
 * 
 * Purpose: Tests the identity attribute implementation.
 */
package com.qut.middleware.esoe.sessions;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.sessions.bean.IdentityAttribute;
import com.qut.middleware.esoe.sessions.bean.impl.IdentityAttributeImpl;
import com.qut.middleware.saml2.schemas.esoe.sessions.DataType;

/** */
@SuppressWarnings("nls")
public class IdentityAttributeTest
{
	private IdentityAttribute attribute;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		this.attribute = new IdentityAttributeImpl();
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.sessions.bean.impl.IdentityAttributeImpl#addValue(java.lang.Object)}.
	 */
	@Test
	public final void testAddValue()
	{
		Object object = "Some arbitrary object"; // A string will work fine.
		
		this.attribute.addValue(object);
		
		assertTrue("Object was not added to list", this.attribute.getValues().contains(object));
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.sessions.bean.impl.IdentityAttributeImpl#setType(java.lang.String)}.
	 */
	@Test
	public final void testSetType()
	{
		String type = DataType.BOOLEAN.name();
		
		this.attribute.setType(type);
		
		assertEquals("Type was not set correctly", type, this.attribute.getType());
	}
	
	/**
	 * Test method for {@link com.qut.middleware.esoe.sessions.bean.impl.IdentityAttributeImpl#getHandlers()}.
	 */
	@Test
	public final void testgetHandlers()
	{				
		// Is this method even used?
		assertNotNull(this.attribute.getHandlers());
	}

}
