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
package com.qut.middleware.esoemanager.client.ui;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SourcesTabEvents;
import com.google.gwt.user.client.ui.TabListener;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.qut.gwtuilib.client.eventdriven.eventmgr.BaseEvent;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventController;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventListener;
import com.qut.gwtuilib.client.eventdriven.events.MessageEvent;
import com.qut.middleware.esoemanager.client.EsoeManager;
import com.qut.middleware.esoemanager.client.EsoeManagerConstants;
import com.qut.middleware.esoemanager.client.events.EventConstants;
import com.qut.middleware.esoemanager.client.events.ShowServiceDetailsEvent;
import com.qut.middleware.esoemanager.client.ui.panels.AttributesPanel;
import com.qut.middleware.esoemanager.client.ui.panels.CreditsPanel;
import com.qut.middleware.esoemanager.client.ui.panels.CryptoPanel;
import com.qut.middleware.esoemanager.client.ui.panels.DashboardPanel;
import com.qut.middleware.esoemanager.client.ui.panels.MetadataPanel;
import com.qut.middleware.esoemanager.client.ui.panels.ServiceCreationPanel;
import com.qut.middleware.esoemanager.client.ui.panels.ServicePanel;
import com.qut.middleware.esoemanager.client.ui.panels.ServicesPanel;

public class CorePanel extends VerticalPanel implements EventListener
{
	/* All events this class instance will respond to */
	private final String[] registeredEvents =
	{
		EventConstants.showServiceDetails
	};

	private TabPanel serviceComponents;

	private DashboardPanel dashboardPanel;
	private CryptoPanel cryptoPanel;
	private AttributesPanel attributesPanel;
	private MetadataPanel metadataPanel;
	private ServicesPanel servicesPanel;
	private ServicePanel servicePanel;
	private ServiceCreationPanel serviceCreationPanel;
	private CreditsPanel creditsPanel;

	private String esoeID;

	public CorePanel(String esoeID)
	{
		super();
		this.esoeID = esoeID;
		EsoeManager.contentService.isSuperUser(new SuperUserHandler());
		EventController.registerListener(this);
	}

	private void createInterface(boolean superUser)
	{
		this.serviceComponents = new TabPanel();

		if (superUser)
		{
			this.cryptoPanel = new CryptoPanel(esoeID);
			this.attributesPanel = new AttributesPanel(esoeID);
			this.serviceCreationPanel = new ServiceCreationPanel();
		}

		this.dashboardPanel = new DashboardPanel(esoeID);
		this.metadataPanel = new MetadataPanel(esoeID);
		this.servicesPanel = new ServicesPanel();
		this.servicePanel = new ServicePanel();
		this.creditsPanel = new CreditsPanel();

		VerticalPanel serviceManager = new VerticalPanel();
		serviceManager.add(servicesPanel);
		serviceManager.add(servicePanel);

		this.serviceComponents.add(this.dashboardPanel, "Dashboard");
		this.serviceComponents.add(serviceManager, "Services");

		if (superUser)
		{
			this.serviceComponents.add(this.cryptoPanel, "Cryptography");
			this.serviceComponents.add(this.attributesPanel, "Attributes");
			this.serviceComponents.add(this.serviceCreationPanel, "Service Creation");
		}
		
		this.serviceComponents.add(this.metadataPanel, "Network Metadata");
		this.serviceComponents.add(this.creditsPanel, "Credits");

		this.serviceComponents.selectTab(0);
		this.serviceComponents.addTabListener(new TabListener()
		{
			public boolean onBeforeTabSelected(SourcesTabEvents sender, int tabIndex)
			{
				if (tabIndex == 1)
					EventController.executeEvent(new BaseEvent(EventConstants.showServiceList));

				return true;
			}

			public void onTabSelected(SourcesTabEvents sender, int tabIndex)
			{
			}
		});

		this.add(this.serviceComponents);
	}

	public void executeEvent(BaseEvent event)
	{
		if (event.getName().equals(EventConstants.showServiceDetails))
		{
			ShowServiceDetailsEvent serviceDetails = (ShowServiceDetailsEvent) event;
			serviceComponents.selectTab(1);
			ShowServiceDetailsEvent showServiceDetails = new ShowServiceDetailsEvent(EventConstants.showService,
					serviceDetails.getServiceID());
			EventController.executeEvent(showServiceDetails);
		}
	}

	public String[] getRegisteredEvents()
	{
		return this.registeredEvents;
	}

	private class SuperUserHandler implements AsyncCallback
	{
		public void onFailure(Throwable caught)
		{
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
					MessageEvent.error,
					"Unable to determine user access level contact a system administrator. Server response: "
							+ caught.getLocalizedMessage()));
		}

		public void onSuccess(Object result)
		{
			Boolean superUser = (Boolean) result;
			createInterface(superUser.booleanValue());
		}
	}
}
