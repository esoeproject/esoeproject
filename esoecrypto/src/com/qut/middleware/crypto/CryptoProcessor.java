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
 * Purpose: Handles cryptography operations, inparticular Key and Keystore generation and serialization
 */
package com.qut.middleware.crypto;

import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

import com.qut.middleware.crypto.exception.CryptoException;
import com.qut.middleware.saml2.schemas.metadata.KeyDescriptor;

public interface CryptoProcessor
{
	public String generatePassphrase();

	public KeyDescriptor createSigningKeyDescriptor(RSAPublicKey pubKey, String keyPairName);

	public KeyPair generateKeyPair() throws CryptoException;

	public KeyStore generateKeyStore() throws CryptoException;

	public void addPublicKey(KeyStore ks, KeyPair keyPair, String keyPairName, String keyPairSubjectDN)
			throws CryptoException;

	public KeyStore addKeyPair(KeyStore keyStore, String keyStorePassphrase, KeyPair keyPair, String keyPairName,
			String keyPairPassphrase, String keyPairSubjectDN) throws CryptoException;

	public X509Certificate generateV3Certificate(KeyPair pair, String certSubjectDN) throws CryptoException;

	public byte[] convertKeystoreByteArray(KeyStore keyStore, String keyStorePassphrase) throws CryptoException;

	public int getCertExpiryIntervalInYears();

	public void setCertExpiryIntervalInYears(int certExpiryIntervalInYears);

	public String getCertIssuerDN();

	public void setCertIssuerDN(String certIssuerDN);

	public String getCertIssuerEmail();

	public void setCertIssuerEmail(String certIssuerEmail);

	public int getKeySize();

	public void setKeySize(int keySize);

	public KeyStoreResolver getLocalResolver();

	public void setLocalResolver(KeyStoreResolver localResolver);
	
	public void serializeKeyStore(KeyStore keyStore, String keyStorePassphrase, String filename) throws CryptoException;

}