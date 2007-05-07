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
 * Author: Shaun Mangelsdorf
 * Creation Date: 01/11/2006
 * 
 * Purpose: Resolves a key from a keystore.
 */
package com.qut.middleware.esoe.crypto.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.text.MessageFormat;

import org.apache.log4j.Logger;

import com.qut.middleware.esoe.crypto.KeyStoreResolver;
import com.qut.middleware.esoe.spep.Messages;

/** Resolves a key from a keystore. */
public class KeyStoreResolverImpl implements KeyStoreResolver
{
	private KeyStore keyStore;
	private PrivateKey esoePrivateKey;
	private String esoeKeyAlias;
	private PublicKey esoePublicKey;
	
	/* Local logging instance */
	private Logger logger = Logger.getLogger(KeyStoreResolverImpl.class.getName());

	/** Constructor.
	 * 
	 * @param keyStoreFile The path to the keystore to be used.
	 * @param keyStorePassword Password of the keystore to be used.
	 * @param esoeKeyAlias alias of the ESOE private key.
	 * @param esoeKeyPassword password for the ESOE private key. If null an empty password is attempted
	 * when retrieving the ESOE private key.
	 */
	public KeyStoreResolverImpl(File keyStoreFile, String keyStorePassword, String esoeKeyAlias, String esoeKeyPassword)
	{
		this.esoeKeyAlias = esoeKeyAlias;
		String keyPassword;
		
		if(keyStoreFile == null)
		{
			throw new IllegalArgumentException(Messages.getString("KeyStoreResolverImpl.14")); //$NON-NLS-1$
		}
		if(keyStorePassword == null)
		{
			throw new IllegalArgumentException(Messages.getString("KeyStoreResolverImpl.15")); //$NON-NLS-1$
		}
		if(esoeKeyAlias == null)
		{
			throw new IllegalArgumentException(Messages.getString("KeyStoreResolverImpl.16")); //$NON-NLS-1$
		}
		if(esoeKeyPassword == null)
		{
			keyPassword = ""; //$NON-NLS-1$
		}
		else
			keyPassword = esoeKeyPassword;
		
		try
		{
			this.logger.debug(MessageFormat.format(Messages.getString("KeyStoreResolverImpl.4"), keyStoreFile.getAbsolutePath(), esoeKeyAlias)); //$NON-NLS-1$
			
			this.keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			InputStream keyStoreStream = new FileInputStream(keyStoreFile);
			this.keyStore.load(keyStoreStream, keyStorePassword.toCharArray());
			keyStoreStream.close();
			
			this.logger.info(MessageFormat.format(Messages.getString("KeyStoreResolverImpl.5"), keyStoreFile.getAbsolutePath(), esoeKeyAlias)); //$NON-NLS-1$
		}
		catch (KeyStoreException e)
		{
			String message = Messages.getString("KeyStoreResolverImpl.6"); //$NON-NLS-1$
			this.logger.error(message, e);
			throw new UnsupportedOperationException(message, e);
		}
		catch (FileNotFoundException e)
		{
			String message = Messages.getString("KeyStoreResolverImpl.7"); //$NON-NLS-1$
			this.logger.error(message, e);
			throw new UnsupportedOperationException(message, e);
		}
		catch (NoSuchAlgorithmException e)
		{
			String message = Messages.getString("KeyStoreResolverImpl.8"); //$NON-NLS-1$
			this.logger.error(message, e);
			throw new UnsupportedOperationException(message, e);
		}
		catch (CertificateException e)
		{
			String message = Messages.getString("KeyStoreResolverImpl.9"); //$NON-NLS-1$
			this.logger.error(message, e);
			throw new UnsupportedOperationException(message, e);
		}
		catch (IOException e)
		{
			String message = Messages.getString("KeyStoreResolverImpl.10"); //$NON-NLS-1$
			this.logger.error(message, e);
			throw new UnsupportedOperationException(message, e);
		}
		
		try
		{
			Key key = this.keyStore.getKey(esoeKeyAlias, keyPassword.toCharArray());
			if (key instanceof PrivateKey)
			{
				this.esoePrivateKey = (PrivateKey)key;
			}
			else
			{
				this.logger.debug(MessageFormat.format(Messages.getString("KeyStoreResolverImpl.11"), keyStoreFile.getAbsolutePath(), esoeKeyAlias)); //$NON-NLS-1$
				this.esoePrivateKey = null;
			}
			
			this.esoePublicKey = resolveKey(esoeKeyAlias);
		}
		catch (KeyStoreException e)
		{
			this.logger.error(Messages.getString("KeyStoreResolverImpl.1"), e); //$NON-NLS-1$
			throw new UnsupportedOperationException(Messages.getString("KeyStoreResolverImpl.1"), e); //$NON-NLS-1$
		}
		catch (NoSuchAlgorithmException e)
		{
			this.logger.error(Messages.getString("KeyStoreResolverImpl.2"), e); //$NON-NLS-1$
			throw new UnsupportedOperationException(Messages.getString("KeyStoreResolverImpl.2"), e); //$NON-NLS-1$
		}
		catch (UnrecoverableKeyException e)
		{
			this.logger.error(Messages.getString("KeyStoreResolverImpl.3"), e); //$NON-NLS-1$
			throw new UnsupportedOperationException(Messages.getString("KeyStoreResolverImpl.3"), e); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.saml2.ExternalKeyResolver#resolveKey(java.lang.String)
	 */
	public PublicKey resolveKey(String alias)
	{
		Certificate certificate = this.resolveCertificate(alias);
		if(certificate == null) 
		{
			this.logger.debug(MessageFormat.format(Messages.getString("KeyStoreResolverImpl.12"), alias)); //$NON-NLS-1$
			return null;
		}
		return certificate.getPublicKey();
	}

	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.crypto.KeyStoreResolver#resolveCertificate(java.lang.String)
	 */
	public Certificate resolveCertificate(String alias)
	{
		try
		{
			return this.keyStore.getCertificate(alias);
		}
		catch (KeyStoreException e)
		{
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.crypto.KeyStoreResolver#getPrivateKey()
	 */
	public PrivateKey getPrivateKey()
	{
		if (this.esoePrivateKey != null)
		{
			return this.esoePrivateKey;
		}
		
		this.logger.error(MessageFormat.format(Messages.getString("KeyStoreResolverImpl.13"), this.esoeKeyAlias)); //$NON-NLS-1$
		throw new IllegalArgumentException(Messages.getString("KeyStoreResolverImpl.0")); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.crypto.KeyStoreResolver#getKeyAlias()
	 */
	public String getKeyAlias()
	{
		return this.esoeKeyAlias;
	}

	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.crypto.KeyStoreResolver#getPublicKey()
	 */
	public PublicKey getPublicKey()
	{
		return this.esoePublicKey;
	}

}
