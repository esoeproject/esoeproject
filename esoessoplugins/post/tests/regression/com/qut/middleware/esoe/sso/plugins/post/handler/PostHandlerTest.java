/* Copyright 2008, Queensland University of Technology
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
 * Creation Date: 05/11/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.esoe.sso.plugins.post.handler;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.sso.bean.SSOProcessorData;
import com.qut.middleware.esoe.sso.bean.SSOProcessorData.RequestMethod;
import com.qut.middleware.esoe.sso.bean.impl.SSOProcessorDataImpl;
import com.qut.middleware.esoe.sso.constants.SSOConstants;
import com.qut.middleware.esoe.sso.pipeline.Handler.result;
import com.qut.middleware.esoe.sso.plugins.post.bean.PostBindingData;
import com.qut.middleware.esoe.sso.plugins.post.exception.PostBindingException;

public class PostHandlerTest
{

	private PostLogic logic;
	private PostHandler handler;
	private SSOProcessorData data;
	
	private List<Object> mocked;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private String requestString;

	@Before
	public void setUp() throws Exception
	{
		this.mocked = new ArrayList<Object>();
		
		this.logic = createMock(PostLogic.class);
		this.mocked.add(this.logic);
		
		this.data = new SSOProcessorDataImpl();
		this.data.setRemoteAddress("127.0.0.1");
		this.data.setRequestMethod(RequestMethod.HTTP_POST);
		this.data.setIssuerID("http://spep.example.com");
		
		this.request = createMock(HttpServletRequest.class);
		this.response = createMock(HttpServletResponse.class);
		
		this.mocked.add(request);
		this.mocked.add(response);
		
		this.data.setHttpRequest(request);
		this.data.setHttpResponse(response);
		
		this.handler = new PostHandler(this.logic);

		this.requestString = "";
	}
	
	private void startMock()
	{
		for (Object o : this.mocked)
		{
			replay(o);
		}
	}
	
	private void endMock()
	{
		for (Object o : this.mocked)
		{
			verify(o);
		}
	}
	
	@Test
	// Everything null, can't identify binding
	public void testHandleRequest1a() throws Exception
	{
		// Overwrite the semi-sensible values used for most of the tests.
		this.data = new SSOProcessorDataImpl();
		this.data.setRemoteAddress("127.0.0.1");

		startMock();
		
		result theResult = this.handler.executeRequest(this.data);
		
		endMock();
		
		assertEquals(result.NoAction, theResult);
	}
	
	@Test
	// HTTP method not POST, wrong binding
	public void testHandleRequest1b() throws Exception
	{
		this.data.setRequestMethod(RequestMethod.HTTP_GET);
		
		startMock();
		
		result theResult = this.handler.executeRequest(this.data);
		
		endMock();
		
		assertEquals(result.NoAction, theResult);
	}
	
	@Test
	// No parameter called SAMLRequest, wrong binding
	public void testHandleRequest1c() throws Exception
	{
		expect(this.request.getParameter(SSOConstants.SAML_REQUEST_ELEMENT)).andReturn(null).anyTimes();
		expect(this.request.getParameter(SSOConstants.SAML_RELAY_STATE)).andReturn(null).anyTimes();
		
		startMock();
		
		result theResult = this.handler.executeRequest(this.data);
		
		endMock();
		
		assertEquals(result.NoAction, theResult);
	}

	@Test
	// Correct binding, executed correctly.
	public void testHandleRequest2a() throws Exception
	{
		expect(this.request.getParameter(SSOConstants.SAML_REQUEST_ELEMENT)).andReturn(this.requestString).anyTimes();
		expect(this.request.getParameter(SSOConstants.SAML_RELAY_STATE)).andReturn(null).anyTimes();
		
		this.logic.handlePostRequest((SSOProcessorData)notNull(), (PostBindingData)notNull());
		expectLastCall().once();
		
		startMock();
		
		result theResult = this.handler.executeRequest(this.data);
		
		endMock();
		
		assertEquals(result.Successful, theResult);
	}

	@Test
	// Correct binding, but failing to execute
	public void testHandleRequest2b() throws Exception
	{
		expect(this.request.getParameter(SSOConstants.SAML_REQUEST_ELEMENT)).andReturn(this.requestString).anyTimes();
		expect(this.request.getParameter(SSOConstants.SAML_RELAY_STATE)).andReturn(null).anyTimes();
		
		this.logic.handlePostRequest((SSOProcessorData)notNull(), (PostBindingData)notNull());
		expectLastCall().andThrow(new PostBindingException("Test failure.")).once();
		
		startMock();
		
		result theResult = this.handler.executeRequest(this.data);
		
		endMock();
		
		assertEquals(result.InvalidRequest, theResult);
	}

}
