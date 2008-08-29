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

import java.io.File;

/** */
@SuppressWarnings("nls")
public class ConfigurationConstants
{
	/* Loggers */
	public static final String authnLogger = "esoe.authn";
	public static final String authzLogger = "esoe.authz";
	
	/** Timezone in use for the ESOE */
	public static final String timeZone = "UTC"; 
	
	/** Spring IoC Integration */
	public static final String ESOE_CONFIG = File.separatorChar + "config" + File.separatorChar + "esoe.config"; 
	public static final String AUTHN_PROCESSOR = "authnProcessor"; 
	public static final String LOGOUT_PROCESSOR = "logoutProcessor"; 
	public static final String SSO_PROCESSOR = "ssoProcessor"; 
	public static final String ESOE_SESSION_TOKEN_NAME = "sessionTokenName";
	public static final String COMMON_DOMAIN_TOKEN_NAME = "commonDomainTokenName";
	public static final String DISABLE_SSO_TOKEN_NAME = "disableAutomatedAuthnTokenName";
	public static final String AUTHN_REDIRECT_URL = "authenticationURL";
	public static final String LOGOUT_REDIRECT_URL = "logoutURL";
	public static final String LOGOUT_RESPONSE_REDIRECT_URL = "logoutSuccessURL";
	public static final String AUTHN_DYNAMIC_URL_PARAM = "authenticationDynamicParameter";
	public static final String SSO_URL = "ssoURL";	
	public static final String ESOE_SESSION_DOMAIN = "sessionDomain";	
	public static final String ESOE_IDENTIFIER = "esoeIdentifier";	
	public static final String COMMON_DOMAIN = "commonDomain";	
	
	/** Policy state changes as specified by poll actions */
	public final static char POLICY_STATE_UPDATE = 'U';
	public final static char POLICY_STATE_DELETE = 'D';
	public final static char POLICY_STATE_ADD = 'A';
	public final static char POLICY_STATE_NONE = 'z';
}
