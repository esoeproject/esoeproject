/* 
 * Copyright 2008, Queensland University of Technology
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
 * Creation Date: 17/09/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.esoemanager.manager.bean;

import org.springframework.beans.factory.FactoryBean;

import com.qut.middleware.esoemanager.exception.ManagerDAOException;
import com.qut.middleware.esoemanager.manager.sqlmap.ManagerDAO;

public class ServiceEntIDFactoryBean implements FactoryBean
{
	private Integer value;

	public ServiceEntIDFactoryBean(ManagerDAO managerDAO, String entityIdentifier)
	{
		try
		{
			this.value = managerDAO.getEntID(entityIdentifier);
		}
		catch (ManagerDAOException e)
		{
			throw new IllegalArgumentException("Cannot initialize - unable to get ENTID value for EntityID: " + entityIdentifier);
		}
	}

	public Object getObject() throws Exception
	{
		return this.value;
	}

	public Class<?> getObjectType()
	{
		return Integer.class;
	}
	@Override
	public boolean isSingleton()
	{
		return true;
	}

}
