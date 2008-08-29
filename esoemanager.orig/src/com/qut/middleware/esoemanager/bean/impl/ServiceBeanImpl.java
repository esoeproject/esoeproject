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
 * 
 * Purpose: Service Bean Impl
 */
package com.qut.middleware.esoemanager.bean.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import com.qut.middleware.esoemanager.bean.ContactPersonBean;
import com.qut.middleware.esoemanager.bean.ServiceBean;
import com.qut.middleware.esoemanager.bean.ServiceNodeBean;

public class ServiceBeanImpl implements ServiceBean
{
	private String serviceName;
	private String serviceURL;
	private Vector<ContactPersonBean> contacts;
	private Vector<ServiceNodeBean> serviceNodes;
	private String serviceDescription;
	private String serviceAuthzFailureMsg;
	private String descriptorID;
	private String entityID;
	private String activeFlag;
	private byte[] descriptorXML;
	
	private Integer entID;
	private Integer descID;

	private String keyStorePassphrase;
	private String keyPairPassphrase;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.bean.ServiceBean#getContacts()
	 */
	public Vector<ContactPersonBean> getContacts()
	{
		return this.contacts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.bean.ServiceBean#setContacts(java.util.Vector)
	 */
	public void setContacts(Vector<ContactPersonBean> contacts)
	{
		this.contacts = contacts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.bean.ServiceBean#getServiceURL()
	 */
	public String getServiceURL()
	{
		return this.serviceURL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.bean.ServiceBean#setServiceURL(java.lang.String)
	 */
	public void setServiceURL(String serviceURL)
	{
		this.serviceURL = serviceURL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.bean.ServiceBean#getSpeps()
	 */
	public Vector<ServiceNodeBean> getServiceNodes()
	{
		return this.serviceNodes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.bean.ServiceBean#setSpeps(java.util.Vector)
	 */
	public void setServiceNodes(Vector<ServiceNodeBean> serviceNodes)
	{
		this.serviceNodes = serviceNodes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.bean.ServiceBean#getServiceName()
	 */
	public String getServiceName()
	{
		return this.serviceName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.bean.ServiceBean#setServiceName(java.lang.String)
	 */
	public void setServiceName(String serviceName)
	{
		this.serviceName = serviceName;
	}

	public String getKeyPairPassphrase()
	{
		return this.keyPairPassphrase;
	}

	public String getKeyStorePassphrase()
	{
		return this.keyStorePassphrase;
	}

	public void setKeyPairPassphrase(String keyPairPassphrase)
	{
		this.keyPairPassphrase = keyPairPassphrase;
	}

	public void setKeyStorePassphrase(String keyStorePassphrase)
	{
		this.keyStorePassphrase = keyStorePassphrase;
	}

	public String getServiceAuthzFailureMsg()
	{
		return this.serviceAuthzFailureMsg;
	}

	public String getServiceDescription()
	{
		return this.serviceDescription;
	}

	public void setServiceAuthzFailureMsg(String serviceAuthzFailureMsg)
	{
		this.serviceAuthzFailureMsg = serviceAuthzFailureMsg;
	}

	public void setServiceDescription(String serviceDescription)
	{
		this.serviceDescription = serviceDescription;
	}

	public String getDescriptorID()
	{
		return this.descriptorID;
	}

	public void setDescriptorID(String descriptorID)
	{
		this.descriptorID = descriptorID;
	}

	public String getEntityID()
	{
		return entityID;
	}

	public void setEntityID(String entityID)
	{
		this.entityID = entityID;
	}

	public String getActiveFlag()
	{
		return activeFlag;
	}

	public void setActiveFlag(String activeFlag)
	{
		this.activeFlag = activeFlag;
	}

	public byte[] getDescriptorXML()
	{
		return descriptorXML;
	}

	public void setDescriptorXML(byte[] descriptorXML)
	{
		this.descriptorXML = descriptorXML;
	}

	public String getServiceHost()
	{
		try
		{
			URL serviceURL = new URL(this.serviceURL);
			if(serviceURL.getPort() == -1)
				return serviceURL.getProtocol() + "://" + serviceURL.getHost();
			else
				return serviceURL.getProtocol() + "://" + serviceURL.getHost() + ":" + serviceURL.getPort();
		}
		catch (MalformedURLException e)
		{
			return null;
		}
	}

	public Integer getEntID()
	{
		return entID;
	}

	public void setEntID(Integer entID)
	{
		this.entID = entID;
	}

	public Integer getDescID()
	{
		return descID;
	}

	public void setDescID(Integer descID)
	{
		this.descID = descID;
	}
}
