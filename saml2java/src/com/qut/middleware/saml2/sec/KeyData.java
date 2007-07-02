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
 * Creation Date: Stores data relating to key information gleaned from metadata, both purpose of the key and the value of the Public Key itself, this was
 * implemented in addition to the JAXB generated KeyDescriptor as it provides much easier access to key data for client applications
 * 
 * Purpose:
 */
package com.qut.middleware.saml2.sec;

import java.security.PublicKey;

import com.qut.middleware.saml2.schemas.metadata.KeyTypes;

/** Stores data relating to key information gleaned from metadata, both purpose of the key and the value of the Public Key itself, this was
 * implemented in addition to the JAXB generated KeyDescriptor as it provides much easier access to key data for client applications.*/
public class KeyData
{
	private KeyTypes use;
	private PublicKey pk;
	
	/**
	 * Generates a key descriptor that stores the purpose of the public key and the public key itself
	 * @param use purpose of this key
	 * @param pk the public key for this descriptor in metadata
	 */
	public KeyData(KeyTypes use, PublicKey pk)
	{
		super();
		this.use = use;
		this.pk = pk;
	}
	
	/**
	 * Generates a key descriptor that stores the purpose of the public key and the public key itself
	 * @param pk the public key for this descriptor in metadata
	 */
	public KeyData(PublicKey pk)
	{
		super();
		this.pk = pk;
	}

	/**
	 * @return The public key
	 */
	public PublicKey getPk()
	{
		return this.pk;
	}

	/**
	 * @return The intended use of this key
	 */
	public KeyTypes getUse()
	{
		return this.use;
	}

}
