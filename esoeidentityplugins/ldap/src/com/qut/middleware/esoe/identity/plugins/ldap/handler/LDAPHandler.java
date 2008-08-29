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
 * Creation Date: 06/10/2006
 * 
 * Purpose: Implements the identity pipeline handler interface, using an LDAP
 * 		data source to resolve attributes.
 */
package com.qut.middleware.esoe.identity.plugins.ldap.handler;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;

import com.qut.middleware.esoe.identity.plugins.ldap.mapper.User;
import com.qut.middleware.esoe.identity.plugins.ldap.mapper.impl.UserAttributeMapper;
import com.qut.middleware.esoe.identity.plugins.ldap.mapper.impl.UserImpl;
import com.qut.middleware.esoe.sessions.Messages;
import com.qut.middleware.esoe.sessions.bean.IdentityData;
import com.qut.middleware.esoe.sessions.bean.SessionConfigData;
import com.qut.middleware.esoe.sessions.identity.pipeline.Handler;

/** */
public class LDAPHandler implements Handler
{
	private LdapTemplate template;
	private String principalAttribute;
	private String searchBase;
	private SessionConfigData sessionConfigData;
	
	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(LDAPHandler.class.getName());

	/**
	 * Constructor
	 * 
	 * @param template
	 *            The LdapTemplate from Spring LDAP
	 * @param principalAttribute
	 *            The attribute in LDAP which is used to search for the data
	 * @param searchBase
	 *            The search base to use
	 * @param sessionConfigData
	 *            The session data to use for resolving attributes.
	 */
	public LDAPHandler(LdapTemplate template, String principalAttribute, String searchBase, SessionConfigData sessionConfigData)
	{
		if (template == null)
		{
			throw new IllegalArgumentException(Messages.getString("LDAPHandlerImpl.LDAPTemplateNull")); //$NON-NLS-1$
		}
		if (principalAttribute == null || principalAttribute.length() == 0)
		{
			throw new IllegalArgumentException(Messages.getString("LDAPHandlerImpl.PrincipalAttributeNull")); //$NON-NLS-1$
		}
		if (searchBase == null)
		{
			throw new IllegalArgumentException(Messages.getString("LDAPHandlerImpl.SearchBaseNull")); //$NON-NLS-1$
		}
		if (sessionConfigData == null)
		{
			throw new IllegalArgumentException(Messages.getString("LDAPHandlerImpl.3")); //$NON-NLS-1$
		}
		
		this.template = template;
		this.principalAttribute = principalAttribute;
		this.searchBase = searchBase;
		this.sessionConfigData = sessionConfigData;
		
		this.logger.info(MessageFormat.format(Messages.getString("LDAPHandlerImpl.0"), principalAttribute, searchBase)); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.identity.pipeline.Handler#execute(com.qut.middleware.esoe.sessions.bean.IdentityData)
	 */
	public Handler.result execute(IdentityData data)
	{
		User user = new UserImpl(data);
		AttributesMapper mapper = new UserAttributeMapper(user, this.sessionConfigData, this.getHandlerName());
		
		this.logger.debug(MessageFormat.format(Messages.getString("LDAPHandlerImpl.1"), data.getPrincipalAuthnIdentifier())); //$NON-NLS-1$

		// Build a filter to match the principal name.
		EqualsFilter filter = new EqualsFilter(this.principalAttribute, data.getPrincipalAuthnIdentifier());
		this.template.search(this.searchBase, filter.encode(), mapper);
		
		this.logger.debug(MessageFormat.format(Messages.getString("LDAPHandlerImpl.2"), data.getPrincipalAuthnIdentifier())); //$NON-NLS-1$

		return result.Successful;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.identity.pipeline.Handler#getHandlerName()
	 */
	public String getHandlerName()
	{
		return "LDAPHandler"; //$NON-NLS-1$
	}
}
