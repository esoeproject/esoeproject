/*
 * Copyright 2006, Queensland University of Technology Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Author: Shaun Mangelsdorf 
 * Creation Date: 02/10/2006
 * 
 * Purpose: Implements the Create interface
 */

package com.qut.middleware.esoe.sessions.impl;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.authn.bean.AuthnIdentityAttribute;
import com.qut.middleware.esoe.sessions.Create;
import com.qut.middleware.esoe.sessions.Messages;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.bean.IdentityAttribute;
import com.qut.middleware.esoe.sessions.bean.IdentityData;
import com.qut.middleware.esoe.sessions.bean.SessionConfigData;
import com.qut.middleware.esoe.sessions.bean.impl.IdentityAttributeImpl;
import com.qut.middleware.esoe.sessions.bean.impl.IdentityDataImpl;
import com.qut.middleware.esoe.sessions.cache.SessionCache;
import com.qut.middleware.esoe.sessions.exception.DataSourceException;
import com.qut.middleware.esoe.sessions.exception.DuplicateSessionException;
import com.qut.middleware.esoe.sessions.exception.HandlerRegistrationException;
import com.qut.middleware.esoe.sessions.identity.IdentityResolver;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.schemas.assertion.AttributeType;
import com.qut.middleware.saml2.schemas.esoe.sessions.DataType;

/**
 * Implements the Create interface.
 */
public class CreateImpl implements Create
{
	private SessionCache cache;
	private SessionConfigData data;
	private IdentityResolver resolver;
	private IdentifierGenerator identifierGenerator;
	private int sessionLength;

	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(CreateImpl.class.getName());
	private Logger authnLogger = LoggerFactory.getLogger(ConfigurationConstants.authnLogger);

	/**
	 * Constructor
	 * 
	 * @param cache
	 *            The session cache to use.
	 * @param data
	 *            The session data from the configuration.
	 * @param resolver
	 *            The identity resolver object to use.
	 * @param sessionLength
	 *            The time until which the principal session on remote SPEP can be considered active
	 */
	public CreateImpl(SessionCache cache, SessionConfigData data, IdentityResolver resolver,
			IdentifierGenerator identifierGenerator, int sessionLength)
	{
		if (cache == null)
		{
			throw new IllegalArgumentException(Messages.getString("CreateImpl.SessionCacheNull")); //$NON-NLS-1$
		}
		if (data == null)
		{
			throw new IllegalArgumentException(Messages.getString("CreateImpl.SessionDataNull")); //$NON-NLS-1$
		}
		if (resolver == null)
		{
			throw new IllegalArgumentException(Messages.getString("CreateImpl.IdentityResolverNull")); //$NON-NLS-1$
		}
		if (identifierGenerator == null)
		{
			throw new IllegalArgumentException(Messages.getString("CreateImpl.7")); //$NON-NLS-1$
		}

		this.cache = cache;
		this.data = data;
		this.resolver = resolver;
		this.identifierGenerator = identifierGenerator;
		this.sessionLength = sessionLength;

		this.logger.info(Messages.getString("CreateImpl.0")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sessions.Create#createLocalSession(java.lang.String, java.lang.String, java.lang.String, java.util.List)
	 */
	public Create.result createLocalSession(String sessionID, String principalAuthnIdentifier, String authenticationContextClass,
			List<AuthnIdentityAttribute> authnIdentityAttributes) throws DataSourceException, DuplicateSessionException
	{
		this.logger.debug(MessageFormat.format(
				Messages.getString("CreateImpl.1"), sessionID, principalAuthnIdentifier, authenticationContextClass)); //$NON-NLS-1$

		String SAMLAuthnID;

		// Resolve attributes and set up the IdentityData object
		IdentityData identityData = new IdentityDataImpl();
		identityData.setPrincipalAuthnIdentifier(principalAuthnIdentifier);
		identityData.setSessionID(sessionID);
		identityData.setIdentity(this.data.getIdentity());

		try
		{
			this.logger.debug(MessageFormat.format(Messages.getString("CreateImpl.2"), sessionID)); //$NON-NLS-1$
			this.resolver.execute(identityData);
		}
		catch (HandlerRegistrationException ex)
		{
			throw new DataSourceException(ex);
		}

		identityData.setCurrentHandler(null);

		this.logger.info(MessageFormat.format(Messages.getString("CreateImpl.3"), sessionID)); //$NON-NLS-1$

		// Create the principal
		Principal principal = new PrincipalImpl(identityData, this.sessionLength);
		principal.setSessionID(identityData.getSessionID());
		principal.setPrincipalAuthnIdentifier(identityData.getPrincipalAuthnIdentifier());

		principal.setAuthnTimestamp(System.currentTimeMillis());
		principal.setAuthenticationContextClass(authenticationContextClass);

		SAMLAuthnID = this.identifierGenerator.generateSAMLAuthnID();
		principal.setSAMLAuthnIdentifier(SAMLAuthnID);
		this.authnLogger.info(Messages.getString("CreateImpl.8") + principal.getPrincipalAuthnIdentifier() + Messages.getString("CreateImpl.9") + SAMLAuthnID); //$NON-NLS-1$ //$NON-NLS-2$

		/*
		 * Principal is created and identity information setup from backend stores, add dynamically specified data from
		 * the authn handler to allow authz decisions to be made on authn type, provides for n-level authn and
		 * higher level security domains
		 */
		if (authnIdentityAttributes != null)
		{
			for (AuthnIdentityAttribute attrib : authnIdentityAttributes)
			{
				/* Insert new identity information into principal */
				if (!principal.getAttributes().containsKey(attrib.getName()))
				{
					List<Object> attributes;
					IdentityAttribute idAttrib = new IdentityAttributeImpl();
					idAttrib.setType(DataType.STRING.name());
					attributes = Collections.synchronizedList(idAttrib.getValues());
					for (String value : attrib.getValues())
					{
						attributes.add(value);
					}

					principal.getAttributes().put(attrib.getName(), idAttrib);
				}
				else
				{
					/* Append values to identity information if they don't already exist */
					IdentityAttribute attribute = principal.getAttributes().get(attrib.getName());
					if (attribute.getType().equals(DataType.STRING.name()))
					{
						for (String value : attrib.getValues())
						{
							if (!attribute.getValues().contains(value))
								attribute.getValues().add(value);
						}
					}
					else
						this.logger.error(Messages.getString("CreateImpl.6")); //$NON-NLS-1$
				}
			}
		}

		// Add the session to the local cache
		this.cache.addSession(principal);

		if (this.cache.getSession(sessionID) == null)
		{
			this.logger.error(MessageFormat.format(Messages.getString("CreateImpl.4"), sessionID)); //$NON-NLS-1$
			throw new DataSourceException(Messages.getString("CreateImpl.NewSessionNull")); //$NON-NLS-1$
		}

		// Ensure principal is accessible via SAMLID or SessionID
		this.cache.updateSessionSAMLID(principal);

		this.logger.debug(MessageFormat.format(Messages.getString("CreateImpl.5"), sessionID)); //$NON-NLS-1$

		return Create.result.SessionCreated;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.Create#createDelegatedSession(java.lang.String, java.lang.String,
	 *      java.lang.String, java.util.List)
	 */
	public result createDelegatedSession(String sessionID, String principalAuthnIdentifier,	String authenticationContextClass, List<AttributeType> principalAttributes) throws DataSourceException, DuplicateSessionException
	{
		/* Directly create the principal object from incoming request,
		 * not there is no need to populate IdentityData directly
		 */
		Principal principal = new PrincipalImpl(this.sessionLength);
		principal.setSessionID(sessionID);
		principal.setPrincipalAuthnIdentifier(principalAuthnIdentifier);
		
		principal.setAuthnTimestamp(System.currentTimeMillis());
		principal.setAuthenticationContextClass(authenticationContextClass);

		String SAMLAuthnID = this.identifierGenerator.generateSAMLAuthnID();
		principal.setSAMLAuthnIdentifier(SAMLAuthnID);
		this.authnLogger.info("SSO identifier established for REMOTE principal " + principal.getPrincipalAuthnIdentifier() + Messages.getString("CreateImpl.9") + SAMLAuthnID); //$NON-NLS-2$
		
		for(AttributeType attrib : principalAttributes)
		{
			/* The casting will take care of itself here */
			IdentityAttribute localAttrib = new IdentityAttributeImpl();
			for(Object value : attrib.getAttributeValues())
				localAttrib.addValue(value);
			
			principal.putAttribute(attrib.getName(), localAttrib);
		}

		// Add the session to the local cache
		this.cache.addSession(principal);

		if (this.cache.getSession(sessionID) == null)
		{
			this.logger.error(MessageFormat.format(Messages.getString("CreateImpl.4"), sessionID)); //$NON-NLS-1$
			throw new DataSourceException(Messages.getString("CreateImpl.NewSessionNull")); //$NON-NLS-1$
		}

		// Ensure principal is accessible via SAMLID or SessionID
		this.cache.updateSessionSAMLID(principal);

		this.logger.debug(MessageFormat.format(Messages.getString("CreateImpl.5"), sessionID)); //$NON-NLS-1$

		return Create.result.SessionCreated;
	}
}
