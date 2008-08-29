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
 * Creation Date: 28/09/2006
 * 
 * Purpose: Implements the IdentityResolver interface to resolve identities
 * 		using supplied handlers.
 */
package com.qut.middleware.esoe.sessions.identity.impl;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.esoe.sessions.Messages;
import com.qut.middleware.esoe.sessions.bean.IdentityAttribute;
import com.qut.middleware.esoe.sessions.bean.IdentityData;
import com.qut.middleware.esoe.sessions.bean.impl.IdentityAttributeImpl;
import com.qut.middleware.esoe.sessions.exception.DataSourceException;
import com.qut.middleware.esoe.sessions.exception.DuplicateSessionException;
import com.qut.middleware.esoe.sessions.exception.HandlerRegistrationException;
import com.qut.middleware.esoe.sessions.identity.IdentityResolver;
import com.qut.middleware.esoe.sessions.identity.pipeline.Handler;
import com.qut.middleware.saml2.schemas.esoe.sessions.AttributeType;
import com.qut.middleware.saml2.schemas.esoe.sessions.IdentityType;

/** */
public class IdentityResolverImpl implements IdentityResolver
{
	private List<Handler> handlers;
	
	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(IdentityResolverImpl.class.getName());

	/**
	 * Constructor
	 * @param handlers The list of handlers to use for resolving identity.
	 */
	public IdentityResolverImpl(List<Handler> handlers)
	{
		if (handlers == null)
		{
			throw new IllegalArgumentException(Messages.getString("IdentityResolverImpl.0")); //$NON-NLS-1$
		}
		this.handlers = Collections.synchronizedList(handlers);
		
		this.logger.info(MessageFormat.format(Messages.getString("IdentityResolverImpl.1"), Integer.toString(handlers.size()))); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.identity.IdentityResolver#registerHandler(com.qut.middleware.esoe.sessions.identity.pipeline.Handler)
	 */
	public void registerHandler(Handler handler)
	{
		if (handler == null)
		{
			throw new IllegalArgumentException(Messages.getString("IdentityResolverImpl.HandlerNull")); //$NON-NLS-1$
		}
		this.handlers.add(handler);
		
		this.logger.info(MessageFormat.format(Messages.getString("IdentityResolverImpl.2"), handler.getHandlerName())); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.identity.IdentityResolver#getRegisteredHandlers()
	 */
	public List<Handler> getRegisteredHandlers()
	{
		return this.handlers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.identity.IdentityResolver#execute(com.qut.middleware.esoe.sessions.bean.IdentityData)
	 */
	public void execute(IdentityData data) throws DataSourceException, HandlerRegistrationException,
			DuplicateSessionException
	{
		this.logger.debug(MessageFormat.format(Messages.getString("IdentityResolverImpl.3"), data.getPrincipalAuthnIdentifier())); //$NON-NLS-1$
		
		int attributeCount = 0;

		// Populate the list of identities to be resolved.
		List<IdentityType> identities = data.getIdentity();
		
		if(identities != null)
		{
			Iterator<IdentityType> identityIterator = identities.iterator();
			while (identityIterator.hasNext())
			{
				IdentityType identity = identityIterator.next();
				List<AttributeType> attributes = identity.getAttributes();
	
				Iterator<AttributeType> attributeIterator = attributes.iterator();
				while (attributeIterator.hasNext())
				{
					AttributeType attribute = attributeIterator.next();
					String identifier = attribute.getIdentifier();
	
					IdentityAttribute identityAttribute = data.getAttributes().get(identifier);
	
					// If the attribute hasn't already been added, add it with the given type
					if (identityAttribute == null)
					{
						identityAttribute = new IdentityAttributeImpl();
						identityAttribute.setType(attribute.getType().name());
						
						attributeCount++;
	
						data.getAttributes().put(identifier, identityAttribute);
					}
				}
			}
		}
		
		this.logger.debug(MessageFormat.format(Messages.getString("IdentityResolverImpl.4"), Integer.toString(attributeCount))); //$NON-NLS-1$

		Iterator<Handler> handlerIterator = this.handlers.iterator();
		if (!handlerIterator.hasNext())
		{
			this.logger.error(Messages.getString("IdentityResolverImpl.5")); //$NON-NLS-1$

			// Doesn't have any handlers. That's a problem.
			throw new HandlerRegistrationException();
		}
		while (handlerIterator.hasNext())
		{
			// Loop through all handlers
			Handler handler = handlerIterator.next();
			data.setCurrentHandler(handler.getHandlerName());
			Handler.result result = handler.execute(data);
			if (!result.equals(Handler.result.Successful))
			{
				// Should never happen.
				this.logger.error(MessageFormat.format(Messages.getString("IdentityResolverImpl.6"), handler.getHandlerName())); //$NON-NLS-1$
			}
		}
		
		this.logger.debug(MessageFormat.format(Messages.getString("IdentityResolverImpl.7"), data.getPrincipalAuthnIdentifier())); //$NON-NLS-1$
	}

}
