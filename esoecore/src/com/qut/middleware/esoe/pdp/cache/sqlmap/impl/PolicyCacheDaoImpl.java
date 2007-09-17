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

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

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
	public long queryLastSequenceId() throws SQLException
	{
		long sequenceId = 0l;
		
		BigDecimal seq = (BigDecimal)(this.getSqlMapClient().queryForObject("policyCacheQueryLatestUpdate", null)); //$NON-NLS-1$
		
		if(seq != null)
			return seq.longValue();
		else 
			return sequenceId;
	}
	
	/**
	 * Retrieves policies based on the data contained in the given query object. If the value of getSequenceId() in the query data
	 * is greater or equal to 0, only policies that have a sequence ID greater than the retrieved value of getSequenceId() are returned. 
	 * If the value of getSequenceId() is less or equal to 0, ALL policies are retrieved. <br>
	 * 
	 * NOTE: Inactive services are not returned. Ie:  where ENTITY_DESCRIPTORS.ACTIVEFLAG = 'N'.
	 * 
	 * @param queryData The query data to use when querying the policy cache.
	 * @return A List of PolicyCacheData objects for all active entities in the data source.
	 * 
	 * @throws SQLException if an error occurs retrieving the data.
	 */
	@SuppressWarnings("unchecked")
	public List<PolicyCacheData> queryPolicyCache(PolicyCacheQueryData queryData) throws SQLException
	{
		List<PolicyCacheData> result = null;
		
		// sequence Id's in the data store must start at 0
		if(queryData != null && queryData.getSequenceId().longValue() >= 0)
			result = this.getSqlMapClient().queryForList("updatedPoliciesCacheQuery", queryData); //$NON-NLS-1$
		else
			result = this.getSqlMapClient().queryForList("allPoliciesCacheQuery", null); //$NON-NLS-1$ //
	
		return result;
	}
}
