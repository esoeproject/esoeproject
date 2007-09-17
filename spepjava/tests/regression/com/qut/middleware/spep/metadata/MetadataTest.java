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
 * Creation Date: 22/11/2006
 * 
 * Purpose: Tests the metadata processor.
 */
package com.qut.middleware.spep.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.junit.Before;
import org.junit.Test;
import org.w3._2000._09.xmldsig_.KeyInfo;
import org.w3._2000._09.xmldsig_.KeyValue;
import org.w3._2000._09.xmldsig_.RSAKeyValue;
import org.w3._2000._09.xmldsig_.Signature;
import org.w3c.dom.Element;

import com.qut.middleware.saml2.BindingConstants;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.identifier.IdentifierCache;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.identifier.impl.IdentifierCacheImpl;
import com.qut.middleware.saml2.identifier.impl.IdentifierGeneratorImpl;
import com.qut.middleware.saml2.schemas.metadata.AttributeAuthorityDescriptor;
import com.qut.middleware.saml2.schemas.metadata.AuthnAuthorityDescriptor;
import com.qut.middleware.saml2.schemas.metadata.EndpointType;
import com.qut.middleware.saml2.schemas.metadata.EntitiesDescriptor;
import com.qut.middleware.saml2.schemas.metadata.EntityDescriptor;
import com.qut.middleware.saml2.schemas.metadata.Extensions;
import com.qut.middleware.saml2.schemas.metadata.IDPSSODescriptor;
import com.qut.middleware.saml2.schemas.metadata.IndexedEndpointType;
import com.qut.middleware.saml2.schemas.metadata.KeyDescriptor;
import com.qut.middleware.saml2.schemas.metadata.KeyTypes;
import com.qut.middleware.saml2.schemas.metadata.SPSSODescriptor;
import com.qut.middleware.saml2.schemas.metadata.extensions.SPEPStartupService;
import com.qut.middleware.saml2.schemas.metadata.lxacml.LXACMLPDPDescriptor;
import com.qut.middleware.spep.ConfigurationConstants;
import com.qut.middleware.spep.metadata.impl.KeyStoreResolverImpl;
import com.qut.middleware.spep.metadata.impl.MetadataImpl;

/** */
@SuppressWarnings("nls")
public class MetadataTest
{

	private String keyName;
	private PrivateKey key;
	private PublicKey publicKey;
	private Metadata metadata;
	private String metadataUrl;
	private String esoeIdentifier;
	private String spepIdentifier;
	private String esoeIDPSingleLogoutServiceLocation;
	private String esoeIDPSingleSignOnServiceLocation;
	private String esoeAttributeServiceLocation;
	private String esoeAuthzServiceLocation1;
	private String esoeAuthzServiceLocation2;
	private String[] schemas;
	private String esoeAuthnQueryServiceLocation;
	private IdentifierCache identifierCache;
	private String spepStartupServiceLocation;
	private Marshaller<SPEPStartupService> spepStartupServiceMarshaller;
	private int nodeID = 0;
	private SPSSODescriptor spepSPDescriptor;
	private String spepSPAssertionConsumerLocation = "the.correct.endpoint";
	private 
	int interval = 10;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{		
		InputStream inputStream = new FileInputStream( "tests" + File.separator + "testdata" + File.separator + "testkeystore.ks");
		KeyStoreResolver keyStoreResolver = new KeyStoreResolverImpl(inputStream, "Es0EKs54P4SSPK", "esoeprimary", "Es0EKs54P4SSPK");
		this.key = keyStoreResolver.getPrivateKey();
		this.publicKey = keyStoreResolver.getPublicKey();
		this.keyName = keyStoreResolver.getKeyAlias();
			
		File tempFile = File.createTempFile("metadata", ".xml");
		tempFile.deleteOnExit();
		
		this.esoeIdentifier = "esoe.url";
		this.spepIdentifier = "spep.url";
		this.identifierCache = new IdentifierCacheImpl();
		
		createMetadata(tempFile);
		
		this.metadataUrl = tempFile.toURL().toExternalForm();
		
		this.metadata = new MetadataImpl(this.spepIdentifier, this.esoeIdentifier, this.metadataUrl, this.publicKey, this.nodeID, interval);
	}
	
	private void createMetadata(File tempFile) throws MarshallerException, IOException
	{
		this.spepStartupServiceMarshaller = new MarshallerImpl<SPEPStartupService>(SPEPStartupService.class.getPackage().getName(), new String[]{ConfigurationConstants.spepStartupService});
		
		IdentifierGenerator identifierGenerator = new IdentifierGeneratorImpl(this.identifierCache);
		
		this.esoeIDPSingleLogoutServiceLocation = "http://idp.esoe.url/singleLogout";
		EndpointType esoeIDPSingleLogoutService = new EndpointType();
		esoeIDPSingleLogoutService.setBinding("binding");
		esoeIDPSingleLogoutService.setLocation(this.esoeIDPSingleLogoutServiceLocation);
		esoeIDPSingleLogoutService.setResponseLocation("http://idp.esoe.url/singleLogoutResponse");
		
		this.esoeIDPSingleSignOnServiceLocation = "http://idp.esoe.url/singleSignOn";
		EndpointType esoeIDPSingleSignOnService = new EndpointType();
		esoeIDPSingleSignOnService.setBinding("binding");
		esoeIDPSingleSignOnService.setLocation(this.esoeIDPSingleSignOnServiceLocation);
		esoeIDPSingleSignOnService.setResponseLocation("http://idp.esoe.url/singleSignOnResponse");
		
		this.esoeAttributeServiceLocation = "http://aa.esoe.url/attributeAuthority";
		EndpointType esoeAttributeService = new EndpointType();
		esoeAttributeService.setBinding("binding");
		esoeAttributeService.setLocation(this.esoeAttributeServiceLocation);
		esoeAttributeService.setResponseLocation("http://aa.esoe.url/attributeAuthorityResponse");
		
		this.esoeAuthzServiceLocation1 = "http://pdp.esoe.url/pdp1";
		EndpointType esoeAuthzService = new EndpointType();
		esoeAuthzService.setBinding("binding");
		esoeAuthzService.setLocation(this.esoeAuthzServiceLocation1);
		esoeAuthzService.setResponseLocation("http://pdp.esoe.url/pdpResponse");
		
		// ADD another authz service to test mutiple service locations for a single IDP
		this.esoeAuthzServiceLocation2 = "http://pdp.esoe.url/pdp2";
		EndpointType esoeAuthzService2 = new EndpointType();
		esoeAuthzService2.setBinding("binding");
		esoeAuthzService2.setLocation(this.esoeAuthzServiceLocation2);
		esoeAuthzService2.setResponseLocation("http://pdp.esoe.url/pdpResponse");
				
		this.esoeAuthnQueryServiceLocation = "http://authn.esoe.url/authnQuery";
		EndpointType esoeAuthnQueryService = new EndpointType();
		esoeAuthnQueryService.setBinding("binding");
		esoeAuthnQueryService.setLocation(this.esoeAuthnQueryServiceLocation);
		esoeAuthnQueryService.setResponseLocation("http://authn.esoe.url/authnQueryResponse");
		
		IDPSSODescriptor esoeIDPDescriptor = new IDPSSODescriptor();
		esoeIDPDescriptor.setID("idp.esoe.url");
		esoeIDPDescriptor.setWantAuthnRequestsSigned(Boolean.TRUE);
		esoeIDPDescriptor.getSingleLogoutServices().add(esoeIDPSingleLogoutService);
		esoeIDPDescriptor.getSingleSignOnServices().add(esoeIDPSingleSignOnService);
		esoeIDPDescriptor.getProtocolSupportEnumerations().add("http");
		
		this.spepStartupServiceLocation = "http://spep.esoe.url/startup";
		SPEPStartupService spepStartupService = new SPEPStartupService();
		spepStartupService.setBinding("http");
		spepStartupService.setLocation(this.spepStartupServiceLocation);
		spepStartupService.setResponseLocation("http://spep.esoe.url/startupResponse");
		
		Element spepStartupServiceElement = this.spepStartupServiceMarshaller.marshallUnSignedElement(spepStartupService);
		
		Extensions idpExtensions = new Extensions();
		idpExtensions.getImplementedExtensions().add(spepStartupServiceElement);
		esoeIDPDescriptor.setExtensions(idpExtensions);
		
		AttributeAuthorityDescriptor esoeAttributeAuthorityDescriptor = new AttributeAuthorityDescriptor();
		esoeAttributeAuthorityDescriptor.setID("aa.esoe.url");
		esoeAttributeAuthorityDescriptor.getAttributeServices().add(esoeAttributeService);
		esoeAttributeAuthorityDescriptor.getProtocolSupportEnumerations().add("http");
		
		LXACMLPDPDescriptor esoeLXACMLPDPDescriptor = new LXACMLPDPDescriptor();
		esoeLXACMLPDPDescriptor.setID("pdp.esoe.url");
		esoeLXACMLPDPDescriptor.getAuthzServices().add(esoeAuthzService);
		esoeLXACMLPDPDescriptor.getAuthzServices().add(esoeAuthzService2);		
		esoeLXACMLPDPDescriptor.getProtocolSupportEnumerations().add("http");
		
		AuthnAuthorityDescriptor esoeAuthnAuthorityDescriptor = new AuthnAuthorityDescriptor();
		esoeAuthnAuthorityDescriptor.setID("authn.esoe.url");
		esoeAuthnAuthorityDescriptor.getProtocolSupportEnumerations().add("http");
		esoeAuthnAuthorityDescriptor.getAuthnQueryServices().add(esoeAuthnQueryService);
		
		KeyDescriptor keyDescriptor = new KeyDescriptor();
		KeyInfo keyInfo = new KeyInfo();
		KeyTypes keyType = KeyTypes.SIGNING;
		
		keyInfo.setId("esoeprimary");
		
		RSAKeyValue rsaKeyValue = new RSAKeyValue();
		
		rsaKeyValue.setExponent(((RSAPublicKey)this.publicKey).getPublicExponent().toByteArray());
		rsaKeyValue.setModulus(((RSAPublicKey)this.publicKey).getModulus().toByteArray());

		KeyValue keyValue = new KeyValue();
		keyValue.getContent().add(rsaKeyValue);
		
		JAXBElement<String> keyNameElement = new JAXBElement<String>(new QName("http://www.w3.org/2000/09/xmldsig#", "KeyName"), String.class, "esoeprimary");
		
		keyInfo.getContent().add(keyValue);
		keyInfo.getContent().add(keyNameElement);
		
		keyDescriptor.setKeyInfo(keyInfo);
		keyDescriptor.setUse(keyType);
		
		esoeIDPDescriptor.getKeyDescriptors().add(keyDescriptor);
		
		EntityDescriptor esoeEntityDescriptor = new EntityDescriptor();
		esoeEntityDescriptor.setEntityID(this.esoeIdentifier);
		esoeEntityDescriptor.setID(this.esoeIdentifier);
		esoeEntityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(esoeIDPDescriptor);
		esoeEntityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(esoeAttributeAuthorityDescriptor);
		esoeEntityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(esoeAuthnAuthorityDescriptor);

		String[] extensionSchema = new String[]{ConfigurationConstants.samlMetadata, ConfigurationConstants.lxacmlMetadata};
		Marshaller<LXACMLPDPDescriptor> extensionMarshaller = new MarshallerImpl<LXACMLPDPDescriptor>(LXACMLPDPDescriptor.class.getPackage().getName(), extensionSchema);
		Element extensionNode = extensionMarshaller.marshallUnSignedElement(esoeLXACMLPDPDescriptor);
		Extensions extensions = new Extensions();
		esoeEntityDescriptor.setExtensions(extensions);
		extensions.getImplementedExtensions().add(extensionNode);
	
		
		 // added an SPSSO Descriptor to fake metadata. This will be used by the SPEP
		// to extract the location of it's OWN assertionConsumerLocation
		this.spepSPDescriptor = new SPSSODescriptor();
		this.spepSPDescriptor.setID(this.spepIdentifier);
		this.spepSPDescriptor.getSingleLogoutServices().add(esoeIDPSingleLogoutService);
		this.spepSPDescriptor.getProtocolSupportEnumerations().add("http");
		
		// create 2 assertion consumer endpoints, one will match the index of this nodeID, one will not
		IndexedEndpointType assConService = new IndexedEndpointType();
		assConService.setIndex(this.nodeID);
		assConService.setLocation(this.spepSPAssertionConsumerLocation);
		assConService.setBinding(BindingConstants.httpPost);
		
		IndexedEndpointType assConService2 = new IndexedEndpointType();
		assConService2.setIndex(187);
		assConService2.setLocation("incorrect.endpoint");
		assConService2.setBinding(BindingConstants.httpPost);
		
		spepSPDescriptor.getAssertionConsumerServices().add(assConService2);
		spepSPDescriptor.getAssertionConsumerServices().add(assConService);
		
		// the entity descriptor that holds the spep info
		EntityDescriptor spepEntityDescriptor = new EntityDescriptor();
		spepEntityDescriptor.setEntityID("uhfew8f9eyfwefyw");
		spepEntityDescriptor.setID("f879d6sf87safp87we6f");
		
		spepEntityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(spepSPDescriptor);
		
		EntitiesDescriptor entitiesDescriptor = new EntitiesDescriptor();
		entitiesDescriptor.setID(identifierGenerator.generateSAMLID());
		entitiesDescriptor.setSignature(new Signature());
		
		// add esoe and spep descriptors
		entitiesDescriptor.getEntitiesDescriptorsAndEntityDescriptors().add(esoeEntityDescriptor);
		entitiesDescriptor.getEntitiesDescriptorsAndEntityDescriptors().add(spepEntityDescriptor);
			
		this.schemas = new String[]{ConfigurationConstants.samlMetadata, ConfigurationConstants.lxacmlMetadata};
		Marshaller<EntitiesDescriptor> entitiesDescriptorMarshaller = new MarshallerImpl<EntitiesDescriptor>(EntitiesDescriptor.class.getPackage().getName(), this.schemas, this.keyName, this.key);
		
		byte[] metadataDocument = entitiesDescriptorMarshaller.marshallSigned(entitiesDescriptor);
	}

	/**
	 * Test for {@link com.qut.middleware.spep.metadata.Metadata#getESOEIdentifier}
	 * @throws Exception 
	 */
	@Test
	public void testMetadata() throws Exception
	{
		assertEquals(this.esoeAttributeServiceLocation,this.metadata.getAttributeServiceEndpoint());
		assertTrue(this.esoeAuthzServiceLocation1.equals(this.metadata.getAuthzServiceEndpoint()) ||
				this.esoeAuthzServiceLocation2.equals(this.metadata.getAuthzServiceEndpoint()) );
		assertEquals(this.esoeIDPSingleSignOnServiceLocation,this.metadata.getSingleSignOnEndpoint());
		assertEquals(this.spepStartupServiceLocation,this.metadata.getSPEPStartupServiceEndpoint());
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testResolveKey1() throws Exception
	{
		PublicKey publicKey = this.metadata.resolveKey(this.keyName);
		assertTrue(publicKey instanceof RSAPublicKey);
		
		RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
		assertEquals(rsaPublicKey.getModulus(), ((RSAPublicKey)this.publicKey).getModulus());
		assertEquals(rsaPublicKey.getPublicExponent(), ((RSAPublicKey)this.publicKey).getPublicExponent());
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testResolveKey2() throws Exception
	{
		PublicKey publicKey = this.metadata.resolveKey("some non existant key");
		assertNull(publicKey);
	}
	
	
	/** Test various getter to ensure validity.
	 * 
	 *
	 */
	@Test
	public void testGetters()
	{
		assertEquals(this.esoeIdentifier, this.metadata.getESOEIdentifier());
		
		assertEquals(this.spepIdentifier, this.metadata.getSPEPIdentifier());
		
		// see setup method for where this has been set in added SPSSODescriptor
		assertEquals(this.spepSPAssertionConsumerLocation, this.metadata.getSPEPAssertionConsumerLocation());
			
		try
		{
			this.metadata.getSingleLogoutEndpoint();
		}
		catch(UnsupportedOperationException e)
		{
			// do nothing .. purely for code coverage, as this method is unsupported at this time
		}
	}
	
	
		
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction1() throws Exception
	{
		this.metadata = new MetadataImpl(null, this.esoeIdentifier, this.metadataUrl, this.publicKey, this.nodeID, interval);
		
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction2() throws Exception
	{
		this.metadata = new MetadataImpl(this.spepIdentifier, null, this.metadataUrl, this.publicKey, this.nodeID, interval);
		
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction3() throws Exception
	{
		this.metadata = new MetadataImpl(this.spepIdentifier, this.esoeIdentifier, null, this.publicKey, this.nodeID, interval);
		
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction4() throws Exception
	{
		this.metadata = new MetadataImpl(this.spepIdentifier, this.esoeIdentifier, this.metadataUrl, null, this.nodeID, interval);
		
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction5() throws Exception
	{
		this.metadata = new MetadataImpl(this.spepIdentifier, this.esoeIdentifier, this.metadataUrl, this.publicKey, -2, interval);
		
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testConstruction6() throws Exception
	{
		this.metadata = new MetadataImpl(this.spepIdentifier, this.esoeIdentifier, this.metadataUrl, this.publicKey, this.nodeID, -1);
		
	}
}
