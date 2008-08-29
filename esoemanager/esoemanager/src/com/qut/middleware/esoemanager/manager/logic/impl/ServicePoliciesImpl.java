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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.esoemanager.Constants;
import com.qut.middleware.esoemanager.exception.CreateServicePolicyException;
import com.qut.middleware.esoemanager.exception.DeleteServicePolicyException;
import com.qut.middleware.esoemanager.exception.RetrieveServicePolicyException;
import com.qut.middleware.esoemanager.exception.ManagerDAOException;
import com.qut.middleware.esoemanager.exception.SaveServicePolicyException;
import com.qut.middleware.esoemanager.manager.logic.ServicePolicies;
import com.qut.middleware.esoemanager.manager.sqlmap.ManagerDAO;
import com.qut.middleware.esoemanager.util.ConvertToLXACMLPolicy;
import com.qut.middleware.esoemanager.util.ConvertToUIPolicy;
import com.qut.middleware.esoemanager.util.PolicyIDGenerator;
import com.qut.middleware.esoemanager.util.UtilFunctions;
import com.qut.middleware.saml2.exception.MarshallerException;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.handler.Marshaller;
import com.qut.middleware.saml2.handler.Unmarshaller;
import com.qut.middleware.saml2.handler.impl.MarshallerImpl;
import com.qut.middleware.saml2.handler.impl.UnmarshallerImpl;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Policy;

public class ServicePoliciesImpl implements ServicePolicies
{
	private ManagerDAO managerDAO;
	private UtilFunctions utils;
	private ConvertToUIPolicy convertToUIPolicy;
	private ConvertToLXACMLPolicy convertToLXACMLPolicy;
	private PolicyIDGenerator policyIDGenerator;

	private Marshaller<Policy> marshaller;
	private Unmarshaller<Policy> unmarshaller;

	private String[] schema =
	{
		Constants.lxacml
	};

	private Logger logger = LoggerFactory.getLogger(ServicePoliciesImpl.class);

	public ServicePoliciesImpl() throws MarshallerException, UnmarshallerException
	{
		this.marshaller = new MarshallerImpl<Policy>(Policy.class.getPackage().getName(), schema);
		this.unmarshaller = new UnmarshallerImpl<Policy>(Policy.class.getPackage().getName(), schema);
	}

	public List<com.qut.middleware.esoemanager.client.rpc.bean.Policy> retrievePolicies(String serviceID)
			throws RetrieveServicePolicyException
	{
		try
		{
			List<com.qut.middleware.esoemanager.client.rpc.bean.Policy> policies = new ArrayList<com.qut.middleware.esoemanager.client.rpc.bean.Policy>();

			Integer entID = new Integer(serviceID);

			List<Map<String, Object>> policyDataList;
			policyDataList = this.managerDAO.queryServicePolicies(entID);
			for (Map<String, Object> policyData : policyDataList)
			{
				byte[] rawPolicy = (byte[]) policyData.get(Constants.FIELD_LXACML_POLICY);
				boolean activated;
				String activeState = (String) policyData.get(Constants.FIELD_ACTIVE_FLAG);
				if (activeState.equalsIgnoreCase(Constants.IS_ACTIVE))
					activated = true;
				else
					activated = false;

				com.qut.middleware.esoemanager.client.rpc.bean.Policy policy = this.convertToUIPolicy.convert(
						rawPolicy, activated);
				if (policy == null)
				{
					policy = new com.qut.middleware.esoemanager.client.rpc.bean.Policy();
					policy.setInvalid(true);
					String policyID = (String) policyData.get(Constants.FIELD_POLICY_ID);
					policy.setPolicyID(policyID);
				}
				else
				{
					policy.setInvalid(false);
				}

				policies.add(policy);
			}

			return policies;
		}
		catch (ManagerDAOException e)
		{
			throw new RetrieveServicePolicyException("Exception when attempting get service policies", e);
		}
	}

	public com.qut.middleware.esoemanager.client.rpc.bean.Policy retrievePolicy(String serviceID, String policyID)
			throws RetrieveServicePolicyException
	{
		try
		{
			Integer entID = new Integer(serviceID);

			List<Map<String, Object>> policyDataList;
			policyDataList = this.managerDAO.queryServicePolicy(entID, policyID);

			if (policyDataList == null || policyDataList.size() != 1)
				throw new RetrieveServicePolicyException("Exception when retrieving policy, to many results");

			com.qut.middleware.esoemanager.client.rpc.bean.Policy policy = null;
			for (Map<String, Object> policyData : policyDataList)
			{
				byte[] rawPolicy = (byte[]) policyData.get(Constants.FIELD_LXACML_POLICY);
				boolean activated;
				String activeState = (String) policyData.get(Constants.FIELD_ACTIVE_FLAG);
				if (activeState.equalsIgnoreCase(Constants.IS_ACTIVE))
					activated = true;
				else
					activated = false;

				policy = this.convertToUIPolicy.convert(rawPolicy, activated);

				if (policy == null)
				{
					policy = new com.qut.middleware.esoemanager.client.rpc.bean.Policy();
					policy.setInvalid(true);
					policy.setPolicyID(policyID);
				}
				else
				{
					policy.setInvalid(false);
				}
			}

			return policy;
		}
		catch (ManagerDAOException e)
		{
			throw new RetrieveServicePolicyException("Exception when attempting get service policies", e);
		}
	}

	public String retrievePolicyXML(String serviceID, String policyID) throws RetrieveServicePolicyException
	{
		try
		{
			Integer entID = new Integer(serviceID);

			List<Map<String, Object>> policyDataList;
			policyDataList = this.managerDAO.queryServicePolicy(entID, policyID);

			if (policyDataList == null || policyDataList.size() != 1)
				throw new RetrieveServicePolicyException("Exception when retrieving policy, to many results");

			String policy = null;
			for (Map<String, Object> policyData : policyDataList)
			{
				byte[] rawPolicy = (byte[]) policyData.get(Constants.FIELD_LXACML_POLICY);
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
			}

			
			return policy;
		}
		catch (ManagerDAOException e)
		{
			throw new RetrieveServicePolicyException("Exception when attempting get service policy", e);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new RetrieveServicePolicyException("Encoding exception when attempting get service policy", e);
		}
	}

	public String createPolicy(String serviceID, com.qut.middleware.esoemanager.client.rpc.bean.Policy uiPol)
			throws CreateServicePolicyException
	{
		try
		{
			Integer entID = new Integer(serviceID);
			Policy policy = this.convertToLXACMLPolicy.convert(uiPol);

			// Create a unique ID for this policy
			policy.setPolicyId(this.policyIDGenerator.generatePolicyID());

			byte[] rawPolicy = this.marshaller.marshallUnSigned(policy);

			// Determine if policy exists in database already or not
			List<Map<String, Object>> policyDataList = this.managerDAO.queryServicePolicy(entID, policy.getPolicyId());

			if (policyDataList == null || policyDataList.size() == 0)
			{
				this.managerDAO.insertServiceAuthorizationPolicy(entID, policy.getPolicyId(), rawPolicy);
			}
			else
			{
				throw new CreateServicePolicyException("PolicyID must be unique when creating a new policy");
			}

			return policy.getPolicyId();
		}
		catch (MarshallerException e)
		{
			throw new CreateServicePolicyException(e.getLocalizedMessage(), e);
		}
		catch (ManagerDAOException e)
		{
			throw new CreateServicePolicyException(e.getLocalizedMessage(), e);
		}
	}

	public String createPolicyXML(String serviceID, String policy) throws CreateServicePolicyException
	{
		try
		{
			Integer entID = new Integer(serviceID);
			byte[] rawPolicy = policy.getBytes("UTF-16");

			// Validate that policy marshalls correctly (is schema valid)
			Policy pol = this.unmarshaller.unMarshallUnSigned(rawPolicy);

			// Determine if policy exists in database already or not
			List<Map<String, Object>> policyDataList = this.managerDAO.queryServicePolicy(entID, pol.getPolicyId());

			if (policyDataList == null || policyDataList.size() == 0)
			{
				this.managerDAO.insertServiceAuthorizationPolicy(entID, pol.getPolicyId(), rawPolicy);
			}
			else
			{
				throw new CreateServicePolicyException("PolicyID must be unique when creating a new policy");
			}

			return pol.getPolicyId();
		}
		catch (NumberFormatException e)
		{
			throw new CreateServicePolicyException("Number format exception when converting policy", e);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new CreateServicePolicyException("Unsupported format exception when converting policy", e);
		}
		catch (ManagerDAOException e)
		{
			throw new CreateServicePolicyException(e.getLocalizedMessage(), e);
		}
		catch (UnmarshallerException e)
		{
			throw new CreateServicePolicyException("Policy was invalid due to " + e.getLocalizedMessage());
		}
	}

	public void savePolicyXML(String serviceID, String policy) throws SaveServicePolicyException
	{
		try
		{
			Integer entID = new Integer(serviceID);
			byte[] rawPolicy = policy.getBytes("UTF-16");

			// Validate that policy marshalls correctly (is schema valid)
			Policy pol = this.unmarshaller.unMarshallUnSigned(rawPolicy);

			// Determine if policy exists in database already or not
			List<Map<String, Object>> policyDataList = this.managerDAO.queryServicePolicy(entID, pol.getPolicyId());

			if (policyDataList == null || policyDataList.size() == 0)
			{
				throw new SaveServicePolicyException("PolicyID does not exist for this service, please create first");
			}
			else
			{
				this.managerDAO.updateServiceAuthorizationPolicy(entID, pol.getPolicyId(), rawPolicy);
			}
		}
		catch (NumberFormatException e)
		{
			throw new SaveServicePolicyException("Number format exception when converting policy", e);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new SaveServicePolicyException("Unsupported format exception when converting policy", e);
		}
		catch (ManagerDAOException e)
		{
			throw new SaveServicePolicyException(e.getLocalizedMessage(), e);
		}
		catch (UnmarshallerException e)
		{
			throw new SaveServicePolicyException("Policy was invalid due to " + e.getLocalizedMessage());
		}
	}

	public void savePolicy(String serviceID, com.qut.middleware.esoemanager.client.rpc.bean.Policy uiPol)
			throws SaveServicePolicyException
	{
		try
		{
			Integer entID = new Integer(serviceID);
			Policy policy = this.convertToLXACMLPolicy.convert(uiPol);
			byte[] rawPolicy = this.marshaller.marshallUnSigned(policy);

			// Determine if policy exists in database already or not
			List<Map<String, Object>> policyDataList = this.managerDAO.queryServicePolicy(entID, policy.getPolicyId());

			if (policyDataList == null || policyDataList.size() == 0)
			{
				throw new SaveServicePolicyException("PolicyID does not exist for this service, please create first");
			}
			else
			{
				this.managerDAO.updateServiceAuthorizationPolicy(entID, policy.getPolicyId(), rawPolicy);
			}
		}
		catch (MarshallerException e)
		{
			throw new SaveServicePolicyException(e.getLocalizedMessage(), e);
		}
		catch (ManagerDAOException e)
		{
			throw new SaveServicePolicyException(e.getLocalizedMessage(), e);
		}
	}

	public void deletePolicy(String serviceID, String policyID) throws DeleteServicePolicyException
	{
		Integer entID = new Integer(serviceID);
		try
		{
			this.managerDAO.deleteServicePolicy(entID, policyID);
		}
		catch (ManagerDAOException e)
		{
			throw new DeleteServicePolicyException(e.getLocalizedMessage(), e);
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

	public ConvertToUIPolicy getConvertToUIPolicy()
	{
		return convertToUIPolicy;
	}

	public void setConvertToUIPolicy(ConvertToUIPolicy convertToUIPolicy)
	{
		this.convertToUIPolicy = convertToUIPolicy;
	}

	public ConvertToLXACMLPolicy getConvertToLXACMLPolicy()
	{
		return convertToLXACMLPolicy;
	}

	public void setConvertToLXACMLPolicy(ConvertToLXACMLPolicy convertToLXACMLPolicy)
	{
		this.convertToLXACMLPolicy = convertToLXACMLPolicy;
	}

	public PolicyIDGenerator getPolicyIDGenerator()
	{
		return policyIDGenerator;
	}

	public void setPolicyIDGenerator(PolicyIDGenerator policyIDGenerator)
	{
		this.policyIDGenerator = policyIDGenerator;
	}

	public UtilFunctions getUtils()
	{
		return utils;
	}

	public void setUtils(UtilFunctions utils)
	{
		this.utils = utils;
	}
}
