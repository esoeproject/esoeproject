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
package com.qut.middleware.esoemanager.client.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.qut.middleware.esoemanager.client.rpc.bean.Policy;
import com.qut.middleware.esoemanager.client.rpc.bean.ServiceContact;

public interface EsoeManagerServiceAsync
{
	/** Authorization **/
	public void isSuperUser(AsyncCallback callback);
	
	/** Administration **/
	public void getESOEDashboardDetails(AsyncCallback callback);

	public void getMDDashboardDetails(AsyncCallback callback);

	public void getServicesDashboardDetails(AsyncCallback callback);

	public void getAttributePolicyXML(AsyncCallback callback);

	public void getMetadataXML(AsyncCallback callback);

	public void saveAttributePolicyXML(String policy, AsyncCallback callback);

	public void getStartupDashboardDetails(AsyncCallback callback);

	/** Services **/
	public void createService(String entityID, String serviceName, String serviceURL, String serviceDescription,
			String entityHost, String firstNode, ServiceContact techContact, AsyncCallback callback);

	public void createServiceContact(String serviceID, String contactID, String name, String email, String telelphone,
			String company, String type, AsyncCallback callback);

	public void createServiceKey(String serviceID, AsyncCallback callback);

	public void createServiceNode(String serviceID, String newNodeURL, String newNodeID, AsyncCallback callback);

	public void createServicePolicy(String serviceID, Policy policy, AsyncCallback callback);

	public void createServicePolicy(String serviceID, String policy, AsyncCallback callback);

	public void deleteServiceContact(String serviceID, String contactID, AsyncCallback callback);

	public void deleteServiceKey(String serviceID, String keypairName, AsyncCallback callback);

	public void deleteServicePolicy(String serviceID, String policyID, AsyncCallback callback);

	public void retrieveConfiguredAttributeList(AsyncCallback callback);
	
	public void retrieveServiceHost(String serviceID, AsyncCallback callback);
	
	public void saveServiceHost(String serviceID, String serviceHost, AsyncCallback callback);

	public void retrieveNodeConfigurations(String serviceID, AsyncCallback callback);

	public void retrieveServiceContacts(String serviceID, AsyncCallback callback);

	public void retrieveServiceKeys(String serviceID, AsyncCallback callback);

	public void retrieveServiceStartups(String serviceID, AsyncCallback callback);

	public void retrieveServiceListing(String serviceID, AsyncCallback callback);

	public void retrieveServiceNodes(String serviceID, AsyncCallback callback);

	public void retrieveServiceDescriptor(String serviceID, AsyncCallback callback);

	public void retrieveServicePolicies(String serviceID, AsyncCallback callback);

	public void retrieveServicePolicy(String serviceID, String policyID, AsyncCallback callback);

	public void retrieveServicePolicyXML(String serviceID, String policyID, AsyncCallback callback);

	public void retrieveSimpleServiceListing(AsyncCallback callback);

	public void saveServiceContact(String serviceID, String contactID, String name, String email, String telephone,
			String company, String type, AsyncCallback callback);

	public void saveServiceDescription(String serviceID, String serviceName, String serviceURL,
			String serviceDescription, String serviceAccessDeniedMessage, AsyncCallback callback);

	public void saveServiceNodeConfiguration(String serviceID, String nodeIdentifier, String nodeURL, String nodeACS,
			String nodeSLS, String nodeCCS, AsyncCallback callback);

	public void saveServicePolicy(String serviceID, Policy policy, AsyncCallback callback);

	public void saveServicePolicy(String serviceID, String policy, AsyncCallback callback);

	public void toggleServiceNodeState(String serviceID, String nodeIdentifier, boolean active, AsyncCallback callback);

	public void toggleServicePolicyState(String serviceID, String policyID, boolean active, AsyncCallback callback);

	public void toggleServiceState(String serviceID, boolean active, AsyncCallback callback);
}
