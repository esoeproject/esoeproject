/* Copyright 2006, Queensland University of Technology
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
 * Creation Date: 24/10/2006
 * 
 * Purpose: Provides an interface to access information from the metadata for the SPEP processor.
 */
package com.qut.middleware.esoe.metadata;

import java.security.PublicKey;
import java.util.List;
import java.util.Map;

import com.qut.middleware.esoe.metadata.exception.InvalidMetadataEndpointException;
import com.qut.middleware.saml2.ExternalKeyResolver;
import com.qut.middleware.saml2.exception.KeyResolutionException;

/** */
public interface Metadata extends ExternalKeyResolver
{
	/** Resolve the assertion consumer service contained in the metadata for the given
	 * endpoint, where an endpoint is the sum of the follwoing params:
	 *  
	 * @param entityID The unique ID of the SPEP service
	 * @param index The index of the endpoint associated with the SPEP service
	 * @return The Location of the assertion consumer service.
	 * 
	 * @throws InvalidMetadataEndpointException if the location cannot be resolved.
	 */
	public String resolveAssertionConsumerService(String entityID, int index) throws InvalidMetadataEndpointException;
	
	/** Resolve identifier types specified for an ACS (of which there may be none)
	 * 
	 * @param entityID The unique ID of the SPEP service
	 * @param index The index of the endpoint associated with the SPEP service
	 * @return Valid identifier types for this service or null if not resolvable
	 */
	public List<String> resolveAssertionConsumerServiceIdentifierTypes(String entityID, int index);
	
	/** Resolve all single logout services contained in the metadata for the given
	 * SPEP service descriptorID.
	 *  
	 * @param entityID The unique ID of the SPEP service
	 * @return List of all appropriate Locations of a single logout service.
	 * 
	 * @throws InvalidMetadataEndpointException if the locations cannot be resolved.
	 */
	public List<String> resolveSingleLogoutService(String entityID) throws InvalidMetadataEndpointException;
	
	/** Resolve all single cache clear services contained in the metadata for the given
	 * SPEP service descriptorID.
	 * 
	 * @param entityID The unique ID of the SPEP service
	 * @return Map of appropriate Locations of a cache clear service, indexed by node index
	 * 
	 * @throws InvalidMetadataEndpointException if the locations cannot be resolved.
	 */
	public Map<Integer,String> resolveCacheClearService(String entityID) throws InvalidMetadataEndpointException;
	
	/** Accessor for current revision.
	 * 
	 * @return Hash code of the current metadata.
	 */
	public String getCurrentRevision();
	
	/** Resolve a public key for the given keyName.
	 * 
	 * @param keyName The globally unique key name.
	 * @return The requested PublicKey object. Null if key isn't in cache.
	 */
	public PublicKey resolveKey(String keyName) throws KeyResolutionException;
	
	/**
	 * @return The ESOE entity identifier string used in metadata
	 */
	public String getEsoeEntityID();
}
