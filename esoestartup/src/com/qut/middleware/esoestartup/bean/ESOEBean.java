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
 * 
 * Purpose: Bean to store all details related to all components required to configure an ESOE install from scratch
 */
package com.qut.middleware.esoestartup.bean;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.util.Vector;

import com.qut.middleware.esoemanager.bean.ContactPersonBean;
import com.qut.middleware.esoemanager.bean.ServiceNodeBean;
import com.qut.middleware.esoemanager.bean.impl.ServiceBeanImpl;
import com.qut.middleware.esoestartup.Constants;

public class ESOEBean extends ServiceBeanImpl
{
	private String esoeDataDirectory;
	private String esoemanagerDataDirectory;
	private String spepDataDirectory;

	private Constants.DatabaseDrivers databaseDriver;
	private String databaseURL;
	private String databaseUsername;
	private String databasePassword;
	private String esoeNodeURL;
	private String esoeSingleSignOn;
	private String esoeAttributeService;
	private String esoeLxacmlService;
	private String esoeSPEPStartupService;
	private String esoeOrganizationName;
	private String esoeOrganizationDisplayName;
	private String esoeOrganizationURL;
	private String certIssuerDN;
	private String certIssuerEmailAddress;
	private String commonDomain;

	private String esoeEntityID;
	private Integer esoeEntID;

	private Integer esoeIdpDescID;
	private Integer esoeAADescID;
	private Integer esoeLxacmlDescID;

	private byte[] idpDescriptorXML;
	private byte[] aaDescriptorXML;
	private byte[] pdpDescriptorXML;

	private String ldapURL;
	private String ldapServerPort;
	private String ldapServerBaseDN;
	private String ldapIdentifier;
	private String ldapRecursive;
	private String ldapDisableSSL;
	private String ldapAdminUserDN;
	private String ldapAdminPassword;

	private byte[] esoeKeystore;
	private String esoeKeyStorePassphrase;
	private KeyPair esoeKeyPair;
	private String esoeKeyPairName;
	private String esoeKeyPairPassphrase;

	private byte[] esoeManagerKeystore;
	private String esoeManagerKeyStorePassphrase;
	private KeyPair esoeManagerKeyPair;
	private String esoeManagerKeyPairName;
	private String esoeManagerKeyPairPassphrase;

	private byte[] esoeMetadataKeystore;
	private String esoeMetadataKeyStorePassphrase;
	private String esoeMetadataKeyPairName;
	private String esoeMetadataKeyPairPassphrase;

	private String metadataIssuerDN;

	private String tomcatWebappPath;

	private Vector<ContactPersonBean> contacts;

	public String getDatabaseDriverString()
	{
		if (this.databaseDriver == Constants.DatabaseDrivers.mysql)
			return Constants.MYSQL_DRIVER;

		if (this.databaseDriver == Constants.DatabaseDrivers.oracle)
			return Constants.ORACLE_DRIVER;

		return Constants.MYSQL_DRIVER;
	}

	public String getLdapServer()
	{
		String[] ldapServer = ldapURL.split("://");

		if (ldapServer != null & ldapServer.length > 1)
			return ldapServer[1];

		return null;
	}

	public String getEsoeCookieDomain()
	{
		URL domain;
		try
		{
			domain = new URL(this.esoeNodeURL);
			return domain.getHost();
		}
		catch (MalformedURLException e)
		{
			return "URL Fault";
		}

	}

	public String getEsoeManagerCookieDomain()
	{
		URL domain;
		try
		{
			domain = new URL(this.getServiceURL());
			return domain.getHost();
		}
		catch (MalformedURLException e)
		{
			return "URL Fault";
		}

	}

	public String getEsoeManagerIP()
	{
		try
		{
			InetAddress addr = InetAddress.getByName(this.getEsoeManagerCookieDomain());
			return addr.getHostAddress();
		}
		catch (UnknownHostException e)
		{
			return "127.0.0.1";
		}
	}

	public String getEsoeManagerHost()
	{
		try
		{
			URL serviceURL = new URL(this.getServiceURL());
			if (serviceURL.getPort() == -1)
				return serviceURL.getProtocol() + "://" + serviceURL.getHost();
			else
				return serviceURL.getProtocol() + "://" + serviceURL.getHost() + ":" + serviceURL.getPort();
		}
		catch (MalformedURLException e)
		{
			return "INVALID ESOE MANAGER URL";
		}
	}

	public byte[] getAaDescriptorXML()
	{
		return aaDescriptorXML;
	}

	public void setAaDescriptorXML(byte[] aaDescriptorXML)
	{
		this.aaDescriptorXML = aaDescriptorXML;
	}

	public String getIdpEntityID()
	{
		return esoeEntityID;
	}

	public void setIdpEntityID(String idpEntityID)
	{
		this.esoeEntityID = idpEntityID;
	}

	public byte[] getIdpDescriptorXML()
	{
		return idpDescriptorXML;
	}

	public void setIdpDescriptorXML(byte[] idpDescriptorXML)
	{
		this.idpDescriptorXML = idpDescriptorXML;
	}

	public byte[] getPdpDescriptorXML()
	{
		return pdpDescriptorXML;
	}

	public void setPdpDescriptorXML(byte[] pdpDescriptorXML)
	{
		this.pdpDescriptorXML = pdpDescriptorXML;
	}

	public Vector<ContactPersonBean> getContacts()
	{
		return contacts;
	}

	public void setContacts(Vector<ContactPersonBean> contacts)
	{
		this.contacts = contacts;
	}

	public Constants.DatabaseDrivers getDatabaseDriver()
	{
		return databaseDriver;
	}

	public void setDatabaseDriver(Constants.DatabaseDrivers databaseDriver)
	{
		this.databaseDriver = databaseDriver;
	}

	public String getDatabasePassword()
	{
		return databasePassword;
	}

	public void setDatabasePassword(String databasePassword)
	{
		this.databasePassword = databasePassword;
	}

	public String getDatabaseURL()
	{
		return databaseURL;
	}

	public void setDatabaseURL(String databaseURL)
	{
		this.databaseURL = databaseURL;
	}

	public String getDatabaseUsername()
	{
		return databaseUsername;
	}

	public void setDatabaseUsername(String databaseUsername)
	{
		this.databaseUsername = databaseUsername;
	}

	public String getEsoeAttributeService()
	{
		return esoeAttributeService;
	}

	public void setEsoeAttributeService(String esoeAttributeService)
	{
		this.esoeAttributeService = esoeAttributeService;
	}

	public String getEsoeLxacmlService()
	{
		return esoeLxacmlService;
	}

	public void setEsoeLxacmlService(String esoeLxacmlService)
	{
		this.esoeLxacmlService = esoeLxacmlService;
	}

	public String getEsoeNodeURL()
	{
		return esoeNodeURL;
	}

	public void setEsoeNodeURL(String esoeNodeURL)
	{
		this.esoeNodeURL = esoeNodeURL;
	}

	public String getEsoeSingleSignOn()
	{
		return esoeSingleSignOn;
	}

	public void setEsoeSingleSignOn(String esoeSingleSignOn)
	{
		this.esoeSingleSignOn = esoeSingleSignOn;
	}

	public String getEsoeSPEPStartupService()
	{
		return esoeSPEPStartupService;
	}

	public void setEsoeSPEPStartupService(String esoeSPEPStartupService)
	{
		this.esoeSPEPStartupService = esoeSPEPStartupService;
	}

	public String getCertIssuerDN()
	{
		return certIssuerDN;
	}

	public void setCertIssuerDN(String certIssuerDN)
	{
		this.certIssuerDN = certIssuerDN;
	}

	public String getCertIssuerEmailAddress()
	{
		return certIssuerEmailAddress;
	}

	public void setCertIssuerEmailAddress(String certIssuerEmailAddress)
	{
		this.certIssuerEmailAddress = certIssuerEmailAddress;
	}

	public String getEsoeOrganizationDisplayName()
	{
		return esoeOrganizationDisplayName;
	}

	public void setEsoeOrganizationDisplayName(String esoeOrganizationDisplayName)
	{
		this.esoeOrganizationDisplayName = esoeOrganizationDisplayName;
	}

	public String getEsoeOrganizationName()
	{
		return esoeOrganizationName;
	}

	public void setEsoeOrganizationName(String esoeOrganizationName)
	{
		this.esoeOrganizationName = esoeOrganizationName;
	}

	public String getEsoeOrganizationURL()
	{
		return esoeOrganizationURL;
	}

	public void setEsoeOrganizationURL(String esoeOrganizationURL)
	{
		this.esoeOrganizationURL = esoeOrganizationURL;
	}

	public String getEsoeKeyPairName()
	{
		return esoeKeyPairName;
	}

	public void setEsoeKeyPairName(String esoeKeyPairName)
	{
		this.esoeKeyPairName = esoeKeyPairName;
	}

	public String getEsoeKeyPairPassphrase()
	{
		return esoeKeyPairPassphrase;
	}

	public void setEsoeKeyPairPassphrase(String esoeKeyPairPassphrase)
	{
		this.esoeKeyPairPassphrase = esoeKeyPairPassphrase;
	}

	public String getEsoeKeyStorePassphrase()
	{
		return esoeKeyStorePassphrase;
	}

	public void setEsoeKeyStorePassphrase(String esoeKeyStorePassphrase)
	{
		this.esoeKeyStorePassphrase = esoeKeyStorePassphrase;
	}

	public String getEsoeManagerKeyPairName()
	{
		return esoeManagerKeyPairName;
	}

	public void setEsoeManagerKeyPairName(String esoeManagerKeyPairName)
	{
		this.esoeManagerKeyPairName = esoeManagerKeyPairName;
	}

	public String getEsoeManagerKeyPairPassphrase()
	{
		return esoeManagerKeyPairPassphrase;
	}

	public void setEsoeManagerKeyPairPassphrase(String esoeManagerKeyPairPassphrase)
	{
		this.esoeManagerKeyPairPassphrase = esoeManagerKeyPairPassphrase;
	}

	public String getEsoeManagerKeyStorePassphrase()
	{
		return esoeManagerKeyStorePassphrase;
	}

	public void setEsoeManagerKeyStorePassphrase(String esoeManagerKeyStorePassphrase)
	{
		this.esoeManagerKeyStorePassphrase = esoeManagerKeyStorePassphrase;
	}

	public String getEsoeMetadataKeyPairName()
	{
		return esoeMetadataKeyPairName;
	}

	public void setEsoeMetadataKeyPairName(String esoeMetadataKeyPairName)
	{
		this.esoeMetadataKeyPairName = esoeMetadataKeyPairName;
	}

	public String getEsoeMetadataKeyPairPassphrase()
	{
		return esoeMetadataKeyPairPassphrase;
	}

	public void setEsoeMetadataKeyPairPassphrase(String esoeMetadataKeyPairPassphrase)
	{
		this.esoeMetadataKeyPairPassphrase = esoeMetadataKeyPairPassphrase;
	}

	public String getEsoeMetadataKeyStorePassphrase()
	{
		return esoeMetadataKeyStorePassphrase;
	}

	public void setEsoeMetadataKeyStorePassphrase(String esoeMetadataKeyStorePassphrase)
	{
		this.esoeMetadataKeyStorePassphrase = esoeMetadataKeyStorePassphrase;
	}

	public void setEsoeKeystore(byte[] esoeKeystore)
	{
		this.esoeKeystore = esoeKeystore;
	}

	public void setEsoeManagerKeystore(byte[] esoeManagerKeystore)
	{
		this.esoeManagerKeystore = esoeManagerKeystore;
	}

	public void setEsoeMetadataKeystore(byte[] esoeMetadataKeystore)
	{
		this.esoeMetadataKeystore = esoeMetadataKeystore;
	}

	public byte[] getEsoeKeystore()
	{
		return esoeKeystore;
	}

	public byte[] getEsoeManagerKeystore()
	{
		return esoeManagerKeystore;
	}

	public byte[] getEsoeMetadataKeystore()
	{
		return esoeMetadataKeystore;
	}

	public String getMetadataIssuerDN()
	{
		return metadataIssuerDN;
	}

	public void setMetadataIssuerDN(String metadataIssuerDN)
	{
		this.metadataIssuerDN = metadataIssuerDN;
	}

	public String getLdapAdminPassword()
	{
		return ldapAdminPassword;
	}

	public void setLdapAdminPassword(String ldapAdminPassword)
	{
		this.ldapAdminPassword = ldapAdminPassword;
	}

	public String getLdapAdminUserDN()
	{
		return ldapAdminUserDN;
	}

	public void setLdapAdminUserDN(String ldapAdminUserDN)
	{
		this.ldapAdminUserDN = ldapAdminUserDN;
	}

	public String getLdapDisableSSL()
	{
		return ldapDisableSSL;
	}

	public void setLdapDisableSSL(String ldapDisableSSL)
	{
		this.ldapDisableSSL = ldapDisableSSL;
	}

	public String getLdapIdentifier()
	{
		return ldapIdentifier;
	}

	public void setLdapIdentifier(String ldapIdentifier)
	{
		this.ldapIdentifier = ldapIdentifier;
	}

	public String getLdapRecursive()
	{
		return ldapRecursive;
	}

	public void setLdapRecursive(String ldapRecursive)
	{
		this.ldapRecursive = ldapRecursive;
	}

	public String getLdapServerBaseDN()
	{
		return ldapServerBaseDN;
	}

	public void setLdapServerBaseDN(String ldapServerBaseDN)
	{
		this.ldapServerBaseDN = ldapServerBaseDN;
	}

	public String getLdapServerPort()
	{
		return ldapServerPort;
	}

	public void setLdapServerPort(String ldapServerPort)
	{
		this.ldapServerPort = ldapServerPort;
	}

	public String getLdapURL()
	{
		return ldapURL;
	}

	public void setLdapURL(String ldapURL)
	{
		this.ldapURL = ldapURL;
	}

	public String getTomcatWebappPath()
	{
		return tomcatWebappPath;
	}

	public void setTomcatWebappPath(String tomcatWebappPath)
	{
		this.tomcatWebappPath = tomcatWebappPath;
	}

	public KeyPair getEsoeKeyPair()
	{
		return esoeKeyPair;
	}

	public void setEsoeKeyPair(KeyPair esoeKeyPair)
	{
		this.esoeKeyPair = esoeKeyPair;
	}

	public KeyPair getEsoeManagerKeyPair()
	{
		return esoeManagerKeyPair;
	}

	public void setEsoeManagerKeyPair(KeyPair esoeManagerKeyPair)
	{
		this.esoeManagerKeyPair = esoeManagerKeyPair;
	}

	public String getEsoeDataDirectory()
	{
		return esoeDataDirectory;
	}

	public void setEsoeDataDirectory(String esoeData)
	{
		this.esoeDataDirectory = esoeData;
	}

	public String getEsoemanagerDataDirectory()
	{
		return esoemanagerDataDirectory;
	}

	public void setEsoemanagerDataDirectory(String esoemanagerData)
	{
		this.esoemanagerDataDirectory = esoemanagerData;
	}

	public String getSpepDataDirectory()
	{
		return spepDataDirectory;
	}

	public void setSpepDataDirectory(String spepData)
	{
		this.spepDataDirectory = spepData;
	}

	public String getCommonDomain()
	{
		return commonDomain;
	}

	public void setCommonDomain(String commonDomain)
	{
		this.commonDomain = commonDomain;
	}

	public String getEsoeEntityID()
	{
		return esoeEntityID;
	}

	public void setEsoeEntityID(String esoeEntityID)
	{
		this.esoeEntityID = esoeEntityID;
	}

	public Integer getEsoeEntID()
	{
		return esoeEntID;
	}

	public void setEsoeEntID(Integer esoeEntID)
	{
		this.esoeEntID = esoeEntID;
	}

	public Integer getEsoeIdpDescID()
	{
		return esoeIdpDescID;
	}

	public void setEsoeIdpDescID(Integer esoeDescID)
	{
		this.esoeIdpDescID = esoeDescID;
	}

	public Integer getEsoeAADescID()
	{
		return esoeAADescID;
	}

	public void setEsoeAADescID(Integer esoeAADescID)
	{
		this.esoeAADescID = esoeAADescID;
	}

	public Integer getEsoeLxacmlDescID()
	{
		return esoeLxacmlDescID;
	}

	public void setEsoeLxacmlDescID(Integer esoeLxacmlDescID)
	{
		this.esoeLxacmlDescID = esoeLxacmlDescID;
	}
}
