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
package com.qut.middleware.esoemanager.client.ui.nodes;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.qut.gwtuilib.client.RegexConstants;
import com.qut.gwtuilib.client.display.FlexibleTable;
import com.qut.gwtuilib.client.display.HiddenIntegratedTextBox;
import com.qut.gwtuilib.client.display.IntegratedLabel;
import com.qut.gwtuilib.client.display.Loader;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventController;
import com.qut.gwtuilib.client.eventdriven.events.MessageEvent;
import com.qut.gwtuilib.client.exceptions.InvalidContentException;
import com.qut.gwtuilib.client.input.ConfirmationStyledButton;
import com.qut.gwtuilib.client.input.StyledButton;
import com.qut.middleware.esoemanager.client.CSSConstants;
import com.qut.middleware.esoemanager.client.EsoeManager;
import com.qut.middleware.esoemanager.client.EsoeManagerConstants;
import com.qut.middleware.esoemanager.client.events.EventConstants;
import com.qut.middleware.esoemanager.client.ui.ActiveState;

public class NodeUI extends VerticalPanel
{
	private IntegratedLabel nodeIdentifier;
	private HiddenIntegratedTextBox nodeURL;
	private HiddenIntegratedTextBox nodeACS;
	private HiddenIntegratedTextBox nodeSLS;
	private HiddenIntegratedTextBox nodeCCS;
	private ActiveState state;
	
	Loader loader;

	String serviceIdentifier;
	
	ConfirmationStyledButton save;
	ConfirmationStyledButton toggleState;
	
	VerticalPanel content;

	public NodeUI(String serviceIdentifier)
	{
		this.serviceIdentifier = serviceIdentifier;
		
		this.nodeIdentifier = new IntegratedLabel(this, "Identifier");

		this.nodeURL = new HiddenIntegratedTextBox(this, 1, RegexConstants.URLRegex,
				"Node URL must conform to URL syntax", "URL", EsoeManagerConstants.areaID);

		this.nodeACS = new HiddenIntegratedTextBox(this, 1, RegexConstants.PathRegex,
				"Node Assertion Consumer Service must conform to URL path syntax", "Assertion Consumer", EsoeManagerConstants.areaID);

		this.nodeSLS = new HiddenIntegratedTextBox(this, 1, RegexConstants.PathRegex,
				"Node Single Logout Service must conform to URL path syntax", "Single Logout", EsoeManagerConstants.areaID);

		this.nodeCCS = new HiddenIntegratedTextBox(this, 1, RegexConstants.PathRegex,
				"Node Cache Clear Service must conform to URL path syntax", "Cache Clear", EsoeManagerConstants.areaID);

		this.state = new ActiveState();
		this.loader = new Loader();
	}

	public void createInterface()
	{
		this.addStyleName(CSSConstants.esoeManagerNode);
		this.content = new VerticalPanel();
		
		HorizontalPanel banner = new HorizontalPanel();
		banner.addStyleName(CSSConstants.esoeManagerNodeBanner);
		banner.setSpacing(5);
		
		final HorizontalPanel subBanner = new HorizontalPanel();
		subBanner.addStyleName(CSSConstants.esoeManagerNodeSubBanner);
		subBanner.setSpacing(5);
		
		StyledButton showContent = new StyledButton("application", "");
		showContent.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				content.setVisible(!content.isVisible());
			}
		});
		
		banner.add(showContent);
		banner.add(new Label("Node - "));
		try
		{
			banner.add(new Label(this.nodeURL.getText()));
		}
		catch (InvalidContentException e)
		{
		}
		
		this.save = new ConfirmationStyledButton("Save node changes?", "save", "Save");
		this.save.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				try
				{
					EsoeManager.contentService.saveServiceNodeConfiguration(serviceIdentifier, getNodeIdentifier(),
							getNodeURL(), getNodeACS(), getNodeSLS(), getNodeCCS(), new ConfigChangeHandler(
									NodeUI.this.loader));
				}
				catch (InvalidContentException e)
				{
				}
			}
		});
		
		subBanner.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		subBanner.add(this.save);

		this.toggleState = new ConfirmationStyledButton("Change node state?", "toggle", "");
		this.toggleState.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				
				
				if (NodeUI.this.isActive())
					EsoeManager.contentService.toggleServiceNodeState(NodeUI.this.serviceIdentifier, getNodeIdentifier(), true, new StateHandler(NodeUI.this.loader));
				else
					EsoeManager.contentService.toggleServiceNodeState(NodeUI.this.serviceIdentifier, getNodeIdentifier(), false, new StateHandler(NodeUI.this.loader));
			}
		});
		
		FlexibleTable layout = new FlexibleTable(5, 5);

		layout.insertWidget(this.nodeIdentifier.getTitle());
		layout.insertWidget(this.nodeIdentifier.getContent());
		layout.nextRow();

		layout.insertWidget(this.nodeURL.getTitle());
		layout.insertWidget(this.nodeURL.getContent());
		layout.nextRow();

		Label stateLbl = new Label("Active State");
		stateLbl.addStyleName(CSSConstants.serviceValueTitle);
		layout.insertWidget(stateLbl);
		
		HorizontalPanel statePanel = new HorizontalPanel();
		statePanel.setSpacing(5);
		statePanel.add(this.state);
		statePanel.add(this.toggleState);
		layout.insertWidget(statePanel);
		layout.nextRow();

		layout.insertWidget(this.nodeACS.getTitle());
		layout.insertWidget(this.nodeACS.getContent());
		layout.nextRow();

		layout.insertWidget(this.nodeSLS.getTitle());
		layout.insertWidget(this.nodeSLS.getContent());
		layout.nextRow();

		layout.insertWidget(this.nodeCCS.getTitle());
		layout.insertWidget(this.nodeCCS.getContent());
		layout.nextRow();
		
		this.content.add(subBanner);
		this.content.add(layout);
		this.content.setVisible(false);
		
		this.add(banner);
		this.add(this.content);
	}

	public String getNodeIdentifier()
	{
		return this.nodeIdentifier.getText();
	}

	public String getNodeURL() throws InvalidContentException
	{
		return this.nodeURL.getText();
	}

	public String getNodeACS() throws InvalidContentException
	{
		return this.nodeACS.getText();
	}

	public String getNodeSLS() throws InvalidContentException
	{
		return this.nodeSLS.getText();
	}

	public String getNodeCCS() throws InvalidContentException
	{
		return this.nodeCCS.getText();
	}

	public boolean isActive()
	{
		return this.state.isActive();
	}

	public void setNodeIdentifier(String nodeIdentifier)
	{
		this.nodeIdentifier.setText(nodeIdentifier);
	}

	public void setNodeURL(String nodeURL)
	{
		try
		{
			this.nodeURL.setText(nodeURL);
		}
		catch (InvalidContentException e)
		{
		}
	}

	public void setNodeACS(String nodeACS)
	{
		try
		{
			this.nodeACS.setText(nodeACS);
		}
		catch (InvalidContentException e)
		{
		}
	}

	public void setNodeSLS(String nodeSLS)
	{
		try
		{
			this.nodeSLS.setText(nodeSLS);
		}
		catch (InvalidContentException e)
		{
		}
	}

	public void setNodeCCS(String nodeCCS)
	{
		try
		{
			this.nodeCCS.setText(nodeCCS);
		}
		catch (InvalidContentException e)
		{
		}
	}

	public void setServiceActivated(boolean serviceActivated)
	{
		if (serviceActivated)
			this.state.setActivated();
		else
			this.state.setDeactivated();
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
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
					MessageEvent.error,
					"Unable to obtain modify node active state on the server, please contact a system administrator. Server response: "
							+ caught.getLocalizedMessage()));
		}

		public void onSuccess(Object result)
		{
			this.loader.setVisible(false);
			if (NodeUI.this.isActive())
				NodeUI.this.setServiceActivated(false);
			else
				NodeUI.this.setServiceActivated(true);

			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
					MessageEvent.ok, "Node state changed successfully"));
		}
	}
	
	private class ConfigChangeHandler implements AsyncCallback
	{
		Loader loader;

		public ConfigChangeHandler(Loader loader)
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
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
					MessageEvent.ok, "Node configuration saved successfully"));
		}
	}
}
