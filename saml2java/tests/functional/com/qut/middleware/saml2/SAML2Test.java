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
 * Purpose: Provides real world functionality tests for critical aspects of the integrated marshaller and unmarshaller package
 */

package com.qut.middleware.saml2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.SimpleTimeZone;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.TransformerConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.w3._2000._09.xmldsig_.Signature;
import org.xml.sax.SAXException;

import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.Unmarshaller;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.handler.impl.UnmarshallerImpl;
import com.qut.middleware.saml2.schemas.assertion.Assertion;
import com.qut.middleware.saml2.schemas.assertion.AudienceRestriction;
import com.qut.middleware.saml2.schemas.assertion.AuthnContext;
import com.qut.middleware.saml2.schemas.assertion.AuthnStatement;
import com.qut.middleware.saml2.schemas.assertion.ConditionAbstractType;
import com.qut.middleware.saml2.schemas.assertion.Conditions;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.assertion.Subject;
import com.qut.middleware.saml2.schemas.assertion.SubjectLocality;
import com.qut.middleware.saml2.schemas.protocol.AuthnRequest;
import com.qut.middleware.saml2.schemas.protocol.Response;
import com.qut.middleware.saml2.schemas.protocol.Status;
import com.qut.middleware.saml2.schemas.protocol.StatusCode;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

@SuppressWarnings(value = { "unqualified-field-access", "nls", "boxing" })
public class SAML2Test
{
	private String path;

	public SAML2Test() throws Exception
	{
		this.path = "tests" + File.separator + "testdata"
				+ File.separator;
	
		System.setProperty("file.encoding", "UTF-16");
		
		System.getProperties().list(System.out);
	}

	private PrivateKey getPrivateKey() throws Exception
	{
		PrivateKey privKey = null;

		KeyStore ks = KeyStore.getInstance("PKCS12");
		FileInputStream fis = new FileInputStream(this.path + "tests.ks");
		char[] passwd = { 't', 'e', 's', 't', 'p', 'a', 's', 's' };
		ks.load(fis, passwd);

		privKey = (PrivateKey) ks.getKey("myrsakey", passwd);

		return privKey;
	}

	private PublicKey getPublicKey() throws Exception
	{
		Certificate cert;
		PublicKey pubKey = null;

		KeyStore ks = KeyStore.getInstance("PKCS12");
		FileInputStream fis = new FileInputStream(this.path + "tests.ks");
		char[] passwd = { 't', 'e', 's', 't', 'p', 'a', 's', 's' };
		ks.load(fis, passwd);

		cert = ks.getCertificate("myrsakey");
		pubKey = cert.getPublicKey();

		return pubKey;
	}

	private AuthnRequest generateAuthnRequest()
	{
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
		XMLGregorianCalendar xmlCalander = new XMLGregorianCalendarImpl(calendar);

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
		authnRequest.setProviderName("spep-n1-itscandy-you-like-it");
		authnRequest.setID("abe567de6-122wert67");
		authnRequest.setVersion("2.0");
		authnRequest.setIssueInstant(xmlCalander);

		return authnRequest;
	}

	private XMLGregorianCalendar generateXMLCalendar(int offset)
	{
		SimpleTimeZone gmt;
		GregorianCalendar calendar;
		XMLGregorianCalendar xmlCalendar;

		/* GMT timezone TODO: I am sure this isn't correct need to ensure we set GMT */
		// GMT or UTC ??
		gmt = new SimpleTimeZone(0, "UTC");
		calendar = new GregorianCalendar(gmt);
		calendar.add(Calendar.MILLISECOND, offset);
		xmlCalendar = new XMLGregorianCalendarImpl(calendar);

		return xmlCalendar;
	}

	private Response generateResponse()
	{
		AuthnStatement authnStatement;
		SubjectLocality subjectLocality;
		AuthnContext authnContext;
		Subject subject;
		NameIDType issuer;
		NameIDType nameID;
		Signature signature;
		Assertion assertion;
		Assertion assertion2;
		Status status;
		StatusCode statusCode;
		Response response;

		/* Generate subject locality */
		subjectLocality = new SubjectLocality();
		subjectLocality.setDNSName("esoe.test.code");

		/*
		 * Generate AuthnContext, SAML spec requires previous session to be set if user not directly authenticated
		 * during this transaction TODO: This needs to be GMT
		 */
		authnContext = new AuthnContext();
		authnContext.setAuthnContextClassRef(AuthenticationContextConstants.previousSession);

		/* Generate successful authentication response for consumption by SPEP */
		authnStatement = new AuthnStatement();
		authnStatement.setAuthnInstant(generateXMLCalendar(0));
		authnStatement.setSessionIndex("1");
		authnStatement.setSessionNotOnOrAfter(generateXMLCalendar(200)); /*
																			 * Add our allowed time skew to the current
																			 * time
																			 */

		authnStatement.setSubjectLocality(subjectLocality);
		authnStatement.setAuthnContext(authnContext);

		/* Generate Issuer to attach to assertion and response */
		issuer = new NameIDType();
		issuer.setValue("esoetestcode");

		/* Generate placeholder <Signature/> block for SAML2lib-j in assertion and response */
		signature = new Signature();

		/* Generate subject to attach to assertion */
		subject = new Subject();
		nameID = new NameIDType();

		nameID.setValue("_12345");
		subject.setNameID(nameID);

		/* Generate the assertions */
		assertion = new Assertion();
		assertion.setVersion(VersionConstants.saml20);
		assertion.setID("_12345-ass");
		assertion.setIssueInstant(generateXMLCalendar(0));

		assertion.setIssuer(issuer);
		assertion.setSignature(signature);
		assertion.setSubject(subject);
		assertion.getAuthnStatementsAndAuthzDecisionStatementsAndAttributeStatements().add(authnStatement);

		assertion2 = new Assertion();
		assertion2.setVersion(VersionConstants.saml20);
		assertion2.setID("_12345-ass2");
		assertion2.setIssueInstant(generateXMLCalendar(0));

		assertion2.setIssuer(issuer);
		assertion2.setSignature(signature);
		assertion2.setSubject(subject);
		assertion2.getAuthnStatementsAndAuthzDecisionStatementsAndAttributeStatements().add(authnStatement);

		/* Generate successful status, only top level code supplied */
		statusCode = new StatusCode();
		statusCode.setValue(StatusCodeConstants.success);
		status = new Status();
		status.setStatusCode(statusCode);

		/* Generate our response */
		response = new Response();
		response.setID("_12345-res");
		response.setInResponseTo("_0987");
		response.setVersion(VersionConstants.saml20);
		response.setIssueInstant(generateXMLCalendar(0));
		response.setDestination("http://esoe.test.code");
		response.setConsent(ConsentIdentifierConstants.prior);

		response.setIssuer(issuer);
		response.setSignature(new Signature());
		response.setStatus(status);
		response.getEncryptedAssertionsAndAssertions().add(assertion);
		response.getEncryptedAssertionsAndAssertions().add(assertion2);

		return response;
	}

	/*
	 * Tests to ensure that a generated xml document can be signed, base64 encoded and base 64 decoded without
	 * corruption (mimicks SAML browser post sso profile requirements)
	 * 
	 * If this test is failing and your on windows it will be because of the hebrew - be sure to carefully read http://java.sun.com/j2se/1.5.0/docs/guide/intl/encoding.doc.html
	 * its most likely you have only the european version of the JVM installed
	 */
	@Test
	public void testSAML2lib1() throws Exception
	{
		Marshaller<AuthnRequest> marshaller;
		Unmarshaller<AuthnRequest> unmarshaller;
		String[] schemas = new String[] { "saml-schema-protocol-2.0.xsd", "saml-schema-assertion-2.0.xsd" };

		/* Supplied private/public key will be in RSA format */
		marshaller = new MarshallerImpl<AuthnRequest>(AuthnRequest.class.getPackage().getName(), schemas, "myrsakey", getPrivateKey());
		unmarshaller = new UnmarshallerImpl<AuthnRequest>(AuthnRequest.class.getPackage().getName(), schemas);

		try
		{
			AuthnRequest authnRequest = generateAuthnRequest();
			String schema = "saml-schema-protocol-2.0.xsd";

			byte[] doc = marshaller.marshallSigned(authnRequest);

			assertNotNull("Supplied XML document should not be null", doc);

			byte[] base64 = Base64.encodeBase64(doc);
			byte[] debase64 = Base64.decodeBase64(base64);
		
			AuthnRequest authnRequestDecoded = unmarshaller.unMarshallSigned(getPublicKey(), debase64); //$NON-NLS-1$
			
			assertEquals("Expected Signature ID not supplied", "abe567de6-122wert67", authnRequestDecoded.getID());
		}
		catch (TransformerConfigurationException tce)
		{
			fail("Unexcepted exception thrown");
		}
		catch (InvalidAlgorithmParameterException iape)
		{
			fail("Unexcepted exception thrown");
		}
		catch (NoSuchAlgorithmException nsae)
		{
			fail("Unexcepted exception thrown");
		}
		catch (ClassNotFoundException cfe)
		{
			fail("Unexcepted exception thrown");
		}
		catch (IllegalAccessException iae)
		{
			fail("Unexcepted exception thrown");
		}
		catch (InstantiationException cfe)
		{
			fail("Unexcepted exception thrown");
		}
		catch (SAXException saxe)
		{
			fail("Unexcepted exception thrown");
		}
	}

	/*
	 * Tests to ensure that a generated xml document can successfully have multiple embedded signatures, both as parents
	 * and siblings Ensure we don't get any exceptions when this is called
	 */
	@Test
	public void testSAML2lib2() throws Exception
	{
		Marshaller<Response> marshaller;
		Unmarshaller<Response> unmarshaller;

		String[] schemas = new String[] { "saml-schema-protocol-2.0.xsd", "saml-schema-assertion-2.0.xsd" };

		/* Supplied private/public key will be in RSA format */
		marshaller = new MarshallerImpl<Response>(AuthnRequest.class.getPackage().getName(), schemas, "myrsakey", getPrivateKey());
		unmarshaller = new UnmarshallerImpl<Response>(Response.class.getPackage().getName(), schemas);

		try
		{
			Response authnResponse = generateResponse();
			String schema = "saml-schema-protocol-2.0.xsd";

			byte[] doc = marshaller.marshallSigned(authnResponse);

			assertNotNull("Supplied XML document should not be null", doc);

			Response authnResponseDecoded = unmarshaller.unMarshallSigned(getPublicKey(), doc); //$NON-NLS-1$			
		}
		catch (TransformerConfigurationException tce)
		{
			fail("Unexcepted exception thrown");
		}
		catch (InvalidAlgorithmParameterException iape)
		{
			fail("Unexcepted exception thrown");
		}
		catch (NoSuchAlgorithmException nsae)
		{
			fail("Unexcepted exception thrown");
		}
		catch (ClassNotFoundException cfe)
		{
			fail("Unexcepted exception thrown");
		}
		catch (IllegalAccessException iae)
		{
			fail("Unexcepted exception thrown");
		}
		catch (InstantiationException cfe)
		{
			fail("Unexcepted exception thrown");
		}
		catch (SAXException saxe)
		{
			fail("Unexcepted exception thrown");
		}
	}
}
