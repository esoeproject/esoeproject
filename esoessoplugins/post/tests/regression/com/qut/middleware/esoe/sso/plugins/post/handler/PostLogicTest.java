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

import static org.easymock.EasyMock.anyBoolean;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.sso.SSOProcessor;
import com.qut.middleware.esoe.sso.bean.SSOProcessorData;
import com.qut.middleware.esoe.sso.bean.impl.SSOProcessorDataImpl;
import com.qut.middleware.esoe.sso.exception.SSOException;
import com.qut.middleware.esoe.sso.plugins.post.bean.PostBindingData;
import com.qut.middleware.esoe.sso.plugins.post.exception.PostBindingException;
import com.qut.middleware.esoe.sso.plugins.post.handler.impl.PostLogicImpl;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.schemas.protocol.AuthnRequest;

public class PostLogicTest
{
	private List<Object> mocked;
	private PostLogic postLogic;
	private SSOProcessor processor;
	private AuthnRequest authnRequest;

	@Before
	public void setUp() throws Exception
	{
		this.mocked = new ArrayList<Object>();
		this.authnRequest = new AuthnRequest();
		
		this.postLogic = new PostLogicImpl();
		
		this.processor = createMock(SSOProcessor.class);
		expect(this.processor.unmarshallRequest((byte[])notNull(), anyBoolean())).andReturn(this.authnRequest).once();
		expect(this.processor.unmarshallRequest((byte[])eq(null), anyBoolean())).andThrow(new UnmarshallerException("message", null, null)).anyTimes();
		this.mocked.add(this.processor);
	}
	
	public void startMock()
	{
		for (Object o : this.mocked)
		{
			replay(o);
		}
	}
	
	public void endMock()
	{
		for (Object o : this.mocked)
		{
			verify(o);
		}
	}
	
	@Test
	public void testHandleRequest1a() throws Exception
	{
		SSOProcessorData data = new SSOProcessorDataImpl();
		data.setRemoteAddress("127.0.0.1");
		PostBindingData bindingData = new PostBindingData();
		
		data.setSSOProcessor(this.processor);
		data.setRequestDocument(new byte[0]);
		
		this.processor.processAuthnRequest(eq(data));
		expectLastCall().once();
		
		startMock();
		
		this.postLogic.handlePostRequest(data, bindingData);
		
		endMock();
	}
	
	@Test(expected = PostBindingException.class)
	public void testHandleRequest1b() throws Exception
	{
		SSOProcessorData data = new SSOProcessorDataImpl();
		data.setRemoteAddress("127.0.0.1");
		PostBindingData bindingData = new PostBindingData();
		
		data.setSSOProcessor(this.processor);
		data.setRequestDocument(new byte[0]);
		
		this.processor.processAuthnRequest(eq(data));
		expectLastCall().andThrow(new SSOException());
		
		startMock();
		
		this.postLogic.handlePostRequest(data, bindingData);
		
		endMock();
	}
	
	@Test(expected = PostBindingException.class)
	public void testHandleRequest2a() throws Exception
	{
		SSOProcessorData data = new SSOProcessorDataImpl();
		data.setRemoteAddress("127.0.0.1");
		PostBindingData bindingData = new PostBindingData();
		
		data.setSSOProcessor(this.processor);
		//data.setRequestDocument(new byte[0]);
		
		this.processor.processAuthnRequest(eq(data));
		expectLastCall().once();
		
		startMock();
		
		this.postLogic.handlePostRequest(data, bindingData);
		
		endMock();
	}

	
	@Test(expected = PostBindingException.class)
	public void testHandleRequest2b() throws Exception
	{
		SSOProcessorData data = new SSOProcessorDataImpl();
		data.setRemoteAddress("127.0.0.1");
		PostBindingData bindingData = new PostBindingData();
		
		//data.setSSOProcessor(this.processor);
		data.setRequestDocument(new byte[0]);
		
		this.processor.processAuthnRequest(eq(data));
		expectLastCall().once();
		
		startMock();
		
		this.postLogic.handlePostRequest(data, bindingData);
		
		endMock();
	}
}
