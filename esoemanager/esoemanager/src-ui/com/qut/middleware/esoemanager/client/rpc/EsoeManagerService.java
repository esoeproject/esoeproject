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

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.qut.middleware.esoemanager.client.rpc.bean.EsoeDashBean;
import com.qut.middleware.esoemanager.client.rpc.bean.ExtendedServiceListing;
import com.qut.middleware.esoemanager.client.rpc.bean.KeyDetails;
import com.qut.middleware.esoemanager.client.rpc.bean.MetadataDashBean;
import com.qut.middleware.esoemanager.client.rpc.bean.Policy;
import com.qut.middleware.esoemanager.client.rpc.bean.ServiceContact;
import com.qut.middleware.esoemanager.client.rpc.bean.ServiceNode;
import com.qut.middleware.esoemanager.client.rpc.bean.ServiceNodeConfiguration;
import com.qut.middleware.esoemanager.client.rpc.bean.ServiceStartupBean;
import com.qut.middleware.esoemanager.client.rpc.bean.ServicesDashBean;
import com.qut.middleware.esoemanager.client.rpc.bean.SimpleServiceListing;

public interface EsoeManagerService extends RemoteService
{
	/** Authorization **/
	public Boolean isSuperUser();
	
	/** Administration **/
	public EsoeDashBean getESOEDashboardDetails() throws EsoeManagerServiceException;

	public MetadataDashBean getMDDashboardDetails() throws EsoeManagerServiceException;

	public ServicesDashBean getServicesDashboardDetails() throws EsoeManagerServiceException;

	public String getAttributePolicyXML() throws EsoeManagerServiceException;

	public String getMetadataXML() throws EsoeManagerServiceException;

	public void saveAttributePolicyXML(String policy) throws EsoeManagerServiceException;

	public List<ServiceStartupBean> getStartupDashboardDetails() throws EsoeManagerServiceException;

	/** Services **/
	
	public String retrieveServiceHost(String serviceID) throws EsoeManagerServiceException;
	public void saveServiceHost(String serviceID, String serviceHost) throws EsoeManagerServiceException;
			
	public String createService(String entityID, String serviceName, String serviceURL, String serviceDescription,
			String entityHost, String firstNode, ServiceContact techContact) throws EsoeManagerServiceException;

	public List<SimpleServiceListing> retrieveSimpleServiceListing() throws EsoeManagerServiceException;

	public ExtendedServiceListing retrieveServiceListing(String serviceID) throws EsoeManagerServiceException;

	public List<ServiceContact> retrieveServiceContacts(String serviceID) throws EsoeManagerServiceException;

	public void saveServiceContact(String serviceID, String contactID, String name, String email, String telephone,
			String company, String type) throws EsoeManagerServiceException;

	public List<ServiceNode> retrieveServiceNodes(String serviceID) throws EsoeManagerServiceException;

	public List<Policy> retrieveServicePolicies(String serviceID) throws EsoeManagerServiceException;

	public Policy retrieveServicePolicy(String serviceID, String policyID) throws EsoeManagerServiceException;

	public List<ServiceStartupBean> retrieveServiceStartups(String serviceID) throws EsoeManagerServiceException;

	public String retrieveServicePolicyXML(String serviceID, String policyID) throws EsoeManagerServiceException;

	public List<String> retrieveConfiguredAttributeList() throws EsoeManagerServiceException;

	public void toggleServiceState(String serviceID, boolean active) throws EsoeManagerServiceException;

	public void saveServiceDescription(String serviceID, String serviceName, String serviceURL,
			String serviceDescription, String serviceAccessDeniedMessage) throws EsoeManagerServiceException;

	public void createServiceNode(String serviceID, String newNodeURL, String newNodeID)
			throws EsoeManagerServiceException;

	public void toggleServiceNodeState(String serviceID, String nodeIdentifier, boolean active)
			throws EsoeManagerServiceException;

	public void saveServiceNodeConfiguration(String serviceID, String nodeIdentifier, String nodeURL, String nodeACS,
			String nodeSLS, String nodeCCS) throws EsoeManagerServiceException;

	public void createServiceContact(String serviceID, String contactID, String name, String email, String telelphone,
			String company, String type) throws EsoeManagerServiceException;

	public void deleteServiceContact(String serviceID, String contactID) throws EsoeManagerServiceException;

	public void saveServicePolicy(String serviceID, Policy policy) throws EsoeManagerServiceException;

	public void saveServicePolicy(String serviceID, String policy) throws EsoeManagerServiceException,
			EsoeManagerInvalidXMLException;

	public String createServicePolicy(String serviceID, Policy policy) throws EsoeManagerServiceException;

	public String retrieveServiceDescriptor(String serviceID) throws EsoeManagerServiceException;

	public String createServicePolicy(String serviceID, String policy) throws EsoeManagerServiceException,
			EsoeManagerInvalidXMLException;

	public void toggleServicePolicyState(String serviceID, String policyID, boolean active)
			throws EsoeManagerServiceException;

	public void deleteServicePolicy(String serviceID, String policyID) throws EsoeManagerServiceException;

	public List<KeyDetails> retrieveServiceKeys(String serviceID) throws EsoeManagerServiceException;

	public void createServiceKey(String serviceID) throws EsoeManagerServiceException;

	public List<ServiceNodeConfiguration> retrieveNodeConfigurations(String serviceID)
			throws EsoeManagerServiceException;

	public void deleteServiceKey(String serviceID, String keypairName) throws EsoeManagerServiceException;
}
