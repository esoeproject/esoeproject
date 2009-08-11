/* Copyright 2008, Queensland University of Technology
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
package com.qut.middleware.esoemanager.manager.logic.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.crypto.CryptoProcessor;
import com.qut.middleware.crypto.exception.CryptoException;
import com.qut.middleware.esoemanager.Constants;
import com.qut.middleware.esoemanager.client.rpc.bean.KeyDetails;
import com.qut.middleware.esoemanager.exception.ManagerDAOException;
import com.qut.middleware.esoemanager.exception.RetrieveKeystoreException;
import com.qut.middleware.esoemanager.exception.RetrieveServiceCryptoException;
import com.qut.middleware.esoemanager.exception.ServiceCryptoCreationException;
import com.qut.middleware.esoemanager.exception.ServiceCryptoDeletionException;
import com.qut.middleware.esoemanager.manager.bean.KeyStoreBean;
import com.qut.middleware.esoemanager.manager.logic.ServiceCrypto;
import com.qut.middleware.esoemanager.manager.sqlmap.ManagerDAO;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;

public class ServiceCryptoImpl implements ServiceCrypto
{
	private ManagerDAO managerDAO;
	private IdentifierGenerator identifierGenerator;
	private CryptoProcessor cryptoProcessor;

	private int esoeENTID;
	private String esoeEntityID;

	private long expiryWarnTime = 5184000000L; // Will expire within 60 days
	private long expiryErrorTime = 2592000000L; // Will expire within 30 days

	private Logger logger = LoggerFactory.getLogger(ServiceCryptoImpl.class);

	public List<KeyDetails> retrieveServiceKeys(String serviceID) throws RetrieveServiceCryptoException
	{
		List<KeyDetails> keyDetailsList = new ArrayList<KeyDetails>();
		List<Map<String, Object>> keyStoreDetailsMap;

		try
		{
			Integer entID = new Integer(serviceID);
			if (entID.intValue() == this.esoeENTID)
			{
				Integer descID = this.managerDAO.getDescID(entID, Constants.IDP_DESCRIPTOR);
				keyStoreDetailsMap = this.managerDAO.queryKeyStoreDetails(descID);
			}
			else
			{
				Integer descID = this.managerDAO.getDescID(entID, Constants.SP_DESCRIPTOR);
				keyStoreDetailsMap = this.managerDAO.queryKeyStoreDetails(descID);
			}
		}
		catch (ManagerDAOException e)
		{
			throw new RetrieveServiceCryptoException("Exception when attempting to retrieve keystore", e);
		}

		if (keyStoreDetailsMap == null)
		{
			throw new RetrieveServiceCryptoException("No keystore details available for this service");
		}

		for (Map<String, Object> keyStoreDetail : keyStoreDetailsMap)
		{
			KeyDetails keyDetails = new KeyDetails();
			keyDetails.setExpiryDate((Date) keyStoreDetail.get(Constants.FIELD_PKI_EXPIRY_DATE));
			keyDetails.setKeypairName((String) keyStoreDetail.get(Constants.FIELD_PKI_KEYPAIRNAME));
			keyDetails.setKeystorePassphrase((String) keyStoreDetail.get(Constants.FIELD_PKI_KEYSTORE_PASSPHRASE));
			keyDetails.setKeypairPassphrase((String) keyStoreDetail.get(Constants.FIELD_PKI_KEYPAIR_PASSPHRASE));

			// Determine if we need to issue a warning or error about this key
			if (keyDetails.getExpiryDate().getTime() > System.currentTimeMillis())
			{
				if (keyDetails.getExpiryDate().getTime() < System.currentTimeMillis() + this.expiryErrorTime)
					keyDetails.setExpireError(true);
				else
					if (keyDetails.getExpiryDate().getTime() < System.currentTimeMillis() + this.expiryWarnTime)
						keyDetails.setExpireWarn(true);
			}

			keyDetailsList.add(keyDetails);
		}

		return keyDetailsList;
	}

	public KeyStoreBean retrieveKeystore(String serviceID, String keypairName) throws RetrieveServiceCryptoException
	{
		List<Map<String, Object>> keyStoreData;
		byte[] rawKeyStore;
		String keyStorePassphrase;
		KeyStore keyStore;
		
		KeyStoreBean result = new KeyStoreBean();

		try
		{
			Integer entID = new Integer(serviceID);
			if (entID.intValue() == this.esoeENTID)
			{
				Integer descID = this.managerDAO.getDescID(entID, Constants.IDP_DESCRIPTOR);
				keyStoreData = this.managerDAO.queryKeystoreBinary(descID, keypairName);
			}
			else
			{
				Integer descID = this.managerDAO.getDescID(entID, Constants.SP_DESCRIPTOR);
				keyStoreData = this.managerDAO.queryKeystoreBinary(descID, keypairName);
			}

			if (keyStoreData == null || keyStoreData.size() == 0)
			{
				this.logger.info("No key data returned for provided descriptor");
				throw new RetrieveKeystoreException("No key data returned for provided descriptor");
			}

			rawKeyStore = (byte[]) keyStoreData.get(0).get(Constants.FIELD_PKI_KEYSTORE);
			keyStorePassphrase = (String) keyStoreData.get(0).get(Constants.FIELD_PKI_KEYSTORE_PASSPHRASE);

			keyStore = this.cryptoProcessor.generateKeyStore();
			ByteArrayInputStream in = new ByteArrayInputStream(rawKeyStore);
			keyStore.load(in, keyStorePassphrase.toCharArray());
			
			result.setKeyStore(keyStore);
			result.setPassphrase(keyStorePassphrase);

			return result;
		}
		catch (NumberFormatException e)
		{
			throw new RetrieveServiceCryptoException(e.getLocalizedMessage(), e);
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new RetrieveServiceCryptoException(e.getLocalizedMessage(), e);
		}
		catch (CertificateException e)
		{
			throw new RetrieveServiceCryptoException(e.getLocalizedMessage(), e);
		}
		catch (ManagerDAOException e)
		{
			throw new RetrieveServiceCryptoException(e.getLocalizedMessage(), e);
		}
		catch (RetrieveKeystoreException e)
		{
			throw new RetrieveServiceCryptoException(e.getLocalizedMessage(), e);
		}
		catch (CryptoException e)
		{
			throw new RetrieveServiceCryptoException(e.getLocalizedMessage(), e);
		}
		catch (IOException e)
		{
			throw new RetrieveServiceCryptoException(e.getLocalizedMessage(), e);
		}

	}

	public void createServiceKey(String serviceID) throws ServiceCryptoCreationException
	{
		String keyStorePassphrase;
		String keyPairName;
		String keyPairPassphrase;
		String keyPairSubjectDN;
		KeyPair spKeyPair;
		KeyStore keyStore;
		byte[] keyStoreBytes;

		try
		{
			Integer entID = new Integer(serviceID);

			if (entID.intValue() == this.esoeENTID)
			{
				keyPairSubjectDN = this.generateSubjectDN(this.esoeEntityID);
			}
			else
			{
				String serviceURL = null;
				Map<String, Object> description = this.managerDAO.queryServiceDescription(entID);
				if (description == null)
					throw new ServiceCryptoCreationException("Unable to retrieve serviceURL for this service");

				serviceURL = (String) description.get(Constants.FIELD_SERVICE_URL);
				keyPairSubjectDN = this.generateSubjectDN(serviceURL);
			}

			keyPairName = this.identifierGenerator.generateXMLKeyName();

			keyStorePassphrase = this.generatePassphrase();
			keyPairPassphrase = this.generatePassphrase();

			keyStore = this.cryptoProcessor.generateKeyStore();
			spKeyPair = this.cryptoProcessor.generateKeyPair();
			this.cryptoProcessor.addKeyPair(keyStore, keyStorePassphrase, spKeyPair, keyPairName, keyPairPassphrase,
					keyPairSubjectDN);
			keyStoreBytes = this.cryptoProcessor.convertKeystoreByteArray(keyStore, keyStorePassphrase);
			
			X509Certificate certificate = (X509Certificate)keyStore.getCertificate(keyPairName);

			/* Determine expiry date of PKI data */
			Calendar expiryDate = Calendar.getInstance();
			expiryDate.add(Calendar.YEAR, this.cryptoProcessor.getCertExpiryIntervalInYears());

			if (entID.intValue() == this.esoeENTID)
			{
				/** Commit key data to IDP reference */
				Integer descID_IDP = this.managerDAO.getDescID(entID, Constants.IDP_DESCRIPTOR);
				this.managerDAO.insertPublicKey(descID_IDP, expiryDate.getTime(), keyPairName, this.cryptoProcessor.getCertIssuerDN(),
						certificate.getSerialNumber().toString(), this.cryptoProcessor.convertPublicKeyByteArray(spKeyPair.getPublic()));

				this.managerDAO.insertPKIData(descID_IDP, expiryDate.getTime(), keyStoreBytes, keyStorePassphrase,
						keyPairName, keyPairPassphrase);

				/** Commit key data to PDP reference */
				Integer descID_PDP = this.managerDAO.getDescID(entID, Constants.LXACML_PDP_DESCRIPTOR);
				this.managerDAO.insertPublicKey(descID_PDP, expiryDate.getTime(), keyPairName, this.cryptoProcessor.getCertIssuerDN(),
						certificate.getSerialNumber().toString(), this.cryptoProcessor.convertPublicKeyByteArray(spKeyPair.getPublic()));

				this.managerDAO.insertPKIData(descID_PDP, expiryDate.getTime(), keyStoreBytes, keyStorePassphrase,
						keyPairName, keyPairPassphrase);

				/** Commit key data to Attribute Authority reference */
				Integer descID_AA = this.managerDAO.getDescID(entID, Constants.ATTRIBUTE_AUTHORITY_DESCRIPTOR);
				this.managerDAO.insertPublicKey(descID_AA, expiryDate.getTime(), keyPairName, this.cryptoProcessor.getCertIssuerDN(),
						certificate.getSerialNumber().toString(), this.cryptoProcessor.convertPublicKeyByteArray(spKeyPair.getPublic()));

				this.managerDAO.insertPKIData(descID_AA, expiryDate.getTime(), keyStoreBytes, keyStorePassphrase,
						keyPairName, keyPairPassphrase);
			}
			else
			{
				Integer descID = this.managerDAO.getDescID(entID, Constants.SP_DESCRIPTOR);
				this.managerDAO.insertPublicKey(descID, expiryDate.getTime(), keyPairName, this.cryptoProcessor.getCertIssuerDN(),
						certificate.getSerialNumber().toString(), this.cryptoProcessor.convertPublicKeyByteArray(spKeyPair.getPublic()));
				this.managerDAO.insertPKIData(descID, expiryDate.getTime(), keyStoreBytes, keyStorePassphrase,
						keyPairName, keyPairPassphrase);
			}

		}
		catch (NumberFormatException e)
		{
			throw new ServiceCryptoCreationException(e.getLocalizedMessage(), e);
		}
		catch (ManagerDAOException e)
		{
			throw new ServiceCryptoCreationException(e.getLocalizedMessage(), e);
		}
		catch (CryptoException e)
		{
			throw new ServiceCryptoCreationException(e.getLocalizedMessage(), e);
		}
		catch (KeyStoreException e)
		{
			throw new ServiceCryptoCreationException("Could not obtain certificate from new keystore.", e);
		}
	}

	public void deleteServiceKey(String serviceID, String keypairName) throws ServiceCryptoDeletionException
	{
		Integer entID = new Integer(serviceID);
		try
		{
			if (entID.intValue() == this.esoeENTID)
			{
				Integer descID_IDP = this.managerDAO.getDescID(entID, Constants.IDP_DESCRIPTOR);
				this.managerDAO.deleteServiceKey(descID_IDP, keypairName);

				Integer descID_PDP = this.managerDAO.getDescID(entID, Constants.LXACML_PDP_DESCRIPTOR);
				this.managerDAO.deleteServiceKey(descID_PDP, keypairName);

				Integer descID_AA = this.managerDAO.getDescID(entID, Constants.ATTRIBUTE_AUTHORITY_DESCRIPTOR);
				this.managerDAO.deleteServiceKey(descID_AA, keypairName);
			}
			else
			{
				Integer descID = this.managerDAO.getDescID(entID, Constants.SP_DESCRIPTOR);
				this.managerDAO.deleteServiceKey(descID, keypairName);
			}
		}
		catch (ManagerDAOException e)
		{
			throw new ServiceCryptoDeletionException(e.getLocalizedMessage(), e);
		}
	}

	private String generatePassphrase()
	{
		SecureRandom random;
		String passphrase;
		byte[] buf;

		try
		{
			random = SecureRandom.getInstance("SHA1PRNG");
		}
		catch (NoSuchAlgorithmException nsae)
		{
			this.logger
					.error("NoSuchAlgorithmException when trying to create SecureRandom instance " + nsae.getLocalizedMessage()); //$NON-NLS-1$
			this.logger.debug(nsae.getLocalizedMessage(), nsae);
			random = new SecureRandom();
		}

		buf = new byte[Constants.PASSPHRASE_LENGTH];
		random.nextBytes(buf);
		passphrase = new String(Hex.encodeHex(buf));

		return passphrase;
	}

	private String generateSubjectDN(String service)
	{
		try
		{
			String result = new String();
			URL serviceURL = new URL(service);
			String[] components = serviceURL.getHost().split("\\.");
			for (String component : components)
			{
				if (result.length() != 0)
					result = result + ",";

				result = result + "dc=" + component;
			}
			return result;
		}
		catch (MalformedURLException e)
		{
			this.logger.error("Error attempting to generate certificate subjectDN " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			return "dc=" + service;
		}
	}

	public ManagerDAO getManagerDAO()
	{
		return managerDAO;
	}

	public void setManagerDAO(ManagerDAO managerDAO)
	{
		this.managerDAO = managerDAO;
	}

	public IdentifierGenerator getIdentifierGenerator()
	{
		return identifierGenerator;
	}

	public void setIdentifierGenerator(IdentifierGenerator identifierGenerator)
	{
		this.identifierGenerator = identifierGenerator;
	}

	public CryptoProcessor getCryptoProcessor()
	{
		return cryptoProcessor;
	}

	public void setCryptoProcessor(CryptoProcessor cryptoProcessor)
	{
		this.cryptoProcessor = cryptoProcessor;
	}

	public int getEsoeENTID()
	{
		return esoeENTID;
	}

	public void setEsoeENTID(int esoeENTID)
	{
		this.esoeENTID = esoeENTID;
	}

	public String getEsoeEntityID()
	{
		return esoeEntityID;
	}

	public void setEsoeEntityID(String esoeEntityID)
	{
		this.esoeEntityID = esoeEntityID;
	}
}
