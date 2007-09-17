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
 * Creation Date: 31/10/2006
 * 
 * Purpose: Stores references to all SAML 2.0 bindings values. 
 * Docuemnt: saml-bindings-2.0-os.pdf, 3.0
 */
package com.qut.middleware.saml2;

/** Stores references to all SAML 2.0 bindings values. */
public class BindingConstants
{
	/**
	 * SOAP is a lightweight protocol intended for exchanging structured information in a decentralized, distributed
	 * environment [SOAP11]. It uses XML technologies to define an extensible messaging framework providing a message
	 * construct that can be exchanged over a variety of underlying protocols. The SAML SOAP binding defines how to use
	 * SOAP to send and receive SAML requests and responses.
	 */
	public static String soap = "urn:oasis:names:tc:SAML:2.0:bindings:SOAP"; //$NON-NLS-1$

	/**
	 * This binding leverages the Reverse HTTP Binding for SOAP specification [PAOS]. Implementers MUST comply with the
	 * general processing rules specified in [PAOS] in addition to those specified in this document. In case of
	 * conflict, [PAOS] is normative.
	 * 
	 */
	public static String paos = "urn:oasis:names:tc:SAML:2.0:bindings:PAOS"; //$NON-NLS-1$

	/**
	 * The HTTP Redirect binding defines a mechanism by which SAML protocol messages can be transmitted within URL
	 * parameters. Permissible URL length is theoretically infinite, but unpredictably limited in practice. Therefore,
	 * specialized encodings are needed to carry XML messages on a URL, and larger or more complex message content can
	 * be sent using the HTTP POST or Artifact bindings. This binding MAY be composed with the HTTP POST binding (see
	 * Section 3.5) and the HTTP Artifact binding (see Section 3.6) to transmit request and response messages in a
	 * single protocol exchange using two different bindings. This binding involves the use of a message encoding. While
	 * the definition of this binding includes the definition of one particular message encoding, others MAY be defined
	 * and used.
	 * 
	 */
	public static String httpRedirect = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect"; //$NON-NLS-1$

	/**
	 * The HTTP POST binding defines a mechanism by which SAML protocol messages may be transmitted within the
	 * base64-encoded content of an HTML form control. This binding MAY be composed with the HTTP Redirect binding (see
	 * Section 3.4) and the HTTP Artifact binding (see Section 3.6) to transmit request and response messages in a
	 * single protocol exchange using two different bindings.
	 */
	public static String httpPost = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"; //$NON-NLS-1$

	/**
	 * In the HTTP Artifact binding, the SAML request, the SAML response, or both are transmitted by reference using a
	 * small stand-in called an artifact. A separate, synchronous binding, such as the SAML SOAP binding, is used to
	 * exchange the artifact for the actual protocol message using the artifact resolution protocol defined in the SAML
	 * assertions and protocols specification [SAMLCore].
	 */
	public static String httpArtifact = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Artifact"; //$NON-NLS-1$

	/**
	 * URIs are a protocol-independent means of referring to a resource. This binding is not a general SAML
	 * request/response binding, but rather supports the encapsulation of a <samlp:AssertionIDRequest> message with a
	 * single <saml:AssertionIDRef> into the resolution of a URI. The result of a successful request is a SAML
	 * <saml:Assertion> element (but not a complete SAML response).
	 */
	public static String uri = "urn:oasis:names:tc:SAML:2.0:bindings:URI"; //$NON-NLS-1$
	
	/**
	 * Encoding method associated HTTP Redirect Binding
	 */
	public static String deflateEncoding = "urn:oasis:names:tc:SAML:2.0:bindings:URL-Encoding:DEFLATE"; //$NON-NLS-1$



}
