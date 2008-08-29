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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.qut.gwtuilib.client.display.Loader;
import com.qut.gwtuilib.client.eventdriven.eventmgr.BaseEvent;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventController;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventListener;
import com.qut.gwtuilib.client.eventdriven.events.MessageEvent;
import com.qut.gwtuilib.client.input.StyledButton;
import com.qut.middleware.esoemanager.client.CSSConstants;
import com.qut.middleware.esoemanager.client.EsoeManager;
import com.qut.middleware.esoemanager.client.EsoeManagerConstants;
import com.qut.middleware.esoemanager.client.events.EventConstants;
import com.qut.middleware.esoemanager.client.events.PolicyCancelledEvent;
import com.qut.middleware.esoemanager.client.events.PolicyCreatedEvent;
import com.qut.middleware.esoemanager.client.events.PolicyDeletedEvent;
import com.qut.middleware.esoemanager.client.events.PolicyUpdateEvent;
import com.qut.middleware.esoemanager.client.exceptions.PolicyComponentValidationException;
import com.qut.middleware.esoemanager.client.rpc.bean.Policy;
import com.qut.middleware.esoemanager.client.ui.policy.PolicyUI;

public class PolicyEditorPanel extends VerticalPanel implements EventListener
{
	/* All events this class instance will respond to */
	private final String[] registeredEvents =
	{
			EventConstants.cancelPolicyCreation, EventConstants.successfulPolicyCreation, EventConstants.savedPolicy,
			EventConstants.savePolicyFailure, EventConstants.updatePolicy, EventConstants.deletedPolicy, EventConstants.deletePolicyFailure
	};

	String serviceIdentifier;

	private VerticalPanel policiesPanel;
	private Loader loader;

	List<com.qut.middleware.esoemanager.client.ui.policy.PolicyUI> policies;

	public PolicyEditorPanel(String serviceIdentifier)
	{
		this.serviceIdentifier = serviceIdentifier;

		this.createInterface();
		EsoeManager.contentService.retrieveServicePolicies(this.serviceIdentifier, new PoliciesRetrievalHandler());

		EventController.registerListener(this);
	}

	private void createInterface()
	{
		this.policies = new ArrayList<com.qut.middleware.esoemanager.client.ui.policy.PolicyUI>();
		this.policiesPanel = new VerticalPanel();

		HorizontalPanel heading = new HorizontalPanel();
		heading.setSpacing(5);
		heading.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

		Label title = new Label("Policies governing access to service resources");
		title.addStyleName(CSSConstants.esoeManagerSubSectionTitle);

		StyledButton addPolicy = new StyledButton("add", "Add Policy");
		addPolicy.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				PolicyUI policyUI = new PolicyUI(PolicyEditorPanel.this.serviceIdentifier, true);
				PolicyEditorPanel.this.policies.add(policyUI);
				PolicyEditorPanel.this.policiesPanel.add(policyUI);
			}
		});

		// display loader until data returns
		this.loader = new Loader();
		heading.add(this.loader);
		heading.add(addPolicy);

		this.add(title);
		this.add(heading);
		this.add(this.policiesPanel);
	}

	private void populateInterface(List<Policy> policies)
	{
		this.policies.clear();
		if (policies != null)
		{
			for (Policy policy : policies)
			{
				PolicyUI policyUI = new PolicyUI(this.serviceIdentifier, false);
				try
				{
					policyUI.populateContent(policy);
					this.policies.add(policyUI);
				}
				catch (PolicyComponentValidationException e)
				{
					this.policies.add(policyUI);

					EventController.executeEvent(new MessageEvent(EventConstants.userMessage,
							EsoeManagerConstants.areaID, MessageEvent.error, e.getLocalizedMessage()));
				}
				this.policiesPanel.add(policyUI);
			}
		}
	}

	public void executeEvent(BaseEvent event)
	{
		if (event.getName().equals(EventConstants.cancelPolicyCreation))
		{
			PolicyCancelledEvent pce = (PolicyCancelledEvent) event;
			this.policies.remove(pce.getPolicy());
			this.policiesPanel.remove(pce.getPolicy());
		}

		if (event.getName().equals(EventConstants.successfulPolicyCreation))
		{
			PolicyCreatedEvent created = (PolicyCreatedEvent) event;
			this.policies.remove(created.getPolicyUI());
			this.policiesPanel.remove(created.getPolicyUI());
			EsoeManager.contentService.retrieveServicePolicy(this.serviceIdentifier, created.getPolicyID(),
					new PolicyRetrievalHandler());
		}

		if (event.getName().equals(EventConstants.updatePolicy))
		{
			PolicyUpdateEvent updated = (PolicyUpdateEvent) event;
			EsoeManager.contentService.retrieveServicePolicy(this.serviceIdentifier, updated.getPolicyID(),
					new PolicyRetrievalHandler());
		}

		if (event.getName().equals(EventConstants.deletedPolicy))
		{
			PolicyDeletedEvent pde = (PolicyDeletedEvent) event;
			for (PolicyUI policyUI : this.policies)
			{
				if (policyUI.getPolicyID().equals(pde.getPolicyID()))
				{
					this.policies.remove(policyUI);
					this.policiesPanel.remove(policyUI);
				}
			}
		}
	}

	public String[] getRegisteredEvents()
	{
		return this.registeredEvents;
	}

	private class PolicyRetrievalHandler implements AsyncCallback
	{
		public void onFailure(Throwable caught)
		{
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
					MessageEvent.error,
					"Unable to retrieve a policy for this service, please contact a system administrator. Server response: "
							+ caught.getLocalizedMessage()));
		}

		public void onSuccess(Object result)
		{
			if (result != null)
			{
				Policy policy = (Policy) result;
				try
				{
					boolean update = false;
					for (PolicyUI policyUI : PolicyEditorPanel.this.policies)
					{
						if (policyUI.getPolicyID().equals(policy.getPolicyID()))
						{
							update = true;
							policyUI.populateContent(policy);
						}
					}

					if (!update)
					{
						PolicyUI policyUI = new PolicyUI(PolicyEditorPanel.this.serviceIdentifier, false);
						policyUI.populateContent(policy);
						PolicyEditorPanel.this.policies.add(policyUI);
						PolicyEditorPanel.this.policiesPanel.add(policyUI);
					}

				}
				catch (PolicyComponentValidationException e)
				{
					EventController.executeEvent(new MessageEvent(EventConstants.userMessage,
							EsoeManagerConstants.areaID, MessageEvent.error,
							"Unable to validate policy please contact a system administrator. Server response: "
									+ e.getLocalizedMessage()));
				}
			}
		}
	}

	private class PoliciesRetrievalHandler implements AsyncCallback
	{
		public void onFailure(Throwable caught)
		{
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
					MessageEvent.error,
					"Unable to retrieve a policy listing for this service, please contact a system administrator. Server response: "
							+ caught.getLocalizedMessage()));
		}

		public void onSuccess(Object result)
		{
			if (result != null)
			{
				List<Policy> policies = (List<Policy>) result;
				populateInterface(policies);
			}
		}
	}
}
