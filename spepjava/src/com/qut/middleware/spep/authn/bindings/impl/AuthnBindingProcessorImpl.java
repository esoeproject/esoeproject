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

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.metadata.bean.saml.TrustedESOERole;
import com.qut.middleware.metadata.exception.MetadataStateException;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.spep.authn.bindings.AuthnBinding;
import com.qut.middleware.spep.authn.bindings.AuthnBindingProcessor;
import com.qut.middleware.spep.exception.AuthenticationException;

public class AuthnBindingProcessorImpl implements AuthnBindingProcessor
{
	private List<AuthnBinding> bindings;
	private MetadataProcessor metadataProcessor;
	private String esoeIdentifier;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public void setBindingHandlers(List<AuthnBinding> bindings)
	{
		this.bindings = bindings;
	}
	
	public void setMetadataProcessor(MetadataProcessor metadataProcessor)
	{
		this.metadataProcessor = metadataProcessor;
	}
	
	public void setEsoeIdentifier(String esoeIdentifier)
	{
		this.esoeIdentifier = esoeIdentifier;
	}

	public AuthnBinding chooseBinding(HttpServletRequest request) throws AuthenticationException
	{
		try
		{
			TrustedESOERole esoe = this.metadataProcessor.getEntityRoleData(this.esoeIdentifier, TrustedESOERole.class);
			if (esoe == null)
			{
				this.logger.error("No entity data available for trusted ESOE {}. Unable to authenticate.", this.esoeIdentifier);
				throw new AuthenticationException("No trusted ESOE role was present for the ESOE entity ID. Unable to authenticate");
			}
			
			for (AuthnBinding binding : this.bindings)
			{
				String bindingIdentifier = binding.getBindingIdentifier();
				
				String endpoint = esoe.getSingleSignOnService(bindingIdentifier);
				if (endpoint != null)
				{
					this.logger.debug("Got endpoint {} for binding {} - authenticating using this binding", endpoint, bindingIdentifier);
					return binding;
				}
				
				this.logger.debug("No endpoint for binding {}", bindingIdentifier);
			}
			
			this.logger.error("No valid bindings available for entity {}. Unable to authenticate.", this.esoeIdentifier);
			throw new AuthenticationException("No valid bindings available for the ESOE entity. Unable to authenticate");
		}
		catch (MetadataStateException e)
		{
			this.logger.error("The metadata is in an invalid state, unable to retrieve any endpoints for the ESOE entity. Unable to authenticate. Error was: {}", e.getMessage());
			throw new AuthenticationException("The metadata is in an invalid state. Unable to authenticate");
		}
	}
	
	public AuthnBinding getBinding(String bindingIdentifier)
	{
		for (AuthnBinding binding : this.bindings)
		{
			if (binding.getBindingIdentifier().equals(bindingIdentifier)) return binding;
		}
		
		return null;
	}

}
