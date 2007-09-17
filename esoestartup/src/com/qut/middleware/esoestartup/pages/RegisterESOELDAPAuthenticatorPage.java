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
import com.qut.middleware.esoestartup.bean.ESOEBean;
import com.qut.middleware.esoestartup.pages.forms.impl.LDAPAuthenticatorForm;

public class RegisterESOELDAPAuthenticatorPage extends BorderPage
{
	/* ESOE LDAP Authenticator form */
	public LDAPAuthenticatorForm ldapForm;
	
	private ESOEBean esoeBean;

	public RegisterESOELDAPAuthenticatorPage()
	{
		this.ldapForm = new LDAPAuthenticatorForm();
	}

	public void onInit()
	{
		this.esoeBean = (ESOEBean) this.retrieveSession(ESOEBean.class.getName());

		this.ldapForm.init();
		
		Submit backButton = new Submit(PageConstants.NAV_PREV_LABEL, this, PageConstants.NAV_PREV_FUNC);
		Submit nextButton = new Submit(PageConstants.NAV_NEXT_LABEL, this, PageConstants.NAV_NEXT_FUNC);
		this.ldapForm.add(backButton);
		this.ldapForm.add(nextButton);
		this.ldapForm.setButtonAlign(Form.ALIGN_RIGHT);
		
	}

	public void onGet()
	{
		/* Ensure session data is correctly available */
		if(this.esoeBean == null)
		{
			previousClick();
			return;
		}
		
		/* Check if previous registration stage completed */
		Boolean status = (Boolean)this.retrieveSession(PageConstants.STAGE3_RES);
		if(status == null || status.booleanValue() != true)
		{
			previousClick();
		}
		
		/* If client has populated data reload it */
		if((String)this.esoeBean.getLdapURL()!= null)
		{	
			this.ldapForm.getField(PageConstants.LDAP_URL).setValue((String)this.esoeBean.getLdapURL());
			this.ldapForm.getField(PageConstants.LDAP_PORT).setValue((String)this.esoeBean.getLdapServerPort());
			this.ldapForm.getField(PageConstants.LDAP_BASE_DN).setValue((String)this.esoeBean.getLdapServerBaseDN());
			this.ldapForm.getField(PageConstants.LDAP_ACCOUNT_IDENTIFIER).setValue((String)this.esoeBean.getLdapIdentifier());
			this.ldapForm.getField(PageConstants.LDAP_RECURSIVE).setValue((String)this.esoeBean.getLdapRecursive());
			this.ldapForm.getField(PageConstants.LDAP_DISABLE_SSL).setValue((String)this.esoeBean.getLdapDisableSSL());
			this.ldapForm.getField(PageConstants.LDAP_ADMIN_USER).setValue((String)this.esoeBean.getLdapAdminUserDN());
			this.ldapForm.getField(PageConstants.LDAP_ADMIN_PASSWORD).setValue((String)this.esoeBean.getLdapAdminPassword());
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
		
		if(this.ldapForm.isValid())
		{
			/* Store details for later retrieval */
			this.esoeBean.setLdapURL(this.ldapForm.getFieldValue(PageConstants.LDAP_URL));
			this.esoeBean.setLdapServerPort( this.ldapForm.getFieldValue(PageConstants.LDAP_PORT));
			this.esoeBean.setLdapServerBaseDN( this.ldapForm.getFieldValue(PageConstants.LDAP_BASE_DN));
			this.esoeBean.setLdapIdentifier(this.ldapForm.getFieldValue(PageConstants.LDAP_ACCOUNT_IDENTIFIER));
			this.esoeBean.setLdapRecursive( this.ldapForm.getFieldValue(PageConstants.LDAP_RECURSIVE));
			this.esoeBean.setLdapDisableSSL( this.ldapForm.getFieldValue(PageConstants.LDAP_DISABLE_SSL));
			this.esoeBean.setLdapAdminUserDN(this.ldapForm.getFieldValue(PageConstants.LDAP_ADMIN_USER));
			this.esoeBean.setLdapAdminPassword(this.ldapForm.getFieldValue(PageConstants.LDAP_ADMIN_PASSWORD));
			
			this.storeSession(PageConstants.STAGE4_RES, new Boolean(true));
			
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
