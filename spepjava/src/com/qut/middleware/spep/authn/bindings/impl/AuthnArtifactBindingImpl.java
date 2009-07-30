package com.qut.middleware.spep.authn.bindings.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.MessageFormat;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.qut.middleware.metadata.bean.saml.IdentityProviderRole;
import com.qut.middleware.metadata.bean.saml.TrustedESOERole;
import com.qut.middleware.metadata.exception.MetadataStateException;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.saml2.BindingConstants;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.schemas.protocol.AuthnRequest;
import com.qut.middleware.saml2.schemas.protocol.Response;
import com.qut.middleware.spep.SPEP;
import com.qut.middleware.spep.authn.AuthnProcessorData;
import com.qut.middleware.spep.authn.bindings.AuthnBinding;
import com.qut.middleware.spep.exception.AuthenticationException;

public class AuthnArtifactBindingImpl implements AuthnBinding
{

	private static final String ARTIFACT_PARAMETER = "SAMLart";
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private IdentifierGenerator generator;
	
	public AuthnArtifactBindingImpl(IdentifierGenerator generator)
	{
		this.generator = generator;
	}

	public String getBindingIdentifier()
	{
		return BindingConstants.httpArtifact;
	}

	public void handleRequest(HttpServletRequest request, HttpServletResponse response, AuthnProcessorData data, SPEP spep) throws AuthenticationException
	{
		if (!data.isReturningRequest())
		{
			this.initiateArtifactRequest(request, response, data, spep);
			return;
		}
		
		this.processArtifactResponse(request, response, data, spep);
	}

	private void initiateArtifactRequest(HttpServletRequest request, HttpServletResponse response, AuthnProcessorData data, SPEP spep) throws AuthenticationException
	{
		if (request.getParameter(ARTIFACT_PARAMETER) != null)
		{
			throw new AuthenticationException("SAML artifact parameter found but the session was not in a valid state for response processing.");
		}
		
		String remoteAddress = request.getRemoteAddr();
		
		this.logger.info("[Authn for {}] Initiating HTTP Artifact binding. Creating AuthnRequest", remoteAddress);
		String artifact = createArtifactAuthnRequest(request.getParameter("redirectURL"), request, response, data, spep);

		// null for RelayState
		try
		{
			String artifactRedirect = this.artifactRedirectURL(data.getDestinationURL(), artifact, null);
			this.logger.info("[Authn for {}] Redirecting user to {} for Artifact binding", new Object[]{remoteAddress, artifactRedirect});
			response.sendRedirect(artifactRedirect);
		}
		catch (IOException e)
		{
			throw new AuthenticationException("I/O error while trying to send redirect", e);
		}
	}

	private void processArtifactResponse(HttpServletRequest request, HttpServletResponse response, AuthnProcessorData data, SPEP spep) throws AuthenticationException
	{
		String remoteAddress = request.getRemoteAddr();
		this.logger.debug("[Authn for {}] Going to process authentication response.", remoteAddress);

		String artifactToken = request.getParameter(ARTIFACT_PARAMETER);
		if (artifactToken == null)
		{
			throw new AuthenticationException("Artifact response expected but no artifact token was received. Unable to process.");
		}
		
		Element authnResponseElement = spep.getArtifactProcessor().getRemoteArtifact(artifactToken);
		this.logger.info("[Authn for {}] Got artifact from remote source, going to unmarshal. Artifact token: {}", remoteAddress, artifactToken);
		
		Response responseObject = spep.getAuthnProcessor().unmarshalResponse(authnResponseElement);
		this.logger.info("[Authn for {}] Unmarshalled authentication response, going to process. Response ID: {}  InResponseTo: {}", new Object[]{remoteAddress, responseObject.getID(), responseObject.getInResponseTo()});
		
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

	private String createArtifactAuthnRequest(String requestedURL, HttpServletRequest request, HttpServletResponse response, AuthnProcessorData data, SPEP spep) throws AuthenticationException
	{
		String remoteAddress = request.getRemoteAddr();
		this.logger.debug("[Authn for {}] Going to build AuthnRequest for new unauthenticated session.", remoteAddress);
		
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
		Element requestElement = spep.getAuthnProcessor().marshalRequestToElement(authnRequest);
		
		this.logger.info("[Authn for {}] Marshalled AuthnRequest successfully. Destination URL: {}  ID: {}", new Object[]{remoteAddress, ssoURL, authnRequest.getID()});

		String artifactToken = spep.getArtifactProcessor().registerArtifact(requestElement, spep.getTrustedESOEIdentifier());
		
		this.logger.info("[Authn for {}] Registered artifact document successfully. Artifact token: {}", remoteAddress, artifactToken);
		
		return artifactToken;
	}

	private String artifactRedirectURL(String ssoURL, String artifactToken, String relayState)
	{
		String extra = "";
		if (relayState != null) extra = "&RelayState=" + relayState;
		return MessageFormat.format("{0}?SAMLart={1}{2}", new Object[]{ssoURL, artifactToken, extra});
	}
}
