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
package com.qut.middleware.esoemanager;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import com.qut.middleware.esoemanager.manager.logic.Attributes;
import com.qut.middleware.esoemanager.manager.logic.Dashboard;
import com.qut.middleware.esoemanager.manager.logic.Service;
import com.qut.middleware.esoemanager.manager.logic.ServiceContacts;
import com.qut.middleware.esoemanager.manager.logic.ServiceCrypto;
import com.qut.middleware.esoemanager.manager.logic.ServiceNodes;
import com.qut.middleware.esoemanager.manager.logic.ServicePolicies;
import com.qut.middleware.esoemanager.manager.logic.ToggleState;
import com.qut.middleware.esoemanager.metadata.logic.MetadataCache;
import com.qut.middleware.esoemanager.util.UtilFunctions;

public class EsoeManagerImpl implements EsoeManager
{
	UtilFunctions utils;
	
	Attributes attributes;
	Dashboard dashboard;
	
	Service service;
	ServiceContacts serviceContacts;
	ServiceNodes serviceNodes;
	ServicePolicies servicePolicies;
	ServiceCrypto serviceCrypto;

	ToggleState toggleState;
	
	MetadataCache metadataCache;
	
	/**
	 * @see Security pointcut associated with this, return false by default in case of problems 
	 */
	public Boolean isSuperUser()
	{
		return false;
	}
	
	public String getCompleteMD()
	{
		String md;
		byte[] rawMD = metadataCache.getCompleteMD();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
		{
			this.utils.prettyPrintXML(rawMD, baos);
			md = new String(baos.toByteArray(), "UTF-16");
		}
		catch (Exception e)
		{
			try
			{
				// We tried to pretty things up at least.. :)
				md = new String(rawMD, "UTF-16");
			}
			catch (UnsupportedEncodingException e1)
			{
				md = "Metadata unavailable";
			}
		}
		
		return md;
	}

	public ToggleState getToggleState()
	{
		return toggleState;
	}

	public void setToggleState(ToggleState toggleState)
	{
		this.toggleState = toggleState;
	}
			
	public Service getService()
	{
		return service;
	}

	public void setService(Service service)
	{
		this.service = service;
	}

	public ServiceContacts getServiceContacts()
	{
		return serviceContacts;
	}

	public void setServiceContacts(ServiceContacts serviceContacts)
	{
		this.serviceContacts = serviceContacts;
	}

	public ServiceNodes getServiceNodes()
	{
		return serviceNodes;
	}

	public void setServiceNodes(ServiceNodes serviceNodes)
	{
		this.serviceNodes = serviceNodes;
	}

	public ServicePolicies getServicePolicies()
	{
		return servicePolicies;
	}

	public void setServicePolicies(ServicePolicies servicePolicies)
	{
		this.servicePolicies = servicePolicies;
	}

	public ServiceCrypto getServiceCrypto()
	{
		return serviceCrypto;
	}

	public void setServiceCrypto(ServiceCrypto serviceCrypto)
	{
		this.serviceCrypto = serviceCrypto;
	}

	public Attributes getAttributes()
	{
		return attributes;
	}

	public void setAttributes(Attributes attributes)
	{
		this.attributes = attributes;
	}

	public Dashboard getDashboard()
	{
		return dashboard;
	}

	public void setDashboard(Dashboard dashboard)
	{
		this.dashboard = dashboard;
	}

	public MetadataCache getMetadataCache()
	{
		return metadataCache;
	}

	public void setMetadataCache(MetadataCache metadataCache)
	{
		this.metadataCache = metadataCache;
	}

	public UtilFunctions getUtils()
	{
		return utils;
	}

	public void setUtils(UtilFunctions utils)
	{
		this.utils = utils;
	}
	
}
