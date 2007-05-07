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

	public Constants.DatabaseDrivers databaseDriver;
	public String databaseURL;
	public String databaseUsername;
	public String databasePassword;
	public String esoeNodeURL;
	public String esoeSingleSignOn;
	public String esoeAttributeService;
	public String esoeLxacmlService;
	public String esoeSPEPStartupService;
	public String esoeOrganizationName;
	public String esoeOrganizationDisplayName;
	public String esoeOrganizationURL;
	public String certIssuerDN;
	public String certIssuerEmailAddress;
	public String managerServiceName;
	public String managerServiceURL;
	public String managerServiceDescription;
	public String managerServiceAuthzFail;
	public String writeableDirectory;
	public String tomcatWebappsDirectory;
	public String ldapURL;
	public String ldapServerPort;
	public String ldapServerBaseDN;
	public String ldapIdentifier;
	public String ldapRecursive;
	public String ldapDisableSSL;
	public String ldapAdminUserDN;
	public String ldapAdminPassword;
	
	public Vector<ContactPersonBean> contacts;
	public Vector<ServiceNodeBean> managerServiceNodes;
	
	public boolean error;
	public String errorMessage;
	
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
		error = false;
		this.finalizeForm.init();
		
		Submit nextButton = new Submit(PageConstants.NAV_COMPLETE_LABEL, this, PageConstants.NAV_COMPLETE_FUNC);
		Submit backButton = new Submit(PageConstants.NAV_PREV_LABEL, this, PageConstants.NAV_PREV_FUNC);

		this.finalizeForm.add(backButton);
		this.finalizeForm.add(nextButton);
		this.finalizeForm.setButtonAlign(Form.ALIGN_RIGHT);

		/* Grab all session data to display to user */
		this.databaseDriver = (Constants.DatabaseDrivers) this.retrieveSession(PageConstants.STORED_DATA_REPOSITORY_DRIVER);
		this.databaseURL = (String) this.retrieveSession(PageConstants.STORED_DATA_REPOSITORY_URL);
		this.databaseUsername = (String) this.retrieveSession(PageConstants.STORED_DATA_REPOSITORY_USERNAME);
		this.databasePassword = (String) this.retrieveSession(PageConstants.STORED_DATA_REPOSITORY_PASSWORD);
		this.esoeNodeURL = (String) this.retrieveSession(PageConstants.STORED_ESOE_NODE_URL);
		this.esoeSingleSignOn = (String) this.retrieveSession(PageConstants.STORED_ESOE_SINGLE_SIGN_ON_SERVICE);
		this.esoeAttributeService = (String) this.retrieveSession(PageConstants.STORED_ESOE_ATTRIBUTE_SERVICE);
		this.esoeLxacmlService = (String) this.retrieveSession(PageConstants.STORED_ESOE_LXACML_SERVICE);
		this.esoeSPEPStartupService = (String) this.retrieveSession(PageConstants.STORED_ESOE_SPEP_STARTUP_SERVICE);
		this.esoeOrganizationName = (String) this.retrieveSession(PageConstants.STORED_ESOE_ORGANIZATION_NAME);
		this.esoeOrganizationDisplayName = (String) this.retrieveSession(PageConstants.STORED_ESOE_ORGANIZATION_DISPLAY_NAME);
		this.esoeOrganizationURL = (String) this.retrieveSession(PageConstants.STORED_ESOE_ORGANIZATION_URL);
		
		this.certIssuerDN = (String) this.retrieveSession(PageConstants.STORED_CRYPTO_ISSUER_DN);
		this.certIssuerEmailAddress = (String) this.retrieveSession(PageConstants.STORED_CRYPTO_ISSUER_EMAIL);
		this.contacts = (Vector<ContactPersonBean>) this.retrieveSession(PageConstants.STORED_CONTACTS);
		
		this.managerServiceName = (String) this.retrieveSession(PageConstants.STORED_SERVICE_NAME);
		this.managerServiceDescription = (String) this.retrieveSession(PageConstants.STORED_SERVICE_DESCRIPTION);
		this.managerServiceURL = (String) this.retrieveSession(PageConstants.STORED_SERVICE_URL);
		this.managerServiceAuthzFail = (String) this.retrieveSession(PageConstants.STORED_SERVICE_AUTHZ_FAILURE_MESSAGE);
		this.managerServiceNodes = (Vector<ServiceNodeBean>) this.retrieveSession(PageConstants.STORED_SERVICE_NODES);
		
		this.ldapURL = (String)this.retrieveSession(PageConstants.STORED_LDAP_URL);
		
		this.ldapServerPort = (String)this.retrieveSession(PageConstants.STORED_LDAP_PORT);
		this.ldapServerBaseDN = (String)this.retrieveSession(PageConstants.STORED_LDAP_BASE_DN);
		this.ldapIdentifier = (String)this.retrieveSession(PageConstants.STORED_LDAP_ACCOUNT_IDENTIFIER);
		this.ldapRecursive = (String)this.retrieveSession(PageConstants.STORED_LDAP_RECURSIVE);
		this.ldapDisableSSL = (String)this.retrieveSession(PageConstants.STORED_LDAP_RECURSIVE);
		this.ldapAdminUserDN = (String)this.retrieveSession(PageConstants.STORED_LDAP_ADMIN_USER);
		this.ldapAdminPassword = (String)this.retrieveSession(PageConstants.STORED_LDAP_ADMIN_PASSWORD);
	}
	
	@Override
	public void onGet()
	{
		/* Check if previous registration stage completed */
		Boolean status = (Boolean)this.retrieveSession(PageConstants.STAGE7_RES);
		if(status == null || status.booleanValue() != true)
		{
			previousClick();
		}
	}
	
	public boolean completeClick()
	{
		if(this.finalizeForm.isValid())
		{
			boolean validDir = false;
			File writeDir = new File(this.finalizeForm.getFieldValue(PageConstants.WRITEABLE_DIRECTORY));
			File tomcatDir = new File(this.finalizeForm.getFieldValue(PageConstants.TOMCAT_WEBAPPS_DIRECTORY));
			
			if(writeDir.isDirectory() && writeDir.canWrite())
			{
				this.writeableDirectory = writeDir.getPath();
			}
			else
			{
				this.finalizeForm.setError("Directory for writing content must already exist and be writable by the tomcat user, please correct");
				return true;
			}
			if(tomcatDir.isDirectory() && tomcatDir.canWrite())
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
			
			ESOEBean bean = new ESOEBean();
			bean.setDatabaseDriver(this.databaseDriver);
			bean.setDatabaseUsername(this.databaseUsername);
			bean.setDatabasePassword(this.databasePassword);
			bean.setDatabaseURL(this.databaseURL);
			
			bean.setEsoeNodeURL(this.esoeNodeURL);
			bean.setEsoeSingleSignOn(this.esoeSingleSignOn);
			bean.setEsoeAttributeService(this.esoeAttributeService);
			bean.setEsoeLxacmlService(this.esoeLxacmlService);
			bean.setEsoeSPEPStartupService(this.esoeSPEPStartupService);
			bean.setEsoeOrganizationName(this.esoeOrganizationName);
			bean.setEsoeOrganizationDisplayName(this.esoeOrganizationDisplayName);
			bean.setEsoeOrganizationURL(this.esoeOrganizationURL);
			
			bean.setManagerServiceName(this.managerServiceName);
			bean.setManagerServiceDescription(this.managerServiceDescription);
			bean.setManagerServiceURL(this.managerServiceURL);
			bean.setManagerServiceAuthzFail(this.managerServiceAuthzFail);
			bean.setManagerServiceNodes(this.managerServiceNodes);
			
			bean.setContacts(this.contacts);
			
			bean.setCertIssuerDN(this.certIssuerDN);
			bean.setCertIssuerEmailAddress(this.certIssuerEmailAddress);
			
			bean.setLdapURL(this.ldapURL);
			bean.setLdapServerPort(this.ldapServerPort);
			bean.setLdapServerBaseDN(this.ldapServerBaseDN);
			bean.setLdapIdentifier(this.ldapIdentifier);
			bean.setLdapRecursive(this.ldapRecursive);
			bean.setLdapDisableSSL(this.ldapDisableSSL);
			bean.setLdapAdminUserDN(this.ldapAdminUserDN);
			bean.setLdapAdminPassword(this.ldapAdminPassword);
			
			bean.setWriteableDirectory(this.writeableDirectory);
			bean.setTomcatWebappPath(this.tomcatWebappsDirectory);
			
			try
			{
				this.logic.execute(bean);
				
				/* Everything has been setup correctly, move to completed page */
				this.storeSession(PageConstants.STORED_ESOE_COMPLETED_DETAILS, bean);
				this.storeSession(PageConstants.STAGE8_RES, new Boolean(true));
				
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
