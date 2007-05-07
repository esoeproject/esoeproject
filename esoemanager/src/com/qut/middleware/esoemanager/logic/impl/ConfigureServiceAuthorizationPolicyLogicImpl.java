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
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

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
import com.qut.middleware.saml2.schemas.esoe.lxacml.PolicySet;

public class ConfigureServiceAuthorizationPolicyLogicImpl implements ConfigureServiceAuthorizationPolicyLogic
{
	private SPEPDAO spepDAO;
	private PolicyGuard policyGuard;

	/* Local logging instance */
	private Logger logger = Logger.getLogger(ConfigureServiceAuthorizationPolicyLogicImpl.class.getName());

	Unmarshaller<PolicySet> lxacmlUnmarshaller;

	private final String MAR_PKGNAMES = PolicySet.class.getPackage().getName();
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

		this.lxacmlUnmarshaller = new UnmarshallerImpl<PolicySet>(MAR_PKGNAMES, schemas);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.logic.impl.ConfigureServiceAuthorizationPolicyLogic#getActiveServiceAuthorizationPolicy(java.lang.String)
	 */
	public AuthorizationPolicyBean getActiveServiceAuthorizationPolicy(String descriptorID)
			throws ServiceAuthorizationPolicyException
	{
		AuthorizationPolicyBean activePolicy = new AuthorizationPolicyBeanImpl();
		List<Map<String, Object>> rawPolicy;
		try
		{
			rawPolicy = this.spepDAO.queryActiveAuthorizationPolicy(descriptorID);
		}
		catch (SPEPDAOException e)
		{
			throw new ServiceAuthorizationPolicyException("Failure in attempting to get policy", e);
		}

		for (Map<String, Object> policy : rawPolicy)
		{
			activePolicy.setLxacmlPolicy((String) policy.get(Constants.FIELD_LXACML_POLICY));
			activePolicy.setLastUpdated((Date) policy.get(Constants.FIELD_LXACML_DATE_LAST_UPDATED));
		}

		if (activePolicy.getLxacmlPolicy() == null || activePolicy.getLxacmlPolicy().length() == 1)
		{
			throw new ServiceAuthorizationPolicyException(
					"No policy exists in database for this service, default policies must be inserted at service creation");
		}

		return activePolicy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.logic.impl.ConfigureServiceAuthorizationPolicyLogic#updateServiceAuthorizationPolicy(java.lang.String,
	 *      java.lang.String)
	 */
	public void updateServiceAuthorizationPolicy(String descriptorID, String policy) throws PolicyGuardException,
			ServiceAuthorizationPolicyException
	{
		try
		{
			/* Validate supplied policy is valid LXACML */
			this.lxacmlUnmarshaller.unMarshallUnSigned(policy);

			/*
			 * Ensure we store a historical record of this policy update, incase there is a need for future roll back or
			 * determination of what policies where being enforced at some period in time
			 */
			this.spepDAO.insertServiceAuthorizationHistoricalPolicy(descriptorID, policy, new Date());

			/* Determine via implemented PolicyGuard if this change should be aloud to proceed or not */
			this.policyGuard.validatePolicy(policy);
			this.spepDAO.updateServiceAuthorizationPolicy(descriptorID, policy, new Date());
		}
		catch (UnmarshallerException e)
		{
			throw new ServiceAuthorizationPolicyException("Invalid policy XML supplied " + e.getLocalizedMessage());
		}
		catch (SPEPDAOException e)
		{
			this.logger.warn("SPEPDAOException when communicating with repository " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new ServiceAuthorizationPolicyException("SPEPDAOException when communicating with repository "
					+ e.getLocalizedMessage(), e);

		}
	}
}
