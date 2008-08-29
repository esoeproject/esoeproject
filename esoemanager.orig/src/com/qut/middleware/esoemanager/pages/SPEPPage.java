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
import java.util.Vector;

import com.qut.middleware.esoemanager.bean.ServiceBean;
import com.qut.middleware.esoemanager.bean.ServiceNodeBean;
import com.qut.middleware.esoemanager.bean.impl.ServiceNodeBeanImpl;
import com.qut.middleware.esoemanager.pages.forms.impl.SPEPForm;

public class SPEPPage extends BorderPage
{
	public Vector<ServiceNodeBean> serviceNodes;
	public String action;
	public Integer ref;

	/* SPEP Details */
	public SPEPForm spepDetails;

	public SPEPPage()
	{
		this.spepDetails = new SPEPForm();
	}

	@Override
	public void onInit()
	{
		this.spepDetails.init();
	}

	@Override
	public void onGet()
	{

	}

	public void onPost()
	{

	}

	protected void deleteSPEP(ServiceBean serviceBean)
	{
		this.serviceNodes = (Vector<ServiceNodeBean>) serviceBean.getServiceNodes();
		if (this.serviceNodes != null)
		{
			if (this.ref != null)
			{
				/*
				 * Submitted value for contact ID will be the current position of the contact in the contacts vector - 1
				 * as listed by velocity when writing the response
				 */
				this.serviceNodes.remove(this.ref - 1);
				serviceBean.setServiceNodes(this.serviceNodes);
			}

		}
	}

	protected void editSPEP(ServiceBean serviceBean)
	{
		this.serviceNodes = (Vector<ServiceNodeBean>) serviceBean.getServiceNodes();
		if (this.serviceNodes != null)
		{
			if (this.ref != null)
			{
				/*
				 * Submitted value for contact ID will be the current position of the contact in the contacts vector - 1
				 * as listed by velocity when writing the response
				 */
				ServiceNodeBean spep = this.serviceNodes.get(this.ref - 1);
				this.serviceNodes.remove(this.ref - 1);
				serviceBean.setServiceNodes(this.serviceNodes);

				/* Now the contact person has been switched out of the active array translate to comma seperated string */
				translateDetails(spep);
			}

		}
	}

	private void translateDetails(ServiceNodeBean bean)
	{
		this.spepDetails.getField(PageConstants.SPEP_NODE_URL).setValue(bean.getNodeURL());
		this.spepDetails.getField(PageConstants.ASSERTION_CONSUMER_SERVICE)
				.setValue(bean.getAssertionConsumerService());
		this.spepDetails.getField(PageConstants.SINGLE_LOGOUT_SERVICE).setValue(bean.getSingleLogoutService());
		this.spepDetails.getField(PageConstants.CACHE_CLEAR_SERVICE).setValue(bean.getCacheClearService());
	}

	protected void createOrUpdateSPEP(ServiceBean serviceBean)
	{
		if (this.spepDetails.isValid())
		{
			/* All data for creating a new contact person is valid, copy to service node bean instance */
			this.serviceNodes = (Vector<ServiceNodeBean>) serviceBean.getServiceNodes();
			if (this.serviceNodes == null)
				this.serviceNodes = new Vector<ServiceNodeBean>();

			this.serviceNodes.add(translateDetails());

			serviceBean.setServiceNodes(this.serviceNodes);
		}
	}

	private ServiceNodeBean translateDetails()
	{
		ServiceNodeBean bean = new ServiceNodeBeanImpl();
		try
		{
			/* Esnure the supplied nodeURL is valid (at least to some degree) */
			URL nodeURL = new URL(this.spepDetails.getFieldValue(PageConstants.SPEP_NODE_URL));

			String sanitisedNodeURL = sanitiseURL(nodeURL);

			bean.setNodeURL(sanitisedNodeURL);
			bean.setAssertionConsumerService(this.spepDetails.getFieldValue(PageConstants.ASSERTION_CONSUMER_SERVICE));
			bean.setSingleLogoutService(this.spepDetails.getFieldValue(PageConstants.SINGLE_LOGOUT_SERVICE));
			bean.setCacheClearService(this.spepDetails.getFieldValue(PageConstants.CACHE_CLEAR_SERVICE));

			return bean;
		}
		catch (MalformedURLException e)
		{
			// TODO Add Log4j here
			this.spepDetails.getField(PageConstants.SPEP_NODE_URL).setError("Malformed server address submitted");
			return null;
		}
	}

	private String sanitiseURL(URL nodeURL)
	{
		String sanitisedNodeURL;
		sanitisedNodeURL = nodeURL.getProtocol() + PageConstants.PROTOCOL_SEPERATOR + nodeURL.getHost();
		if (nodeURL.getPort() != -1)
			sanitisedNodeURL = sanitisedNodeURL + PageConstants.PORT_SEPERATOR + nodeURL.getPort();

		return sanitisedNodeURL;
	}
}
