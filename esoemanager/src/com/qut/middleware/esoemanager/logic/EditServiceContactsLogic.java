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
 * Purpose: Allows clients to add/edit/delete contacts asociated with their service
 */
package com.qut.middleware.esoemanager.logic;

import java.util.Vector;

import com.qut.middleware.esoemanager.bean.ContactPersonBean;
import com.qut.middleware.esoemanager.exception.EditServiceContactException;

public interface EditServiceContactsLogic
{
	public Vector<ContactPersonBean> getServiceContacts(String entityID) throws EditServiceContactException;

	public void updateServiceContacts(String entityID, Vector<ContactPersonBean> contacts)
			throws EditServiceContactException;

	public void deleteServiceContact(String entityDescriptorID, String contactID) throws EditServiceContactException;
}
