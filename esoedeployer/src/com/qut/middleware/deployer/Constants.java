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
 */
package com.qut.middleware.deployer;

import java.io.File;

public class Constants
{
	// SAML Schema constants
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
	
	// Database Interactions
	public enum DatabaseDrivers {mysql, oracle}
	public static final String ORACLE = "oracle";
	public static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";
	
	public static final String MYSQL = "mysql";
	public static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
	
	public static final String ALIVE_QUERY = "SELECT * FROM SERVICE_POLICIES";
	public static final String ENTITY_ACTIVE = "y";
	
	public static final String IDP_DESCRIPTOR = "1";
	public static final String SP_DESCRIPTOR = "2";
	public static final String LXACML_PDP_DESCRIPTOR = "3";
	public static final String AUTHN_AUTHORITY_DESCRIPTOR = "4"; 
	public static final String ATTRIBUTE_AUTHORITY_DESCRIPTOR ="5";	
	
	// Crypto Generation
	public static final int CERT_EXPIRY_YEARS = 2;
	public static final int KEY_SIZE = 2048;
	public static final int PASSPHRASE_LENGTH = 10;
	public static final String METADATA_ISSUER = "cn=\"metadata\",";
	
	// Config and Data output
	public static final String CONFIG_DIR = File.separatorChar + "config";
	public static final String LOGGING_DIR = File.separatorChar + "logging";
	public static final String MD_HISTORICAL_DIR = File.separatorChar + "metadatahistory";
	
	public static final String ESOECONFIG = "esoe.config";
	public static final String ESOEMANAGERSPEPCONFIG = "spep.config";
	public static final String ESOEMANAGERCONFIG = "esoemanager.config";
	
	public static final String METADATA_KEYSTORE_NAME = "metadataKeystore.ks";
	public static final String ESOE_KEYSTORE_NAME = "esoeKeystore.ks";
	public static final String ESOE_MANAGER_SPEP_KEYSTORE_NAME = "spepKeystore.ks";
	
	public static final String ESOE_STARTUP_WARNAME = "esoestartup";

	public static final String SCHEMA_SEPERATOR = ":";
	
	// iBatis Field Names
	public static final String FIELD_ENT_ID = "ENT_ID";
	public static final String FIELD_ENTITY_ID = "entityID";
	public static final String FIELD_DESC_ID = "DESC_ID";
	public static final String FIELD_DESCRIPTOR_ID = "descriptorID";
	public static final String FIELD_POLICY_ID = "policyID";
	public static final String FIELD_DESCRIPTOR_TYPE_ID = "descriptorTypeID";
	public static final String FIELD_DESCRIPTOR_XML = "descriptorXML";
	public static final String FIELD_ACTIVE_FLAG = "activeFlag";
	public static final String FIELD_ENTITY_HOST = "entityHost";
	public static final String FIELD_ORGANIZATION_NAME = "organizationName";
	public static final String FIELD_ORGANIZATION_DISPLAY_NAME = "organizationDisplayName";
	public static final String FIELD_ORGANIZATION_URL = "organizationURL";
	public static final String FIELD_ATTRIBUTE_POLICY = "attribPolicy";
	public static final String FIELD_NAMEID = "nameID";
		
	public static final String FIELD_SERVICE_NAME = "serviceName";
	public static final String FIELD_SERVICE_URL = "serviceURL";
	public static final String FIELD_SERVICE_DESC = "serviceDesc";
	public static final String FIELD_SERVICE_AUTHZ_FAIL = "authzFailureMsg";
	
	public static final String FIELD_CONTACT_ID = "contactID";
	public static final String FIELD_CONTACT_TYPE = "contactType";
	public static final String FIELD_CONTACT_COMPANY = "company";
	public static final String FIELD_CONTACT_GIVEN_NAME = "givenName";
	public static final String FIELD_CONTACT_SURNAME = "surname";
	public static final String FIELD_CONTACT_EMAIL_ADDRESS = "emailAddress";
	public static final String FIELD_CONTACT_TELEPHONE_NUMBER = "telephoneNumber";
	
	public static final String FIELD_PK_EXPIRY_DATE = "expiryDate";
	public static final String FIELD_PK_KEYPAIR_NAME = "keyName";
	public static final String FIELD_PK_ISSUER = "issuerDN";
	public static final String FIELD_PK_SERIAL = "serialNumber";
	public static final String FIELD_PK_BINARY = "publicKey";
	
	public static final String FIELD_PKI_KEYPAIRNAME = "keyPairName"; //$NON-NLS-1$$
	public static final String FIELD_PKI_EXPIRY_DATE = "expiryDate";
	public static final String FIELD_PKI_KEYSTORE = "keyStore";
	public static final String FIELD_PKI_KEYSTORE_PASSPHRASE = "keyStorePassphrase"; //$NON-NLS-1$
	public static final String FIELD_PKI_KEYPAIR_PASSPHRASE = "keyPairPassphrase"; //$NON-NLS-1$
	
	public static final String FIELD_ENDPOINT_ID = "endpointID";
	public static final String FIELD_ENDPOINT_NODEURL = "nodeURL";
	public static final String FIELD_ENDPOINT_ASSERTIONCONSUMER = "assertionConsumerEndpoint";
	public static final String FIELD_ENDPOINT_SINGLELOGOUT = "singleLogoutEndpoint";
	public static final String FIELD_ENDPOINT_CACHECLEAR = "cacheClearEndpoint";
	
	public static final String FIELD_LXACML_POLICY = "lxacmlPolicy";
	public static final String FIELD_LXACML_POLICY_ID = "lxacmlPolicyID";
	public static final String FIELD_LXACML_DATE_INSERTED = "lxacmlDate";
	
	public static final String FIELD_NODEID = "nodeID" ;
	public static final String FIELD_IPADDRESS = "ipAddress" ;
	public static final String FIELD_COMPILEDATE = "compiledDate" ;
	public static final String FIELD_COMPILESYSTEM = "compileSysten" ;
	public static final String FIELD_VERSION = "version" ;
	public static final String FIELD_ENVIRONMENT = "environment" ;
	public static final String FIELD_DATEADDED = "dateAdded" ;

	// iBatis field values
	public static final String FIELD_VALUE_SPEP_ACTIVE_AT_REGISTER = "y";
	public static final String FIELD_VALUE_SPEP_NOT_ACTIVE_AT_REGISTER = "n";
	
	public static final String QUERY_NEXT_ENT_ID = "getNextEntID";
	public static final String QUERY_NEXT_DESC_ID = "getNextDescID";
	public static final String QUERY_ENT_ID = "getEntID";
	public static final String QUERY_ENTITY_ID = "getEntityID";
	public static final String QUERY_ENTITY_HOST = "getEntityHost";
	public static final String QUERY_ENTITY_DESCRIPTORS = "getDescriptors";
	public static final String QUERY_DESCRIPTOR_PUBLIC_KEYS = "getPublicKeyData";
	public static final String QUERY_ACTIVE_ENTITY_LIST = "getActiveEntityIDList";
	public static final String QUERY_SERVICES_LIST = "getServicesList";
	public static final String QUERY_IDP_LIST = "getIDPList";
	public static final String QUERY_SP_LIST = "getSPList";
	public static final String QUERY_SUPPORTED_NAMEID_FORMATS = "getNameIDFormats";
	public static final String QUERY_SERVICE_DESCRIPTOR = "getServiceDescriptor";
	public static final String QUERY_SERVICE_DESCRIPTOR_ID = "getServiceDescriptorID";
	public static final String QUERY_SERVICE_DESCRIPTION = "getServiceDescription";
	public static final String QUERY_ATTRIBUTE_AUTHORITY_LIST = "getAttributeAuthorityList";
	public static final String QUERY_LXACMLPDP_LIST = "getLXACMLPDPList";
	public static final String QUERY_CONTACTS = "getContacts";
	public static final String QUERY_SERVICE_CONTACTS = "getServiceContacts";
	public static final String QUERY_KEYSTORE = "getKeyStore";
	public static final String QUERY_SERVICE_DETAILS = "getServiceDetails";
	public static final String QUERY_SERVICE_NODES = "getServiceNodes";
	public static final String QUERY_ACTIVE_SERVICE_NODES = "getActiveServiceNodes";
	public static final String QUERY_KEYSTORE_DETAILS = "getKeyStoreDetails";
	public static final String QUERY_AUTHORIZATION_POLICY = "getAuthorizationPolicy";
	public static final String QUERY_ACTIVE_AUTHORIZATION_POLICIES = "getAuthorizationPolicies";
	public static final String QUERY_ACTIVE_ATTRIBUTE_POLICY = "getActiveAttributePolicy";
	public static final String QUERY_RECENT_NODE_STARTUP = "getRecentNodeStartup";
	public static final String QUERY_RECENT_SERVICE_NODE_STARTUP = "getRecentServiceNodeStartup";
	
	public static final String QUERY_ACTIVE_NODE_COUNT = "getActiveNodeCount";
	public static final String QUERY_ACTIVE_POLICY_COUNT = "getActivePolicyCount";
	public static final String QUERY_ACTIVE_SERVICE_COUNT = "getActiveServiceCount";
	public static final String QUERY_NODE_COUNT = "getNodeCount";
	public static final String QUERY_POLICY_COUNT = "getPolicyCount";
	public static final String QUERY_SERVICE_COUNT = "getServiceCount";
	
	public static final String QUERY_SERVICES_CLOSE_EXPIRY = "getServicesCloseExpiry";
	public static final String QUERY_ENTID_FROM_DESCID = "getEntIDfromDescID";
	
	public static final String INSERT_ENTITY_DESCRIPTOR = "insertEntityDescriptor";
	public static final String INSERT_SERVICE_DESCRIPTION = "insertServiceDescription";
	public static final String INSERT_SERVICE_CONTACTS = "insertServiceContacts";
	public static final String INSERT_DESCRIPTOR = "insertDescriptor";
	public static final String INSERT_DESCRIPTOR_PUBLIC_KEY = "insertDescriptorPublicKey";
	public static final String INSERT_PKI_DATA = "insertPKIData";
	public static final String INSERT_METADATA_PKI_DATA = "insertMetadataPKIData";
	public static final String INSERT_SERVICE_NODE = "insertServiceNode";
	public static final String INSERT_SERVICE_AUTHORIZATION_POLICY = "insertServiceAuthorizationPolicy";
	public static final String INSERT_MANAGEMENT_AUTHORIZATION_POLICY = "insertManagementAuthorizationPolicy";
	public static final String INSERT_SERVICE_AUTHORIZATION_SHUNTED_POLICY = "insertServiceAuthorizationShuntedPolicy";
	public static final String INSERT_SERVICE_AUTHORIZATION_HISTORICAL_POLICY = "insertServiceAuthorizationHistoricalPolicy";
	public static final String INSERT_ATTRIBUTE_POLICY = "insertAttributePolicy";
	
	public static final String UPDATE_SERVICE_CONTACT = "updateServiceContact";
	public static final String UPDATE_SERVICE_ACTIVE_STATUS = "updateServiceActiveStatus";
	public static final String UPDATE_SERVICE_NODE = "updateServiceNode";
	public static final String UPDATE_SERVICE_NODE_ACTIVE_STATUS = "updateServiceNodeActiveStatus";
	public static final String UPDATE_SERVICE_POLICY_ACTIVE_STATUS = "updateServicePolicyActiveStatus";
	public static final String UPDATE_SERVICE_DESCRIPTION = "updateServiceDescription";
	public static final String UPDATE_SERVICE_AUTHORIZATION_POLICY = "updateServiceAuthorizationPolicy";
	public static final String UPDATE_ATTRIBUTE_POLICY = "updateActiveAttributePolicy";
	public static final String UPDATE_DESCRIPTOR = "updateDescriptor";
	public static final String UPDATE_ENTITY_HOST = "updateEntityHost";
	
	public static final String DELETE_SERVICE_CONTACT = "deleteServiceContact";
	public static final String DELETE_SERVICE_POLICY = "deleteServicePolicy";
	public static final String DELETE_SERVICE_KEYPAIR = "deleteServiceKeypair";
	public static final String DELETE_SERVICE_KEYSTORE = "deleteServiceKeystore";
}
