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
 * Purpose: Validates Request element to SAML 2.0 requirements
 */
package com.qut.middleware.saml2.validator;

import com.qut.middleware.saml2.exception.InvalidSAMLRequestException;
import com.qut.middleware.saml2.schemas.protocol.RequestAbstractType;

/** Validates Request element to SAML 2.0 requirements. */
public interface SAMLRequestValidator
{
	/**
	 * Validate a SAML 2.0 request. 
	 * 
	 * Any invalid request will throw an exception state, valid documents do not cause any response
	 * (NB: XML Cryptography verification with unmarshaller/marshaller is expected to have been utilised before calling this function)
	 * 
	 * @param request An unmarshalled request object
	 * @throws InvalidSAMLRequestException if there is an error validating the request.
	 */
	public void validate(RequestAbstractType request) throws InvalidSAMLRequestException;
}
