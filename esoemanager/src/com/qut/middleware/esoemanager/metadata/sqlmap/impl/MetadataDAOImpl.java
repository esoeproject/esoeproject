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
 * Purpose: Metadata DAO default implemenation
 */
package com.qut.middleware.esoemanager.metadata.sqlmap.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.qut.middleware.esoemanager.Constants;
import com.qut.middleware.esoemanager.metadata.sqlmap.MetadataDAO;

public class MetadataDAOImpl extends SqlMapClientDaoSupport implements MetadataDAO
{

	private Logger logger = Logger.getLogger(MetadataDAOImpl.class.getName());
	
	/* (non-Javadoc)
	 * @see com.qut.middleware.esoemanager.metadata.sqlmap.MetadataDAO#queryActiveEntities()
	 */
	public List<String> queryActiveEntities()
	{
		List<String> activeEntities = new ArrayList<String>();
		try
		{
			activeEntities = this.getSqlMapClient().queryForList(Constants.QUERY_ACTIVE_ENTITY_LIST, null);
			return activeEntities;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get active entities from data repository \n" + e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoemanager.metadata.sqlmap.MetadataDAO#queryAttributeAuthorityDescriptor(java.lang.String)
	 */
	public List<Map<String, String>> queryAttributeAuthorityDescriptor(String entityID)
	{
		List<Map<String, String>> attributeAuthorities = new ArrayList<Map<String, String>>();
		Map<String, String> queryParameters = new HashMap<String,String>();
		queryParameters.put(Constants.FIELD_ENTITY_ID, entityID);
		queryParameters.put(Constants.FIELD_DESCRIPTOR_TYPE_ID, Constants.ATTRIBUTE_AUTHORITY_DESCRIPTOR);
		try
		{
			attributeAuthorities = this.getSqlMapClient().queryForList(Constants.QUERY_ATTRIBUTE_AUTHORITY_LIST, queryParameters);
			return attributeAuthorities;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get attribute authorities from data repository \n" + e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoemanager.metadata.sqlmap.MetadataDAO#queryContacts(java.lang.String)
	 */
	public List<Map<String, String>> queryContacts(String entityID)
	{
		List<Map<String, String>> contacts = new ArrayList<Map<String, String>>();
		try
		{
			contacts = this.getSqlMapClient().queryForList(Constants.QUERY_CONTACTS, entityID);
			return contacts;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get entity contacts from data repository \n" + e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoemanager.metadata.sqlmap.MetadataDAO#queryIDPDescriptor(java.lang.String)
	 */
	public List<Map<String, String>> queryIDPDescriptor(String entityID)
	{
		List<Map<String, String>> idpDescriptors = new ArrayList<Map<String, String>>();
		Map<String, String> queryParameters = new HashMap<String,String>();
		queryParameters.put(Constants.FIELD_ENTITY_ID, entityID);
		queryParameters.put(Constants.FIELD_DESCRIPTOR_TYPE_ID, Constants.IDP_DESCRIPTOR);
		
		try
		{
			idpDescriptors = this.getSqlMapClient().queryForList(Constants.QUERY_IDP_LIST, queryParameters);
			return idpDescriptors;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get IDP descriptors from data repository \n" + e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoemanager.metadata.sqlmap.MetadataDAO#queryLXACMLPDPDescriptor(java.lang.String)
	 */
	public List<Map<String, String>> queryLXACMLPDPDescriptor(String entityID)
	{
		List<Map<String, String>>lxacmlDescriptors = new ArrayList<Map<String, String>>();
		Map<String, String> queryParameters = new HashMap<String,String>();
		queryParameters.put(Constants.FIELD_ENTITY_ID, entityID);
		queryParameters.put(Constants.FIELD_DESCRIPTOR_TYPE_ID, Constants.LXACML_PDP_DESCRIPTOR);
		try
		{
			lxacmlDescriptors = this.getSqlMapClient().queryForList(Constants.QUERY_LXACMLPDP_LIST, queryParameters);
			return lxacmlDescriptors;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get LXACML descriptors from data repository \n" + e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoemanager.metadata.sqlmap.MetadataDAO#querySPDescriptors(java.lang.String)
	 */
	public List<Map<String, String>> querySPDescriptors(String entityID)
	{
		List<Map<String, String>> spDescriptors = new ArrayList<Map<String, String>>();
		Map<String, String> queryParameters = new HashMap<String,String>();
		queryParameters.put(Constants.FIELD_ENTITY_ID, entityID);
		queryParameters.put(Constants.FIELD_DESCRIPTOR_TYPE_ID, Constants.SP_DESCRIPTOR);
		
		try
		{
			spDescriptors = this.getSqlMapClient().queryForList(Constants.QUERY_SP_LIST, queryParameters);
			return spDescriptors;
		}
		catch (SQLException e)
		{
			this.logger.error("SQL exception when attempting to get SP descriptors from data repository \n" + e.getLocalizedMessage());
			this.logger.debug(e.getLocalizedMessage(), e);
			return null;
		}
	}

}
