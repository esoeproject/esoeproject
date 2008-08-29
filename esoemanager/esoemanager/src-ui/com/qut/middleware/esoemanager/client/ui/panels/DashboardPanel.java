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
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.qut.gwtuilib.client.display.FlexibleTable;
import com.qut.gwtuilib.client.display.IntegratedLabel;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventController;
import com.qut.gwtuilib.client.eventdriven.events.MessageEvent;
import com.qut.middleware.esoemanager.client.CSSConstants;
import com.qut.middleware.esoemanager.client.EsoeManager;
import com.qut.middleware.esoemanager.client.EsoeManagerConstants;
import com.qut.middleware.esoemanager.client.events.EventConstants;
import com.qut.middleware.esoemanager.client.rpc.bean.EsoeDashBean;
import com.qut.middleware.esoemanager.client.rpc.bean.MetadataDashBean;
import com.qut.middleware.esoemanager.client.rpc.bean.ServiceExpiryBean;
import com.qut.middleware.esoemanager.client.rpc.bean.ServiceStartupBean;
import com.qut.middleware.esoemanager.client.rpc.bean.ServicesDashBean;

public class DashboardPanel extends VerticalPanel
{
	private String esoeID;

	private VerticalPanel metadataDash;
	private VerticalPanel esoeDash;
	private VerticalPanel serviceDash;
	private VerticalPanel startupDash;

	/** MD Dash */

	/** ESOE Dash */
	IntegratedLabel numServices;
	IntegratedLabel activeServices;
	IntegratedLabel numNodes;
	IntegratedLabel activeNodes;
	IntegratedLabel numPolicies;
	IntegratedLabel activePolicies;

	/** Service Expiry Dash */
	VerticalPanel serviceExpiry;
	
	/** Service Startup Dash */
	VerticalPanel serviceStartup;

	public DashboardPanel(String esoeID)
	{
		this.esoeID = esoeID;

		this.createInterface();

		// This disabled on purpose until more MD data can be retrieved
		// EsoeManager.contentService.getMDDashboardDetails(new MetadataDashHandler());

		EsoeManager.contentService.getESOEDashboardDetails(new EsoeDashHandler());
		EsoeManager.contentService.getServicesDashboardDetails(new ServicesDashHandler());
		EsoeManager.contentService.getStartupDashboardDetails(new StartupDashHandler());
	}

	private void createInterface()
	{
		HorizontalPanel importantKeyDetails = new HorizontalPanel();
		importantKeyDetails.setSpacing(5);
		importantKeyDetails.setVerticalAlignment(ALIGN_TOP);

		this.esoeDash = new VerticalPanel();
		this.esoeDash.addStyleName(CSSConstants.esoeDash);

		this.metadataDash = new VerticalPanel();
		this.metadataDash.addStyleName(CSSConstants.metadataDash);

		this.serviceDash = new VerticalPanel();
		this.serviceDash.addStyleName(CSSConstants.serviceDash);
		
		this.startupDash = new VerticalPanel();
		this.startupDash.addStyleName(CSSConstants.startupDash);

		VerticalPanel col1 = new VerticalPanel();
		VerticalPanel col2 = new VerticalPanel();

		col1.add(this.esoeDash);
		col1.add(this.startupDash);
		col2.add(this.serviceDash);

		importantKeyDetails.add(col1);
		importantKeyDetails.add(col2);

		// This disabled on purpose until more MD data can be retrieved
		// this.createMetadataDashboard();
		this.createESOEDashboard();
		this.createServicesDashboard();
		this.createStartupDashboard();

		this.add(importantKeyDetails);
	}

	private void createMetadataDashboard()
	{
		HorizontalPanel titleBanner = new HorizontalPanel();
		titleBanner.addStyleName(CSSConstants.dashTitle);

		Label title = new Label("Metadata");
		titleBanner.add(title);

		VerticalPanel content = new VerticalPanel();
		content.addStyleName(CSSConstants.dashContent);

		Label keyHeading = new Label("Registered Keys");
		keyHeading.addStyleName(CSSConstants.dashContentTitle);

		content.add(keyHeading);

		this.metadataDash.add(titleBanner);
		this.metadataDash.add(content);
	}

	private void createESOEDashboard()
	{
		HorizontalPanel titleBanner = new HorizontalPanel();
		titleBanner.addStyleName(CSSConstants.dashTitle);

		Label title = new Label("ESOE Statistics");
		titleBanner.add(title);

		VerticalPanel content = new VerticalPanel();
		content.addStyleName(CSSConstants.dashContent);

		FlexibleTable stats = new FlexibleTable(3, 3);

		numServices = new IntegratedLabel("Total Services ", "");
		numServices.addTableRow(stats);

		activeServices = new IntegratedLabel("Active Services ", "");
		activeServices.addTableRow(stats);

		numNodes = new IntegratedLabel("Total Endpoints ", "");
		numNodes.addTableRow(stats);

		activeNodes = new IntegratedLabel("Active Endpoints ", "");
		activeNodes.addTableRow(stats);

		numPolicies = new IntegratedLabel("Total Policies ", "");
		numPolicies.addTableRow(stats);

		activePolicies = new IntegratedLabel("Active Policies ", "");
		activePolicies.addTableRow(stats);

		content.add(stats);

		this.esoeDash.add(titleBanner);
		this.esoeDash.add(content);
	}

	private void createServicesDashboard()
	{
		HorizontalPanel titleBanner = new HorizontalPanel();
		titleBanner.addStyleName(CSSConstants.dashTitle);

		Label title = new Label("Upcoming service expiry");
		titleBanner.add(title);

		VerticalPanel content = new VerticalPanel();
		content.addStyleName(CSSConstants.dashContent);

		serviceExpiry = new VerticalPanel();

		content.add(serviceExpiry);

		this.serviceDash.add(titleBanner);
		this.serviceDash.add(content);
	}
	
	private void createStartupDashboard()
	{
		HorizontalPanel titleBanner = new HorizontalPanel();
		titleBanner.addStyleName(CSSConstants.dashTitle);

		Label title = new Label("Recent service activations");
		titleBanner.add(title);

		VerticalPanel content = new VerticalPanel();
		content.addStyleName(CSSConstants.dashContent);

		this.serviceStartup = new VerticalPanel();

		content.add(this.serviceStartup);

		this.startupDash.add(titleBanner);
		this.startupDash.add(content);
	}

	private class MetadataDashHandler implements AsyncCallback
	{
		public void onFailure(Throwable caught)
		{
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
					MessageEvent.error,
					"Unable to retrieve md dashboard data, please contact a system administrator Server response "
							+ caught.getLocalizedMessage()));
		}

		public void onSuccess(Object result)
		{
			MetadataDashBean bean = (MetadataDashBean) result;

			if (bean != null)
			{

			}
			else
				EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
						MessageEvent.error, "Response data was invalid when creating md dashboard"));
		}
	}

	private class EsoeDashHandler implements AsyncCallback
	{
		public void onFailure(Throwable caught)
		{
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
					MessageEvent.error,
					"Unable to retrieve esoe dashboard data, please contact a system administrator Server response "
							+ caught.getLocalizedMessage()));
		}

		public void onSuccess(Object result)
		{
			EsoeDashBean bean = (EsoeDashBean) result;

			if (bean != null)
			{
				numServices.setText(bean.getNumServices());
				activeServices.setText(bean.getActiveServices());
				numNodes.setText(bean.getNumNodes());
				activeNodes.setText(bean.getActiveNodes());
				numPolicies.setText(bean.getNumPolicies());
				activePolicies.setText(bean.getActivePolicies());
			}
			else
				EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
						MessageEvent.error, "Response data was invalid when creating esoe dashboard"));
		}
	}

	private class ServicesDashHandler implements AsyncCallback
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
			ServicesDashBean bean = (ServicesDashBean) result;

			if (bean != null)
			{
				FlexibleTable expServices = new FlexibleTable(3, 3);

				expServices.insertHeading(0, "Service Name");
				expServices.insertHeading(1, "Expiry Date");

				for (ServiceExpiryBean service : bean.getExpiries())
				{
					expServices.insertText(service.getName());
					expServices.insertText(service.getExpiryDate().toString());
					expServices.nextRow();
				}

				serviceExpiry.add(expServices);
			}
			else
				EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
						MessageEvent.error, "Response data was invalid when creating services dashboard"));
		}
	}
	
	private class StartupDashHandler implements AsyncCallback
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

				for (ServiceStartupBean event : startups)
				{
					startupEvents.insertText(event.getServiceName());
					startupEvents.insertText(event.getNodeID());
					startupEvents.insertText(event.getVersion());
					startupEvents.insertText(event.getDate().toString());
					startupEvents.nextRow();
				}

				serviceStartup.add(startupEvents);
			}
			else
				EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
						MessageEvent.error, "Response data was invalid when creating services dashboard"));
		}
	}
}
