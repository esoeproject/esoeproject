package com.qut.middleware.esoe;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3._2000._09.xmldsig_.Signature;
import org.xml.sax.SAXException;

import static org.easymock.EasyMock.*;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpInternalErrorException;
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
import com.qut.middleware.esoe.sso.SSOProcessor;
import com.qut.middleware.esoe.sso.bean.SSOProcessorData;
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
import com.qut.middleware.saml2.schemas.assertion.AudienceRestriction;
import com.qut.middleware.saml2.schemas.assertion.AuthnStatement;
import com.qut.middleware.saml2.schemas.assertion.Conditions;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.protocol.AttributeQuery;
import com.qut.middleware.saml2.schemas.protocol.AuthnRequest;
import com.qut.middleware.saml2.schemas.protocol.NameIDPolicy;
import com.qut.middleware.saml2.schemas.protocol.Response;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

@SuppressWarnings({"unqualified-field-access", "nls"})

public class CompleteLogoutRequestTest {

	private SSOProcessorData data;
	private String sessionID = "esoeSession";
	private Marshaller<AuthnRequest> marshaller;
	private PrivateKey privKey;
	private PublicKey pubKey;
	private KeyStoreResolver keyStoreResolver;
	private String keyAlias = "477b96407f89ef84";
	private Metadata metadata;

	// TEST target URL to perform user logout
	private String logoutURL = "https://esoe-dev.qut.edu.au:8443/logout";
	
	// TEST target URL to perform user login
	private String signinURL = "https://esoe-dev.qut.edu.au:8443/signin";
	
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

		
	@Before
	public void setUp() throws MarshallerException, UnmarshallerException
	{
		data = createMock(SSOProcessorData.class);
		metadata = createMock(Metadata.class);
				
		// required setup for authentication emulation
		String keyStorePath = "secure/idpkeystore.ks";
		String keyStorePassword = "esoekspass";
		String esoeKeyPassword = "Es0EKs54P4SSPK";

		keyStoreResolver = new KeyStoreResolverImpl(new File(keyStorePath), keyStorePassword, keyAlias, esoeKeyPassword);
		privKey = keyStoreResolver.getPrivateKey();
		pubKey = keyStoreResolver.resolveKey(keyAlias);
		
		String[] schemas = new String[] { ConfigurationConstants.samlProtocol };
		this.marshaller = new MarshallerImpl<AuthnRequest>(AuthnRequest.class.getPackage().getName(), schemas, keyAlias, privKey);
	
	}

	@After
	public void tearDown()
	{
		// Not Implemented
	}

	private void setupMock()
	{
		replay(this.data);	
		replay(this.metadata);
	}

	private void tearDownMock()
	{
		/* Verify the mock responses */
		
	}

	/**
	 * Performs a complete logout request with valid parameters. As the follwing post has a redirect URL, we expect that
	 * the logout processor will return us to this page after logout. 
	 */
	@Test
	public void CompleteLogoutTest1()
	{
		
		try
		{			
			WebConversation webConversation;
			WebResponse resp;
			
			webConversation = new WebConversation();
			String cookieValue = null;
			cookieValue = this.getAuthenticatedCookieValue();			
			webConversation.addCookie(this.sessionID, cookieValue);
			
			// we want to be sent here after logout success
			String requestedLogoutResponseURL = "http://www.google.com.au";
		
			// we'll create our own POST form to emulate what a possible site may have
			// implemented in their logout form
			PostMethodWebRequest logoutPost = new PostMethodWebRequest(this.logoutURL);
			logoutPost.setParameter("esoelogout_response", requestedLogoutResponseURL);
			logoutPost.setParameter("esoelogout_nonsso", "");			
			// this emulates the user ticking the disable SSO check box
			logoutPost.setParameter("disablesso", "true");
			
			// send it
			resp = webConversation.getResponse(logoutPost);
		
			/*
			for(String name : webConversation.getCookieNames())
			{
				System.out.print(name  + "=" + webConversation.getCookieValue(name));
				System.out.println();
			}
			 */
			
			// ensure esoe session cookie killed
			assertEquals("ESOE Session cookie has not been invalidated.", "", webConversation.getCookieValue(this.sessionID));
			
			// ensure disable SSO cookie set to true as requested
			assertEquals("ESOE SSO disable cookie has not been set correctly.", "true", webConversation.getCookieValue("esoeNoAuto"));
			
			// check that response page was in fact the requested redirect URL. NOTE: this may fail if the 
			// requested URL has a redirect
			assertEquals("Logout response URL does not match requested URL" , requestedLogoutResponseURL, resp.getURL().toString());
			
			tearDownMock();

		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
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
	}
	
	
	/**
	 * Performs a complete logout request with valid parameters. As the following post DOES NOT have 
	 * a redirect URL, we expect that the logout processor will return us to a default page after logout. 
	 */
	@Test
	public void CompleteLogoutTest2()
	{
		try
		{
			WebConversation webConversation;
			WebResponse resp;
			
			webConversation = new WebConversation();
			String cookieValue = null;
			cookieValue = this.getAuthenticatedCookieValue();			
			webConversation.addCookie(this.sessionID, cookieValue);
			
			// Check the URL sent after logout success. NOTE this is configured on the ESOE and may
			// change. Check esoe.config parameter: logoutSuccessURL on target system for matching
			String defaultLogoutURL = "https://esoe-dev.qut.edu.au:8443/logout_success.jsp";
		
			// we'll create our own POST form to emulate what a possible site may have
			// implemented in their logout form
			PostMethodWebRequest logoutPost = new PostMethodWebRequest(this.logoutURL);
			logoutPost.setParameter("esoelogout_nonsso", "");			
			// this emulates the user ticking the disable SSO check box
			logoutPost.setParameter("disablesso", "true");
			
			// send it
			resp = webConversation.getResponse(logoutPost);
		
			/*
			for(String name : webConversation.getCookieNames())
			{
				System.out.print(name  + "=" + webConversation.getCookieValue(name));
				System.out.println();
			}
			 */
			
			// ensure esoe session cookie killed
			assertEquals("ESOE Session cookie has not been invalidated.", "", webConversation.getCookieValue(this.sessionID));
			
			// ensure disable SSO cookie set to true as requested
			assertEquals("ESOE SSO disable cookie has not been set correctly.", "true", webConversation.getCookieValue("esoeNoAuto"));
			
			// check that response page was in fact the requested redirect URL. NOTE: this may fail if the 
			// requested URL has a redirect
			assertEquals("Logout response URL does not match requested URL" , defaultLogoutURL, resp.getURL().toString());
			
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
		
	}

	
	/**
	 * Performs a complete logout request with invalid parameters. Ie: the POST is missing a required parameter. 
	 * Should result in a 501 error being returned.
	 */
	@Test
	public void CompleteLogoutTest3()
	{
		try
		{
			WebConversation webConversation;
			
			webConversation = new WebConversation();
			
			// instead of logging in so that the cookie is set with the session ID, we'll just use
			// an arbitrary value to force an error
			webConversation.addCookie(this.sessionID, "_67436dghs7f83207rt8");
			
			// we'll create our own POST form to emulate what a possible site may have
			// implemented in their logout form. We have ommitted the required parameter
			// esoelogout_nonsso
			PostMethodWebRequest logoutPost = new PostMethodWebRequest(this.logoutURL);
			logoutPost.setParameter("esoelogout_nonsso", "");			
			// this emulates the user ticking the disable SSO check box
			logoutPost.setParameter("disablesso", "true");
			
			// send it
			webConversation.getResponse(logoutPost);			
			
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
		catch(HttpInternalErrorException e)
		{
			// we want one of these
			assertEquals("Did not recieve expected Exception", 500, e.getResponseCode());
			return;
		}
		
		fail("Expected Internal server error did not occur.");
		
	}

	
	/**
	 * Performs a complete logout request with valid parameters, but an invalid session ID. Should result in a 501
	 * error being returned, as the ESOE knows nothing about the requested logout principal.
	 */
	@Test
	public void CompleteLogoutTest4()
	{
		try
		{
			WebConversation webConversation;
			
			webConversation = new WebConversation();
			String cookieValue = null;
			cookieValue = this.getAuthenticatedCookieValue();			
			webConversation.addCookie(this.sessionID, cookieValue);
			
			// we'll create our own POST form to emulate what a possible site may have
			// implemented in their logout form. We have ommitted the required parameter
			// esoelogout_nonsso
			PostMethodWebRequest logoutPost = new PostMethodWebRequest(this.logoutURL);
			// this emulates the user ticking the disable SSO check box
			logoutPost.setParameter("disablesso", "true");
			
			// send it
			webConversation.getResponse(logoutPost);			
			
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
		catch(HttpInternalErrorException e)
		{
			// we want one of these
			assertEquals("Did not recieve expected Exception", 500, e.getResponseCode());
			return;
		}
		
		fail("Expected Internal server error did not occur.");
		
	}

	/**
	 * Performs a complete end to end authentication as a user would expect to in real life, assumes the user has
	 * started directly on the login screen and does not already have a session established. It then returns the
	 * cookie value set by the ESOE (the user session ID) to use for logout testing.
	 */
	private String getAuthenticatedCookieValue()
	{
		String cookieValue = null;
		
		try
		{
			WebConversation wc;
			WebRequest req;
			WebResponse resp;
			WebForm form;
			SubmitButton submit;
			CookieListenerImpl cookieListenerImpl;
			
			expect(metadata.resolveKey(keyAlias)).andReturn(pubKey).anyTimes();
			setupMock();

			wc = new WebConversation();

			cookieListenerImpl = new CookieListenerImpl();
			CookieProperties.setDomainMatchingStrict(false);
			CookieProperties.addCookieListener(cookieListenerImpl);

			// generate a saml request and post it so sso servlet
			req = new GetMethodWebRequest(this.signinURL);

			resp = wc.getResponse(req);
			form = resp.getFormWithName("userpassAuthenticator");
			form.setParameter("esoeauthn_user", "beddoes");
			form.setParameter("esoeauthn_pw", "itscandyyoulikeit");

			submit = form.getSubmitButton("login", "login");
			resp = form.submit(submit);
			
			tearDownMock();
						
			// should now be a sessionID cookie set, return it
			cookieValue = wc.getCookieValue(this.sessionID);

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
		catch (KeyResolutionException e)
		{
			fail("Exception should not occur during this test. Cause: " + e.getMessage());
		}
		
		return cookieValue;
	}	
	
}
