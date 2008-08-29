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
 * Creation Date: 30/05/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.metadata;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;
import org.w3._2000._09.xmldsig_.Signature;

import com.qut.middleware.crypto.KeystoreResolver;
import com.qut.middleware.crypto.impl.KeystoreResolverImpl;
import com.qut.middleware.metadata.bean.EntityData;
import com.qut.middleware.metadata.bean.saml.AttributeAuthorityRole;
import com.qut.middleware.metadata.bean.saml.ESOERole;
import com.qut.middleware.metadata.bean.saml.IdentityProviderRole;
import com.qut.middleware.metadata.bean.saml.SPEPRole;
import com.qut.middleware.metadata.bean.saml.ServiceProviderRole;
import com.qut.middleware.metadata.bean.saml.TrustedESOERole;
import com.qut.middleware.metadata.cache.MetadataCache;
import com.qut.middleware.metadata.cache.impl.MetadataCacheImpl;
import com.qut.middleware.metadata.constants.FormatConstants;
import com.qut.middleware.metadata.exception.MetadataCacheUpdateException;
import com.qut.middleware.metadata.exception.MetadataSourceException;
import com.qut.middleware.metadata.processor.DynamicMetadataUpdater;
import com.qut.middleware.metadata.processor.FormatHandler;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.metadata.processor.impl.MetadataProcessorImpl;
import com.qut.middleware.metadata.processor.saml.SAMLEntityDescriptorProcessor;
import com.qut.middleware.metadata.processor.saml.impl.SAMLIdentityProviderProcessor;
import com.qut.middleware.metadata.processor.saml.impl.SAMLMetadataFormatHandler;
import com.qut.middleware.metadata.processor.saml.impl.SAMLServiceProviderProcessor;
import com.qut.middleware.metadata.source.DynamicMetadataSource;
import com.qut.middleware.metadata.source.MetadataSource;
import com.qut.middleware.metadata.source.impl.DynamicMetadataSourceBase;
import com.qut.middleware.metadata.source.impl.MetadataSourceBase;
import com.qut.middleware.metadata.source.impl.URLMetadataSource;
import com.qut.middleware.saml2.LocalKeyResolver;
import com.qut.middleware.saml2.ProtocolConstants;
import com.qut.middleware.saml2.SchemaConstants;
import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.schemas.metadata.AttributeAuthorityDescriptor;
import com.qut.middleware.saml2.schemas.metadata.EndpointType;
import com.qut.middleware.saml2.schemas.metadata.EntitiesDescriptor;
import com.qut.middleware.saml2.schemas.metadata.EntityDescriptor;
import com.qut.middleware.saml2.schemas.metadata.Extensions;
import com.qut.middleware.saml2.schemas.metadata.IDPSSODescriptor;
import com.qut.middleware.saml2.schemas.metadata.IndexedEndpointType;
import com.qut.middleware.saml2.schemas.metadata.SPSSODescriptor;
import com.qut.middleware.saml2.schemas.metadata.extensions.CacheClearService;
import com.qut.middleware.saml2.schemas.metadata.extensions.SPEPStartupService;
import com.qut.middleware.saml2.schemas.metadata.lxacml.LXACMLPDPDescriptor;

/*
 * Keystore was created with:
 * keytool -genkeypair -alias testpriv -keyalg rsa -keysize 1024 -validity 3650 -keystore tests/testdata/testKeystore.ks -storepass kspass -keypass keypass -storetype jks
 * keytool -exportcert -alias testpriv -keystore tests/testdata/testKeystore.ks -storepass kspass -file temp.crt
 * keytool -importcert -alias testcert -keystore tests/testdata/testKeystore.ks -storepass kspass -file temp.crt
 * (keypair was duplicated as a trusted cert so that validation can be done properly.)
 */
@SuppressWarnings("all")
public class CompleteMetadataTest
{
	public static String ssoEndpointPath = "/sso";
	public static String logoutEndpointPath = "/logout";
	public static String aaEndpointPath = "/aa";
	public static String pdpEndpointPath = "/pdp";
	public static String cacheClearEndpointPath = "/cacheClear";
	public static String spepStartupEndpointPath = "/spepStartup";
	public static String binding = "binding";
	
	private static String keystorePath = "tests/testdata/testKeystore.ks";
	private static String keystorePassword = "kspass";
	private static String keyPassword = "keypass";
	private static String keyAlias = "testcert";
	
	private static KeystoreResolver keystoreResolver;
	
	private String[] startupSchemas = new String[]{SchemaConstants.spepStartupService, SchemaConstants.samlMetadata};
	private Marshaller<SPEPStartupService> startupServiceMarshaller;
	private String[] cacheClearSchemas = new String[]{SchemaConstants.cacheClearService, SchemaConstants.samlMetadata};
	private Marshaller<CacheClearService> cacheClearServiceMarshaller;
	private String[] lxacmlPDPDescriptorSchemas = new String[]{SchemaConstants.lxacmlMetadata, SchemaConstants.samlMetadata};
	private Marshaller<LXACMLPDPDescriptor> lxacmlPDPDescriptorMarshaller;
	private String[] metadataSchemas = new String[]{SchemaConstants.samlMetadata, SchemaConstants.cacheClearService, SchemaConstants.spepStartupService};
	private Marshaller<EntitiesDescriptor> entitiesDescriptorMarshaller;
	
	public CompleteMetadataTest() throws Exception
	{
		this.keystoreResolver = new KeystoreResolverImpl(new File(keystorePath), keystorePassword, "testpriv", keyPassword);
		String packages = EntitiesDescriptor.class.getPackage().getName() + ":" + SPEPStartupService.class.getPackage().getName() + ":" + CacheClearService.class.getPackage().getName();
		startupServiceMarshaller = new MarshallerImpl<SPEPStartupService>(packages, startupSchemas);
		cacheClearServiceMarshaller = new MarshallerImpl<CacheClearService>(packages, cacheClearSchemas);
		
		LocalKeyResolver localKeyResolver = createMock(LocalKeyResolver.class);
		expect(localKeyResolver.getLocalCertificate()).andStubReturn(this.keystoreResolver.getLocalCertificate());
		expect(localKeyResolver.getLocalKeyAlias()).andStubReturn(this.keyAlias);
		expect(localKeyResolver.getLocalPrivateKey()).andStubReturn(this.keystoreResolver.getLocalPrivateKey());
		expect(localKeyResolver.getLocalPublicKey()).andStubReturn(this.keystoreResolver.getLocalPublicKey());
		replay(localKeyResolver);
		entitiesDescriptorMarshaller = new MarshallerImpl<EntitiesDescriptor>(packages, metadataSchemas, localKeyResolver);
		
		lxacmlPDPDescriptorMarshaller = new MarshallerImpl<LXACMLPDPDescriptor>(LXACMLPDPDescriptor.class.getPackage().getName(), lxacmlPDPDescriptorSchemas);
	}
	
	@Test
	public void test() throws Exception
	{
		assertTrue(true);
		assertFalse(false);
		assertNull(null);
		
		String domain1 = "example.com";
		EntityGenerator domain1EntityGenerator = new EntityGenerator(domain1);
		
		EntitiesDescriptor entitiesDescriptor = new EntitiesDescriptor();
		
		entitiesDescriptor.setSignature(new Signature());
		entitiesDescriptor.setID("_" + domain1EntityGenerator.generateRandomID(20));
		
		int scale = 10;
		
		for (int i=0; i<1*scale; ++i)
		{
			entitiesDescriptor.getEntitiesDescriptorsAndEntityDescriptors().add(
				domain1EntityGenerator.generateIdPEntityDescriptor(i, true, true, true)
			);
		}
		for (int i=1*scale; i<2*scale; ++i)
		{
			entitiesDescriptor.getEntitiesDescriptorsAndEntityDescriptors().add(
				domain1EntityGenerator.generateIdPEntityDescriptor(i, true, false, false)
			);
		}
		for (int i=2*scale; i<3*scale; ++i)
		{
			entitiesDescriptor.getEntitiesDescriptorsAndEntityDescriptors().add(
				domain1EntityGenerator.generateIdPEntityDescriptor(i, false, false, false)
			);
		}
		for (int i=3*scale; i<4*scale; ++i)
		{
			List<Integer> indices = new ArrayList<Integer>();
			indices.add(1000 + i);
			indices.add(2000 + i);
			entitiesDescriptor.getEntitiesDescriptorsAndEntityDescriptors().add(
				domain1EntityGenerator.generateSPEntityDescriptor(i, (i%2 == 0), indices)
			);
		}
		// Choosing number 0 as the trusted ESOE.
		String trustedEntityID = domain1EntityGenerator.generateEntityID(0);
		final String metadataLocation = "local://test-metadata.example.com";
		final int metadataPriority = 1;
		
		final byte[] metadataDocument = entitiesDescriptorMarshaller.marshallSigned(entitiesDescriptor);
		MetadataSourceBase source = new MetadataSourceBase(){
			public String getFormat()
			{ return FormatConstants.SAML2; }
			public String getLocation()
			{ return metadataLocation; }
			public void updateMetadata(MetadataProcessor processor) throws MetadataSourceException
			{
				try
				{
					this.readMetadata(new ByteArrayInputStream(metadataDocument), processor);
				}
				catch (IOException e)
				{
					fail(e.getMessage());
				}
			}
		};
		
		DynamicMetadataUpdater dynamicMetadataUpdater = new DynamicMetadataUpdater(){
			public EntityData dynamicUpdate(MetadataProcessor processor, String entityID)
			{
				// TODO Auto-generated method stub
				return null;
			}
		};
		
		List<SAMLEntityDescriptorProcessor> samlProcessors = new ArrayList<SAMLEntityDescriptorProcessor>();
		samlProcessors.add(new SAMLIdentityProviderProcessor(trustedEntityID));
		samlProcessors.add(new SAMLServiceProviderProcessor());
		
		List<FormatHandler> formatHandlers = new ArrayList<FormatHandler>();
		formatHandlers.add(new SAMLMetadataFormatHandler(keystoreResolver, samlProcessors));
		
		List<MetadataSource> sources = new ArrayList<MetadataSource>();
		sources.add(source);
		
		MetadataCache cache = new MetadataCacheImpl(dynamicMetadataUpdater);
		MetadataProcessor metadataProcessor = new MetadataProcessorImpl(cache, formatHandlers, sources);
		
		metadataProcessor.update();
		
		assertNotNull(metadataProcessor.getEntityData(trustedEntityID));
		for (int i=0; i<1*scale; ++i)
		{
			String entityID = domain1EntityGenerator.generateEntityID(i);
			EntityData entityData = metadataProcessor.getEntityData(entityID);
			ESOERole esoeRole = entityData.getRoleData(ESOERole.class);
			assertNotNull(esoeRole);
			assertEquals(domain1EntityGenerator.generateEndpoint(i, ssoEndpointPath), esoeRole.getSingleSignOnService(binding));
			assertEquals(domain1EntityGenerator.generateEndpoint(i, aaEndpointPath), esoeRole.getAttributeServiceEndpoint(binding));
			if (entityData.getEntityID().compareTo(trustedEntityID) == 0)
			{
				TrustedESOERole trustedRole = entityData.getRoleData(TrustedESOERole.class);
				assertNotNull(trustedRole);
				
				assertEquals(domain1EntityGenerator.generateEndpoint(i, spepStartupEndpointPath), trustedRole.getSPEPStartupServiceEndpoint(binding));
				assertEquals(domain1EntityGenerator.generateEndpoint(i, pdpEndpointPath), trustedRole.getLXACMLAuthzServiceEndpoint(binding));
			}
		}
		for (int i=1*scale; i<2*scale; ++i)
		{
			String entityID = domain1EntityGenerator.generateEntityID(i);
			EntityData entityData = metadataProcessor.getEntityData(entityID);
			IdentityProviderRole idpRole = entityData.getRoleData(IdentityProviderRole.class);
			AttributeAuthorityRole aaRole = entityData.getRoleData(AttributeAuthorityRole.class);
			assertNotNull(idpRole);
			assertNotNull(aaRole);
			assertEquals(domain1EntityGenerator.generateEndpoint(i, ssoEndpointPath), idpRole.getSingleSignOnService(binding));
			assertEquals(domain1EntityGenerator.generateEndpoint(i, aaEndpointPath), aaRole.getAttributeServiceEndpoint(binding));
		}
		for (int i=2*scale; i<3*scale; ++i)
		{
			String entityID = domain1EntityGenerator.generateEntityID(i);
			EntityData entityData = metadataProcessor.getEntityData(entityID);
			IdentityProviderRole idpRole = entityData.getRoleData(IdentityProviderRole.class);
			assertNotNull(idpRole);
			assertEquals(domain1EntityGenerator.generateEndpoint(i, ssoEndpointPath), idpRole.getSingleSignOnService(binding));
		}
		for (int i=3*scale; i<4*scale; ++i)
		{
			String entityID = domain1EntityGenerator.generateEntityID(i);
			int index1 = 1000 + i;
			int index2 = 2000 + i;
			boolean isSPEP = (i%2 == 0);
			EntityData entityData = metadataProcessor.getEntityData(entityID);
			ServiceProviderRole spRole = entityData.getRoleData(ServiceProviderRole.class);
			assertNotNull(spRole);
			assertEquals(domain1EntityGenerator.generateIndexedEndpoint(i, index1, ssoEndpointPath), spRole.getAssertionConsumerServiceEndpoint(binding, index1));
			assertEquals(domain1EntityGenerator.generateIndexedEndpoint(i, index2, ssoEndpointPath), spRole.getAssertionConsumerServiceEndpoint(binding, index2));
			assertEquals(domain1EntityGenerator.generateEndpoint(i, logoutEndpointPath), spRole.getSingleLogoutServiceEndpoint(binding));		
			if (isSPEP)
			{
				SPEPRole spepRole = entityData.getRoleData(SPEPRole.class);
				assertNotNull(spepRole);
				assertEquals(domain1EntityGenerator.generateIndexedEndpoint(i, index1, cacheClearEndpointPath), spepRole.getCacheClearServiceEndpoint(binding, index1));
				assertEquals(domain1EntityGenerator.generateIndexedEndpoint(i, index2, cacheClearEndpointPath), spepRole.getCacheClearServiceEndpoint(binding, index2));
			}
		}
	}
	
	@Test
	public void testDynamic() throws Exception
	{
		assertTrue(true);
		assertFalse(false);
		assertNull(null);
		
		String domain1 = "example.com";
		EntityGenerator domain1EntityGenerator = new EntityGenerator(domain1);
		
		EntitiesDescriptor entitiesDescriptor = new EntitiesDescriptor();
		
		entitiesDescriptor.setSignature(new Signature());
		entitiesDescriptor.setID("_" + domain1EntityGenerator.generateRandomID(20));
		
		int scale = 1;
		
		for (int i=0; i<1*scale; ++i)
		{
			entitiesDescriptor.getEntitiesDescriptorsAndEntityDescriptors().add(
				domain1EntityGenerator.generateIdPEntityDescriptor(i, true, true, true)
			);
		}
		for (int i=1*scale; i<2*scale; ++i)
		{
			entitiesDescriptor.getEntitiesDescriptorsAndEntityDescriptors().add(
				domain1EntityGenerator.generateIdPEntityDescriptor(i, true, false, false)
			);
		}
		for (int i=2*scale; i<3*scale; ++i)
		{
			entitiesDescriptor.getEntitiesDescriptorsAndEntityDescriptors().add(
				domain1EntityGenerator.generateIdPEntityDescriptor(i, false, false, false)
			);
		}
		for (int i=3*scale; i<4*scale; ++i)
		{
			List<Integer> indices = new ArrayList<Integer>();
			indices.add(1000 + i);
			indices.add(2000 + i);
			entitiesDescriptor.getEntitiesDescriptorsAndEntityDescriptors().add(
				domain1EntityGenerator.generateSPEntityDescriptor(i, (i%2 == 0), indices)
			);
		}
		// Choosing number 0 as the trusted ESOE.
		String trustedEntityID = domain1EntityGenerator.generateEntityID(0);
		final String metadataLocation = "local://test-metadata.example.com";
		final int metadataPriority = 1;
		
		final byte[] metadataDocument = entitiesDescriptorMarshaller.marshallSigned(entitiesDescriptor);
		
		final DynamicMetadataSourceBase source = new DynamicMetadataSourceBase(){
			public String getFormat()
			{ return FormatConstants.SAML2; }
			@Override
			public boolean isTrusted(String entityID)
			{ return true; }
			public EntityData updateDynamicMetadata(MetadataProcessor processor, String entityID) throws MetadataSourceException
			{
				try
				{
					return this.readMetadata(new ByteArrayInputStream(metadataDocument), processor, entityID);
				}
				catch (IOException e)
				{
					fail(e.getMessage());
				}
				return null;
			}
		};
		
		DynamicMetadataUpdater dynamicMetadataUpdater = new DynamicMetadataUpdater(){
			public EntityData dynamicUpdate(MetadataProcessor processor, String entityID)
			{
				try
				{
					return source.updateDynamicMetadata(processor, entityID);
				}
				catch (MetadataSourceException e)
				{
					fail(e.getMessage());
				}
				return null;
			}
		};
		
		List<SAMLEntityDescriptorProcessor> samlProcessors = new ArrayList<SAMLEntityDescriptorProcessor>();
		samlProcessors.add(new SAMLIdentityProviderProcessor(trustedEntityID));
		samlProcessors.add(new SAMLServiceProviderProcessor());
		
		List<FormatHandler> formatHandlers = new ArrayList<FormatHandler>();
		formatHandlers.add(new SAMLMetadataFormatHandler(keystoreResolver, samlProcessors));
		
		List<MetadataSource> sources = new ArrayList<MetadataSource>();
		
		MetadataCache cache = new MetadataCacheImpl(dynamicMetadataUpdater);
		MetadataProcessor metadataProcessor = new MetadataProcessorImpl(cache, formatHandlers, sources);
		
		metadataProcessor.update();
		
		assertNotNull(metadataProcessor.getEntityData(trustedEntityID));
		for (int i=0; i<1*scale; ++i)
		{
			String entityID = domain1EntityGenerator.generateEntityID(i);
			EntityData entityData = metadataProcessor.getEntityData(entityID);
			ESOERole esoeRole = entityData.getRoleData(ESOERole.class);
			assertNotNull(esoeRole);
			assertEquals(domain1EntityGenerator.generateEndpoint(i, ssoEndpointPath), esoeRole.getSingleSignOnService(binding));
			assertEquals(domain1EntityGenerator.generateEndpoint(i, aaEndpointPath), esoeRole.getAttributeServiceEndpoint(binding));
			if (entityData.getEntityID().compareTo(trustedEntityID) == 0)
			{
				TrustedESOERole trustedRole = entityData.getRoleData(TrustedESOERole.class);
				assertNotNull(trustedRole);
				
				assertEquals(domain1EntityGenerator.generateEndpoint(i, spepStartupEndpointPath), trustedRole.getSPEPStartupServiceEndpoint(binding));
				assertEquals(domain1EntityGenerator.generateEndpoint(i, pdpEndpointPath), trustedRole.getLXACMLAuthzServiceEndpoint(binding));
			}
		}
		for (int i=1*scale; i<2*scale; ++i)
		{
			String entityID = domain1EntityGenerator.generateEntityID(i);
			EntityData entityData = metadataProcessor.getEntityData(entityID);
			IdentityProviderRole idpRole = entityData.getRoleData(IdentityProviderRole.class);
			AttributeAuthorityRole aaRole = entityData.getRoleData(AttributeAuthorityRole.class);
			assertNotNull(idpRole);
			assertNotNull(aaRole);
			assertEquals(domain1EntityGenerator.generateEndpoint(i, ssoEndpointPath), idpRole.getSingleSignOnService(binding));
			assertEquals(domain1EntityGenerator.generateEndpoint(i, aaEndpointPath), aaRole.getAttributeServiceEndpoint(binding));
		}
		for (int i=2*scale; i<3*scale; ++i)
		{
			String entityID = domain1EntityGenerator.generateEntityID(i);
			EntityData entityData = metadataProcessor.getEntityData(entityID);
			IdentityProviderRole idpRole = entityData.getRoleData(IdentityProviderRole.class);
			assertNotNull(idpRole);
			assertEquals(domain1EntityGenerator.generateEndpoint(i, ssoEndpointPath), idpRole.getSingleSignOnService(binding));
		}
		for (int i=3*scale; i<4*scale; ++i)
		{
			String entityID = domain1EntityGenerator.generateEntityID(i);
			int index1 = 1000 + i;
			int index2 = 2000 + i;
			boolean isSPEP = (i%2 == 0);
			EntityData entityData = metadataProcessor.getEntityData(entityID);
			ServiceProviderRole spRole = entityData.getRoleData(ServiceProviderRole.class);
			assertNotNull(spRole);
			assertEquals(domain1EntityGenerator.generateIndexedEndpoint(i, index1, ssoEndpointPath), spRole.getAssertionConsumerServiceEndpoint(binding, index1));
			assertEquals(domain1EntityGenerator.generateIndexedEndpoint(i, index2, ssoEndpointPath), spRole.getAssertionConsumerServiceEndpoint(binding, index2));
			assertEquals(domain1EntityGenerator.generateEndpoint(i, logoutEndpointPath), spRole.getSingleLogoutServiceEndpoint(binding));		
			if (isSPEP)
			{
				SPEPRole spepRole = entityData.getRoleData(SPEPRole.class);
				assertNotNull(spepRole);
				assertEquals(domain1EntityGenerator.generateIndexedEndpoint(i, index1, cacheClearEndpointPath), spepRole.getCacheClearServiceEndpoint(binding, index1));
				assertEquals(domain1EntityGenerator.generateIndexedEndpoint(i, index2, cacheClearEndpointPath), spepRole.getCacheClearServiceEndpoint(binding, index2));
			}
		}
	}
	
	public class NameGenerator
	{
		private String domain;
		private Random random;
		public NameGenerator(String domain) { this.domain = domain; this.random = new Random(); }
		public String generateEntityID(int sequence)
		{ return "http://entity-n" + sequence + "." + this.domain; }
		public String generateEndpoint(int sequence, String path)
		{ return this.generateEntityID(sequence) + path; }
		public String generateIndexedEndpoint(int sequence, int index, String path)
		{ return "http://entity-n" + sequence + "-i" + index + "." + this.domain + path; }
		public String generateRandomID(int length)
		{ byte[] bytes = new byte[length]; this.random.nextBytes(bytes); return new String(Hex.encodeHex(bytes)); }
	}
	public class EntityGenerator
	{
		private NameGenerator nameGenerator;
		public EntityGenerator(String domain)
		{ this.nameGenerator = new NameGenerator(domain); }
		public String generateEntityID(int sequence)
		{ return this.nameGenerator.generateEntityID(sequence); }
		public String generateEndpoint(int sequence, String path)
		{ return this.nameGenerator.generateEndpoint(sequence, path); }
		public String generateIndexedEndpoint(int sequence, int index, String path)
		{ return this.nameGenerator.generateIndexedEndpoint(sequence, index, path); }
		public String generateRandomID(int length)
		{ return this.nameGenerator.generateRandomID(length); }
		public EntityDescriptor generateIdPEntityDescriptor(
				int sequence,
				boolean aa,
				boolean pdp,
				boolean esoe
		) throws Exception
		{
			EntityDescriptor entityDescriptor = new EntityDescriptor();
			entityDescriptor.setEntityID(this.nameGenerator.generateEntityID(sequence));
			entityDescriptor.setID("_" + this.nameGenerator.generateRandomID(20));
			
			IDPSSODescriptor idpSSODescriptor = new IDPSSODescriptor();
			idpSSODescriptor.setID("_" + this.nameGenerator.generateRandomID(20));
			idpSSODescriptor.getProtocolSupportEnumerations().add(ProtocolConstants.protocol);
			
			EndpointType ssoEndpoint = new EndpointType();
			ssoEndpoint.setLocation(this.nameGenerator.generateEndpoint(sequence, ssoEndpointPath));
			ssoEndpoint.setBinding(binding);
			idpSSODescriptor.getSingleSignOnServices().add(ssoEndpoint);
			
			EndpointType logoutEndpoint = new EndpointType();
			logoutEndpoint.setLocation(this.nameGenerator.generateEndpoint(sequence, logoutEndpointPath));
			logoutEndpoint.setBinding(binding);
			idpSSODescriptor.getSingleLogoutServices().add(logoutEndpoint);
			
			if (esoe)
			{
				SPEPStartupService spepStartupEndpoint = new SPEPStartupService();
				spepStartupEndpoint.setLocation(this.nameGenerator.generateEndpoint(sequence, spepStartupEndpointPath));
				spepStartupEndpoint.setBinding(binding);
				
				Extensions extensions = new Extensions();
				extensions.getImplementedExtensions().add(startupServiceMarshaller.marshallUnSignedElement(spepStartupEndpoint));
				
				idpSSODescriptor.setExtensions(extensions);
			}
			
			entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(idpSSODescriptor);
			
			if (aa)
			{
				AttributeAuthorityDescriptor attributeAuthorityDescriptor = new AttributeAuthorityDescriptor();
				attributeAuthorityDescriptor.setID("_" + this.nameGenerator.generateRandomID(20));
				attributeAuthorityDescriptor.getProtocolSupportEnumerations().add(ProtocolConstants.protocol);
				
				EndpointType aaEndpoint = new EndpointType();
				aaEndpoint.setLocation(this.nameGenerator.generateEndpoint(sequence, aaEndpointPath));
				aaEndpoint.setBinding(binding);
				attributeAuthorityDescriptor.getAttributeServices().add(aaEndpoint);
				
				entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(attributeAuthorityDescriptor);
			}
			
			if (pdp)
			{
				LXACMLPDPDescriptor lxacmlPDPDescriptor = new LXACMLPDPDescriptor();
				lxacmlPDPDescriptor.setID("_" + this.nameGenerator.generateRandomID(20));
				lxacmlPDPDescriptor.getProtocolSupportEnumerations().add(ProtocolConstants.protocol);
				
				EndpointType authzEndpoint = new EndpointType();
				authzEndpoint.setBinding(binding);
				authzEndpoint.setLocation(this.nameGenerator.generateEndpoint(sequence, pdpEndpointPath));
				lxacmlPDPDescriptor.getAuthzServices().add(authzEndpoint);
				
				Extensions extensions = new Extensions();
				extensions.getImplementedExtensions().add(lxacmlPDPDescriptorMarshaller.marshallUnSignedElement(lxacmlPDPDescriptor));
				
				entityDescriptor.setExtensions(extensions);
			}
			
			return entityDescriptor;
		}
		public EntityDescriptor generateSPEntityDescriptor(
				int sequence,
				boolean spep,
				List<Integer> indices
		) throws Exception
		{
			EntityDescriptor entityDescriptor = new EntityDescriptor();
			entityDescriptor.setEntityID(this.nameGenerator.generateEntityID(sequence));
			entityDescriptor.setID("_" + this.nameGenerator.generateRandomID(20));
			
			SPSSODescriptor spSSODescriptor = new SPSSODescriptor();
			spSSODescriptor.setID("_" + this.nameGenerator.generateRandomID(20));
			spSSODescriptor.getProtocolSupportEnumerations().add(ProtocolConstants.protocol);
			
			EndpointType logoutEndpoint = new EndpointType();
			logoutEndpoint.setLocation(this.nameGenerator.generateEndpoint(sequence, logoutEndpointPath));
			logoutEndpoint.setBinding(binding);
			spSSODescriptor.getSingleLogoutServices().add(logoutEndpoint);
			
			Extensions extensions = new Extensions();
			for (Integer i : indices)
			{
				IndexedEndpointType ssoEndpoint = new IndexedEndpointType();
				ssoEndpoint.setLocation(this.nameGenerator.generateIndexedEndpoint(sequence, i, ssoEndpointPath));
				ssoEndpoint.setBinding(binding);
				ssoEndpoint.setIndex(i);
				spSSODescriptor.getAssertionConsumerServices().add(ssoEndpoint);
				
				if (spep)
				{
					CacheClearService cacheClearEndpoint = new CacheClearService();
					cacheClearEndpoint.setLocation(this.nameGenerator.generateIndexedEndpoint(sequence, i, cacheClearEndpointPath));
					cacheClearEndpoint.setBinding(binding);
					cacheClearEndpoint.setIndex(i);
					
					extensions.getImplementedExtensions().add(cacheClearServiceMarshaller.marshallUnSignedElement(cacheClearEndpoint));
					
					spSSODescriptor.setExtensions(extensions);
				}
			}
			
			entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(spSSODescriptor);
			
			return entityDescriptor;
		}
	}
}
