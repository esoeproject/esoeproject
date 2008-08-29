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
package com.qut.middleware.esoemanager.servlet;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.qut.middleware.esoemanager.EsoeManager;
import com.qut.middleware.esoemanager.client.rpc.EsoeManagerInvalidXMLException;
import com.qut.middleware.esoemanager.client.rpc.EsoeManagerService;
import com.qut.middleware.esoemanager.client.rpc.EsoeManagerServiceException;
import com.qut.middleware.esoemanager.client.rpc.bean.EsoeDashBean;
import com.qut.middleware.esoemanager.client.rpc.bean.ExtendedServiceListing;
import com.qut.middleware.esoemanager.client.rpc.bean.KeyDetails;
import com.qut.middleware.esoemanager.client.rpc.bean.MetadataDashBean;
import com.qut.middleware.esoemanager.client.rpc.bean.Policy;
import com.qut.middleware.esoemanager.client.rpc.bean.ServiceContact;
import com.qut.middleware.esoemanager.client.rpc.bean.ServiceNode;
import com.qut.middleware.esoemanager.client.rpc.bean.ServiceNodeConfiguration;
import com.qut.middleware.esoemanager.client.rpc.bean.ServiceStartupBean;
import com.qut.middleware.esoemanager.client.rpc.bean.ServicesDashBean;
import com.qut.middleware.esoemanager.client.rpc.bean.SimpleServiceListing;
import com.qut.middleware.esoemanager.exception.CreateServiceContactException;
import com.qut.middleware.esoemanager.exception.CreateServiceException;
import com.qut.middleware.esoemanager.exception.CreateServiceNodeException;
import com.qut.middleware.esoemanager.exception.CreateServicePolicyException;
import com.qut.middleware.esoemanager.exception.DeleteServiceContactException;
import com.qut.middleware.esoemanager.exception.DeleteServicePolicyException;
import com.qut.middleware.esoemanager.exception.EditServiceDetailsException;
import com.qut.middleware.esoemanager.exception.RetrieveAttributeException;
import com.qut.middleware.esoemanager.exception.RetrieveDashboardDetailsException;
import com.qut.middleware.esoemanager.exception.RetrieveServiceContactException;
import com.qut.middleware.esoemanager.exception.RetrieveServiceCryptoException;
import com.qut.middleware.esoemanager.exception.RetrieveServiceDescriptorException;
import com.qut.middleware.esoemanager.exception.RetrieveServiceListException;
import com.qut.middleware.esoemanager.exception.RetrieveServiceListingException;
import com.qut.middleware.esoemanager.exception.RetrieveServiceNodeException;
import com.qut.middleware.esoemanager.exception.RetrieveServicePolicyException;
import com.qut.middleware.esoemanager.exception.RetrieveServiceStartupsException;
import com.qut.middleware.esoemanager.exception.SaveAttributeException;
import com.qut.middleware.esoemanager.exception.SaveServiceContactException;
import com.qut.middleware.esoemanager.exception.SaveServiceNodeException;
import com.qut.middleware.esoemanager.exception.SaveServicePolicyException;
import com.qut.middleware.esoemanager.exception.ServiceCryptoCreationException;
import com.qut.middleware.esoemanager.exception.ServiceCryptoDeletionException;
import com.qut.middleware.esoemanager.exception.ToggleException;

public class RPCServlet extends RemoteServiceServlet implements EsoeManagerService
{
	private static final long serialVersionUID = -8659126850994287425L;

	private final String ESOE_MANAGER_IMPL = "esoeManager";

	private EsoeManager esoeManager;

	private Logger logger = LoggerFactory.getLogger(RPCServlet.class);

	@Override
	public void init() throws ServletException
	{
		super.init();

		/* Spring integration to make our servlet aware of IoC */
		WebApplicationContext webAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(this
				.getServletContext());

		this.esoeManager = (EsoeManager) webAppContext.getBean(ESOE_MANAGER_IMPL,
				com.qut.middleware.esoemanager.EsoeManager.class);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoemanager.client.rpc.EsoeManagerService#isSuperUser()
	 * @see Security pointcut associated with this method in ESOEManager
	 */
	public Boolean isSuperUser()
	{
		return esoeManager.isSuperUser();
	}

	public List<SimpleServiceListing> retrieveSimpleServiceListing() throws EsoeManagerServiceException
	{
		try
		{
			return this.esoeManager.getService().retrieveSimpleServiceListing();
		}
		catch (RetrieveServiceListException e)
		{
			this.logger.error("Unable to retrieve service listings");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to retrieve service listings");
		}
	}

	public ExtendedServiceListing retrieveServiceListing(String serviceID) throws EsoeManagerServiceException
	{
		try
		{
			return this.esoeManager.getService().retrieveExtendedServiceListing(serviceID);
		}
		catch (RetrieveServiceListingException e)
		{
			this.logger.error("Unable to retrieve service listing");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to retrieve service listing");
		}
	}

	public void saveServiceDescription(String serviceID, String serviceName, String serviceURL,
			String serviceDescription, String serviceAccessDeniedMessage) throws EsoeManagerServiceException
	{
		try
		{
			this.esoeManager.getService().saveServiceDetails(serviceID, serviceName, serviceURL,
					serviceDescription, serviceAccessDeniedMessage);
		}
		catch (EditServiceDetailsException e)
		{
			this.logger.error("Unable to save service details");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to save service details");
		}
	}

	public void toggleServiceState(String serviceID, boolean active) throws EsoeManagerServiceException
	{
		try
		{
			this.esoeManager.getToggleState().toggleServiceState(serviceID, active);
		}
		catch (ToggleException e)
		{
			this.logger.error("Unable to toggle service state");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to toggle service state");
		}
	}

	public List<ServiceStartupBean> retrieveServiceStartups(String serviceID) throws EsoeManagerServiceException
	{
		try
		{
			return this.esoeManager.getService().retrieveServiceStartups(serviceID);
		}
		catch (RetrieveServiceStartupsException e)
		{
			this.logger.error("Unable to retrieve service startups");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to retrieve service startups");
		}
	}

	public String createService(String entityID, String serviceName, String serviceURL, String serviceDescription,
			String entityHost, String firstNode, ServiceContact techContact) throws EsoeManagerServiceException
	{
		try
		{
			return this.esoeManager.getService().createService(entityID, serviceName, serviceURL, serviceDescription, entityHost, firstNode, techContact);
		}
		catch (CreateServiceException e)
		{
			this.logger.error("Unable to create new service");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to create new service - " + e.getLocalizedMessage());
		}
	}

	public List<ServiceContact> retrieveServiceContacts(String serviceID) throws EsoeManagerServiceException
	{
		try
		{
			return this.esoeManager.getServiceContacts().retrieveServiceContacts(serviceID);
		}
		catch (RetrieveServiceContactException e)
		{
			this.logger.error("Unable to retrieve service contacts");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to retrieve service contacts");
		}
	}

	public void saveServiceContact(String serviceID, String contactID, String name, String email, String telephone,
			String company, String type) throws EsoeManagerServiceException
	{
		try
		{
			this.esoeManager.getServiceContacts().saveServiceContact(serviceID, contactID, name, email, telephone,
					company, type);
		}
		catch (SaveServiceContactException e)
		{
			this.logger.error("Unable to save service contact");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to save service contact");
		}
	}

	public void deleteServiceContact(String serviceID, String contactID) throws EsoeManagerServiceException
	{
		try
		{
			this.esoeManager.getServiceContacts().deleteServiceContact(serviceID, contactID);
		}
		catch (DeleteServiceContactException e)
		{
			this.logger.error("Unable to delete service contact");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to delete service contact");
		}
	}

	public void createServiceContact(String serviceID, String contactID, String name, String email, String telephone,
			String company, String type) throws EsoeManagerServiceException
	{
		try
		{
			this.esoeManager.getServiceContacts().createServiceContact(serviceID, contactID, name, email, telephone,
					company, type);
		}
		catch (CreateServiceContactException e)
		{
			this.logger.error("Unable to create service contact");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to create service contact");
		}
	}

	public List<ServiceNode> retrieveServiceNodes(String serviceID) throws EsoeManagerServiceException
	{
		try
		{
			return this.esoeManager.getServiceNodes().retrieveNodes(serviceID);
		}
		catch (RetrieveServiceNodeException e)
		{
			this.logger.error("Unable to retrieve service nodes");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to retrieve service nodes");
		}
	}

	public List<ServiceNodeConfiguration> retrieveNodeConfigurations(String serviceID)
			throws EsoeManagerServiceException
	{
		try
		{
			return this.esoeManager.getServiceNodes().retrieveNodeConfigurations(serviceID);
		}
		catch (RetrieveServiceNodeException e)
		{
			this.logger.error("Unable to retrieve service node configuration");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to retrieve service node configuration");
		}
	}

	public void saveServiceNodeConfiguration(String serviceID, String nodeIdentifier, String nodeURL, String nodeACS,
			String nodeSLS, String nodeCCS) throws EsoeManagerServiceException
	{
		try
		{
			this.esoeManager.getServiceNodes().saveServiceNodeConfiguration(serviceID, nodeIdentifier, nodeURL,
					nodeACS, nodeSLS, nodeCCS);
		}
		catch (SaveServiceNodeException e)
		{
			this.logger.error("Unable to save service node");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to save service node");
		}
	}

	public void createServiceNode(String serviceID, String newNodeURL, String newNodeID)
			throws EsoeManagerServiceException
	{
		try
		{
			this.esoeManager.getServiceNodes().createServiceNode(serviceID, newNodeURL, newNodeID);
		}
		catch (CreateServiceNodeException e)
		{
			this.logger.error("Unable to save service node");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to save service node");
		}
	}

	public void toggleServiceNodeState(String serviceID, String nodeIdentifier, boolean active)
			throws EsoeManagerServiceException
	{
		try
		{
			this.esoeManager.getToggleState().toggleNodeState(serviceID, nodeIdentifier, active);
		}
		catch (ToggleException e)
		{
			this.logger.error("Unable to toggle service node state");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to toggle service node state");
		}
	}

	public String retrieveServiceDescriptor(String serviceID) throws EsoeManagerServiceException
	{
		try
		{
			return this.esoeManager.getService().retrieveServiceDescriptor(serviceID);
		}
		catch (RetrieveServiceDescriptorException e)
		{
			this.logger.error("Unable to retrieve service descriptor");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to retrieve service descriptor");
		}
	}

	public String retrieveServiceHost(String serviceID) throws EsoeManagerServiceException
	{
		try
		{
			return this.esoeManager.getServiceNodes().retrieveServiceHost(serviceID);
		}
		catch (RetrieveServiceNodeException e)
		{
			this.logger.error("Unable to retrieve service host");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to retrieve service host");
		}
	}

	public void saveServiceHost(String serviceID, String serviceHost) throws EsoeManagerServiceException
	{
		try
		{
			this.esoeManager.getServiceNodes().saveServiceHost(serviceID, serviceHost);
		}
		catch (SaveServiceNodeException e)
		{
			this.logger.error("Unable to save service host");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to save service host");
		}
	}

	public List<Policy> retrieveServicePolicies(String serviceID) throws EsoeManagerServiceException
	{
		try
		{
			return this.esoeManager.getServicePolicies().retrievePolicies(serviceID);
		}
		catch (RetrieveServicePolicyException e)
		{
			this.logger.error("Unable to retrieve service policies");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to retrieve service policies");
		}
	}

	public Policy retrieveServicePolicy(String serviceID, String policyID) throws EsoeManagerServiceException
	{
		try
		{
			return this.esoeManager.getServicePolicies().retrievePolicy(serviceID, policyID);
		}
		catch (RetrieveServicePolicyException e)
		{
			this.logger.error("Unable to retrieve service policy");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to retrieve service policy");
		}
	}

	public String retrieveServicePolicyXML(String serviceID, String policyID) throws EsoeManagerServiceException
	{
		try
		{
			return this.esoeManager.getServicePolicies().retrievePolicyXML(serviceID, policyID);
		}
		catch (RetrieveServicePolicyException e)
		{
			this.logger.error("Unable to retrieve service policy xml");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to retrieve service policy xml");
		}
	}

	public void toggleServicePolicyState(String serviceID, String policyID, boolean active)
			throws EsoeManagerServiceException
	{
		try
		{
			this.esoeManager.getToggleState().toggleServicePolicyState(serviceID, policyID, active);
		}
		catch (ToggleException e)
		{
			this.logger.error("Unable to toggle service policy state");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to toggle service policy state");
		}
	}

	public void saveServicePolicy(String serviceID, Policy policy) throws EsoeManagerServiceException
	{
		try
		{
			this.esoeManager.getServicePolicies().savePolicy(serviceID, policy);
		}
		catch (SaveServicePolicyException e)
		{
			this.logger.error("Unable to save service policy");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to save service policy - " + e.getLocalizedMessage());
		}
	}

	public void saveServicePolicy(String serviceID, String policy) throws EsoeManagerServiceException,
			EsoeManagerInvalidXMLException
	{
		try
		{
			this.esoeManager.getServicePolicies().savePolicyXML(serviceID, policy);
		}
		catch (SaveServicePolicyException e)
		{
			this.logger.error("Unable to save service policy xml");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to save service policy xml - " + e.getLocalizedMessage());
		}
	}

	public String createServicePolicy(String serviceID, Policy policy) throws EsoeManagerServiceException
	{
		try
		{
			return this.esoeManager.getServicePolicies().createPolicy(serviceID, policy);
		}
		catch (CreateServicePolicyException e)
		{
			this.logger.error("Unable to create service policy");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to create service policy - " + e.getLocalizedMessage());
		}
	}

	public String createServicePolicy(String serviceID, String policy) throws EsoeManagerServiceException,
			EsoeManagerInvalidXMLException
	{
		try
		{
			return this.esoeManager.getServicePolicies().createPolicyXML(serviceID, policy);
		}
		catch (CreateServicePolicyException e)
		{
			this.logger.error("Unable to create service policy xml");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to create service policy xml - " + e.getLocalizedMessage());
		}
	}

	public void deleteServicePolicy(String serviceID, String policyID) throws EsoeManagerServiceException
	{
		try
		{
			this.esoeManager.getServicePolicies().deletePolicy(serviceID, policyID);
		}
		catch (DeleteServicePolicyException e)
		{
			this.logger.error("Unable to delete service policy xml");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to delete service policy xml - " + e.getLocalizedMessage());
		}
	}

	public List<KeyDetails> retrieveServiceKeys(String serviceID) throws EsoeManagerServiceException
	{
		try
		{
			return this.esoeManager.getServiceCrypto().retrieveServiceKeys(serviceID);
		}
		catch (RetrieveServiceCryptoException e)
		{
			this.logger.error("Unable to retrieve service crypto");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to retrieve service crypto");
		}
	}

	public void createServiceKey(String serviceID) throws EsoeManagerServiceException
	{
		try
		{
			this.esoeManager.getServiceCrypto().createServiceKey(serviceID);
		}
		catch (ServiceCryptoCreationException e)
		{
			this.logger.error("Unable to create service key");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to create service key");
		}
	}

	public void deleteServiceKey(String serviceID, String keypairName) throws EsoeManagerServiceException
	{
		try
		{
			this.esoeManager.getServiceCrypto().deleteServiceKey(serviceID, keypairName);
		}
		catch (ServiceCryptoDeletionException e)
		{
			this.logger.error("Unable to delete service key");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to delete service key");
		}
	}

	public List<String> retrieveConfiguredAttributeList()
	{
		List<String> result = new ArrayList<String>();
		return result;
	}

	public String getAttributePolicyXML() throws EsoeManagerServiceException
	{
		try
		{
			return this.esoeManager.getAttributes().getAttributePolicyXML();
		}
		catch (RetrieveAttributeException e)
		{
			this.logger.error("Unable to obtain attribute release polciy");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to obtain attribute release polciy");
		}
	}

	public void saveAttributePolicyXML(String policy) throws EsoeManagerServiceException
	{
		try
		{
			this.esoeManager.getAttributes().saveAttributePolicyXML(policy);
		}
		catch (SaveAttributeException e)
		{
			this.logger.error("Unable to save attribute release polciy");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to save attribute release policy - " + e.getLocalizedMessage());
		}		
	}

	public EsoeDashBean getESOEDashboardDetails() throws EsoeManagerServiceException
	{
		try
		{
			return this.esoeManager.getDashboard().getESOEDashboardDetails();
		}
		catch (RetrieveDashboardDetailsException e)
		{
			this.logger.error("Unable to obtain esoe dashboard");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to obtain esoe dashboard");
		}
	}

	public String getMetadataXML() throws EsoeManagerServiceException
	{
		return this.esoeManager.getCompleteMD();
	}

	public ServicesDashBean getServicesDashboardDetails() throws EsoeManagerServiceException
	{
		try
		{
			return this.esoeManager.getDashboard().getServicesDashboardDetails();
		}
		catch (RetrieveDashboardDetailsException e)
		{
			this.logger.error("Unable to obtain services dashboard");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to obtain services dashboard");
		}
	}

	public List<ServiceStartupBean> getStartupDashboardDetails() throws EsoeManagerServiceException
	{
		try
		{
			return this.esoeManager.getDashboard().getStartupDashboardDetails();
		}
		catch (RetrieveDashboardDetailsException e)
		{
			this.logger.error("Unable to obtain startup dashboard");
			this.logger.debug(e.getLocalizedMessage(), e);
			throw new EsoeManagerServiceException("Unable to obtain startup dashboard");
		}
	}
	
	public MetadataDashBean getMDDashboardDetails() throws EsoeManagerServiceException
	{
		return null;
	}
}
