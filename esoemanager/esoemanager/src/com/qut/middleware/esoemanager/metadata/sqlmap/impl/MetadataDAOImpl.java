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
package com.qut.middleware.esoemanager.metadata.sqlmap.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import com.qut.middleware.esoemanager.Constants;
import com.qut.middleware.esoemanager.exception.MetadataDAOException;
import com.qut.middleware.esoemanager.metadata.sqlmap.MetadataDAO;

@Transactional
//See -
//http://static.springframework.org/spring/docs/2.0.x/reference/transaction.html#transaction-declarative-annotations
public class MetadataDAOImpl extends SqlMapClientDaoSupport implements MetadataDAO
{

	private Logger logger = LoggerFactory.getLogger(MetadataDAOImpl.class.getName());

	public String getEntityID(Integer entID) throws MetadataDAOException
	{
		try
		{
			String result = (String) this.getSqlMapClient().queryForObject(Constants.QUERY_ENTITY_ID, entID);
			if (result != null)
			{
				return result;
			}
			else
			{
				throw new MetadataDAOException("No value for entityID mapping for supplied entID of " + entID
						+ " could be established");
			}

		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new MetadataDAOException(e.getLocalizedMessage(), e);
		}
	}
	
	public Map<String, Object> queryServiceDetails(Integer entID) throws MetadataDAOException
	{
		Map<String, Object> description = new HashMap<String, Object>();
		try
		{
			description = (Map<String, Object>) this.getSqlMapClient().queryForObject(Constants.QUERY_SERVICE_DETAILS, entID);
			return description;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get service description from data repository \n"
					+ e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new MetadataDAOException(e.getLocalizedMessage(), e);
		}
	}

	public List<Integer> queryActiveEntities() throws MetadataDAOException
	{
		List<Integer> activeEntities = new ArrayList<Integer>();
		try
		{
			activeEntities = this.getSqlMapClient().queryForList(Constants.QUERY_ACTIVE_ENTITY_LIST, null);
			return activeEntities;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get active entities from data repository \n"
					+ e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new MetadataDAOException(e.getLocalizedMessage(), e);
		}
	}

	public List<Map<String, Object>> queryAttributeAuthorityDescriptor(Integer entID) throws MetadataDAOException
	{
		List<Map<String, Object>> attributeAuthorities = new ArrayList<Map<String, Object>>();
		Map<String, Object> queryParameters = new HashMap<String, Object>();
		queryParameters.put(Constants.FIELD_ENT_ID, entID);
		queryParameters.put(Constants.FIELD_DESCRIPTOR_TYPE_ID, Constants.ATTRIBUTE_AUTHORITY_DESCRIPTOR);
		try
		{
			attributeAuthorities = this.getSqlMapClient().queryForList(Constants.QUERY_ATTRIBUTE_AUTHORITY_LIST,
					queryParameters);
			return attributeAuthorities;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get attribute authorities from data repository \n"
					+ e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new MetadataDAOException(e.getLocalizedMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.qut.middleware.esoemanager.metadata.sqlmap.MetadataDAO#queryContacts
	 * (java.lang.String)
	 */
	public List<Map<String, String>> queryContacts(Integer entID) throws MetadataDAOException
	{
		List<Map<String, String>> contacts = new ArrayList<Map<String, String>>();
		try
		{
			contacts = this.getSqlMapClient().queryForList(Constants.QUERY_CONTACTS, entID);
			return contacts;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get entity contacts from data repository \n"
					+ e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new MetadataDAOException(e.getLocalizedMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.qut.middleware.esoemanager.metadata.sqlmap.MetadataDAO#queryIDPDescriptor
	 * (java.lang.String)
	 */
	public List<Map<String, Object>> queryIDPDescriptor(Integer entID) throws MetadataDAOException
	{
		List<Map<String, Object>> idpDescriptors = new ArrayList<Map<String, Object>>();
		Map<String, Object> queryParameters = new HashMap<String, Object>();
		queryParameters.put(Constants.FIELD_ENT_ID, entID);
		queryParameters.put(Constants.FIELD_DESCRIPTOR_TYPE_ID, Constants.IDP_DESCRIPTOR);

		try
		{
			idpDescriptors = this.getSqlMapClient().queryForList(Constants.QUERY_IDP_LIST, queryParameters);
			return idpDescriptors;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get IDP descriptors from data repository \n"
					+ e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new MetadataDAOException(e.getLocalizedMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.qut.middleware.esoemanager.metadata.sqlmap.MetadataDAO#
	 * queryLXACMLPDPDescriptor(java.lang.String)
	 */
	public List<Map<String, Object>> queryLXACMLPDPDescriptor(Integer entID) throws MetadataDAOException
	{
		List<Map<String, Object>> lxacmlDescriptors = new ArrayList<Map<String, Object>>();
		Map<String, Object> queryParameters = new HashMap<String, Object>();
		queryParameters.put(Constants.FIELD_ENT_ID, entID);
		queryParameters.put(Constants.FIELD_DESCRIPTOR_TYPE_ID, Constants.LXACML_PDP_DESCRIPTOR);
		try
		{
			lxacmlDescriptors = this.getSqlMapClient().queryForList(Constants.QUERY_LXACMLPDP_LIST, queryParameters);
			return lxacmlDescriptors;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get LXACML descriptors from data repository \n"
					+ e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new MetadataDAOException(e.getLocalizedMessage(), e);
		}
	}

	public List<Map<String, Object>> queryDescriptorActivePublicKeys(Integer descID) throws MetadataDAOException
	{
		List<Map<String, Object>> keyData = new ArrayList<Map<String, Object>>();

		try
		{
			keyData = this.getSqlMapClient().queryForList(Constants.QUERY_DESCRIPTOR_PUBLIC_KEYS, descID);
			return keyData;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get SP descriptors from data repository \n"
					+ e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new MetadataDAOException(e.getLocalizedMessage(), e);
		}
	}

	public List<Map<String, Object>> queryActiveServiceNodes(Integer descID) throws MetadataDAOException
	{
		List<Map<String, Object>> serviceNodes = new ArrayList<Map<String, Object>>();
		try
		{
			serviceNodes = this.getSqlMapClient().queryForList(Constants.QUERY_ACTIVE_SERVICE_NODES, descID);
			return serviceNodes;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get service nodes from data repository \n"
					+ e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new MetadataDAOException(e.getLocalizedMessage(), e);
		}
	}

	public List<Map<String, Object>> querySupportedNameIDFormats(Integer descID) throws MetadataDAOException
	{
		List<Map<String, Object>> supportedFormats = new ArrayList<Map<String, Object>>();
		try
		{
			supportedFormats = this.getSqlMapClient().queryForList(Constants.QUERY_SUPPORTED_NAMEID_FORMATS, descID);
			return supportedFormats;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get supported name id formats from data repository \n"
					+ e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new MetadataDAOException(e.getLocalizedMessage(), e);
		}
	}

	public List<Map<String, Object>> querySPList(Integer entID) throws MetadataDAOException
	{
		List<Map<String, Object>> spList = new ArrayList<Map<String, Object>>();
		try
		{
			spList = this.getSqlMapClient().queryForList(Constants.QUERY_SP_LIST, entID);
			return spList;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get list of service providers for entityID \n"
					+ e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new MetadataDAOException(e.getLocalizedMessage(), e);
		}
	}

	public void updateDescriptor(Integer descID, String descriptorID, byte[] descriptorXML) throws MetadataDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.FIELD_DESC_ID, descID);
		parameters.put(Constants.FIELD_DESCRIPTOR_ID, descriptorID);
		parameters.put(Constants.FIELD_DESCRIPTOR_XML, descriptorXML);

		try
		{
			this.getSqlMapClient().update(Constants.UPDATE_DESCRIPTOR, parameters);
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to update descriptors \n" + e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new MetadataDAOException(e.getLocalizedMessage(), e);
		}
	}
}
