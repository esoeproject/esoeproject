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
 * Purpose: Test out all functionality of saml2lib-j for unmarshalling purposes
 */

package com.qut.middleware.saml2.handler;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.qut.middleware.saml2.ExternalKeyResolver;
import com.qut.middleware.saml2.exception.KeyResolutionException;
import com.qut.middleware.saml2.exception.ReferenceValueException;
import com.qut.middleware.saml2.exception.SignatureValueException;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.handler.impl.UnmarshallerImpl;
import com.qut.middleware.saml2.schemas.assertion.AudienceRestriction;
import com.qut.middleware.saml2.schemas.assertion.ConditionAbstractType;
import com.qut.middleware.saml2.schemas.esoe.lxacml.PolicySet;
import com.qut.middleware.saml2.schemas.metadata.EntityDescriptor;
import com.qut.middleware.saml2.schemas.metadata.RoleDescriptorType;
import com.qut.middleware.saml2.schemas.metadata.SPSSODescriptor;
import com.qut.middleware.saml2.schemas.metadata.extensions.CacheClearService;
import com.qut.middleware.saml2.schemas.protocol.AuthnRequest;
import com.qut.middleware.saml2.schemas.protocol.LogoutResponse;
import com.qut.middleware.saml2.schemas.protocol.StatusResponseType;
import com.qut.middleware.saml2.sec.KeyData;

@SuppressWarnings(value = { "unqualified-field-access", "nls" })
public class UnmarshallerTest
{
	private String path;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		this.path = System.getProperty("user.dir") + File.separator + "tests" + File.separator + "testdata" + File.separator;

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

	/*
	 * Tests for validity of exception state when null passed to constructor for package name
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testUnmarshallerException1() throws Exception
	{
		Unmarshaller<AuthnRequest> unmarshaller;
		String[] schemas = new String[] { "saml-schema-protocol-2.0.xsd", "saml-schema-assertion-2.0.xsd" };
		unmarshaller = new UnmarshallerImpl<AuthnRequest>(null, schemas);
	}

	/*
	 * Tests for validity of exception state when null passed to constructor for schemas
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testUnmarshallerException2() throws Exception
	{
		Unmarshaller<AuthnRequest> unmarshaller;
		String[] schemas = new String[] { "saml-schema-protocol-2.0.xsd", "saml-schema-assertion-2.0.xsd" };
		unmarshaller = new UnmarshallerImpl<AuthnRequest>(AuthnRequest.class.getPackage().getName(), null);
	}

	/*
	 * Tests for validity of exception state when null passed to constructor for key resolver
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testUnmarshallerException3() throws Exception
	{

		Unmarshaller<AuthnRequest> unmarshaller;
		String[] schemas = new String[] { "saml-schema-protocol-2.0.xsd", "saml-schema-assertion-2.0.xsd" };
		unmarshaller = new UnmarshallerImpl<AuthnRequest>(AuthnRequest.class.getPackage().getName(), schemas, null);
	}

	/*
	 * Tests for validity of exception state when unknown schema (based on local classpath) is passed to schema list
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testUnmarshallerException4() throws Exception
	{
		PublicKey pk;
		KeyStore ks = KeyStore.getInstance("PKCS12");
		FileInputStream fis = new FileInputStream(this.path + "tests.ks");
		char[] passwd = { 't', 'e', 's', 't', 'p', 'a', 's', 's' };
		ks.load(fis, passwd);

		Certificate cert = ks.getCertificate("myrsakey");
		pk = cert.getPublicKey();

		ExternalKeyResolver resolver = createMock(ExternalKeyResolver.class);
		expect(resolver.resolveKey("myrsakey")).andReturn(pk);
		replay(resolver);

		Unmarshaller<AuthnRequest> unmarshaller;
		String[] schemas = new String[] { "my-fake-schema.xsd", "saml-schema-assertion-2.0.xsd" };
		unmarshaller = new UnmarshallerImpl<AuthnRequest>(AuthnRequest.class.getPackage().getName(), schemas, resolver);

		verify(resolver);
	}

	/*
	 * Tests for validity of exception state when unknown package is passed
	 */
	@Test(expected = UnmarshallerException.class)
	public final void testUnmarshallerException5() throws Exception
	{
		PublicKey pk;
		KeyStore ks = KeyStore.getInstance("PKCS12");
		FileInputStream fis = new FileInputStream(this.path + "tests.ks");
		char[] passwd = { 't', 'e', 's', 't', 'p', 'a', 's', 's' };
		ks.load(fis, passwd);

		Certificate cert = ks.getCertificate("myrsakey");
		pk = cert.getPublicKey();

		ExternalKeyResolver resolver = createMock(ExternalKeyResolver.class);
		expect(resolver.resolveKey("myrsakey")).andReturn(pk);
		replay(resolver);

		Unmarshaller<AuthnRequest> unmarshaller;
		String[] schemas = new String[] { "saml-schema-assertion-2.0.xsd" };
		unmarshaller = new UnmarshallerImpl<AuthnRequest>("this.is.a.fake.package", schemas, resolver);

		verify(resolver);
	}

	/*
	 * Tests for validity of exception state when unknown jsr105 provider is passed
	 */
	@Test(expected = UnmarshallerException.class)
	public final void testUnmarshallerException6() throws UnmarshallerException
	{

		System.setProperty("jsr105Provider", "some.fake.Class");

		String filename = this.path + "AuthnRequestSigned-valid.xml";
		PublicKey pk = null;

		Unmarshaller<AuthnRequest> unmarshaller;
		String[] schemas = new String[] { "saml-schema-protocol-2.0.xsd", "saml-schema-assertion-2.0.xsd" };
		unmarshaller = new UnmarshallerImpl<AuthnRequest>(AuthnRequest.class.getPackage().getName(), schemas);

		try
		{
			KeyStore ks = KeyStore.getInstance("PKCS12");
			FileInputStream fis = new FileInputStream(this.path + "tests.ks");
			char[] passwd = { 't', 'e', 's', 't', 'p', 'a', 's', 's' };
			ks.load(fis, passwd);

			Certificate cert = ks.getCertificate("myrsakey");
			pk = cert.getPublicKey();

			// Get the size of the file
			File file = new File(filename);
			long length = file.length();

			// Create the byte array to hold the data
			byte[] bytes = new byte[(int) length];

			// Read in the bytes
			InputStream fileStream = new FileInputStream(file);
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead = fileStream.read(bytes, offset, bytes.length - offset)) >= 0)
			{
				offset += numRead;
			}

			// Close the input stream and return bytes
			fileStream.close();

			AuthnRequest authn = unmarshaller.unMarshallSigned(pk, bytes); //$NON-NLS-1$	
			System.out.println(authn.getID());

		}
		catch (SignatureValueException sve)
		{
			fail("Unexpected failure when validating document. Reason: " + sve.getMessage());
		}
		catch (ReferenceValueException rve)
		{
			fail("Unexpected failure when validating document. Reason: " + rve.getMessage());
		}
		catch (UnsupportedEncodingException uee)
		{
			fail("Unexpected failure when validating document. Reason: " + uee.getMessage());
		}
		catch (IOException e)
		{
			fail("Unexpected failure when validating document. Reason: " + e.getMessage());
		}
		catch (KeyStoreException e)
		{
			fail("Unexpected failure when validating document. Reason: " + e.getMessage());
		}
		catch (NoSuchAlgorithmException e)
		{
			fail("Unexpected failure when validating document. Reason: " + e.getMessage());
		}
		catch (CertificateException e)
		{
			fail("Unexpected failure when validating document. Reason: " + e.getMessage());
		}

	}

	/*
	 * Tests for validity of unmarshallsigned method of Unmarshaller
	 */
	@Test
	public final void testUnmarshallSigned1() throws Exception
	{
		String filename = this.path + "AuthnRequestSigned-valid.xml";
		PublicKey pk = null;

		Unmarshaller<AuthnRequest> unmarshaller;
		String[] schemas = new String[] { "saml-schema-protocol-2.0.xsd", "saml-schema-assertion-2.0.xsd" };
		unmarshaller = new UnmarshallerImpl<AuthnRequest>(AuthnRequest.class.getPackage().getName(), schemas);

		try
		{
			KeyStore ks = KeyStore.getInstance("PKCS12");
			FileInputStream fis = new FileInputStream(this.path + "tests.ks");
			char[] passwd = { 't', 'e', 's', 't', 'p', 'a', 's', 's' };
			ks.load(fis, passwd);

			Certificate cert = ks.getCertificate("myrsakey");
			pk = cert.getPublicKey();

			// Get the size of the file
			File file = new File(filename);
			long length = file.length();

			// Create the byte array to hold the data
			byte[] bytes = new byte[(int) length];

			// Read in the bytes
			InputStream fileStream = new FileInputStream(file);
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead = fileStream.read(bytes, offset, bytes.length - offset)) >= 0)
			{
				offset += numRead;
			}

			// Close the input stream and return bytes
			fileStream.close();

			AuthnRequest authn = unmarshaller.unMarshallSigned(pk, bytes); //$NON-NLS-1$	
		
			if (authn == null)
			{
				fail("Failed to correctly unmarshall AuthnRequest");
				return;
			}

			assertEquals("Expected Signature ID not supplied", "abe567de6-122wert67", authn.getID());
		}
		catch (SignatureValueException sve)
		{
			fail("Unexpected failure when validating document. Reason: " + sve.getMessage());
		}
		catch (ReferenceValueException rve)
		{
			fail("Unexpected failure when validating document. Reason: " + rve.getMessage());
		}
		catch (UnmarshallerException ue)
		{
			fail("Unexpected failure when validating document. Reason: " + ue.getMessage());
		}
		catch (UnsupportedEncodingException uee)
		{
			fail("Unexpected failure when validating document. Reason: " + uee.getMessage());
		}
		catch (Exception e)
		{
			fail("Unexpected and unknown failure when validating document");
		}
	}

	/*
	 * Tests for expected failure of unmarshallsigned when xml content is tampered with during transmission
	 */
	@Test(expected = ReferenceValueException.class)
	public final void testUnmarshallSigned2() throws ReferenceValueException
	{
		String filename = this.path + "AuthnRequestSigned-tampered.xml";

		PublicKey pk = null;

		Unmarshaller<AuthnRequest> unmarshaller;
		String[] schemas = new String[] { "saml-schema-protocol-2.0.xsd", "saml-schema-assertion-2.0.xsd" };

		try
		{
			unmarshaller = new UnmarshallerImpl<AuthnRequest>(EntityDescriptor.class.getPackage().getName(), schemas);

			KeyStore ks = KeyStore.getInstance("PKCS12");
			FileInputStream fis = new FileInputStream(this.path + "tests.ks");
			char[] passwd = { 't', 'e', 's', 't', 'p', 'a', 's', 's' };
			ks.load(fis, passwd);

			Certificate cert = ks.getCertificate("myrsakey");
			pk = cert.getPublicKey();

			// Get the size of the file
			File file = new File(filename);
			long length = file.length();
			byte[] byteArray = new byte[(int) length];

			InputStream fileStream = new FileInputStream(file);
			fileStream.read(byteArray);
			fileStream.close();

			unmarshaller.unMarshallSigned(pk, byteArray); //$NON-NLS-1$			
		}
		catch (SignatureValueException sve)
		{
			fail("Unexpected failure when validating document. Reason: " + sve.getMessage());
		}
		catch (UnmarshallerException ue)
		{
			fail("Unexpected failure when validating document. Reason: " + ue.getMessage());
		}
		catch (UnsupportedEncodingException uee)
		{
			fail("Unexpected failure when validating document. Reason: " + uee.getMessage());
		}
		catch (FileNotFoundException fnfe)
		{
			fail("Unexpected failure when validating document. Reason: " + fnfe.getMessage());
		}
		catch (KeyStoreException kse)
		{
			fail("Unexpected failure when validating document. Reason: " + kse.getMessage());
		}
		catch (NoSuchAlgorithmException nsae)
		{
			fail("Unexpected failure when validating document. Reason: " + nsae.getMessage());
		}
		catch (CertificateException ce)
		{
			fail("Unexpected failure when validating document. Reason: " + ce.getMessage());
		}
		catch (IOException ioe)
		{
			fail("Unexpected failure when validating document. Reason: " + ioe.getMessage());
		}
		finally
		{

		}
	}

	/*
	 * Tests for expected failure of unmarshallsigned when xml signature is tampered with during transmission
	 */
	@Test(expected = SignatureValueException.class)
	public final void testUnmarshallSigned2a() throws SignatureValueException
	{
		String filename = this.path + "AuthnRequestSigned-invalid-signature.xml";

		PublicKey pk = null;

		Unmarshaller<AuthnRequest> unmarshaller;
		String[] schemas = new String[] { "saml-schema-protocol-2.0.xsd", "saml-schema-assertion-2.0.xsd" };

		try
		{
			unmarshaller = new UnmarshallerImpl<AuthnRequest>(EntityDescriptor.class.getPackage().getName(), schemas);

			KeyStore ks = KeyStore.getInstance("PKCS12");
			FileInputStream fis = new FileInputStream(this.path + "tests.ks");
			char[] passwd = { 't', 'e', 's', 't', 'p', 'a', 's', 's' };
			ks.load(fis, passwd);

			Certificate cert = ks.getCertificate("myrsakey");
			pk = cert.getPublicKey();

			// Get the size of the file
			File file = new File(filename);
			long length = file.length();

			// Create the byte array to hold the data
			byte[] bytes = new byte[(int) length];

			// Read in the bytes
			InputStream fileStream = new FileInputStream(file);
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead = fileStream.read(bytes, offset, bytes.length - offset)) >= 0)
			{
				offset += numRead;
			}

			// Close the input stream and return bytes
			fileStream.close();

			unmarshaller.unMarshallSigned(pk, bytes); //$NON-NLS-1$			
		}
		catch (ReferenceValueException rve)
		{
			fail("Unexpected failure when validating document. Reason: " + rve.getMessage());
		}
		catch (UnmarshallerException ue)
		{
			fail("Unexpected failure when validating document. Reason: " + ue.getMessage());
		}
		catch (UnsupportedEncodingException uee)
		{
			fail("Unexpected failure when validating document. Reason: " + uee.getMessage());
		}
		catch (FileNotFoundException fnfe)
		{
			fail("Unexpected failure when validating document. Reason: " + fnfe.getMessage());
		}
		catch (KeyStoreException kse)
		{
			fail("Unexpected failure when validating document. Reason: " + kse.getMessage());
		}
		catch (NoSuchAlgorithmException nsae)
		{
			fail("Unexpected failure when validating document. Reason: " + nsae.getMessage());
		}
		catch (CertificateException ce)
		{
			fail("Unexpected failure when validating document. Reason: " + ce.getMessage());
		}
		catch (IOException ioe)
		{
			fail("Unexpected failure when validating document. Reason: " + ioe.getMessage());
		}
	}

	/*
	 * Tests for validity of unmarshall signed method of Unmarshaller
	 */
	@Test
	public final void testUnmarshallSigned3() throws Exception
	{
		String filename = this.path + "AuthnRequestSigned-valid-keyinfo.xml";

		PublicKey pk = null;

		try
		{
			KeyStore ks = KeyStore.getInstance("PKCS12");
			FileInputStream fis = new FileInputStream(this.path + "tests.ks");
			char[] passwd = { 't', 'e', 's', 't', 'p', 'a', 's', 's' };
			ks.load(fis, passwd);

			Certificate cert = ks.getCertificate("myrsakey");
			pk = cert.getPublicKey();

			ExternalKeyResolver resolver = createMock(ExternalKeyResolver.class);
			expect(resolver.resolveKey("myrsakey")).andReturn(pk);
			replay(resolver);

			Unmarshaller<AuthnRequest> unmarshaller;
			String[] schemas = new String[] { "saml-schema-protocol-2.0.xsd", "saml-schema-assertion-2.0.xsd" };
			unmarshaller = new UnmarshallerImpl<AuthnRequest>(AuthnRequest.class.getPackage().getName(), schemas, resolver);

			// Get the size of the file
			File file = new File(filename);
			long length = file.length();

			// Create the byte array to hold the data
			byte[] bytes = new byte[(int) length];

			// Read in the bytes
			InputStream fileStream = new FileInputStream(file);
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead = fileStream.read(bytes, offset, bytes.length - offset)) >= 0)
			{
				offset += numRead;
			}

			// Close the input stream and return bytes
			fileStream.close();

			AuthnRequest authn = unmarshaller.unMarshallSigned(bytes); //$NON-NLS-1$	

			verify(resolver);

			if (authn == null)
			{
				fail("Failed to correctly unmarshall AuthnRequest");
				return;
			}

			assertEquals("Expected Signature ID not supplied", "abe567de6-122wert67", authn.getID());
		}
		catch (SignatureValueException sve)
		{
			fail("Unexpected failure when validating document. Reason: " + sve.getMessage());
		}
		catch (ReferenceValueException rve)
		{
			fail("Unexpected failure when validating document. Reason: " + rve.getMessage());
		}
		catch (UnmarshallerException ue)
		{
			fail("Unexpected failure when validating document. Reason: " + ue.getMessage());
		}
		catch (UnsupportedEncodingException uee)
		{
			fail("Unexpected failure when validating document. Reason: " + uee.getMessage());
		}
		catch (Exception e)
		{
			fail("Unexpected and unknown failure when validating document");
		}
	}

	/*
	 * Tests for expected failure of unmarshallsigned when xml content is tampered with during transmission
	 */
	@Test(expected = ReferenceValueException.class)
	public final void testUnmarshallSigned4() throws ReferenceValueException
	{
		String filename = this.path + "AuthnRequestSigned-tampered-keyinfo.xml";

		PublicKey pk = null;

		try
		{
			KeyStore ks = KeyStore.getInstance("PKCS12");
			FileInputStream fis = new FileInputStream(this.path + "tests.ks");
			char[] passwd = { 't', 'e', 's', 't', 'p', 'a', 's', 's' };
			ks.load(fis, passwd);

			Certificate cert = ks.getCertificate("myrsakey");
			pk = cert.getPublicKey();

			ExternalKeyResolver resolver = createMock(ExternalKeyResolver.class);
			expect(resolver.resolveKey("myrsakey")).andReturn(pk);
			replay(resolver);

			Unmarshaller<AuthnRequest> unmarshaller;
			String[] schemas = new String[] { "saml-schema-protocol-2.0.xsd", "saml-schema-assertion-2.0.xsd" };
			unmarshaller = new UnmarshallerImpl<AuthnRequest>(EntityDescriptor.class.getPackage().getName(), schemas, resolver);

			// Get the size of the file
			File file = new File(filename);
			long length = file.length();

			// Create the byte array to hold the data
			byte[] bytes = new byte[(int) length];

			// Read in the bytes
			InputStream fileStream = new FileInputStream(file);
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead = fileStream.read(bytes, offset, bytes.length - offset)) >= 0)
			{
				offset += numRead;
			}

			// Close the input stream and return bytes
			fileStream.close();

			unmarshaller.unMarshallSigned(bytes); //$NON-NLS-1$	

			verify(resolver);
		}
		catch (SignatureValueException sve)
		{
			fail("Unexpected failure when validating document. Reason: " + sve.getMessage());
		}
		catch (UnmarshallerException ue)
		{
			fail("Unexpected failure when validating document. Reason: " + ue.getMessage());
		}
		catch (UnsupportedEncodingException uee)
		{
			fail("Unexpected failure when validating document. Reason: " + uee.getMessage());
		}
		catch (FileNotFoundException fnfe)
		{
			fail("Unexpected failure when validating document. Reason: " + fnfe.getMessage());
		}
		catch (KeyStoreException kse)
		{
			fail("Unexpected failure when validating document. Reason: " + kse.getMessage());
		}
		catch (NoSuchAlgorithmException nsae)
		{
			fail("Unexpected failure when validating document. Reason: " + nsae.getMessage());
		}
		catch (CertificateException ce)
		{
			fail("Unexpected failure when validating document. Reason: " + ce.getMessage());
		}
		catch (IOException ioe)
		{
			fail("Unexpected failure when validating document. Reason: " + ioe.getMessage());
		}
		catch (KeyResolutionException e)
		{
			fail("Unexpected failure when validating document. Reason: " + e.getMessage());
		}
	}

	/*
	 * Tests for expected failure of unmarshallsigned when xml signature is tampered with during transmission
	 */
	@Test(expected = SignatureValueException.class)
	public final void testUnmarshallSigned4a() throws SignatureValueException
	{
		String filename = this.path + "AuthnRequestSigned-invalid-signature-keyinfo.xml";

		PublicKey pk = null;

		try
		{

			KeyStore ks = KeyStore.getInstance("PKCS12");
			FileInputStream fis = new FileInputStream(this.path + "tests.ks");
			char[] passwd = { 't', 'e', 's', 't', 'p', 'a', 's', 's' };
			ks.load(fis, passwd);

			Certificate cert = ks.getCertificate("myrsakey");
			pk = cert.getPublicKey();

			ExternalKeyResolver resolver = createMock(ExternalKeyResolver.class);
			expect(resolver.resolveKey("myrsakey")).andReturn(pk);
			replay(resolver);

			Unmarshaller<AuthnRequest> unmarshaller;
			String[] schemas = new String[] { "saml-schema-protocol-2.0.xsd", "saml-schema-assertion-2.0.xsd" };
			unmarshaller = new UnmarshallerImpl<AuthnRequest>(AuthnRequest.class.getPackage().getName(), schemas, resolver);

			// Get the size of the file
			File file = new File(filename);
			long length = file.length();

			// Create the byte array to hold the data
			byte[] bytes = new byte[(int) length];

			// Read in the bytes
			InputStream fileStream = new FileInputStream(file);
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead = fileStream.read(bytes, offset, bytes.length - offset)) >= 0)
			{
				offset += numRead;
			}

			// Close the input stream and return bytes
			fileStream.close();

			unmarshaller.unMarshallSigned(bytes); //$NON-NLS-1$	

			verify(resolver);
		}
		catch (ReferenceValueException rve)
		{
			fail("Unexpected failure when validating document. Reason: " + rve.getMessage());
		}
		catch (UnmarshallerException ue)
		{
			fail("Unexpected failure when validating document. Reason: " + ue.getMessage());
		}
		catch (UnsupportedEncodingException uee)
		{
			fail("Unexpected failure when validating document. Reason: " + uee.getMessage());
		}
		catch (FileNotFoundException fnfe)
		{
			fail("Unexpected failure when validating document. Reason: " + fnfe.getMessage());
		}
		catch (KeyStoreException kse)
		{
			fail("Unexpected failure when validating document. Reason: " + kse.getMessage());
		}
		catch (NoSuchAlgorithmException nsae)
		{
			fail("Unexpected failure when validating document. Reason: " + nsae.getMessage());
		}
		catch (CertificateException ce)
		{
			fail("Unexpected failure when validating document. Reason: " + ce.getMessage());
		}
		catch (IOException ioe)
		{
			fail("Unexpected failure when validating document. Reason: " + ioe.getMessage());
		}
		catch (KeyResolutionException e)
		{
			fail("Unexpected failure when validating document. Reason: " + e.getMessage());
		}
	}

	/*
	 * Tests for expected failure of unmarshallsigned when keyname is unknown
	 */
	@Test(expected = UnmarshallerException.class)
	public final void testUnmarshallSigned4b() throws UnmarshallerException
	{
		String filename = this.path + "AuthnRequestSigned-invalid-signature-keyinfo.xml";

		PublicKey pk = null;

		try
		{

			KeyStore ks = KeyStore.getInstance("PKCS12");
			FileInputStream fis = new FileInputStream(this.path + "tests.ks");
			char[] passwd = { 't', 'e', 's', 't', 'p', 'a', 's', 's' };
			ks.load(fis, passwd);

			Certificate cert = ks.getCertificate("myrsakey");
			pk = cert.getPublicKey();

			ExternalKeyResolver resolver = createMock(ExternalKeyResolver.class);
			expect(resolver.resolveKey("myrsakey")).andReturn(null);
			replay(resolver);

			Unmarshaller<AuthnRequest> unmarshaller;
			String[] schemas = new String[] { "saml-schema-protocol-2.0.xsd", "saml-schema-assertion-2.0.xsd" };
			unmarshaller = new UnmarshallerImpl<AuthnRequest>(EntityDescriptor.class.getPackage().getName(), schemas, resolver);

			// Get the size of the file
			File file = new File(filename);
			long length = file.length();

			// Create the byte array to hold the data
			byte[] bytes = new byte[(int) length];

			// Read in the bytes
			InputStream fileStream = new FileInputStream(file);
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead = fileStream.read(bytes, offset, bytes.length - offset)) >= 0)
			{
				offset += numRead;
			}

			// Close the input stream and return bytes
			fileStream.close();

			unmarshaller.unMarshallSigned(bytes); //$NON-NLS-1$	
		}
		catch (ReferenceValueException rve)
		{
			fail("Unexpected failure when validating document. Reason: " + rve.getMessage());
		}
		catch (SignatureValueException sve)
		{
			fail("Unexpected failure when validating document. Reason: " + sve.getMessage());
		}
		catch (UnsupportedEncodingException uee)
		{
			fail("Unexpected failure when validating document. Reason: " + uee.getMessage());
		}
		catch (FileNotFoundException fnfe)
		{
			fail("Unexpected failure when validating document. Reason: " + fnfe.getMessage());
		}
		catch (KeyStoreException kse)
		{
			fail("Unexpected failure when validating document. Reason: " + kse.getMessage());
		}
		catch (NoSuchAlgorithmException nsae)
		{
			fail("Unexpected failure when validating document. Reason: " + nsae.getMessage());
		}
		catch (CertificateException ce)
		{
			fail("Unexpected failure when validating document. Reason: " + ce.getMessage());
		}
		catch (IOException ioe)
		{
			fail("Unexpected failure when validating document. Reason: " + ioe.getMessage());
		}
		catch (KeyResolutionException e)
		{
			fail("Unexpected failure when validating document. Reason: " + e.getMessage());
		}
	}

	/*
	 * Tests for expected exception when document supplied is null
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testUnmarshallSigned5() throws Exception
	{
		PublicKey pk = null;

		Unmarshaller<AuthnRequest> unmarshaller;
		String[] schemas = new String[] { "saml-schema-protocol-2.0.xsd", "saml-schema-assertion-2.0.xsd" };
		unmarshaller = new UnmarshallerImpl<AuthnRequest>(AuthnRequest.class.getPackage().getName(), schemas);

		KeyStore ks = KeyStore.getInstance("PKCS12");
		FileInputStream fis = new FileInputStream(this.path + "tests.ks");
		char[] passwd = { 't', 'e', 's', 't', 'p', 'a', 's', 's' };
		ks.load(fis, passwd);

		Certificate cert = ks.getCertificate("myrsakey");
		pk = cert.getPublicKey();

		AuthnRequest authn = unmarshaller.unMarshallSigned(pk, null);
	}

	/*
	 * Tests for expected exception when key supplied is null
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testUnmarshallSigned5a() throws Exception
	{
		Unmarshaller<AuthnRequest> unmarshaller;
		String[] schemas = new String[] { "saml-schema-protocol-2.0.xsd", "saml-schema-assertion-2.0.xsd" };
		unmarshaller = new UnmarshallerImpl<AuthnRequest>(AuthnRequest.class.getPackage().getName(), schemas);

		AuthnRequest authn = unmarshaller.unMarshallSigned(null, new String("<some fake document/>").getBytes());
	}

	/*
	 * Tests for expected exception when document supplied is null
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testUnmarshallSigned5b() throws Exception
	{
		PublicKey pk = null;

		Unmarshaller<AuthnRequest> unmarshaller;
		String[] schemas = new String[] { "saml-schema-protocol-2.0.xsd", "saml-schema-assertion-2.0.xsd" };
		unmarshaller = new UnmarshallerImpl<AuthnRequest>(AuthnRequest.class.getPackage().getName(), schemas);

		AuthnRequest authn = unmarshaller.unMarshallSigned(null);
	}

	/*
	 * Tests for expected exception when no external key resolver is passed to constructor
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testUnmarshallSigned5c() throws Exception
	{
		PublicKey pk = null;

		Unmarshaller<AuthnRequest> unmarshaller;
		String[] schemas = new String[] { "saml-schema-protocol-2.0.xsd", "saml-schema-assertion-2.0.xsd" };
		unmarshaller = new UnmarshallerImpl<AuthnRequest>(AuthnRequest.class.getPackage().getName(), schemas);

		AuthnRequest authn = unmarshaller.unMarshallSigned(new String("<some fake document/>").getBytes());
	}

	/*
	 * Tests for validity of unmarshallmetadata method of Unmarshaller
	 */
	@Test
	public final void testUnmarshallMetadata1() throws Exception
	{
		String filename = this.path + "SAMLMetadataSigned.xml";
		PublicKey pk = null;
		Unmarshaller<EntityDescriptor> unmarshaller;
		Unmarshaller<CacheClearService> unmarshaller2;

		String[] schemas = new String[] { "saml-schema-metadata-2.0.xsd", "cacheclear-schema-saml-metadata.xsd" };
		unmarshaller = new UnmarshallerImpl<EntityDescriptor>(EntityDescriptor.class.getPackage().getName(), schemas);
		unmarshaller2 = new UnmarshallerImpl<CacheClearService>(CacheClearService.class.getPackage().getName(), schemas);

		try
		{
			KeyStore ks = KeyStore.getInstance("PKCS12");
			FileInputStream fis = new FileInputStream(this.path + "tests.ks");
			char[] passwd = { 't', 'e', 's', 't', 'p', 'a', 's', 's' };
			ks.load(fis, passwd);

			Certificate cert = ks.getCertificate("myrsakey");
			pk = cert.getPublicKey();

			File file = new File(filename);
			long length = file.length();
			byte[] byteArray = new byte[(int) length];

			InputStream fileStream = new FileInputStream(file);
			fileStream.read(byteArray);
			fileStream.close();

			Map<String, KeyData> keys = new HashMap<String, KeyData>();

			EntityDescriptor entity = unmarshaller.unMarshallMetadata(pk, byteArray, keys); //$NON-NLS-1$			
			if (entity == null)
			{
				fail("Failed to correctly unmarshall AuthnRequest");
				return;
			}

			assertEquals("Ensure retrieved public key is identical to keystore version", ((RSAPublicKey) pk).getModulus(), ((RSAPublicKey) keys.get("myrsakey").getPk()).getModulus());

			for (RoleDescriptorType descriptor : entity.getIDPDescriptorAndSSODescriptorAndRoleDescriptors())
			{
				/* Located an SP, deal with its data type */
				if (descriptor instanceof SPSSODescriptor)
				{
					SPSSODescriptor spSSODescriptor = (SPSSODescriptor) descriptor;

					/* Extensions can be complex so test to ensure they work correctly */
					List<Element> elements = spSSODescriptor.getExtensions().getImplementedExtensions();

					/*
					 * We need to individually unmarshall these elements, JAXB isn't quite smart enough to deal with
					 * anyType :)
					 */
					for (Element element : elements)
					{
						if (element.getLocalName().equals("CacheClearService"))
						{
							CacheClearService cacheClearService = unmarshaller2.unMarshallUnSigned(element);

							assertEquals("Ensure the embeded cache clear service details are correct", "http://spep1.qut.edu.au/clear", cacheClearService.getLocation());
						}
					}
				}
			}

		}
		catch (SignatureValueException sve)
		{
			fail("Unexpected failure when validating document. Reason: " + sve.getMessage());
		}
		catch (ReferenceValueException rve)
		{
			fail("Unexpected failure when validating document. Reason: " + rve.getMessage());
		}
		catch (UnmarshallerException ue)
		{
			fail("Unexpected failure when validating document. Reason: " + ue.getMessage());
		}
		catch (UnsupportedEncodingException uee)
		{
			fail("Unexpected failure when validating document. Reason: " + uee.getMessage());
		}
	}

	/*
	 * Tests for validity of unmarshallmetadata method of Unmarshaller (Metadata generated by cpp SAML lib)
	 */
	@Test
	public final void testUnmarshallMetadata1a() throws Exception
	{
		String filename = this.path + "SAMLMetadataSignedCpp.xml";

		PublicKey pk = null;

		Unmarshaller<EntityDescriptor> unmarshaller;

		String[] schemas = new String[] { "saml-schema-metadata-2.0.xsd", "cacheclear-schema-saml-metadata.xsd" };
		unmarshaller = new UnmarshallerImpl<EntityDescriptor>(EntityDescriptor.class.getPackage().getName(), schemas);

		try
		{
			KeyStore ks = KeyStore.getInstance("PKCS12");
			FileInputStream fis = new FileInputStream(this.path + "tests.ks");
			char[] passwd = { 't', 'e', 's', 't', 'p', 'a', 's', 's' };
			ks.load(fis, passwd);

			Certificate cert = ks.getCertificate("myrsakey");
			pk = cert.getPublicKey();

			File file = new File(filename);
			long length = file.length();
			byte[] byteArray = new byte[(int) length];

			InputStream fileStream = new FileInputStream(file);
			fileStream.read(byteArray);
			fileStream.close();

			Map<String, KeyData> keys = new HashMap<String, KeyData>();

			EntityDescriptor entity = unmarshaller.unMarshallMetadata(pk, byteArray, keys); //$NON-NLS-1$			
			if (entity == null)
			{
				fail("Failed to correctly unmarshall SAMLLIB-cpp EntityDescriptor");
				return;
			}
		}
		catch (SignatureValueException sve)
		{
			fail("Unexpected failure when validating document. Reason: " + sve.getMessage());
		}
		catch (ReferenceValueException rve)
		{
			fail("Unexpected failure when validating document. Reason: " + rve.getMessage());
		}
		catch (UnmarshallerException ue)
		{
			fail("Unexpected failure when validating document. Reason: " + ue.getMessage());
		}
		catch (UnsupportedEncodingException uee)
		{
			fail("Unexpected failure when validating document. Reason: " + uee.getMessage());
		}
	}

	/*
	 * Tests for expected exception when pk passed to metadata function is null
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testUnmarshallMetadata2() throws Exception
	{
		String filename = this.path + "SAMLMetadataSigned.xml";
		PublicKey pk = null;
		Unmarshaller<EntityDescriptor> unmarshaller;
		Unmarshaller<CacheClearService> unmarshaller2;

		String[] schemas = new String[] { "saml-schema-metadata-2.0.xsd", "cacheclear-schema-saml-metadata.xsd" };
		unmarshaller = new UnmarshallerImpl<EntityDescriptor>(EntityDescriptor.class.getPackage().getName(), schemas);
		unmarshaller2 = new UnmarshallerImpl<CacheClearService>(CacheClearService.class.getPackage().getName(), schemas);

		try
		{
			KeyStore ks = KeyStore.getInstance("PKCS12");
			FileInputStream fis = new FileInputStream(this.path + "tests.ks");
			char[] passwd = { 't', 'e', 's', 't', 'p', 'a', 's', 's' };
			ks.load(fis, passwd);

			Certificate cert = ks.getCertificate("myrsakey");
			pk = cert.getPublicKey();

			File file = new File(filename);
			long length = file.length();
			byte[] byteArray = new byte[(int) length];

			InputStream fileStream = new FileInputStream(file);
			fileStream.read(byteArray);
			fileStream.close();

			Map<String, KeyData> keys = new HashMap<String, KeyData>();

			EntityDescriptor entity = unmarshaller.unMarshallMetadata(null, byteArray, keys); //$NON-NLS-1$			
		}
		catch (SignatureValueException sve)
		{
			fail("Unexpected failure when validating document. Reason: " + sve.getMessage());
		}
		catch (ReferenceValueException rve)
		{
			fail("Unexpected failure when validating document. Reason: " + rve.getMessage());
		}
		catch (UnmarshallerException ue)
		{
			fail("Unexpected failure when validating document. Reason: " + ue.getMessage());
		}
		catch (UnsupportedEncodingException uee)
		{
			fail("Unexpected failure when validating document. Reason: " + uee.getMessage());
		}
	}

	/*
	 * Tests for expected exception when document passed to metadata function is null
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testUnmarshallMetadata2a() throws Exception
	{
		String filename = this.path + "SAMLMetadataSigned.xml";
		PublicKey pk = null;
		Unmarshaller<EntityDescriptor> unmarshaller;
		Unmarshaller<CacheClearService> unmarshaller2;

		String[] schemas = new String[] { "saml-schema-metadata-2.0.xsd", "cacheclear-schema-saml-metadata.xsd" };
		unmarshaller = new UnmarshallerImpl<EntityDescriptor>(EntityDescriptor.class.getPackage().getName(), schemas);
		unmarshaller2 = new UnmarshallerImpl<CacheClearService>(CacheClearService.class.getPackage().getName(), schemas);

		try
		{
			KeyStore ks = KeyStore.getInstance("PKCS12");
			FileInputStream fis = new FileInputStream(this.path + "tests.ks");
			char[] passwd = { 't', 'e', 's', 't', 'p', 'a', 's', 's' };
			ks.load(fis, passwd);

			Certificate cert = ks.getCertificate("myrsakey");
			pk = cert.getPublicKey();

			File file = new File(filename);
			long length = file.length();
			byte[] byteArray = new byte[(int) length];

			InputStream fileStream = new FileInputStream(file);
			fileStream.read(byteArray);
			fileStream.close();

			Map<String, KeyData> keys = new HashMap<String, KeyData>();

			EntityDescriptor entity = unmarshaller.unMarshallMetadata(pk, null, keys); //$NON-NLS-1$			
		}
		catch (SignatureValueException sve)
		{
			fail("Unexpected failure when validating document. Reason: " + sve.getMessage());
		}
		catch (ReferenceValueException rve)
		{
			fail("Unexpected failure when validating document. Reason: " + rve.getMessage());
		}
		catch (UnmarshallerException ue)
		{
			fail("Unexpected failure when validating document. Reason: " + ue.getMessage());
		}
		catch (UnsupportedEncodingException uee)
		{
			fail("Unexpected failure when validating document. Reason: " + uee.getMessage());
		}
	}

	/*
	 * Tests for expected exception when keys passed to metadata function is null
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testUnmarshallMetadata2b() throws Exception
	{
		String filename = this.path + "SAMLMetadataSigned.xml";
		PublicKey pk = null;
		Unmarshaller<EntityDescriptor> unmarshaller;
		Unmarshaller<CacheClearService> unmarshaller2;

		String[] schemas = new String[] { "saml-schema-metadata-2.0.xsd", "cacheclear-schema-saml-metadata.xsd" };
		unmarshaller = new UnmarshallerImpl<EntityDescriptor>(EntityDescriptor.class.getPackage().getName(), schemas);
		unmarshaller2 = new UnmarshallerImpl<CacheClearService>(CacheClearService.class.getPackage().getName(), schemas);

		try
		{
			KeyStore ks = KeyStore.getInstance("PKCS12");
			FileInputStream fis = new FileInputStream(this.path + "tests.ks");
			char[] passwd = { 't', 'e', 's', 't', 'p', 'a', 's', 's' };
			ks.load(fis, passwd);

			Certificate cert = ks.getCertificate("myrsakey");
			pk = cert.getPublicKey();

			File file = new File(filename);
			long length = file.length();
			byte[] byteArray = new byte[(int) length];

			InputStream fileStream = new FileInputStream(file);
			fileStream.read(byteArray);
			fileStream.close();

			Map<String, KeyData> keys = new HashMap<String, KeyData>();

			EntityDescriptor entity = unmarshaller.unMarshallMetadata(pk, byteArray, null); //$NON-NLS-1$			
		}
		catch (SignatureValueException sve)
		{
			fail("Unexpected failure when validating document. Reason: " + sve.getMessage());
		}
		catch (ReferenceValueException rve)
		{
			fail("Unexpected failure when validating document. Reason: " + rve.getMessage());
		}
		catch (UnmarshallerException ue)
		{
			fail("Unexpected failure when validating document. Reason: " + ue.getMessage());
		}
		catch (UnsupportedEncodingException uee)
		{
			fail("Unexpected failure when validating document. Reason: " + uee.getMessage());
		}
	}

	/*
	 * Tests for expected exception when modified metadata document is presented
	 */
	@Test(expected = ReferenceValueException.class)
	public final void testUnmarshallMetadata3() throws Exception
	{
		String filename = this.path + "SAMLMetadataSigned-tampered.xml";
		PublicKey pk = null;
		Unmarshaller<EntityDescriptor> unmarshaller;

		String[] schemas = new String[] { "saml-schema-metadata-2.0.xsd", "cacheclear-schema-saml-metadata.xsd" };
		unmarshaller = new UnmarshallerImpl<EntityDescriptor>(EntityDescriptor.class.getPackage().getName(), schemas);

		try
		{
			KeyStore ks = KeyStore.getInstance("PKCS12");
			FileInputStream fis = new FileInputStream(this.path + "tests.ks");
			char[] passwd = { 't', 'e', 's', 't', 'p', 'a', 's', 's' };
			ks.load(fis, passwd);

			Certificate cert = ks.getCertificate("myrsakey");
			pk = cert.getPublicKey();

			File file = new File(filename);
			long length = file.length();
			byte[] byteArray = new byte[(int) length];

			InputStream fileStream = new FileInputStream(file);
			fileStream.read(byteArray);
			fileStream.close();

			Map<String, KeyData> keys = new HashMap<String, KeyData>();

			EntityDescriptor entity = unmarshaller.unMarshallMetadata(pk, byteArray, keys); //$NON-NLS-1$			
		}
		catch (SignatureValueException sve)
		{
			fail("Unexpected failure when validating document. Reason: " + sve.getMessage());
		}
		catch (UnsupportedEncodingException uee)
		{
			fail("Unexpected failure when validating document. Reason: " + uee.getMessage());
		}
	}

	/*
	 * Tests for expected exception when modified metadata document signature is presented
	 */
	@Test(expected = SignatureValueException.class)
	public final void testUnmarshallMetadata3a() throws Exception
	{
		String filename = this.path + "SAMLMetadataSigned-tamperedsig.xml";
		PublicKey pk = null;
		Unmarshaller<EntityDescriptor> unmarshaller;

		String[] schemas = new String[] { "saml-schema-metadata-2.0.xsd", "cacheclear-schema-saml-metadata.xsd" };
		unmarshaller = new UnmarshallerImpl<EntityDescriptor>(EntityDescriptor.class.getPackage().getName(), schemas);

		try
		{
			KeyStore ks = KeyStore.getInstance("PKCS12");
			FileInputStream fis = new FileInputStream(this.path + "tests.ks");
			char[] passwd = { 't', 'e', 's', 't', 'p', 'a', 's', 's' };
			ks.load(fis, passwd);

			Certificate cert = ks.getCertificate("myrsakey");
			pk = cert.getPublicKey();

			File file = new File(filename);
			long length = file.length();
			byte[] byteArray = new byte[(int) length];

			InputStream fileStream = new FileInputStream(file);
			fileStream.read(byteArray);
			fileStream.close();

			Map<String, KeyData> keys = new HashMap<String, KeyData>();

			EntityDescriptor entity = unmarshaller.unMarshallMetadata(pk, byteArray, keys); //$NON-NLS-1$			
		}
		catch (ReferenceValueException rve)
		{
			fail("Unexpected failure when validating document. Reason: " + rve.getMessage());
		}
		catch (UnsupportedEncodingException uee)
		{
			fail("Unexpected failure when validating document. Reason: " + uee.getMessage());
		}
	}

	/*
	 * Tests for expected exception when metadata which contains a key that has already been defined locally is
	 * presented
	 */
	@Test(expected = UnmarshallerException.class)
	public final void testUnmarshallMetadata3b() throws Exception
	{
		String filename = this.path + "SAMLMetadataSigned.xml";
		PublicKey pk = null;
		Unmarshaller<EntityDescriptor> unmarshaller;

		String[] schemas = new String[] { "saml-schema-metadata-2.0.xsd", "cacheclear-schema-saml-metadata.xsd" };
		unmarshaller = new UnmarshallerImpl<EntityDescriptor>(EntityDescriptor.class.getPackage().getName(), schemas);

		try
		{
			KeyStore ks = KeyStore.getInstance("PKCS12");
			FileInputStream fis = new FileInputStream(this.path + "tests.ks");
			char[] passwd = { 't', 'e', 's', 't', 'p', 'a', 's', 's' };
			ks.load(fis, passwd);

			Certificate cert = ks.getCertificate("myrsakey");
			pk = cert.getPublicKey();

			File file = new File(filename);
			long length = file.length();
			byte[] byteArray = new byte[(int) length];

			InputStream fileStream = new FileInputStream(file);
			fileStream.read(byteArray);
			fileStream.close();

			Map<String, KeyData> keys = createMock(Map.class);
			expect(keys.containsKey((String) notNull())).andReturn(true);
			replay(keys);

			EntityDescriptor entity = unmarshaller.unMarshallMetadata(pk, byteArray, keys); //$NON-NLS-1$
			verify(keys);
		}
		catch (SignatureValueException sve)
		{
			fail("Unexpected failure when validating document. Reason: " + sve.getMessage());
		}
		catch (ReferenceValueException rve)
		{
			fail("Unexpected failure when validating document. Reason: " + rve.getMessage());
		}
		catch (UnsupportedEncodingException uee)
		{
			fail("Unexpected failure when validating document. Reason: " + uee.getMessage());
		}
	}

	/*
	 * Tests for validity of unmarshallunsign method of Unmarshaller
	 */
	@Test
	public final void testUnmarshallUnSigned1() throws Exception
	{
		String filename = this.path + "AuthnRequest.xml";

		Unmarshaller<AuthnRequest> unmarshaller;
		String[] schemas = new String[] { "saml-schema-protocol-2.0.xsd", "saml-schema-assertion-2.0.xsd" };
		unmarshaller = new UnmarshallerImpl<AuthnRequest>(AuthnRequest.class.getPackage().getName(), schemas);

		try
		{
			File file = new File(filename);
			long length = file.length();
			byte[] byteArray = new byte[(int) length];

			InputStream fileStream = new FileInputStream(file);
			fileStream.read(byteArray);
			fileStream.close();

			AuthnRequest authn = unmarshaller.unMarshallUnSigned(byteArray); //$NON-NLS-1$

			if (authn == null)
			{
				fail("Failed to correctly unmarshall AuthnRequest");
				return;
			}

			List<ConditionAbstractType> conditions = authn.getConditions().getConditionsAndOneTimeUsesAndAudienceRestrictions();

			for (ConditionAbstractType c : conditions)
			{
				if (c instanceof AudienceRestriction)
				{
					AudienceRestriction aud = (AudienceRestriction) c;
					for (String a : aud.getAudiences())
						assertEquals("Ensure correct unmarshal for AudienceRestriction element", "some.spep.qut.edu.au", a);
				}
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Exception state should not be reached in this test block");
		}
	}

	/*
	 * Tests for validity of unmarshallunsign method of Unmarshaller
	 */
	@Test
	public final void testUnmarshallUnSigned1a() throws Exception
	{
		String filename = this.path + "PolicySet.xml";

		StringBuffer xml = new StringBuffer();

		Unmarshaller<PolicySet> unmarshaller;
		String[] schemas = new String[] { "saml-schema-protocol-2.0.xsd", "saml-schema-assertion-2.0.xsd", "lxacml-schema.xsd" };
		unmarshaller = new UnmarshallerImpl<PolicySet>(PolicySet.class.getPackage().getName(), schemas);

		try
		{
			File file = new File(filename);
			long length = file.length();
			byte[] byteArray = new byte[(int) length];

			InputStream fileStream = new FileInputStream(file);
			fileStream.read(byteArray);
			fileStream.close();

			PolicySet policySet = unmarshaller.unMarshallUnSigned(byteArray); //$NON-NLS-1$

			if (policySet == null)
			{
				fail("Failed to correctly unmarshall PolicySet");
				return;
			}

			assertEquals("Ensure correct unmarshal for PolicySet element", "Description Element", policySet.getDescription());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Exception state should not be reached in this test block");
		}
	}

	/*
	 * Tests for expected exception when null supplied as document
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testUnmarshallUnSigned2() throws Exception
	{
		String filename = this.path + "PolicySet.xml";

		StringBuffer xml = new StringBuffer();

		Unmarshaller<PolicySet> unmarshaller;
		String[] schemas = new String[] { "saml-schema-protocol-2.0.xsd", "saml-schema-assertion-2.0.xsd", "lxacml-schema.xsd" };
		unmarshaller = new UnmarshallerImpl<PolicySet>(PolicySet.class.getPackage().getName(), schemas);

		PolicySet policySet = unmarshaller.unMarshallUnSigned((byte[])null); //$NON-NLS-1$
	}

	/*
	 * Tests for expected exception when null supplied as node
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testUnmarshallUnSigned2a() throws Exception
	{
		String filename = this.path + "PolicySet.xml";

		StringBuffer xml = new StringBuffer();

		Unmarshaller<PolicySet> unmarshaller;
		String[] schemas = new String[] { "saml-schema-protocol-2.0.xsd", "saml-schema-assertion-2.0.xsd", "lxacml-schema.xsd" };
		unmarshaller = new UnmarshallerImpl<PolicySet>(PolicySet.class.getPackage().getName(), schemas);

		PolicySet policySet = unmarshaller.unMarshallUnSigned((Node) null); //$NON-NLS-1$
	}
}
