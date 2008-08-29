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
 * Creation Date: 27/05/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.metadata.processor;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.util.Random;

import org.junit.*;

import com.qut.middleware.metadata.bean.impl.EntityDataImpl;
import com.qut.middleware.metadata.bean.saml.SPEPRole;
import com.qut.middleware.metadata.bean.saml.ServiceProviderRole;
import com.qut.middleware.metadata.processor.saml.impl.SAMLServiceProviderProcessor;
import com.qut.middleware.saml2.SchemaConstants;
import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.schemas.metadata.AttributeConsumingService;
import com.qut.middleware.saml2.schemas.metadata.EntityDescriptor;
import com.qut.middleware.saml2.schemas.metadata.Extensions;
import com.qut.middleware.saml2.schemas.metadata.IndexedEndpointType;
import com.qut.middleware.saml2.schemas.metadata.RequestedAttribute;
import com.qut.middleware.saml2.schemas.metadata.SPSSODescriptor;
import com.qut.middleware.saml2.schemas.metadata.extensions.CacheClearService;

@SuppressWarnings("all")
public class SAMLServiceProviderProcessorTest
{
	private String testSourceLocation = "http://source.example.com/test";
	private String trustedID = "http://entity.example.com";
	private String binding = "binding";
	private String ssoLocation = "http://entity.example.com/sso";
	private String logoutLocation = "http://entity.example.com/logout";
	private String cacheClearLocation = "http://entity.example.com/cacheClear";
	private String nameIDFormat = "nameID";
	private String keyName = "keyName";
	
	private String[] cacheClearSchemas = new String[]{SchemaConstants.cacheClearService, SchemaConstants.samlMetadata};
	private Marshaller<CacheClearService> cacheClearServiceMarshaller;

	@Before
	public void setup() throws Exception
	{
		this.cacheClearServiceMarshaller = new MarshallerImpl<CacheClearService>(CacheClearService.class.getPackage().getName(), this.cacheClearSchemas);
	}
	
	@Test
	public void testProcessSAMLSP() throws Exception
	{
		int index = 0;

		SAMLServiceProviderProcessor processor = new SAMLServiceProviderProcessor();
		
		EntityDescriptor entityDescriptor = new EntityDescriptor();
		
		IndexedEndpointType ssoEndpoint = new IndexedEndpointType();
		ssoEndpoint.setBinding(binding);
		ssoEndpoint.setIndex(index);
		ssoEndpoint.setLocation(ssoLocation);
		IndexedEndpointType logoutEndpoint = new IndexedEndpointType();
		logoutEndpoint.setBinding(binding);
		logoutEndpoint.setIndex(index);
		logoutEndpoint.setLocation(logoutLocation);
		
		SPSSODescriptor spSSODescriptor = new SPSSODescriptor();
		spSSODescriptor.getAssertionConsumerServices().add(ssoEndpoint);
		spSSODescriptor.getSingleLogoutServices().add(logoutEndpoint);
		entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(spSSODescriptor);
		
		EntityDataImpl entityData = new EntityDataImpl(testSourceLocation, 0);
		entityData.setRandom(new Random());
		processor.process(entityData, entityDescriptor);
		
		ServiceProviderRole spRole = entityData.getRoleData(ServiceProviderRole.class);
		assertNotNull(spRole);
		assertFalse(spRole instanceof SPEPRole);
		assertTrue(spRole.getAssertionConsumerServiceEndpoint(binding, index).equals(ssoLocation));
		assertTrue(spRole.getSingleLogoutServiceEndpoint(binding).equals(logoutLocation));
	}
	
	@Test
	public void testProcessSPEP() throws Exception
	{
		int index = 0;

		SAMLServiceProviderProcessor processor = new SAMLServiceProviderProcessor();
		
		EntityDescriptor entityDescriptor = new EntityDescriptor();
		
		IndexedEndpointType ssoEndpoint = new IndexedEndpointType();
		ssoEndpoint.setBinding(binding);
		ssoEndpoint.setIndex(index);
		ssoEndpoint.setLocation(ssoLocation);
		IndexedEndpointType logoutEndpoint = new IndexedEndpointType();
		logoutEndpoint.setBinding(binding);
		logoutEndpoint.setIndex(index);
		logoutEndpoint.setLocation(logoutLocation);
		CacheClearService cacheClearEndpoint = new CacheClearService();
		cacheClearEndpoint.setBinding(binding);
		cacheClearEndpoint.setIndex(index);
		cacheClearEndpoint.setLocation(cacheClearLocation);
		
		Extensions extensions = new Extensions();
		extensions.getImplementedExtensions().add(this.cacheClearServiceMarshaller.marshallUnSignedElement(cacheClearEndpoint));
		
		SPSSODescriptor spSSODescriptor = new SPSSODescriptor();
		spSSODescriptor.getAssertionConsumerServices().add(ssoEndpoint);
		spSSODescriptor.getSingleLogoutServices().add(logoutEndpoint);
		spSSODescriptor.setExtensions(extensions);
		
		entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(spSSODescriptor);
		
		EntityDataImpl entityData = new EntityDataImpl(testSourceLocation, 0);
		entityData.setRandom(new Random());
		processor.process(entityData, entityDescriptor);
		
		SPEPRole spepRole = entityData.getRoleData(SPEPRole.class);
		assertNotNull(spepRole);
		assertTrue(spepRole.getAssertionConsumerServiceEndpoint(binding, index).equals(ssoLocation));
		assertTrue(spepRole.getSingleLogoutServiceEndpoint(binding).equals(logoutLocation));
		assertTrue(spepRole.getCacheClearServiceEndpoint(binding, index).equals(cacheClearLocation));
	}
	
	public void testProcessSPAttributes() throws Exception
	{
		int index = 0;

		SAMLServiceProviderProcessor processor = new SAMLServiceProviderProcessor();
		
		EntityDescriptor entityDescriptor = new EntityDescriptor();
		
		IndexedEndpointType ssoEndpoint = new IndexedEndpointType();
		ssoEndpoint.setBinding(binding);
		ssoEndpoint.setIndex(index);
		ssoEndpoint.setLocation(ssoLocation);
		IndexedEndpointType logoutEndpoint = new IndexedEndpointType();
		logoutEndpoint.setBinding(binding);
		logoutEndpoint.setIndex(index);
		logoutEndpoint.setLocation(logoutLocation);
		
		String friendlyName = "uid";
		String name = "uid";
		String nameFormat = "nameFormat";
		
		RequestedAttribute requestedAttribute = new RequestedAttribute();
		requestedAttribute.setFriendlyName(friendlyName);
		requestedAttribute.setName(name);
		requestedAttribute.setNameFormat(nameFormat);
		requestedAttribute.setIsRequired(true);
		
		AttributeConsumingService attributeConsumingService = new AttributeConsumingService();
		attributeConsumingService.setIndex(index);
		attributeConsumingService.setIsDefault(false);
		attributeConsumingService.getRequestedAttributes().add(requestedAttribute);
		
		SPSSODescriptor spSSODescriptor = new SPSSODescriptor();
		spSSODescriptor.getAssertionConsumerServices().add(ssoEndpoint);
		spSSODescriptor.getSingleLogoutServices().add(logoutEndpoint);
		spSSODescriptor.getAttributeConsumingServices().add(attributeConsumingService);
		entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(spSSODescriptor);
		
		EntityDataImpl entityData = new EntityDataImpl(testSourceLocation, 0);
		entityData.setRandom(new Random());
		processor.process(entityData, entityDescriptor);
		
		ServiceProviderRole spRole = entityData.getRoleData(ServiceProviderRole.class);
		assertNotNull(spRole);
		assertFalse(spRole instanceof SPEPRole);
		assertTrue(spRole.getAssertionConsumerServiceEndpoint(binding, index).equals(ssoLocation));
		assertTrue(spRole.getSingleLogoutServiceEndpoint(binding).equals(logoutLocation));
		assertEquals(name, spRole.getAttributeConsumingService(index).getRequestedAttributes().get(0).getName());
	}
}
