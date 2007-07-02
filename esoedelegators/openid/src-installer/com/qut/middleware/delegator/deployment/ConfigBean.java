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
 * Creation Date: 19/06/2007
 * 
 * Purpose: Generic bean to hold delegator config for openID
 */
package com.qut.middleware.delegator.deployment;

import java.security.KeyStore;

public class ConfigBean
{
	private String extractedFiles, esoeKeystore, esoeConfig, openIDEndpoint, outputDirectory, esoeKeyStorePassphrase,
			esoeKeyName, esoeKeyPassphrase, esoeURL, esoeSSOURL, esoeSessionDomain, issuerID, oidKeyStorePassphrase, oidKeyPairName, oidKeyPairPassphrase, httpsOffload;
	
	private int certExpiryInterval = 0;
	
	private KeyStore keyStore;
	
	public int getCertExpiryInterval()
	{
		return certExpiryInterval;
	}
	public void setCertExpiryInterval(int certExpiryInterval)
	{
		this.certExpiryInterval = certExpiryInterval;
	}
	public String getEsoeConfig()
	{
		return esoeConfig;
	}
	public void setEsoeConfig(String esoeConfig)
	{
		this.esoeConfig = esoeConfig;
	}
	public String getEsoeKeyName()
	{
		return esoeKeyName;
	}
	public void setEsoeKeyName(String esoeKeyName)
	{
		this.esoeKeyName = esoeKeyName;
	}
	public String getEsoeKeyPassphrase()
	{
		return esoeKeyPassphrase;
	}
	public void setEsoeKeyPassphrase(String esoeKeyPassphrase)
	{
		this.esoeKeyPassphrase = esoeKeyPassphrase;
	}
	public String getEsoeKeystore()
	{
		return esoeKeystore;
	}
	public void setEsoeKeystore(String esoeKeystore)
	{
		this.esoeKeystore = esoeKeystore;
	}
	public String getEsoeKeyStorePassphrase()
	{
		return esoeKeyStorePassphrase;
	}
	public void setEsoeKeyStorePassphrase(String esoeKeyStorePassphrase)
	{
		this.esoeKeyStorePassphrase = esoeKeyStorePassphrase;
	}
	public String getEsoeURL()
	{
		return esoeURL;
	}
	public void setEsoeURL(String esoeURL)
	{
		this.esoeURL = esoeURL;
	}
	public String getExtractedFiles()
	{
		return extractedFiles;
	}
	public void setExtractedFiles(String extractedFiles)
	{
		this.extractedFiles = extractedFiles;
	}
	public String getOpenIDEndpoint()
	{
		return openIDEndpoint;
	}
	public void setOpenIDEndpoint(String openIDEndpoint)
	{
		this.openIDEndpoint = openIDEndpoint;
	}
	public String getOutputDirectory()
	{
		return outputDirectory;
	}
	public void setOutputDirectory(String outputDirectory)
	{
		this.outputDirectory = outputDirectory;
	}
	public KeyStore getKeyStore()
	{
		return keyStore;
	}
	public void setKeyStore(KeyStore keyStore)
	{
		this.keyStore = keyStore;
	}
	public String getOidKeyPairName()
	{
		return oidKeyPairName;
	}
	public void setOidKeyPairName(String oidKeyPairName)
	{
		this.oidKeyPairName = oidKeyPairName;
	}
	public String getOidKeyPairPassphrase()
	{
		return oidKeyPairPassphrase;
	}
	public void setOidKeyPairPassphrase(String oidKeyPairPassphrase)
	{
		this.oidKeyPairPassphrase = oidKeyPairPassphrase;
	}
	public String getOidKeyStorePassphrase()
	{
		return oidKeyStorePassphrase;
	}
	public void setOidKeyStorePassphrase(String oidKeyStorePassphrase)
	{
		this.oidKeyStorePassphrase = oidKeyStorePassphrase;
	}
	public String getEsoeSSOURL()
	{
		return esoeSSOURL;
	}
	public void setEsoeSSOURL(String esoeSSOURL)
	{
		this.esoeSSOURL = esoeSSOURL;
	}
	public String getEsoeSessionDomain()
	{
		return esoeSessionDomain;
	}
	public void setEsoeSessionDomain(String esoeSessionDomain)
	{
		this.esoeSessionDomain = esoeSessionDomain;
	}
	public String getIssuerID()
	{
		return issuerID;
	}
	public void setIssuerID(String issuerID)
	{
		this.issuerID = issuerID;
	}
	public String getHttpsOffload()
	{
		return httpsOffload;
	}
	public void setHttpsOffload(String httpsOffload)
	{
		this.httpsOffload = httpsOffload;
	}
}
