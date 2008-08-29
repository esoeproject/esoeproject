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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.qut.gwtuilib.client.RegexConstants;
import com.qut.gwtuilib.client.display.FlexibleTable;
import com.qut.gwtuilib.client.display.IntegratedTextBox;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventController;
import com.qut.gwtuilib.client.eventdriven.events.MessageEvent;
import com.qut.gwtuilib.client.exceptions.InvalidContentException;
import com.qut.gwtuilib.client.input.ConfirmationStyledButton;
import com.qut.middleware.esoemanager.client.CSSConstants;
import com.qut.middleware.esoemanager.client.EsoeManager;
import com.qut.middleware.esoemanager.client.EsoeManagerConstants;
import com.qut.middleware.esoemanager.client.events.EventConstants;
import com.qut.middleware.esoemanager.client.events.ShowServiceDetailsEvent;
import com.qut.middleware.esoemanager.client.rpc.bean.ServiceContact;

public class ServiceCreationPanel extends VerticalPanel
{
	// Entity details
	IntegratedTextBox entityID;
	
	// The service details
	IntegratedTextBox serviceName;
	IntegratedTextBox serviceURL;
	IntegratedTextBox serviceDescription;
	
	// Node details
	IntegratedTextBox entityHost;
	IntegratedTextBox primaryNode;
	
	// Initial Contact Details
	IntegratedTextBox contactID;
	IntegratedTextBox contactName;
	IntegratedTextBox contactEmail;
	IntegratedTextBox contactPhone;
	IntegratedTextBox contactCompany;
	
	VerticalPanel createPanel;
	VerticalPanel createdPanel;
	
	FlexibleTable input;
	
	String lastCreatedServiceID;
		
	public ServiceCreationPanel()
	{
		this.createInterface();
	}
	
	private void createInterface()
	{
		this.input = new FlexibleTable(5, 5);
		this.createServiceInterface();
		this.createCompletedInterface();

		this.add(this.createPanel);
		this.add(this.createdPanel);
	}
	
	private void createCompletedInterface()
	{
		this.createdPanel = new VerticalPanel();
		this.createdPanel.setVisible(false);
		
		Label serviceCreated = new Label("The service has been successfully created");
		Hyperlink serviceLink = new Hyperlink("View service details", false, null);
		Hyperlink createLink = new Hyperlink("Create another service", false, null);
		
		serviceLink.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				ShowServiceDetailsEvent event = new ShowServiceDetailsEvent(EventConstants.showServiceDetails, lastCreatedServiceID);
				EventController.executeEvent(event);
			}		
		});
		
		createLink.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				createdPanel.setVisible(false);
				createPanel.setVisible(true);
			}		
		});
		
		this.createdPanel.add(serviceCreated);
		this.createdPanel.add(serviceLink);
		this.createdPanel.add(createLink);
	}
	
	private void createServiceInterface()
	{		
		this.createPanel = new VerticalPanel();
		Label serviceDetails = new Label("Service Details");
		serviceDetails.addStyleName(CSSConstants.dashTitle);
		this.input.insertWidget(serviceDetails);
		this.input.nextRow();
		
		entityID = new IntegratedTextBox(this, 1, RegexConstants.URLRegex, "EntityID is required", "EntityID", EsoeManagerConstants.areaID);
		entityID.addTableRow(this.input);
		
		serviceName = new IntegratedTextBox(this, 1, RegexConstants.matchAll, "Service Name is required", "Service Name", EsoeManagerConstants.areaID);
		serviceName.addTableRow(this.input);
		
		serviceURL = new IntegratedTextBox(this, 1, RegexConstants.URLRegex, "Service URL is required and must be in protocol://server.address format", "Service Content URL", EsoeManagerConstants.areaID);
		serviceURL.addTableRow(this.input);
		
		serviceDescription = new IntegratedTextBox(this, 1, RegexConstants.matchAll, "Service Description is required", "Service Description", EsoeManagerConstants.areaID);
		serviceDescription.addTableRow(this.input);
		
		entityHost = new IntegratedTextBox(this, 1, RegexConstants.URLRegex, "Primary service host is required and must be in protocol://server.address format", "Primary Service Host", EsoeManagerConstants.areaID);
		entityHost.addTableRow(this.input);
		
		primaryNode = new IntegratedTextBox(this, 1, RegexConstants.URLRegex, "First service node is required and must be in protocol://server.address format", "First Service Node", EsoeManagerConstants.areaID);
		primaryNode.addTableRow(this.input);
		
		Label technicalContact = new Label("Technical Contact");
		technicalContact.addStyleName(CSSConstants.dashTitle);
		this.input.insertWidget(technicalContact);
		this.input.nextRow();
		
		/* ID */
		contactID = new IntegratedTextBox(this, 1, RegexConstants.numeric, "ID must be numeric and unique", "ID", EsoeManagerConstants.areaID);
		contactID.addTableRow(input);
		
		/* Name */
		contactName = new IntegratedTextBox(this, 1, RegexConstants.commonName, "Name must contain letters and numbers only", "Name", EsoeManagerConstants.areaID);
		contactName.addTableRow(input);
		
		/* Email */
		contactEmail = new IntegratedTextBox(this, 1, RegexConstants.emailAddress,
				"Email must be in standard name@server format", "Email Address", EsoeManagerConstants.areaID);
		contactEmail.addTableRow(input);
		
		/* Telphone */
		contactPhone = new IntegratedTextBox(this, 1, RegexConstants.telephoneNumber,
				"Telephone number must only container numbers", "Telephone Number", EsoeManagerConstants.areaID);
		contactPhone.addTableRow(input);
		
		contactCompany = new IntegratedTextBox(this, 1, RegexConstants.commonName, "Company must contain letters and numbers only", "Company", EsoeManagerConstants.areaID);
		contactCompany.addTableRow(input);
		
		this.createPanel.add(input);
		
		ConfirmationStyledButton create = new ConfirmationStyledButton("Create new service?", "createcontact", "Create Service");
		create.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				try
				{
					ServiceContact contact = new ServiceContact();
					contact.setContactID(contactID.getText());
					contact.setName(contactName.getText());
					contact.setEmail(contactEmail.getText());
					contact.setTelephone(contactPhone.getText());
					contact.setCompany(contactCompany.getText());
					contact.setType("technical");
					EsoeManager.contentService.createService(entityID.getText(), serviceName.getText(), serviceURL.getText(), serviceDescription.getText(), entityHost.getText(), primaryNode.getText(), contact, new ServiceCreationHandler());
				}
				catch (InvalidContentException e)
				{
				}			
			}
		});
		
		this.createPanel.setHorizontalAlignment(ALIGN_RIGHT);
		this.createPanel.add(create);
	}

	private class ServiceCreationHandler implements AsyncCallback
	{

		public void onFailure(Throwable caught)
		{
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
					MessageEvent.error,
					"Unable to create new service, please contact a system administrator. Server response: "
							+ caught.getLocalizedMessage()));
		}

		public void onSuccess(Object result)
		{
			lastCreatedServiceID = (String) result;
			
			EventController
					.executeEvent(new MessageEvent(
							EventConstants.userMessage,
							EsoeManagerConstants.areaID,
							MessageEvent.ok,
							"Created new service successfully. Service is currently inactive and requires policies to be secured"));
			
			try
			{
				entityID.setText(null);
				entityHost.setText(null);
				primaryNode.setText(null);
				serviceName.setText(null);
				serviceURL.setText(null);
				serviceDescription.setText(null);
				contactID.setText(null);
				contactName.setText(null);
				contactEmail.setText(null);
				contactPhone.setText(null);
				contactCompany.setText(null);
			}
			catch (InvalidContentException e)
			{
			}
			
			createPanel.setVisible(false);
			createdPanel.setVisible(true);
		}
	}
}
