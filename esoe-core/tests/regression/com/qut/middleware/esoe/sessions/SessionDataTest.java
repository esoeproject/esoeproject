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
 * Purpose: Tests the session data implementation.
 */
package com.qut.middleware.esoe.sessions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Vector;

import org.junit.Test;

import com.qut.middleware.esoe.sessions.bean.SessionConfigData;
import com.qut.middleware.esoe.sessions.bean.impl.SessionConfigDataImpl;
import com.qut.middleware.esoe.sessions.exception.ConfigurationValidationException;
import com.qut.middleware.saml2.schemas.esoe.sessions.AttributeType;
import com.qut.middleware.saml2.schemas.esoe.sessions.HandlerType;
import com.qut.middleware.saml2.schemas.esoe.sessions.IdentityType;

/** */
@SuppressWarnings("nls")
public class SessionDataTest
{

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sessions.bean.impl.SessionConfigDataImpl#SessionConfigDataImpl(java.lang.String)}.
	 */
	@SuppressWarnings( { "boxing", "nls" })
	@Test
	public final void testSessionConfigDataImpl1()
	{
		SessionConfigData data;
		File xml;
		try
		{
			xml = new File(this.getClass().getResource("sessionDataValid.xml").toURI());

			data = new SessionConfigDataImpl(xml);

			assertNotNull("Root of session data was null?", data.getIdentity());
			assertEquals("Incorrect number of Identity elements unmarshalled", data.getIdentity().size(), 1);
			IdentityType identity = data.getIdentity().get(0);
			if (identity != null)
			{
				assertEquals("Incorrect number of Attribute elements unmarshalled", identity.getAttributes().size(), 3);
				AttributeType attribute = identity.getAttributes().get(0);

				List<String> identifierComparator = new Vector<String>(0, 1);
				identifierComparator.add("uid");
				identifierComparator.add("sn");
				identifierComparator.add("mail");

				if (attribute != null)
				{
					assertTrue("Incorrect identifier unmarshalled: " + attribute.getIdentifier(), identifierComparator
							.contains(attribute.getIdentifier()));

					assertEquals("Incorrect number of Handler elements unmarshalled", attribute.getHandlers().size(), 1);
					HandlerType handler = attribute.getHandlers().get(0);

					if (handler != null)
					{
						assertTrue("Incorrect local identifier unmarshalled: " + handler.getLocalIdentifier(),
								identifierComparator.contains(handler.getLocalIdentifier()));

						List<String> handlerTypeComparator = new Vector<String>(0, 1);
						handlerTypeComparator.add("NullHandlerImpl");
						handlerTypeComparator.add("LDAPHandlerImpl");
						handlerTypeComparator.add("DatabaseHandlerImpl");

						assertTrue("Incorrect list of HandlerTypes unmarshalled", handlerTypeComparator
								.contains(handler.getName()));
					}
				}
			}
		}
		catch (ConfigurationValidationException ex)
		{
			fail("Couldn't validate XML. " + ex.getMessage());
			return;
		}
		catch (URISyntaxException ex)
		{
			fail("URI Syntax exception encountered.");
		}
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sessions.bean.impl.SessionConfigDataImpl#SessionConfigDataImpl(java.lang.String)}.
	 */
	@SuppressWarnings( { "boxing", "nls" })
	@Test
	public final void testSessionConfigDataImpl2()
	{
		SessionConfigData data;
		File xml;
		try
		{
			xml = new File(this.getClass().getResource("sessionDataValid2.xml").toURI());

			data = new SessionConfigDataImpl(xml);

			assertNotNull("Root of session data was null?", data.getIdentity());
			assertEquals("Incorrect number of Identity elements unmarshalled", data.getIdentity().size(), 1);
			IdentityType identity = data.getIdentity().get(0);
			if (identity != null)
			{
				assertEquals("Incorrect number of Attribute elements unmarshalled", identity.getAttributes().size(), 1);
				AttributeType attribute = identity.getAttributes().get(0);

				List<String> identifierComparator = new Vector<String>(0, 1);
				identifierComparator.add("uid");
				identifierComparator.add("sn");
				identifierComparator.add("mail");

				if (attribute != null)
				{
					assertTrue("Incorrect identifier unmarshalled: " + attribute.getIdentifier(), identifierComparator
							.contains(attribute.getIdentifier()));

					assertEquals("Incorrect number of Handler elements unmarshalled", attribute.getHandlers().size(), 3);
					HandlerType handler = attribute.getHandlers().get(0);

					if (handler != null)
					{
						assertTrue("Incorrect local identifier unmarshalled: " + handler.getLocalIdentifier(),
								identifierComparator.contains(handler.getLocalIdentifier()));

						List<String> handlerTypeComparator = new Vector<String>(0, 1);
						handlerTypeComparator.add("NullHandlerImpl");
						handlerTypeComparator.add("LDAPHandlerImpl");
						handlerTypeComparator.add("DatabaseHandlerImpl");

						assertTrue("Incorrect list of HandlerTypes unmarshalled", handlerTypeComparator
								.contains(handler.getName()));
					}
				}
			}
		}
		catch (ConfigurationValidationException ex)
		{
			fail("Couldn't validate XML. " + ex.getMessage());
			return;
		}
		catch (URISyntaxException ex)
		{
			fail("URI Syntax exception encountered.");
		}
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sessions.bean.impl.SessionConfigDataImpl#SessionConfigDataImpl(java.lang.String)}.
	 */
	@SuppressWarnings( { "boxing", "nls" })
	@Test
	public final void testSessionConfigDataImpl3()
	{
		SessionConfigData data = null;
		File xml;
		boolean caught = false;
		try
		{
			xml = new File(this.getClass().getResource("sessionDataInvalid.xml").toURI());

			data = new SessionConfigDataImpl(xml);

			fail("Invalid XML passed validation.");
		}
		catch (ConfigurationValidationException ex)
		{
			caught = true;
		}
		catch (URISyntaxException ex)
		{
			fail("URI Syntax exception encountered.");
		}

		assertTrue("Invalid XML did not generate an exception", caught);
		assertNull("Invalid XML still resulted in non-null session data", data);
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sessions.bean.impl.SessionConfigDataImpl#SessionConfigDataImpl(java.lang.String)}.
	 */
	@SuppressWarnings( { "boxing", "nls" })
	@Test
	public final void testSessionConfigDataImpl4()
	{
		SessionConfigData data = null;
		File xml;
		boolean caught = false;
		try
		{
			xml = new File(this.getClass().getResource("sessionDataInvalid2.xml").toURI());

			data = new SessionConfigDataImpl(xml);

			fail("Invalid XML passed validation.");
		}
		catch (ConfigurationValidationException ex)
		{
			caught = true;
		}
		catch (URISyntaxException ex)
		{
			fail("URI Syntax exception encountered.");
		}

		assertTrue("Invalid XML did not generate an exception", caught);
		assertNull("Invalid XML still resulted in non-null session data", data);
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sessions.bean.impl.SessionConfigDataImpl#SessionConfigDataImpl(java.lang.String)}.
	 */
	@SuppressWarnings( { "boxing", "nls" })
	@Test
	public final void testSessionConfigDataImpl5()
	{
		SessionConfigData data = null;
		File xml;
		boolean caught = false;
		try
		{
			xml = new File(this.getClass().getResource("sessionDataInvalid3.xml").toURI());

			data = new SessionConfigDataImpl(xml);

			fail("Invalid XML passed validation.");
		}
		catch (ConfigurationValidationException ex)
		{
			caught = true;
		}
		catch (URISyntaxException ex)
		{
			fail("URI Syntax exception encountered.");
		}

		assertTrue("Invalid XML did not generate an exception", caught);
		assertNull("Invalid XML still resulted in non-null session data", data);
	}
}
