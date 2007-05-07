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
 * Purpose: Bean to store all details about a registered service
 */
package com.qut.middleware.esoemanager.bean;

import java.util.Vector;


public interface ServiceBean
{
	public Vector<ContactPersonBean> getContacts();

	public void setContacts(Vector<ContactPersonBean> contacts);

	public String getServiceURL();

	public void setServiceURL(String serviceURL);

	public Vector<ServiceNodeBean> getServiceNodes();

	public void setServiceNodes(Vector<ServiceNodeBean> serviceNodes);

	public String getServiceName();

	public void setServiceName(String serviceName);

	public String getKeyStorePassphrase();

	public void setKeyStorePassphrase(String keyStorePassphrase);

	public String getKeyPairPassphrase();

	public void setKeyPairPassphrase(String keyPairPassphrase);

	public String getServiceDescription();

	public void setServiceDescription(String serviceDescription);

	public String getServiceAuthzFailureMsg();

	public void setServiceAuthzFailureMsg(String serviceAuthzFailureMsg);

	public void setEntityID(String entityID);

	public String getEntityID();

	public void setDescriptorID(String descriptorID);

	public String getDescriptorID();

	public String getActiveFlag();

	public void setActiveFlag(String activeFlag);
	
	public void setDescriptorXML(String descriptorXML);
	
	public String getDescriptorXML();
}
