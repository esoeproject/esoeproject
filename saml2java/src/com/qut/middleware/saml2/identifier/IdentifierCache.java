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
 * Creation Date: 20/10/2006
 * 
 * Purpose: Caches identifiers that have been received so that we can prevent
 * 		replay attacks from happening.
 */
package com.qut.middleware.saml2.identifier;

import com.qut.middleware.saml2.identifier.exception.IdentifierCollisionException;

/** Caches identifiers that have been received so that we can prevent
 * 		replay attacks from happening.*/
public interface IdentifierCache
{
	/**
	 * Registers an identifier as having been used.
	 * 
	 * @param identifier The identifier to add to the cache.
	 * @throws IdentifierCollisionException if the identifier exists in the cache.
	 */
	public void registerIdentifier(String identifier) throws IdentifierCollisionException;
	
	/**
	 * Returns boolean statement determining if the cache holds an identifier or not.
	 * 
	 * @param identifier The identifier to check for in the cache.
	 * @return true / false
	 */
	public boolean containsIdentifier(String identifier);
	
	
	/** Clear any entries from the cache that are older than specified age.
	 *
	 * @param age The age in seconds which an entry remains valid.
	 * @return The number of entries removed from the cache.
	 */
	public int cleanCache(int age);
}
