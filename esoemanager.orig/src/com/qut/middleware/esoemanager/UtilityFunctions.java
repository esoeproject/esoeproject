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
 * Creation Date: 1/5/07
 * 
 * Purpose: Generalised utility functions
 */
package com.qut.middleware.esoemanager;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.saml2.identifier.impl.Messages;
import com.qut.middleware.saml2.schemas.metadata.AttributeAuthorityDescriptor;
import com.qut.middleware.saml2.schemas.metadata.AuthnAuthorityDescriptor;
import com.qut.middleware.saml2.schemas.metadata.IDPSSODescriptor;
import com.qut.middleware.saml2.schemas.metadata.PDPDescriptor;
import com.qut.middleware.saml2.schemas.metadata.SPSSODescriptor;
import com.qut.middleware.saml2.schemas.metadata.lxacml.LXACMLPDPDescriptor;

public class UtilityFunctions
{
	/* Utilised secure rng algorithm */
	private final String RNG = "SHA1PRNG"; //$NON-NLS-1$
	
	/* Maximum random value to return */
	private final int MAX_VALUE = 65535;
	
	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(UtilityFunctions.class.getName());

	/**
	 * Returns RoleDescriptorType class name for corresponding database id
	 * 
	 * @param idNumber
	 * @return
	 */
	public String getRoleDescriptorType(String idNumber)
	{
		if (idNumber.equals(Constants.IDP_DESCRIPTOR)) //$NON-NLS-1$
		{
			return IDPSSODescriptor.class.getCanonicalName();
		}
		else
			if (idNumber.equals(Constants.SP_DESCRIPTOR)) //$NON-NLS-1$
			{
				return SPSSODescriptor.class.getCanonicalName();
			}
			else
				if (idNumber.equals(Constants.LXACML_PDP_DESCRIPTOR)) //$NON-NLS-1$
				{
					return PDPDescriptor.class.getCanonicalName();
				}
				else
					if (idNumber.equals(Constants.AUTHN_AUTHORITY_DESCRIPTOR)) //$NON-NLS-1$
					{
						return AuthnAuthorityDescriptor.class.getCanonicalName();
					}
					else
						if (idNumber.equals(Constants.ATTRIBUTE_AUTHORITY_DESCRIPTOR)) //$NON-NLS-1$
						{
							return AttributeAuthorityDescriptor.class.getCanonicalName();
						}
						else
						{
							return null;
						}
	}

	/**
	 * Returns the database id number for any RoleDescriptorType
	 * 
	 * @param className
	 *            of the descriptor being used
	 * @return Value of the descriptor object, or null on error
	 */
	public String getRoleDescriptorTypeId(String className)
	{
		if (className.equalsIgnoreCase(IDPSSODescriptor.class.getCanonicalName()))
		{
			return Constants.IDP_DESCRIPTOR;
		}
		else
			if (className.equalsIgnoreCase(SPSSODescriptor.class.getCanonicalName()))
			{
				return Constants.SP_DESCRIPTOR;
			}
			else
				if (className.equalsIgnoreCase(PDPDescriptor.class.getCanonicalName()))
				{
					return Constants.LXACML_PDP_DESCRIPTOR;
				}
				else
					if (className.equalsIgnoreCase(LXACMLPDPDescriptor.class.getCanonicalName()))
					{
						return Constants.LXACML_PDP_DESCRIPTOR;
					}
					else
						if (className.equalsIgnoreCase(AuthnAuthorityDescriptor.class.getCanonicalName()))
						{
							return Constants.AUTHN_AUTHORITY_DESCRIPTOR;
						}
						else
							if (className.equalsIgnoreCase(AttributeAuthorityDescriptor.class.getCanonicalName()))
							{
								return Constants.ATTRIBUTE_AUTHORITY_DESCRIPTOR;
							}
		return null;
	}

	/**
	 * Generates a random number using SecureRandom, will return a value capable of being used for SAML indexes
	 * 
	 * @return The generated random string
	 */
	public String generateID()
	{
		SecureRandom random;
		Integer id;

		try
		{
			/* Attempt to get the specified RNG instance */
			random = SecureRandom.getInstance(this.RNG);
		}
		catch (NoSuchAlgorithmException nsae)
		{
			this.logger.error(Messages.getString("IdentifierGeneratorImpl.13")); //$NON-NLS-1$
			this.logger.debug(nsae.getLocalizedMessage(), nsae);
			random = new SecureRandom();
		}
		
		int randVal = random.nextInt(this.MAX_VALUE);
		id = new Integer(randVal);

		return id.toString();
	}
}
