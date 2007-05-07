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
 * Purpose:  Edit service contacts logi default implementation
 */
package com.qut.middleware.esoemanager.logic.impl;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.qut.middleware.esoemanager.Constants;
import com.qut.middleware.esoemanager.UtilityFunctions;
import com.qut.middleware.esoemanager.bean.ContactPersonBean;
import com.qut.middleware.esoemanager.bean.impl.ContactPersonBeanImpl;
import com.qut.middleware.esoemanager.exception.EditServiceContactException;
import com.qut.middleware.esoemanager.exception.SPEPDAOException;
import com.qut.middleware.esoemanager.logic.EditServiceContactsLogic;
import com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO;

public class EditServiceContactsLogicImpl implements EditServiceContactsLogic
{
	private SPEPDAO spepDAO;
	private UtilityFunctions util;

	/* Local logging instance */
	private Logger logger = Logger.getLogger(EditServiceContactsLogicImpl.class.getName());

	public EditServiceContactsLogicImpl(SPEPDAO spepDAO)
	{
		if (spepDAO == null)
		{
			this.logger.error("spepDAO for EditServiceContactsLogicImpl was NULL");
			throw new IllegalArgumentException("spepDAO for EditServiceContactsLogicImpl was NULL");
		}

		this.spepDAO = spepDAO;
		this.util = new UtilityFunctions();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.logic.EditServiceContactLogic#getServiceContacts(java.lang.String)
	 */
	public Vector<ContactPersonBean> getServiceContacts(String entityID) throws EditServiceContactException
	{
		List<Map<String, String>> contactData;

		try
		{
			contactData = this.spepDAO.queryServiceContacts(entityID);
		}
		catch (SPEPDAOException e)
		{
			throw new EditServiceContactException("Exception when attempting to retireve contact data", e);
		}

		if (contactData == null)
		{
			this.logger.info("No service contacts returned for entityID " + entityID);
			return null;
		}

		return generateContactPersonList(contactData);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoemanager.logic.EditServiceContactLogic#updateServiceContacts(java.util.Vector)
	 */
	public void updateServiceContacts(String entityID, Vector<ContactPersonBean> contacts)
			throws EditServiceContactException
	{
		try
		{
			for (ContactPersonBean contact : contacts)
			{
				/* No ID indicated new contact to insert */
				if (contact.getContactID() == null || contact.getContactID().length() == 0)
				{
					contact.setContactID(this.util.generateID());
					this.logger.debug("Inserting new contact with contactID " + contact.getContactID());
					this.spepDAO.insertServiceContacts(entityID, contact.getContactID(), contact
							.getContactType(), contact.getCompany(), contact.getGivenName(), contact.getSurName(),
							contact.getEmailAddress(), contact.getTelephoneNumber());
				}
				else
				{
					if (contact.isModified())
					{
						this.logger.debug("Updating contact with contactID " + contact.getContactID());
						this.spepDAO.updateServiceContact(entityID, contact.getContactID(), contact
								.getContactType(), contact.getCompany(), contact.getGivenName(), contact.getSurName(),
								contact.getEmailAddress(), contact.getTelephoneNumber());
					}
				}
			}
		}
		catch (SPEPDAOException e)
		{
			this.logger.error("SPEPDAOException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new EditServiceContactException(e.getLocalizedMessage(), e);
		}

	}

	public void deleteServiceContact(String entityDescriptorID, String contactID)
			throws EditServiceContactException
	{
		try
		{
			this.spepDAO.deleteServiceContact(entityDescriptorID, contactID);
		}
		catch (SPEPDAOException e)
		{
			this.logger.error("SPEPDAOException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new EditServiceContactException(e.getLocalizedMessage(), e);
		}
	}

	private Vector<ContactPersonBean> generateContactPersonList(List<Map<String, String>> contacts)
	{
		Vector<ContactPersonBean> contactPersons = new Vector<ContactPersonBean>();

		for (Map<String, String> contact : contacts)
		{
			ContactPersonBean contactPerson = new ContactPersonBeanImpl();
			contactPerson.setContactID(contact.get(Constants.FIELD_CONTACT_ID));
			contactPerson.setCompany(contact.get(Constants.FIELD_CONTACT_COMPANY));
			contactPerson.setContactType(contact.get(Constants.FIELD_CONTACT_TYPE));
			contactPerson.setGivenName(contact.get(Constants.FIELD_CONTACT_GIVEN_NAME));
			contactPerson.setSurName(contact.get(Constants.FIELD_CONTACT_SURNAME));
			contactPerson.setEmailAddress(contact.get(Constants.FIELD_CONTACT_EMAIL_ADDRESS));
			contactPerson.setTelephoneNumber(contact.get(Constants.FIELD_CONTACT_TELEPHONE_NUMBER));

			contactPersons.add(contactPerson);
		}

		return contactPersons;
	}
}
