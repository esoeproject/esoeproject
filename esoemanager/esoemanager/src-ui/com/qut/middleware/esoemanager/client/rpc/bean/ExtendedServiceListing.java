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
package com.qut.middleware.esoemanager.client.rpc.bean;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Stores data about a service
 * @author Bradley Beddoes
 */
public class ExtendedServiceListing implements IsSerializable
{
	private String identifier;
	private String serviceName;
	private String serviceURL;
	private boolean active;
	private String serviceDescription;	
	private String serviceAuthorizationFailureMessage;
	
	/**
	 * @return the serviceName
	 */
	public String getServiceName()
	{
		return serviceName;
	}
	/**
	 * @param serviceName the serviceName to set
	 */
	public void setServiceName(String serviceName)
	{
		this.serviceName = serviceName;
	}
	/**
	 * @return the serviceURL
	 */
	public String getServiceURL()
	{
		return serviceURL;
	}
	/**
	 * @param serviceURL the serviceURL to set
	 */
	public void setServiceURL(String serviceURL)
	{
		this.serviceURL = serviceURL;
	}
	/**
	 * @return the active
	 */
	public boolean isActive()
	{
		return active;
	}
	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active)
	{
		this.active = active;
	}
	/**
	 * @return the identifier
	 */
	public String getIdentifier()
	{
		return identifier;
	}
	/**
	 * @param identifier the identifier to set
	 */
	public void setIdentifier(String identifier)
	{
		this.identifier = identifier;
	}
	/**
	 * @return the serviceDescription
	 */
	public String getServiceDescription()
	{
		return serviceDescription;
	}
	/**
	 * @param serviceDescription the serviceDescription to set
	 */
	public void setServiceDescription(String serviceDescription)
	{
		this.serviceDescription = serviceDescription;
	}
	/**
	 * @return the serviceAuthorizationFailureMessage
	 */
	public String getServiceAuthorizationFailureMessage()
	{
		return serviceAuthorizationFailureMessage;
	}
	/**
	 * @param serviceAuthorizationFailureMessage the serviceAuthorizationFailureMessage to set
	 */
	public void setServiceAuthorizationFailureMessage(String serviceAuthorizationFailureMessage)
	{
		this.serviceAuthorizationFailureMessage = serviceAuthorizationFailureMessage;
	}
	
}
