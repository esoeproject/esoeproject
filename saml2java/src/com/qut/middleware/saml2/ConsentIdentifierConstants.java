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
 * Purpose: Stores references to all SAML 2.0 consent identifiers values
 * Docuemnt: saml-core-2.0-os.pdf, 8.4
 */
package com.qut.middleware.saml2;

/** Stores references to all SAML 2.0 consent identifiers values. */
public class ConsentIdentifierConstants
{
	/*
	 * The following identifiers MAY be used in the Consent attribute defined on the RequestAbstractType and
	 * StatusResponseType complex types to communicate whether a principal gave consent, and under what conditions, for
	 * the message.
	 */

	/** No claim as to principal consent is being made. */
	public static final String unspecified = "urn:oasis:names:tc:SAML:2.0:consent:unspecified"; //$NON-NLS-1$

	/** Indicates that a principal’s consent has been obtained by the issuer of the message. */
	public static final String obtained = "urn:oasis:names:tc:SAML:2.0:consent:obtained"; //$NON-NLS-1$

	/**
	 * Indicates that a principal’s consent has been obtained by the issuer of the message at some point prior to the
	 * action that initiated the message.
	 */
	public static final String prior = "urn:oasis:names:tc:SAML:2.0:consent:prior"; //$NON-NLS-1$

	/**
	 * Indicates that a principal's consent has been implicitly obtained by the issuer of the message during the action
	 * that initiated the message, as part of a broader indication of consent. Implicit consent is typically more
	 * proximal to the action in time and presentation than prior consent, such as part of a session of activities.
	 */
	public static final String currentImplicit = "urn:oasis:names:tc:SAML:2.0:consent:current-implicit"; //$NON-NLS-1$

}
