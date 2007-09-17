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
 * Author: Bradley Beddoes
 * Creation Date: 1/5/07
 */
package com.qut.middleware.esoemanager.pages;

import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.qut.middleware.esoemanager.bean.KeyStoreDetailsBean;
import com.qut.middleware.esoemanager.bean.ServiceNodeBean;
import com.qut.middleware.esoemanager.exception.RetrieveServiceKeyStoreException;
import com.qut.middleware.esoemanager.exception.RetrieveServiceNodesException;
import com.qut.middleware.esoemanager.logic.RetrieveServiceKeyStoreDetailsLogic;
import com.qut.middleware.esoemanager.logic.RetrieveServiceNodesLogic;

public class ServiceNodeConfigurationPage extends BorderPage
{
	RetrieveServiceNodesLogic serviceNodesLogic;
	RetrieveServiceKeyStoreDetailsLogic serviceKeyStoreLogic;

	/* entityID and serviceID are passed in on query string */
	public String eid;
	public String did;
	public String sid;

	public String esoeTrustedID;
	public String serviceID;
	public String metadataKeyName;
	public List<ServiceNodeBean> serviceNodes;
	public KeyStoreDetailsBean keyStoreDetails;

	/* Local logging instance */
	private Logger logger = Logger.getLogger(ServiceNodeConfigurationPage.class.getName());

	public ServiceNodeConfigurationPage()
	{
		this.serviceNodes = null;
		this.keyStoreDetails = null;
	}

	@Override
	public void onGet()
	{
		try
		{
			if (did != null)
			{
				this.serviceNodes = serviceNodesLogic.execute(new Integer(did));
				this.keyStoreDetails = serviceKeyStoreLogic.execute(new Integer(did));
			}

			if (sid != null)
			{
				this.serviceID = new String( Base64.decodeBase64(sid.getBytes()) );
			}
		}
		catch (RetrieveServiceKeyStoreException e)
		{
			this.logger.info("Exception from logic call " + e.getLocalizedMessage());
			this.serviceNodes = null;
			this.keyStoreDetails = null;
		}
		catch (RetrieveServiceNodesException e)
		{
			this.logger.info("Exception from logic call " + e.getLocalizedMessage());
			this.serviceNodes = null;
			this.keyStoreDetails = null;
		}
	}

	public RetrieveServiceNodesLogic getRetrieveServiceNodeLogic()
	{
		return this.serviceNodesLogic;
	}

	public void setRetrieveServiceNodesLogic(RetrieveServiceNodesLogic serviceNodesLogic)
	{
		this.serviceNodesLogic = serviceNodesLogic;
	}

	public void setEsoeTrustedID(String esoeTrustedID)
	{
		this.esoeTrustedID = esoeTrustedID;
	}

	public String getEsoeTrustedID()
	{
		return this.esoeTrustedID;
	}

	public RetrieveServiceKeyStoreDetailsLogic getRetrieveServiceKeyStoreLogic()
	{
		return this.serviceKeyStoreLogic;
	}

	public void setRetrieveServiceKeyStoreLogic(RetrieveServiceKeyStoreDetailsLogic serviceKeyStoreLogic)
	{
		this.serviceKeyStoreLogic = serviceKeyStoreLogic;
	}

	public String getMetadataKeyName()
	{
		return this.metadataKeyName;
	}

	public void setMetadataKeyName(String metadataKeyName)
	{
		this.metadataKeyName = metadataKeyName;
	}
}
