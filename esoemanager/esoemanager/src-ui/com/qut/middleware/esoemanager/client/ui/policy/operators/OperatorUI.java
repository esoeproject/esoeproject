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
package com.qut.middleware.esoemanager.client.ui.policy.operators;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.qut.gwtuilib.client.display.HiddenIntegratedTextBox;
import com.qut.gwtuilib.client.display.MessagePanel;
import com.qut.gwtuilib.client.eventdriven.eventmgr.BaseEvent;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventController;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventListener;
import com.qut.gwtuilib.client.exceptions.InvalidContentException;
import com.qut.gwtuilib.client.input.ConfirmationStyledButton;
import com.qut.gwtuilib.client.input.StyledButton;
import com.qut.middleware.esoemanager.client.CSSConstants;
import com.qut.middleware.esoemanager.client.EsoeManagerConstants;
import com.qut.middleware.esoemanager.client.events.EventConstants;
import com.qut.middleware.esoemanager.client.exceptions.PolicyComponentValidationException;
import com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.And;
import com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Not;
import com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Operator;
import com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Or;
import com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.StringEqual;
import com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.StringLowerCase;
import com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.StringNormalizeSpace;
import com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.StringRegexMatch;
import com.qut.middleware.esoemanager.client.ui.policy.OperatorChooserUI;
import com.qut.middleware.esoemanager.client.ui.policy.PolicyUIComponent;

public abstract class OperatorUI extends VerticalPanel implements EventListener, PolicyUIComponent
{
	/* All events this class will respond to */
	private String[] registeredEvents = { EventConstants.integratedTextBoxUpdated };

	OperatorUI parent;
	HiddenIntegratedTextBox desc;
	List<OperatorUI> children;
	List<String> validChildOperators;
	OperatorChooserUI operatorChooser;

	boolean newOperator;
	boolean allowsChildren;

	HorizontalPanel banner;
	VerticalPanel content;
	VerticalPanel localContent;
	MessagePanel localMessages;

	public OperatorUI(OperatorUI parent, boolean newOperator, boolean allowsChildren)
	{
		this.parent = parent;
		this.newOperator = newOperator;
		this.allowsChildren = allowsChildren;

		this.children = new ArrayList<OperatorUI>();
		this.validChildOperators = new ArrayList<String>();

		this.createInterface();

		EventController.registerListener(this);
	}

	private void createInterface()
	{
		this.clear();

		this.addStyleName(CSSConstants.esoeManagerPolicyOperator);

		this.banner = new HorizontalPanel();
		this.banner.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		banner.addStyleName(CSSConstants.esoeManagerPolicyOperatorHeading);

		this.content = new VerticalPanel();
		this.content.addStyleName(CSSConstants.esoeManagerPolicyOperatorContent);

		this.localContent = new VerticalPanel();

		if (this.allowsChildren)
		{
			HorizontalPanel bannerLeft = new HorizontalPanel();
			
			this.banner.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
			this.banner.add(bannerLeft);

			this.desc = new HiddenIntegratedTextBox(this, 1, null, "You must provide a description",
					"Description", EsoeManagerConstants.areaID);
			this.desc.getContent().addStyleName(CSSConstants.esoeManagerPolicyOperatorHeadingDescription);
			if (this.newOperator)
				this.desc.showEditor();

			if (!this.newOperator)
				this.content.setVisible(false);

			StyledButton showContent = new StyledButton("application", "");
			
			showContent.addClickListener(new ClickListener()
			{
				public void onClick(Widget sender)
				{
					// toggle content visibility
					OperatorUI.this.content.setVisible(!OperatorUI.this.content.isVisible());

					// If we just minimised this content close all it's children as well
					if (!OperatorUI.this.content.isVisible())
						hideAllSubContent();
				}
			});

			StyledButton showSubContent = new StyledButton("cascade", "");
			showSubContent.addClickListener(new ClickListener()
			{
				public void onClick(Widget sender)
				{
					showAllSubContent();
				}
			});
			
			bannerLeft.add(showContent);
			bannerLeft.add(showSubContent);
			bannerLeft.add(this.desc.getContent());

			HorizontalPanel bannerRight = new HorizontalPanel();
			this.banner.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			this.banner.add(bannerRight);
			
			this.operatorChooser = new OperatorChooserUI(this, this.validChildOperators);
			this.operatorChooser.setVisible(false);

			StyledButton add = new StyledButton("add", "");
			add.addClickListener(new ClickListener()
			{
				public void onClick(Widget sender)
				{
					OperatorUI.this.content.setVisible(true);
					OperatorUI.this.operatorChooser.setVisible(true);
				}
			});

			bannerRight.add(add);

			if (this.parent != null)
			{
				ConfirmationStyledButton delete = new ConfirmationStyledButton(
						"Delete this operator and all children?", "delete", "");
				delete.addClickListener(new ClickListener()
				{
					public void onClick(Widget sender)
					{
						OperatorUI.this.parent.deleteChild(OperatorUI.this);
					}
				});

				bannerRight.add(delete);
			}			
		}

		createLocalInterface();
		this.localMessages = new MessagePanel();

		this.content.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		if(this.allowsChildren)
			this.content.add(this.operatorChooser);
		this.content.add(this.localMessages);
		
		this.content.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		this.content.add(this.localContent);

		this.add(this.banner);
		this.add(this.content);
	}

	public void showParentContent()
	{
		this.content.setVisible(true);
		if (this.parent != null)
			this.parent.showParentContent();
	}

	public void hideAllSubContent()
	{
		this.content.setVisible(false);

		for (OperatorUI child : this.children)
			child.hideAllSubContent();
	}

	protected void showAllSubContent()
	{
		this.content.setVisible(true);

		for (OperatorUI child : this.children)
			child.showAllSubContent();
	}

	public void validate(List<String> idList) throws PolicyComponentValidationException
	{
		if (this.allowsChildren)
		{
			if (!this.desc.isValid())
			{
				this.showParentContent();
				this.localMessages.errorMsg("Operators must have a valid description");
				throw new PolicyComponentValidationException("Operators must have a valid description");
			}

			if (this.children != null)
			{
				for (OperatorUI child : this.children)
				{
					child.validate(idList);
				}
			}
		}
	}

	public abstract void createLocalInterface();

	public abstract void addChild(OperatorUI child, String childType);

	public abstract void deleteChild(OperatorUI child);

	public com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Operator extractContent()
			throws PolicyComponentValidationException
	{
		com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Operator operator = determineOperator();

		if (operator != null && this.allowsChildren)
		{
			try
			{
				operator.setDescription(this.desc.getText());
				List<com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Operator> children = new ArrayList<com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Operator>();
				for (OperatorUI child : this.children)
				{
					children.add(child.extractContent());
				}

				operator.setChildren(children);
			}
			catch (InvalidContentException e)
			{
				this.showParentContent();
				this.localMessages.errorMsg("Rule must have a valid condition to evaluate");
				throw new PolicyComponentValidationException("Rule must have a condition to evaluate");
			}
		}

		return operator;
	}

	public void populateContent(Operator operator) throws PolicyComponentValidationException
	{
		try
		{
			if (operator.getDescription() != null)
				this.desc.setText(operator.getDescription());
			if (operator.getChildren() != null)
			{
				for (Operator child : operator.getChildren())
				{
					OperatorUI childUI = determineOperatorUI(child);
					childUI.populateContent(child);
					this.children.add(childUI);
				}
			}
			if (this.operatorChooser != null)
				this.operatorChooser.setVisible(false);
			
			this.createLocalInterface();
		}
		catch (InvalidContentException e)
		{
			throw new PolicyComponentValidationException(e);
		}
	}

	protected OperatorUI determineOperatorUI(Operator operator)
	{
		if (operator instanceof Or)
			return new OrUI(this, false);
		if (operator instanceof And)
			return new AndUI(this, false);
		if (operator instanceof Not)
			return new NotUI(this, false);

		if (operator instanceof StringEqual)
			return new StringEqualsUI(this, false);
		if (operator instanceof StringRegexMatch)
			return new StringRegexMatchUI(this, false);

		if (operator instanceof StringLowerCase)
			return new StringNormLowerCaseUI(this, false);
		if (operator instanceof StringNormalizeSpace)
			return new StringNormSpaceUI(this, false);

		return null;
	}

	protected Operator determineOperator()
	{
		if (this instanceof OrUI)
			return new Or();
		if (this instanceof AndUI)
			return new And();
		if (this instanceof NotUI)
			return new Not();

		if (this instanceof StringEqualsUI)
			return new StringEqual();
		if (this instanceof StringRegexMatchUI)
			return new StringRegexMatch();

		if (this instanceof StringNormSpaceUI)
			return new StringNormalizeSpace();
		if (this instanceof StringNormLowerCaseUI)
			return new StringLowerCase();

		return null;
	}

	public void setDescription(String description)
	{
		try
		{
			this.desc.setText(description);
		}
		catch (InvalidContentException e)
		{
		}
	}

	public String getDescription() throws InvalidContentException
	{
		return this.desc.getText();
	}

	public void executeEvent(BaseEvent event)
	{

	}

	public String[] getRegisteredEvents()
	{
		return this.registeredEvents;
	}

}
