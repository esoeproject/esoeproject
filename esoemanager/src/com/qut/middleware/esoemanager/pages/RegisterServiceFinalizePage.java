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

import java.util.Vector;

import net.sf.click.control.Form;
import net.sf.click.control.Submit;

import org.apache.log4j.Logger;

import com.qut.middleware.esoemanager.bean.ContactPersonBean;
import com.qut.middleware.esoemanager.bean.ServiceBean;
import com.qut.middleware.esoemanager.bean.ServiceNodeBean;
import com.qut.middleware.esoemanager.bean.impl.ServiceBeanImpl;
import com.qut.middleware.esoemanager.exception.RegisterServiceException;
import com.qut.middleware.esoemanager.logic.RegisterServiceLogic;

public class RegisterServiceFinalizePage extends BorderPage
{
	/* Backend business logic for registering a service */
	private RegisterServiceLogic logic;
	
	public Form navigation;
	public Boolean status1;
	public Boolean status2;
	public Boolean status3;
	
	public String serviceName;
	public String serviceURL;
	public String serviceDescription;
	public String serviceAuthzFailureMsg;
	public Vector<ContactPersonBean> contacts;
	public Vector<ServiceNodeBean> serviceNodes;
	
	/* Local logging instance */
	private Logger logger = Logger.getLogger(RegisterServiceFinalizePage.class.getName());
	
	public RegisterServiceFinalizePage()
	{
		super();
		this.navigation = new Form();
	}
	
	public void setRegisterServiceLogic(RegisterServiceLogic logic)
	{
		this.logic = logic;
		
		Submit cmpltButton = new Submit(PageConstants.NAV_COMPLETE_LABEL, this, PageConstants.NAV_COMPLETE_FUNC);
		Submit backButton = new Submit(PageConstants.NAV_PREV_LABEL, this, PageConstants.NAV_PREV_FUNC);
		
		this.navigation.add(backButton);
		this.navigation.add(cmpltButton);
		this.navigation.setButtonAlign(Form.ALIGN_RIGHT);
	}
	
	@Override
	public void onInit()
	{
		super.onInit();
		
		this.serviceName = (String) this.retrieveSession(PageConstants.STORED_SERVICE_NAME);
		this.serviceURL = (String) this.retrieveSession(PageConstants.STORED_SERVICE_URL);
		this.serviceDescription = (String)this.retrieveSession(PageConstants.STORED_SERVICE_DESC);
		this.serviceAuthzFailureMsg = (String)this.retrieveSession(PageConstants.STORED_SERVICE_AUTHZ_MSG);
		this.contacts = (Vector<ContactPersonBean>) this.retrieveSession(PageConstants.STORED_CONTACTS);
		this.serviceNodes = (Vector<ServiceNodeBean>) this.retrieveSession(PageConstants.STORED_SERVICE_NODES);
	}
	
	@Override
	public void onGet()
	{
		/* Determine if what is presented is valid */
		if(!validateStatus())
			return;
	}
	
	public boolean completeClick()
	{
		ServiceBean bean = new ServiceBeanImpl();
		String registeredServiceID;
		
		/* Ensure everything is submitted */
		if(validateStatus())
		{
			bean.setServiceName(this.serviceName);
			bean.setServiceURL(this.serviceURL);
			bean.setServiceDescription(this.serviceDescription);
			bean.setServiceAuthzFailureMsg(this.serviceAuthzFailureMsg);
			bean.setContacts(this.contacts);
			bean.setServiceNodes(this.serviceNodes);
			
			try
			{
				registeredServiceID = this.logic.execute(bean);
			}
			catch (RegisterServiceException e)
			{
				this.logger.error("RegisterServiceException thrown, " + e.getLocalizedMessage());
				this.logger.debug(e);
				
				/* Process was in fatal error state, move client to error page */
				// TODO setup error page
				String redirectPath = getContext().getPagePath(RegisterServiceErrorPage.class);
				setRedirect(redirectPath);
				
				cleanSession();
				return true;
			}
			
			/* Process was completed successfully, move client to success page */
			String redirectPath = getContext().getPagePath(RegisterServiceCompletePage.class);
			setForward(redirectPath + "?" + PageConstants.ENTITYID + "=" + registeredServiceID);
			
			cleanSession();
			return false;
		}
		
		return true;
	}
	
	public boolean previousClick()
	{
		/* Move client to register spep nodes page */
		String redirectPath = getContext().getPagePath(RegisterServiceSPEPPage.class);
		setRedirect(redirectPath);
				
		return false;
	}
	
	private void cleanSession()
	{
		/* Cleanup session data
		 */
		this.removeSession(PageConstants.STORED_SERVICE_NAME);
		this.removeSession(PageConstants.STORED_SERVICE_URL);
		this.removeSession(PageConstants.STORED_CONTACTS);
		this.removeSession(PageConstants.STORED_SERVICE_NODES);
		this.removeSession(PageConstants.STAGE1_RES);
		this.removeSession(PageConstants.STAGE2_RES);
		this.removeSession(PageConstants.STAGE3_RES);
		this.removeSession(PageConstants.STORED_SERVICE_DESC);
		this.removeSession(PageConstants.STORED_SERVICE_AUTHZ_MSG);
	}
	
	private boolean validateStatus()
	{
		/* Check if previous registration stages completed */
		this.status1 = (Boolean)this.retrieveSession(PageConstants.STAGE1_RES);
		this.status2 = (Boolean)this.retrieveSession(PageConstants.STAGE2_RES);
		this.status3 = (Boolean)this.retrieveSession(PageConstants.STAGE3_RES);
		
		if(this.status1 == null || this.status1.booleanValue() != true)
		{
			return false;
		}
		if(this.status2 == null || this.status2.booleanValue() != true)
		{
			return false;
		}
		if(this.status3 == null || this.status3.booleanValue() != true)
		{
			return false;
		}
		
		/* Stages have all indicated success ensure all data is actually valid */
		if(this.serviceName == null || this.serviceURL == null || this.serviceDescription == null || this.serviceAuthzFailureMsg == null)
		{
			this.status1 = false;
			this.storeSession(PageConstants.STAGE1_RES, new Boolean(false));
			return false;
		}
		
		if(this.contacts == null || this.contacts.size() == 0)
		{
			this.status2 = false;
			this.storeSession(PageConstants.STAGE2_RES, new Boolean(false));
			return false;
		}
		
		if(this.serviceNodes == null || this.serviceNodes.size() == 0)
		{
			this.status3 = false;
			this.storeSession(PageConstants.STAGE3_RES, new Boolean(false));
			return false;
		}
		
		/* All considered valid at this stage */
		return true;		
	}
}
