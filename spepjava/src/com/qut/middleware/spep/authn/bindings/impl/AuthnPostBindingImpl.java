/* Copyright 2008, Queensland University of Technology
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
 * Creation Date: 12/12/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.spep.authn.bindings.impl;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.metadata.bean.saml.IdentityProviderRole;
import com.qut.middleware.metadata.bean.saml.TrustedESOERole;
import com.qut.middleware.metadata.exception.MetadataStateException;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.saml2.BindingConstants;
import com.qut.middleware.saml2.schemas.protocol.AuthnRequest;
import com.qut.middleware.saml2.schemas.protocol.Response;
import com.qut.middleware.spep.SPEP;
import com.qut.middleware.spep.authn.AuthnProcessorData;
import com.qut.middleware.spep.authn.bindings.AuthnBinding;
import com.qut.middleware.spep.exception.AuthenticationException;

public class AuthnPostBindingImpl implements AuthnBinding
{
	private static final String HTTP_GET = "GET";
	private static final String HTTP_POST= "POST";

	private MessageFormat samlMessageFormat;

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public AuthnPostBindingImpl()
	{
		try
		{
			InputStream inputStream = this.getClass().getResourceAsStream("samlPostRequestTemplate.html");
			DataInputStream dataInputStream = new DataInputStream(inputStream);
			
			byte[] document = new byte[dataInputStream.available()];
			dataInputStream.readFully(document);
			
			// Load the document as UTF-8 format and create a formatter for it.
			String samlResponseTemplate = new String(document, "UTF-8");
			this.samlMessageFormat = new MessageFormat(samlResponseTemplate);
			
			this.logger.info("Created AuthnPostBindingImpl successfully");
		}
		catch (IOException e)
		{
			throw new IllegalArgumentException("HTTP POST binding form could not be loaded due to an I/O error", e);
		}
		
	}

	public String getBindingIdentifier()
	{
		return BindingConstants.httpPost;
	}

	public void handleRequest(HttpServletRequest request, HttpServletResponse response, AuthnProcessorData data, SPEP spep) throws AuthenticationException
	{
		if (request.getMethod().equals(HTTP_GET))
		{
			this.handleAuthnRequest(request, response, data, spep);
		}
		else if (request.getMethod().equals(HTTP_POST))
		{
			this.handleAuthnResponse(request, response, data, spep);
		}
		else
		{
			throw new AuthenticationException("Unsupported HTTP request method for this binding: " + request.getMethod());
		}
	}
	
	private void handleAuthnRequest(HttpServletRequest request, HttpServletResponse response, AuthnProcessorData data, SPEP spep) throws AuthenticationException
	{
		try
		{
			String remoteAddress = request.getRemoteAddr();
			
			this.logger.info("[Authn for {}] Initiating HTTP POST binding. Creating AuthnRequest", remoteAddress);
			String document = buildAuthnRequestDocument(request.getParameter("redirectURL"), request, response, data, spep);
			PrintStream out = new PrintStream(response.getOutputStream());
			
			/* Set cookie to allow javascript enabled browsers to autosubmit, ensures navigation with the back button is not broken because auto submit is active for only a very short period */
			Cookie autoSubmit = new Cookie("spepAutoSubmit", "enabled");
			autoSubmit.setMaxAge(172800); //set expiry to be 48 hours just to make sure we still work with badly configured clocks skewed from GMT
			autoSubmit.setPath("/");
			response.addCookie(autoSubmit);

			response.setStatus(HttpServletResponse.SC_OK);
			response.setHeader("Content-Type", "text/html");

			out.print(document);

			out.close();
			
			this.logger.info("[Authn for {}] Sent AuthnRequest successfully", remoteAddress);
		}
		catch (IOException e)
		{
			throw new AuthenticationException("Unable to send response due to an I/O error.", e);
		}
	}

	private void handleAuthnResponse(HttpServletRequest request, HttpServletResponse response, AuthnProcessorData data, SPEP spep) throws AuthenticationException
	{
		String remoteAddress = request.getRemoteAddr();
		this.logger.debug("[Authn for {}] Going to process authentication response.", remoteAddress);
		
		String base64SAMLDocument = request.getParameter("SAMLResponse");
		if (base64SAMLDocument == null || base64SAMLDocument.length() == 0)
		{
			throw new AuthenticationException("SAMLResponse request parameter was null. Unable to process response.");
		}
		
		byte[] samlDocument;
		try
		{
			samlDocument = Base64.decodeBase64(base64SAMLDocument.getBytes("UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			throw new AuthenticationException("Unable to complete authentication because a required character encoding is not supported.", e);
		}
		// Use the AuthnProcessor to unmarshal the response document.
		Response responseObject = spep.getAuthnProcessor().unmarshalResponse(samlDocument);
		this.logger.info("[Authn for {}] Got an authentication response, going to process. Response ID: {}  InResponseTo: {}", new Object[]{remoteAddress, responseObject.getID(), responseObject.getInResponseTo()});

		spep.getAuthnProcessor().processAuthnResponse(data, responseObject);

		String sessionID = data.getSessionID();
		if (sessionID == null)
		{
			throw new AuthenticationException("Session identifier from AuthnProcessor was null. Unable to process SSO event");
		}

		Cookie cookie = new Cookie(spep.getTokenName(), sessionID);

		cookie.setPath("/");
		response.addCookie(cookie);
		
		try
		{
			String redirectURL = null;
			String base64RequestURL = data.getRequestURL();
			if (base64RequestURL != null)
			{
				redirectURL = new String(Base64.decodeBase64(base64RequestURL.getBytes()));
			}
			else
			{
				redirectURL = spep.getDefaultUrl();
			}
			
			this.logger.info("[Authn for {}] Processed response ID: {} .. Created local session with session ID: {}  Redirecting user to requested content: {}", new Object[]{remoteAddress, responseObject.getID(), sessionID, redirectURL});

			response.sendRedirect(redirectURL);
		}
		catch (IOException e)
		{
			throw new AuthenticationException("Unable to send redirect back to authenticated content as an I/O error occurred", e);
		}
	}
	
	// Builds the HTTP POST document for transmitting a request to the ESOE.
	private String buildAuthnRequestDocument(String requestedURL, HttpServletRequest request, HttpServletResponse response, AuthnProcessorData data, SPEP spep) throws AuthenticationException
	{
		String remoteAddress = request.getRemoteAddr();
		this.logger.debug("[Authn for {}] Going to build AuthnRequest for new unauthenticated session.", remoteAddress);
		
		byte[] samlRequestEncoded;
		
		/* Base64 strings do not have spaces in them. So if one does, it means
		 * that something strange has happened to make the servlet engine translate
		 * the plus symbols into spaces. We just need to translate them back.
		 */
		requestedURL = requestedURL.replace(' ', '+');
		data.setRequestURL( requestedURL );
		
		this.logger.debug("[Authn for {}] Requested URL was: {}", remoteAddress, requestedURL);

		String ssoURL;
		try
		{
			MetadataProcessor metadataProcessor = spep.getMetadataProcessor();
			IdentityProviderRole identityProviderRole;
			if (spep.enableCompatibility())
			{
				identityProviderRole = metadataProcessor.getEntityRoleData(spep.getTrustedESOEIdentifier(), IdentityProviderRole.class);
			}
			else
			{
				identityProviderRole = metadataProcessor.getEntityRoleData(spep.getTrustedESOEIdentifier(), TrustedESOERole.class);
			}
			
			if (identityProviderRole == null)
			{
				throw new AuthenticationException("No ESOE entry in metadata. Unable to process authentication");
			}
			
			ssoURL = identityProviderRole.getSingleSignOnService(this.getBindingIdentifier());
			data.setDestinationURL(ssoURL);
		}
		catch (MetadataStateException e)
		{
			throw new AuthenticationException("Authentication could not be completed because the metadata state is invalid. Exception was: " + e.getMessage(), e);
		}

		AuthnRequest authnRequest = spep.getAuthnProcessor().generateAuthnRequest(data);
		this.logger.debug("[Authn for {}] Created AuthnRequest, going to marshal", remoteAddress);
		byte[] requestDocument = spep.getAuthnProcessor().marshalRequestToBytes(authnRequest);
		
		this.logger.info("[Authn for {}] Marshalled AuthnRequest successfully. Destination URL: {}  ID: {}", new Object[]{remoteAddress, ssoURL, authnRequest.getID()});

		samlRequestEncoded = Base64.encodeBase64(requestDocument);
		String base64SAMLDocument = new String(samlRequestEncoded);

		String document = this.samlMessageFormat.format(new Object[] { ssoURL, base64SAMLDocument });
		this.logger.debug("[Authn for {}] Created HTTP POST document successfully", remoteAddress);

		return document;
	}
}
