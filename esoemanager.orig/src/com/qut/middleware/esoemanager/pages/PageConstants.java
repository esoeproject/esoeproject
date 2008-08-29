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
package com.qut.middleware.esoemanager.pages;

public class PageConstants
{
	public final static String STAGE1_RES = "stage1-status";
	public final static String STAGE2_RES = "stage2-status";
	public final static String STAGE3_RES = "stage3-status";

	/** Request name/value pairs * */
	public final static String ADD = "add";
	public final static String EDIT = "edit";
	public final static String DELETE = "delete";
	public final static String REF = "ref";
	public final static String CONFIRMED = "confirmed";
	public final static String COMPLETED = "completed";
	public final static String ERROR = "error";
	public final static String EID = "eid";
	public final static String DID = "did";
	public final static String SERVICE_ACTIVE = "activate";
	public final static String SERVICE_INACTIVE = "deactivate";

	/** Form Components * */
	/* Generic */
	public final static String NAV_COMPLETE_LABEL = "Complete";
	public final static String NAV_COMPLETE_FUNC = "completeClick";

	public final static String NAV_NEXT_LABEL = "Next >";
	public final static String NAV_NEXT_FUNC = "nextClick";

	public final static String NAV_PREV_LABEL = "< Previous";
	public final static String NAV_PREV_FUNC = "previousClick";

	/* Services */
	public final static String SERVICE_NAME = "ServiceName";
	public final static String SERVICE_IDENTIFIER = "ServiceIdentifier";
	public final static String SERVICE_URL = "ServiceURL";
	public final static String SERVICE_DESCRIPTION = "ServiceDescription";
	public final static String SERVICE_AUTHZ_FAILURE_MESSAGE = "ServiceAuthzFailure";
	public final static String SAML_DESCRIPTOR_XML = "SamlDescriptorXML";
	
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
	public final static String EDIT_SERVICE_CONTACT = "Editing contact";
	public final static String DELETE_SERVICE_CONTACT = "Successfully deleted contact";
	public final static String DELETE_SERVICE_CONTACT_DENIED = "Unable to remove last remaining contact";
	
	/* SPEP */
	public final static String SPEP_NODE_URL = "SPEPNodeURL";
	public final static String SINGLE_LOGOUT_SERVICE = "SingleLogoutService";
	public final static String ASSERTION_CONSUMER_SERVICE = "AssertionConsumerService";
	public final static String ATTRIBUTE_CONSUMER_SERVICE = "AttributeConsumingService";
	public final static String CACHE_CLEAR_SERVICE = "CacheClearService";
	public final static String SERVICE_TYPE = "serviceBaseTechnologyType";
	public final static String SERVICE_TYPE_JAVA = "Java";
	public final static String SERVICE_TYPE_APACHE = "Apache";
	public final static String SERVICE_TYPE_IIS = "IIS";
	
	/* Policy */
	public final static String LXACML_POLICY = "lxacmlPolicy";
	
	/* Attributes */
	public final static String ATTRIBUTE_POLICY = "attributePolicy";
	
	public final static String SAVE_NODE = "Save Node";

	/** Generic Cross Page Variables */
	public final static String DEFAULT_PROTOCOL = "https://";
	public final static String PROTOCOL_SEPERATOR = "://";
	public final static String PORT_SEPERATOR = ":";
	public final static String TRUE = "true";
	public final static String FALSE = "false";
	public final static int URL_FIELD_WIDTH = 80;
	public final static String SPEP_WEBAPP_NAME = "/spep";
	

}
