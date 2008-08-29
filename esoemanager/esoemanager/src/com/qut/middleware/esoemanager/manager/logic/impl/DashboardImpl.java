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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.qut.middleware.esoemanager.Constants;
import com.qut.middleware.esoemanager.client.rpc.bean.EsoeDashBean;
import com.qut.middleware.esoemanager.client.rpc.bean.ServiceExpiryBean;
import com.qut.middleware.esoemanager.client.rpc.bean.ServiceStartupBean;
import com.qut.middleware.esoemanager.client.rpc.bean.ServicesDashBean;
import com.qut.middleware.esoemanager.exception.RetrieveDashboardDetailsException;
import com.qut.middleware.esoemanager.exception.ManagerDAOException;
import com.qut.middleware.esoemanager.manager.logic.Dashboard;
import com.qut.middleware.esoemanager.manager.sqlmap.ManagerDAO;

public class DashboardImpl implements Dashboard
{
	private ManagerDAO managerDAO;
	private int esoeENTID;

	public EsoeDashBean getESOEDashboardDetails() throws RetrieveDashboardDetailsException
	{
		EsoeDashBean bean = new EsoeDashBean();

		try
		{
			bean.setActiveNodes(this.managerDAO.getActiveNodeCount().toString());
			bean.setNumNodes(this.managerDAO.getNodeCount().toString());

			bean.setActivePolicies(this.managerDAO.getActivePolicyCount().toString());
			bean.setNumPolicies(this.managerDAO.getPolicyCount().toString());

			bean.setActiveServices(this.managerDAO.getActiveServiceCount().toString());
			bean.setNumServices(this.managerDAO.getServiceCount().toString());

			return bean;
		}
		catch (ManagerDAOException e)
		{
			throw new RetrieveDashboardDetailsException(e.getLocalizedMessage(), e);
		}
	}

	public ServicesDashBean getServicesDashboardDetails() throws RetrieveDashboardDetailsException
	{
		ServicesDashBean bean = new ServicesDashBean();

		try
		{
			List<Map<String, Object>> services = this.managerDAO.queryServicesCloseExpiry();
			if (services != null)
			{
				List<ServiceExpiryBean> expiries = new ArrayList<ServiceExpiryBean>();
				
				for (Map<String, Object> service : services)
				{
					ServiceExpiryBean serviceBean = new ServiceExpiryBean();
					serviceBean.setExpiryDate((Date) service.get(Constants.FIELD_PK_EXPIRY_DATE));

					Integer entID = this.managerDAO.getEntIDfromDescID((Integer) service.get(Constants.FIELD_DESC_ID));

					if (entID != null)
					{
						if (entID != this.esoeENTID)
						{
							Map<String, Object> description = this.managerDAO.queryServiceDescription(entID);
							serviceBean.setName((String) description.get(Constants.FIELD_SERVICE_NAME));
							expiries.add(serviceBean);
						}
					}
					
				}
				bean.setExpiries(expiries);
				
				return bean;
			}

			throw new RetrieveDashboardDetailsException("Unable to retrieve expiring services");
		}
		catch (ManagerDAOException e)
		{
			throw new RetrieveDashboardDetailsException(e.getLocalizedMessage(), e);
		}
	}
	
	public List<ServiceStartupBean> getStartupDashboardDetails() throws RetrieveDashboardDetailsException
	{
		List<ServiceStartupBean> recentStartups = new ArrayList<ServiceStartupBean>();
		
		try
		{
			List<Map<String,Object>> startups = this.managerDAO.queryRecentNodeStartup();
			if(startups != null)
			{
				for(Map<String, Object> startup : startups)
				{
					ServiceStartupBean bean = new ServiceStartupBean();
					bean.setDate((Date)startup.get(Constants.FIELD_DATEADDED));
					bean.setNodeID((String)startup.get(Constants.FIELD_NODEID));
					bean.setEnv((String)startup.get(Constants.FIELD_ENVIRONMENT));
					bean.setVersion((String)startup.get(Constants.FIELD_VERSION));
					
					Integer entID = (Integer) startup.get(Constants.FIELD_ENT_ID);
					if (entID != null)
					{
						if (entID != this.esoeENTID)
						{
							Map<String, Object> description = this.managerDAO.queryServiceDescription(entID);
							bean.setServiceName((String) description.get(Constants.FIELD_SERVICE_NAME));
							recentStartups.add(bean);
						}
					}
				}
				return recentStartups;
			}
			throw new RetrieveDashboardDetailsException("Unable to retrieve recent activations");
		}
		catch (ManagerDAOException e)
		{
			throw new RetrieveDashboardDetailsException(e.getLocalizedMessage(), e);
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

	public int getEsoeENTID()
	{
		return esoeENTID;
	}

	public void setEsoeENTID(int esoeENTID)
	{
		this.esoeENTID = esoeENTID;
	}
}
