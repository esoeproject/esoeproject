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

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;

import com.qut.middleware.saml2.VersionConstants;
import com.qut.middleware.saml2.exception.InvalidSAMLRequestException;
import com.qut.middleware.saml2.identifier.IdentifierCache;
import com.qut.middleware.saml2.identifier.exception.IdentifierCollisionException;
import com.qut.middleware.saml2.schemas.protocol.RequestAbstractType;
import com.qut.middleware.saml2.validator.SAMLRequestValidator;

public class SAMLRequestValidatorImpl implements SAMLRequestValidator
{
	private int allowedTimeSkew;
	private IdentifierCache identifierCache;
	
	/* Local logging instance */
	private Logger logger = Logger.getLogger(SAMLRequestValidatorImpl.class.getName());

	/**
	 * @param identifierCache An implementation of IdentifierCache through which SAML ID's can be verified against for uniqueness
	 * @param allowedTimeSkew Time skew in seconds thats documents may slip
	 */
	public SAMLRequestValidatorImpl(IdentifierCache identifierCache, int allowedTimeSkew)
	{
		if( identifierCache == null )
		{
			this.logger.fatal(Messages.getString("SAMLRequestValidatorImpl.3"));  //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("SAMLRequestValidatorImpl.3")); //$NON-NLS-1$
		}
		
		if(allowedTimeSkew > Integer.MAX_VALUE / 1000)
		{
			this.logger.fatal(Messages.getString("SAMLRequestValidatorImpl.5")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("SAMLRequestValidatorImpl.5")); //$NON-NLS-1$
		}
		
		this.allowedTimeSkew = allowedTimeSkew * 1000; /* internally work in milliseconds */
		this.identifierCache = identifierCache;
		
		this.logger.info(Messages.getString("SAMLRequestValidatorImpl.8")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.saml2.handler.SAMLRequestValidator#validate(com.qut.middleware.saml2.schemas.protocol.RequestAbstractType)
	 *
	 * NOTE: xml timestamps are assumued to be UTC+0. Validation will fail if the sender has created
	 * the request with a different timezone.
	 */
	public void validate(RequestAbstractType request) throws InvalidSAMLRequestException
	{
		if( request == null )
		{
			this.logger.error(Messages.getString("SAMLRequestValidatorImpl.9")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("SAMLRequestValidatorImpl.10")); //$NON-NLS-1$
		}
		
		if(request.getID() == null)
		{
			this.logger.warn(Messages.getString("SAMLRequestValidatorImpl.6")); //$NON-NLS-1$
			throw new InvalidSAMLRequestException(Messages.getString("SAMLRequestValidatorImpl.6")); //$NON-NLS-1$
		}
		
		/* Get the time that this document was generated. */
		if( request.getIssueInstant() == null)
		{
			this.logger.warn(Messages.getString("SAMLRequestValidatorImpl.7")); //$NON-NLS-1$
			throw new InvalidSAMLRequestException(Messages.getString("SAMLRequestValidatorImpl.7")); //$NON-NLS-1$
		}
		
		XMLGregorianCalendar issueInstant = request.getIssueInstant();
		GregorianCalendar issuerCalendar = issueInstant.toGregorianCalendar();
		long documentTime = issuerCalendar.getTimeInMillis();
		
		/* current time is current UTC time, not localized time */
		SimpleTimeZone utc = new SimpleTimeZone(0, "UTC"); //$NON-NLS-1$
		Calendar thisCalendar = new GregorianCalendar(utc);
		long currentTime = thisCalendar.getTimeInMillis();
		
		/* Make sure that the time doesn't differ by any more than the amount of clock skew we are allowed. */
		if(Math.abs(currentTime - documentTime) > this.allowedTimeSkew)
		{
			this.logger.debug(MessageFormat.format(Messages.getString("SAMLRequestValidatorImpl.12"), documentTime, currentTime) ); //$NON-NLS-1$
			this.logger.warn(Messages.getString("SAMLRequestValidatorImpl.0")); //$NON-NLS-1$
			throw new InvalidSAMLRequestException(Messages.getString("SAMLRequestValidatorImpl.0")); //$NON-NLS-1$
		}
		
		/* Ensure that an issuer has been provided for this request */
		if( request.getIssuer() == null || request.getIssuer().getValue() == null || request.getIssuer().getValue().length() <= 0 )
		{
			this.logger.warn(Messages.getString("SAMLRequestValidatorImpl.4")); //$NON-NLS-1$
			throw new InvalidSAMLRequestException(Messages.getString("SAMLRequestValidatorImpl.4")); //$NON-NLS-1$
		}
		
		/* Check the version of the SAML document. */
		if (!request.getVersion().equals(VersionConstants.saml20))
		{
			this.logger.warn(Messages.getString("SAMLRequestValidatorImpl.1")); //$NON-NLS-1$
			throw new InvalidSAMLRequestException(Messages.getString("SAMLRequestValidatorImpl.1")); //$NON-NLS-1$
		}
		
		/* Make sure the identifier hasn't already been used and we aren't seeing an attempted replay attack */
		try
		{
			this.identifierCache.registerIdentifier(request.getID());
		}
		catch (IdentifierCollisionException e)
		{
			this.logger.warn(Messages.getString("SAMLRequestValidatorImpl.11")); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new InvalidSAMLRequestException(Messages.getString("SAMLRequestValidatorImpl.2"), e); //$NON-NLS-1$
		}
	}

}
