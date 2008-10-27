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
 * Author:
 * Creation Date:
 * 
 * Purpose:
 */
package com.qut.middleware.esoe.pdp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

import com.qut.middleware.esoe.authz.cache.bean.FailedAuthzCacheUpdate;
import com.qut.middleware.esoe.authz.cache.bean.impl.FailedAuthzCacheUpdateImpl;


/**
 * @author the Dre
 *
 */
public class FailedAuthzCacheUpdateTest
{

	private FailedAuthzCacheUpdate testFailedUpdate;
	private String testRequest;
	private Date testTimestamp;
	private String testEndpoint;
	
	private static String ENDPOINT = "http://blah.qut.edu.au/"; //$NON-NLS-1$
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		this.testRequest = new String("RequestNode");
		this.testTimestamp = new Date(System.currentTimeMillis());
		this.testEndpoint = ENDPOINT;
		
		this.testFailedUpdate = new FailedAuthzCacheUpdateImpl();
	}


	/**
	 * Test method for {@link com.qut.middleware.esoe.authz.cache.bean.impl.FailedAuthzCacheUpdateImpl#getEndPoint()}.
	 */
	@Test
	public final void testGetEndPoint()
	{
		this.testSetEndPoint();
		
		assertEquals("Endpoint comparison", this.testEndpoint, this.testFailedUpdate.getEndPoint()); //$NON-NLS-1$
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.authz.cache.bean.impl.FailedAuthzCacheUpdateImpl#getRequestDocument()}.
	 */
	@Test
	public final void testGetRequestDocument()
	{
		this.testSetRequestDocument();
		
		assertEquals("Request comparison", this.testRequest, this.testFailedUpdate.getRequestDocument().getNodeName()); //$NON-NLS-1$
	}
	

	/**
	 * Test method for {@link com.qut.middleware.esoe.authz.cache.bean.impl.FailedAuthzCacheUpdateImpl#getTimeStamp()}.
	 */
	@Test
	public final void testGetTimeStamp()
	{
		this.testSetTimeStamp();
		
		assertEquals("Timestamp comparison", this.testTimestamp, this.testFailedUpdate.getTimeStamp()); //$NON-NLS-1$
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.authz.cache.bean.impl.FailedAuthzCacheUpdateImpl#setEndPoint(java.lang.String)}.
	 */
	@Test
	public final void testSetEndPoint()
	{
		this.testFailedUpdate.setEndPoint(this.testEndpoint);
		
		String test = this.testFailedUpdate.getEndPoint();
		
		assertTrue(test != null && test.equals(this.testEndpoint));
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.authz.cache.bean.impl.FailedAuthzCacheUpdateImpl#setRequestDocument(org.opensaml.saml2.core.Request)}.
	 */
	@Test
	public final void testSetRequestDocument()
	{
		this.testFailedUpdate.setRequestDocument(this.getDodgyElement(this.testRequest));
		
		String test = this.testFailedUpdate.getRequestDocument().getNodeName();
		
		assertTrue(test != null && test.equals(this.testRequest) );
	}

	/**
	 * Test method for {@link com.qut.middleware.esoe.authz.cache.bean.impl.FailedAuthzCacheUpdateImpl#setTimeStamp(java.util.Date)}.
	 */
	@Test
	public final void testSetTimeStamp()
	{
		this.testFailedUpdate.setTimeStamp(this.testTimestamp);
		
		assertEquals("Incorrect timestamp recieved", this.testTimestamp, this.testFailedUpdate.getTimeStamp());
	}

	private Element getDodgyElement(String nodename)
	{

		DOMImplementation dom = null;
		try 
		{
			dom = DOMImplementationRegistry.newInstance().getDOMImplementation("XML 1.0");
		}
		catch (Exception e)
		{
			fail("Failed to instantiate DomImplementation.");
			//e.printStackTrace();
		} 
		
		Element elem = null;
		Document doc = null;
		
		try
		{
			doc =  dom.createDocument("http://www.w3.org/2001/XMLSchema", "xs:string", null);
			elem = doc.createElement(nodename);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail("Error occured created Document or Element");
		}
		
		return elem;
	}
}
