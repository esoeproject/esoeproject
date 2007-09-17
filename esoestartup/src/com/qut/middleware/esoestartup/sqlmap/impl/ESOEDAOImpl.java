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
 * Purpose: ESOE DAO implementation
 */
package com.qut.middleware.esoestartup.sqlmap.impl;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import com.qut.middleware.esoemanager.Constants;
import com.qut.middleware.esoemanager.exception.SPEPDAOException;
import com.qut.middleware.esoestartup.exception.ESOEDAOException;
import com.qut.middleware.esoestartup.sqlmap.ESOEDAO;

@Transactional
//See -
//http://static.springframework.org/spring/docs/2.0.x/reference/transaction.html#transaction-declarative-annotations
public class ESOEDAOImpl extends SqlMapClientDaoSupport implements ESOEDAO
{
	/* Local logging instance */
	private Logger logger = Logger.getLogger(ESOEDAOImpl.class.getName());
	

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoestartup.esoe.sqlmap.ESOEDAO#getNextEntID()
	 */
	public Integer getNextEntID() throws ESOEDAOException
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
				throw new ESOEDAOException("No value for NEXT_ENT_ID could be established");
			}
			
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new ESOEDAOException(e.getLocalizedMessage(), e);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.qut.middleware.esoestartup.esoe.sqlmap.ESOEDAO#getNextDescID()
	 */
	public Integer getNextDescID() throws ESOEDAOException
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
				throw new ESOEDAOException("No value for NEXT_DESC_ID could be established");
			}
			
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new ESOEDAOException(e.getLocalizedMessage(), e);
		}
	}

	public void insertDescriptor(Integer entID, Integer descID, String descriptorID, byte[] descriptorXML,
			String descriptorTypeID) throws ESOEDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_DESC_ID, descID);
		parameters.put(Constants.FIELD_DESCRIPTOR_ID, descriptorID);
		parameters.put(Constants.FIELD_ENT_ID, entID);
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
			throw new ESOEDAOException(e.getLocalizedMessage(), e);
		}

	}

	public void insertEntityDescriptor(Integer entID, String entityID, String organizationName,
			String organizationDisplayName, String organizationURL, String activeFlag)
			throws ESOEDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_ENT_ID, entID);
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
			throw new ESOEDAOException(e.getLocalizedMessage(), e);
		}
	}

	public void insertPKIData(Integer descID, Date expiryDate, byte[] keyStore, String keyStorePassphrase,
			String keyPairName, String keyPairPassphrase) throws ESOEDAOException
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
			this.logger.debug(e);
			throw new ESOEDAOException(e.getLocalizedMessage(), e);
		}
	}

	public void insertServiceContacts(Integer entID, String contactID, String contactType, String company, String givenName,
			String surname, String emailAddress, String telephoneNumber) throws ESOEDAOException
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
			this.logger.debug(e);
			throw new ESOEDAOException(e.getLocalizedMessage(), e);
		}
	}

	public void insertServiceDescription(Integer entID, String serviceName, String serviceURL,
			String serviceDescription, String serviceAuthzFailureMsg) throws ESOEDAOException
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
			this.logger.debug(e);
			throw new ESOEDAOException(e.getLocalizedMessage(), e);
		}

	}
	
	public void insertServiceAuthorizationPolicy(Integer entID, String policyID, byte[] lxacmlPolicy) throws ESOEDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_ENT_ID, entID);
		parameters.put(Constants.FIELD_POLICY_ID, policyID);
		parameters.put(Constants.FIELD_LXACML_POLICY, lxacmlPolicy);
		
		try
		{
			this.getSqlMapClient().insert(Constants.INSERT_SERVICE_AUTHORIZATION_POLICY, parameters);
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new ESOEDAOException(e.getLocalizedMessage(), e);
		}		
	}

	public void insertServiceNode(String endpointID, Integer descID, String nodeURL,
			String assertionConsumerEndpoint, String singleLogoutEndpoint, String cacheClearEndpoint)
			throws ESOEDAOException
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
			this.logger.debug(e);
			throw new ESOEDAOException(e.getLocalizedMessage(), e);
		}
	}

	public void insertAttributePolicy(Integer entID, byte[] attribPolicy) throws ESOEDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_ENT_ID, entID);
		parameters.put(Constants.FIELD_ATTRIBUTE_POLICY, attribPolicy);

		try
		{
			this.getSqlMapClient().insert(Constants.INSERT_ATTRIBUTE_POLICY, parameters);
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new ESOEDAOException(e.getLocalizedMessage(), e);
		}		
	}
	
	public void insertMetadataPKIData(Date expiryDate, byte[] keyStore, String keyStorePassphrase,
			String keyPairName, String keyPairPassphrase) throws ESOEDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_PKI_EXPIRY_DATE, expiryDate);
		parameters.put(Constants.FIELD_PKI_KEYSTORE, keyStore);
		parameters.put(Constants.FIELD_PKI_KEYSTORE_PASSPHRASE, keyStorePassphrase);
		parameters.put(Constants.FIELD_PKI_KEYPAIRNAME, keyPairName);
		parameters.put(Constants.FIELD_PKI_KEYPAIR_PASSPHRASE, keyPairPassphrase);

		try
		{
			this.getSqlMapClient().insert(Constants.INSERT_METADATA_PKI_DATA, parameters);
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new ESOEDAOException(e.getLocalizedMessage(), e);
		}
	}
	
	public void insertPublicKey(Integer DESC_IC, Date expiryDate, String keyPairName, byte[] publicKey) throws ESOEDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_DESC_ID, DESC_IC);
		parameters.put(Constants.FIELD_PK_EXPIRY_DATE, expiryDate);
		parameters.put(Constants.FIELD_PK_KEYPAIR_NAME, keyPairName);
		parameters.put(Constants.FIELD_PK_BINARY, publicKey);
		
		try
		{
			this.getSqlMapClient().insert(Constants.INSERT_DESCRIPTOR_PUBLIC_KEY, parameters);
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new ESOEDAOException(e.getLocalizedMessage(), e);
		}		
	}
}
