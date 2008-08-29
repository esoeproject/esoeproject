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
package com.qut.middleware.esoemanager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
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
import com.qut.middleware.esoemanager.util.ConvertToUIPolicy;
import com.qut.middleware.saml2.exception.UnmarshallerException;

public class ClientTestServlet extends RemoteServiceServlet implements EsoeManagerService
{
	private boolean addedNode = false;
	private boolean addedContact = false;

	List<Policy> result = new ArrayList<Policy>();
	List<KeyDetails> keys = new ArrayList<KeyDetails>();
	List<ServiceNodeConfiguration> nodeConfigs = new ArrayList<ServiceNodeConfiguration>();
	String xmlPolicy = "";

	int id = 0;
	int keyCount = 0;

	@Override
	public Boolean isSuperUser()
	{
		return true;
	}

	
	
	@Override
	public String retrieveServiceHost(String serviceID) throws EsoeManagerServiceException
	{
		return "http://service.subdomain.com";
	}

	@Override
	public void saveServiceHost(String serviceID, String serviceHost) throws EsoeManagerServiceException
	{
				
	}



	public List<SimpleServiceListing> retrieveSimpleServiceListing() throws EsoeManagerServiceException
	{
		List<SimpleServiceListing> response = new ArrayList<SimpleServiceListing>();
		
		return response;/*

		SimpleServiceListing service1 = new SimpleServiceListing();
		service1.setActive(true);
		service1.setServiceName("Test Service 1");
		service1.setServiceURL("https://service.com.au");
		service1.setIdentifier("234324");

		response.add(service1);

		SimpleServiceListing service2 = new SimpleServiceListing();
		service2.setActive(false);
		service2.setServiceName("Test Service 2");
		service2.setServiceURL("https://service2.com.au");
		service2.setIdentifier("111111234324");

		response.add(service2);

		return response;
		*/
	}

	public ExtendedServiceListing retrieveServiceListing(String serviceID) throws EsoeManagerServiceException
	{
		ExtendedServiceListing service = new ExtendedServiceListing();
		service.setActive(true);
		service.setServiceName("Test Service 1");
		service.setServiceURL("https://www.google.com");
		service.setIdentifier("1234567890");
		service.setServiceDescription("This is a description of this great service");
		service.setServiceAuthorizationFailureMessage("This authz has failed chump");

		return service;
	}

	public List<ServiceContact> retrieveServiceContacts(String serviceIdentifier) throws EsoeManagerServiceException
	{
		ServiceContact contact = new ServiceContact();
		contact.setContactID("23");
		contact.setName("Bradley Beddoes");
		contact.setEmail("beddoes@test.com");
		contact.setTelephone("1234");
		contact.setCompany("Intient");
		contact.setType("technical");

		ServiceContact contact2 = new ServiceContact();
		contact2.setContactID("3");
		contact2.setName("radley Beddoes");
		contact2.setEmail("eddoes@test.com");
		contact2.setTelephone("234");
		contact2.setCompany("ent");
		contact2.setType("administrative");

		ServiceContact contact3 = new ServiceContact();
		contact3.setContactID("3");
		contact3.setName("Some Guy");
		contact3.setEmail("blah@test.com");
		contact3.setTelephone("34");
		contact3.setCompany("QUT");
		contact3.setType("support");

		List<ServiceContact> response = new ArrayList<ServiceContact>();
		response.add(contact);
		response.add(contact2);

		if (this.addedContact)
			response.add(contact3);

		return response;
	}

	public List<ServiceNode> retrieveServiceNodes(String serviceIdentifier) throws EsoeManagerServiceException
	{
		ServiceNode node1 = new ServiceNode();
		node1.setNodeIdentifier("12345");
		node1.setNodeURL("http://arum.qut.edu.au:8080");
		node1.setAcs("/spep/sso");
		node1.setSls("/spep/services/spep/singleLogout");
		node1.setCcs("/spep/services/spep/authzCacheClear");
		node1.setActive(true);

		ServiceNode node2 = new ServiceNode();
		node2.setNodeIdentifier("4356");
		node2.setNodeURL("http://baram.qut.edu.au:8080");
		node2.setAcs("/spep/sso");
		node2.setSls("/spep/services/spep/singleLogout");
		node2.setCcs("/spep/services/spep/authzCacheClear");
		node2.setActive(false);

		ServiceNode node3 = new ServiceNode();
		node3.setNodeIdentifier("90");
		node3.setNodeURL("http://addednode.qut.edu.au:8080");
		node3.setAcs("/spep/sso");
		node3.setSls("/spep/services/spep/singleLogout");
		node3.setCcs("/spep/services/spep/authzCacheClear");
		node3.setActive(false);

		List<ServiceNode> response = new ArrayList<ServiceNode>();
		response.add(node1);
		response.add(node2);

		if (this.addedNode)
			response.add(node3);

		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.qut.middleware.esoemanager.client.rpc.EsoeManagerService#
	 * toggleServiceNodeState(java.lang.String, java.lang.String)
	 */
	public void toggleServiceNodeState(String serviceIdentifier, String nodeIdentifier)
			throws EsoeManagerServiceException
	{
		try
		{
			Thread.sleep(500);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.qut.middleware.esoemanager.client.rpc.EsoeManagerService#
	 * saveServiceNodeConfiguration(java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void saveServiceNodeConfiguration(String serviceIdentifier, String nodeIdentifier, String nodeURL,
			String nodeACS, String nodeSLS, String nodeCCS) throws EsoeManagerServiceException
	{
		try
		{
			Thread.sleep(500);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.qut.middleware.esoemanager.client.rpc.EsoeManagerService#
	 * saveServiceDescription(java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	public void saveServiceDescription(String serviceIdentifier, String serviceName, String serviceURL,
			String serviceDescription, String serviceAccessDeniedMessage) throws EsoeManagerServiceException
	{
		try
		{
			Thread.sleep(500);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.qut.middleware.esoemanager.client.rpc.EsoeManagerService#
	 * toggleServiceState(java.lang.String)
	 */
	public void toggleServiceState(String serviceIdentifier) throws EsoeManagerServiceException
	{
		try
		{
			Thread.sleep(500);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.qut.middleware.esoemanager.client.rpc.EsoeManagerService#addServiceNode
	 * (java.lang.String, java.lang.String)
	 */
	public void createServiceNode(String serviceIdentifier, String newNodeURL)
	{
		try
		{
			addedNode = true;
			Thread.sleep(500);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.qut.middleware.esoemanager.client.rpc.EsoeManagerService#
	 * saveServiceContact(java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	public void saveServiceContact(String contactID, String name, String email, String telephone, String company,
			String type) throws EsoeManagerServiceException
	{
		try
		{
			Thread.sleep(500);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.qut.middleware.esoemanager.client.rpc.EsoeManagerService#
	 * addServiceContact(java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	public void createServiceContact(String name, String email, String telelphone, String company, String type)
			throws EsoeManagerServiceException
	{
		try
		{
			this.addedContact = true;
			Thread.sleep(500);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.qut.middleware.esoemanager.client.rpc.EsoeManagerService#
	 * retrieveConfiguredAttributeList()
	 */
	public List<String> retrieveConfiguredAttributeList()
	{
		try
		{
			Thread.sleep(500);
			List<String> result = new ArrayList<String>();
			result.add("uid");
			result.add("emailAddress");
			result.add("groupMembership");
			result.add("telephone");
			result.add("eduPersonPrincipalName");

			return result;
		}
		catch (InterruptedException e)
		{
			return null;
		}
	}

	public List<Policy> retrieveServicePolicies(String serviceIdentifier)
	{
		try
		{
			ConvertToUIPolicy converter = new ConvertToUIPolicy();
			InputStream in = this.getClass().getResourceAsStream("policy1.xml");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			int c;
			while ((c = in.read()) != -1)
			{
				baos.write((char) c);
			}
			Policy pol1 = converter.convert(baos.toByteArray(), true);
			
			List<Policy> policies = new ArrayList<Policy>();
			policies.add(pol1);
			
			return policies;

		}
		catch (UnmarshallerException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.qut.middleware.esoemanager.client.rpc.EsoeManagerService#
	 * createNewServicePolicy(java.lang.String)
	 */
	public void createNewServicePolicy(String serviceIdentifier, Policy newPolicy) throws EsoeManagerServiceException
	{
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.qut.middleware.esoemanager.client.rpc.EsoeManagerService#
	 * saveServicePolicy(java.lang.String,
	 * com.qut.middleware.esoemanager.client.rpc.bean.Policy)
	 */
	public void saveServicePolicy(String serviceIdentifier, Policy policy) throws EsoeManagerServiceException
	{
		if (policy.getPolicyID() == null || policy.getPolicyID().length() == 0)
		{
			policy.setPolicyID("qwewrs!#-" + id++);
			this.result.add(policy);
		}
		else
			for (Policy pol : this.result)
			{
				if (pol.getPolicyID().equals(policy.getPolicyID()))
				{
					this.result.remove(pol);
					this.result.add(policy);
					return;
				}
			}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.qut.middleware.esoemanager.client.rpc.EsoeManagerService#
	 * retrieveServicePolicy(java.lang.String, java.lang.String)
	 */
	public Policy retrieveServicePolicy(String serviceIdentifier, String policyID) throws EsoeManagerServiceException
	{
		for (Policy pol : this.result)
		{
			if (pol.getPolicyID().equals(policyID))
			{
				return pol;
			}
		}

		throw new EsoeManagerServiceException("Policy doesn't exist");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.qut.middleware.esoemanager.client.rpc.EsoeManagerService#
	 * toggleServicePolicyState(java.lang.String, boolean)
	 */
	public void toggleServicePolicyState(String serviceIdentifier, String policyID, boolean active)
			throws EsoeManagerServiceException
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.qut.middleware.esoemanager.client.rpc.EsoeManagerService#
	 * deleteServicePolicyState(java.lang.String, java.lang.String)
	 */
	public void deleteServicePolicy(String serviceIdentifier, String policyID) throws EsoeManagerServiceException
	{
		for (Policy pol : this.result)
		{
			if (pol.getPolicyID().equals(policyID))
			{
				this.result.remove(pol);
				return;
			}
		}

		throw new EsoeManagerServiceException("Policy doesn't exist");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.qut.middleware.esoemanager.client.rpc.EsoeManagerService#
	 * retrieveServicePolicyXML(java.lang.String, java.lang.String)
	 */
	public String retrieveServicePolicyXML(String serviceIdentifier, String policyID)
			throws EsoeManagerServiceException
	{
		return this.xmlPolicy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.qut.middleware.esoemanager.client.rpc.EsoeManagerService#
	 * createNewServicePolicy(java.lang.String, java.lang.String)
	 */
	public void createNewServicePolicy(String serviceIdentifier, String newPolicy) throws EsoeManagerServiceException,
			EsoeManagerInvalidXMLException
	{
		if (newPolicy.equals("fake"))
			throw new EsoeManagerInvalidXMLException("SAXParse Exception, the element FAKE is not schema valid");

		this.xmlPolicy = newPolicy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.qut.middleware.esoemanager.client.rpc.EsoeManagerService#
	 * saveServicePolicy(java.lang.String, java.lang.String)
	 */
	public void saveServicePolicy(String serviceIdentifier, String policyID, String policy)
			throws EsoeManagerServiceException, EsoeManagerInvalidXMLException
	{
		if (policy.equals("fake"))
			throw new EsoeManagerInvalidXMLException("SAXParse Exception, the element FAKE is not schema valid");

		this.xmlPolicy = policy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.qut.middleware.esoemanager.client.rpc.EsoeManagerService#createServiceKey
	 * (java.lang.String)
	 */
	public void createServiceKey(String serviceIdentifier) throws EsoeManagerServiceException
	{
		KeyDetails key = new KeyDetails();
		key.setKeypairName("qwErt5");
		key.setKeypairPassphrase("ewrqwer");
		key.setKeystorePassphrase("fasdfadsf");

		Calendar rightNow = Calendar.getInstance();

		if (this.keyCount == 1)
		{
			rightNow.add(Calendar.DAY_OF_YEAR, 29);
			key.setExpiryDate(rightNow.getTime());
			key.setExpireError(true);
		}
		else
			if (this.keyCount == 2)
			{
				rightNow.add(Calendar.DAY_OF_YEAR, 55);
				key.setExpiryDate(rightNow.getTime());
				key.setExpireWarn(true);
			}
			else
				if (this.keyCount == 3)
				{
					rightNow.add(Calendar.DAY_OF_YEAR, 400);
					key.setExpiryDate(rightNow.getTime());
					key.setExpireError(false);
					key.setExpireWarn(false);
				}
				else
				{
					this.keyCount = 0;
					key.setExpiryDate(rightNow.getTime());
					key.setExpireError(false);
					key.setExpireWarn(false);
				}

		this.keyCount++;
		this.keys.add(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.qut.middleware.esoemanager.client.rpc.EsoeManagerService#
	 * retrieveServiceKeys(java.lang.String)
	 */
	public List<KeyDetails> retrieveServiceKeys(String serviceIdentifier) throws EsoeManagerServiceException
	{
		return this.keys;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.qut.middleware.esoemanager.client.rpc.EsoeManagerService#
	 * retrieveNodeConfigurations(java.lang.String)
	 */
	public List<ServiceNodeConfiguration> retrieveNodeConfigurations(String serviceIdentifier)
			throws EsoeManagerServiceException
	{
		ServiceNodeConfiguration conf1 = new ServiceNodeConfiguration();
		conf1.setEsoeIdentifier("com:company:esoe");
		conf1.setKeystorePassword("hello123");
		conf1.setMetadataURL("http://esoe.company.com/metadata.htm");
		conf1.setNodeIdentifier("12345");
		conf1.setSpepIdentifier("com:company:service");
		conf1.setSpepKeyAlias("spep key");
		conf1.setSpepKeyPassword("spep123");
		conf1.setServiceHost("http://node1.service.company.com");

		this.nodeConfigs.add(conf1);

		ServiceNodeConfiguration conf2 = new ServiceNodeConfiguration();
		conf2.setEsoeIdentifier("com:company:esoe");
		conf2.setKeystorePassword("hello123");
		conf2.setMetadataURL("http://esoe.company.com/metadata.htm");
		conf2.setNodeIdentifier("6789");
		conf2.setSpepIdentifier("com:company:service2");
		conf2.setSpepKeyAlias("spep key");
		conf2.setSpepKeyPassword("spep123");
		conf2.setServiceHost("http://node2.service.company.com");

		this.nodeConfigs.add(conf2);

		return this.nodeConfigs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.qut.middleware.esoemanager.client.rpc.EsoeManagerService#revokeKey
	 * (java.lang.String, java.lang.String)
	 */
	public void deleteServiceKey(String serviceIdentifier, String keypairName) throws EsoeManagerServiceException
	{
		// TODO Auto-generated method stub

	}

	public String createServicePolicy(String serviceIdentifier, Policy policy) throws EsoeManagerServiceException
	{
		String polID = "qwewrs!#-" + id++;
		policy.setPolicyID(polID);
		this.result.add(policy);

		return polID;
	}

	public String createServicePolicy(String serviceIdentifier, String policy) throws EsoeManagerServiceException,
			EsoeManagerInvalidXMLException
	{
		return null;
	}

	@Override
	public void saveServicePolicy(String serviceIdentifier, String policy) throws EsoeManagerServiceException,
			EsoeManagerInvalidXMLException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void toggleServiceNodeState(String serviceID, String nodeIdentifier, boolean active)
			throws EsoeManagerServiceException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void toggleServiceState(String serviceID, boolean active) throws EsoeManagerServiceException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createServiceContact(String serviceID, String contactID, String name, String email, String telelphone,
			String company, String type) throws EsoeManagerServiceException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createServiceNode(String serviceID, String newNodeURL, String newNodeID)
			throws EsoeManagerServiceException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveServiceContact(String serviceID, String contactID, String name, String email, String telephone,
			String company, String type) throws EsoeManagerServiceException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteServiceContact(String serviceID, String contactID) throws EsoeManagerServiceException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public EsoeDashBean getESOEDashboardDetails() throws EsoeManagerServiceException
	{
		EsoeDashBean bean = new EsoeDashBean();
		
		bean.setActiveNodes("5");
		bean.setActivePolicies("15");
		bean.setActiveServices("1");
		bean.setNumNodes("13");
		bean.setNumPolicies("123");
		bean.setNumServices("12");
		
		return bean;
	}

	@Override
	public MetadataDashBean getMDDashboardDetails() throws EsoeManagerServiceException
	{
		MetadataDashBean bean = new MetadataDashBean();
		
		List<KeyDetails> keys = new ArrayList<KeyDetails>();
		KeyDetails key1 = new KeyDetails();
		key1.setExpiryDate(new Date());
		key1.setKeypairName("esoekeypair");
		key1.setKeypairPassphrase("secureme123");
		key1.setKeystorePassphrase("securemeks123");
		
		keys.add(key1);
		
		bean.setKeys(keys);
		
		return bean;
	}

	@Override
	public ServicesDashBean getServicesDashboardDetails() throws EsoeManagerServiceException
	{
		ServicesDashBean bean = new ServicesDashBean();
		Map<String, Date> exp = new HashMap<String, Date>();
		
		return bean;
	}

	@Override
	public String getAttributePolicyXML() throws EsoeManagerServiceException
	{
		return "<attributes>A policy would go here ;-) </attributes>";
	}

	@Override
	public String getMetadataXML() throws EsoeManagerServiceException
	{
		return "<metadata>Metadata would go here ;-) </metadata>";
	}

	@Override
	public void saveAttributePolicyXML(String policy) throws EsoeManagerServiceException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<ServiceStartupBean> getStartupDashboardDetails() throws EsoeManagerServiceException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String retrieveServiceDescriptor(String serviceID) throws EsoeManagerServiceException
	{
		String descriptor = "<SPSSODescriptor>this is clearly invalid ;-) </SPSSODescriptor>";
		return descriptor;
	}

	@Override
	public List<ServiceStartupBean> retrieveServiceStartups(String serviceID) throws EsoeManagerServiceException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createService(String entityID, String serviceName, String serviceURL, String serviceDescription,
			String entityHost, String firstNode, ServiceContact techContact) throws EsoeManagerServiceException
	{
		return "1";		
	}	
}
