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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.esoemanager.Constants;
import com.qut.middleware.esoemanager.client.rpc.bean.ExtendedServiceListing;
import com.qut.middleware.esoemanager.client.rpc.bean.ServiceContact;
import com.qut.middleware.esoemanager.client.rpc.bean.ServiceStartupBean;
import com.qut.middleware.esoemanager.client.rpc.bean.SimpleServiceListing;
import com.qut.middleware.esoemanager.exception.CreateServiceContactException;
import com.qut.middleware.esoemanager.exception.CreateServiceException;
import com.qut.middleware.esoemanager.exception.CreateServicePolicyException;
import com.qut.middleware.esoemanager.exception.EditServiceDetailsException;
import com.qut.middleware.esoemanager.exception.ManagerDAOException;
import com.qut.middleware.esoemanager.exception.RetrieveServiceDescriptorException;
import com.qut.middleware.esoemanager.exception.RetrieveServiceListException;
import com.qut.middleware.esoemanager.exception.RetrieveServiceListingException;
import com.qut.middleware.esoemanager.exception.RetrieveServiceStartupsException;
import com.qut.middleware.esoemanager.exception.ServiceCryptoCreationException;
import com.qut.middleware.esoemanager.manager.logic.Service;
import com.qut.middleware.esoemanager.manager.logic.ServiceContacts;
import com.qut.middleware.esoemanager.manager.logic.ServiceCrypto;
import com.qut.middleware.esoemanager.manager.logic.ServicePolicies;
import com.qut.middleware.esoemanager.manager.sqlmap.ManagerDAO;
import com.qut.middleware.esoemanager.util.PolicyIDGenerator;
import com.qut.middleware.esoemanager.util.UtilFunctions;

public class ServiceImpl implements Service
{
	private ManagerDAO managerDAO;
	private UtilFunctions utils;
	PolicyIDGenerator policyIDGenerator;
	private ServiceContacts serviceContacts;
	private ServiceCrypto serviceCrypto;
	private ServicePolicies servicePolicies;

	private File defaultServiceAccessPolicyLocation;
	private File defaultServiceManagementPolicyLocation;
	
	private String organizationName;
	private String organizationDisplayName;
	private String organizationURL;
	private String defaultACS;
	private String defaultSLS;
	private String defaultCCS;

	private String defaultServiceAccessPolicy;
	private String defaultServiceManagementPolicy;
	
	private Integer esoeManagerENTID;

	private final String POL_ID = "[policyID]";
	private final String SERVICE_ID = "[serviceID]";
	private Logger logger = LoggerFactory.getLogger(ServiceImpl.class);

	public void init() throws IOException
	{
		this.defaultServiceAccessPolicy = this.loadPolicy(this.defaultServiceAccessPolicyLocation);
		this.defaultServiceManagementPolicy = this.loadPolicy(this.defaultServiceManagementPolicyLocation);
	}

	public ExtendedServiceListing retrieveExtendedServiceListing(String serviceID)
			throws RetrieveServiceListingException
	{
		ExtendedServiceListing bean = new ExtendedServiceListing();

		try
		{
			Integer entID = new Integer(serviceID);
			Map<String, Object> service = this.managerDAO.queryServiceDetails(entID);
			bean.setIdentifier(serviceID);
			String activeState = (String) service.get(Constants.FIELD_ACTIVE_FLAG);
			if (activeState.equalsIgnoreCase(Constants.IS_ACTIVE))
				bean.setActive(true);
			else
				bean.setActive(false);

			/* Get descriptive detail about the service */
			Map<String, Object> description = this.managerDAO.queryServiceDescription(entID);
			bean.setServiceName((String) description.get(Constants.FIELD_SERVICE_NAME));
			bean.setServiceURL((String) description.get(Constants.FIELD_SERVICE_URL));
			bean.setServiceDescription((String) description.get(Constants.FIELD_SERVICE_DESC));
			bean.setServiceAuthorizationFailureMessage((String) description.get(Constants.FIELD_SERVICE_AUTHZ_FAIL));

			return bean;
		}
		catch (ManagerDAOException e)
		{
			throw new RetrieveServiceListingException("Exception when attempting get service details", e);
		}
	}

	public List<SimpleServiceListing> retrieveSimpleServiceListing() throws RetrieveServiceListException
	{
		try
		{
			List<SimpleServiceListing> services = new ArrayList<SimpleServiceListing>();
			List<Integer> activeServices = this.managerDAO.queryServices();

			for (Integer entID : activeServices)
			{
				SimpleServiceListing bean = new SimpleServiceListing();

				/* Get the core system data for this service */
				Map<String, Object> service = this.managerDAO.queryServiceDetails(entID);
				bean.setIdentifier(entID.toString());
				String activeState = (String) service.get(Constants.FIELD_ACTIVE_FLAG);

				if (activeState.equalsIgnoreCase(Constants.IS_ACTIVE))
					bean.setActive(true);
				else
					bean.setActive(false);

				/* Get descriptive detail about the service */
				Map<String, Object> description = this.managerDAO.queryServiceDescription(entID);
				bean.setServiceName((String) description.get(Constants.FIELD_SERVICE_NAME));
				bean.setServiceURL((String) description.get(Constants.FIELD_SERVICE_URL));

				services.add(bean);
			}

			return services;
		}
		catch (ManagerDAOException e)
		{
			throw new RetrieveServiceListException(e.getLocalizedMessage(), e);
		}
	}

	public String createService(String entityID, String serviceName, String serviceURL, String serviceDescription,
			String entityHost, String firstNode, ServiceContact contact)
			throws CreateServiceException
	{
		// TODO - Wrap this all up in one big transactional piece

		try
		{
			Integer newEntID = this.managerDAO.getNextEntID();
			Integer newDescID = this.managerDAO.getNextDescID();

			// Create the entity
			// TODO Make the default active flag configurable.
			this.managerDAO.insertEntityDescriptor(newEntID, entityID, entityHost, organizationName, organizationDisplayName,
					organizationURL, "y");

			// Create the description
			this.managerDAO.insertServiceDescription(newEntID, serviceName, serviceURL, serviceDescription,
					"please specify");

			// Create the service descriptor
			String nonProcessedDescriptor = "will be available when service is activated...";	// Descriptor XML is created/updated by first valid pass of metadata engine
			this.managerDAO.insertDescriptor(newEntID, newDescID, "0", nonProcessedDescriptor.getBytes("UTF-16"), Constants.SP_DESCRIPTOR);
			
			// Create the technical contact
			this.serviceContacts.createServiceContact(newEntID.toString(), contact.getContactID(), contact.getName(), contact.getEmail(), contact.getTelephone(), contact.getCompany(), contact.getType());
			
			// Create the service crypto
			this.serviceCrypto.createServiceKey(newEntID.toString());
			
			// Create service node details
			this.managerDAO.insertServiceNode("1", newDescID, firstNode, defaultACS, defaultSLS, defaultCCS);

			// Create default service access policy
			String serviceAccessPolicy = this.defaultServiceAccessPolicy;
			String policyID = this.policyIDGenerator.generatePolicyID();
			serviceAccessPolicy = serviceAccessPolicy.replace(this.POL_ID, policyID);
			this.servicePolicies.createPolicyXML(newEntID.toString(), serviceAccessPolicy);

			// Create default service management policy (add to esoemanager pool as this policy set controls management interface access)
			String serviceManagementPolicy = this.defaultServiceManagementPolicy;
			String managementPolicyID = entityID;
			serviceManagementPolicy = serviceManagementPolicy.replace(this.POL_ID, managementPolicyID);
			serviceManagementPolicy = serviceManagementPolicy.replace(this.SERVICE_ID, newEntID.toString());
			this.servicePolicies.createPolicyXML(this.esoeManagerENTID.toString(), serviceManagementPolicy);
			
			return newEntID.toString();
		}
		catch (ManagerDAOException e)
		{
			throw new CreateServiceException(e.getLocalizedMessage(), e);
		}
		catch (CreateServiceContactException e)
		{
			throw new CreateServiceException(e.getLocalizedMessage(), e);
		}
		catch (ServiceCryptoCreationException e)
		{
			throw new CreateServiceException(e.getLocalizedMessage(), e);
		}
		catch (CreateServicePolicyException e)
		{
			throw new CreateServiceException(e.getLocalizedMessage(), e);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new CreateServiceException(e.getLocalizedMessage(), e);
		}
	}

	public void saveServiceDetails(String serviceID, String serviceName, String serviceURL, String serviceDescription,
			String serviceAccessDeniedMessage) throws EditServiceDetailsException
	{
		try
		{
			Integer entID = new Integer(serviceID);
			this.managerDAO.updateServiceDescription(entID, serviceName, serviceURL, serviceDescription,
					serviceAccessDeniedMessage);
		}
		catch (ManagerDAOException e)
		{
			this.logger.warn("SPEPDAOException when updating service details " + e.getLocalizedMessage());
			throw new EditServiceDetailsException("Unable to update service details service details - "
					+ e.getLocalizedMessage(), e);
		}
	}

	public List<ServiceStartupBean> retrieveServiceStartups(String serviceID) throws RetrieveServiceStartupsException
	{
		List<ServiceStartupBean> recentStartups = new ArrayList<ServiceStartupBean>();

		try
		{
			Integer entID = new Integer(serviceID);
			List<Map<String, Object>> startups = this.managerDAO.queryRecentServiceNodeStartup(entID);
			if (startups != null)
			{
				for (Map<String, Object> startup : startups)
				{
					ServiceStartupBean bean = new ServiceStartupBean();
					bean.setDate((Date) startup.get(Constants.FIELD_DATEADDED));
					bean.setNodeID((String) startup.get(Constants.FIELD_NODEID));
					bean.setEnv((String) startup.get(Constants.FIELD_ENVIRONMENT));
					bean.setVersion((String) startup.get(Constants.FIELD_VERSION));

					Map<String, Object> description = this.managerDAO.queryServiceDescription(entID);
					bean.setServiceName((String) description.get(Constants.FIELD_SERVICE_NAME));
					recentStartups.add(bean);
				}
				return recentStartups;
			}
			throw new RetrieveServiceStartupsException("Unable to retrieve recent activations");
		}
		catch (ManagerDAOException e)
		{
			throw new RetrieveServiceStartupsException(e.getLocalizedMessage(), e);
		}
	}

	public String retrieveServiceDescriptor(String serviceID) throws RetrieveServiceDescriptorException
	{
		try
		{
			Integer entID = new Integer(serviceID);
			Map<String, Object> descriptor = this.managerDAO.queryServiceDescriptor(entID);

			String policy;
			byte[] rawPolicy = (byte[]) descriptor.get(Constants.FIELD_DESCRIPTOR_XML);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try
			{
				this.utils.prettyPrintXML(rawPolicy, baos);
				policy = new String(baos.toByteArray(), "UTF-16");
			}
			catch (Exception e)
			{
				// We tried to pretty things up at least.. :)
				policy = new String(rawPolicy, "UTF-16");
			}

			return policy;
		}
		catch (UnsupportedEncodingException e)
		{
			throw new RetrieveServiceDescriptorException("Unable to decode descriptor");
		}
		catch (ManagerDAOException e)
		{
			throw new RetrieveServiceDescriptorException(e.getLocalizedMessage(), e);
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

	public UtilFunctions getUtils()
	{
		return utils;
	}

	public void setUtils(UtilFunctions utils)
	{
		this.utils = utils;
	}

	private String loadPolicy(File policyFile) throws IOException
	{
		InputStream fileStream = null;

		try
		{
			long length = policyFile.length();
			byte[] byteArray = new byte[(int) length];
			fileStream = new FileInputStream(policyFile);
			fileStream.read(byteArray);
			fileStream.close();

			return new String(byteArray, "UTF-16");
		}
		finally
		{
			if (fileStream != null)
				fileStream.close();
		}
	}



	public void setServiceContacts(ServiceContacts serviceContacts)
	{
		this.serviceContacts = serviceContacts;
	}

	public void setServiceCrypto(ServiceCrypto serviceCrypto)
	{
		this.serviceCrypto = serviceCrypto;
	}

	public void setDefaultServiceAccessPolicyLocation(File defaultServiceAccessPolicyLocation)
	{
		this.defaultServiceAccessPolicyLocation = defaultServiceAccessPolicyLocation;
	}

	public void setDefaultServiceManagementPolicyLocation(File defaultServiceManagementPolicyLocation)
	{
		this.defaultServiceManagementPolicyLocation = defaultServiceManagementPolicyLocation;
	}

	public void setOrganizationName(String organizationName)
	{
		this.organizationName = organizationName;
	}

	public void setOrganizationDisplayName(String organizationDisplayName)
	{
		this.organizationDisplayName = organizationDisplayName;
	}

	public void setOrganizationURL(String organizationURL)
	{
		this.organizationURL = organizationURL;
	}

	public void setDefaultACS(String defaultACS)
	{
		this.defaultACS = defaultACS;
	}

	public void setDefaultSLS(String defaultSLS)
	{
		this.defaultSLS = defaultSLS;
	}

	public void setDefaultCCS(String defaultCCS)
	{
		this.defaultCCS = defaultCCS;
	}
	public void setPolicyIDGenerator(PolicyIDGenerator policyIDGenerator)
	{
		this.policyIDGenerator = policyIDGenerator;
	}

	public void setServicePolicies(ServicePolicies servicePolicies)
	{
		this.servicePolicies = servicePolicies;
	}

	public void setEsoeManagerENTID(Integer esoeManagerENTID)
	{
		this.esoeManagerENTID = esoeManagerENTID;
	}
	
}
