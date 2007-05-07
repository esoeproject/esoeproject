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
 * Purpose: Holds constant values for the various modules in the ESOE that won't reasonably need to be
 * changed at any client site, all fields must have associated comments indicating where they are used
 */
package com.qut.middleware.esoe;

/** */
@SuppressWarnings("nls")
public class ConfigurationConstants
{
	/* Schema constants, included across the project */
	
	/** SAML Protocol schema */
	public static final String samlProtocol = "saml-schema-protocol-2.0.xsd"; 
	/** SAML Assertion schema */
	public static final String samlAssertion = "saml-schema-assertion-2.0.xsd"; 
	/** SAML Metadata schema */
	public static final String samlMetadata = "saml-schema-metadata-2.0.xsd"; 
	/** LXACML schema */
	public static final String lxacml = "lxacml-schema.xsd"; 
	/** LXACML SAML Protocol schema */
	public static final String lxacmlSAMLProtocol = "lxacml-schema-saml-protocol.xsd"; 
	/** LXACML SAML Assertion schema */
	public static final String lxacmlSAMLAssertion  = "lxacml-schema-saml-assertion.xsd"; 
	/** LXACML Group Target schema */
	public static final String lxacmlGroupTarget = "lxacml-schema-grouptarget.xsd"; 
	/** LXACML Context schema */
	public static final String lxacmlContext = "lxacml-schema-context.xsd"; 
	/** LXACML Metadata schema */
	public static final String lxacmlMetadata = "lxacml-schema-metadata.xsd"; 
	/** ESOE Protocol schema */
	public static final String esoeProtocol = "esoe-schema-saml-protocol.xsd"; 
	/** Cache Clear Service schema */
	public static final String cacheClearService = "cacheclear-schema-saml-metadata.xsd"; 
	/** Session Data schema */
	public static final String sessionData = "sessiondata-schema.xsd"; 
	/** Delegated Authentication schema */
	public static final String delegatedAuthn = "delegated-schema-saml-protocol.xsd";
	
	/** Timezone in use for the ESOE */
	public static final String timeZone = "UTC"; 
	
	/** Spring IoC Integration */
	public static final String ESOE_CONFIG = "/WEB-INF/esoe.config"; 
	public static final String AUTHN_PROCESSOR = "authnProcessor"; 
	public static final String LOGOUT_AUTHORITY_PROCESSOR = "logoutAuthorityProcessor"; 
	public static final String AUTHN_AUTHORITY_PROCESSOR = "authnAuthorityProcessor"; 
	public static final String SESSION_TOKEN_NAME = "sessionTokenName";
	public static final String DISABLE_SSO_TOKEN_NAME = "disableAutomatedAuthnTokenName";
	public static final String AUTHN_REDIRECT_URL = "authenticationURL";
	public static final String LOGOUT_REDIRECT_URL = "logoutURL";
	public static final String LOGOUT_RESPONSE_REDIRECT_URL = "logoutSuccessURL";
	public static final String AUTHN_DYNAMIC_URL_PARAM = "authenticationDynamicParameter";
	public static final String SSO_URL = "ssoURL";	
	public static final String COOKIE_SESSION_DOMAIN = "sessionDomain";	
	
	
}
