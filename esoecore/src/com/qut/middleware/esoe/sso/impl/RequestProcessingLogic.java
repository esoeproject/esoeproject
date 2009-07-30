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
 * Creation Date: 04/09/2006
 * 
 * Purpose: 
 */
package com.qut.middleware.esoe.sso.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.esoe.sso.bean.SSOProcessorData;
import com.qut.middleware.saml2.exception.InvalidSAMLRequestException;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.protocol.AuthnRequest;
import com.qut.middleware.saml2.validator.SAMLValidator;

public class RequestProcessingLogic
{
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private SAMLValidator samlValidator;
	
	public void processRequest(SSOProcessorData data, AuthnRequest request)
	{
		//
	}
	
	public void validateRequest(AuthnRequest request) throws InvalidSAMLRequestException
	{
		String requestID = request.getID();
		NameIDType issuerNameID = request.getIssuer();
		if (issuerNameID == null)
		{
			
			this.logger.error("No Issuer name presented for request with ID: {} - Validation failed.", requestID);
			throw new InvalidSAMLRequestException("No Issuer name presented for request with ID: " + requestID);
		}
		
		String issuerID = issuerNameID.getValue();
		
		this.logger.debug("Validating SAML Request ID {} issued by {} .."); //$NON-NLS-1$
		this.samlValidator.getRequestValidator().validate(request);

		this.logger.debug(Messages.getString("SSOProcessor.33")); //$NON-NLS-1$
		this.samlValidator.getAuthnRequestValidator().validate(request);
	}
}
