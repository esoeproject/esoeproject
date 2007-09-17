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

import net.sf.click.control.Field;
import net.sf.click.control.Submit;

import org.apache.log4j.Logger;

import com.qut.middleware.esoemanager.bean.ContactPersonBean;
import com.qut.middleware.esoemanager.bean.ServiceBean;
import com.qut.middleware.esoemanager.bean.impl.ServiceBeanImpl;
import com.qut.middleware.esoemanager.exception.EditServiceContactException;
import com.qut.middleware.esoemanager.logic.EditServiceContactsLogic;
import com.qut.middleware.esoemanager.pages.forms.impl.ContactForm;

public class EditServiceContactsPage extends ContactPersonPage
{
	private EditServiceContactsLogic logic;

	public String eid;
	public String action;
	public String contactID;
	public String confirm;
	
	public ServiceBean serviceBean;

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
		serviceBean = (ServiceBean) this.retrieveSession(ServiceBean.class.getName());
		if(serviceBean == null)
		{
			serviceBean = new ServiceBeanImpl();
			this.storeSession(ServiceBean.class.getName(), serviceBean);
		}
		
		this.contactDetails.init();

		Submit completeButton = new Submit(PageConstants.NAV_COMPLETE_LABEL, this, PageConstants.NAV_COMPLETE_FUNC);
		this.contactDetails.add(completeButton);

		if (this.eid != null)
		{
			/* Store reference to serviceID for future interactions across multiple form submissions */
			serviceBean.setEntID(new Integer(this.eid));

			/*
			 * Refresh contacts whenever serviceID is being submitted as this is a new edit attempt and things may get
			 * out of sync
			 */
			try
			{
				serviceBean.setContacts (this.logic.getServiceContacts(new Integer(this.eid)));
			}
			catch (EditServiceContactException e)
			{
				serviceBean.setContacts(null);
			}
		}
		else
		{
			if(this.serviceBean == null)
				setError();
			
			this.eid = (String) this.serviceBean.getEntityID();
		}

	}

	@Override
	public void onGet()
	{
		if(this.serviceBean == null)
			setError();
		
		/* Determine action, none or unknown default to add which requires no additional processing */
		for (ContactPersonBean contact : this.serviceBean.getContacts())
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
					if (this.serviceBean.getContacts().size() == 1)
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
						this.logic.deleteServiceContact(new Integer(this.eid), contact.getContactID());
					}
					catch (EditServiceContactException e)
					{
						setError();
					}

					this.serviceBean.getContacts().removeElement(contact);
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
		if(this.serviceBean == null)
			setError();
		
		super.createOrUpdateContact(serviceBean);

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
		if(this.serviceBean == null )
			setError();
		
		/* If the user has submitted form content then attempt to save last input before proceeding */
		for (Field field : (List<Field>) this.contactDetails.getFieldList())
		{
			if (!field.isHidden() && !field.getName().equals(PageConstants.CONTACTTYPE)
					&& field.getValue().length() > 0)
			{
				createOrUpdateContact(serviceBean);
				if (!this.contactDetails.isValid())
				{
					/* Make user fix submitted errors */
					return true;
				}
				break;
			}
		}

		try
		{
			this.logic.updateServiceContacts(this.serviceBean.getEntID(), this.serviceBean.getContacts());
		}
		catch (EditServiceContactException e)
		{
			this.logger.error("EditServiceContactException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);

			setError();
		}

		/* Move users to completed view */
		this.action = PageConstants.COMPLETED;
		
		/* Set eid so user may return to listing */
		this.eid = serviceBean.getEntID().toString();
		
		cleanUp();
		return false;
	}
	
	private void cleanUp()
	{
		this.removeSession(ServiceBean.class.getName());
	}

	private void setError()
	{
		/* Move users to error view */
		this.action = PageConstants.ERROR;	
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
