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
 * Creation Date: 28/11/2006
 * 
 * Purpose: The tests defined in this file are quite specific to an actual running implementation of the ESOE
 * they require valid metadata, keystores, user accounts/attributes and other such real world data.
 * 
 * Running the tests in an automated environment apart from where they were originally designed to operate would probably
 * be difficult at best and would require the above to be setup and possibly the tests themselves changed. It is not intended that
 * this test suite would be run in continuous integration mode outside the core development environment.
 */
package com.qut.middleware.esoe;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3._2000._09.xmldsig_.Signature;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.SubmitButton;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.cookies.CookieListener;
import com.meterware.httpunit.cookies.CookieProperties;
import com.qut.middleware.esoe.crypto.KeyStoreResolver;
import com.qut.middleware.esoe.crypto.impl.KeyStoreResolverImpl;
import com.qut.middleware.esoe.metadata.Metadata;
import com.qut.middleware.saml2.exception.KeyResolutionException;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.exception.ReferenceValueException;
import com.qut.middleware.saml2.exception.SignatureValueException;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.Unmarshaller;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.handler.impl.UnmarshallerImpl;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.identifier.impl.IdentifierCacheImpl;
import com.qut.middleware.saml2.identifier.impl.IdentifierGeneratorImpl;
import com.qut.middleware.saml2.schemas.assertion.Assertion;
import com.qut.middleware.saml2.schemas.assertion.AttributeType;
import com.qut.middleware.saml2.schemas.assertion.AudienceRestriction;
import com.qut.middleware.saml2.schemas.assertion.AuthnStatement;
import com.qut.middleware.saml2.schemas.assertion.Conditions;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.assertion.Subject;
import com.qut.middleware.saml2.schemas.protocol.AttributeQuery;
import com.qut.middleware.saml2.schemas.protocol.AuthnRequest;
import com.qut.middleware.saml2.schemas.protocol.NameIDPolicy;
import com.qut.middleware.saml2.schemas.protocol.Response;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

@SuppressWarnings(value = { "unqualified-field-access", "nls" })
public class CompleteAuthenticationTest
{
	/**
	 * Private class used with HTTPUnit to respond to failed cookie set events
	 */
	private class CookieListenerImpl implements CookieListener
	{
		public CookieListenerImpl()
		{
			// Not Implemented
		}

		public void cookieRejected(String arg0, int arg1, String arg2)
		{
			fail("Cookies set by the ESOE system should not be rejected failed trying to set " + arg0
					+ " with reason: " + arg1 + " on domain: " + arg2);
		}
	}

	private Marshaller<AuthnRequest> marshaller;
	private Marshaller<AttributeQuery> marshallerAttrib;
	private Unmarshaller<Response> unmarshaller;


	private PrivateKey privKey;
	private PublicKey pk;
	private KeyStoreResolver keyStoreResolver;

	private IdentifierGenerator identifierGenerator;
	private Metadata metadata;

	private String[] schemas;
	
	// alias of ESOE private key in given keystore
	private String keyAlias = "477b96407f89ef84";

	// TEST target URL to perform user login
	private String signinURL = "https://esoe-dev.qut.edu.au:8443/signin";
	
	//	actual URL of page for the login form
	private String loginPage = "https://esoe-dev.qut.edu.au:8443/login_qut.jsp";
	
	// TEST target URL to perform SAML SSO posts for faking an SPEP
	private String ssoURL = "https://esoe-dev.qut.edu.au:8443/sso";
	
	// the value of issuerID for SAML Requests. Must be present in metadata being used by the target
	// ESOE when submitting sso requests. Ie: it is the identifier of the SPEP being faked by these tests.
	// it is the value of <md:SPSSODescriptor ID="?"> in metadata.
	private String spepID = "_77496fd45defd48910e929f3751bcaeebf9f6c56-2fa8553610f2d3a0180e121d5118fccd";
	
	// URL of the attribute authority web service on the TEST ESOE
	private String attributeService = "http://esoe-dev.qut.edu.au:8080/ws/services/esoe/attributeAuthority";
	
	
	@Before
	public void setUp()
	{
		try
		{
			identifierGenerator = new IdentifierGeneratorImpl(new IdentifierCacheImpl());

			metadata = createMock(Metadata.class);

			/* PRE-REQ: the keystore used here must contain the private key matching the
			 * public key that exists in metadata for the SPEP ID used by this test case.
			 * Ie: we will sign SAML Requests with this key and the ESOE must have the matching
			 * public key to decrypt it.
			 */
			String keyStorePath = "secure/idpkeystore.ks";
			String keyStorePassword = "esoekspass";
			String esoeKeyPassword = "Es0EKs54P4SSPK";

			keyStoreResolver = new KeyStoreResolverImpl(new File(keyStorePath), keyStorePassword, keyAlias, esoeKeyPassword);
			privKey = keyStoreResolver.getPrivateKey();
			pk = keyStoreResolver.resolveKey(keyAlias);

			schemas = new String[] { ConfigurationConstants.samlProtocol };

			/* Supplied private/public key will be in RSA format */
			this.marshaller = new MarshallerImpl<AuthnRequest>(AuthnRequest.class.getPackage().getName(), schemas, keyAlias, privKey);
			this.marshallerAttrib = new MarshallerImpl<AttributeQuery>(AttributeQuery.class.getPackage().getName(), schemas, keyAlias, privKey);
			this.unmarshaller = new UnmarshallerImpl<Response>(Response.class.getPackage().getName(), schemas, metadata);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Unexpected exception on creating marshaller: " + e.getCause());
		}
	}

	@After
	public void tearDown()
	{
		// Not Implemented
	}

	private void setupMock()
	{
		/* Start the replay for all our configured mock objects */
		replay(this.metadata);
	}

	private void tearDownMock()
	{
		/* Verify the mock responses */
		verify(this.metadata);
	}

	/**
	 * Performs a complete end to end authentication as a user would expect to in real life, assumes the user has
	 * started directly on the login screen and does not already have a session established, creates a session before
	 * faking SPEP behaviour to establish a session context, also will not allow a session to be established if a valid one does not already exist
	 */
	@Test
	public void CompleteAuthenticationTest1()
	{
		try
		{
			WebConversation wc;
			WebRequest req;
			WebResponse resp;
			WebForm form, formResp;
			SubmitButton submit;
			CookieListenerImpl cookieListenerImpl;
			String requestID, base64Resp, samlResponse;
			byte[] samlResponseBytes;

			Response response;
			Assertion assertion;
			AuthnStatement authnStatement;

			expect(metadata.resolveKey(keyAlias)).andReturn(pk).atLeastOnce();

			setupMock();

			wc = new WebConversation();

			cookieListenerImpl = new CookieListenerImpl();
			CookieProperties.setDomainMatchingStrict(false);
			CookieProperties.addCookieListener(cookieListenerImpl);

			req = new GetMethodWebRequest(this.signinURL);

			// we'll use the login form provided by the target esoe, as there should always 
			// be a form present (unlike sso servlet for example)
			resp = wc.getResponse(req);
			form = resp.getFormWithName("userpassAuthenticator");
			form.setParameter("esoeauthn_user", "beddoes");
			form.setParameter("esoeauthn_pw", "itscandyyoulikeit");

			submit = form.getSubmitButton("login", "login");
			
			// send the login POST request
			resp = form.submit(submit);

			// now establish a session on our fake SPEP
			
			requestID = identifierGenerator.generateSAMLAuthnID();

			// we'll create our own POST for the SAML SSO servlet. This is what an SPEP will
			// generate when an unauthenticated user hits an SPEP protected resource.
			PostMethodWebRequest ssoPost = new PostMethodWebRequest(this.ssoURL);
			ssoPost.setParameter("SAMLRequest", generateValidRequest(requestID, false));
			
			// send the sso POST
			resp = wc.getResponse(ssoPost);
						
			// POST will return a form with the generated SAML Response embedded 
			formResp = resp.getFormWithName("samlResponse");

			// retrieve the SAML Response and check
			base64Resp = formResp.getParameterValue("SAMLResponse");
			samlResponseBytes = Base64.decodeBase64(base64Resp.getBytes("UTF-8"));
			samlResponse = new String(samlResponseBytes);

			response = unmarshaller.unMarshallSigned(samlResponse);

			assertEquals("Ensures InResponseTo is the same as the ID of the original request", response
					.getInResponseTo(), requestID);

			assertion = (Assertion) response.getEncryptedAssertionsAndAssertions().get(0);
			authnStatement = (AuthnStatement) assertion
					.getAuthnStatementsAndAuthzDecisionStatementsAndAttributeStatements().get(0);
			assertEquals("Ensures the correct transport type is set",
					"urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport", authnStatement
							.getAuthnContext().getAuthnContextClassRef());

			tearDownMock();

		}
		catch (MalformedURLException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
		catch (IOException e)
		{
			e.printStackTrace();
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
		catch (SAXException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
		catch (SignatureValueException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
		catch (ReferenceValueException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
		catch (UnmarshallerException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
		catch (KeyResolutionException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
	}

	/**
	 * Performs a complete end to end authentication as a user would expect to in real life, assumes the user has
	 * started directly on the login screen and does not already have a session established, creates a session before
	 * faking as SPEP, does allow a session to be established if a valid one does not already exist
	 */
	@Test
	public void CompleteAuthenticationTest1a()
	{
		try
		{
			WebConversation wc;
			WebRequest req;
			WebResponse resp;
			WebForm form, formResp;
			SubmitButton submit;
			CookieListenerImpl cookieListenerImpl;
			String requestID, base64Resp, samlResponse;
			byte[] samlResponseBytes;

			Response response;
			Assertion assertion;
			AuthnStatement authnStatement;

			expect(metadata.resolveKey(keyAlias)).andReturn(pk).atLeastOnce();

			setupMock();

			wc = new WebConversation();

			cookieListenerImpl = new CookieListenerImpl();
			CookieProperties.setDomainMatchingStrict(false);
			CookieProperties.addCookieListener(cookieListenerImpl);

			req = new GetMethodWebRequest(this.signinURL);

			// we'll use the login form provided by the target esoe, as there should always 
			// be a form present (unlike sso servlet for example)
			resp = wc.getResponse(req);
			form = resp.getFormWithName("userpassAuthenticator");
			form.setParameter("esoeauthn_user", "beddoes");
			form.setParameter("esoeauthn_pw", "itscandyyoulikeit");

			submit = form.getSubmitButton("login", "login");
			
			// send the login POST request
			resp = form.submit(submit);

			requestID = identifierGenerator.generateSAMLAuthnID();

			// we'll create our own POST for the SAML SSO servlet. This is what an SPEP will
			// generate when an unauthenticated user hits an SPEP protected resource.
			PostMethodWebRequest ssoPost = new PostMethodWebRequest(this.ssoURL);
			ssoPost.setParameter("SAMLRequest", generateValidRequest(requestID, false));
			
			// send the sso POST
			resp = wc.getResponse(ssoPost);
						
			// POST will return a form with the generated SAML Response embedded 
			formResp = resp.getFormWithName("samlResponse");
			
			base64Resp = formResp.getParameterValue("SAMLResponse");
			samlResponseBytes = Base64.decodeBase64(base64Resp.getBytes("UTF-8"));
			samlResponse = new String(samlResponseBytes);

			response = unmarshaller.unMarshallSigned(samlResponse);

			assertEquals("Ensures InResponseTo is the same as the ID of the original request", response
					.getInResponseTo(), requestID);

			assertion = (Assertion) response.getEncryptedAssertionsAndAssertions().get(0);
			authnStatement = (AuthnStatement) assertion
					.getAuthnStatementsAndAuthzDecisionStatementsAndAttributeStatements().get(0);
			assertEquals("Ensure the correct transport type is set",
					"urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport", authnStatement
							.getAuthnContext().getAuthnContextClassRef());

			tearDownMock();

		}
		catch (MalformedURLException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
		catch (IOException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
		catch (SAXException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
		catch (SignatureValueException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
		catch (ReferenceValueException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
		catch (UnmarshallerException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
		catch (KeyResolutionException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
	}
	
	/**
	 * Performs a complete end to end authentication as a user would expect to in real life, assumes the user has
	 * started directly on the login screen and does not already have a session established, creates a session before
	 * faking as SPEP, does allow a session to be established if a valid one does not already exist, then removes SPEP cookie
	 * and attempts to reuse ESOE cookie again to establish another session, imitating movement between services.
	 */
	@Test
	public void CompleteAuthenticationTest1b()
	{
		try
		{
			WebConversation wc;
			WebRequest req;
			WebResponse resp;
			WebForm form, formResp;
			SubmitButton submit;
			CookieListenerImpl cookieListenerImpl;
			String requestID, base64Resp, samlResponse;
			byte[] samlResponseBytes;

			Response response;
			Assertion assertion;
			AuthnStatement authnStatement;

			expect(metadata.resolveKey(keyAlias)).andReturn(pk).atLeastOnce();

			setupMock();

			wc = new WebConversation();

			cookieListenerImpl = new CookieListenerImpl();
			CookieProperties.setDomainMatchingStrict(false);
			CookieProperties.addCookieListener(cookieListenerImpl);

			req = new GetMethodWebRequest(this.signinURL);

			// we'll use the login form provided by the target esoe, as there should always 
			// be a form present (unlike sso servlet for example)
			resp = wc.getResponse(req);
			form = resp.getFormWithName("userpassAuthenticator");
			form.setParameter("esoeauthn_user", "beddoes");
			form.setParameter("esoeauthn_pw", "itscandyyoulikeit");

			submit = form.getSubmitButton("login", "login");
			
			// send the login POST request
			resp = form.submit(submit);

			requestID = identifierGenerator.generateSAMLAuthnID();

			// we'll create our own POST for the SAML SSO servlet. This is what an SPEP will
			// generate when an unauthenticated user hits an SPEP protected resource.
			PostMethodWebRequest ssoPost = new PostMethodWebRequest(this.ssoURL);
			ssoPost.setParameter("SAMLRequest", generateValidRequest(requestID, false));
			
			// send the sso POST
			resp = wc.getResponse(ssoPost);
						
			// POST will return a form with the generated SAML Response embedded 
			formResp = resp.getFormWithName("samlResponse");

			base64Resp = formResp.getParameterValue("SAMLResponse");
			samlResponseBytes = Base64.decodeBase64(base64Resp.getBytes("UTF-8"));
			samlResponse = new String(samlResponseBytes);

			response = unmarshaller.unMarshallSigned(samlResponse);

			assertEquals("Ensures InResponseTo is the same as the ID of the original request", response
					.getInResponseTo(), requestID);

			assertion = (Assertion) response.getEncryptedAssertionsAndAssertions().get(0);
			authnStatement = (AuthnStatement) assertion
					.getAuthnStatementsAndAuthzDecisionStatementsAndAttributeStatements().get(0);
			assertEquals("Ensure the correct transport type is set",
					"urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport", authnStatement
							.getAuthnContext().getAuthnContextClassRef());
			
			/* Establish second session using a new identifier */
			requestID = identifierGenerator.generateSAMLAuthnID();
			
			PostMethodWebRequest ssoPost2 = new PostMethodWebRequest(this.ssoURL);
			ssoPost2.setParameter("SAMLRequest", generateValidRequest(requestID, false));
			
			// send the sso POST
			resp = wc.getResponse(ssoPost2);
						
			formResp = resp.getFormWithName("samlResponse");

			base64Resp = formResp.getParameterValue("SAMLResponse");
			samlResponseBytes = Base64.decodeBase64(base64Resp.getBytes("UTF-8"));
			samlResponse = new String(samlResponseBytes);

			response = unmarshaller.unMarshallSigned(samlResponse);

			assertEquals("Ensures InResponseTo is the same as the ID of the original request", response
					.getInResponseTo(), requestID);

			assertion = (Assertion) response.getEncryptedAssertionsAndAssertions().get(0);
			authnStatement = (AuthnStatement) assertion
					.getAuthnStatementsAndAuthzDecisionStatementsAndAttributeStatements().get(0);
			assertEquals("Ensure the correct transport type is set",
					"urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport", authnStatement
							.getAuthnContext().getAuthnContextClassRef());

			tearDownMock();

		}
		catch (MalformedURLException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
		catch (IOException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
		catch (SAXException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
		catch (SignatureValueException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
		catch (ReferenceValueException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
		catch (UnmarshallerException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
		catch (KeyResolutionException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
	}

	/**
	 * Performs a complete end to end authentication as a user would expect to in real life, assumes the user has
	 * started at an SPEP and does not already have a session established, expects to be redirected to establish one via
	 * usernamePasswordHandler, does allow a session to be established if a valid one does not already exist
	 */
	@Test
	public void CompleteAuthenticationTest2()
	{
		try
		{
			WebConversation wc;
			WebRequest req;
			WebResponse resp;
			WebForm form, formResp;
			SubmitButton submit;
			CookieListenerImpl cookieListenerImpl;
			String requestID, base64Resp, samlResponse;
			byte[] samlResponseBytes;

			Response response;
			Assertion assertion;
			AuthnStatement authnStatement;
			String attributes;

			expect(metadata.resolveKey(keyAlias)).andReturn(pk).atLeastOnce();

			setupMock();

			wc = new WebConversation();

			cookieListenerImpl = new CookieListenerImpl();
			CookieProperties.setDomainMatchingStrict(false);
			CookieProperties.addCookieListener(cookieListenerImpl);

			requestID = identifierGenerator.generateSAMLAuthnID();

			req = new GetMethodWebRequest(this.signinURL);

			// we'll use the login form provided by the target esoe, as there should always 
			// be a form present (unlike sso servlet for example)
			resp = wc.getResponse(req);
			form = resp.getFormWithName("userpassAuthenticator");
			form.setParameter("esoeauthn_user", "beddoes");
			form.setParameter("esoeauthn_pw", "itscandyyoulikeit");

			submit = form.getSubmitButton("login", "login");
			
			// send the login POST request
			resp = form.submit(submit);

			requestID = identifierGenerator.generateSAMLAuthnID();

			// we'll create our own POST for the SAML SSO servlet. This is what an SPEP will
			// generate when an unauthenticated user hits an SPEP protected resource.
			PostMethodWebRequest ssoPost = new PostMethodWebRequest(this.ssoURL);
			ssoPost.setParameter("SAMLRequest", generateValidRequest(requestID, false));
			
			// send the sso POST
			resp = wc.getResponse(ssoPost);
						
			// POST will return a form with the generated SAML Response embedded 
			formResp = resp.getFormWithName("samlResponse");

			base64Resp = formResp.getParameterValue("SAMLResponse");
			samlResponseBytes = Base64.decodeBase64(base64Resp.getBytes("UTF-8"));
			samlResponse = new String(samlResponseBytes);

			response = unmarshaller.unMarshallSigned(samlResponse);

			assertEquals("Ensures InResponseTo is the same as the ID of the original request", response
					.getInResponseTo(), requestID);

			assertion = (Assertion) response.getEncryptedAssertionsAndAssertions().get(0);
			authnStatement = (AuthnStatement) assertion
					.getAuthnStatementsAndAuthzDecisionStatementsAndAttributeStatements().get(0);
			assertEquals("Ensure the correct transport type is set",
					"urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport", authnStatement
							.getAuthnContext().getAuthnContextClassRef());
			
			attributes = getAttributes(assertion.getSubject().getNameID().getValue());
			
			response = unmarshaller.unMarshallSigned(attributes);
			
			assertTrue("Ensure that response XML document in string format contains the username beddoes", attributes.contains("beddoes"));
			
			tearDownMock();

		}
		catch (MalformedURLException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
		catch (IOException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
		catch (SAXException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
		catch (SignatureValueException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
		catch (ReferenceValueException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
		catch (UnmarshallerException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
		catch (KeyResolutionException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
	}

	/**
	 * Performs a complete end to end authentication as a user would expect to in real life, assumes the user has
	 * started at an SPEP and does not already have a session established, expects to be redirected to establish one via
	 * usernamePasswordHandler, does allow a session to be established if a valid one does not already exist.
	 * 
	 * Additionally this test deliberately faults its first authentication attempt and expects to be sent back to the authentication page with appropriate values.
	 */
	@Test
	public void CompleteAuthenticationTest2a()
	{
		try
		{
			WebConversation wc;
			WebRequest req;
			WebResponse resp;
			WebForm form, formResp, samlForm;
			SubmitButton submit;
			String requestID, base64Resp, samlResponse;
			byte[] samlResponseBytes;

			Response response;
			Assertion assertion;
			AuthnStatement authnStatement;

			expect(metadata.resolveKey(keyAlias)).andReturn(pk).atLeastOnce();

			setupMock();

			wc = new WebConversation();

			CookieListenerImpl cookieListenerImpl = new CookieListenerImpl();
			CookieProperties.setDomainMatchingStrict(false);
			CookieProperties.addCookieListener(cookieListenerImpl);

			requestID = identifierGenerator.generateSAMLAuthnID();
			String samlRequest = generateValidRequest(requestID, true);
			
			// emulate the user hitting an SPEP and being forced to sso portal. We do this by sending
			// a SAML Request to the SSO servlet, like one that would be generated by the SPEP.
			PostMethodWebRequest ssoPost = new PostMethodWebRequest(this.ssoURL);
			ssoPost.setParameter("SAMLRequest", samlRequest);
			
			resp = wc.getResponse(ssoPost);
				
			// the ESOE has no principal session details so it will send a redirect to auth page
			assertEquals("Response was not is redirected back to the username/password login screen", this.loginPage, resp.getURL().toString() );

			// Deliberately fail first auth attempt 
			form = resp.getFormWithName("userpassAuthenticator");
			form.setParameter("esoeauthn_user", "beddoes");
			form.setParameter("esoeauthn_pw", "itscandyyoudontlikeit");

			submit = form.getSubmitButton("login", "login");
			resp = form.submit(submit);			

			System.out.println("Stat = " + resp.getResponseCode() + resp.getText());
			
			assertEquals("Ensure that the response is redirected back to the username/password login screen with appropriate failed namevalue pair",
					this.loginPage +"?rc=authnfail", resp.getURL().toString());
			
			
			/* Silly user forgetting passwords, ok now auth correctly */
			form = resp.getFormWithName("userpassAuthenticator");
			form.setParameter("esoeauthn_user", "beddoes");
			form.setParameter("esoeauthn_pw", "itscandyyoulikeit");
			submit = form.getSubmitButton("login", "login");			
			resp = form.submit(submit);
		
			// after we login the esoe will redirect us back to the SSO servlet. Because the ESOE
			// now knows about us, it will respond to the original SAMLRequest with a response
			samlForm = resp.getFormWithName("samlResponse");
			
			base64Resp = samlForm.getParameterValue("SAMLResponse");
			samlResponseBytes = Base64.decodeBase64(base64Resp.getBytes("UTF-8"));
			samlResponse = new String(samlResponseBytes);

			response = unmarshaller.unMarshallSigned(samlResponse);

			assertEquals("Ensures InResponseTo is the same as the ID of the original request", response
					.getInResponseTo(), requestID);

			assertion = (Assertion) response.getEncryptedAssertionsAndAssertions().get(0);
			authnStatement = (AuthnStatement) assertion
					.getAuthnStatementsAndAuthzDecisionStatementsAndAttributeStatements().get(0);
			assertEquals("Ensure the correct transport type is set",
					"urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport", authnStatement
							.getAuthnContext().getAuthnContextClassRef());

			tearDownMock();

		}
		catch (MalformedURLException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
		catch (IOException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
		catch (SAXException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
		catch (SignatureValueException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
		catch (ReferenceValueException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
		catch (UnmarshallerException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
		catch (KeyResolutionException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
	}
	
	/** Makes a call to the attribute service on the ESOE to retrieve attributes stored
	 * in user session. 
	 * 
	 * @param principal The principal to obtain attributes for.
	 * @return Attributes returned by the target esoe for the given principal.
	 */
	private String getAttributes(String principal)
	{
		EndpointReference targetEPR = new EndpointReference(this.attributeService);
		
		try
		{
			/* Create ourselves as a WS client and communicate with the ESOE WS interface for Attributes */
			StringReader reader = new StringReader(createAttributeRequest(principal));

			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader xmlreader = xif.createXMLStreamReader(reader);

			StAXOMBuilder builder = new StAXOMBuilder(xmlreader);
			OMElement request = builder.getDocumentElement();

			ServiceClient serviceClient = new ServiceClient();
			Options options = new Options();
			serviceClient.setOptions(options);
			options.setTo(targetEPR);

			OMElement result = serviceClient.sendReceive(request);

			StringWriter writer = new StringWriter();
			result.serialize(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));
			writer.flush();

			return writer.toString();
		}
		catch (Exception ex)
		{
			return null;
		}
	}

	/**
	 * Creates a valid SAML AuthnRequest like would be supplied by an SPEP
	 * 
	 * @return String containing SAML AuthnRequest
	 */
	private String generateValidRequest(String requestID, boolean allowSessionCreation)
	{
		try
		{
			AudienceRestriction audienceRestriction = new AudienceRestriction();
			Conditions conditions = new Conditions();
			NameIDType issuer = new NameIDType();
			NameIDPolicy policy = new NameIDPolicy();
			Signature signature = new Signature();
			AuthnRequest authnRequest = new AuthnRequest();
			String result;

			/* GMT timezone */
			SimpleTimeZone gmt = new SimpleTimeZone(0, ConfigurationConstants.timeZone);

			/* GregorianCalendar with the GMT time zone */
			GregorianCalendar calendar = new GregorianCalendar(gmt);
			XMLGregorianCalendar xmlCalendar = new XMLGregorianCalendarImpl(calendar);

			audienceRestriction.getAudiences().add("spep-n1.qut.edu.au");
			audienceRestriction.getAudiences().add("spep-n2.qut.edu.au");
			conditions.getConditionsAndOneTimeUsesAndAudienceRestrictions().add(audienceRestriction);

			issuer.setValue(this.spepID);

			policy.setAllowCreate(allowSessionCreation);
			authnRequest.setNameIDPolicy(policy);

			authnRequest.setSignature(signature);
			authnRequest.setConditions(conditions);

			authnRequest.setForceAuthn(false);
			authnRequest.setIsPassive(false);
			authnRequest.setAssertionConsumerServiceIndex(0);
			authnRequest.setProviderName("spep-n1");
			authnRequest.setID(requestID);
			authnRequest.setVersion("2.0");
			authnRequest.setIssueInstant(xmlCalendar);
			authnRequest.setIssuer(issuer);

			result = marshaller.marshallSigned(authnRequest);

			String resultEncoded = new String(Base64.encodeBase64(result.getBytes("UTF-8")));

			return resultEncoded;
		}
		catch (Exception e)
		{
			fail("Unexpected exception state thrown when creating authnRequest");
			return null;
		}
	}
	
	private String createAttributeRequest(String principal)
	{
		String destination = "https://esoe.url/test"; // AttributeQuery attribute
		String consent = "https://esoe.url/test"; // AttributeQuery attribute
		
		AttributeQuery attributeQuery = new AttributeQuery();
		attributeQuery.setID(this.identifierGenerator.generateSAMLID());
		attributeQuery.setIssueInstant(new XMLGregorianCalendarImpl(new GregorianCalendar()));
		attributeQuery.setDestination(destination);
		attributeQuery.setConsent(consent);
		attributeQuery.setVersion("2.0");

		Subject subject = new Subject();
		NameIDType subjectNameID = new NameIDType();
		subjectNameID.setValue(principal);

		subject.setNameID(subjectNameID);
		attributeQuery.setSubject(subject);

		NameIDType issuerNameID = new NameIDType();
		issuerNameID.setValue(this.spepID);
		attributeQuery.setIssuer(issuerNameID);

		AttributeType attributeUsername = new AttributeType();
		attributeUsername.setName("uid");
		AttributeType attributeMail = new AttributeType();
		attributeMail.setName("mail");
		AttributeType attributeSurname = new AttributeType();
		attributeSurname.setName("sn");
		AttributeType attributeID = new AttributeType();
		attributeID.setName("clientID");
		attributeQuery.getAttributes().add(attributeUsername);
		attributeQuery.getAttributes().add(attributeMail);
		attributeQuery.getAttributes().add(attributeSurname);
		attributeQuery.getAttributes().add(attributeID);
				
		Signature signature = new Signature();
		attributeQuery.setSignature(signature);

		String request = null;
		try
		{
			request = this.marshallerAttrib.marshallSigned(attributeQuery);
		}
		catch (MarshallerException e)
		{
			fail("Marshaller error: " + e.getMessage() + "\n" + e.getCause().getMessage());
		}
		
		return request;
	}
}
