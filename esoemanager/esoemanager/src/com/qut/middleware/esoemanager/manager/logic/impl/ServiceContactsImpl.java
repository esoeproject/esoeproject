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
package com.qut.middleware.esoemanager.manager.logic.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.qut.middleware.esoemanager.Constants;
import com.qut.middleware.esoemanager.client.rpc.bean.ServiceContact;
import com.qut.middleware.esoemanager.exception.CreateServiceContactException;
import com.qut.middleware.esoemanager.exception.DeleteServiceContactException;
import com.qut.middleware.esoemanager.exception.RetrieveServiceContactException;
import com.qut.middleware.esoemanager.exception.ManagerDAOException;
import com.qut.middleware.esoemanager.exception.SaveServiceContactException;
import com.qut.middleware.esoemanager.manager.logic.ServiceContacts;
import com.qut.middleware.esoemanager.manager.sqlmap.ManagerDAO;

public class ServiceContactsImpl implements ServiceContacts
{
	private ManagerDAO managerDAO;

	public void createServiceContact(String serviceID, String contactID, String name, String email, String telephone, String company,
			String type) throws CreateServiceContactException
	{
		Integer entID = new Integer(serviceID);

		/*
		 * This is hackish and assumes a lot about the name format but it does
		 * the job
		 */
		String[] names = name.split(" ");
		String givenName = "";
		String surname = "";
		for (int i = 0; i < names.length - 1; i++)
		{
			givenName = givenName + names[i];
			if (i != names.length - 1)
				givenName = givenName + " ";
		}
		surname = names[names.length - 1];

		try
		{
			this.managerDAO.insertServiceContact(entID, contactID, type, company, givenName, surname, email, telephone);
		}
		catch (ManagerDAOException e)
		{
			throw new CreateServiceContactException("Exception when attempting save service contact", e);
		}

	}

	public List<ServiceContact> retrieveServiceContacts(String serviceID) throws RetrieveServiceContactException
	{
		try
		{
			Integer entID = new Integer(serviceID);
			List<ServiceContact> contactList = new ArrayList<ServiceContact>();

			List<Map<String, Object>> contacts = this.managerDAO.queryServiceContacts(entID);
			for (Map<String, Object> contact : contacts)
			{
				ServiceContact contactPerson = new ServiceContact();
				contactPerson.setCompany((String) contact.get(Constants.FIELD_CONTACT_COMPANY));
				contactPerson.setContactID((String) contact.get(Constants.FIELD_CONTACT_ID));
				contactPerson.setType((String) contact.get(Constants.FIELD_CONTACT_TYPE));
				contactPerson.setName((String) contact.get(Constants.FIELD_CONTACT_GIVEN_NAME) + " "
						+ (String) contact.get(Constants.FIELD_CONTACT_SURNAME));
				contactPerson.setEmail((String) contact.get(Constants.FIELD_CONTACT_EMAIL_ADDRESS));
				contactPerson.setTelephone((String) contact.get(Constants.FIELD_CONTACT_TELEPHONE_NUMBER));

				contactList.add(contactPerson);
			}

			return contactList;
		}
		catch (ManagerDAOException e)
		{
			throw new RetrieveServiceContactException("Exception when attempting get service contacts", e);
		}
	}

	public void saveServiceContact(String serviceID, String contactID, String name, String email, String telephone,
			String company, String type) throws SaveServiceContactException
	{
		Integer entID = new Integer(serviceID);

		/*
		 * This is hackish and assumes a lot about the name format but it does
		 * the job
		 */
		String[] names = name.split(" ");
		String givenName = "";
		String surname = "";
		for (int i = 0; i < names.length - 1; i++)
		{
			givenName = givenName + names[i];
			if (i != names.length - 1)
				givenName = givenName + " ";
		}
		surname = names[names.length - 1];

		try
		{
			this.managerDAO.updateServiceContact(entID, contactID, type, company, givenName, surname, email, telephone);
		}
		catch (ManagerDAOException e)
		{
			throw new SaveServiceContactException("Exception when attempting save service contact", e);
		}
	}
	
	public void deleteServiceContact(String serviceID, String contactID) throws DeleteServiceContactException
	{
		Integer entID = new Integer(serviceID);
		try
		{
			this.managerDAO.deleteServiceContact(entID, contactID);
		}
		catch (ManagerDAOException e)
		{
			throw new DeleteServiceContactException("Exception when attempting delete of service contact", e);
		}
	}

	public ManagerDAO getManagerDAO()
	{
		return managerDAO;
	}

	public void setManagerDAO(ManagerDAO managerDAO)
	{
		this.managerDAO = managerDAO;
	}	
}
