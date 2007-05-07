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
 * Purpose: Implements the User interface.
 */
package com.qut.middleware.esoe.sessions.identity.pipeline.ldap.impl;

import java.text.MessageFormat;
import java.util.Map;

import org.apache.log4j.Logger;

import com.qut.middleware.esoe.log4j.InsaneLogLevel;
import com.qut.middleware.esoe.sessions.Messages;
import com.qut.middleware.esoe.sessions.bean.IdentityAttribute;
import com.qut.middleware.esoe.sessions.bean.IdentityData;
import com.qut.middleware.esoe.sessions.identity.pipeline.ldap.User;

/** */
public class UserImpl implements User
{
	private IdentityData data;
	
	/* Local logging instance */
	private Logger logger = Logger.getLogger(UserImpl.class.getName());

	/**
	 * Constructor
	 * 
	 * @param data
	 *            Identity data to fill.
	 */
	public UserImpl(IdentityData data)
	{
		if (data == null)
		{
			throw new IllegalArgumentException(Messages.getString("UserImpl.IdentityDataNull")); //$NON-NLS-1$
		}
		
		this.data = data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.identity.pipeline.ldap.User#addAttributeValue(java.lang.String,
	 *      java.lang.Object)
	 */
	public void addAttributeValue(String attributeName, Object value)
	{
		// The attribute name passed in will be the attribute name as seen by the ESOE, not the local identifier used by
		// the Handler
		Map<String, IdentityAttribute> attributeMap = this.data.getAttributes();
		IdentityAttribute attribute = attributeMap.get(attributeName);

		// We can safely ignore it if the attribute comes back null
		if (attribute != null)
		{
			this.logger.log(InsaneLogLevel.INSANE, MessageFormat.format(Messages.getString("UserImpl.0"), attributeName, value.toString())); //$NON-NLS-1$
			
			attribute.getValues().add(value);
		}
	}
}
