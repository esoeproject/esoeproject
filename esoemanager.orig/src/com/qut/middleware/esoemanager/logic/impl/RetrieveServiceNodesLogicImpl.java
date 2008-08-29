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
 * Creation Date: 1/5/07
 * 
 * Purpose: Retrieve service nodes logic default implementation
 */
package com.qut.middleware.esoemanager.logic.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.esoemanager.Constants;
import com.qut.middleware.esoemanager.bean.ServiceNodeBean;
import com.qut.middleware.esoemanager.bean.impl.ServiceNodeBeanImpl;
import com.qut.middleware.esoemanager.exception.RetrieveServiceNodesException;
import com.qut.middleware.esoemanager.exception.SPEPDAOException;
import com.qut.middleware.esoemanager.logic.RetrieveServiceNodesLogic;
import com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO;

public class RetrieveServiceNodesLogicImpl implements RetrieveServiceNodesLogic
{
	private SPEPDAO spepDAO;

	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(RetrieveServiceNodesLogicImpl.class.getName());

	public RetrieveServiceNodesLogicImpl(SPEPDAO spepDAO)
	{
		if (spepDAO == null)
		{
			this.logger.error("spepDAO for RetrieveServiceNodesLogicImpl was NULL");
			throw new IllegalArgumentException("spepDAO for RetrieveServiceNodesLogicImpl was NULL");
		}

		this.spepDAO = spepDAO;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoemanager.logic.impl.RetrieveServiceNodes#execute(java.lang.String)
	 */
	public List<ServiceNodeBean> execute(Integer descID) throws RetrieveServiceNodesException
	{
		List<ServiceNodeBean> nodes = new ArrayList<ServiceNodeBean>();

		List<Map<String, Object>> nodeDetails;
		try
		{
			nodeDetails = this.spepDAO.queryServiceNodes(descID);
		}
		catch (SPEPDAOException e)
		{
			throw new RetrieveServiceNodesException("Exception when attempting to retrieve service nodes", e);
		}
		
		for(Map<String, Object> nodeDetail : nodeDetails)
		{
			ServiceNodeBean node = new ServiceNodeBeanImpl();
			node.setNodeID((String)nodeDetail.get(Constants.FIELD_ENDPOINT_ID));
			node.setNodeURL((String)nodeDetail.get(Constants.FIELD_ENDPOINT_NODEURL));
			node.setAssertionConsumerService((String)nodeDetail.get(Constants.FIELD_ENDPOINT_ASSERTIONCONSUMER));
			node.setSingleLogoutService((String)nodeDetail.get(Constants.FIELD_ENDPOINT_SINGLELOGOUT));
			node.setCacheClearService((String)nodeDetail.get(Constants.FIELD_ENDPOINT_CACHECLEAR));
			
			nodes.add(node);
		}
		
		return nodes;
	}
}
