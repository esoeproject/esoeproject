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

import java.util.List;

import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.qut.gwtuilib.client.exceptions.InvalidContentException;
import com.qut.middleware.esoemanager.client.CSSConstants;
import com.qut.middleware.esoemanager.client.PolicyConstants;
import com.qut.middleware.esoemanager.client.exceptions.PolicyComponentValidationException;
import com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Condition;
import com.qut.middleware.esoemanager.client.ui.policy.OperatorChooserUI;
import com.qut.middleware.esoemanager.client.ui.policy.RuleUI;

public class ConditionUI extends OperatorUI
{
	OperatorUI child;
	RuleUI rule;
	
	Label conditionApplied;
	
	HorizontalPanel childAddition;
	OperatorChooserUI operatorChooser;

	public ConditionUI(RuleUI rule, OperatorUI parent, boolean newOperator, boolean allowsChildren)
	{
		super(parent, newOperator, allowsChildren);
		this.rule = rule;
	}

	private void init()
	{
		this.validChildOperators.add(PolicyConstants.OR);
		this.validChildOperators.add(PolicyConstants.AND);
		this.validChildOperators.add(PolicyConstants.NOT);
		this.validChildOperators.add(PolicyConstants.STRINGEQUAL);
		this.validChildOperators.add(PolicyConstants.STRINGREGEXMATCH);
		
		try
		{
			this.desc.setText("Condition");
		}
		catch (InvalidContentException e)
		{
		}
	}
	
	@Override
	public void showParentContent()
	{
		super.showParentContent();
		rule.showParentContent();
	}

	@Override
	public void addChild(OperatorUI child, String childType)
	{
		this.child = child;
		this.renderCondition();
		this.childAddition.setVisible(false);
	}

	@Override
	public void deleteChild(OperatorUI child)
	{
		this.child.clear();
		this.renderCondition();
		this.operatorChooser.setVisible(true);
		this.childAddition.setVisible(true);
	}

	@Override
	public void createLocalInterface()
	{
		init();
		
		/* Core rule does not require a banner */
		this.banner.setVisible(false);
		this.addStyleName(CSSConstants.esoeManagerPolicyOperatorCore);
		this.childAddition = new HorizontalPanel();
		this.childAddition.setSpacing(5);
		this.childAddition.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		
		if(newOperator)
			this.conditionApplied = new Label("Condition to evaluate for this rule");
		else
			this.conditionApplied = new Label("");
		
		this.add(this.conditionApplied);

		Label conditionBase = new Label("Condition base");
		this.operatorChooser = new OperatorChooserUI(this, this.validChildOperators);
		this.operatorChooser.setVisible(true);
		
		this.childAddition.add(conditionBase);
		this.childAddition.add(this.operatorChooser);
		
		this.add(this.childAddition);
	}

	protected void renderCondition()
	{
		this.localContent.clear();
		if (this.child != null)
			this.localContent.add(this.child);
	}

	@Override
	public void validate(List<String> idList) throws PolicyComponentValidationException
	{
		if (this.child == null)
		{
			this.showParentContent();
			this.localMessages.errorMsg("Rule must have a valid condition to evaluate");
			throw new PolicyComponentValidationException("Rule must have a condition to evaluate");
		}

		this.child.validate(idList);
	}

	public Condition extractContent() throws PolicyComponentValidationException
	{
		if (this.child == null)
		{
			this.showParentContent();
			this.localMessages.errorMsg("Rule must have a valid condition to evaluate");
			throw new PolicyComponentValidationException("Rule must have a condition to evaluate");
		}

		Condition condition = new Condition();
		condition.setChild(this.child.extractContent());
		
		return condition;
	}
	
	public void populateContent(Condition condition) throws PolicyComponentValidationException
	{		
		if (condition != null && condition.getChild() != null)
		{
			this.conditionApplied.setText("For the effect of this rule to be applied the following condition must evaluate to true");
			OperatorUI childUI = determineOperatorUI(condition.getChild());
			childUI.populateContent(condition.getChild());
			this.child = childUI;
			this.childAddition.setVisible(false);
			this.content.setVisible(true);
			this.renderCondition();
		}
		else
		{
			this.conditionApplied.setText("This rule has no conditional statements, its effect is ALWAYS applied");
		}
	}
}
