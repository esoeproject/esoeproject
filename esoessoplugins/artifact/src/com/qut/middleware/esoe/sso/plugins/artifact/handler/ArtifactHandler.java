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

package com.qut.middleware.esoe.sso.plugins.artifact.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import com.qut.middleware.esoe.sso.bean.SSOProcessorData;
import com.qut.middleware.esoe.sso.bean.SSOProcessorData.RequestMethod;
import com.qut.middleware.esoe.sso.pipeline.Handler;
import com.qut.middleware.esoe.sso.plugins.artifact.bean.ArtifactBindingData;
import com.qut.middleware.esoe.sso.plugins.artifact.exception.ArtifactBindingException;
import com.qut.middleware.saml2.BindingConstants;

public class ArtifactHandler implements Handler
{
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String SAML_ARTIFACT = "SAMLart";
	private static final String HANDLER_NAME = "SAML-Artifact-Handler";
	
	private ArtifactLogic logic;
	
	public ArtifactHandler(ArtifactLogic logic)
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
			this.logger.debug("[SSO for {}] HTTP request and/or response were null. Request was not submitted by HTTP Artifact binding.", remoteAddress);
			return result.NoAction;
		}
		
		String artifact = request.getParameter(SAML_ARTIFACT);
		
		if (artifact != null)
		{
			this.logger.info("[SSO for {}] Identified request as HTTP Artifact binding.", remoteAddress);
			
			ArtifactBindingData bindingData = new ArtifactBindingData();
			
			bindingData.setArtifactToken(artifact);
			
			try
			{
				this.logic.handleArtifactRequest(data, bindingData);
				this.logger.info("[SSO for {}] Successfully handled HTTP Artifact profile request.", remoteAddress);
				return result.Successful;
			}
			catch (ArtifactBindingException e)
			{
				this.logger.error("[SSO for {}] Error occurred trying to handle HTTP Artifact profile request. Error reported was: {}", new Object[]{remoteAddress, e.getMessage()});
				this.logger.debug("[SSO for {}] Error occurred trying to handle HTTP Artifact profile request.", remoteAddress, e);
				return result.InvalidRequest;
			}
		}
		
		this.logger.debug("[SSO for {}] No parameter called {}. Request was not submitted via HTTP Artifact binding.", remoteAddress);
		return result.NoAction;
	}

	public result executeResponse(SSOProcessorData data)
	{
		HttpServletRequest request = data.getHttpRequest();
		HttpServletResponse response = data.getHttpResponse();
		
		if (request == null || response == null)
		{
			this.logger.debug("[SSO for unknown address] HTTP request and/or response objects were null. Response will not be submitted via HTTP Artifact binding.");
			return result.NoAction;
		}
		
		String remoteAddress = request.getRemoteAddr();
		
		if (!data.getSamlBinding().equals(BindingConstants.httpArtifact))
		{
			this.logger.debug("[SSO for {}] Request did not come in via HTTP Artifact binding. Will not respond with this binding.", remoteAddress);
			return result.NoAction;
		}
		this.logger.debug("[SSO for {}] Identified as HTTP Artifact binding. Responding via HTTP Artifact.", remoteAddress);
		
		ArtifactBindingData bindingData = data.getBindingData(ArtifactBindingData.class);
		if (bindingData == null)
		{
			this.logger.error("[SSO for {}] Artifact binding data was not set in the request stage. Unable to continue.", remoteAddress);
			return result.UnwillingToRespond;
		}
		
		try
		{
			this.logic.handleArtifactResponse(data, bindingData);
			return result.Successful;
		}
		catch (ArtifactBindingException e)
		{
			this.logger.error("[SSO for {}] Error occurred while processing the Artifact profile response. Error was: {}", new Object[]{remoteAddress, e.getMessage()});
			this.logger.debug(MessageFormatter.format("[SSO for {}] Error occurred while processing the Artifact profile response. Exception follows", remoteAddress), e);
			return result.UnwillingToRespond;
		}
	}

}
