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
 * Creation Date: 03/05/2007
 * 
 * Purpose: Dynamic LOB handler determination based on data source, automatically injects DefaultLobHandler or OracleLobHandler, could be extended to others.
 * In this version we have to play a little dirty because we don't know the datasource type at creation time, so when asked for get object we execute the original "afterPropertiesSet" method
 * in SqlMapClientFactoryBean, (this naturally assumes by this time that full DataSource setup has occured by the application) and return, caching for subsequent calls.
 */
package com.qut.middleware.esoestartup;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.OracleLobHandler;
import org.springframework.orm.ibatis.SqlMapClientFactoryBean;

public class DynamicSqlMapClientFactoryBean extends SqlMapClientFactoryBean
{
	BasicDataSource dataSource;
	OracleLobHandler oracleLobHandler;
	DefaultLobHandler defaultLobHandler;

	/* Local logging instance */
	private Logger logger = Logger.getLogger(DynamicSqlMapClientFactoryBean.class.getName());

	public DynamicSqlMapClientFactoryBean(BasicDataSource dataSource, OracleLobHandler oracleLobHandler,
			DefaultLobHandler defaultLobHandler)
	{
		if (dataSource == null)
			throw new IllegalArgumentException("Invalid data source");

		if (oracleLobHandler == null)
			throw new IllegalArgumentException("Invalid oracleLobHandler");

		if (defaultLobHandler == null)
			throw new IllegalArgumentException("Invalid defaultLobHandler");

		this.dataSource = dataSource;
		this.oracleLobHandler = oracleLobHandler;
		this.defaultLobHandler = defaultLobHandler;
		
		return;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
	}

	@Override
	public Object getObject()
	{
		try
		{
			if (super.getObject() != null)
				return getObject();

			synchronized (DynamicSqlMapClientFactoryBean.class)
			{
				if (dataSource.getDriverClassName().contains("oracle"))
				{
					this.logger.info("Setting database LOB handler to ORACLE");
					setDataSource(dataSource);
					setLobHandler(oracleLobHandler);
				}
				else
				{
					this.logger.info("Setting database LOB handler to DEFAULT");
					setDataSource(dataSource);
					setLobHandler(defaultLobHandler);
				}
				
				super.afterPropertiesSet();
				return super.getObject();
			}
		}
		catch (Exception e)
		{
			this.logger.fatal("Exception while retrieving sqlMap");
			return null;
		}
	}
}
