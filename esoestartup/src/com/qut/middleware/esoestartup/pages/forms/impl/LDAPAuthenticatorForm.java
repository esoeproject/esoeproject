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
package com.qut.middleware.esoestartup.pages.forms.impl;

import net.sf.click.control.Form;
import net.sf.click.control.PasswordField;
import net.sf.click.control.Select;
import net.sf.click.control.TextField;

import com.qut.middleware.esoemanager.pages.forms.BaseForm;
import com.qut.middleware.esoestartup.pages.PageConstants;

public class LDAPAuthenticatorForm extends Form implements BaseForm
{
	private static final long serialVersionUID = -3433770425550184568L;
	
	public void init()
	{
		TextField ldapURL = new TextField(PageConstants.LDAP_URL);
		ldapURL.setValue("ldap://");
		ldapURL.setLabel("LDAP Server URL <a href=\"help.htm#ldap\" target=\"_blank\">?</a>");
		
		TextField ldapPort  = new TextField(PageConstants.LDAP_PORT);
		ldapPort.setValue("389");
		ldapPort.setLabel("LDAP Server Port <a href=\"help.htm#ldap\" target=\"_blank\">?</a>");
		
		TextField baseDN = new TextField(PageConstants.LDAP_BASE_DN);
		baseDN.setLabel("LDAP Base DN <a href=\"help.htm#ldap\" target=\"_blank\">?</a>");
		
		TextField accountIdentifier = new TextField(PageConstants.LDAP_ACCOUNT_IDENTIFIER);
		accountIdentifier.setValue("uid");
		accountIdentifier.setLabel("LDAP Account Identifier <a href=\"help.htm#ldap\" target=\"_blank\">?</a>");
		
		Select recursive = new Select(PageConstants.LDAP_RECURSIVE);
		recursive.setLabel("LDAP Recursive <a href=\"help.htm#ldap\" target=\"_blank\">?</a>");
		recursive.add(PageConstants.TRUE);
		recursive.add(PageConstants.FALSE);
		
		Select disableSSL = new Select(PageConstants.LDAP_DISABLE_SSL);
		disableSSL.setLabel("LDAP Disable SSL <a href=\"help.htm#ldap\" target=\"_blank\">?</a>");
		disableSSL.add(PageConstants.TRUE);
		disableSSL.add(PageConstants.FALSE);
		
		TextField adminUser = new TextField(PageConstants.LDAP_ADMIN_USER);
		adminUser.setLabel("Admin User DN <a href=\"help.htm#ldap\" target=\"_blank\">?</a>");
		
		PasswordField adminPassword = new PasswordField(PageConstants.LDAP_ADMIN_PASSWORD);
		adminPassword.setLabel("Admin Password <a href=\"help.htm#ldap\" target=\"_blank\">?</a>");
		
		this.add(ldapURL);
		this.add(ldapPort);
		this.add(baseDN);
		this.add(accountIdentifier);
		this.add(recursive);
		this.add(disableSSL);
		this.add(adminUser);
		this.add(adminPassword);
	}

}
