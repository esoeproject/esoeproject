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

package com.qut.middleware.esoe.sso.plugins.redirect.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import com.qut.middleware.esoe.sso.bean.SSOProcessorData;
import com.qut.middleware.esoe.sso.bean.SSOProcessorData.RequestMethod;
import com.qut.middleware.esoe.sso.constants.SSOConstants;
import com.qut.middleware.esoe.sso.pipeline.Handler;
import com.qut.middleware.esoe.sso.plugins.redirect.bean.RedirectBindingData;
import com.qut.middleware.esoe.sso.plugins.redirect.exception.RedirectBindingException;
import com.qut.middleware.saml2.BindingConstants;

public class RedirectHandler implements Handler
{
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private RedirectLogic logic;
	private static final String HANDLER_NAME = "SAML-Redirect-Handler";

	public result executeRequest(SSOProcessorData data)
	{
		HttpServletRequest request = data.getHttpRequest();
		HttpServletResponse response = data.getHttpResponse();

		String remoteAddress = data.getRemoteAddress();

		if (request == null || response == null)
		{
			this.logger.debug("[SSO for {}] HTTP request and/or response objects were null. Request was not submitted via HTTP Redirect binding.", remoteAddress);
			return result.NoAction;
		}

		if (data.getRequestMethod() != RequestMethod.HTTP_GET)
		{
			this.logger.debug("[SSO for {}] HTTP request method was not GET. Request was not submitted via HTTP Redirect binding.", remoteAddress);
			return result.NoAction;
		}

		/* Determine if this is a HTTP Redirect Binding SAML AuthnRequest */
		String samlRequest = request.getParameter(SSOConstants.SAML_REQUEST_ELEMENT);
		String relayState = request.getParameter(SSOConstants.SAML_RELAY_STATE);
		String encoding = request.getParameter(SSOConstants.SAML_REQUEST_ENCODING);
		String sigAlg = request.getParameter(SSOConstants.SAML_SIG_ALGORITHM);
		String signature = request.getParameter(SSOConstants.SAML_REQUEST_SIGNATURE);

		if (samlRequest != null && samlRequest.length() > 0)
		{
			this.logger.info("[SSO for {}] Identified request as HTTP Redirect profile.", remoteAddress);

			RedirectBindingData bindingData = new RedirectBindingData();
			bindingData.setRelayState(relayState);
			bindingData.setRequestEncoding(encoding);
			bindingData.setSignatureAlgorithm(sigAlg);
			bindingData.setSignature(signature);
			bindingData.setSAMLRequestString(samlRequest);

			data.setSamlBinding(BindingConstants.httpRedirect);
			data.setBindingData(bindingData);

			data.setHttpRequest(request);
			data.setHttpResponse(response);

			try
			{
				this.logic.handleRedirectRequest(data, bindingData);
				this.logger.info("[SSO for {}] Successfully handled HTTP Redirect profile request.", remoteAddress);
				return result.Successful;
			}
			catch (RedirectBindingException e)
			{
				this.logger.error("[SSO for {}] Error occurred trying to handle HTTP Redirect profile request. Error reported was: {}", remoteAddress, e.getLocalizedMessage());
				return result.InvalidRequest;
			}
		}

		this.logger.debug("[SSO for {}] No GET parameter called {}. Request was not submitted via HTTP Redirect binding.", remoteAddress, SSOConstants.SAML_REQUEST_ELEMENT);
		// No action taken at this point.
		return result.NoAction;
	}

	public result executeResponse(SSOProcessorData data)
	{
		HttpServletRequest request = data.getHttpRequest();
		HttpServletResponse response = data.getHttpResponse();

		if (request == null || response == null)
		{
			this.logger.debug("[SSO for unknown address] HTTP request and/or response objects were null. Response will not be submitted via HTTP Redirect binding.");
			return result.NoAction;
		}

		String remoteAddress = request.getRemoteAddr();

		if (!data.getSamlBinding().equals(BindingConstants.httpRedirect))
		{
			this.logger.debug("[SSO for {}] Request did not come in via HTTP Redirect binding. Response will not be submitted via HTTP Redirect binding.", remoteAddress);
			return result.NoAction;
		}
		this.logger.debug("[SSO for {}] Identified as HTTP Redirect binding. Responding via HTTP Redirect.", remoteAddress);

		RedirectBindingData bindingData = data.getBindingData(RedirectBindingData.class);
		if (bindingData == null)
		{
			this.logger.error("[SSO for {}] Redirect binding data was not set in the request stage. Unable to continue.", remoteAddress);
			return result.UnwillingToRespond;
		}

		try
		{
			this.logic.handleRedirectResponse(data, bindingData);
			// Note, this is not the normal return code from an SSO plugin.
			return result.Reset;
		}
		catch (RedirectBindingException e)
		{
			this.logger.error("[SSO for {}] Error occurred while processing the Redirect profile response. Error was: {}", new Object[]{remoteAddress, e.getMessage()});
			this.logger.debug(MessageFormatter.format("[SSO for {}] Error occurred while processing the Redirect profile response. Exception follows", remoteAddress), e);
			return result.UnwillingToRespond;
		}
	}

	public String getHandlerName()
	{
		return HANDLER_NAME;
	}

}
