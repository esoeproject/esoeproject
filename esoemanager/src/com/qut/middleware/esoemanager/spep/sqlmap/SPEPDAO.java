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
	/* Data repository query operations */
	public List<Map<String, Object>> queryKeystoreBinary(String descriptorID) throws SPEPDAOException;

	public List<String> queryActiveServices() throws SPEPDAOException;
	
	public List<Map<String, String>> queryServiceDetails(String entityID) throws SPEPDAOException;

	public List<Map<String, String>> queryServiceContacts(String entityID) throws SPEPDAOException;

	public List<Map<String, String>> queryServiceDescriptor(String entityID) throws SPEPDAOException;

	public List<Map<String, String>> queryServiceDescription(String entityID) throws SPEPDAOException;
	
	public List<Map<String, String>> queryServiceNodes(String descriptorID) throws SPEPDAOException;
	
	public List<Map<String, Object>> queryKeyStoreDetails(String descriptorID) throws SPEPDAOException;
	
	public List<Map<String, Object>> queryActiveAuthorizationPolicy(String descriptorID) throws SPEPDAOException;

	/* Data repository insert operations */
	public void insertEntityDescriptor(String entityID, String organizationName,
			String organizationDisplayName, String organizationURL, String activeFlag)
			throws SPEPDAOException;

	public void insertServiceDescription(String entityID, String serviceName, String serviceURL,
			String serviceDescription, String serviceAuthzFailureMsg) throws SPEPDAOException;

	public void insertServiceContacts(String entityID, String contactID, String contactType, String company, String givenName,
			String surname, String emailAddress, String telephoneNumber) throws SPEPDAOException;

	public void insertDescriptor(String entityID, String descriptorID, String descriptorXML,
			String descriptorTypeID) throws SPEPDAOException;

	public void insertPKIData(String descriptorID, Date expiryDate, byte[] keyStore, String keyStorePassphrase,
			String keyPairName, String keyPairPassphrase) throws SPEPDAOException;

	public void insertServiceNode(String endpointID, String descriptorID, String nodeURL,
			String assertionConsumerEndpoint, String singleLogoutEndpoint, String cacheClearEndpoint)
			throws SPEPDAOException;
	
	public void insertServiceAuthorizationPolicy(String descriptorID, String lxacmlPolicy, Date updateTime) throws SPEPDAOException;
	
	public void insertServiceAuthorizationHistoricalPolicy(String descriptorID, String lxacmlPolicy, Date insertTime) throws SPEPDAOException;
	
	public void insertServiceAuthorizationShuntedPolicy(String descriptorID, String lxacmlPolicy, Date insertTime) throws SPEPDAOException;
	
	/* Data repository update operations */
	public void updateServiceContact(String entityID, String contactID, String contactType, String company, String givenName,
			String surname, String emailAddress, String telephoneNumber) throws SPEPDAOException;
	
	public void updateServiceActiveState(String entityID, String state) throws SPEPDAOException;
	
	public void updateServiceDescription(String entityID, String serviceName, String serviceURL, String serviceDescription, String serviceAuthzFailureMsg) throws SPEPDAOException;
	
	public void updateServiceAuthorizationPolicy(String descriptorID, String lxacmlPolicy, Date updateTime) throws SPEPDAOException;
	
	/* Data repository delete operations */
	public void deleteServiceContact(String entityID, String contactID) throws SPEPDAOException;
}
