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
package com.qut.middleware.esoemanager;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.OracleLobHandler;
import org.springframework.orm.ibatis.SqlMapClientFactoryBean;

public class DynamicSqlMapClientFactoryBean extends SqlMapClientFactoryBean
{
	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(DynamicSqlMapClientFactoryBean.class.getName());
	
	public DynamicSqlMapClientFactoryBean(BasicDataSource dataSource, OracleLobHandler oracleLobHandler,
			DefaultLobHandler defaultLobHandler)
	{
		if (dataSource == null)
			throw new IllegalArgumentException("Invalid data source");

		if (oracleLobHandler == null)
			throw new IllegalArgumentException("Invalid oracleLobHandler");

		if (defaultLobHandler == null)
			throw new IllegalArgumentException("Invalid defaultLobHandler");

		if (dataSource.getDriverClassName().contains("oracle"))
		{
			this.logger.info("Setting database LOB handler to ORACLE");
			setDataSource(dataSource);
			setLobHandler(oracleLobHandler);
			return;
		}

		this.logger.info("Setting database LOB handler to DEFAULT");
		setDataSource(dataSource);
		setLobHandler(defaultLobHandler);
		return;
	}
}
