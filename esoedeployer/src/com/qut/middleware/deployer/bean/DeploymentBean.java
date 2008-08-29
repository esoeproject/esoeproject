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
 */
package com.qut.middleware.deployer.bean;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyPair;

import com.qut.middleware.deployer.Constants;

public class DeploymentBean extends DeploymentBeanConsts
{		
	// Data Repository
	private Constants.DatabaseDrivers databaseDriver;
	private String databaseURL;
	private String databaseUsername;
	private String databasePassword;
	
	// ESOE Core
	private Integer esoeEntID;
	private String esoeEntityID;
	private String esoeNode;

	private String esoeOrganizationName;
	private String esoeOrganizationDisplayName;
	private String esoeOrganizationURL;
	
	private Integer esoeIdpDescID;
	private Integer esoeAADescID;
	private Integer esoeLxacmlDescID;

	private byte[] idpDescriptorXML;
	private byte[] aaDescriptorXML;
	private byte[] pdpDescriptorXML;
	private byte[] spDescriptorXML;

	private byte[] esoeKeystore;
	private String esoeKeyStorePassphrase;
	private KeyPair esoeKeyPair;
	private String esoeKeyPairName;
	private String esoeKeyPairPassphrase;
	
	private ContactPersonBean contact;

	// ESOE Manager
	private Integer managerEntID;
	private Integer managerDescID;
	
	private String managerEntityID;
	private String managerServiceNode;
		
	private byte[] esoeManagerKeystore;
	private String esoeManagerKeyStorePassphrase;
	private KeyPair esoeManagerKeyPair;
	private String esoeManagerKeyPairName;
	private String esoeManagerKeyPairPassphrase;
	
	// Crypto
	private String certIssuerDN;
	private String certIssuerEmailAddress;
	private String commonDomain;
	
	// Metadata
	private byte[] esoeMetadataKeystore;
	private String esoeMetadataKeyStorePassphrase;
	private String esoeMetadataKeyPairName;
	private String esoeMetadataKeyPairPassphrase;
	

	public String getDatabaseDriverString()
	{
		if (this.databaseDriver == Constants.DatabaseDrivers.mysql)
			return Constants.MYSQL_DRIVER;

		if (this.databaseDriver == Constants.DatabaseDrivers.oracle)
			return Constants.ORACLE_DRIVER;

		return Constants.MYSQL_DRIVER;
	}
	
	public String getDatabaseTypeString()
	{
		if (this.databaseDriver == Constants.DatabaseDrivers.mysql)
			return Constants.MYSQL;

		if (this.databaseDriver == Constants.DatabaseDrivers.oracle)
			return Constants.ORACLE;

		return Constants.MYSQL;
	}

	public String getEsoeCookieDomain()
	{
		URL domain;
		try
		{
			domain = new URL(this.esoeNode);
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
			InetAddress addr = InetAddress.getByName(this.getEsoeNode());
			return addr.getHostAddress();
		}
		catch (UnknownHostException e)
		{
			return "127.0.0.1";
		}
	}
	
	public ContactPersonBean getContact() {
		return contact;
	}

	public void setContact(ContactPersonBean contact) {
		this.contact = contact;
	}

	public Constants.DatabaseDrivers getDatabaseDriver() {
		return databaseDriver;
	}

	public void setDatabaseDriver(Constants.DatabaseDrivers databaseDriver) {
		this.databaseDriver = databaseDriver;
	}

	public String getDatabaseURL() {
		return databaseURL;
	}

	public void setDatabaseURL(String databaseURL) {
		this.databaseURL = databaseURL;
	}

	public String getDatabaseUsername() {
		return databaseUsername;
	}

	public void setDatabaseUsername(String databaseUsername) {
		this.databaseUsername = databaseUsername;
	}

	public String getDatabasePassword() {
		return databasePassword;
	}

	public void setDatabasePassword(String databasePassword) {
		this.databasePassword = databasePassword;
	}
	
	public Integer getEsoeEntID() {
		return esoeEntID;
	}

	public void setEsoeEntID(Integer esoeEntID) {
		this.esoeEntID = esoeEntID;
	}

	public String getEsoeEntityID() {
		return esoeEntityID;
	}

	public void setEsoeEntityID(String esoeEntityID) {
		this.esoeEntityID = esoeEntityID;
	}

	public String getEsoeNode() {
		return esoeNode;
	}

	public void setEsoeNode(String esoeNode) {
		this.esoeNode = esoeNode;
	}

	public String getEsoeOrganizationName() {
		return esoeOrganizationName;
	}

	public void setEsoeOrganizationName(String esoeOrganizationName) {
		this.esoeOrganizationName = esoeOrganizationName;
	}

	public String getEsoeOrganizationDisplayName() {
		return esoeOrganizationDisplayName;
	}

	public void setEsoeOrganizationDisplayName(String esoeOrganizationDisplayName) {
		this.esoeOrganizationDisplayName = esoeOrganizationDisplayName;
	}

	public String getEsoeOrganizationURL() {
		return esoeOrganizationURL;
	}

	public void setEsoeOrganizationURL(String esoeOrganizationURL) {
		this.esoeOrganizationURL = esoeOrganizationURL;
	}

	public Integer getEsoeIdpDescID() {
		return esoeIdpDescID;
	}

	public void setEsoeIdpDescID(Integer esoeIdpDescID) {
		this.esoeIdpDescID = esoeIdpDescID;
	}

	public Integer getEsoeAADescID() {
		return esoeAADescID;
	}

	public void setEsoeAADescID(Integer esoeAADescID) {
		this.esoeAADescID = esoeAADescID;
	}

	public Integer getEsoeLxacmlDescID() {
		return esoeLxacmlDescID;
	}

	public void setEsoeLxacmlDescID(Integer esoeLxacmlDescID) {
		this.esoeLxacmlDescID = esoeLxacmlDescID;
	}

	public byte[] getIdpDescriptorXML() {
		return idpDescriptorXML;
	}

	public void setIdpDescriptorXML(byte[] idpDescriptorXML) {
		this.idpDescriptorXML = idpDescriptorXML;
	}

	public byte[] getAaDescriptorXML() {
		return aaDescriptorXML;
	}

	public void setAaDescriptorXML(byte[] aaDescriptorXML) {
		this.aaDescriptorXML = aaDescriptorXML;
	}

	public byte[] getPdpDescriptorXML() {
		return pdpDescriptorXML;
	}

	public void setPdpDescriptorXML(byte[] pdpDescriptorXML) {
		this.pdpDescriptorXML = pdpDescriptorXML;
	}

	public byte[] getSpDescriptorXML() {
		return spDescriptorXML;
	}

	public void setSpDescriptorXML(byte[] spDescriptorXML) {
		this.spDescriptorXML = spDescriptorXML;
	}

	public byte[] getEsoeKeystore() {
		return esoeKeystore;
	}

	public void setEsoeKeystore(byte[] esoeKeystore) {
		this.esoeKeystore = esoeKeystore;
	}

	public String getEsoeKeyStorePassphrase() {
		return esoeKeyStorePassphrase;
	}

	public void setEsoeKeyStorePassphrase(String esoeKeyStorePassphrase) {
		this.esoeKeyStorePassphrase = esoeKeyStorePassphrase;
	}

	public KeyPair getEsoeKeyPair() {
		return esoeKeyPair;
	}

	public void setEsoeKeyPair(KeyPair esoeKeyPair) {
		this.esoeKeyPair = esoeKeyPair;
	}

	public String getEsoeKeyPairName() {
		return esoeKeyPairName;
	}

	public void setEsoeKeyPairName(String esoeKeyPairName) {
		this.esoeKeyPairName = esoeKeyPairName;
	}

	public String getEsoeKeyPairPassphrase() {
		return esoeKeyPairPassphrase;
	}

	public void setEsoeKeyPairPassphrase(String esoeKeyPairPassphrase) {
		this.esoeKeyPairPassphrase = esoeKeyPairPassphrase;
	}
	
	public Integer getManagerEntID() {
		return managerEntID;
	}

	public void setManagerEntID(Integer managerEntID) {
		this.managerEntID = managerEntID;
	}

	public Integer getManagerDescID() {
		return managerDescID;
	}

	public void setManagerDescID(Integer managerDescID) {
		this.managerDescID = managerDescID;
	}

	public String getManagerEntityID() {
		return managerEntityID;
	}

	public void setManagerEntityID(String managerEntityID) {
		this.managerEntityID = managerEntityID;
	}
	
	public String getManagerServiceNode() {
		return managerServiceNode;
	}

	public void setManagerServiceNode(String managerServiceNode) {
		this.managerServiceNode = managerServiceNode;
	}

	public byte[] getEsoeManagerKeystore() {
		return esoeManagerKeystore;
	}

	public void setEsoeManagerKeystore(byte[] esoeManagerKeystore) {
		this.esoeManagerKeystore = esoeManagerKeystore;
	}

	public String getEsoeManagerKeyStorePassphrase() {
		return esoeManagerKeyStorePassphrase;
	}

	public void setEsoeManagerKeyStorePassphrase(
			String esoeManagerKeyStorePassphrase) {
		this.esoeManagerKeyStorePassphrase = esoeManagerKeyStorePassphrase;
	}

	public KeyPair getEsoeManagerKeyPair() {
		return esoeManagerKeyPair;
	}

	public void setEsoeManagerKeyPair(KeyPair esoeManagerKeyPair) {
		this.esoeManagerKeyPair = esoeManagerKeyPair;
	}

	public String getEsoeManagerKeyPairName() {
		return esoeManagerKeyPairName;
	}

	public void setEsoeManagerKeyPairName(String esoeManagerKeyPairName) {
		this.esoeManagerKeyPairName = esoeManagerKeyPairName;
	}

	public String getEsoeManagerKeyPairPassphrase() {
		return esoeManagerKeyPairPassphrase;
	}

	public void setEsoeManagerKeyPairPassphrase(String esoeManagerKeyPairPassphrase) {
		this.esoeManagerKeyPairPassphrase = esoeManagerKeyPairPassphrase;
	}
	
	public String getCertIssuerDN() {
		return certIssuerDN;
	}

	public void setCertIssuerDN(String certIssuerDN) {
		this.certIssuerDN = certIssuerDN;
	}

	public String getCertIssuerEmailAddress() {
		return certIssuerEmailAddress;
	}

	public void setCertIssuerEmailAddress(String certIssuerEmailAddress) {
		this.certIssuerEmailAddress = certIssuerEmailAddress;
	}

	public String getCommonDomain() {
		return commonDomain;
	}

	public void setCommonDomain(String commonDomain) {
		this.commonDomain = commonDomain;
	}

	public byte[] getEsoeMetadataKeystore() {
		return esoeMetadataKeystore;
	}

	public void setEsoeMetadataKeystore(byte[] esoeMetadataKeystore) {
		this.esoeMetadataKeystore = esoeMetadataKeystore;
	}

	public String getEsoeMetadataKeyStorePassphrase() {
		return esoeMetadataKeyStorePassphrase;
	}

	public void setEsoeMetadataKeyStorePassphrase(
			String esoeMetadataKeyStorePassphrase) {
		this.esoeMetadataKeyStorePassphrase = esoeMetadataKeyStorePassphrase;
	}

	public String getEsoeMetadataKeyPairName() {
		return esoeMetadataKeyPairName;
	}

	public void setEsoeMetadataKeyPairName(String esoeMetadataKeyPairName) {
		this.esoeMetadataKeyPairName = esoeMetadataKeyPairName;
	}

	public String getEsoeMetadataKeyPairPassphrase() {
		return esoeMetadataKeyPairPassphrase;
	}

	public void setEsoeMetadataKeyPairPassphrase(
			String esoeMetadataKeyPairPassphrase) {
		this.esoeMetadataKeyPairPassphrase = esoeMetadataKeyPairPassphrase;
	}
}
