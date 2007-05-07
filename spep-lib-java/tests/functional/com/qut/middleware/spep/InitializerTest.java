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

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;
import static com.qut.middleware.test.functional.Capture.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringBufferInputStream;
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
		// for method coverage only
		new Initializer();
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
		
		InputStream configStream = new FileInputStream(System.getProperty("user.dir") + File.separator + "webapp" + File.separator + "descriptors" + File.separator + "spep.config");
		expect(context.getResourceAsStream("/WEB-INF/spep.config")).andReturn(configStream).once();
		
		InputStream attributeConfigStream = new FileInputStream(System.getProperty("user.dir") + File.separator + "webapp" + File.separator + "descriptors" + File.separator + "attributeProcessor.xml");
		expect(context.getResourceAsStream("/WEB-INF/attributeProcessor.xml")).andReturn(attributeConfigStream).once();
		
		InputStream spKeyStoreStream = new FileInputStream(System.getProperty("user.dir") + File.separator + "secure" + File.separator + "spkeystore.ks");
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
				
		InputStream configStream = new FileInputStream(System.getProperty("user.dir") + File.separator + "webapp" + File.separator + "descriptors" + File.separator + "spep.config");
		expect(context.getResourceAsStream("/WEB-INF/spep.config")).andReturn(null).once();
		
		replay(context);
		
		Initializer.init(context);		
	}	
	
	
}
