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
 * Purpose: Implements the KeyStoreResolver interface.
 */
package com.qut.middleware.spep.metadata.impl;

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

import com.qut.middleware.spep.metadata.KeyStoreResolver;
import com.qut.middleware.spep.metadata.Messages;

/** Implements the KeyStoreResolver interface. */
public class KeyStoreResolverImpl implements KeyStoreResolver
{
	private KeyStore keyStore;
	private PrivateKey spepPrivateKey;
	private String spepKeyAlias;
	private PublicKey spepPublicKey;


	/* Local logging instance */
	private Logger logger = Logger.getLogger(KeyStoreResolverImpl.class.getName());

	/** Constructor.
	 *  
	 * @param keyStoreStream stream occurance of a KeyStore containing keys and certs required for crypto.
	 * @param keyStorePassword password of the given keystore.
	 * @param spepKeyAlias Alias of the SPEP private key.
	 * @param spepKeyPassword Password for the SPEP private key.
	 */
	public KeyStoreResolverImpl(InputStream keyStoreStream, String keyStorePassword, String spepKeyAlias, String spepKeyPassword)
	{
		if(keyStoreStream == null)
		{
			throw new IllegalArgumentException(Messages.getString("KeyStoreResolverImpl.20")); //$NON-NLS-1$
		}
		if(keyStorePassword == null)
		{
			throw new IllegalArgumentException(Messages.getString("KeyStoreResolverImpl.21")); //$NON-NLS-1$
		}
		if(spepKeyAlias == null)
		{
			throw new IllegalArgumentException(Messages.getString("KeyStoreResolverImpl.22")); //$NON-NLS-1$
		}
		if(spepKeyPassword == null)
		{
			throw new IllegalArgumentException(Messages.getString("KeyStoreResolverImpl.23")); //$NON-NLS-1$
		}
		
		this.spepKeyAlias = spepKeyAlias;
		
		try
		{
			this.keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			this.keyStore.load(keyStoreStream, keyStorePassword.toCharArray());
			keyStoreStream.close();
			
			this.logger.debug(Messages.getString("KeyStoreResolverImpl.4")); //$NON-NLS-1$
		}
		catch (KeyStoreException e)
		{
			this.logger.error(MessageFormat.format(Messages.getString("KeyStoreResolverImpl.5"), e.getMessage())); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("KeyStoreResolverImpl.6")); //$NON-NLS-1$
		}
		catch (FileNotFoundException e)
		{
			this.logger.error(MessageFormat.format(Messages.getString("KeyStoreResolverImpl.7"), e.getMessage())); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("KeyStoreResolverImpl.8")); //$NON-NLS-1$
		}
		catch (NoSuchAlgorithmException e)
		{
			this.logger.error(MessageFormat.format(Messages.getString("KeyStoreResolverImpl.9"), e.getMessage())); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("KeyStoreResolverImpl.10")); //$NON-NLS-1$
		}
		catch (CertificateException e)
		{
			this.logger.error(MessageFormat.format(Messages.getString("KeyStoreResolverImpl.11"), e.getMessage())); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("KeyStoreResolverImpl.12")); //$NON-NLS-1$
		}
		catch (IOException e)
		{
			this.logger.error(MessageFormat.format(Messages.getString("KeyStoreResolverImpl.13"), e.getMessage())); //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.getString("KeyStoreResolverImpl.14")); //$NON-NLS-1$
		}
		
		try
		{
			Key key = this.keyStore.getKey(spepKeyAlias, spepKeyPassword.toCharArray());
			if (key instanceof PrivateKey)
			{
				this.logger.debug(MessageFormat.format(Messages.getString("KeyStoreResolverImpl.15"), spepKeyAlias)); //$NON-NLS-1$
				this.spepPrivateKey = (PrivateKey)key;
			}
			else
			{
				this.logger.error(MessageFormat.format(Messages.getString("KeyStoreResolverImpl.16"), spepKeyAlias)); //$NON-NLS-1$
				throw new IllegalArgumentException(Messages.getString("KeyStoreResolverImpl.0")); //$NON-NLS-1$
			}
			
			this.spepPublicKey = resolveKey(spepKeyAlias);
		}
		catch (KeyStoreException e)
		{
			throw new UnsupportedOperationException(Messages.getString("KeyStoreResolverImpl.1"), e); //$NON-NLS-1$
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new UnsupportedOperationException(Messages.getString("KeyStoreResolverImpl.2"), e); //$NON-NLS-1$
		}
		catch (UnrecoverableKeyException e)
		{
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
			this.logger.debug(MessageFormat.format(Messages.getString("KeyStoreResolverImpl.17"), alias)); //$NON-NLS-1$
			return null;
		}
		return certificate.getPublicKey();
	}

	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.spep.metadata.KeyStoreResolver#resolveCertificate(java.lang.String)
	 */
	public Certificate resolveCertificate(String alias)
	{
		try
		{
			return this.keyStore.getCertificate(alias);
		}
		catch (KeyStoreException e)
		{
			this.logger.debug(MessageFormat.format(Messages.getString("KeyStoreResolverImpl.18"), alias, e.getMessage())); //$NON-NLS-1$
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.spep.metadata.KeyStoreResolver#getPrivateKey()
	 */
	public PrivateKey getPrivateKey()
	{
		return this.spepPrivateKey;
	}

	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.spep.metadata.KeyStoreResolver#getKeyAlias()
	 */
	public String getKeyAlias()
	{
		return this.spepKeyAlias;
	}

	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.spep.metadata.KeyStoreResolver#getPublicKey()
	 */
	public PublicKey getPublicKey()
	{
		return this.spepPublicKey;
	}

}
