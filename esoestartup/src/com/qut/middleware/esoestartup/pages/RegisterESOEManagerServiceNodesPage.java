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
import java.util.Vector;

import net.sf.click.control.Form;
import net.sf.click.control.Submit;

import com.qut.middleware.esoemanager.bean.ServiceNodeBean;
import com.qut.middleware.esoemanager.pages.SPEPPage;
import com.qut.middleware.esoemanager.pages.forms.impl.SPEPForm;

public class RegisterESOEManagerServiceNodesPage extends SPEPPage
{
	public String defaultSingleLogoutService;
	public String defaultAssertionConsumerService;
	public String defaultCacheClearService;

	public RegisterESOEManagerServiceNodesPage()
	{
		this.spepDetails = new SPEPForm();
	}

	public void onInit()
	{
		super.onInit();

		/* No need to choose type we know its java */
		this.spepDetails.remove(this.spepDetails.getField(PageConstants.SERVICE_TYPE));

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
			String path = getContext().getPagePath(RegisterESOEManagerServiceNodesPage.class);
			setRedirect(path);
		}
	}

	@Override
	public void onGet()
	{
		/* Check if previous registration stage completed */
		Boolean status = (Boolean) this.retrieveSession(PageConstants.STAGE5_RES);
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
			try
			{
				String node = (String) this.retrieveSession(PageConstants.STORED_SERVICE_URL);
				URL nodeURL = new URL(node);
				if (nodeURL.getPort() != -1 )
					this.spepDetails.getField(PageConstants.SPEP_NODE_URL).setValue(
							nodeURL.getProtocol() + PageConstants.PROTOCOL_SEPERATOR + nodeURL.getHost()
									+ PageConstants.PORT_SEPERATOR + nodeURL.getPort());
				else
					this.spepDetails.getField(PageConstants.SPEP_NODE_URL).setValue(
							nodeURL.getProtocol() + PageConstants.PROTOCOL_SEPERATOR + nodeURL.getHost());
			}
			catch (MalformedURLException e)
			{
				this.spepDetails.getField(PageConstants.SPEP_NODE_URL).setValue(null);
			}

			/* Setup the default values for service endpoints dependend on technology selected */
			this.spepDetails.getField(PageConstants.ASSERTION_CONSUMER_SERVICE).setValue(
					this.defaultAssertionConsumerService);
			this.spepDetails.getField(PageConstants.SINGLE_LOGOUT_SERVICE).setValue(this.defaultSingleLogoutService);
			this.spepDetails.getField(PageConstants.CACHE_CLEAR_SERVICE).setValue(this.defaultCacheClearService);
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

		/* This stage completed correctly */
		this.storeSession(PageConstants.STAGE6_RES, new Boolean(true));

		/* Move client to add speps for this service */
		redirectPath = getContext().getPagePath(RegisterESOECryptoPage.class);
		setRedirect(redirectPath);

		return false;
	}

	public boolean previousClick()
	{
		/* Move client to register service page */
		String path = getContext().getPagePath(RegisterESOEManagerServicePage.class);
		setRedirect(path);

		return false;
	}

	public String getDefaultAssertionConsumerService()
	{
		return defaultAssertionConsumerService;
	}

	public void setDefaultAssertionConsumerService(String defaultAssertionConsumerService)
	{
		this.defaultAssertionConsumerService = defaultAssertionConsumerService;
	}

	public String getDefaultCacheClearService()
	{
		return defaultCacheClearService;
	}

	public void setDefaultCacheClearService(String defaultCacheClearService)
	{
		this.defaultCacheClearService = defaultCacheClearService;
	}

	public String getDefaultSingleLogoutService()
	{
		return defaultSingleLogoutService;
	}

	public void setDefaultSingleLogoutService(String defaultSingleLogoutService)
	{
		this.defaultSingleLogoutService = defaultSingleLogoutService;
	}
}
