/*
 * Copyright 2008, Queensland University of Technology
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
 * Author: Shaun Mangelsdorf
 * Creation Date: Apr 22, 2008
 * 
 * Purpose: 
 */

package com.qut.middleware.saml2;

/** */
@SuppressWarnings("nls")
public class SchemaConstants
{
	/* Schema constants, included across the project */
	
	/** SAML Protocol schema */
	public static final String samlProtocol = "saml-schema-protocol-2.0.xsd"; //$NON-NLS-1$
	/** SAML Assertion schema */
	public static final String samlAssertion = "saml-schema-assertion-2.0.xsd"; //$NON-NLS-1$
	/** SAML Metadata schema */
	public static final String samlMetadata = "saml-schema-metadata-2.0.xsd"; //$NON-NLS-1$
	/** LXACML schema */
	public static final String lxacml = "lxacml-schema.xsd"; //$NON-NLS-1$
	/** LXACML SAML Protocol schema */
	public static final String lxacmlSAMLProtocol = "lxacml-schema-saml-protocol.xsd"; //$NON-NLS-1$
	/** LXACML SAML Assertion schema */
	public static final String lxacmlSAMLAssertion  = "lxacml-schema-saml-assertion.xsd"; //$NON-NLS-1$
	/** LXACML Group Target schema */
	public static final String lxacmlGroupTarget = "lxacml-schema-grouptarget.xsd"; //$NON-NLS-1$
	/** LXACML Context schema */
	public static final String lxacmlContext = "lxacml-schema-context.xsd"; //$NON-NLS-1$
	/** LXACML Metadata schema */
	public static final String lxacmlMetadata = "lxacml-schema-metadata.xsd"; //$NON-NLS-1$
	/** ESOE Protocol schema */
	public static final String esoeProtocol = "esoe-schema-saml-protocol.xsd"; //$NON-NLS-1$
	/** Cache Clear Service schema */
	public static final String cacheClearService = "cacheclear-schema-saml-metadata.xsd"; //$NON-NLS-1$
	/** SPEP Startup Service schema */
	public static final String spepStartupService = "spepstartup-schema-saml-metadata.xsd"; //$NON-NLS-1$
	/** Session Data schema */
	public static final String sessionData = "sessiondata-schema.xsd"; //$NON-NLS-1$
	/** Attribute Config schema */
	public static final String attributeConfig = "attributeconfig-schema.xsd"; //$NON-NLS-1$
	/** Delegated Authentication schema */
	public static final String delegatedAuthn = "delegated-schema-saml-protocol.xsd";
}
