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

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private Logger logger = LoggerFactory.getLogger(EditServiceContactsLogicImpl.class.getName());

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

	public Vector<ContactPersonBean> getServiceContacts(Integer entID) throws EditServiceContactException
	{
		List<Map<String, Object>> contactData;

		try
		{
			contactData = this.spepDAO.queryServiceContacts(entID);
		}
		catch (SPEPDAOException e)
		{
			throw new EditServiceContactException("Exception when attempting to retireve contact data", e);
		}

		if (contactData == null)
		{
			this.logger.info("No service contacts returned for entID " + entID);
			return null;
		}

		return generateContactPersonList(contactData);
	}

	public void updateServiceContacts(Integer entID, Vector<ContactPersonBean> contacts)
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
					this.spepDAO.insertServiceContacts(entID, contact.getContactID(), contact
							.getContactType(), contact.getCompany(), contact.getGivenName(), contact.getSurName(),
							contact.getEmailAddress(), contact.getTelephoneNumber());
				}
				else
				{
					if (contact.isModified())
					{
						this.logger.debug("Updating contact with contactID " + contact.getContactID());
						this.spepDAO.updateServiceContact(entID, contact.getContactID(), contact
								.getContactType(), contact.getCompany(), contact.getGivenName(), contact.getSurName(),
								contact.getEmailAddress(), contact.getTelephoneNumber());
					}
				}
			}
		}
		catch (SPEPDAOException e)
		{
			this.logger.error("SPEPDAOException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new EditServiceContactException(e.getLocalizedMessage(), e);
		}

	}

	public void deleteServiceContact(Integer entID, String contactID)
			throws EditServiceContactException
	{	
		try
		{
			this.spepDAO.deleteServiceContact(entID, contactID);
		}
		catch (SPEPDAOException e)
		{
			this.logger.error("SPEPDAOException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e.toString());
			throw new EditServiceContactException(e.getLocalizedMessage(), e);
		}
	}

	private Vector<ContactPersonBean> generateContactPersonList(List<Map<String, Object>> contacts)
	{
		Vector<ContactPersonBean> contactPersons = new Vector<ContactPersonBean>();

		for (Map<String, Object> contact : contacts)
		{
			ContactPersonBean contactPerson = new ContactPersonBeanImpl();
			contactPerson.setContactID((String)contact.get(Constants.FIELD_CONTACT_ID));
			contactPerson.setCompany((String)contact.get(Constants.FIELD_CONTACT_COMPANY));
			contactPerson.setContactType((String)contact.get(Constants.FIELD_CONTACT_TYPE));
			contactPerson.setGivenName((String)contact.get(Constants.FIELD_CONTACT_GIVEN_NAME));
			contactPerson.setSurName((String)contact.get(Constants.FIELD_CONTACT_SURNAME));
			contactPerson.setEmailAddress((String)contact.get(Constants.FIELD_CONTACT_EMAIL_ADDRESS));
			contactPerson.setTelephoneNumber((String)contact.get(Constants.FIELD_CONTACT_TELEPHONE_NUMBER));

			contactPersons.add(contactPerson);
		}

		return contactPersons;
	}
}
