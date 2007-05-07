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
 * Creation Date: 27/10/2006
 * 
 * Purpose: Control point for various implemented SAML document validators
 */
package com.qut.middleware.saml2.validator.impl;

import org.apache.log4j.Logger;

import com.qut.middleware.saml2.identifier.IdentifierCache;
import com.qut.middleware.saml2.validator.SAMLAssertionValidator;
import com.qut.middleware.saml2.validator.SAMLAuthnRequestValidator;
import com.qut.middleware.saml2.validator.SAMLRequestValidator;
import com.qut.middleware.saml2.validator.SAMLResponseValidator;
import com.qut.middleware.saml2.validator.SAMLValidator;

public class SAMLValidatorImpl implements SAMLValidator
{
	private SAMLRequestValidator requestValidator;
	private SAMLAuthnRequestValidator authnRequestValidator;
	private SAMLResponseValidator responseValidator;
	private SAMLAssertionValidator assertionValidator;
	
	/* Local logging instance */
	private Logger logger = Logger.getLogger(SAMLValidatorImpl.class.getName());
	
	/**
	 * @param identifierCache An implementation of IdentifierCache through which SAML ID's can be verified against for uniqueness
	 * @param allowedTimeSkew Time skew in seconds thats documents may slip
	 */
	public SAMLValidatorImpl(IdentifierCache identifierCache, int allowedTimeSkew)
	{
		/* Ensure validator is created with a stable base */
		if( identifierCache == null )
		{
			this.logger.fatal(Messages.getString("SAMLValidatorImpl.0")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("SAMLValidatorImpl.0")); //$NON-NLS-1$
		}
		
		if(allowedTimeSkew > Integer.MAX_VALUE / 1000)
		{
			this.logger.fatal(Messages.getString("SAMLValidatorImpl.1")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("SAMLValidatorImpl.1")); //$NON-NLS-1$
		}
		
		this.requestValidator = new SAMLRequestValidatorImpl(identifierCache, allowedTimeSkew);
		this.authnRequestValidator = new SAMLAuthnRequestValidatorImpl();
		this.responseValidator = new SAMLResponseValidatorImpl(identifierCache, allowedTimeSkew);
		this.assertionValidator = new SAMLAssertionValidatorImpl(identifierCache, allowedTimeSkew);
		
		this.logger.info(Messages.getString("SAMLValidatorImpl.2") + allowedTimeSkew + Messages.getString("SAMLValidatorImpl.3")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/* (non-Javadoc)
	 * @see com.qut.middleware.saml2.validator.SAMLValidator#getAssertionValidator()
	 */
	public SAMLAssertionValidator getAssertionValidator()
	{
		return this.assertionValidator;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.saml2.validator.SAMLValidator#getRequestValidator()
	 */
	public SAMLRequestValidator getRequestValidator()
	{
		return this.requestValidator;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.saml2.validator.SAMLValidator#getResponseValidator()
	 */
	public SAMLResponseValidator getResponseValidator()
	{
		return this.responseValidator;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.saml2.validator.SAMLValidator#getAuthnRequestValidator()
	 */
	public SAMLAuthnRequestValidator getAuthnRequestValidator()
	{
		return authnRequestValidator;
	}
}
