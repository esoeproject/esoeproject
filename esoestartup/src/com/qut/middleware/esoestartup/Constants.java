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
 * Creation Date: 1/5/07
 */
package com.qut.middleware.esoestartup;

public class Constants
{
	/** SAML schemas */
	/* SAML Schema constants */
	public static final String samlProtocol = "saml-schema-protocol-2.0.xsd"; //$NON-NLS-1$
	public static final String samlAssertion = "saml-schema-assertion-2.0.xsd"; //$NON-NLS-1$
	public static final String samlMetadata = "saml-schema-metadata-2.0.xsd"; //$NON-NLS-1$
	public static final String lxacml = "lxacml-schema.xsd"; //$NON-NLS-1$
	public static final String lxacmlSAMLProtocol = "lxacml-schema-saml-protocol.xsd"; //$NON-NLS-1$
	public static final String lxacmlSAMLAssertion = "lxacml-schema-saml-assertion.xsd"; //$NON-NLS-1$
	public static final String lxacmlGroupTarget = "lxacml-schema-grouptarget.xsd"; //$NON-NLS-1$
	public static final String lxacmlContext = "lxacml-schema-context.xsd"; //$NON-NLS-1$
	public static final String lxacmlMetadata = "lxacml-schema-metadata.xsd"; //$NON-NLS-1$
	public static final String esoeProtocol = "esoe-schema-saml-protocol.xsd"; //$NON-NLS-1$
	public static final String cacheClearService = "cacheclear-schema-saml-metadata.xsd"; //$NON-NLS-1$
	public static final String sessionData = "sessiondata-schema.xsd"; //$NON-NLS-1$
	public static final String delegatedAuthn = "delegated-schema-saml-protocol.xsd"; //$NON-NLS-1$*/
	public static final String spepStartup = "spepstartup-schema-saml-metadata.xsd"; //$NON-NLS-1$
	public static final String attributeConfig = "attributeconfig-schema.xsd"; //$NON-NLS-1$
	
	/* Database Interactions */
	public enum DatabaseDrivers {mysql, oracle}
	public static final String ORACLE = "oracle";
	public static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";
	
	public static final String MYSQL = "mysql";
	public static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
	
	public static final String ALIVE_QUERY = "SELECT COUNT(*) FROM DUAL";
	public static final String ENTITY_ACTIVE = "y";
	
	/* Crypto Generation */
	public static final int CERT_EXPIRY_YEARS = 2;
	public static final int KEY_SIZE = 2048;
	public static final int PASSPHRASE_LENGTH = 10;
	public static final String METADATA_ISSUER = "cn=\"metadata\",";
	
	/* Web Application WAR file generation */
	public static final String WEBINF = "WEB-INF";
	public static final String WEBAPP_NAME = "esoestartup";
	
	public static final String ESOECONFIG = "esoe.config";
	public static final String ESOEMANAGERSPEPCONFIG = "spep.config";
	public static final String ESOEMANAGERCONFIG = "esoemanager.config";
	
	public static final String ESOE_WAR_NAME = "ROOT.war";
	public static final String ESOE_EXPLODED_DIR = "esoe";

	public static final String ESOE_MANAGER_SPEP_WAR_NAME = "spep.war";
	public static final String ESOE_MANAGER_SPEP_EXPLODED_DIR = "spep";
	
	public static final String ESOE_MANAGER_WAR_NAME = "esoemanager.war";
	public static final String ESOE_MANAGER_EXPLODED_DIR = "esoemanager";
	
	public static final String ESOE_STARTUP_WARNAME = "esoestartup";
	
	public static final String METADATA_KEYSTORE_NAME = "metadataKeystore.ks";
	public static final String ESOE_KEYSTORE_NAME = "esoeKeystore.ks";
	public static final String ESOE_MANAGER_SPEP_KEYSTORE_NAME = "spepKeystore.ks";
	
	/* General */
	public static final String SCHEMA_SEPERATOR = ":";
}
