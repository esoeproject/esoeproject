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
	private String extractedFiles, esoeKeystore, esoeConfig, shibEndpoint, outputDirectory, esoeKeyStorePassphrase,
			esoeKeyName, esoeKeyPassphrase, esoeURL, esoeSSOURL, esoeSessionDomain, issuerID, shibKeyStorePassphrase, shibKeyPairName, shibKeyPairPassphrase;
	
	private int certExpiryInterval = 0;
	
	private KeyStore keyStore;

	/**
	 * @return the certExpiryInterval
	 */
	public int getCertExpiryInterval()
	{
		return certExpiryInterval;
	}

	/**
	 * @param certExpiryInterval the certExpiryInterval to set
	 */
	public void setCertExpiryInterval(int certExpiryInterval)
	{
		this.certExpiryInterval = certExpiryInterval;
	}

	/**
	 * @return the esoeConfig
	 */
	public String getEsoeConfig()
	{
		return esoeConfig;
	}

	/**
	 * @param esoeConfig the esoeConfig to set
	 */
	public void setEsoeConfig(String esoeConfig)
	{
		this.esoeConfig = esoeConfig;
	}

	/**
	 * @return the esoeKeyName
	 */
	public String getEsoeKeyName()
	{
		return esoeKeyName;
	}

	/**
	 * @param esoeKeyName the esoeKeyName to set
	 */
	public void setEsoeKeyName(String esoeKeyName)
	{
		this.esoeKeyName = esoeKeyName;
	}

	/**
	 * @return the esoeKeyPassphrase
	 */
	public String getEsoeKeyPassphrase()
	{
		return esoeKeyPassphrase;
	}

	/**
	 * @param esoeKeyPassphrase the esoeKeyPassphrase to set
	 */
	public void setEsoeKeyPassphrase(String esoeKeyPassphrase)
	{
		this.esoeKeyPassphrase = esoeKeyPassphrase;
	}

	/**
	 * @return the esoeKeystore
	 */
	public String getEsoeKeystore()
	{
		return esoeKeystore;
	}

	/**
	 * @param esoeKeystore the esoeKeystore to set
	 */
	public void setEsoeKeystore(String esoeKeystore)
	{
		this.esoeKeystore = esoeKeystore;
	}

	/**
	 * @return the esoeKeyStorePassphrase
	 */
	public String getEsoeKeyStorePassphrase()
	{
		return esoeKeyStorePassphrase;
	}

	/**
	 * @param esoeKeyStorePassphrase the esoeKeyStorePassphrase to set
	 */
	public void setEsoeKeyStorePassphrase(String esoeKeyStorePassphrase)
	{
		this.esoeKeyStorePassphrase = esoeKeyStorePassphrase;
	}

	/**
	 * @return the esoeSessionDomain
	 */
	public String getEsoeSessionDomain()
	{
		return esoeSessionDomain;
	}

	/**
	 * @param esoeSessionDomain the esoeSessionDomain to set
	 */
	public void setEsoeSessionDomain(String esoeSessionDomain)
	{
		this.esoeSessionDomain = esoeSessionDomain;
	}

	/**
	 * @return the esoeSSOURL
	 */
	public String getEsoeSSOURL()
	{
		return esoeSSOURL;
	}

	/**
	 * @param esoeSSOURL the esoeSSOURL to set
	 */
	public void setEsoeSSOURL(String esoeSSOURL)
	{
		this.esoeSSOURL = esoeSSOURL;
	}

	/**
	 * @return the esoeURL
	 */
	public String getEsoeURL()
	{
		return esoeURL;
	}

	/**
	 * @param esoeURL the esoeURL to set
	 */
	public void setEsoeURL(String esoeURL)
	{
		this.esoeURL = esoeURL;
	}

	/**
	 * @return the extractedFiles
	 */
	public String getExtractedFiles()
	{
		return extractedFiles;
	}

	/**
	 * @param extractedFiles the extractedFiles to set
	 */
	public void setExtractedFiles(String extractedFiles)
	{
		this.extractedFiles = extractedFiles;
	}

	/**
	 * @return the issuerID
	 */
	public String getIssuerID()
	{
		return issuerID;
	}

	/**
	 * @param issuerID the issuerID to set
	 */
	public void setIssuerID(String issuerID)
	{
		this.issuerID = issuerID;
	}

	/**
	 * @return the keyStore
	 */
	public KeyStore getKeyStore()
	{
		return keyStore;
	}

	/**
	 * @param keyStore the keyStore to set
	 */
	public void setKeyStore(KeyStore keyStore)
	{
		this.keyStore = keyStore;
	}

	/**
	 * @return the outputDirectory
	 */
	public String getOutputDirectory()
	{
		return outputDirectory;
	}

	/**
	 * @param outputDirectory the outputDirectory to set
	 */
	public void setOutputDirectory(String outputDirectory)
	{
		this.outputDirectory = outputDirectory;
	}

	/**
	 * @return the shibEndpoint
	 */
	public String getShibEndpoint()
	{
		return shibEndpoint;
	}

	/**
	 * @param shibEndpoint the shibEndpoint to set
	 */
	public void setShibEndpoint(String shibEndpoint)
	{
		this.shibEndpoint = shibEndpoint;
	}

	/**
	 * @return the shibKeyPairName
	 */
	public String getShibKeyPairName()
	{
		return shibKeyPairName;
	}

	/**
	 * @param shibKeyPairName the shibKeyPairName to set
	 */
	public void setShibKeyPairName(String shibKeyPairName)
	{
		this.shibKeyPairName = shibKeyPairName;
	}

	/**
	 * @return the shibKeyPairPassphrase
	 */
	public String getShibKeyPairPassphrase()
	{
		return shibKeyPairPassphrase;
	}

	/**
	 * @param shibKeyPairPassphrase the shibKeyPairPassphrase to set
	 */
	public void setShibKeyPairPassphrase(String shibKeyPairPassphrase)
	{
		this.shibKeyPairPassphrase = shibKeyPairPassphrase;
	}

	/**
	 * @return the shibKeyStorePassphrase
	 */
	public String getShibKeyStorePassphrase()
	{
		return shibKeyStorePassphrase;
	}

	/**
	 * @param shibKeyStorePassphrase the shibKeyStorePassphrase to set
	 */
	public void setShibKeyStorePassphrase(String shibKeyStorePassphrase)
	{
		this.shibKeyStorePassphrase = shibKeyStorePassphrase;
	}
	
	
}	
