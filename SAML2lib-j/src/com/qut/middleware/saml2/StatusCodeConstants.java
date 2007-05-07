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
 * Purpose: Stores references to all SAML 2.0 status code values
 * Docuemnt: saml-core-2.0-os.pdf, 3.2.2.2
 */
package com.qut.middleware.saml2;

/** Stores references to all SAML 2.0 status code values. */
public class StatusCodeConstants
{
	/** Permissible top-level <StatusCode> values * */

	/**
	 * The request succeeded. Additional information MAY be returned in the <StatusMessage> and/or <StatusDetail>
	 * elements.
	 */
	public static final String success = "urn:oasis:names:tc:SAML:2.0:status:Success"; //$NON-NLS-1$

	/** The request could not be performed due to an error on the part of the requester */
	public static final String requester = "urn:oasis:names:tc:SAML:2.0:status:Requester"; //$NON-NLS-1$

	/** The request could not be performed due to an error on the part of the responder or SAML authority */
	public static final String responder = "urn:oasis:names:tc:SAML:2.0:status:Responder"; //$NON-NLS-1$

	/** The SAML responder could not process the request because the version of the request message was incorrect. */
	public static final String versionMismatch = "urn:oasis:names:tc:SAML:2.0:status:VersionMismatch"; //$NON-NLS-1$

	/* Permissible second-level <StatusCode> values * */
	/* TODO: Ensure the esoe is not using these as top level reasons */

	/** The responding provider was unable to successfully authenticate the principal. */
	public static final String authnFailed = "urn:oasis:names:tc:SAML:2.0:status:AuthnFailed"; //$NON-NLS-1$

	/**
	 * Unexpected or invalid content was encountered within a <saml:Attribute> or <saml:AttributeValue> element.
	 */
	public static final String invalidAttr = "urn:oasis:names:tc:SAML:2.0:status:InvalidAttrNameOrValue"; //$NON-NLS-1$

	/** The responding provider cannot or will not support the requested name identifier policy. */
	public static final String invalidNameIDPolicy = "urn:oasis:names:tc:SAML:2.0:status:InvalidNameIDPolicy"; //$NON-NLS-1$

	/** The specified authentication context requirements cannot be met by the responder. */
	public static final String noAuthnContent = "urn:oasis:names:tc:SAML:2.0:status:NoAuthnContext"; //$NON-NLS-1$

	/**
	 * Used by an intermediary to indicate that none of the supported identity provider <Loc> elements in an <IDPList>
	 * can be resolved or that none of the supported identity providers are available.
	 */
	public static final String noAvailableIDP = "urn:oasis:names:tc:SAML:2.0:status:NoAvailableIDP"; //$NON-NLS-1$

	/** Indicates the responding provider cannot authenticate the principal passively, as has been requested. */
	public static final String noPassive = "urn:oasis:names:tc:SAML:2.0:status:NoPassive"; //$NON-NLS-1$

	/**
	 * Used by an intermediary to indicate that none of the identity providers in an <IDPList> are supported by the
	 * intermediary.
	 */
	public static final String noSupportedIDP = "urn:oasis:names:tc:SAML:2.0:status:NoSupportedIDP"; //$NON-NLS-1$

	/**
	 * Used by a session authority to indicate to a session participant that it was not able to propagate logout to all
	 * other session participants.
	 */
	public static final String partialLogout = "urn:oasis:names:tc:SAML:2.0:status:PartialLogout"; //$NON-NLS-1$

	/**
	 * Indicates that a responding provider cannot authenticate the principal directly and is not permitted to proxy the
	 * request further.
	 */
	public static final String proxyCountExceeded = "urn:oasis:names:tc:SAML:2.0:status:ProxyCountExceeded"; //$NON-NLS-1$

	/**
	 * The SAML responder or SAML authority is able to process the request but has chosen not to respond. This status
	 * code MAY be used when there is concern about the security context of the request message or the sequence of
	 * request messages received from a particular requester.
	 */
	public static final String requestDenied = "urn:oasis:names:tc:SAML:2.0:status:RequestDenied"; //$NON-NLS-1$

	/** The SAML responder or SAML authority does not support the request. */
	public static final String requestUnsupported = "urn:oasis:names:tc:SAML:2.0:status:RequestUnsupported"; //$NON-NLS-1$

	/** The SAML responder cannot process any requests with the protocol version specified in the request. */
	public static final String requestVersionDeprecated = "urn:oasis:names:tc:SAML:2.0:status:RequestVersionDeprecated"; //$NON-NLS-1$

	/**
	 * The SAML responder cannot process the request because the protocol version specified in the request message is a
	 * major upgrade from the highest protocol version supported by the responder.
	 */
	public static final String requestVersionTooHigh = "urn:oasis:names:tc:SAML:2.0:status:RequestVersionTooHigh"; //$NON-NLS-1$

	/**
	 * The SAML responder cannot process the request because the protocol version specified in the request message is
	 * too low.
	 */
	public static final String requestVersionTooLow = "urn:oasis:names:tc:SAML:2.0:status:RequestVersionTooLow"; //$NON-NLS-1$

	/** The resource value provided in the request message is invalid or unrecognized. */
	public static final String resourceNotRecognised = "urn:oasis:names:tc:SAML:2.0:status:ResourceNotRecognized"; //$NON-NLS-1$

	/** The response message would contain more elements than the SAML responder is able to return. */
	public static final String tooManyResponses = "urn:oasis:names:tc:SAML:2.0:status:TooManyResponses"; //$NON-NLS-1$

	/**
	 * An entity that has no knowledge of a particular attribute profile has been presented with an attribute drawn from
	 * that profile.
	 */
	public static final String unknownAttrProfile = "urn:oasis:names:tc:SAML:2.0:status:UnknownAttrProfile"; //$NON-NLS-1$

	/** The responding provider does not recognize the principal specified or implied by the request. */
	public static final String unknownPrincipal = "urn:oasis:names:tc:SAML:2.0:status:UnknownPrincipal"; //$NON-NLS-1$

	/**
	 * The SAML responder cannot properly fulfill the request using the protocol binding specified in the request.
	 */
	public static final String unsupportedBinding = "urn:oasis:names:tc:SAML:2.0:status:UnsupportedBinding"; //$NON-NLS-1$

}
