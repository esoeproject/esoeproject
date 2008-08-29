package com.qut.middleware.esoe.authz.cache.sqlmap;
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
import java.util.List;

import com.qut.middleware.esoe.authz.cache.sqlmap.impl.PolicyCacheData;
import com.qut.middleware.esoe.authz.cache.sqlmap.impl.PolicyCacheQueryData;

public interface PolicyCacheDao 
{

	/** Query the sequence id value for policies stored in the data store.
	 * 
	 * @return The highest value sequenceId in the data source, representing the latest updated Policy.
	 * @throws SQLException if an error occurs communicating with the data store.
	 */
	public long queryLastSequenceId() throws SQLException;
	
	
	/**
	 * Retrieves policies based on the data contained in the given query data object. 
	 * 
	 * @param queryData The query data to use when querying the policy cache.
	 * @return The Policy data of Policies matching the query.
	 * @throws SQLException if an error occurs communicating with the data store.
	 */
	public List<PolicyCacheData> queryPolicyCache(PolicyCacheQueryData queryData) throws SQLException;
	
}
