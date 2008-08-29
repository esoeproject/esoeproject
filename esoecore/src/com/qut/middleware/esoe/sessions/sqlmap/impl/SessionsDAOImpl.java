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
 * Creation Date: 22/8/2007
 * 
 * Purpose: Implements the
 */
package com.qut.middleware.esoe.sessions.sqlmap.impl;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.qut.middleware.esoe.sessions.exception.SessionsDAOException;
import com.qut.middleware.esoe.sessions.sqlmap.SessionsDAO;

public class SessionsDAOImpl extends SqlMapClientDaoSupport implements SessionsDAO
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.sessions.sqlmap.SessionsDAO#selectActiveAttributePolicy(java.lang.String)
	 */
	public byte[] selectActiveAttributePolicy(Integer entID) throws SessionsDAOException
	{
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("ENT_ID", entID);

		List<Map<String, byte[]>> results;
		try
		{
			results = this.getSqlMapClient().queryForList("getActiveAttributePolicy", parameters);

			if (results.size() == 1)
				return results.get(0).get("attribPolicy");

			throw new SessionsDAOException("0 or more then 1 results returned for entityID, should only be 1 entry");
		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new SessionsDAOException(e.getLocalizedMessage(), e);
		}
	}

	public Integer getEntID(String entityID) throws SessionsDAOException
	{
		try
		{
			Integer result = (Integer) this.getSqlMapClient().queryForObject("getEntID", entityID);
			if (result != null)
			{
				return result;
			}
			else
			{
				throw new SessionsDAOException("No value for ENT_ID mapping for supplied entityID of " + entityID + " could be established");
			}

		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new SessionsDAOException(e.getLocalizedMessage(), e);
		}
	}

}
