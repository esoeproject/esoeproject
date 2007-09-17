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
 * Creation Date: 26/10/2006
 * 
 * Purpose: Implementation to validate submitted Request element to SAML 2.0 requirements
 */
package com.qut.middleware.saml2.validator.impl;

import org.apache.log4j.Logger;

import com.qut.middleware.saml2.exception.InvalidSAMLRequestException;
import com.qut.middleware.saml2.schemas.protocol.AuthnRequest;
import com.qut.middleware.saml2.validator.SAMLAuthnRequestValidator;

public class SAMLAuthnRequestValidatorImpl implements SAMLAuthnRequestValidator
{
	
	/* Local logging instance */
	private Logger logger = Logger.getLogger(SAMLAuthnRequestValidatorImpl.class.getName());
	
	/* (non-Javadoc)
	 * @see com.qut.middleware.saml2.handler.SAMLAuthnRequestValidator#validate(com.qut.middleware.saml2.schemas.protocol.RequestAbstractType)
	 */
	public void validate(AuthnRequest authnRequest) throws InvalidSAMLRequestException
	{
		if( authnRequest.getNameIDPolicy() == null)
		{
			this.logger.warn(Messages.getString("SAMLAuthnRequestValidatorImpl.0")); //$NON-NLS-1$
			throw new InvalidSAMLRequestException(Messages.getString("SAMLAuthnRequestValidatorImpl.1")); //$NON-NLS-1$
		}
	}
}
