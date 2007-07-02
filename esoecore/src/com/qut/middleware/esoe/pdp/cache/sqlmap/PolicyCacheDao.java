package com.qut.middleware.esoe.pdp.cache.sqlmap;
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
 * Author: Andre Zitelli
 * Creation Date: 7/12/2006
 * 
 * Purpose: An interface for retrieval of LXACML Policy data from a data source.
 */


import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import com.qut.middleware.esoe.pdp.cache.sqlmap.impl.PolicyCacheData;
import com.qut.middleware.esoe.pdp.cache.sqlmap.impl.PolicyCacheQueryData;

public interface PolicyCacheDao 
{

	/**
	 * @return The date/time the policy cache was last updated
	 * @throws SQLException
	 */
	public Date queryDateLastUpdated() throws SQLException;
	
	
	/**
	 * Retrieves policies based on a query. If the date in the query data is specified, only policies
	 * updated after the given date/time are returned. If the date is not specified, all policies are
	 * returned.
	 * @param queryData The query data to use when querying the policy cache
	 * @return The policies matching the query
	 * @throws SQLException
	 */
	@SuppressWarnings("unchecked")
	public Map<String,PolicyCacheData> queryPolicyCache(PolicyCacheQueryData queryData) throws SQLException;
	
}
