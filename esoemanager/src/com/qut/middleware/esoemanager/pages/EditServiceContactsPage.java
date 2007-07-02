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

import java.util.List;
import java.util.Vector;

import net.sf.click.control.Field;
import net.sf.click.control.Submit;

import org.apache.log4j.Logger;

import com.qut.middleware.esoemanager.bean.ContactPersonBean;
import com.qut.middleware.esoemanager.exception.EditServiceContactException;
import com.qut.middleware.esoemanager.logic.EditServiceContactsLogic;
import com.qut.middleware.esoemanager.pages.forms.impl.ContactForm;

public class EditServiceContactsPage extends ContactPersonPage
{
	private EditServiceContactsLogic logic;

	public String entityID;
	public String action;
	public String contactID;
	public String confirm;

	public String actionLabel;

	/* Local logging instance */
	private Logger logger = Logger.getLogger(EditServiceContactsPage.class.getName());

	public EditServiceContactsPage()
	{
		this.contactDetails = new ContactForm();
	}

	@Override
	public void onInit()
	{
		this.contactDetails.init();

		Submit completeButton = new Submit(PageConstants.NAV_COMPLETE_LABEL, this, PageConstants.NAV_COMPLETE_FUNC);
		this.contactDetails.add(completeButton);

		if (this.entityID != null)
		{
			/* Store reference to serviceID for future interactions across multiple form submissions */
			this.storeSession(PageConstants.STORED_ENTITY_ID, this.entityID);

			/*
			 * Refresh contacts whenerver serviceID is being submitted as this is a new edit attempt and things may get
			 * out of sync
			 */
			try
			{
				this.contacts = this.logic.getServiceContacts(this.entityID);
				this.storeSession(PageConstants.STORED_CONTACTS, this.contacts);
			}
			catch (EditServiceContactException e)
			{
				this.storeSession(PageConstants.STORED_CONTACTS, null);
			}
		}
		else
		{
			this.entityID = (String) this.retrieveSession(PageConstants.STORED_ENTITY_ID);
		}

		if (this.entityID == null)
		{
			setError();
		}

		if (this.contacts == null)
		{
			this.contacts = (Vector<ContactPersonBean>) this.retrieveSession(PageConstants.STORED_CONTACTS);

			/* If contacts can't be retrieved from session then can't proceed */
			if (this.contacts == null)
			{
				setError();
			}
		}

	}

	@Override
	public void onGet()
	{
		/* Determine action, none or unknown default to add which requires no additional processing */
		for (ContactPersonBean contact : this.contacts)
		{
			/* Determine which contact is being edited or deleted (skip newley created contacts with null ID's) */
			if (contact.getContactID() != null && contact.getContactID().equals(this.contactID))
			{
				if (this.action.equals(PageConstants.EDIT))
				{
					translateDetails(contact);
					this.actionLabel = PageConstants.EDIT_SERVICE_CONTACT + " " + contact.getGivenName() + " "
							+ contact.getSurName();
					return;
				}
				if (this.action.equals(PageConstants.DELETE) && this.confirm != null
						&& this.confirm.equals(PageConstants.CONFIRMED))
				{
					/* Do not allow removal of the last stored contact */
					if (this.contacts.size() == 1)
					{
						this.actionLabel = PageConstants.DELETE_SERVICE_CONTACT_DENIED;
						this.action = null;
						this.confirm = null;
						return;
					}

					this.actionLabel = PageConstants.DELETE_SERVICE_CONTACT + " " + contact.getGivenName() + " "
							+ contact.getSurName();

					try
					{
						this.logic.deleteServiceContact(this.entityID, contact.getContactID());
					}
					catch (EditServiceContactException e)
					{
						setError();
					}

					this.contacts.removeElement(contact);
					this.action = null;
					this.confirm = null;
					return;
				}
			}
		}
	}

	@Override
	public void onPost()
	{
		super.onPost();

		if (this.contactDetails.isValid())
		{
			String redirectPath;

			/* Move allow users to enter additional contacts, send current form back */
			redirectPath = getContext().getPagePath(EditServiceContactsPage.class);
			setRedirect(redirectPath);
		}
	}

	public boolean completeClick()
	{
		/* If the user has submitted form content then attempt to save last input before proceeding */
		for (Field field : (List<Field>) this.contactDetails.getFieldList())
		{
			if (!field.isHidden() && !field.getName().equals(PageConstants.CONTACTTYPE)
					&& field.getValue().length() > 0)
			{
				createOrUpdateContact();
				if (!this.contactDetails.isValid())
				{
					/* Make user fix submitted errors */
					return true;
				}
			}
		}

		try
		{
			this.logic.updateServiceContacts(this.entityID, this.contacts);
		}
		catch (EditServiceContactException e)
		{
			this.logger.error("EditServiceContactException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);

			setError();
		}

		/* Move users to completed view */
		this.action = PageConstants.COMPLETED;
		cleanSession();
		return false;
	}

	private void setError()
	{
		/* Move users to error view */
		this.action = PageConstants.ERROR;
		cleanSession();		
	}
	
	private void cleanSession()
	{
		this.removeSession(PageConstants.STORED_ENTITY_ID);
		this.removeSession(PageConstants.STORED_CONTACTS);
	}

	public EditServiceContactsLogic getEditServiceContactsLogic()
	{
		return this.logic;
	}

	public void setEditServiceContactsLogic(EditServiceContactsLogic logic)
	{
		this.logic = logic;
	}
}
