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
 * Creation Date: 17/10/2006
 * 
 * Purpose: Interface to all marshalling operations supported by saml2lib-j
 */
package com.qut.middleware.saml2.handler;

import org.w3c.dom.Element;

import com.qut.middleware.saml2.exception.MarshallerException;

/** Interface to all marshalling operations supported by saml2lib-j.<br>
 * @param <T> The type of object being marshalled, usually a JAXB generated class.  
 */
public interface Marshaller<T>
{
	/**
	 * Marshalls content that requires enveloped XML signature creation
	 * 
	 * This marshaller will validate the supplied object to schema, the final document will include enveloped digital signatures for the elemets listed
	 * 
	 * @param xmlObj JAXB object of type to be marshalled, MUST contain an empty <ds:Signature/> element
	 * @return Valid  XML document if no errors occur.
	 * @throws MarshallerException if an error occurs marshalling the document.
	 */
	public byte[] marshallSigned( T xmlObj )  throws MarshallerException;
	
	/**
	 * Marshalls content that does not require any signing in the final generated XML document.
	 * 
	 * This marshaller does not undertake any validation of schema, it is assumed that the supplied object for xmlObj has been supplied with all
	 * required data for generating a valid XML document. 
	 * 
	 * @param xmlObj JAXB created implementation of some defined schema object
	 * @return A marshalled XML document if no errors occur.
	 * @throws MarshallerException  if an error occurs marshalling the document.
	 */
	public byte[] marshallUnSigned( T xmlObj )  throws MarshallerException;
	
	/**
	 * Marshalls content that does not require any signing in the final generated XML node.
	 * 
	 * This marshaller does not undertake any validation of schema, it is assumed that the supplied object for xmlObj has been supplied with all
	 * required data for generating a valid XML element. 
	 * 
	 * @param xmlObj JAXB created implementation of some defined schema object
	 * @return A marshalled  XML document if no errors occur.
	 * @throws MarshallerException if an error occurs marshalling the document.
	 */
	public Element marshallUnSignedElement( T xmlObj )  throws MarshallerException;
	
	/**
	 * Marshalls content that requires enveloped XML signature creation and requires a certain type of charset encoding on this call
	 * 
	 * This marshaller will validate the supplied object to schema, the final document will include enveloped digital signatures for the elemets listed
	 * 
	 * @param xmlObj JAXB object of type to be marshalled, MUST contain an empty <ds:Signature/> element
	 * @return Valid  XML document if no errors occur.
	 * @throws MarshallerException if an error occurs marshalling the document.
	 */
	public byte[] marshallSigned( T xmlObj, String encoding )  throws MarshallerException;
	
	/**
	 * Marshalls content that does not require any signing in the final generated XML document and requires a certain type of charset encoding on this call.
	 * 
	 * This marshaller does not undertake any validation of schema, it is assumed that the supplied object for xmlObj has been supplied with all
	 * required data for generating a valid XML document. 
	 * 
	 * @param xmlObj JAXB created implementation of some defined schema object
	 * @return A marshalled XML document if no errors occur.
	 * @throws MarshallerException  if an error occurs marshalling the document.
	 */
	public byte[] marshallUnSigned( T xmlObj, String encoding )  throws MarshallerException;
	
	/**
	 * Marshalls content that does not require any signing in the final generated XML node and requires a certain type of charset encoding on this call.
	 * 
	 * This marshaller does not undertake any validation of schema, it is assumed that the supplied object for xmlObj has been supplied with all
	 * required data for generating a valid XML element. 
	 * 
	 * @param xmlObj JAXB created implementation of some defined schema object
	 * @return A marshalled  XML document if no errors occur.
	 * @throws MarshallerException if an error occurs marshalling the document.
	 */
	public Element marshallUnSignedElement( T xmlObj, String encoding )  throws MarshallerException;
	
	/**
	 * Marshalls content that requires enveloped XML signature creation.
	 * 
	 * This marshaller will validate the supplied object to schema, the final document will include enveloped digital signatures for the elements listed
	 * 
	 * @param xmlObj JAXB created implementation of some defined schema object
	 * @return An XML DOM object if no errors occur.
	 * @throws MarshallerException if an error occurs marshalling the document.
	 */
	public Element marshallSignedElement(T xmlObj) throws MarshallerException;
	
	/**
	 * Marshalls content that requires enveloped XML signature creation and requires a certain type of charset encoding on this call.
	 * 
	 * This marshaller will validate the supplied object to schema, the final document will include enveloped digital signatures for the elements listed
	 * 
	 * @param xmlObj JAXB created implementation of some defined schema object
	 * @return An XML DOM object if no errors occur.
	 * @throws MarshallerException if an error occurs marshalling the document.
	 */
	public Element marshallSignedElement(T xmlObj, String encoding) throws MarshallerException;
}
