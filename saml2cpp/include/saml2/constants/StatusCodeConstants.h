/* 
 * Copyright 2006-2007, Queensland University of Technology
 * Licensed under the Apache License, Version 2.0 (the= L"License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy of 
 * the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an= L"AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations under 
 * the License.
 * 
 * Author: Bradley Beddoes
 * Creation Date: 14/02/2007
 * 
 * Purpose: Stores references to all SAML 2.0 status code values
 * Docuemnt: saml-core-2.0-os.pdf, 3.2.2.2
 */
 
#ifndef STATUSCODECONSTANTS_H_
#define STATUSCODECONSTANTS_H_
 
 #include "saml2/SAML2Defs.h"
 
 /* STL */
 #include <string>
 
 namespace saml2
 {
 	namespace statuscode
 	{
		/** Permissible top-level <StatusCode> values * */
	
		/**
		 * The request succeeded. Additional information MAY be returned in the <StatusMessage> and/or <StatusDetail>
		 * elements.
		 */
 		SAML2CONSTANT const std::wstring SUCCESS= L"urn:oasis:names:tc:SAML:2.0:status:Success";
	
		/** The request could not be performed due to an error on the part of the requester */
 		SAML2CONSTANT const std::wstring REQUESTOR= L"urn:oasis:names:tc:SAML:2.0:status:Requester";
	
		/** The request could not be performed due to an error on the part of the responder or SAML authority */
 		SAML2CONSTANT const std::wstring RESPONDER= L"urn:oasis:names:tc:SAML:2.0:status:Responder";
	
		/** The SAML responder could not process the request because the version of the request message was incorrect. */
 		SAML2CONSTANT const std::wstring VERSION_MISMATCH= L"urn:oasis:names:tc:SAML:2.0:status:VersionMismatch";
	
		/* Permissible second-level <StatusCode> values * */
		/* TODO: Ensure the esoe is not using these as top level reasons */
	
		/** The responding provider was unable to successfully authenticate the principal. */
 		SAML2CONSTANT const std::wstring AUTHN_FAILED= L"urn:oasis:names:tc:SAML:2.0:status:AuthnFailed";
	
		/**
		 * Unexpected or invalid content was encountered within a <saml:Attribute> or <saml:AttributeValue> element.
		 */
 		SAML2CONSTANT const std::wstring INVALID_ATTR= L"urn:oasis:names:tc:SAML:2.0:status:InvalidAttrNameOrValue";
	
		/** The responding provider cannot or will not support the requested name identifier policy. */
 		SAML2CONSTANT const std::wstring INVALID_NAMEID_POLICY= L"urn:oasis:names:tc:SAML:2.0:status:InvalidNameIDPolicy";
	
		/** The specified authentication context requirements cannot be met by the responder. */
 		SAML2CONSTANT const std::wstring NO_AUTHN_CONTENT= L"urn:oasis:names:tc:SAML:2.0:status:NoAuthnContext";
	
		/**
		 * Used by an intermediary to indicate that none of the supported identity provider <Loc> elements in an <IDPList>
		 * can be resolved or that none of the supported identity providers are available.
		 */
 		SAML2CONSTANT const std::wstring NO_AVAILABLE_IDP= L"urn:oasis:names:tc:SAML:2.0:status:NoAvailableIDP";
	
		/** Indicates the responding provider cannot authenticate the principal passively, as has been requested. */
 		SAML2CONSTANT const std::wstring NO_PASSIVE= L"urn:oasis:names:tc:SAML:2.0:status:NoPassive";
	
		/**
		 * Used by an intermediary to indicate that none of the identity providers in an <IDPList> are supported by the
		 * intermediary.
		 */
 		SAML2CONSTANT const std::wstring NO_SUPPORTED_IDP= L"urn:oasis:names:tc:SAML:2.0:status:NoSupportedIDP";
	
		/**
		 * Used by a session authority to indicate to a session participant that it was not able to propagate logout to all
		 * other session participants.
		 */
 		SAML2CONSTANT const std::wstring PARTIAL_LOGOUT= L"urn:oasis:names:tc:SAML:2.0:status:PartialLogout";
	
		/**
		 * Indicates that a responding provider cannot authenticate the principal directly and is not permitted to proxy the
		 * request further.
		 */
 		SAML2CONSTANT const std::wstring PROXY_COUNT_EXCEEDED= L"urn:oasis:names:tc:SAML:2.0:status:ProxyCountExceeded";
	
		/**
		 * The SAML responder or SAML authority is able to process the request but has chosen not to respond. This status
		 * code MAY be used when there is concern about the security context of the request message or the sequence of
		 * request messages received from a particular requester.
		 */
 		SAML2CONSTANT const std::wstring REQUEST_DENIED= L"urn:oasis:names:tc:SAML:2.0:status:RequestDenied";
	
		/** The SAML responder or SAML authority does not support the request. */
 		SAML2CONSTANT const std::wstring REQUEST_UNSUPPORTED= L"urn:oasis:names:tc:SAML:2.0:status:RequestUnsupported";
	
		/** The SAML responder cannot process any requests with the protocol version specified in the request. */
 		SAML2CONSTANT const std::wstring REQUEST_VERSION_DEPRECATED= L"urn:oasis:names:tc:SAML:2.0:status:RequestVersionDeprecated";
	
		/**
		 * The SAML responder cannot process the request because the protocol version specified in the request message is a
		 * major upgrade from the highest protocol version supported by the responder.
		 */
 		SAML2CONSTANT const std::wstring REQUEST_VERSION_TOO_HIGH= L"urn:oasis:names:tc:SAML:2.0:status:RequestVersionTooHigh";
	
		/**
		 * The SAML responder cannot process the request because the protocol version specified in the request message is
		 * too low.
		 */
 		SAML2CONSTANT const std::wstring REQUEST_VERSION_TOO_LOW= L"urn:oasis:names:tc:SAML:2.0:status:RequestVersionTooLow";
	
		/** The resource value provided in the request message is invalid or unrecognized. */
 		SAML2CONSTANT const std::wstring RESOURCE_NOT_RECOGNISED= L"urn:oasis:names:tc:SAML:2.0:status:ResourceNotRecognized";
	
		/** The response message would contain more elements than the SAML responder is able to return. */
 		SAML2CONSTANT const std::wstring TOO_MANY_RESPONSES= L"urn:oasis:names:tc:SAML:2.0:status:TooManyResponses";
	
		/**
		 * An entity that has no knowledge of a particular attribute profile has been presented with an attribute drawn from
		 * that profile.
		 */
 		SAML2CONSTANT const std::wstring UNKNOWN_ATTR_PROFILE= L"urn:oasis:names:tc:SAML:2.0:status:UnknownAttrProfile";
	
		/** The responding provider does not recognize the principal specified or implied by the request. */
 		SAML2CONSTANT const std::wstring UNKNOWN_PRINCIPAL= L"urn:oasis:names:tc:SAML:2.0:status:UnknownPrincipal";
	
		/**
		 * The SAML responder cannot properly fulfill the request using the protocol binding specified in the request.
		 */
 		SAML2CONSTANT const std::wstring UNSUPPORTED_BINDING= L"urn:oasis:names:tc:SAML:2.0:status:UnsupportedBinding";
 	}
 }
 
 #endif
