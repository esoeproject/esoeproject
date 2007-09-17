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
 * Purpose: ESOE DAO
 */
package com.qut.middleware.esoestartup.sqlmap;

import java.util.Date;

import com.qut.middleware.esoestartup.exception.ESOEDAOException;

public interface ESOEDAO
{
	/* Data repository select operations */
	/**
	 * Selects the next sequence value as controlled by the repository for ENT_ID, this is a data repository specific value which can be used as a mapping between entries in SAML metadata (entity) and backend store rows
	 * @return The next ID which can be subsequently used to insert a new record into ENTITY_DESCRIPTORS
	 * @throws ESOEDAOException
	 */
	public Integer getNextEntID() throws ESOEDAOException;

	/**
	 * Selects the next sequence value as controlled by the repository for DESC_ID, this is a data repository specific value which can be used as a mapping between entries in SAML metadata (descriptors) and backend store rows
	 * @return The next ID which can be subsequently used to insert a new record into DESCRIPTORS
	 * @throws ESOEDAOException
	 */
	public Integer getNextDescID() throws ESOEDAOException;

	/* Data repository insert operations */
	public void insertEntityDescriptor(Integer entID, String entityID, String organizationName, String organizationDisplayName, String organizationURL, String activeFlag) throws ESOEDAOException;

	public void insertServiceDescription(Integer entID, String serviceName, String serviceURL, String serviceDescription, String serviceAuthzFailureMsg) throws ESOEDAOException;

	public void insertServiceContacts(Integer entID, String contactID, String contactType, String company, String givenName, String surname, String emailAddress, String telephoneNumber) throws ESOEDAOException;

	public void insertDescriptor(Integer entID, Integer descID, String descriptorID, byte[] descriptorXML, String descriptorTypeID) throws ESOEDAOException;

	public void insertServiceNode(String endpointID, Integer descID, String nodeURL, String assertionConsumerEndpoint, String singleLogoutEndpoint, String cacheClearEndpoint) throws ESOEDAOException;

	public void insertServiceAuthorizationPolicy(Integer entID, String policyID, byte[] lxacmlPolicy) throws ESOEDAOException;

	public void insertAttributePolicy(Integer entID, byte[] attribPolicy) throws ESOEDAOException;
	
	public void insertMetadataPKIData(Date expiryDate, byte[] keyStore, String keyStorePassphrase, String keyPairName, String keyPairPassphrase) throws ESOEDAOException;
	
	public void insertPKIData(Integer descID, Date expiryDate, byte[] keyStore, String keyStorePassphrase, String keyPairName, String keyPairPassphrase) throws ESOEDAOException;
	
	public void insertPublicKey(Integer descID, Date expiryDate, String keyPairName, byte[] publicKey)  throws ESOEDAOException;
}