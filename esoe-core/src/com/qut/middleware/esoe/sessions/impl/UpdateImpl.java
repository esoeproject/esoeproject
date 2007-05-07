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
 * Author: Shaun Mangelsdorf
 * Creation Date: 06/10/2006
 * 
 * Purpose: Implements the Update interface.
 */
package com.qut.middleware.esoe.sessions.impl;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.qut.middleware.esoe.authn.bean.AuthnIdentityAttribute;
import com.qut.middleware.esoe.log4j.InsaneLogLevel;
import com.qut.middleware.esoe.sessions.Messages;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.Update;
import com.qut.middleware.esoe.sessions.bean.IdentityAttribute;
import com.qut.middleware.esoe.sessions.bean.impl.IdentityAttributeImpl;
import com.qut.middleware.esoe.sessions.cache.SessionCache;
import com.qut.middleware.esoe.sessions.exception.DuplicateSessionException;
import com.qut.middleware.esoe.sessions.exception.InvalidDescriptorIdentifierException;
import com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException;
import com.qut.middleware.saml2.schemas.esoe.sessions.DataType;

/** */
public class UpdateImpl implements Update
{
	private SessionCache cache;

	/* Local logging instance */
	private Logger logger = Logger.getLogger(UpdateImpl.class.getName());

	/**
	 * Constructor.
	 * 
	 * @param cache
	 *            The session cache to be used.
	 */
	public UpdateImpl(SessionCache cache)
	{
		if (cache == null)
		{
			throw new IllegalArgumentException(Messages.getString("UpdateImpl.SessionCacheNull")); //$NON-NLS-1$
		}
		this.cache = cache;

		this.logger.info(Messages.getString("UpdateImpl.0")); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.Update#updateEntityList(java.lang.String, java.lang.String)
	 */
	public void updateDescriptorList(String sessionID, String descriptorID) throws InvalidSessionIdentifierException
	{
		Principal principal = this.getPrincipal(sessionID);

		principal.addActiveDescriptor(descriptorID);

		this.logger.log(InsaneLogLevel.INSANE, MessageFormat.format(
				Messages.getString("UpdateImpl.1"), descriptorID, sessionID)); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.Update#updateEntitySessionIdentifierList(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void updateDescriptorSessionIdentifierList(String sessionID, String descriptorID, String descriptorSessionID)
			throws InvalidSessionIdentifierException, InvalidDescriptorIdentifierException
	{
		Principal principal = this.getPrincipal(sessionID);

		principal.addDescriptorSessionIdentifier(descriptorID, descriptorSessionID);

		this.logger.log(InsaneLogLevel.INSANE, MessageFormat.format(
				Messages.getString("UpdateImpl.2"), descriptorSessionID, descriptorID, sessionID)); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.Update#updateSAMLAuthnIdentifier(java.lang.String, java.lang.String)
	 */
	public void updateSAMLAuthnIdentifier(String sessionID, String samlID) throws DuplicateSessionException,
			InvalidSessionIdentifierException
	{
		Principal principal = this.getPrincipal(sessionID);

		principal.setSAMLAuthnIdentifier(samlID);

		this.cache.updateSessionSAMLID(principal);

		this.logger.log(InsaneLogLevel.INSANE, MessageFormat.format(
				Messages.getString("UpdateImpl.3"), sessionID, samlID)); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoe.sessions.Update#updatePrincipalAttributes(java.lang.String, java.util.List)
	 */
	public void updatePrincipalAttributes(String sessionID, List<AuthnIdentityAttribute> authnIdentityAttributes) throws InvalidSessionIdentifierException
	{
		Principal principal = this.getPrincipal(sessionID);
		
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
						this.logger.error(Messages.getString("UpdateImpl.6")); //$NON-NLS-1$
				}
			}
		}
	}

	/**
	 * @param sessionID
	 *            Principals sessionID
	 * @return Principal Object
	 * @throws InvalidSessionIdentifierException
	 */
	private Principal getPrincipal(String sessionID) throws InvalidSessionIdentifierException
	{
		Principal principal;

		// Get the principal object from the cache.
		principal = this.cache.getSession(sessionID);
		if (principal == null)
		{
			this.logger.warn(MessageFormat.format(Messages.getString("UpdateImpl.4"), sessionID)); //$NON-NLS-1$
			throw new InvalidSessionIdentifierException();
		}

		this.logger.log(InsaneLogLevel.INSANE, MessageFormat.format(
				Messages.getString("UpdateImpl.5"), principal.getPrincipalAuthnIdentifier(), sessionID)); //$NON-NLS-1$
		return principal;
	}
}
