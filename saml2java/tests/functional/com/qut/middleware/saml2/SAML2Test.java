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

			String doc = marshaller.marshallSigned(authnRequest);

			assertNotNull("Supplied XML document should not be null", doc);
			assertTrue("Supplied XML document MUST have a signature", doc.contains("ds:SignatureValue"));
			assertTrue("Supplied XML document MUST contain data from marshalled JAXB object", doc
					.contains("<saml:Audience>spep-n1.qut.edu.au</saml:Audience>"));

			byte[] base64 = Base64.encodeBase64(doc.getBytes("UTF-8"));
			String encodedBase64 = new String(base64);
			byte[] debase64 = Base64.decodeBase64(encodedBase64.getBytes());
			String decodedBase64 = new String(debase64);
			
			AuthnRequest authnRequestDecoded = unmarshaller.unMarshallSigned(getPublicKey(), decodedBase64); //$NON-NLS-1$
			
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
	 * Tests to ensure that a generated xml document can be signed, base64 encoded and base 64 decoded without
	 * corruption when it itself contains base64 data (mimicks SAML browser post sso profile requirements).
	 * 
	 * This test actually embeds a base64 encoded string version of its own AuthnRequest (precomputed) into its
	 * AudienceRestriction element to prove that the base64 encoding and transport of content by this library has no
	 * adverse effects.
	 * 
     * 
	 * If this test is failing and your on windows it will be because of the hebrew - be sure to carefully read http://java.sun.com/j2se/1.5.0/docs/guide/intl/encoding.doc.html
	 * its most likely you have only the european version of the JVM installed
	 */
	@Test
	public void testSAML2lib1a() throws Exception
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

			AudienceRestriction audienceRestriction = new AudienceRestriction();
			audienceRestriction
					.getAudiences()
					.add( "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTE2Ij8+PHNhbWxwOkF1dGhuUmVxdWVzdCB4bWxuczpzYW1scD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOnByb3RvY29sIiB4bWxuczpkcz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnIyIgeG1sbnM6ZXNvZT0iaHR0cDovL3d3dy5xdXQuY29tL21pZGRsZXdhcmUvRVNPRVByb3RvY29sU2NoZW1hIiB4bWxuczpseGFjbWw9Imh0dHA6Ly93d3cucXV0LmNvbS9taWRkbGV3YXJlL2x4YWNtbFNjaGVtYSIgeG1sbnM6bHhhY21sLWNvbnRleHQ9Imh0dHA6Ly93d3cucXV0LmNvbS9taWRkbGV3YXJlL2x4YWNtbENvbnRleHRTY2hlbWEiIHhtbG5zOmx4YWNtbGE9Imh0dHA6Ly93d3cucXV0LmNvbS9taWRkbGV3YXJlL2x4YWNtbFNBTUxBc3NlcnRpb25TY2hlbWEiIHhtbG5zOmx4YWNtbHA9Imh0dHA6Ly93d3cucXV0LmNvbS9taWRkbGV3YXJlL2x4YWNtbFNBTUxQcm90b2NvbFNjaGVtYSIgeG1sbnM6bWQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDptZXRhZGF0YSIgeG1sbnM6c2FtbD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiIgeG1sbnM6eGVuYz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxlbmMjIiBBc3NlcnRpb25Db25zdW1lclNlcnZpY2VVUkw9Imh0dHA6Ly9zcGVwLW4xLnF1dC5lZHUuYXUvc3NvL2FhIiBBdHRyaWJ1dGVDb25zdW1pbmdTZXJ2aWNlSW5kZXg9IjAiIEZvcmNlQXV0aG49ImZhbHNlIiBJRD0iYWJlNTY3ZGU2LTEyMndlcnQ2NyIgSXNzdWVJbnN0YW50PSIyMDA3LTAxLTAzVDA3OjI3OjQyLjczMloiIFByb3ZpZGVyTmFtZT0ic3BlcC1uMS1pdHNjYW5keS15b3UtbGlrZS1pdCIgVmVyc2lvbj0iMi4wIj48ZHM6U2lnbmF0dXJlPjxkczpTaWduZWRJbmZvPjxkczpDYW5vbmljYWxpemF0aW9uTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMjAwMS9SRUMteG1sLWMxNG4tMjAwMTAzMTUjV2l0aENvbW1lbnRzIi8+PGRzOlNpZ25hdHVyZU1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNyc2Etc2hhMSIvPjxkczpSZWZlcmVuY2UgVVJJPSIjYWJlNTY3ZGU2LTEyMndlcnQ2NyI+PGRzOlRyYW5zZm9ybXM+PGRzOlRyYW5zZm9ybSBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNlbnZlbG9wZWQtc2lnbmF0dXJlIi8+PC9kczpUcmFuc2Zvcm1zPjxkczpEaWdlc3RNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjc2hhMSIvPjxkczpEaWdlc3RWYWx1ZT5oYW1ONVFhYVMwZE81azAwbVl4MDdMUmNIU3c9PC9kczpEaWdlc3RWYWx1ZT48L2RzOlJlZmVyZW5jZT48L2RzOlNpZ25lZEluZm8+PGRzOlNpZ25hdHVyZVZhbHVlPmt4dzdNcE1RRGtXK3dnMG9UdGhYRVcyN0crVUhsQmZ5NytLc2hRN2lYVFp5M2dhcHByRTM3cjVOb2RBMnBjS2xjNEpIVHc2OTU5MkEKR3BMVTdkSTU2UGcvVldoZmVoWWhua1NyR2RBWlI5b2kyTUkyV0QraVJ1QTlmVTNQbExPZFA1S21HbURONmNNM3B5NUNDeVVYUmtTRgpxSklZRU84OUdlQ1dRaHU0NXNjPTwvZHM6U2lnbmF0dXJlVmFsdWU+PGRzOktleUluZm8+PGRzOktleU5hbWU+bXlyc2FrZXk8L2RzOktleU5hbWU+PC9kczpLZXlJbmZvPjwvZHM6U2lnbmF0dXJlPjxzYW1sOlN1YmplY3Q+PHNhbWw6TmFtZUlEIEZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOnNvbWV0aGluZyI+YmVkZG9lc0BxdXQuY29tPC9zYW1sOk5hbWVJRD48L3NhbWw6U3ViamVjdD48c2FtbDpDb25kaXRpb25zPjxzYW1sOkF1ZGllbmNlUmVzdHJpY3Rpb24+PHNhbWw6QXVkaWVuY2U+c3BlcC1uMS5xdXQuZWR1LmF1PC9zYW1sOkF1ZGllbmNlPjxzYW1sOkF1ZGllbmNlPnNwZXAtbjIucXV0LmVkdS5hdTwvc2FtbDpBdWRpZW5jZT48c2FtbDpBdWRpZW5jZT7Xkdeo16nXkNeZ16o8L3NhbWw6QXVkaWVuY2U+PC9zYW1sOkF1ZGllbmNlUmVzdHJpY3Rpb24+PC9zYW1sOkNvbmRpdGlvbnM+PC9zYW1scDpBdXRoblJlcXVlc3Q+" );
							
			/* We only want our audience base64 hack for this test */
			authnRequest.getConditions().getConditionsAndOneTimeUsesAndAudienceRestrictions().clear();
			authnRequest.getConditions().getConditionsAndOneTimeUsesAndAudienceRestrictions().add(audienceRestriction);

			String doc = marshaller.marshallSigned(authnRequest);

			assertNotNull("Supplied XML document should not be null", doc);
			assertTrue("Supplied XML document MUST have a signature", doc.contains("ds:SignatureValue"));

			byte[] base64 = Base64.encodeBase64(doc.getBytes("UTF-16"));
			byte[] debase64 = Base64.decodeBase64(base64);
			String decodedbase64 = new String(debase64, "UTF-16");

			AuthnRequest authnRequestDecoded = unmarshaller.unMarshallSigned(getPublicKey(), decodedbase64); //$NON-NLS-1$			
			assertEquals("Expected Signature ID not supplied", "abe567de6-122wert67", authnRequestDecoded.getID());

			List<ConditionAbstractType> conditions = authnRequestDecoded.getConditions()
					.getConditionsAndOneTimeUsesAndAudienceRestrictions();

			for (ConditionAbstractType c : conditions)
			{
				if (c instanceof AudienceRestriction)
				{
					AudienceRestriction aud = (AudienceRestriction) c;
					for (String a : aud.getAudiences())
					{
						byte[] debase642 = Base64.decodeBase64(a.getBytes("UTF-8"));
						String decodedbase642 = new String(debase642);

						AuthnRequest authnRequestDecoded2 = unmarshaller.unMarshallSigned(getPublicKey(), decodedbase642); //$NON-NLS-1$
						assertEquals("Expected Signature ID not supplied", "abe567de6-122wert67", authnRequestDecoded2
								.getID());
					}
				}
			}
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

			String doc = marshaller.marshallSigned(authnResponse);

			assertNotNull("Supplied XML document should not be null", doc);
			assertTrue("Supplied XML document MUST have a signature", doc.contains("ds:SignatureValue"));

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
