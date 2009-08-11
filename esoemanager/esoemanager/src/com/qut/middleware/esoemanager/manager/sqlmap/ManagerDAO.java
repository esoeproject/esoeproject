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
package com.qut.middleware.esoemanager.manager.sqlmap;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.qut.middleware.esoemanager.exception.ManagerDAOException;

public interface ManagerDAO
{
	/* Data repository select operations */
	/**
	 * Selects the next sequence value as controlled by the repository for entID, this is a data repository specific value which can be used as a mapping between entries in SAML metadata (entity) and backend store rows
	 * @return The next ID which can be subsequently used to insert a new record into ENTITY_DESCRIPTORS
	 * @throws ESOEDAOException
	 */
	public Integer getNextEntID() throws ManagerDAOException;

	/**
	 * Selects the next sequence value as controlled by the repository for descID, this is a data repository specific value which can be used as a mapping between entries in SAML metadata (descriptors) and backend store rows
	 * @return The next ID which can be subsequently used to insert a new record into DESCRIPTORS
	 * @throws ESOEDAOException
	 */
	public Integer getNextDescID() throws ManagerDAOException;
	
	/**
	 * Returns the entID mapping from the data repository for a given entityID
	 * @param entityID As configured in metadata
	 * @return The value for entID for use in future queries with the data repository
	 * @throws ManagerDAOException
	 */
	public Integer getEntID(String entityID) throws ManagerDAOException;
	
	/**
	 * Return the DESC_ID mapping from the data repository for a given service, services will only
	 * ever have a single descriptor ID.
	 * @param entID The ENT_ID of the service
	 * @param descriptor TODO
	 * @return The DESC_ID of the service
	 * @throws ManagerDAOException
	 */
	public Integer getDescID(Integer entID, String descriptor) throws ManagerDAOException;
	
	public Integer getServiceCount() throws ManagerDAOException;
	public Integer getActiveServiceCount() throws ManagerDAOException;
	
	public Integer getNodeCount() throws ManagerDAOException;
	public Integer getActiveNodeCount() throws ManagerDAOException;
	
	public Integer getPolicyCount() throws ManagerDAOException;
	public Integer getActivePolicyCount() throws ManagerDAOException;
	
	public Integer getEntIDfromDescID(Integer descID) throws ManagerDAOException;	
	
	/* Data repository query operations */
	public String queryServiceHost(Integer entID) throws ManagerDAOException;
	
	public List<Map<String, Object>> queryKeystoreBinary(Integer descID) throws ManagerDAOException;

	public List<Map<String, Object>> queryKeystoreBinary(Integer descID, String keyName) throws ManagerDAOException;

	public List<Integer> queryServices() throws ManagerDAOException;
	
	public Map<String, Object> queryServiceDetails(Integer entID) throws ManagerDAOException;

	public List<Map<String, Object>> queryServiceContacts(Integer entID) throws ManagerDAOException;

	public Map<String, Object> queryServiceDescriptor(Integer entID) throws ManagerDAOException;

	public Map<String, Object> queryServiceDescription(Integer entID) throws ManagerDAOException;
	
	public List<Map<String, Object>> queryServiceNodes(Integer descID) throws ManagerDAOException;
	
	public List<Map<String, Object>> queryKeyStoreDetails(Integer descID) throws ManagerDAOException;
	
	public List<Map<String, Object>> queryServicePolicies(Integer entID) throws ManagerDAOException;
	
	public List<Map<String, Object>> queryServicePolicy(Integer entID, String policyID) throws ManagerDAOException;
	
	public Map<String, byte[]> queryActiveAttributePolicy(Integer entID) throws ManagerDAOException;
	
	public List<Map<String, Object>> queryServicesCloseExpiry() throws ManagerDAOException;
	
	public List<Map<String, Object>> queryRecentNodeStartup() throws ManagerDAOException;
	
	public List<Map<String, Object>> queryRecentServiceNodeStartup(Integer entID) throws ManagerDAOException;

	/* Data repository insert operations */
	public void insertEntityDescriptor(Integer entID, String entityID, String entityHost, String organizationName,
			String organizationDisplayName, String organizationURL, String activeFlag)
			throws ManagerDAOException;

	public void insertServiceDescription(Integer entID, String serviceName, String serviceURL,
			String serviceDescription, String serviceAuthzFailureMsg) throws ManagerDAOException;

	public void insertServiceContact(Integer entID, String contactID, String contactType, String company, String givenName,
			String surname, String emailAddress, String telephoneNumber) throws ManagerDAOException;

	public void insertDescriptor(Integer entID, Integer descID, String descriptorID, byte[] descriptorXML,
			String descriptorTypeID) throws ManagerDAOException;
	
	public void insertPublicKey(Integer DESC_IC, Date expiryDate, String keyPairName, String issuerDN, String serialNumber, byte[] publicKey)  throws ManagerDAOException;

	public void insertPKIData(Integer descID,Date expiryDate, byte[] keyStore, String keyStorePassphrase,
			String keyPairName, String keyPairPassphrase) throws ManagerDAOException;

	public void insertServiceNode(String endpointID, Integer descID, String nodeURL,
			String assertionConsumerEndpoint, String singleLogoutEndpoint, String cacheClearEndpoint)
			throws ManagerDAOException;
	
	public void insertServiceAuthorizationPolicy(Integer entID, String policyID, byte[] lxacmlPolicy) throws ManagerDAOException;
	
	public void insertServiceAuthorizationShuntedPolicy(Integer entID, byte[] lxacmlPolicy, Date insertTime) throws ManagerDAOException;
	
	/* Data repository update operations */
	public void updateServiceContact(Integer entID, String contactID, String contactType, String company, String givenName,
			String surname, String emailAddress, String telephoneNumber) throws ManagerDAOException;
	
	public void updateServiceActiveState(Integer entID, String state) throws ManagerDAOException;
	
	public void updateServiceHost(Integer entID, String serviceHost) throws ManagerDAOException;
	
	public void updateServiceNode(Integer descID, String nodeIdentifier, String nodeURL, String nodeACS, String nodeSLS, String nodeCCS) throws ManagerDAOException;
	
	public void updateServiceNodeActiveState(Integer descID, String nodeIdentifier, String state) throws ManagerDAOException;
	
	public void updateServicePolicyActiveState(Integer entID, String policyID, String state) throws ManagerDAOException;
	
	public void updateServiceDescription(Integer entID, String serviceName, String serviceURL, String serviceDescription, String serviceAuthzFailureMsg) throws ManagerDAOException;
	
	public void updateServiceAuthorizationPolicy(Integer entID, String policyID, byte[] lxacmlPolicy) throws ManagerDAOException;
	
	public void updateActiveAttributePolicy(Integer entID, byte[] attributePolicy) throws ManagerDAOException;
	
	/* Data repository delete operations */
	public void deleteServiceContact(Integer entID, String contactID) throws ManagerDAOException;
	public void deleteServicePolicy(Integer entID, String policyID) throws ManagerDAOException;
	public void deleteServiceKey(Integer descID, String keypairName) throws ManagerDAOException;
}
