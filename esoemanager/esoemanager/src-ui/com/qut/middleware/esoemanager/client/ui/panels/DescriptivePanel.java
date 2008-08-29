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
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.qut.gwtuilib.client.RegexConstants;
import com.qut.gwtuilib.client.display.FlexibleTable;
import com.qut.gwtuilib.client.display.HiddenIntegratedTextArea;
import com.qut.gwtuilib.client.display.HiddenIntegratedTextBox;
import com.qut.gwtuilib.client.display.Loader;
import com.qut.gwtuilib.client.eventdriven.eventmgr.BaseEvent;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventController;
import com.qut.gwtuilib.client.eventdriven.events.MessageEvent;
import com.qut.gwtuilib.client.exceptions.InvalidContentException;
import com.qut.gwtuilib.client.input.ConfirmationStyledButton;
import com.qut.gwtuilib.client.input.StyledButton;
import com.qut.middleware.esoemanager.client.CSSConstants;
import com.qut.middleware.esoemanager.client.EsoeManager;
import com.qut.middleware.esoemanager.client.EsoeManagerConstants;
import com.qut.middleware.esoemanager.client.events.EventConstants;
import com.qut.middleware.esoemanager.client.rpc.EsoeManagerServiceAsync;
import com.qut.middleware.esoemanager.client.rpc.bean.ExtendedServiceListing;
import com.qut.middleware.esoemanager.client.ui.ActiveState;

public class DescriptivePanel extends VerticalPanel
{
	private final String serviceNameRegex = RegexConstants.matchAll;
	private final String serviceURLRegex = RegexConstants.URLRegex;
	private final String serviceDescriptionRegex = RegexConstants.matchAll;
	private final String serviceAccessDeniedRegex = RegexConstants.matchAll;
	
	HiddenIntegratedTextBox serviceName;
	HiddenIntegratedTextBox serviceURL;
	private ActiveState activeState;
	HiddenIntegratedTextArea serviceDescription;
	HiddenIntegratedTextArea serviceAccessDeniedMessage;
	
	EsoeManagerServiceAsync contentService;
	String serviceIdentifier;
	private String areaID;
	
	private VerticalPanel content;
	private Loader loader;
	
	ConfirmationStyledButton toggle;
	StyledButton save;
	
	public DescriptivePanel(String serviceIdentifier)
	{
		this.contentService = EsoeManager.contentService;
		this.serviceIdentifier = serviceIdentifier;
		this.areaID = EsoeManagerConstants.areaID;
		
		this.createInterface();
		this.contentService.retrieveServiceListing(this.serviceIdentifier, new ServiceHandler());
	}
	
	private void createInterface()
	{
		this.clear();
		this.addStyleName(CSSConstants.esoeManagerDescription);
		this.content = new VerticalPanel();
		
		Label title = new Label("Service Description");
		title.addStyleName(CSSConstants.esoeManagerSubSectionTitle);
		
		FlexibleTable details = new FlexibleTable(5, 5);

		this.serviceName = new HiddenIntegratedTextBox(this, 1, this.serviceNameRegex, "Service name must have 1 character",
				"Name", this.areaID);
		this.serviceName.addTableRow(details);

		this.serviceURL = new HiddenIntegratedTextBox(this, 1, this.serviceURLRegex, "ServiceURL must be in a URL format", "URL", this.areaID);
		this.serviceURL.addTableRow(details);
		
		this.toggle = new ConfirmationStyledButton("Are you sure you wish to change the state of this service?", "toggle", "");
		this.toggle.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				if (activeState.isActive())
					contentService.toggleServiceState(serviceIdentifier, true, new StateHandler(loader));
				else
					contentService.toggleServiceState(serviceIdentifier, false, new StateHandler(loader));
				
				
				loader.setVisible(true);
			}
		});

		Label stateLbl = new Label("Active Status");
		stateLbl.addStyleName(CSSConstants.serviceValueTitle);
		this.activeState = new ActiveState();
		details.insertWidget(stateLbl);
		
		HorizontalPanel statePanel = new HorizontalPanel();
		statePanel.setSpacing(5);
		statePanel.add(this.activeState);
		statePanel.add(this.toggle);
		details.insertWidget(statePanel);
		details.nextRow();

		this.serviceDescription = new HiddenIntegratedTextArea(this, 1, this.serviceDescriptionRegex,
				"Description", "Service description must be completed",  this.areaID);
		this.serviceDescription.addTableRow(details);

		this.serviceAccessDeniedMessage = new HiddenIntegratedTextArea(this, 0, this.serviceAccessDeniedRegex,
				"Access Denied Message", "Service access denied message must be completed",  this.areaID);
		this.serviceAccessDeniedMessage.addTableRow(details);
		
		this.content.add(title);
		this.content.add(details);
		
		// display our loader until we get some data back
		this.loader = new Loader();
		this.add(this.loader);
	}
	
	private void populateInterface(ExtendedServiceListing service)
	{
		try
		{
			this.serviceName.setText(service.getServiceName());
			this.serviceURL.setText(service.getServiceURL());
			this.serviceDescription.setText(service.getServiceDescription());
			this.serviceAccessDeniedMessage.setText(service.getServiceAuthorizationFailureMessage());
			if (service.isActive())
				this.activeState.setActivated();
			else
				this.activeState.setDeactivated();
			
			VerticalPanel operations = new VerticalPanel();
			FlexibleTable operationsContent = new FlexibleTable(5, 5);
			operations.add(operationsContent);
			
			/* Create save functionality */
			final Loader saveLoader = new Loader();
			saveLoader.setVisible(false);

			this.save = new StyledButton("save", "Save");
			this.save.addClickListener(new ClickListener()
			{
				public void onClick(Widget sender)
				{
					try
					{
						DescriptivePanel.this.contentService.saveServiceDescription(DescriptivePanel.this.serviceIdentifier, DescriptivePanel.this.serviceName.getText(), DescriptivePanel.this.serviceURL.getText(), DescriptivePanel.this.serviceDescription.getText(), DescriptivePanel.this.serviceAccessDeniedMessage.getText(), new SaveDescriptionHandler(saveLoader));
						saveLoader.setVisible(true);
					}
					catch (InvalidContentException e)
					{
					}
				}
			});
			operationsContent.insertWidget(save);
			operationsContent.insertWidget(saveLoader);
			operationsContent.nextRow();

			operationsContent.nextRow();
			
			this.content.add(operations);
			this.add(content);
		}
		catch (InvalidContentException e)
		{
		}
	}
	
	private class SaveDescriptionHandler implements AsyncCallback
	{
		Loader loader;

		public SaveDescriptionHandler(Loader loader)
		{
			this.loader = loader;
		}

		public void onFailure(Throwable caught)
		{
			this.loader.setVisible(false);
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, DescriptivePanel.this.areaID,
					MessageEvent.error,
					"Unable to save service description on the server, please contact a system administrator. Server response: "
							+ caught.getLocalizedMessage()));
		}

		public void onSuccess(Object result)
		{
			this.loader.setVisible(false);
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, DescriptivePanel.this.areaID,
					MessageEvent.ok, "Service description saved successfully"));
			EventController.executeEvent(new BaseEvent(EventConstants.configurationChange));
		}
	}
	
	private class StateHandler implements AsyncCallback
	{
		Loader loader;

		public StateHandler(Loader loader)
		{
			this.loader = loader;
		}

		public void onFailure(Throwable caught)
		{
			this.loader.setVisible(false);
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, DescriptivePanel.this.areaID,
					MessageEvent.error,
					"Unable to obtain modify service active state on the server, please contact a system administrator. Server response: "
							+ caught.getLocalizedMessage()));
		}

		public void onSuccess(Object result)
		{
			this.loader.setVisible(false);
			if (activeState.isActive())
				activeState.setDeactivated();
			else
				activeState.setActivated();

			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, DescriptivePanel.this.areaID,
					MessageEvent.ok, "Service state changed successfully"));
		}
	}

	private class ServiceHandler implements AsyncCallback
	{
		public void onFailure(Throwable caught)
		{
			DescriptivePanel.this.loader.setVisible(false);
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, DescriptivePanel.this.areaID,
					MessageEvent.error,
					"Unable to obtain service details from the server, please contact a system administrator"));
		}

		public void onSuccess(Object result)
		{
			DescriptivePanel.this.loader.setVisible(false);
			if (result != null)
				DescriptivePanel.this.populateInterface((ExtendedServiceListing) result);
			else
				EventController.executeEvent(new MessageEvent(EventConstants.userMessage, DescriptivePanel.this.areaID,
						MessageEvent.error,
						"Unable to obtain valid service details from the server, please contact a system administrator"));
		}
	}
}
