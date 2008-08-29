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
package com.qut.middleware.esoemanager.client.ui.policy;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.qut.gwtuilib.client.display.FlexibleTable;
import com.qut.gwtuilib.client.display.HiddenIntegratedTextBox;
import com.qut.gwtuilib.client.display.IntegratedLabel;
import com.qut.gwtuilib.client.display.Loader;
import com.qut.gwtuilib.client.display.MessagePanel;
import com.qut.gwtuilib.client.eventdriven.eventmgr.BaseEvent;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventController;
import com.qut.gwtuilib.client.eventdriven.events.MessageEvent;
import com.qut.gwtuilib.client.exceptions.InvalidContentException;
import com.qut.gwtuilib.client.input.ConfirmationStyledButton;
import com.qut.gwtuilib.client.input.StyledButton;
import com.qut.middleware.esoemanager.client.CSSConstants;
import com.qut.middleware.esoemanager.client.EsoeManager;
import com.qut.middleware.esoemanager.client.EsoeManagerConstants;
import com.qut.middleware.esoemanager.client.events.EventConstants;
import com.qut.middleware.esoemanager.client.events.PolicyCancelledEvent;
import com.qut.middleware.esoemanager.client.events.PolicyCreatedEvent;
import com.qut.middleware.esoemanager.client.events.PolicyDeletedEvent;
import com.qut.middleware.esoemanager.client.events.PolicySavedEvent;
import com.qut.middleware.esoemanager.client.events.PolicyUpdateEvent;
import com.qut.middleware.esoemanager.client.exceptions.PolicyComponentValidationException;
import com.qut.middleware.esoemanager.client.rpc.bean.Policy;
import com.qut.middleware.esoemanager.client.rpc.bean.Rule;
import com.qut.middleware.esoemanager.client.rpc.bean.Target;
import com.qut.middleware.esoemanager.client.ui.ActiveState;

public class PolicyUI extends VerticalPanel implements PolicyUIComponent
{

	PolicyResourcesUI resources;
	PolicyActionsUI actions;
	List<RuleUI> rules;
	HiddenIntegratedTextBox desc;
	IntegratedLabel id;
	ActiveState active;
	PolicyXMLUI policyXMLUI;
	boolean newPolicy;
	String serviceIdentifier;
	FlexibleTable content;
	VerticalPanel rulesPanel;
	ConfirmationStyledButton activeToggle;
	Loader loader;
	MessagePanel localMessages;
	VerticalPanel policyPanel;
	VerticalPanel graphicalEditor;
	VerticalPanel xmlEditor;
	VerticalPanel activeEditor;

	HorizontalPanel banner;
	HorizontalPanel subBanner;
	ConfirmationStyledButton graphicalMode;
	ConfirmationStyledButton xmlMode;

	HorizontalPanel validationPanel;

	Label validationMessage;

	public PolicyUI(String serviceIdentifier, boolean newPolicy)
	{

		this.serviceIdentifier = serviceIdentifier;
		this.newPolicy = newPolicy;

		this.createInterface();
	}

	private void createInterface()
	{

		this.graphicalEditor = new VerticalPanel();
		this.graphicalEditor.setVisible(false);
		this.xmlEditor = new VerticalPanel();
		this.xmlEditor.setVisible(false);

		this.activeEditor = this.graphicalEditor;

		this.addStyleName(CSSConstants.esoeManagerPolicy);

		createGraphicalInterface();
		createXMLInterface();

		banner = new HorizontalPanel();
		banner.addStyleName(CSSConstants.esoeManagerPolicyBanner);
		banner.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

		subBanner = new HorizontalPanel();
		subBanner.setVisible(false);
		subBanner.addStyleName(CSSConstants.esoeManagerPolicySubBanner);

		this.graphicalMode = new ConfirmationStyledButton(
				"If you have unsaved changes these will be lost when switching to the graphical editor, continue?",
				"toggle", "Graphical Editor");

		this.xmlMode = new ConfirmationStyledButton(
				"If you have unsaved changes these will be lost when switching to the xml editor, continue?", "toggle",
				"XML Editor");
		graphicalMode.setVisible(false);

		graphicalMode.addClickListener(new ClickListener()
		{

			public void onClick(Widget sender)
			{

				if (!PolicyUI.this.graphicalEditor.isVisible())
				{
					if (!newPolicy)
					{
						EventController.executeEvent(new PolicyUpdateEvent(EventConstants.updatePolicy, PolicyUI.this
								.getPolicyID()));
					}

					graphicalMode.setVisible(false);
					xmlMode.setVisible(true);
					PolicyUI.this.graphicalEditor.setVisible(true);
					PolicyUI.this.xmlEditor.setVisible(false);

					PolicyUI.this.activeEditor = PolicyUI.this.graphicalEditor;
				}
			}
		});

		xmlMode.addClickListener(new ClickListener()
		{

			public void onClick(Widget sender)
			{

				if (!PolicyUI.this.xmlEditor.isVisible())
				{
					graphicalMode.setVisible(true);
					xmlMode.setVisible(false);
					PolicyUI.this.graphicalEditor.setVisible(false);
					PolicyUI.this.xmlEditor.setVisible(true);

					PolicyUI.this.activeEditor = PolicyUI.this.xmlEditor;

					if (PolicyUI.this.newPolicy)
						PolicyUI.this.policyXMLUI.enableXMLEditor();
					else
					{
						PolicyUI.this.policyXMLUI.enableXMLEditor(PolicyUI.this.id.getText());
					}
				}
			}
		});

		StyledButton showContent = new StyledButton("application", "");
		showContent.addClickListener(new ClickListener()
		{

			public void onClick(Widget sender)
			{
				subBanner.setVisible(!subBanner.isVisible());
				PolicyUI.this.activeEditor.setVisible(!PolicyUI.this.activeEditor.isVisible());
			}
		});

		final ConfirmationStyledButton savePolicy = new ConfirmationStyledButton(
				"Are you sure you wish to save this policy?", "save", "Save");
		savePolicy.addClickListener(new ClickListener()
		{

			public void onClick(Widget sender)
			{

				if (PolicyUI.this.activeEditor == PolicyUI.this.graphicalEditor)
				{
					saveGraphicalPolicy();
				}
				else
				{
					saveXMLPolicy();
				}
			}
		});

		final ConfirmationStyledButton deletePolicy = new ConfirmationStyledButton(
				"Are you sure you wish to delete this policy?", "delete", "Delete");
		deletePolicy.addClickListener(new ClickListener()
		{

			public void onClick(Widget sender)
			{
				EsoeManager.contentService.deleteServicePolicy(PolicyUI.this.serviceIdentifier, id.getText(),
						new PolicyDeletionHandler());
			}
		});

		final ConfirmationStyledButton cancelPolicy = new ConfirmationStyledButton(
				"Are you sure you wish to cancel creation of this policy?", "cancel", "Cancel");
		cancelPolicy.addClickListener(new ClickListener()
		{

			public void onClick(Widget sender)
			{

				EventController.executeEvent(new PolicyCancelledEvent(EventConstants.cancelPolicyCreation,
						PolicyUI.this));
			}
		});

		HorizontalPanel bannerTitle = new HorizontalPanel();
		bannerTitle.addStyleName(CSSConstants.esoeManagerPolicyBannerTitle);
		bannerTitle.setSpacing(5);
		bannerTitle.add(showContent);
		if (!this.newPolicy)
		{
			bannerTitle.add(new Label("Policy - "));
			bannerTitle.add(this.id.getContent());
		}
		else
			bannerTitle.add(new Label("New Policy"));
		banner.add(bannerTitle);

		HorizontalPanel editorControlButtons = new HorizontalPanel();
		editorControlButtons.setSpacing(5);
		editorControlButtons.add(graphicalMode);
		editorControlButtons.add(xmlMode);

		HorizontalPanel subBannerButtons = new HorizontalPanel();
		subBannerButtons.setSpacing(5);
		if (!this.newPolicy)
		{
			subBannerButtons.add(savePolicy);
			// subBannerButtons.add(deletePolicy); - disabled for now, may enable in the future if deleting becomes required, better to deactivate though
		}
		else
		{
			subBannerButtons.add(savePolicy);
			subBannerButtons.add(cancelPolicy);
		}

		subBanner.add(editorControlButtons);
		subBanner.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		subBanner.add(subBannerButtons);

		this.add(banner);
		this.add(subBanner);
		this.add(this.graphicalEditor);
		this.add(this.xmlEditor);

		this.activeEditor = this.graphicalEditor;

		/* Finally if this is a new policy then show the actual editor */
		if (newPolicy)
		{
			subBanner.setVisible(true);
			this.graphicalEditor.setVisible(true);
		}
	}

	private void createXMLInterface()
	{

		this.policyXMLUI = new PolicyXMLUI(this.serviceIdentifier);
		this.xmlEditor.add(this.policyXMLUI);
	}

	private void createGraphicalInterface()
	{

		this.policyPanel = new VerticalPanel();
		this.validationPanel = new HorizontalPanel();

		this.resources = new PolicyResourcesUI();
		this.actions = new PolicyActionsUI();

		this.rules = new ArrayList<RuleUI>();

		this.desc = new HiddenIntegratedTextBox(this, 1, null, "Policies are required to have a description",
				"Description", EsoeManagerConstants.areaID);
		this.id = new IntegratedLabel(this, "ID");

		this.active = new ActiveState();
		if (!this.newPolicy)
		{
			this.active.setDeactivated();

			this.loader = new Loader();
			this.loader.setVisible(false);
			this.activeToggle = new ConfirmationStyledButton("Change policy active state?", "toggle", "");
			this.activeToggle.addClickListener(new ClickListener()
			{

				public void onClick(Widget sender)
				{
					EsoeManager.contentService.toggleServicePolicyState(PolicyUI.this.serviceIdentifier,
							PolicyUI.this.id.getText(), PolicyUI.this.active.isActive(), new PolicyActivationHandler());
				}
			});
		}
		else
		{
			this.resources.addValue();
			this.actions.addValue();
			this.active.setDeactivated();
		}

		if (this.newPolicy)
		{
			this.desc.showEditor();
		}

		this.content = new FlexibleTable(2, 2);
		
		this.localMessages = new MessagePanel();
		this.content.insertWidget(this.localMessages);
		this.content.nextRow();
		
		if (!this.newPolicy)
		{
			HorizontalPanel actPan = new HorizontalPanel();
			actPan.setSpacing(2);
			actPan.setVerticalAlignment(ALIGN_MIDDLE);
			actPan.add(this.active);
			actPan.add(this.activeToggle);
			
			this.content.insertWidget(actPan);
			this.content.nextRow();
		}
		
		HorizontalPanel descPan = new HorizontalPanel();
		descPan.setSpacing(2);
		descPan.setVerticalAlignment(ALIGN_MIDDLE);
		descPan.add(this.desc.getTitle());
		descPan.add(this.desc.getContent());
		this.content.insertWidget(descPan);
		this.content.nextRow();

		

		Label rulesLbl = new Label("This policy contains the following rules");

		StyledButton addRule = new StyledButton("add", "Add Rule");
		addRule.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				RuleUI rule = new RuleUI(PolicyUI.this, true);
				PolicyUI.this.rules.add(rule);
				PolicyUI.this.renderRules();
			}
		});
		
		this.rulesPanel = new VerticalPanel();
		this.rulesPanel.addStyleName(CSSConstants.esoeManagerPolicyRules);

		this.content.insertWidget(this.resources.getContent());
		this.content.nextRow();
		this.content.insertWidget(this.actions.getContent());
		this.content.nextRow();
		this.content.insertWidget(rulesLbl);
		this.content.insertWidget(addRule);
		this.content.nextRow();

		this.policyPanel.add(this.content);
		this.policyPanel.add(this.rulesPanel);

		this.validationMessage = new Label();
		this.validationPanel.setSpacing(5);
		this.validationPanel.add(new Loader());
		this.validationPanel.add(this.validationMessage);
		this.validationPanel.setVisible(false);

		this.graphicalEditor.add(this.policyPanel);
		this.graphicalEditor.add(this.validationPanel);
	}

	public void saveGraphicalPolicy()
	{

		try
		{
			List<String> idList = new ArrayList<String>();
			this.validate(idList);
			Policy policy = PolicyUI.this.extractContent();

			if (!this.newPolicy)
			{
				EsoeManager.contentService.saveServicePolicy(this.serviceIdentifier, policy, new PolicySaveHandler(
						this.loader));
			}
			else
			{
				EsoeManager.contentService.createServicePolicy(this.serviceIdentifier, policy,
						new PolicyCreationHandler());
			}
		}
		catch (PolicyComponentValidationException e)
		{
		}
	}

	public void saveXMLPolicy()
	{
		if (!this.newPolicy)
		{
			EsoeManager.contentService.saveServicePolicy(PolicyUI.this.serviceIdentifier, this.policyXMLUI
					.getXMLMarkup(), new PolicySaveHandler(PolicyUI.this.loader));
		}
		else
		{
			EsoeManager.contentService.createServicePolicy(PolicyUI.this.serviceIdentifier, this.policyXMLUI
					.getXMLMarkup(), new PolicyCreationHandler());
		}
	}

	public String getPolicyID()
	{

		return this.id.getText();
	}

	public void validate(List<String> idList) throws PolicyComponentValidationException
	{

		if (!this.newPolicy)
		{
			// This shouldn't ever occur but we'll check it anyway
			String policyIDVal = this.id.getText();
			if (policyIDVal == null || policyIDVal.length() == 0)
			{
				this.showParentContent();
				this.localMessages.errorMsg("Policy ID is invalid");
				throw new PolicyComponentValidationException("Policy ID is invalid");
			}

			if (idList.contains(this.id.getText()))
			{
				this.showParentContent();
				this.localMessages.errorMsg("Policy ID is has already been used, ids must be unique");
				throw new PolicyComponentValidationException("Policy ID is has already been used, id's must be unique");
			}
			else
			{
				idList.add(this.id.getText());
			}

		}

		if (!this.desc.isValid())
		{
			this.showParentContent();
			this.localMessages.errorMsg("Policy description is invalid");
			throw new PolicyComponentValidationException("Policy description is invalid");
		}

		try
		{
			List<String> targetList = resources.getValues();
			if (targetList == null || targetList.size() == 0)
			{
				this.showParentContent();
				this.localMessages.errorMsg("Policy requires valid targets to be configured");
				throw new PolicyComponentValidationException("Policy requires valid targets to be configured");
			}
		}
		catch (InvalidContentException e)
		{
			this.showParentContent();
			this.localMessages.errorMsg("Policy requires valid targets to be configured");
			throw new PolicyComponentValidationException(e);
		}

		if (this.rules == null || this.rules.size() == 0)
		{
			this.showParentContent();
			this.localMessages.errorMsg("Policy requires at least 1 valid rule to be configured");
			throw new PolicyComponentValidationException("policy requires valid targets to be configured");
		}

		for (RuleUI rule : this.rules)
		{
			rule.validate(idList);
		}

	}

	void renderRules()
	{
		this.rulesPanel.clear();
		for (RuleUI rule : this.rules)
			this.rulesPanel.add(rule);
	}

	protected void deleteRule(RuleUI rule)
	{
		if (this.rules.contains(rule))
			this.rules.remove(rule);

		this.renderRules();
	}

	public void showParentContent()
	{

		this.content.setVisible(true);
		this.rulesPanel.setVisible(true);
	}

	public Policy extractContent() throws PolicyComponentValidationException
	{

		try
		{
			Policy policy = new Policy();
			List<Rule> children = new ArrayList<Rule>();
			for (RuleUI rule : rules)
			{
				children.add(rule.extractContent());
			}

			policy.setPolicyID(this.id.getText());
			policy.setDescription(this.desc.getText());
			policy.setRules(children);
			policy.setActivated(this.active.isActive());

			Target target = new Target();
			target.setResources(this.resources.getValues());

			if (this.actions.actionsSet())
				target.setActions(this.actions.getValues());

			policy.setTarget(target);

			return policy;
		}
		catch (InvalidContentException e)
		{
			throw new PolicyComponentValidationException(e);
		}
	}

	public void populateContent(Policy policy) throws PolicyComponentValidationException
	{
		this.newPolicy = false;
		this.id.setText(policy.getPolicyID());

		try
		{
			if (policy.isInvalid())
			{
				this.graphicalEditor.setVisible(false);
				this.xmlEditor.setVisible(true);

				PolicyUI.this.activeEditor = PolicyUI.this.xmlEditor;
				PolicyUI.this.policyXMLUI.enableXMLEditor(policy.getPolicyID());
				subBanner.setVisible(true);
				graphicalMode.setVisible(true);
				xmlMode.setVisible(false);

				EventController
						.executeEvent(new MessageEvent(
								EventConstants.userMessage,
								EsoeManagerConstants.areaID,
								MessageEvent.error,
								"Policy: "
										+ policy.getPolicyID()
										+ " contains invalid XML, disabling graphical editor, please correct XML or contact an administrator"));
			}
			else
			{
				this.desc.setText(policy.getDescription());

				if (policy.isActivated())
					this.active.setActivated();
				else
					this.active.setDeactivated();

				this.resources.clear();
				this.actions.clear();
				Target target = policy.getTarget();
				if (target != null)
				{
					for (String resource : target.getResources())
					{
						this.resources.addValue(resource);
					}
					if (target.getActions() != null)
					{
						for (String action : target.getActions())
						{
							this.actions.addValue(action);
						}
						this.actions.enableActions();
					}
				}

				this.rules.clear();
				if (policy.getRules() != null && policy.getRules().size() > 0)
				{
					for (Rule rule : policy.getRules())
					{
						RuleUI ruleUI = new RuleUI(this, false);
						ruleUI.populateContent(rule);
						this.rules.add(ruleUI);
					}
					this.renderRules();
				}
			}
		}
		catch (InvalidContentException e)
		{
			throw new PolicyComponentValidationException(e);
		}
	}

	public boolean isEditorGraphical()
	{

		if (this.activeEditor == this.graphicalEditor)
			return true;

		return false;
	}

	private class PolicyActivationHandler implements AsyncCallback
	{

		public PolicyActivationHandler()
		{

		}

		public void onFailure(Throwable caught)
		{
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
					MessageEvent.error,
					"Unable to create a new policy, please contact a system administrator. Server response: "
							+ caught.getLocalizedMessage()));
		}

		public void onSuccess(Object result)
		{
			if (PolicyUI.this.active.isActive())
			{
				PolicyUI.this.active.setDeactivated();
				EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
						MessageEvent.ok, "Policy deactivated, changes will promoted on next ESOE policy update"));
			}
			else
			{
				PolicyUI.this.active.setActivated();
				EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
						MessageEvent.ok, "Policy activated, changes will be promoted on next ESOE policy update"));
			}
		}
	}

	private class PolicySaveHandler implements AsyncCallback
	{

		public PolicySaveHandler(Loader loader)
		{

		}

		public void onFailure(Throwable caught)
		{
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
					MessageEvent.error,
					"Unable to save policy, please contact a system administrator. Server response: "
							+ caught.getLocalizedMessage()));
			EventController.executeEvent(new BaseEvent(EventConstants.savePolicyFailure));
		}

		public void onSuccess(Object result)
		{
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
					MessageEvent.ok, "Policy successfully saved, changes will be promoted on next ESOE policy update"));

			if (PolicyUI.this.id.getText() != null && PolicyUI.this.id.getText().length() > 0)
				EventController.executeEvent(new PolicySavedEvent(EventConstants.savedPolicy, PolicyUI.this.id
						.getText()));
		}
	}

	private class PolicyDeletionHandler implements AsyncCallback
	{

		public PolicyDeletionHandler()
		{

		}

		public void onFailure(Throwable caught)
		{
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
					MessageEvent.error,
					"Unable to create save policy, please contact a system administrator. Server response: "
							+ caught.getLocalizedMessage()));
			EventController.executeEvent(new BaseEvent(EventConstants.deletePolicyFailure));
		}

		public void onSuccess(Object result)
		{
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
					MessageEvent.ok, "Policy deleted, changes will be promoted on next ESOE policy update"));
			EventController.executeEvent(new PolicyDeletedEvent(EventConstants.deletedPolicy, PolicyUI.this.id
					.getText()));
		}
	}

	private class PolicyCreationHandler implements AsyncCallback
	{

		public void onFailure(Throwable caught)
		{
			PolicyUI.this.setVisible(false);
			EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
					MessageEvent.error,
					"Unable to create a new policy, please contact a system administrator. Server response: "
							+ caught.getLocalizedMessage()));
		}

		public void onSuccess(Object result)
		{
			PolicyUI.this.setVisible(false);
			String policyID = (String) result;
			EventController.executeEvent(new PolicyCreatedEvent(EventConstants.successfulPolicyCreation, policyID,
					PolicyUI.this));
		}
	}

}
