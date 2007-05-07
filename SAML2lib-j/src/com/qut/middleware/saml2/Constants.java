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
 * Creation Date: 11/04/2007
 * 
 * Purpose: Defines various constants for SAML2lib-j
 */
package com.qut.middleware.saml2;

public class Constants
{
	/* Crypto Constants */
	public static final String DOM_FACTORY = "DOM"; //$NON-NLS-1$
	public static final String JSR_MECHANISM = "jsr105Provider"; //$NON-NLS-1$
	public static final String JSR_PROVIDER = "org.jcp.xml.dsig.internal.dom.XMLDSigRI"; //$NON-NLS-1$
	
	/** The attribute that the signing component of the Marshaller will use to identify tags */
	public static final String ID_ATTRIBUTE = "ID"; //$NON-NLS-1$
	/** The name of the element that will contain a signature */
	public static final String SIGNATURE_ELEMENT = "Signature"; //$NON-NLS-1$
	
	public static final String EXC14NTRANS = "http://www.w3.org/2001/10/xml-exc-c14n#";
	public static final String ENVTRANS = "http://www.w3.org/2000/09/xmldsig#enveloped-signature";
	
	public static final String RSA_KEY = "RSA";
	public static final String DSA_KEY = "DSA";
}
