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
import com.qut.middleware.esoemanager.client.rpc.bean.ServiceStartupBean;
import com.qut.middleware.esoemanager.client.ui.data.ContactListing;

public class StartupsPanel extends VerticalPanel
{
	private String serviceIdentifier;

	private VerticalPanel content;

	public StartupsPanel(String serviceIdentifier)
	{
		this.serviceIdentifier = serviceIdentifier;

		this.createInterface();
		EsoeManager.contentService.retrieveServiceStartups(this.serviceIdentifier, new ServiceStartupsHandler());
	}

	private void createInterface()
	{
		this.clear();
		
		Label title = new Label("Service Activations");
		title.addStyleName(CSSConstants.esoeManagerSubSectionTitle);
		
		this.content = new VerticalPanel();
		
		this.add(title);
		this.add(this.content);
	}

	private class ServiceStartupsHandler implements AsyncCallback
	{
		public void onFailure(Throwable caught)
		{
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
					MessageEvent.error,
					"Unable to retrieve services dashboard data, please contact a system administrator Server response "
							+ caught.getLocalizedMessage()));
		}

		public void onSuccess(Object result)
		{
			List<ServiceStartupBean> startups = (List<ServiceStartupBean>) result;

			if (startups != null)
			{
				FlexibleTable startupEvents = new FlexibleTable(3, 3);

				startupEvents.insertHeading(0, "Service Name");
				startupEvents.insertHeading(1, "Node ID");
				startupEvents.insertHeading(2, "Version");
				startupEvents.insertHeading(3, "Date");
				startupEvents.insertHeading(4, "Environment");

				for (ServiceStartupBean event : startups)
				{
					startupEvents.insertText(event.getServiceName());
					startupEvents.insertText(event.getNodeID());
					startupEvents.insertText(event.getVersion());
					startupEvents.insertText(event.getDate().toString());
					startupEvents.insertText(event.getEnv());
					startupEvents.nextRow();
				}

				content.add(startupEvents);
			}
			else
				EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
						MessageEvent.error, "Response data was invalid when creating services dashboard"));
		}
	}
}
