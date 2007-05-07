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
 * Creation Date: 11/10/2006
 *
 * Purpose: Tests the LDAP implementation of the Handler interface for mapping
 * 		attributes to identities.
 */
package com.qut.middleware.esoe.sessions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.ldap.LdapTemplate;
import org.springframework.ldap.support.LdapContextSource;

import com.qut.middleware.esoe.sessions.bean.IdentityAttribute;
import com.qut.middleware.esoe.sessions.bean.IdentityData;
import com.qut.middleware.esoe.sessions.bean.SessionConfigData;
import com.qut.middleware.esoe.sessions.bean.impl.IdentityAttributeImpl;
import com.qut.middleware.esoe.sessions.bean.impl.IdentityDataImpl;
import com.qut.middleware.esoe.sessions.bean.impl.SessionConfigDataImpl;
import com.qut.middleware.esoe.sessions.exception.ConfigurationValidationException;
import com.qut.middleware.esoe.sessions.exception.DataSourceException;
import com.qut.middleware.esoe.sessions.identity.pipeline.Handler;
import com.qut.middleware.esoe.sessions.identity.pipeline.impl.LDAPHandlerImpl;

/**
 * @author Shaun
 * 
 */
public class LDAPHandlerImplTest
{

	/**
	 * Test method for
	 * {@link com.qut.middleware.esoe.sessions.identity.pipeline.impl.LDAPHandlerImpl#execute(com.qut.middleware.esoe.sessions.bean.IdentityData)}.
	 */
	@Test
	public final void testExecute()
	{
		String contextBase = "dc=au"; //$NON-NLS-1$
		String searchBase = "ou=people,dc=qut,dc=edu"; //$NON-NLS-1$
		String identifier = "uid"; //$NON-NLS-1$
		String user = "uid=beddoes,ou=people,dc=qut,dc=edu,dc=au"; //$NON-NLS-1$
		String password = "itscandyyoulikeit"; //$NON-NLS-1$
		String url = "ldap://nettle.qut.edu.au"; //$NON-NLS-1$

		LdapContextSource context = new LdapContextSource();
		context.setBase(contextBase);
		context.setUserName(user);
		context.setPassword(password);
		context.setUrl(url);
		
		try{
		context.afterPropertiesSet();
		}catch(Exception ex){throw new UnsupportedOperationException();}

		LdapTemplate template = new LdapTemplate(context);
		
		SessionConfigData sessionData = null;
		
		try
		{
			File xmlConfig = new File(this.getClass().getResource("sessiondata.xml").toURI()); //$NON-NLS-1$
			
			sessionData = new SessionConfigDataImpl(xmlConfig);
		}
		catch(URISyntaxException ex)
		{
			fail("Failed to parse session data"); //$NON-NLS-1$
		}
		catch (ConfigurationValidationException ex)
		{
			fail("Failed to parse session data"); //$NON-NLS-1$
		}

		Handler handler = new LDAPHandlerImpl(template, identifier, searchBase, sessionData);

		IdentityData data = new IdentityDataImpl();
		data.setPrincipalAuthnIdentifier("beddoes"); //$NON-NLS-1$

		IdentityAttribute attribute = new IdentityAttributeImpl();
		data.getAttributes().put("uid", attribute); //$NON-NLS-1$
		attribute = new IdentityAttributeImpl();
		data.getAttributes().put("mail", attribute); //$NON-NLS-1$
		attribute = new IdentityAttributeImpl();
		data.getAttributes().put("sn", attribute); //$NON-NLS-1$

		try
		{
			handler.execute(data);
		}
		catch (DataSourceException ex)
		{
			fail("Problem connecting to LDAP server"); //$NON-NLS-1$
			return;
		}
		
		Map<String,IdentityAttribute> attributes = data.getAttributes();
		IdentityAttribute uidAttribute = attributes.get("uid"); //$NON-NLS-1$
		List<Object> values = uidAttribute.getValues();
		
		assertTrue("Exactly one UID found for user", values.size() > 0); //$NON-NLS-1$
		if(values.size() > 0)
		{
			assertTrue("UID is string type", values.get(0) instanceof String); //$NON-NLS-1$
			String uid = (String)values.get(0);
			assertEquals("UID matches principal name", uid, data.getPrincipalAuthnIdentifier()); //$NON-NLS-1$
		}
	}
}
