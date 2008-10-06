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
import java.util.List;
import java.util.Map;

import com.qut.middleware.esoemanager.Constants;
import com.qut.middleware.esoemanager.client.rpc.bean.ServiceNode;
import com.qut.middleware.esoemanager.client.rpc.bean.ServiceNodeConfiguration;
import com.qut.middleware.esoemanager.exception.CreateServiceNodeException;
import com.qut.middleware.esoemanager.exception.ManagerDAOException;
import com.qut.middleware.esoemanager.exception.RetrieveServiceNodeException;
import com.qut.middleware.esoemanager.exception.SaveServiceNodeException;
import com.qut.middleware.esoemanager.manager.logic.ServiceNodes;
import com.qut.middleware.esoemanager.manager.sqlmap.ManagerDAO;

public class ServiceNodesImpl implements ServiceNodes
{
	String esoeIdentifier;
	private final String metadataURL = "Provided by your system administrator";

	private ManagerDAO managerDAO;

	public String retrieveServiceHost(String serviceID) throws RetrieveServiceNodeException
	{
		try
		{
			Integer entID = new Integer(serviceID);
			String serviceHost = this.managerDAO.queryServiceHost(entID);
			
			return serviceHost;
		}
		catch (NumberFormatException e)
		{
			throw new RetrieveServiceNodeException("Exception when attempting get service host", e);
		}
		catch (ManagerDAOException e)
		{
			throw new RetrieveServiceNodeException("Exception when attempting get service host", e);
		}
	}

	public List<ServiceNode> retrieveNodes(String serviceID) throws RetrieveServiceNodeException
	{
		try
		{
			Integer entID = new Integer(serviceID);
			Integer descID = this.managerDAO.getDescID(entID, Constants.SP_DESCRIPTOR);

			List<ServiceNode> nodeList = new ArrayList<ServiceNode>();

			/* Get the service nodes */
			List<Map<String, Object>> serviceNodeData = this.managerDAO.queryServiceNodes(descID);
			for (Map<String, Object> node : serviceNodeData)
			{
				ServiceNode serviceNode = new ServiceNode();
				serviceNode.setNodeIdentifier((String) node.get(Constants.FIELD_ENDPOINT_ID));
				String activeState = (String) node.get(Constants.FIELD_ACTIVE_FLAG);
				if (activeState.equalsIgnoreCase(Constants.IS_ACTIVE))
					serviceNode.setActive(true);
				else
					serviceNode.setActive(false);
				serviceNode.setNodeURL((String) node.get(Constants.FIELD_ENDPOINT_NODEURL));
				serviceNode.setAcs((String) node.get(Constants.FIELD_ENDPOINT_ASSERTIONCONSUMER));
				serviceNode.setSls((String) node.get(Constants.FIELD_ENDPOINT_SINGLELOGOUT));
				serviceNode.setCcs((String) node.get(Constants.FIELD_ENDPOINT_CACHECLEAR));

				nodeList.add(serviceNode);
			}

			return nodeList;
		}
		catch (ManagerDAOException e)
		{
			throw new RetrieveServiceNodeException("Exception when attempting get service nodes", e);
		}
	}

	public List<ServiceNodeConfiguration> retrieveNodeConfigurations(String serviceID)
			throws RetrieveServiceNodeException
	{
		List<ServiceNodeConfiguration> configurations = new ArrayList<ServiceNodeConfiguration>();
		try
		{
			Integer entID = new Integer(serviceID);
			Integer descID = this.managerDAO.getDescID(entID, Constants.SP_DESCRIPTOR);

			Map<String, Object> service = this.managerDAO.queryServiceDetails(entID);
			if (service == null)
				throw new RetrieveServiceNodeException("Unable to correctly identify details for this service");

			List<Map<String, Object>> keyStoreDetailsMap = this.managerDAO.queryKeyStoreDetails(descID);

			if (keyStoreDetailsMap == null || keyStoreDetailsMap.size() == 0)
				throw new RetrieveServiceNodeException("Unable to correctly identify crypto for this service");

			Map<String, Object> keyStoreDetail = keyStoreDetailsMap.get(0);

			/* Get the service nodes */
			List<Map<String, Object>> serviceNodeData = this.managerDAO.queryServiceNodes(descID);
			for (Map<String, Object> node : serviceNodeData)
			{
				ServiceNodeConfiguration nodeConfiguration = new ServiceNodeConfiguration();

				nodeConfiguration.setEsoeIdentifier(this.esoeIdentifier);
				nodeConfiguration.setMetadataURL(this.metadataURL);

				nodeConfiguration.setSpepIdentifier((String) service.get(Constants.FIELD_ENTITY_ID));

				nodeConfiguration.setNodeIdentifier((String) node.get(Constants.FIELD_ENDPOINT_ID));
				nodeConfiguration.setServiceHost((String) node.get(Constants.FIELD_ENDPOINT_NODEURL));

				nodeConfiguration.setSpepKeyAlias((String) keyStoreDetail.get(Constants.FIELD_PKI_KEYPAIRNAME));
				nodeConfiguration.setKeystorePassword((String) keyStoreDetail
						.get(Constants.FIELD_PKI_KEYSTORE_PASSPHRASE));
				nodeConfiguration.setSpepKeyPassword((String) keyStoreDetail
						.get(Constants.FIELD_PKI_KEYPAIR_PASSPHRASE));

				configurations.add(nodeConfiguration);
			}

			return configurations;
		}
		catch (NumberFormatException e)
		{
			throw new RetrieveServiceNodeException(e.getLocalizedMessage(), e);
		}
		catch (ManagerDAOException e)
		{
			throw new RetrieveServiceNodeException(e.getLocalizedMessage(), e);
		}

	}

	public void saveServiceHost(String serviceID, String serviceHost) throws SaveServiceNodeException
	{
		try
		{
			Integer entID = new Integer(serviceID);
			this.managerDAO.updateServiceHost(entID, serviceHost);
		}
		catch (NumberFormatException e)
		{
			throw new SaveServiceNodeException(e.getLocalizedMessage(), e);
		}
		catch (ManagerDAOException e)
		{
			throw new SaveServiceNodeException(e.getLocalizedMessage(), e);
		}		
	}

	public void saveServiceNodeConfiguration(String serviceID, String nodeIdentifier, String nodeURL, String nodeACS,
			String nodeSLS, String nodeCCS) throws SaveServiceNodeException
	{
		Integer entID = new Integer(serviceID);

		try
		{
			Integer descID = this.managerDAO.getDescID(entID, Constants.SP_DESCRIPTOR);
			this.managerDAO.updateServiceNode(descID, nodeIdentifier, nodeURL, nodeACS, nodeSLS, nodeCCS);
		}
		catch (ManagerDAOException e)
		{
			throw new SaveServiceNodeException("Exception when attempting update service node", e);
		}
	}

	public void createServiceNode(String serviceID, String newNodeURL, String newNodeID)
			throws CreateServiceNodeException
	{
		Integer entID = new Integer(serviceID);

		try
		{
			Integer descID = this.managerDAO.getDescID(entID, Constants.SP_DESCRIPTOR);
			this.managerDAO.insertServiceNode(newNodeID, descID, newNodeURL, Constants.ACS_URL, Constants.SLS_URL,
					Constants.CCS_URL);
		}
		catch (ManagerDAOException e)
		{
			throw new CreateServiceNodeException("Exception when attempting update service node", e);
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

	public String getEsoeIdentifier()
	{
		return esoeIdentifier;
	}

	public void setEsoeIdentifier(String esoeIdentifier)
	{
		this.esoeIdentifier = esoeIdentifier;
	}
}
