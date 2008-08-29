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
 * Creation Date: 23/05/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.metadata.processor;

import java.util.Random;

import javax.swing.text.ChangedCharSetException;

import org.junit.Before;
import org.junit.Test;
import org.w3._2000._09.xmldsig_.KeyInfo;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import com.qut.middleware.metadata.bean.EntityData;
import com.qut.middleware.metadata.bean.impl.EntityDataImpl;
import com.qut.middleware.metadata.bean.saml.ESOERole;
import com.qut.middleware.metadata.bean.saml.IdentityProviderRole;
import com.qut.middleware.metadata.bean.saml.TrustedESOERole;
import com.qut.middleware.metadata.exception.InvalidMetadataException;
import com.qut.middleware.metadata.processor.saml.SAMLEntityDescriptorProcessor;
import com.qut.middleware.metadata.processor.saml.impl.SAMLIdentityProviderProcessor;
import com.qut.middleware.saml2.SchemaConstants;
import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.schemas.metadata.AttributeAuthorityDescriptor;
import com.qut.middleware.saml2.schemas.metadata.EndpointType;
import com.qut.middleware.saml2.schemas.metadata.EntityDescriptor;
import com.qut.middleware.saml2.schemas.metadata.Extensions;
import com.qut.middleware.saml2.schemas.metadata.IDPSSODescriptor;
import com.qut.middleware.saml2.schemas.metadata.KeyDescriptor;
import com.qut.middleware.saml2.schemas.metadata.extensions.CacheClearService;
import com.qut.middleware.saml2.schemas.metadata.extensions.SPEPStartupService;
import com.qut.middleware.saml2.schemas.metadata.lxacml.LXACMLPDPDescriptor;

@SuppressWarnings("all")
public class SAMLIdentityProviderProcessorTest
{
	private String testSourceLocation = "http://source.example.com/test";
	private String nonTrustedID = "http://nontrusted.example.com";
	private String trustedID = "http://entity.example.com";
	private String binding = "binding";
	private String ssoLocation = "http://entity.example.com/sso";
	private String logoutLocation = "http://entity.example.com/logout";
	private String attributeLocation = "http://entity.example.com/attribute";
	private String authzLocation = "http://entity.example.com/authz";
	private String startupLocation = "http://entity.example.com/startup";
	private String nameIDFormat = "nameID";
	private String keyName = "keyName";
	private String protocol = "protocol";
	
	private String[] startupSchemas = new String[]{SchemaConstants.spepStartupService, SchemaConstants.samlMetadata};
	private Marshaller<SPEPStartupService> startupServiceMarshaller;
	private String[] lxacmlSchemas = new String[]{SchemaConstants.lxacmlMetadata, SchemaConstants.samlMetadata};
	private Marshaller<LXACMLPDPDescriptor> lxacmlMarshaller;
	
	@Before
	public void setup() throws Exception
	{
		this.startupServiceMarshaller = new MarshallerImpl<SPEPStartupService>(SPEPStartupService.class.getPackage().getName(), startupSchemas);
		this.lxacmlMarshaller = new MarshallerImpl<LXACMLPDPDescriptor>(LXACMLPDPDescriptor.class.getPackage().getName(), lxacmlSchemas);
	}
	
	@Test
	public void testProcessSAMLIdP() throws Exception
	{
		EntityDescriptor entityDescriptor = new EntityDescriptor();
		
		EndpointType ssoEndpoint = new EndpointType();
		ssoEndpoint.setBinding(binding);
		ssoEndpoint.setLocation(ssoLocation);
		
		EndpointType logoutEndpoint = new EndpointType();
		logoutEndpoint.setBinding(binding);
		logoutEndpoint.setLocation(logoutLocation);
		
		IDPSSODescriptor idpSSODescriptor = new IDPSSODescriptor();
		idpSSODescriptor.getSingleSignOnServices().add(ssoEndpoint);
		idpSSODescriptor.getSingleLogoutServices().add(logoutEndpoint);
		idpSSODescriptor.getNameIDFormats().add(nameIDFormat);
		
		KeyDescriptor keyDescriptor = new KeyDescriptor();
		KeyInfo keyInfo = new KeyInfo();
		keyInfo.setId(keyName);
		keyDescriptor.setKeyInfo(keyInfo);
		
		idpSSODescriptor.getKeyDescriptors().add(keyDescriptor);
		
		entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(idpSSODescriptor);
		entityDescriptor.setEntityID(nonTrustedID);
		
		SAMLEntityDescriptorProcessor processor = new SAMLIdentityProviderProcessor(trustedID);
		
		EntityDataImpl entityData = new EntityDataImpl(testSourceLocation, 0);
		entityData.setRandom(new Random());
		processor.process(entityData, entityDescriptor);
		
		IdentityProviderRole role = entityData.getRoleData(IdentityProviderRole.class);
		assertNotNull(role);
		assertFalse(role instanceof ESOERole);
		
		assertEquals(logoutLocation, role.getSingleLogoutServiceEndpoint(binding));
		assertEquals(ssoLocation, role.getSingleSignOnService(binding));
		assertTrue(role.getNameIDFormatList().contains(nameIDFormat));
		assertEquals(1, role.getNameIDFormatList().size());
		assertTrue(role.getKeyNames().contains(keyName));
		assertEquals(1, role.getKeyNames().size());
	}
	
	@Test
	public void testProcessESOE() throws Exception
	{
		EntityDescriptor entityDescriptor = new EntityDescriptor();
		
		EndpointType ssoEndpoint = new EndpointType();
		ssoEndpoint.setBinding(binding);
		ssoEndpoint.setLocation(ssoLocation);
		
		EndpointType logoutEndpoint = new EndpointType();
		logoutEndpoint.setBinding(binding);
		logoutEndpoint.setLocation(logoutLocation);
		
		IDPSSODescriptor idpSSODescriptor = new IDPSSODescriptor();
		idpSSODescriptor.getSingleSignOnServices().add(ssoEndpoint);
		idpSSODescriptor.getSingleLogoutServices().add(logoutEndpoint);
		idpSSODescriptor.getNameIDFormats().add(nameIDFormat);
		
		EndpointType authzEndpoint = new EndpointType();
		authzEndpoint.setBinding(binding);
		authzEndpoint.setLocation(authzLocation);
		LXACMLPDPDescriptor lxacmlPDPDescriptor = new LXACMLPDPDescriptor();
		lxacmlPDPDescriptor.getAuthzServices().add(authzEndpoint);
		entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(lxacmlPDPDescriptor);
		
		EndpointType attributeEndpoint = new EndpointType();
		attributeEndpoint.setBinding(binding);
		attributeEndpoint.setLocation(attributeLocation);
		AttributeAuthorityDescriptor attributeAuthorityDescriptor = new AttributeAuthorityDescriptor();
		attributeAuthorityDescriptor.getAttributeServices().add(attributeEndpoint);
		entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(attributeAuthorityDescriptor);
		
		KeyDescriptor keyDescriptor = new KeyDescriptor();
		KeyInfo keyInfo = new KeyInfo();
		keyInfo.setId(keyName);
		keyDescriptor.setKeyInfo(keyInfo);
		
		idpSSODescriptor.getKeyDescriptors().add(keyDescriptor);
		
		entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(idpSSODescriptor);
		entityDescriptor.setEntityID(nonTrustedID);
		
		SAMLEntityDescriptorProcessor processor = new SAMLIdentityProviderProcessor(trustedID);
		
		EntityDataImpl entityData = new EntityDataImpl(testSourceLocation, 0);
		entityData.setRandom(new Random());
		processor.process(entityData, entityDescriptor);
		
		ESOERole role = entityData.getRoleData(ESOERole.class);
		assertNotNull(role);
		assertFalse(role instanceof TrustedESOERole);
		
		assertEquals(logoutLocation, role.getSingleLogoutServiceEndpoint(binding));
		assertEquals(ssoLocation, role.getSingleSignOnService(binding));
		assertEquals(attributeLocation, role.getAttributeServiceEndpoint(binding));
		assertTrue(role.getNameIDFormatList().contains(nameIDFormat));
		assertEquals(1, role.getNameIDFormatList().size());
		assertTrue(role.getKeyNames().contains(keyName));
		assertEquals(1, role.getKeyNames().size());
	}
	
	@Test
	public void testProcessTrustedESOE1() throws Exception
	{
		// LXACML PDP Descriptor in role descriptors.
		EntityDescriptor entityDescriptor = new EntityDescriptor();
		
		EndpointType ssoEndpoint = new EndpointType();
		ssoEndpoint.setBinding(binding);
		ssoEndpoint.setLocation(ssoLocation);
		
		EndpointType logoutEndpoint = new EndpointType();
		logoutEndpoint.setBinding(binding);
		logoutEndpoint.setLocation(logoutLocation);
		
		IDPSSODescriptor idpSSODescriptor = new IDPSSODescriptor();
		idpSSODescriptor.getSingleSignOnServices().add(ssoEndpoint);
		idpSSODescriptor.getSingleLogoutServices().add(logoutEndpoint);
		idpSSODescriptor.getNameIDFormats().add(nameIDFormat);
		
		EndpointType authzEndpoint = new EndpointType();
		authzEndpoint.setBinding(binding);
		authzEndpoint.setLocation(authzLocation);
		LXACMLPDPDescriptor lxacmlPDPDescriptor = new LXACMLPDPDescriptor();
		lxacmlPDPDescriptor.getAuthzServices().add(authzEndpoint);
		entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(lxacmlPDPDescriptor);
		
		EndpointType attributeEndpoint = new EndpointType();
		attributeEndpoint.setBinding(binding);
		attributeEndpoint.setLocation(attributeLocation);
		AttributeAuthorityDescriptor attributeAuthorityDescriptor = new AttributeAuthorityDescriptor();
		attributeAuthorityDescriptor.getAttributeServices().add(attributeEndpoint);
		entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(attributeAuthorityDescriptor);
		
		SPEPStartupService startupEndpoint = new SPEPStartupService();
		startupEndpoint.setBinding(binding);
		startupEndpoint.setLocation(startupLocation);
		Extensions extensions = new Extensions();
		extensions.getImplementedExtensions().add(this.startupServiceMarshaller.marshallUnSignedElement(startupEndpoint));
		idpSSODescriptor.setExtensions(extensions);
		
		KeyDescriptor keyDescriptor = new KeyDescriptor();
		KeyInfo keyInfo = new KeyInfo();
		keyInfo.setId(keyName);
		keyDescriptor.setKeyInfo(keyInfo);
		
		idpSSODescriptor.getKeyDescriptors().add(keyDescriptor);
		
		entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(idpSSODescriptor);
		entityDescriptor.setEntityID(trustedID);
		
		SAMLEntityDescriptorProcessor processor = new SAMLIdentityProviderProcessor(trustedID);
		
		EntityDataImpl entityData = new EntityDataImpl(testSourceLocation, 0);
		entityData.setRandom(new Random());
		processor.process(entityData, entityDescriptor);
		
		TrustedESOERole role = entityData.getRoleData(TrustedESOERole.class);
		assertNotNull(role);
		
		assertEquals(logoutLocation, role.getSingleLogoutServiceEndpoint(binding));
		assertEquals(ssoLocation, role.getSingleSignOnService(binding));
		assertEquals(attributeLocation, role.getAttributeServiceEndpoint(binding));
		assertEquals(authzLocation, role.getLXACMLAuthzServiceEndpoint(binding));
		assertEquals(startupLocation, role.getSPEPStartupServiceEndpoint(binding));
		assertTrue(role.getNameIDFormatList().contains(nameIDFormat));
		assertEquals(1, role.getNameIDFormatList().size());
		assertTrue(role.getKeyNames().contains(keyName));
		assertEquals(1, role.getKeyNames().size());
	}
	
	@Test
	public void testProcessTrustedESOE2() throws Exception
	{
		// LXACML PDP Descriptor in extensions.
		EntityDescriptor entityDescriptor = new EntityDescriptor();
		
		EndpointType ssoEndpoint = new EndpointType();
		ssoEndpoint.setBinding(binding);
		ssoEndpoint.setLocation(ssoLocation);
		
		EndpointType logoutEndpoint = new EndpointType();
		logoutEndpoint.setBinding(binding);
		logoutEndpoint.setLocation(logoutLocation);
		
		IDPSSODescriptor idpSSODescriptor = new IDPSSODescriptor();
		idpSSODescriptor.getSingleSignOnServices().add(ssoEndpoint);
		idpSSODescriptor.getSingleLogoutServices().add(logoutEndpoint);
		idpSSODescriptor.getNameIDFormats().add(nameIDFormat);
		
		EndpointType authzEndpoint = new EndpointType();
		authzEndpoint.setBinding(binding);
		authzEndpoint.setLocation(authzLocation);
		LXACMLPDPDescriptor lxacmlPDPDescriptor = new LXACMLPDPDescriptor();
		lxacmlPDPDescriptor.getAuthzServices().add(authzEndpoint);
		lxacmlPDPDescriptor.getProtocolSupportEnumerations().add(protocol);
		
		Extensions entityExtensions = new Extensions();
		entityExtensions.getImplementedExtensions().add(this.lxacmlMarshaller.marshallUnSignedElement(lxacmlPDPDescriptor));
		entityDescriptor.setExtensions(entityExtensions);
		
		EndpointType attributeEndpoint = new EndpointType();
		attributeEndpoint.setBinding(binding);
		attributeEndpoint.setLocation(attributeLocation);
		AttributeAuthorityDescriptor attributeAuthorityDescriptor = new AttributeAuthorityDescriptor();
		attributeAuthorityDescriptor.getAttributeServices().add(attributeEndpoint);
		entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(attributeAuthorityDescriptor);
		
		SPEPStartupService startupEndpoint = new SPEPStartupService();
		startupEndpoint.setBinding(binding);
		startupEndpoint.setLocation(startupLocation);
		Extensions extensions = new Extensions();
		extensions.getImplementedExtensions().add(this.startupServiceMarshaller.marshallUnSignedElement(startupEndpoint));
		idpSSODescriptor.setExtensions(extensions);
		
		KeyDescriptor keyDescriptor = new KeyDescriptor();
		KeyInfo keyInfo = new KeyInfo();
		keyInfo.setId(keyName);
		keyDescriptor.setKeyInfo(keyInfo);
		
		idpSSODescriptor.getKeyDescriptors().add(keyDescriptor);
		
		entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(idpSSODescriptor);
		entityDescriptor.setEntityID(trustedID);
		
		SAMLEntityDescriptorProcessor processor = new SAMLIdentityProviderProcessor(trustedID);
		
		EntityDataImpl entityData = new EntityDataImpl(testSourceLocation, 0);
		entityData.setRandom(new Random());
		processor.process(entityData, entityDescriptor);
		
		TrustedESOERole role = entityData.getRoleData(TrustedESOERole.class);
		assertNotNull(role);
		
		assertEquals(logoutLocation, role.getSingleLogoutServiceEndpoint(binding));
		assertEquals(ssoLocation, role.getSingleSignOnService(binding));
		assertEquals(attributeLocation, role.getAttributeServiceEndpoint(binding));
		assertEquals(authzLocation, role.getLXACMLAuthzServiceEndpoint(binding));
		assertEquals(startupLocation, role.getSPEPStartupServiceEndpoint(binding));
		assertTrue(role.getNameIDFormatList().contains(nameIDFormat));
		assertEquals(1, role.getNameIDFormatList().size());
		assertTrue(role.getKeyNames().contains(keyName));
		assertEquals(1, role.getKeyNames().size());
	}
	
	@Test(expected = InvalidMetadataException.class)
	public void testProcessInvalidTrustedESOE1() throws Exception
	{
		// No LXACML PDP Descriptor
		EntityDescriptor entityDescriptor = new EntityDescriptor();
		
		EndpointType ssoEndpoint = new EndpointType();
		ssoEndpoint.setBinding(binding);
		ssoEndpoint.setLocation(ssoLocation);
		
		EndpointType logoutEndpoint = new EndpointType();
		logoutEndpoint.setBinding(binding);
		logoutEndpoint.setLocation(logoutLocation);
		
		IDPSSODescriptor idpSSODescriptor = new IDPSSODescriptor();
		idpSSODescriptor.getSingleSignOnServices().add(ssoEndpoint);
		idpSSODescriptor.getSingleLogoutServices().add(logoutEndpoint);
		idpSSODescriptor.getNameIDFormats().add(nameIDFormat);
		
		EndpointType attributeEndpoint = new EndpointType();
		attributeEndpoint.setBinding(binding);
		attributeEndpoint.setLocation(attributeLocation);
		AttributeAuthorityDescriptor attributeAuthorityDescriptor = new AttributeAuthorityDescriptor();
		attributeAuthorityDescriptor.getAttributeServices().add(attributeEndpoint);
		entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(attributeAuthorityDescriptor);
		
		SPEPStartupService startupEndpoint = new SPEPStartupService();
		startupEndpoint.setBinding(binding);
		startupEndpoint.setLocation(startupLocation);
		Extensions extensions = new Extensions();
		extensions.getImplementedExtensions().add(this.startupServiceMarshaller.marshallUnSignedElement(startupEndpoint));
		idpSSODescriptor.setExtensions(extensions);
		
		KeyDescriptor keyDescriptor = new KeyDescriptor();
		KeyInfo keyInfo = new KeyInfo();
		keyInfo.setId(keyName);
		keyDescriptor.setKeyInfo(keyInfo);
		
		idpSSODescriptor.getKeyDescriptors().add(keyDescriptor);
		
		entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(idpSSODescriptor);
		entityDescriptor.setEntityID(trustedID);
		
		SAMLEntityDescriptorProcessor processor = new SAMLIdentityProviderProcessor(trustedID);
		
		EntityDataImpl entityData = new EntityDataImpl(testSourceLocation, 0);
		entityData.setRandom(new Random());
		processor.process(entityData, entityDescriptor);
	}
	
	@Test(expected = InvalidMetadataException.class)
	public void testProcessInvalidTrustedESOE2() throws Exception
	{
		// No SPEP startup endpoint
		EntityDescriptor entityDescriptor = new EntityDescriptor();
		
		EndpointType ssoEndpoint = new EndpointType();
		ssoEndpoint.setBinding(binding);
		ssoEndpoint.setLocation(ssoLocation);
		
		EndpointType logoutEndpoint = new EndpointType();
		logoutEndpoint.setBinding(binding);
		logoutEndpoint.setLocation(logoutLocation);
		
		IDPSSODescriptor idpSSODescriptor = new IDPSSODescriptor();
		idpSSODescriptor.getSingleSignOnServices().add(ssoEndpoint);
		idpSSODescriptor.getSingleLogoutServices().add(logoutEndpoint);
		idpSSODescriptor.getNameIDFormats().add(nameIDFormat);
		
		EndpointType authzEndpoint = new EndpointType();
		authzEndpoint.setBinding(binding);
		authzEndpoint.setLocation(authzLocation);
		LXACMLPDPDescriptor lxacmlPDPDescriptor = new LXACMLPDPDescriptor();
		lxacmlPDPDescriptor.getAuthzServices().add(authzEndpoint);
		lxacmlPDPDescriptor.getProtocolSupportEnumerations().add(protocol);
		
		Extensions entityExtensions = new Extensions();
		entityExtensions.getImplementedExtensions().add(this.lxacmlMarshaller.marshallUnSignedElement(lxacmlPDPDescriptor));
		entityDescriptor.setExtensions(entityExtensions);
		
		EndpointType attributeEndpoint = new EndpointType();
		attributeEndpoint.setBinding(binding);
		attributeEndpoint.setLocation(attributeLocation);
		AttributeAuthorityDescriptor attributeAuthorityDescriptor = new AttributeAuthorityDescriptor();
		attributeAuthorityDescriptor.getAttributeServices().add(attributeEndpoint);
		entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(attributeAuthorityDescriptor);
		
		KeyDescriptor keyDescriptor = new KeyDescriptor();
		KeyInfo keyInfo = new KeyInfo();
		keyInfo.setId(keyName);
		keyDescriptor.setKeyInfo(keyInfo);
		
		idpSSODescriptor.getKeyDescriptors().add(keyDescriptor);
		
		entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(idpSSODescriptor);
		entityDescriptor.setEntityID(trustedID);
		
		SAMLEntityDescriptorProcessor processor = new SAMLIdentityProviderProcessor(trustedID);
		
		EntityDataImpl entityData = new EntityDataImpl(testSourceLocation, 0);
		entityData.setRandom(new Random());
		processor.process(entityData, entityDescriptor);
	}
	
	@Test(expected = InvalidMetadataException.class)
	public void testProcessInvalidTrustedESOE3() throws Exception
	{
		// No Attribute Authority
		EntityDescriptor entityDescriptor = new EntityDescriptor();
		
		EndpointType ssoEndpoint = new EndpointType();
		ssoEndpoint.setBinding(binding);
		ssoEndpoint.setLocation(ssoLocation);
		
		EndpointType logoutEndpoint = new EndpointType();
		logoutEndpoint.setBinding(binding);
		logoutEndpoint.setLocation(logoutLocation);
		
		IDPSSODescriptor idpSSODescriptor = new IDPSSODescriptor();
		idpSSODescriptor.getSingleSignOnServices().add(ssoEndpoint);
		idpSSODescriptor.getSingleLogoutServices().add(logoutEndpoint);
		idpSSODescriptor.getNameIDFormats().add(nameIDFormat);
		
		EndpointType authzEndpoint = new EndpointType();
		authzEndpoint.setBinding(binding);
		authzEndpoint.setLocation(authzLocation);
		LXACMLPDPDescriptor lxacmlPDPDescriptor = new LXACMLPDPDescriptor();
		lxacmlPDPDescriptor.getAuthzServices().add(authzEndpoint);
		lxacmlPDPDescriptor.getProtocolSupportEnumerations().add(protocol);
		
		Extensions entityExtensions = new Extensions();
		entityExtensions.getImplementedExtensions().add(this.lxacmlMarshaller.marshallUnSignedElement(lxacmlPDPDescriptor));
		entityDescriptor.setExtensions(entityExtensions);
		
		SPEPStartupService startupEndpoint = new SPEPStartupService();
		startupEndpoint.setBinding(binding);
		startupEndpoint.setLocation(startupLocation);
		Extensions extensions = new Extensions();
		extensions.getImplementedExtensions().add(this.startupServiceMarshaller.marshallUnSignedElement(startupEndpoint));
		idpSSODescriptor.setExtensions(extensions);
		
		KeyDescriptor keyDescriptor = new KeyDescriptor();
		KeyInfo keyInfo = new KeyInfo();
		keyInfo.setId(keyName);
		keyDescriptor.setKeyInfo(keyInfo);
		
		idpSSODescriptor.getKeyDescriptors().add(keyDescriptor);
		
		entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(idpSSODescriptor);
		entityDescriptor.setEntityID(trustedID);
		
		SAMLEntityDescriptorProcessor processor = new SAMLIdentityProviderProcessor(trustedID);
		
		EntityDataImpl entityData = new EntityDataImpl(testSourceLocation, 0);
		entityData.setRandom(new Random());
		processor.process(entityData, entityDescriptor);
	}
	
	@Test(expected = InvalidMetadataException.class)
	public void testProcessInvalidTrustedESOE4() throws Exception
	{
		// No single sign-on endpoint
		EntityDescriptor entityDescriptor = new EntityDescriptor();
		
		EndpointType ssoEndpoint = new EndpointType();
		ssoEndpoint.setBinding(binding);
		ssoEndpoint.setLocation(ssoLocation);
		
		EndpointType logoutEndpoint = new EndpointType();
		logoutEndpoint.setBinding(binding);
		logoutEndpoint.setLocation(logoutLocation);
		
		IDPSSODescriptor idpSSODescriptor = new IDPSSODescriptor();
		idpSSODescriptor.getSingleLogoutServices().add(logoutEndpoint);
		idpSSODescriptor.getNameIDFormats().add(nameIDFormat);
		
		EndpointType authzEndpoint = new EndpointType();
		authzEndpoint.setBinding(binding);
		authzEndpoint.setLocation(authzLocation);
		LXACMLPDPDescriptor lxacmlPDPDescriptor = new LXACMLPDPDescriptor();
		lxacmlPDPDescriptor.getAuthzServices().add(authzEndpoint);
		lxacmlPDPDescriptor.getProtocolSupportEnumerations().add(protocol);
		
		Extensions entityExtensions = new Extensions();
		entityExtensions.getImplementedExtensions().add(this.lxacmlMarshaller.marshallUnSignedElement(lxacmlPDPDescriptor));
		entityDescriptor.setExtensions(entityExtensions);
		
		EndpointType attributeEndpoint = new EndpointType();
		attributeEndpoint.setBinding(binding);
		attributeEndpoint.setLocation(attributeLocation);
		AttributeAuthorityDescriptor attributeAuthorityDescriptor = new AttributeAuthorityDescriptor();
		attributeAuthorityDescriptor.getAttributeServices().add(attributeEndpoint);
		entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(attributeAuthorityDescriptor);
		
		SPEPStartupService startupEndpoint = new SPEPStartupService();
		startupEndpoint.setBinding(binding);
		startupEndpoint.setLocation(startupLocation);
		Extensions extensions = new Extensions();
		extensions.getImplementedExtensions().add(this.startupServiceMarshaller.marshallUnSignedElement(startupEndpoint));
		idpSSODescriptor.setExtensions(extensions);
		
		KeyDescriptor keyDescriptor = new KeyDescriptor();
		KeyInfo keyInfo = new KeyInfo();
		keyInfo.setId(keyName);
		keyDescriptor.setKeyInfo(keyInfo);
		
		idpSSODescriptor.getKeyDescriptors().add(keyDescriptor);
		
		entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(idpSSODescriptor);
		entityDescriptor.setEntityID(trustedID);
		
		SAMLEntityDescriptorProcessor processor = new SAMLIdentityProviderProcessor(trustedID);
		
		EntityDataImpl entityData = new EntityDataImpl(testSourceLocation, 0);
		entityData.setRandom(new Random());
		processor.process(entityData, entityDescriptor);
	}
}
