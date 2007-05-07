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
 * Creation Date: 24/11/2006
 * 
 * Purpose: Resolves keys from a keystore
 */
package com.qut.middleware.esoemanager.crypto;

import java.security.cert.Certificate;
import java.security.PrivateKey;
import java.security.PublicKey;

import com.qut.middleware.saml2.ExternalKeyResolver;

/** Resolves keys from a keystore. */
public interface KeyStoreResolver extends ExternalKeyResolver
{
	/* (non-Javadoc)
	 * @see com.qut.middleware.saml2.ExternalKeyResolver#resolveKey(java.lang.String)
	 */
	public PublicKey resolveKey(String alias);
	
	/** Get the private key stored in a keystore.
	 * 
	 * @return The private key
	 */
	public PrivateKey getPrivateKey();
	
	/** Get the public key stored in a keystore.
	 * 
	 * @return The public key
	 */
	public PublicKey getPublicKey();

	/** Get the alias of the private key stored in a keystore.
	 * 
	 * @return The key name
	 */
	public String getKeyAlias();
	
	/** Get the certificate corresponding to the given alias in a keystore.
	 * 
	 * @param alias The alias of the certificate to resolve
	 * @return The Certificate, or null if it couldn't be resolved
	 */
	public Certificate resolveCertificate(String alias);
}
