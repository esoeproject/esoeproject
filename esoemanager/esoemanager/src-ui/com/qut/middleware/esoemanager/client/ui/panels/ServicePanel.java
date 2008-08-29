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

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.qut.gwtuilib.client.display.Loader;
import com.qut.gwtuilib.client.eventdriven.eventmgr.BaseEvent;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventController;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventListener;
import com.qut.middleware.esoemanager.client.CSSConstants;
import com.qut.middleware.esoemanager.client.events.EventConstants;
import com.qut.middleware.esoemanager.client.events.ShowServiceDetailsEvent;

public class ServicePanel extends VerticalPanel implements EventListener
{
	/* All events this class instance will respond to */
	private final String[] registeredEvents = { EventConstants.showService, EventConstants.showServiceList };

	private TabPanel serviceComponents;
		
	private Label serviceTitleLbl;
	private DescriptivePanel descriptivePanel;
	private ContactsPanel contactsPanel;
	private NodesPanel nodesPanel;
	private PoliciesPanel policiesPanel;
	private CryptoPanel cryptoPanel;
	private ConfigurationPanel configurationPanel;
	private StartupsPanel activationPanel;
	private ServiceMetadataPanel serviceMetadataPanel;

	Loader loader;

	public ServicePanel()
	{
		super();
		EventController.registerListener(this);
	}

	private void createInterface(String serviceIdentifier)
	{
		this.clear();
		this.serviceComponents = new TabPanel();
		this.createPanelTitle();
		
		this.descriptivePanel = new DescriptivePanel(serviceIdentifier);
		this.contactsPanel = new ContactsPanel(serviceIdentifier);
		this.nodesPanel = new NodesPanel(serviceIdentifier);
		this.policiesPanel = new PoliciesPanel(serviceIdentifier);
		this.cryptoPanel = new CryptoPanel(serviceIdentifier);
		this.configurationPanel = new ConfigurationPanel(serviceIdentifier);
		this.activationPanel = new StartupsPanel(serviceIdentifier);
		this.serviceMetadataPanel = new ServiceMetadataPanel(serviceIdentifier);
		
		this.serviceComponents.add(this.descriptivePanel, "Description");
		this.serviceComponents.add(this.activationPanel, "Activations");
		this.serviceComponents.add(this.contactsPanel, "Contacts");
		this.serviceComponents.add(this.nodesPanel, "Nodes");
		this.serviceComponents.add(this.policiesPanel, "Policies");
		this.serviceComponents.add(this.cryptoPanel, "Cryptography");
		this.serviceComponents.add(this.configurationPanel, "Configuration");
		this.serviceComponents.add(this.serviceMetadataPanel, "Current Metadata");

		this.add(this.serviceComponents);
		this.setVisible(false);
	}

	private void createPanelTitle()
	{
		this.serviceTitleLbl = new Label("Service Details");
		this.serviceTitleLbl.addStyleName(CSSConstants.esoeManagerSectionTitle);
		this.add(this.serviceTitleLbl);
	}

	public void executeEvent(BaseEvent event)
	{
		if(event.getName().equals(EventConstants.showServiceList))
			this.setVisible(false);
		
		if (event instanceof ShowServiceDetailsEvent && event.getName().equals(EventConstants.showService))
		{
			ShowServiceDetailsEvent ssd = (ShowServiceDetailsEvent) event;
			this.createInterface(ssd.getServiceID());
			this.serviceComponents.selectTab(0);
			this.setVisible(true);
		}
	}

	public String[] getRegisteredEvents()
	{
		return this.registeredEvents;
	}
}
