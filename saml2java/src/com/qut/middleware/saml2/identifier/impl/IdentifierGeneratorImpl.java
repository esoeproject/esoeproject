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
 * Author: Bradley Beddoes
 * Creation Date: 03/10/2006
 * 
 * Purpose: Generates random identifiers compliant to the IdentiferGenerator interface specification
 */

package com.qut.middleware.saml2.identifier.impl;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;

import com.qut.middleware.saml2.identifier.IdentifierCache;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.identifier.exception.IdentifierCollisionException;
import com.qut.middleware.saml2.identifier.exception.IdentifierGeneratorException;

/** Generates random identifiers compliant to the IdentiferGenerator interface specification. */
public class IdentifierGeneratorImpl implements IdentifierGenerator
{
	private final String XS_ID_DELIM = "_"; //$NON-NLS-1$
	private final String ID_DELIM = "-"; //$NON-NLS-1$
	private final String RNG = "SHA1PRNG"; //$NON-NLS-1$
	
	/* Local logging instance */
	private Logger logger = Logger.getLogger(IdentifierGeneratorImpl.class.getName());
	
	private IdentifierCache cache;
	
	public IdentifierGeneratorImpl(IdentifierCache cache)
	{
		if(cache == null)
		{
			throw new IllegalArgumentException("identifier cache cannot be null."); //$NON-NLS-1$
		}
		this.cache = cache;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.saml2.identifier.IdentifierGenerator#generateSAMLAuthnID()
	 */
	public String generateSAMLAuthnID()
	{
		String id = this.XS_ID_DELIM;
		id = id + generate(20);
		id = id + this.ID_DELIM + generate(16);
		
		this.logger.debug(Messages.getString("IdentifierGeneratorImpl.0") + id); //$NON-NLS-1$
		
		try
		{
			this.cache.registerIdentifier(id);
		}
		catch (IdentifierCollisionException e)
		{
			this.logger.fatal(Messages.getString("IdentifierGeneratorImpl.1")); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new IdentifierGeneratorException(Messages.getString("IdentifierGeneratorImpl.2")); //$NON-NLS-1$
		}

		return id;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.saml2.identifier.IdentifierGenerator#generateSAMLID()
	 */
	public String generateSAMLID()
	{
		String id = this.XS_ID_DELIM;
		id = id + generate(20);
		id = id + this.ID_DELIM + generate(16);
		
		this.logger.debug(Messages.getString("IdentifierGeneratorImpl.3") + id); //$NON-NLS-1$

		try
		{
			this.cache.registerIdentifier(id);
		}
		catch (IdentifierCollisionException e)
		{
			this.logger.fatal(Messages.getString("IdentifierGeneratorImpl.4")); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new IdentifierGeneratorException(Messages.getString("IdentifierGeneratorImpl.5")); //$NON-NLS-1$
		}
		
		return id;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.saml2.identifier.IdentifierGenerator#generateSAMLSessionID()
	 */
	public String generateSAMLSessionID()
	{
		String id = generate(10);
		
		this.logger.debug(Messages.getString("IdentifierGeneratorImpl.6") + id); //$NON-NLS-1$
		
		try
		{
			this.cache.registerIdentifier(id);
		}
		catch (IdentifierCollisionException e)
		{
			this.logger.fatal(Messages.getString("IdentifierGeneratorImpl.7")); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new IdentifierGeneratorException(Messages.getString("IdentifierGeneratorImpl.8")); //$NON-NLS-1$
		}
		
		return id;
	}
	
	/* (non-Javadoc)
	 * @see com.qut.middleware.saml2.identifier.IdentifierGenerator#generateXMLKeyName()
	 */
	public String generateXMLKeyName()
	{
		String id = generate(8);
		
		this.logger.debug(Messages.getString("IdentifierGeneratorImpl.9") + id); //$NON-NLS-1$
				
		return id;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.saml2.identifier.IdentifierGenerator#generateSessionID()
	 */
	public String generateSessionID()
	{
		String id = generate(20);
		id = id + this.ID_DELIM + generate(16);
		id = id + this.ID_DELIM + System.currentTimeMillis();
		
		this.logger.debug(Messages.getString("IdentifierGeneratorImpl.10") + id); //$NON-NLS-1$
		
		try
		{
			this.cache.registerIdentifier(id);
		}
		catch (IdentifierCollisionException e)
		{
			this.logger.fatal(Messages.getString("IdentifierGeneratorImpl.11")); //$NON-NLS-1$
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new IdentifierGeneratorException(Messages.getString("IdentifierGeneratorImpl.12")); //$NON-NLS-1$
		}
		
		return id;
	}

	/**
	 * Generates the specified number of random bytes using SecureRandom
	 * 
	 * @param length
	 *            The number of random bytes to generate
	 * @return The generated random string
	 */
	private String generate(int length)
	{
		SecureRandom random;
		String id;
		byte[] buf;

		try
		{
			/* Attempt to get the specified RNG instance */
			random = SecureRandom.getInstance(this.RNG);
		}
		catch (NoSuchAlgorithmException nsae)
		{
			this.logger.fatal(Messages.getString("IdentifierGeneratorImpl.13")); //$NON-NLS-1$
			this.logger.debug(nsae.getLocalizedMessage(), nsae);
			random = new SecureRandom();
		}

		buf = new byte[length];
		random.nextBytes(buf);
		id = new String(Hex.encodeHex(buf));

		return id;
	}

}
