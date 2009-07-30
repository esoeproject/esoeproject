/*
 * Copyright 2008, Queensland University of Technology
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
 * Creation Date: 26/08/2008
 *
 * Purpose:
 */

package com.qut.middleware.esoe.sso.plugins.redirect.handler.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.esoe.sso.SSOProcessor;
import com.qut.middleware.esoe.sso.bean.SSOProcessorData;
import com.qut.middleware.esoe.sso.exception.SSOException;
import com.qut.middleware.esoe.sso.plugins.redirect.bean.RedirectBindingData;
import com.qut.middleware.esoe.sso.plugins.redirect.exception.RedirectBindingException;
import com.qut.middleware.esoe.sso.plugins.redirect.handler.RedirectLogic;
import com.qut.middleware.saml2.BindingConstants;
import com.qut.middleware.saml2.StatusCodeConstants;
import com.qut.middleware.saml2.exception.ReferenceValueException;
import com.qut.middleware.saml2.exception.SignatureValueException;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.schemas.protocol.AuthnRequest;

public class RedirectLogicImpl implements RedirectLogic
{
	private static final int TMP_BUFFER_SIZE = 1024;
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sso.plugins.redirect.handler.RedirectLogic#handleRedirectRequest(com.qut.middleware.esoe.sso.bean.SSOProcessorData, com.qut.middleware.esoe.sso.plugins.redirect.bean.RedirectBindingData)
	 */
	public void handleRedirectRequest(SSOProcessorData data, RedirectBindingData bindingData)
		throws RedirectBindingException
	{
		// Grab the request object from the
		this.getRedirectRequest(data, bindingData);

		SSOProcessor ssoProcessor = data.getSSOProcessor();
		if (ssoProcessor == null)
		{
			throw new RedirectBindingException("SSOProcessor in data bean was null");
		}

		String remoteAddress = data.getRemoteAddress();

		data.setAuthnRequest(getRedirectRequest(data, bindingData));
		this.logger.debug("[SSO for {}] AuthnRequest was unmarshalled successfully by the SSO Processor", remoteAddress);

		try
		{
			ssoProcessor.processAuthnRequest(data);
			this.logger.debug("[SSO for {}] AuthnRequest was processed successfully by the SSO Processor", remoteAddress);
		}
		catch (SSOException e)
		{
			this.logger.error("[SSO for {}] SSOProcessor reported an error while processing the AuthnRequest. Error was: {}", new Object[]{remoteAddress, e.getMessage()});
			throw new RedirectBindingException("Redirect binding failed due to an error processing the AuthnRequest", e);
		}
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sso.plugins.redirect.handler.RedirectLogic#handleRedirectResponse(com.qut.middleware.esoe.sso.bean.SSOProcessorData, com.qut.middleware.esoe.sso.plugins.redirect.bean.RedirectBindingData)
	 */
	public void handleRedirectResponse(SSOProcessorData data, RedirectBindingData bindingData)
		throws RedirectBindingException
	{
			this.logger.debug("[SSO for {}] Redirect binding response. Deferring to POST binding for the response.");

			// TODO Make a smarter decision about the response binding
			data.setSamlBinding(BindingConstants.httpPost);

			// The "reset" is issued in the RedirectHandler
	}

	private AuthnRequest getRedirectRequest(SSOProcessorData data, RedirectBindingData bindingData) throws RedirectBindingException
	{
		InflaterInputStream inflaterStream = null;
		ByteArrayOutputStream inflatedByteStream = null;
		byte[] chunk = new byte[TMP_BUFFER_SIZE];

		boolean signed = (bindingData.getSignature() != null && bindingData.getSignature().length() > 0);

		SSOProcessor ssoProcessor = data.getSSOProcessor();
		String remoteAddress = data.getRemoteAddress();

		try {
			if (bindingData.getSAMLRequestString() == null || bindingData.getSAMLRequestString().length() <= 0) {
				ssoProcessor.createStatusAuthnResponse(data, StatusCodeConstants.requester, null, "No AuthnRequest document was supplied to the Redirect binding.", true);
				this.logger.error("[SSO for {}] Redirect binding failed: No AuthnRequest document was supplied in the request.", remoteAddress);
				throw new RedirectBindingException("Redirect binding failed as no AuthnRequest document was supplied in the request.");
			}

			if (bindingData.getRequestEncoding() != null && !bindingData.getRequestEncoding().equals(BindingConstants.deflateEncoding)) {
				ssoProcessor.createStatusAuthnResponse(data, StatusCodeConstants.requester, null, "The given SAML Request encoding is not supported in this implementation.", true);
				this.logger.error("[SSO for {}] Redirect binding failed: SAML Request encoding '{}' is not supported in the current implementation.", new Object[]{remoteAddress, bindingData.getRequestEncoding()});
				throw new RedirectBindingException("Redirect binding failed as the given SAML Request encoding is not supported in the current implementation.");
			}

			if (bindingData.getSignature() != null) {
				ssoProcessor.createStatusAuthnResponse(data, StatusCodeConstants.requester, null, "Signed Redirect binding documents are not supported in this implementation.", true);
				this.logger.error("[SSO for {}] Redirect binding failed: Signed Redirect binding documents are not supported in the current implementation.", remoteAddress);
				throw new RedirectBindingException("Redirect binding failed as Signed Redirect binding documents are not supported in the current implementation.");
			}
		} catch (SSOException e) {
			this.logger.error("[SSO for {}] Redirect binding failed to generate an error response. Error was: {}", new Object[]{remoteAddress, e.getMessage()});
			throw new RedirectBindingException("Redirect binding failed to generate an error reponse. Original error follows", e);
		}

		try
		{
			/*
			 * Retrieves the AuthnRequest from the encoded and compressed String extracted from the request of SAML HTTP
			 * Redirect. The AuthnRequest XML is retrieved in the following order: 1. Base64 decode, 2. Inflate
			 */
			byte[] decodedBytes = Base64.decodeBase64(bindingData.getSAMLRequestString().getBytes());
			ByteArrayInputStream decodedByteStream = new ByteArrayInputStream(decodedBytes);
			inflaterStream = new InflaterInputStream(decodedByteStream, new Inflater(true));
			inflatedByteStream = new ByteArrayOutputStream();

			int writeCount = 0;
			int count = 0;
			// Inflate and dump in the output stream to build a byte array.
			while ((count = inflaterStream.read(chunk)) >= 0)
			{
				inflatedByteStream.write(chunk, 0, count);
				writeCount = writeCount + count;
			}

			byte[] samlRequestDocument = inflatedByteStream.toByteArray();

			AuthnRequest authnRequest = ssoProcessor.unmarshallRequest(samlRequestDocument, signed);
			this.logger.debug("[SSO for {}] AuthnRequest was unmarshalled successfully by the SSO Processor", remoteAddress);

			return authnRequest;
		}
		catch (IOException e)
		{
			this.logger.error("[SSO for {}] IO exception occurred while inflating the request document. Error was: {}", new Object[]{remoteAddress, e.getMessage()});
			throw new RedirectBindingException("IO exception occurred while inflating the request document.");
		}
		catch (SignatureValueException e)
		{
			this.logger.error("[SSO for {}] Signature value exception occurred while trying to unmarshal the redirect request. Error was: {}", new Object[]{remoteAddress, e.getMessage()});
			this.logger.debug("[SSO for {}] Signature value exception occurred while trying to unmarshal the redirect request. Exception follows", remoteAddress, e);
			throw new RedirectBindingException("Signature value exception occurred while trying to unmarshal the redirect request.");
		}
		catch (ReferenceValueException e)
		{
			this.logger.error("[SSO for {}] Reference value exception occurred while unmarshalling the redirect request. Error was: {}", new Object[]{remoteAddress, e.getMessage()});
			this.logger.debug("[SSO for {}] Reference value exception occurred while unmarshalling the redirect request. Exception follows", remoteAddress, e);
			throw new RedirectBindingException("Reference value exception occurred while unmarshalling the redirect request.");
		}
		catch (UnmarshallerException e)
		{
			this.logger.error("[SSO for {}] Unmarshaller exception occurred while unmarshalling the redirect request. Error was: {}", new Object[]{remoteAddress, e.getMessage()});
			this.logger.debug("[SSO for {}] Unmarshaller exception occurred while unmarshalling the redirect request. Exception follows", remoteAddress, e);
			throw new RedirectBindingException("Unmarshaller exception occurred while unmarshalling the redirect request.");
		}
		finally
		{
			try
			{
				if (inflatedByteStream != null)
				{
					inflatedByteStream.reset();
					inflatedByteStream.close();
				}

				if(inflaterStream != null)
					inflaterStream.close();
			}
			catch (IOException e)
			{
				this.logger.error("Unable to close stream correctly - " + e.getLocalizedMessage());
				this.logger.debug(e.getLocalizedMessage(), e);
			}
		}
	}
}
