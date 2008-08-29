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
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.qut.gwtuilib.client.RegexConstants;
import com.qut.gwtuilib.client.display.FlexibleTable;
import com.qut.gwtuilib.client.display.HiddenIntegratedTextBox;
import com.qut.gwtuilib.client.display.Loader;
import com.qut.gwtuilib.client.eventdriven.eventmgr.BaseEvent;
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
import com.qut.middleware.esoemanager.client.rpc.bean.ServiceNode;
import com.qut.middleware.esoemanager.client.ui.nodes.NodeUI;

public class NodesPanel extends VerticalPanel
{
	private String serviceIdentifier;
	private String areaID;

	private VerticalPanel content;

	private int activeCount;

	HiddenIntegratedTextBox serviceHost;

	public NodesPanel(String serviceIdentifier)
	{
		this.serviceIdentifier = serviceIdentifier;
		this.areaID = EsoeManagerConstants.areaID;
		this.activeCount = 0;

		this.createInterface();
		EsoeManager.contentService.retrieveServiceNodes(this.serviceIdentifier, new NodeListHandler());
		EsoeManager.contentService.retrieveServiceHost(this.serviceIdentifier, new ServiceHostHandler());
	}

	private void createInterface()
	{
		this.clear();
		this.content = new VerticalPanel();
		this.content.setSpacing(10);

		FlexibleTable serviceHostPanel = new FlexibleTable(5, 5);
		Label serviceHostLbl = new Label("Service Host");
		serviceHostLbl.addStyleName(CSSConstants.esoeManagerSubSectionTitle);
		serviceHost = new HiddenIntegratedTextBox(this, 1, RegexConstants.URLRegex,
				"Service Host URL must conform to URL syntax", "URL", EsoeManagerConstants.areaID);

		ConfirmationStyledButton saveServiceHost = new ConfirmationStyledButton("Change base service host?", "save",
				"Save");
		saveServiceHost.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				try
				{
					EsoeManager.contentService.saveServiceHost(serviceIdentifier, serviceHost.getText(),
							new ServiceHostSaveHandler());
				}
				catch (InvalidContentException e)
				{
				}
			}
		});

		serviceHostPanel.insertWidget(serviceHostLbl);
		serviceHostPanel.nextRow();
		serviceHostPanel.insertWidget(serviceHost.getContent());
		serviceHostPanel.insertWidget(saveServiceHost);

		VerticalPanel serviceNodesPanel = new VerticalPanel();
		serviceNodesPanel.setSpacing(10);
		Label title = new Label("Service Nodes");
		title.addStyleName(CSSConstants.esoeManagerSubSectionTitle);

		serviceNodesPanel.add(title);
		serviceNodesPanel.add(this.content);

		this.add(serviceHostPanel);
		this.add(serviceNodesPanel);

		createNewNodeInterface();
	}

	private void createNewNodeInterface()
	{
		VerticalPanel create = new VerticalPanel();
		create.setSpacing(10);

		Label createLbl = new Label("Create new node for this service");
		createLbl.addStyleName(CSSConstants.esoeManagerSubSectionTitle);

		FlexibleTable input = new FlexibleTable(5, 5);
		Label nodeURLLbl = new Label("Node URL");
		nodeURLLbl.addStyleName(CSSConstants.serviceValueTitle);
		Label nodeIDLbl = new Label("Node ID");
		nodeURLLbl.addStyleName(CSSConstants.serviceValueTitle);
		final Loader newNodeLoader = new Loader();
		newNodeLoader.setVisible(false);
		final ValidatingTextBox newNodeURL = new ValidatingTextBox(1, RegexConstants.URLRegex, "Must be in URL format",
				this.areaID);
		final ValidatingTextBox newNodeID = new ValidatingTextBox(1, RegexConstants.numeric,
				"Must be numeric and unique", this.areaID);

		StyledButton createBtn = new StyledButton("createnode", "Create Node");
		createBtn.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				try
				{
					EsoeManager.contentService.createServiceNode(serviceIdentifier, newNodeURL.getText(), newNodeID
							.getText(), new NodeCreationHandler());
					newNodeLoader.setVisible(true);
					newNodeURL.setText(null);
					newNodeID.setText(null);
				}
				catch (InvalidContentException e)
				{
				}
			}
		});

		input.insertWidget(nodeURLLbl);
		input.insertWidget(newNodeURL);
		input.nextRow();
		input.insertWidget(nodeIDLbl);
		input.insertWidget(newNodeID);
		input.nextRow();
		input.insertWidget(createBtn);
		input.insertWidget(newNodeLoader);
		input.nextRow();

		create.add(createLbl);
		create.add(input);

		this.add(create);
	}

	void populateInterface(List<ServiceNode> result)
	{
		this.content.clear();

		for (ServiceNode node : result)
		{
			VerticalPanel listingContent = new VerticalPanel();

			final NodeUI listing = new NodeUI(this.serviceIdentifier);
			listing.setNodeIdentifier(node.getNodeIdentifier());
			listing.setNodeURL(node.getNodeURL());
			listing.setNodeACS(node.getAcs());
			listing.setNodeSLS(node.getSls());
			listing.setNodeCCS(node.getCcs());
			listing.setServiceActivated(node.isActive());
			listing.createInterface();
			listingContent.add(listing);

			if (node.isActive())
				this.activeCount++;

			this.content.add(listingContent);

			VerticalPanel operations = new VerticalPanel();
			operations.setSpacing(5);

			listingContent.add(operations);
		}
	}

	private class ServiceHostHandler implements AsyncCallback
	{
		public void onFailure(Throwable caught)
		{
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, NodesPanel.this.areaID,
					MessageEvent.error,
					"Unable to get service host from the server, please contact a system administrator Server response: "
							+ caught.getLocalizedMessage()));
		}

		public void onSuccess(Object result)
		{
			String service = (String) result;

			if (service != null && service.length() > 0)
			{
				try
				{
					serviceHost.setText(service);
				}
				catch (InvalidContentException e)
				{
				}
			}
			else
				serviceHost.showEditor();
		}
	}

	private class ServiceHostSaveHandler implements AsyncCallback
	{
		public void onFailure(Throwable caught)
		{
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, NodesPanel.this.areaID,
					MessageEvent.error,
					"Unable to save service host on the server, please contact a system administrator Server response: "
							+ caught.getLocalizedMessage()));
		}

		public void onSuccess(Object result)
		{
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, NodesPanel.this.areaID,
					MessageEvent.ok, "Service host saved successfully"));
		}
	}

	private class NodeListHandler implements AsyncCallback
	{
		public void onFailure(Throwable caught)
		{
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, NodesPanel.this.areaID,
					MessageEvent.error,
					"Unable to node listing from the server, please contact a system administrator Server response: "
							+ caught.getLocalizedMessage()));
		}

		public void onSuccess(Object result)
		{
			if (result != null)
			{
				List<ServiceNode> res = (List<ServiceNode>) result;
				populateInterface(res);
			}
		}
	}

	private class NodeCreationHandler implements AsyncCallback
	{
		public void onFailure(Throwable caught)
		{
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, NodesPanel.this.areaID,
					MessageEvent.error,
					"Unable to create new node on the server, please contact a system administrator Server response: "
							+ caught.getLocalizedMessage()));
		}

		public void onSuccess(Object result)
		{
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, NodesPanel.this.areaID,
					MessageEvent.ok, "New node created successfully, refreshing nodes..."));
			EsoeManager.contentService.retrieveServiceNodes(serviceIdentifier, new NodeListHandler());
			EventController.executeEvent(new BaseEvent(EventConstants.configurationChange));
		}
	}

}
