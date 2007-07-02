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
 * Author: Shaun Mangelsdorf
 * Creation Date: 17/11/2006
 * 
 * Purpose: Creates and maintains an instance of SqlMapClient
 */
package com.qut.middleware.esoe.pdp.cache.sqlmap.impl;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.qut.middleware.esoe.pdp.cache.sqlmap.PolicyCacheDao;


public class PolicyCacheDaoImpl extends SqlMapClientDaoSupport implements PolicyCacheDao 
{	
	/**
	 * @return The date/time the policy cache was last updated, else null if no records exist.
	 * 
	 * @throws SQLException if an error occurs retrieving the data.
	 */
	@SuppressWarnings("nls")
	public Date queryDateLastUpdated() throws SQLException
	{
		Date lastUpdated = null;
		
		lastUpdated = (Date)this.getSqlMapClient().queryForObject("policyCacheQueryLatestUpdate", null); //$NON-NLS-1$
		
		return lastUpdated;
	}
	
	/**
	 * Retrieves policies based on a query. If the date in the query data is specified, only policies
	 * updated after the given date/time are returned. If the queryData object does not contain a 
	 * lastUpdated time stamp, ALL policies are retrieved. NOTE: inactive services are not returned. Ie:
	 * where ENTITY_DESCRIPTORS.ACTIVEFLAG = 'N'.
	 * 
	 * @param queryData The query data to use when querying the policy cache
	 * @return A Map of descriptorID -> PolicyCacheData objects for all active entities in the data source.
	 * 
	 * @throws SQLException if an error occurs retrieving the data.
	 */
	@SuppressWarnings("unchecked")
	public Map<String,PolicyCacheData> queryPolicyCache(PolicyCacheQueryData queryData) throws SQLException
	{
		Map<String,PolicyCacheData> result = null;
		
		if(queryData != null && queryData.getDateLastUpdated() != null)
			result = this.getSqlMapClient().queryForMap("updatedPoliciesCacheQuery", queryData, "descriptorID"); //$NON-NLS-1$ //$NON-NLS-2$
		else
			result = this.getSqlMapClient().queryForMap("allPoliciesCacheQuery", null, "descriptorID"); //$NON-NLS-1$ //$NON-NLS-2$
	
		return result;
	}
}
