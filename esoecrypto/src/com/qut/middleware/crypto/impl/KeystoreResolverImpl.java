/*
 * Copyright 2008, Queensland University of Technology
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
 * Author: Shaun Mangelsdorf
 * Creation Date: 23/05/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.crypto.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.qut.middleware.crypto.IssuerSerialPair;
import com.qut.middleware.crypto.KeystoreResolver;
import com.qut.middleware.crypto.exception.KeystoreResolverException;
import com.qut.middleware.saml2.exception.KeyResolutionException;

public class KeystoreResolverImpl implements KeystoreResolver
{
	private static final String KEYSTORE_TYPE = "JKS";
	
	private String localAlias = null;
	private PrivateKey localPrivateKey = null;
	private PublicKey localPublicKey = null;
	private Certificate localCertificate = null;
	private Map<String,Certificate> certificatesByName = null;
	private Map<IssuerSerialPair,Certificate> certificates = null;
	
	private File keystoreFile = null;
	private String keystorePassword = null;
	private String keyPassword = null;
	
	private long lastModified = 0;
	
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	
	public KeystoreResolverImpl(File keystoreFile, String keystorePassword, String localAlias, String keyPassword) throws KeystoreResolverException
	{
		this.loadKeystoreInternal(keystoreFile, keystorePassword, localAlias, keyPassword);
	}
	
	public KeystoreResolverImpl(File keystoreFile, String keystorePassword) throws KeystoreResolverException
	{
		this.loadKeystoreInternal(keystoreFile, keystorePassword, null, null);
	}
	
	private void loadKeystoreInternal(File keystoreFile, String keystorePassword, String localAlias, String keyPassword) throws KeystoreResolverException
	{
		// No lock here - after construction this will only ever be called from within reload()
		
		Map<String,Certificate> certificatesByName = new TreeMap<String, Certificate>();
		Map<IssuerSerialPair,Certificate> certificates = new TreeMap<IssuerSerialPair, Certificate>();
		PrivateKey localPrivateKey = null;
		Certificate localCertificate = null;
		PublicKey localPublicKey = null;
		long lastModified = keystoreFile.lastModified();

		try
		{
			InputStream keystoreInputStream = new FileInputStream(keystoreFile);
			KeyStore keystore = KeyStore.getInstance(KEYSTORE_TYPE);
			keystore.load(keystoreInputStream, keystorePassword.toCharArray());
			keystoreInputStream.close();
			
			if (localAlias != null)
			{
				Key key = keystore.getKey(localAlias, keyPassword.toCharArray());
				if (key instanceof PrivateKey)
				{
					localPrivateKey = (PrivateKey)key;
				}
				else
				{
					String className = (key == null) ? "null" : key.getClass().getName();
					throw new KeystoreResolverException("Keystore resolver could not be instantiated. The supplied local key alias does not have a corresponding private key in the keystore. The instance class of the key was: " + className);
				}
				
				localCertificate = keystore.getCertificate(localAlias);
				if (localCertificate == null)
				{
					throw new KeystoreResolverException("Keystore resolver could not be instantiated. The local private key does not have a matching certificate entry in the keystore.");
				}
				localPublicKey = localCertificate.getPublicKey();
			}
			
			
			Enumeration<String> aliases = keystore.aliases();
			while (aliases.hasMoreElements())
			{
				String alias = aliases.nextElement();
				if (keystore.isCertificateEntry(alias) || keystore.isKeyEntry(alias))
				{
					cacheCertificate(certificatesByName, certificates, alias, keystore.getCertificate(alias));
				}
			}
		}
		catch (KeyStoreException e)
		{
			throw new KeystoreResolverException("Keystore resolver could not be instantiated due to a problem with the supplied keystore. Error was: " + e.getMessage(), e);
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new KeystoreResolverException("Keystore resolver could not be instantiated due to a missing algorithm. Error was: " + e.getMessage(), e);
		}
		catch (CertificateException e)
		{
			throw new KeystoreResolverException("Keystore resolver could not be instantiated due to a certificate problem. Error was: " + e.getMessage(), e);
		}
		catch (UnrecoverableKeyException e)
		{
			throw new KeystoreResolverException("Keystore resolver could not be instantiated due to an invalid key. Error was: " + e.getMessage(), e);
		}
		catch (IOException e)
		{
			throw new KeystoreResolverException("Keystore resolver could not be instantiated due to an I/O error. Error was: " + e.getMessage(), e);
		}
		
		// Loaded successfully... replace the cached objects
		this.certificatesByName = certificatesByName;
		this.certificates = certificates;
		this.localPrivateKey = localPrivateKey;
		this.localCertificate = localCertificate;
		this.localPublicKey = localPublicKey;
		this.localAlias = localAlias;
		this.keystoreFile = keystoreFile;
		this.keystorePassword = keystorePassword;
		this.keyPassword = keyPassword;
		this.lastModified = lastModified;
	}
	
	private void cacheCertificate(Map<String,Certificate> certificatesByName, Map<IssuerSerialPair,Certificate> certificates, String alias, Certificate cert)
	{
		if (cert == null) return;
		
		certificatesByName.put(alias, cert);
		if (cert instanceof X509Certificate)
		{
			X509Certificate x509Certificate = (X509Certificate)cert;
			String name = x509Certificate.getIssuerDN().getName();
			BigInteger serial = x509Certificate.getSerialNumber();
			
			certificates.put(new IssuerSerialPairImpl(name, serial), cert);
		}
	}
	
	public String getLocalKeyAlias()
	{
		this.lock.readLock().lock();
		try
		{
			return this.localAlias;
		}
		finally
		{
			this.lock.readLock().unlock();
		}
	}

	public PrivateKey getLocalPrivateKey()
	{
		this.lock.readLock().lock();
		try
		{
			return this.localPrivateKey;
		}
		finally
		{
			this.lock.readLock().unlock();
		}
	}

	public PublicKey getLocalPublicKey()
	{
		this.lock.readLock().lock();
		try
		{
			return this.localPublicKey;
		}
		finally
		{
			this.lock.readLock().unlock();
		}
	}
	
	public Certificate getLocalCertificate()
	{
		this.lock.readLock().lock();
		try
		{
			return this.localCertificate;
		}
		finally
		{
			this.lock.readLock().unlock();
		}
	}

	public Certificate resolveCertificate(String alias)
	{
		this.lock.readLock().lock();
		try
		{
			return this.certificatesByName.get(alias);
		}
		finally
		{
			this.lock.readLock().unlock();
		}
	}

	public PublicKey resolveKey(String alias)
	{
		this.lock.readLock().lock();
		try
		{
			Certificate cert = this.resolveCertificate(alias);
			if (cert != null)
			{
				return cert.getPublicKey();
			}
			
			return null;
		}
		finally
		{
			this.lock.readLock().unlock();
		}
	}

	public Certificate resolveCertificate(String issuerDN, BigInteger serialNumber)
	{
		this.lock.readLock().lock();
		try
		{
			IssuerSerialPair issuerSerialPair = new IssuerSerialPairImpl(issuerDN, serialNumber);
			return this.certificates.get(issuerSerialPair);
		}
		finally
		{
			this.lock.readLock().unlock();
		}
	}

	public PublicKey resolveKey(String issuerDN, BigInteger serialNumber) throws KeyResolutionException
	{
		this.lock.readLock().lock();
		try
		{
			Certificate cert = this.resolveCertificate(issuerDN, serialNumber);
			if (cert != null)
			{
				return cert.getPublicKey();
			}
			
			return null;
		}
		finally
		{
			this.lock.readLock().unlock();
		}
	}
	
	public void reload() throws KeystoreResolverException
	{
		// Write lock here to stop two threads updating the keystore concurrently.
		this.lock.writeLock().lock();
		try
		{
			long lastModified = this.keystoreFile.lastModified();
			if (this.lastModified < lastModified)
			{
				this.loadKeystoreInternal(this.keystoreFile, this.keystorePassword, this.localAlias, this.keyPassword);
			}
		}
		finally
		{
			this.lock.writeLock().unlock();
		}
	}

	public Map<String, Certificate> getCertificates()
	{
		this.lock.readLock().lock();
		try
		{
			return Collections.unmodifiableMap(this.certificatesByName);
		}
		finally
		{
			this.lock.readLock().unlock();
		}
	}
}
