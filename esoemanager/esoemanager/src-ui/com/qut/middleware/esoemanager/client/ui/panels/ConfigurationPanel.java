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
import com.qut.gwtuilib.client.display.Loader;
import com.qut.gwtuilib.client.eventdriven.eventmgr.BaseEvent;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventController;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventListener;
import com.qut.gwtuilib.client.eventdriven.events.MessageEvent;
import com.qut.middleware.esoemanager.client.CSSConstants;
import com.qut.middleware.esoemanager.client.EsoeManager;
import com.qut.middleware.esoemanager.client.EsoeManagerConstants;
import com.qut.middleware.esoemanager.client.events.EventConstants;
import com.qut.middleware.esoemanager.client.rpc.bean.ServiceNodeConfiguration;
import com.qut.middleware.esoemanager.client.ui.nodes.NodeConfigurationUI;

public class ConfigurationPanel extends VerticalPanel implements EventListener
{
	private final String[] registeredEvents =
	{
			EventConstants.configurationChange		
	};
	
	String serviceIdentifier;
	Loader loader;
	
	VerticalPanel nodeConfigs;
	
	public ConfigurationPanel(String serviceIdentifier)
	{
		this.serviceIdentifier = serviceIdentifier;
		this.createInterface();
		
		EventController.registerListener(this);
	}
	
	public void createInterface()
	{
		Label title = new Label("Service Configuration");
		title.addStyleName(CSSConstants.esoeManagerSubSectionTitle);
		
		Label intro = new Label("The below information allows configuration of each service node's config file");
		
		this.loader = new Loader();
		this.nodeConfigs = new VerticalPanel();
		
		this.add(title);
		this.add(intro);
		this.add(this.loader);
		this.add(this.nodeConfigs);
		
		EsoeManager.contentService.retrieveNodeConfigurations(this.serviceIdentifier, new NodeConfigurationHandler(this.loader));
	}
	
	public void executeEvent(BaseEvent event)
	{
		if (event.getName().equals(EventConstants.configurationChange))
		{
			EsoeManager.contentService.retrieveNodeConfigurations(this.serviceIdentifier, new NodeConfigurationHandler(this.loader));
		}
	}
	
	public String[] getRegisteredEvents()
	{
		return this.registeredEvents;
	}
	
	void populateInterface(List<ServiceNodeConfiguration> configs)
	{
		this.nodeConfigs.clear();
		for(ServiceNodeConfiguration config : configs)
		{
			NodeConfigurationUI nodeConfigurationUI = new NodeConfigurationUI();
			nodeConfigurationUI.createInterface(config);
			this.nodeConfigs.add(nodeConfigurationUI);
		}
	}
	
	private class NodeConfigurationHandler implements AsyncCallback
	{
		Loader loader;

		public NodeConfigurationHandler(Loader loader)
		{
			this.loader = loader;
		}

		public void onFailure(Throwable caught)
		{
			this.loader.setVisible(false);
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
					MessageEvent.error,
					"Unable to save new node configuration on the server, please contact a system administrator Server response: "
							+ caught.getLocalizedMessage()));
		}

		public void onSuccess(Object result)
		{
			this.loader.setVisible(false);
			if(result != null)
			{
				List<ServiceNodeConfiguration> configs = (List<ServiceNodeConfiguration>) result;
				populateInterface(configs);
			}
		}
	}
}
