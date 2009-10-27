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
 * Creation Date: 8/12/2006
 *
 * Purpose: Tests the Initializer class.
 */
package com.qut.middleware.spep;

import static com.qut.middleware.test.functional.Capture.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Vector;

import javax.servlet.ServletContext;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.test.functional.Capture;

/** */
public class InitializerTest
{

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		System.setProperty("spep.data", "webapp" + File.separator + "descriptors" );
	}

	/**
	 * Test method for {@link com.qut.middleware.spep.Initializer#init(javax.servlet.ServletContext)}.
	 * @throws Exception
	 */
	@Test
	public void testInit1() throws Exception
	{
		Capture<SPEP> captureSPEP = new Capture<SPEP>();

		ServletContext context = createMock(ServletContext.class);
		expect(context.getAttribute((String)notNull())).andReturn(null).anyTimes();
		context.setAttribute((String)notNull(), capture(captureSPEP));
		expectLastCall().anyTimes();

		InputStream spKeyStoreStream = new FileInputStream("tests" + File.separator + "testdata" + File.separator + "testspkeystore.ks");
		expect(context.getResourceAsStream("/WEB-INF/spkeystore.ks")).andReturn(spKeyStoreStream).once();

		replay(context);

		Initializer.init(context);

		verify(context);

		Vector<SPEP> captured = captureSPEP.getCaptured();
		assertTrue(captured.size() > 0);

		SPEP spep = captured.get(0);
		assertNotNull(spep);
	}

	/**
	 * Test method for {@link com.qut.middleware.spep.Initializer#init(javax.servlet.ServletContext)}.
	 * @throws Exception
	 */
	@Test
	public void testInit2() throws Exception
	{
		SPEP spep = createMock(SPEP.class);

		ServletContext context = createMock(ServletContext.class);
		expect(context.getAttribute((String)notNull())).andReturn(spep).anyTimes();

		replay(context);

		assertSame(spep, Initializer.init(context));

		verify(context);
	}


	/**
	 * Test method for {@link com.qut.middleware.spep.Initializer#init(javax.servlet.ServletContext)}.
	 * @throws Exception
	 *
	 * Test behaviour when an invalid context is provided.
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testInit3() throws Exception
	{
		Initializer.init(null);
	}

	/**
	 * Test method for {@link com.qut.middleware.spep.Initializer#init(javax.servlet.ServletContext)}.
	 * @throws Exception
	 *
	 * Test behaviour when properties fail to load.
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testInit4() throws Exception
	{
		ServletContext context = createMock(ServletContext.class);
		expect(context.getAttribute((String)notNull())).andReturn(null).anyTimes();

		expect(context.getResourceAsStream("/WEB-INF/spepvars.config")).andReturn(null).once();

		replay(context);

		Initializer.init(context);
	}


}
