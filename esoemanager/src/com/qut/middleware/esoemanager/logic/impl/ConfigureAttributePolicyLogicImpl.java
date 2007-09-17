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
 * Author: 22/8/2007
 * Creation Date: Bradley Beddoes
 * 
 * Purpose: Implementation of logic to retrieve and set policy data
 */
package com.qut.middleware.esoemanager.logic.impl;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.qut.middleware.esoemanager.Constants;
import com.qut.middleware.esoemanager.exception.AttributePolicyException;
import com.qut.middleware.esoemanager.exception.SPEPDAOException;
import com.qut.middleware.esoemanager.logic.ConfigureAttributePolicyLogic;
import com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.handler.Unmarshaller;
import com.qut.middleware.saml2.handler.impl.UnmarshallerImpl;
import com.qut.middleware.saml2.schemas.esoe.sessions.SessionData;

public class ConfigureAttributePolicyLogicImpl implements ConfigureAttributePolicyLogic
{
	private final String UNMAR_PKGNAMES = SessionData.class.getPackage().getName();
	
	private Unmarshaller<SessionData> sessionDataUnmarshaller;
	private SPEPDAO spepDAO;
	private String esoeEntityID;
	private Integer esoeEntID;
	
	/* Local logging instance */
	private Logger logger = Logger.getLogger(ConfigureAttributePolicyLogicImpl.class.getName());

	public ConfigureAttributePolicyLogicImpl(SPEPDAO spepDAO, String esoeEntityID) throws UnmarshallerException, AttributePolicyException
	{
		this.spepDAO = spepDAO;
		this.esoeEntityID = esoeEntityID;
		
		this.sessionDataUnmarshaller = new UnmarshallerImpl<SessionData>(this.UNMAR_PKGNAMES, new String[]{Constants.sessionData});
		
		try
		{
			this.esoeEntID = spepDAO.getEntID(this.esoeEntityID);
		}
		catch (SPEPDAOException e)
		{
			this.logger.fatal("Unable to map configured ESOE Identifer to ENT_ID in data repository");
			throw new AttributePolicyException("Unable to map configured ESOE Identifer to ENT_ID in data repository", e);
		}
	}

	public byte[] getActiveAttributePolicy() throws AttributePolicyException
	{
		try
		{
			List<Map<String, byte[]>> result = spepDAO.queryActiveAttributePolicy(this.esoeEntID);
			if(result != null && result.size() == 1)
				return result.get(0).get(Constants.FIELD_ATTRIBUTE_POLICY);
			
			return null;
		}
		catch (SPEPDAOException e)
		{
			this.logger.info("Exception when attempting to retrieve latest attribute policy from datastore");
			this.logger.debug("Exception when attempting to retrieve latest attribute policy from datastore", e);
			throw new AttributePolicyException("Exception when attempting to retrieve latest attribute policy from datastore", e);
		}
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoemanager.logic.ConfigureAttributePolicyLogic#updateAttributePolicy(byte[])
	 */
	public void updateAttributePolicy(byte[] updatedPolicy) throws AttributePolicyException
	{
		try
		{
			this.sessionDataUnmarshaller.unMarshallUnSigned(updatedPolicy);
			this.spepDAO.updateActiveAttributePolicy(this.esoeEntID, updatedPolicy);
		}
		catch (SPEPDAOException e)
		{
			this.logger.info("Exception when attempting to update latest attribute policy from datastore");
			this.logger.debug("Exception when attempting to update latest attribute policy from datastore", e);
			throw new AttributePolicyException("Exception when attempting to update latest attribute policy from datastore", e);
		}
		catch (UnmarshallerException e)
		{
			this.logger.info("Policy is not valid according to sessiondata-schema.xsd, please review input");
			this.logger.debug("Policy is not valid according to sessiondata-schema.xsd, please review input", e);
			throw new AttributePolicyException("Policy is not valid according to sessiondata-schema.xsd, please review input", e);
		};
	}

}
