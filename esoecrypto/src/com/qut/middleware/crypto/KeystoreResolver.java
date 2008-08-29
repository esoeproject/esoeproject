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

package com.qut.middleware.crypto;

import java.math.BigInteger;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Map;

import com.qut.middleware.crypto.exception.KeystoreResolverException;
import com.qut.middleware.saml2.ExternalKeyResolver;
import com.qut.middleware.saml2.LocalKeyResolver;

public interface KeystoreResolver extends ExternalKeyResolver, LocalKeyResolver
{
	/* (non-Javadoc)
	 * @see com.qut.middleware.saml2.ExternalKeyResolver#resolveKey(java.lang.String)
	 */
	public PublicKey resolveKey(String alias);
	
	/**
	 * @param alias The alias of the certificate to resolve
	 * @return The Certificate, or null if it could not be resolved
	 */
	public Certificate resolveCertificate(String alias);
	
	/**
	 * @param issuerDN The issuer DN of the certificate to resolve
	 * @param serialNumber The serial number of the certificate to resolve
	 * @return The certificate, or null if it could not be resolved
	 */
	public Certificate resolveCertificate(String issuerDN, BigInteger serialNumber);
	
	/**
	 * Checks for any updates to the source this keystore was loaded from, 
	 * and updates the cached data if necessary.
	 */
	public void reload() throws KeystoreResolverException;
	
	/**
	 * @return An immutable map of certificates cached in this resolver.
	 */
	public Map<String, Certificate> getCertificates();
}
