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
 */
package com.qut.middleware.esoemanager.pages;

import net.sf.click.control.Submit;

import com.qut.middleware.esoemanager.bean.AuthorizationPolicyBean;
import com.qut.middleware.esoemanager.exception.PolicyGuardException;
import com.qut.middleware.esoemanager.exception.ServiceAuthorizationPolicyException;
import com.qut.middleware.esoemanager.logic.ConfigureServiceAuthorizationPolicyLogic;
import com.qut.middleware.esoemanager.pages.forms.impl.PolicyForm;

public class ConfigureServiceAuthorizationPolicyPage extends BorderPage
{
	ConfigureServiceAuthorizationPolicyLogic logic;
	
	/* entityID and serviceID are supplied by name=value attribute pairs on the request */
	public String entityID;
	public String serviceID;
	
	public PolicyForm policyForm;
	public boolean validPolicy;
	public boolean policyGuardRejected;
	public boolean editComplete;
	public String errorMessage;
	

	public ConfigureServiceAuthorizationPolicyPage()
	{
		this.validPolicy = true;
		this.policyGuardRejected = false;
		this.editComplete = false;
		this.policyForm = new PolicyForm();
	}

	@Override
	public void onInit()
	{
		this.policyForm.init();
		Submit submitPolicy = new Submit(PageConstants.NAV_COMPLETE_LABEL, this, PageConstants.NAV_COMPLETE_FUNC);
		this.policyForm.add(submitPolicy);
	}

	@Override
	public void onGet()
	{
		if (this.entityID != null && this.serviceID != null)
		{
			try
			{
				AuthorizationPolicyBean authorizationPolicy = this.logic.getActiveServiceAuthorizationPolicy(this.serviceID);
				this.policyForm.getField(PageConstants.LXACML_POLICY).setValue(authorizationPolicy.getLxacmlPolicy());
				this.policyForm.getField(PageConstants.ENTITYID).setValue(this.entityID);
				this.policyForm.getField(PageConstants.DESCRIPTORID).setValue(this.serviceID);
			}
			catch (ServiceAuthorizationPolicyException e)
			{
				// TODO: Error page
			}
		}
		else
		{
			// TODO: Error page
		}
	}
	
	public boolean completeClick()
	{
		this.entityID = this.policyForm.getFieldValue(PageConstants.ENTITYID);
		this.serviceID = this.policyForm.getFieldValue(PageConstants.DESCRIPTORID);
		
		if(this.policyForm.isValid())
		{
			try
			{
				this.logic.updateServiceAuthorizationPolicy(this.policyForm.getFieldValue(PageConstants.DESCRIPTORID), this.policyForm.getFieldValue(PageConstants.LXACML_POLICY));
			}
			catch (PolicyGuardException e)
			{
				this.policyGuardRejected = true;
				return true;
			}
			catch (ServiceAuthorizationPolicyException e)
			{
				this.validPolicy = false;
				this.errorMessage = e.getLocalizedMessage();
				return true;
			}
			
			this.editComplete = true;
			return false;
		}
		
		return true;
	}

	public ConfigureServiceAuthorizationPolicyLogic getConfigureServiceAuthorizationPolicyLogic()
	{
		return this.logic;
	}

	public void setConfigureServiceAuthorizationPolicyLogic(ConfigureServiceAuthorizationPolicyLogic logic)
	{
		this.logic = logic;
	}
}
