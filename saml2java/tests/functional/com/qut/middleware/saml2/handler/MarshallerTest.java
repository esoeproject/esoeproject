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
 * Author: Bradley Beddoes
 * Creation Date:  23/10/2006
 * 
 * Purpose: Test out all functionality of saml2lib-j for marshalling purposes
 */

package com.qut.middleware.saml2.handler;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.SimpleTimeZone;

import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3._2000._09.xmldsig_.KeyInfo;
import org.w3._2000._09.xmldsig_.KeyValue;
import org.w3._2000._09.xmldsig_.RSAKeyValue;
import org.w3._2000._09.xmldsig_.Signature;
import org.w3c.dom.Element;

import sun.misc.BASE64Encoder;

import com.qut.middleware.saml2.AttributeFormatConstants;
import com.qut.middleware.saml2.BindingConstants;
import com.qut.middleware.saml2.LocalKeyResolver;
import com.qut.middleware.saml2.NameIDFormatConstants;
import com.qut.middleware.saml2.ProtocolConstants;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.handler.impl.UnmarshallerImpl;
import com.qut.middleware.saml2.schemas.assertion.AudienceRestriction;
import com.qut.middleware.saml2.schemas.assertion.Conditions;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.assertion.Subject;
import com.qut.middleware.saml2.schemas.metadata.AttributeConsumingService;
import com.qut.middleware.saml2.schemas.metadata.EndpointType;
import com.qut.middleware.saml2.schemas.metadata.EntityDescriptor;
import com.qut.middleware.saml2.schemas.metadata.Extensions;
import com.qut.middleware.saml2.schemas.metadata.IndexedEndpointType;
import com.qut.middleware.saml2.schemas.metadata.KeyDescriptor;
import com.qut.middleware.saml2.schemas.metadata.KeyTypes;
import com.qut.middleware.saml2.schemas.metadata.LocalizedNameType;
import com.qut.middleware.saml2.schemas.metadata.Organization;
import com.qut.middleware.saml2.schemas.metadata.OrganizationURL;
import com.qut.middleware.saml2.schemas.metadata.RequestedAttribute;
import com.qut.middleware.saml2.schemas.metadata.SPSSODescriptor;
import com.qut.middleware.saml2.schemas.metadata.extensions.CacheClearService;
import com.qut.middleware.saml2.schemas.protocol.AuthnRequest;
import com.qut.middleware.saml2.sec.KeyData;
import com.qut.middleware.saml2.sec.KeyName;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

@SuppressWarnings(value = { "unqualified-field-access", "nls", "boxing" })
public class MarshallerTest
{
	private String keyAlias;
	private String path;
	private String[] schemas;
	private PrivateKey privKey;
	private PublicKey pk;
	private LocalKeyResolver localKeyResolver;

	public MarshallerTest() throws Exception
	{
		this.path = System.getProperty("user.dir") + File.separator + "tests" + File.separator + "testdata"
				+ File.separator;
		schemas = new String[] { "saml-schema-protocol-2.0.xsd", "saml-schema-assertion-2.0.xsd",
				"saml-schema-metadata-2.0.xsd" };

		KeyStore ks = KeyStore.getInstance("PKCS12");
		FileInputStream fis = new FileInputStream(this.path + "tests.ks");
		char[] passwd = { 't', 'e', 's', 't', 'p', 'a', 's', 's' };
		ks.load(fis, passwd);
		
		keyAlias = "myrsakey";

		privKey = (PrivateKey) ks.getKey(keyAlias, passwd);
		Certificate cert = ks.getCertificate(keyAlias);
		pk = cert.getPublicKey();

		BASE64Encoder myB64 = new BASE64Encoder();
		String b64 = myB64.encode(privKey.getEncoded());
		
		localKeyResolver = createMock(LocalKeyResolver.class);
		expect(localKeyResolver.getLocalCertificate()).andReturn(cert).anyTimes();
		expect(localKeyResolver.getLocalKeyAlias()).andReturn(keyAlias).anyTimes();
		expect(localKeyResolver.getLocalPrivateKey()).andReturn(privKey).anyTimes();
		expect(localKeyResolver.getLocalPublicKey()).andReturn(pk).anyTimes();
		
		replay(localKeyResolver);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		// re-enable default value to ensure tests which change system prop dont impact others
		System.setProperty("jsr105Provider", "org.jcp.xml.dsig.internal.dom.XMLDSigRI");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception
	{
		// Not Implemented
	}

	/* Tests to ensure exception state when passing null to constructor for package */
	@Test(expected = IllegalArgumentException.class)
	public void testMarshaller1()
	{
		try
		{
			Marshaller<AuthnRequest> marshaller = new MarshallerImpl<AuthnRequest>(null, schemas, localKeyResolver);
		}
		catch (MarshallerException e)
		{
			e.printStackTrace();
			fail("Unexcpected Marshaller exception");
		}
	}

	/* Tests to ensure exception state when passing null to constructor for schemas */
	@Test(expected = IllegalArgumentException.class)
	public void testMarshaller1a()
	{
		try
		{
			Marshaller<AuthnRequest> marshaller = new MarshallerImpl<AuthnRequest>(
					"com.qut.middleware.saml2.schemas.protocol", null, localKeyResolver);
		}
		catch (MarshallerException e)
		{
			e.printStackTrace();
			fail("Unexcpected Marshaller exception");
		}
	}

	/* Tests to ensure exception state when passing null to constructor for keyname */
	@Test(expected = IllegalArgumentException.class)
	public void testMarshaller1b()
	{
		try
		{
			Marshaller<AuthnRequest> marshaller = new MarshallerImpl<AuthnRequest>(
					"com.qut.middleware.saml2.schemas.protocol", schemas, null);
		}
		catch (MarshallerException e)
		{
			e.printStackTrace();
			fail("Unexcpected Marshaller exception");
		}
	}

	/* Tests to ensure exception state when passing null to constructor for package */
	@Test(expected = IllegalArgumentException.class)
	public void testMarshaller1c()
	{
		try
		{
			Marshaller<AuthnRequest> marshaller = new MarshallerImpl<AuthnRequest>(null, schemas);
		}
		catch (MarshallerException e)
		{
			e.printStackTrace();
			fail("Unexcpected Marshaller exception");
		}
	}

	/* Tests to ensure exception state when passing null to constructor for schemas */
	@Test(expected = IllegalArgumentException.class)
	public void testMarshaller1d()
	{
		try
		{
			Marshaller<AuthnRequest> marshaller = new MarshallerImpl<AuthnRequest>(
					"com.qut.middleware.saml2.schemas.protocol", null);
		}
		catch (MarshallerException e)
		{
			e.printStackTrace();
			fail("Unexcpected Marshaller exception");
		}
	}

	/* Tests to ensure exception state when passing null to constructor for schemas */
	@Test(expected = IllegalArgumentException.class)
	public void testMarshaller1e()
	{
		try
		{
			Marshaller<AuthnRequest> marshaller = new MarshallerImpl<AuthnRequest>(
					"com.qut.middleware.saml2.schemas.protocol", null);
		}
		catch (MarshallerException e)
		{
			e.printStackTrace();
			fail("Unexcpected Marshaller exception");
		}
	}

	/* Tests to ensure exception state when invalid package name is passed to constructor */
	@Test(expected = MarshallerException.class)
	public void testMarshaller1f() throws MarshallerException
	{
		Marshaller<AuthnRequest> marshaller = new MarshallerImpl<AuthnRequest>(
				"com.qut.middleware.saml2.schemas.protocol.INVALID", schemas);
	}

	/* Tests to ensure exception state when invalid package name is passed to constructor */
	@Test(expected = MarshallerException.class)
	public void testMarshaller1g() throws MarshallerException
	{
		Marshaller<AuthnRequest> marshaller = new MarshallerImpl<AuthnRequest>(
				"com.qut.middleware.saml2.schemas.protocol.INVALID", schemas, localKeyResolver);
	}

	/* Tests to ensure exception state when invalid package name is passed to constructor */
	@Test(expected = MarshallerException.class)
	public void testMarshaller1h() throws MarshallerException
	{
		// re-enable default value to ensure tests which change system prop dont impact others
		System.setProperty("jsr105Provider", "fake.path.to.Class");

		Marshaller<AuthnRequest> marshaller;

			/* Supplied private/public key will be in RSA format */
			marshaller = new MarshallerImpl<AuthnRequest>("com.qut.middleware.saml2.schemas.protocol", schemas,
					localKeyResolver);

			AudienceRestriction audienceRestriction = new AudienceRestriction();
			Conditions conditions = new Conditions();
			NameIDType nameID = new NameIDType();
			Subject subject = new Subject();
			Signature signature = new Signature();
			AuthnRequest authnRequest = new AuthnRequest();

			/* GMT timezone */
			SimpleTimeZone gmt = new SimpleTimeZone(0, "UTC");

			/* GregorianCalendar with the GMT time zone */
			GregorianCalendar calendar = new GregorianCalendar(gmt);
			XMLGregorianCalendar xmlCalendar = new XMLGregorianCalendarImpl(calendar);

			audienceRestriction.getAudiences().add("spep-n1.qut.edu.au");
			audienceRestriction.getAudiences().add("spep-n2.qut.edu.au");
			conditions.getConditionsAndOneTimeUsesAndAudienceRestrictions().add(audienceRestriction);

			nameID.setValue("beddoes@qut.com");
			nameID.setFormat("urn:oasis:names:tc:SAML:2.0:something");

			subject.setNameID(nameID);

			authnRequest.setSignature(signature);
			authnRequest.setSubject(subject);
			authnRequest.setConditions(conditions);

			authnRequest.setForceAuthn(false);
			authnRequest.setAssertionConsumerServiceURL("http://spep-n1.qut.edu.au/sso/aa");
			authnRequest.setAttributeConsumingServiceIndex(0);
			authnRequest.setProviderName("spep-n1");
			authnRequest.setID("abe567de6-122wert67");
			authnRequest.setVersion("2.0");
			authnRequest.setIssueInstant(xmlCalendar);

			byte[] doc = marshaller.marshallSigned(authnRequest);

			assertNotNull("Supplied XML document should not be null", doc);
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.saml2.handler.impl.MarshallerImpl#marshallSigned(java.lang.String, java.security.PrivateKey, java.lang.String)}.
	 * 
	 * Tests for correct signature generation from supplied JAXB populated object
	 */
	@Test
	public void testMarshallSigned1()
	{
		Marshaller<AuthnRequest> marshaller;

		try
		{
			/* Supplied private/public key will be in RSA format */
			marshaller = new MarshallerImpl<AuthnRequest>("com.qut.middleware.saml2.schemas.protocol", schemas,
					localKeyResolver);

			AudienceRestriction audienceRestriction = new AudienceRestriction();
			Conditions conditions = new Conditions();
			NameIDType nameID = new NameIDType();
			Subject subject = new Subject();
			Signature signature = new Signature();
			AuthnRequest authnRequest = new AuthnRequest();

			/* GMT timezone */
			SimpleTimeZone gmt = new SimpleTimeZone(0, "UTC");

			/* GregorianCalendar with the GMT time zone */
			GregorianCalendar calendar = new GregorianCalendar(gmt);
			XMLGregorianCalendar xmlCalendar = new XMLGregorianCalendarImpl(calendar);

			audienceRestriction.getAudiences().add("spep-n1.qut.edu.au");
			audienceRestriction.getAudiences().add("spep-n2.qut.edu.au");
			conditions.getConditionsAndOneTimeUsesAndAudienceRestrictions().add(audienceRestriction);

			nameID.setValue("beddoes@qut.com");
			nameID.setFormat("urn:oasis:names:tc:SAML:2.0:something");

			subject.setNameID(nameID);

			authnRequest.setSignature(signature);
			authnRequest.setSubject(subject);
			authnRequest.setConditions(conditions);

			authnRequest.setForceAuthn(false);
			authnRequest.setAssertionConsumerServiceURL("http://spep-n1.qut.edu.au/sso/aa");
			authnRequest.setAttributeConsumingServiceIndex(0);
			authnRequest.setProviderName("spep-n1");
			authnRequest.setID("abe567de6-122wert67");
			authnRequest.setVersion("2.0");
			authnRequest.setIssueInstant(xmlCalendar);

			byte[] doc = marshaller.marshallSigned(authnRequest);

			assertNotNull("Supplied XML document should not be null", doc);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Exception caught signing test failure");
		}
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.saml2.handler.impl.MarshallerImpl#marshallSigned(java.lang.String, java.security.PrivateKey, java.lang.String)}.
	 * 
	 * Tests for correct signature generation from supplied JAXB populated object
	 */
	@Test
	public void testMarshallSigned2()
	{
		Marshaller<EntityDescriptor> marshaller;
		Marshaller<CacheClearService> marshaller2;

		try
		{
			/* Supplied private/public key will be in RSA format */
			marshaller = new MarshallerImpl<EntityDescriptor>(EntityDescriptor.class.getPackage().getName(), schemas,
					localKeyResolver);
			marshaller2 = new MarshallerImpl<CacheClearService>(CacheClearService.class.getPackage().getName(), schemas,
					localKeyResolver);

			/*
			 * We will set a KeyInfo block to the public key of the private key the document is signed with Obviously
			 * this is for testing purposes only and MUST NEVER be done in anything even remotely close to non test code
			 */
			RSAPublicKey rsaPK = (RSAPublicKey) pk;

			EntityDescriptor entityDescriptor = new EntityDescriptor();
			SPSSODescriptor spSSODescriptor = new SPSSODescriptor();
			KeyDescriptor keyDescriptor = new KeyDescriptor();
			KeyInfo keyInfo = new KeyInfo();
			KeyValue keyValue = new KeyValue();
			RSAKeyValue rsaKeyValue = new RSAKeyValue();
			EndpointType singleLogoutService = new EndpointType();
			IndexedEndpointType assertionConsumerService = new IndexedEndpointType();
			AttributeConsumingService attributeConsumingService = new AttributeConsumingService();
			LocalizedNameType localizedName = new LocalizedNameType();
			RequestedAttribute requestedAttribute = new RequestedAttribute();
			CacheClearService cacheClearService = new CacheClearService();

			entityDescriptor.setID("_12345-67890");
			entityDescriptor.setEntityID("987562314");
			entityDescriptor.setSignature(new Signature());
			spSSODescriptor.setSignature(new Signature());
			spSSODescriptor.setID("_12345-678901");
			spSSODescriptor.setAuthnRequestsSigned(true);
			spSSODescriptor.getProtocolSupportEnumerations().add(ProtocolConstants.protocol);
			spSSODescriptor.getProtocolSupportEnumerations().add("a");
			keyDescriptor.setUse(KeyTypes.SIGNING);

			KeyName keyName = new KeyName(keyAlias);
			keyInfo.getContent().add(keyName);
			rsaKeyValue.setExponent(rsaPK.getPublicExponent().toByteArray());
			rsaKeyValue.setModulus(rsaPK.getModulus().toByteArray());
			keyValue.getContent().add(rsaKeyValue);
			keyInfo.getContent().add(keyValue);

			keyDescriptor.setKeyInfo(keyInfo);
			spSSODescriptor.getKeyDescriptors().add(keyDescriptor);

			singleLogoutService.setBinding(BindingConstants.soap);
			singleLogoutService.setLocation("https://spep1.qut.edu.au");
			spSSODescriptor.getSingleLogoutServices().add(singleLogoutService);
			spSSODescriptor.getNameIDFormats().add(NameIDFormatConstants.trans);
			spSSODescriptor.getNameIDFormats().add("a");
			assertionConsumerService.setIsDefault(true);
			assertionConsumerService.setIndex(0);
			assertionConsumerService.setBinding(BindingConstants.httpPost);
			assertionConsumerService.setBinding("a");
			assertionConsumerService.setLocation("https://spep1.qut.edu.au");
			assertionConsumerService.setLocation("a");
			spSSODescriptor.getAssertionConsumerServices().add(assertionConsumerService);
			attributeConsumingService.setIndex(0);
			localizedName.setLang(Locale.ENGLISH.getLanguage());
			localizedName.setValue("Crazy SPEP of candy death");
			attributeConsumingService.getServiceNames().add(localizedName);
			requestedAttribute.setNameFormat(AttributeFormatConstants.uri);
			requestedAttribute.setName("urn:oid:1.3.6.1.4.1.5923.1.1.1.7");
			requestedAttribute.setFriendlyName("eduPersonEntitlement");
			attributeConsumingService.getRequestedAttributes().add(requestedAttribute);
			spSSODescriptor.getAttributeConsumingServices().add(attributeConsumingService);

			/*
			 * To embed extenstions we need to do a double marshal to get an element, its slightly messy but not used
			 * often
			 */
			cacheClearService.setBinding(BindingConstants.soap);
			cacheClearService.setLocation("http://spep1.qut.edu.au/clear");
			cacheClearService.setResponseLocation("http://spep1.qut.edu.au/clear");

			Element element = marshaller2.marshallUnSignedElement(cacheClearService);
			spSSODescriptor.setExtensions(new Extensions());
			spSSODescriptor.getExtensions().getImplementedExtensions().add(element);

			entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(spSSODescriptor);

			byte[] doc = marshaller.marshallSigned(entityDescriptor);

			assertNotNull("Supplied XML document should not be null", doc);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Exception caught signing test failure");
		}
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.saml2.handler.impl.MarshallerImpl#marshallSigned(java.lang.String, java.security.PrivateKey, java.lang.String)}.
	 * 
	 * Tests for correct exception state when object requested to be signed has no empty signature element
	 */
	@Test(expected = MarshallerException.class)
	public void testMarshallSigned2a() throws MarshallerException
	{
		Marshaller<EntityDescriptor> marshaller;
		Marshaller<CacheClearService> marshaller2;

		/* Supplied private/public key will be in RSA format */
		marshaller = new MarshallerImpl<EntityDescriptor>(CacheClearService.class.getPackage().getName(), schemas,
				localKeyResolver);
		marshaller2 = new MarshallerImpl<CacheClearService>(EntityDescriptor.class.getPackage().getName(), schemas,
				localKeyResolver);

		/*
		 * We will set a KeyInfo block to the public key of the private key the document is signed with Obviously this
		 * is for testing purposes only and MUST NEVER be done in anything even remotely close to non test code
		 */
		RSAPublicKey rsaPK = (RSAPublicKey) pk;

		EntityDescriptor entityDescriptor = new EntityDescriptor();
		SPSSODescriptor spSSODescriptor = new SPSSODescriptor();
		KeyDescriptor keyDescriptor = new KeyDescriptor();
		KeyInfo keyInfo = new KeyInfo();
		KeyValue keyValue = new KeyValue();
		RSAKeyValue rsaKeyValue = new RSAKeyValue();
		EndpointType singleLogoutService = new EndpointType();
		IndexedEndpointType assertionConsumerService = new IndexedEndpointType();
		AttributeConsumingService attributeConsumingService = new AttributeConsumingService();
		LocalizedNameType localizedName = new LocalizedNameType();
		RequestedAttribute requestedAttribute = new RequestedAttribute();
		CacheClearService cacheClearService = new CacheClearService();

		entityDescriptor.setID("_12345-67890");
		entityDescriptor.setEntityID("987562314");
		spSSODescriptor.setID("_12345-678901");
		spSSODescriptor.setAuthnRequestsSigned(true);
		spSSODescriptor.getProtocolSupportEnumerations().add(ProtocolConstants.protocol);
		spSSODescriptor.getProtocolSupportEnumerations().add("a");
		keyDescriptor.setUse(KeyTypes.SIGNING);

		KeyName keyName = new KeyName(keyAlias);
		keyInfo.getContent().add(keyName);
		rsaKeyValue.setExponent(rsaPK.getPublicExponent().toByteArray());
		rsaKeyValue.setModulus(rsaPK.getModulus().toByteArray());
		keyValue.getContent().add(rsaKeyValue);
		keyInfo.getContent().add(keyValue);

		keyDescriptor.setKeyInfo(keyInfo);
		spSSODescriptor.getKeyDescriptors().add(keyDescriptor);

		singleLogoutService.setBinding(BindingConstants.soap);
		singleLogoutService.setLocation("https://spep1.qut.edu.au");
		spSSODescriptor.getSingleLogoutServices().add(singleLogoutService);
		spSSODescriptor.getNameIDFormats().add(NameIDFormatConstants.trans);
		spSSODescriptor.getNameIDFormats().add("a");
		assertionConsumerService.setIsDefault(true);
		assertionConsumerService.setIndex(0);
		assertionConsumerService.setBinding(BindingConstants.httpPost);
		assertionConsumerService.setBinding("a");
		assertionConsumerService.setLocation("https://spep1.qut.edu.au");
		assertionConsumerService.setLocation("a");
		spSSODescriptor.getAssertionConsumerServices().add(assertionConsumerService);
		attributeConsumingService.setIndex(0);
		localizedName.setLang(Locale.ENGLISH.getLanguage());
		localizedName.setValue("Crazy SPEP of candy death");
		attributeConsumingService.getServiceNames().add(localizedName);
		requestedAttribute.setNameFormat(AttributeFormatConstants.uri);
		requestedAttribute.setName("urn:oid:1.3.6.1.4.1.5923.1.1.1.7");
		requestedAttribute.setFriendlyName("eduPersonEntitlement");
		attributeConsumingService.getRequestedAttributes().add(requestedAttribute);
		spSSODescriptor.getAttributeConsumingServices().add(attributeConsumingService);

		/*
		 * To embed extenstions we need to do a double marshal to get an element, its slightly messy but not used often
		 */
		cacheClearService.setBinding(BindingConstants.soap);
		cacheClearService.setLocation("http://spep1.qut.edu.au/clear");
		cacheClearService.setResponseLocation("http://spep1.qut.edu.au/clear");

		Element element = marshaller2.marshallUnSignedElement(cacheClearService);
		spSSODescriptor.setExtensions(new Extensions());
		spSSODescriptor.getExtensions().getImplementedExtensions().add(element);

		entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(spSSODescriptor);

		byte[] doc = marshaller.marshallSigned(entityDescriptor);
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.saml2.handler.impl.MarshallerImpl#marshallSigned(java.lang.String, java.security.PrivateKey, java.lang.String)}.
	 * 
	 * Tests for correct signature/document generation from disk loaded XML file that has signature populated
	 */
	@Test
	public void testMarshallSigned3()
	{
		Marshaller<EntityDescriptor> marshaller;
		Unmarshaller<EntityDescriptor> unmarshaller;

		this.path = System.getProperty("user.dir") + File.separator + "tests" + File.separator + "testdata"
				+ File.separator;
		schemas = new String[] { "saml-schema-protocol-2.0.xsd", "saml-schema-assertion-2.0.xsd",
				"saml-schema-metadata-2.0.xsd" };

		String filename = this.path + "SAMLMetadataUnSigned.xml";

		try
		{
			/* Supplied private/public key will be in RSA format */
			marshaller = new MarshallerImpl<EntityDescriptor>(EntityDescriptor.class.getPackage().getName(), schemas,
					localKeyResolver);
			unmarshaller = new UnmarshallerImpl<EntityDescriptor>(EntityDescriptor.class.getPackage().getName(),
					schemas);

			File file = new File(filename);
			long length = file.length();
			byte[] byteArray = new byte[(int) length];

			InputStream fileStream = new FileInputStream(file);
			fileStream.read(byteArray);
			fileStream.close();

			Map<String, KeyData> keys = new HashMap<String, KeyData>();

			EntityDescriptor entity = unmarshaller.unMarshallUnSigned(byteArray); //$NON-NLS-1$	
			entity.setSignature(new Signature());
			SPSSODescriptor sp = (SPSSODescriptor) entity.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().get(0);
			sp.setSignature(new Signature());

			byte[] doc = marshaller.marshallSigned(entity);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Exception caught signing test failure");
		}
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.saml2.handler.impl.MarshallerImpl#marshallSigned(java.lang.String, java.security.PrivateKey, java.lang.String)}.
	 * 
	 * Tests for correct exception generation for null xmlobj
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testMarshallSigned4()
	{
		Marshaller<EntityDescriptor> marshaller;
		schemas = new String[] { "saml-schema-protocol-2.0.xsd", "saml-schema-assertion-2.0.xsd",
				"saml-schema-metadata-2.0.xsd" };
		try
		{
			marshaller = new MarshallerImpl<EntityDescriptor>(EntityDescriptor.class.getPackage().getName(), schemas,
					localKeyResolver);

			marshaller.marshallSigned(null);
		}
		catch (MarshallerException e)
		{
			e.printStackTrace();
			fail("MarshallerException should not occur in this test");
		}
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.saml2.handler.impl.MarshallerImpl#marshallSigned(java.lang.String, java.security.PrivateKey, java.lang.String)}.
	 * 
	 * Tests for correct exception generation for marshaller setup without a private key but signing operation requested
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testMarshallSigned4a()
	{
		Marshaller<EntityDescriptor> marshaller;
		schemas = new String[] { "saml-schema-protocol-2.0.xsd", "saml-schema-assertion-2.0.xsd",
				"saml-schema-metadata-2.0.xsd" };
		EntityDescriptor entityDescriptor = new EntityDescriptor();
		try
		{
			marshaller = new MarshallerImpl<EntityDescriptor>(EntityDescriptor.class.getPackage().getName(), schemas);

			marshaller.marshallSigned(entityDescriptor);
		}
		catch (MarshallerException e)
		{
			e.printStackTrace();
			fail("MarshallerException should not occur in this test");
		}
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.saml2.handler.impl.MarshallerImpl#marshallSigned(java.lang.String, java.security.PrivateKey, java.lang.String)}.
	 * 
	 * Tests for correct document generation from supplied JAXB populated object
	 */
	@Test
	public void testMarshallUnSigned1()
	{
		Marshaller<EntityDescriptor> marshaller;
		Marshaller<CacheClearService> marshaller2;

		try
		{

			/*
			 * We will set a KeyInfo block to the public key of the private key the document is signed with Obviously
			 * this is for testing purposes only and MUST NEVER be done in anything even remotely close to non test code
			 */
			RSAPublicKey rsaPK = (RSAPublicKey) pk;

			/* Supplied private/public key will be in RSA format */
			marshaller = new MarshallerImpl<EntityDescriptor>(EntityDescriptor.class.getPackage().getName(), schemas);
			marshaller2 = new MarshallerImpl<CacheClearService>(CacheClearService.class.getPackage().getName(), schemas);

			EntityDescriptor entityDescriptor = new EntityDescriptor();
			SPSSODescriptor spSSODescriptor = new SPSSODescriptor();
			KeyDescriptor keyDescriptor = new KeyDescriptor();
			KeyInfo keyInfo = new KeyInfo();
			KeyValue keyValue = new KeyValue();
			RSAKeyValue rsaKeyValue = new RSAKeyValue();
			EndpointType singleLogoutService = new EndpointType();
			IndexedEndpointType assertionConsumerService = new IndexedEndpointType();
			AttributeConsumingService attributeConsumingService = new AttributeConsumingService();
			LocalizedNameType localizedName = new LocalizedNameType();
			RequestedAttribute requestedAttribute = new RequestedAttribute();
			CacheClearService cacheClearService = new CacheClearService();

			entityDescriptor.setID("_12345-67890");
			entityDescriptor.setEntityID("987562314");
			spSSODescriptor.setID("_12345-678901");
			spSSODescriptor.setAuthnRequestsSigned(true);
			spSSODescriptor.getProtocolSupportEnumerations().add(ProtocolConstants.protocol);
			spSSODescriptor.getProtocolSupportEnumerations().add("a");
			keyDescriptor.setUse(KeyTypes.SIGNING);

			// JAXBElement<String> keyName = new JAXBElement<String>(new QName("ds:KeyName"), String.class, "myrsakey");

			KeyName keyName = new KeyName(keyAlias);
			keyInfo.getContent().add(keyName);
			rsaKeyValue.setExponent(rsaPK.getPublicExponent().toByteArray());
			rsaKeyValue.setModulus(rsaPK.getModulus().toByteArray());
			keyValue.getContent().add(rsaKeyValue);
			keyInfo.getContent().add(keyValue);

			keyDescriptor.setKeyInfo(keyInfo); // spSSODescriptor.getKeyDescriptors().add(keyDescriptor);

			singleLogoutService.setBinding(BindingConstants.soap);
			singleLogoutService.setLocation("https://spep1.qut.edu.au");
			spSSODescriptor.getSingleLogoutServices().add(singleLogoutService);
			spSSODescriptor.getNameIDFormats().add(NameIDFormatConstants.trans);
			spSSODescriptor.getNameIDFormats().add("a");
			assertionConsumerService.setIsDefault(true);
			assertionConsumerService.setIndex(0);
			assertionConsumerService.setBinding(BindingConstants.httpPost);
			assertionConsumerService.setBinding("a");
			assertionConsumerService.setLocation("https://spep1.qut.edu.au");
			assertionConsumerService.setLocation("a");
			spSSODescriptor.getAssertionConsumerServices().add(assertionConsumerService);
			attributeConsumingService.setIndex(0);
			localizedName.setLang(Locale.ENGLISH.getLanguage());
			localizedName.setValue("Crazy SPEP of candy death");
			attributeConsumingService.getServiceNames().add(localizedName);
			requestedAttribute.setNameFormat(AttributeFormatConstants.uri);
			requestedAttribute.setName("urn:oid:1.3.6.1.4.1.5923.1.1.1.7");
			requestedAttribute.setFriendlyName("eduPersonEntitlement");
			attributeConsumingService.getRequestedAttributes().add(requestedAttribute);
			spSSODescriptor.getAttributeConsumingServices().add(attributeConsumingService);

			/*
			 * To embed extenstions we need to do a double marshal to get an element, its slightly messy but not used
			 * often
			 */
			cacheClearService.setBinding(BindingConstants.soap);
			cacheClearService.setLocation("http://spep1.qut.edu.au/clear");
			cacheClearService.setResponseLocation("http://spep1.qut.edu.au/clear");

			Element element = marshaller2.marshallUnSignedElement(cacheClearService);
			spSSODescriptor.setExtensions(new Extensions());
			spSSODescriptor.getExtensions().getImplementedExtensions().add(element);

			entityDescriptor.getIDPDescriptorAndSSODescriptorAndRoleDescriptors().add(spSSODescriptor);

			byte[] doc = marshaller.marshallUnSigned(entityDescriptor);

			assertNotNull("Generated XML document should not be null", doc);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Exception caught signing test failure");
		}
	}

	/**
	 * Test method for
	 * {@link com.qut.middleware.saml2.handler.impl.MarshallerImpl#marshallSigned(java.lang.String, java.security.PrivateKey, java.lang.String)}.
	 * 
	 * Tests for exception generation when null passed to marshall operation
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testMarshallUnSigned2()
	{
		Marshaller<EntityDescriptor> marshaller;

		/* Supplied private/public key will be in RSA format */
		try
		{
			marshaller = new MarshallerImpl<EntityDescriptor>(CacheClearService.class.getPackage().getName(), schemas);
			marshaller.marshallUnSigned(null);
		}
		catch (MarshallerException e)
		{
			e.printStackTrace();
			fail("MarshallerException should not occur in this test");
		}
	}

	/* Tests to ensure correct operation when marshalling single element */
	@Test
	public void testMarshallUnsignedElement1()
	{
		try
		{
			Marshaller<Organization> marshaller = new MarshallerImpl<Organization>(
					"com.qut.middleware.saml2.schemas.metadata", schemas);

			Organization organization = new Organization();

			LocalizedNameType orgName = new LocalizedNameType();
			LocalizedNameType orgDisplName = new LocalizedNameType();
			OrganizationURL orgURL = new OrganizationURL();
			orgName.setLang("en");
			orgName.setValue("org111111");
			orgDisplName.setLang("en");
			orgDisplName.setValue("orgDisplName1");
			orgURL.setLang("en");
			orgURL.setValue("orgURL1");
			organization.getOrganizationDisplayNames().add(orgDisplName);
			organization.getOrganizationNames().add(orgName);
			organization.getOrganizationURLs().add(orgURL);

			Element element = marshaller.marshallUnSignedElement(organization);

			assertEquals("Ensure the Organization name comes back correctly", "en", element.getFirstChild()
					.getAttributes().getNamedItem("xml:lang").getNodeValue());
		}
		catch (Exception e)
		{
			fail("Exception state should not occur in this test");
		}
	}
	
	/* Tests to ensure exception is thrown when null passed to marshall node */
	@Test(expected = IllegalArgumentException.class)
	public void testMarshallUnsignedElement2()
	{
		Marshaller<EntityDescriptor> marshaller;

		/* Supplied private/public key will be in RSA format */
		try
		{
			marshaller = new MarshallerImpl<EntityDescriptor>(CacheClearService.class.getPackage().getName(), schemas);
			marshaller.marshallUnSignedElement(null);
		}
		catch (MarshallerException e)
		{
			e.printStackTrace();
			fail("MarshallerException should not occur in this test");
		}
	}
}
