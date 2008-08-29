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
 * Author: Shaun Mangelsdorf
 * Creation Date: 10/10/2006
 *
 * Purpose: Maps the attributes of an LDAP user to the User object.
 */
package com.qut.middleware.esoe.identity.plugins.ldap.mapper.impl;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.AttributesMapper;

import com.qut.middleware.esoe.identity.plugins.ldap.mapper.User;
import com.qut.middleware.esoe.sessions.Messages;
import com.qut.middleware.esoe.sessions.bean.SessionConfigData;
import com.qut.middleware.saml2.schemas.esoe.sessions.AttributeType;
import com.qut.middleware.saml2.schemas.esoe.sessions.HandlerType;
import com.qut.middleware.saml2.schemas.esoe.sessions.IdentityType;


/** Maps the attributes of an LDAP user to the User object. */
public class UserAttributeMapper implements AttributesMapper
{
	private User user;
	private SessionConfigData sessionConfigData;
	private String handlerName;
	
	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(UserAttributeMapper.class.getName());

	/**
	 * Constructor
	 * 
	 * @param user
	 *            The user object to populate with the attribute data.
	 * @param sessionConfigData
	 *            The session data to use for resolving attributes.
	 * @param handlerName
	 *            The handler we are mapping attributes on behalf of
	 */
	public UserAttributeMapper(User user, SessionConfigData sessionConfigData, String handlerName)
	{
		if (user == null)
		{
			throw new IllegalArgumentException(Messages.getString("UserAttributeMapper.UserObjectNull")); //$NON-NLS-1$
		}
		if (sessionConfigData == null)
		{
			throw new IllegalArgumentException(Messages.getString("UserAttributeMapper.4")); //$NON-NLS-1$
		}
		if (handlerName == null)
		{
			throw new IllegalArgumentException(Messages.getString("UserAttributeMapper.5")); //$NON-NLS-1$
		}
		
		this.user = user;
		this.sessionConfigData = sessionConfigData;
		this.handlerName = handlerName;
		
		this.logger.debug(Messages.getString("UserAttributeMapper.0")); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.ldap.AttributesMapper#mapFromAttributes(javax.naming.directory.Attributes)
	 */
	public Object mapFromAttributes(Attributes attributes) throws NamingException
	{
		this.logger.debug(Messages.getString("UserAttributeMapper.1")); //$NON-NLS-1$
		
		/*
		 * For each attribute returned from LDAP, we need to find out if there is a mapping from that attribute to an
		 * attribute defined locally. If not, we need not worry about populating it.
		 */
		NamingEnumeration<? extends Attribute> attributeEnumeration = attributes.getAll();
		while (attributeEnumeration.hasMore())
		{
			// Get the attribute.
			Attribute a = attributeEnumeration.next();
			String localAttributeName = a.getID();

			this.logger.debug(MessageFormat.format(Messages.getString("UserAttributeMapper.2"), localAttributeName)); //$NON-NLS-1$

			List<String> attributeNameList = findLocalAttribute(localAttributeName);

			NamingEnumeration<?> valueEnumeration = a.getAll();
			while (valueEnumeration.hasMore())
			{
				Object value = valueEnumeration.next();

				Iterator<String> attributeNameIterator = attributeNameList.iterator();
				while (attributeNameIterator.hasNext())
				{
					String attributeName = attributeNameIterator.next();
					// It's up to the DAO to decide whether it wants to *use* the attribute or not
					this.user.addAttributeValue(attributeName, value);
				}
			}
		}

		return this.user;
	}

	/**
	 * Finds all attributes that are mapped from the local identifier given.
	 */
	private List<String> findLocalAttribute(String localAttributeName)
	{
		List<String> attributeNames = new Vector<String>(0, 1);

		// For each identity in the session config, we need to find the attribute names that have
		// localAttributeName mapped
		List<IdentityType> identityList = this.sessionConfigData.getIdentity();
		Iterator<IdentityType> identityIterator = identityList.iterator();

		while (identityIterator.hasNext())
		{
			IdentityType identity = identityIterator.next();
			List<AttributeType> attributeList = identity.getAttributes();
			Iterator<AttributeType> attributeIterator = attributeList.iterator();

			while (attributeIterator.hasNext())
			{
				AttributeType attribute = attributeIterator.next();
				List<HandlerType> handlerList = attribute.getHandlers();
				Iterator<HandlerType> handlerIterator = handlerList.iterator();

				while (handlerIterator.hasNext())
				{
					HandlerType handler = handlerIterator.next();

					// If the handler that owns this mapper is responsible for the attribute,
					// and the attribute matches the local name given, add it to the list.
					if (this.handlerName.equals(handler.getName())
							&& localAttributeName.equalsIgnoreCase(handler.getLocalIdentifier()))
					{
						this.logger.debug(MessageFormat.format(Messages.getString("UserAttributeMapper.3"), localAttributeName, handler.getLocalIdentifier())); //$NON-NLS-1$
						attributeNames.add(attribute.getIdentifier());
					}
				}
			}
		}

		return attributeNames;
	}
}
