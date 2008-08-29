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

import java.util.Vector;

import com.qut.middleware.esoemanager.bean.ContactPersonBean;
import com.qut.middleware.esoemanager.bean.ServiceBean;
import com.qut.middleware.esoemanager.bean.impl.ContactPersonBeanImpl;
import com.qut.middleware.esoemanager.pages.forms.impl.ContactForm;

public class ContactPersonPage extends BorderPage
{
	/* Contact details */
	public ContactForm contactDetails;

	public ContactPersonPage()
	{
		this.contactDetails = new ContactForm();
	}

	@Override
	public void onInit()
	{
		this.contactDetails.init();
	}

	protected void createOrUpdateContact(ServiceBean serviceBean)
	{
		if (this.contactDetails.isValid())
		{
			/* All data for creating a new contact person is valid, copy to SAML compliant instance */
			Vector<ContactPersonBean> contacts = (Vector<ContactPersonBean>) serviceBean.getContacts();
			if (contacts == null)
				contacts = new Vector<ContactPersonBean>();

			for (ContactPersonBean contactPerson : contacts)
			{
				/*
				 * If this contact already exists in the data respository it will have a contactID, in which case we
				 * remove our local copy, ready for updated version to ensure no duplication
				 */
				if (contactPerson.getContactID() != null && contactPerson.getContactID().length() > 1 && contactPerson.getContactID().equals(this.contactDetails.getFieldValue(PageConstants.CONTACTID)))
				{
					contacts.remove(contactPerson);
					break;
				}
			}

			contacts.add(translateDetails());
			serviceBean.setContacts(contacts);
		}
	}

	protected void translateDetails(ContactPersonBean contactPerson)
	{
		this.contactDetails.getField(PageConstants.CONTACTID).setValue(contactPerson.getContactID());
		this.contactDetails.getField(PageConstants.COMPANY).setValue(contactPerson.getCompany());
		this.contactDetails.getField(PageConstants.GIVENNAME).setValue(contactPerson.getGivenName());
		this.contactDetails.getField(PageConstants.SURNAME).setValue(contactPerson.getSurName());
		this.contactDetails.getField(PageConstants.EMAILADDRESS).setValue(contactPerson.getEmailAddress());
		this.contactDetails.getField(PageConstants.TELEPHONENUMBER).setValue(contactPerson.getTelephoneNumber());
		this.contactDetails.getField(PageConstants.CONTACTTYPE).setValue(contactPerson.getContactType());
	}

	private ContactPersonBean translateDetails()
	{
		ContactPersonBean contactPerson = new ContactPersonBeanImpl();

		contactPerson.setContactID(this.contactDetails.getFieldValue(PageConstants.CONTACTID));
		contactPerson.setCompany(this.contactDetails.getFieldValue(PageConstants.COMPANY));
		contactPerson.setGivenName(this.contactDetails.getFieldValue(PageConstants.GIVENNAME));
		contactPerson.setSurName(this.contactDetails.getFieldValue(PageConstants.SURNAME));
		contactPerson.setContactType(this.contactDetails.getFieldValue(PageConstants.CONTACTTYPE));
		contactPerson.setEmailAddress(this.contactDetails.getFieldValue(PageConstants.EMAILADDRESS));
		contactPerson.setTelephoneNumber(this.contactDetails.getFieldValue(PageConstants.TELEPHONENUMBER));
		contactPerson.setModified(true);

		return contactPerson;
	}
}
