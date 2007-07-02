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
 * Creation Date: 30/10/2006
 * 
 * Purpose: Stores references to all SAML 2.0 confirmation method identifiers values
 * Docuemnt: saml-profiles-2.0-os.pdf, 3.0
 */
package com.qut.middleware.saml2;

/** Stores references to all SAML 2.0 confirmation method identifiers values.*/
public class ConfirmationMethodConstants
{
	/**
	 * One or more <ds:KeyInfo> elements MUST be present within the <SubjectConfirmationData> element. An xsi:type
	 * attribute MAY be present in the <SubjectConfirmationData> element and, if present, MUST be set to
	 * saml:KeyInfoConfirmationDataType (the namespace prefix is arbitrary but must reference the SAML assertion
	 * namespace).
	 */
	public static final String holderOfKey = "urn:oasis:names:tc:SAML:2.0:cm:holder-of-key"; //$NON-NLS-1$

	/**
	 * Indicates that no other information is available about the context of use of the assertion. The relying party
	 * SHOULD utilize other means to determine if it should process the assertion further, subject to optional
	 * constraints on confirmation using the attributes that MAY be present in the <SubjectConfirmationData> element, as
	 * defined by [SAMLCore].
	 */
	public static final String senderVouches = "urn:oasis:names:tc:SAML:2.0:cm:sender-vouches"; //$NON-NLS-1$

	/**
	 * The subject of the assertion is the bearer of the assertion, subject to optional constraints on confirmation
	 * using the attributes that MAY be present in the <SubjectConfirmationData> element, as defined by [SAMLCore].
	 */
	public static final String bearer = "urn:oasis:names:tc:SAML:2.0:cm:bearer"; //$NON-NLS-1$
}
