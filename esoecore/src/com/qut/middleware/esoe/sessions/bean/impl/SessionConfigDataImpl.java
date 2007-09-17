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
 * Purpose: Implements the session data interface to parse and contain the session 
 * 		data configuration.
 */
package com.qut.middleware.esoe.sessions.bean.impl;

import java.util.List;

import org.apache.log4j.Logger;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.metadata.Metadata;
import com.qut.middleware.esoe.sessions.Messages;
import com.qut.middleware.esoe.sessions.bean.SessionConfigData;
import com.qut.middleware.esoe.sessions.exception.ConfigurationValidationException;
import com.qut.middleware.esoe.sessions.exception.SessionsDAOException;
import com.qut.middleware.esoe.sessions.sqlmap.SessionsDAO;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.handler.Unmarshaller;
import com.qut.middleware.saml2.handler.impl.UnmarshallerImpl;
import com.qut.middleware.saml2.schemas.esoe.sessions.IdentityType;
import com.qut.middleware.saml2.schemas.esoe.sessions.SessionData;

/** Implements the session data interface to parse and contain the session 
 * 		data configuration. */
public class SessionConfigDataImpl implements SessionConfigData
{
	private List<IdentityType> identityList;
	private SessionsDAO sessionsDAO;
	private Metadata metadata;
	private Integer esoeEntID;
	
	private final String UNMAR_PKGNAMES = SessionData.class.getPackage().getName();
		
	/* Local logging instance */
	private Logger logger = Logger.getLogger(SessionConfigDataImpl.class.getName());

	/**
	 * Default constructor
	 * @throws ConfigurationValidationException
	 */
	public SessionConfigDataImpl(SessionsDAO sessionsDAO, Metadata metadata) throws ConfigurationValidationException
	{
		try
		{		
			this.sessionsDAO = sessionsDAO;
			this.metadata = metadata;
			
			Unmarshaller<SessionData> sessionDataUnmarshaller = new UnmarshallerImpl<SessionData>(this.UNMAR_PKGNAMES, new String[]{ConfigurationConstants.sessionData});

			this.esoeEntID = this.sessionsDAO.getEntID(this.metadata.getEsoeEntityID());
			byte[] xmlConfigData = this.sessionsDAO.selectActiveAttributePolicy(this.esoeEntID);
			
			this.logger.trace(xmlConfigData);
			
			SessionData sessionData;
			sessionData = sessionDataUnmarshaller.unMarshallUnSigned(xmlConfigData);
			this.identityList = sessionData.getIdentities();
		}
		catch (UnmarshallerException e)
		{
			this.logger.error(Messages.getString("SessionConfigDataImpl.1")); //$NON-NLS-1$
			this.logger.debug(Messages.getString("SessionConfigDataImpl.1"), e); //$NON-NLS-1$
			throw new ConfigurationValidationException(e);
		}
		catch (SessionsDAOException e)
		{
			this.logger.error(Messages.getString("SessionConfigDataImpl.2")); //$NON-NLS-1$
			this.logger.debug(Messages.getString("SessionConfigDataImpl.3"), e); //$NON-NLS-1$
			throw new ConfigurationValidationException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.xml.sessions.SessionDataType#getIdentity()
	 */
	public List<IdentityType> getIdentity()
	{
		return this.identityList;
	}
}
