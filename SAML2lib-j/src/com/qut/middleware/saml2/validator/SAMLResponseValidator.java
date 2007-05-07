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
 * Purpose: Validates Response element to SAML 2.0 requirements
 */
package com.qut.middleware.saml2.validator;

import com.qut.middleware.saml2.exception.InvalidSAMLResponseException;
import com.qut.middleware.saml2.schemas.protocol.StatusResponseType;

/** Validates Response element to SAML 2.0 requirements. */
public interface SAMLResponseValidator
{
	/**
	 * Validate a SAML 2.0 response.
	 * 
	 * Any invalid request will throw an exception state, valid documents do not cause any response.
	 * 
	 * @param response An unmarshalled response object
	 * @throws InvalidSAMLResponseException if there is an error validating the response.
	 */
	public void validate(StatusResponseType response) throws InvalidSAMLResponseException;
}
