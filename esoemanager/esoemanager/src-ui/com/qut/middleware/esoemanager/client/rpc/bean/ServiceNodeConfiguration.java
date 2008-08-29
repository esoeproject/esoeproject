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

import com.google.gwt.user.client.rpc.IsSerializable;


public class ServiceNodeConfiguration  implements IsSerializable
{
	private String keystorePassword;
	private String spepKeyAlias;
	private String spepKeyPassword;

	private String esoeIdentifier;
	private String spepIdentifier;
	private String metadataURL;
	private String nodeIdentifier;
	private String serviceHost;

	/**
	 * @return the keystorePassword
	 */
	public String getKeystorePassword()
	{
		return keystorePassword;
	}
	/**
	 * @param keystorePassword the keystorePassword to set
	 */
	public void setKeystorePassword(String keystorePassword)
	{
		this.keystorePassword = keystorePassword;
	}
	/**
	 * @return the spepKeyAlias
	 */
	public String getSpepKeyAlias()
	{
		return spepKeyAlias;
	}
	/**
	 * @param spepKeyAlias the spepKeyAlias to set
	 */
	public void setSpepKeyAlias(String spepKeyAlias)
	{
		this.spepKeyAlias = spepKeyAlias;
	}
	/**
	 * @return the spepKeyPassword
	 */
	public String getSpepKeyPassword()
	{
		return spepKeyPassword;
	}
	/**
	 * @param spepKeyPassword the spepKeyPassword to set
	 */
	public void setSpepKeyPassword(String spepKeyPassword)
	{
		this.spepKeyPassword = spepKeyPassword;
	}
	/**
	 * @return the esoeIdentifier
	 */
	public String getEsoeIdentifier()
	{
		return esoeIdentifier;
	}
	/**
	 * @param esoeIdentifier the esoeIdentifier to set
	 */
	public void setEsoeIdentifier(String esoeIdentifier)
	{
		this.esoeIdentifier = esoeIdentifier;
	}
	/**
	 * @return the spepIdentifier
	 */
	public String getSpepIdentifier()
	{
		return spepIdentifier;
	}
	/**
	 * @param spepIdentifier the spepIdentifier to set
	 */
	public void setSpepIdentifier(String spepIdentifier)
	{
		this.spepIdentifier = spepIdentifier;
	}
	/**
	 * @return the metadataURL
	 */
	public String getMetadataURL()
	{
		return metadataURL;
	}
	/**
	 * @param metadataURL the metadataURL to set
	 */
	public void setMetadataURL(String metadataURL)
	{
		this.metadataURL = metadataURL;
	}
	/**
	 * @return the nodeIdentifier
	 */
	public String getNodeIdentifier()
	{
		return nodeIdentifier;
	}
	/**
	 * @param nodeIdentifier the nodeIdentifier to set
	 */
	public void setNodeIdentifier(String nodeIdentifier)
	{
		this.nodeIdentifier = nodeIdentifier;
	}
	/**
	 * @return the serviceHost
	 */
	public String getServiceHost()
	{
		return serviceHost;
	}
	/**
	 * @param serviceHost the serviceHost to set
	 */
	public void setServiceHost(String serviceHost)
	{
		this.serviceHost = serviceHost;
	}	
}
