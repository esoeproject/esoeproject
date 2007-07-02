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
 * Creation Date: 18/06/2007
 * 
 * Purpose: Generates dynamic openID 2.0 compliant form for authn request
 */
package com.qut.middleware.delegator.openid.pages;

import java.io.IOException;
import java.util.Map;

import net.sf.click.Page;

import org.apache.log4j.Logger;
import org.openid4java.message.AuthRequest;

import com.qut.middleware.delegator.openid.ConfigurationConstants;

public class OpenIDAuthenticatorPage extends Page
{
	public String openIDServerEndpoint;
	public Map<String, String> openIDParameters;
	
	/* Local logging instance */
	private Logger logger = Logger.getLogger(OpenIDAuthenticatorPage.class.getName());
	
	public OpenIDAuthenticatorPage()
	{
		
	}
	
	public void onGet()
	{
		AuthRequest authReq;
		
		authReq = (AuthRequest)this.getContext().getSessionAttribute(ConfigurationConstants.OPENID_AUTH_REQUEST);
		
		if(authReq != null)
		{
			this.openIDServerEndpoint = authReq.getDestinationUrl(false);
			this.openIDParameters = authReq.getParameterMap();
		}
		else
		{
			try
			{
				this.getContext().getResponse().sendError(400);
			}
			catch (IOException e)
			{
				this.logger.error("IOException while attempting to send error response");
				this.logger.debug(e);
			}
		}
		
		return;
	}
}
