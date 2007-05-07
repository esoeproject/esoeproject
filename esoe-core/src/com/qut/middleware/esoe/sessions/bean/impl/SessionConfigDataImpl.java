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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.util.FileCopyUtils;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.log4j.InsaneLogLevel;
import com.qut.middleware.esoe.sessions.Messages;
import com.qut.middleware.esoe.sessions.bean.SessionConfigData;
import com.qut.middleware.esoe.sessions.exception.ConfigurationValidationException;
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
	
	
	private final String UNMAR_PKGNAMES = SessionData.class.getPackage().getName();
		
	/* Local logging instance */
	private Logger logger = Logger.getLogger(SessionConfigDataImpl.class.getName());

	/**
	 * @param xmlConfigFile
	 *            The configuration file to be parsed.
	 * @throws ConfigurationValidationException
	 */
	public SessionConfigDataImpl(File xmlConfigFile) throws ConfigurationValidationException
	{
		if (xmlConfigFile == null || !xmlConfigFile.exists())
		{
			throw new IllegalArgumentException(Messages.getString("SessionConfigDataImpl.ConfigPathNull")); //$NON-NLS-1$
		}
		
		InputStream fileStream = null;
		
		try
		{
			this.logger.debug(MessageFormat.format(Messages.getString("SessionConfigDataImpl.0"), xmlConfigFile.getAbsolutePath())); //$NON-NLS-1$
			
			Unmarshaller<SessionData> sessionDataUnmarshaller = new UnmarshallerImpl<SessionData>(this.UNMAR_PKGNAMES, new String[]{ConfigurationConstants.sessionData});

			fileStream = new FileInputStream(xmlConfigFile);
			Reader reader = new InputStreamReader(fileStream, "UTF-16"); //$NON-NLS-1$
			
			String xmlConfigData = FileCopyUtils.copyToString(reader);
			
			this.logger.log(InsaneLogLevel.INSANE, xmlConfigData);
			
			SessionData sessionData;
			sessionData = sessionDataUnmarshaller.unMarshallUnSigned(xmlConfigData);
			this.identityList = sessionData.getIdentities();
		}
		catch (UnmarshallerException e)
		{
			this.logger.debug(MessageFormat.format(Messages.getString("SessionConfigDataImpl.1"), xmlConfigFile.getAbsolutePath()), e); //$NON-NLS-1$
			throw new ConfigurationValidationException(e);
		}
		catch (FileNotFoundException e)
		{
			this.logger.debug(MessageFormat.format(Messages.getString("SessionConfigDataImpl.2"), xmlConfigFile.getAbsolutePath()), e); //$NON-NLS-1$
			throw new ConfigurationValidationException(e);
		}
		catch (IOException e)
		{
			this.logger.debug(MessageFormat.format(Messages.getString("SessionConfigDataImpl.3"), xmlConfigFile.getAbsolutePath()), e); //$NON-NLS-1$
			throw new ConfigurationValidationException(e);
		}
		finally
		{
			try
			{
				if(fileStream != null)
					fileStream.close();
			}
			catch(IOException e)
			{
				//
			}
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
