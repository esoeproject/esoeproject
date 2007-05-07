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
 * Creation Date: 09/11/2006
 * 
 * Purpose: Interface for the metadata component of the SPEP. This class keeps an
 * 		actively updated local copy of the metadata and resolves required information.
 */
package com.qut.middleware.spep.metadata;

import com.qut.middleware.saml2.ExternalKeyResolver;

/** Interface for the metadata component of the SPEP. This class keeps an
 * 		actively updated local copy of the metadata and resolves required information.*/
public interface Metadata extends ExternalKeyResolver
{
	/**
	 * @return The identifier of the SPEP in the metadata
	 */
	public String getSPEPIdentifier();
	
	/**
	 * @return A single sign-on endpoint on the ESOE.
	 */
	public String getSingleSignOnEndpoint();
	
	/**
	 * @return A single logout endpoint on the ESOE.
	 */
	public String getSingleLogoutEndpoint();
	
	/**
	 * @return An attribute service endpoint on the ESOE.
	 */
	public String getAttributeServiceEndpoint();
	
	/**
	 * @return An authorization service endpoint on the ESOE.
	 */
	public String getAuthzServiceEndpoint();
	
	/**
	 * @return The identifier of the ESOE in the metadata.
	 */
	public String getESOEIdentifier();

	/**
	 * @return The SPEP startup service endpoint on the ESOE.
	 */
	public String getSPEPStartupServiceEndpoint();
	
	
	/** The Location of the assertionConsumer service provided by this SPEP node. This data is used
	 * to verify the destination field of Requests/Responses to ensure that they are destined
	 * for this node.
	 * 
	 * @return The service URL for this SPEP node.
	 */
	public String getSPEPAssertionConsumerLocation();
	
}
