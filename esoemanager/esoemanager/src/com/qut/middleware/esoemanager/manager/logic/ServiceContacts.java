/* Copyright 2008, Queensland University of Technology
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
package com.qut.middleware.esoemanager.manager.logic;

import java.util.List;

import com.qut.middleware.esoemanager.client.rpc.bean.ServiceContact;
import com.qut.middleware.esoemanager.exception.CreateServiceContactException;
import com.qut.middleware.esoemanager.exception.DeleteServiceContactException;
import com.qut.middleware.esoemanager.exception.RetrieveServiceContactException;
import com.qut.middleware.esoemanager.exception.SaveServiceContactException;

public interface ServiceContacts
{
	public List<ServiceContact> retrieveServiceContacts(String serviceID) throws RetrieveServiceContactException;
	public void saveServiceContact(String serviceID, String contactID, String name, String email, String telephone, String company, String type) throws SaveServiceContactException;
	public void createServiceContact(String serviceID, String contactID, String name, String email, String telephone, String company, String type) throws CreateServiceContactException;
	public void deleteServiceContact(String serviceID, String contactID) throws DeleteServiceContactException;
}
