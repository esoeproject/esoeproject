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

import net.sf.click.control.Form;
import net.sf.click.control.Submit;

import com.qut.middleware.esoemanager.bean.ServiceBean;
import com.qut.middleware.esoemanager.bean.impl.ServiceBeanImpl;
import com.qut.middleware.esoemanager.pages.forms.impl.ServiceForm;

public class RegisterServicePage extends BorderPage
{
	/* Service Details */
	public ServiceForm serviceDetails;

	ServiceBean bean;

	public RegisterServicePage()
	{
		this.serviceDetails = new ServiceForm();
	}

	@Override
	public void onInit()
	{
		bean = new ServiceBeanImpl();
		this.storeSession(ServiceBean.class.getName(), bean);

		this.serviceDetails.init();

		if (this.bean.getEntityID() != null)
			this.serviceDetails.getField(PageConstants.SERVICE_IDENTIFIER).setValue((String) this.bean.getEntityID());

		this.serviceDetails.getField(PageConstants.SERVICE_NAME).setValue((String) this.bean.getServiceName());
		this.serviceDetails.getField(PageConstants.SERVICE_URL).setValue((String) this.bean.getServiceURL());
		this.serviceDetails.getField(PageConstants.SERVICE_DESCRIPTION).setValue((String) this.bean.getServiceDescription());
		this.serviceDetails.getField(PageConstants.SERVICE_AUTHZ_FAILURE_MESSAGE).setValue((String) this.bean.getServiceAuthzFailureMsg());

		Submit nextButton = new Submit(PageConstants.NAV_NEXT_LABEL, this, PageConstants.NAV_NEXT_FUNC);

		this.serviceDetails.add(nextButton);
		this.serviceDetails.setButtonAlign(Form.ALIGN_RIGHT);
	}

	public boolean nextClick()
	{
		String redirectPath;

		if (this.serviceDetails.isValid())
		{
			try
			{
				URL validHost = new URL(this.serviceDetails.getFieldValue(PageConstants.SERVICE_URL));

				/* Store details in the session */
				this.bean.setEntityID(this.serviceDetails.getFieldValue(PageConstants.SERVICE_IDENTIFIER));
				this.bean.setServiceName(this.serviceDetails.getFieldValue(PageConstants.SERVICE_NAME));
				this.bean.setServiceURL(this.serviceDetails.getFieldValue(PageConstants.SERVICE_URL));
				this.bean.setServiceDescription(this.serviceDetails.getFieldValue(PageConstants.SERVICE_DESCRIPTION));
				this.bean.setServiceAuthzFailureMsg(this.serviceDetails.getFieldValue(PageConstants.SERVICE_AUTHZ_FAILURE_MESSAGE));

				/* This stage completed correctly */
				this.storeSession(PageConstants.STAGE1_RES, new Boolean(true));

				/* Move client to add contacts for this service */
				redirectPath = getContext().getPagePath(RegisterServiceContactPersonPage.class);
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
}
