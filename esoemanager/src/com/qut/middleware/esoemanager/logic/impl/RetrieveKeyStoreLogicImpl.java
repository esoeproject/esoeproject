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
 * Purpose: Retrieve keystore default logi implementation
 */
package com.qut.middleware.esoemanager.logic.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.sql.Blob;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.qut.middleware.crypto.CryptoProcessor;
import com.qut.middleware.crypto.exception.CryptoException;
import com.qut.middleware.esoemanager.Constants;
import com.qut.middleware.esoemanager.bean.KeyStoreBean;
import com.qut.middleware.esoemanager.bean.impl.KeyStoreBeanImpl;
import com.qut.middleware.esoemanager.exception.RetrieveKeystoreException;
import com.qut.middleware.esoemanager.exception.SPEPDAOException;
import com.qut.middleware.esoemanager.logic.RetrieveKeyStoreLogic;
import com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO;

public class RetrieveKeyStoreLogicImpl implements RetrieveKeyStoreLogic
{
	private SPEPDAO spepDAO;
	private CryptoProcessor cryptoProcessor;

	/* Local logging instance */
	private Logger logger = Logger.getLogger(RetrieveKeyStoreLogicImpl.class.getName());

	public RetrieveKeyStoreLogicImpl(CryptoProcessor cryptoProcessor, SPEPDAO spepDAO)
	{
		if (cryptoProcessor == null)
		{
			this.logger.error("cryptoProcessor for RetrieveKeyStoreLogicImpl was NULL");
			throw new IllegalArgumentException("cryptoProcessor for RetrieveKeyStoreLogicImpl was NULL");
		}
		if (spepDAO == null)
		{
			this.logger.error("spepDAO for RetrieveKeyStoreLogicImpl was NULL");
			throw new IllegalArgumentException("spepDAO for RetrieveKeyStoreLogicImpl was NULL");
		}

		this.cryptoProcessor = cryptoProcessor;
		this.spepDAO = spepDAO;
	}

	public KeyStoreBean execute(Integer descID) throws RetrieveKeystoreException
	{
		List<Map<String, Object>> keyStoreData;
		Blob binaryKeystore;
		byte[] rawKeyStore;
		String keyStorePassphrase;
		KeyStore keyStore;
		KeyStoreBean bean = new KeyStoreBeanImpl();

		try
		{
			keyStoreData = this.spepDAO.queryKeystoreBinary(descID);
			
			if(keyStoreData == null || keyStoreData.size() == 0)
			{
				this.logger.info("No key data returned for provided descriptor");
				throw new RetrieveKeystoreException("No key data returned for provided descriptor");
			}
			
			rawKeyStore = (byte[]) keyStoreData.get(0).get(Constants.FIELD_PKI_KEYSTORE);
			keyStorePassphrase = (String) keyStoreData.get(0).get(Constants.FIELD_PKI_KEYSTORE_PASSPHRASE);

			keyStore = this.cryptoProcessor.generateKeyStore();
			ByteArrayInputStream in = new ByteArrayInputStream(rawKeyStore);
			keyStore.load(in, keyStorePassphrase.toCharArray());

			bean.setKeyStore(keyStore);
			bean.setKeyStorePassphrase(keyStorePassphrase);

			return bean;
		}
		catch (SPEPDAOException e)
		{
			this.logger.error("SPEPDAOException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new RetrieveKeystoreException(e.getLocalizedMessage(), e);
		}
		catch (CryptoException e)
		{
			this.logger.error("CryptoException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new RetrieveKeystoreException(e.getLocalizedMessage(), e);
		}
		catch (NoSuchAlgorithmException e)
		{
			this.logger.error("NoSuchAlgorithmException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new RetrieveKeystoreException(e.getLocalizedMessage(), e);
		}
		catch (CertificateException e)
		{
			this.logger.error("CertificateException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new RetrieveKeystoreException(e.getLocalizedMessage(), e);
		}
		catch (IOException e)
		{
			this.logger.error("IOException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new RetrieveKeystoreException(e.getLocalizedMessage(), e);
		}
	}

}
