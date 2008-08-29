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
 * Creation Date: 21/05/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.metadata.bean;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.qut.middleware.metadata.bean.saml.endpoint.Endpoint;
import com.qut.middleware.metadata.bean.saml.endpoint.EndpointCollection;
import com.qut.middleware.metadata.bean.saml.endpoint.IndexedEndpoint;
import com.qut.middleware.metadata.bean.saml.endpoint.IndexedEndpointCollection;
import com.qut.middleware.metadata.bean.saml.endpoint.impl.EndpointCollectionImpl;
import com.qut.middleware.metadata.bean.saml.endpoint.impl.EndpointImpl;
import com.qut.middleware.metadata.bean.saml.endpoint.impl.IndexedEndpointCollectionImpl;
import com.qut.middleware.metadata.bean.saml.endpoint.impl.IndexedEndpointImpl;

@SuppressWarnings("all")
public class EndpointCollectionTest
{
	private Random random;
	private String bindingA = "bindingA";
	private String bindingB = "bindingB";
	private String locationPrefix = "location";
	private String location1 = "location1";
	private String location2 = "location2";
	private String location3 = "location3";
	private String location4 = "location4";
	private String location5 = "location5";
	private String location6 = "location6";
	private String location7 = "location7";
	private String location8 = "location8";
	private int index1 = 1;
	private int index2 = 2;
	private int index3 = 3;
	private int index4 = 4;
	private int index5 = 1;
	private int index6 = 2;
	private int index7 = 1;
	private int index8 = 2;

	@Before
	public void startup()
	{
		this.random = new Random();
	}
	
	@Test
	public void testEndpointCollectionImpl()
	{
		EndpointCollection endpointCollection = new EndpointCollectionImpl(this.random);
		
		Endpoint endpoint1 = new EndpointImpl(bindingA, location1);
		Endpoint endpoint2 = new EndpointImpl(bindingB, location2);
		Endpoint endpoint3 = new EndpointImpl(bindingA, location3);
		Endpoint endpoint4 = new EndpointImpl(bindingB, location4);
		Endpoint endpoint5 = new EndpointImpl(bindingA, location5);
		Endpoint endpoint6 = new EndpointImpl(bindingB, location6);
		Endpoint endpoint7 = new EndpointImpl(bindingA, location7);
		Endpoint endpoint8 = new EndpointImpl(bindingB, location8);
		
		endpointCollection.getEndpointList().add(endpoint1);

		assertNull(endpointCollection.getEndpoint(bindingB));

		endpointCollection.getEndpointList().add(endpoint2);
		
		assertTrue(endpointCollection.getEndpoint(bindingA).equals(location1));
		assertTrue(endpointCollection.getEndpoint(bindingB).equals(location2));
		
		endpointCollection.getEndpointList().add(endpoint3);
		endpointCollection.getEndpointList().add(endpoint4);
		endpointCollection.getEndpointList().add(endpoint5);
		endpointCollection.getEndpointList().add(endpoint6);
		endpointCollection.getEndpointList().add(endpoint7);
		endpointCollection.getEndpointList().add(endpoint8);

		assertTrue(endpointCollection.getEndpoint(bindingA).startsWith(locationPrefix));
		assertTrue(endpointCollection.getEndpoint(bindingB).startsWith(locationPrefix));
	}
	
	@Test
	public void testIndexedEndpointCollectionImpl()
	{
		IndexedEndpointCollection endpointCollection = new IndexedEndpointCollectionImpl(this.random);
		
		IndexedEndpoint endpoint1 = new IndexedEndpointImpl(bindingA, location1, index1);
		IndexedEndpoint endpoint2 = new IndexedEndpointImpl(bindingB, location2, index2);
		IndexedEndpoint endpoint3 = new IndexedEndpointImpl(bindingA, location3, index3);
		IndexedEndpoint endpoint4 = new IndexedEndpointImpl(bindingB, location4, index4);
		IndexedEndpoint endpoint5 = new IndexedEndpointImpl(bindingA, location5, index5);
		IndexedEndpoint endpoint6 = new IndexedEndpointImpl(bindingB, location6, index6);
		IndexedEndpoint endpoint7 = new IndexedEndpointImpl(bindingA, location7, index7);
		IndexedEndpoint endpoint8 = new IndexedEndpointImpl(bindingB, location8, index8);
		
		endpointCollection.getEndpointList().add(endpoint1);

		assertEquals(0, endpointCollection.getEndpointList(bindingB).size());

		endpointCollection.getEndpointList().add(endpoint2);
		
		assertEquals(1, endpointCollection.getEndpointList(bindingA).size());
		assertTrue(endpointCollection.getEndpointList(bindingA).get(0).getLocation().equals(location1));
		assertEquals(1, endpointCollection.getEndpointList(bindingB).size());
		assertTrue(endpointCollection.getEndpointList(bindingB).get(0).getLocation().equals(location2));
		
		endpointCollection.getEndpointList().add(endpoint3);
		endpointCollection.getEndpointList().add(endpoint4);
		endpointCollection.getEndpointList().add(endpoint5);
		endpointCollection.getEndpointList().add(endpoint6);
		endpointCollection.getEndpointList().add(endpoint7);
		endpointCollection.getEndpointList().add(endpoint8);

		assertTrue(endpointCollection.getEndpoint(bindingA, index1).startsWith(locationPrefix));
		assertTrue(endpointCollection.getEndpoint(bindingB, index2).startsWith(locationPrefix));
		assertTrue(endpointCollection.getEndpoint(bindingA, index3).equals(location3));
		assertTrue(endpointCollection.getEndpoint(bindingB, index4).equals(location4));
	}
}
