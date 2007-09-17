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
package com.qut.middleware.esoestartup.pages;

public class PageConstants
{
	/** Stages */
	public final static String STAGE1_RES = "stageOneResult";
	public final static String STAGE2_RES = "stageTwoResult";
	public final static String STAGE3_RES = "stageThreeResult";
	public final static String STAGE4_RES = "stageFourResult";
	public final static String STAGE5_RES = "stageFiveResult";
	public final static String STAGE6_RES = "stageSixResult";
	public final static String STAGE7_RES = "stageSevenResult";
	public final static String STAGE8_RES = "stageEightResult";
	public final static String STAGE9_RES = "stageEightResult";

	/** Form Control Constants */
	/* General */
	public final static String NAV_NEXT_FUNC = "nextClick";
	public final static String NAV_COMPLETE_FUNC = "completeClick";
	public final static String NAV_PREV_FUNC = "previousClick";

	/* ESOE */
	public final static String ESOE_IDENTIFIER = "esoeIdentifier";
	public final static String ESOE_COMMON_DOMAIN = "esoeCommonDomain";
	public final static String ESOE_NODE_URL = "nodeURL";
	public final static String ESOE_SINGLE_SIGN_ON_SERVICE = "singleSignOnService";
	public final static String ESOE_ATTRIBUTE_SERVICE = "attributeService";
	public final static String ESOE_LXACML_SERVICE = "lxacmlService";
	public final static String ESOE_SPEP_STARTUP_SERVICE = "spepStartupService";
	public final static String ESOE_ORGANIZATION_NAME = "esoeOrganizationName";
	public final static String ESOE_ORGANIZATION_DISPLAY_NAME = "esoeOrganizationDisplayName";
	public final static String ESOE_ORGANIZATION_URL = "esoeOrganizationURL";
	
	/* Content Directories */
	public final static String ESOE_CONTENT_DATA = "esoe.data";
	public final static String ESOEMANAGER_CONTENT_DATA = "esoemanager.data";
	public final static String SPEP_CONTENT_DATA = "spep.data";

	/* Data Repository */
	public final static String DATA_REPOSITORY_DRIVER = "driverType";
	public final static String DATA_REPOSITORY_DRIVER_ORACLE = "oracle";
	public final static String DATA_REPOSITORY_DRIVER_MYSQL = "mysql";
	public final static String DATA_REPOSITORY_URL = "dataRepositoryURL";
	public final static String DATA_REPOSITORY_USERNAME = "dataRepositoryUsername";
	public final static String DATA_REPOSITORY_PASSWORD = "dataRepositoryPassword";
	public final static String DATA_REPOSITORY_SUBMIT = "onDataRepositorySubmit";
	
	/* LDAP */
	public final static String LDAP_URL = "ldapURL";
	public final static String LDAP_PORT = "ldapPort";
	public final static String LDAP_BASE_DN = "ldapBaseDN";
	public final static String LDAP_ACCOUNT_IDENTIFIER = "ldapAccountIdentifier";
	public final static String LDAP_RECURSIVE = "ldapRecursive";
	public final static String LDAP_DISABLE_SSL = "ldapDisableSSL";
	public final static String LDAP_ADMIN_USER = "ldapAdminUser";
	public final static String LDAP_ADMIN_PASSWORD = "ldapAdminPassword";

	/* Contact Person */
	public final static String COMPANY = "Company";
	public final static String GIVENNAME = "GivenName";
	public final static String SURNAME = "SurName";
	public final static String SURNAME_LABEL = "Surname";
	public final static String EMAILADDRESS = "EmailAddress";
	public final static String TELEPHONENUMBER = "TelephoneNumber";
	public final static String CONTACTID = "ContactID";
	public final static String CONTACTTYPE = "ContactType";
	public final static String CONTACTTYPE_TECH = "technical";
	public final static String CONTACTTYPE_SUP = "support";
	public final static String CONTACTTYPE_ADMIN = "administrative";
	public final static String CONTACTTYPE_BILL = "billing";
	public final static String SAVE_CONTACT = "Save Contact";

	/* Services */
	public final static String SERVICE_NAME = "ServiceName";
	public final static String SERVICE_IDENTIFIER = "ServiceIdentifier";
	public final static String SERVICE_URL = "ServiceURL";
	public final static String SERVICE_DESCRIPTION = "ServiceDescription";
	public final static String SERVICE_AUTHZ_FAILURE_MESSAGE = "ServiceAuthzFailure";
	public final static String SERVICE_TYPE = "serviceBaseTechnologyType";
	public final static String ESOE_MANAGER_SERVICE_NAME = "ESOE Manager Web Application";
	public final static String ESOE_MANAGER_SERVICE_DESCRIPTION = "This is the manager application for the Enterprise Sign On Engine, it allows administrators to create, " +
																  "update, enable and disable remote services as well as monitor various aspects of the ESOE itself";
	
	/* Service Nodes */
	public final static String SPEP_NODE_URL = "SPEPNodeURL";
	public final static String SINGLE_LOGOUT_SERVICE = "SingleLogoutService";
	public final static String ASSERTION_CONSUMER_SERVICE = "AssertionConsumerService";
	public final static String ATTRIBUTE_CONSUMER_SERVICE = "AttributeConsumingService";
	public final static String CACHE_CLEAR_SERVICE = "CacheClearService";

	/* Crypto */
	public final static String CRYPTO_ISSUER_EMAIL = "issuerEmailAddress";
	public final static String CRYPTO_ISSUER_DN = "issuerDN";

	/* Output directory */
	public final static String WRITEABLE_DIRECTORY = "writeableDirectory";
	public final static String TOMCAT_WEBAPPS_DIRECTORY = "tomcatWebapps";
	
	/** Form error state messages */
	public final static String ESOE_CONTENT_ERROR = "Unable to write to directory specified for esoe.data, please ensure directory exists and has correct permissions";
	public final static String ESOEMANAGER_CONTENT_ERROR = "Unable to write to directory specified for esoemanager.data, please ensure directory exists and has correct permissions";
	public final static String SPEP_CONTENT_ERROR = "Unable to write to directory specified for spep.data, please ensure directory exists and has correct permissions";
	
	public final static String DATA_REPOSITORY_TEST_ERROR = "Using the below details attempting to connect to the database failed, please review the output below and adjust values accordingly <br/>";
	public final static String DATA_REPOSITORY_UNKNOWN_DRIVER = "The driver you submitted is unknown to this system";
	public final static String EDIT_SERVICE_CONTACT = "Editing contact";
	public final static String DELETE_SERVICE_CONTACT = "Successfully deleted contact";
	public final static String DELETE_SERVICE_CONTACT_DENIED = "Unable to remove last remaining contact";

	/** Form Labels */
	public final static String NAV_NEXT_LABEL = "Next >";
	public final static String NAV_PREV_LABEL = "< Previous";
	public final static String NAV_COMPLETE_LABEL = "Complete";

	/** Actions */
	public final static String EDIT = "edit";
	public final static String DELETE = "delete";
	public final static String COMPLETED = "complete";

	/** Generic */
	public final static int URL_FIELD_WIDTH = 80;
	public final static String DEFAULT_PROTOCOL = "https://";
	public final static String PROTOCOL_SEPERATOR = "://";
	public final static String PORT_SEPERATOR = ":";
	public final static String TRUE = "true";
	public final static String FALSE = "false";
	public final static String SPEP_WEBAPP_NAME = "/spep";
}
