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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.qut.gwtuilib.client.display.FlexibleTable;
import com.qut.gwtuilib.client.display.Loader;
import com.qut.gwtuilib.client.eventdriven.eventmgr.BaseEvent;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventController;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventListener;
import com.qut.gwtuilib.client.eventdriven.events.MessageEvent;
import com.qut.middleware.esoemanager.client.CSSConstants;
import com.qut.middleware.esoemanager.client.EsoeManager;
import com.qut.middleware.esoemanager.client.EsoeManagerConstants;
import com.qut.middleware.esoemanager.client.events.EventConstants;
import com.qut.middleware.esoemanager.client.rpc.bean.SimpleServiceListing;
import com.qut.middleware.esoemanager.client.ui.data.ServiceListing;

public class ServicesPanel extends VerticalPanel implements EventListener
{
	/* All events this editor instance will respond to */
	private final String[] registeredEvents = { EventConstants.showService, EventConstants.showServiceList };

	private FlexibleTable services;

	Loader loader;

	public ServicesPanel()
	{
		super();

		this.createInterface();
		EventController.registerListener(this);
	}

	private void createInterface()
	{
		this.createPanelTitle();
		
		this.services = new FlexibleTable(5, 10);
		this.services.insertHeading(0, "Name");
		this.services.insertHeading(1, "URL");
		this.services.insertHeading(2, "Status");
		this.add(ServicesPanel.this.services);
		
		this.createServiceListing();		
	}	
	
	private void createPanelTitle()
	{
		Label serviceTitleLbl = new Label("Available Services");
		serviceTitleLbl.addStyleName(CSSConstants.esoeManagerSectionTitle);
		this.add(serviceTitleLbl);
	}
	
	private void createServiceListing()
	{
		this.loader = new Loader();
		this.add(this.loader);
		EsoeManager.contentService.retrieveSimpleServiceListing(new ServiceListingHandler());
	}

	private class ServiceListingHandler implements AsyncCallback
	{
		public void onFailure(Throwable caught)
		{
			ServicesPanel.this.loader.setVisible(false);
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
					MessageEvent.error,
					"Unable to obtain list of services from the server, please contact a system administrator"));
		}

		public void onSuccess(Object result)
		{
			ServicesPanel.this.loader.setVisible(false);
			ServicesPanel.this.services.clear();
			
			if(result != null)
			{
				List<SimpleServiceListing> listing = (List<SimpleServiceListing>) result;
				if(listing.size() > 0)
				for(SimpleServiceListing simpleServiceListing : listing)
				{
					ServiceListing serviceListing = new ServiceListing();
					serviceListing.setServiceName(simpleServiceListing.getServiceName());
					serviceListing.setServiceURL(simpleServiceListing.getServiceURL());
					serviceListing.setServiceActivated(simpleServiceListing.isActive());
					serviceListing.setServiceIdentifier(simpleServiceListing.getIdentifier());
					
					serviceListing.createRow(ServicesPanel.this.services);
				}
				else
				{
					ServicesPanel.this.services.insertWidget(new Label("You currently don't have any services granted to you"));
				}
			}
		}
	}

	public void executeEvent(BaseEvent event)
	{
		if(event.getName().equals(EventConstants.showServiceList))
		{
			this.setVisible(true);
			EsoeManager.contentService.retrieveSimpleServiceListing(new ServiceListingHandler());
		}
			
		if(event.getName().equals(EventConstants.showService))
			this.setVisible(false);
			
	}

	public String[] getRegisteredEvents()
	{
		return this.registeredEvents;
	}
}
