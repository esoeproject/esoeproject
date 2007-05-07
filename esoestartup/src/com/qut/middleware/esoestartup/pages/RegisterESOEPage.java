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

import net.sf.click.control.Form;
import net.sf.click.control.Submit;

import com.qut.middleware.esoemanager.pages.BorderPage;
import com.qut.middleware.esoestartup.pages.forms.impl.ESOEForm;

public class RegisterESOEPage extends BorderPage
{
	/* ESOE Endpoints form */
	public ESOEForm esoeForm;

	private String defaultSingleSignOnService;
	private String defaultAttributeService;
	private String defaultLxacmlService;
	private String defaultSpepStartupService;

	public RegisterESOEPage()
	{
		this.esoeForm = new ESOEForm();
	}

	public void onInit()
	{
		this.esoeForm.init();
		
		Submit backButton = new Submit(PageConstants.NAV_PREV_LABEL, this, PageConstants.NAV_PREV_FUNC);
		Submit nextButton = new Submit(PageConstants.NAV_NEXT_LABEL, this, PageConstants.NAV_NEXT_FUNC);
		this.esoeForm.add(backButton);
		this.esoeForm.add(nextButton);
		this.esoeForm.setButtonAlign(Form.ALIGN_RIGHT);
		
		this.esoeForm.getField(PageConstants.ESOE_NODE_URL).setValue( (String) this.retrieveSession(PageConstants.STORED_ESOE_NODE_URL));
		this.esoeForm.getField(PageConstants.ESOE_SINGLE_SIGN_ON_SERVICE).setValue( (String) this.retrieveSession(PageConstants.STORED_ESOE_SINGLE_SIGN_ON_SERVICE));
		this.esoeForm.getField(PageConstants.ESOE_ATTRIBUTE_SERVICE).setValue( (String) this.retrieveSession(PageConstants.STORED_ESOE_ATTRIBUTE_SERVICE));
		this.esoeForm.getField(PageConstants.ESOE_LXACML_SERVICE).setValue( (String) this.retrieveSession(PageConstants.STORED_ESOE_LXACML_SERVICE));
		this.esoeForm.getField(PageConstants.ESOE_SPEP_STARTUP_SERVICE).setValue( (String) this.retrieveSession(PageConstants.STORED_ESOE_SPEP_STARTUP_SERVICE));
		this.esoeForm.getField(PageConstants.ESOE_ORGANIZATION_NAME).setValue( (String) this.retrieveSession(PageConstants.STORED_ESOE_ORGANIZATION_NAME));
		this.esoeForm.getField(PageConstants.ESOE_ORGANIZATION_DISPLAY_NAME).setValue( (String) this.retrieveSession(PageConstants.STORED_ESOE_ORGANIZATION_DISPLAY_NAME));
		this.esoeForm.getField(PageConstants.ESOE_ORGANIZATION_URL).setValue( (String) this.retrieveSession(PageConstants.STORED_ESOE_ORGANIZATION_URL));
	}

	public void onGet()
	{
		/* Check if previous registration stage completed */
		Boolean status = (Boolean)this.retrieveSession(PageConstants.STAGE1_RES);
		if(status == null || status.booleanValue() != true)
		{
			previousClick();
		}
			
		this.esoeForm.getField(PageConstants.ESOE_SINGLE_SIGN_ON_SERVICE).setValue(this.defaultSingleSignOnService);
		this.esoeForm.getField(PageConstants.ESOE_ATTRIBUTE_SERVICE).setValue(this.defaultAttributeService);
		this.esoeForm.getField(PageConstants.ESOE_LXACML_SERVICE).setValue(this.defaultLxacmlService);
		this.esoeForm.getField(PageConstants.ESOE_SPEP_STARTUP_SERVICE).setValue(this.defaultSpepStartupService);
	}
	
	public boolean nextClick()
	{
		String redirectPath;
		
		if(this.esoeForm.isValid())
		{
			/* Store details for later retrieval */
			this.storeSession(PageConstants.STORED_ESOE_NODE_URL, this.esoeForm.getFieldValue(PageConstants.ESOE_NODE_URL));
			this.storeSession(PageConstants.STORED_ESOE_SINGLE_SIGN_ON_SERVICE, this.esoeForm.getFieldValue(PageConstants.ESOE_SINGLE_SIGN_ON_SERVICE));
			this.storeSession(PageConstants.STORED_ESOE_ATTRIBUTE_SERVICE, this.esoeForm.getFieldValue(PageConstants.ESOE_ATTRIBUTE_SERVICE));
			this.storeSession(PageConstants.STORED_ESOE_LXACML_SERVICE, this.esoeForm.getFieldValue(PageConstants.ESOE_LXACML_SERVICE));
			this.storeSession(PageConstants.STORED_ESOE_SPEP_STARTUP_SERVICE, this.esoeForm.getFieldValue(PageConstants.ESOE_SPEP_STARTUP_SERVICE));
			this.storeSession(PageConstants.STORED_ESOE_ORGANIZATION_NAME, this.esoeForm.getFieldValue(PageConstants.ESOE_ORGANIZATION_NAME));
			this.storeSession(PageConstants.STORED_ESOE_ORGANIZATION_DISPLAY_NAME, this.esoeForm.getFieldValue(PageConstants.ESOE_ORGANIZATION_DISPLAY_NAME));
			this.storeSession(PageConstants.STORED_ESOE_ORGANIZATION_URL, this.esoeForm.getFieldValue(PageConstants.ESOE_ORGANIZATION_URL));
			
			this.storeSession(PageConstants.STAGE2_RES, new Boolean(true));
			
			/* Move users to third stage, registration of ESOE contacts */
			redirectPath = getContext().getPagePath(RegisterESOELDAPAuthenticatorPage.class);
			setRedirect(redirectPath);
			
			return false;
		}
		
		return true;
	}
	
	public boolean previousClick()
	{
		/* Move client to register service page */
		String path = getContext().getPagePath(RegisterESOEDatabasePage.class);
		setRedirect(path);
				
		return false;
	}

	public String getDefaultAttributeService()
	{
		return defaultAttributeService;
	}

	public void setDefaultAttributeService(String defaultAttributeService)
	{
		this.defaultAttributeService = defaultAttributeService;
	}

	public String getDefaultLxacmlService()
	{
		return defaultLxacmlService;
	}

	public void setDefaultLxacmlService(String defaultLxacmlService)
	{
		this.defaultLxacmlService = defaultLxacmlService;
	}

	public String getDefaultSingleSignOnService()
	{
		return defaultSingleSignOnService;
	}

	public void setDefaultSingleSignOnService(String defaultSingleSignOnService)
	{
		this.defaultSingleSignOnService = defaultSingleSignOnService;
	}

	public String getDefaultSpepStartupService()
	{
		return defaultSpepStartupService;
	}

	public void setDefaultSpepStartupService(String defaultSpepStartupService)
	{
		this.defaultSpepStartupService = defaultSpepStartupService;
	}
}
