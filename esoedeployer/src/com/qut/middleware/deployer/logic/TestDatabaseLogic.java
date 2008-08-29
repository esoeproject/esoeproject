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
 */
package com.qut.middleware.deployer.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.qut.middleware.deployer.Constants;
import com.qut.middleware.deployer.exception.TestDatabaseException;

public class TestDatabaseLogic
{
	private JdbcTemplate jdbcTemplate;
	
	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(TestDatabaseLogic.class.getName());
	
	public TestDatabaseLogic()
	{

	}

	public void testDatabase(Constants.DatabaseDrivers driver, String url, String username, String password) throws TestDatabaseException
	{
		try
		{
			DriverManagerDataSource dataSource = new DriverManagerDataSource();
			
			switch (driver)
			{
				case mysql: dataSource.setDriverClassName(Constants.MYSQL_DRIVER);
						break;
				case oracle: dataSource.setDriverClassName(Constants.ORACLE_DRIVER);
						break;
						 /* Attempt to connect to mysql source if not submitted */
				default: dataSource.setDriverClassName(Constants.MYSQL_DRIVER);
			}
			
			dataSource.setUrl(url);
			dataSource.setUsername(username);
			dataSource.setPassword(password);
			
			this.jdbcTemplate = new JdbcTemplate(dataSource);
			
			this.jdbcTemplate.execute(Constants.ALIVE_QUERY);
		}
		catch(DataAccessException e)
		{
			this.logger.error("Unable to connect with database " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new TestDatabaseException(e.getLocalizedMessage(), e);
		}
	}
}
