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
package com.qut.middleware.esoemanager.manager.sqlmap.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import com.qut.middleware.esoemanager.Constants;
import com.qut.middleware.esoemanager.exception.ManagerDAOException;
import com.qut.middleware.esoemanager.manager.sqlmap.ManagerDAO;

@Transactional
// See -
// http://static.springframework.org/spring/docs/2.0.x/reference/transaction.html#transaction-declarative-annotations
public class ManagerDAOImpl extends SqlMapClientDaoSupport implements ManagerDAO
{
	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(ManagerDAOImpl.class.getName());

	public Integer getNextEntID() throws ManagerDAOException
	{
		try
		{
			Integer result = (Integer) this.getSqlMapClient().queryForObject(Constants.QUERY_NEXT_ENT_ID);
			if(result != null)
			{
				return result;
			}
			else
			{
				throw new ManagerDAOException("No value for NEXT_ENT_ID could be established");
			}
			
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}
	
	public Integer getNextDescID() throws ManagerDAOException
	{
		try
		{
			Integer result = (Integer)this.getSqlMapClient().queryForObject(Constants.QUERY_NEXT_DESC_ID);
			if(result != null)
			{
				return result;
			}
			else
			{
				throw new ManagerDAOException("No value for NEXT_DESC_ID could be established");
			}
			
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}
	
	public Integer getEntID(String entityID) throws ManagerDAOException
	{
		try
		{
			Integer result = (Integer) this.getSqlMapClient().queryForObject(Constants.QUERY_ENT_ID, entityID);
			if(result != null)
			{
				return result;
			}
			else
			{
				throw new ManagerDAOException("No value for entID mapping for supplied entityID of " + entityID + " could be established");
			}
			
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}
	
	public Integer getEntIDfromDescID(Integer descID) throws ManagerDAOException
	{
		try
		{
			Integer result = (Integer) this.getSqlMapClient().queryForObject(Constants.QUERY_ENTID_FROM_DESCID, descID);
			if(result != null)
			{
				return result;
			}
			else
			{
				throw new ManagerDAOException("No value for entID mapping for supplied descID of " + descID + " could be established");
			}
			
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}
	
	public Integer getActiveNodeCount() throws ManagerDAOException
	{
		try
		{
			Integer result = (Integer) this.getSqlMapClient().queryForObject(Constants.QUERY_ACTIVE_NODE_COUNT);
			if(result != null)
			{
				return result;
			}
			else
			{
				return 0;
			}
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}

	public Integer getActivePolicyCount() throws ManagerDAOException
	{
		try
		{
			Integer result = (Integer) this.getSqlMapClient().queryForObject(Constants.QUERY_ACTIVE_POLICY_COUNT);
			if(result != null)
			{
				return result;
			}
			else
			{
				return 0;
			}
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}

	public Integer getActiveServiceCount() throws ManagerDAOException
	{
		try
		{
			Integer result = (Integer) this.getSqlMapClient().queryForObject(Constants.QUERY_ACTIVE_SERVICE_COUNT);
			if(result != null)
			{
				return result;
			}
			else
			{
				return 0;
			}
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}

	public Integer getNodeCount() throws ManagerDAOException
	{
		try
		{
			Integer result = (Integer) this.getSqlMapClient().queryForObject(Constants.QUERY_NODE_COUNT);
			if(result != null)
			{
				return result;
			}
			else
			{
				return 0;
			}
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}

	public Integer getPolicyCount() throws ManagerDAOException
	{
		try
		{
			Integer result = (Integer) this.getSqlMapClient().queryForObject(Constants.QUERY_POLICY_COUNT);
			if(result != null)
			{
				return result;
			}
			else
			{
				return 0;
			}
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}

	public Integer getServiceCount() throws ManagerDAOException
	{
		try
		{
			Integer result = (Integer) this.getSqlMapClient().queryForObject(Constants.QUERY_SERVICE_COUNT);
			if(result != null)
			{
				return result;
			}
			else
			{
				return 0;
			}
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}

	public Integer getDescID(Integer entID, String descriptor) throws ManagerDAOException
	{
		Map<String, Object> queryParameters = new HashMap<String, Object>();
		queryParameters.put(Constants.FIELD_ENT_ID, entID);
		queryParameters.put(Constants.FIELD_DESCRIPTOR_TYPE_ID, descriptor);

		try
		{
			Integer result = (Integer) this.getSqlMapClient().queryForObject(Constants.QUERY_SERVICE_DESCRIPTOR_ID, queryParameters);
			return result;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get service descriptor from data repository \n"
					+ e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}

	public String queryServiceHost(Integer entID) throws ManagerDAOException
	{
		try
		{
			String result = (String) this.getSqlMapClient().queryForObject(Constants.QUERY_ENTITY_HOST, entID);
			return result;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get service host (ENTITY_DESCRIPTORS.ENTITYHOST from data repository for entID " + entID + " \n"
					+ e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO#queryActiveEntities()
	 */
	public List<Integer> queryServices() throws ManagerDAOException
	{
		List<Integer> activeEntities = new ArrayList<Integer>();
		try
		{
			activeEntities = this.getSqlMapClient().queryForList(Constants.QUERY_SERVICES_LIST, null);
			return activeEntities;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get active services from data repository \n"
					+ e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}
	
	public List<Map<String, Object>> queryServicesCloseExpiry() throws ManagerDAOException
	{
		List<Map<String, Object>> services = new ArrayList<Map<String, Object>>();
		try
		{
			services = this.getSqlMapClient().queryForList(Constants.QUERY_SERVICES_CLOSE_EXPIRY);
			return services;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get services close to expiry \n"
					+ e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}
	
	public List<Map<String, Object>> queryRecentNodeStartup() throws ManagerDAOException
	{
		List<Map<String, Object>> services = new ArrayList<Map<String, Object>>();
		try
		{
			services = this.getSqlMapClient().queryForList(Constants.QUERY_RECENT_NODE_STARTUP);
			return services;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get recent startups"
					+ e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}
	
	public List<Map<String, Object>> queryRecentServiceNodeStartup(Integer entID) throws ManagerDAOException
	{
		List<Map<String, Object>> services = new ArrayList<Map<String, Object>>();
		try
		{
			services = this.getSqlMapClient().queryForList(Constants.QUERY_RECENT_SERVICE_NODE_STARTUP, entID);
			return services;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get recent startups"
					+ e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO#queryServiceContacts(java.lang.String)
	 */
	public List<Map<String, Object>> queryServiceContacts(Integer entID) throws ManagerDAOException
	{
		List<Map<String, Object>> contacts = new ArrayList<Map<String, Object>>();
		try
		{
			contacts = this.getSqlMapClient().queryForList(Constants.QUERY_SERVICE_CONTACTS, entID);
			return contacts;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get service contacts from data repository \n"
					+ e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO#queryServiceDescription(java.lang.String)
	 */
	public Map<String, Object> queryServiceDescription(Integer entID) throws ManagerDAOException
	{
		Map<String, Object> description = new HashMap<String, Object>();
		try
		{
			description = (Map<String, Object>) this.getSqlMapClient().queryForObject(Constants.QUERY_SERVICE_DESCRIPTION, entID);
			return description;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get service description from data repository \n"
					+ e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO#queryServiceDescriptor(java.lang.String)
	 */ 
	public Map<String, Object> queryServiceDescriptor(Integer entID) throws ManagerDAOException
	{
		Map<String, Object> spDescriptor = new HashMap<String, Object>();
		Map<String, Object> queryParameters = new HashMap<String, Object>();
		queryParameters.put(Constants.FIELD_ENT_ID, entID);
		queryParameters.put(Constants.FIELD_DESCRIPTOR_TYPE_ID, Constants.SP_DESCRIPTOR);

		try
		{
			spDescriptor = (Map<String, Object>) this.getSqlMapClient().queryForObject(Constants.QUERY_SERVICE_DESCRIPTOR, queryParameters);
			return spDescriptor;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get service descriptor from data repository \n"
					+ e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}	

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO#queryServiceDetails(java.lang.String)
	 */
	public Map<String, Object> queryServiceDetails(Integer entID) throws ManagerDAOException
	{
		Map<String, Object> serviceDetails = new HashMap<String, Object>();
		try
		{
			serviceDetails = (Map<String, Object>) this.getSqlMapClient().queryForObject(Constants.QUERY_SERVICE_DETAILS, entID);
			return serviceDetails;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get service details from data repository \n"
					+ e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO#queryServiceNodes(java.lang.String)
	 */
	public List<Map<String, Object>> queryServiceNodes(Integer descID) throws ManagerDAOException
	{
		List<Map<String, Object>> serviceNodes = new ArrayList<Map<String, Object>>();
		try
		{
			serviceNodes = this.getSqlMapClient().queryForList(Constants.QUERY_SERVICE_NODES, descID);
			return serviceNodes;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get service nodes from data repository \n"
					+ e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}
	
	public List<Map<String, Object>> queryKeyStoreDetails(Integer descID) throws ManagerDAOException
	{
		List<Map<String, Object>> keyStoreDetails = new ArrayList<Map<String, Object>>();
		try
		{
			keyStoreDetails = this.getSqlMapClient().queryForList(Constants.QUERY_KEYSTORE_DETAILS, descID);
			return keyStoreDetails;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get keystore details from data repository \n"
					+ e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO#queryKeystoreBinary(java.lang.String)
	 */
	public List<Map<String, Object>> queryKeystoreBinary(Integer descID) throws ManagerDAOException
	{
		List<Map<String, Object>> keyStoreData;

		try
		{
			keyStoreData = this.getSqlMapClient().queryForList(Constants.QUERY_KEYSTORE, descID);
			return keyStoreData;
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}
	
	public List<Map<String, Object>> queryServicePolicies(Integer entID) throws ManagerDAOException
	{
		List<Map<String, Object>> policyData;
		try
		{
			policyData = this.getSqlMapClient().queryForList(Constants.QUERY_ACTIVE_AUTHORIZATION_POLICIES, entID);
			return policyData;
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
		
	}
	
	public List<Map<String, Object>> queryServicePolicy(Integer entID, String policyID) throws ManagerDAOException
	{
		List<Map<String, Object>> policyData;
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_ENT_ID, entID);
		parameters.put(Constants.FIELD_POLICY_ID, policyID);
		
		try
		{
			policyData = this.getSqlMapClient().queryForList(Constants.QUERY_AUTHORIZATION_POLICY, parameters);
			return policyData;
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO#insertDescriptor(java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	public void insertDescriptor(Integer entID, Integer descID, String descriptorID, byte[] descriptorXML,
			String descriptorTypeID) throws ManagerDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_ENT_ID, entID);
		parameters.put(Constants.FIELD_DESC_ID, descID);
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
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO#insertEntityDescriptor(java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String, char, java.lang.String)
	 */
	public void insertEntityDescriptor(Integer entID, String entityID, String entityHost, String organizationName,
			String organizationDisplayName, String organizationURL, String activeFlag)
			throws ManagerDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_ENT_ID, entID);
		parameters.put(Constants.FIELD_ENTITY_ID, entityID);
		parameters.put(Constants.FIELD_ENTITY_HOST, entityHost);
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
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO#insertPKIData(java.lang.String, java.util.Date, byte[],
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public void insertPKIData(Integer descID, Date expiryDate, byte[] keyStore, String keyStorePassphrase,
			String keyPairName, String keyPairPassphrase) throws ManagerDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_DESC_ID, descID);
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
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO#insertServiceContacts(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void insertServiceContact(Integer entID, String contactID, String contactType, String company, String givenName,
			String surname, String emailAddress, String telephoneNumber) throws ManagerDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_ENT_ID, entID);
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
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO#insertServiceDescription(java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void insertServiceDescription(Integer entID, String serviceName, String serviceURL,
			String serviceDescription, String serviceAuthzFailureMsg) throws ManagerDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_ENT_ID, entID);
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
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}

	}

	public void insertServiceNode(String endpointID, Integer descID, String nodeURL,
			String assertionConsumerEndpoint, String singleLogoutEndpoint, String cacheClearEndpoint)
			throws ManagerDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_ENDPOINT_ID, endpointID);
		parameters.put(Constants.FIELD_DESC_ID, descID);
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
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}

	public void insertServiceAuthorizationPolicy(Integer entID, String policyID, byte[] lxacmlPolicy) throws ManagerDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_ENT_ID, entID);
		parameters.put(Constants.FIELD_LXACML_POLICY_ID, policyID);
		parameters.put(Constants.FIELD_LXACML_POLICY, lxacmlPolicy);
		
		try
		{
			this.getSqlMapClient().insert(Constants.INSERT_SERVICE_AUTHORIZATION_POLICY, parameters);
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}		
	}

	public void insertServiceAuthorizationShuntedPolicy(Integer entID, byte[] lxacmlPolicy, Date insertTime) throws ManagerDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_DESC_ID, entID);
		parameters.put(Constants.FIELD_LXACML_POLICY, lxacmlPolicy);
		parameters.put(Constants.FIELD_LXACML_DATE_INSERTED, insertTime);
		
		try
		{
			this.getSqlMapClient().insert(Constants.INSERT_SERVICE_AUTHORIZATION_SHUNTED_POLICY, parameters);
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}		
	}
	
	public void insertPublicKey(Integer DESC_IC, Date expiryDate, String keyPairName, String issuerDN, String serialNumber, byte[] publicKey) throws ManagerDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_DESC_ID, DESC_IC);
		parameters.put(Constants.FIELD_PK_EXPIRY_DATE, expiryDate);
		parameters.put(Constants.FIELD_PK_KEYPAIR_NAME, keyPairName);
		parameters.put(Constants.FIELD_PK_ISSUER, issuerDN);
		parameters.put(Constants.FIELD_PK_SERIAL, serialNumber);
		parameters.put(Constants.FIELD_PK_BINARY, publicKey);
		
		try
		{
			this.getSqlMapClient().insert(Constants.INSERT_DESCRIPTOR_PUBLIC_KEY, parameters);
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}		
	}

	public void updateServiceContact(Integer entID, String contactID, String contactType, String company, String givenName, String surname, String emailAddress, String telephoneNumber) throws ManagerDAOException
	{
		int count;
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_ENT_ID, entID);
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
				this.logger.error("Update staement changed to many rows for entID " + entID + " and contactID " + contactID);
				throw new ManagerDAOException("Update staement changed to many rows for entID " + entID + " and contactID " + contactID);
			}
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}		
	}
	
	public void updateServiceActiveState(Integer entID, String state) throws ManagerDAOException
	{
		int count;
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_ENT_ID, entID);
		parameters.put(Constants.FIELD_ACTIVE_FLAG, state);
		
		try
		{
			count = this.getSqlMapClient().update(Constants.UPDATE_SERVICE_ACTIVE_STATUS, parameters);
			
			if(count != 1)
			{
				this.logger.error("Update statement changed to many rows for entID " + entID + " and new state value " + state);
				throw new ManagerDAOException("Update statement changed to many rows for entID " + entID + " and new state value " + state);
			}
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}

	public void updateServiceHost(Integer entID, String serviceHost) throws ManagerDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_ENT_ID, entID);
		parameters.put(Constants.FIELD_ENTITY_HOST, serviceHost);	
		
		try
		{
			int count = this.getSqlMapClient().update(Constants.UPDATE_ENTITY_HOST, parameters);
			
			if(count != 1)
			{
				this.logger.error("Update statement changed to many rows for entID " + entID + " and new service host value");
				throw new ManagerDAOException("Update statement changed to many rows for entID " + entID + " and new service host value");
			}
		}
		catch (SQLException e)
		{
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}

	public void updateServiceNode(Integer descID, String nodeID, String nodeURL, String nodeACS,
			String nodeSLS, String nodeCCS)  throws ManagerDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_DESC_ID, descID);
		parameters.put(Constants.FIELD_ENDPOINT_ID, nodeID);
		parameters.put(Constants.FIELD_ENDPOINT_NODEURL, nodeURL);
		parameters.put(Constants.FIELD_ENDPOINT_ASSERTIONCONSUMER, nodeACS);
		parameters.put(Constants.FIELD_ENDPOINT_SINGLELOGOUT, nodeSLS);
		parameters.put(Constants.FIELD_ENDPOINT_CACHECLEAR, nodeCCS);
		
		try
		{
			int count = this.getSqlMapClient().update(Constants.UPDATE_SERVICE_NODE, parameters);
			
			if(count != 1)
			{
				this.logger.error("Update statement changed to many rows for descID " + descID + " and new node values");
				throw new ManagerDAOException("Update statement changed to many rows for descID " + descID + " and new node values");
			}
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}

	public void updateServiceNodeActiveState(Integer descID, String nodeID, String state) throws ManagerDAOException
	{
		int count;
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_DESC_ID, descID);
		parameters.put(Constants.FIELD_ENDPOINT_ID, nodeID);
		parameters.put(Constants.FIELD_ACTIVE_FLAG, state);
		
		try
		{
			count = this.getSqlMapClient().update(Constants.UPDATE_SERVICE_NODE_ACTIVE_STATUS, parameters);
			
			if(count != 1)
			{
				this.logger.error("Update statement changed to many rows for descID " + descID + " and new state value " + state);
				throw new ManagerDAOException("Update statement changed to many rows for descID " + descID + " and new state value " + state);
			}
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}
	
	public void updateServicePolicyActiveState(Integer entID, String policyID, String state) throws ManagerDAOException
	{
		int count;
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_ENT_ID, entID);
		parameters.put(Constants.FIELD_POLICY_ID, policyID);
		parameters.put(Constants.FIELD_ACTIVE_FLAG, state);
		
		try
		{
			count = this.getSqlMapClient().update(Constants.UPDATE_SERVICE_POLICY_ACTIVE_STATUS, parameters);
			
			if(count != 1)
			{
				this.logger.error("Update statement changed to many rows for entID " + entID + " and new state value " + state);
				throw new ManagerDAOException("Update statement changed to many rows for entID " + entID + " and new state value " + state);
			}
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}
	
	public void updateServiceDescription(Integer entID, String serviceName, String serviceURL, String serviceDescription, String serviceAuthzFailureMsg) throws ManagerDAOException
	{
		int count;
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_ENT_ID, entID);
		parameters.put(Constants.FIELD_SERVICE_NAME, serviceName);
		parameters.put(Constants.FIELD_SERVICE_URL, serviceURL);
		parameters.put(Constants.FIELD_SERVICE_DESC, serviceDescription);
		parameters.put(Constants.FIELD_SERVICE_AUTHZ_FAIL, serviceAuthzFailureMsg);
		
		try
		{
			count = this.getSqlMapClient().update(Constants.UPDATE_SERVICE_DESCRIPTION, parameters);
			
			if(count != 1)
			{
				this.logger.error("Update statement changed to many rows for entID " + entID + " new service details");
				throw new ManagerDAOException("Update statement changed to many rows for entID " + entID + " new service details");
			}
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}
	
	public void updateServiceAuthorizationPolicy(Integer entID, String policyID, byte[] lxacmlPolicy) throws ManagerDAOException
	{
		int count;
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_ENT_ID, entID);
		parameters.put(Constants.FIELD_LXACML_POLICY_ID, policyID);
		parameters.put(Constants.FIELD_LXACML_POLICY, lxacmlPolicy);
		
		try
		{
			count = this.getSqlMapClient().update(Constants.UPDATE_SERVICE_AUTHORIZATION_POLICY, parameters);
			if(count != 1)
			{
				this.logger.error("LXACML Policy update statement changed " + count + " rows for entID " + entID + " this is invalid, 1 row should be modified");
				throw new ManagerDAOException("LXACML Policy update statement changed " + count + " rows for entID " + entID + " this is invalid, 1 row should be modified");
			}
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}		
	}
	
	public void deleteServiceContact(Integer entID, String contactID) throws ManagerDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_ENT_ID, entID);
		parameters.put(Constants.FIELD_CONTACT_ID, contactID);

		try
		{
			this.getSqlMapClient().delete(Constants.DELETE_SERVICE_CONTACT, parameters);
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}		
	}
	
	public void deleteServicePolicy(Integer entID, String policyID) throws ManagerDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_ENT_ID, entID);
		parameters.put(Constants.FIELD_POLICY_ID, policyID);

		try
		{
			this.getSqlMapClient().delete(Constants.DELETE_SERVICE_POLICY, parameters);
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}		
	}
	
	public void deleteServiceKey(Integer descID, String keypairName) throws ManagerDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_DESC_ID, descID);
		parameters.put(Constants.FIELD_PK_KEYPAIR_NAME, keypairName);
		parameters.put(Constants.FIELD_PKI_KEYPAIRNAME, keypairName);
		
		try
		{
			this.getSqlMapClient().delete(Constants.DELETE_SERVICE_KEYPAIR, parameters);
			this.getSqlMapClient().delete(Constants.DELETE_SERVICE_KEYSTORE, parameters);
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}

	public Map<String, byte[]> queryActiveAttributePolicy(Integer entID) throws ManagerDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_ENT_ID, entID);
		
		try
		{
			return (Map<String, byte[]>) this.getSqlMapClient().queryForObject(Constants.QUERY_ACTIVE_ATTRIBUTE_POLICY, parameters);
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}
	}

	public void updateActiveAttributePolicy(Integer entID, byte[] attributePolicy) throws ManagerDAOException
	{
		int count;
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_ENT_ID, entID);
		parameters.put(Constants.FIELD_ATTRIBUTE_POLICY, attributePolicy);
		
		try
		{
			count = this.getSqlMapClient().update(Constants.UPDATE_ATTRIBUTE_POLICY, parameters);
			if(count != 1)
			{
				this.logger.error("Attribute Policy update statement changed " + count + " rows for entID " + entID + " this is invalid, 1 row should be modified");
				throw new ManagerDAOException("Attribute Policy update statement changed " + count + " rows for entID " + entID + " this is invalid, 1 row should be modified");
			}
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new ManagerDAOException(e.getLocalizedMessage(), e);
		}				
	}
}
