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
import com.qut.middleware.esoestartup.pages.forms.impl.LDAPAuthenticatorForm;

public class RegisterESOELDAPAuthenticatorPage extends BorderPage
{
	/* ESOE LDAP Authenticator form */
	public LDAPAuthenticatorForm ldapForm;

	public RegisterESOELDAPAuthenticatorPage()
	{
		this.ldapForm = new LDAPAuthenticatorForm();
	}

	public void onInit()
	{
		this.ldapForm.init();
		
		Submit backButton = new Submit(PageConstants.NAV_PREV_LABEL, this, PageConstants.NAV_PREV_FUNC);
		Submit nextButton = new Submit(PageConstants.NAV_NEXT_LABEL, this, PageConstants.NAV_NEXT_FUNC);
		this.ldapForm.add(backButton);
		this.ldapForm.add(nextButton);
		this.ldapForm.setButtonAlign(Form.ALIGN_RIGHT);
		
	}

	public void onGet()
	{
		/* Check if previous registration stage completed */
		Boolean status = (Boolean)this.retrieveSession(PageConstants.STAGE2_RES);
		if(status == null || status.booleanValue() != true)
		{
			previousClick();
		}
		
		/* If client has populated data reload it */
		if((String)this.retrieveSession(PageConstants.STORED_LDAP_URL) != null)
		{	
			this.ldapForm.getField(PageConstants.LDAP_URL).setValue((String)this.retrieveSession(PageConstants.STORED_LDAP_URL));
			this.ldapForm.getField(PageConstants.LDAP_PORT).setValue((String)this.retrieveSession(PageConstants.STORED_LDAP_PORT));
			this.ldapForm.getField(PageConstants.LDAP_BASE_DN).setValue((String)this.retrieveSession(PageConstants.STORED_LDAP_BASE_DN));
			this.ldapForm.getField(PageConstants.LDAP_ACCOUNT_IDENTIFIER).setValue((String)this.retrieveSession(PageConstants.STORED_LDAP_ACCOUNT_IDENTIFIER));
			this.ldapForm.getField(PageConstants.LDAP_RECURSIVE).setValue((String)this.retrieveSession(PageConstants.STORED_LDAP_RECURSIVE));
			this.ldapForm.getField(PageConstants.LDAP_DISABLE_SSL).setValue((String)this.retrieveSession(PageConstants.STORED_LDAP_RECURSIVE));
			this.ldapForm.getField(PageConstants.LDAP_ADMIN_USER).setValue((String)this.retrieveSession(PageConstants.STORED_LDAP_ADMIN_USER));
			this.ldapForm.getField(PageConstants.LDAP_ADMIN_PASSWORD).setValue((String)this.retrieveSession(PageConstants.STORED_LDAP_ADMIN_PASSWORD));
		}
	}
	
	public boolean nextClick()
	{
		String redirectPath;
		
		if(this.ldapForm.isValid())
		{
			/* Store details for later retrieval */
			this.storeSession(PageConstants.STORED_LDAP_URL, this.ldapForm.getFieldValue(PageConstants.LDAP_URL));
			this.storeSession(PageConstants.STORED_LDAP_PORT, this.ldapForm.getFieldValue(PageConstants.LDAP_PORT));
			this.storeSession(PageConstants.STORED_LDAP_BASE_DN, this.ldapForm.getFieldValue(PageConstants.LDAP_BASE_DN));
			this.storeSession(PageConstants.STORED_LDAP_ACCOUNT_IDENTIFIER, this.ldapForm.getFieldValue(PageConstants.LDAP_ACCOUNT_IDENTIFIER));
			this.storeSession(PageConstants.STORED_LDAP_RECURSIVE, this.ldapForm.getFieldValue(PageConstants.LDAP_RECURSIVE));
			this.storeSession(PageConstants.STORED_LDAP_DISABLE_SSL, this.ldapForm.getFieldValue(PageConstants.LDAP_DISABLE_SSL));
			this.storeSession(PageConstants.STORED_LDAP_ADMIN_USER, this.ldapForm.getFieldValue(PageConstants.LDAP_ADMIN_USER));
			this.storeSession(PageConstants.STORED_LDAP_ADMIN_PASSWORD, this.ldapForm.getFieldValue(PageConstants.LDAP_ADMIN_PASSWORD));
			
			this.storeSession(PageConstants.STAGE3_RES, new Boolean(true));
			
			/* Move users to third stage, registration of ESOE contacts */
			redirectPath = getContext().getPagePath(RegisterESOEContactPersonPage.class);
			setRedirect(redirectPath);
			
			return false;
		}
		
		return true;
	}
	
	public boolean previousClick()
	{
		/* Move client to register service page */
		String path = getContext().getPagePath(RegisterESOEPage.class);
		setRedirect(path);
				
		return false;
	}
}
