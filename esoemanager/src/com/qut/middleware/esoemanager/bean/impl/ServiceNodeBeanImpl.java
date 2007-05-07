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
 * Purpose: Service Node Bean Impl
 */
package com.qut.middleware.esoemanager.bean.impl;

import java.io.Serializable;

import com.qut.middleware.esoemanager.bean.ServiceNodeBean;

public class ServiceNodeBeanImpl implements Serializable, ServiceNodeBean
{
	private static final long serialVersionUID = 4232917876960298675L;
	
	private String nodeID;
	private String nodeURL;
	private String assertionConsumerService;
	private String singleLogoutService;
	private String cacheClearService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.bean.impl.ServiceNodeBean#getAssertionConsumerService()
	 */
	public String getAssertionConsumerService()
	{
		return assertionConsumerService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.bean.impl.ServiceNodeBean#setAssertionConsumerService(java.lang.String)
	 */
	public void setAssertionConsumerService(String assertionConsumerService)
	{
		this.assertionConsumerService = assertionConsumerService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.bean.impl.ServiceNodeBean#getCacheClearService()
	 */
	public String getCacheClearService()
	{
		return cacheClearService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.bean.impl.ServiceNodeBean#setCacheClearService(java.lang.String)
	 */
	public void setCacheClearService(String cacheClearService)
	{
		this.cacheClearService = cacheClearService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.bean.impl.ServiceNodeBean#getNodeURL()
	 */
	public String getNodeURL()
	{
		return nodeURL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.bean.impl.ServiceNodeBean#setNodeURL(java.lang.String)
	 */
	public void setNodeURL(String nodeURL)
	{
		this.nodeURL = nodeURL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.bean.impl.ServiceNodeBean#getSingleLogoutService()
	 */
	public String getSingleLogoutService()
	{
		return singleLogoutService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.bean.impl.ServiceNodeBean#setSingleLogoutService(java.lang.String)
	 */
	public void setSingleLogoutService(String singleLogoutService)
	{
		this.singleLogoutService = singleLogoutService;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoemanager.bean.ServiceNodeBean#getNodeID()
	 */
	public String getNodeID()
	{
		return nodeID;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoemanager.bean.ServiceNodeBean#setNodeID(java.lang.String)
	 */
	public void setNodeID(String nodeID)
	{
		this.nodeID = nodeID;
	}
}
