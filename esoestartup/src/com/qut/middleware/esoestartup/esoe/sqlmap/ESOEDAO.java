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
 * 
 * Purpose: ESOE DAO
 */
package com.qut.middleware.esoestartup.esoe.sqlmap;

import java.util.Date;

import com.qut.middleware.esoestartup.exception.ESOEDAOException;

public interface ESOEDAO
{
	/* Data repository insert operations */
	public void insertEntityDescriptor(String entityID, String organizationName,
			String organizationDisplayName, String organizationURL, String activeFlag)
			throws ESOEDAOException;

	public void insertServiceDescription(String entityID, String serviceName, String serviceURL,
			String serviceDescription, String serviceAuthzFailureMsg) throws ESOEDAOException;

	public void insertServiceContacts(String entityID, String contactID, String contactType, String company, String givenName,
			String surname, String emailAddress, String telephoneNumber) throws ESOEDAOException;

	public void insertDescriptor(String entityID, String descriptorID, String descriptorXML,
			String descriptorTypeID) throws ESOEDAOException;

	public void insertServiceNode(String endpointID, String descriptorID, String nodeURL,
			String assertionConsumerEndpoint, String singleLogoutEndpoint, String cacheClearEndpoint)
			throws ESOEDAOException;
	
	public void insertServiceAuthorizationPolicy(String descriptorID, String lxacmlPolicy, Date updateTime) throws ESOEDAOException;
}
