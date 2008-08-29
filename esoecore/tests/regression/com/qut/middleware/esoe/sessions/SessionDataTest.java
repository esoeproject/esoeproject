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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Vector;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.sessions.bean.SessionConfigData;
import com.qut.middleware.esoe.sessions.bean.impl.SessionConfigDataImpl;
import com.qut.middleware.esoe.sessions.exception.ConfigurationValidationException;
import com.qut.middleware.esoe.sessions.exception.SessionsDAOException;
import com.qut.middleware.esoe.sessions.sqlmap.SessionsDAO;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.saml2.schemas.esoe.sessions.AttributeType;
import com.qut.middleware.saml2.schemas.esoe.sessions.HandlerType;
import com.qut.middleware.saml2.schemas.esoe.sessions.IdentityType;

/** */
@SuppressWarnings("nls")
public class SessionDataTest
{
	private SessionsDAO sessionsDAO;
	private MetadataProcessor metadata;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{

	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception
	{
	}

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

			String entityID = "http://test.service.com";
			Integer entID = new Integer("1");
			FileInputStream attribStream = new FileInputStream(xml);
			byte[] attribPol = new byte[(int)xml.length()];
			
			attribStream.read(attribPol);
			
			this.metadata = createMock(MetadataProcessor.class);
			
			this.sessionsDAO = createMock(SessionsDAO.class);
			expect(sessionsDAO.getEntID(entityID)).andReturn(entID);
			expect(sessionsDAO.selectActiveAttributePolicy(entID)).andReturn(attribPol);
			
			replay(this.metadata);
			replay(this.sessionsDAO);
			
			data = new SessionConfigDataImpl(sessionsDAO, metadata, entityID);

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
			
			verify(this.metadata);
			verify(this.sessionsDAO);
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
		catch (SessionsDAOException e)
		{
			fail("SessionsDAOException "  + e.getLocalizedMessage());
		}
		catch (IOException e)
		{
			fail("IOException  "  + e.getLocalizedMessage());
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

			String entityID = "http://test.service.com";
			Integer entID = new Integer("1");
			FileInputStream attribStream = new FileInputStream(xml);
			byte[] attribPol = new byte[(int)xml.length()];
			
			attribStream.read(attribPol);
			
			this.metadata = createMock(MetadataProcessor.class);
			
			this.sessionsDAO = createMock(SessionsDAO.class);
			expect(sessionsDAO.getEntID(entityID)).andReturn(entID);
			expect(sessionsDAO.selectActiveAttributePolicy(entID)).andReturn(attribPol);
			
			replay(this.metadata);
			replay(this.sessionsDAO);
			
			data = new SessionConfigDataImpl(sessionsDAO, metadata, entityID);


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
			verify(this.metadata);
			verify(this.sessionsDAO);
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
		catch (SessionsDAOException e)
		{
			fail("SessionsDAOException "  + e.getLocalizedMessage());
		}
		catch (IOException e)
		{
			fail("IOException  "  + e.getLocalizedMessage());
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

			String entityID = "http://test.service.com";
			Integer entID = new Integer("1");
			FileInputStream attribStream = new FileInputStream(xml);
			byte[] attribPol = new byte[(int)xml.length()];
			
			attribStream.read(attribPol);
			
			this.metadata = createMock(MetadataProcessor.class);
			
			this.sessionsDAO = createMock(SessionsDAO.class);
			expect(sessionsDAO.getEntID(entityID)).andReturn(entID);
			expect(sessionsDAO.selectActiveAttributePolicy(entID)).andReturn(attribPol);
			
			replay(this.metadata);
			replay(this.sessionsDAO);
			
			data = new SessionConfigDataImpl(sessionsDAO, metadata, entityID);


			fail("Invalid XML passed validation.");
			
			verify(this.metadata);
			verify(this.sessionsDAO);
		}
		catch (ConfigurationValidationException ex)
		{
			caught = true;
		}
		catch (URISyntaxException ex)
		{
			fail("URI Syntax exception encountered.");
		}
		catch (SessionsDAOException e)
		{
			fail("SessionsDAOException "  + e.getLocalizedMessage());
		}
		catch (IOException e)
		{
			fail("IOException  "  + e.getLocalizedMessage());
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

			String entityID = "http://test.service.com";
			Integer entID = new Integer("1");
			FileInputStream attribStream = new FileInputStream(xml);
			byte[] attribPol = new byte[(int)xml.length()];
			
			attribStream.read(attribPol);
			
			this.metadata = createMock(MetadataProcessor.class);
			
			this.sessionsDAO = createMock(SessionsDAO.class);
			expect(sessionsDAO.getEntID(entityID)).andReturn(entID);
			expect(sessionsDAO.selectActiveAttributePolicy(entID)).andReturn(attribPol);
			
			replay(this.metadata);
			replay(this.sessionsDAO);
			
			data = new SessionConfigDataImpl(sessionsDAO, metadata, entityID);


			fail("Invalid XML passed validation.");
			
			verify(this.metadata);
			verify(this.sessionsDAO);
		}
		catch (ConfigurationValidationException ex)
		{
			caught = true;
		}
		catch (URISyntaxException ex)
		{
			fail("URI Syntax exception encountered.");
		}
		catch (SessionsDAOException e)
		{
			fail("SessionsDAOException "  + e.getLocalizedMessage());
		}
		catch (IOException e)
		{
			fail("IOException  "  + e.getLocalizedMessage());
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

			String entityID = "http://test.service.com";
			Integer entID = new Integer("1");
			FileInputStream attribStream = new FileInputStream(xml);
			byte[] attribPol = new byte[(int)xml.length()];
			
			attribStream.read(attribPol);
			
			this.metadata = createMock(MetadataProcessor.class);
			
			this.sessionsDAO = createMock(SessionsDAO.class);
			expect(sessionsDAO.getEntID(entityID)).andReturn(entID);
			expect(sessionsDAO.selectActiveAttributePolicy(entID)).andReturn(attribPol);
			
			replay(this.metadata);
			replay(this.sessionsDAO);
			
			data = new SessionConfigDataImpl(sessionsDAO, metadata, entityID);


			fail("Invalid XML passed validation.");
			
			verify(this.metadata);
			verify(this.sessionsDAO);
		}
		catch (ConfigurationValidationException ex)
		{
			caught = true;
		}
		catch (URISyntaxException ex)
		{
			fail("URI Syntax exception encountered.");
		}
		catch (SessionsDAOException e)
		{
			fail("SessionsDAOException "  + e.getLocalizedMessage());
		}
		catch (IOException e)
		{
			fail("IOException  "  + e.getLocalizedMessage());
		}

		assertTrue("Invalid XML did not generate an exception", caught);
		assertNull("Invalid XML still resulted in non-null session data", data);
	}
}
