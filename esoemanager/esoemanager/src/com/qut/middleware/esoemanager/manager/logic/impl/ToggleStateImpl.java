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
package com.qut.middleware.esoemanager.manager.logic.impl;

import com.qut.middleware.esoemanager.Constants;
import com.qut.middleware.esoemanager.exception.ManagerDAOException;
import com.qut.middleware.esoemanager.exception.ToggleException;
import com.qut.middleware.esoemanager.manager.logic.ToggleState;
import com.qut.middleware.esoemanager.manager.sqlmap.ManagerDAO;

public class ToggleStateImpl implements ToggleState
{
	private ManagerDAO managerDAO;
	
	public void toggleServiceState(String serviceID, boolean active) throws ToggleException
	{
		Integer entID = new Integer(serviceID);
	
		try
		{
			if(active)
				this.managerDAO.updateServiceActiveState(entID, Constants.SERVICE_INACTIVE);
			else
				this.managerDAO.updateServiceActiveState(entID, Constants.SERVICE_ACTIVE);
		}
		catch (ManagerDAOException e)
		{
			throw new ToggleException(e.getLocalizedMessage(), e);
		}
	}

	public void toggleNodeState(String serviceID, String nodeID, boolean active) throws ToggleException
	{
		Integer entID = new Integer(serviceID);
		
		try
		{
			Integer descID = this.managerDAO.getDescID(entID, Constants.SP_DESCRIPTOR);
			
			if(active)
				this.managerDAO.updateServiceNodeActiveState(descID, nodeID, Constants.SERVICE_INACTIVE);
			else
				this.managerDAO.updateServiceNodeActiveState(descID, nodeID, Constants.SERVICE_ACTIVE);
		}
		catch (ManagerDAOException e)
		{
			throw new ToggleException(e.getLocalizedMessage(), e);
		}		
	}
	
	public void toggleServicePolicyState(String serviceID, String policyID, boolean active) throws ToggleException
	{
		Integer entID = new Integer(serviceID);
	
		try
		{
			if(active)
				this.managerDAO.updateServicePolicyActiveState(entID, policyID, Constants.SERVICE_INACTIVE);
			else
				this.managerDAO.updateServicePolicyActiveState(entID, policyID, Constants.SERVICE_ACTIVE);
		}
		catch (ManagerDAOException e)
		{
			throw new ToggleException(e.getLocalizedMessage(), e);
		}
	}

	public ManagerDAO getManagerDAO()
	{
		return managerDAO;
	}

	public void setManagerDAO(ManagerDAO managerDAO)
	{
		this.managerDAO = managerDAO;
	}
}
