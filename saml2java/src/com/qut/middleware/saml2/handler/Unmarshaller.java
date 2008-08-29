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
 * Creation Date: 18/10/2006
 * 
 * Purpose: Interface to all unmarshalling operations supported by saml2lib-j
 */
package com.qut.middleware.saml2.handler;

import java.security.PublicKey;
import java.util.Map;

import org.w3c.dom.Node;

import com.qut.middleware.saml2.exception.ReferenceValueException;
import com.qut.middleware.saml2.exception.SignatureValueException;
import com.qut.middleware.saml2.exception.UnmarshallerException;
import com.qut.middleware.saml2.sec.KeyData;

/** Interface to all unmarshalling operations supported by saml2lib-j.<br>
 * @param <T> The type of object being unmarshalled, usually a JAXB generated type
 */
public interface Unmarshaller<T>
{
	/** The name of the element that will contain a signature to be verified */
	public static final String SIGNATURE_ELEMENT = "Signature"; //$NON-NLS-1$
	
	/**
	 * Validates document against schema, validates xml signatures and Unmarshalls supplied document to JAXB compiled object instantiated as T.
	 * Utilised where public key information is extracted from submitted document
	 * 
	 * @param document The document to unmarshall
	 * @return The JAXB unmarshalled object or null if an error occurs.
	 * @throws SignatureValueException if the signature cannot be decoded.
	 * @throws ReferenceValueException if the reference cannot be decoded.
	 * @throws UnmarshallerException if an error occurs unmarshalling the document.
	 */
	public T unMarshallSigned( byte[] document ) throws SignatureValueException, ReferenceValueException, UnmarshallerException;
	
	/**
	 * Validates document against schema, validates xml signatures and Unmarshalls supplied document to JAXB compiled object instantiated as T.
	 * Utilised where public key to validate signature is explicity supplied
	 * 
	 * @param pk The public key of the entity generating the document to validate signatures
	 * @param document The document to unmarshall
	 * @return The JAXB unmarshalled object or null if an error occurs.
	 * @throws SignatureValueException if the signature cannot be decoded.
	 * @throws ReferenceValueException if the reference cannot be decoded.
	 * @throws UnmarshallerException if an error occurs unmarshalling the document.
	 */
	public T unMarshallSigned( PublicKey pk, byte[] document ) throws SignatureValueException, ReferenceValueException, UnmarshallerException;
	
	/**
	 * Validates document against schema and Unmarshalls supplied document to JAXB compiled object instatiated as T.
	 * 
	 * @param document The document to unmarshall
	 * @return The JAXB unmarshalled object or null if an error occurs.
	 * @throws UnmarshallerException if an error occurs unmarshalling the document.
	 */
	public T unMarshallUnSigned( byte[] document ) throws UnmarshallerException;
	
	/**
	 * Validates node against schema and Unmarshalls supplied node to JAXB compiled object instantiated as T.
	 * 
	 * @param node The node to unmarshall
	 * @return The JAXB unmarshalled object or null if an error occurs.
	 * @throws UnmarshallerException if an error occurs unmarshalling the document.
	 */
	public T unMarshallUnSigned( Node node ) throws UnmarshallerException;
	
	/**
	 * Validates SAML metadata document against schema and Unmarshalls supplied document to JAXB compiled object instantiated as T. Additionally determines
	 * all public keys stored in document and returns to caller for future use.
	 * 
	 * @param pk The public key to use when verifying the signature
	 * @param document The document to unmarshall
	 * @param keyList An initiated but empty Map<String, PublicKey> with which to fill with key values
	 * @return The JAXB unmarshalled metadata object or null if an error occurs.
	 * @throws SignatureValueException if the signature cannot be decoded.
	 * @throws ReferenceValueException if the reference cannot be decoded.
	 * @throws UnmarshallerException if an error occurs unmarshalling the document.
	 */
	public T unMarshallMetadata( PublicKey pk, byte[] document, Map<String, KeyData> keyList ) throws SignatureValueException, ReferenceValueException, UnmarshallerException;
	
	/**
	 * Validates SAML metadata document against schema and Unmarshalls supplied document to JAXB compiled object instantiated as T. Additionally determines
	 * all public keys stored in document and returns to caller for future use.
	 * Utilised where public key information for validating the metadata signature is extracted from a supplied key resolver.
	 * 
	 * @param document The document to unmarshall
	 * @param keyList An initiated but empty Map<String, PublicKey> with which to fill with key values
	 * @return The JAXB unmarshalled metadata object or null if an error occurs.
	 * @throws SignatureValueException if the signature cannot be decoded.
	 * @throws ReferenceValueException if the reference cannot be decoded.
	 * @throws UnmarshallerException if an error occurs unmarshalling the document.
	 */
	public T unMarshallMetadata( byte[] document, Map<String, KeyData> keyList, boolean signed ) throws SignatureValueException, ReferenceValueException, UnmarshallerException;
}
