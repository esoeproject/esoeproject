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

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import net.sf.click.control.Form;
import net.sf.click.control.Submit;

import com.qut.middleware.esoemanager.bean.ContactPersonBean;
import com.qut.middleware.esoemanager.bean.ServiceNodeBean;
import com.qut.middleware.esoemanager.pages.BorderPage;
import com.qut.middleware.esoestartup.Constants;
import com.qut.middleware.esoestartup.bean.ESOEBean;
import com.qut.middleware.esoestartup.exception.RegisterESOEException;
import com.qut.middleware.esoestartup.logic.RegisterESOELogic;
import com.qut.middleware.esoestartup.pages.forms.impl.FinalizeForm;

public class RegisterESOEFinalizePage extends BorderPage
{
	public RegisterESOELogic logic;
	
	public FinalizeForm finalizeForm;

	public Vector<ContactPersonBean> contacts;
	public Vector<ServiceNodeBean> managerServiceNodes;
	
	public ESOEBean esoeBean;
	
	public boolean error;
	public String errorMessage;
	
	private String tomcatWebappsDirectory;
	
	public RegisterESOEFinalizePage()
	{
		this.finalizeForm = new FinalizeForm();
		
	}
	
	public RegisterESOELogic getRegisterESOELogic()
	{
		return this.logic;
	}
	
	public void setRegisterESOELogic(RegisterESOELogic logic)
	{
		this.logic = logic;
	}

	public void onInit()
	{
		this.esoeBean = (ESOEBean) this.retrieveSession(ESOEBean.class.getName());
		
		this.error = false;
		this.finalizeForm.init();
		
		Submit nextButton = new Submit(PageConstants.NAV_COMPLETE_LABEL, this, PageConstants.NAV_COMPLETE_FUNC);
		Submit backButton = new Submit(PageConstants.NAV_PREV_LABEL, this, PageConstants.NAV_PREV_FUNC);

		this.finalizeForm.add(backButton);
		this.finalizeForm.add(nextButton);
		this.finalizeForm.setButtonAlign(Form.ALIGN_RIGHT);
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
		Boolean status = (Boolean)this.retrieveSession(PageConstants.STAGE8_RES);
		if(status == null || status.booleanValue() != true)
		{
			previousClick();
		}
	}
	
	public boolean completeClick()
	{
		/* Ensure session data is correctly available */
		if(this.esoeBean == null)
		{
			previousClick();
			return false;
		}
		
		if(this.finalizeForm.isValid())
		{
			boolean validDir = false;
			File tomcatDir = new File(this.finalizeForm.getFieldValue(PageConstants.TOMCAT_WEBAPPS_DIRECTORY));
			
			if(tomcatDir.isDirectory())
			{
				
				File[] webapps = tomcatDir.listFiles();
				if(webapps != null)
				{
					List<File> files = Arrays.asList(webapps);
					for(File file : files)
					{
						if (file.getName().equals(Constants.ESOE_STARTUP_WARNAME))
							validDir = true;
					}
				}
				
				if(validDir)
					this.tomcatWebappsDirectory = tomcatDir.getPath();
				else
				{
					this.finalizeForm.setError("Incorrect tomcat webapps directory supplied could not locate esoestartup");
					return true;
				}
			}
			else
			{
				this.finalizeForm.setError("Directory for tomcat webapps must be valid, eg: /var/tomcat5/webapps");
				return true;
			}			
			
			esoeBean.setTomcatWebappPath(this.tomcatWebappsDirectory);
			
			try
			{
				this.logic.execute(esoeBean);
				
				/* Everything has been setup correctly, move to completed page */
				this.storeSession(PageConstants.STAGE9_RES, new Boolean(true));
				
				/* Move client to register service page */
				String path = getContext().getPagePath(RegisterESOECompletePage.class);
				setRedirect(path);
						
				return false;
			}
			catch (RegisterESOEException e)
			{
				error = true;
				this.errorMessage = e.getLocalizedMessage();
				
				return true;
			}
		}
		
		return true;
	}
	
	public boolean previousClick()
	{
		/* Move client to register service page */
		String path = getContext().getPagePath(RegisterESOECryptoPage.class);
		setRedirect(path);
				
		return false;
	}
}
