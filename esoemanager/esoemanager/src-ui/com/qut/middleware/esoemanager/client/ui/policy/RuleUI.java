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

import java.util.List;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.qut.gwtuilib.client.display.FlexibleTable;
import com.qut.gwtuilib.client.display.HiddenIntegratedTextBox;
import com.qut.gwtuilib.client.display.MessagePanel;
import com.qut.gwtuilib.client.exceptions.InvalidContentException;
import com.qut.gwtuilib.client.input.ConfirmationStyledButton;
import com.qut.gwtuilib.client.input.StyledButton;
import com.qut.middleware.esoemanager.client.CSSConstants;
import com.qut.middleware.esoemanager.client.EsoeManagerConstants;
import com.qut.middleware.esoemanager.client.exceptions.PolicyComponentValidationException;
import com.qut.middleware.esoemanager.client.rpc.bean.Rule;
import com.qut.middleware.esoemanager.client.rpc.bean.Target;
import com.qut.middleware.esoemanager.client.ui.policy.operators.ConditionUI;

public class RuleUI extends VerticalPanel implements PolicyUIComponent
{

	PolicyUI parent;
	EffectUI effect;
	ActionsUI actions;
	ResourcesUI resources;
	ConditionUI condition;

	HiddenIntegratedTextBox desc;
	HiddenIntegratedTextBox id;

	boolean newRule;

	VerticalPanel content;

	MessagePanel localMessages;

	public RuleUI(PolicyUI parent, boolean newRule)
	{
		this.parent = parent;
		this.newRule = newRule;

		createContentInterface();
	}

	public void createContentInterface()
	{
		this.addStyleName(CSSConstants.esoeManagerPolicyRule);

		this.content = new VerticalPanel();
		this.content.addStyleName(CSSConstants.esoeManagerPolicyRuleContent);

		this.id = new HiddenIntegratedTextBox(this, 1, null, "Rules are required to have an ID", "ID",
				EsoeManagerConstants.areaID);
		this.desc = new HiddenIntegratedTextBox(this, 1, null, "Rules are required to have a description",
				"Description", EsoeManagerConstants.areaID);
		StyledButton showContent = new StyledButton("application", "");
		showContent.addClickListener(new ClickListener()
		{

			public void onClick(Widget sender)
			{

				// toggle content visibility
				content.setVisible(!content.isVisible());
			}
		});

		ConfirmationStyledButton removeRule = new ConfirmationStyledButton("Really delete this rule?", "delete",
				"Delete");
		removeRule.addClickListener(new ClickListener()
		{

			public void onClick(Widget sender)
			{

				// toggle content visibility
				parent.deleteRule(RuleUI.this);
			}
		});

		HorizontalPanel banner = new HorizontalPanel();
		banner.addStyleName(CSSConstants.esoeManagerPolicyBanner);
		banner.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		HorizontalPanel bannerTitle = new HorizontalPanel();
		bannerTitle.addStyleName(CSSConstants.esoeManagerPolicyBannerTitle);
		bannerTitle.setSpacing(5);
		bannerTitle.add(showContent);
		bannerTitle.add(new Label("Rule"));
		banner.add(bannerTitle);

		banner.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		HorizontalPanel bannerButtons = new HorizontalPanel();
		bannerButtons.setSpacing(5);
		bannerButtons.add(removeRule);
		banner.add(bannerButtons);

		FlexibleTable heading = new FlexibleTable(2, 2);
		heading.nextRow();
		heading.insertWidget(this.id.getTitle());
		heading.insertWidget(this.id.getContent());
		heading.nextRow();
		heading.insertWidget(this.desc.getTitle());
		heading.insertWidget(this.desc.getContent());

		if (this.newRule)
		{
			this.id.showEditor();
			this.desc.showEditor();
		}

		this.localMessages = new MessagePanel();
		this.effect = new EffectUI();
		this.actions = new ActionsUI();
		if (this.newRule)
			this.actions.addValue();

		this.resources = new ResourcesUI();
		if (this.newRule)
			this.resources.addValue();

		this.condition = new ConditionUI(this, null, this.newRule, true);

		this.content.add(this.localMessages);
		this.content.add(this.effect);
		this.content.add(this.resources.getContent());
		this.content.add(this.actions.getContent());

		this.content.add(this.condition);

		if (!this.newRule)
			this.content.setVisible(false);

		this.add(banner);
		this.add(heading);
		this.add(this.content);
	}

	public void showParentContent()
	{

		this.content.setVisible(true);
		if (this.parent != null)
			this.parent.showParentContent();
	}

	public void validate(List<String> idList) throws PolicyComponentValidationException
	{

		if (!this.id.isValid())
		{
			this.showParentContent();
			this.localMessages.errorMsg("The rule ID must be specified");
			throw new PolicyComponentValidationException("The rule ID must be specified");
		}

		try
		{
			if (idList.contains(this.id.getText()))
			{
				this.showParentContent();
				this.localMessages.errorMsg("Rule ID is has already been used, ids must be unique");
				throw new PolicyComponentValidationException("Rule ID is has already been used, ids must be unique");
			}
			else
			{
				idList.add(this.id.getText());
			}
		}
		catch (InvalidContentException e)
		{
			this.showParentContent();
			throw new PolicyComponentValidationException(e);
		}

		if (!this.desc.isValid())
		{
			this.showParentContent();
			this.localMessages.errorMsg("The rule description must be specified");
			throw new PolicyComponentValidationException("The rule description must be specified");
		}

		if (this.actions.actionsSet())
		{
			try
			{
				List<String> actionList = this.actions.getValues();
				if (actionList == null || actionList.size() == 0)
				{
					this.showParentContent();
					this.localMessages.errorMsg("Actions are enabled for this rule but no values specified");
					throw new PolicyComponentValidationException(
							"Actions are enabled for this rule but no values specified");
				}
			}
			catch (InvalidContentException e)
			{
				this.showParentContent();
				this.localMessages.errorMsg("Actions are enabled for this rule but invalid values specified");
				throw new PolicyComponentValidationException(
						"Actions are enabled for this rule but invalid values specified");
			}
		}

		if (this.resources.resourcesSet())
		{
			try
			{
				List<String> targetList = this.resources.getValues();
				if (targetList == null || targetList.size() == 0)
				{
					this.showParentContent();
					this.localMessages.errorMsg("Targets are enabled for this rule but no values specified");
					throw new PolicyComponentValidationException(
							"Targets are enabled for this rule but no values specified");
				}
			}
			catch (InvalidContentException e)
			{
				this.showParentContent();
				this.localMessages.errorMsg("Targets are enabled for this rule but invalid values specified");
				throw new PolicyComponentValidationException(
						"Targets are enabled for this rule but invalid values specified");
			}
		}

		this.condition.validate(idList);
	}

	public com.qut.middleware.esoemanager.client.rpc.bean.Rule extractContent()
			throws PolicyComponentValidationException
	{

		try
		{
			com.qut.middleware.esoemanager.client.rpc.bean.Rule rule = new com.qut.middleware.esoemanager.client.rpc.bean.Rule();

			rule.setDescription(this.desc.getText());
			rule.setRuleID(this.id.getText());
			rule.setEffect(this.effect.getEffect());
			rule.setCondition(this.condition.extractContent());

			Target target = new Target();
			if (this.resources.resourcesSet())
				target.setResources(this.resources.getValues());
			if (this.actions.actionsSet())
				target.setActions(this.actions.getValues());

			rule.setTarget(target);

			return rule;
		}
		catch (InvalidContentException e)
		{
			throw new PolicyComponentValidationException(e);
		}
	}

	public void populateContent(Rule rule) throws PolicyComponentValidationException
	{

		try
		{
			this.id.setText(rule.getRuleID());
			this.desc.setText(rule.getDescription());
			this.effect.setEffect(rule.getEffect());
			Target target = rule.getTarget();
			if (target != null)
			{
				if (target.getResources() != null)
				{
					for (String resource : target.getResources())
					{
						this.resources.addValue(resource);
					}
					this.resources.enableResources();
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
			this.condition.populateContent(rule.getCondition());
		}
		catch (InvalidContentException e)
		{
			throw new PolicyComponentValidationException(e);
		}

	}
}
