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
 * Creation Date: 22/04/2007
 * 
 * Purpose: SPEP DAO
 */
package com.qut.middleware.esoemanager.spep.sqlmap;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.qut.middleware.esoemanager.exception.SPEPDAOException;

public interface SPEPDAO
{
	/* Data repository select operations */
	/**
	 * Selects the next sequence value as controlled by the repository for entID, this is a data repository specific value which can be used as a mapping between entries in SAML metadata (entity) and backend store rows
	 * @return The next ID which can be subsequently used to insert a new record into ENTITY_DESCRIPTORS
	 * @throws ESOEDAOException
	 */
	public Integer getNextEntID() throws SPEPDAOException;

	/**
	 * Selects the next sequence value as controlled by the repository for descID, this is a data repository specific value which can be used as a mapping between entries in SAML metadata (descriptors) and backend store rows
	 * @return The next ID which can be subsequently used to insert a new record into DESCRIPTORS
	 * @throws ESOEDAOException
	 */
	public Integer getNextDescID() throws SPEPDAOException;
	
	/**
	 * Returns the entID mapping from the data repository for a given entityID
	 * @param entityID As configured in metadata
	 * @return The value for entID for use in future queries with the data repository
	 * @throws SPEPDAOException
	 */
	public Integer getEntID(String entityID) throws SPEPDAOException;
	
	/* Data repository query operations */
	public List<Map<String, Object>> queryKeystoreBinary(Integer descID) throws SPEPDAOException;

	public List<Integer> queryActiveServices() throws SPEPDAOException;
	
	public List<Map<String, Object>> queryServiceDetails(Integer entID) throws SPEPDAOException;

	public List<Map<String, Object>> queryServiceContacts(Integer entID) throws SPEPDAOException;

	public List<Map<String, Object>> queryServiceDescriptor(Integer entID) throws SPEPDAOException;

	public List<Map<String, Object>> queryServiceDescription(Integer entID) throws SPEPDAOException;
	
	public List<Map<String, Object>> queryServiceNodes(Integer descID) throws SPEPDAOException;
	
	public List<Map<String, Object>> queryKeyStoreDetails(Integer descID) throws SPEPDAOException;
	
	public List<Map<String, Object>> queryActiveAuthorizationPolicy(Integer entID) throws SPEPDAOException;
	
	public List<Map<String, byte[]>> queryActiveAttributePolicy(Integer entID) throws SPEPDAOException;

	/* Data repository insert operations */
	public void insertEntityDescriptor(Integer entID, String entityID, String organizationName,
			String organizationDisplayName, String organizationURL, String activeFlag)
			throws SPEPDAOException;

	public void insertServiceDescription(Integer entID, String serviceName, String serviceURL,
			String serviceDescription, String serviceAuthzFailureMsg) throws SPEPDAOException;

	public void insertServiceContacts(Integer entID, String contactID, String contactType, String company, String givenName,
			String surname, String emailAddress, String telephoneNumber) throws SPEPDAOException;

	public void insertDescriptor(Integer entID, Integer descID, String descriptorID, byte[] descriptorXML,
			String descriptorTypeID) throws SPEPDAOException;
	
	public void insertPublicKey(Integer DESC_IC, Date expiryDate, String keyPairName, byte[] publicKey)  throws SPEPDAOException;

	public void insertPKIData(Integer descID,Date expiryDate, byte[] keyStore, String keyStorePassphrase,
			String keyPairName, String keyPairPassphrase) throws SPEPDAOException;

	public void insertServiceNode(String endpointID, Integer descID, String nodeURL,
			String assertionConsumerEndpoint, String singleLogoutEndpoint, String cacheClearEndpoint)
			throws SPEPDAOException;
	
	public void insertServiceAuthorizationPolicy(Integer entID, String policyID, byte[] lxacmlPolicy) throws SPEPDAOException;
	
	public void insertServiceAuthorizationShuntedPolicy(Integer entID, byte[] lxacmlPolicy, Date insertTime) throws SPEPDAOException;
	
	/* Data repository update operations */
	public void updateServiceContact(Integer entID, String contactID, String contactType, String company, String givenName,
			String surname, String emailAddress, String telephoneNumber) throws SPEPDAOException;
	
	public void updateServiceActiveState(Integer entID, String state) throws SPEPDAOException;
	
	public void updateServiceDescription(Integer entID, String serviceName, String serviceURL, String serviceDescription, String serviceAuthzFailureMsg) throws SPEPDAOException;
	
	public void updateServiceAuthorizationPolicy(Integer entID, String policyID, byte[] lxacmlPolicy) throws SPEPDAOException;
	
	public void updateActiveAttributePolicy(Integer entID, byte[] attributePolicy) throws SPEPDAOException;
	
	/* Data repository delete operations */
	public void deleteServiceContact(Integer entID, String contactID) throws SPEPDAOException;
}
