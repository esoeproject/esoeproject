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
 * Purpose: SPEP DAO implementation
 */
package com.qut.middleware.esoemanager.spep.sqlmap.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import com.qut.middleware.esoemanager.Constants;
import com.qut.middleware.esoemanager.exception.SPEPDAOException;
import com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO;

@Transactional
// See -
// http://static.springframework.org/spring/docs/2.0.x/reference/transaction.html#transaction-declarative-annotations
public class SPEPDAOImpl extends SqlMapClientDaoSupport implements SPEPDAO
{
	/* Local logging instance */
	private Logger logger = Logger.getLogger(SPEPDAOImpl.class.getName());

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO#queryActiveEntities()
	 */
	public List<String> queryActiveServices() throws SPEPDAOException
	{
		List<String> activeEntities = new ArrayList<String>();
		try
		{
			activeEntities = this.getSqlMapClient().queryForList(Constants.QUERY_ACTIVE_SERVICES_LIST, null);
			return activeEntities;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get active services from data repository \n"
					+ e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new SPEPDAOException(e.getLocalizedMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO#queryServiceContacts(java.lang.String)
	 */
	public List<Map<String, String>> queryServiceContacts(String entityID) throws SPEPDAOException
	{
		List<Map<String, String>> contacts = new ArrayList<Map<String, String>>();
		try
		{
			contacts = this.getSqlMapClient().queryForList(Constants.QUERY_SERVICE_CONTACTS, entityID);
			return contacts;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get service contacts from data repository \n"
					+ e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new SPEPDAOException(e.getLocalizedMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO#queryServiceDescription(java.lang.String)
	 */
	public List<Map<String, String>> queryServiceDescription(String entityID) throws SPEPDAOException
	{
		List<Map<String, String>> description = new ArrayList<Map<String, String>>();
		try
		{
			description = this.getSqlMapClient().queryForList(Constants.QUERY_SERVICE_DESCRIPTION, entityID);
			return description;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get service description from data repository \n"
					+ e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new SPEPDAOException(e.getLocalizedMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO#queryServiceDescriptor(java.lang.String)
	 */ 
	public List<Map<String, String>> queryServiceDescriptor(String entityID) throws SPEPDAOException
	{
		List<Map<String, String>> spDescriptors = new ArrayList<Map<String, String>>();
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put(Constants.FIELD_ENTITY_ID, entityID);
		queryParameters.put(Constants.FIELD_DESCRIPTOR_TYPE_ID, Constants.SP_DESCRIPTOR);

		try
		{
			spDescriptors = this.getSqlMapClient().queryForList(Constants.QUERY_SERVICE_DESCRIPTOR, queryParameters);
			return spDescriptors;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get service descriptor from data repository \n"
					+ e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new SPEPDAOException(e.getLocalizedMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO#queryServiceDetails(java.lang.String)
	 */
	public List<Map<String, String>> queryServiceDetails(String entityID) throws SPEPDAOException
	{
		List<Map<String, String>> serviceDetails = new ArrayList<Map<String, String>>();
		try
		{
			serviceDetails = this.getSqlMapClient().queryForList(Constants.QUERY_SERVICE_DETAILS, entityID);
			return serviceDetails;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get service details from data repository \n"
					+ e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new SPEPDAOException(e.getLocalizedMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO#queryServiceNodes(java.lang.String)
	 */
	public List<Map<String, String>> queryServiceNodes(String descriptorID) throws SPEPDAOException
	{
		List<Map<String, String>> serviceNodes = new ArrayList<Map<String, String>>();
		try
		{
			serviceNodes = this.getSqlMapClient().queryForList(Constants.QUERY_SERVICE_NODES, descriptorID);
			return serviceNodes;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get service nodes from data repository \n"
					+ e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new SPEPDAOException(e.getLocalizedMessage(), e);
		}
	}
	
	public List<Map<String, Object>> queryKeyStoreDetails(String descriptorID) throws SPEPDAOException
	{
		List<Map<String, Object>> keyStoreDetails = new ArrayList<Map<String, Object>>();
		try
		{
			keyStoreDetails = this.getSqlMapClient().queryForList(Constants.QUERY_KEYSTORE_DETAILS, descriptorID);
			return keyStoreDetails;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get keystore details from data repository \n"
					+ e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new SPEPDAOException(e.getLocalizedMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO#queryKeystoreBinary(java.lang.String)
	 */
	public List<Map<String, Object>> queryKeystoreBinary(String descriptorID) throws SPEPDAOException
	{
		List<Map<String, Object>> keyStoreData;

		try
		{
			keyStoreData = this.getSqlMapClient().queryForList(Constants.QUERY_KEYSTORE, descriptorID);
			return keyStoreData;
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new SPEPDAOException(e.getLocalizedMessage(), e);
		}
	}
	
	public List<Map<String, Object>> queryActiveAuthorizationPolicy(String descriptorID) throws SPEPDAOException
	{
		List<Map<String, Object>> policyData;
		try
		{
			policyData = this.getSqlMapClient().queryForList(Constants.QUERY_ACTIVE_AUTHORIZATION_POLICY, descriptorID);
			return policyData;
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new SPEPDAOException(e.getLocalizedMessage(), e);
		}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO#insertDescriptor(java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	public void insertDescriptor(String entityID, String descriptorID, String descriptorXML,
			String descriptorTypeID) throws SPEPDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_ENTITY_ID, entityID);
		parameters.put(Constants.FIELD_DESCRIPTOR_ID, descriptorID);
		parameters.put(Constants.FIELD_DESCRIPTOR_XML, descriptorXML);
		parameters.put(Constants.FIELD_DESCRIPTOR_TYPE_ID, descriptorTypeID);

		try
		{
			this.getSqlMapClient().insert(Constants.INSERT_DESCRIPTOR, parameters);
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new SPEPDAOException(e.getLocalizedMessage(), e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO#insertEntityDescriptor(java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String, char, java.lang.String)
	 */
	public void insertEntityDescriptor(String entityID, String organizationName,
			String organizationDisplayName, String organizationURL, String activeFlag)
			throws SPEPDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_ENTITY_ID, entityID);
		parameters.put(Constants.FIELD_ORGANIZATION_NAME, organizationName);
		parameters.put(Constants.FIELD_ORGANIZATION_DISPLAY_NAME, organizationDisplayName);
		parameters.put(Constants.FIELD_ORGANIZATION_URL, organizationURL);
		parameters.put(Constants.FIELD_ACTIVE_FLAG, activeFlag);

		try
		{
			this.getSqlMapClient().insert(Constants.INSERT_ENTITY_DESCRIPTOR, parameters);
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new SPEPDAOException(e.getLocalizedMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO#insertPKIData(java.lang.String, java.util.Date, byte[],
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public void insertPKIData(String descriptorID, Date expiryDate, byte[] keyStore, String keyStorePassphrase,
			String keyPairName, String keyPairPassphrase) throws SPEPDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_DESCRIPTOR_ID, descriptorID);
		parameters.put(Constants.FIELD_PKI_EXPIRY_DATE, expiryDate);
		parameters.put(Constants.FIELD_PKI_KEYSTORE, keyStore);
		parameters.put(Constants.FIELD_PKI_KEYSTORE_PASSPHRASE, keyStorePassphrase);
		parameters.put(Constants.FIELD_PKI_KEYPAIRNAME, keyPairName);
		parameters.put(Constants.FIELD_PKI_KEYPAIR_PASSPHRASE, keyPairPassphrase);

		try
		{
			this.getSqlMapClient().insert(Constants.INSERT_PKI_DATA, parameters);
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new SPEPDAOException(e.getLocalizedMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO#insertServiceContacts(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void insertServiceContacts(String entityID, String contactID, String contactType, String company, String givenName,
			String surname, String emailAddress, String telephoneNumber) throws SPEPDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_ENTITY_ID, entityID);
		parameters.put(Constants.FIELD_CONTACT_ID, contactID);
		parameters.put(Constants.FIELD_CONTACT_TYPE, contactType);
		parameters.put(Constants.FIELD_CONTACT_COMPANY, company);
		parameters.put(Constants.FIELD_CONTACT_GIVEN_NAME, givenName);
		parameters.put(Constants.FIELD_CONTACT_SURNAME, surname);
		parameters.put(Constants.FIELD_CONTACT_EMAIL_ADDRESS, emailAddress);
		parameters.put(Constants.FIELD_CONTACT_TELEPHONE_NUMBER, telephoneNumber);

		try
		{
			this.getSqlMapClient().insert(Constants.INSERT_SERVICE_CONTACTS, parameters);
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new SPEPDAOException(e.getLocalizedMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO#insertServiceDescription(java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void insertServiceDescription(String entityID, String serviceName, String serviceURL,
			String serviceDescription, String serviceAuthzFailureMsg) throws SPEPDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_ENTITY_ID, entityID);
		parameters.put(Constants.FIELD_SERVICE_NAME, serviceName);
		parameters.put(Constants.FIELD_SERVICE_URL, serviceURL);
		parameters.put(Constants.FIELD_SERVICE_DESC, serviceDescription);
		parameters.put(Constants.FIELD_SERVICE_AUTHZ_FAIL, serviceAuthzFailureMsg);

		try
		{
			this.getSqlMapClient().insert(Constants.INSERT_SERVICE_DESCRIPTION, parameters);
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new SPEPDAOException(e.getLocalizedMessage(), e);
		}

	}

	public void insertServiceNode(String endpointID, String descriptorID, String nodeURL,
			String assertionConsumerEndpoint, String singleLogoutEndpoint, String cacheClearEndpoint)
			throws SPEPDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_ENDPOINT_ID, endpointID);
		parameters.put(Constants.FIELD_DESCRIPTOR_ID, descriptorID);
		parameters.put(Constants.FIELD_ENDPOINT_NODEURL, nodeURL);
		parameters.put(Constants.FIELD_ENDPOINT_ASSERTIONCONSUMER, assertionConsumerEndpoint);
		parameters.put(Constants.FIELD_ENDPOINT_SINGLELOGOUT, singleLogoutEndpoint);
		parameters.put(Constants.FIELD_ENDPOINT_CACHECLEAR, cacheClearEndpoint);

		try
		{
			this.getSqlMapClient().insert(Constants.INSERT_SERVICE_NODE, parameters);
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new SPEPDAOException(e.getLocalizedMessage(), e);
		}
	}
	
	public void insertServiceAuthorizationHistoricalPolicy(String descriptorID, String lxacmlPolicy, Date insertTime) throws SPEPDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_DESCRIPTOR_ID, descriptorID);
		parameters.put(Constants.FIELD_LXACML_POLICY, lxacmlPolicy);
		parameters.put(Constants.FIELD_LXACML_DATE_INSERTED, insertTime);
		
		try
		{
			this.getSqlMapClient().insert(Constants.INSERT_SERVICE_AUTHORIZATION_HISTORICAL_POLICY, parameters);
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new SPEPDAOException(e.getLocalizedMessage(), e);
		}
	}

	public void insertServiceAuthorizationPolicy(String descriptorID, String lxacmlPolicy, Date updateTime) throws SPEPDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_DESCRIPTOR_ID, descriptorID);
		parameters.put(Constants.FIELD_LXACML_POLICY, lxacmlPolicy);
		parameters.put(Constants.FIELD_LXACML_DATE_LAST_UPDATED, updateTime);
		
		try
		{
			this.getSqlMapClient().insert(Constants.INSERT_SERVICE_AUTHORIZATION_POLICY, parameters);
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new SPEPDAOException(e.getLocalizedMessage(), e);
		}		
	}

	public void insertServiceAuthorizationShuntedPolicy(String descriptorID, String lxacmlPolicy, Date insertTime) throws SPEPDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_DESCRIPTOR_ID, descriptorID);
		parameters.put(Constants.FIELD_LXACML_POLICY, lxacmlPolicy);
		parameters.put(Constants.FIELD_LXACML_DATE_INSERTED, insertTime);
		
		try
		{
			this.getSqlMapClient().insert(Constants.INSERT_SERVICE_AUTHORIZATION_SHUNTED_POLICY, parameters);
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new SPEPDAOException(e.getLocalizedMessage(), e);
		}		
	}

	public void updateServiceContact(String entityID, String contactID, String contactType, String company, String givenName, String surname, String emailAddress, String telephoneNumber) throws SPEPDAOException
	{
		int count;
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_ENTITY_ID, entityID);
		parameters.put(Constants.FIELD_CONTACT_ID, contactID);
		parameters.put(Constants.FIELD_CONTACT_TYPE, contactType);
		parameters.put(Constants.FIELD_CONTACT_COMPANY, company);
		parameters.put(Constants.FIELD_CONTACT_GIVEN_NAME, givenName);
		parameters.put(Constants.FIELD_CONTACT_SURNAME, surname);
		parameters.put(Constants.FIELD_CONTACT_EMAIL_ADDRESS, emailAddress);
		parameters.put(Constants.FIELD_CONTACT_TELEPHONE_NUMBER, telephoneNumber);

		try
		{
			count = this.getSqlMapClient().update(Constants.UPDATE_SERVICE_CONTACT, parameters);
			
			if(count != 1)
			{
				this.logger.error("Update staement changed to many rows for entityID " + entityID + " and contactID " + contactID);
				throw new SPEPDAOException("Update staement changed to many rows for entityID " + entityID + " and contactID " + contactID);
			}
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new SPEPDAOException(e.getLocalizedMessage(), e);
		}		
	}
	
	public void updateServiceActiveState(String entityID, String state) throws SPEPDAOException
	{
		int count;
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_ENTITY_ID, entityID);
		parameters.put(Constants.FIELD_ACTIVE_FLAG, state);
		
		try
		{
			count = this.getSqlMapClient().update(Constants.UPDATE_SERVICE_ACTIVE_STATUS, parameters);
			
			if(count != 1)
			{
				this.logger.error("Update staement changed to many rows for entityID " + entityID + " and new state value " + state);
				throw new SPEPDAOException("Update staement changed to many rows for entityID " + entityID + " and new state value " + state);
			}
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new SPEPDAOException(e.getLocalizedMessage(), e);
		}
	}
	
	public void updateServiceDescription(String entityID, String serviceName, String serviceURL, String serviceDescription, String serviceAuthzFailureMsg) throws SPEPDAOException
	{
		int count;
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_ENTITY_ID, entityID);
		parameters.put(Constants.FIELD_SERVICE_NAME, serviceName);
		parameters.put(Constants.FIELD_SERVICE_URL, serviceURL);
		parameters.put(Constants.FIELD_SERVICE_DESC, serviceDescription);
		parameters.put(Constants.FIELD_SERVICE_AUTHZ_FAIL, serviceAuthzFailureMsg);
		
		try
		{
			count = this.getSqlMapClient().update(Constants.UPDATE_SERVICE_DESCRIPTION, parameters);
			
			if(count != 1)
			{
				this.logger.error("Update statement changed to many rows for entityID " + entityID + " new service details");
				throw new SPEPDAOException("Update statement changed to many rows for entityID " + entityID + " new service details");
			}
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new SPEPDAOException(e.getLocalizedMessage(), e);
		}
	}
	
	public void updateServiceAuthorizationPolicy(String descriptorID, String lxacmlPolicy, Date updateTime) throws SPEPDAOException
	{
		int count;
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_DESCRIPTOR_ID, descriptorID);
		parameters.put(Constants.FIELD_LXACML_POLICY, lxacmlPolicy);
		parameters.put(Constants.FIELD_LXACML_DATE_LAST_UPDATED, updateTime);
		
		try
		{
			count = this.getSqlMapClient().update(Constants.UPDATE_SERVICE_AUTHORIZATION_POLICY, parameters);
			if(count != 1)
			{
				this.logger.error("LXACML Policy update statement changed " + count + " rows for descriptorID " + descriptorID + " this is invalid, 1 row should be modified");
				throw new SPEPDAOException("LXACML Policy update statement changed " + count + " rows for descriptorID " + descriptorID + " this is invalid, 1 row should be modified");
			}
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new SPEPDAOException(e.getLocalizedMessage(), e);
		}		
	}
	
	public void deleteServiceContact(String entityID, String contactID) throws SPEPDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_ENTITY_ID, entityID);
		parameters.put(Constants.FIELD_CONTACT_ID, contactID);

		try
		{
			this.getSqlMapClient().insert(Constants.DELETE_SERVICE_CONTACT, parameters);
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new SPEPDAOException(e.getLocalizedMessage(), e);
		}		
	}

}
