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
package com.qut.middleware.esoestartup.pages;

import java.net.MalformedURLException;
import java.net.URL;

import net.sf.click.control.Form;
import net.sf.click.control.Submit;

import com.qut.middleware.esoemanager.pages.BorderPage;
import com.qut.middleware.esoemanager.pages.forms.impl.ServiceForm;
import com.qut.middleware.esoestartup.bean.ESOEBean;

public class RegisterESOEManagerServicePage extends BorderPage
{
	public ServiceForm serviceDetails;

	private ESOEBean esoeBean;

	public RegisterESOEManagerServicePage()
	{
		this.serviceDetails = new ServiceForm();
	}

	public void onInit()
	{
		this.esoeBean = (ESOEBean) this.retrieveSession(ESOEBean.class.getName());

		this.serviceDetails.init();

		Submit nextButton = new Submit(PageConstants.NAV_NEXT_LABEL, this, PageConstants.NAV_NEXT_FUNC);
		Submit backButton = new Submit(PageConstants.NAV_PREV_LABEL, this, PageConstants.NAV_PREV_FUNC);
		this.serviceDetails.getField(PageConstants.SERVICE_URL).setLabel("Service URL (Must end in /esoemanager)");

		this.serviceDetails.add(backButton);
		this.serviceDetails.add(nextButton);
		this.serviceDetails.setButtonAlign(Form.ALIGN_RIGHT);

		if (esoeBean != null)
		{
			this.serviceDetails.getField(PageConstants.SERVICE_NAME).setValue((String) this.esoeBean.getServiceName());
			this.serviceDetails.getField(PageConstants.SERVICE_URL).setValue((String) this.esoeBean.getServiceURL());
			this.serviceDetails.getField(PageConstants.SERVICE_IDENTIFIER).setValue((String) this.esoeBean.getEntityID());

			if (this.serviceDetails.getFieldValue(PageConstants.SERVICE_URL) == null || this.serviceDetails.getFieldValue(PageConstants.SERVICE_URL).length() < 1)
				this.serviceDetails.getField(PageConstants.SERVICE_URL).setValue("http://{HOSTNAME}/esoemanager");

			if (this.serviceDetails.getFieldValue(PageConstants.SERVICE_IDENTIFIER) == null || this.serviceDetails.getFieldValue(PageConstants.SERVICE_IDENTIFIER).length() < 1)
				this.serviceDetails.getField(PageConstants.SERVICE_IDENTIFIER).setValue("http://{HOSTNAME}/esoemanager");

			this.serviceDetails.getField(PageConstants.SERVICE_DESCRIPTION).setValue((String) this.esoeBean.getServiceDescription());
			this.serviceDetails.getField(PageConstants.SERVICE_AUTHZ_FAILURE_MESSAGE).setValue((String) this.esoeBean.getServiceAuthzFailureMsg());
		}
	}

	public boolean nextClick()
	{
		String redirectPath;
		
		/* Ensure session data is correctly available */
		if(this.esoeBean == null)
		{
			previousClick();
			return false;
		}

		if (this.serviceDetails.isValid())
		{
			try
			{
				URL validHost = new URL(this.serviceDetails.getFieldValue(PageConstants.SERVICE_URL));

				/* Store details in the session */
				this.esoeBean.setEntityID(this.serviceDetails.getFieldValue(PageConstants.SERVICE_IDENTIFIER));
				this.esoeBean.setServiceName(this.serviceDetails.getFieldValue(PageConstants.SERVICE_NAME));
				this.esoeBean.setServiceURL(this.serviceDetails.getFieldValue(PageConstants.SERVICE_URL));
				this.esoeBean.setServiceDescription(this.serviceDetails.getFieldValue(PageConstants.SERVICE_DESCRIPTION));
				this.esoeBean.setServiceAuthzFailureMsg(this.serviceDetails.getFieldValue(PageConstants.SERVICE_AUTHZ_FAILURE_MESSAGE));

				/* This stage completed correctly */
				this.storeSession(PageConstants.STAGE6_RES, new Boolean(true));

				/* Move client to add contacts for this service */
				redirectPath = getContext().getPagePath(RegisterESOEManagerServiceNodesPage.class);
				setRedirect(redirectPath);
			}
			catch (MalformedURLException e)
			{
				// TODO Log4j here
				this.serviceDetails.getField(PageConstants.SERVICE_URL).setError("Malformed server address submitted");
				return true;
			}

			return false;
		}

		return true;
	}

	@Override
	public void onGet()
	{
		/* Ensure session data is correctly available */
		if(this.esoeBean == null)
		{
			previousClick();
			return;
		}
		
		/* Check if previous registration stage completed */
		Boolean status = (Boolean) this.retrieveSession(PageConstants.STAGE5_RES);
		if (status == null || status.booleanValue() != true)
		{
			previousClick();
		}

		this.serviceDetails.getField(PageConstants.SERVICE_NAME).setValue(PageConstants.ESOE_MANAGER_SERVICE_NAME);
		this.serviceDetails.getField(PageConstants.SERVICE_DESCRIPTION).setValue(PageConstants.ESOE_MANAGER_SERVICE_DESCRIPTION);
	}

	public boolean previousClick()
	{
		/* Move client to register service page */
		String path = getContext().getPagePath(RegisterESOEContactPersonPage.class);
		setRedirect(path);

		return false;
	}
}
