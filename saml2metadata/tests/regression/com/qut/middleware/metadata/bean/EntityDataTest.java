/*
 * Copyright 2008, Queensland University of Technology
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
 * Creation Date: 09/04/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.metadata.bean;

import org.junit.Test;
import static org.junit.Assert.*;

import com.qut.middleware.metadata.bean.impl.EntityDataImpl;

@SuppressWarnings("all")
public class EntityDataTest
{
	private class A implements Role { public A() {} }
	private class B extends A { public B() {} }
	private class C extends B { public C() {} }
	private class D extends A { public D() {} }
	private class E implements Role { public E() {} }

	@Test
	public void testSetGetEntityID()
	{
		String entityID = "http://example.com";
		
		EntityDataImpl data = new EntityDataImpl("", 0);
		data.setEntityID(entityID);
		
		assertEquals(entityID, data.getEntityID());
	}
	
	@Test
	public void testAddGetRoleData()
	{
		EntityDataImpl data = new EntityDataImpl("", 0);
		
		A a = new A();
		B b = new B();
		C c = new C();
		D d = new D();
		E e = new E();
		
		// Test the internal type checking that EntityDataImpl does.. make sure 
		// it's correctly detecting subclasses
		data.addRoleData(c);
		assertEquals(c, data.getRoleData(A.class));
		assertEquals(c, data.getRoleData(B.class));
		assertEquals(c, data.getRoleData(C.class));
		assertNull(data.getRoleData(D.class));
		assertNull(data.getRoleData(E.class));
		data.addRoleData(b);
		assertEquals(b, data.getRoleData(A.class));
		assertEquals(b, data.getRoleData(B.class));
		assertEquals(c, data.getRoleData(C.class));
		assertNull(data.getRoleData(D.class));
		assertNull(data.getRoleData(E.class));
		data.addRoleData(a);
		assertEquals(a, data.getRoleData(A.class));
		assertEquals(b, data.getRoleData(B.class));
		assertEquals(c, data.getRoleData(C.class));
		assertNull(data.getRoleData(D.class));
		assertNull(data.getRoleData(E.class));
		data.addRoleData(d);
		assertEquals(a, data.getRoleData(A.class));
		assertEquals(b, data.getRoleData(B.class));
		assertEquals(c, data.getRoleData(C.class));
		assertEquals(d, data.getRoleData(D.class));
		assertNull(data.getRoleData(E.class));
		data.addRoleData(e);
		assertEquals(a, data.getRoleData(A.class));
		assertEquals(b, data.getRoleData(B.class));
		assertEquals(c, data.getRoleData(C.class));
		assertEquals(d, data.getRoleData(D.class));
		assertEquals(e, data.getRoleData(E.class));
	}
	
	@Test(expected = Exception.class)
	public void testCollideRoleData()
	{
		EntityDataImpl data = new EntityDataImpl("", 0);
		
		A a1 = new A();
		A a2 = new A();
		
		data.addRoleData(a1);
		data.addRoleData(a2);
	}
	
	@Test
	public void testGetSetExpiryTime()
	{
		EntityDataImpl data = new EntityDataImpl("", 0);
		
		assertEquals(0, data.getExpiryTimeMillis());
		
		long value = 86400000L;
		data.setExpiryTimeMillis(value);
		assertEquals(value, data.getExpiryTimeMillis());
	}
	
	@Test
	public void testGetSetTrusted()
	{
		EntityDataImpl data = new EntityDataImpl("", 0);
		
		assertFalse(data.isTrusted());
		
		data.setTrusted(true);
		assertTrue(data.isTrusted());
	}
}
