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
package com.qut.middleware.esoemanager.client.ui.panels;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.qut.gwtuilib.client.RegexConstants;
import com.qut.gwtuilib.client.display.FlexibleTable;
import com.qut.gwtuilib.client.display.Loader;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventController;
import com.qut.gwtuilib.client.eventdriven.events.MessageEvent;
import com.qut.gwtuilib.client.exceptions.InvalidContentException;
import com.qut.gwtuilib.client.input.ConfirmationStyledButton;
import com.qut.gwtuilib.client.input.StyledButton;
import com.qut.gwtuilib.client.input.ValidatingTextBox;
import com.qut.middleware.esoemanager.client.CSSConstants;
import com.qut.middleware.esoemanager.client.EsoeManager;
import com.qut.middleware.esoemanager.client.EsoeManagerConstants;
import com.qut.middleware.esoemanager.client.events.EventConstants;
import com.qut.middleware.esoemanager.client.rpc.EsoeManagerServiceAsync;
import com.qut.middleware.esoemanager.client.rpc.bean.ServiceContact;
import com.qut.middleware.esoemanager.client.ui.data.ContactListing;

public class ContactsPanel extends VerticalPanel
{
	private EsoeManagerServiceAsync contentService;
	private String serviceIdentifier;
	private String areaID;

	private SimplePanel content;
	private FlexibleTable contacts;
	private Loader loader;

	public ContactsPanel(String serviceIdentifier)
	{
		this.contentService = EsoeManager.contentService;
		this.serviceIdentifier = serviceIdentifier;
		this.areaID = EsoeManagerConstants.areaID;

		this.createInterface();
		this.contentService.retrieveServiceContacts(this.serviceIdentifier, new ServiceHandler());
	}

	private void createInterface()
	{
		this.clear();
		
		Label title = new Label("Service Contacts");
		title.addStyleName(CSSConstants.esoeManagerSubSectionTitle);
		
		this.content = new SimplePanel();
		this.contacts = new FlexibleTable(5, 5);

		this.contacts.insertHeading(0, "ID");
		this.contacts.insertHeading(1, "Name");
		this.contacts.insertHeading(2, "Email Address");
		this.contacts.insertHeading(3, "Telephone");
		this.contacts.insertHeading(4, "Company");
		this.contacts.insertHeading(5, "Type");

		this.content.add(this.contacts);

		// display our loader until we get some data back
		this.loader = new Loader();
		this.add(this.loader);
		
		this.add(title);
		this.add(this.content);
		createNewContactInterface();
	}

	private void createNewContactInterface()
	{
		VerticalPanel create = new VerticalPanel();

		Label createLbl = new Label("Create new contact for this service");
		createLbl.addStyleName(CSSConstants.esoeManagerSubSectionTitle);

		FlexibleTable input = new FlexibleTable(5, 5);
		
		/* ID */
		Label idLbl = new Label("ID");
		idLbl.addStyleName(CSSConstants.serviceValueTitle);
		final ValidatingTextBox newID = new ValidatingTextBox(1, RegexConstants.numeric,
				"ID must be numeric and unique", this.areaID);
		
		/* Name */
		Label nameLbl = new Label("Name");
		nameLbl.addStyleName(CSSConstants.serviceValueTitle);
		final ValidatingTextBox newName = new ValidatingTextBox(1, RegexConstants.commonName,
				"Name must contain letters and numbers only", this.areaID);
		
		/* Email */
		Label emailLbl = new Label("Email");
		emailLbl.addStyleName(CSSConstants.serviceValueTitle);
		final ValidatingTextBox newEmail = new ValidatingTextBox(1, RegexConstants.emailAddress,
				"Email must be in standard name@server format", this.areaID);
		
		/* Telphone */
		Label telephoneLbl = new Label("Telephone");
		telephoneLbl.addStyleName(CSSConstants.serviceValueTitle);
		final ValidatingTextBox newTelephone = new ValidatingTextBox(1, RegexConstants.telephoneNumber,
				"Telephone number must only container numbers", this.areaID);
		
		/* Company */
		Label companyLbl = new Label("Company");
		companyLbl.addStyleName(CSSConstants.serviceValueTitle);
		final ValidatingTextBox newCompany = new ValidatingTextBox(1, RegexConstants.commonName,
				"Company must contain letters and numbers only", this.areaID);
		
		/* Type */
		Label typeLbl = new Label("Type");
		typeLbl.addStyleName(CSSConstants.serviceValueTitle);
		final ListBox types = new ListBox();
		types.addItem("technical");
		types.addItem("administrative");
		types.addItem("support");
		types.addItem("billing");
		

		final Loader newContactLoader = new Loader();
		newContactLoader.setVisible(false);
		StyledButton createBtn = new StyledButton("createcontact", "Create Contact");
		createBtn.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				try
				{
					contentService.createServiceContact(ContactsPanel.this.serviceIdentifier, newID.getText(), newName.getText(), newEmail.getText(), newTelephone.getText(), newCompany.getText(), types.getItemText(types.getSelectedIndex()), new SaveContactHandler(newContactLoader));
					newContactLoader.setVisible(true);
					newID.setText(null);
					newName.setText(null);
					newEmail.setText(null);
					newTelephone.setText(null);
					newCompany.setText(null);
					
				}
				catch (InvalidContentException e)
				{
				}
			}
		});
		
		input.insertWidget(idLbl);
		input.insertWidget(newID);
		input.nextRow();

		input.insertWidget(nameLbl);
		input.insertWidget(newName);
		input.nextRow();
		
		input.insertWidget(emailLbl);
		input.insertWidget(newEmail);
		input.nextRow();
		
		input.insertWidget(telephoneLbl);
		input.insertWidget(newTelephone);
		input.nextRow();
		
		input.insertWidget(companyLbl);
		input.insertWidget(newCompany);
		input.nextRow();
		
		input.insertWidget(typeLbl);
		input.insertWidget(types);
		input.nextRow();
		
		input.insertWidget(createBtn);
		input.insertWidget(newContactLoader);
		input.nextRow();

		create.add(createLbl);
		create.add(input);

		this.add(create);
	}

	private void populateInterface(List<ServiceContact> contacts)
	{	
		this.contacts.clear();
		for (ServiceContact contact : contacts)
		{
			final ContactListing listing = new ContactListing(this.areaID);
			listing.setContactID(contact.getContactID());
			listing.setName(contact.getName());
			listing.setEmail(contact.getEmail());
			listing.setTelephone(contact.getTelephone());
			listing.setCompany(contact.getCompany());
			listing.setType(contact.getType());

			listing.createRow(this.contacts);

			final Loader saveLoader = new Loader();
			saveLoader.setVisible(false);
			ConfirmationStyledButton save = new ConfirmationStyledButton("Save contact details?", "save", "");
			save.addClickListener(new ClickListener()
			{
				public void onClick(Widget sender)
				{
					try
					{
						contentService.saveServiceContact(ContactsPanel.this.serviceIdentifier, listing.getContactID(), listing.getName(),
								listing.getEmail(), listing.getTelephone(), listing.getCompany(), listing.getType(),
								new SaveContactHandler(saveLoader));
						saveLoader.setVisible(true);
					}
					catch (InvalidContentException e)
					{
					}
				}
			});
			ConfirmationStyledButton delete = new ConfirmationStyledButton("Delete this contact?", "delete", "");
			delete.addClickListener(new ClickListener()
			{
				public void onClick(Widget sender)
				{
						contentService.deleteServiceContact(ContactsPanel.this.serviceIdentifier, listing.getContactID(), new DeleteContactHandler());
						saveLoader.setVisible(true);
				}
			});
			this.contacts.insertWidget(save);
			this.contacts.insertWidget(delete);
			this.contacts.nextRow();
		}
	}

	private class SaveContactHandler implements AsyncCallback
	{
		Loader loader;

		public SaveContactHandler(Loader loader)
		{
			this.loader = loader;
		}

		public void onFailure(Throwable caught)
		{
			this.loader.setVisible(false);
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, ContactsPanel.this.areaID,
					MessageEvent.error,
					"Unable to save contact details to the server, please contact a system administrator Server response: "
							+ caught.getLocalizedMessage()));
		}

		public void onSuccess(Object result)
		{
			this.loader.setVisible(false);
			ContactsPanel.this.contentService.retrieveServiceContacts(ContactsPanel.this.serviceIdentifier, new ServiceHandler());
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, ContactsPanel.this.areaID,
					MessageEvent.ok, "Contact details saved successfully, updating contacts..."));
		}
	}
	
	private class DeleteContactHandler implements AsyncCallback
	{

		public void onFailure(Throwable caught)
		{
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, ContactsPanel.this.areaID,
					MessageEvent.error,
					"Unable to delete contact details from the server, please contact a system administrator Server response: "
							+ caught.getLocalizedMessage()));
		}

		public void onSuccess(Object result)
		{
			ContactsPanel.this.contentService.retrieveServiceContacts(ContactsPanel.this.serviceIdentifier, new ServiceHandler());
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, ContactsPanel.this.areaID,
					MessageEvent.ok, "Contact details deleted successfully"));
		}
	}

	private class ServiceHandler implements AsyncCallback
	{
		public void onFailure(Throwable caught)
		{
			ContactsPanel.this.remove(ContactsPanel.this.loader);
			EventController
					.executeEvent(new MessageEvent(EventConstants.userMessage, ContactsPanel.this.areaID,
							MessageEvent.error,
							"Unable to obtain list of service contacts from the server, please contact a system administrator"));
		}

		public void onSuccess(Object result)
		{
			ContactsPanel.this.remove(ContactsPanel.this.loader);
			if (result != null)
				ContactsPanel.this.populateInterface((List<ServiceContact>) result);
			else
				EventController.executeEvent(new MessageEvent(EventConstants.userMessage, ContactsPanel.this.areaID,
						MessageEvent.error,
						"Unable to obtain valid contacts data from the server, please contact a system administrator"));
		}
	}
}
