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
 * Purpose: Tests the principal implementation.
 */
package com.qut.middleware.esoe.sessions;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.Vector;

import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Test;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.sessions.bean.IdentityAttribute;
import com.qut.middleware.esoe.sessions.bean.impl.IdentityAttributeImpl;
import com.qut.middleware.esoe.sessions.bean.impl.IdentityDataImpl;
import com.qut.middleware.esoe.sessions.exception.InvalidDescriptorIdentifierException;
import com.qut.middleware.esoe.sessions.impl.PrincipalImpl;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

/** */
@SuppressWarnings("nls")
public class PrincipalTest
{

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sessions.impl.PrincipalImpl#addDescriptorSessionIdentifier(java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testAddEntitySessionIdentifier()
	{
		String entity1 = "85792837982374987";
		String session11 = "aslfkjwqlekjrlkajs";
		String session12 = "aslkdfjalkjhpopwtj";
		String entity2 = "r2387491827598712";
		String session21 = "oiwetoiqjwpoiejgab";
		String entity3 = "92385491827395792";
		String session31 = "tpsdkfjlakjelrkjwl";
		String session32 = "lkjweotijoqwiejorf";
		String session33 = "zxncvmnvlkrjntogia";
		String entity4 = "97235871293874987";

		Principal principal = new PrincipalImpl(new IdentityDataImpl(), 360);
		principal.addActiveDescriptor(entity1);
		try
		{
			principal.addDescriptorSessionIdentifier(entity1, session11);
			principal.addDescriptorSessionIdentifier(entity1, session12);
		}
		catch (InvalidDescriptorIdentifierException ex)
		{
			fail("Invalid entity identifier although entity was added already.");
		}
		principal.addActiveDescriptor(entity2);
		try
		{
			principal.addDescriptorSessionIdentifier(entity2, session21);
		}
		catch (InvalidDescriptorIdentifierException ex)
		{
			fail("Invalid entity identifier although entity was added already.");
		}
		try
		{
			principal.addActiveDescriptor(entity3);
			principal.addDescriptorSessionIdentifier(entity3, session31);
			principal.addDescriptorSessionIdentifier(entity3, session32);
			principal.addDescriptorSessionIdentifier(entity3, session33);
		}
		catch (InvalidDescriptorIdentifierException ex)
		{
			fail("Invalid entity identifier although entity was added already.");
		}

		List<String> comparator = new Vector<String>(0, 1);
		comparator.add(session11);
		comparator.add(session12);

		try
		{
			assertTrue("Got back unexpected session values for entity1", comparator.containsAll(principal
					.getDescriptorSessionIdentifiers(entity1)));
			assertTrue("Didn't get back all session values for entity1", principal.getDescriptorSessionIdentifiers(entity1)
					.containsAll(comparator));
		}
		catch (InvalidDescriptorIdentifierException ex)
		{
			fail("Invalid entity identifier although entity was added already.");
		}

		comparator = new Vector<String>(0, 1);
		comparator.add(session21);

		try
		{
			assertTrue("Got back unexpected session values for entity2", comparator.containsAll(principal
					.getDescriptorSessionIdentifiers(entity2)));
			assertTrue("Didn't get back all session values for entity2", principal.getDescriptorSessionIdentifiers(entity2)
					.containsAll(comparator));
		}
		catch (InvalidDescriptorIdentifierException ex)
		{
			fail("Invalid entity identifier although entity was added already.");
		}

		comparator = new Vector<String>(0, 1);
		comparator.add(session31);
		comparator.add(session32);
		comparator.add(session33);

		try
		{
			assertTrue("Got back unexpected session values for entity3", comparator.containsAll(principal
					.getDescriptorSessionIdentifiers(entity3)));
			assertTrue("Didn't get back all session values for entity3", principal.getDescriptorSessionIdentifiers(entity3)
					.containsAll(comparator));
		}
		catch (InvalidDescriptorIdentifierException ex)
		{
			fail("Invalid entity identifier although entity was added already.");
		}

		boolean caught = false;
		try
		{
			principal.getDescriptorSessionIdentifiers(entity4);
		}
		catch (InvalidDescriptorIdentifierException ex)
		{
			caught = true;
		}
		assertTrue("Got session list back for entity that was never added.", caught);
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sessions.impl.PrincipalImpl#putAttribute(java.lang.String, com.qut.middleware.esoe.sessions.bean.IdentityAttribute)}.
	 */
	@Test
	public final void testPutAttribute()
	{
		Principal principal = new PrincipalImpl(new IdentityDataImpl(), 360);
		IdentityAttribute attribute = new IdentityAttributeImpl();

		principal.putAttribute("roar", attribute);

		Map<String, IdentityAttribute> attributes = principal.getAttributes();

		assertSame("Expected same object to be returned", attribute, attributes.get("roar"));
	}

	
	/** Added a few more accessor/mutator tests to cover methods uncalled thus far.
	 * 
	 *
	 */
	@Test
	public void testCoverage()
	{
		Principal principal = new PrincipalImpl(new IdentityDataImpl(), 360);
		
		principal.setAuthenticationContextClass("MyAuthnContext");		
		assertEquals("MyAuthnContext", principal.getAuthenticationContextClass());
		
		Date now = new Date();
		long time = now.getTime();
		
		principal.setAuthnTimestamp(time);
		assertEquals(time, principal.getAuthnTimestamp());
				
		// should be the same as authn timestamp if the object has not been accessed
		TimeZone utc = new SimpleTimeZone(0, ConfigurationConstants.timeZone); 
		GregorianCalendar cal = new GregorianCalendar(utc);
		XMLGregorianCalendar xmlCal = new XMLGregorianCalendarImpl(cal);
		
		// not a hell of a lot we can do here, other than check it's greater than now
		// the actual value set should be the time the principal was created + the allowed 
		// time skew
		assertTrue(principal.getSessionNotOnOrAfter().toGregorianCalendar().after(xmlCal.toGregorianCalendar()));
	}
}
