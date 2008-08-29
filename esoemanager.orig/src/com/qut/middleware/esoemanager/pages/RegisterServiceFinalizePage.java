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

import net.sf.click.control.Form;
import net.sf.click.control.Submit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.esoemanager.bean.ServiceBean;
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

	public ServiceBean serviceBean;
	
	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(RegisterServiceFinalizePage.class.getName());
	
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
		serviceBean = (ServiceBean)this.retrieveSession(ServiceBean.class.getName());
	}
	
	@Override
	public void onGet()
	{
		/* Ensure registration session is active */
		if(serviceBean == null)
		{
			previousClick();
			return;
		}
		
		/* Determine if what is presented is valid */
		if(!validateStatus())
			return;
	}
	
	public boolean completeClick()
	{	
		/* Ensure registration session is active */
		if(serviceBean == null)
		{
			previousClick();
			return false;
		}
		
		/* Ensure everything is submitted */
		if(validateStatus())
		{		
			try
			{
				this.logic.execute(serviceBean);
			}
			catch (RegisterServiceException e)
			{
				this.logger.error("RegisterServiceException thrown, " + e.getLocalizedMessage());
				this.logger.debug(e.toString());
				
				/* Process was in fatal error state, move client to error page */
				// TODO setup error page
				String redirectPath = getContext().getPagePath(RegisterServiceErrorPage.class);
				setRedirect(redirectPath);
				
				cleanSession();
				return true;
			}
			
			/* Process was completed successfully, move client to success page */
			String redirectPath = getContext().getPagePath(RegisterServiceCompletePage.class);
			setForward(redirectPath + "?" + PageConstants.EID + "=" + serviceBean.getEntID());
			
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
		this.removeSession(ServiceBean.class.getName());
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
		
		/* All considered valid at this stage */
		return true;		
	}
}
