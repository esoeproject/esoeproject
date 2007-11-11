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
 * Purpose: Stores references to all SAML 2.0 name format values. 
 * Docuemnt: saml-core-2.0-os.pdf, 3.4.1.1 and 8.3
 */
 
#ifndef NAMEIDFORMATCONSTANTS_H_
#define NAMEIDFORMATCONSTANTS_H_
 
 #include "saml2/SAML2Defs.h"
 
/* STL */
 #include <string>
 
 namespace saml2
 {
 	namespace nameidformat
 	{
		/*
		 * The following identifiers MAY be used in the Format attribute of the <NameID>, <NameIDPolicy>, or <Issuer>
		 * elements (see Section 2.2) to refer to common formats for the content of the elements and the associated
		 * processing rules, if any. Note: Several identifiers that were deprecated in SAML V1.1 have been removed for SAML
		 * V2.0.
		 */
	
		/**
		 * The special Format value urn:oasis:names:tc:SAML:2.0:nameid-format:encrypted indicates that the resulting
		 * assertion(s) MUST contain <EncryptedID> elements instead of plaintext. The underlying name identifier's
		 * unencrypted form can be of any type supported by the identity provider for the requested subject.
		 */
 		SAML2CONSTANT const std::wstring ENCRYPTED= L"urn:oasis:names:tc:SAML:2.0:nameid-format:encrypted";
	
		/** The interpretation of the content of the element is left to individual implementations. */
 		SAML2CONSTANT const std::wstring UNSPECIFIED= L"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified";
	
		/**
		 * Indicates that the content of the element is in the form of an email address, specifically= L"addr-spec" as defined
		 * in IETF RFC 2822 [RFC 2822] Section 3.4.1. An addr-spec has the form local-part@domain. Note that an addr-spec
		 * has no phrase (such as a common name) before it, has no comment (text surrounded in parentheses) after it, and is
		 * not surrounded by= L"<" and= L">".
		 */
 		SAML2CONSTANT const std::wstring EMAIL_ADDRESS= L"urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
	
		/**
		 * Indicates that the content of the element is in the form specified for the contents of the <ds:X509SubjectName>
		 * element in the XML Signature Recommendation [XMLSig]. Implementors should note that the XML Signature
		 * specification specifies encoding rules for X.509 subject names that differ from the rules given in IETF RFC 2253
		 * [RFC 2253].
		 */
 		SAML2CONSTANT const std::wstring X509_SUBJECT_NAME= L"urn:oasis:names:tc:SAML:1.1:nameid-format:X509SubjectName";
	
		/**
		 * Indicates that the content of the element is a Windows domain qualified name. A Windows domain qualified user
		 * name is a string of the form= L"DomainName\UserName". The domain name and= L"\" separator MAY be omitted.
		 */
 		SAML2CONSTANT const std::wstring WINDOWS_DOMAIN_QUAL_NAME= L"urn:oasis:names:tc:SAML:1.1:nameid-format:WindowsDomainQualifiedName";
	
		/**
		 * Indicates that the content of the element is in the form of a Kerberos principal name using the format
		 * name[/instance]@REALM. The syntax, format and characters allowed for the name, instance, and realm are described
		 * in IETF RFC 1510 [RFC 1510].
		 */
 		SAML2CONSTANT const std::wstring KERBEROS= L"urn:oasis:names:tc:SAML:2.0:nameid-format:kerberos";
	
		/**
		 * Indicates that the content of the element is the identifier of an entity that provides SAML-based services (such
		 * as a SAML authority, requester, or responder) or is a participant in SAML profiles (such as a service provider
		 * supporting the browser SSO profile). Such an identifier can be used in the <Issuer> element to identify the
		 * issuer of a SAML request, response, or assertion, or within the <NameID> element to make assertions about system
		 * entities that can issue SAML requests, responses, and assertions. It can also be used in other elements and
		 * attributes whose purpose is to identify a system entity in various protocol exchanges. The syntax of such an
		 * identifier is a URI of not more than 1024 characters in length. It is RECOMMENDED that a system entity use a URL
		 * containing its own domain name to identify itself. The NameQualifier, SPNameQualifier, and SPProvidedID
		 * attributes MUST be omitted.
		 */
 		SAML2CONSTANT const std::wstring ENTITY= L"urn:oasis:names:tc:SAML:2.0:nameid-format:entity";
	
		/**
		 * Indicates that the content of the element is a persistent opaque identifier for a principal that is specific to
		 * an identity provider and a service provider or affiliation of service providers. Persistent name identifiers
		 */
 		SAML2CONSTANT const std::wstring PERSISTENT= L"urn:oasis:names:tc:SAML:2.0:nameid-format:persistent";
	
		/**
		 * Indicates that the content of the element is an identifier with transient semantics and SHOULD be treated as an
		 * opaque and temporary value by the relying party. Transient identifier values MUST be generated in accordance with
		 * the rules for SAML identifiers (see Section 1.3.4), and MUST NOT exceed a length of 256 characters.
		 */
 		SAML2CONSTANT const std::wstring TRANS= L"urn:oasis:names:tc:SAML:2.0:nameid-format:transient";
 	}
 }
 
 #endif
