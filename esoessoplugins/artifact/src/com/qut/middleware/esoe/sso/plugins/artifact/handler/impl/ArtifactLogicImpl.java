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
 * Creation Date: 21/11/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.esoe.sso.plugins.artifact.handler.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.qut.middleware.esoe.sso.SSOProcessor;
import com.qut.middleware.esoe.sso.bean.SSOProcessorData;
import com.qut.middleware.esoe.sso.exception.SSOException;
import com.qut.middleware.esoe.sso.plugins.artifact.ArtifactProcessor;
import com.qut.middleware.esoe.sso.plugins.artifact.bean.ArtifactBindingData;
import com.qut.middleware.esoe.sso.plugins.artifact.exception.ArtifactBindingException;
import com.qut.middleware.esoe.sso.plugins.artifact.handler.ArtifactLogic;
import com.qut.middleware.saml2.exception.ReferenceValueException;
import com.qut.middleware.saml2.exception.SignatureValueException;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.schemas.protocol.AuthnRequest;

public class ArtifactLogicImpl implements ArtifactLogic
{
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private ArtifactProcessor artifactProcessor;
	
	public void setArtifactProcessor(ArtifactProcessor artifactProcessor)
	{
		this.artifactProcessor = artifactProcessor;
	}

	public void handleArtifactRequest(SSOProcessorData data, ArtifactBindingData bindingData) throws ArtifactBindingException
	{
		SSOProcessor ssoProcessor = data.getSSOProcessor();
		if (ssoProcessor == null)
		{
			throw new ArtifactBindingException("SSOProcessor in data bean was null");
		}
		
		String remoteAddress = data.getRemoteAddress();
		
		String artifactToken = bindingData.getArtifactToken();
		this.logger.info("[SSO for {}] Client presented artifact token {} - going to resolve.", new Object[]{remoteAddress, artifactToken});
		
		boolean signed = true;
		Element document = this.artifactProcessor.getRemoteArtifact(artifactToken);
		
		AuthnRequest authnRequest;
		try
		{
			authnRequest = ssoProcessor.unmarshallRequest(document, signed);
			this.logger.debug("[SSO for {}] AuthnRequest was unmarshalled successfully by the SSO Processor", remoteAddress);
		}
		catch (SignatureValueException e)
		{
			String issuer = "unknown";
			if (e.getJAXBObject() != null)
			{
				authnRequest = (AuthnRequest)e.getJAXBObject();
				issuer = authnRequest.getIssuer().getValue();
			}
			this.logger.error("[SSO for {}] Signature validation failure while unwrapping request document. Issuer was: {}  Error was: {}", new Object[]{remoteAddress, issuer, e.getMessage()});
			throw new ArtifactBindingException("Signature validation failed on artifact document. Unable to process AuthnRequest. Issuer was: " + issuer, e);
		}
		catch (ReferenceValueException e)
		{
			String issuer = "unknown";
			if (e.getJAXBObject() != null)
			{
				authnRequest = (AuthnRequest)e.getJAXBObject();
				issuer = authnRequest.getIssuer().getValue();
			}
			this.logger.error("[SSO for {}] Reference value failure while unwrapping request document. Issuer was: {}  Error was: {}", new Object[]{remoteAddress, issuer, e.getMessage()});
			throw new ArtifactBindingException("Reference value failure on artifact document. Unable to process AuthnRequest. Issuer was: " + issuer, e);
		}
		catch (UnmarshallerException e)
		{
			this.logger.error("[SSO for {}] Unmarshalling failed while unwrapping request document. Error was: {}", new Object[]{remoteAddress, e.getMessage()});
			throw new ArtifactBindingException("Unmarshalling failed on artifact document. Unable to process AuthnRequest", e);
		}
		
		data.setAuthnRequest(authnRequest);
		try
		{
			ssoProcessor.processAuthnRequest(data);
			this.logger.debug("[SSO for {}] AuthnRequest was processed successfully by the SSO Processor", remoteAddress);
		}
		catch (SSOException e)
		{
			this.logger.error("[SSO for {}] SSOProcessor reported an error while processing the AuthnRequest. Issuer was: {}  Error was: {}", new Object[]{remoteAddress, authnRequest.getIssuer().getValue(), e.getMessage()});
			throw new ArtifactBindingException("Artifact binding failed due to an error processing the AuthnRequest", e);
		}
	}

	public void handleArtifactResponse(SSOProcessorData data, ArtifactBindingData bindingData) throws ArtifactBindingException
	{
		String remoteAddress = data.getRemoteAddress();
		String sessionIndex = data.getSessionIndex();
		String requestCharsetName = data.getRequestCharsetName();
		
		Element response;
		try
		{
			response = data.getSSOProcessor().createSuccessfulAuthnResponseElement(data, sessionIndex, requestCharsetName, true);
			this.logger.debug("[SSO for {}] Generated Authn response. Going to generate artifact token", remoteAddress);
		}
		catch (SSOException e)
		{
			this.logger.error("[SSO for {}] Failed to generate Authn response document. Error was: {}", new Object[]{remoteAddress, e.getMessage()});
			throw new ArtifactBindingException("Failed to generate Authn response document", e);
		}
		
		data.setResponded(true);
		
		this.logger.info("[SSO for {}] Generated Authn response document for session with SAML ID {}, session index is {} and response charset is {}", new Object[]{remoteAddress, data.getPrincipal().getSAMLAuthnIdentifier(), sessionIndex, requestCharsetName});
		this.artifactProcessor.registerArtifact(response, data.getIssuerID());
	}

}
