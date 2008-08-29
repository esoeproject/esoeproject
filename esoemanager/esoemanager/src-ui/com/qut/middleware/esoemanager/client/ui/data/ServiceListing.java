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
package com.qut.middleware.esoemanager.client.ui.data;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.qut.gwtuilib.client.display.FlexibleTable;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventController;
import com.qut.gwtuilib.client.input.StyledButton;
import com.qut.middleware.esoemanager.client.events.EventConstants;
import com.qut.middleware.esoemanager.client.events.ShowServiceDetailsEvent;
import com.qut.middleware.esoemanager.client.ui.ActiveState;

public class ServiceListing
{
	private HorizontalPanel content;
	private Label serviceName;
	private Label serviceURL;
	private ActiveState state;
	private StyledButton edit;
	
	private String serviceIdentifier;

	public ServiceListing()
	{
		this.createInterface();
		this.createHandlers();
		
	}
	
	private void createInterface()
	{		
		this.serviceName = new Label();
		this.serviceURL = new Label();
		this.state = new ActiveState();
		this.edit = new StyledButton("edit", "Details");
	}
	
	private void createHandlers()
	{	
		this.edit.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				EventController.executeEvent(new ShowServiceDetailsEvent(EventConstants.showServiceDetails, ServiceListing.this.serviceIdentifier));
			}	
		});
	}
	
	public void createRow(FlexibleTable table)
	{
		table.insertWidget(this.serviceName);
		table.insertWidget(this.serviceURL);
		table.insertWidget(this.state);
		table.insertWidget(this.edit);
		table.nextRow();
	}
	
	public void setServiceName(String serviceName)
	{
		this.serviceName.setText(serviceName);
	}
	
	public void setServiceURL(String serviceURL)
	{
		this.serviceURL.setText(serviceURL);
	}
	
	public void setServiceActivated(boolean serviceActivated)
	{
		if(serviceActivated)
			this.state.setActivated();
		else
			this.state.setDeactivated();
	}
		
	/**
	 * @return the serviceIdentifier
	 */
	public String getServiceIdentifier()
	{
		return serviceIdentifier;
	}

	/**
	 * @param serviceIdentifier the serviceIdentifier to set
	 */
	public void setServiceIdentifier(String serviceIdentifier)
	{
		this.serviceIdentifier = serviceIdentifier;
	}
}
