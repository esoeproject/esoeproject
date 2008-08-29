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
 * Purpose: Implementation to validate Assertion element to SAML 2.0 requirements
 */
package com.qut.middleware.saml2.validator.impl;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.saml2.VersionConstants;
import com.qut.middleware.saml2.exception.InvalidSAMLAssertionException;
import com.qut.middleware.saml2.identifier.IdentifierCache;
import com.qut.middleware.saml2.identifier.exception.IdentifierCollisionException;
import com.qut.middleware.saml2.schemas.assertion.Assertion;
import com.qut.middleware.saml2.schemas.assertion.AudienceRestriction;
import com.qut.middleware.saml2.schemas.assertion.SubjectConfirmation;
import com.qut.middleware.saml2.schemas.assertion.SubjectConfirmationDataType;
import com.qut.middleware.saml2.validator.SAMLAssertionValidator;

/** Implementation to validate Assertion element to SAML 2.0 requirements */
public class SAMLAssertionValidatorImpl implements SAMLAssertionValidator
{
	private int allowedTimeSkew;
	private IdentifierCache identifierCache;

	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(SAMLAssertionValidatorImpl.class.getName());

	SAMLAssertionValidatorImpl(IdentifierCache identifierCache, int allowedTimeSkew)
	{
		if (identifierCache == null)
		{
			this.logger.error(Messages.getString("SAMLRequestValidatorImpl.3")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("SAMLRequestValidatorImpl.3")); //$NON-NLS-1$
		}

		if (allowedTimeSkew > Integer.MAX_VALUE / 1000)
		{
			this.logger.error(Messages.getString("SAMLRequestValidatorImpl.5")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("SAMLRequestValidatorImpl.5")); //$NON-NLS-1$
		}

		this.allowedTimeSkew = allowedTimeSkew * 1000; /* internally work in milliseconds */
		this.identifierCache = identifierCache;

		this.logger.info(Messages.getString("SAMLAssertionValidatorImpl.0")); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.saml2.validator.SAMLAssertionValidator#validate(com.qut.middleware.saml2.schemas.assertion.Assertion)
	 */
	public void validate(Assertion assertion) throws InvalidSAMLAssertionException
	{
		if (assertion == null)
		{
			this.logger.error(Messages.getString("SAMLAssertionValidatorImpl.1")); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("SAMLAssertionValidatorImpl.2")); //$NON-NLS-1$
		}

		if (assertion.getID() == null)
		{
			this.logger.warn(Messages.getString("SAMLAssertionValidatorImpl.3")); //$NON-NLS-1$
			throw new InvalidSAMLAssertionException(Messages.getString("SAMLAssertionValidatorImpl.4")); //$NON-NLS-1$
		}

		if (!assertion.getVersion().equals(VersionConstants.saml20))
		{
			this.logger.warn(Messages.getString("SAMLAssertionValidatorImpl.5")); //$NON-NLS-1$
			throw new InvalidSAMLAssertionException(Messages.getString("SAMLAssertionValidatorImpl.6")); //$NON-NLS-1$
		}

		if (assertion.getIssuer() == null || assertion.getIssuer().getValue() == null
				|| assertion.getIssuer().getValue().length() <= 0)
		{
			this.logger.warn(Messages.getString("SAMLAssertionValidatorImpl.7")); //$NON-NLS-1$
			throw new InvalidSAMLAssertionException(Messages.getString("SAMLAssertionValidatorImpl.8")); //$NON-NLS-1$
		}

		if (assertion.getSubject() == null
				|| (assertion.getSubject().getNameID() == null && assertion.getSubject().getEncryptedID() == null))
		{
			this.logger.warn(Messages.getString("SAMLAssertionValidatorImpl.9")); //$NON-NLS-1$
			throw new InvalidSAMLAssertionException(Messages.getString("SAMLAssertionValidatorImpl.10")); //$NON-NLS-1$
		}

		/* Processes subjectconfirmation fields */
		for (SubjectConfirmation confirmation : assertion.getSubject().getSubjectConfirmationNonID())
		{
			SubjectConfirmationDataType confirmationData = confirmation.getSubjectConfirmationData();
			
			if (!(confirmationData.getNotOnOrAfter() == null))
			{
				XMLGregorianCalendar issueInstant = confirmationData.getNotOnOrAfter();

				GregorianCalendar issuerCalendar = issueInstant.toGregorianCalendar();
				long documentTime = issuerCalendar.getTimeInMillis();

				// current time is current UTC time, not localized time
				SimpleTimeZone utc = new SimpleTimeZone(0, "UTC"); //$NON-NLS-1$
				Calendar thisCalendar = new GregorianCalendar(utc);
				long currentTime = thisCalendar.getTimeInMillis();

				/* Make sure that the time doesn't differ by any more than the amount of clock skew we are allowed. */
				if (currentTime > documentTime)
				{
					this.logger.warn(Messages.getString("SAMLAssertionValidatorImpl.13")); //$NON-NLS-1$
					throw new InvalidSAMLAssertionException(Messages.getString("SAMLAssertionValidatorImpl.14")); //$NON-NLS-1$
				}
			}

			if (!(confirmationData.getInResponseTo() == null))
			{
				/* Make sure the identifier was issued locally */
				if (!this.identifierCache.containsIdentifier(confirmationData.getInResponseTo()))
				{
					this.logger.warn(Messages.getString("SAMLAssertionValidatorImpl.19")); //$NON-NLS-1$
					throw new InvalidSAMLAssertionException(Messages.getString("SAMLAssertionValidatorImpl.20")); //$NON-NLS-1$
				}
			}
		}

		if (assertion.getConditions() == null)
		{
			this.logger.warn(Messages.getString("SAMLAssertionValidatorImpl.15")); //$NON-NLS-1$
			throw new InvalidSAMLAssertionException(Messages.getString("SAMLAssertionValidatorImpl.16")); //$NON-NLS-1$
		}

		boolean audienceRestrictionPresent = false;
		for (Object condition : assertion.getConditions().getConditionsAndOneTimeUsesAndAudienceRestrictions())
		{
			if (condition instanceof AudienceRestriction)
				audienceRestrictionPresent = true;
		}

		if (!audienceRestrictionPresent)
		{
			this.logger.warn(Messages.getString("SAMLAssertionValidatorImpl.17")); //$NON-NLS-1$
			throw new InvalidSAMLAssertionException(Messages.getString("SAMLAssertionValidatorImpl.18")); //$NON-NLS-1$
		}

		/* Make sure the identifier hasn't already been used and we aren't seeing an attempted replay attack */
		try
		{
			this.identifierCache.registerIdentifier(assertion.getID());
		}
		catch (IdentifierCollisionException e)
		{
			this.logger.warn(Messages.getString("SAMLAssertionValidatorImpl.21")); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new InvalidSAMLAssertionException(Messages.getString("SAMLAssertionValidatorImpl.22"), e); //$NON-NLS-1$
		}
	}
}
