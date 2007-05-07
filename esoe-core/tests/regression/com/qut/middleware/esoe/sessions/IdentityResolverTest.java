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
 * Purpose: Tests the identity resolver implementation.
 */
package com.qut.middleware.esoe.sessions;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.qut.middleware.esoe.sessions.identity.IdentityResolver;
import com.qut.middleware.esoe.sessions.identity.impl.IdentityResolverImpl;
import com.qut.middleware.esoe.sessions.identity.pipeline.Handler;
import com.qut.middleware.esoe.sessions.identity.pipeline.impl.NullHandlerImpl;

/** */
@SuppressWarnings("nls")
public class IdentityResolverTest
{
	private IdentityResolver resolver;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		this.resolver = new IdentityResolverImpl(new Vector<Handler>(0,1));
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sessions.identity.impl.IdentityResolverImpl#registerHandler(com.qut.middleware.esoe.sessions.identity.pipeline.Handler)}.
	 */
	@Test
	public final void testRegisterHandler()
	{
		Handler handler1 = new NullHandlerImpl();
		Handler handler2 = new NullHandlerImpl();
		Handler handler3 = new NullHandlerImpl();
		Handler handler4 = new NullHandlerImpl();

		this.resolver.registerHandler(handler1);
		this.resolver.registerHandler(handler2);
		this.resolver.registerHandler(handler3);
		this.resolver.registerHandler(handler4);

		List<Handler> list = new ArrayList<Handler>();
		list.add(handler1);
		list.add(handler2);
		list.add(handler3);
		list.add(handler4);

		assertTrue("Handler expected to be found in list.", this.resolver.getRegisteredHandlers().containsAll(list));
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sessions.identity.impl.IdentityResolverImpl#execute(com.qut.middleware.esoe.sessions.bean.IdentityData)}.
	 */
	@Test
	@Ignore
	public final void testExecute()
	{
		fail("Not yet implemented");
	}

}
