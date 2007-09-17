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
 * Purpose:  Default crypto processor impl
 */
package com.qut.middleware.crypto.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

import javax.security.auth.x500.X500Principal;
import javax.security.auth.x500.X500PrivateCredential;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.w3._2000._09.xmldsig_.KeyInfo;
import org.w3._2000._09.xmldsig_.KeyValue;
import org.w3._2000._09.xmldsig_.RSAKeyValue;

import com.qut.middleware.crypto.CryptoProcessor;
import com.qut.middleware.crypto.KeyStoreResolver;
import com.qut.middleware.crypto.exception.CryptoException;
import com.qut.middleware.saml2.schemas.metadata.KeyDescriptor;
import com.qut.middleware.saml2.schemas.metadata.KeyTypes;
import com.qut.middleware.saml2.sec.KeyName;

public class CryptoProcessorImpl implements CryptoProcessor
{
	private int certExpiryIntervalInYears;

	private String certIssuerDN;
	private String certIssuerEmail;
	private int keySize;

	/* Provides keydata that metadata documents will be signed with */
	private KeyStoreResolver localResolver;

	/* Local logging instance */
	private Logger logger = Logger.getLogger(CryptoProcessorImpl.class.getName());

	/**
	 * Creates crypto processor object that must be configured using setters before use
	 */
	public CryptoProcessorImpl()
	{
		// not implemented
	}

	/**
	 * Creates fully populated crypto processor ready for use
	 * 
	 * @param localResolver
	 * @param certIssuerDN
	 * @param certIssuerEmail
	 * @param certExpiryInterval
	 * @param keySize
	 */
	public CryptoProcessorImpl(KeyStoreResolver localResolver, String certIssuerDN, String certIssuerEmail, int certExpiryInterval, int keySize)
	{
		if (localResolver == null)
		{
			this.logger.error("localResolver for CryptoProcessorImpl was NULL");
			throw new IllegalArgumentException("localResolver for CryptoProcessorImpl was NULL");
		}
		if (certIssuerDN == null)
		{
			this.logger.error("certIssuerDN for CryptoProcessorImpl was NULL");
			throw new IllegalArgumentException("certIssuerDN for CryptoProcessorImpl was NULL");
		}
		if (certIssuerEmail == null)
		{
			this.logger.error("certIssuerEmail for CryptoProcessorImpl was NULL");
			throw new IllegalArgumentException("certIssuerEmail for CryptoProcessorImpl was NULL");
		}

		this.localResolver = localResolver;
		this.certIssuerDN = certIssuerDN;
		this.certIssuerEmail = certIssuerEmail;
		this.certExpiryIntervalInYears = certExpiryInterval;
		this.keySize = keySize;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.crypto.CryptoProcessor#generatePassphrase()
	 */
	public String generatePassphrase()
	{
		SecureRandom random;
		String passphrase;
		byte[] buf;

		try
		{
			/* Attempt to get the specified RNG instance */
			random = SecureRandom.getInstance("SHA1PRNG");
		}
		catch (NoSuchAlgorithmException nsae)
		{
			random = new SecureRandom();
		}

		buf = new byte[10];
		random.nextBytes(buf);
		passphrase = new String(Hex.encodeHex(buf));

		return passphrase;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.crypto.impl.CryptoProcessor#createSigningKeyDescriptor(java.security.interfaces.RSAPublicKey,
	 *      java.lang.String)
	 */
	public KeyDescriptor createSigningKeyDescriptor(RSAPublicKey pubKey, String keyPairName)
	{
		KeyDescriptor keyDescriptor = new KeyDescriptor();
		keyDescriptor.setUse(KeyTypes.SIGNING);
		KeyInfo keyInfo = new KeyInfo();
		KeyName keyName = new KeyName(keyPairName);
		keyInfo.getContent().add(keyName);

		KeyValue keyValue = new KeyValue();
		RSAKeyValue rsaKeyValue = new RSAKeyValue();
		rsaKeyValue.setExponent(pubKey.getPublicExponent().toByteArray());
		rsaKeyValue.setModulus(pubKey.getModulus().toByteArray());
		keyValue.getContent().add(rsaKeyValue);
		keyInfo.getContent().add(keyValue);
		keyDescriptor.setKeyInfo(keyInfo);

		logger.debug("Generated KeyDescriptor for document signing with keyname " + keyPairName);

		return keyDescriptor;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.crypto.impl.CryptoProcessor#generateKeyPair(int)
	 */
	public KeyPair generateKeyPair() throws CryptoException
	{
		try
		{
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(this.keySize);

			KeyPair keyPair = keyGen.generateKeyPair();
			return keyPair;
		}
		catch (NoSuchAlgorithmException e)
		{
			this.logger.error("NoSuchAlgorithmException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new CryptoException(e.getLocalizedMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.crypto.impl.CryptoProcessor#generateKeyStore(java.lang.String)
	 */
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

			/*
			 * Populate all new key stores with the key data of the local resolver, generally this is for metadata
			 * purposes to ensure that all systems in the authentication network can correctly validate the signed
			 * metadata document
			 */
			addPublicKey(keyStore, new KeyPair(this.localResolver.getPublicKey(), this.localResolver.getPrivateKey()), this.localResolver.getKeyAlias(), this.certIssuerDN);

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.crypto.impl.CryptoProcessor#addPublicKey(java.security.KeyStore, java.security.KeyPair,
	 *      java.lang.String, java.lang.String)
	 */
	public void addPublicKey(KeyStore ks, KeyPair keyPair, String keyPairName, String keyPairSubjectDN) throws CryptoException
	{
		try
		{
			X509Certificate cert = generateV3Certificate(keyPair, keyPairSubjectDN);
			ks.setCertificateEntry(keyPairName, cert);

		}
		catch (KeyStoreException e)
		{
			this.logger.error("KeyStoreException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new CryptoException(e.getLocalizedMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.crypto.impl.CryptoProcessor#addKeyPair(java.security.KeyStore, java.lang.String,
	 *      java.security.KeyPair, java.lang.String, java.lang.String, java.lang.String)
	 */
	public KeyStore addKeyPair(KeyStore keyStore, String keyStorePassphrase, KeyPair keyPair, String keyPairName, String keyPairPassphrase, String keyPairSubjectDN) throws CryptoException
	{
		logger.debug("Adding key pair to existing key store");

		try
		{
			// Create the public key certificate for storage in the key store.
			X509Certificate cert = generateV3Certificate(keyPair, keyPairSubjectDN);
			X500PrivateCredential privateCredentials = new X500PrivateCredential(cert, keyPair.getPrivate(), keyPairName);

			Certificate[] certChain = new X509Certificate[1];
			certChain[0] = privateCredentials.getCertificate();

			// Load our generated key store up. They all have the same password, which we set.
			keyStore.load(null, keyStorePassphrase.toCharArray());

			/* Add certificate which contains the public key and set the private key as a key entry in the key store */
			keyStore.setCertificateEntry(privateCredentials.getAlias(), privateCredentials.getCertificate());
			keyStore.setKeyEntry(privateCredentials.getAlias(), keyPair.getPrivate(), keyPairPassphrase.toCharArray(), certChain);

			return keyStore;
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
		catch (KeyStoreException e)
		{
			this.logger.error("KeyStoreException thrown, " + e.getLocalizedMessage());
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.crypto.impl.CryptoProcessor#generateV3Certificate(java.security.KeyPair,
	 *      java.lang.String)
	 */
	public X509Certificate generateV3Certificate(KeyPair pair, String certSubjectDN) throws CryptoException
	{
		X509V3CertificateGenerator cert = new X509V3CertificateGenerator();

		/* Set the certificate serial number to a random number */
		Random rand = new Random();
		rand.setSeed(System.currentTimeMillis());

		/* Generates a number between 0 and 2^32 as the serial */
		BigInteger serial = BigInteger.valueOf(rand.nextInt(Integer.MAX_VALUE));
		logger.info("Setting X509 Cert Serial to: " + serial);

		cert.setSerialNumber(serial);

		/* Set the certificate issuer */
		cert.setIssuerDN(new X500Principal(this.certIssuerDN));

		/* Set the start of valid period to now - a few seconds for time skew. */
		Calendar before = new GregorianCalendar();
		before.add(Calendar.DAY_OF_MONTH, -1);
		cert.setNotBefore(before.getTime());

		/* Set the certificate expiry date to current time plus configured interval for expiry. */
		Calendar expiry = new GregorianCalendar();
		expiry.add(Calendar.YEAR, this.certExpiryIntervalInYears);
		cert.setNotAfter(expiry.getTime());

		/* Set the subject */
		cert.setSubjectDN(new X500Principal(certSubjectDN));

		cert.setPublicKey(pair.getPublic());

		/* Signature algorithm, this may need to be changed if not all hosts have SHA256 and RSA implementations */
		cert.setSignatureAlgorithm("SHA512withRSA");

		cert.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(false));

		/* Only for signing */
		cert.addExtension(X509Extensions.KeyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyCertSign));
		cert.addExtension(X509Extensions.ExtendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth));

		/* Set a contact email address for the issuer */
		cert.addExtension(X509Extensions.SubjectAlternativeName, false, new GeneralNames(new GeneralName(GeneralName.rfc822Name, this.certIssuerEmail)));

		logger.debug("Generating X509Certificate for key pair: " + pair);

		try
		{
			/* Use the BouncyCastle provider to actually generate the X509Certificate now */
			return cert.generateX509Certificate(pair.getPrivate(), "BC");
		}
		catch (InvalidKeyException e)
		{
			this.logger.error("InvalidKeyException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new CryptoException(e.getLocalizedMessage(), e);
		}
		catch (NoSuchProviderException e)
		{
			this.logger.error("NoSuchProviderException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new CryptoException(e.getLocalizedMessage(), e);
		}
		catch (SecurityException e)
		{
			this.logger.error("SecurityException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new CryptoException(e.getLocalizedMessage(), e);
		}
		catch (SignatureException e)
		{
			this.logger.error("SignatureException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new CryptoException(e.getLocalizedMessage(), e);
		}

	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.crypto.CryptoProcessor#serializeKeyStore(java.security.KeyStore, java.lang.String, java.lang.String)
	 */
	public void serializeKeyStore(KeyStore keyStore, String keyStorePassphrase, String filename) throws CryptoException
	{
		FileOutputStream fos = null;
		try
		{
			fos = new FileOutputStream(filename);
			keyStore.store(fos, keyStorePassphrase.toCharArray());
		}
		catch (FileNotFoundException e)
		{
			this.logger.error(e.getLocalizedMessage());
			this.logger.debug(e);
			throw new CryptoException(e.getLocalizedMessage(), e);
		}
		catch (KeyStoreException e)
		{
			this.logger.error(e.getLocalizedMessage());
			this.logger.debug(e);
			throw new CryptoException(e.getLocalizedMessage(), e);
		}
		catch (NoSuchAlgorithmException e)
		{
			this.logger.error(e.getLocalizedMessage());
			this.logger.debug(e);
			throw new CryptoException(e.getLocalizedMessage(), e);
		}
		catch (CertificateException e)
		{
			this.logger.error(e.getLocalizedMessage());
			this.logger.debug(e);
			throw new CryptoException(e.getLocalizedMessage(), e);
		}
		catch (IOException e)
		{
			this.logger.error(e.getLocalizedMessage());
			this.logger.debug(e);
			throw new CryptoException(e.getLocalizedMessage(), e);
		}
		finally
		{
			if (fos != null)
			{
				try
				{
					fos.flush();
					fos.close();
				}
				catch (IOException e)
				{
					this.logger.error(e.getLocalizedMessage());
					this.logger.debug(e);
					throw new CryptoException(e.getLocalizedMessage(), e);
				}
			}
		}
	}
	
	public PublicKey convertByteArrayPublicKey(byte[] rawKey) throws CryptoException
	{
		ByteArrayInputStream inputStream = null; 
		ObjectInputStream objectInputStream = null;
		PublicKey key;
		
		try
		{
			inputStream = new ByteArrayInputStream(rawKey);
			objectInputStream = new ObjectInputStream(inputStream);
			key = (PublicKey) objectInputStream.readObject();
			
			return key;
		}
		catch (IOException e)
		{
			throw new CryptoException("Exception when attempting to unserialize PublicKey", e);
		}
		catch (ClassNotFoundException e)
		{
			throw new CryptoException("Exception when attempting to unserialize PublicKey", e);
		}
		finally
		{
			try
			{
				if(objectInputStream != null)
					objectInputStream.close();
				
				if(inputStream != null)
					inputStream.close();
			}
			catch (IOException e)
			{
				throw new CryptoException("Exception when attempting to unserialize PublicKey (stream close failed)", e);
			}
		}
	}
	
	public byte[] convertPublicKeyByteArray(PublicKey key) throws CryptoException
	{
		byte[] serializedPK;
		ByteArrayOutputStream outputStream = null;
		ObjectOutputStream objectOutputStream = null;
		
		try
		{
			 outputStream = new ByteArrayOutputStream();
			 objectOutputStream = new ObjectOutputStream(outputStream);
			objectOutputStream.writeObject(key);
			serializedPK = outputStream.toByteArray();
			
			return serializedPK;
		}
		catch (IOException e)
		{
			throw new CryptoException("Exception when attempting to serialize PublicKey", e);
		}
		finally
		{
			try
			{
				if(objectOutputStream != null)
					objectOutputStream.close();
				
				if(outputStream != null)
					outputStream.close();
			}
			catch (IOException e)
			{
				throw new CryptoException("Exception when attempting to serialize PublicKey (stream close failed)", e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.crypto.CryptoProcessor#convertKeystoreByteArray(java.security.KeyStore, java.lang.String)
	 */
	public byte[] convertKeystoreByteArray(KeyStore keyStore, String keyStorePassphrase) throws CryptoException
	{
		byte[] keyStoreBytes;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		try
		{
			keyStore.store(outputStream, keyStorePassphrase.toCharArray());

			keyStoreBytes = outputStream.toByteArray();
			return keyStoreBytes;
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
		finally
		{
			try
			{
				outputStream.close();
			}
			catch (IOException e)
			{
				this.logger.error("IOException thrown in finally, " + e.getLocalizedMessage());
				this.logger.debug(e);
			}
		}
	}

	public int getCertExpiryIntervalInYears()
	{
		return this.certExpiryIntervalInYears;
	}

	public void setCertExpiryIntervalInYears(int certExpiryIntervalInYears)
	{
		this.certExpiryIntervalInYears = certExpiryIntervalInYears;
	}

	public String getCertIssuerDN()
	{
		return this.certIssuerDN;
	}

	public void setCertIssuerDN(String certIssuerDN)
	{
		this.certIssuerDN = certIssuerDN;
	}

	public String getCertIssuerEmail()
	{
		return this.certIssuerEmail;
	}

	public void setCertIssuerEmail(String certIssuerEmail)
	{
		this.certIssuerEmail = certIssuerEmail;
	}

	public int getKeySize()
	{
		return this.keySize;
	}

	public void setKeySize(int keySize)
	{
		this.keySize = keySize;
	}

	public KeyStoreResolver getLocalResolver()
	{
		return this.localResolver;
	}

	public void setLocalResolver(KeyStoreResolver localResolver)
	{
		this.localResolver = localResolver;
	}
}
