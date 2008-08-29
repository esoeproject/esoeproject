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
 * Purpose:  Configure Service Authorization Policy Logic default implementation
 */
package com.qut.middleware.esoemanager.logic.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.esoemanager.Constants;
import com.qut.middleware.esoemanager.bean.AuthorizationPolicyBean;
import com.qut.middleware.esoemanager.bean.impl.AuthorizationPolicyBeanImpl;
import com.qut.middleware.esoemanager.exception.PolicyGuardException;
import com.qut.middleware.esoemanager.exception.SPEPDAOException;
import com.qut.middleware.esoemanager.exception.ServiceAuthorizationPolicyException;
import com.qut.middleware.esoemanager.guard.PolicyGuard;
import com.qut.middleware.esoemanager.logic.ConfigureServiceAuthorizationPolicyLogic;
import com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.handler.Unmarshaller;
import com.qut.middleware.saml2.handler.impl.UnmarshallerImpl;
import com.qut.middleware.saml2.schemas.esoe.lxacml.Policy;
import com.qut.middleware.saml2.schemas.esoe.lxacml.PolicySet;

public class ConfigureServiceAuthorizationPolicyLogicImpl implements ConfigureServiceAuthorizationPolicyLogic
{
	private SPEPDAO spepDAO;
	private PolicyGuard policyGuard;

	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(ConfigureServiceAuthorizationPolicyLogicImpl.class.getName());

	Unmarshaller<Policy> lxacmlUnmarshaller;

	private final String MAR_PKGNAMES = Policy.class.getPackage().getName();
	private final String[] schemas = { Constants.lxacml, Constants.lxacmlContext };

	public ConfigureServiceAuthorizationPolicyLogicImpl(PolicyGuard policyGuard, SPEPDAO spepDAO) throws IOException,
			UnmarshallerException
	{
		if (policyGuard == null)
		{
			this.logger.error("policyGuard for ConfigureServiceAuthorizationPolicyLogicImpl was NULL");
			throw new IllegalArgumentException("policyGuard for ConfigureServiceAuthorizationPolicyLogicImpl was NULL");
		}
		if (spepDAO == null)
		{
			this.logger.error("spepDAO for ConfigureServiceAuthorizationPolicyLogicImpl was NULL");
			throw new IllegalArgumentException("spepDAO for ConfigureServiceAuthorizationPolicyLogicImpl was NULL");
		}

		this.policyGuard = policyGuard;
		this.spepDAO = spepDAO;

		this.lxacmlUnmarshaller = new UnmarshallerImpl<Policy>(MAR_PKGNAMES, schemas);
	}

	public AuthorizationPolicyBean getActiveServiceAuthorizationPolicy(Integer descID)
			throws ServiceAuthorizationPolicyException
	{
		AuthorizationPolicyBean activePolicy = new AuthorizationPolicyBeanImpl();
		List<Map<String, Object>> rawPolicy;
		try
		{
			rawPolicy = this.spepDAO.queryActiveAuthorizationPolicy(descID);
		}
		catch (SPEPDAOException e)
		{
			throw new ServiceAuthorizationPolicyException("Failure in attempting to get policy", e);
		}

		for (Map<String, Object> policy : rawPolicy)
		{
			activePolicy.setLxacmlPolicy((byte[]) policy.get(Constants.FIELD_LXACML_POLICY));
		}

		if (activePolicy.getLxacmlPolicy() == null || activePolicy.getLxacmlPolicy().length <= 1)
		{
			throw new ServiceAuthorizationPolicyException(
					"No policy exists in database for this service, default policies must be inserted at service creation");
		}

		return activePolicy;
	}

	public void updateServiceAuthorizationPolicy(Integer entID, byte[] policy) throws PolicyGuardException,
			ServiceAuthorizationPolicyException
	{
		try
		{
			/* Validate supplied policy is valid LXACML */
			Policy policyOb = this.lxacmlUnmarshaller.unMarshallUnSigned(policy);

			/* Determine via implemented PolicyGuard if this change should be aloud to proceed or not */
			this.policyGuard.validatePolicy(policy);
			this.spepDAO.updateServiceAuthorizationPolicy(entID, policyOb.getPolicyId(), policy);
		}
		catch (UnmarshallerException e)
		{
			throw new ServiceAuthorizationPolicyException("Invalid policy XML supplied " + e.getLocalizedMessage());
		}
		catch (SPEPDAOException e)
		{
			this.logger.warn("SPEPDAOException when communicating with repository " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new ServiceAuthorizationPolicyException("SPEPDAOException when communicating with repository "
					+ e.getLocalizedMessage(), e);

		}
	}
}
