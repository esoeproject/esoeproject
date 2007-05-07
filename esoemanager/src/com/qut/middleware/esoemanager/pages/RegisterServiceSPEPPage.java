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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import net.sf.click.control.Form;
import net.sf.click.control.Submit;

import com.qut.middleware.esoemanager.bean.ServiceNodeBean;
import com.qut.middleware.esoemanager.pages.forms.impl.ServiceForm;

public class RegisterServiceSPEPPage extends SPEPPage
{
	public String headInclude = "ajax/ajax-registerservice-head.htm";
	public String bodyOnload = "registerAjax();";
	
	/* Service Details */
	ServiceForm serviceDetails;

	public List<String> defaultSingleLogoutService;
	public List<String> defaultAssertionConsumerService;
	public List<String> defaultCacheClearService;

	public RegisterServiceSPEPPage()
	{
		super();
	}

	@Override
	public void onInit()
	{
		super.onInit();

		Submit nextButton = new Submit(PageConstants.NAV_NEXT_LABEL, this, PageConstants.NAV_NEXT_FUNC);
		Submit backButton = new Submit(PageConstants.NAV_PREV_LABEL, this, PageConstants.NAV_PREV_FUNC);

		this.spepDetails.add(backButton);
		this.spepDetails.add(nextButton);
		this.spepDetails.setButtonAlign(Form.ALIGN_RIGHT);
	}

	@Override
	public void onPost()
	{
		super.onPost();

		if (this.spepDetails.isValid())
		{
			/* Move allow users to enter additional speps, send current form back */
			String path = getContext().getPagePath(RegisterServiceSPEPPage.class);
			setRedirect(path);
		}
	}

	@Override
	public void onGet()
	{
		/* Check if previous registration stage completed */
		Boolean status = (Boolean) this.retrieveSession(PageConstants.STAGE2_RES);
		if (status == null || status.booleanValue() != true)
		{
			previousClick();
		}

		if (this.action != null)
		{
			super.onGet();
		}
		else
		{
			/* Set the default NodeURL as the service URL */
			this.spepDetails.getField(PageConstants.SPEP_NODE_URL).setValue((String) this.retrieveSession(PageConstants.STORED_SERVICE_URL));

			/* Setup the default values for service endpoints dependend on technology selected */
			if (this.spepDetails.getField(PageConstants.SERVICE_TYPE).getValue().equals(PageConstants.SERVICE_TYPE_JAVA))
			{
				this.spepDetails.getField(PageConstants.ASSERTION_CONSUMER_SERVICE).setValue(
						this.defaultAssertionConsumerService.get(0));
				this.spepDetails.getField(PageConstants.SINGLE_LOGOUT_SERVICE)
						.setValue(this.defaultSingleLogoutService.get(0));
				this.spepDetails.getField(PageConstants.CACHE_CLEAR_SERVICE).setValue(this.defaultCacheClearService.get(0));
			}
			
			if (this.spepDetails.getField(PageConstants.SERVICE_TYPE).getValue().equals(PageConstants.SERVICE_TYPE_APACHE))
			{
				this.spepDetails.getField(PageConstants.ASSERTION_CONSUMER_SERVICE).setValue(
						this.defaultAssertionConsumerService.get(1));
				this.spepDetails.getField(PageConstants.SINGLE_LOGOUT_SERVICE)
						.setValue(this.defaultSingleLogoutService.get(1));
				this.spepDetails.getField(PageConstants.CACHE_CLEAR_SERVICE).setValue(this.defaultCacheClearService.get(1));
			}
			
			if (this.spepDetails.getField(PageConstants.SERVICE_TYPE).getValue().equals(PageConstants.SERVICE_TYPE_IIS))
			{
				this.spepDetails.getField(PageConstants.ASSERTION_CONSUMER_SERVICE).setValue(
						this.defaultAssertionConsumerService.get(2));
				this.spepDetails.getField(PageConstants.SINGLE_LOGOUT_SERVICE)
						.setValue(this.defaultSingleLogoutService.get(2));
				this.spepDetails.getField(PageConstants.CACHE_CLEAR_SERVICE).setValue(this.defaultCacheClearService.get(2));
			}
		}
	}

	public boolean nextClick()
	{
		String redirectPath;
		this.serviceNodes = (Vector<ServiceNodeBean>) this.retrieveSession(PageConstants.STORED_SERVICE_NODES);
		
		/* Attempt to save the incoming data as user has clicked next instead of save in all likely hood */
		if (this.serviceNodes == null || this.serviceNodes.size() == 0)
		{
			if (this.spepDetails.isValid())
			{
				try
				{
					URL validHost = new URL(this.spepDetails.getFieldValue(PageConstants.SPEP_NODE_URL));
					this.createOrUpdateSPEP();
				}
				catch (MalformedURLException e)
				{
					// TODO Log4j here
					this.spepDetails.getField(PageConstants.SPEP_NODE_URL).setError(
							"Malformed server address submitted");
					return true;
				}

				/* Attempting to create submittied node failed */
				if (!this.spepDetails.isValid())
				{
					this.serviceNodes = (Vector<ServiceNodeBean>) this
							.retrieveSession(PageConstants.STORED_SERVICE_NODES);
				}
			}
			else
			{
				return true;
			}
		}

		if (this.serviceNodes == null || this.serviceNodes.size() == 0)
		{
			/* We don't care about errors on the submission itself just that no speps have been added */
			this.spepDetails.clearErrors();

			this.spepDetails.setError("No Nodes added, please add at least one node to this service");
			return true;
		}

		/* This stage completed correctly */
		this.storeSession(PageConstants.STAGE3_RES, new Boolean(true));

		/* Move client to add speps for this service */
		redirectPath = getContext().getPagePath(RegisterServiceFinalizePage.class);
		setRedirect(redirectPath);

		return false;
	}

	public boolean previousClick()
	{
		String redirectPath;

		/* Move client to register service page */
		redirectPath = getContext().getPagePath(RegisterServiceContactPersonPage.class);
		setRedirect(redirectPath);

		return false;
	}

	public List<String> getDefaultAssertionConsumerService()
	{
		return this.defaultAssertionConsumerService;
	}

	public void setDefaultAssertionConsumerService(List<String> defaultAssertionConsumerService)
	{
		this.defaultAssertionConsumerService = defaultAssertionConsumerService;
	}

	public List<String> getDefaultSingleLogoutService()
	{
		return this.defaultSingleLogoutService;
	}

	public void setDefaultSingleLogoutService(List<String> defaultSingleLogoutService)
	{
		this.defaultSingleLogoutService = defaultSingleLogoutService;
	}

	public List<String> getDefaultCacheClearService()
	{
		return this.defaultCacheClearService;
	}

	public void setDefaultCacheClearService(List<String> defaultCacheClearService)
	{
		this.defaultCacheClearService = defaultCacheClearService;
	}
}
