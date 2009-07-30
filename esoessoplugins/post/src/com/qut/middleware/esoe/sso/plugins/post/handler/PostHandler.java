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
 * Creation Date: 27/08/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.esoe.sso.plugins.post.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import com.qut.middleware.esoe.sso.bean.SSOProcessorData;
import com.qut.middleware.esoe.sso.bean.SSOProcessorData.RequestMethod;
import com.qut.middleware.esoe.sso.constants.SSOConstants;
import com.qut.middleware.esoe.sso.pipeline.Handler;
import com.qut.middleware.esoe.sso.plugins.post.bean.PostBindingData;
import com.qut.middleware.esoe.sso.plugins.post.exception.PostBindingException;
import com.qut.middleware.saml2.BindingConstants;

public class PostHandler implements Handler
{
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private PostLogic logic;
	private static final String HANDLER_NAME = "SAML-POST-Handler";
	
	public PostHandler(PostLogic logic)
	{
		this.logic = logic;
	}

	public String getHandlerName()
	{
		return HANDLER_NAME;
	}
	
	public result executeRequest(SSOProcessorData data)
	{
		HttpServletRequest request = data.getHttpRequest();
		HttpServletResponse response = data.getHttpResponse();
		
		String remoteAddress = data.getRemoteAddress();
		
		if (request == null || response == null)
		{
			this.logger.debug("[SSO for {}] HTTP request and/or response objects were null. Request was not submitted via HTTP POST binding.", remoteAddress);
			return result.NoAction;
		}
		
		if (data.getRequestMethod() != RequestMethod.HTTP_POST)
		{
			this.logger.debug("[SSO for {}] HTTP request method was not POST. Request was not submitted via HTTP POST binding.", remoteAddress);
			return result.NoAction;
		}
		
		String samlRequest = request.getParameter(SSOConstants.SAML_REQUEST_ELEMENT);
		String relayState = request.getParameter(SSOConstants.SAML_RELAY_STATE);

		if (samlRequest != null)
		{
			this.logger.info("[SSO for {}] Identified request as HTTP POST profile.", remoteAddress);

			PostBindingData bindingData = new PostBindingData();
			
			data.setSamlBinding(BindingConstants.httpPost);
			data.setBindingData(bindingData);
			
			bindingData.setRelayState(relayState);
			bindingData.setSAMLRequestString(samlRequest);

			try
			{
				this.logic.handlePostRequest(data, bindingData);
				this.logger.info("[SSO for {}] Successfully handled HTTP POST profile request.", remoteAddress);
				return result.Successful;
			}
			catch (PostBindingException e)
			{
				this.logger.error("[SSO for {}] Error occurred trying to handle HTTP POST profile request. Error reported was: {}", remoteAddress, e.getLocalizedMessage());
				this.logger.debug("[SSO for {}] Error occurred trying to handle HTTP POST profile request.", remoteAddress, e);
				return result.InvalidRequest;
			}
		}
		
		this.logger.debug("[SSO for {}] No POST parameter called {}. Request was not submitted via HTTP POST binding.", remoteAddress, SSOConstants.SAML_REQUEST_ELEMENT);
		return result.NoAction;
	}

	public result executeResponse(SSOProcessorData data)
	{
		HttpServletRequest request = data.getHttpRequest();
		HttpServletResponse response = data.getHttpResponse();

		if (request == null || response == null)
		{
			this.logger.debug("[SSO for unknown address] HTTP request and/or response objects were null. Response will not be submitted via HTTP POST binding.");
			return result.NoAction;
		}

		String remoteAddress = request.getRemoteAddr();

		if (!data.getSamlBinding().equals(BindingConstants.httpPost))
		{
			this.logger.debug("[SSO for {}] Request did not come in via HTTP POST binding. Response will not be submitted via HTTP POST binding.", remoteAddress);
			return result.NoAction;
		}
		this.logger.debug("[SSO for {}] Identified as HTTP POST binding. Responding via HTTP POST.", remoteAddress);

		PostBindingData bindingData = data.getBindingData(PostBindingData.class);
		if (bindingData == null)
		{
			this.logger.debug("[SSO for {}] POST binding data was not set. Assuming a binding switch, replying via POST binding anyway", remoteAddress);
		}

		try
		{
			this.logic.handlePostResponse(data, bindingData);
			return result.Successful;
		}
		catch (PostBindingException e)
		{
			this.logger.error("[SSO for {}] Error occurred while processing the POST profile response. Error was: {}", new Object[]{remoteAddress, e.getMessage()});
			this.logger.debug(MessageFormatter.format("[SSO for {}] Error occurred while processing the POST profile response. Exception follows", remoteAddress), e);
			return result.UnwillingToRespond;
		}
	}
}
