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
 * 
 * Author: Shaun Mangelsdorf
 * Creation Date: 16/09/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.esoe.sessions.data;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.jdbc.support.lob.OracleLobHandler;
import org.springframework.orm.ibatis.SqlMapClientFactoryBean;

import com.ibatis.common.resources.Resources;

public class ResourceLoaderSqlMapClientFactoryBean implements FactoryBean, InitializingBean
{
	private SqlMapClientFactoryBean factoryBean = new SqlMapClientFactoryBean();
	private ClassLoader classLoader = this.getClass().getClassLoader();
	
	public ResourceLoaderSqlMapClientFactoryBean(BasicDataSource dataSource, OracleLobHandler oracleLobHandler,
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
			setDataSource(dataSource);
			setLobHandler(oracleLobHandler);
			return;
		}
	
		setDataSource(dataSource);
		setLobHandler(defaultLobHandler);
		return;
	}

	public void afterPropertiesSet() throws Exception
	{
		/* afterPropertiesSet is where SqlMapClientFactoryBean does instantiation
		 * so we need to get our classloader in before that takes place		
		 */
		
		ClassLoader oldDefaultClassLoader = Resources.getDefaultClassLoader();
		Resources.setDefaultClassLoader(this.classLoader);
		factoryBean.afterPropertiesSet();
		Resources.setDefaultClassLoader(oldDefaultClassLoader);
	}

	public void setConfigLocation(Resource configLocation)
	{
		factoryBean.setConfigLocation(configLocation);
	}

	public boolean equals(Object obj)
	{
		return factoryBean.equals(obj);
	}

	public Object getObject()
	{
		return factoryBean.getObject();
	}

	public Class<?> getObjectType()
	{
		return factoryBean.getObjectType();
	}

	public int hashCode()
	{
		return factoryBean.hashCode();
	}

	public boolean isSingleton()
	{
		return factoryBean.isSingleton();
	}

	public void setDataSource(DataSource dataSource)
	{
		factoryBean.setDataSource(dataSource);
	}

	public void setLobHandler(LobHandler lobHandler)
	{
		factoryBean.setLobHandler(lobHandler);
	}

	public void setSqlMapClientProperties(Properties sqlMapClientProperties)
	{
		factoryBean.setSqlMapClientProperties(sqlMapClientProperties);
	}

	public void setTransactionConfigClass(Class<?> transactionConfigClass)
	{
		factoryBean.setTransactionConfigClass(transactionConfigClass);
	}

	public void setTransactionConfigProperties(Properties transactionConfigProperties)
	{
		factoryBean.setTransactionConfigProperties(transactionConfigProperties);
	}

	public void setUseTransactionAwareDataSource(boolean useTransactionAwareDataSource)
	{
		factoryBean.setUseTransactionAwareDataSource(useTransactionAwareDataSource);
	}

	public String toString()
	{
		return factoryBean.toString();
	}
	
	
}
