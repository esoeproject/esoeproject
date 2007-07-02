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
 * Creation Date: 1/05/07
 * 
 * Purpose: Extends crypto processor for esoestartup specific requrements
 */
package com.qut.middleware.esoestartup.processor;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.qut.middleware.crypto.exception.CryptoException;
import com.qut.middleware.crypto.impl.CryptoProcessorImpl;
import com.qut.middleware.esoestartup.Constants;

public class CryptoProcessorESOEImpl extends CryptoProcessorImpl
{
	/* Local logging instance */
	private Logger logger = Logger.getLogger(CryptoProcessorESOEImpl.class.getName());
	
	public CryptoProcessorESOEImpl()
	{
		super();
		
		setCertExpiryIntervalInYears(Constants.CERT_EXPIRY_YEARS);
		setKeySize(Constants.KEY_SIZE);	
	}
	
	@Override
	public KeyStore generateKeyStore() throws CryptoException
	{
		try
		{
			logger.debug("Generating a new key store.");

			/* Add BC to the jdk security manager to be able to use it as a provider */
			Security.addProvider(new BouncyCastleProvider());

			/* Create and init an empty key store */
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(null, null);

			return keyStore;
		}
		catch (KeyStoreException e)
		{
			this.logger.error("KeyStoreException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new CryptoException(e.getLocalizedMessage(), e);
		}
		catch (NoSuchAlgorithmException e)
		{
			this.logger.error("NoSuchAlgorithmException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new CryptoException(e.getLocalizedMessage(), e);
		}
		catch (CertificateException e)
		{
			this.logger.error("CertificateException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new CryptoException(e.getLocalizedMessage(), e);
		}
		catch (IOException e)
		{
			this.logger.error("IOException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new CryptoException(e.getLocalizedMessage(), e);
		}
	}
}
