/* Copyright 2006, Queensland University of Technology
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
 * Creation Date: 28/08/2008
 *
 * Purpose:
 */

package com.qut.middleware.esoe.sso.plugins.post.handler.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.text.MessageFormat;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.qut.middleware.esoe.sso.SSOProcessor;
import com.qut.middleware.esoe.sso.bean.SSOProcessorData;
import com.qut.middleware.esoe.sso.exception.SSOException;
import com.qut.middleware.esoe.sso.plugins.post.bean.PostBindingData;
import com.qut.middleware.esoe.sso.plugins.post.exception.PostBindingException;
import com.qut.middleware.esoe.sso.plugins.post.handler.PostLogic;
import com.qut.middleware.saml2.exception.ReferenceValueException;
import com.qut.middleware.saml2.exception.SignatureValueException;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.schemas.protocol.AuthnRequest;

public class PostLogicImpl implements PostLogic
{
	private final String SAML_RESPONSE_TEMPLATE = "samlResponseTemplate.html"; //$NON-NLS-1$
	private final String samlResponseTemplate;
	private MessageFormat samlMessageFormat;
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public PostLogicImpl()
	{
		try
		{
			URL url = this.getClass().getResource(this.SAML_RESPONSE_TEMPLATE);
			if (url == null)
			{
				throw new IllegalArgumentException("Unable to construct logic class for HTTP POST binding. URL for SAML response template was null.");
			}

			this.samlResponseTemplate = FileCopyUtils.copyToString(new InputStreamReader(url.openStream()));
			this.samlMessageFormat = new MessageFormat(this.samlResponseTemplate);
		}
		catch (FileNotFoundException e)
		{
			throw new IllegalArgumentException("Unable to construct logic class for HTTP POST binding. File was not found while trying to load the SAML response template.", e);
		}
		catch (IOException e)
		{
			throw new UnsupportedOperationException("Unable to construct logic class for HTTP POST binding. IO error occurred while trying to load the SAML response template.", e);
		}

		this.logger.info("HTTP POST binding logic initialized");
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sso.plugins.post.handler.impl.PostLogic#handlePostRequest(com.qut.middleware.esoe.sso.bean.SSOProcessorData, com.qut.middleware.esoe.sso.plugins.post.bean.PostBindingData)
	 */
	public void handlePostRequest(SSOProcessorData data, PostBindingData bindingData)
		throws PostBindingException
	{
		SSOProcessor ssoProcessor = data.getSSOProcessor();
		if (ssoProcessor == null)
		{
			throw new PostBindingException("SSOProcessor in data bean was null");
		}

		String remoteAddress = data.getRemoteAddress();

		String samlRequest = bindingData.getSAMLRequestString();
		if (samlRequest == null) {
			throw new PostBindingException("SAML Request in binding data was null");
		}

		byte[] requestDocument = Base64.decodeBase64(samlRequest.getBytes());
		boolean signed = true; // TODO Should this be specified somewhere, or is hard coded 'true' ok?
		AuthnRequest authnRequest;

		try
		{
			CharsetDetector detector = new CharsetDetector();
			detector.setText(requestDocument);
			CharsetMatch match = detector.detect();
			if (match != null)
			{
				data.setRequestCharsetName(match.getName());
			}

			authnRequest = ssoProcessor.unmarshallRequest(requestDocument, signed);
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
			throw new PostBindingException("Post binding failed due to signature validation failure.", e);
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
			throw new PostBindingException("Post binding failed due to reference value error.", e);
		}
		catch (UnmarshallerException e)
		{
			this.logger.error("[SSO for {}] Unmarshalling failed while unwrapping request document. Error was: {}", new Object[]{remoteAddress, e.getMessage()});
			throw new PostBindingException("Post binding failed due to unmarshalling error.", e);
		}

		data.setAuthnRequest(authnRequest);
		try
		{
			ssoProcessor.processAuthnRequest(data);
			this.logger.debug("[SSO for {}] AuthnRequest was processed successfully by the SSO Processor", remoteAddress);
		}
		catch (SSOException e)
		{
			String issuer = (authnRequest != null ? (authnRequest.getIssuer() != null ? authnRequest.getIssuer().getValue() : "null") : "unknown");
			this.logger.error("[SSO for {}] SSOProcessor reported an error while processing the AuthnRequest. Issuer was: {}  Error was: {}", new Object[]{remoteAddress, issuer, e.getMessage()});
			throw new PostBindingException("Post binding failed due to an error processing the AuthnRequest", e);
		}
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sso.plugins.post.handler.impl.PostLogic#handlePostResponse(com.qut.middleware.esoe.sso.bean.SSOProcessorData, com.qut.middleware.esoe.sso.plugins.post.bean.PostBindingData)
	 */
	public void handlePostResponse(SSOProcessorData data, PostBindingData bindingData)
		throws PostBindingException
	{
		String remoteAddress = data.getRemoteAddress();
		String sessionIndex = data.getSessionIndex();
		String requestCharsetName = data.getRequestCharsetName();
		try
		{
			byte[] response = data.getSSOProcessor().createSuccessfulAuthnResponse(data, sessionIndex, requestCharsetName, true);
			data.setResponseDocument(response);
			this.logger.debug("[SSO for {}] Generated Authn response. Going to send response document", remoteAddress);
		}
		catch (SSOException e)
		{
			this.logger.error("[SSO for {}] Failed to generate Authn response document. Error was: {}", new Object[]{remoteAddress, e.getMessage()});
			throw new PostBindingException("Failed to generate Authn response document", e);
		}

		this.sendPostResponseDocument(data);

		this.logger.info("[SSO for {}] Generated Authn response document for session with SAML ID {}, session index is {} and response charset is {}", new Object[]{remoteAddress, data.getPrincipal().getSAMLAuthnIdentifier(), sessionIndex, requestCharsetName});
	}

	private void sendPostResponseDocument(SSOProcessorData data) throws PostBindingException
	{
		String remoteAddress = data.getHttpRequest().getRemoteAddr();
		try
		{
			HttpServletResponse response = data.getHttpResponse();
			PrintWriter writer = response.getWriter();

			response.setContentType("text/html");

			/* Set cookie to allow javascript enabled browsers to auto submit, ensures navigation with the back button is not broken
			 * because auto submit is active only when this cookie exists, and the submit javascript removes it */
			Cookie autoSubmit = new Cookie("esoeAutoSubmit", "enabled");
			autoSubmit.setMaxAge(172800); //set expiry to be 48 hours just to make sure we still work with badly configured clocks skewed from GMT
			autoSubmit.setPath("/");
			response.addCookie(autoSubmit);

			this.logger.debug("[SSO for {}] Cookie added. About to check for response document.", remoteAddress); //$NON-NLS-1$

			if (data.getResponseDocument() == null)
			{
				this.logger.error("[SSO for {}] No response document was generated. Unable to respond to HTTP-POST binding request.", remoteAddress); //$NON-NLS-1$
				throw new PostBindingException("No response document was generated. Unable to respond to HTTP-POST binding request.");
			}

			// TODO relaystate
			String responseRelayState = "";// = data.getRelayState();
			if (responseRelayState == null)
				responseRelayState = new String("");

			/* Encode SAML Response in base64 */
			byte[] samlResponseEncoded = Base64.encodeBase64(data.getResponseDocument()); //$NON-NLS-1$
			Object[] responseArgs = new Object[] { data.getResponseEndpoint(), new String(samlResponseEncoded), responseRelayState };
			String htmlOutput = this.samlMessageFormat.format(responseArgs);

			this.logger.debug("[SSO for {}] Writing HTML document, response for HTTP-POST request. Length: {} bytes", remoteAddress, htmlOutput.length());

			this.logger.trace("[SSO for {}] Writing HTML document. Content:\n{}", remoteAddress, htmlOutput);

			writer.print(htmlOutput);
			writer.flush();
		}
		catch (IOException e)
		{
			this.logger.error("[SSO for {}] I/O exception occurred trying to write the HTTP response. Unable to respond with HTTP-POST binding. Error was: {}", remoteAddress, e.getMessage());
			throw new PostBindingException("I/O exception occurred trying to write the HTTP response. Unable to respond with HTTP-POST binding.", e);
		}
	}
}
