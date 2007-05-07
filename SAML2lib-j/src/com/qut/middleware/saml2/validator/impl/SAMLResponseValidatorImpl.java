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
 * Purpose: Implementation to validate Response element to SAML 2.0 requirements
 */
package com.qut.middleware.saml2.validator.impl;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;

import com.qut.middleware.saml2.VersionConstants;
import com.qut.middleware.saml2.exception.InvalidSAMLResponseException;
import com.qut.middleware.saml2.identifier.IdentifierCache;
import com.qut.middleware.saml2.identifier.exception.IdentifierCollisionException;
import com.qut.middleware.saml2.schemas.protocol.StatusResponseType;
import com.qut.middleware.saml2.validator.SAMLResponseValidator;

public class SAMLResponseValidatorImpl implements SAMLResponseValidator
{

	private IdentifierCache identifierCache;
	private int allowedTimeSkew;

	/* Local logging instance */
	private Logger logger = Logger.getLogger(SAMLResponseValidatorImpl.class.getName());

	public SAMLResponseValidatorImpl(IdentifierCache identifierCache, int allowedTimeSkew)
	{
		if (identifierCache == null)
		{
			this.logger.fatal(Messages.getString("SAMLRequestValidatorImpl.3")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("SAMLRequestValidatorImpl.3")); //$NON-NLS-1$
		}

		if (allowedTimeSkew > Integer.MAX_VALUE / 1000)
		{
			this.logger.fatal(Messages.getString("SAMLRequestValidatorImpl.5")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("SAMLRequestValidatorImpl.5")); //$NON-NLS-1$
		}

		this.allowedTimeSkew = allowedTimeSkew * 1000; /* internally work in milliseconds */
		this.identifierCache = identifierCache;

		this.logger.info("Created SAMLResponseValidatorImpl successfully"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.saml2.validator.SAMLResponseValidator#validate(com.qut.middleware.saml2.schemas.protocol.Response)
	 */
	public void validate(StatusResponseType response) throws InvalidSAMLResponseException
	{
		if (response == null)
		{
			this.logger.error(Messages.getString("SAMLResponseValidatorImpl.0")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("SAMLResponseValidatorImpl.1")); //$NON-NLS-1$
		}

		if (response.getID() == null)
		{
			this.logger.warn(Messages.getString("SAMLResponseValidatorImpl.2")); //$NON-NLS-1$
			throw new InvalidSAMLResponseException(Messages.getString("SAMLResponseValidatorImpl.3")); //$NON-NLS-1$
		}

		/* Get the time that this document was generated. */
		if (response.getIssueInstant() == null)
		{
			this.logger.warn(Messages.getString("SAMLResponseValidatorImpl.4")); //$NON-NLS-1$
			throw new InvalidSAMLResponseException(Messages.getString("SAMLResponseValidatorImpl.5")); //$NON-NLS-1$
		}

		XMLGregorianCalendar issueInstant = response.getIssueInstant();
		GregorianCalendar issuerCalendar = issueInstant.toGregorianCalendar();
		long documentTime = issuerCalendar.getTimeInMillis();
		
		// current time is current UTC time, not localized time
		SimpleTimeZone utc = new SimpleTimeZone(0, "UTC"); //$NON-NLS-1$
		Calendar thisCalendar = new GregorianCalendar(utc);
		long currentTime = thisCalendar.getTimeInMillis();
		
		/* Make sure that the time doesn't differ by any more than the amount of clock skew we are allowed. */
		if (Math.abs(currentTime - documentTime) > this.allowedTimeSkew)
		{
			this.logger.warn(Messages.getString("SAMLResponseValidatorImpl.6")); //$NON-NLS-1$
			throw new InvalidSAMLResponseException(Messages.getString("SAMLResponseValidatorImpl.7")); //$NON-NLS-1$
		}

		/* Ensure that an issuer has been provided for this response */
		if (response.getIssuer() == null || response.getIssuer().getValue() == null
				|| response.getIssuer().getValue().length() <= 0)
		{
			this.logger.warn(Messages.getString("SAMLResponseValidatorImpl.8")); //$NON-NLS-1$
			throw new InvalidSAMLResponseException(Messages.getString("SAMLResponseValidatorImpl.9")); //$NON-NLS-1$
		}

		if (!response.getVersion().equals(VersionConstants.saml20))
		{
			this.logger.warn(Messages.getString("SAMLResponseValidatorImpl.10")); //$NON-NLS-1$
			throw new InvalidSAMLResponseException(Messages.getString("SAMLResponseValidatorImpl.11")); //$NON-NLS-1$
		}
		
		if(response.getStatus() == null || response.getStatus().getStatusCode() == null)
		{
			this.logger.warn(Messages.getString("SAMLResponseValidatorImpl.12")); //$NON-NLS-1$
			throw new InvalidSAMLResponseException(Messages.getString("SAMLResponseValidatorImpl.13")); //$NON-NLS-1$
		}

		if (response.getInResponseTo() != null)
		{
			if (!this.identifierCache.containsIdentifier(response.getInResponseTo()))
			{
				this.logger.warn(Messages.getString("SAMLResponseValidatorImpl.14")); //$NON-NLS-1$
				throw new InvalidSAMLResponseException(Messages.getString("SAMLResponseValidatorImpl.15")); //$NON-NLS-1$
			}
		}

		/* Make sure the identifier hasn't already been used and we aren't seeing an attempted replay attack */
		try
		{
			this.identifierCache.registerIdentifier(response.getID());
		}
		catch (IdentifierCollisionException e)
		{
			this.logger.warn("ID has already been used. Possible replay attack."); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new InvalidSAMLResponseException("ID has already been used. Possible replay attack", e); //$NON-NLS-1$
		}
	}
}
