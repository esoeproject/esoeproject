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
 * Creation Date: 29/05/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.metadata.bean;

import java.math.BigInteger;
import java.security.PublicKey;

public interface KeyEntry
{
	/**
	 * @return The alias of this key entry.
	 */
	public String getKeyAlias();
	
	/**
	 * @return The entity ID that this key entry belongs to.
	 */
	public String getOwnerEntityID();
	
	/**
	 * @return The public key associated with this key entry.
	 */
	public PublicKey getPublicKey();
	
	/**
	 * @return The priority given to this key entry.
	 */
	public int getPriority();
	
	/**
	 * @return The metadata source this key was obtained from.
	 */
	public String getMetadataSourceLocation();
	
	/**
	 * @return The Issuer DN on the certificate for this public key.
	 */
	public String getIssuerDN();
	
	/**
	 * @return The serial number on the certificate for this public key.
	 */
	public BigInteger getSerialNumber();
}
