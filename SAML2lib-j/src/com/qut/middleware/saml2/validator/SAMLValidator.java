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
 * Creation Date: 27/10/2006
 * 
 * Purpose: Validates components of SAML documents
 */
package com.qut.middleware.saml2.validator;

/** Validates components of SAML documents. */
public interface SAMLValidator
{
	/**
	 * @return The SAML request validator instance.
	 */
	public SAMLRequestValidator getRequestValidator();
	/**
	 * @return The SAML authn request validator instance.
	 */
	public SAMLAuthnRequestValidator getAuthnRequestValidator();
	/**
	 * @return The SAML response validator instance.
	 */
	public SAMLResponseValidator getResponseValidator();
	/**
	 * @return The SAML assertion validator instance.
	 */
	public SAMLAssertionValidator getAssertionValidator();
}
