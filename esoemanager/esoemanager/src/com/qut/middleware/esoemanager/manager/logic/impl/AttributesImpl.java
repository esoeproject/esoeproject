/* Copyright 2008, Queensland University of Technology
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
 */
package com.qut.middleware.esoemanager.manager.logic.impl;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import com.qut.middleware.esoemanager.Constants;
import com.qut.middleware.esoemanager.exception.RetrieveAttributeException;
import com.qut.middleware.esoemanager.exception.ManagerDAOException;
import com.qut.middleware.esoemanager.exception.SaveAttributeException;
import com.qut.middleware.esoemanager.manager.logic.Attributes;
import com.qut.middleware.esoemanager.manager.sqlmap.ManagerDAO;
import com.qut.middleware.esoemanager.util.UtilFunctions;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.handler.Unmarshaller;
import com.qut.middleware.saml2.handler.impl.UnmarshallerImpl;
import com.qut.middleware.saml2.schemas.esoe.sessions.SessionData;

public class AttributesImpl implements Attributes
{
	private ManagerDAO managerDAO;
	private UtilFunctions utils;
	
	private int esoeENTID;
	
	private Unmarshaller<SessionData> unmarshaller;
	private String[] schema =
	{
		Constants.sessionData
	};
	
	public AttributesImpl() throws UnmarshallerException
	{
		this.unmarshaller = new UnmarshallerImpl<SessionData>(SessionData.class.getPackage().getName(), schema);
	}
	
	public String getAttributePolicyXML() throws RetrieveAttributeException
	{
		byte[] rawPolicy;
		String policy;
		
		try
		{
			Map<String, byte[]> result = managerDAO.queryActiveAttributePolicy(this.esoeENTID);
			if(result != null)
			{
				rawPolicy = result.get(Constants.FIELD_ATTRIBUTE_POLICY);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try
				{
					this.utils.prettyPrintXML(rawPolicy, baos);
					policy = new String(baos.toByteArray(), "UTF-16");
				}
				catch (Exception e)
				{
					// We tried to pretty things up at least.. :)
					policy = new String(rawPolicy, "UTF-16");
				}
				
				return policy;
			}
			
			throw new RetrieveAttributeException("Unable to retrieve the attribute policy");
		}
		catch (UnsupportedEncodingException e)
		{
			 throw new RetrieveAttributeException(e.getLocalizedMessage(), e);
		}
		catch (ManagerDAOException e)
		{
			 throw new RetrieveAttributeException(e.getLocalizedMessage(), e);
		}
	}

	public void saveAttributePolicyXML(String policy) throws SaveAttributeException
	{
		try
		{
			this.unmarshaller.unMarshallUnSigned(policy.getBytes("UTF-16"));
			this.managerDAO.updateActiveAttributePolicy(this.esoeENTID, policy.getBytes("UTF-16"));
		}
		catch (UnsupportedEncodingException e)
		{
			throw new SaveAttributeException(e.getLocalizedMessage(), e);
		}
		catch (UnmarshallerException e)
		{
			throw new SaveAttributeException(e.getLocalizedMessage(), e);
		}
		catch (ManagerDAOException e)
		{
			throw new SaveAttributeException(e.getLocalizedMessage(), e);
		}		
	}

	public ManagerDAO getManagerDAO()
	{
		return managerDAO;
	}

	public void setManagerDAO(ManagerDAO managerDAO)
	{
		this.managerDAO = managerDAO;
	}

	public UtilFunctions getUtils()
	{
		return utils;
	}

	public void setUtils(UtilFunctions utils)
	{
		this.utils = utils;
	}

	public int getEsoeENTID()
	{
		return esoeENTID;
	}

	public void setEsoeENTID(int esoeENTID)
	{
		this.esoeENTID = esoeENTID;
	}
}
