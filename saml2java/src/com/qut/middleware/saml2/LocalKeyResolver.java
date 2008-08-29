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
 * Creation Date: 14/07/2008
 * 
 * Purpose: Resolves keys from a local source
 */

package com.qut.middleware.saml2;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

public interface LocalKeyResolver
{
	/**
	 * @return The private key associated with the local entity, or null if
	 * 		this resolver is not associated with a local key.
	 */
	public PrivateKey getLocalPrivateKey();
	
	/**
	 * @return The public key associated with the local entity, or null if
	 * 		this resolver is not associated with a local key.
	 */
	public PublicKey getLocalPublicKey();

	/**
	 * @return The public key associated with the local entity, or null if
	 * 		this resolver is not associated with a local key.
	 */
	public Certificate getLocalCertificate();

	/**
	 * @return The alias of the local entity's key pair, or null if
	 * 		this resolver is not associated with a local key.
	 */
	public String getLocalKeyAlias();
}
